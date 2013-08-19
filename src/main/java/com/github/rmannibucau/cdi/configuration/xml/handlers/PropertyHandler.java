package com.github.rmannibucau.cdi.configuration.xml.handlers;

import com.github.rmannibucau.cdi.configuration.model.ConfigBean;

public class PropertyHandler extends AttributeHandler {
    @Override
    public boolean support(final String uri) {
        return "property".equals(uri);
    }

    @Override
    protected void parseAttribute(final ConfigBean bean, final String localName, final String value) {
        bean.getDirectAttributes().put(localName, value);
    }
}
