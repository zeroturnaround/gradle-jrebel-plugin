package org.zeroturnaround.jrebel.gradle.test;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Functional Test class using TestKit based on https://docs.gradle.org/current/userguide/test_kit.html
 */
@RunWith(Parameterized.class)
public class RebelPluginFunctionalIntegrationTest {

  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder();
  private File buildFile;

  private final String version;

  @Before
  public void setup() throws IOException {
    buildFile = testProjectDir.newFile("build.gradle");
  }

  public RebelPluginFunctionalIntegrationTest(String version) {
    this.version = version;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { "2.8" },
        { "2.14" },
        { "3.0" },
        { "3.1" },
        { "3.2.1" },
        { "3.3" },
        { "3.4.1" },
        { "3.5.1" },
        { "4.0" },
        { "4.1" },
        { "4.2" },
        { "4.2.1" },
        { "4.3" },
        { "4.4" },
        { "4.5" },
        { "4.6" },
        { "4.7" },
        { "4.8" },
        { "4.9" },
        { "4.10.3", },
        { "5.0", },
        { "5.1", },
        { "5.2.1", },
    });
  }

  @Test
  public void testCommandLineArtifactContainRebelXml() throws IOException {
    String buildFileContents = TestUtils.getBuildFileContents("/test-build.gradle");
    TestUtils.writeFile(buildFile, buildFileContents);

    GradleRunner runner = GradleRunner.create()
        .withProjectDir(testProjectDir.getRoot())
        .withPluginClasspath(TestUtils.getPluginTestClasspath())
        .withArguments("clean", "build", "--info", "--stacktrace")
        .forwardStdOutput(new OutputStreamWriter(System.out))
        .forwardStdError(new OutputStreamWriter(System.out));

    if (version != null) {
      runner.withGradleVersion(version);
    }

    BuildResult result = runner.build();

    assertEquals(result.task(":build").getOutcome(), SUCCESS);
    assertZipContainsRebelXML(runner);

    result = runner.build();
    assertEquals(result.task(":build").getOutcome(), SUCCESS);
    assertZipContainsRebelXML(runner);

  }

  private void assertZipContainsRebelXML(GradleRunner runner) throws IOException {
    ZipFile zipFile = new ZipFile(new File(runner.getProjectDir(), "build/libs/test.war"));

    Enumeration<? extends ZipEntry> entries = zipFile.entries();

    boolean exists = false;
    while(entries.hasMoreElements()){
      ZipEntry entry = entries.nextElement();
      //InputStream stream = zipFile.getInputStream(entry);
      if ("WEB-INF/classes/rebel.xml".equals(entry.getName())) {
        exists = true;
        break;
      }
    }
    Assert.assertTrue("Archive contains rebel.xml", exists);
  }
}
