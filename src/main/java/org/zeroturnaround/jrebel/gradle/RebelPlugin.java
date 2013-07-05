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

  public void apply(final Project project) {
    // by default, register a dummy task that reports missing JavaPlugin
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

  private void configure(final Project project) {
    project.getLogger().info("Configuring Rebel plugin...");

    project.getExtensions().create(REBEL_EXTENSION_NAME, RebelPluginExtension.class);

    // configure Rebel task
    final RebelGenerateTask generateRebelTask = project.getTasks().replace(GENERATE_REBEL_TASK_NAME, RebelGenerateTask.class);
    final IConventionAware conventionAwareRebelTask = (IConventionAware) generateRebelTask;
    
    // let everything be compiled and processed so that classes / resources directories are there
    generateRebelTask.dependsOn(project.getTasks().getByName(JavaPlugin.CLASSES_TASK_NAME));

    conventionAwareRebelTask.getConventionMapping().map("rebelXmlDirectory", new Callable<Object>() {
      public Object call() throws Exception {
        RebelPluginExtension rebelExtension = (RebelPluginExtension) project.getExtensions().getByName(REBEL_EXTENSION_NAME);
        if (rebelExtension.getRebelXmlDirectory() != null) {
          return new File(rebelExtension.getRebelXmlDirectory());
        }
        else {
          JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
          return javaConvention.getSourceSets().getByName("main").getOutput().getClassesDir();
        }
      }
    });

    // set default value
    generateRebelTask.setPackaging(RebelGenerateTask.PACKAGING_TYPE_JAR);

    // if WarPlugin already applied, or if it is applied later than this plugin...
    project.getPlugins().withType(WarPlugin.class).all(new Action<Plugin>() {
      public void execute(Plugin p) {
        generateRebelTask.setPackaging(RebelGenerateTask.PACKAGING_TYPE_WAR);
  
        conventionAwareRebelTask.getConventionMapping().map("warSourceDirectory", new Callable<Object>() {
          public Object call() throws Exception {
            RebelPluginExtension rebelExtension = (RebelPluginExtension) project.getExtensions().getByName(REBEL_EXTENSION_NAME);
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

    conventionAwareRebelTask.getConventionMapping().map("addResourcesDirToRebelXml", new Callable<Object>() {
      public Object call() throws Exception {
        RebelPluginExtension rebelExtension = (RebelPluginExtension) project.getExtensions().getByName(REBEL_EXTENSION_NAME);
        if (rebelExtension.getAddResourcesDirToRebelXml() != null) {
          return rebelExtension.getAddResourcesDirToRebelXml();
        }
        else {
          return true;
        }
      }
    });

    conventionAwareRebelTask.getConventionMapping().map("showGenerated",  new Callable<Object>() {
      public Object call() throws Exception {
        RebelPluginExtension rebelExtension = (RebelPluginExtension) project.getExtensions().getByName(REBEL_EXTENSION_NAME);
        return rebelExtension.getShowGenerated() ? rebelExtension.getShowGenerated() : false;
      }
    });

    conventionAwareRebelTask.getConventionMapping().map("alwaysGenerate", new Callable<Object>() {
      public Object call() throws Exception {
        RebelPluginExtension rebelExtension = (RebelPluginExtension) project.getExtensions().getByName(REBEL_EXTENSION_NAME);
        if (rebelExtension.getAlwaysGenerate() != null) {
          return rebelExtension.getAlwaysGenerate();
        }
        else {
          return false;
        }
      }
    });
  }
}
