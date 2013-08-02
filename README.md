JRebel Gradle Plugin
====================

The *rebel.xml* descriptor file is the main resource you need to add to your project in order to start reloading classes and resources with JRebel.
Have a look at [this page](http://manuals.zeroturnaround.com/jrebel/standalone/config.html) for a reference on the *rebel.xml* file format.

JRebel Gradle plugin can be used to automatically generate a suitable *rebel.xml* configuration file for you during the Gradle build.



1. Enable JRebel Gradle plugin
------------------------------

Add the following to your *build.gradle* script. (NB! This adds a dependency for Gradle itself. Don't mix it up with your project's
project-level dependencies - you have to put this code into the ``buildscript {..}`` block!)

``` groovy
apply plugin: 'rebel'

buildscript {
  repositories {
     mavenCentral()
     mavenRepo(
       name: 'zt-public-snapshots',
       url: 'http://repos.zeroturnaround.com/nexus/content/groups/zt-public/'
     )
  }

  dependencies {
     classpath group: 'org.zeroturnaround', name: 'gradle-jrebel-plugin', version: '1.1.1'
  }
}
```

This will provide your Gradle build with a new task called *generateRebel*.

You probably also want to make the *generateRebel* task part of your main
build flow, instead of executing it manually after every clean. A good place to plug it in is right before the task that generates the build archive
(the .jar or the .war).

Therefore if your project uses the Java plugin: 

``` groovy
jar.dependsOn(generateRebel)
```

And if your project uses the War plugin:

``` groovy
war.dependsOn(generateRebel)
```

In many cases, this is all you need to do. The plugin should be able to read the locations of your classes and resources from Gradle's project model
and put them into your *rebel.xml*.  



2. Additional configuration
---------------------------

In some cases, the out-of-box configuration will be insufficient and you need to set some configuration explicitly. To get an idea about this,
start off by having a look at the *rebel.xml* that was generated based on the defaults, and figure out where is it going wrong.  

You can override the default settings or customize the behavior of JRebel Gradle plugin by adding a ``rebel {}`` element into your *build.gradle*,
specifying any of the parameters below:

``` groovy
rebel {
  /*
   * alwaysGenerate - default is false
   *
   * If 'false' - rebel.xml is generated if timestamps of build.gradle and the current rebel.xml file are not equal.
   * If 'true' - rebel.xml will always be generated
   */
  alwaysGenerate = true

  /*
   * showGenerated - default is false
   *
   * If set to true, generated rebel.xml will be printed out in console during the build, so you can immediately see what was generated.
   */
  showGenerated = true

  /*
   * rebelXmlDirectory - default is 'build/classes'
   *
   * Output directory for rebel.xml.
   */
  rebelXmlDirectory = "build/classes"

}
```




### 2.1. Configuring &lt;classpath&gt;


The ``<classpath>`` element in rebel.xml defines which locations are monitored by JRebel for new versions of your classes. If you
use Gradle's Java or War plugin, the plugin will ask for the classes output location from your Gradle project model. In many cases this
will be sufficient to make class reloading work (you just have to check that your IDE is also auto-compiling your
classes into that same directory). In that case, you can just completely leave out the ``classpath { .. } `` block and the
defaults will be used.

If for some reason the plugin is not getting it right, or you want to add additional classpath locations to your *rebel.xml* or
explicitly fine-tune excluded or included resources, you can do so by providing  ``classpath { .. } `` section in your
*build.gredle* DSL:

``` groovy
rebel {
  // (other config)
  
  classpath {
    resource {
      directory = "build/main/other-classes-dir"
      includes = ["**/*"]
      excludes = ["*.java", "*.properties"]
    }
    
    // the default element
    resource {}
    
    resource {
      directory = "build/integration-tests/classes"
    }
  }
}
```

Each ``resource {..}`` element will define one classpath search location in your ``rebel.xml``.

The empty ``resource {}`` element has a special meaning - this is a placeholder for the default classpath
location asked from the Java or War plugin. It can be used to control the order where the default location will
be placed in the generated *rebel.xml*. For example, the above configuration would generate a *rebel.xml* that
instructs JRebel to search for a class from these directories in that same order:

 1. ``build/main/other-classes-dir``
 2. *[the default compilation output directory known by your Gradle Java plugin]*
 3. ``build/integration-tests/classes``

If you omit the empty ``resource {}`` block, the default classpath will be added as the first element into your
*rebel.xml*. If you want the default classpath not to appear at all, use the ``omitDefault`` configuration option:

``` groovy
rebel {
  // (other config)
  
  classpath {
    // don't add the default classes target directory 
    omitDefaultClassesDir = true
    
    // don't add the default resources directory 
    omitDefaultResourcesDir = true
  
    resource {
      directory = "build/main/other-classes-dir"
      includes = ["**/*"]
      excludes = ["*.java", "*.properties"]
    }
  }

}
```

### 2.2. Configuring &lt;web&gt;

The ``<web>`` element is valid for war projects and lets you map specific locations of your workspace
against specific locations inside your war archive. An example: your war contains a folder */WEB-INF/jsps*,
which's contents comes from your workspace folder *src/main/jsps*. In order to make the changes you
make to JSPs in your workspace immediately available in your deployed application, you have to define
a mapping in your *rebel.xml*. The corresponding mapping is created by the first ``resource { .. }`` block
in the example below:

``` groovy
rebel {
  // other config ..

  web {
    resource {
      directory = "src/main/jsps"
      target = "/WEB-INF/jsps"
    }
    
    resource { }
    
    resource {
      directory = "src/main/WEB-INF-resources"
      target = "/WEB-INF/"
      includes = ["**/*.xml"]
      excludes = ["*.java", "*.groovy", "*.scala"]
    }
  }
}
```

The empty ``resource {}`` block here has similar meaning and properties as the one in ``classpath {..}``
configuration block.  It can be used to control the placement of the default resource-mapping element.
The default resource-mapping maps your war's root to the main webapp directory known by Gradle's project model.

Also here you can use the ``omitDefault`` setting to completely exclude the default configuration from
the generated *rebel.xml*:
  
``` groovy
rebel {
  // other config ..

  web {
    omitDefault = true
    resource {
      directory = "src/main/jsps"
      target = "/WEB-INF/jsps"
    }
  }
}
```

Once again, omit the ``web {..}`` configuration block as a whole if you are satisfied with the defaults.



### 2.3 Configuring &lt;war&gt;

You can also add the *<war>* element to your *rebel.xml* by adding the following to your *build.gradle*.
Refer to JRebel manual for details on the meaning of the *<war>* element.

``` groovy
rebel {
  war {
    path = "build/dist/my-other-webapp.war"
  }
}
```


3. IDE configuration
--------------------

Please note that the Grade Eclipse plugin does not seem to generate project files that would configure Eclipse to auto-compile your classes into
the same folder where Gradle is compiling them. JRebel class reloading relies on your IDE to automatically re-compile your classes, so that
JRebel can pick them up. The compilation output directory of your IDE and the monitored classes directory have to match in order
for the class reloading to work. Therefore, make sure that your IDE is compiling classes into the same directory where your Gradle project model
and the rebel.xml file are expecting them (*build/classes/main* by default, as opposed to *bin* which is the default for Eclipse).


