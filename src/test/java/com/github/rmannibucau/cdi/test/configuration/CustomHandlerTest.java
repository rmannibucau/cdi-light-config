package com.github.rmannibucau.cdi.test.configuration;

import com.github.rmannibucau.cdi.configuration.model.ConfigBean;
import com.github.rmannibucau.cdi.configuration.xml.NamespaceHandler;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.Attributes;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class CustomHandlerTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWraps.base("custom-handler.war")
                    .addClasses(SingletonBean.class, CustomHandler.class)
                    .addAsServiceProvider(NamespaceHandler.class, CustomHandler.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsResource(new ClassLoaderAsset("test/" + CustomHandlerTest.class.getSimpleName() + ".xml"), "cdi-configuration.xml");
    }

    @Inject
    @Named("singleton")
    private SingletonBean bean;

    @Test
    public void inline() {
        assertNotNull(bean);
        assertNotNull("custom-handler", bean.getValue());
    }

    public static class SingletonBean {
        private String value;

        public String getValue() {
            return value;
        }
    }

    public static class CustomHandler implements NamespaceHandler<Attributes> {
        @Override
        public boolean support(final String uri) {
            return "custom".equals(uri);
        }

        @Override
        public Collection<ConfigBean> parse(final ConfigBean bean, final String key, final Attributes value) {
            final ConfigBean configBean = new ConfigBean("singleton", SingletonBean.class.getName(), "application");
            configBean.getDirectAttributes().put("value", value.getValue("default"));
            return Arrays.asList(configBean);
        }
    }
}
