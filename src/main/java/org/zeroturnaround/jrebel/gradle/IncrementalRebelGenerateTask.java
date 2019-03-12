package org.zeroturnaround.jrebel.gradle;

import static org.zeroturnaround.jrebel.gradle.LegacyRebelPlugin.REBEL_EXTENSION_NAME;
import static org.zeroturnaround.jrebel.gradle.LegacyRebelGenerateTask.PACKAGING_TYPE_WAR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSetOutput;
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

import groovy.lang.Closure;

public class IncrementalRebelGenerateTask extends DefaultTask implements BaseRebelGenerateTask {

  private LoggerWrapper log = new LoggerWrapper(getProject().getLogger());

  private RebelDslMain rebelDsl;

  private Provider<String> configuredRootPath;
  private Provider<File> jrebelBuildDir;

  private Provider<File> defaultWebappDirectory;
  private Provider<List<File>> defaultClassesDirectory;
  private Provider<File> defaultResourcesDirectory;
  private Long alwaysGenerateTrigger;

  private boolean isPluginConfigured;
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

  @Input
  @Optional
  public File getDefaultWebappDirectory() {
    return defaultWebappDirectory != null ? defaultWebappDirectory.getOrNull() : null;
  }

  public void setDefaultWebappDirectory(Provider<File> provider) {
    this.defaultWebappDirectory = provider;
  }

  @Input
  @Optional
  public List<File> getDefaultClassesDirectory() {
    return defaultClassesDirectory.getOrNull();
  }


  public void setDefaultClassesDirectory(Provider<List<File>> provider) {
    this.defaultClassesDirectory = provider;
  }

  @Optional
  @Input
  public File getDefaultResourcesDirectory() {
    return defaultResourcesDirectory.getOrNull();
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
    // Only able to run if the 'RebelPlugin#configure' block has been executed, i.e. if the Java Plugin has been added.
    if (!isPluginConfigured) {
      throw new IllegalStateException(
          "generateRebel is only valid when JavaPlugin is applied directly or indirectly " +
              "(via other plugins that apply it implicitly, like Groovy or War); please update your build"
      );
    }

    RebelDslMain rebelDsl = getRebelDsl();
    log.info("rebel.alwaysGenerate = " + rebelDsl.getAlwaysGenerate());
    log.info("rebel.showGenerated = " + rebelDsl.getShowGenerated());
    log.info("rebel.rebelXmlDirectory = " + rebelDsl.getRebelXmlDirectory());
    log.info("rebel.packaging = " + rebelDsl.getPackaging());
    log.info("rebel.war = " + rebelDsl.getWar());
    log.info("rebel.web = " + rebelDsl.getWeb());
    log.info("rebel.classpath = " + rebelDsl.getClasspath());
    log.info("rebel.defaultClassesDirectories = " + getDefaultClassesDirectory());
    log.info("rebel.defaultResourcesDirectory = " + getDefaultResourcesDirectory());
    log.info("rebel.defaultWebappDirectory = " + getDefaultWebappDirectory());
    log.info("rebel.configuredRootPath = " + getConfiguredRootPath());
    log.info("rebel.configuredRelativePath = " + getRebelDsl().getRelativePath());


    log.info("jrebel output dir " + jrebelBuildDir.getOrNull());

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
        getProject().getProjectDir()
    ).build();

    if (!skipWritingRebelXml) {
      File buildDir = jrebelBuildDir.get();
      if (!buildDir.exists()) {
        buildDir.mkdir();
      }

      File rebelXmlFile = new File(buildDir, "rebel.xml");
      log.info("Processing " + getProject().getGroup() + ":" + getProject().getName()  + " with packaging " + getRebelDsl().getPackaging());
      log.info("Generating \"" + rebelXmlFile.getAbsolutePath() + "\"...");

      try {
        String xmlFileContents = rebelModel.toXmlString();

        if (getRebelDsl().getShowGenerated()) {
          System.out.println(xmlFileContents);
        }

        rebelXmlFile.getParentFile().mkdirs();

        FileUtil.writeToFile(rebelXmlFile, xmlFileContents);
      }
      catch (IOException e) {
        throw new BuildException("Failed writing \"${rebelXmlFile}\"", e);
      }
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

    setDefaultClassesDirectory(getProject().provider(new Callable<List<File>>() {
      @Override
      public List<File> call() {
        JavaPluginConvention javaConvention = getProject().getConvention().findPlugin(JavaPluginConvention.class);
        if (javaConvention != null ) {
          SourceSetOutput main = javaConvention.getSourceSets().getByName("main").getOutput();
          return new ArrayList<File>(main.getClassesDirs().getFiles());
        } else {
          return null;
        }
      }
    }));

    setDefaultResourcesDirectory(getProject().provider(new Callable<File>() {
      @Override
      public File call() {
        JavaPluginConvention javaConvention = getProject().getConvention().findPlugin(JavaPluginConvention.class);
        if (javaConvention != null ) {
          return javaConvention.getSourceSets().getByName("main").getOutput().getResourcesDir();
        } else {
          return null;
        }
      }
    }));

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

    final Provider<File> jRebelBuildDir = getProject().provider(new Callable<File>() {
      @Override
      public File call() throws Exception {
        if (getProject().hasProperty("rebel.rebelXmlDirectory")) {
          return new File(getProject().getProjectDir(), getProject().property("rebel.rebelXmlDirectory").toString());
        }
        else if (rebelDsl.getRebelXmlDirectory() != null) {
          return new File(getProject().getProjectDir(), rebelDsl.getRebelXmlDirectory());
        }
        else {
          return new File(getProject().getBuildDir(), "jrebel");
        }
      }
    });

    final Closure<File> fileClosure = new Closure<File>(this) {
      @Override
      public File call() {
        return jRebelBuildDir.getOrNull();
      }
    };

    setJRebelBuildDir(jRebelBuildDir);

    final IncrementalRebelGenerateTask generateTask = this;
    getProject().getPlugins().withType(JavaPlugin.class).all(new Action<Plugin>() {
      public void execute(Plugin p) {
        Copy processResourcesTask = (Copy) getProject().getTasks().getByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME);
        processResourcesTask.dependsOn(generateTask);
        processResourcesTask.from(fileClosure);

        setPluginConfigured();
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
    return rebelModel.getWar();
  }

  void setPluginConfigured() {
    this.isPluginConfigured = true;
  }
}
