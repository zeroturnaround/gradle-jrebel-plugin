package org.zeroturnaround.jrebel.gradle.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class FileUtil {

  /**
   * file writer helper
   */
  public static void writeToFile(File file, String contents) throws IOException {
    Writer w = null;
    try {
      FileOutputStream is = new FileOutputStream(file);
      OutputStreamWriter osw = new OutputStreamWriter(is);    
      w = new BufferedWriter(osw);
      w.write(contents);
    }
    finally {
      w.close();
    }
  }
  
}