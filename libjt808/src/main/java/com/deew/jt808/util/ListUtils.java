package com.deew.jt808.util;

import java.util.LinkedList;
import java.util.List;

/**
 * A collection of utility methods for List objects.
 *
 */
public class ListUtils {

  /**
   * Converts a list of wrappers to an array of primitives.
   * <p>
   * Any unspecified element will be ignored.
   *
   * @param wrappers a Byte list, may be {@code null}
   * @return a byte array, {@link ArrayUtils#EMPTY_BYTE_ARRAY} if null or empty list input
   */
  public static byte[] toPrimitives(List<Byte> wrappers) {
    if (wrappers == null || wrappers.size() <= 0) {
      return ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    byte[] primitives = new byte[wrappers.size()];

    int i = 0;
    for (Byte b : wrappers) {
      if (b == null) {
        continue;
      }
      primitives[i++] = b;
    }

    return primitives;
  }

  @SuppressWarnings("unchecked")
  public static List<Byte> addAll(List<Byte> dst, byte[] src) {
    if (ArrayUtils.isEmpty(src)) {
      return dst;
    }
    if (dst == null) {
      dst = new LinkedList<>();
    }

    for (byte b : src) {
      dst.add(b);
    }

    return dst;
  }

}
