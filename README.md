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

```xml
<?xml version="1.0"?>
<cdi-beans>
  <foo class="org.superbiz.ABean1">
    <attributeBean1>bean1</attributeBean1>
    <bean2><bar /></bean2>
  </foo>

  <bar class="org.superbiz.ABean2" scope="application">
    <attributeBean2>bean2</attributeBean2>
  </bar>

  <qualified class="org.superbiz.ABean2"
             qualifier="org.superbiz.MyNamed" />

  <simpleBean class="org.superbiz.ABean2"
             qualifier="org.superbiz.Simple" />

  <fromFactory class="org.superbiz.ABean3"
              factory-class="org.superbiz.Factory"
              qualifier="">
    <name>factory</name>
  </fromFactory>
  <constructor use-constructor="true"
               class="org.superbiz.ABean4">
    <value>constructor</value>
    <integer>1</integer>
  </constructor>
</cdi-beans>
```

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

```java
@Inject
@Named("foo")
private ABean1 bean1;
```

# Advanced configuration
## Array, List, Set

For these types use a comma separated values (CSV) format. For instance:

```xml
<?xml version="1.0"?>
<cdi-beans>
  <array class="org.superbiz.ArrayBean">
    <array>1,2,3</array>
  </array>
  <list class="org.superbiz.ListBean">
    <list>1,2,3</list>
  </list>
  <set class="org.superbiz.SetBean">
    <set>1,2,3</set>
  </set>
</cdi-beans>
```

Note: attributes needs to set types explicitely. For instance `List<?>` will not be settable correctly but `List<Integer>` will be.

## Map

With the same constraint (parameterized types needs to be set), you can initialize a map attribute using the CSV format
and equal separator for keys/values:

```xml
<?xml version="1.0"?>
<cdi-beans>
  <map class="org.superbiz.MapBean">
    <map>1=v1,2=v2,3=v3</map>
  </map>
</cdi-beans>
```

# Extensibility

Some basic extensibility is supported through interface `com.github.rmannibucau.cdi.configuration.xml.handlers.NamespaceHandler`.

You'll basically use the child `com.github.rmannibucau.cdi.configuration.xml.handlers.NamespaceHandlerSupport` since
`NamespaceHandler` supports two kind of handling: attribute handling like properties and bean handling (shortcut to create a bean).
`NamespaceHandlerSupport` just throw an exception if the handler is used for a part it shouldn't handle.

The first one will be used to add beans from a tag (same level as bean ones) and the second add add attributes from inline tags (as property one).


## Default available handlers
### array

```xml
<?xml version="1.0"?>
<cdi-beans xmlns:array="array">
  <array:myArray type="java.lang.Integer" item-0="0" item-1="2" item-2="4" />
</cdi-beans>
```

### list

```xml
<?xml version="1.0"?>
<cdi-beans xmlns:list="list">
  <list:myList type="java.lang.Integer" item-0="0" item-1="2" item-2="4" />
</cdi-beans>
```

### set

```xml
<?xml version="1.0"?>
<cdi-beans xmlns:set="set">
  <set:mySet type="java.lang.Integer" item-0="0" item-1="2" item-2="4" />
</cdi-beans>
```

### map

```xml
<?xml version="1.0"?>
<cdi-beans xmlns:map="map">
  <map:myMap key-type="java.lang.String" value-type="java.lang.Integer"
             key-0="0" value-0="0"
             key-1="1" value-1="2"
             key-2="2" value-2="4" />
</cdi-beans>
```

keys and values are matched using the index after `key-` or `value-`. It allows
keys/values which are not xml compliant.

### webservice

```xml
<?xml version="1.0"?>
<cdi-beans xmlns:ws="webservice">
  <ws:myWs interface="org.superbiz.MyWebService"
           service-qname="{http://configuration.test.cdi.rmannibucau.github.com/}MyWebServiceService"
           port-qname="{http://configuration.test.cdi.rmannibucau.github.com/}MyWebServicePort"
           wsdl="http://webservice.domain.com:8080/MyWebService?wsdl" />
</cdi-beans>
```

`port-name` is optional.

### lookup

```xml
<?xml version="1.0"?>
<cdi-beans xmlns:lookup="lookup">
  <lookup:service type="corg.superbiz.MyService"
                  jndi="java:global/myapp/mymodule/MyService"
                  java.naming.factory.initial="org.apache.openejb.core.LocalInitialContextFactory" />
</cdi-beans>
```

All properties of the initial context can be set as attributes.

### properties

```xml
<?xml version="1.0"?>
<cdi-beans xmlns:prop="properties">
  <prop:props path="ab.properties" />
</cdi-beans>
```

Read either file or classpath resource `ab.properties`.

### property

To be more concise you can set properties inline using property namespace:

```xml
<?xml version="1.0"?>
<cdi-beans xmlns:p="property">
  <inline class="org.superbiz.Inline"
          p:value="foo" />
</cdi-beans>
```

Just add `xmlns:p="property"` and prefix inline your properties name by this namespace (`p`).

Note: for the value you can use `ref:name` to reference the bean with the name `name`.

# Basic interpolation

Thanks to Apache DeltaSpike `org.apache.deltaspike.core.spi.config.ConfigSource` SPI you can interpolate some values.

By default `META-INF/apache-deltaspike.properties` is read but you can add all the `ConfigSource` implementations you want.

Once your value is configured it can be used in `cdi-configuration.xml`:

```xml
<?xml version="1.0"?>
<cdi-beans xmlns:p="property">
  <msg class="org.superbiz.Message"
       p:message="${value}" />
</cdi-beans>
```
# Hook methods

```xml
<?xml version="1.0"?>
<cdi-beans>
  <lifecycle class="org.superbiz.Lifecycle"
             init-method="init"
             destroy-method="destroy" />
  <lifecycleFactory class="org.superbiz.Lifecycle"
             factory-class="org.superbiz.LifecycleFactory"
             init-method="initFactory"
             destroy-method="destroyFactory" />
</cdi-beans>
```

`init-method` and `destroy-method` define a way to initialize and cleanup a bean. When set on a bean using a factory
it will apply on the factory (and you'll have to call the bean hooks in the factory hooks if you need it).
