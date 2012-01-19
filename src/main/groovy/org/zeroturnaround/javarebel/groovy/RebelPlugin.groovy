package org.zeroturnaround.javarebel.groovy


import org.gradle.api.Plugin;
import org.gradle.api.Project;

class RebelPlugin implements Plugin<Project> {
	def void apply(Project project) {
		project.logger.info "Configuring Rebel plugin..."

		project.extensions.rebel = new RebelPluginExtension()

		// configure Rebel task
		RebelGenerateTask generateRebelTask = project.tasks.add('generateRebel', RebelGenerateTask)
        
        // set default value
        generateRebelTask.conventionMapping.packaging = { "jar" }
    
        // check if WarPlugin is loaded to find out the type of the project 
        for (plugin in project.plugins) {
          if (plugin.class.name == "org.gradle.api.plugins.WarPlugin") {
            generateRebelTask.conventionMapping.packaging = { "war" }
          }
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