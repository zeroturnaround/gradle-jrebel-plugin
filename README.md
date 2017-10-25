# JRebel Gradle Plugin

JRebel Gradle plugin generates the *rebel.xml* file for your project during the Gradle build (requires Gradle 2.0+ and Java 1.6+).

To enable JRebel for a project, you need to add the *rebel.xml* configuration file to it. The *rebel.xml* configuration file has to be added to the deployed .war or .jar archive. This will let the JRebel agent know which workspace paths to monitor for class and resource updates.

Refer to [JRebel Gradle plugin guide](https://manuals.zeroturnaround.com/jrebel/standalone/gradle.html) for usage instructions.


