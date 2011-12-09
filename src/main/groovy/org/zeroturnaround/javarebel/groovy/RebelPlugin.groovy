package org.zeroturnaround.javarebel.groovy


import org.gradle.api.Plugin;
import org.gradle.api.Project;

class RebelPlugin implements Plugin<Project> {
	def void apply(Project project) {
		project.logger.info "Configuring Rebel plugin..."

		project.extensions.rebel = new RebelPluginExtension()

		// configure Rebel task
		RebelGenerateTask generateRebelTask = project.tasks.add('generateRebel', RebelGenerateTask)

		generateRebelTask.conventionMapping.packaging = {
			project.rebel.packaging ? project.rebel.packaging : "jar"
		}

		generateRebelTask.conventionMapping.rebelXmlDirectory = {
			project.rebel.rebelXmlDirectory ? project.file(project.rebel.rebelXmlDirectory) : project.file("${project.buildDir}" + File.separator + "classes")
		}

		generateRebelTask.conventionMapping.warSourceDirectory = {
			project.rebel.warSourceDirectory ? project.file(project.rebel.warSourceDirectory) : project.file("${project.buildFile.parentFile.absolutePath}" + File.separator + "src" + File.separator + "main" + File.separator + "webapp")
		}

		generateRebelTask.conventionMapping.addResourcesDirToRebelXml = {
			project.rebel.addResourcesDirToRebelXml ? project.rebel.addResourcesDirToRebelXml : "false"
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