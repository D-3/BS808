package ml.that.pigeon.msg;

import android.util.Log;

import ml.that.pigeon.util.ArrayUtils;
import ml.that.pigeon.util.IntegerUtils;
import ml.that.pigeon.util.LogUtils;

/**
 * A login response message.
 *
 * @author That Mr.L (thatmr.l@gmail.com)
 */
public class LoginResponse extends Message {

  private static final String TAG = LogUtils.makeTag(LoginResponse.class);

  public static final short ID = 0x1002;

  public static final byte RESULT_OK            = 0;
  public static final byte RESULT_WRONG_SVR_CHK = 9;

  public static final short STATUS_LOGIN_NOTIFY_OFF = 0;
  public static final short STATUS_LOGIN_NOTIFY_ON  = 1;

  private static final byte[] EMPTY_CFG_TIME = new byte[6];
  private static final byte[] RESERVED_FIELD = new byte[16];

  private final byte   mResult;
  private final byte[] mLastCfgTime;
  private final short  mCltStatus;

  private LoginResponse(Builder builder) {
    super(ID, builder.cipher, builder.phone, builder.body);

    mResult = builder.result;
    mLastCfgTime = builder.lastCfgTime;
    mCltStatus = builder.cltStatus;
  }

  @Override
  public String toString() {
    return new StringBuilder("{ id=1002")
        .append(", result=").append(mResult)
        .append(", cfgTime=").append(mLastCfgTime)
        .append(", cStat=").append(mCltStatus)
        .append(" }").toString();
  }

  public static class Builder extends MessageBuilder {

    // Optional parameters - initialized to default values
    private byte   result      = RESULT_OK;
    private byte[] lastCfgTime = EMPTY_CFG_TIME;
    private short  cltStatus   = STATUS_LOGIN_NOTIFY_OFF;

    public Builder result(byte result) {
      switch (result) {
        case RESULT_OK:
        case RESULT_WRONG_SVR_CHK:
          this.result = result;
          break;
        default:
          Log.w(TAG, "result: Unknown result, use default.");
      }

      return this;
    }

    public Builder lastCfgTime(byte[] time) {
      if (time != null && time.length == 6) {
        this.lastCfgTime = time;
      } else {
        Log.w(TAG, "lastCfgTime: Illeage configure time, use default.");
      }

      return this;
    }

    public Builder cltStatus(short status) {
      switch (status) {
        case STATUS_LOGIN_NOTIFY_OFF:
        case STATUS_LOGIN_NOTIFY_ON:
          this.cltStatus = status;
          break;
        default:
          Log.w(TAG, "cltStatus: Unknown client status, use default.");
      }

      return this;
    }

    @Override
    public LoginResponse build() {
      this.body = ArrayUtils.concatenate(IntegerUtils.asBytes(this.result),
                                         this.lastCfgTime,
                                         IntegerUtils.asBytes(this.cltStatus),
                                         RESERVED_FIELD);

      return new LoginResponse(this);
    }

  }

}
