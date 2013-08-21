package com.github.rmannibucau.cdi.configuration.qualifier;

import com.github.rmannibucau.cdi.configuration.ConfigurationException;
import com.github.rmannibucau.cdi.configuration.loader.ClassLoaders;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public abstract class Qualifiers {
    public static String selectQualifier(final String qualifier) {
        if (qualifier == null || "name".equals(qualifier)) {
            return "name";
        }
        return qualifier;
    }

    public static Annotation toQualifier(final String rawQualifier, final String name) {
        final String qualifier = selectQualifier(rawQualifier);
        if (qualifier == null || "name".equals(qualifier)) {
            return new NamedQualifier(name);
        }
        if (qualifier.isEmpty()) {
            return null;
        }

        final Map<String, String> potentialAttributes = new HashMap<String, String>();
        potentialAttributes.put("value", name);
        potentialAttributes.put("name", name);

        try {
            return AnnotationInstanceProvider.of((Class<? extends Annotation>) ClassLoaders.tccl().loadClass(qualifier), potentialAttributes);
        } catch (final ClassNotFoundException e) {
            // no-op
        }

        throw new ConfigurationException("Can't find qualfier '" + qualifier + "'");
    }
}
