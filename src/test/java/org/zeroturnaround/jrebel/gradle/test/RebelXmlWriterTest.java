/**
 *  Copyright (C) 2012 ZeroTurnaround <support@zeroturnaround.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.zeroturnaround.jrebel.gradle.test;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.jrebel.gradle.LegacyRebelGenerateTask;
import org.zeroturnaround.jrebel.gradle.RebelXmlWriter;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspathResource;
import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;
import org.zeroturnaround.jrebel.gradle.model.RebelWebResource;

/**
 * WARNING! The underlying JUnit implementation here is a JUnit 3.x fork, due to XMLUnit's dependencies!
 * 
 * @author Sander Sonajalg
 */
public class RebelXmlWriterTest extends XMLTestCase {

  private static Logger log = LoggerFactory.getLogger(RebelXmlWriterTest.class);
  
  private RebelXmlWriter writer = new RebelXmlWriter(); 
  
  /**
   * This will be run once before each method. Cannot use @Before and @BeforeClass, as this is Junit 3.x here.
   */
  public void setUp() {
    log.info("\n\n\n === Executing test " + getName() + "\n");
    // Overriding some XMLUnit's non-sense defaults (especially the white spaces!)
    XMLUnit.setIgnoreWhitespace(true);
    XMLUnit.setIgnoreComments(true);
  }
  
  @Test
  public void testXmlWithClasspathDir() throws Exception {
    RebelMainModel model = new RebelMainModel();
    
    RebelClasspathResource resource = new RebelClasspathResource();
    resource.setDirectory("build/classes");
    model.addClasspathDir(resource);
    model.setRemoteId("testXmlWithClasspathDir");
    
    String generatedXml = writer.toXmlString(model);

    String expectedResult = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<application generated-by=\"gradle\" " +
        "build-tool-version=\"" + LegacyRebelGenerateTask.GRADLE_VERSION + "\" " +
        "plugin-version=\"" + LegacyRebelGenerateTask.GRADLE_PLUGIN_VERSION + "\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"https://www.zeroturnaround.com http://update.zeroturnaround.com/jrebel/rebel-2_3.xsd\">" +
        "  <id>testXmlWithClasspathDir</id>" +
        "  <classpath> " +
        "    <dir name=\"build/classes\" />" +
        "  </classpath>" +
        "</application>";
    
    log.info("testXmlWithClasspathDir -- generated xml: \n" + generatedXml);    
    
    assertXMLEqual("Generated rebel.xml not matching with expectation!", expectedResult, generatedXml);
  }
  
  /**
   * Test writing the <jar> tag
   */
  @Test
  public void testXmlWithClasspathJar() throws Exception {
    RebelMainModel model = new RebelMainModel();
    
    RebelClasspathResource resource = new RebelClasspathResource();
    resource.setJar("/my/library.jar");
    model.addClasspathJar(resource);
    model.setRemoteId("testXmlWithClasspathJar");
    
    String generatedXml = writer.toXmlString(model);

    String expectedResult = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<application generated-by=\"gradle\" " +
        "build-tool-version=\"" + LegacyRebelGenerateTask.GRADLE_VERSION + "\" " +
        "plugin-version=\"" + LegacyRebelGenerateTask.GRADLE_PLUGIN_VERSION + "\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"https://www.zeroturnaround.com http://update.zeroturnaround.com/jrebel/rebel-2_3.xsd\">" +
        "  <id>testXmlWithClasspathJar</id>" +
        "  <classpath> " +
        "    <jar name=\"/my/library.jar\" />" +
        "  </classpath>" +
        "</application>";
    
    log.info("testXmlWithClasspathJar -- generated xml: \n" + generatedXml);    
    
    assertXMLEqual("Generated rebel.xml not matching with expectation!", expectedResult, generatedXml);
  }

  /**
   * Test writing the <dirset> tag
   */
  @Test
  public void testXmlWithClasspathDirset() throws Exception {
    RebelMainModel model = new RebelMainModel();
    
    RebelClasspathResource resource = new RebelClasspathResource();
    resource.setDirset("/my/workspace/build/classes");
    model.addClasspathDirset(resource);
    model.setRemoteId("testXmlWithClasspathDirset");
    
    String generatedXml = writer.toXmlString(model);

    String expectedResult = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<application generated-by=\"gradle\" " +
        "build-tool-version=\"" + LegacyRebelGenerateTask.GRADLE_VERSION + "\" " +
        "plugin-version=\"" + LegacyRebelGenerateTask.GRADLE_PLUGIN_VERSION + "\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"https://www.zeroturnaround.com http://update.zeroturnaround.com/jrebel/rebel-2_3.xsd\">" +
        "  <id>testXmlWithClasspathDirset</id>" +
        "  <classpath>" +
        "    <dirset dir=\"/my/workspace/build/classes\" />" +
        "  </classpath>" +
        "</application>";
    
    log.info("testXmlWithClasspathDirset -- generated xml: \n" + generatedXml);    
    
    assertXMLEqual("Generated rebel.xml not matching with expectation!", expectedResult, generatedXml);
  }
  
  @Test
  public void testXmlWithClasspathJarset() throws Exception {
    RebelMainModel model = new RebelMainModel();
    
    RebelClasspathResource resource = new RebelClasspathResource();
    resource.setJarset("/my/workspace/build/classes");
    model.addClasspathJarset(resource);
    model.setRemoteId("testXmlWithClasspathJarset");
    
    String generatedXml = writer.toXmlString(model);

    String expectedResult = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<application generated-by=\"gradle\" " +
        "build-tool-version=\"" + LegacyRebelGenerateTask.GRADLE_VERSION + "\" " +
        "plugin-version=\"" + LegacyRebelGenerateTask.GRADLE_PLUGIN_VERSION + "\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"https://www.zeroturnaround.com http://update.zeroturnaround.com/jrebel/rebel-2_3.xsd\">" +
        "  <id>testXmlWithClasspathJarset</id>" +
        "  <classpath> " +
        "    <jarset dir=\"/my/workspace/build/classes\" />" +
        "  </classpath>" +
        "</application>";
    
    log.info("testXmlWithClasspathJarset -- generated xml: \n" + generatedXml);    
    
    assertXMLEqual("Generated rebel.xml not matching with expectation!", expectedResult, generatedXml);
  }
  
  /**
   * Test writing the <jarset> tag
   */
  @Test
  public void testXmlWithWebResource() throws Exception {
    RebelMainModel model = new RebelMainModel();
    
    RebelWebResource resource = new RebelWebResource();
    resource.setDirectory("build/webapp");
    resource.setTarget("/");
    model.addWebResource(resource);
    model.setRemoteId("testXmlWithWebResource");
    
    String generatedXml = writer.toXmlString(model);

    String expectedResult = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<application generated-by=\"gradle\" " +
        "build-tool-version=\"" + LegacyRebelGenerateTask.GRADLE_VERSION + "\" " +
        "plugin-version=\"" + LegacyRebelGenerateTask.GRADLE_PLUGIN_VERSION + "\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"https://www.zeroturnaround.com http://update.zeroturnaround.com/jrebel/rebel-2_3.xsd\">" +
        "  <id>testXmlWithWebResource</id>" +
        "  <classpath />" +
        "  <web>" +
        "    <link target=\"/\">" +
        "      <dir name=\"build/webapp\" />" +
        "    </link>" +
        "  </web>" +
        "</application>";
    
    log.info("testXmlWithWebResource -- generated xml: \n" + generatedXml);    
    
    assertXMLEqual("Generated rebel.xml not matching with expectation!", expectedResult, generatedXml);
  }

  /**
   * Test writing the <exclude> and <include> tags.
   */
  @Test
  public void testXmlWithClasspathIncludesExcludes() throws Exception {
    RebelMainModel model = new RebelMainModel();
    
    RebelClasspathResource resource = new RebelClasspathResource();
    resource.setDirectory("build/classes");
    resource.addExclude("*.xml");
    resource.addExclude("*.properties");
    resource.addInclude("**/*.java");
    model.addClasspathDir(resource);
    model.setRemoteId("testXmlWithClasspathIncludesExcludes");
    
    String generatedXml = writer.toXmlString(model);

    String expectedResult = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<application generated-by=\"gradle\" " +
        "build-tool-version=\"" + LegacyRebelGenerateTask.GRADLE_VERSION + "\" " +
        "plugin-version=\"" + LegacyRebelGenerateTask.GRADLE_PLUGIN_VERSION + "\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"https://www.zeroturnaround.com http://update.zeroturnaround.com/jrebel/rebel-2_3.xsd\">" +
        "  <id>testXmlWithClasspathIncludesExcludes</id>" +
        "  <classpath> " +
        "    <dir name=\"build/classes\">" +
        "      <exclude name=\"*.xml\"/>" +
        "      <exclude name=\"*.properties\"/>" +
        "      <include name=\"**/*.java\"/>" +
        "    </dir>" +
        "  </classpath>" +
        "</application>";
    
    log.info("testXmlWithClasspathIncludesExcludes -- generated xml: \n" + generatedXml);    
    
    assertXMLEqual("Generated rebel.xml not matching with expectation!", expectedResult, generatedXml);
  }
  
  /**
   * Test writing the <war> eĺement and custom dir.
   */
  @Test
  public void testXmlWithWarCustomDir() throws Exception {
    RebelMainModel model = new RebelMainModel();
    
    RebelWar rebelWar = new RebelWar();
    rebelWar.setDir("/my/path");
    model.setWar(rebelWar);
    model.setRemoteId("testXmlWithWarCustomDir");
    
    String generatedXml = writer.toXmlString(model);
    
    String expectedResult = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<application generated-by=\"gradle\" " +
        "build-tool-version=\"" + LegacyRebelGenerateTask.GRADLE_VERSION + "\" " +
        "plugin-version=\"" + LegacyRebelGenerateTask.GRADLE_PLUGIN_VERSION + "\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"https://www.zeroturnaround.com http://update.zeroturnaround.com/jrebel/rebel-2_3.xsd\">" +
        "  <id>testXmlWithWarCustomDir</id>" +
        "  <classpath />" +
        "  <war dir=\"/my/path\" />" +
        "</application>";
    
    log.info("testXmlWithWar -- generated xml: \n" + generatedXml);    
    assertXMLEqual("Generated rebel.xml not matching with expectation!", expectedResult, generatedXml);
  }

  /**
   * Test writing the <war> eĺement and custom file.
   */
  @Test
  public void testXmlWithWarCustomFile() throws Exception {
    RebelMainModel model = new RebelMainModel();

    RebelWar rebelWar = new RebelWar();
    rebelWar.setFile("/my/path/file.war");
    model.setWar(rebelWar);
    model.setRemoteId("testXmlWithWarCustomFile");

    String generatedXml = writer.toXmlString(model);

    String expectedResult = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<application generated-by=\"gradle\" " +
        "build-tool-version=\"" + LegacyRebelGenerateTask.GRADLE_VERSION + "\" " +
        "plugin-version=\"" + LegacyRebelGenerateTask.GRADLE_PLUGIN_VERSION + "\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"https://www.zeroturnaround.com http://update.zeroturnaround.com/jrebel/rebel-2_3.xsd\">" +
        "  <id>testXmlWithWarCustomFile</id>" +
        "  <classpath />" +
        "  <war file=\"/my/path/file.war\" />" +
        "</application>";

    log.info("testXmlWithWar -- generated xml: \n" + generatedXml);
    assertXMLEqual("Generated rebel.xml not matching with expectation!", expectedResult, generatedXml);
  }
}
