package org.zeroturnaround.jrebel.gradle.util;

import groovy.lang.Closure;
import org.gradle.tooling.BuildException;
import org.gradle.util.GradleVersion;
import org.gradle.util.internal.ConfigureUtil;

public class ConfigureUtilAdapter {
  public static <T> T configure(Closure configureClosure, T target) {
    if (GradleVersion.current().compareTo(GradleVersion.version("8.5")) >= 0) {
      return ConfigureUtil.configure(configureClosure, target);
    }
    else {
      try {
        return (T) Class.forName("org.gradle.util.ConfigureUtil")
            .getDeclaredMethod("configure", Closure.class, Object.class)
            .invoke(null, configureClosure, target);
      }
      catch (ReflectiveOperationException e) {
        throw new BuildException("Unable to configure Gradle API", e);
      }
    }
  }
}
