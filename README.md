JRebel Gradle Plugin
====================

JRebel Gradle plugin can be used to automatically generate the JRebel's `rebel.xml` configuration file during your Gradle build.

1. Enable JRebel Gradle plugin
------------------------------

Add the following to ```build.gradle``` script. (This adds a dependency for Gradle itself. Don't mix it up with your project's project-level dependencies - it has to be put into the ``buildscript {..}`` block.

``` groovy
apply plugin: 'rebel'

buildscript {
  repositories {
     mavenLocal()
     mavenCentral()
     mavenRepo(name: 'zt-public-snapshots',
               url: 'http://repos.zeroturnaround.com/nexus/content/groups/zt-public/')
  }

  dependencies {
     classpath group: 'org.zeroturnaround', name: 'gradle-jrebel-plugin', version: '1.0.2-SNAPSHOT'
  }
}
```

2. Additional configuration
---------------------------

Add rebel element with following parameters to your build.gradle:

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
   * If 'true' generated rebel.xml will be printed out in console, so you can immediately see what was generated
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
   * The directory with web resources.
   */
  warSourceDirectory = "src/main/webapp"
}
```


You probably also want to make the ``generateRebel`` task part of your main build flow, instead of executing it manually after every clean. Many standard Gradle tasks should be suitable as the injection point (``processResources``, ``jar``, ``build``, ..).

``` groovy
processResources.dependsOn(generateRebel)
```