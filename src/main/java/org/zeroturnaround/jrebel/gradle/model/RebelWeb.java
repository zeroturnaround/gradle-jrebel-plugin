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

import java.util.Arrays;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Web configuration.
 */
public class RebelWeb {

  private RebelWebResource[] resources;

  public RebelWebResource[] getResources() {
    return resources;
  }

  public void setResources(RebelWebResource[] resources) {
    this.resources = resources;
  }
  
  // TODO wow.. why not a List here???
  public void addResource(RebelWebResource resource) {
    resources = append(resources, resource);
  }

  /**
   * hack-around to the fact that arrays have constant length.. 
   * TODO - change RebelWeb to use ArrayList-s
   */
  static RebelWebResource[] append(RebelWebResource[] arr, RebelWebResource element) {
    if (arr == null) {
      return new RebelWebResource[] { element };
    }
    else {
      int n = arr.length;
      arr = Arrays.copyOf(arr, n + 1);
      arr[n] = element;
      return arr;
    }
  }

  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("resources", resources);
    return builder.toString();
  }
  
}

