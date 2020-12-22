package org.zeroturnaround.jrebel.gradle.util;

import java.util.StringTokenizer;

import org.gradle.api.Project;

public class RemoteUtil {

  public static String getRemoteId(Project project, String sourceSetName) {
    String projectPath = project.getPath();
    StringTokenizer tokenizer = new StringTokenizer(projectPath, ":");
    StringBuilder remoteIdBuilder = new StringBuilder();

    boolean first = true;
    if (projectPath.charAt(0) == ':') {
      remoteIdBuilder.append(project.getRootProject().getName());
      first = false;
    }

    while (tokenizer.hasMoreTokens()) {
      if (!first) {
        remoteIdBuilder.append('.');
      } else {
        first = false;
      }
      remoteIdBuilder.append(tokenizer.nextToken());
    }

    if (sourceSetName != null) {
      remoteIdBuilder.append('.').append(sourceSetName);
    }
    return remoteIdBuilder.toString();
  }
}
