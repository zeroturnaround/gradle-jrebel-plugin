package org.zeroturnaround.javarebel.groovy

import java.io.File;

import org.zeroturnaround.javarebel.groovy.model.RebelClasspath;
import org.zeroturnaround.javarebel.groovy.model.RebelWar;
import org.zeroturnaround.javarebel.groovy.model.RebelWeb;

class RebelPluginExtension {
	String packaging
	File classesDirectory
	File resourcesDirectory
	String warSourceDirectory
	String webappDirectory
	RebelClasspath classpath
	RebelClasspath resourcesClasspath
	RebelWar war
	RebelWeb web
	String rootPath
	File relativePath
	String rebelXmlDirectory
	String showGenerated
	String addResourcesDirToRebelXml
	String alwaysGenerate
}