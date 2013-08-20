package com.github.rmannibucau.cdi.configuration.xml.handlers;

import com.github.rmannibucau.cdi.configuration.ConfigurationException;
import com.github.rmannibucau.cdi.configuration.factory.SetterFallback;
import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import org.xml.sax.Attributes;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class LookupHandler extends NamespaceHandlerSupport {
    @Override
    public String supportedUri() {
        return "lookup";
    }

    @Override
    public ConfigBean createBean(final String localName, final Attributes attributes) {
        final ConfigBean bean = new ConfigBean(localName, attributes.getValue("type"),
                                                attributes.getValue("scope"), attributes.getValue("qualifier"),
                                                LookupFactory.class.getName(), "create", null, null, false);

        for (int i = 0; i < attributes.getLength(); i++) {
            bean.getDirectAttributes().put(attributes.getLocalName(i), attributes.getValue(i));
        }

        return bean;
    }

    public static class LookupFactory implements SetterFallback {
        private final Properties properties = new Properties();
        private String jndi;

        public Object create() {
            try {
                return context().lookup(jndi);
            } catch (final NamingException e) {
                throw new ConfigurationException(e);
            }
        }

        private InitialContext context() throws NamingException {
            if (properties.isEmpty()) {
                return new InitialContext();
            }
            return new InitialContext(properties);
        }

        @Override
        public void set(final String key, final String value) {
            properties.setProperty(key, value);
        }
    }
}
