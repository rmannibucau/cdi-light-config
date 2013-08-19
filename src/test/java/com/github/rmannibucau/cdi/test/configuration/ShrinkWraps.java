package com.github.rmannibucau.cdi.test.configuration;

import com.github.rmannibucau.cdi.configuration.LightConfigurationExtension;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import javax.enterprise.inject.spi.Extension;

import static org.apache.ziplock.JarLocation.jarLocation;

public final class ShrinkWraps {
    private ShrinkWraps() {
        // no-op
    }

    public static WebArchive base(final String name) {
        return ShrinkWrap.create(WebArchive.class, name)
                    // extension
                    .addPackages(true, LightConfigurationExtension.class.getPackage().getName())
                    .addAsServiceProvider(Extension.class, LightConfigurationExtension.class)
                    // dependencies
                    .addAsLibraries(jarLocation(BeanProvider.class));
    }
}
