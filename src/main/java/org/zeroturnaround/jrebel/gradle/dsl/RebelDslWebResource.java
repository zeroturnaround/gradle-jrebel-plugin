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
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.zeroturnaround.jrebel.gradle.model.RebelWebResource;


/**
 * Web reource configuration.
 * 
 * @author Sander Sonajalg
 */
public class RebelDslWebResource implements Serializable {

  private String directory;
  
  private List<String> excludes;
  
  private List<String> includes;
  
  private String target;

  @Optional
  @Input
  public String getDirectory() {
    return directory;
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
  public String getTarget() {
    return target;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public void setExcludes(List<String> excludes) {
    this.excludes = excludes;
  }

  public void setIncludes(List<String> includes) {
    this.includes = includes;
  }

  public void setTarget(String target) {
    this.target = target;
  }
  
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
    builder.append("directory", directory);
    builder.append("excludes", excludes);
    builder.append("includes", includes);
    builder.append("target", target);
    return builder.toString();
  }

  /**
   * Convert to backend model
   */
  public RebelWebResource toRebelWebResource() {
    RebelWebResource resource = new RebelWebResource();
    resource.setDirectory(directory);
    resource.setExcludes(excludes);
    resource.setIncludes(includes);
    resource.setTarget(target);
    return resource;
  }
}

