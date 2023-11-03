package org.zeroturnaround.jrebel.gradle;

import static org.zeroturnaround.jrebel.gradle.LegacyRebelPlugin.GENERATE_REBEL_TASK_NAME;
import static org.zeroturnaround.jrebel.gradle.LegacyRebelPlugin.REBEL_EXTENSION_NAME;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.util.GradleVersion;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslMain;
import org.zeroturnaround.jrebel.gradle.util.LoggerWrapper;

import groovy.lang.Closure;

public class IncrementalRebelPlugin implements Plugin<Project> {

  private LoggerWrapper log;

  @Override
  public void apply(final Project project) {
    log = new LoggerWrapper(project.getLogger());

    log.info("Configuring Rebel plugin for project " + project.getName());

    project.getExtensions().create(REBEL_EXTENSION_NAME, RebelDslMain.class);

    project.getPlugins().withType(JavaBasePlugin.class).all(new Action<JavaBasePlugin>() {
      @Override
      public void execute(JavaBasePlugin javaPlugin) {
        getJavaSourceSets(project).all(new Action<SourceSet>() {
          @Override
          public void execute(final SourceSet sourceSet) {
            log.info("Creating task for sourceSet " + sourceSet.getName());
            registerTaskForSourceSet(project, sourceSet);
          }
        });
      }
    });
  }

  private void registerTaskForSourceSet(final Project project, final SourceSet sourceSet) {
    final SourceSetDefaults sourceSetDefaults = new SourceSetDefaults(
        project.provider(new Callable<Collection<File>>() {
          @Override
          public Collection<File> call() throws Exception {
            return sourceSet.getOutput().getClassesDirs().getFiles();
          }
        }),
        project.provider(new Callable<File>() {
          @Override
          public File call() throws Exception {
            return sourceSet.getOutput().getResourcesDir();
          }
        }),
        sourceSet.getName(),
        sourceSet.getName()
    );

    // "generateRebelMain", "generateRebelTest", "generateRebelIntegrationTest", etc.
    String taskName = sourceSet.getTaskName(GENERATE_REBEL_TASK_NAME, null);
    final IncrementalRebelGenerateTask generateRebelSourceSet = project.getTasks().create(taskName,
        IncrementalRebelGenerateTask.class,
        new Action<IncrementalRebelGenerateTask>() {
          @Override
          public void execute(IncrementalRebelGenerateTask task) {
            task.configureSourceSet(sourceSetDefaults);
          }
        });
    generateRebelSourceSet.setDescription("Generate JRebel xml configuration for this SourceSet");

    if (sourceSet.getName().equals("main")) {
      Copy processResourcesTask = (Copy) project.getTasks().getByName(sourceSet.getProcessResourcesTaskName());
      processResourcesTask.dependsOn(generateRebelSourceSet);
      processResourcesTask.from(new Closure<File>(generateRebelSourceSet) {
        @Override
        public File call() {
          return generateRebelSourceSet.getJRebelBuildDir();
        }
      });

      // Backwards compatibility as old scripts may do "x.dependsOn(generateRebel)"
      Task defaultTask = project.getTasks().create(GENERATE_REBEL_TASK_NAME);
      defaultTask.setDescription("Generate JRebel xml configuration for the main SourceSet");
      defaultTask.dependsOn(generateRebelSourceSet);
    }
  }

  private static SourceSetContainer getJavaSourceSets(final Project project) {
    try {
      return project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
    }
    catch (Throwable t) {
      if (GradleVersion.current().compareTo(GradleVersion.version("7.1")) < 0) {
        return project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
      }
      else {
        throw new RuntimeException(t);
      }
    }
  }
}
