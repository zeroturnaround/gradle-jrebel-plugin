package org.zeroturnaround.javarebel.groovy


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class LiveRebelGenerateTask extends DefaultTask {

    @TaskAction
    def generate() {
        println "Execute liverebel:generate..."
    }
}
