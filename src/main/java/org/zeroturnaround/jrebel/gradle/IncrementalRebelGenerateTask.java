package org.zeroturnaround.jrebel.gradle;

import static org.zeroturnaround.jrebel.gradle.LegacyRebelGenerateTask.PACKAGING_TYPE_WAR;
import static org.zeroturnaround.jrebel.gradle.LegacyRebelPlugin.REBEL_EXTENSION_NAME;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.BuildException;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslClasspath;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslMain;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslWar;
import org.zeroturnaround.jrebel.gradle.dsl.RebelDslWeb;
import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;
import org.zeroturnaround.jrebel.gradle.util.FileUtil;
import org.zeroturnaround.jrebel.gradle.util.LoggerWrapper;

public class IncrementalRebelGenerateTask extends DefaultTask implements BaseRebelGenerateTask {

  private final LoggerWrapper log = new LoggerWrapper(getProject().getLogger());

  private final RebelDslMain rebelDsl;

  private Provider<String> configuredRootPath;
  private Provider<File> jrebelBuildDir;

  private Provider<File> defaultWebappDirectory;
  private Provider<Collection<File>> defaultClassesDirectory;
  private Provider<File> defaultResourcesDirectory;
  private Provider<String> remoteId;

  private boolean skipWritingRebelXml;
  private RebelMainModel rebelModel;

  @Nested
  public RebelDslMain getRebelDsl() {
    return rebelDsl;
  }

  @Input
  @Optional
  public String getConfiguredRootPath() {
    return configuredRootPath.getOrNull();
  }

  public void setConfiguredRootPath(Provider<String> provider) {
    configuredRootPath = provider;
  }

  @OutputDirectory
  public File getJRebelBuildDir() {
    return jrebelBuildDir.get();
  }

  public void setJRebelBuildDir(Provider<File> provider) {
    jrebelBuildDir = provider;
  }

  @Internal
  public File getDefaultWebappDirectory() {
    return defaultWebappDirectory != null ? defaultWebappDirectory.getOrNull() : null;
  }

  @Input
  @Optional
  public String getDefaultWebappDirectoryPath() {
    File webappDirectory = getDefaultWebappDirectory();
    return webappDirectory != null ? webappDirectory.getAbsolutePath() : null;
  }

  public void setDefaultWebappDirectory(Provider<File> provider) {
    this.defaultWebappDirectory = provider;
  }

  @Input
  @Optional
  public Collection<File> getDefaultClassesDirectory() {
    return defaultClassesDirectory.getOrNull();
  }


  public void setDefaultClassesDirectory(Provider<Collection<File>> provider) {
    this.defaultClassesDirectory = provider;
  }

  @Internal
  public File getDefaultResourcesDirectory() {
    return defaultResourcesDirectory.getOrNull();
  }

  @Input
  @Optional
  public String getDefaultResourcesDirectoryPath() {
    File resourcesDirectory = getDefaultResourcesDirectory();
    return resourcesDirectory != null ? resourcesDirectory.getAbsolutePath() : null;
  }

  public void setDefaultResourcesDirectory(Provider<File> provider) {
    this.defaultResourcesDirectory = provider;
  }

  /*
    Force a rebuild with ever changing input value if 'alwaysGenerate = true'
   */
  @Input
  public Long getAlwaysGenerateTrigger() {
    return rebelDsl.getAlwaysGenerate() ? System.currentTimeMillis() : 0;
  }

  @TaskAction
  public void generate() {
    RebelDslMain rebelDsl = getRebelDsl();
    log.info("rebel.alwaysGenerate = " + rebelDsl.getAlwaysGenerate());
    log.info("rebel.showGenerated = " + rebelDsl.getShowGenerated());
    log.info("rebel.rebelXmlDirectory = " + rebelDsl.getRebelXmlDirectory());
    log.info("rebel.packaging = " + rebelDsl.getPackaging());
    log.info("rebel.war = " + rebelDsl.getWar());
    log.info("rebel.web = " + rebelDsl.getWeb());
    log.info("rebel.classpath = " + rebelDsl.getClasspath());
    log.info("rebel.remoteId = " + rebelDsl.getRemoteId());
    log.info("rebel.defaultClassesDirectories = " + getDefaultClassesDirectory());
    log.info("rebel.defaultResourcesDirectory = " + getDefaultResourcesDirectoryPath());
    log.info("rebel.defaultWebappDirectory = " + getDefaultWebappDirectoryPath());
    log.info("rebel.configuredRootPath = " + getConfiguredRootPath());
    log.info("rebel.configuredRelativePath = " + getRebelDsl().getRelativePathName());
    log.info("rebel.generateRebelRemote = " + getRebelDsl().getGenerateRebelRemote());

    log.info("jrebel output dir " + jrebelBuildDir.getOrNull());

    generateRebelXml();
    generateRemoteXml();
  }

  private void generateRebelXml() {
    if (skipWritingRebelXml) {
      return;
    }

    RebelDslClasspath classpath = getRebelDsl().getClasspath();
    RebelDslWeb web = getRebelDsl().getWeb();
    RebelDslWar war = getRebelDsl().getWar();
    rebelModel = new RebelModelBuilder(
        getProject(),
        getRebelDsl().getPackaging(),
        classpath != null ? classpath.toRebelClasspath() : null,
        web != null ? web.toRebelWeb() : null,
        war != null ? war.toRebelWar() :null,
        getDefaultClassesDirectory(),
        getDefaultResourcesDirectory(),
        getDefaultWebappDirectory(),
        getConfiguredRootPath(),
        getRebelDsl().getRelativePath(),
        getProject().getProjectDir(),
        remoteId.getOrNull()
    ).build();

    File buildDir = jrebelBuildDir.get();
    File rebelXmlFile = new File(buildDir, "rebel.xml");
    try {
      if (!buildDir.exists() && !buildDir.mkdirs()) {
        throw new IOException("Failed to create directory for rebel.xml: " + buildDir);
      }

      log.info("Processing " + getProject().getGroup() + ":" + getProject().getName() + " with packaging " + getRebelDsl().getPackaging());
      log.info("Generating \"" + rebelXmlFile.getAbsolutePath() + "\"...");

      String xmlFileContents = rebelModel.toXmlString();

      if (getRebelDsl().getShowGenerated()) {
        log.lifecycle(xmlFileContents);
      }

      FileUtil.writeToFile(rebelXmlFile, xmlFileContents);
    }
    catch (IOException e) {
      throw new BuildException("Failed writing " + rebelXmlFile, e);
    }
  }

  private void generateRemoteXml() {
    if (!getRebelDsl().getGenerateRebelRemote()) {
      return;
    }

    File buildDir = this.jrebelBuildDir.get();
    File rebelRemoteFile = new File(buildDir, "rebel-remote.xml");

    if (!buildDir.exists() && !buildDir.mkdirs()) {
      log.error("Failed to create directory for rebel-remote.xml: " + buildDir);
      return;
    }

    log.info("Generating rebel-remote.xml with id " + remoteId.getOrNull() + " to \"" + buildDir + "\"");

    Writer writer = null;
    try {
      writer = new StringWriter();

      RebelRemoteWriter remoteWriter = new RebelRemoteWriter(remoteId.getOrNull());
      remoteWriter.writeXml(writer);

      String contents = writer.toString();
      if (getRebelDsl().getShowGenerated()) {
        log.lifecycle(contents);
      }
      FileUtil.writeToFile(rebelRemoteFile, contents);
      writer.close();
    }
    catch (IOException e) {
      throw new BuildException("Failed writing " + rebelRemoteFile, e);
    }
    finally {
      IOUtils.closeQuietly(writer);
    }
  }

  public IncrementalRebelGenerateTask() {
    this.rebelDsl = (RebelDslMain) getProject().getExtensions().getByName(REBEL_EXTENSION_NAME);

    getProject().getPlugins().withType(WarPlugin.class).all(new Action<Plugin>() {
      public void execute(Plugin p) {

        getRebelDsl().setPackaging(PACKAGING_TYPE_WAR);

        setDefaultWebappDirectory(getProject().provider(new Callable<File>() {
          @Override
          public File call() {
            try {
              WarPluginConvention warConvention = getProject().getConvention().getPlugin(WarPluginConvention.class);
              return warConvention.getWebAppDir();
            }
            catch (Exception e) {
              return null;
            }
          }
        }));
      }
    });

    setConfiguredRootPath(
        getProject().getProviders().provider(new Callable<String>() {
          @Override
          public String call() throws Exception {
            String rootPathFromProjectProperties = getProject().hasProperty("rebel.rootPath") ? getProject().property("rebel.rootPath").toString() : null;

            // The value from external configuration wins
            String rootPath;
            if (rootPathFromProjectProperties != null) {
              rootPath = rootPathFromProjectProperties;
            }
            else {
              rootPath = getRebelDsl().getRootPath();
            }
            return rootPath;
          }
        })
    );
  }

  public void configureSourceSet(final SourceSetDefaults sourceSetDefaults) {
    setDefaultClassesDirectory(sourceSetDefaults.classesDirs);
    setDefaultResourcesDirectory(sourceSetDefaults.resourcesDir);

    final Provider<File> jRebelBuildDir = getProject().provider(new Callable<File>() {
      @Override
      public File call() throws Exception {
        File outputBase;
        if (getProject().hasProperty("rebel.rebelXmlDirectory")) {
          return new File(getProject().property("rebel.rebelXmlDirectory").toString());
        }
        else if (rebelDsl.getRebelXmlDirectory() != null) {
          outputBase = new File(getProject().getProjectDir(), rebelDsl.getRebelXmlDirectory());
        }
        else {
          outputBase = new File(getProject().getBuildDir(), "jrebel");
        }
        if (sourceSetDefaults.xmlOutputDirName != null) {
          return new File(outputBase, sourceSetDefaults.xmlOutputDirName);
        }
        return outputBase;
      }
    });

    setJRebelBuildDir(jRebelBuildDir);

    remoteId = getProject().provider(new Callable<String>() {
      @Override
      public String call() throws Exception {
        if (rebelDsl.getRemoteId() != null) {
          return rebelDsl.getRemoteId();
        }

        String projectPath = getProject().getPath();
        StringTokenizer tokenizer = new StringTokenizer(projectPath, ":");
        StringBuilder remoteIdBuilder = new StringBuilder();

        boolean first = true;
        if (projectPath.charAt(0) == ':') {
          remoteIdBuilder.append(getProject().getRootProject().getName());
          first = false;
        }

        while (tokenizer.hasMoreTokens()) {
          if (!first) {
            remoteIdBuilder.append('.');
          } else {
            first = false;
          }
          remoteIdBuilder.append(tokenizer.nextToken());
        }

        if (sourceSetDefaults.remoteIdSuffix != null) {
          remoteIdBuilder.append('.').append(sourceSetDefaults.remoteIdSuffix);
        }
        return remoteIdBuilder.toString();
      }
    });
  }

  @Override
  public String getPackaging() {
    return getRebelDsl().getPackaging();
  }

  @Override
  public void skipWritingRebelXml() {
    this.skipWritingRebelXml = true;
  }

  @Override
  public RebelMainModel getRebelModel() {
    return rebelModel;
  }

  @Override
  public void propagateConventionMappingSettings() {
  }

  @Override
  public boolean getShowGenerated() {
    return getRebelDsl().getShowGenerated();
  }

  @Override
  public boolean getAlwaysGenerate() {
    return getRebelDsl().getAlwaysGenerate();
  }

  @Override
  public RebelWar getWar() {
    return rebelModel != null ? rebelModel.getWar() : null;
  }
}
