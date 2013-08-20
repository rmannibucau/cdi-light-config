package com.github.rmannibucau.cdi.configuration.xml.handlers;

import com.github.rmannibucau.cdi.configuration.ConfigurationException;
import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import com.github.rmannibucau.cdi.loader.ClassLoaders;
import org.xml.sax.Attributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PropertiesHandler extends NamespaceHandlerSupport {
    @Override
    public String supportedUri() {
        return "properties";
    }

    @Override
    public ConfigBean createBean(final String localName, final Attributes attributes) {
        final ConfigBean bean = new ConfigBean(localName, Properties.class.getName(), "dependent", attributes.getValue("qualifier"),
                                                PropertiesFactory.class.getName(), "create", false);
        bean.getDirectAttributes().put("path", attributes.getValue("path"));
        bean.getDirectAttributes().put("cached", Boolean.toString("true".equalsIgnoreCase(attributes.getValue("cached"))));
        return bean;
    }

    public static class PropertiesFactory {
        // because it can only be @Dependent we cache it to not read it each time is asked
        private static final ConcurrentMap<String, Properties> CACHE = new ConcurrentHashMap<String, Properties>();

        private boolean cached = false;
        private String path;

        public Properties create() {
            Properties props = null;
            if (cached) {
                props = CACHE.get(path);
            }

            if (props == null) {
                props = new Properties();
                try {
                    props.load(findInputStream());
                } catch (final IOException e) {
                    throw new ConfigurationException(e);
                }
                if (cached) {
                    final Properties old = CACHE.putIfAbsent(path, props);
                    if (old != null) {
                        return old;
                    }
                    return props;
                }
            }
            return props;
        }

        private InputStream findInputStream() throws FileNotFoundException {
            final File f = new File(path);
            if (f.exists()) {
                return new FileInputStream(f);
            }
            return ClassLoaders.tccl().getResourceAsStream(path);
        }
    }
}
