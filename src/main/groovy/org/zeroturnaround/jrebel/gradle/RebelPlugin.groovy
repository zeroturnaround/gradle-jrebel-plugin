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
package org.zeroturnaround.jrebel.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin


class RebelPlugin implements Plugin<Project> {

  private final static String GENERATE_REBEL = 'generateRebel'

  def void apply(Project project) {
    // by default, register a dummy task that reports missing JavaPlugin
    project.tasks.add(GENERATE_REBEL) << {
      throw new IllegalStateException(
          "generateRebel is only valid when JavaPlugin is aplied directly or indirectly " +
          "(via other plugins that apply it implicitly, like Groovy or War); please update your build")
    }
    // only configure the real one if JavaPlugin gets enabled (it is pulled in by Groovy, Scala, War, ...)
    project.logger.info "Registering deferred Rebel plugin configuration..."
    project.plugins.withType(JavaPlugin) { configure(project) }
  }

  private configure(Project project) {
    project.logger.info "Configuring Rebel plugin..."

    project.extensions.rebel = new RebelPluginExtension()

    // configure Rebel task
    RebelGenerateTask generateRebelTask = project.tasks.replace(GENERATE_REBEL, RebelGenerateTask)
    // let everything be compiled and processed so that classes / resources directories are there
    generateRebelTask.dependsOn(project.tasks.classes)

    generateRebelTask.conventionMapping.rebelXmlDirectory = {
      project.rebel.rebelXmlDirectory ? project.file(project.rebel.rebelXmlDirectory) : project.sourceSets.main.output.classesDir
    }

    // set default value
    generateRebelTask.conventionMapping.packaging = { "jar" }

    // if WarPlugin already applied, or if it is applied later than this plugin...
    project.plugins.withType(WarPlugin) {
      generateRebelTask.conventionMapping.packaging = { "war" }
      generateRebelTask.conventionMapping.warSourceDirectory = {
        project.rebel.warSourceDirectory ? project.file(project.rebel.warSourceDirectory) : project.webAppDir
      }
    }

    generateRebelTask.conventionMapping.addResourcesDirToRebelXml = {
      project.rebel.addResourcesDirToRebelXml ? project.rebel.addResourcesDirToRebelXml : "true"
    }

    generateRebelTask.conventionMapping.showGenerated = {
      project.rebel.showGenerated ? project.rebel.showGenerated : "false"
    }

    generateRebelTask.conventionMapping.alwaysGenerate = {
      project.rebel.alwaysGenerate ? project.rebel.alwaysGenerate : "false"
    }
  }
}
