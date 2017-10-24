/*
 *  Copyright (C) 2012 ZeroTurnaround <support@zeroturnaround.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.zeroturnaround.jrebel.gradle.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.ProjectEvaluationListener;
import org.gradle.api.Task;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.project.ProjectStateInternal;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.jrebel.gradle.RebelGenerateTask;
import org.zeroturnaround.jrebel.gradle.RebelPlugin;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslMain;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslWar;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslWeb;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslWebResource;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspathResource;
import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;
import org.zeroturnaround.jrebel.gradle.model.RebelWebResource;

/**
 * General tests for plugins integration with Gradle lifecycles, configuration option handling, etc.
 *
 * @author Sander Sonajalg, Igor Bljahhin
 */
public class RebelPluginTest {

  private static final Logger log = LoggerFactory.getLogger(RebelPluginTest.class);

  @Rule
  public TestName name = new TestName();
  @Rule
  public ExpectedException ex = ExpectedException.none();

  @Before
  public void beforeEachTest() {
    log.info("\n\n === Executing test " + name.getMethodName() + "\n");
  }

  /**
   * Test that the plugin adds a dummy task to the project when no JavaPlugin is applied
   */
  @Test
  public void testAddsDummyTaskWhenJavaPluginNotApplied() {
    ex.expectCause(CoreMatchers.<Throwable>instanceOf(IllegalStateException.class));
    ex.expectCause(hasMessage(containsString("generateRebel is only valid when JavaPlugin is applied")));

    Project project = ProjectBuilder.builder().build();
    project.getProject().getPlugins().apply(RebelPlugin.class);

    Task task = project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);
    assertTrue(task instanceof DefaultTask);
    ((DefaultTask) task).execute();

    cleanUp(project);
  }

  /**
   * Test that the plugin adds RebelGenerateTask to project when JavaPlugin is already applied
   */
  @Test
  public void testAddsRebelTaskWhenJavaPluginApplied() {
    Project project = ProjectBuilder.builder().build();
    project.getProject().getPlugins().apply(JavaPlugin.class);
    project.getProject().getPlugins().apply(RebelPlugin.class);

    Task task = project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);
    assertTrue(task instanceof RebelGenerateTask);

    RebelGenerateTask rebelTask = (RebelGenerateTask) task;
    assertTrue(rebelTask.getPackaging().equals(RebelGenerateTask.PACKAGING_TYPE_JAR));

    // check that the dependsOn is not set
    Task classesTask = project.getTasks().getByName(JavaPlugin.CLASSES_TASK_NAME);
    assertFalse(task.getDependsOn().contains(classesTask));

    cleanUp(project);
  }

  /**
   * Test that the plugin adds rebel task to project after GroovyPlugin is applied
   */
  @Test
  public void testAddsRebelTaskAfterGroovyPluginApplied() {
    Project project = ProjectBuilder.builder().build();
    project.getProject().getPlugins().apply(RebelPlugin.class);
    project.getProject().getPlugins().apply(GroovyPlugin.class);

    Task task = project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);
    assertTrue(task instanceof RebelGenerateTask);

    RebelGenerateTask rebelTask = (RebelGenerateTask) task;
    assertTrue(rebelTask.getPackaging().equals(RebelGenerateTask.PACKAGING_TYPE_JAR));

    // check that the dependsOn is not set
    Task classesTask = project.getTasks().getByName(JavaPlugin.CLASSES_TASK_NAME);
    assertFalse(task.getDependsOn().contains(classesTask));

    cleanUp(project);
  }

  /**
   * Test that the plugin uses war packaging mode when WarPlugin is already applied
   */
  @Test
  public void testUsesWarPackagingWithWarPlugin() {
    Project project = ProjectBuilder.builder().build();
    project.getProject().getPlugins().apply(WarPlugin.class);
    project.getProject().getPlugins().apply(RebelPlugin.class);

    Task task = project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);
    assertTrue(task instanceof RebelGenerateTask);

    RebelGenerateTask rebelTask = (RebelGenerateTask) task;
    assertTrue(rebelTask.getPackaging().equals(RebelGenerateTask.PACKAGING_TYPE_WAR));

    // check that the dependsOn is not set
    Task classesTask = project.getTasks().getByName(JavaPlugin.CLASSES_TASK_NAME);
    assertFalse(task.getDependsOn().contains(classesTask));

    cleanUp(project);
  }

  /**
   * Test that the plugin uses war packaging mode after JettyPlugin gets applied
   */
  @Test
  public void testUsesWarPackagingWithJettyPlugin() {
    Project project = ProjectBuilder.builder().build();
    project.getProject().getPlugins().apply(RebelPlugin.class);
    project.getProject().getPlugins().apply(WarPlugin.class);

    Task task = project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);
    assertTrue(task instanceof RebelGenerateTask);

    RebelGenerateTask rebelTask = (RebelGenerateTask) task;
    assertTrue(rebelTask.getPackaging().equals(RebelGenerateTask.PACKAGING_TYPE_WAR));

    // check that the dependsOn is not set
    Task classesTask = project.getTasks().getByName(JavaPlugin.CLASSES_TASK_NAME);
    assertFalse(task.getDependsOn().contains(classesTask));

    cleanUp(project);
  }

  /**
   * Test that the configuration options from RebelPluginExtension propagate nicely
   * through to RebelGenerateTaks.
   */
  @Test
  public void testConfigurationOptionPropagation() throws Exception {
    Project project = ProjectBuilder.builder().build();
    project.getPlugins().apply(WarPlugin.class);
    project.getPlugins().apply(RebelPlugin.class);

    // Configure the rebel plugin
    RebelDslMain rebelExtension = (RebelDslMain) project.getExtensions().getByName(RebelPlugin.REBEL_EXTENSION_NAME);

    String myWarPath = "/my/war/path";
    RebelDslWar dslWar = new RebelDslWar();
    rebelExtension.setWar(dslWar);
    dslWar.setDir(myWarPath);

    Boolean myShowGenerated = getRandomBoolean();
    rebelExtension.setShowGenerated(myShowGenerated);

    Boolean myAlwaysGenerate = getRandomBoolean();
    rebelExtension.setAlwaysGenerate(myAlwaysGenerate);

    callAfterEvaluated(project);

    // Just get the rebel task (don't execute it)
    RebelGenerateTask task = (RebelGenerateTask) project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);

    assertNotNull(task);

    task.propagateConventionMappingSettings();

    // 'showGenerate'
    assertEquals(myShowGenerated, task.getShowGenerated());

    // 'alwasGenerate'
    assertEquals(myAlwaysGenerate, task.getAlwaysGenerate());

    // 'warPath'
    assertEquals(myWarPath, task.getWar().getDir());

    cleanUp(project);
  }

  @Test
  public void testJarProjectDefaultsWithCleanProject() throws Exception {
    Project project = ProjectBuilder.builder().build();
    project.getPlugins().apply(JavaPlugin.class);
    project.getPlugins().apply(RebelPlugin.class);

    callAfterEvaluated(project);
    project.getTasks().getByName("clean");

    // Get and execute the rebel task
    RebelGenerateTask task = (RebelGenerateTask) project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);
    task.skipWritingRebelXml();
    task.generate();

    RebelMainModel model = task.getRebelModel();

    // Check the classpath directories and if they exist
    Assert.assertEquals(2, model.getClasspathDirs().size());
    for (RebelClasspathResource resource : model.getClasspathDirs()) {
      assertTrue(new File(resource.getDirectory()).exists());
    }

    cleanUp(project);
  }

  /**
   * Test the default configuration (i.e. without any classpath/web/war DSL blocks) for a jar project.
   */
  @Test
  public void testJarProjectDefaults() throws Exception {
    Project project = ProjectBuilder.builder().build();
    project.getPlugins().apply(JavaPlugin.class);
    project.getPlugins().apply(RebelPlugin.class);

    callAfterEvaluated(project);

    // Create the default classes directory by hand, as the Java plugin is not actually executing in our test
    JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
    Set<File> defaultOutputDirs = javaConvention.getSourceSets().getByName("main").getOutput().getFiles();
    for (File f : defaultOutputDirs) {
      f.mkdirs();
      log.info("defaultClassesDir: {}", f.getAbsolutePath());
    }

    // Get and execute the rebel task
    RebelGenerateTask task = (RebelGenerateTask) project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);
    task.skipWritingRebelXml();
    task.generate();

    RebelMainModel model = task.getRebelModel();

    // Check the classpath directories
    List<RebelClasspathResource> classpathDirs = model.getClasspathDirs();
    Assert.assertEquals(2, classpathDirs.size());

    log.info("classpathDirs size = {}", classpathDirs.size());
    for (RebelClasspathResource resource : classpathDirs) {
      File dir = new File(resource.getDirectory());
      assertThat(defaultOutputDirs, hasItem(dir));
    }

    cleanUp(project);
  }

  /**
   * Test the default configuration (i.e. without any classpath/web/war DSL blocks) for a war project.
   */
  @Test
  public void testWarProjectDefaults() throws Exception {

    Project project = ProjectBuilder.builder().build();
    project.getPlugins().apply(WarPlugin.class);
    project.getPlugins().apply(RebelPlugin.class);

    callAfterEvaluated(project);

    // Default classes directory
    JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
    Set<File> defaultOutputDirs = javaConvention.getSourceSets().getByName("main").getOutput().getFiles();
    // create directory by hand, as the Java plugin is not actually executing in our test
    for (File f : defaultOutputDirs) {
      f.mkdirs();
      log.info("Default classes dir: " + f.getAbsolutePath());
    }

    // Default webapp directory
    WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);
    File defaultWebappDirectory = warConvention.getWebAppDir();
    // create directory by hand, as the War plugin is not actually executing in our test
    defaultWebappDirectory.mkdirs();
    log.info("Default webapp dir: " + defaultWebappDirectory.getAbsolutePath());

    // Get and execute the rebel task
    RebelGenerateTask task = (RebelGenerateTask) project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);
    task.skipWritingRebelXml();
    task.generate();

    RebelMainModel model = task.getRebelModel();

    // Check the classpath directories
    List<RebelClasspathResource> classpathDirs = model.getClasspathDirs();
    Assert.assertEquals(2, classpathDirs.size());

    for (RebelClasspathResource resource : classpathDirs) {
      File dir = new File(resource.getDirectory());
      assertThat(defaultOutputDirs, hasItem(dir));
    }

    // Check the web directories
    List<RebelWebResource> webappResources = model.getWebResources();
    Assert.assertEquals(1, webappResources.size());

    for (RebelWebResource resource : webappResources) {
      File dir = new File(resource.getDirectory());
      assertEquals(defaultWebappDirectory, dir);
    }

    cleanUp(project);
  }

  /**
   * Test handling of the "war { .. }" configuration block. Should create a RebelWar element in the model.
   */
  @Test
  public void testWar() throws Exception {
    Project project = ProjectBuilder.builder().build();
    project.getPlugins().apply(WarPlugin.class);
    project.getPlugins().apply(RebelPlugin.class);

    // Configure the rebel plugin
    RebelDslMain rebelExtension = (RebelDslMain) project.getExtensions().getByName(RebelPlugin.REBEL_EXTENSION_NAME);

    String myWarPath = "/my/war/path";

    RebelDslWar dslWar = new RebelDslWar();
    dslWar.setDir(myWarPath);
    rebelExtension.setWar(dslWar);

    callAfterEvaluated(project);

    // Get the rebel task
    RebelGenerateTask task = (RebelGenerateTask) project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);

    // tell the task to actually not write any rebel.xml down to file system when running in test mode!
    task.skipWritingRebelXml();

    // execute the task
    task.generate();

    // validate the eventual model
    RebelMainModel model = task.getRebelModel();
    RebelWar war = model.getWar();

    assertNotNull(war);
    assertEquals(myWarPath, war.getOriginalDir());

    cleanUp(project);
  }

  /**
   * Test handling of the "web { .. }" configuration block.
   *
   * TODO not finished
   */
  @Test
  public void testWeb() throws Exception {
    Project project = ProjectBuilder.builder().build();
    project.getPlugins().apply(WarPlugin.class);
    project.getPlugins().apply(RebelPlugin.class);

    // Configure the rebel plugin
    RebelDslMain rebelExtension = (RebelDslMain) project.getExtensions().getByName(RebelPlugin.REBEL_EXTENSION_NAME);

    RebelDslWeb web = new RebelDslWeb();

    RebelDslWebResource webResource1 = new RebelDslWebResource();
    webResource1.setTarget("/");
    webResource1.setDirectory("src/main/webapp");
    webResource1.setIncludes(Arrays.<String>asList("*.xml"));
    webResource1.setExcludes(Arrays.<String>asList("*.java", "*.groovy", "*.scala"));
    web.addWebResources(webResource1);

    RebelDslWebResource webResource2 = new RebelDslWebResource();
    webResource2.setTarget("/WEB-INF/");
    webResource2.setDirectory("src/main/my-web-inf");
    web.addWebResources(webResource2);

    log.info("RebelDslWeb : " + web);

    rebelExtension.setWeb(web);

    callAfterEvaluated(project);

    // Execute the rebel task, validate the generated model
    RebelGenerateTask task = (RebelGenerateTask) project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);

    // tell the task to actually not write any rebel.xml down to file system when running in test mode!
    task.skipWritingRebelXml();

    // execute the task
    task.generate();

    // validate the eventual model
    RebelMainModel model = task.getRebelModel();
    assertNotNull(model);

    List<RebelWebResource> webResources = model.getWebResources();

    // TODO very rough test that doesn't actually validate almost anything... just make sure the code runs through
    // TODO make it test the real requirements more thoroughly
    assertTrue(webResources.size() > 0);

    log.info("testWeb() XML :  \n" + model.toXmlString());

    cleanUp(project);
  }

  // TODO tests for other properties -- what should the model look like after setting those config options

  // TODO a test for java plugin project with customized source location -

  // TODO a test for java plugin project with MULTIPLE source locations with customized source location

  // TODO a test for war plugin project with customized source location -

  // TODO a test for war plugin project with MULTIPLE source locations with customized source location

  // TODO finally, write a combined end-to-end smoke test for simple scenario --
  //      a project goes in, rebel.xml goes out, XMLunit checks that its contents is adequate


  // TODO a test for the fixPath... somehow

  /**
   * Make sure the temporary project folder gets deleted after running the test
   */
  private static void cleanUp(Project project) {
    File projectDir = project.getProjectDir();
    try {
      FileUtils.deleteDirectory(projectDir);
    }
    catch (IOException e) {
      log.info("Exception while deleting the temporary project directory when running unit tests : ");
      e.printStackTrace();
    }
  }

  private static boolean getRandomBoolean() {
    return Math.random() < 0.5;
  }

  /**
   * Bad, internal-API-dependent code that works around the issue of 'afterEvaluated' not being called
   */
  private static void callAfterEvaluated(Project project) {
    ProjectStateInternal projectState = new ProjectStateInternal();
    projectState.executed();
    ProjectEvaluationListener evaluationListener = ((ProjectInternal) project).getProjectEvaluationBroadcaster();
    evaluationListener.afterEvaluate(project, projectState);
  }

}
