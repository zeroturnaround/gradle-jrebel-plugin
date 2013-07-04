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

import org.zeroturnaround.jrebel.gradle.model.RebelClasspath;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;
import org.zeroturnaround.jrebel.gradle.model.RebelWeb;

public class RebelPluginExtension {

  private String packaging;

  private File classesDirectory;

  private File resourcesDirectory;

  private String warSourceDirectory;

  private String webappDirectory;

  private RebelClasspath classpath;

  private RebelClasspath resourcesClasspath;

  private RebelWar war;

  private RebelWeb web;

  private String rootPath;

  private File relativePath;

  private String rebelXmlDirectory;

  private String showGenerated;

  private String addResourcesDirToRebelXml;

  private String alwaysGenerate;

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

  public RebelClasspath getClasspath() {
    return classpath;
  }

  public void setClasspath(RebelClasspath classpath) {
    this.classpath = classpath;
  }

  public RebelClasspath getResourcesClasspath() {
    return resourcesClasspath;
  }

  public void setResourcesClasspath(RebelClasspath resourcesClasspath) {
    this.resourcesClasspath = resourcesClasspath;
  }

  public RebelWar getWar() {
    return war;
  }

  public void setWar(RebelWar war) {
    this.war = war;
  }

  public RebelWeb getWeb() {
    return web;
  }

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

  public String getShowGenerated() {
    return showGenerated;
  }

  public void setShowGenerated(String showGenerated) {
    this.showGenerated = showGenerated;
  }

  public String getAddResourcesDirToRebelXml() {
    return addResourcesDirToRebelXml;
  }

  public void setAddResourcesDirToRebelXml(String addResourcesDirToRebelXml) {
    this.addResourcesDirToRebelXml = addResourcesDirToRebelXml;
  }

  public String getAlwaysGenerate() {
    return alwaysGenerate;
  }

  public void setAlwaysGenerate(String alwaysGenerate) {
    this.alwaysGenerate = alwaysGenerate;
  }
}
