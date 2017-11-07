# JRebel Gradle Plugin 

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) [![Build Status](https://travis-ci.org/zeroturnaround/gradle-jrebel-plugin?branch=master)](https://travis-ci.org/zeroturnaround/gradle-jrebel-plugin) https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/org/zeroturnaround/gradle-jrebel-plugin/maven-metadata.xml.svg [![Twitter Follow](https://img.shields.io/twitter/follow/zeroturnaround.svg?style=social&label=Follow&maxAge=2592000)](https://twitter.com/zeroturnaround)

JRebel Gradle plugin generates the *rebel.xml* file for your project during the Gradle build (requires Gradle 2.0+ and Java 1.6+).

To enable JRebel for a project, you need to add the *rebel.xml* configuration file to it. The *rebel.xml* configuration file has to be added to the deployed .war or .jar archive. This will let the JRebel agent know which workspace paths to monitor for class and resource updates.

Refer to [JRebel Gradle plugin guide](https://manuals.zeroturnaround.com/jrebel/standalone/gradle.html) for usage instructions.


