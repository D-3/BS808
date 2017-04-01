package ml.that.pigeon.msg;

import ml.that.pigeon.util.LogUtils;

/**
 * A heart beat request message.
 *
 * @author That Mr.L (thatmr.l@gmail.com)
 */
public class HeartbeatMessage extends Message {

  private static final String TAG = LogUtils.makeTag(HeartbeatMessage.class);

  public static final short ID = 0x0002;

  private HeartbeatMessage(Builder builder) {
    super(ID, builder.cipher, builder.phone, builder.body);
  }

  public static class Builder extends MessageBuilder {

    @Override
    public HeartbeatMessage build() {
      this.body = EMPTY_BODY;
      return new HeartbeatMessage(this);
    }

  }

}
