package com.github.rmannibucau.cdi.test.configuration;

import com.github.rmannibucau.cdi.configuration.LightConfigurationExtension;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.ziplock.JarLocation.jarLocation;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class MapListArrayConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "default-config.war")
                    // extension
                    .addPackages(true, LightConfigurationExtension.class.getPackage().getName())
                    .addAsServiceProvider(Extension.class, LightConfigurationExtension.class)
                    // test beans/config
                    .addClasses(ListBean.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsResource(new ClassLoaderAsset(MapListArrayConfigurationTest.class.getSimpleName() + ".xml"), "cdi-configuration.xml")
                    // dependencies
                    .addAsLibraries(jarLocation(BeanProvider.class));
    }

    @Inject
    @Named("array")
    private ArrayBean array;

    @Inject
    @Named("list")
    private ListBean list;

    @Inject
    @Named("set")
    private SetBean set;

    @Inject
    @Named("map")
    private MapBean map;

    @Test
    public void array() {
        assertNotNull(array);
        assertEquals(3, array.array().length);
        for (int i = 0; i < array.array().length; i++) {
            assertEquals(i + 1, array.array()[i].intValue());
        }
    }

    @Test
    public void list() {
        assertNotNull(list);
        assertEquals(3, list.list().size());
        for (int i = 0; i < list.list().size(); i++) {
            assertEquals(i + 1, list.list().get(i).intValue());
        }
    }

    @Test
    public void set() {
        assertNotNull(set);
        assertEquals(3, set.set().size());
        for (int i = 0; i < set.set().size(); i++) {
            assertTrue(set.set().contains(i + 1));
        }
    }

    @Test
    public void map() {
        assertNotNull(map);
        assertEquals(3, map.map().size());
        for (final Map.Entry<Integer, Long> entry : map.map().entrySet()) {
            assertThat(entry.getKey(), instanceOf(Integer.class));
            assertThat(entry.getValue(), instanceOf(Long.class));
        }
        for (int i = 0; i < map.map().size(); i++) {
            assertEquals(Integer.valueOf(i + 1).longValue(), map.map().get(i + 1).longValue());
        }
    }

    public static class ArrayBean {
        private Integer[] array;

        public Integer[] array() {
            return array;
        }
    }

    public static class ListBean {
        private List<Integer> list;

        public List<Integer> list() {
            return list;
        }
    }

    public static class SetBean {
        private Set<Integer> set;

        public Set<Integer> set() {
            return set;
        }
    }

    public static class MapBean {
        private Map<Integer, Long> map;

        public Map<Integer, Long> map() {
            return map;
        }
    }
}
