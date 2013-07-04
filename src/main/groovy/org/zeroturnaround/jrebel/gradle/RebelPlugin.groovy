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
import org.gradle.api.plugins.WarPlugin;

/**
 * The main entry-point for the JRebel Gralde plugin.
 * 
 * @author Igor Bljahhin
 */
public class RebelPlugin implements Plugin<Project> {

  /**
   * The name of the task that our plugin will define
   */
  public static final String GENERATE_REBEL_TASK_NAME = "generateRebel";
  
  public static final String REBEL_EXTENSION_NAME = "rebel";

  public void apply(Project project) {
    // by default, register a dummy task that reports missing JavaPlugin
    project.tasks.add(GENERATE_REBEL_TASK_NAME) << {
      throw new IllegalStateException(
          "generateRebel is only valid when JavaPlugin is aplied directly or indirectly " +
          "(via other plugins that apply it implicitly, like Groovy or War); please update your build");
    }
    // only configure the real one if JavaPlugin gets enabled (it is pulled in by Groovy, Scala, War, ...)
    project.getLogger().info("Registering deferred Rebel plugin configuration...");
    project.getPlugins().withType(JavaPlugin) { configure(project) };
  }

  private configure(Project project) {
    project.getLogger().info("Configuring Rebel plugin...");

    project.getExtensions().add(REBEL_EXTENSION_NAME, new RebelPluginExtension());

    // configure Rebel task
    RebelGenerateTask generateRebelTask = project.getTasks().replace(GENERATE_REBEL_TASK_NAME, RebelGenerateTask)
    
    // let everything be compiled and processed so that classes / resources directories are there
    generateRebelTask.dependsOn(project.getTasks().getByName("classes"));

    generateRebelTask.conventionMapping.rebelXmlDirectory = {
      RebelPluginExtension rebelExtension = (RebelPluginExtension) project.getExtenions().getByName(REBEL_EXTENSION_NAME);
      rebelExtension.getRebelXmlDirectory() ? rebelExtension.getRebelXmlDirectory() : project.sourceSets.main.output.classesDir
    }

    // set default value
    generateRebelTask.setPackaging(RebelGenerateTask.PACKAGING_TYPE_JAR);

    // if WarPlugin already applied, or if it is applied later than this plugin...
    project.getPlugins().withType(WarPlugin) {
      generateRebelTask.setPackaging(RebelGenerateTask.PACKAGING_TYPE_WAR);

      generateRebelTask.conventionMapping.warSourceDirectory = {
        RebelPluginExtension rebelExtension = (RebelPluginExtension) project.getExtenions().getByName(REBEL_EXTENSION_NAME);
        rebelExtension.getWarSourceDirectory() ? project.file(rebelExtension.getWarSourceDirectory()) : project.webAppDir;
      }
    }

    generateRebelTask.conventionMapping.addResourcesDirToRebelXml = {
      RebelPluginExtension rebelExtension = (RebelPluginExtension) project.getExtenions().getByName(REBEL_EXTENSION_NAME);
      rebelExtension.getAddResourcesDirToRebelXml() ? rebelExtension.getAddResourcesDirToRebelXml() : true;
    }

    generateRebelTask.conventionMapping.showGenerated = {
      RebelPluginExtension rebelExtension = (RebelPluginExtension) project.getExtenions().getByName(REBEL_EXTENSION_NAME);
      rebelExtension.getShowGenerated() ? rebelExtension.getShowGenerated() : false;
    }

    generateRebelTask.conventionMapping.alwaysGenerate = {
      RebelPluginExtension rebelExtension = (RebelPluginExtension) project.getExtenions().getByName(REBEL_EXTENSION_NAME);
      rebelExtension.getAlwaysGenerate() ? rebelExtension.getAlwaysGenerate() : false;
    }
  }
}
