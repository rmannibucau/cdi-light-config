package com.github.rmannibucau.cdi.test.configuration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class InlineConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWraps.base("inline-config.war")
                    .addClasses(Inline.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsResource(new ClassLoaderAsset("test/" + InlineConfigurationTest.class.getSimpleName() + ".xml"), "cdi-configuration.xml");
    }

    @Inject
    @Named("inline")
    private Inline bean;

    @Test
    public void inline() {
        assertNotNull(bean);
        assertNotNull("inline", bean.getValue());
    }

    public static class Inline {
        private String value;

        public String getValue() {
            return value;
        }
    }
}
