package org.zeroturnaround.jrebel.gradle;

import java.io.IOException;
import java.io.Writer;

import org.zeroturnaround.jrebel.gradle.util.SystemUtils;

public class RebelRemoteWriter {

  public RebelRemoteWriter(String remoteId) {
    this.remoteId = remoteId;
  }

  private final String remoteId;

  public void writeXml(Writer writer) throws IOException {
    writer.write("" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<rebel-remote xmlns=\"http://www.zeroturnaround.com/rebel/remote\">\n" +
        "  <id>" + SystemUtils.ensurePathAndURLSafeName(remoteId) + "</id>\n" +
        "</rebel-remote>"
    );
  }
}
