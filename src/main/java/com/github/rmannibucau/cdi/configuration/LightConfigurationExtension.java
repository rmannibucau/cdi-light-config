package com.github.rmannibucau.cdi.configuration;

import com.github.rmannibucau.cdi.configuration.factory.ContextualFactory;
import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import com.github.rmannibucau.cdi.configuration.qualifier.NamedQualifier;
import com.github.rmannibucau.cdi.configuration.xml.ConfigParser;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class LightConfigurationExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger(LightConfigurationExtension.class.getName());

    private Collection<Bean<?>> readConfigurationFile(final BeanManager bm, final URL url) throws Exception {
        final Collection<Bean<?>> beans = new ArrayList<Bean<?>>();
        final InputStream is = url.openStream();
        try {
            for (final ConfigBean bean : ConfigParser.parse(is)) {
                final Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(bean.getClassname());
                final String name = bean.getName();

                final BeanBuilder<Object> beanBuilder = new BeanBuilder<Object>(bm)
                    .passivationCapable(true)
                    .beanClass(clazz)
                    .name(name)
                    .types(clazz, Object.class)
                    .scope(toScope(bean.getScope()))
                    .beanLifecycle(new ContextualFactory<Object>(bean));

                final Annotation qualifier = toQualifier(bean.getQualifier(), name);
                if (qualifier != null) {
                    beanBuilder.qualifiers(qualifier);
                }

                beans.add(beanBuilder.create());
            }
            LOGGER.info("Read: " + url.toExternalForm());
        } finally {
            try {
                is.close();
            } catch (final IOException ioe) {
                // no-op
            }
        }
        return beans;
    }

    private Annotation toQualifier(final String qualifier, final String name) {
        if (qualifier == null || "name".equals(qualifier)) {
            return new NamedQualifier(name);
        }
        if (qualifier.isEmpty()) {
            return null;
        }

        final Map<String, String> potentialAttributes = new HashMap<String, String>();
        potentialAttributes.put("value", name);
        potentialAttributes.put("name", name);

        try {
            return AnnotationInstanceProvider.of((Class<? extends Annotation>) Thread.currentThread().getContextClassLoader().loadClass(qualifier), potentialAttributes);
        } catch (final ClassNotFoundException e) {
            // no-op
        }

        throw new ConfigurationException("Can't find qualfier '" + qualifier + "'");
    }

    private Class<? extends Annotation> toScope(final String scope) {
        if ("application".equalsIgnoreCase(scope)) {
            return ApplicationScoped.class;
        }
        if ("session".equalsIgnoreCase(scope)) {
            return SessionScoped.class;
        }
        if ("request".equalsIgnoreCase(scope)) {
            return RequestScoped.class;
        }
        if ("dependent".equalsIgnoreCase(scope) || scope == null) {
            return Dependent.class;
        }
        try {
            return (Class<? extends Annotation>) Thread.currentThread().getContextClassLoader().loadClass(scope);
        } catch (final ClassNotFoundException e) {
            // no-op
        }
        throw new ConfigurationException("Unknown scope: " + scope);
    }

    void readAllConfigurations(final @Observes AfterBeanDiscovery abd, final BeanManager bm) {
        final String configurationName = ConfigResolver.getPropertyValue(LightConfigurationExtension.class.getName() + ".path", "cdi-configuration.xml");
        try {
            final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(configurationName);
            while (resources.hasMoreElements()) {
                final URL url = resources.nextElement();
                for (final Bean<?> bean : readConfigurationFile(bm, url)) {
                    abd.addBean(bean);
                    LOGGER.finer("Added '" + bean.getName() + "'");
                }
            }
        } catch (final Exception e) {
            throw new ConfigurationException(e);
        }
    }
}
