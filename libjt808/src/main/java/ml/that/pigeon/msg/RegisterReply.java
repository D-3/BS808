package ml.that.pigeon.msg;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import ml.that.pigeon.util.IntegerUtils;
import ml.that.pigeon.util.LogUtils;

public class RegisterReply extends Message {

  private static final String TAG = LogUtils.makeTag(RegisterReply.class);

  public static final short ID = (short) 0x8100;

  public static final byte RESULT_OK             = 0;
  public static final byte RESULT_VEH_REGISTERED = 1;
  public static final byte RESULT_VEH_NOT_FOUND  = 2;
  public static final byte RESULT_CLT_REGISTERED = 3;
  public static final byte RESULT_CLT_NOT_FOUND  = 4;

  private final short  mReqSn;
  private final byte   mResult;
  private final String mAuthCode;

  private RegisterReply(Builder builder) {
    super(ID, builder.cipher, builder.phone, builder.body);

    mReqSn = builder.reqSn;
    mResult = builder.result;
    mAuthCode = builder.authCode;
  }

  public short getReqSn() {
    return mReqSn;
  }

  public byte getResult() {
    return mResult;
  }

  public String getAuthCode() {
    return mAuthCode;
  }

  @Override
  public String toString() {
    return new StringBuilder("{ id=8100")
        .append(", reqSn=").append(mReqSn)
        .append(", result=").append(mResult)
        .append(", auth=").append(mAuthCode)
        .append(" }").toString();
  }

  public static class Builder extends MessageBuilder {

    // Required parameters
    private final short reqSn;
    private final byte  result;

    // Optional parameters - initialized to default values
    private String authCode = null;

    public Builder(Message msg) {
      if (msg == null) {
        throw new NullPointerException("Message is null.");
      }
      if (ID != msg.getId()) {
        throw new IllegalArgumentException("Wrong message ID.");
      }
      if (msg.getBody().length < 3) {
        throw new IllegalArgumentException("Message body incomplete.");
      }

      this.cipher = msg.getCipher();
      this.phone = msg.getPhone();
      this.body = msg.getBody();

      this.reqSn = IntegerUtils.parseShort(Arrays.copyOf(this.body, 2));

      switch (this.body[2]) {
        case RESULT_OK:
          if (this.body.length < 4) {
            throw new IllegalArgumentException("Authentication code missing.");
          }
          try {
            this.authCode = new String(Arrays.copyOfRange(this.body, 3, this.body.length), "ascii");
          } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "Builder: Encode authentication code failed.", uee);
          }
        case RESULT_VEH_REGISTERED:
        case RESULT_VEH_NOT_FOUND:
        case RESULT_CLT_REGISTERED:
        case RESULT_CLT_NOT_FOUND:
          this.result = this.body[2];
          break;
        default:
          throw new IllegalArgumentException("Unknown result.");
      }
    }

    @Override
    public RegisterReply build() {
      return new RegisterReply(this);
    }

  }

}
