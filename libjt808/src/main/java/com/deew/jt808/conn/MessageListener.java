package com.deew.jt808.conn;

import com.deew.jt808.msg.Message;

/**
 * Provides a mechanism to listen for messages that pass a specified filter. This allows event-style
 * programming -- every time a new message is found, the {@link #processMessage(Message)} method
 * will be called. This is the opposite approach to the functionality provided by a {@link
 * MessageCollector} which lets you block while waiting for results.
 *
 */
public interface MessageListener {

  /**
   * Process the next message sent to this message listener.
   * <p>
   * A single thread is responsible for invoking all listeners, so it's very important that
   * implementation of this method not block for any extended period of time.
   *
   * @param msg the message to process
   */
  void processMessage(Message msg);

}
