package org.zeroturnaround.jrebel.gradle;

import java.io.File;
import java.lang.reflect.Method;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.bundling.War;
import org.zeroturnaround.jrebel.gradle.util.LoggerWrapper;

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
    try {
      Class<?> warPluginConventionClass = Class.forName("org.gradle.api.plugins.WarPluginConvention");
      Object warPluginConvention = project.getConvention().getPlugin(warPluginConventionClass);
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
