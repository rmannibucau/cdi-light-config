package com.github.rmannibucau.cdi.configuration.xml.handlers;

import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import com.github.rmannibucau.cdi.configuration.xml.NamespaceHandler;

import java.util.Collection;

public abstract class AttributeHandler implements NamespaceHandler<String> {
    @Override
    public Collection<ConfigBean> parse(final ConfigBean currentBean, final String localName, final String value) {
        parseAttribute(currentBean, localName, value);
        return null;
    }

    protected abstract void parseAttribute(ConfigBean bean, String localName, String value);
}
