package com.github.rmannibucau.cdi.test.configuration;

import com.github.rmannibucau.cdi.configuration.LightConfigurationExtension;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.apache.ziplock.JarLocation.jarLocation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(Arquillian.class)
public class DefaultConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "default-config.war")
                    // extension
                    .addPackages(true, LightConfigurationExtension.class.getPackage().getName())
                    .addAsServiceProvider(Extension.class, LightConfigurationExtension.class)
                    // test beans/config
                    .addClasses(Configured.class, DefaultNamed.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsResource(new ClassLoaderAsset("test/" + DefaultConfigurationTest.class.getSimpleName() + ".xml"), "cdi-configuration.xml")
                    // dependencies
                    .addAsLibraries(jarLocation(BeanProvider.class));
    }

    @Inject
    @DefaultNamed(name = "default")
    private Configured bean;

    @Test
    public void defaultApplicationScopeAndQualifier() {
        assertNotNull(bean); // means default qualifier worked
        assertSame(BeanProvider.getContextualReference("default"), bean);
        assertEquals("built", bean.getAttr());
    }

    public static class Configured {
        private final String attr;

        public Configured(final String attr) {
            this.attr = attr;
        }

        public Configured() { // otherwise not proxiable by javassist
            this(null);
        }

        public String getAttr() {
            return attr;
        }
    }

    @Qualifier
    @Documented
    @Retention(value= RetentionPolicy.RUNTIME)
    public static @interface DefaultNamed {
        String name() default "";
    }
}
