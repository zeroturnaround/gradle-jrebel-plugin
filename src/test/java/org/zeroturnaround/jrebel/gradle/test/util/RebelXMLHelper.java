package org.zeroturnaround.jrebel.gradle.test.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RebelXMLHelper {

  private final String content;
  private final Document document;

  public RebelXMLHelper(String content) {
    this.content = content;

    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      this.document = builder.parse(new InputSource(new StringReader(this.content)));
    }
    catch (SAXException e) {
      throw new  RuntimeException(e);
    }
    catch (IOException e) {
      throw new  RuntimeException(e);
    }
    catch (ParserConfigurationException e) {
      throw new  RuntimeException(e);
    }
  }

  public String getRemoteId() {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      return (String) xpath.evaluate("/application/id", document, XPathConstants.STRING);
    }
    catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }

  public String getClasspathDir(int i) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      return (String) xpath.evaluate("/application/classpath/dir[" + i + "]/@name", document, XPathConstants.STRING);
    }
    catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }

  public String getWebDir(int i) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      return (String) xpath.evaluate("/application/web/link/dir[" + i + "]/@name", document, XPathConstants.STRING);
    }
    catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }
}
