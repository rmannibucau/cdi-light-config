package com.github.rmannibucau.cdi.configuration;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(final Exception e) {
        super(e);
    }

    public ConfigurationException(final String message) {
        super(message);
    }
}
