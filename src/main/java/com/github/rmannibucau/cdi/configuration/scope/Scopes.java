package com.github.rmannibucau.cdi.configuration.scope;

import com.github.rmannibucau.cdi.configuration.ConfigurationException;
import com.github.rmannibucau.cdi.loader.ClassLoaders;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import java.lang.annotation.Annotation;

public abstract class Scopes {
    public static Class<? extends Annotation> toScope(final String scope) {
        try {
            return (Class<? extends Annotation>) ClassLoaders.tccl().loadClass(toScopeClass(scope));
        } catch (final Exception e) {
            throw new ConfigurationException("Unknown scope: " + scope);
        }
    }

    public static String toScopeClass(final String scope) {
        if (scope == null || "dependent".equalsIgnoreCase(scope) || Dependent.class.getName().equals(scope)) {
            return Dependent.class.getName();
        }
        if ("application".equalsIgnoreCase(scope) || ApplicationScoped.class.getName().equals(scope)) {
            return ApplicationScoped.class.getName();
        }
        if ("session".equalsIgnoreCase(scope) || SessionScoped.class.getName().equals(scope)) {
            return SessionScoped.class.getName();
        }
        if ("request".equalsIgnoreCase(scope) || RequestScoped.class.getName().equals(scope)) {
            return RequestScoped.class.getName();
        }
        return scope;
    }
}
