package org.zeroturnaround.javarebel.groovy



import static org.junit.Assert.assertTrue

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

public class RebelPluginTest {
	@Test
	public void rebelPluginAddsRebelTaskToProject() {
		Project project = ProjectBuilder.builder().build()
		project.project.plugins.apply(RebelPlugin.class)

		assertTrue(project.tasks.generateRebel instanceof RebelGenerateTask)
	}
}
