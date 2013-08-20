package com.github.rmannibucau.cdi.configuration.xml.handlers;

import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import org.xml.sax.Attributes;

public abstract class NamespaceHandlerSupport implements NamespaceHandler {
    @Override
    public void decorate(final ConfigBean bean, final String localName, final String value) {
        throw new UnsupportedOperationException("this handler doesn't support bean tag decoration");
    }

    @Override
    public ConfigBean createBean(final String localName, final Attributes attributes) {
        throw new UnsupportedOperationException("this handler doesn't support bean tag");
    }

    protected static String scope(final String scope) {
        if (scope == null) {
            return "dependent";
        }
        return scope;
    }
}
