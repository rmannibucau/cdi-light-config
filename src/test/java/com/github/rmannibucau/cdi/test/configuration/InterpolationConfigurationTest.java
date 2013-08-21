package com.github.rmannibucau.cdi.test.configuration;

import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class InterpolationConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWraps.base(InterpolationConfigurationTest.class)
                    .addClasses(Message.class, SimpleConfigSource.class)
                    .addAsServiceProvider(ConfigSource.class, SimpleConfigSource.class);
    }

    @Inject
    @Named("msg")
    private Message message;

    @Test
    public void interpolate() {
        assertNotNull(message);
        assertEquals("cdi", message.getMessage());
    }

    public static class Message {
        private String message;

        public String getMessage() {
            return message;
        }
    }

    public static class SimpleConfigSource implements ConfigSource {
        @Override
        public int getOrdinal() {
            return 0;
        }

        @Override
        public Map<String, String> getProperties() {
            return Collections.emptyMap();
        }

        @Override
        public String getPropertyValue(final String key) {
            if ("value".equals(key)) {
                return "cdi";
            }
            return null;
        }

        @Override
        public String getConfigName() {
            return "simple";
        }

        @Override
        public boolean isScannable() {
            return false;
        }
    }
}
