package ml.that.pigeon.conn;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ml.that.pigeon.conn.Connection.ListenerWrapper;
import ml.that.pigeon.msg.Message;
import ml.that.pigeon.msg.Packet;
import ml.that.pigeon.util.LogUtils;

/**
 * Listens for packet traffic from the JT/T808 server and parse it into message objects.
 * <p>
 * The message reader also invokes all message listeners and collectors.
 *
 * @author That Mr.L (thatmr.l@gmail.com)
 */
class MessageReader {

  private static final String TAG = LogUtils.makeTag(MessageReader.class);

  private Connection      mConnection;
  private InputStream     mInput;
  private Thread          mReadThread;
  private ExecutorService mExecutor;

  private boolean mDone;

  /**
   * Creates a new message reader with the specified connection.
   *
   * @param conn the connection
   */
  MessageReader(Connection conn) {
    mConnection = conn;
    init();
  }

  /**
   * Initializes the reader in order to be used. The reader is initialized during the first
   * connection and when reconnecting due to an abruptly disconnection.
   */
  void init() {
    mDone = false;
    mInput = mConnection.getInput();

    mReadThread = new ReadThread();
    // TODO: 10/24/2016 add connection count to the name
    mReadThread.setName("Pigeon Message Reader ( )");
    mReadThread.setDaemon(true);

    // Create an executor to deliver incoming messages to listeners. We'll use a single thread with
    // an unbounded queue
    mExecutor = Executors.newSingleThreadExecutor();
  }

  /** Starts the packet read thread. */
  public synchronized void startup() {
    mReadThread.start();
  }

  /** Shuts the message reader down. */
  public void shutdown() {
    mDone = true;
  }

  /** Parses packets in order to process them further. */
  private void readPackets() {
    try {
      byte[] buf = new byte[128];
      int len;
      while (!mDone && (len = mInput.read(buf)) != -1) {
        if (len > 0) {
          byte[] raw = new byte[len];
          System.arraycopy(buf, 0, raw, 0, len);
          Packet packet = new Packet(raw);
          Log.d(TAG, "readPackets: " + packet);
          Message msg = new Message.Builder(packet).build();
          processMessage(msg);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    // TODO: 10/24/2016 implement this method
  }

  /**
   * Processes a message after it's been fully parsed by looping through the installed message
   * collectors and listeners and letting them examine the message to see if they are a match with
   * the filter.
   *
   * @param msg the message to process
   */
  private void processMessage(Message msg) {
    if (msg == null) {
      return;
    }

    // Loop through all collectors and notify the appropriate ones.
    for (MessageCollector collector : mConnection.getCollectors()) {
      collector.processMessage(msg);
    }

    // Deliver the incoming message to listeners
    mExecutor.submit(new ListenerNotification(msg));
  }

  /** A thread to read packets from the connection. */
  private class ReadThread extends Thread {

    @Override
    public void run() {
      super.run();
      readPackets();
    }

  }

  /** A runnable to notify all listeners of a message. */
  private class ListenerNotification implements Runnable {

    private Message message;

    public ListenerNotification(Message msg) {
      this.message = msg;
    }

    @Override
    public void run() {
      for (ListenerWrapper wrapper : mConnection.getRcvListeners().values()) {
        wrapper.notifyListener(this.message);
      }
    }

  }

}
