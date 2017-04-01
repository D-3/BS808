package ml.that.pigeon.filter;

import ml.that.pigeon.msg.Message;

/**
 * Filters for messages with a particular message ID.
 *
 * @author That Mr.L (thatmr.l@gmail.com)
 */
public class MessageIdFilter implements MessageFilter {

  private short mId;

  /**
   * Creates a new message ID filter using the specified message ID.
   *
   * @param id the message ID to filter for
   */
  public MessageIdFilter(short id) {
    mId = id;
  }

  @Override
  public boolean accept(Message msg) {
    return mId == msg.getId();
  }

  @Override
  public String toString() {
    return "MessageIdFilter by ID: " + mId;
  }

}
