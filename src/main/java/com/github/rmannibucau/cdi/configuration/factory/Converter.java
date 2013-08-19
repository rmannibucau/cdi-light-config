package com.github.rmannibucau.cdi.configuration.factory;

import com.github.rmannibucau.cdi.configuration.ConfigurationException;

import javax.xml.namespace.QName;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Converter {
    private Converter() {
        // no-op
    }

    public static Object convertTo(final Type type, final String value) {
        if (value == null || String.class.equals(type)) {
            return value;
        }
        if (Class.class.isInstance(type)) {
            final Class<?> rawType = Class.class.cast(type);

            if (List.class.isAssignableFrom(rawType)) {
                return Arrays.asList(toArray(String.class, value));
            }
            if (Set.class.isAssignableFrom(rawType)) {
                final Set<Object> set = new HashSet<Object>();
                set.addAll(Arrays.asList(toArray(String.class, value)));
                return set;
            }
            if (Map.class.isAssignableFrom(rawType)) {
                return toMap(value, String.class, String.class);
            }
            if (Integer.class.equals(type) || Integer.TYPE.equals(type)) {
                return Integer.parseInt(value);
            }
            if (Long.class.equals(type) || Long.TYPE.equals(type)) {
                return Long.parseLong(value);
            }
            if (Short.class.equals(type) || Short.TYPE.equals(type)) {
                return Short.parseShort(value);
            }
            if (Boolean.class.equals(type) || Boolean.TYPE.equals(type)) {
                return Boolean.parseBoolean(value);
            }
            if (Double.class.equals(type) || Double.TYPE.equals(type)) {
                return Double.parseDouble(value);
            }
            if (Float.class.equals(type) || Float.TYPE.equals(type)) {
                return Float.parseFloat(value);
            }
            if (rawType.isArray()) {
                final Class<?> componentType = rawType.getComponentType();
                return toArray(componentType, value);
            }
            if (URL.class.equals(rawType)) {
                try {
                    return new URL(value);
                } catch (final MalformedURLException e) {
                    throw new ConfigurationException(e);
                }
            }
            if (URI.class.equals(rawType)) {
                try {
                    return new URL(value).toURI();
                } catch (final Exception e) {
                    throw new ConfigurationException(e);
                }
            }
            if (QName.class.equals(rawType)) {
                final int endIdx = value.indexOf("}");
                if (value.startsWith("{") && endIdx > 0) {
                    return new QName(value.substring(1, endIdx), value.substring(endIdx + 1));
                }
                return new QName(value);
            }
            if (Class.class.equals(rawType)) {
                try {
                    return Thread.currentThread().getContextClassLoader().loadClass(value);
                } catch (final ClassNotFoundException e) {
                    throw new ConfigurationException(e);
                }
            }
        }
        if (ParameterizedType.class.isInstance(type)) {
            final ParameterizedType parameterizedType = ParameterizedType.class.cast(type);
            final Class<?> rawType = Class.class.cast(parameterizedType.getRawType());
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            final Class<?> param = (Class<?>) actualTypeArguments[0];

            if (List.class.isAssignableFrom(rawType)) {
                return Arrays.asList(toArray(param, value));
            }
            if (Set.class.isAssignableFrom(rawType)) {
                final Set<Object> set = new HashSet<Object>();
                set.addAll(Arrays.asList(toArray(param, value)));
                return set;
            }
            if (Map.class.isAssignableFrom(rawType)) {
                final Class<?> valueType = (Class<?>) actualTypeArguments[1];
                return toMap(value, param, valueType);
            }
        }

        throw new ConfigurationException("Can't convert '" + value + "' to " + type);
    }

    private static Map<?, ?> toMap(final String value, final Class<?> param, final Class<?> valueType) {
        final Map<Object, Object> map = new HashMap<Object, Object>();
        final String[] raw = value.split(",");
        for (final String aRaw : raw) {
            final String[] kv = aRaw.split("=");
            if (kv.length == 1) {
                map.put(convertTo(param, aRaw), null);
            } else {
                map.put(convertTo(param, kv[0]), convertTo(valueType, kv[1]));
            }
        }
        return map;
    }

    private static Object[] toArray(final Class<?> componentType, final String value) {
        final String[] raw = value.split(",");
        final Object array = Array.newInstance(componentType, raw.length);
        for (int i = 0; i < raw.length; i++) {
            Array.set(array, i, convertTo(componentType, raw[i]));
        }
        return Object[].class.cast(array);
    }
}
