package com.github.rmannibucau.cdi.test.configuration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class ArrayConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWraps.base(ArrayConfigurationTest.class);
    }

    @Inject
    @Named("empty")
    private String[] empty;

    @Inject
    @Named("three")
    private Integer[] three;

    @Test
    public void empty() {
        assertNotNull(empty);
        assertEquals(0, empty.length);
    }

    @Test
    public void three() {
        assertNotNull(three);
        assertEquals(3, three.length);
        for (int i = 0; i < three.length; i++) {
            assertEquals(i * 2, three[i].intValue());
        }
    }
}
