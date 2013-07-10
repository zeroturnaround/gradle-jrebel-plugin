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
     classpath group: 'org.zeroturnaround', name: 'gradle-jrebel-plugin', version: '1.0.3'
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

  /*
   * warSourceDirectory - default is 'src/main/webapp'
   *
   * The directory with web resources that will be monitored for changes.
   */
  warSourceDirectory = "src/main/webapp"
  
  
  /*
   * Adds the <war> element into the rebel.xml with the specified path attribute.
   */
  war {
    path = "build/war"
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

