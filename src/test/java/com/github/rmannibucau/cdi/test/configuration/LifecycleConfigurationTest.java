package com.github.rmannibucau.cdi.test.configuration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class LifecycleConfigurationTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWraps.base(LifecycleConfigurationTest.class).addClasses(Lifecycle.class, LifecycleFactory.class);
    }

    @Inject
    private BeanManager bm;

    @Test
    public void checkLifecycle() {
        doCheckLifecycle("lifecycle");
    }

    @Test
    public void checkLifecycleWithFactory() {
        doCheckLifecycle("lifecycleFactory");
    }

    private void doCheckLifecycle(final String name) {
        final Bean<Lifecycle> bean = Bean.class.cast(bm.resolve(bm.getBeans(name)));
        final CreationalContext<Lifecycle> cc = bm.createCreationalContext(null);
        final Lifecycle l = Lifecycle.class.cast(bm.getReference(bean, Lifecycle.class, cc));
        assertEquals(1, l.getState());
        bean.destroy(l, cc);
        assertEquals(2, l.getState());
    }

    public static class Lifecycle {
        private int state = 0;

        public int getState() {
            return state;
        }

        public void init() {
            state++;
        }

        public void destroy() {
            state++;
        }
    }

    public static class LifecycleFactory {
        private Lifecycle instance;

        public void initFactory() {
            instance = new Lifecycle();
            instance.state++;
        }

        public void destroyFactory() {
            instance.state++;
        }

        public Lifecycle create() {
            return instance;
        }
    }
}
