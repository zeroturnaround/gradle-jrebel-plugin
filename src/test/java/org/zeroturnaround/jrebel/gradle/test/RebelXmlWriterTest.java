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
import org.zeroturnaround.jrebel.gradle.RebelXmlWriter;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspathResource;
import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;
import org.zeroturnaround.jrebel.gradle.model.RebelWebResource;

/**
 * WARNING! The underlying JUnit implementation here is a JUnit 3.x fork, due to XMLUnit's dependencies!
 * 
 * @author Sander Sõnajalg
 */
public class RebelXmlWriterTest extends XMLTestCase {

  private RebelXmlWriter writer = new RebelXmlWriter(); 
  
  /**
   * This will be run once before each method. Cannot use @Before and @BeforeClass, as this is Junit 3.x here.
   */
  public void setUp() {
    log("\n\n === Executing test " + getName() + "\n");
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
    
    String generatedXml = writer.toXmlString(model);
    
    
    String expectedResult = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">" +
      "  <classpath> " +
      "    <dir name=\"build/classes\" />" +
      "  </classpath>" +
      "</application>";
    
    System.out.println("testXmlWithClasspathDir -- generated xml: \n" + generatedXml);    
    
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
    
    String generatedXml = writer.toXmlString(model);
    
    String expectedResult = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">" +
      "  <classpath> " +
      "    <jar name=\"/my/library.jar\" />" +
      "  </classpath>" +
      "</application>";
    
    System.out.println("testXmlWithClasspathJar -- generated xml: \n" + generatedXml);    
    
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
    
    String generatedXml = writer.toXmlString(model);
    
    String expectedResult = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">" +
      "  <classpath> " +
      "    <dirset dir=\"/my/workspace/build/classes\" />" +
      "  </classpath>" +
      "</application>";
    
    System.out.println("testXmlWithClasspathDirset -- generated xml: \n" + generatedXml);    
    
    assertXMLEqual("Generated rebel.xml not matching with expectation!", expectedResult, generatedXml);
  }
  
  @Test
  public void testXmlWithClasspathJarset() throws Exception {
    RebelMainModel model = new RebelMainModel();
    
    RebelClasspathResource resource = new RebelClasspathResource();
    resource.setJarset("/my/workspace/build/classes");
    model.addClasspathJarset(resource);
    
    String generatedXml = writer.toXmlString(model);
    
    String expectedResult = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">" +
      "  <classpath> " +
      "    <jarset dir=\"/my/workspace/build/classes\" />" +
      "  </classpath>" +
      "</application>";
    
    System.out.println("testXmlWithClasspathJarset -- generated xml: \n" + generatedXml);    
    
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
    
    String generatedXml = writer.toXmlString(model);
    
    String expectedResult = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">" +
      "  <classpath />" +
      "  <web>" +
      "    <link target=\"/\">" +
      "      <dir name=\"build/webapp\" />" +
      "    </link>" +
      "  </web>" +
      "</application>";
    
    System.out.println("testXmlWithWebResource -- generated xml: \n" + generatedXml);    
    
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
    
    String generatedXml = writer.toXmlString(model);
    
    String expectedResult = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">" +
      "  <classpath> " +
      "    <dir name=\"build/classes\">" +
      "      <exclude name=\"*.xml\"/>" +
      "      <exclude name=\"*.properties\"/>" +
      "      <include name=\"**/*.java\"/>" +
      "    </dir>" +
      "  </classpath>" +
      "</application>";
    
    System.out.println("testXmlWithClasspathIncludesExcludes -- generated xml: \n" + generatedXml);    
    
    assertXMLEqual("Generated rebel.xml not matching with expectation!", expectedResult, generatedXml);
  }
  
  /**
   * Test writing the <war> eĺement.
   */
  @Test
  public void testXmlWithWar() throws Exception {
    RebelMainModel model = new RebelMainModel();
    
    RebelWar rebelWar = new RebelWar();
    rebelWar.setPath("/my/path");
    model.setWar(rebelWar);
    
    String generatedXml = writer.toXmlString(model);
    
    String expectedResult = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">" +
      "  <classpath />" +
      "  <war dir=\"/my/path\" />" +
      "</application>";
    
    System.out.println("testXmlWithWar -- generated xml: \n" + generatedXml);    
    
    assertXMLEqual("Generated rebel.xml not matching with expectation!", expectedResult, generatedXml);    
  }
  
  /**
   * TODO implement properly
   */
  private static void log(String msg) {
    System.out.println(msg);
  }
}
