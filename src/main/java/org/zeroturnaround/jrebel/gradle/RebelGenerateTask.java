/**
   Copyright (C) 2012 ZeroTurnaround <support@zeroturnaround.com>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.zeroturnaround.jrebel.gradle;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.BuildException;
import org.gradle.util.GradleVersion;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspath;
import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;
import org.zeroturnaround.jrebel.gradle.model.RebelWeb;
import org.zeroturnaround.jrebel.gradle.util.FileUtil;
import org.zeroturnaround.jrebel.gradle.util.LoggerWrapper;

public class RebelGenerateTask extends DefaultTask {

  public static final String PACKAGING_TYPE_JAR = "jar";

  public static final String PACKAGING_TYPE_WAR = "war";

  public static final String GRADLE_PLUGIN_VERSION = extractVersionOfPluginFromManifest();

  public static final String GRADLE_VERSION = GradleVersion.current().getVersion();

  private LoggerWrapper log = new LoggerWrapper(getProject().getLogger());

  private String packaging;

  private RebelClasspath classpath;

  private RebelWeb web;

  private RebelWar war;

  private RebelMainModel rebelModel;

  private boolean skipWritingRebelXml;

  private boolean alwaysGenerate;

  private List<File> defaultClassesDirectories;

  private File defaultResourcesDirectory;

  private File defaultWebappDirectory;

  private boolean showGenerated;

  private File rebelXmlDirectory;

  private boolean isPluginConfigured;

  private String configuredRootPath;

  /**
   * XXX -- i'm not sure about this property at all. this is used in fixPath, so i don't dare to delete it as well.. ask
   *        Rein who probably originally introduced it to Maven plugin where it was copy-pasted from.
   */
  private File configuredRelativePath;



  public String getConfiguredRootPath() {
    return configuredRootPath;
  }

  public void setConfiguredRootPath(String path) {
    this.configuredRootPath = path;
  }

  public File getConfiguredRelativePath() {
    return configuredRelativePath;
  }

  public void setConfiguredRelativePath(File path) {
    this.configuredRelativePath = path;
  }

  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  public boolean getShowGenerated() {
    return showGenerated;
  }

  public void setShowGenerated(boolean showGenerated) {
    this.showGenerated = showGenerated;
  }

  public RebelClasspath getClasspath() {
    return classpath;
  }

  public void setClasspath(RebelClasspath path) {
    this.classpath = path;
  }

  public RebelWeb getWeb() {
    return web;
  }

  public void setWeb(RebelWeb web) {
    this.web = web;
  }

  public RebelWar getWar() {
    return war;
  }

  public void setWar(RebelWar _war) {
    this.war = _war;
  }

  public boolean getAlwaysGenerate() {
    return alwaysGenerate;
  }

  public void setAlwaysGenerate(boolean alwaysGenerate) {
    this.alwaysGenerate = alwaysGenerate;
  }

  public List<File> getDefaultClassesDirectory() {
    return defaultClassesDirectories;
  }

  public File getDefaultResourcesDirectory() {
    return defaultResourcesDirectory;
  }

  public File getDefaultWebappDirectory() {
    return defaultWebappDirectory;
  }

  public File getRebelXmlDirectory() {
    return rebelXmlDirectory;
  }

  /**
   * Getter for the functional tests to examine the model
   */
  public RebelMainModel getRebelModel() {
    return rebelModel;
  }

  /**
   * Only for automated tests! Tests should not try to write the actual file.
   */
  public void skipWritingRebelXml() {
    this.skipWritingRebelXml = true;
  }

  /**
   * The RebelPlugin#configure block has been executed
   */
  public void setPluginConfigured() {
    this.isPluginConfigured = true;
  }

  private File getRebelXml() {
    if (rebelXmlDirectory == null) {
      return null;
    }
    return new File(rebelXmlDirectory, "rebel.xml");
  }

  /**
   * The actual invocation of our plugin task. Will construct the in-memory model (RebelXmlBuilder),
   * generate the XML output based on it and write the XML into a file-system file (rebel.xml).
   */
  @TaskAction
  public void generate() {
    // Only able to run if the 'RebelPlugin#configure' block has been executed, i.e. if the Java Plugin has been added.
    if (!isPluginConfigured) {
      throw new IllegalStateException(
        "generateRebel is only valid when JavaPlugin is applied directly or indirectly " +
        "(via other plugins that apply it implicitly, like Groovy or War); please update your build"
      );
    }

    propagateConventionMappingSettings();

    log.info("rebel.alwaysGenerate = " + alwaysGenerate);
    log.info("rebel.showGenerated = " + showGenerated);
    log.info("rebel.rebelXmlDirectory = " + rebelXmlDirectory);
    log.info("rebel.packaging = " + packaging);
    log.info("rebel.war = " + war);
    log.info("rebel.web = " + web);
    log.info("rebel.classpath = " + classpath);
    log.info("rebel.defaultClassesDirectories = " + defaultClassesDirectories);
    log.info("rebel.defaultResourcesDirectory = " + defaultResourcesDirectory);
    log.info("rebel.defaultWebappDirectory = " + defaultWebappDirectory);
    log.info("rebel.configuredRootPath = " + configuredRootPath);
    log.info("rebel.configuredRelativePath = " + configuredRelativePath);

    File rebelXmlFile = getRebelXml();
    File buildXmlFile = getProject().getBuildFile();

    if (!alwaysGenerate && (rebelXmlFile != null) && rebelXmlFile.exists() && (buildXmlFile != null) && buildXmlFile.exists() && rebelXmlFile.lastModified() > buildXmlFile.lastModified()) {
      return;
    }

    rebelModel = new RebelModelBuilder(
        getProject(),
        getPackaging(),
        classpath,
        web,
        war,
        defaultClassesDirectories,
        defaultResourcesDirectory,
        defaultWebappDirectory,
        configuredRootPath,
        configuredRelativePath,
        getProject().getProjectDir()
    ).build();

    if (rebelModel != null && !skipWritingRebelXml) {
      generateRebelXml(rebelXmlFile);
    }
  }

  private static String extractVersionOfPluginFromManifest() {
    String result = RebelGenerateTask.class.getPackage().getImplementationVersion();
    return result == null ? "Unknown" : result;
  }

  private void generateRebelXml(File rebelXmlFile) {
    // TODO replacement of those placeholders does not work and probably has never worked (probably copy-pasted from maven plugin). REPLACE!
    log.info("Processing ${project.group}:${project.name} with packaging " + getPackaging());
    log.info("Generating \"${rebelXmlFile}\"...");

    // Do generate the rebel.xml
    try {
      String xmlFileContents = getRebelModel().toXmlString();

      // Print generated rebel.xml out to console if user wants to see it
      if (getShowGenerated()) {
        System.out.println(xmlFileContents);
      }

      // Write out the rebel.xml file
      rebelXmlFile.getParentFile().mkdirs();
      FileUtil.writeToFile(rebelXmlFile, xmlFileContents);
    }
    catch (IOException e) {
      throw new BuildException("Failed writing \"${rebelXmlFile}\"", e);
    }
  }

  /* ====================================================================================================
   *   Properties intercepted by Gradle's convention-mapping byte code magic. These methods will actually
   *   be intercepted and return values set by the callback set up in RebelPlugin#configure.
   *
   *   These properties are cached into local variables to lessen the magic. See propagateConventionMappingSettings().
   */

  public static final String NAME_DEFAULT_CLASSES_DIRECTORIES = "defaultClassesDirectories$MAGIC";

  public static final String NAME_DEFAULT_RESOURCES_DIRECTORY = "defaultResourcesDirectory$MAGIC";

  public static final String NAME_DEFAULT_WEBAPP_DIRECTORY = "defaultWebappDirectory$MAGIC";

  public static final String NAME_REBEL_XML_DIRECTORY = "rebelXmlDirectory$MAGIC";

  public List<File> getDefaultClassesDirectories$MAGIC() {
    return null;
  }

  public File getDefaultResourcesDirectory$MAGIC() {
    return null;
  }

  public File getDefaultWebappDirectory$MAGIC() {
    return null;
  }

  public File getRebelXmlDirectory$MAGIC() {
    return null;
  }

  /**
   * Let the convention-mappings propagate its settings to me through the magic getters,
   * save copies of them locally into normal instance variables.
   *
   * (public only for unit tests)
   */
  public void propagateConventionMappingSettings() {
    defaultClassesDirectories = getDefaultClassesDirectories$MAGIC();
    defaultResourcesDirectory = getDefaultResourcesDirectory$MAGIC();
    defaultWebappDirectory = getDefaultWebappDirectory$MAGIC();
    rebelXmlDirectory = getRebelXmlDirectory$MAGIC();
  }

  // ========== END OF convention-mapping's intercepted magic methods

}