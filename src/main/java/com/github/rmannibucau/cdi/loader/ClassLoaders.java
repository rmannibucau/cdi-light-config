package com.github.rmannibucau.cdi.loader;

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
}
