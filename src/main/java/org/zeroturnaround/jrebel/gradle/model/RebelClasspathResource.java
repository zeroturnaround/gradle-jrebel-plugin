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

import java.util.List;


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

  public boolean isTargetSet() {
    return directory != null || jar != null || jarset != null
        || dirset != null;
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

  public void setIncludes(List<String> includes) {
    this.includes = includes;
  }

  public void setJar(String jar) {
    this.jar = jar;
  }

  public void setJarset(String jarset) {
    this.jarset = jarset;
  }

}
