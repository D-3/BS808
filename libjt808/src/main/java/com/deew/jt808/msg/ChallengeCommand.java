package com.deew.jt808.msg;

import com.deew.jt808.util.IntegerUtils;
import com.deew.jt808.util.LogUtils;

import java.util.Arrays;

/**
 * A challenge command message.
 *
 */
public class ChallengeCommand extends Message {

  private static final String TAG = LogUtils.makeTag(ChallengeCommand.class);

  public static final short ID = (short) 0x9001;

  public static final byte ALGORITHM_AES128 = 0;

  private final byte   mAlgorithm;
  private final short  mSvrKeyIndex;
  private final short  mCltKeyIndex;
  private final byte[] mEncryptedRdmA;

  private ChallengeCommand(Builder builder) {
    super(ID, builder.cipher, builder.phone, builder.body);

    mAlgorithm = builder.algorithm;
    mSvrKeyIndex = builder.svrKeyIndex;
    mCltKeyIndex = builder.cltKeyIndex;
    mEncryptedRdmA = builder.encryptedRdmA;
  }

  public byte getAlgorithm() {
    return mAlgorithm;
  }

  public short getSvrKeyIndex() {
    return mSvrKeyIndex;
  }

  public short getCltKeyIndex() {
    return mCltKeyIndex;
  }

  public byte[] getEncryptedRdmA() {
    return mEncryptedRdmA;
  }

  @Override
  public String toString() {
    return new StringBuilder("{ id=9001")
        .append(", alg=").append(mAlgorithm)
        .append(", sKey=").append(mSvrKeyIndex)
        .append(", cKey=").append(mCltKeyIndex)
        .append(", rdmA=").append(mEncryptedRdmA)
        .append(" }").toString();
  }

  public static class Builder extends MessageBuilder {

    // Required parameters
    private final byte   algorithm;
    private final short  svrKeyIndex;
    private final short  cltKeyIndex;
    private final byte[] encryptedRdmA;

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

      switch (this.body[0]) {
        case ALGORITHM_AES128:
          if (this.body.length != 22) {
            throw new IllegalArgumentException("Message body incorrect.");
          }
          this.algorithm = this.body[0];
          break;
        default:
          throw new IllegalArgumentException("No such algorithm.");
      }

      this.svrKeyIndex = IntegerUtils.parseShort(Arrays.copyOfRange(this.body, 1, 3));
      if (this.svrKeyIndex < 0) {
        throw new IllegalArgumentException("Illegal server key index.");
      }

      this.cltKeyIndex = IntegerUtils.parseShort(Arrays.copyOfRange(this.body, 3, 5));
      if (this.cltKeyIndex < 0) {
        throw new IllegalArgumentException("Illegal client key index.");
      }

      this.encryptedRdmA = Arrays.copyOfRange(this.body, 5, this.body.length);
    }

    @Override
    public ChallengeCommand build() {
      return new ChallengeCommand(this);
    }

  }

}
