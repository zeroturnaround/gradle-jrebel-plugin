package org.zeroturnaround.jrebel.gradle;

import static org.zeroturnaround.jrebel.gradle.LegacyRebelGenerateTask.PACKAGING_TYPE_JAR;
import static org.zeroturnaround.jrebel.gradle.LegacyRebelGenerateTask.PACKAGING_TYPE_WAR;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gradle.api.Project;
import org.gradle.tooling.BuildException;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspath;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspathResource;
import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;
import org.zeroturnaround.jrebel.gradle.model.RebelWeb;
import org.zeroturnaround.jrebel.gradle.model.RebelWebResource;
import org.zeroturnaround.jrebel.gradle.util.FileUtil;
import org.zeroturnaround.jrebel.gradle.util.LoggerWrapper;

public class RebelModelBuilder {

  private LoggerWrapper log;

  public RebelModelBuilder(
      Project project,
      String packaging,
      RebelClasspath classpath,
      RebelWeb web,
      RebelWar war,
      Collection<File> defaultClassesDirectories,
      File defaultResourcesDirectory,
      File defaultWebappDirectory,
      String configuredRootPath,
      File configuredRelativePath,
      File projectDir,
      String remoteId) {
    this.log = new LoggerWrapper(project.getLogger());

    this.packaging = packaging;
    this.classpath = classpath;
    this.web = web;
    this.war = war;
    this.defaultClassesDirectories = defaultClassesDirectories;
    this.defaultResourcesDirectory = defaultResourcesDirectory;
    this.defaultWebappDirectory = defaultWebappDirectory;
    this.configuredRootPath = configuredRootPath;
    this.configuredRelativePath = configuredRelativePath;
    this.projectDir = projectDir;
    this.remoteId = remoteId;
  }

  private final String packaging;

  private final RebelClasspath classpath;
  private final RebelWeb web;
  private final RebelWar war;

  private final Collection<File> defaultClassesDirectories;

  private final File defaultResourcesDirectory;
  private final File defaultWebappDirectory;

  private final String configuredRootPath;

  private final String remoteId;

  /**
   * XXX -- i'm not sure about this property at all. this is used in fixPath, so i don't dare to delete it as well.. ask
   *        Rein who probably originally introduced it to Maven plugin where it was copy-pasted from.
   */
  private File configuredRelativePath;

  private File projectDir;

  public RebelMainModel build() {
    RebelMainModel rebelMainModel;

    // find the type of the project
    if (PACKAGING_TYPE_JAR.equals(packaging)) {
      rebelMainModel = buildModelForJar();
    }
    else if (PACKAGING_TYPE_WAR.equals(packaging)) {
      rebelMainModel = buildModelForWar();
    } else {
      throw new IllegalArgumentException("Illegal packaging type value '" + packaging + "'. Supported values are 'jar' and 'war'");
    }

    return rebelMainModel;
  }

  /**
   * Construct a builder for jar projects
   */
  private RebelMainModel buildModelForJar() {
    log.info("Building rebel backend model for jar ..");
    RebelMainModel model = new RebelMainModel();
    model.setRemoteId(remoteId);

    buildClasspath(model);

    log.info("Backend model eventually built: " + model);
    return model;
  }

  /**
   * Construct a builder for war projects
   */
  private RebelMainModel buildModelForWar() {
    log.info("Building rebel backend model for war ..");
    RebelMainModel model = new RebelMainModel();
    model.setRemoteId(remoteId);

    buildWeb(model);
    buildClasspath(model);
    buildWar(model);

    log.info("Backend model eventually built: " + model);
    return model;
  }

  /**
   * Compile the model that corresponds to the <classpath> node in rebel.xml.
   */
  private void buildClasspath(RebelMainModel model) {

    // User has defined no 'classpath {}' block in the DSL configuration. Just add the default and return.
    if (classpath == null) {
      log.info("No custom classpath configuration found .. using the defaults");
      buildDefaultClasspath(model, null);
    }

    // User has provided custom 'classpath {}' configuration
    else {
      // Search for the default element. If we find it, we have to place it exactly into the same place where we
      // found it (preserving the order). If we *don't* find it, we'll add the default classpath as first element.
      boolean addDefaultAsFirst = true;
      RebelClasspathResource defaultClasspath = null;

      // Just search for the default element. Don't add anything anywhere yet.
      for (RebelClasspathResource resource : classpath.getResources()) {
        // we found the default.
        if (resource.isDefaultClasspathElement()) {
          addDefaultAsFirst = false;
          defaultClasspath = resource;
          break;
        }
      }

      // Default classpath element not found. Put the default as first.
      if (addDefaultAsFirst) {
        // check if configuration allows adding the default
        buildDefaultClasspath(model, defaultClasspath);
      }

      // Iterate through all classpath elements and add them.
      for (RebelClasspathResource resource : classpath.getResources()) {

        // Special treatment for the default.
        if (resource.isDefaultClasspathElement()) {
          buildDefaultClasspath(model, resource);
        }
        // An ordinary element. Add it.
        else {
          // TODO TODO TODO add the fixpath stuff!! --- better implementation!!
          // TODO fix paths for other elements as well!
          resource.setDirectory(fixFilePath(resource.getDirectory()));
          model.addClasspathDir(resource);
        }
      }
    }
  }

  /**
   * Add the default classes directory to classpath
   */
  private void buildDefaultClasspath(RebelMainModel model, RebelClasspathResource defaultClasspath) throws BuildException {
    // Add default resources dir to rebel.xml unless user's configuration disallows it
    if (classpath == null || !classpath.isOmitDefaultResourcesDir()) {
      addDefaultResourcesDirToClasspath(model);
    }

    // Add default classes dir to rebel.xml unless user's configuration disallows it
    if (classpath == null || !classpath.isOmitDefaultClassesDir()) {
      addDefaultClassesDirToClasspath(model, defaultClasspath);
    }
  }

  /**
   * Add the default classes directory to classpath (create the dirs if dont exist yet)
   */
  private void addDefaultClassesDirToClasspath(RebelMainModel model, RebelClasspathResource defaultClasspath) {
    for (File classesDir : defaultClassesDirectories) {
      // project output directory
      RebelClasspathResource classpathResource = new RebelClasspathResource();

      String fixedDefaultClassesDirectory = fixFilePath(classesDir);
      log.info("fixed default classes directory : " + fixedDefaultClassesDirectory);

      classpathResource.setDirectory(fixedDefaultClassesDirectory);

      if (defaultClasspath != null) {
        classpathResource.setIncludes(defaultClasspath.getIncludes());
        classpathResource.setExcludes(defaultClasspath.getExcludes());
      }

      model.addClasspathDir(classpathResource);
      createIfDoesNotExist(classesDir);
    }
  }

  /**
   * Add the default resources directory to classpath (create the dir if dont exist yet)
   */
  private void addDefaultResourcesDirToClasspath(RebelMainModel model) throws BuildException {
    if (defaultResourcesDirectory == null) {
      return;
    }
    log.info("Adding default resources directory to classpath ..");

    RebelClasspathResource resourcesClasspathResource = new RebelClasspathResource();
    String fixedDefaultResourcesDir = fixFilePath(defaultResourcesDirectory);
    log.info("Default resources directory after normalizing: " + fixedDefaultResourcesDir);

    resourcesClasspathResource.setDirectory(fixedDefaultResourcesDir);
    model.addClasspathDir(resourcesClasspathResource);
    createIfDoesNotExist(defaultResourcesDirectory);
  }

  /**
   * Build model for thw <war> element in rebel.xml
   */
  private void buildWar(RebelMainModel model) {
    // fix the path on the RebelWar object (whoooh...not nicest and not the nicest placing)
    if (war != null) {
      if (war.getDir()!= null) {
        war.setOriginalDir(war.getDir());
        war.setDir(fixFilePath(war.getDir()));
      }
      if (war.getFile()!= null) {
        war.setOriginalFile(war.getFile());
        war.setFile(fixFilePath(war.getFile()));
      }
      model.setWar(war);
    }
  }

  /**
   * Build the model for the <web> element in rebel.xml
   */
  private void buildWeb(RebelMainModel model) {

    // User has not devfined a 'web {}' block
    if (web == null) {
      buildDefaultWeb(model, null);
    }

    // A 'web {}' block was defined in configuration DSL
    else {

      // Go through all elements, look up the default one
      boolean addDefaultAsFirst = true;
      RebelWebResource defaultWeb = null;

      for (RebelWebResource resource : web.getResources()) {
        if (resource.isDefaultElement()) {
          defaultWeb = resource;
          addDefaultAsFirst = false;
          break;
        }
      }

      // Add the default one as first, if a specific location was not specified by the empty element
      if (addDefaultAsFirst) {
        if (web != null && !web.getOmitDefault()) {
          buildDefaultWeb(model, defaultWeb);
        }
      }

      // Add all the other elements from the user's configuration
      List<RebelWebResource> resources = web.getResources();
      if (resources != null && resources.size() > 0) {
        for (int i = 0; i < resources.size(); i++) {
          RebelWebResource resource = resources.get(i);

          // Add the default element
          if (resource.isDefaultElement()) {
            if (!web.getOmitDefault()) {
              buildDefaultWeb(model, resource);
            }
          }
          // Add a normal, non-default element
          else {
            resource.setDirectory(fixFilePath(resource.getDirectory()));
            model.addWebResource(resource);
          }
        }
      }
    }
  }

  /**
   * Get the absolute, normalized path.
   * XXX maybe should be moved to an external utility class
   */
  private String fixFilePath(File file) {
    File baseDir = getProjectDir();

    if (file.isAbsolute() && !FileUtil.isRelativeToPath(new File(baseDir, getRelativePath()), file)) {
      return StringUtils.replace(FileUtil.getCanonicalPath(file), "\\", "/");
    }

    if (!file.isAbsolute()) {
      file = new File(baseDir, file.getPath());
    }

    String relative = FileUtil.getRelativePath(new File(baseDir, getRelativePath()), file);

    if (!(new File(relative)).isAbsolute()) {
      return StringUtils.replace(getRootPath(), "\\", "/") + "/" + relative;
    }

    // relative path was outside baseDir

    // if root path is absolute then try to get a path relative to root
    if ((new File(getRootPath())).isAbsolute()) {
      String s = FileUtil.getRelativePath(new File(getRootPath()), file);

      if (!(new File(s)).isAbsolute()) {
        return StringUtils.replace(getRootPath(), "\\", "/") + "/" + s;
      }
      else {
        // root path and the calculated path are absolute, so
        // just return calculated path
        return s;
      }
    }

    // return absolute path to file
    return StringUtils.replace(file.getAbsolutePath(), "\\", "/");
  }

  private File getProjectDir() {
    return projectDir;
  }

  /**
   * The default for the <web> element in rebel.xml
   */
  private void buildDefaultWeb(RebelMainModel model, RebelWebResource defaultWeb) {
    RebelWebResource r = new RebelWebResource();
    r.setTarget("/");
    r.setDirectory(fixFilePath(defaultWebappDirectory));

    if (defaultWeb != null) {
      r.setIncludes(defaultWeb.getIncludes());
      r.setExcludes(defaultWeb.getExcludes());
    }

    model.addWebResource(r);
  }

  private String fixFilePath(String path) {
    return fixFilePath(new File(path));
  }

  private String getRelativePath() {
    if (configuredRelativePath != null) {
      return configuredRelativePath.getAbsolutePath();
    }
    else {
      return ".";
    }
  }

  private String getRootPath() {
    if (configuredRootPath != null) {
      return configuredRootPath;
    }
    else {
      return projectDir.getAbsolutePath();
    }
  }

  private void createIfDoesNotExist(File dir) {
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }
}
