package com.deew.jt808.msg;

import android.util.Log;

import com.deew.jt808.Jtt415Constants;
import com.deew.jt808.util.ArrayUtils;
import com.deew.jt808.util.IntegerUtils;
import com.deew.jt808.util.LogUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class RegisterRequest extends Message {

  private static final String TAG = LogUtils.makeTag(RegisterRequest.class);

  public static final short ID = 0x0100;

  private static final byte[] EMPTY_MFRS_ID    = new byte[5];
  private static final byte[] EMPTY_CLT_MODEL  = new byte[20];
  private static final byte[] EMPTY_CLT_ID     = new byte[7];
  private static final String EMPTY_PLATE_TEXT = "";

  private final short  mProvId;     //WORD
  private final short  mCityId;     //WORD
  private final byte[] mMfrsId;     //BYTE[5]
  private final byte[] mCltModel;   //BYTE[20]
  private final byte[] mCltId;      //BYTE[7]
  private final byte   mPlateColor; //BYTE
  private final String mPlateText;  //STRING

  private RegisterRequest(Builder builder) {
    super(ID, builder.cipher, builder.phone, builder.body);

    mProvId = builder.provId;
    mCityId = builder.cityId;
    mMfrsId = builder.mfrsId;
    mCltModel = builder.cltModel;
    mCltId = builder.cltId;
    mPlateColor = builder.plateColor;
    mPlateText = builder.plateText;
  }

  @Override
  public String toString() {
    return new StringBuilder("{ id=0100")
        .append(", prov=").append(mProvId)
        .append(", city=").append(mCityId)
        .append(", mfrs=").append(Arrays.toString(mMfrsId))
        .append(", model=").append(Arrays.toString(mCltModel))
        .append(", cltId=").append(Arrays.toString(mCltId))
        .append(", pClr=").append(mPlateColor)
        .append(", pTxt=").append(mPlateText)
        .append(" }").toString();
  }

  public static class Builder extends MessageBuilder {

    // Optional parameters - initialized to default values
    private short  provId     = 0;
    private short  cityId     = 0;
    private byte[] mfrsId     = EMPTY_MFRS_ID;
    private byte[] cltModel   = EMPTY_CLT_MODEL;
    private byte[] cltId      = EMPTY_CLT_ID;
    private byte   plateColor = Jtt415Constants.PLATE_COLOR_TEST;
    private String plateText  = EMPTY_PLATE_TEXT;

    public Builder provId(short id) {
      this.provId = id;
      return this;
    }

    public Builder cityId(short id) {
      this.cityId = id;
      return this;
    }

    public Builder mfrsId(byte[] id) {
      if (id != null && id.length == 5) {
        this.mfrsId = id;
      } else {
        Log.w(TAG, "mfrsId: Illegal manufacturer ID, use default.");
      }

      return this;
    }

    public Builder cltModel(byte[] model) {
      if (model != null && model.length == 20) {
        this.cltModel = model;
      } else {
        Log.w(TAG, "cltModel: Illegal client model, use default.");
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

    @Override
    public RegisterRequest build() {
      try {
        this.body = ArrayUtils.concatenate(IntegerUtils.asBytes(this.provId),
                                           IntegerUtils.asBytes(this.cityId),
                                           this.mfrsId,
                                           this.cltModel,
                                           this.cltId,
                                           IntegerUtils.asBytes(this.plateColor),
                                           this.plateText.getBytes("GBK"));
      } catch (UnsupportedEncodingException uee) {
        Log.e(TAG, "build: Encode message body failed.", uee);
      }

      return new RegisterRequest(this);
    }

  }

}
