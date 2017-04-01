package com.deew.jt808.msg;

import android.util.Log;

import com.deew.jt808.Jtt415Constants;
import com.deew.jt808.util.ArrayUtils;
import com.deew.jt808.util.CryptoUtils;
import com.deew.jt808.util.IntegerUtils;
import com.deew.jt808.util.LogUtils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * A challenge response message.
 *
 */
public class ChallengeResponse extends Message {

  private static final String TAG = LogUtils.makeTag(ChallengeResponse.class);

  public static final short ID = 0x1001;

  private static final byte[] EMPTY_MFRS_ID    = new byte[5];
  private static final byte[] EMPTY_CLT_ID     = new byte[7];
  private static final String EMPTY_PLATE_TEXT = "";
  private static final byte[] EMPTY_SCHOOL_NO  = new byte[6];
  private static final byte[] RESERVED_FIELD   = new byte[16];

  private final byte[] mMfrsId;
  private final byte[] mCltId;
  private final short  mHardwareVer;
  private final short  mSoftwareVer;
  private final short  mProtocolVer;
  private final byte   mCustomVer;
  private final byte   mPlateColor;
  private final String mPlateText;
  private final byte[] mSchoolNo;
  private final short  mCltKeyIndex;
  private final String mCltKey;
  private final byte[] mRandomA;
  private final byte[] mRandomB;
  private final byte[] mDeviceSn;
  private final byte[] mSvrAddress;

  private ChallengeResponse(Builder builder) {
    super(ID, builder.cipher, builder.phone, builder.body);

    mMfrsId = builder.mfrsId;
    mCltId = builder.cltId;
    mHardwareVer = builder.hardwareVer;
    mSoftwareVer = builder.softwareVer;
    mProtocolVer = builder.protocolVer;
    mCustomVer = builder.customVer;
    mPlateColor = builder.plateColor;
    mPlateText = builder.plateText;
    mSchoolNo = builder.schoolNo;
    mCltKeyIndex = builder.cltKeyIndex;
    mCltKey = builder.cltKey;
    mRandomA = builder.randomA;
    mRandomB = builder.randomB;
    mDeviceSn = builder.deviceSn;
    mSvrAddress = builder.svrAddress;
  }

  @Override
  public String toString() {
    return new StringBuilder("{ id=1001")
        .append(", mfrs=").append(mMfrsId)
        .append(", cltId=").append(mCltId)
        .append(", hwV=").append(mHardwareVer)
        .append(", swV=").append(mSoftwareVer)
        .append(", proV=").append(mProtocolVer)
        .append(", cusV=").append(mCustomVer)
        .append(", pClr=").append(mPlateColor)
        .append(", pTxt=").append(mPlateText)
        .append(", sch=").append(mSchoolNo)
        .append(", cKey=").append(mCltKeyIndex).append("-").append(mCltKey)
        .append(", rdmA=").append(mRandomA)
        .append(", rdmB=").append(mRandomB)
        .append(", dvc=").append(mDeviceSn)
        .append(", addr=").append(mSvrAddress)
        .append(" }").toString();
  }

  public static class Builder extends MessageBuilder {

    // Required parameters
    private final short  cltKeyIndex;
    private final String cltKey;
    private final byte[] randomA;
    private final byte[] randomB;

    // Optional parameters - initialized to default values
    private byte[] mfrsId      = EMPTY_MFRS_ID;
    private byte[] cltId       = EMPTY_CLT_ID;
    private short  hardwareVer = 0;
    private short  softwareVer = 0;
    private short  protocolVer = 0;
    private byte   customVer   = 0;
    private byte   plateColor  = Jtt415Constants.PLATE_COLOR_TEST;
    private String plateText   = EMPTY_PLATE_TEXT;
    private byte[] schoolNo    = EMPTY_SCHOOL_NO;
    private byte[] deviceSn    = RESERVED_FIELD;
    private byte[] svrAddress  = RESERVED_FIELD;

    public Builder(short idx, String key, byte[] rdmA, byte[] rdmB) {
      if (idx < 0) {
        throw new IllegalArgumentException("Client key index is less than 0.");
      }
      if (rdmA == null || rdmA.length != 16) {
        throw new IllegalArgumentException("Random number A incorrect.");
      }
      if (rdmB == null || rdmB.length != 16) {
        throw new IllegalArgumentException("Random number B incorrect.");
      }

      this.cltKeyIndex = idx;
      this.cltKey = key;
      this.randomA = rdmA;
      this.randomB = rdmB;
    }

    public Builder mfrsId(byte[] id) {
      if (id != null && id.length == 5) {
        this.mfrsId = id;
      } else {
        Log.w(TAG, "mfrsId: Illegal manufacturer ID, use default.");
      }

      return this;
    }

    public Builder cltId(byte[] id) {
      if (id != null && id.length == 7) {
        this.cltId = id;
      } else {
        Log.w(TAG, "cltId: Illegal client ID, use default.");
      }

      return this;
    }

    public Builder hardwareVer(short ver) {
      if (ver >= 0 && ver <= 9999) {
        this.hardwareVer = ver;
      } else {
        Log.w(TAG, "hardwareVer: Illegal hardware version, use default.");
      }

      return this;
    }

    public Builder softwareVer(short ver) {
      if (ver >= 0 && ver <= 9999) {
        this.softwareVer = ver;
      } else {
        Log.w(TAG, "softwareVer: Illegal software version, use default.");
      }

      return this;
    }

    public Builder protocolVer(short ver) {
      if (ver >= 0 && ver <= 9999) {
        this.protocolVer = ver;
      } else {
        Log.w(TAG, "protocolVer: Illegal protocol version, use default.");
      }

      return this;
    }

    public Builder customVer(byte ver) {
      if (ver >= 0 && ver <= 99) {
        this.customVer = ver;
      } else {
        Log.w(TAG, "customVer: Illegal custom version, use default.");
      }

      return this;
    }

    public Builder plateColor(byte color) {
      switch (color) {
        case Jtt415Constants.PLATE_COLOR_NONE:
        case Jtt415Constants.PLATE_COLOR_BLUE:
        case Jtt415Constants.PLATE_COLOR_YELLOW:
        case Jtt415Constants.PLATE_COLOR_BLACK:
        case Jtt415Constants.PLATE_COLOR_WHITE:
        case Jtt415Constants.PLATE_COLOR_TEST:
          this.plateColor = color;
          break;
        default:
          Log.w(TAG, "plateColor: Unknown plate color, use default.");
      }

      return this;
    }

    public Builder plateText(String text) {
      if (text != null) {
        this.plateText = text;
      } else {
        Log.w(TAG, "plateText: Plate text is null, use default.");
      }

      return this;
    }

    public Builder schoolNo(byte[] school) {
      if (school != null && school.length == 6) {
        this.schoolNo = school;
      } else {
        Log.w(TAG, "schoolNo: Illegal school number, use default.");
      }

      return this;
    }

    public Builder deviceSn(byte[] sn) {
      if (sn != null && sn.length == 16) {
        this.deviceSn = sn;
      } else {
        Log.w(TAG, "deviceSn: Illegal device SN, use default.");
      }

      return this;
    }

    public Builder svrAddress(byte[] addr) {
      if (addr != null && addr.length == 16) {
        this.svrAddress = addr;
      } else {
        Log.w(TAG, "svrAddress: Illegal server address, use default.");
      }

      return this;
    }

    @Override
    public ChallengeResponse build() {
      try {
        this.body =
            ArrayUtils.concatenate(
                this.mfrsId,
                this.cltId,
                ArrayUtils.ensureLength(IntegerUtils.toBcd(this.hardwareVer), 2),
                ArrayUtils.ensureLength(IntegerUtils.toBcd(this.softwareVer), 2),
                ArrayUtils.ensureLength(IntegerUtils.toBcd(this.protocolVer), 2),
                ArrayUtils.ensureLength(IntegerUtils.toBcd(this.customVer), 1),
                RESERVED_FIELD,
                IntegerUtils.asBytes(this.plateColor),
                ArrayUtils.ensureLength(this.plateText.getBytes("ascii"), 12),
                this.schoolNo,
                RESERVED_FIELD,
                IntegerUtils.asBytes(this.cltKeyIndex),
                CryptoUtils.encrypt(ArrayUtils.leftXor(this.randomB, this.cltId), this.cltKey),
                CryptoUtils.encrypt(ArrayUtils.leftXor(this.randomB, this.randomA), this.cltKey),
                CryptoUtils.encrypt(ArrayUtils.leftXor(this.deviceSn, this.randomA), this.cltKey),
                CryptoUtils.encrypt(ArrayUtils.leftXor(this.svrAddress, this.randomA), this.cltKey)
            );
      } catch (UnsupportedEncodingException uee) {
        Log.e(TAG, "build: Encode message body failed.", uee);
      } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
        Log.e(TAG, "build: Encode message body failed.", e);
        return null;
      }

      return new ChallengeResponse(this);
    }

  }

}
