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
    
  /**
   * NB! These property names are also used by the Gradle's Conventions magic, do **NOT** rename them!!
   * If you still have to, also update the corresponding string constant or things will break for sure!
   * See their usage in RebelPlugin.
   */
  
  public static final String NAME_ADD_RESOURCES_DIR_TO_REBEL_XML = "addResourcesDirToRebelXml";
  
  private Boolean addResourcesDirToRebelXml;

  public static final String NAME_ALWAYS_GENERATE = "alwaysGenerate";
  
  private Boolean alwaysGenerate;
  
  private String packaging;

  public static final String NAME_REBEL_XML_DIRECTORY = "rebelXmlDirectory";
  
  private File rebelXmlDirectory;
  
  public static final String NAME_SHOW_GENERATED = "showGenerated";

  private Boolean showGenerated;
  
  public static final String NAME_WAR_SOURCE_DIRECTORY = "warSourceDirectory";

  private File warSourceDirectory;
  
  private RebelWeb web;
  
  private File webappDirectory;
  
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
  
  /**
   * The actual invocation of our plugin task. Will construct the in-memory model (RebelXmlBuilder),
   * generate the XML output based on it and write the XML into a file-system file (rebel.xml). 
   */
  @TaskAction
  public void generate() {
    log.info("rebel.alwaysGenerate = " + getAlwaysGenerate());
    log.info("rebel.showGenerated = " + getShowGenerated());
    log.info("rebel.rebelXmlDirectory = " + getRebelXmlDirectory());
    log.info("rebel.warSourceDirectory = " + getWarSourceDirectory());
    log.info("rebel.addResourcesDirToRebelXml = " + getAddResourcesDirToRebelXml());
    log.info("rebel.packaging = " + getPackaging());
  
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
    RebelMainModel builder = null;
  
    if (getPackaging().equals(PACKAGING_TYPE_JAR)) {
      builder = buildJar();
    }
    else if (getPackaging().equals(PACKAGING_TYPE_WAR)) {
      builder = buildWar();
    }
  
    if (builder != null) {
      log.info("Processing ${project.group}:${project.name} with packaging " + getPackaging());
      log.info("Generating \"${rebelXmlFile}\"...");
  
      // Do generate the rebel.xml
      try {
        String xmlFileContents = builder.toXmlString();
  
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
  }

  private void buildClasspath(RebelMainModel builder) {
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
      buildDefaultClasspath(builder, defaultClasspath);
    }
  }

  private void buildDefaultClasspath(RebelMainModel builder, RebelClasspathResource defaultClasspath) throws BuildException {
    if (getAddResourcesDirToRebelXml()) {
      buildDefaultClasspathResources(builder);
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

    builder.addClasspathDir(r);
  }

  private void buildDefaultClasspathResources(RebelMainModel builder) throws BuildException {
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
    builder.addClasspathDir(r);
  }

  private void buildDefaultWeb(RebelMainModel builder, RebelWebResource defaultWeb) {
    RebelWebResource r = new RebelWebResource();
    r.setTarget("/");
    r.setDirectory(fixFilePath(getWarSourceDirectory()));

    if (defaultWeb != null) {
      r.setIncludes(defaultWeb.getIncludes());
      r.setExcludes(defaultWeb.getExcludes());
    }

    builder.addWebResource(r);
  }

  /**
  * Construct a builder for jar projects
  */
  private RebelMainModel buildJar() {
    RebelMainModel builder = new RebelMainModel();
    buildClasspath(builder);

    return builder;
  }

  /**
   * Construct a builder for war projects
   */
  private RebelMainModel buildWar() {
    RebelWar war = getConfiguredWar();

    RebelMainModel builder = new RebelMainModel();
    buildWeb(builder);
    buildClasspath(builder);

    if (war != null) {
      war.setPath(fixFilePath(war.getPath()));
      builder.setWar(war);
    }

    return builder;
  }

  /**
   * Build the model for the <web> element in rebel.xml
   */
  private void buildWeb(RebelMainModel builder) {
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
      buildDefaultWeb(builder, defaultWeb);
    }

    if (web != null) {
      RebelWebResource[] resources = web.getResources();
      if (resources != null && resources.length > 0) {
        for (int i = 0; i < resources.length; i++) {
          RebelWebResource r = resources[i];
          if (r.getDirectory() == null && r.getTarget() == null) {
            buildDefaultWeb(builder, r);
            continue;
          }
          r.setDirectory(fixFilePath(r.getDirectory()));
          builder.addWebResource(r);
        }
      }
    }
  }

  private String fixFilePath(File file) {
    File baseDir = getProject().getProjectDir();

    if (file.isAbsolute() && !isRelativeToPath(new File(baseDir, getRelativePath()), file)) {
      return StringUtils.replace(getCanonicalPath(file), "\\", "/");
    }

    if (!file.isAbsolute()) {
      file = new File(baseDir, file.getPath());
    }

    String relative = getRelativePath(new File(baseDir, getRelativePath()), file);

    if (!(new File(relative)).isAbsolute()) {
      return StringUtils.replace(getRootPath(), "\\", "/") + "/" + relative;
    }

    // relative path was outside baseDir

    // if root path is absolute then try to get a path relative to root
    if ((new File(getRootPath())).isAbsolute()) {
      String s = getRelativePath(new File(getRootPath()), file);

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

  private String getCanonicalPath(File file) throws BuildException {
    try {
      return file.getCanonicalPath();
    }
    catch (IOException e) {
      throw new BuildException("Failed to get canonical path of " + file.getAbsolutePath(), e);
    }
  }

  private File getClassesDirectory() {
    if (getConfiguredClassesDirectory() != null) {
      return getConfiguredClassesDirectory();
    }
    else {
      return getSourceSets().getByName("main").getOutput().getClassesDir();
    }
  }
  
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

  private String getRelativePath(File baseDir, File file) throws BuildException {
    // Avoid the common prefix problem (see case 17005)
    // if:
    //  baseDir = /myProject/web-module/.
    //  file  = /myProject/web-module-shared/something/something/something
    // then basedirpath cannot be a prefix of the absolutePath, or the relative path will be calculated incorrectly!
    // This problem is avoided by adding a trailing slash to basedirpath.
    String basedirpath = getCanonicalPath(baseDir) + File.separator;

    String absolutePath = getCanonicalPath(file);

    String relative;

    if (absolutePath.equals(basedirpath)) {
      relative = ".";
    }
    else if (absolutePath.startsWith(basedirpath)) {
      relative = absolutePath.substring(basedirpath.length());
    }
    else {
      relative = absolutePath;
    }

    relative = StringUtils.replace(relative, "\\", "/");

    return relative;
  }

  private String getRootPath() {
    if (getConfiguredRootPath() != null) {
      return getConfiguredRootPath();
    }
    else {
      return getProject().getProjectDir().getAbsolutePath();
    }
  }

  private boolean isRelativeToPath(File baseDir, File file) throws BuildException {
    String basedirpath = getCanonicalPath(baseDir);
    String absolutePath = getCanonicalPath(file);

    return absolutePath.startsWith(basedirpath);
  }

  // =========== STUFF BELOW HERE REFERS TO THE RebelExtension OBJECT DIRECTLY AND SHOULD BE GOTTEN RID OF !!!
  // - replace with usage of ConventionMappings.
  // - the model has to be worked on. RebelPluginExtension probably cannot operate with our custom types
  //      as end-user doesn't know or want to know how to configure them
  
  private String getConfiguredRootPath() {
    return getRebelExtension().getRootPath();
  }
  
  private File getConfiguredRelativePath() {
    return getRebelExtension().getRelativePath();
  }
  
  private File getConfiguredResourcesDirectory() {
    return getRebelExtension().getResourcesDirectory();
  }

  private File getConfiguredClassesDirectory() {
    return getRebelExtension().getClassesDirectory();
  }
 
  private RebelWar getConfiguredWar() {
    String warPath = getRebelExtension().getWarPath();
    if (warPath != null) {
      RebelWar war = new RebelWar();
      war.setPath(warPath);
      return war;
    }
    return null;
  }
  
  private RebelClasspath getConfiguredResourcesClasspath() {
    return getRebelExtension().getResourcesClasspath();
  }

  private RebelClasspath getConfiguredClasspath() {
    return getRebelExtension().getClasspath();
  }
  
  /**
   * Get rid of it, achieve the same by using conventions
   */
  @Deprecated
  private RebelPluginExtension getRebelExtension() {
    return (RebelPluginExtension) getProject().getExtensions().getByName(RebelPlugin.REBEL_EXTENSION_NAME);
  }
  
}