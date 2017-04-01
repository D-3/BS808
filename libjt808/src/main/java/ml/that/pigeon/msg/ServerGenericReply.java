package ml.that.pigeon.msg;

import java.util.Arrays;

import ml.that.pigeon.util.IntegerUtils;
import ml.that.pigeon.util.LogUtils;

public class ServerGenericReply extends Message {

  private static final String TAG = LogUtils.makeTag(ServerGenericReply.class);

  public static final short ID = (short) 0x8001;

  public static final byte RESULT_OK          = 0;
  public static final byte RESULT_FAIL        = 1;
  public static final byte RESULT_BAD_REQUEST = 2;
  public static final byte RESULT_UNSUPPORTED = 3;
  public static final byte RESULT_CONFIRM     = 4;

  private final short mReqSn;
  private final short mReqId;
  private final byte  mResult;

  private ServerGenericReply(Builder builder) {
    super(ID, builder.cipher, builder.phone, builder.body);

    mReqSn = builder.reqSn;
    mReqId = builder.reqId;
    mResult = builder.result;
  }

  public short getReqSn() {
    return mReqSn;
  }

  public short getReqId() {
    return mReqId;
  }

  public byte getResult() {
    return mResult;
  }

  @Override
  public String toString() {
    return new StringBuilder("{ id=8001")
        .append(", reqSn=").append(mReqSn)
        .append(", reqId=").append(mReqId)
        .append(", result=").append(mResult)
        .append(" }").toString();
  }

  public static class Builder extends MessageBuilder {

    // Required parameters
    private final short reqSn;
    private final short reqId;
    private final byte  result;

    public Builder(Message msg) {
      if (msg == null) {
        throw new NullPointerException("Message is null.");
      }
      if (ID != msg.getId()) {
        throw new IllegalArgumentException("Wrong message ID.");
      }

      this.cipher = msg.getCipher();
      this.phone = msg.getPhone();
      this.body = msg.getBody();
      if (this.body.length != 5) {
        throw new IllegalArgumentException("Message body incorrect.");
      }

      this.reqSn = IntegerUtils.parseShort(Arrays.copyOfRange(this.body, 0, 2));
      this.reqId = IntegerUtils.parseShort(Arrays.copyOfRange(this.body, 2, 4));

      switch (this.body[4]) {
        case RESULT_OK:
        case RESULT_FAIL:
        case RESULT_BAD_REQUEST:
        case RESULT_UNSUPPORTED:
        case RESULT_CONFIRM:
          this.result = this.body[4];
          break;
        default:
          throw new IllegalArgumentException("Unknown result.");
      }
    }

    @Override
    public ServerGenericReply build() {
      return new ServerGenericReply(this);
    }

  }

}
