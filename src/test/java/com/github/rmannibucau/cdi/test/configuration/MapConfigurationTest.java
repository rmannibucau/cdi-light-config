package com.github.rmannibucau.cdi.test.configuration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class MapConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWraps.base(MapConfigurationTest.class);
    }

    @Inject
    @Named("empty")
    private Map<String, String> empty;

    @Inject
    @Named("three")
    private Map<String, Integer> three;

    @Test
    public void empty() {
        assertNotNull(empty);
        assertTrue(empty.isEmpty());
    }

    @Test
    public void three() {
        assertNotNull(three);
        assertEquals(3, three.size());
        for (int i = 0; i < three.size(); i++) {
            assertEquals(i * 2, three.get("" + i).intValue());
        }
    }
}
