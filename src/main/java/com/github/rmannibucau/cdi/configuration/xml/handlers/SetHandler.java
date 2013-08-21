package com.github.rmannibucau.cdi.configuration.xml.handlers;

import java.util.HashSet;
import java.util.Set;

public class SetHandler extends CollectionHandler {
    @Override
    public String supportedUri() {
        return "set";
    }

    @Override
    protected String getRawBeanType(final String componentType) {
        return Set.class.getName();
    }

    @Override
    protected Class<?> getFactory() {
        return SetFactory.class;
    }

    public static class SetFactory<T> extends ItemsFactory<T> {
        public Set<T> create() {
            return new HashSet<T>(doCreate());
        }
    }
}
