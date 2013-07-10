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
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.Action;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.logging.Logger;
import org.zeroturnaround.jrebel.gradle.dsl.model.RebelDslWar;
import org.zeroturnaround.jrebel.gradle.dsl.model.RebelDslWeb;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspath;

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
  
  private Logger log; 

  public void apply(final Project project) {
    log = project.getLogger();
    
    // by default, register a dummy task that reports missing JavaPlugin
    // TODO also get rid of this deprecated "add" method. Also, Luke says using this task replacement is bad idea anyway.
    project.getTasks().add(GENERATE_REBEL_TASK_NAME).doLast(new Action<Task>() {
      public void execute(Task task) {
        throw new IllegalStateException(
            "generateRebel is only valid when JavaPlugin is aplied directly or indirectly " +
            "(via other plugins that apply it implicitly, like Groovy or War); please update your build");        
      }
    });
    
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

    project.getExtensions().create(REBEL_EXTENSION_NAME, RebelPluginExtension.class);

    // configure Rebel task
    final RebelGenerateTask generateRebelTask = project.getTasks().replace(GENERATE_REBEL_TASK_NAME, RebelGenerateTask.class);
    final IConventionAware conventionAwareRebelTask = (IConventionAware) generateRebelTask;
    
    // let everything be compiled and processed so that classes / resources directories are there
    generateRebelTask.dependsOn(project.getTasks().getByName(JavaPlugin.CLASSES_TASK_NAME));

    final RebelPluginExtension rebelExtension = (RebelPluginExtension) project.getExtensions().getByName(REBEL_EXTENSION_NAME);
    
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

    // handle the 'packaging' configuration option
    generateRebelTask.setPackaging(RebelGenerateTask.PACKAGING_TYPE_JAR);

    // if WarPlugin already applied, or if it is applied later than this plugin...
    project.getPlugins().withType(WarPlugin.class).all(new Action<Plugin>() {
      public void execute(Plugin p) {
        generateRebelTask.setPackaging(RebelGenerateTask.PACKAGING_TYPE_WAR);

        // handle the 'warSourceDirectory' configuration option
        conventionAwareRebelTask.getConventionMapping().map(RebelGenerateTask.NAME_WAR_SOURCE_DIRECTORY, new Callable<Object>() {
          public Object call() throws Exception {
            if (rebelExtension.getWarSourceDirectory() != null) {
              return project.file(rebelExtension.getWarSourceDirectory());
            }
            else {
              WarPluginConvention warConvention = project.getConvention().getPlugin(WarPluginConvention.class);
              return warConvention.getWebAppDir();
            }
          }
        });
      }
    });

    // handle the 'addResourcesDirToRebelXml' configuration option
    conventionAwareRebelTask.getConventionMapping().map(RebelGenerateTask.NAME_ADD_RESOURCES_DIR_TO_REBEL_XML, new Callable<Object>() {
      public Object call() throws Exception {
        if (rebelExtension.getAddResourcesDirToRebelXml() != null) {
          return rebelExtension.getAddResourcesDirToRebelXml();
        }
        else {
          return true;
        }
      }
    });

    // handle the 'showGenerated' configuration option
    conventionAwareRebelTask.getConventionMapping().map(RebelGenerateTask.NAME_SHOW_GENERATED,  new Callable<Object>() {
      public Object call() throws Exception {
        if (rebelExtension.getShowGenerated() != null) {
          return rebelExtension.getShowGenerated();
        }
        else {
          return false;
        }
      }
    });

    // handle the 'alwaysGenerate' configuration option
    conventionAwareRebelTask.getConventionMapping().map(RebelGenerateTask.NAME_ALWAYS_GENERATE, new Callable<Object>() {
      public Object call() throws Exception {
        if (rebelExtension.getAlwaysGenerate() != null) {
          return rebelExtension.getAlwaysGenerate();
        }
        else {
          return false;
        }
      }
    });
    
    // handle the 'warPath' configuration option
    // TODO this should probably be put into "project.afterEvaluate" block
    conventionAwareRebelTask.getConventionMapping().map(RebelGenerateTask.NAME_WAR, new Callable<Object>() {
      public Object call() throws Exception {
        RebelDslWar war = rebelExtension.getWar();
        if (war != null) {
          return war.toRebelWar();
        }
        return null;
      }
    });

    // TODO remove
    log.info("webdsl : " + rebelExtension.getDslWeb());

    // This has to be here.. if i just execute it right away, rebel DSL is not yet evaluated
    project.afterEvaluate(new Action<Project>() {

      @Override
      public void execute(Project project) {

        // XXX below is actually basically a bunch of dead code.. its not dead technically, but these are the undocumented
        //     features bljahhin somehow copy-pasted from Maven plugin and that have never been used. I'll keep them here
        //     until I know what's gonna replace them and have a better solution ready
        
        // Hmmm... I think this stuff actuall doesn't work here at all.. the "rebel {..}" DSL  is not evaluated yet
        
        generateRebelTask.setConfiguredRootPath(rebelExtension.getRootPath());
        generateRebelTask.setConfiguredRelativePath(rebelExtension.getRelativePath());
        generateRebelTask.setConfiguredResourcesDirectory(rebelExtension.getResourcesDirectory());
        generateRebelTask.setConfiguredClassesDirectory(rebelExtension.getClassesDirectory());
        generateRebelTask.setConfiguredResourcesClasspath(rebelExtension.getResourcesClasspath());
        generateRebelTask.setConfiguredClasspath(rebelExtension.getClasspath());

        
        // TODO restore
//        RebelDslWar war = rebelExtension.getWar();
//        if (war != null) {
//          generateRebelTask.setWar(war.toRebelWar());
//        }
        
        RebelDslWeb web = rebelExtension.getDslWeb();
        if (web != null) {
          generateRebelTask.setWeb(rebelExtension.getDslWeb().toRebelWeb());
        }
        
      }
      
    });
    
  }
}
