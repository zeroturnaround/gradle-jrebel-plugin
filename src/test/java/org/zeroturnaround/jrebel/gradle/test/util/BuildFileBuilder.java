package org.zeroturnaround.jrebel.gradle.test.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.rules.TemporaryFolder;

public class BuildFileBuilder {

  TemporaryFolder projectDir;
  List<String> statements = new ArrayList<String>();
  List<String> pluginStatements = new ArrayList<String>();
  String rebelBlock = "";

  public BuildFileBuilder(TemporaryFolder projectDir) {
    this.projectDir = projectDir;
    addPlugin("id 'java'");
    addPlugin("id 'war'");
    addPlugin("id \"org.zeroturnaround.gradle.jrebel\"");

    add("war {\n" +
        "  archiveName = 'test.war'\n" +
        "}");
  }

  public BuildFileBuilder addRebelApplyBlock(){
    add("allprojects { project ->\n" +
        "    plugins.withId('java') {\n" +
        "        project.apply plugin: 'org.zeroturnaround.gradle.jrebel'\n" +
        "        def jarTask = project.tasks.findByName('jar')\n" +
        "        if (jarTask) {\n" +
        "            jarTask.dependsOn(generateRebel)\n" +
        "        }\n" +
        "        def warTask = project.tasks.findByName('war')\n" +
        "        if (warTask) {\n" +
        "            warTask.dependsOn(generateRebel)\n" +
        "        }\n" +
        "    }\n" +
        "}");
    return this;
  }

  public BuildFileBuilder addPlugin(String line) {
    pluginStatements.add(line);
    return this;
  }

  public BuildFileBuilder add(String line) {
    statements.add(line);
    return  this;
  }

  public BuildFileBuilder rebelBlock(String rebelBlock) {
    this.rebelBlock = rebelBlock;
    return this;
  }

  public BuildFileBuilder write() {
    StringBuilder sb = new StringBuilder();

    if (!pluginStatements.isEmpty()) {
      sb.append("plugins {").append("\n");

      for (String statement : pluginStatements) {
        sb.append(statement)
            .append("\n");
      }

      sb.append("}").append("\n");
    }

    if (!rebelBlock.isEmpty()) {
      sb.append("rebel {").append("\n")
          .append(rebelBlock).append("\n")
          .append("}").append("\n");
    }

    for (String statement : statements) {
      sb.append(statement)
          .append("\n");
    }

    try {
      TestUtils.writeFile(new File(projectDir.getRoot(), "build.gradle"), sb.toString());
      TestUtils.writeFile(new File(projectDir.getRoot(), "settings.gradle"), "rootProject.name = 'testProject'");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    return this;
  }
}
