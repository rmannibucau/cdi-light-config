package com.github.rmannibucau.cdi.configuration.xml;

import com.github.rmannibucau.cdi.configuration.model.ConfigBean;

import java.util.Collection;

public interface NamespaceHandler<T> {
    /**
     * @param uri current namespace
     * @return true if this handler supports uri
     */
    boolean support(String uri);

    /**
     * @param currentBean the bean being built (used for inline namespaces like properties for instance)
     * @param localName the localName
     * @param value dependent on the handler, can be an attribute value (String) or Attributes
     * @return the list of beans to add or null if this handler just modify currentBean
     */
    Collection<ConfigBean> parse(ConfigBean currentBean, String localName, T value);
}
