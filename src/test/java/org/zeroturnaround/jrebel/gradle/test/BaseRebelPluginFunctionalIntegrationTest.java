package org.zeroturnaround.jrebel.gradle.test;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.jrebel.gradle.test.util.BuildFileBuilder;
import org.zeroturnaround.jrebel.gradle.test.util.TestUtils;

public class BaseRebelPluginFunctionalIntegrationTest {
  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder();

  protected GradleRunner runner;
  protected File buildFile;
  protected BuildFileBuilder buildFileBuilder;

  protected final String version;

  public BaseRebelPluginFunctionalIntegrationTest(String version) {
    this.version = version;
  }

  @Before
  public void setup() throws IOException {
    buildFile = testProjectDir.newFile("build.gradle");

    runner = GradleRunner.create()
        .withProjectDir(testProjectDir.getRoot())
        .withPluginClasspath(TestUtils.getPluginTestClasspath())
        .withArguments("build", "--info", "--stacktrace")
        .forwardStdOutput(new OutputStreamWriter(System.out))
        .forwardStdError(new OutputStreamWriter(System.out))
        .withDebug(true);

    if (version != null) {
      runner.withGradleVersion(version);
    }

    buildFileBuilder = new BuildFileBuilder();
  }


  protected void assertGenerateRebelIsUpToDateOnRebuild() throws IOException {
    TaskOutcome expectedStatus = version.startsWith("4") || version.startsWith("5") ? UP_TO_DATE : SUCCESS;
    assertEquals(expectedStatus, build().task(":generateRebel").getOutcome());
  }

  protected BuildResult build() throws IOException {
    return runner.build();
  }

  protected String absolutePath(String path) {
    return new File(testProjectDir.getRoot(), path).getAbsolutePath();
  }

  protected String getRebelXML() throws IOException {
    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(absolutePath( "build/libs/test.war"));
      InputStream stream = null;

      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while(entries.hasMoreElements()){
        ZipEntry entry = entries.nextElement();
        try {
          stream = zipFile.getInputStream(entry);
          if ("WEB-INF/classes/rebel.xml".equals(entry.getName())) {
            return IOUtils.toString(stream, Charset.forName("UTF-8"));
          }
        } finally {
          stream.close();
        }
      }
      throw new RuntimeException("RebelXML not found");
    } finally {
      if (zipFile != null)
        zipFile.close();
    }
  }
}
