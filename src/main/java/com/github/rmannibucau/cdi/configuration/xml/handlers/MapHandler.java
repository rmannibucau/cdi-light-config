package com.github.rmannibucau.cdi.configuration.xml.handlers;

import com.github.rmannibucau.cdi.configuration.factory.SetterFallback;
import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.github.rmannibucau.cdi.configuration.factory.Converter.convertTo;
import static com.github.rmannibucau.cdi.configuration.qualifier.Qualifiers.selectQualifier;
import static com.github.rmannibucau.cdi.configuration.scope.Scopes.toScopeClass;

public class MapHandler extends CollectionHandler {
    protected static final String KEY_PREFIX = "key-";
    protected static final String VALUE_PREFIX = "value-";

    @Override
    public String supportedUri() {
        return "map";
    }

    @Override
    public ConfigBean createBean(final String localName, final Attributes attributes) {
        final ConfigBean bean = new ConfigBean(localName, Map.class.getName(), attributes.getValue("scope"), attributes.getValue("qualifier"), MapFactory.class.getName(), "create", false);
        for (int i = 0; i < attributes.getLength(); i++) {
            final String name = attributes.getLocalName(i);
            if (!name.endsWith("-type")) {
                bean.getDirectAttributes().put(name, attributes.getValue(i));
            }
        }

        final String keyType = parameterType(attributes.getValue("key-value"));
        final String valueType = parameterType(attributes.getValue("value-type"));
        bean.getDirectAttributes().put("keyType", keyType);
        bean.getDirectAttributes().put("valueType", valueType);
        bean.getTypeParameters().add(keyType);
        bean.getTypeParameters().add(valueType);

        return bean;
    }

    public static class MapFactory<A, B> implements SetterFallback {
        protected Class<?> keyType;
        protected Class<?> valueType;
        protected final Map<String, String> listKeys = new TreeMap<String, String>(new ItemComparator(""));
        protected final Map<String, String> listValues = new TreeMap<String, String>(new ItemComparator(""));

        public Map<A, B> create() {
            final Map<A, B> map = new HashMap<A, B>();
            for (final Map.Entry<String, String> key : listKeys.entrySet()) {
                map.put((A) convertTo(keyType, key.getValue()), (B) convertTo(valueType, listValues.get(key.getKey())));
            }
            return map;
        }

        @Override
        public void set(final String key, final String value) {
            if (key.startsWith(KEY_PREFIX)) {
                listKeys.put(key.substring(KEY_PREFIX.length()), value);
            } else if (key.startsWith(VALUE_PREFIX)) {
                listValues.put(key.substring(VALUE_PREFIX.length()), value);
            } else {
                throw new IllegalArgumentException("Key " + key + " unknown");
            }
        }
    }
}
