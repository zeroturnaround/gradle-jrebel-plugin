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
import org.junit.Before;
import org.junit.Test;
import org.zeroturnaround.jrebel.gradle.RebelXmlWriter;
import org.zeroturnaround.jrebel.gradle.model.RebelClasspathResource;
import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;

public class RebelXmlWriterTest extends XMLTestCase {

  private RebelXmlWriter writer = new RebelXmlWriter(); 

  /**
   * Overriding some XMLUnit's non-sense defaults (especially the white spaces!)
   */
  @Before
  public void setUp() {
    XMLUnit.setIgnoreWhitespace(true);
    XMLUnit.setIgnoreComments(true);
  }
  
  @Test
  public void testSimpleXmlGeneration() throws Exception {
    RebelMainModel model = new RebelMainModel();
    
    RebelClasspathResource resource1 = new RebelClasspathResource();
    resource1.setDirectory("build/classes");
    model.addClasspathDir(resource1);
    
    String generatedXml = writer.toXmlString(model);
    
    
    String expectedResult = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.zeroturnaround.com\" xsi:schemaLocation=\"http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd\">" +
      "    <classpath> " +
      "        <dir name=\"build/classes\">" +
      "        </dir>" +
      "    </classpath>" +
      "</application>";
    
    System.out.println("testSimpleXmlGeneration -- generated xml: \n" + generatedXml);    
    
    assertXMLEqual("Generated rebel.xml not matching with expectation!", expectedResult, generatedXml);
  }
  
}
