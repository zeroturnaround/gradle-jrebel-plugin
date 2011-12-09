package org.zeroturnaround.javarebel.groovy.model;

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
