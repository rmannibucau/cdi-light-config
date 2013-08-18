package com.github.rmannibucau.cdi.configuration.qualifier;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.Set;

public class NamedQualifier extends AnnotationLiteral<Named> implements Named {
    private final String name;

    public NamedQualifier(final String name) {
        this.name = name;
    }

    @Override
    public String value() {
        return name;
    }
}
