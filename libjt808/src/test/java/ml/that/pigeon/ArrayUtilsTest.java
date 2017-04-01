package ml.that.pigeon;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import ml.that.pigeon.util.ArrayUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 * Local unit test for {@link ArrayUtils}, will execute on the development machine (host).
 *
 * @author That Mr.L (thatmr.l@gmail.com)
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ArrayUtilsTest {

  @Test
  public void testBytesIsEmpty() {
    assertEquals(true, ArrayUtils.isEmpty((byte[]) null));
    assertEquals(true, ArrayUtils.isEmpty(ArrayUtils.EMPTY_BYTE_ARRAY));
  }

  @Test
  public void testShortsIsEmpty() {
    assertEquals(true, ArrayUtils.isEmpty((short[]) null));
    assertEquals(true, ArrayUtils.isEmpty(ArrayUtils.EMPTY_SHORT_ARRAY));
  }

  @Test
  public void testIntsIsEmpty() {
    assertEquals(true, ArrayUtils.isEmpty((int[]) null));
    assertEquals(true, ArrayUtils.isEmpty(ArrayUtils.EMPTY_INT_ARRAY));
  }

  @Test
  public void testLongsIsEmpty() {
    assertEquals(true, ArrayUtils.isEmpty((long[]) null));
    assertEquals(true, ArrayUtils.isEmpty(ArrayUtils.EMPTY_LONG_ARRAY));
  }

  @Test
  public void testObjectsIsEmpty() {
    assertEquals(true, ArrayUtils.isEmpty((Object[]) null));
    assertEquals(true, ArrayUtils.isEmpty(new Object[0]));
  }

  @Test
  public void testConcatenateArrays() {
    assertArrayEquals(new byte[]{ 0x30, 0x7e, 0x08, 0x7d, 0x55 },
                      ArrayUtils.concatenate(new byte[]{ 0x30, 0x7e },
                                             new byte[]{ 0x08 },
                                             new byte[]{ 0x7d, 0x55 }));
  }

  @Test
  public void testConcatenateList() {
    List<byte[]> sample = new LinkedList<>();
    sample.add(new byte[]{ 0x30, 0x7e });
    sample.add(new byte[]{ 0x08 });
    sample.add(new byte[]{ 0x7d, 0x55 });

    assertArrayEquals(new byte[]{ 0x30, 0x7e, 0x08, 0x7d, 0x55 }, ArrayUtils.concatenate(sample));
  }

  @Test
  public void testDivide() {
    List<byte[]> expected = new LinkedList<>();
    expected.add(new byte[]{ 0, 1, 2 });
    expected.add(new byte[]{ 3, 4, 5 });
    expected.add(new byte[]{ 6, 7, 8 });
    expected.add(new byte[]{ 9 });
    List<byte[]> actual = ArrayUtils.divide(new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, 3);

    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertArrayEquals(expected.get(i), actual.get(i));
    }
  }

  @Test
  public void testXorCheck() {
    assertEquals(0x6e, ArrayUtils.xorCheck(new byte[]{ 0x30, 0x7e, 0x08, 0x7d, 0x55 }));
  }

  @Test
  public void testEscape() {
    assertArrayEquals(new byte[]{ 0x30, 0x7d, 0x02, 0x08, 0x7d, 0x01, 0x55 },
                      ArrayUtils.escape(new byte[]{ 0x30, 0x7e, 0x08, 0x7d, 0x55 }));
  }

  @Test
  public void testUnescape() {
    assertArrayEquals(new byte[]{ 0x30, 0x7e, 0x08, 0x7d, 0x55 },
                      ArrayUtils.unescape(new byte[]{ 0x30, 0x7d, 0x02, 0x08, 0x7d, 0x01, 0x55 }));
  }

  @Test
  public void testShortsToBytes() {
    assertArrayEquals(new byte[]{ 0x30, 0x7e, 0x08, 0x7d },
                      ArrayUtils.toBytes(new short[]{ 0x307e, 0x087d }));
  }

  @Test
  public void testIntsToBytes() {
    assertArrayEquals(new byte[]{ 0x30, 0x7e, 0x08, 0x7d },
                      ArrayUtils.toBytes(new int[]{ 0x307e087d }));
  }

  @Test
  public void testLongsToBytes() {
    assertArrayEquals(new byte[]{ 0x00, 0x00, 0x00, 0x30, 0x7e, 0x08, 0x7d, 0x55 },
                      ArrayUtils.toBytes(new long[]{ 0x307e087d55L }));
  }

  @Test
  public void testToShorts() {
    assertArrayEquals(new short[]{ 0x307e, 0x087d },
                      ArrayUtils.toShorts(new byte[]{ 0x30, 0x7e, 0x08, 0x7d, 0x55 }));
  }

  @Test
  public void testToInts() {
    assertArrayEquals(new int[]{ 0x307e087d },
                      ArrayUtils.toInts(new byte[]{ 0x30, 0x7e, 0x08, 0x7d, 0x55 }));
  }

  @Test
  public void testToLongs() {
    assertArrayEquals(new long[]{ 0x307e087d55L },
                      ArrayUtils.toLongs(new byte[]{ 0x00, 0x00, 0x00, 0x30,
                                                     0x7e, 0x08, 0x7d, 0x55 }));
  }

}
