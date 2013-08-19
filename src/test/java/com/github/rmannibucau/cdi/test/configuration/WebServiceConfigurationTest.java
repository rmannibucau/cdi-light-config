package com.github.rmannibucau.cdi.test.configuration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jws.WebService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class WebServiceConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWraps.base("webservice-namespace.war")
                    .addClasses(WS.class, WSImpl.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsResource(new ClassLoaderAsset("test/" + WebServiceConfigurationTest.class.getSimpleName() + ".xml"), "cdi-configuration.xml");
    }

    @Inject
    @Named("myWs")
    private WS ws;

    @Test
    public void constructor() {
        assertNotNull(ws);
        assertEquals("hi", ws.hi());
    }

    @WebService
    public static interface WS {
        String hi();
    }

    @WebService @Singleton
    public static class WSImpl implements WS {
        public String hi() {
            return "hi";
        }
    }
}
