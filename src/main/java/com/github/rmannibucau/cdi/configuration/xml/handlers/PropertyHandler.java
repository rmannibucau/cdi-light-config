package com.github.rmannibucau.cdi.configuration.xml.handlers;

import com.github.rmannibucau.cdi.configuration.model.ConfigBean;

public class PropertyHandler extends NamespaceHandlerSupport {
    @Override
    public String supportedUri() {
        return "property";
    }

    @Override
    public void decorate(final ConfigBean bean, final String localName, final String value) {
        bean.getDirectAttributes().put(localName, value);
    }
}
