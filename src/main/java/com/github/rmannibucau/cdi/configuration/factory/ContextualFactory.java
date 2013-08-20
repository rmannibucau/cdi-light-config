package com.github.rmannibucau.cdi.configuration.factory;

import com.github.rmannibucau.cdi.configuration.ConfigurationException;
import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ContextualFactory<T> implements ContextualLifecycle<T> {
    private static final boolean HOOK_ACTIVATED = "true".equalsIgnoreCase(ConfigResolver.getPropertyValue("cdi.config.hooks", "false"));

    private final ObjectFactory<T> delegate;
    private final Method postConstruct;
    private final Method preDestroy;

    public ContextualFactory(final ConfigBean bean) {
        this.delegate = new ObjectFactory<T>(bean);
        if (HOOK_ACTIVATED) {
            this.postConstruct = find(PostConstruct.class, delegate.targetClass());
            this.preDestroy = find(PreDestroy.class, delegate.targetClass());
        } else {
            this.postConstruct = null;
            this.preDestroy = null;
        }
    }

    @Override
    public T create(final Bean<T> bean, final CreationalContext<T> creationalContext) {
        final T instance = delegate.create();
        if (postConstruct != null) {
            try {
                postConstruct.invoke(instance);
            } catch (final Exception e) {
                throw new ConfigurationException(e);
            }
        }
        return instance;
    }

    @Override
    public void destroy(final Bean<T> bean, final T instance, final CreationalContext<T> creationalContext) {
        if (preDestroy != null) {
            try {
                preDestroy.invoke(instance);
            } catch (final Exception e) {
                throw new ConfigurationException(e);
            }
        }
    }

    private static Method find(final Class<? extends Annotation> annotation, final Class<?> clazz) {
        final Class<?> current = clazz;
        while (clazz != null && !Object.class.equals(clazz)) {
            for (final Method m : current.getDeclaredMethods()) {
                if (m.getAnnotation(annotation) != null) {
                    return m;
                }
            }
        }
        return null;
    }
}
