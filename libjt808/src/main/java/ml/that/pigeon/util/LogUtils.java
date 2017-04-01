package ml.that.pigeon.util;

/**
 * Utility class for Android LogCat.
 *
 * @author That Mr.L (thatmr.l@gmail.com)
 */
public class LogUtils {

  @SuppressWarnings("unchecked")
  public static String makeTag(Class cls) {
    return "Pigeon_" + cls.getSimpleName();
  }

}
