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

import java.util.ArrayList;
import java.util.List;

import groovy.lang.Closure;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.gradle.util.ConfigureUtil;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspath;

/**
 * Gradle DSL level model for <web> elements configuration (corresponds to RebelWeb in backend model).
 * 
 * @author Sander Sonajalg
 */
public class RebelDslClasspath {

  private String fallback;

  private List<RebelDslClasspathResource> resources;

  public RebelDslClasspath() {
    resources = new ArrayList<RebelDslClasspathResource>();
  }
  
  public String getFallback() {
    return fallback;
  }

  public List<RebelDslClasspathResource> getResources() {
    return resources;
  }

  public void setFallback(String fallback) {
    this.fallback = fallback;
  }

  public void setResources(List<RebelDslClasspathResource> _resources) {
    this.resources = _resources;
  }

  public void addResource(RebelDslClasspathResource resource) {
    this.resources.add(resource);
  }
  
  /**
   * DSL method to handle the 'resource { .. }' configuration block
   */
  public void resource(Closure closure) {
    RebelDslClasspathResource resource = new RebelDslClasspathResource();
    ConfigureUtil.configure(closure, resource);
    
    resources.add(resource);
  }
  
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
    builder.append("resources", resources);
    builder.append("fallback", fallback);
    return builder.toString();
  }

  public RebelClasspath toRebelClasspath() {
    RebelClasspath rebelClasspath = new RebelClasspath();
    rebelClasspath.setFallback(this.fallback);
    
    for (RebelDslClasspathResource resource : resources) {
      rebelClasspath.addResource(resource.toRebelClasspathResource());
    }
    
    return rebelClasspath;
  }

}
