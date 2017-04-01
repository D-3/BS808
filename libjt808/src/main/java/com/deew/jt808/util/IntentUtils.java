package com.deew.jt808.util;

/**
 * Utility class for intent.
 *
 */
public class IntentUtils {

  @SuppressWarnings("unchecked")
  public static String makeAction(Class cls, String act) {
    return cls.getCanonicalName() + "." + act.toUpperCase();
  }

}
