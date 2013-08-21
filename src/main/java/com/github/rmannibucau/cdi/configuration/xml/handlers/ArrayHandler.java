package com.github.rmannibucau.cdi.configuration.xml.handlers;

import com.github.rmannibucau.cdi.configuration.ConfigurationException;
import com.github.rmannibucau.cdi.configuration.loader.ClassLoaders;
import com.github.rmannibucau.cdi.configuration.model.ConfigBean;

import java.lang.reflect.Array;

public class ArrayHandler extends CollectionHandler {
    @Override
    public String supportedUri() {
        return "array";
    }

    @Override
    protected Class<?> getFactory() {
        return ArrayFactory.class;
    }

    @Override
    protected void addTypeParameters(final ConfigBean bean, final String type) {
        // no-op
    }

    @Override
    protected String getRawBeanType(final String componentType) {
        try {
            return Array.newInstance(ClassLoaders.tccl().loadClass(componentType), 0).getClass().getName();
        } catch (final ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }
    }

    public static class ArrayFactory<T> extends ItemsFactory<T> {
        public T[] create() {
            return doCreate().toArray((T[]) Array.newInstance(type, listValues.size()));
        }
    }
}
