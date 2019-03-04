package org.zeroturnaround.jrebel.gradle.test.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class TestUtils {

  public static void writeFile(File destination, String content) throws IOException {
    FileUtils.writeStringToFile(destination, content, Charset.forName("UTF-8"));
  }
  
  public static Iterable<? extends File> getPluginTestClasspath() throws IOException {
    String pluginClassPathTxt = TestUtils.getClasspathResourceAsString("/plugin-classpath.txt");
    List<File> classpath = new ArrayList<File>();

    for (String file : pluginClassPathTxt.split("\n")) {
      classpath.add(new File(file));
    }

    return classpath;
  }

  private  static String getClasspathResourceAsString(String path) throws IOException {
    InputStream stream = TestUtils.class.getResourceAsStream(path);
    return IOUtils.toString(stream, Charset.forName("UTF-8"));
  }
}
