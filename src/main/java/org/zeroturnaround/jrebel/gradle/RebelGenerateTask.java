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
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.gradle.api.DefaultTask;
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

  
  private String packaging;
  
  private RebelClasspath classpath;
  
  private RebelWeb web;
  
  private File webappDirectory;
  
  private RebelWar war;

  // === interal properties of the task
  
  private RebelMainModel rebelModel;
  
  private boolean skipWritingRebelXml;
  
  // === Options propagated here through the convention-mappings
  
  private Boolean addResourcesDirToRebelXml;
  
  private Boolean alwaysGenerate;

  private File defaultClassesDirectory;
  
  private File defaultResourcesDirectory;
  
  private Boolean showGenerated;
  
  private File warSourceDirectory;
  
  private File rebelXmlDirectory;

  
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
  
  private RebelClasspath configuredResourcesClasspath;
    
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

  public RebelClasspath getConfiguredResourcesClasspath() {
    return configuredResourcesClasspath;
  }
  
  public void setConfiguredResourcesClasspath(RebelClasspath rebelPath) {
    this.configuredResourcesClasspath = rebelPath;
  }

  // ============================= END OF WEIRD STUFF =========================================
  


  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
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
  
  public Boolean getAddResourcesDirToRebelXml() {
    return addResourcesDirToRebelXml;
  }

  public Boolean getAlwaysGenerate() {
    return alwaysGenerate;
  }
  
  public File getDefaultClassesDirectory() {
    return defaultClassesDirectory;
  }

  public File getDefaultResourcesDirectory() {
    return defaultResourcesDirectory;
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
   * The actual invocation of our plugin task. Will construct the in-memory model (RebelXmlBuilder),
   * generate the XML output based on it and write the XML into a file-system file (rebel.xml). 
   */
  @TaskAction
  public void generate() {
    propagateConventionMappingSettings();
    
    log.info("rebel.alwaysGenerate = " + alwaysGenerate);
    log.info("rebel.showGenerated = " + showGenerated);
    log.info("rebel.rebelXmlDirectory = " + rebelXmlDirectory);
    log.info("rebel.warSourceDirectory = " + warSourceDirectory);
    log.info("rebel.addResourcesDirToRebelXml = " + addResourcesDirToRebelXml);
    log.info("rebel.packaging = " + packaging);
    log.info("rebel.war = " + war);
    log.info("rebel.web = " + web);
    log.info("rebel.classpath = " + classpath);
    log.info("rebel.defaultClassesDirectory = " + defaultClassesDirectory);
    log.info("rebel.defaultResourcesDirectory = " + defaultResourcesDirectory);
    
    // find rebel.xml location
    File rebelXmlFile = null;
  
    if (rebelXmlDirectory != null) {
      rebelXmlFile = new File(rebelXmlDirectory, "rebel.xml");
    }
  
    // find build.gradle location
    File buildXmlFile = getProject().getBuildFile();
  
    if (!alwaysGenerate && (rebelXmlFile != null) && rebelXmlFile.exists() && (buildXmlFile != null) && buildXmlFile.exists() && rebelXmlFile.lastModified() > buildXmlFile.lastModified()) {
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

  /**
   * Compile the model that corresponds to the <classpath> node in rebel.xml.
   */
  private void buildClasspath(RebelMainModel model) {

    // Search for the default element. If we find it, we have to place it exactly into the same place where we
    // found it (preserving the order). If we *don't* find it, we'll add the default classpath as first element.
    
    // TODO later on there probably also has to be a "omitDefault" setting!
    
    boolean addDefaultAsFirst = true;
    RebelClasspathResource defaultClasspath = null;
  
    // Just search for the default element. Don't add anything anywhere yet.
    if (classpath != null) {
      for (RebelClasspathResource resource : classpath.getResources()) {

        // we found the default.
        if (resource.isDefaultClasspathElement()) {
          addDefaultAsFirst = false;
          defaultClasspath = resource;
          break;
        }
      }
    }
  
    // Default classpath element not found. Put the default as first.
    if (addDefaultAsFirst) {
      buildDefaultClasspath(model, defaultClasspath);
    }
    
    // Iterate through all classpath elements and add them.
    
    if (classpath != null) {
      for (RebelClasspathResource resource : classpath.getResources()) {

        // Special treatment for the default.
        if (resource.isDefaultClasspathElement()) {
          buildDefaultClasspath(model, resource);
        }
        // An ordinary element. Add it.
        else {
          // TODO TODO TODO add the fixpath stuff!! --- better implementation!! 
          // TODO fix paths for other elements as well!
          resource.setDirectory(fixFilePath(resource.getDirectory()));
          model.addClasspathDir(resource);
        }
      }
    }
  }

  /**
   * Add the default classes directory to classpath
   */
  private void buildDefaultClasspath(RebelMainModel model, RebelClasspathResource defaultClasspath) throws BuildException {
    if (addResourcesDirToRebelXml) {
      buildDefaultClasspathResources(model);
    }
  
    // project output directory
    RebelClasspathResource r = new RebelClasspathResource();
    r.setDirectory(fixFilePath(defaultClassesDirectory));
    if (!new File(r.getDirectory()).isDirectory()) {
      return;
    }
  
    if (defaultClasspath != null) {
      r.setIncludes(defaultClasspath.getIncludes());
      r.setExcludes(defaultClasspath.getExcludes());
    }
  
    model.addClasspathDir(r);
  }

  /**
   * Add the default resources directory to classpath
   */
  private void buildDefaultClasspathResources(RebelMainModel model) throws BuildException {
    RebelClasspathResource r = new RebelClasspathResource();
    r.setDirectory(fixFilePath(defaultResourcesDirectory));
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
  
    // Hmm.. this first part looks like a hack to go through all the elements just to find 
    // out if one of them is the default element. Nothing is actually added anywhere. [sander]
    // TODO rewrite
    
    if (web != null) {
      List<RebelWebResource> resources = web.getResources();
  
      if (resources != null && resources.size() > 0) {
        for (int i = 0; i < resources.size(); i++) {
          RebelWebResource r = resources.get(i);
  
          if (r.getDirectory() == null && r.getTarget() == null) {
            defaultWeb = r;
            addDefaultAsFirst = false;
            break;
          }
        }
      }
    }
  
    // Add the default, if we are gonna add it at all 
    if (addDefaultAsFirst) {
      buildDefaultWeb(model, defaultWeb);
    }
  
    if (web != null) {
      List<RebelWebResource> resources = web.getResources();
      if (resources != null && resources.size() > 0) {
        for (int i = 0; i < resources.size(); i++) {
          RebelWebResource r = resources.get(i);
          
          // Skip the default (hmm..)
          if (r.getDirectory() == null && r.getTarget() == null) {
            buildDefaultWeb(model, r);
            continue;
          }
          // Otherwise, add the resource.
          r.setDirectory(fixFilePath(r.getDirectory()));
          model.addWebResource(r);
        }
      }
    }
  }

  /**
   * The default for the <web> element in rebel.xml
   */
  private void buildDefaultWeb(RebelMainModel model, RebelWebResource defaultWeb) {
    RebelWebResource r = new RebelWebResource();
    r.setTarget("/");
    r.setDirectory(fixFilePath(warSourceDirectory));
  
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
 
  /* ====================================================================================================
   *   Properties intercepted by Gradle's convention-mapping byte code magic. These methods will actually
   *   be intercepted and return values set by the callback set up in RebelPlugin#configure.
   * 
   *   These properties are cached into local variables to lessen the magic. See propagateConventionMappingSettings().
   */
  
  public static final String NAME_ADD_RESOURCES_DIR_TO_REBEL_XML = "addResourcesDirToRebelXml$MAGIC";

  public static final String NAME_ALWAYS_GENERATE = "alwaysGenerate$MAGIC";

  public static final String NAME_DEFAULT_CLASSES_DIRECTORY = "defaultClassesDirectory$MAGIC";
  
  public static final String NAME_DEFAULT_RESOURCES_DIRECTORY = "defaultResourcesDirectory$MAGIC";
  
  public static final String NAME_REBEL_XML_DIRECTORY = "rebelXmlDirectory$MAGIC";

  public static final String NAME_SHOW_GENERATED = "showGenerated$MAGIC";
  
  public static final String NAME_WAR_SOURCE_DIRECTORY = "warSourceDirectory$MAGIC";
  
  public Boolean getAddResourcesDirToRebelXml$MAGIC() {
    return null;
  }
  
  public Boolean getAlwaysGenerate$MAGIC() {
    return null;
  }

  public File getDefaultClassesDirectory$MAGIC() {
    return null;
  }
  
  public File getDefaultResourcesDirectory$MAGIC() {
    return null;
  }
  
  public File getRebelXmlDirectory$MAGIC() {
    return null;
  }

  public Boolean getShowGenerated$MAGIC() {
    return null;
  }
  
  public File getWarSourceDirectory$MAGIC() {
    return null;
  }
  
  /**
   * Let the convention-mappings propagate its settings to me through the magic getters,
   * save copies of them locally into normal instance variables.
   * 
   * (public only for unit tests)
   */
  public void propagateConventionMappingSettings() {
    addResourcesDirToRebelXml = getAddResourcesDirToRebelXml$MAGIC();
    alwaysGenerate = getAlwaysGenerate$MAGIC();
    defaultClassesDirectory = getDefaultClassesDirectory$MAGIC();
    defaultResourcesDirectory = getDefaultResourcesDirectory$MAGIC();
    rebelXmlDirectory = getRebelXmlDirectory$MAGIC();
    showGenerated = getShowGenerated$MAGIC();
    warSourceDirectory = getWarSourceDirectory$MAGIC();
  }
  
  // ========== END OF convention-mapping's intercepted magic methods
  
}