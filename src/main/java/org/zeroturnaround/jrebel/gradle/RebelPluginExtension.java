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

import org.zeroturnaround.jrebel.gradle.model.RebelClasspath;
import org.zeroturnaround.jrebel.gradle.model.RebelWeb;

public class RebelPluginExtension {

  private String packaging;

  private File classesDirectory;

  private File resourcesDirectory;

  private String warSourceDirectory;

  private String webappDirectory;

  // TODO change to use strings. no-one knows how to nor wants to use our custom types in their build.gradle
  private RebelClasspath classpath;

  // TODO change to use strings. no-one knows how to nor wants to use our custom types in their build.gradle
  private RebelClasspath resourcesClasspath;

  private String warPath;
  
  // TODO change to use strings. no-one knows how to nor wants to use our custom types in their build.gradle
  private RebelWeb web;

  private String rootPath;

  private File relativePath;

  private String rebelXmlDirectory;

  private Boolean showGenerated;

  private Boolean addResourcesDirToRebelXml;

  private Boolean alwaysGenerate;

  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  public File getClassesDirectory() {
    return classesDirectory;
  }

  public void setClassesDirectory(File classesDirectory) {
    this.classesDirectory = classesDirectory;
  }

  public File getResourcesDirectory() {
    return resourcesDirectory;
  }

  public void setResourcesDirectory(File resourcesDirectory) {
    this.resourcesDirectory = resourcesDirectory;
  }

  public String getWarSourceDirectory() {
    return warSourceDirectory;
  }

  public void setWarSourceDirectory(String warSourceDirectory) {
    this.warSourceDirectory = warSourceDirectory;
  }

  public String getWebappDirectory() {
    return webappDirectory;
  }

  public void setWebappDirectory(String webappDirectory) {
    this.webappDirectory = webappDirectory;
  }

  // TODO change to use strings. no-one knows how to nor wants to use our custom types in their build.gradle
  public RebelClasspath getClasspath() {
    return classpath;
  }

  // TODO change to use strings. no-one knows how to nor wants to use our custom types in their build.gradle
  public void setClasspath(RebelClasspath classpath) {
    this.classpath = classpath;
  }

  // TODO change to use strings. no-one knows how to nor wants to use our custom types in their build.gradle
  public RebelClasspath getResourcesClasspath() {
    return resourcesClasspath;
  }

  // TODO change to use strings. no-one knows how to nor wants to use our custom types in their build.gradle
  public void setResourcesClasspath(RebelClasspath resourcesClasspath) {
    this.resourcesClasspath = resourcesClasspath;
  }

  public String getWarPath() {
    return warPath;
  }

  public void setWarPath(String warPath) {
    this.warPath = warPath;
  }

  // TODO change to use strings. no-one knows how to nor wants to use our custom types in their build.gradle
  public RebelWeb getWeb() {
    return web;
  }

  // TODO change to use strings. no-one knows how to nor wants to use our custom types in their build.gradle
  public void setWeb(RebelWeb web) {
    this.web = web;
  }

  public String getRootPath() {
    return rootPath;
  }

  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  public File getRelativePath() {
    return relativePath;
  }

  public void setRelativePath(File relativePath) {
    this.relativePath = relativePath;
  }

  public String getRebelXmlDirectory() {
    return rebelXmlDirectory;
  }

  public void setRebelXmlDirectory(String rebelXmlDirectory) {
    this.rebelXmlDirectory = rebelXmlDirectory;
  }

  public Boolean getShowGenerated() {
    return showGenerated;
  }

  public void setShowGenerated(Boolean showGenerated) {
    this.showGenerated = showGenerated;
  }

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

}
