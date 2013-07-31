package org.zeroturnaround.jrebel.gradle.util;

import org.gradle.api.logging.Logger;

/**
 * Tiny wrapper around the Gradle's own logger
 * 
 * @author Sander Sõnajalg
 */
public class LoggerWrapper {
  
  private Logger wrappedLogger;
  
  private static final String PREFIX = "[rebel] ";
  
  public LoggerWrapper(Logger _wrappedLogger) {
    this.wrappedLogger = _wrappedLogger;
  }

  public void error(String msg) {
    wrappedLogger.error(PREFIX + msg);
  }
  
  public void info(String msg) {
    wrappedLogger.info(PREFIX + msg);
  }
  
  public void debug(String msg) {
    wrappedLogger.debug(PREFIX + msg);
  }
  
  public void trace(String msg) {
    wrappedLogger.trace(PREFIX + msg);
  }
  
}