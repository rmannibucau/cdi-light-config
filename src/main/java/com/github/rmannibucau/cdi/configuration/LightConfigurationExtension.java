package com.github.rmannibucau.cdi.configuration;

import com.github.rmannibucau.cdi.configuration.factory.ContextualFactory;
import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import com.github.rmannibucau.cdi.configuration.reflect.ParameterizedTypeImpl;
import com.github.rmannibucau.cdi.configuration.xml.ConfigParser;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.bean.BeanBuilder;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.github.rmannibucau.cdi.configuration.loader.ClassLoaders.tccl;
import static com.github.rmannibucau.cdi.configuration.qualifier.Qualifiers.toQualifier;
import static com.github.rmannibucau.cdi.configuration.scope.Scopes.toScope;

public class LightConfigurationExtension implements Extension, Deactivatable {
    private static final Logger LOGGER = Logger.getLogger(LightConfigurationExtension.class.getName());

    private final Map<String, ConfigBean> beans = new HashMap<String, ConfigBean>();
    private boolean activated;

    void readAllConfigurations(final @Observes BeforeBeanDiscovery bdd) {
        activated = ClassDeactivationUtils.isActivated(LightConfigurationExtension.class);
        if (!activated) {
            return;
        }

        final String configurationName = ConfigResolver.getPropertyValue(LightConfigurationExtension.class.getName() + ".path", "cdi-configuration.xml");
        try {
            final Enumeration<URL> resources = tccl().getResources(configurationName);
            while (resources.hasMoreElements()) {
                addBeansForUrl(resources.nextElement());
            }
        } catch (final Exception e) {
            throw new ConfigurationException(e);
        }
    }

    void addCdiBeans(final @Observes AfterBeanDiscovery abd, final BeanManager bm) {
        if (!activated) {
            return;
        }

        for (final ConfigBean bean : beans.values()) {
            try {
                final Bean<Object> cdiBean = createBean(bm, bean);
                abd.addBean(cdiBean);
                LOGGER.fine("Added bean " + cdiBean.getName());
            } catch (final Exception e) {
                throw new ConfigurationException(e);
            }
        }
    }

    private void addBeansForUrl(final URL url) throws Exception {
        final InputStream is = url.openStream();
        try {
            for (final ConfigBean bean : ConfigParser.parse(is)) {
                final String name = bean.getName();
                if (name != null) {
                    beans.put(name, bean);
                } else {
                    beans.put("_no_name_" + bean.hashCode(), bean);
                }
            }
            LOGGER.info("Read: " + url.toExternalForm());
        } finally {
            try {
                is.close();
            } catch (final IOException ioe) {
                // no-op
            }
        }
    }

    private static Bean<Object> createBean(final BeanManager bm, final ConfigBean bean) throws Exception {
        final ClassLoader classLoader = tccl();
        final Class<?> clazz = classLoader.loadClass(bean.getClassname());
        final String name = bean.getName();
        final Type type = findType(classLoader, clazz, bean.getTypeParameters());

        final BeanBuilder<Object> beanBuilder = new BeanBuilder<Object>(bm)
            .passivationCapable(true)
            .beanClass(clazz)
            .name(name)
            .types(type, Object.class)
            .scope(toScope(bean.getScope()))
            .beanLifecycle(new ContextualFactory<Object>(bean));

        final Annotation qualifier = toQualifier(bean.getQualifier(), name);
        if (qualifier != null) {
            beanBuilder.qualifiers(qualifier);
        }

        return beanBuilder.create();
    }

    private static Type findType(final ClassLoader classLoader, final Class<?> base, final Collection<String> typeParameters) throws Exception {
        if (typeParameters == null) {
            return base;
        }
        final Type[] params = new Type[typeParameters.size()];
        int i = 0;
        for (final String type : typeParameters) {
            params[i++] = classLoader.loadClass(type);
        }
        return new ParameterizedTypeImpl(base, params);
    }
}
