package com.github.rmannibucau.cdi.configuration;

import com.github.rmannibucau.cdi.configuration.factory.ContextualFactory;
import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import com.github.rmannibucau.cdi.configuration.xml.ConfigParser;
import com.github.rmannibucau.cdi.loader.ClassLoaders;
import com.github.rmannibucau.cdi.reflect.ParameterizedTypeImpl;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.util.bean.BeanBuilder;

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
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.logging.Logger;

import static com.github.rmannibucau.cdi.configuration.qualifier.Qualifiers.toQualifier;
import static com.github.rmannibucau.cdi.configuration.scope.Scopes.toScope;

public class LightConfigurationExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger(LightConfigurationExtension.class.getName());

    private Collection<Bean<?>> readConfigurationFile(final BeanManager bm, final URL url) throws Exception {
        final Collection<Bean<?>> beans = new ArrayList<Bean<?>>();
        final InputStream is = url.openStream();
        try {
            for (final ConfigBean bean : ConfigParser.parse(is)) {
                final ClassLoader classLoader = ClassLoaders.tccl();
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

    void readAllConfigurations(final @Observes AfterBeanDiscovery abd, final BeanManager bm) {
        final String configurationName = ConfigResolver.getPropertyValue(LightConfigurationExtension.class.getName() + ".path", "cdi-configuration.xml");
        try {
            final Enumeration<URL> resources = ClassLoaders.tccl().getResources(configurationName);
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
