package com.github.rmannibucau.cdi.configuration.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeImpl implements ParameterizedType {
    private final Type raw;
    private final Type[] arguments;

    public ParameterizedTypeImpl(final Type base, final Type[] params) {
        this.raw = base;
        this.arguments = params;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return arguments;
    }

    @Override
    public Type getRawType() {
        return raw;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}
