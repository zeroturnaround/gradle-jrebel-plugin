package org.zeroturnaround.jrebel.gradle.test;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zeroturnaround.jrebel.gradle.test.util.RebelXMLHelper;

/**
 * Functional Test class using TestKit based on https://docs.gradle.org/current/userguide/test_kit.html
 */
@RunWith(Parameterized.class)
public class LegacyFunctionalIntegrationTest extends BaseRebelPluginFunctionalIntegrationTest {

  public LegacyFunctionalIntegrationTest(String version) {
    super(version);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> versions() {
    return Arrays.asList(new Object[][] {
        { "2.8" },
        { "2.14" },
        { "3.0" },
        { "3.1" },
        { "3.2.1" },
        { "3.3" },
        { "3.4.1" },
        { "3.5.1" }
    });
  }

  @Test
  public void testSecondBuildIsSkippedWhenNoChange() throws IOException {
    buildFileBuilder
        .write();

    assertEquals(SUCCESS, build().task(":build").getOutcome());

    RebelXMLHelper rebelXML = new RebelXMLHelper(getRebelXML());
    assertEquals(absolutePath("build/resources/main"), rebelXML.getClasspathDir(1));
    assertEquals(absolutePath("build/classes/main"), rebelXML.getClasspathDir(2));
    assertEquals(absolutePath("src/main/webapp"), rebelXML.getWebDir(1));

    assertGenerateRebelIsUpToDateOnRebuild();
  }
}
