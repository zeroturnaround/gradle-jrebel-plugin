package org.zeroturnaround.javarebel.groovy;

import static org.apache.commons.lang.StringEscapeUtils.escapeXml;
import static org.apache.commons.lang.StringUtils.repeat;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.zeroturnaround.javarebel.groovy.model.RebelClasspathResource;
import org.zeroturnaround.javarebel.groovy.model.RebelResource;
import org.zeroturnaround.javarebel.groovy.model.RebelWar;
import org.zeroturnaround.javarebel.groovy.model.RebelWebResource;

/**
 * Class for constructing xml configuration.
 */
class RebelXmlBuilder {

	private List<RebelClasspathResource> classpathDir = new ArrayList<RebelClasspathResource>();
	private List<RebelClasspathResource> classpathDirset = new ArrayList<RebelClasspathResource>();
	private List<RebelClasspathResource> classpathJar = new ArrayList<RebelClasspathResource>();
	private List<RebelClasspathResource> classpathJarset = new ArrayList<RebelClasspathResource>();
	private String fallbackClasspath;
	private RebelWar war;
	private List<RebelWebResource> webResources = new ArrayList<RebelWebResource>();

	public void addClasspathDir(RebelClasspathResource dir) {
		classpathDir.add(dir);
	}

	public void addClasspathDirset(RebelClasspathResource dirset) {
		classpathDirset.add(dirset);
	}

	public void addClasspathJar(RebelClasspathResource jar) {
		classpathJar.add(jar);
	}

	public void addClasspathJarset(RebelClasspathResource jarset) {
		classpathJarset.add(jarset);
	}

	public void addWebresource(RebelWebResource webResource) {
		webResources.add(webResource);
	}

	public void setFallbackClasspath(String fallbackClasspath) {
		this.fallbackClasspath = fallbackClasspath;
	}

	public void setWar(RebelWar war) {
		this.war = war;
	}

	private void writeExcludeInclude(Writer writer, RebelClasspathResource r)
			throws IOException {
		writeExcludeInclude(writer, r, 3);
	}

	private void writeExcludeInclude(Writer writer, RebelResource r, int indent)
			throws IOException {
		String indention = repeat("\t", indent);

		if (r.getExcludes() != null) {
			for (String exclude : r.getExcludes()) {
				writer.write(indention + "<exclude name=\""
						+ escapeXml(exclude) + "\"/>\n");
			}
		}

		if (r.getIncludes() != null) {
			for (String include : r.getIncludes()) {
				writer.write(indention + "<include name=\""
						+ escapeXml(include) + "\"/>\n");
			}
		}
	}

	private void writeExcludeInclude(Writer writer, RebelWebResource r)
			throws IOException {
		writeExcludeInclude(writer, r, 4);
	}

	public void writeXml(Writer writer) throws IOException {
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		writer.write("<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">\n");
		writer.write("\n");
		writer.write("\t<classpath");
		
		if (fallbackClasspath != null) {
			writer.write(" fallback=\"" + fallbackClasspath + "\"");
		}
		writer.write(">\n");

		for (RebelClasspathResource r : classpathDir) {
			writer.write("\t\t<dir name=\"" + escapeXml(r.getDirectory())
					+ "\">\n");
			writeExcludeInclude(writer, r);
			writer.write("\t\t</dir>\n");
		}

		for (RebelClasspathResource r : classpathJar) {
			writer.write("\t\t<jar name=\"" + escapeXml(r.getJar()) + "\">\n");
			writeExcludeInclude(writer, r);
			writer.write("\t\t</jar>\n");
		}

		for (RebelClasspathResource r : classpathJarset) {
			writer.write("\t\t<jarset dir=\"" + escapeXml(r.getJarset())
					+ "\">\n");
			writeExcludeInclude(writer, r);
			writer.write("\t\t</jarset>\n");
		}

		for (RebelClasspathResource r : classpathDirset) {
			writer.write("\t\t<dirset dir=\"" + escapeXml(r.getDirset())
					+ "\">\n");
			writeExcludeInclude(writer, r);
			writer.write("\t\t</dirset>\n");
		}

		writer.write("\t</classpath>\n");
		writer.write("\n");

		if (war != null && war.getPath() != null) {
			writer.write("\t<war dir=\"" + escapeXml(war.getPath()) + "\"/>\n");
			writer.write("\n");
		}

		if (webResources.size() > 0) {
			writer.write("\t<web>\n");
			for (RebelWebResource r : webResources) {
				writer.write("\t\t<link target=\"" + escapeXml(r.getTarget())
						+ "\">\n");
				writer.write("\t\t\t<dir name=\"" + escapeXml(r.getDirectory())
						+ "\">\n");
				writeExcludeInclude(writer, r);
				writer.write("\t\t\t</dir>\n");
				writer.write("\t\t</link>\n");
			}
			writer.write("\t</web>\n");
			writer.write("\n");
		}

		writer.write("</application>\n");
		writer.flush();
	}

}
