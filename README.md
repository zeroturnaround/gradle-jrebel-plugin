JRebel Gradle Plugin
====================

JRebel Gradle plugin can be used the generate `rebel.xml`.

Step 1 Add the following to build.gradle script:
------------------------------------------------

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

Step 2 Additional configuration:
--------------------------------

Add rebel element with following parameters to your build.gradle:

``` groovy
rebel {
  /*
  * alwaysGenerate - default is false
  *
  * If 'false' - rebel.xml is generated if timestamps of build.gradle and the current rebel.xml file are not equal.
  * If 'true' - rebel.xml will always be generated
  */
  alwaysGenerate = "true"

  /*
  * showGenerated - default is false
  *
  * If 'true' generated rebel.xml will be printed out in console, so you can immediately see what was generated
  */
  showGenerated = "true"

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
