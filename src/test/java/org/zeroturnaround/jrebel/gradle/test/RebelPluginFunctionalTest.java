package org.zeroturnaround.jrebel.gradle.test;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.gradle.testkit.runner.TaskOutcome.*;

/**
 * Functional Test class using TestKit based on https://docs.gradle.org/current/userguide/test_kit.html
 */
public class RebelPluginFunctionalTest {

  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder();
  private File buildFile;

  @Before
  public void setup() throws IOException {
    buildFile = testProjectDir.newFile("build.gradle");
  }

  @Test
  public void testCommandLineRebelRootPath() throws IOException {
    //Simplest gradle java project with latest JRebel plugin
    String buildFileContent = "plugins { id \"org.zeroturnaround.gradle.jrebel\" version \"1.1.7\" }\n apply plugin: 'java'\n";
    writeFile(buildFile, buildFileContent);

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir.getRoot())
        .withArguments("generateRebel", "-Prebel.rootPath=/opt/myproject", "--info")
        .build();

    assertEquals(result.task(":generateRebel").getOutcome(), SUCCESS);
    assertTrue(result.getOutput().contains("rebel.configuredRootPath = /opt/myproject"));
  }

  @Test
  public void testCustomRebelXmlDirectory() throws IOException {
    String buildFileContent = "plugins { id \"org.zeroturnaround.gradle.jrebel\" version \"1.1.7\" }\n" +
        "apply plugin: 'java'\n" +
        "rebel.rebelXmlDirectory = \"build\"\n";
    writeFile(buildFile, buildFileContent);

    BuildResult result = GradleRunner.create()
        .withProjectDir(testProjectDir.getRoot())
        .withArguments("generateRebel", "--info")
        .build();

    assertEquals(result.task(":generateRebel").getOutcome(), SUCCESS);
    assertTrue(result.getOutput().contains("rebel.rebelXmlDirectory = build"));
  }

  private void writeFile(File destination, String content) throws IOException {
    BufferedWriter output = null;
    try {
      output = new BufferedWriter(new FileWriter(destination));
      output.write(content);
    }
    finally {
      if (output != null) {
        output.close();
      }
    }
  }
}
