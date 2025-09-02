package org.zeroturnaround.jrebel.gradle.util;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.tooling.BuildException;
import org.gradle.util.GradleVersion;

public class JavaSourceSetContainerAccessor {

  public static SourceSetContainer getSourceSets(Project project) {
    if (GradleVersion.current().compareTo(GradleVersion.version("7.1")) >= 0) {
      return project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
    }
    else {
      try {
        Object convention = project.getClass().getMethod("getConvention").invoke(project);
        Class<?> javaPluginConventionClass = Class.forName("org.gradle.api.plugins.JavaPluginConvention");
        Object javaPluginConvention = convention.getClass().getMethod("getPlugin", Class.class).invoke(convention, javaPluginConventionClass);
        return (SourceSetContainer) javaPluginConvention.getClass().getMethod("getSourceSets").invoke(javaPluginConvention);
      }
      catch (ReflectiveOperationException e) {
        throw new BuildException("Failed to access Java plugin convention", e);
      }
    }
  }
}
