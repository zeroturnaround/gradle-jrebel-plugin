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
package org.zeroturnaround.jrebel.gradle

import org.apache.commons.lang.StringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildException
import org.zeroturnaround.jrebel.gradle.model.RebelClasspath;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspathResource;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;
import org.zeroturnaround.jrebel.gradle.model.RebelWeb;
import org.zeroturnaround.jrebel.gradle.model.RebelWebResource;


class RebelGenerateTask extends DefaultTask {

  def String addResourcesDirToRebelXml
  /**
   * TODO for some reason it's impossible to pass boolean value from build script into task's variable using conventional mapping.
   * Asked on the forum why: http://forums.gradle.org/gradle/topics/problem_with_conventional_mapping
   */
  def String alwaysGenerate
  
  def String packaging
  
  def File rebelXmlDirectory
  
  def String showGenerated
  
  def File warSourceDirectory
  
  def RebelWeb web;
  
  def File webappDirectory

  def buildClasspath(RebelXmlBuilder builder) {
    def boolean addDefaultAsFirst = true
    def RebelClasspathResource defaultClasspath = null
    def RebelClasspath classpath = project.rebel.classpath

    // check if there is a element with no dir/jar/dirset/jarset set. if there
    // is then don't put default classpath as
    // first but put it where this element was.

    if (classpath != null) {
      RebelClasspathResource[] resources = classpath.getResources()

      if (resources != null && resources.length > 0) {
        for (int i = 0; i < resources.length; i++) {
          RebelClasspathResource r = resources[i]

          if (!r.isTargetSet()) {
            addDefaultAsFirst = false
            defaultClasspath = r
            break;
          }
        }
      }
    }

    if (addDefaultAsFirst) {
      buildDefaultClasspath(builder, defaultClasspath)
    }
  }

  def buildDefaultClasspath(RebelXmlBuilder builder, RebelClasspathResource defaultClasspath) throws BuildException {
    if (isTrue(getAddResourcesDirToRebelXml())) {
      buildDefaultClasspathResources(builder);
    }

    // project output directory
    RebelClasspathResource r = new RebelClasspathResource();
    r.setDirectory(fixFilePath(getClassesDirectory()));
    if (!new File(r.directory).directory) {
      return
    }

    if (defaultClasspath != null) {
      r.setIncludes(defaultClasspath.getIncludes());
      r.setExcludes(defaultClasspath.getExcludes());
    }

    builder.addClasspathDir(r);
  }

  def buildDefaultClasspathResources(RebelXmlBuilder builder) throws BuildException {
    RebelClasspathResource r = new RebelClasspathResource();
    r.directory = fixFilePath(getResourcesDirectory())
    if (!new File(r.directory).directory) {
      return
    }

    def resourcesClasspath = project.rebel.resourcesClasspath;
    if (resourcesClasspath) {
      r.setIncludes(resourcesClasspath.getIncludes());
      r.setExcludes(resourcesClasspath.getExcludes());
    }
    builder.addClasspathDir(r);
  }

  private void buildDefaultWeb(RebelXmlBuilder builder, RebelWebResource defaultWeb) {
    RebelWebResource r = new RebelWebResource();
    r.setTarget("/");
    r.setDirectory(fixFilePath(getWarSourceDirectory()));

    if (defaultWeb != null) {
      r.setIncludes(defaultWeb.getIncludes());
      r.setExcludes(defaultWeb.getExcludes());
    }

    builder.addWebresource(r);
  }

  def RebelXmlBuilder buildJar() {
    def builder = new RebelXmlBuilder()
    buildClasspath(builder)

    return builder
  }

  def RebelXmlBuilder buildWar() {
    // TODO convert this variable to the field
    def RebelWar war = project.rebel.war

    def builder = new RebelXmlBuilder()
    buildWeb(builder)
    buildClasspath(builder)

    if (war != null) {
      war.setPath(fixFilePath(war.getPath()))
      builder.setWar(war)
    }

    return builder
  }

  private void buildWeb(RebelXmlBuilder builder) {
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
          builder.addWebresource(r);
        }
      }
    }
  }

  def String fixFilePath(File file) {
    File baseDir = project.projectDir

    if (file.isAbsolute() && !isRelativeToPath(new File(baseDir, getRelativePath()), file)) {
      return StringUtils.replace(getCanonicalPath(file), '\\', '/')
    }

    if (!file.isAbsolute()) {
      file = new File(baseDir, file.getPath())
    }

    String relative = getRelativePath(new File(baseDir, getRelativePath()), file)

    if (!(new File(relative)).isAbsolute()) {
      return StringUtils.replace(getRootPath(), '\\', '/') + "/" + relative
    }

    // relative path was outside baseDir

    // if root path is absolute then try to get a path relative to root
    if ((new File(getRootPath())).isAbsolute()) {
      String s = getRelativePath(new File(getRootPath()), file)

      if (!(new File(s)).isAbsolute()) {
        return StringUtils.replace(getRootPath(), '\\', '/') + "/" + s
      }
      else {
        // root path and the calculated path are absolute, so
        // just return calculated path
        return s
      }
    }

    // return absolute path to file
    return StringUtils.replace(file.getAbsolutePath(), '\\', '/')
  }

  def String fixFilePath(String path) {
    return fixFilePath(new File(path))
  }

  @TaskAction
  def generate() {
    project.logger.info "rebel.alwaysGenerate = " + getAlwaysGenerate()
    project.logger.info "rebel.showGenerated = " + getShowGenerated()
    project.logger.info "rebel.rebelXmlDirectory = " + getRebelXmlDirectory()
    project.logger.info "rebel.warSourceDirectory = " + getWarSourceDirectory()
    project.logger.info "rebel.addResourcesDirToRebelXml = " + getAddResourcesDirToRebelXml()
    project.logger.info "rebel.packaging = " + getPackaging()

    // find rebel.xml location
    def File rebelXmlFile = null

    if (getRebelXmlDirectory()) {
      rebelXmlFile = new File(getRebelXmlDirectory(), "rebel.xml")
    }

    // find build.gradle location
    def File buildXmlFile = project.buildFile

    if (!isTrue(getAlwaysGenerate()) && rebelXmlFile && rebelXmlFile.exists() && buildXmlFile && buildXmlFile.exists() && rebelXmlFile.lastModified() > buildXmlFile.lastModified()) {
      return;
    }

    // find the type of the project
    def builder = null

    if (getPackaging() == "jar") {
      builder = buildJar()
    }
    else if (getPackaging() == "war") {
      builder = buildWar()
    }

    if (builder) {
      project.logger.info "Processing ${project.group}:${project.name} with packaging " + getPackaging();

      project.logger.info "Generating \"${rebelXmlFile}\"..."

      def Writer w = new StringWriter()
      builder.writeXml(w)

      if (isTrue(getShowGenerated())) {
        try {
          println w.toString()
        }
        catch (IOException _ignore) {
        }
      }

      try {
        rebelXmlFile.parentFile.mkdirs()
        rebelXmlFile.write w.toString()
      }
      catch (IOException e) {
        throw new BuildException("Failed writing \"${rebelXmlFile}\"", e);
      }
      finally {
        if (w != null) {
          try {
            w.close()
          }
          catch (IOException _ignore) {
          }
        }
      }
    }
  }


  def String getCanonicalPath(File file) throws BuildException {
    try {
      return file.canonicalPath;
    }
    catch (IOException e) {
      throw new BuildException("Failed to get canonical path of " + file.absolutePath, e);
    }
  }

  def File getClassesDirectory() {
    if (project.rebel.classesDirectory) {
      return project.rebel.classesDirectory
    }
    else {
      return project.sourceSets.main.output.classesDir
    }
  }

  File getResourcesDirectory() {
    if (project.rebel.resourcesDirectory) {
      return project.rebel.resourcesDirectory
    }
    else {
      return project.sourceSets.main.output.resourcesDir
    }
  }

  def String getRelativePath() {
    if (project.rebel.relativePath) {
      return project.rebel.relativePath.absolutePath
    }
    else {
      return '.'
    }
  }

  def String getRelativePath(File baseDir, File file) throws BuildException {
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

    relative = StringUtils.replace(relative, '\\', '/');

    return relative;
  }

  def String getRootPath() {
    if (project.rebel.rootPath) {
      return project.rebel.rootPath
    }
    else {
      return project.projectDir
    }
  }

  def boolean isRelativeToPath(File baseDir, File file) throws BuildException {
    String basedirpath = getCanonicalPath(baseDir);
    String absolutePath = getCanonicalPath(file);

    return absolutePath.startsWith(basedirpath);
  }

  def boolean isTrue(String value) {
    return "true".equals(value);
  }
}
