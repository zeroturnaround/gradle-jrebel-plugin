package org.zeroturnaround.jrebel.gradle.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;
import org.gradle.tooling.BuildException;

public class FileUtil {

  /**
   * file writer helper
   */
  public static void writeToFile(File file, String contents) throws IOException {
    try (Writer w = new BufferedWriter(new FileWriter(file))) {
      w.write(contents);
    }
  }
 
  public static String getCanonicalPath(File file) throws BuildException {
    try {
      return file.getCanonicalPath();
    }
    catch (IOException e) {
      throw new BuildException("Failed to get canonical path of " + file.getAbsolutePath(), e);
    }
  }
  
  public static boolean isRelativeToPath(File baseDir, File file) throws BuildException {
    String basedirpath = FileUtil.getCanonicalPath(baseDir);
    String absolutePath = FileUtil.getCanonicalPath(file);

    return absolutePath.startsWith(basedirpath);
  }
 
  public static String getRelativePath(File baseDir, File file) throws BuildException {
    // Avoid the common prefix problem (see case 17005)
    // if:
    //  baseDir = /myProject/web-module/.
    //  file  = /myProject/web-module-shared/something/something/something
    // then basedirpath cannot be a prefix of the absolutePath, or the relative path will be calculated incorrectly!
    // This problem is avoided by adding a trailing slash to basedirpath.
    String basedirpath = getCanonicalPath(baseDir) + File.separator;

    String absolutePath = getCanonicalPath(file);

    String relative;

    if (absolutePath.equals(basedirpath)) {
      relative = ".";
    }
    else if (absolutePath.startsWith(basedirpath)) {
      relative = absolutePath.substring(basedirpath.length());
    }
    else {
      relative = absolutePath;
    }

    relative = StringUtils.replace(relative, "\\", "/");

    return relative;
  }
 
}
