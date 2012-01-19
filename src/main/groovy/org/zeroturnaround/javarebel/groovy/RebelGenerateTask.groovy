package org.zeroturnaround.javarebel.groovy

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildException
import org.zeroturnaround.javarebel.groovy.model.RebelClasspathResource
import org.zeroturnaround.javarebel.groovy.model.RebelWeb
import org.zeroturnaround.javarebel.groovy.model.RebelWebResource
import org.zeroturnaround.javarebel.groovy.model.RebelClasspath
import org.zeroturnaround.javarebel.groovy.model.RebelWar

import org.apache.commons.lang.StringUtils

class RebelGenerateTask extends DefaultTask {
	def String addResourcesDirToRebelXml
	/**
	 * TODO for some reason it's impossible to pass boolean value from build script into task's variable using conventional mapping. 
	 * Asked on the forum why: http://forums.gradle.org/gradle/topics/problem_with_conventional_mapping
	 */
	def String alwaysGenerate
	def String packaging
	def File rebelXmlDirectory
	def String showGenerated
	def File warSourceDirectory
	def RebelWeb web;
	def File webappDirectory

	def buildClasspath(RebelXmlBuilder builder) {
		def boolean addDefaultAsFirst = true
		def RebelClasspathResource defaultClasspath = null
		def RebelClasspath classpath = project.rebel.classpath

		// check if there is a element with no dir/jar/dirset/jarset set. if there
		// is then don't put default classpath as
		// first but put it where this element was.

		if (classpath != null) {
			RebelClasspathResource[] resources = classpath.getResources()

			if (resources != null && resources.length > 0) {
				for (int i = 0; i < resources.length; i++) {
					RebelClasspathResource r = resources[i]

					if (!r.isTargetSet()) {
						addDefaultAsFirst = false
						defaultClasspath = r
						break;
					}
				}
			}
		}

		if (addDefaultAsFirst) {
			buildDefaultClasspath(builder, defaultClasspath)
		}
	}

	def buildDefaultClasspath(RebelXmlBuilder builder, RebelClasspathResource defaultClasspath) throws BuildException {
		if (isTrue(getAddResourcesDirToRebelXml())) {
			buildDefaultClasspathResources(builder);
		}

		// project output directory
		RebelClasspathResource r = new RebelClasspathResource();
		r.setDirectory(fixFilePath(getClassesDirectory()));

		if (defaultClasspath != null) {
			r.setIncludes(defaultClasspath.getIncludes());
			r.setExcludes(defaultClasspath.getExcludes());
		}

		builder.addClasspathDir(r);
	}

	def buildDefaultClasspathResources(RebelXmlBuilder builder) throws BuildException {
		// TODO implement
	}

	private void buildDefaultWeb(RebelXmlBuilder builder, RebelWebResource defaultWeb)  {
		RebelWebResource r = new RebelWebResource();
		r.setTarget("/");
		r.setDirectory(fixFilePath(getWarSourceDirectory()));

		if (defaultWeb != null) {
			r.setIncludes(defaultWeb.getIncludes());
			r.setExcludes(defaultWeb.getExcludes());
		}

		builder.addWebresource(r);
	}

	def RebelXmlBuilder buildJar() {
		def builder = new RebelXmlBuilder()
		buildClasspath(builder)

		return builder
	}

	def RebelXmlBuilder buildWar() {
		// TODO convert this variable to the field
		def RebelWar war = project.rebel.war

		def builder = new RebelXmlBuilder()
		buildWeb(builder)
		buildClasspath(builder)

		if (war != null) {
			war.setPath(fixFilePath(war.getPath()))
			builder.setWar(war)
		}

		return builder
	}



	private void  buildWeb(RebelXmlBuilder builder) {
		boolean addDefaultAsFirst = true;
		RebelWebResource defaultWeb = null;

		if (web != null) {
			RebelWebResource[] resources = web.getResources();

			if (resources != null && resources.length > 0) {
				for (int i = 0; i < resources.length; i++) {
					RebelWebResource r = resources[i];

					if (r.getDirectory() == null && r.getTarget() == null) {
						defaultWeb = r;
						addDefaultAsFirst = false;
						break;
					}
				}
			}
		}

		if (addDefaultAsFirst) {
			buildDefaultWeb(builder, defaultWeb);
		}

		if (web != null) {
			RebelWebResource[] resources = web.getResources();
			if (resources != null && resources.length > 0) {
				for (int i = 0; i < resources.length; i++) {
					RebelWebResource r = resources[i];
					if (r.getDirectory() == null && r.getTarget() == null) {
						buildDefaultWeb(builder, r);
						continue;
					}
					r.setDirectory(fixFilePath(r.getDirectory()));
					builder.addWebresource(r);
				}
			}
		}
	}

	def String fixFilePath(File file) {
		File baseDir = project.buildFile.getParentFile();

		if (file.isAbsolute() && !isRelativeToPath(new File(baseDir, getRelativePath()), file)) {
			return StringUtils.replace(getCanonicalPath(file), '\\', '/')
		}

		if (!file.isAbsolute()) {
			file = new File(baseDir, file.getPath())
		}

		String relative = getRelativePath(new File(baseDir, getRelativePath()), file)

		if (!(new File(relative)).isAbsolute()) {
			return StringUtils.replace(getRootPath(), '\\', '/') + "/" + relative
		}

		// relative path was outside baseDir

		// if root path is absolute then try to get a path relative to root
		if ((new File(getRootPath())).isAbsolute()) {
			String s = getRelativePath(new File(getRootPath()), file)

			if (!(new File(s)).isAbsolute()) {
				return StringUtils.replace(getRootPath(), '\\', '/') + "/" + s
			} else {
				// root path and the calculated path are absolute, so
				// just return calculated path
				return s
			}
		}

		// return absolute path to file
		return StringUtils.replace(file.getAbsolutePath(), '\\', '/')
	}

	def String fixFilePath(String path) {
		return fixFilePath(new File(path))
	}

	@TaskAction
	def generate() {
		project.logger.info "rebel.alwaysGenerate = " + getAlwaysGenerate()
		project.logger.info "rebel.showGenerated = " + getShowGenerated()
		project.logger.info "rebel.rebelXmlDirectory = " + getRebelXmlDirectory()
		project.logger.info "rebel.warSourceDirectory = " + getWarSourceDirectory()
		project.logger.info "rebel.addResourcesDirToRebelXml = " + getAddResourcesDirToRebelXml()
		project.logger.info "rebel.packaging = " + getPackaging()

		// find rebel.xml location
		def File rebelXmlFile

		if (getRebelXmlDirectory()) {
			rebelXmlFile = new File(getRebelXmlDirectory(), "rebel.xml")
		}

		// find build.gradle location
		def File buildXmlFile = project.buildFile

		if (!isTrue(getAlwaysGenerate()) && rebelXmlFile.exists() && buildXmlFile.exists() && rebelXmlFile.lastModified() > buildXmlFile.lastModified()) {
			return;
		}

		// find the type of the project
		def builder

		if (getPackaging() == "jar") {
			builder = buildJar()
		} else if (getPackaging() == "war") {
			builder = buildWar()
		}

		if (builder) {
			project.logger.info "Processing ${project.group}:${project.name} with packaging " + getPackaging();

            project.logger.info "Generating \"${rebelXmlFile}\"..."

			def Writer w = new StringWriter()
            builder.writeXml(w)

			if (isTrue(getShowGenerated())) {
				try {
					println w.toString()
				} catch (IOException _ignore) {
				}
			}

			try {
				rebelXmlFile.parentFile.mkdirs()
				rebelXmlFile.write w.toString()
			} catch (IOException e) {
				throw new BuildException("Failed writing \"${rebelXmlFile}\"", e);
			} finally {
				if (w != null) {
					try {
						w.close()
					} catch (IOException _ignore) {
					}
				}
			}
		}
	}


	def String getCanonicalPath(File file) throws BuildException {
		try {
			return file.canonicalPath;
		}
		catch (IOException e) {
			throw new BuildException("Failed to get canonical path of " + file.absolutePath, e);
		}
	}

	def File getClassesDirectory() {
		if (project.rebel.classesDirectory) {
			return project.rebel.classesDirectory
		} else {
			return project.file(project.buildDir.absolutePath + File.separator + "classes")
		}
	}

	def String getRelativePath() {
		if (project.rebel.relativePath) {
			return project.rebel.relativePath.absolutePath
		} else {
			return '.'
		}
	}

	def String getRelativePath(File baseDir, File file) throws BuildException {
		// Avoid the common prefix problem (see case 17005)
		//  if:
		//    baseDir = /myProject/web-module/.
		//    file    = /myProject/web-module-shared/something/something/something
		//  then basedirpath cannot be a prefix of the absolutePath, or the relative path will be calculated incorrectly!
		//  This problem is avoided by adding a trailing slash to basedirpath.
		String basedirpath = getCanonicalPath(baseDir) + File.separator;

		String absolutePath = getCanonicalPath(file);

		String relative;

		if (absolutePath.equals(basedirpath)) {
			relative = ".";
		} else if (absolutePath.startsWith(basedirpath)) {
			relative = absolutePath.substring(basedirpath.length());
		} else {
			relative = absolutePath;
		}

		relative = StringUtils.replace(relative, '\\', '/');

		return relative;
	}

	def String getRootPath() {
		if (project.rebel.rootPath)
			return project.rebel.rootPath
		else
			return project.buildFile.parentFile.absolutePath
	}

	def boolean isRelativeToPath(File baseDir, File file) throws BuildException {
		String basedirpath = getCanonicalPath(baseDir);
		String absolutePath = getCanonicalPath(file);

		return absolutePath.startsWith(basedirpath);
	}

	def boolean isTrue(String value) {
		return "true".equals(value);
	}
}