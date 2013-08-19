package com.github.rmannibucau.cdi.configuration.xml.handlers;

import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import com.github.rmannibucau.cdi.configuration.xml.NamespaceHandler;
import org.xml.sax.Attributes;

import java.util.Collection;

public abstract class BeanHandler implements NamespaceHandler<Attributes> {
    @Override
    public Collection<ConfigBean> parse(final ConfigBean currentBean, final String localName, final Attributes value) {
        return createBeans(localName, value);
    }

    protected abstract Collection<ConfigBean> createBeans(String localName, Attributes value);
}
