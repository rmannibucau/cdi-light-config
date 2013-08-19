[![Build Status](https://secure.travis-ci.org/rmannibucau/cdi-light-config.png)](http://travis-ci.org/rmannibucau/cdi-light-config)

# Goal

Be able to declare CDI beans by configuration.

It is useful for legacy libraries (without beans.xml) and avoids a bunch of producers in some cases.

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

Root tag name is not important but `cdi-beans` is recommanded. It supports two main attributes:

* default-scope: which defines the scope to use for all beans if noone is configured
* default-qualifier: which defines the qualifier to use for all beans if noone is configured

## Simple bean

Bean tag is its name (Bean name and name to use with @Named qualifier). To set attributes (from setter if possible or
directly on fields if no setter exists) just add sub tags matching the name of the attributes with the expected values.
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

## Use constructor

Simply add `use-constructor="true"` in the bean attributes. Here the attributes names are just used
for documentation purpose.

# Get the created beans

By default you should be able to use:

    @Inject
    @Named("foo")
    private ABean1 bean1;

# Advanced configuration
## Array, List, Set

For these types use a comma separated values (CSV) format. For instance:

    <?xml version="1.0"?>
    <cdi-beans>
      <array class="com.github.rmannibucau.cdi.test.configuration.MapListArrayConfigurationTest$ArrayBean">
        <array>1,2,3</array>
      </array>
      <list class="com.github.rmannibucau.cdi.test.configuration.MapListArrayConfigurationTest$ListBean">
        <list>1,2,3</list>
      </list>
      <set class="com.github.rmannibucau.cdi.test.configuration.MapListArrayConfigurationTest$SetBean">
        <set>1,2,3</set>
      </set>
    </cdi-beans>

Note: attributes needs to set types explicitely. For instance `List<?>` will not be settable correctly but `List<Integer>` will be.

## Map

With the same constraint (parameterized types needs to be set), you can initialize a map attribute using the CSV format
and equal separator for keys/values:

    <?xml version="1.0"?>
    <cdi-beans>
      <map class="com.github.rmannibucau.cdi.test.configuration.MapListArrayConfigurationTest$MapBean">
        <map>1=v1,2=v2,3=v3</map>
      </map>
    </cdi-beans>
