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
import javax.inject.Named;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.apache.ziplock.JarLocation.jarLocation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

@RunWith(Arquillian.class)
public class ConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "config.war")
                    // extension
                    .addPackages(true, LightConfigurationExtension.class.getPackage().getName())
                    .addAsServiceProvider(Extension.class, LightConfigurationExtension.class)
                    // test beans/config
                    .addClasses(ABean1.class, ABean2.class, Factory.class, ABean3.class, Simple.class, MyNamed.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsResource(new ClassLoaderAsset(ConfigurationTest.class.getSimpleName() + ".xml"), "cdi-configuration.xml")
                    // dependencies
                    .addAsLibraries(jarLocation(BeanProvider.class));
    }

    @Inject
    @Named("foo")
    private ABean1 bean1;

    @Inject
    @Named("bar")
    private ABean2 bean2;

    @Inject
    @MyNamed("qualified")
    private ABean2 qualified;

    @Inject
    @Simple
    private ABean2 simple;

    @Inject
    private ABean3 fromFactory;

    @Inject
    @Named("constructor")
    private ABean4 constructor;

    @Test
    public void constructor() {
        assertNotNull(constructor);
        assertNotNull("constructor1", constructor.getValue());
    }

    @Test
    public void factory() {
        assertNotNull(fromFactory);
        assertNotNull("factory", fromFactory.name());
    }

    @Test
    public void customQualifiers() {
        assertNotNull(qualified); // with name as param
        assertNotNull(simple); // with no param
    }

    @Test
    public void setter() {
        assertNotNull(bean2);
        assertEquals("bean2", bean2.getValue());
    }

    @Test
    public void beanWithReference() {
        assertNotNull(bean1);
        assertEquals("bean1", bean1.getAttributeBean1());
        assertEquals("bean2", bean1.getBean2().getValue());
    }

    @Test
    public void dependentScope() {
        assertNotSame(BeanProvider.getContextualReference("foo"), BeanProvider.getContextualReference("foo"));
    }

    @Test
    public void applicationScope() {
        assertSame(BeanProvider.getContextualReference("bar"), BeanProvider.getContextualReference("bar"));
    }

    public static class ABean1 {
        private String attributeBean1;
        private ABean2 bean2;

        public String getAttributeBean1() {
            return attributeBean1;
        }

        public ABean2 getBean2() {
            return bean2;
        }
    }

    public static class ABean2 {
        private String value;

        public void setAttributeBean2(final String attributeBean2) {
            this.value = attributeBean2; // setX with x not being the name of the field
        }

        public String getValue() {
            return value;
        }
    }

    public static class ABean4 {
        private final String value;

        public ABean4(final String value, final int id) {
            this.value = value + id;
        }

        public String getValue() {
            return value;
        }
    }

    public static interface ABean3 {
        String name();
    }

    public static class Factory {
        private String name;

        public ABean3 create() {
            return new PrivateABean3(name);
        }

        private static class PrivateABean3 implements ABean3 {
            private final String name;

            public PrivateABean3(final String name) {
                this.name = name;
            }

            @Override
            public String name() {
                return name;
            }
        }
    }

    @Qualifier
    @Documented
    @Retention(value= RetentionPolicy.RUNTIME)
    public static @interface MyNamed {
        String value() default "";
    }

    @Qualifier
    @Documented
    @Retention(value= RetentionPolicy.RUNTIME)
    public static @interface Simple {
    }
}
