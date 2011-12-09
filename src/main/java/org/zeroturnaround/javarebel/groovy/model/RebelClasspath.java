package org.zeroturnaround.javarebel.groovy.model;

/**
 * Classpath configuration.
 */
public class RebelClasspath {

	private String fallback;

	private RebelClasspathResource[] resources;

	public String getFallback() {
		return fallback;
	}

	public RebelClasspathResource[] getResources() {
		return resources;
	}

	public void setFallback(String fallback) {
		this.fallback = fallback;
	}

	public void setResources(RebelClasspathResource[] resources) {
		this.resources = resources;
	}

}
