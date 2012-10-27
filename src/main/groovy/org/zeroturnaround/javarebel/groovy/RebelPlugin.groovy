package org.zeroturnaround.javarebel.groovy


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.WarPlugin;

class RebelPlugin implements Plugin<Project> {
	def void apply(Project project) {
		project.logger.info "Configuring Rebel plugin..."

		project.extensions.rebel = new RebelPluginExtension()

		// configure Rebel task
		RebelGenerateTask generateRebelTask = project.tasks.add('generateRebel', RebelGenerateTask)
        
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

		// configure LiveRebel task
		project.tasks.add('generateLiveRebel', LiveRebelGenerateTask)
	}
}