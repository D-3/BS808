package com.deew.jt808.msg;

import android.util.Log;

import com.deew.jt808.util.ArrayUtils;
import com.deew.jt808.util.IntegerUtils;
import com.deew.jt808.util.LogUtils;

import java.util.Arrays;

/**
 * Represents JT/T808 message packets.
 *
 */
public class Packet {

  private static final String TAG = LogUtils.makeTag(Packet.class);

  /** 808基本数据类型与Java数据类型对应关系

    BYTE       8位       byte
    WORD      16位       short
    DWORD     32位       int
    BYTE[n]   n字节      byte[]
    BCD[n]    n字节      byte[]
    STRING    GBK编码    String 编码调用 String.getBytes["GBK"]
  */

  static final short MAX_LENGTH = 0x03ff;

  //Beginning Marker
  private static final byte PREFIX = 0x7e;
  //Ending Marker
  private static final byte SUFFIX = 0x7e;

  //Packet Header @{
    //消息ID
    private final short   mMsgId;       //BYTE
    //消息体属性 @{
    private final boolean mIsLongMsg;
    private final byte    mCipher;
    private final short   mTotal;
    private final short   mIndex;
    //@}
    //终端号码
    private final byte[]  mPhone;       //BCD[6]
    //消息流水号
    private final short   mSn;
  //@}

  //Packet body @{
    //payload
    private final byte[]  mPayload;
  //@}

  public Packet(short id,
                boolean isLong,
                byte cipher,
                byte[] phone,
                short sn,
                int total,
                int index,
                byte[] payload) {
    switch (cipher) {
      case Message.CIPHER_NONE:
      case Message.CIPHER_RSA:
        mCipher = cipher;
        break;
      default:
        mCipher = Message.CIPHER_NONE;
        Log.w(TAG, "Packet: Unknown cipher mode, set to none.");
    }

    if (phone != null && phone.length == 6) {
      mPhone = phone;
    } else {
      mPhone = Message.EMPTY_PHONE;
      Log.w(TAG, "Packet: Illegal phone number, set to empty.");
    }

    if (payload != null) {
      mPayload = payload;
    } else {
      mPayload = ArrayUtils.EMPTY_BYTE_ARRAY;
      Log.w(TAG, "Packet: Payload not specified, set to empty");
    }

    mMsgId = id;
    mIsLongMsg = isLong;
    mSn = sn;
    mTotal = (short) (isLong ? total : 0);
    mIndex = (short) (isLong ? index : 0);
  }

  // TODO: 2016/10/25 replace with packet pull parser
  @Deprecated
  public Packet(byte[] raw) {
    if (raw[0] != PREFIX || raw[raw.length - 1] != SUFFIX) {
      throw new IllegalArgumentException("Packet prefix or suffix not found.");
    }

    byte[] main = ArrayUtils.unescape(Arrays.copyOfRange(raw, 1, raw.length - 1));
    if (main.length < 13) {
      throw new IllegalArgumentException("Insufficient packet length.");
    }

    mCipher = (byte) (main[2] & 0x1c);
    if (mCipher != Message.CIPHER_NONE && mCipher != Message.CIPHER_RSA) {
      throw new IllegalArgumentException("Unknown cipher mode.");
    }

    mIsLongMsg = (main[2] & 0x20) == 0x20;
    int len = (IntegerUtils.parseInt(Arrays.copyOfRange(main, 2, 4)) & MAX_LENGTH);
    if ((isLongMsg() && len != main.length - 17) || (!isLongMsg() && len != main.length - 13)) {
      throw new IllegalArgumentException("Incorrect packet length.");
    }

    byte checksum = main[main.length - 1];
    if (checksum != ArrayUtils.xorCheck(Arrays.copyOfRange(main, 0, main.length - 1))) {
      throw new IllegalArgumentException("XOR check failed.");
    }

    mMsgId = IntegerUtils.parseShort(Arrays.copyOfRange(main, 0, 2));
    mPhone = Arrays.copyOfRange(main, 4, 10);
    mSn = IntegerUtils.parseShort(Arrays.copyOfRange(main, 10, 12));
    mTotal = isLongMsg() ? IntegerUtils.parseShort(Arrays.copyOfRange(main, 12, 14)) : 0;
    mIndex = isLongMsg() ? IntegerUtils.parseShort(Arrays.copyOfRange(main, 14, 16)) : 0;
    mPayload = Arrays.copyOfRange(main, main.length - 1 - len, main.length - 1);
  }

  public byte[] getBytes() {
    short attr = (short) ((mIsLongMsg ? 1 << 13 : 0) | (mCipher << 8) | (mPayload.length));

    byte[] header = ArrayUtils.concatenate(IntegerUtils.asBytes(mMsgId),
                                           IntegerUtils.asBytes(attr),
                                           mPhone,
                                           IntegerUtils.asBytes(mSn));
    if (mIsLongMsg) {
      header = ArrayUtils.concatenate(header,
                                      IntegerUtils.asBytes(mTotal),
                                      IntegerUtils.asBytes(mIndex));
    }

    byte checksum = ArrayUtils.xorCheck(ArrayUtils.concatenate(header, mPayload));

    byte[] main = ArrayUtils.concatenate(header, mPayload, IntegerUtils.asBytes(checksum));

    return ArrayUtils.concatenate(IntegerUtils.asBytes(PREFIX),
                                  ArrayUtils.escape(main),
                                  IntegerUtils.asBytes(SUFFIX));
  }

  public int length() {
    return mPayload.length;
  }

  public short getMsgId() {
    return mMsgId;
  }

  public boolean isLongMsg() {
    return mIsLongMsg;
  }

  public byte getCipher() {
    return mCipher;
  }

  public byte[] getPhone() {
    return mPhone;
  }

  public short getSn() {
    return mSn;
  }

  public short getTotal() {
    return mTotal;
  }

  public short getIndex() {
    return mIndex;
  }

  public byte[] getPayload() {
    return mPayload;
  }

  @Override
  public String toString() {
    return new StringBuilder()
        .append("{ id=").append(Integer.toHexString(mMsgId))
        .append(", lng=").append(mIsLongMsg)
        .append(", ciph=").append(mCipher)
        .append(", phn=").append(Arrays.toString(mPhone))
        .append(", sn=").append(mSn)
        .append(", ttl=").append(mTotal)
        .append(", idx=").append(mIndex)
        .append(", pld=").append(Arrays.toString(mPayload))
        .append(" }").toString();
  }

}
