package com.github.rmannibucau.cdi.configuration.xml;

import com.github.rmannibucau.cdi.configuration.ConfigurationException;
import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import com.github.rmannibucau.cdi.configuration.xml.handlers.PropertyHandler;
import com.github.rmannibucau.cdi.configuration.xml.handlers.WebServiceHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

public final class ConfigParser extends DefaultHandler {
    private static final SAXParserFactory FACTORY = SAXParserFactory.newInstance();
    private static final NamespaceHandler[] HANDLERS;
    static {
        FACTORY.setNamespaceAware(true);
        FACTORY.setValidating(false);

        // default handlers
        final Collection<NamespaceHandler<?>> handlers = new ArrayList<NamespaceHandler<?>>(5);
            // defaults
        handlers.add(new PropertyHandler());
        handlers.add(new WebServiceHandler());
            // extensions
        for (final NamespaceHandler handler : ServiceLoader.load(NamespaceHandler.class)) {
            handlers.add(handler);
        }
        HANDLERS = handlers.toArray(new NamespaceHandler<?>[handlers.size()]);
    }

    private static interface Level {
        static final int ROOT = 0;
        static final int BEAN = 1;
        static final int ATTRIBUTE = 2;
        static final int REF = 3;
    }

    private Collection<ConfigBean> beans = new ArrayList<ConfigBean>();

    private int level = 0;
    private ConfigBean bean = null;
    private StringBuilder text = null;
    private String ref = null;
    private String defaultQualifier = null;
    private String defaultScope = null;

    private ConfigParser() {
        // no-op
    }

    @Override
    public void startElement(final String uri, final String localName,
                             final String qName, final Attributes attributes) throws SAXException {
        if (isDefaultNamespace(uri)) {
            if (level == Level.BEAN) {
                String scope = attributes.getValue("scope");
                if (scope == null) {
                    scope = defaultScope;
                }

                final String classname = attributes.getValue("class");
                if (classname == null) {
                    throw new ConfigurationException("class attribute can't be null");
                }

                final boolean useConstructor = "true".equalsIgnoreCase(attributes.getValue("use-constructor"));

                String qualifier = attributes.getValue("qualifier");
                if (qualifier == null) {
                    qualifier = defaultQualifier;
                }

                bean = new ConfigBean(localName, classname, scope, qualifier,
                    attributes.getValue("factory-class"), attributes.getValue("factory-method"),
                    useConstructor);

                for (int i = 0; i < attributes.getLength(); i++) {
                    final String attrUri = attributes.getURI(i);
                    if (attrUri != null && !attrUri.isEmpty()) {
                        handle(attrUri, attributes.getLocalName(i), attributes.getValue(i));
                    }
                }
            } else if (level == Level.ATTRIBUTE) {
                text = new StringBuilder();
            } else if (level == Level.ROOT) {
                defaultQualifier = attributes.getValue("default-qualifier");
                defaultScope = attributes.getValue("default-scope");
                if (defaultScope == null) {
                    defaultScope = "dependent";
                }
            }
            level++;
        } else {
            handle(uri, localName, attributes);
        }
    }

    private <T> void handle(final String uri, final String key, final T value) {
        for (final NamespaceHandler handler : HANDLERS) {
            if (handler.support(uri)) {
                final Collection<ConfigBean> toAdd = handler.parse(bean, key, value);
                if (toAdd != null) {
                    beans.addAll(toAdd);
                }
                break;
            }
        }
    }

    @Override
    public void characters(final char ch[], final int start, final int length) throws SAXException {
        if (text != null) {
            text.append(ch, start, length);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (isDefaultNamespace(uri)) {
            level--;
            if (level == Level.BEAN) {
                beans.add(bean);
                bean = null;
            } else if (level == Level.ATTRIBUTE) {
                if (ref == null) {
                    bean.getDirectAttributes().put(localName, text.toString());
                } else {
                    bean.getRefAttributes().put(localName, ref);
                }
                bean.getAttributeOrder().add(localName);

                ref = null;
                text = null;
            } else if (level == Level.REF) {
                ref = localName;
            }
        }
    }

    private boolean isDefaultNamespace(String uri) {
        return uri == null || uri.isEmpty();
    }

    public static Collection<ConfigBean> parse(final InputStream is) throws ParserConfigurationException, SAXException, IOException {
        final ConfigParser handler = new ConfigParser();
        final SAXParser parser = FACTORY.newSAXParser();
        parser.parse(is, handler);
        return handler.beans;

    }
}
