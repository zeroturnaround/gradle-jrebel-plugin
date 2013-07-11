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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * Classpath resource configuration.
 */
public class RebelClasspathResource implements RebelResource {

  private String directory;
  
  private String dirset;
  
  private List<String> excludes;
  
  private List<String> includes;
  
  private String jar;
  
  private String jarset;

  public String getDirectory() {
    return directory;
  }

  public String getDirset() {
    return dirset;
  }

  public List<String> getExcludes() {
    return excludes;
  }

  public List<String> getIncludes() {
    return includes;
  }

  public String getJar() {
    return jar;
  }

  public String getJarset() {
    return jarset;
  }

  /**
   * True if at least one of the target attributes is defined for this resource.
   */
  public boolean isTargetSet() {
    return directory != null || jar != null || jarset != null
        || dirset != null;
  }
  
  /**
   * We consider an empty "resource {}" element to mark the placement of the default classpath.
   */
  public boolean isDefaultClasspathElement() {
    return !isTargetSet();
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

}
