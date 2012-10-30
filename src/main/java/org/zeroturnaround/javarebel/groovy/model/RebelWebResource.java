/**
 *    Copyright (C) 2012 ZeroTurnaround LLC <support@zeroturnaround.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.zeroturnaround.javarebel.groovy.model;

import java.util.List;

/**
 * Web reource configuration.
 */
public class RebelWebResource implements RebelResource {

	private String directory;
	private List<String> excludes;
	private List<String> includes;
	private String target;

	public String getDirectory() {
		return directory;
	}

	public List<String> getExcludes() {
		return excludes;
	}

	public List<String> getIncludes() {
		return includes;
	}

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

}
