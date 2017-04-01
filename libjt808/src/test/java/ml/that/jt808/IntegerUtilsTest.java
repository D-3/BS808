package ml.that.jt808;

import org.junit.Test;

import com.deew.jt808.util.IntegerUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 * Local unit test for {@link IntegerUtils}, will execute on the development machine (host).
 *
 * @author That Mr.L (thatmr.l@gmail.com)
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class IntegerUtilsTest {

  @Test
  public void testByteAsBytes() {
    assertArrayEquals(new byte[]{ 0x30 }, IntegerUtils.asBytes((byte) 0x30));
  }

  @Test
  public void testShortAsBytes() {
    assertArrayEquals(new byte[]{ 0x30, 0x7e }, IntegerUtils.asBytes((short) 0x307e));
  }

  @Test
  public void testIntAsBytes() {
    assertArrayEquals(new byte[]{ 0x30, 0x7e, 0x08, 0x7d }, IntegerUtils.asBytes(0x307e087d));
  }

  @Test
  public void testLongAsBytes() {
    assertArrayEquals(new byte[]{ 0x00, 0x00, 0x00, 0x30, 0x7e, 0x08, 0x7d, 0x55 },
                      IntegerUtils.asBytes(0x307e087d55L));
  }

  @Test
  public void testParseByte() {
    assertEquals(0x55, IntegerUtils.parseByte(new byte[]{ 0x30, 0x7e, 0x08, 0x7d, 0x55 }));
  }

  @Test
  public void testParseShort() {
    assertEquals(0x7d55, IntegerUtils.parseShort(new byte[]{ 0x30, 0x7e, 0x08, 0x7d, 0x55 }));
  }

  @Test
  public void testParseInt() {
    assertEquals(0x7e087d55, IntegerUtils.parseInt(new byte[]{ 0x30, 0x7e, 0x08, 0x7d, 0x55 }));
  }

  @Test
  public void testParseLong() {
    assertEquals(0x307e087d55L, IntegerUtils.parseLong(new byte[]{ 0x30, 0x7e, 0x08, 0x7d, 0x55 }));
  }

}
