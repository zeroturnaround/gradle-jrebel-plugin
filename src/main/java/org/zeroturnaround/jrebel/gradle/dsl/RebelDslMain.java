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

import groovy.lang.Closure;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.gradle.api.Action;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.zeroturnaround.jrebel.gradle.util.ConfigureUtilAdapter;

import java.io.File;
import java.io.Serializable;

import static org.zeroturnaround.jrebel.gradle.LegacyRebelGenerateTask.PACKAGING_TYPE_JAR;

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

  private boolean showGenerated;

  private boolean alwaysGenerate;

  private RebelDslWeb web;

  private RebelDslWar war;

  private String remoteId;

  private boolean generateRebelRemote = false;

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
  public boolean getShowGenerated() {
    return showGenerated;
  }

  /**
   * <p>Display the generated xml configuration files in gradle output under <code>lifecycle</code> level</p>
   *
   * default: false
   */
  public void setShowGenerated(boolean showGenerated) {
    this.showGenerated = showGenerated;
  }

  @Input
  public boolean getAlwaysGenerate() {
    return alwaysGenerate;
  }

  public void setAlwaysGenerate(boolean alwaysGenerate) {
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

  @Input
  @Optional
  public String getRemoteId() {
    return remoteId;
  }

  /**
   * <p>Set the remote id in rebel.xml and rebel-remote.xml files.
   * The JRebel IDE plugin must find a matching id in the corresponding xml configuration file under project sources.</p>
   *
   * <p><b>Warning</b>: Resulting id in rebel-remote.xml may have certain characters and words filtered out</p>
   *
   * <b>default</b>: dot-separated path (example: {@code myproject.mymodule.main})
   */
  public void setRemoteId(String remoteId) {
    this.remoteId = remoteId;
  }


  @Input
  public boolean getGenerateRebelRemote() {
    return generateRebelRemote;
  }

  /**
   * <p>Generate a rebel-remote.xml file that is required for JRebel Remote functionality.</p>
   *
   * <p>The id in this file identifies the application or library on the remote server.
   * The default value may be overridden using {@link RebelDslMain#setRemoteId}.
   * The JRebel IDE plugin will use the id in the same file under project sources when syncing.</p>
   *
   * default: false
   */
  public void setGenerateRebelRemote(boolean generateRebelRemote) {
    this.generateRebelRemote = generateRebelRemote;
  }

  /**
   * Evaluate the 'classpath {..}' block.
   * Groovy only
   */
  @Deprecated
  public void classpath(Closure closure) {
    classpath = new RebelDslClasspath();
    ConfigureUtilAdapter.configure(closure, classpath);
  }

  /**
   * Evaluate the 'classpath {..}' block.
   */
  public void classpath(Action<RebelDslClasspath> action) {
    classpath = new RebelDslClasspath();
    action.execute(classpath);
  }

  /**
   * Evaluate the 'web {..}' block.
   * Groovy only
   */
  @Deprecated
  public void web(Closure closure) {
    web = new RebelDslWeb();
    ConfigureUtilAdapter.configure(closure, web);
  }

  /**
   * Evaluate the 'web {..}' block.
   */
  public void web(Action<RebelDslWeb> action) {
    web = new RebelDslWeb();
    action.execute(web);
  }

  /**
   * Evaluate the 'war {..}' block.
   * Groovy only
   */
  @Deprecated
  public void war(Closure closure) {
    war = new RebelDslWar();
    ConfigureUtilAdapter.configure(closure, war);
  }

  /**
   * Evaluate the 'war {..}' block.
   */
  public void war(Action<RebelDslWar> action) {
    war = new RebelDslWar();
    action.execute(war);
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
    builder.append("remoteId", remoteId);
    builder.append("generateRebelRemote", generateRebelRemote);
    return builder.toString();
  }
  
}
