package com.github.rmannibucau.cdi.configuration.xml.handlers;

import java.util.List;

public class ListHandler extends CollectionHandler {
    @Override
    public String supportedUri() {
        return "list";
    }

    @Override
    protected String getRawBeanType(final String componentType) {
        return List.class.getName();
    }

    @Override
    protected Class<?> getFactory() {
        return ListFactory.class;
    }

    public static class ListFactory<T> extends ItemsFactory<T> {
        public List<T> create() {
            return doCreate();
        }
    }
}
