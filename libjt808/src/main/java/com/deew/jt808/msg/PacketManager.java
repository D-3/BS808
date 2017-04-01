package com.deew.jt808.msg;

class PacketManager {

  private static final short MAX_SN = 2000;

  private static short sSn = 1;

  static short getSn() {
    if (sSn < MAX_SN) {
      return sSn++;
    } else {
      sSn = 1;
      return MAX_SN;
    }
  }

}
