package com.deew.jt808.util;

/**
 * Utility class for Android LogCat.
 *
 */
public class LogUtils {

  @SuppressWarnings("unchecked")
  public static String makeTag(Class cls) {
    return "JT808_" + cls.getSimpleName();
  }

}
