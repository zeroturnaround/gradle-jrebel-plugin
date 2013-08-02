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
package org.zeroturnaround.jrebel.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.Action;
import org.gradle.api.internal.IConventionAware;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslClasspath;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslMain;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslWar;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslWeb;
import org.zeroturnaround.jrebel.gradle.util.BooleanUtil;
import org.zeroturnaround.jrebel.gradle.util.LoggerWrapper;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * The main entry-point for the JRebel Gradle plugin.
 * 
 * @author Sander Sonajalg, Igor Bljahhin
 */
public class RebelPlugin implements Plugin<Project> {

  /**
   * The name of the task that our plugin will define
   */
  public static final String GENERATE_REBEL_TASK_NAME = "generateRebel";
  
  public static final String REBEL_EXTENSION_NAME = "rebel";
  
  private LoggerWrapper log; 

  public void apply(final Project project) {
    log = new LoggerWrapper(project.getLogger());
    
    // register the Rebel task
    project.getTasks().create(GENERATE_REBEL_TASK_NAME, RebelGenerateTask.class);
    
    // only configure the real one if JavaPlugin gets enabled (it is pulled in by Groovy, Scala, War, ...)
    project.getLogger().info("Registering deferred Rebel plugin configuration...");
    project.getPlugins().withType(JavaPlugin.class).all(new Action<Plugin>() {
      public void execute(Plugin p) {
        configure(project);
      }
    });
  }

  /**
   * The conventionMappings callbacks will be executed lazily by Gradle's internal magic. If RebelGenerateTask
   * is later needing one of those properties, those callbacks configured here will be later executed to find
   * the actual value of those properties.
   */
  private void configure(final Project project) {
    log.info("Configuring Rebel plugin...");

    project.getExtensions().create(REBEL_EXTENSION_NAME, RebelDslMain.class);
    
    final RebelGenerateTask generateRebelTask = (RebelGenerateTask) project.getTasks().getByName(GENERATE_REBEL_TASK_NAME);
    final IConventionAware conventionAwareRebelTask = (IConventionAware) generateRebelTask;
    
    // let everything be compiled and processed so that classes / resources directories are there
    generateRebelTask.dependsOn(project.getTasks().getByName(JavaPlugin.CLASSES_TASK_NAME));

    final RebelDslMain rebelExtension = (RebelDslMain) project.getExtensions().getByName(REBEL_EXTENSION_NAME);
    
    configureRebelXmlDirectory(project, conventionAwareRebelTask, rebelExtension);

    // handle the 'packaging' configuration option
    generateRebelTask.setPackaging(RebelGenerateTask.PACKAGING_TYPE_JAR);

    configureWarPluginSettings(project, generateRebelTask, conventionAwareRebelTask, rebelExtension);

    configureDefaultClassesDirectory(project, conventionAwareRebelTask);
    
    configureDefaultResourcesDirectory(project, conventionAwareRebelTask);
    
    configureProjectAfterEvaluate(project, generateRebelTask, rebelExtension);
    
    // raise the flag that plugin configuration has been executed.
    generateRebelTask.setPluginConfigured();
  }

  /**
   * Handle the 'rebelXmlDirectory' configuration option
   */
  private void configureRebelXmlDirectory(final Project project, final IConventionAware conventionAwareRebelTask,
      final RebelDslMain rebelExtension)
  {
    conventionAwareRebelTask.getConventionMapping().map(RebelGenerateTask.NAME_REBEL_XML_DIRECTORY, new Callable<Object>() {
      public Object call() throws Exception {
        if (rebelExtension.getRebelXmlDirectory() != null) {
          return new File(rebelExtension.getRebelXmlDirectory());
        }
        else {
          JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
          return javaConvention.getSourceSets().getByName("main").getOutput().getClassesDir();
        }
      }
    });
  }

  /**
   * Configure things that need to be configured exactly if the WarPlugin has been enabled
   */
  private void configureWarPluginSettings(final Project project, final RebelGenerateTask generateRebelTask,
      final IConventionAware conventionAwareRebelTask, final RebelDslMain rebelExtension)
  {
    // 'execute' will be run if WarPlugin is already applied, or if it will be applied later during the configuration lifecycle
    project.getPlugins().withType(WarPlugin.class).all(new Action<Plugin>() {
      public void execute(Plugin p) {
        generateRebelTask.setPackaging(RebelGenerateTask.PACKAGING_TYPE_WAR);

        // Propagate 'defaultWebappDirectory'
        conventionAwareRebelTask.getConventionMapping().map(RebelGenerateTask.NAME_DEFAULT_WEBAPP_DIRECTORY, new Callable<Object>() {
          public Object call() throws Exception { 
            try {
              WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);
              return warConvention.getWebAppDir();
            }
            catch (Exception e) {
              return null;
            }
          }
        });
      }
    });
  }
  
  /**
   * Propagate 'defaultClassesDirectory'
   */
  private void configureDefaultClassesDirectory(final Project project, final IConventionAware conventionAwareRebelTask) {
    conventionAwareRebelTask.getConventionMapping().map(RebelGenerateTask.NAME_DEFAULT_CLASSES_DIRECTORY, new Callable<Object>() {
      public Object call() {
        try {
          JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
          return javaConvention.getSourceSets().getByName("main").getOutput().getClassesDir();
        }
        catch (Exception e) {
          return null;
        }
      }
    });
  }

  /**
   * Propagate 'defaultResourcesDirectory'
   */
  private void configureDefaultResourcesDirectory(final Project project, final IConventionAware conventionAwareRebelTask) {
    conventionAwareRebelTask.getConventionMapping().map(RebelGenerateTask.NAME_DEFAULT_RESOURCES_DIRECTORY, new Callable<Object>() {
      public Object call() {
        try {
          JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
          return javaConvention.getSourceSets().getByName("main").getOutput().getResourcesDir();
        }
        catch (Exception e) {
          return null;
        }
      }
    });
  }

  /**
   * Things executed in the end of configuration lifecycle. Mostly have to be here.. rebel DSL is not yet evaluated and these
   * things cannot be called within RebelPlugin#configure.
   */
  private void configureProjectAfterEvaluate(final Project project, final RebelGenerateTask generateRebelTask,
      final RebelDslMain rebelExtension)
  { 
    project.afterEvaluate(new Action<Project>() {

      @Override
      public void execute(Project project) {
        Boolean showGenerated = BooleanUtil.convertNullToFalse(rebelExtension.getShowGenerated());
        generateRebelTask.setShowGenerated(showGenerated);
        
        Boolean alwaysGenerate = BooleanUtil.convertNullToFalse(rebelExtension.getAlwaysGenerate());
        generateRebelTask.setAlwaysGenerate(alwaysGenerate);
        
        // XXX these two variables are untested and undocumented
        generateRebelTask.setConfiguredRootPath(rebelExtension.getRootPath());
        generateRebelTask.setConfiguredRelativePath(rebelExtension.getRelativePath());
         
        RebelDslClasspath classpath = rebelExtension.getClasspath();
        if (classpath != null) {
          generateRebelTask.setClasspath(classpath.toRebelClasspath());
        }
        
        RebelDslWar war = rebelExtension.getWar();
        if (war != null) {
          generateRebelTask.setWar(war.toRebelWar());
        }
        
        RebelDslWeb web = rebelExtension.getWeb();
        if (web != null) {
          generateRebelTask.setWeb(rebelExtension.getWeb().toRebelWeb());
        }
      }
      
    });
  }
}
