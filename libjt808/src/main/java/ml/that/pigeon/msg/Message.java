package ml.that.pigeon.msg;

import android.util.Log;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import ml.that.pigeon.util.ArrayUtils;
import ml.that.pigeon.util.LogUtils;

/**
 * Base class for JT/T808 messages.
 * <p>
 * Every message has a unique ID. Optionally, the "cipher", "phone", and "body" fields can be set.
 *
 * @author That Mr.L (thatmr.l@gmail.com)
 */
public class Message {// TODO: 10/23/2016 make this class abstract

  private static final String TAG = LogUtils.makeTag(Message.class);

  public static final byte CIPHER_NONE = 0;
  public static final byte CIPHER_RSA  = 1 << 2;

  public static final byte[] EMPTY_PHONE = new byte[6];
  public static final byte[] EMPTY_BODY  = ArrayUtils.EMPTY_BYTE_ARRAY;

  private final short   mId;
  private final boolean mIsLong;
  private final byte    mCipher;
  private final byte[]  mPhone;
  private final byte[]  mBody;

  protected Message(short id, byte cipher, byte[] phone, byte[] body) {
    mId = id;

    switch (cipher) {
      case CIPHER_NONE:
      case CIPHER_RSA:
        mCipher = cipher;
        break;
      default:
        Log.w(TAG, "Message: Unknown cipher mode, set to none.");
        mCipher = CIPHER_NONE;
    }

    if (phone != null && phone.length == 6) {
      mPhone = phone;
    } else {
      Log.w(TAG, "Message: Illegal phone number, set to empty.");
      mPhone = EMPTY_PHONE;
    }

    if (body != null) {
      mBody = body;
    } else {
      Log.w(TAG, "Message: Message body not specified, set to empty.");
      mBody = EMPTY_BODY;
    }

    mIsLong = mBody.length > Packet.MAX_LENGTH;
  }

  private Message(Builder builder) {
    mId = builder.id;
    mIsLong = builder.body.length > Packet.MAX_LENGTH;
    mCipher = builder.cipher;
    mPhone = builder.phone;
    mBody = builder.body;
  }

  public Packet[] getPackets() {
    List<byte[]> payloads = ArrayUtils.divide(mBody, Packet.MAX_LENGTH);
    int size = payloads.size();
    Packet[] packets = new Packet[size];

    int i = 0;
    for (byte[] payload : payloads) {
      packets[i] = new Packet(mId,
                              mIsLong,
                              mCipher,
                              mPhone,
                              PacketManager.getSn(),
                              size,
                              ++i,
                              payload);
    }

    return packets;
  }

  public int length() {
    return mBody.length;
  }

  public short getId() {
    return mId;
  }

  public boolean isLong() {
    return mIsLong;
  }

  public byte getCipher() {
    return mCipher;
  }

  public byte[] getPhone() {
    return mPhone;
  }

  public byte[] getBody() {
    return mBody;
  }

  @Override
  public String toString() {
    return new StringBuilder("{ ")
        .append("id=").append(Integer.toHexString(mId))
        .append(", lng=").append(mIsLong)
        .append(", cph=").append(mCipher)
        .append(", phn=").append(Arrays.toString(mPhone))
        .append(", bdy=").append(Arrays.toString(mBody))
        .append(" }").toString();
  }

  public static class Builder extends MessageBuilder {

    // Required parameters
    private final short id;

    public Builder(Packet... packets) {
      boolean found = false;
      int head = 0;

      for (int i = 0; i < packets.length; i++) {
        if (packets[i] != null) {
          found = true;
          head = i;
          break;
        }
        Log.w(TAG, "Builder: Packet not specified, ignore and continue.");
      }

      if (!found) {
        throw new NullPointerException("Packets must be specified.");
      }

      // The head packet is found, set fields
      this.id = packets[head].getMsgId();
      this.cipher = packets[head].getCipher();
      this.phone = packets[head].getPhone();

      // The head packet is not a long message, ignore other packets
      if (!packets[head].isLongMsg()) {
        this.body = packets[head].getPayload();
        return;
      }

      // The head packet is a long message, append other packets' payloads
      List<byte[]> payloads = new LinkedList<>();

      for (int i = head + 1; i < packets.length; i++) {
        if (packets[i] == null) {
          Log.w(TAG, "Builder: Packet not specified, ignore and continue.");
          continue;
        }
        if (this.id != packets[i].getMsgId()) {
          Log.w(TAG, "Builder: Packet with different ID, ignore and continue.");
          continue;
        }
        if (!packets[i].isLongMsg()) {
          Log.w(TAG, "Builder: Packet not a long message, ignore and continue.");
          continue;
        }
        if (this.cipher != packets[i].getCipher()) {
          Log.w(TAG, "Builder: Packet with different cipher mode, ignore and continue.");
          continue;
        }
        if (this.phone != packets[i].getPhone()) {
          Log.w(TAG, "Builder: Packet with different phone number, ignore and continue.");
          continue;
        }
        payloads.add(packets[i].getPayload());
      }

      if (packets[head].getTotal() != payloads.size()) {
        throw new IllegalArgumentException("Uncompleted message body.");
      }

      this.body = ArrayUtils.concatenate(payloads);
    }

    public Builder(short id) {
      this.id = id;
    }

    public Builder body(byte[] body) {
      if (body != null) {
        this.body = body;
      } else {
        Log.w(TAG, "body: Message body not specified, use default.");
      }

      return this;
    }

    @Override
    public Message build() {
      return new Message(this);
    }

  }

}
