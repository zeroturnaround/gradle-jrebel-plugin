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

/**
 * Web configuration.
 */
public class RebelWeb {

  private List<RebelWebResource> resources;
  
  private Boolean omitDefault;

  public RebelWeb() {
    resources = new ArrayList<RebelWebResource>();
  }
  
  public List<RebelWebResource> getResources() {
    return resources;
  }

  public void setResources(List<RebelWebResource> _resources) {
    this.resources = _resources;
  }
  
  public void addResource(RebelWebResource resource) {
    resources.add(resource);
  }

  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("resources", resources);
    return builder.toString();
  }

  public Boolean getOmitDefault() {
    return this.omitDefault;
  }
  
  public void setOmitDefault(Boolean omit) {
    this.omitDefault = omit;
  }
  
}

