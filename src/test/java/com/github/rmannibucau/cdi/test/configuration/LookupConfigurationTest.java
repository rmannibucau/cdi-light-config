package com.github.rmannibucau.cdi.test.configuration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.inject.Named;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class LookupConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWraps.base(LookupConfigurationTest.class).addClass(MyService.class);
    }

    @Inject
    @Named("service")
    private MyService service;

    @Test
    public void service() {
        assertNotNull(service);
        assertEquals("ok", service.ok());
    }

    @Singleton
    public static class MyService {
        public String ok() {
            return "ok";
        }
    }
}
