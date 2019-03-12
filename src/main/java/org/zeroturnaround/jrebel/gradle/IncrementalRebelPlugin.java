package org.zeroturnaround.jrebel.gradle;

import static org.zeroturnaround.jrebel.gradle.LegacyRebelPlugin.GENERATE_REBEL_TASK_NAME;
import static org.zeroturnaround.jrebel.gradle.LegacyRebelPlugin.REBEL_EXTENSION_NAME;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslMain;
import org.zeroturnaround.jrebel.gradle.util.LoggerWrapper;

public class IncrementalRebelPlugin implements Plugin<Project> {

  private LoggerWrapper log;

  @Override
  public void apply(final Project project) {
    log = new LoggerWrapper(project.getLogger());

    project.getExtensions().create(REBEL_EXTENSION_NAME, RebelDslMain.class);

    IncrementalRebelGenerateTask generateTask = project.getTasks().create(GENERATE_REBEL_TASK_NAME, IncrementalRebelGenerateTask.class);
    generateTask.setDescription("Generate rebel.xml mappings file to use this project with JRebel.");

    log.info("Configuring Rebel plugin...");
  }
}
