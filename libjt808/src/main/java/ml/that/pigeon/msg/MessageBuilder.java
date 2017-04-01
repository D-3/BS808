package ml.that.pigeon.msg;

import android.util.Log;

import ml.that.pigeon.util.LogUtils;

public abstract class MessageBuilder {

  private static final String TAG = LogUtils.makeTag(MessageBuilder.class);

  // Optional parameters - initialized to default values
  protected byte   cipher = Message.CIPHER_NONE;
//  protected byte[] phone  = Message.EMPTY_PHONE;
  protected byte[] phone = new byte[]{0x08, (byte) 0x91,0x23,0x45,0x67, (byte) 0x89};
  protected byte[] body   = Message.EMPTY_BODY;

  public MessageBuilder cipher(byte cipher) {
    switch (cipher) {
      case Message.CIPHER_NONE:
      case Message.CIPHER_RSA:
        this.cipher = cipher;
        break;
      default:
        Log.w(TAG, "cipher: Unknown cipher mode, use default.");
    }

    return this;
  }

  public MessageBuilder phone(byte[] phone) {
    if (phone != null && phone.length == 6) {
      this.phone = phone;
    } else {
      Log.w(TAG, "phone: Illegal phone number, use default.");
    }

    return this;
  }

  public abstract Message build();

}
