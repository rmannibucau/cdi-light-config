package com.github.rmannibucau.cdi.test.configuration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class PropertiesConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWraps.base(PropertiesConfigurationTest.class).addAsWebInfResource(new StringAsset("a=b"), "classes/ab.properties");
    }

    @Inject
    @Named("props")
    private Properties props;

    @Test
    public void service() {
        assertNotNull(props);
        assertEquals(1, props.size());
        assertEquals("b", props.getProperty("a"));
    }
}
