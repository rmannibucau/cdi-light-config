[![Build Status](https://secure.travis-ci.org/rmannibucau/cdi-light-config.png)](http://travis-ci.org/rmannibucau/cdi-light-config)

# Goal

Be able to configure CDI beans.

It is useful for legacy libraries and avoids a bunch of producers.

# XML

Files in the classloader called with the value
of DeltaSpike property `com.github.rmannibucau.cdi.configuration.LightConfigurationExtension.path`
(see `org.apache.deltaspike.core.spi.config.ConfigSource`). Default value is `cdi-configuration.xml`;

Default behavior is to add bean of a type in CDI context with qualifier @Named.

Here are the main use cases in a sample cdi-configuration.xml:

    <?xml version="1.0"?>
    <cdi-beans>
      <foo class="com.github.rmannibucau.cdi.test.configuration.ConfigurationTest$ABean1">
        <attributeBean1>bean1</attributeBean1>
        <bean2><bar /></bean2>
      </foo>

      <bar class="com.github.rmannibucau.cdi.test.configuration.ConfigurationTest$ABean2" scope="application">
        <attributeBean2>bean2</attributeBean2>
      </bar>

      <qualified class="com.github.rmannibucau.cdi.test.configuration.ConfigurationTest$ABean2"
                 qualifier="com.github.rmannibucau.cdi.test.configuration.ConfigurationTest$MyNamed" />

      <simpleBean class="com.github.rmannibucau.cdi.test.configuration.ConfigurationTest$ABean2"
                 qualifier="com.github.rmannibucau.cdi.test.configuration.ConfigurationTest$Simple" />

      <fromFactory class="com.github.rmannibucau.cdi.test.configuration.ConfigurationTest$ABean3"
                  factory-class="com.github.rmannibucau.cdi.test.configuration.ConfigurationTest$Factory"
                  qualifier="">
        <name>factory</name>
      </fromFactory>
      <constructor use-constructor="true"
                   class="com.github.rmannibucau.cdi.test.configuration.ConfigurationTest$ABean4">
        <value>constructor</value>
        <integer>1</integer>
      </constructor>
    </cdi-beans>

## Root tag

Root tag name is not important but `cdi-beans` is recommanded.

## Simple bean

Bean tag is its name (Bean name and name to use with @Named qualifier). To set attributes (from setter if possible or
directly on fields if no setter exists) just add sub tags matching the name of the attribute with the expected value.
The value can be either a string (will be converted for primitives and String) or another tag matching another bean name.

## Change the qualifier

The XML configuration supports two kind of qualifiers:

* qualifier without parameters
* qualifier with a String as attribute. The attribute needs to be called `name` or `value`.

Simply set the `qualifier` attribute to the desired qualfier class.

Note: set to an empty string, qualifier attribute means no qualifier.

## Use a factory

In this case the configuration defines the factory configuration (fields). You can specify
`factory-class` to set the factory class and `factory-method` for the method (default to `create`).

If the method is static no factory instance will be created.

# Get the created beans

By default you should be able to use:

    @Inject
    @Named("foo")
    private ABean1 bean1;
