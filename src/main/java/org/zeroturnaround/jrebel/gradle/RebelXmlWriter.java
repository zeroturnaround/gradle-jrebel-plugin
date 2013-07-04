package org.zeroturnaround.jrebel.gradle;

import static org.apache.commons.lang.StringEscapeUtils.escapeXml;
import static org.apache.commons.lang.StringUtils.repeat;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.zeroturnaround.jrebel.gradle.model.RebelClasspathResource;
import org.zeroturnaround.jrebel.gradle.model.RebelResource;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;
import org.zeroturnaround.jrebel.gradle.model.RebelWebResource;
import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;

/**
 * Generate XML based on the in-memory model (RebelMainModel instance).
 *
 * XXX - jesus christ, there must be tens of better ways to generate xml! why....????? [sander]
 * 
 * @author Igor Bljahhin
 */
public class RebelXmlWriter {
  
  /**
   * The main method generating the XML output.
   */
  public String toXmlString(RebelMainModel model) {
    Writer writer = new StringWriter();

    try {
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      writer.write("<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">\n");
      writer.write("\n");
      writer.write("\t<classpath");
      
      if (model.getFallbackClasspath() != null) {
        writer.write(" fallback=\"" + model.getFallbackClasspath() + "\"");
      }
      writer.write(">\n");
  
      for (RebelClasspathResource r : model.getClasspathDirs()) {
        writer.write("\t\t<dir name=\"" + escapeXml(r.getDirectory())
            + "\">\n");
        writeExcludeInclude(writer, r);
        writer.write("\t\t</dir>\n");
      }
  
      for (RebelClasspathResource r : model.getClasspathJars()) {
        writer.write("\t\t<jar name=\"" + escapeXml(r.getJar()) + "\">\n");
        writeExcludeInclude(writer, r);
        writer.write("\t\t</jar>\n");
      }
  
      for (RebelClasspathResource r : model.getClasspathJarsets()) {
        writer.write("\t\t<jarset dir=\"" + escapeXml(r.getJarset())
            + "\">\n");
        writeExcludeInclude(writer, r);
        writer.write("\t\t</jarset>\n");
      }
  
      for (RebelClasspathResource r : model.getClasspathDirsets()) {
        writer.write("\t\t<dirset dir=\"" + escapeXml(r.getDirset())
            + "\">\n");
        writeExcludeInclude(writer, r);
        writer.write("\t\t</dirset>\n");
      }
  
      writer.write("\t</classpath>\n");
      writer.write("\n");
  
      RebelWar war = model.getWar();
      if (war != null && war.getPath() != null) {
        writer.write("\t<war dir=\"" + escapeXml(war.getPath()) + "\"/>\n");
        writer.write("\n");
      }
  
      if (model.getWebResources().size() > 0) {
        writer.write("\t<web>\n");
        for (RebelWebResource r : model.getWebResources()) {
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
      
      return writer.toString();
    }
    // can't see it happening
    catch (IOException _ignore) {
      return null;
    }
    finally {
      // close the stream
      if (writer != null) {
        try {
          writer.close();
        }
        catch (IOException _ignore) {
        }
      }
    } 
  }

  private void writeExcludeInclude(Writer writer, RebelClasspathResource r)
      throws IOException
  {
    writeExcludeInclude(writer, r, 3);
  }

  private void writeExcludeInclude(Writer writer, RebelResource r, int indent)
      throws IOException
  {
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
      throws IOException
  {
    writeExcludeInclude(writer, r, 4);
  }
  
}