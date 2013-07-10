/**
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

import java.util.Arrays;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.ProjectEvaluationListener;
import org.gradle.api.ProjectState;
import org.gradle.api.Task;
import org.gradle.api.internal.project.AbstractProject;
import org.gradle.api.internal.project.ProjectStateInternal;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.jetty.JettyPlugin;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;
import org.zeroturnaround.jrebel.gradle.RebelGenerateTask;
import org.zeroturnaround.jrebel.gradle.RebelPlugin;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslMain;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslWar;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslWeb;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslWebResource;
import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;
import org.zeroturnaround.jrebel.gradle.model.RebelWeb;
import org.zeroturnaround.jrebel.gradle.model.RebelWebResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * General tests for plugins integration with Gradle lifecycles, configuration option handling, etc.
 * 
 * @author Sander Sonajalg, Igor Bljahhin
 */
public class RebelPluginTest {

  /**
   * Test that the plugin adds a dummy task to the project when no JavaPlugin is applied
   * @throws TaskExecutionException 
   */
  @Test(expected = IllegalStateException.class)
  public void testAddsDummyTaskWhenJavaPluginNotApplied() throws Throwable {
    Project project = ProjectBuilder.builder().build();
    project.getProject().getPlugins().apply(RebelPlugin.class);

    Task task = project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);
    assertTrue(task instanceof DefaultTask);
    try {
      ((DefaultTask) task).execute();
    }
    catch (TaskExecutionException e) {
      throw e.getCause();
    }
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
    
    // check that the dependsOn got set
    Task classesTask = project.getTasks().getByName(JavaPlugin.CLASSES_TASK_NAME); 
    assertTrue(task.getDependsOn().contains(classesTask));
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
    
    // check that the dependsOn got set
    Task classesTask = project.getTasks().getByName(JavaPlugin.CLASSES_TASK_NAME); 
    assertTrue(task.getDependsOn().contains(classesTask));
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
    
    // check that the dependsOn got set
    Task classesTask = project.getTasks().getByName(JavaPlugin.CLASSES_TASK_NAME); 
    assertTrue(task.getDependsOn().contains(classesTask));
  }
  
  /**
   * Test that the plugin uses war packaging mode after JettyPlugin gets applied
   */
  @Test
  public void testUsesWarPackagingWithJettyPlugin() {
    Project project = ProjectBuilder.builder().build();
    project.getProject().getPlugins().apply(RebelPlugin.class);
    project.getProject().getPlugins().apply(JettyPlugin.class);

    Task task = project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);
    assertTrue(task instanceof RebelGenerateTask);
    
    RebelGenerateTask rebelTask = (RebelGenerateTask) task;
    assertTrue(rebelTask.getPackaging().equals(RebelGenerateTask.PACKAGING_TYPE_WAR));
    
    // check that the dependsOn got set
    Task classesTask = project.getTasks().getByName(JavaPlugin.CLASSES_TASK_NAME); 
    assertTrue(task.getDependsOn().contains(classesTask));
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
    dslWar.setPath(myWarPath);
    
    String myWarSourceDirectory = "/my/war/source/dir";
    rebelExtension.setWarSourceDirectory(myWarSourceDirectory);
    
    Boolean myAddResourcesDirToRebelXml = getRandomBoolean();
    rebelExtension.setAddResourcesDirToRebelXml(myAddResourcesDirToRebelXml);

    Boolean myShowGenerated = getRandomBoolean();
    rebelExtension.setShowGenerated(myShowGenerated);
    
    Boolean myAlwaysGenerate = getRandomBoolean();
    rebelExtension.setAlwaysGenerate(myAlwaysGenerate);
    
    // Execute the rebel task, validate the generated model
    RebelGenerateTask task = (RebelGenerateTask) project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);
    
    assertNotNull(task);
    
    // 'warSourceDirectory'
    assertEquals(myWarSourceDirectory, task.getWarSourceDirectory().getAbsolutePath());
    
    // 'addResourcesDirToRebelXml'
    assertEquals(myAddResourcesDirToRebelXml, task.getAddResourcesDirToRebelXml());

    // 'showGenerate'
    assertEquals(myShowGenerated, task.getShowGenerated());
    
    // 'alwasGenerate'
    assertEquals(myAlwaysGenerate, task.getAlwaysGenerate());
    
    // 'warPath'
    assertEquals(myWarPath, task.getWar().getPath());
  }
  
  /**
   * Test handling of the "war" configuration block. Should create a RebelWar element in the model.
   */
  @Test
  public void testWar() throws Exception {
    Project project = ProjectBuilder.builder().build();
    project.getPlugins().apply(WarPlugin.class);
    project.getPlugins().apply(RebelPlugin.class);
    
    // Cconfigure the rebel plugin
    RebelDslMain rebelExtension = (RebelDslMain) project.getExtensions().getByName(RebelPlugin.REBEL_EXTENSION_NAME);
    
    String myWarPath = "/my/war/path";
    
    RebelDslWar dslWar = new RebelDslWar();
    dslWar.setPath(myWarPath);
    rebelExtension.setWar(dslWar);
    
    // Execute the rebel task, validate the generated model
    RebelGenerateTask task = (RebelGenerateTask) project.getTasks().getByName(RebelPlugin.GENERATE_REBEL_TASK_NAME);

    // tell the task to actually not write any rebel.xml down to file system when running in test mode!
    task.skipWritingRebelXml();
    
    // execute the task
    task.generate();
    
    // validate the eventual model
    RebelMainModel model = task.getRebelModel();
    RebelWar war = model.getWar();
    
    assertNotNull(war);
    assertEquals(myWarPath, war.getOriginalPath());
  }
  
  /**
   * Test handling of the "web" configuration block.
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
    
    System.out.println("WEB IS : " + web);
    
    rebelExtension.setWeb(web);

    // Bad, internal-API-dependent code that works around the issue of 'afterEvaluated' not being called
    ProjectStateInternal projectState = new ProjectStateInternal();
    projectState.executed();
    ProjectEvaluationListener evaluationListener = ((AbstractProject) project).getProjectEvaluationBroadcaster();
    evaluationListener.afterEvaluate(project, projectState);
    
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
    
    System.out.println("testWeb() XML :  \n" + model.toXmlString());
  }
  
  // TODO tests for other properties -- what should the model look like after setting those config options 
  
  // TODO a test for java plugin project with customized source location -
  
  // TODO a test for java plugin project with MULTIPLE source locations with customized source location
  
  // TODO a test for war plugin project with customized source location -
  
  // TODO a test for war plugin project with MULTIPLE source locations with customized source location
  
  // TODO finally, write a combined end-to-end smoke test for simple scenario --
  //      a project goes in, rebel.xml goes out, XMLunit checks that its contents is adequate
  

  // TODO a test for the fixPath... somehow
  
  private static boolean getRandomBoolean() {
    return Math.random() < 0.5;
  }
}
