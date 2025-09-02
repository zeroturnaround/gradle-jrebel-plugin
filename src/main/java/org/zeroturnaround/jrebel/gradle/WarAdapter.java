package org.zeroturnaround.jrebel.gradle;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.bundling.War;
import org.gradle.util.GradleVersion;
import org.zeroturnaround.jrebel.gradle.util.LoggerWrapper;

import java.io.File;
import java.lang.reflect.Method;

public class WarAdapter {

  private final Project project;
  final LoggerWrapper log;

  public WarAdapter(Project project) {
    this.project = project;
    this.log = new LoggerWrapper(project.getLogger());
  }

  public File getWebAppDir() {
    File webAppDir = getWebAppDirFromTasks();

    if (webAppDir == null) {
      webAppDir = getWebAppDirFromConvention();
    }

    return webAppDir;
  }

  // Legacy - from WarPluginConvention
  private File getWebAppDirFromConvention() {
    if (GradleVersion.current().compareTo(GradleVersion.version("9.0")) >= 0) {
      log.debug("Couldn't obtain webappdir from convention: no API in Gradle " + GradleVersion.current());
      return null;
    }
    try {
      Class<?> warPluginConventionClass = Class.forName("org.gradle.api.plugins.WarPluginConvention");
      Object convention = project.getClass().getMethod("getConvention").invoke(project);
      Object warPluginConvention = convention.getClass().getMethod("getPlugin", Class.class).invoke(convention, warPluginConventionClass);
      File webAppDir = (File) warPluginConventionClass.getMethod("getWebAppDir").invoke(warPluginConvention);
      log.debug("webappdir from convention: " + webAppDir);
      if (webAppDir != null) {
        return webAppDir;
      }
    }
    catch (Exception ex) {
      log.debug("Couldn't to obtain webappdir from convention: " + ex);
    }

    return null;
  }

  // Current - from War task
  private File getWebAppDirFromTasks() {
    try {
      Method getWebAppDir = War.class.getMethod("getWebAppDirectory");
      for (War warTask : project.getTasks().withType(War.class)) {
        try {
          DirectoryProperty webAppDirectoryProperty = (DirectoryProperty) getWebAppDir.invoke(warTask);
          File webAppDir = webAppDirectoryProperty.getAsFile().get();
          log.debug("webappdir from task: " + webAppDir);
          if (webAppDir != null) {
            // Just return the first one available for now.
            return webAppDir;
          }
        }
        catch (Exception ex) {
          // Ignored
          ex.printStackTrace();
        }
      }
    }
    catch (Exception ex) {
      log.debug("Couldn't to obtain webappdir from task: " + ex);
    }

    return null;
  }

}
