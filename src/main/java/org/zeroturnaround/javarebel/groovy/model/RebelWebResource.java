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
