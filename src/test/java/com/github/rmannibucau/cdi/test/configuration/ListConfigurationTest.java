package com.github.rmannibucau.cdi.test.configuration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ListConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWraps.base(ListConfigurationTest.class);
    }

    @Inject
    @Named("empty")
    private List<String> empty;

    @Inject
    @Named("three")
    private List<Integer> three;

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
            assertEquals(i * 2, three.get(i).intValue());
        }
    }
}
