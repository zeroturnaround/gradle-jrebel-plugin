package org.zeroturnaround.jrebel.gradle;

import java.io.File;
import java.util.Collection;

import org.gradle.api.provider.Provider;

public class SourceSetDefaults {

  public final Provider<Collection<File>> classesDirs;
  public final Provider<File> resourcesDir;
  public final String xmlOutputDirName;
  public final String remoteIdSuffix;

  public SourceSetDefaults(Provider<Collection<File>> classesDirs, Provider<File> resourcesDir,
                           String xmlOutputDirName, String remoteIdSuffix) {
    if (classesDirs == null || resourcesDir == null || xmlOutputDirName == null || remoteIdSuffix == null) {
      throw new NullPointerException("" +
          "SourceSetDefaults(" + classesDirs + ", " + resourcesDir + ", " + xmlOutputDirName + ", " + remoteIdSuffix + ")");
    }
    this.classesDirs = classesDirs;
    this.resourcesDir = resourcesDir;
    this.xmlOutputDirName = xmlOutputDirName;
    this.remoteIdSuffix = remoteIdSuffix;
  }
}
