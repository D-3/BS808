package ml.that.jt808;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import com.deew.jt808.util.ListUtils;

import static org.junit.Assert.assertArrayEquals;

/**
 * Local unit test for {@link ListUtils}, will execute on the development machine (host).
 *
 * @author That Mr.L (thatmr.l@gmail.com)
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ListUtilsTest {

  @Test
  public void testToPrimitives() {
    List<Byte> sample = new LinkedList<>();
    sample.add((byte) 0x30);
    sample.add((byte) 0x7e);
    sample.add((byte) 0x08);
    sample.add((byte) 0x7d);
    sample.add((byte) 0x55);

    assertArrayEquals(new byte[]{ 0x30, 0x7e, 0x08, 0x7d, 0x55 }, ListUtils.toPrimitives(sample));
  }

}
