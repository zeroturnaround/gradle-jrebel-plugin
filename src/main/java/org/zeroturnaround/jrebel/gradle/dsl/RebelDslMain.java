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
package org.zeroturnaround.jrebel.gradle.dsl;

import static org.zeroturnaround.jrebel.gradle.LegacyRebelGenerateTask.PACKAGING_TYPE_JAR;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.util.ConfigureUtil;

import groovy.lang.Closure;

/**
 * The parent class of the Rebel plugin's DSL extension. 
 * 
 * @author Sander Sonajalg
 */
public class RebelDslMain implements Serializable {

  private String packaging;

  private String webappDirectory;

  private RebelDslClasspath classpath;

  // TODO implement propagation to backend, document
  private String rootPath;

  // TODO implement propagation to backend, document
  private File relativePath;

  private String rebelXmlDirectory;

  private Boolean showGenerated;

  private Boolean alwaysGenerate;

  private RebelDslWeb web;

  private RebelDslWar war;

  public RebelDslMain() {
    this.packaging = PACKAGING_TYPE_JAR;
    this.showGenerated = false;
    this.alwaysGenerate = false;
  }

  @Input
  public String getPackaging() {
    return packaging;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  @Optional
  @Input
  public String getWebappDirectory() {
    return webappDirectory;
  }

  public void setWebappDirectory(String webappDirectory) {
    this.webappDirectory = webappDirectory;
  }

  @Optional
  @Nested
  public RebelDslWeb getWeb() {
    return web;
  }
  
  public void setWeb(RebelDslWeb _web) {
    this.web = _web;
  }

  @Optional
  @Nested
  public RebelDslClasspath getClasspath() {
    return classpath;
  }
  
  public void setClasspath(RebelDslClasspath _classpath) {
    this.classpath = _classpath;
  }

  @Optional
  @Input
  public String getRootPath() {
    return rootPath;
  }

  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  @Internal
  public File getRelativePath() {
    return relativePath;
  }

  @Optional
  @Input
  public String getRelativePathName() {
    return relativePath != null ? relativePath.getAbsolutePath() : null;
  }

  public void setRelativePath(File relativePath) {
    this.relativePath = relativePath;
  }

  @Optional
  @Input
  public String getRebelXmlDirectory() {
    return rebelXmlDirectory;
  }

  public void setRebelXmlDirectory(String rebelXmlDirectory) {
    this.rebelXmlDirectory = rebelXmlDirectory;
  }

  @Input
  public Boolean getShowGenerated() {
    return showGenerated;
  }

  public void setShowGenerated(Boolean showGenerated) {
    this.showGenerated = showGenerated;
  }

  @Input
  public Boolean getAlwaysGenerate() {
    return alwaysGenerate;
  }

  public void setAlwaysGenerate(Boolean alwaysGenerate) {
    this.alwaysGenerate = alwaysGenerate;
  }

  @Optional
  @Nested
  public RebelDslWar getWar() {
    return war;
  }

  public void setWar(RebelDslWar war) {
    this.war = war;
  }

  /**
   * Evaluate the 'classpath {..}' block
   */
  public void classpath(Closure closure) {
    classpath = new RebelDslClasspath();
    ConfigureUtil.configure(closure, classpath);
  }
  
  /**
   * Evaluate the 'web {..}' block
   */
  public void web(Closure closure) {
    web = new RebelDslWeb();
    ConfigureUtil.configure(closure, web);
  }
  
  /**
   * Evaluate the 'war {..}' block
   */
  public void war(Closure closure) {
    war = new RebelDslWar();
    ConfigureUtil.configure(closure, war);
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("packaging", packaging);
    builder.append("webappDirectory", webappDirectory);
    builder.append("classpath", classpath);
    builder.append("web", web);
    builder.append("rootPath", rootPath);
    builder.append("relativePath", relativePath);
    builder.append("rebelXmlDirectory", rebelXmlDirectory);
    builder.append("showGenerated", showGenerated);
    builder.append("alwaysGenerate", alwaysGenerate);
    builder.append("war", war);
    return builder.toString();
  }
  
}
