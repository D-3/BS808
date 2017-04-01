package ml.that.pigeon.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Operations on arrays, primitive arrays (like {@code byte[]} and primitive wrapper arrays (like
 * {@code Byte[]}).
 * <p>
 * This class tries to handle {@code null} input gracefully. An exception will not be thrown for a
 * {@code null}  array input. Each method documents its behavior.
 *
 * @author That Mr.L (thatmr.l@gmail.com)
 */
public class ArrayUtils {

  /** An empty immutable byte array. */
  public static final byte[]  EMPTY_BYTE_ARRAY  = new byte[0];
  /** An empty immutable short array. */
  public static final short[] EMPTY_SHORT_ARRAY = new short[0];
  /** An empty immutable int array. */
  public static final int[]   EMPTY_INT_ARRAY   = new int[0];
  /** An empty immutable long array. */
  public static final long[]  EMPTY_LONG_ARRAY  = new long[0];

  /**
   * Checks if an array of primitive bytes is null or empty.
   *
   * @param arr the array to test
   * @return {@code true} if the array is null or empty
   */
  public static boolean isEmpty(byte[] arr) {
    return arr == null || arr.length <= 0;
  }

  /**
   * Checks if an array of primitive shorts is null or empty.
   *
   * @param arr the array to test
   * @return {@code true} if the array is null or empty
   */
  public static boolean isEmpty(short[] arr) {
    return arr == null || arr.length <= 0;
  }

  /**
   * Checks if an array of primitive ints is null or empty.
   *
   * @param arr the array to test
   * @return {@code true} if the array is null or empty
   */
  public static boolean isEmpty(int[] arr) {
    return arr == null || arr.length <= 0;
  }

  /**
   * Checks if an array of primitive longs is null or empty.
   *
   * @param arr the array to test
   * @return {@code true} if the array is null or empty
   */
  public static boolean isEmpty(long[] arr) {
    return arr == null || arr.length <= 0;
  }

  /**
   * Checks if an array of Objects is null or empty.
   *
   * @param arr the array to test
   * @return {@code true} if the array is null or empty
   */
  public static <T> boolean isEmpty(T[] arr) {
    return arr == null || arr.length <= 0;
  }

  /**
   * Checks if any element of an array of Object is specified.
   *
   * @param arr the array to test
   * @return {@code true} if all elements of the array is specified
   */
  @SuppressWarnings("unchecked")
  public static <T> boolean isFull(T[] arr) {
    if (arr == null) {
      return false;
    }

    for (T t : arr) {
      if (t == null) {
        return false;
      }
    }

    return true;
  }

  public static byte[] concatenate(byte[]... parts) {
    List<Byte> result = new LinkedList<>();

    for (byte[] part : parts) {
      if (isEmpty(part)) {
        continue;
      }
      for (byte b : part) {
        result.add(b);
      }
    }

    return ListUtils.toPrimitives(result);
  }

  public static byte[] concatenate(List<byte[]> parts) {
    List<Byte> result = new LinkedList<>();

    for (byte[] part : parts) {
      if (isEmpty(part)) {
        continue;
      }
      for (byte b : part) {
        result.add(b);
      }
    }

    return ListUtils.toPrimitives(result);
  }

  public static List<byte[]> divide(byte[] entire, int len) {
    List<byte[]> result = new LinkedList<>();

    if (isEmpty(entire)) {
      result.add(EMPTY_BYTE_ARRAY);
      return result;
    }
    if (len <= 0 || len >= entire.length) {
      result.add(entire);
      return result;
    }

    int head = 0;
    while (head + len < entire.length) {
      result.add(Arrays.copyOfRange(entire, head, head += len));
    }
    result.add(Arrays.copyOfRange(entire, head, entire.length));

    return result;
  }

  public static byte xorCheck(byte[] data) {
    if (isEmpty(data)) {
      return 0;
    }

    byte checksum = 0;

    for (byte b : data) {
      checksum ^= b;
    }

    return checksum;
  }

  /**
   * Returns a byte array representation of the XOR result of each pair of bytes in the given
   * arrays. The higher bytes will be aligned.
   *
   * @param fir one of the arrays
   * @param sec the other of the arrays
   * @return a byte array representation of the XOR result
   */
  @SuppressWarnings("unchecked")
  public static byte[] leftXor(byte[] fir, byte[] sec) {
    if (fir == null) {
      return sec;
    }
    if (sec == null) {
      return fir;
    }

    if (fir.length >= sec.length) {
      byte[] result = Arrays.copyOf(fir, fir.length);
      for (int i = 0; i < sec.length; i++) {
        result[i] = (byte) (fir[i] ^ sec[i]);
      }
      return result;
    } else {
      return leftXor(sec, fir);
    }
  }

  /**
   * Returns a byte array representation of the XOR result of each pair of bytes in the given
   * arrays. The lower bytes will be aligned.
   *
   * @param fir one of the arrays
   * @param sec the other of the arrays
   * @return a byte array representation of the XOR result
   */
  @SuppressWarnings("unchecked")
  public static byte[] rightXor(byte[] fir, byte[] sec) {
    if (fir == null) {
      return sec;
    }
    if (sec == null) {
      return fir;
    }

    if (fir.length >= sec.length) {
      byte[] result = Arrays.copyOf(fir, fir.length);
      int head = fir.length - sec.length;
      for (int i = 0; i < sec.length; i++) {
        result[head + i] = (byte) (fir[head + i] ^ sec[i]);
      }
      return result;
    } else {
      return rightXor(sec, fir);
    }
  }

  /**
   * Escapes the bytes in a byte array using JT/T808 rules.
   * <p>
   * Escapes any value it finds into their JT/T808 form.
   * <p>
   * So a byte {@code 0x7d} becomes a sequence of {@code 0x7d} and {@code 0x07}.
   * <p>
   * Example:
   * <pre>
   *   input array: 0x30, 0x7e, 0x08, 0x7d, 0x55
   *   output array: 0x30, 0x7d, 0x02, 0x08, 0x7d, 0x01, 0x55
   * </pre>
   *
   * @param in the byte array to escape values in, may be {@code null}
   * @return an array with escaped values, {@link #EMPTY_BYTE_ARRAY} if null or empty array input
   */
  public static byte[] escape(byte[] in) {
    if (isEmpty(in)) {
      return EMPTY_BYTE_ARRAY;
    }

    List<Byte> out = new LinkedList<>();

    for (byte b : in) {
      switch (b) {
        case 0x7d:
          out.add((byte) 0x7d);
          out.add((byte) 0x01);
          break;
        case 0x7e:
          out.add((byte) 0x7d);
          out.add((byte) 0x02);
          break;
        default:
          out.add(b);
      }
    }

    return ListUtils.toPrimitives(out);
  }

  /**
   * Unescape any JT/T808 pattern found in the byte array.
   * <p>
   * For example, it will turn a sequence of {@code 0x7d} and {@code 0x0e} into a byte {@code
   * 0x7e}.
   *
   * @param in the byte array to unescape, may be {@code null}
   * @return a new unescaped byte array, {@link #EMPTY_BYTE_ARRAY} if null or empty array input
   */
  public static byte[] unescape(byte[] in) {
    if (isEmpty(in)) {
      return EMPTY_BYTE_ARRAY;
    }

    List<Byte> out = new LinkedList<>();

    for (int i = 0; i < in.length; i++) {
      if (in[i] != 0x7d) {
        out.add(in[i]);
        continue;
      }
      if (in[++i] == 0x01) {
        out.add((byte) 0x7d);
      } else {
        out.add((byte) 0x7e);
      }
    }

    return ListUtils.toPrimitives(out);
  }

  /**
   * Converts an array of primitive shorts to an array of primitive bytes.
   *
   * @param arr a short array, may be {@code null}
   * @return a byte array, {@link #EMPTY_BYTE_ARRAY} if null or empty array input
   */
  public static byte[] toBytes(short[] arr) {
    if (isEmpty(arr)) {
      return EMPTY_BYTE_ARRAY;
    }

    byte[] result = new byte[arr.length * 2];

    byte[] digit;
    for (int i = 0; i < arr.length; i++) {
      digit = IntegerUtils.asBytes(arr[i]);
      result[i * 2] = digit[0];
      result[i * 2 + 1] = digit[1];
    }

    return result;
  }

  /**
   * Converts an array of primitive ints to an array of primitive bytes.
   *
   * @param arr a int array, may be {@code null}
   * @return a byte array, {@link #EMPTY_BYTE_ARRAY} if null or empty array input
   */
  public static byte[] toBytes(int[] arr) {
    if (isEmpty(arr)) {
      return EMPTY_BYTE_ARRAY;
    }

    byte[] result = new byte[arr.length * 4];

    byte[] digit;
    for (int i = 0; i < arr.length; i++) {
      digit = IntegerUtils.asBytes(arr[i]);
      result[i * 4] = digit[0];
      result[i * 4 + 1] = digit[1];
      result[i * 4 + 2] = digit[2];
      result[i * 4 + 3] = digit[3];
    }

    return result;
  }

  /**
   * Converts an array of primitive longs to an array of primitive bytes.
   *
   * @param arr a long array, may be {@code null}
   * @return a byte array, {@link #EMPTY_BYTE_ARRAY} if null or empty array input
   */
  public static byte[] toBytes(long[] arr) {
    if (isEmpty(arr)) {
      return EMPTY_BYTE_ARRAY;
    }

    byte[] result = new byte[arr.length * 8];

    byte[] digit;
    for (int i = 0; i < arr.length; i++) {
      digit = IntegerUtils.asBytes(arr[i]);
      result[i * 8] = digit[0];
      result[i * 8 + 1] = digit[1];
      result[i * 8 + 2] = digit[2];
      result[i * 8 + 3] = digit[3];
      result[i * 8 + 4] = digit[4];
      result[i * 8 + 5] = digit[5];
      result[i * 8 + 6] = digit[6];
      result[i * 8 + 7] = digit[7];
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  public static byte[] ensureLength(byte[] arr, int len) {
    if (isEmpty(arr)) {
      return new byte[len];
    }
    if (arr.length < len) {
      return concatenate(new byte[len - arr.length], arr);
    }
    if (arr.length > len) {
      return Arrays.copyOf(arr, len);
    }

    return arr;
  }

  @SuppressWarnings("unchecked")
  public static byte[] ensureMinLength(byte[] arr, int min) {
    if (isEmpty(arr)) {
      return new byte[min];
    }
    if (arr.length >= min) {
      return arr;
    }

    return concatenate(new byte[min - arr.length], arr);
  }

  @SuppressWarnings("unchecked")
  public static byte[] ensureMaxLength(byte[] arr, int max) {
    if (isEmpty(arr)) {
      return EMPTY_BYTE_ARRAY;
    }
    if (arr.length <= max) {
      return arr;
    }

    return Arrays.copyOf(arr, max);
  }

  /**
   * Converts an array of primitive bytes to an array of primitive shorts.
   *
   * @param bytes a byte array, may be {@code null}
   * @return a short array, {@link #EMPTY_SHORT_ARRAY} if null or empty array input
   */
  public static short[] toShorts(byte[] bytes) {
    if (isEmpty(bytes)) {
      return EMPTY_SHORT_ARRAY;
    }

    short[] result = new short[bytes.length / 2];

    for (int i = 0; i < result.length; i++) {
      result[i] = IntegerUtils.parseShort(Arrays.copyOfRange(bytes, i * 2, i * 2 + 2));
    }

    return result;
  }

  /**
   * Converts an array of primitive bytes to an array of primitive ints.
   *
   * @param bytes a byte array, may be {@code null}
   * @return an int array, {@link #EMPTY_INT_ARRAY} if null or empty array input
   */
  public static int[] toInts(byte[] bytes) {
    if (isEmpty(bytes)) {
      return EMPTY_INT_ARRAY;
    }

    int[] result = new int[bytes.length / 4];

    for (int i = 0; i < result.length; i++) {
      result[i] = IntegerUtils.parseInt(Arrays.copyOfRange(bytes, i * 4, i * 4 + 4));
    }

    return result;
  }

  /**
   * Converts an array of primitive bytes to an array of primitive longs.
   *
   * @param bytes a byte array, may be {@code null}
   * @return a long array, {@link #EMPTY_LONG_ARRAY} if null or empty array input
   */
  public static long[] toLongs(byte[] bytes) {
    if (isEmpty(bytes)) {
      return EMPTY_LONG_ARRAY;
    }

    long[] result = new long[bytes.length / 8];

    for (int i = 0; i < result.length; i++) {
      result[i] = IntegerUtils.parseLong(Arrays.copyOfRange(bytes, i * 8, i * 8 + 8));
    }

    return result;
  }

  public static byte[] subarray(byte[] src, int start, int end) {
    if (start > end) {
      throw new IllegalArgumentException("Start index is greater than end index.");
    }
    if (isEmpty(src) || start >= src.length) {
      return EMPTY_BYTE_ARRAY;
    }

    end = Math.min(end, src.length);
    byte[] result = new byte[end - start];

    System.arraycopy(src, start, result, 0, result.length);

    return result;
  }

  @SuppressWarnings("unchecked")
  public static int[] toUnsigned(byte[] bytes) {
    if (isEmpty(bytes)) {
      return EMPTY_INT_ARRAY;
    }

    int[] unsigned = new int[bytes.length];

    for (int i = 0; i < bytes.length; i++) {
      unsigned[i] = (bytes[i] & 0xff);
    }

    return unsigned;
  }

  @SuppressWarnings("unchecked")
  public static String toHexString(byte[] bytes) {
    if (isEmpty(bytes)) {
      return "{ }";
    }

    StringBuilder hex = new StringBuilder('{');

    for (byte b : bytes) {
      hex.append(" " + Integer.toHexString(b) + ",");
    }
    hex.setCharAt(hex.length() - 1, ' ');

    return hex.append('}').toString();
  }

}
