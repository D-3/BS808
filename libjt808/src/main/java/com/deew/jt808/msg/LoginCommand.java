package com.deew.jt808.msg;

import android.util.Log;

import com.deew.jt808.util.LogUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * A login command message.
 *
 */
public class LoginCommand extends Message {

  private static final String TAG = LogUtils.makeTag(LoginCommand.class);

  public static final short ID = (short) 0x9002;

  public static final byte RESULT_OK                  = 0;
  public static final byte RESULT_AUTH_CODE_NOT_FOUND = 1;
  public static final byte RESULT_AUTH_CODE_NOT_MATCH = 2;
  public static final byte RESULT_WRONG_PROTOCOL_VER  = 3;
  public static final byte RESULT_WRONG_PLATE         = 4;
  public static final byte RESULT_WRONG_SCHOOL        = 5;
  public static final byte RESULT_WRONG_SVR_ADDR      = 6;
  public static final byte RESULT_UNEXPECTED_CLT_KEY  = 7;
  public static final byte RESULT_WRONG_HARDWARE      = 8;
  public static final byte RESULT_WRONG_CLT_CHK       = 12;
  public static final byte RESULT_TEST                = (byte) 254;
  public static final byte RESULT_OTHER_ERROR         = (byte) 255;

  private final byte   mResult;
  private final String mSvrTime;
  private final byte[] mEncryptedSvrChk;

  private LoginCommand(Builder builder) {
    super(ID, builder.cipher, builder.phone, builder.body);

    mResult = builder.result;
    mSvrTime = builder.svrTime;
    mEncryptedSvrChk = builder.encryptedSvrChk;
  }

  public byte getResult() {
    return mResult;
  }

  public String getSvrTime() {
    return mSvrTime;
  }

  public byte[] getEncryptedSvrChk() {
    return mEncryptedSvrChk;
  }

  @Override
  public String toString() {
    return new StringBuilder("{ id=9002")
        .append(", result=").append(mResult)
        .append(", sTime=").append(mSvrTime)
        .append(", sChk=").append(mEncryptedSvrChk)
        .append(" }").toString();
  }

  public static class Builder extends MessageBuilder {

    // Required parameters
    private final byte   result;
    private final byte[] encryptedSvrChk;

    // Optional parameters - initialized to default values
    private String svrTime = "19700101000000";

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
      if (this.body.length < 18) {
        throw new IllegalArgumentException("Message body incomplete.");
      }

      switch (this.body[0]) {
        case RESULT_OK:
        case RESULT_AUTH_CODE_NOT_FOUND:
        case RESULT_AUTH_CODE_NOT_MATCH:
        case RESULT_WRONG_PROTOCOL_VER:
        case RESULT_WRONG_PLATE:
        case RESULT_WRONG_SCHOOL:
        case RESULT_WRONG_SVR_ADDR:
        case RESULT_UNEXPECTED_CLT_KEY:
        case RESULT_WRONG_HARDWARE:
        case RESULT_WRONG_CLT_CHK:
        case RESULT_TEST:
        case RESULT_OTHER_ERROR:
          this.result = this.body[0];
          break;
        default:
          throw new IllegalArgumentException("Unknown result.");
      }

      try {
        this.svrTime = new String(Arrays.copyOfRange(this.body, 1, 17), "ascii");
      } catch (UnsupportedEncodingException uee) {
        Log.e(TAG, "Builder: Parse server time failed.", uee);
      }

      this.encryptedSvrChk = Arrays.copyOfRange(this.body, 17, this.body.length);
    }

    @Override
    public LoginCommand build() {
      return new LoginCommand(this);
    }

  }

}
