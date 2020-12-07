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
package org.zeroturnaround.jrebel.gradle.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.zeroturnaround.jrebel.gradle.RebelXmlWriter;

/**
 * Main model class representing the data for the rebel.xml.
 */
public class RebelMainModel {

  private final List<RebelClasspathResource> classpathDirs = new ArrayList<RebelClasspathResource>();
  private final List<RebelClasspathResource> classpathDirsets = new ArrayList<RebelClasspathResource>();
  private final List<RebelClasspathResource> classpathJars = new ArrayList<RebelClasspathResource>();
  private final List<RebelClasspathResource> classpathJarsets = new ArrayList<RebelClasspathResource>();
  private final List<RebelWebResource> webResources = new ArrayList<RebelWebResource>();

  private String fallbackClasspath;
  private RebelWar war;

  private String remoteId;

  public List<RebelClasspathResource> getClasspathDirs() {
    return classpathDirs;
  }

  public void addClasspathDir(RebelClasspathResource dir) {
    classpathDirs.add(dir);
  }

  public List<RebelClasspathResource> getClasspathDirsets() {
    return classpathDirsets;
  }

  public void addClasspathDirset(RebelClasspathResource dirset) {
    classpathDirsets.add(dirset);
  }

  public List<RebelClasspathResource> getClasspathJars() {
    return classpathJars;
  }

  public void addClasspathJar(RebelClasspathResource jar) {
    classpathJars.add(jar);
  }

  public List<RebelClasspathResource> getClasspathJarsets() {
    return classpathJarsets;
  }

  public void addClasspathJarset(RebelClasspathResource jarset) {
    classpathJarsets.add(jarset);
  }

  public List<RebelWebResource> getWebResources() {
    return webResources;
  }

  public void addWebResource(RebelWebResource webResource) {
    webResources.add(webResource);
  }

  public void setFallbackClasspath(String fallbackClasspath) {
    this.fallbackClasspath = fallbackClasspath;
  }

  public String getFallbackClasspath() {
    return fallbackClasspath;
  }

  public RebelWar getWar() {
    return war;
  }

  public void setWar(RebelWar war) {
    this.war = war;
  }

  public String getRemoteId() {
    return remoteId;
  }

  public void setRemoteId(String remoteId) {
    this.remoteId = remoteId;
  }

  /**
   * Construct the actual XML stream (string) from the model in memory.
   */
  public String toXmlString() throws IOException {
    return new RebelXmlWriter().toXmlString(this);
  }

  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("classpathDirs", classpathDirs);
    builder.append("classpathDirsets", classpathDirsets);
    builder.append("classpathJars", classpathJars);
    builder.append("classpathJarsets", classpathJarsets);
    builder.append("fallbackClasspath", fallbackClasspath);
    builder.append("war", war);
    builder.append("webResources", webResources);
    builder.append("remoteId", remoteId);
    return builder.toString();
  }

}
