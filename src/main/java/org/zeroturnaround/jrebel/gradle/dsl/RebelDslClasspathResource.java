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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspathResource;

/**
 * Classpath DSL resource configuration.
 * 
 * @author Sander Sonajalg
 */
public class RebelDslClasspathResource implements Serializable {

  private String directory;
  
  private String dirset;
  
  private List<String> excludes;
  
  private List<String> includes;
  
  private String jar;
  
  private String jarset;

  @Optional
  @Input
  public String getDirectory() {
    return directory;
  }

  @Optional
  @Input
  public String getDirset() {
    return dirset;
  }

  @Optional
  @Input
  public List<String> getExcludes() {
    return excludes;
  }

  @Optional
  @Input
  public List<String> getIncludes() {
    return includes;
  }

  @Optional
  @Input
  public String getJar() {
    return jar;
  }

  @Optional
  @Input
  public String getJarset() {
    return jarset;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public void setDirset(String dirset) {
    this.dirset = dirset;
  }

  public void setExcludes(List<String> excludes) {
    this.excludes = excludes;
  }
  
  public void addExclude(String exclude) {
    if (this.excludes == null) {
      excludes = new ArrayList<String>();
    }
    excludes.add(exclude);
  }

  public void setIncludes(List<String> includes) {
    this.includes = includes;
  }

  public void addInclude(String include) {
    if (this.includes == null) {
      includes = new ArrayList<String>();
    }
    includes.add(include);
  }
  
  public void setJar(String jar) {
    this.jar = jar;
  }

  public void setJarset(String jarset) {
    this.jarset = jarset;
  }
  
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
    builder.append("directory", directory);
    builder.append("dirset", dirset);
    builder.append("excludes", excludes);
    builder.append("includes", includes);
    builder.append("jar", jar);
    builder.append("jarset", jarset);
    return builder.toString();
  }
  
  /**
   * Convert to backend model.
   */
  public RebelClasspathResource toRebelClasspathResource() {
    RebelClasspathResource rebelResource = new RebelClasspathResource();
    rebelResource.setDirectory(this.directory);
    rebelResource.setDirset(this.dirset);
    rebelResource.setExcludes(this.excludes);
    rebelResource.setIncludes(this.includes);
    rebelResource.setJar(this.jar);
    rebelResource.setJarset(this.jarset);
    
    return rebelResource;
  }
  
}
