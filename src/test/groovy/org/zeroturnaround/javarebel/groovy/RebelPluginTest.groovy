package org.zeroturnaround.javarebel.groovy


import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.plugins.jetty.JettyPlugin
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue


public class RebelPluginTest {

    @Test(expected = IllegalStateException)
    public void 'rebel plugin adds dummy task to project when no JavaPlugin applied'() {
        Project project = ProjectBuilder.builder().build()
        project.project.plugins.apply(RebelPlugin)

        def task = project.tasks.generateRebel
        assertTrue(task instanceof DefaultTask)
        try {
            task.execute()
        } catch (TaskExecutionException exc) {
            throw exc.cause
        }
    }

    @Test
    public void 'rebel plugin adds rebel task to project when JavaPlugin already applied'() {
        Project project = ProjectBuilder.builder().build()
        project.project.plugins.apply(JavaPlugin)
        project.project.plugins.apply(RebelPlugin)

        def task = project.tasks.generateRebel
        assertTrue(task instanceof RebelGenerateTask)
        assertTrue(task.packaging == 'jar')
        assertTrue(project.tasks.classes in task.dependsOn)
    }

    @Test
    public void 'rebel plugin adds rebel task to project after GroovyPlugin applied'() {
        Project project = ProjectBuilder.builder().build()
        project.project.plugins.apply(RebelPlugin)
        project.project.plugins.apply(GroovyPlugin)

        def task = project.tasks.generateRebel
        assertTrue(task instanceof RebelGenerateTask)
        assertTrue(task.packaging == 'jar')
        assertTrue(project.tasks.classes in task.dependsOn)
    }

    @Test
    public void 'rebel plugin gets war packaging when WarPlugin already applied'() {
        Project project = ProjectBuilder.builder().build()
        project.project.plugins.apply(WarPlugin)
        project.project.plugins.apply(RebelPlugin)

        def task = project.tasks.generateRebel
        assertTrue(task instanceof RebelGenerateTask)
        assertTrue(task.packaging == 'war')
        assertTrue(project.tasks.classes in task.dependsOn)
    }

    @Test
    public void 'rebel plugin gets war packaging after JettyPlugin applied'() {
        Project project = ProjectBuilder.builder().build()
        project.project.plugins.apply(RebelPlugin)
        project.project.plugins.apply(JettyPlugin)

        def task = project.tasks.generateRebel
        assertTrue(task instanceof RebelGenerateTask)
        assertTrue(task.packaging == 'war')
        assertTrue(project.tasks.classes in task.dependsOn)
    }
}
