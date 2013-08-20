package com.github.rmannibucau.cdi.configuration.xml.handlers;

import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import org.xml.sax.Attributes;

public interface NamespaceHandler {
    /**
     * @return uri of the current namespace
     */
    String supportedUri();

    /**
     * @param bean the bean to decorate
     * @param localName the localName for the parsed attribute
     * @param value the attribute value
     */
    void decorate(ConfigBean bean, String localName, String value);

    /**
     * @param localName the localName for the bean
     * @param attributes the attributes of the bean
     * @return the bean to create
     */
    ConfigBean createBean(String localName, Attributes attributes);
}
