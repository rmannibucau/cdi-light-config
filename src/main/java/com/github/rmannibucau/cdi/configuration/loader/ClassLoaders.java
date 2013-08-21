package com.github.rmannibucau.cdi.configuration.loader;

import com.github.rmannibucau.cdi.configuration.ConfigurationException;

public final class ClassLoaders {
    private ClassLoaders() {
        // no-op
    }

    public static ClassLoader tccl() {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader == null) {
            return ClassLoaders.class.getClassLoader();
        }
        return contextClassLoader;
    }

    public static Class<?> loadClass(final String classname) {
        try {
            return tccl().loadClass(classname);
        } catch (final ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }
    }
}
