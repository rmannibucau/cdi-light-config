package com.github.rmannibucau.cdi.test.configuration;

import com.github.rmannibucau.cdi.configuration.LightConfigurationExtension;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import javax.enterprise.inject.spi.Extension;

import static org.apache.ziplock.JarLocation.jarLocation;

public abstract class ShrinkWraps {
    public static WebArchive base(final Class<?> name) {
        return ShrinkWrap.create(WebArchive.class, name.getSimpleName() + ".war")
                    // extension
                    .addPackages(true, LightConfigurationExtension.class.getPackage().getName())
                    .addAsServiceProvider(Extension.class, LightConfigurationExtension.class)
                    // dependencies
                    .addAsLibraries(jarLocation(BeanProvider.class))
                    // default behavior: activate CDI = add cdi-configuration.xml from test name
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                    .addAsResource(new ClassLoaderAsset("test/" + name.getSimpleName() + ".xml"), "cdi-configuration.xml");
    }
}
