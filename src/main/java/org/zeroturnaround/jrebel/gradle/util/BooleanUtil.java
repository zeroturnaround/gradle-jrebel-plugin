package org.zeroturnaround.jrebel.gradle.util;

public class BooleanUtil {

  public static Boolean convertNullToFalse(Boolean b) {
    if (b == null) {
      return Boolean.FALSE;
    }
    return b;
  }
  
}