package com.github.rmannibucau.cdi.test.configuration;

import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import com.github.rmannibucau.cdi.configuration.xml.handlers.NamespaceHandler;
import com.github.rmannibucau.cdi.configuration.xml.handlers.NamespaceHandlerSupport;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.Attributes;

import javax.inject.Inject;
import javax.inject.Named;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class CustomHandlerTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWraps.base(CustomHandlerTest.class)
                    .addClasses(SingletonBean.class, CustomHandler.class)
                    .addAsServiceProvider(NamespaceHandler.class, CustomHandler.class);
    }

    @Inject
    @Named("singleton")
    private SingletonBean bean;

    @Test
    public void inline() {
        assertNotNull(bean);
        assertNotNull("custom-handler", bean.getValue());
    }

    public static class SingletonBean {
        private String value;

        public String getValue() {
            return value;
        }
    }

    public static class CustomHandler extends NamespaceHandlerSupport {
        @Override
        public String supportedUri() {
            return "custom";
        }

        @Override
        public ConfigBean createBean(final String localName, final Attributes attributes) {
            final ConfigBean configBean = new ConfigBean("singleton", SingletonBean.class.getName(), "application");
            configBean.getDirectAttributes().put("value", attributes.getValue("default"));
            return configBean;
        }
    }
}
