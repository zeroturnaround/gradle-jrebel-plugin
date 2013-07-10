/**
 *  Copyright (C) 2012 ZeroTurnaround <support@zeroturnaround.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.zeroturnaround.jrebel.gradle;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.BuildException;
import org.gradle.api.logging.Logger;

import org.zeroturnaround.jrebel.gradle.model.RebelClasspath;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspathResource;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;
import org.zeroturnaround.jrebel.gradle.model.RebelWeb;
import org.zeroturnaround.jrebel.gradle.model.RebelWebResource;
import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;
import org.zeroturnaround.jrebel.gradle.util.FileUtil;

public class RebelGenerateTask extends DefaultTask {
    
  public final static String PACKAGING_TYPE_JAR = "jar";
    
  public final static String PACKAGING_TYPE_WAR = "war";
  
  private Logger log = getProject().getLogger(); 
    
  /*
   * User-defined configuration options that are propagated here from the RebelPluginExtension via RebelPlugin#configure
   * and Gradle's conventionMapping mechanism
   * 
   * NB! These property names are also used by the Gradle's Conventions magic, do **NOT** rename them!!
   * If you still have to, also update the corresponding string constant or things will break for sure!
   * See their usage in RebelPlugin.
   */
  
  public static final String NAME_ADD_RESOURCES_DIR_TO_REBEL_XML = "addResourcesDirToRebelXml";
  
  private Boolean addResourcesDirToRebelXml;

  public static final String NAME_ALWAYS_GENERATE = "alwaysGenerate";
  
  private Boolean alwaysGenerate;
  
  private String packaging;
  
  private File rebelXmlDirectory;

  public static final String NAME_REBEL_XML_DIRECTORY = "rebelXmlDirectory";
  
  private Boolean showGenerated;
  
  public static final String NAME_SHOW_GENERATED = "showGenerated";
  
  private File warSourceDirectory;

  public static final String NAME_WAR_SOURCE_DIRECTORY = "warSourceDirectory";
  
  private RebelWeb web;
  
  private File webappDirectory;
  
  private RebelWar war;

  public static final String NAME_WAR = "war";
   
  // === interal properties of the task
  
  private RebelMainModel rebelModel;
  
  private boolean skipWritingRebelXml;
  
  // =========== START OF WEIRD STUFF ===============================================

  // XXX most of this is just leftovers from refactoring and will eventually be removed
  
  // Stuff starting from here is the old copy-pasted model from the Maven plugin. These configuration objects are available
  // to the end-user via RebelPluginExtension only theoretically (they are never documented and only available secretly..
  // probably never used in practice!
  // 
  // - see how the appropriate DSL-aware model should replace that model
  // - propagate from RebelPluginExtension to here through the ConventionMappings if there's need for it
  
  
  private String configuredRootPath;
  
  private File configuredRelativePath;
  
  private File configuredResourcesDirectory;
  
  private File configuredClassesDirectory;
  
  private RebelClasspath configuredResourcesClasspath;
  
  private RebelClasspath configuredClasspath;
  
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
  
  public File getConfiguredResourcesDirectory() {
    return configuredResourcesDirectory;
  }

  public void setConfiguredResourcesDirectory(File dir) {
    this.configuredResourcesDirectory = dir;
  }

  public File getConfiguredClassesDirectory() {
    return configuredClassesDirectory;
  }
  
  public void setConfiguredClassesDirectory(File dir) {
    this.configuredClassesDirectory = dir;
  }
  
  public RebelClasspath getConfiguredResourcesClasspath() {
    return configuredResourcesClasspath;
  }
  
  public void setConfiguredResourcesClasspath(RebelClasspath rebelPath) {
    this.configuredResourcesClasspath = rebelPath;
  }

  public RebelClasspath getConfiguredClasspath() {
    return configuredClasspath;
  }
  
  public void setConfiguredClasspath(RebelClasspath path) {
    this.configuredClasspath = path;
  }
  
  // ============================= END OF WEIRD STUFF =========================================
  
  public Boolean getAddResourcesDirToRebelXml() {
    return addResourcesDirToRebelXml;
  }

  public void setAddResourcesDirToRebelXml(Boolean addResourcesDirToRebelXml) {
    this.addResourcesDirToRebelXml = addResourcesDirToRebelXml;
  }

  public Boolean getAlwaysGenerate() {
    return alwaysGenerate;
  }

  public void setAlwaysGenerate(Boolean alwaysGenerate) {
    this.alwaysGenerate = alwaysGenerate;
  }

  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  public File getRebelXmlDirectory() {
    return rebelXmlDirectory;
  }

  public void setRebelXmlDirectory(File rebelXmlDirectory) {
    this.rebelXmlDirectory = rebelXmlDirectory;
  }

  public Boolean getShowGenerated() {
    return showGenerated;
  }

  public void setShowGenerated(Boolean showGenerated) {
    this.showGenerated = showGenerated;
  }

  public File getWarSourceDirectory() {
    return warSourceDirectory;
  }

  public void setWarSourceDirectory(File warSourceDirectory) {
    this.warSourceDirectory = warSourceDirectory;
  }

  public RebelWeb getWeb() {
    return web;
  }

  public void setWeb(RebelWeb web) {
    this.web = web;
  }

  public File getWebappDirectory() {
    return webappDirectory;
  }

  public void setWebappDirectory(File webappDirectory) {
    this.webappDirectory = webappDirectory;
  }
  
  public RebelWar getWar() {
    return war;
  }
 
  public void setWar(RebelWar _war) {
    this.war = _war;
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
   * The actual invocation of our plugin task. Will construct the in-memory model (RebelXmlBuilder),
   * generate the XML output based on it and write the XML into a file-system file (rebel.xml). 
   */
  @TaskAction
  public void generate() {
    // TODO replace this by a proper rebelTask.toString method!
    log.info("rebel.alwaysGenerate = " + getAlwaysGenerate());
    log.info("rebel.showGenerated = " + getShowGenerated());
    log.info("rebel.rebelXmlDirectory = " + getRebelXmlDirectory());
    log.info("rebel.warSourceDirectory = " + getWarSourceDirectory());
    log.info("rebel.addResourcesDirToRebelXml = " + getAddResourcesDirToRebelXml());
    log.info("rebel.packaging = " + getPackaging());
    log.info("rebel.war = " + getWar());
    
    // find rebel.xml location
    File rebelXmlFile = null;
  
    if (getRebelXmlDirectory() != null) {
      rebelXmlFile = new File(getRebelXmlDirectory(), "rebel.xml");
    }
  
    // find build.gradle location
    File buildXmlFile = getProject().getBuildFile();
  
    if (!getAlwaysGenerate() && (rebelXmlFile != null) && rebelXmlFile.exists() && (buildXmlFile != null) && buildXmlFile.exists() && rebelXmlFile.lastModified() > buildXmlFile.lastModified()) {
      return;
    }
  
    // find the type of the project
    if (getPackaging().equals(PACKAGING_TYPE_JAR)) {
      rebelModel = buildModelForJar();
    }
    else if (getPackaging().equals(PACKAGING_TYPE_WAR)) {
      rebelModel = buildModelForWar();
    }
  
    if (rebelModel != null && !skipWritingRebelXml) {
      generateRebelXml(rebelXmlFile);
    }
  }

  /**
   * Construct a builder for jar projects
   */
  private RebelMainModel buildModelForJar() {
    RebelMainModel model = new RebelMainModel();
    buildClasspath(model);
    return model;
  }

  /**
   * Construct a builder for war projects
   */
  private RebelMainModel buildModelForWar() {
    RebelMainModel model = new RebelMainModel();
  
    buildWeb(model);
    buildClasspath(model);
  
    RebelWar war = getWar();
    // fix the path on the RebelWar object (whoooh...not nicest and not the nicest placing)
    if (war != null && war.getPath() != null) {
      war.setOriginalPath(war.getPath());
      war.setPath(fixFilePath(war.getPath()));
      model.setWar(war);
    }
  
    return model;
  }

  private void buildClasspath(RebelMainModel model) {
    boolean addDefaultAsFirst = true;
    RebelClasspathResource defaultClasspath = null;
    RebelClasspath classpath = getConfiguredClasspath();
  
    // check if there is a element with no dir/jar/dirset/jarset set. if there
    // is then don't put default classpath as
    // first but put it where this element was.
  
    if (classpath != null) {
      RebelClasspathResource[] resources = classpath.getResources();
  
      if (resources != null && resources.length > 0) {
        for (int i = 0; i < resources.length; i++) {
          RebelClasspathResource r = resources[i];
  
          if (!r.isTargetSet()) {
            addDefaultAsFirst = false;
            defaultClasspath = r;
            break;
          }
        }
      }
    }
  
    if (addDefaultAsFirst) {
      buildDefaultClasspath(model, defaultClasspath);
    }
  }

  private void buildDefaultClasspath(RebelMainModel model, RebelClasspathResource defaultClasspath) throws BuildException {
    if (getAddResourcesDirToRebelXml()) {
      buildDefaultClasspathResources(model);
    }
  
    // project output directory
    RebelClasspathResource r = new RebelClasspathResource();
    r.setDirectory(fixFilePath(getClassesDirectory()));
    if (!new File(r.getDirectory()).isDirectory()) {
      return;
    }
  
    if (defaultClasspath != null) {
      r.setIncludes(defaultClasspath.getIncludes());
      r.setExcludes(defaultClasspath.getExcludes());
    }
  
    model.addClasspathDir(r);
  }

  private void buildDefaultClasspathResources(RebelMainModel model) throws BuildException {
      RebelClasspathResource r = new RebelClasspathResource();
      r.setDirectory(fixFilePath(getResourcesDirectory()));
      if (!new File(r.getDirectory()).isDirectory()) {
        return;
      }
  
      RebelClasspath resourcesClasspath = getConfiguredResourcesClasspath();
      if (resourcesClasspath != null) {
        // XXX TODO TODO TODO it seems that this code has never been working.. it does not even have correct typing! review!
  //      r.setIncludes(resourcesClasspath.getIncludes());
  //      r.setExcludes(resourcesClasspath.getExcludes());
      }
      model.addClasspathDir(r);
    }

  /**
   * Build the model for the <web> element in rebel.xml
   */
  private void buildWeb(RebelMainModel model) {
    boolean addDefaultAsFirst = true;
    RebelWebResource defaultWeb = null;
  
    if (web != null) {
      RebelWebResource[] resources = web.getResources();
  
      if (resources != null && resources.length > 0) {
        for (int i = 0; i < resources.length; i++) {
          RebelWebResource r = resources[i];
  
          if (r.getDirectory() == null && r.getTarget() == null) {
            defaultWeb = r;
            addDefaultAsFirst = false;
            break;
          }
        }
      }
    }
  
    if (addDefaultAsFirst) {
      buildDefaultWeb(model, defaultWeb);
    }
  
    if (web != null) {
      RebelWebResource[] resources = web.getResources();
      if (resources != null && resources.length > 0) {
        for (int i = 0; i < resources.length; i++) {
          RebelWebResource r = resources[i];
          if (r.getDirectory() == null && r.getTarget() == null) {
            buildDefaultWeb(model, r);
            continue;
          }
          r.setDirectory(fixFilePath(r.getDirectory()));
          model.addWebResource(r);
        }
      }
    }
  }

  private void buildDefaultWeb(RebelMainModel model, RebelWebResource defaultWeb) {
    RebelWebResource r = new RebelWebResource();
    r.setTarget("/");
    r.setDirectory(fixFilePath(getWarSourceDirectory()));
  
    if (defaultWeb != null) {
      r.setIncludes(defaultWeb.getIncludes());
      r.setExcludes(defaultWeb.getExcludes());
    }
  
    model.addWebResource(r);
  }

  private void generateRebelXml(File rebelXmlFile) {
    // TODO seems that those placeholders are not replaced (at least not when running tests)
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

  private String fixFilePath(File file) {
    File baseDir = getProject().getProjectDir();

    if (file.isAbsolute() && !FileUtil.isRelativeToPath(new File(baseDir, getRelativePath()), file)) {
      return StringUtils.replace(FileUtil.getCanonicalPath(file), "\\", "/");
    }

    if (!file.isAbsolute()) {
      file = new File(baseDir, file.getPath());
    }

    String relative = FileUtil.getRelativePath(new File(baseDir, getRelativePath()), file);
    
    if (!(new File(relative)).isAbsolute()) {
      return StringUtils.replace(getRootPath(), "\\", "/") + "/" + relative;
    }

    // relative path was outside baseDir

    // if root path is absolute then try to get a path relative to root
    if ((new File(getRootPath())).isAbsolute()) {
      String s = FileUtil.getRelativePath(new File(getRootPath()), file);

      if (!(new File(s)).isAbsolute()) {
        return StringUtils.replace(getRootPath(), "\\", "/") + "/" + s;
      }
      else {
        // root path and the calculated path are absolute, so
        // just return calculated path
        return s;
      }
    }

    // return absolute path to file
    return StringUtils.replace(file.getAbsolutePath(), "\\", "/");
  }

  private String fixFilePath(String path) {
    return fixFilePath(new File(path));
  }

  private File getClassesDirectory() {
    if (getConfiguredClassesDirectory() != null) {
      return getConfiguredClassesDirectory();
    }
    else {
      return getSourceSets().getByName("main").getOutput().getClassesDir();
    }
  }
  
  /**
   * Tiny helper to get Gradle JavaPlugin's SourceSetContainer (knows about project paths)
   */
  private SourceSetContainer getSourceSets() {
    JavaPluginConvention javaConvention = getProject().getConvention().getPlugin(JavaPluginConvention.class);
    return javaConvention.getSourceSets();
  }

  private File getResourcesDirectory() {
    if (getConfiguredResourcesDirectory() != null) {
      return getConfiguredResourcesDirectory();
    }
    else {
      return getSourceSets().getByName("main").getOutput().getResourcesDir();
    }
  }

  private String getRelativePath() {
    if (getConfiguredRelativePath() != null) {
      return getConfiguredRelativePath().getAbsolutePath();
    }
    else {
      return ".";
    }
  }

  private String getRootPath() {
    if (getConfiguredRootPath() != null) {
      return getConfiguredRootPath();
    }
    else {
      return getProject().getProjectDir().getAbsolutePath();
    }
  }
  
  /**
   * Stringify all configuration options
   */
  // TODO temporary implementation
  public String toStringConfigurationOptions() {
    return "RebelGenerateTask[ configuredWarPath = " + war + " ]";
  }
  
}