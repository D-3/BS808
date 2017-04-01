package com.deew.jt808.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {

  private static final String TAG = LogUtils.makeTag(CryptoUtils.class);

  /**
   * Returns the cryptography byte array of the specified raw byte array using the given key.
   *
   * @param in  the raw byte array whose cryptography byte array to return
   * @param key the key to encrypt <tt>in</tt>
   * @return a cryptography byte array of <tt>in</tt>
   */
  public static byte[] encrypt(byte[] in, String key)
      throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    if (in == null || in.length <= 0) {
      return ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    try {
      Cipher cipher = Cipher.getInstance("aes/ecb/nopadding");
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes("ascii"), "aes"));
      byte[] crypto = cipher.doFinal(in);
      return ArrayUtils.concatenate(IntegerUtils.asBytes((byte) crypto.length), crypto);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException e) {
      Log.w(TAG, "encrypt: Encrypt failed.", e);
      return null;
    }
  }

  /**
   * Returns the raw byte array of the specified cryptography byte array using the given key.
   *
   * @param in  the encrypted byte array whose raw byte array to return
   * @param key the key to decrypt <tt>in</tt>
   * @return a raw byte array of <tt>in</tt>
   */
  public static byte[] decrypt(byte[] in, String key)
      throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    if (in == null || in.length <= 0) {
      return ArrayUtils.EMPTY_BYTE_ARRAY;
    }
    if (in[0] != in.length - 1) {
      throw new IllegalArgumentException("Crypto length incorrect.");
    }

    try {
      Cipher cipher = Cipher.getInstance("aes/ecb/nopadding");
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes("ascii"), "aes"));
      return cipher.doFinal(Arrays.copyOfRange(in, 1, in.length));
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException e) {
      Log.w(TAG, "encrypt: Encrypt failed.", e);
      return null;
    }
  }

}
