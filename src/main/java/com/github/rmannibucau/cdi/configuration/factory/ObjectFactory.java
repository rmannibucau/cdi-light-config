package com.github.rmannibucau.cdi.configuration.factory;

import com.github.rmannibucau.cdi.configuration.ConfigurationException;
import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import com.github.rmannibucau.cdi.loader.ClassLoaders;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import static com.github.rmannibucau.cdi.configuration.factory.Converter.convertTo;

public class ObjectFactory<T> {
    private static final Logger LOGGER = Logger.getLogger(ObjectFactory.class.getName());

    private static final boolean HOOK_ACTIVATED = "true".equalsIgnoreCase(ConfigResolver.getPropertyValue("cdi.config.hooks", "false"));

    private final ConfigBean model;
    private final Factory<T> factory;
    private final Collection<Method> postConstructs = new CopyOnWriteArrayList<Method>();
    private final Collection<Method> preDestroys = new CopyOnWriteArrayList<Method>();

    public ObjectFactory(final ConfigBean bean) {
        model = bean;
        factory = findFactory();

        final Class<?> clazz = factory.beanClass();
        for (final Method m : new Method[] { find(PostConstruct.class, clazz), find(bean.getInitMethod(), clazz) }) {
            if (m != null) {
                postConstructs.add(m);
            }
        }
        for (final Method m : new Method[] { find(PreDestroy.class, clazz), find(bean.getDestroyMethod(), clazz) }) {
            if (m != null) {
                preDestroys.add(m);
            }
        }
    }

    public T create() {
        final T instance = factory.create();
        for (final Method postConstruct : postConstructs) {
            try {
                postConstruct.invoke(instance);
            } catch (final Exception e) {
                throw new ConfigurationException(e);
            }
        }
        return instance;
    }

    public void destroy(final T instance) {
        for (final Method preDestroy : preDestroys) {
            try {
                preDestroy.invoke(instance);
            } catch (final Exception e) {
                throw new ConfigurationException(e);
            }
        }
        factory.destroy(instance);
    }

    private Factory<T> findFactory() {
        try {
            if (model.getFactoryClass() == null && !model.isConstructor()) {
                return new NewFactory<T>(model);
            }
            if (model.isConstructor()) {
                return new ConstructorFactory<T>(model);
            }

            final Class<?> factoryClass = ClassLoaders.tccl().loadClass(model.getFactoryClass());
            final Method method = factoryClass.getDeclaredMethod(model.getFactoryMethod());
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return new MethodFactory<T>(model, Modifier.isStatic(method.getModifiers()), method);
        } catch (final Exception e) {
            throw new ConfigurationException(e);
        }
    }

    protected static interface Setter {
        Type type();
        void set(final Object mainInstance, final Object value) throws Exception;
    }

    protected static class FieldSetter implements Setter {
        private final Field field;

        public FieldSetter(final Field field) {
            this.field = field;
        }

        @Override
        public Type type() {
            return field.getGenericType();
        }

        @Override
        public void set(final Object mainInstance, final Object value) throws Exception {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(mainInstance, value);
        }
    }

    protected static class MethodSetter implements Setter {
        private final Method method;

        public MethodSetter(final Method method) {
            this.method = method;
        }

        @Override
        public Type type() {
            return method.getParameterTypes()[0];
        }

        @Override
        public void set(final Object mainInstance, final Object value) throws Exception {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            method.invoke(mainInstance, value);
        }
    }

    protected static interface Factory<T> {
        T create();
        void destroy(T instance);
        Class<?> beanClass();
    }

    protected static class ConstructorFactory<T> implements Factory<T> {
        private final Constructor<T> constructor;
        private final ConfigBean model;

        public ConstructorFactory(final ConfigBean bean) {
            final int paramNumber = bean.getDirectAttributes().size() + bean.getRefAttributes().size();

            final Class<?> clazz;
            try {
                clazz = ClassLoaders.tccl().loadClass(bean.getClassname());
            } catch (final ClassNotFoundException e) {
                throw new ConfigurationException(e);
            }

            Constructor<?> found = null;
            for (final Constructor<?> c : clazz.getDeclaredConstructors()) {
                if (c.getParameterTypes().length == paramNumber) {
                    found = c;
                    break;
                }
            }

            if (found == null) {
                throw new ConfigurationException("No constructor found matching configuration for " + bean.getName());
            }

            constructor = (Constructor<T>) found;
            model = bean;
        }

        @Override
        public T create() {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            final Object[] params = new Object[parameterTypes.length];
            int i = 0;
            for (final String attr : model.getAttributeOrder()) {
                params[i] = findAttribute(parameterTypes[i], attr);
                i++;
            }
            try {
                return (T) constructor.newInstance(params);
            } catch (final Exception e) {
                throw new ConfigurationException(e);
            }
        }

        @Override
        public void destroy(final T instance) {
            // no-op
        }

        @Override
        public Class<?> beanClass() {
            return constructor.getDeclaringClass();
        }

        private Object findAttribute(final Class<?> clazz, final String attr) {
            final String value = model.getDirectAttributes().get(attr);
            if (value != null) {
                return convertTo(clazz, value);
            }

            final String reference = model.getRefAttributes().get(attr);
            return BeanProvider.getContextualReference(reference);
        }
    }

    protected static class NewFactory<T> implements Factory<T> {
        private final Class<T> clazz;
        private final ConfigBean model;
        private final Map<String, Setter> members;

        public NewFactory(final ConfigBean model) {
            this.model = model;
            try {
                this.clazz = (Class<T>) ClassLoaders.tccl().loadClass(model.getClassname());
            } catch (final ClassNotFoundException e) {
                throw new ConfigurationException(e);
            }
            this.members = mapMembers(clazz);
        }

        @Override
        public T create() {
            try {
                final T t = clazz.newInstance();

                for (final Map.Entry<String, String> attribute : model.getDirectAttributes().entrySet()) {
                    final Setter field = members.get(attribute.getKey());
                    if (field != null) {
                        final Object value = convertTo(field.type(), attribute.getValue());
                        field.set(t, value);
                    } else if (SetterFallback.class.isInstance(t)) {
                        SetterFallback.class.cast(t).set(attribute.getKey(), attribute.getValue());
                    } else {
                        LOGGER.warning("Can't find field " + attribute.getKey());
                    }
                }

                for (final Map.Entry<String, String> attribute : model.getRefAttributes().entrySet()) {
                    final Setter field = members.get(attribute.getKey());
                    if (field != null) {
                        field.set(t, BeanProvider.getContextualReference(attribute.getValue()));
                    } else {
                        LOGGER.warning("Can't find field " + attribute.getKey());
                    }
                }

                return t;
            } catch (final Exception e) {
                throw new ConfigurationException(e);
            }
        }

        @Override
        public void destroy(final T instance) {
            // no-op
        }

        @Override
        public Class<?> beanClass() {
            return clazz;
        }

        private Map<String, Setter> mapMembers(final Class<T> target) {
            final Map<String, Setter> members = new HashMap<String, Setter>();
            Class<?> current = target;
            do {
                for (final Method method : current.getDeclaredMethods()) {
                    final String name = method.getName();
                    if (name.length() > 3 && name.startsWith("set") && method.getParameterTypes().length == 1) {
                        members.put(Introspector.decapitalize(name.substring(3)), new MethodSetter(method));
                    }
                }

                for (final Field field : current.getDeclaredFields()) {
                    final String name = field.getName();
                    if (members.containsKey(name)) {
                        continue;
                    }

                    members.put(name, new FieldSetter(field));
                }

                current = current.getSuperclass();
            } while (current != null && !Object.class.equals(current));
            return members;
        }
    }

    protected static class MethodFactory<T> implements Factory<T> {
        private final boolean staticFactory;
        private final Method method;
        private final ObjectFactory<Object> delegate;
        private final Map<Object, Object> factoryByInstance = new ConcurrentHashMap<Object, Object>();

        public MethodFactory(final ConfigBean mainBean, final boolean staticFactory, final Method method) {
            this.staticFactory = staticFactory;
            this.method = method;

            // reuse this as meta factory for the factory instance if needed
            if (staticFactory) {
                this.delegate = null;
            } else {
                final ConfigBean bean = new ConfigBean(null, mainBean.getFactoryClass(), null, null, null, null, mainBean.getInitMethod(), mainBean.getDestroyMethod(), false);
                bean.getDirectAttributes().putAll(mainBean.getDirectAttributes());
                bean.getRefAttributes().putAll(mainBean.getRefAttributes());
                this.delegate = new ObjectFactory<Object>(bean);
            }
        }

        @Override
        public T create() {
            try {
                if (staticFactory) {
                    return (T) method.invoke(null);
                }

                final Object factoryInstance = delegate.create();
                final T instance = (T) method.invoke(factoryInstance);
                factoryByInstance.put(instance, factoryInstance);
                return instance;
            } catch (final Exception e) {
                throw new ConfigurationException(e);
            }
        }

        @Override
        public void destroy(final T instance) {
            if (staticFactory || instance == null) {
                return;
            }

            final Object factory = factoryByInstance.remove(instance);
            if (factory != null) {
                delegate.destroy(factory);
            }
        }

        @Override
        public Class<?> beanClass() {
            return method.getReturnType();
        }
    }

    private static Method find(final Class<? extends Annotation> annotation, final Class<?> clazz) {
        if (!HOOK_ACTIVATED) {
            return null;
        }

        Class<?> current = clazz;
        while (current != null && !Object.class.equals(current)) {
            for (final Method m : current.getDeclaredMethods()) {
                if (m.getAnnotation(annotation) != null) {
                    return m;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static Method find(final String name, final Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && !Object.class.equals(current)) {
            for (final Method m : current.getDeclaredMethods()) {
                if (m.getName().equals(name)) {
                    return m;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
