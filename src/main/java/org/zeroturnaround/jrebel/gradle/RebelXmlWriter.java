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
 * @author Igor Bljahhin (or maybe just a copy-paste from the Maven plugin?)
 */
public class RebelXmlWriter {
  
  /**
   * The main method generating the XML output.
   */
  public String toXmlString(RebelMainModel model) {
    Writer writer = new StringWriter();

    try {
      writeHeader(writer);
      
      writeClasspath(model, writer);
  
      writeWar(model, writer);
      
      writeWeb(model, writer);
  
      writeFooter(writer);
      
      writer.flush();
      return writer.toString();
    }
    // can't see it happening
    catch (IOException e) {
      return null;
    }
    finally {
      // close the stream
      if (writer != null) {
        try {
          writer.close();
        }
        catch (IOException e) {
        }
      }
    } 
  }

  private void writeHeader(Writer writer) throws IOException {
    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    writer.write("\n<!--\n" +
      "  This is the JRebel configuration file. It maps the running application to your IDE workspace, enabling JRebel reloading for this project.\n" +
      "  Refer to https://manuals.zeroturnaround.com/jrebel/standalone/config.html for more information.\n" + "-->\n" +
      "<application generated-by=\"gradle\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"http://www.zeroturnaround.com http://update.zeroturnaround.com/jrebel/rebel-2_1.xsd\">\n");
  }
  
  /**
   * Write the <classpath> element.
   */
  private void writeClasspath(RebelMainModel model, Writer writer) throws IOException {
    writer.write("  <classpath");
    
    if (model.getFallbackClasspath() != null) {
      writer.write(" fallback=\"" + model.getFallbackClasspath() + "\"");
    }
    writer.write(">\n");
 
    // Classpath dirs
    for (RebelClasspathResource resource : model.getClasspathDirs()) {
      writer.write("    <dir name=\"" + escapeXml(resource.getDirectory()) + "\">\n");
      writeExcludeInclude(writer, resource);
      writer.write("    </dir>\n");
    }
 
    // Classpath jars
    for (RebelClasspathResource resource : model.getClasspathJars()) {
      writer.write("    <jar name=\"" + escapeXml(resource.getJar()) + "\">\n");
      writeExcludeInclude(writer, resource);
      writer.write("    </jar>\n");
    }
 
    // Classpath jarsets
    for (RebelClasspathResource resource : model.getClasspathJarsets()) {
      writer.write("    <jarset dir=\"" + escapeXml(resource.getJarset()) + "\">\n");
      writeExcludeInclude(writer, resource);
      writer.write("    </jarset>\n");
    }
 
    // Classpath dirsets
    for (RebelClasspathResource resource : model.getClasspathDirsets()) {
      writer.write("    <dirset dir=\"" + escapeXml(resource.getDirset()) + "\">\n");
      writeExcludeInclude(writer, resource);
      writer.write("    </dirset>\n");
    }
 
    writer.write("  </classpath>\n");
    writer.write("\n");
  }

  /**
   * Write the <web> element.
   */
  private void writeWeb(RebelMainModel model, Writer writer) throws IOException {
    // web resources
    if (model.getWebResources().size() > 0) {
      writer.write("  <web>\n");
      for (RebelWebResource r : model.getWebResources()) {
        writer.write("    <link target=\"" + escapeXml(r.getTarget()) + "\">\n");
        writer.write("      <dir name=\"" + escapeXml(r.getDirectory()) + "\">\n");
        writeExcludeInclude(writer, r);
        writer.write("      </dir>\n");
        writer.write("    </link>\n");
      }
      writer.write("  </web>\n");
      writer.write("\n");
    }
  }

  /**
   * Write the <war> element.
   */
  private void writeWar(RebelMainModel model, Writer writer) throws IOException {
    RebelWar war = model.getWar();
    if (war != null && war.getPath() != null) {
      writer.write("  <war dir=\"" + escapeXml(war.getPath()) + "\"/>\n");
      writer.write("\n");
    }
  }

  private void writeFooter(Writer writer) throws IOException {
    writer.write("</application>\n");
  }

  private void writeExcludeInclude(Writer writer, RebelClasspathResource r) throws IOException {
    writeExcludeIncludeImpl(writer, r, 3);
  }

  private void writeExcludeInclude(Writer writer, RebelWebResource r) throws IOException {
    writeExcludeIncludeImpl(writer, r, 4);
  }

  /**
   * Write <exclude> and <include> elements. This code is shared between writing multiple resources.
   */
  private void writeExcludeIncludeImpl(Writer writer, RebelResource r, int indent) throws IOException {
    String indention = repeat("  ", indent);

    if (r.getExcludes() != null) {
      for (String exclude : r.getExcludes()) {
        writer.write(indention + "<exclude name=\""
            + escapeXml(exclude) + "\"/>\n");
      }
    }

    if (r.getIncludes() != null) {
      for (String include : r.getIncludes()) {
        writer.write(indention + "<include name=\"" + escapeXml(include) + "\"/>\n");
      }
    }
  }

}