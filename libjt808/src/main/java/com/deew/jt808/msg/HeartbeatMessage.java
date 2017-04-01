package com.deew.jt808.msg;

import com.deew.jt808.util.LogUtils;

/**
 * A heart beat request message.
 *
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
