package org.zeroturnaround.jrebel.gradle.test;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.jrebel.gradle.test.util.TestUtils;

/**
 * Functional Test class using TestKit based on https://docs.gradle.org/current/userguide/test_kit.html
 */
public class RebelPluginFunctionalTest {

  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder();
  private File buildFile;
  private GradleRunner runner;

  @Before
  public void setup() throws IOException {
    buildFile = testProjectDir.newFile("build.gradle");
    runner = GradleRunner.create()
        .withProjectDir(testProjectDir.getRoot())
        .withPluginClasspath(TestUtils.getPluginTestClasspath())
        .forwardStdOutput(new OutputStreamWriter(System.out))
        .forwardStdError(new OutputStreamWriter(System.out));
  }


  @Test
  public void testCommandLineRebelRootPath() throws IOException {
    //Simplest gradle java project with latest JRebel plugin
    String buildFileContent = "" +
        "plugins {\n" +
        "  id \"org.zeroturnaround.gradle.jrebel\"\n" +
        "}\n" +
        "apply plugin: 'java'\n";
    TestUtils.writeFile(buildFile, buildFileContent);

    BuildResult result = runner
        .withArguments("generateRebel", "-Prebel.rootPath=/opt/myproject", "--info", "--stacktrace")
        .build();

    assertEquals(result.task(":generateRebel").getOutcome(), SUCCESS);
    assertTrue(result.getOutput().contains("rebel.configuredRootPath = /opt/myproject"));
  }

  @Test
  public void testCustomRebelXmlDirectory() throws IOException {
    String buildFileContent = "" +
        "plugins {\n" +
        "  id 'org.zeroturnaround.gradle.jrebel'\n" +
        "}\n" +
        "apply plugin: 'java'\n" +
        "rebel.rebelXmlDirectory = \"build\"\n";
    TestUtils.writeFile(buildFile, buildFileContent);

    BuildResult result = runner
        .withArguments("generateRebel", "--info", "--stacktrace")
        .build();

    assertEquals(result.task(":generateRebel").getOutcome(), SUCCESS);
    assertTrue(result.getOutput().contains("rebel.rebelXmlDirectory = build"));
  }
}
