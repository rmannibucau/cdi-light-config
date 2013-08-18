package com.github.rmannibucau.cdi.configuration.factory;

import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

public class ContextualFactory<T> implements ContextualLifecycle<T> {
    private final ObjectFactory<T> delegate;

    public ContextualFactory(final ConfigBean bean) {
        this.delegate = new ObjectFactory<T>(bean);
    }

    @Override
    public T create(final Bean<T> bean, final CreationalContext<T> creationalContext) {
        return delegate.create();
    }

    @Override
    public void destroy(final Bean<T> bean, final T instance, final CreationalContext<T> creationalContext) {
        // no-op
    }
}
