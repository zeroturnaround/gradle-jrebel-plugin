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
 * Classpath configuration.
 */
public class RebelClasspath {

  private String fallback;

  private List<RebelClasspathResource> resources;

  /**
   * Don't add the default classpath element (the one asked from the project model)
   */
  private Boolean omitDefault = false;
  
  public RebelClasspath() {
    this.resources = new ArrayList<RebelClasspathResource>();
  }
  
  public String getFallback() {
    return fallback;
  }

  public List<RebelClasspathResource> getResources() {
    return resources;
  }

  public void setFallback(String fallback) {
    this.fallback = fallback;
  }

  public void setResources(List<RebelClasspathResource> _resources) {
    this.resources = _resources;
  }

  public void addResource(RebelClasspathResource resource) {
    this.resources.add(resource);
  }
  
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
    builder.append("resources", resources);
    builder.append("fallback", fallback);
    builder.append("omitDefault", omitDefault);
    return builder.toString();
  }

  public void setOmitDefault(Boolean omit) {
    this.omitDefault = omit;
  }
  
  public Boolean isOmitDefault() {
    return this.omitDefault;
  }
  
}
