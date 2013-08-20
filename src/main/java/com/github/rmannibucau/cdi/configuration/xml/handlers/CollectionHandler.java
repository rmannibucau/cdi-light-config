package com.github.rmannibucau.cdi.configuration.xml.handlers;

import com.github.rmannibucau.cdi.configuration.factory.SetterFallback;
import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.github.rmannibucau.cdi.configuration.factory.Converter.convertTo;

public abstract class CollectionHandler extends NamespaceHandlerSupport {
    protected static final String ITEM_PREFIX = "item-";

    @Override
    public ConfigBean createBean(final String localName, final Attributes attributes) {
        final String type = parameterType(attributes.getValue("type"));

        final ConfigBean bean = new ConfigBean(localName, getRawBeanType(type),
                                                attributes.getValue("scope"), attributes.getValue("qualifier"),
                                                getFactory().getName(), "create", false);
        for (int i = 0; i < attributes.getLength(); i++) {
            bean.getDirectAttributes().put(attributes.getLocalName(i), attributes.getValue(i));
        }

        bean.getDirectAttributes().put("type", type); // if default value force it to not be null and present
        addTypeParameters(bean, type);

        return bean;
    }

    protected void addTypeParameters(final ConfigBean bean, final String type) {
        bean.getTypeParameters().add(type);
    }

    protected String getRawBeanType(String componentType) {
        throw new UnsupportedOperationException();
    }

    protected Class<?> getFactory() {
        throw new UnsupportedOperationException();
    }

    protected static String parameterType(final String type) {
        if (type == null) {
            return String.class.getName();
        }
        return type;
    }

    protected static class ItemComparator implements Comparator<String> {
        private final String prefix;

        public ItemComparator(final String prefix) {
            this.prefix = prefix;
        }

        public ItemComparator() {
            this(ITEM_PREFIX);
        }

        @Override
        public int compare(final String o1, final String o2) {
            int idx1 = Integer.parseInt(o1.substring(prefix.length()));
            int idx2 = Integer.parseInt(o2.substring(prefix.length()));
            return idx1 - idx2;
        }
    }

    protected static class ItemsFactory<T> implements SetterFallback {
        protected Class<T> type;
        protected final Map<String, String> listValues = new TreeMap<String, String>(new ItemComparator());

        protected List<T> doCreate() {
            final List<T> list = new ArrayList<T>();
            for (final String value : listValues.values()) {
                list.add(type.cast(convertTo(type, value)));
            }
            return list;
        }

        @Override
        public void set(final String key, final String value) {
            if (key.startsWith(ITEM_PREFIX)) {
                listValues.put(key, value);
            } else {
                throw new IllegalArgumentException("Key " + key + " unknown");
            }
        }
    }
}
