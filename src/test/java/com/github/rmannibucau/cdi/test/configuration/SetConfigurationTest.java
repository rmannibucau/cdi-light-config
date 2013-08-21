package com.github.rmannibucau.cdi.test.configuration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class SetConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWraps.base(SetConfigurationTest.class);
    }

    @Inject
    @Named("empty")
    private Set<String> empty;

    @Inject
    @Named("three")
    private Set<Integer> three;

    @Test
    public void empty() {
        assertNotNull(empty);
        assertTrue(empty.isEmpty());
    }

    @Test
    public void three() {
        assertNotNull(three);
        assertEquals(3, three.size());
        assertTrue(three.contains(0));
        assertTrue(three.contains(2));
        assertTrue(three.contains(4));
    }
}
