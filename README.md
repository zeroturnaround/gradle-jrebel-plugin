# JRebel Gradle Plugin 

[![Build Status](https://travis-ci.org/zeroturnaround/gradle-jrebel-plugin.svg?branch=master)](https://travis-ci.org/zeroturnaround/gradle-jrebel-plugin) [![Quality Gate](https://sonarcloud.io/api/badges/gate?key=org.zeroturnaround%3Agradle-jrebel-plugin)](https://sonarcloud.io/dashboard/index/org.zeroturnaround%3Agradle-jrebel-plugin) [![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) [![Maven metadata](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/org/zeroturnaround/gradle-jrebel-plugin/maven-metadata.xml.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.zeroturnaround%22%20AND%20a%3A%22gradle-jrebel-plugin%22) [![Twitter Follow](https://img.shields.io/twitter/follow/zeroturnaround.svg?style=social&label=Follow&maxAge=2592000)](https://twitter.com/zeroturnaround)

JRebel Gradle plugin generates the *rebel.xml* file for your project during the Gradle build (requires Gradle 2.0+ and Java 1.6+).

To enable JRebel for a project, you need to add the *rebel.xml* configuration file to it. The *rebel.xml* configuration file has to be added to the deployed .war or .jar archive. This will let the JRebel agent know which workspace paths to monitor for class and resource updates.

Refer to [JRebel Gradle plugin guide](https://manuals.zeroturnaround.com/jrebel/standalone/gradle.html) for usage instructions.


