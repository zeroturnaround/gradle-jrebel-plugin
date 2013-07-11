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
import org.gradle.util.ConfigureUtil;
import org.zeroturnaround.jrebel.gradle.model.RebelWeb;

/**
 * Gradle DSL level model for <web> elements configuration (corresponds to RebelWeb in backend model).
 * 
 * @author Sander Sonajalg
 */
public class RebelDslWeb {

  private List<RebelDslWebResource> webResources;
  
  public RebelDslWeb() {
    webResources = new ArrayList<RebelDslWebResource>();
  }

  public List<RebelDslWebResource> getWebResources() {
    return webResources;
  }

  public void setWebResources(List<RebelDslWebResource> webResources) {
    this.webResources = webResources;
  }

  /**
   * (mainly for automated tests to emulate DSL behavior)
   */
  public void addWebResources(RebelDslWebResource webResources) {
    this.webResources.add(webResources);
  }
  
  /**
   * DSL-backing method to add a new resource
   */
  public void resource(Closure closure) {
    RebelDslWebResource webResource = new RebelDslWebResource();
    ConfigureUtil.configure(closure, webResource);
    
    webResources.add(webResource);
  }

  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("webResources", webResources);
    return builder.toString();
  }
  
  public RebelWeb toRebelWeb() {
    RebelWeb web = new RebelWeb();
    for (RebelDslWebResource webDslResource : webResources) {
      web.addResource(webDslResource.toRebelWebResource());
    }
    return web;
  }

}
