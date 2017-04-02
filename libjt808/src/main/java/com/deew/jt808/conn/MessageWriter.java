package com.deew.jt808.conn;

import android.util.Log;

import com.deew.jt808.msg.HeartbeatMessage;
import com.deew.jt808.msg.Message;
import com.deew.jt808.msg.Packet;
import com.deew.jt808.util.LogUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Writes messages to a JT/T808 server.
 * <p>
 * Messages are sent using a dedicated thread. Message interceptors can be registered to dynamically
 * modify message before they're actually sent. Message listeners can be registered to listen for
 * all outgoing messages.
 *
 */
class MessageWriter {

  private static final String TAG = LogUtils.makeTag(MessageWriter.class);

  private final BlockingQueue<Packet> mQueue;

  private Connection   mConnection;
  private OutputStream mOutput;
  private Thread       mWriteThread;
  private Thread       mKeepAliveThread;

  private boolean mDone;

  // Timestamp when the last packet was sent to the server. This information is used by the keep
  // alive process to only send heartbeats when the connection has been idle
  private long mLastActive = System.currentTimeMillis();

  /**
   * Creates a new message writer with the specified connection.
   *
   * @param conn the connection
   */
  MessageWriter(Connection conn) {
    mQueue = new ArrayBlockingQueue<>(500, true);
    mConnection = conn;
    init();
  }

  /**
   * Initializes the writer in order to be used. It is called at the first connection and also is
   * invoked if the connection is disconnected by an error.
   */
  void init() {
    mDone = false;
    mOutput = mConnection.getOutput();

    mWriteThread = new WriteThread();
    // TODO: 10/24/2016 add connection count to the name
    mWriteThread.setName("Pigeon Message Writer ( )");
    mWriteThread.setDaemon(true);
  }

  /**
   * Starts the packet write thread. The message writer will continue writing packets until {@link
   * #shutdown} or an error occurs.
   */
  public void startup() {
    mWriteThread.start();
  }

  /**
   * Shuts down the message writer. Once this method has been called, no further packets will be
   * written to the server.
   */
  public void shutdown() {
    Log.d(TAG, "shutdown writer");
    mDone = true;
    synchronized (mQueue) {
      mQueue.notifyAll();
    }
  }

  /**
   * Starts the keep alive process. An empty message (aka heartbeat) is going to be sent to the
   * server every 30 seconds (by default) since the last packet was sent to the server.
   */
  void keepAlive() {
    // Schedule a keep-alive task to run if the feature is enabled, will write out a empty
    // message each time it runs to keep the TCP/IP connection open
    // TODO: 2016/11/1 read from preferences
    int keepAliveInterval = 10;
    if (keepAliveInterval > 0) {
      KeepAliveTask task = new KeepAliveTask(keepAliveInterval);
      mKeepAliveThread = new Thread(task);
      mKeepAliveThread.setDaemon(true);
      // TODO: 2016/11/1 replace with connection counter value
      mKeepAliveThread.setName("Pigeon Keep Alive ( )");
      mKeepAliveThread.start();
    }
  }

  /**
   * Sends the specified message to the server.
   *
   * @param msg the message to send
   */
  public void sendMessage(Message msg) {
    if (!mDone) {
      try {
        for (Packet packet : msg.getPackets()) {
          mQueue.put(packet);
        }
      } catch (InterruptedException ie) {
        ie.printStackTrace();
        return;
      }
      synchronized (mQueue) {
        mQueue.notifyAll();
      }
    }
  }

  private void writePackets() {
    try {
      // Write out packets from the queue
      while (!mDone) {
        Packet packet = nextPacket();
        if (packet != null) {
          Log.d(TAG, "writePackets: " + packet);
          synchronized (mOutput) {
            mOutput.write(packet.getBytes());
            mOutput.flush();
            // Keep track of the last time a packet was sent to the server
            mLastActive = System.currentTimeMillis();
          }
        }
      }

      // Flush out the rest of the queue. If the queue is extremely large, it's possible we won't
      // have time to entirely flush it before the socket is forced closed by the shutdown process.
      synchronized (mOutput) {
        while (!mQueue.isEmpty()) {
          Packet packet = mQueue.remove();
          mOutput.write(packet.getBytes());
        }
        mOutput.flush();
        mOutput.close();
      }

      // Delete the queue contents (hopefully nothing is left)
      mQueue.clear();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      //TODO   this solution is not good
      mConnection.shutDown();
    }
  }

  /**
   * Returns the next available packet from the queue for writing.
   *
   * @return the next available for writing
   */
  private Packet nextPacket() {
    Packet packet = null;

    // Wait until there's a packet or we're done
    while (!mDone && (packet = mQueue.poll()) == null) {
      try {
        synchronized (mQueue) {
          mQueue.wait();
        }
      } catch (InterruptedException ie) {
        // Do nothing
      }
    }

    return packet;
  }

  private class WriteThread extends Thread {

    @Override
    public void run() {
      super.run();
      writePackets();
    }

  }

  /**
   * A TimerTask that keeps connections to the server alive by sending a empty message on an
   * interval.
   */
  private class KeepAliveTask implements Runnable {

    private int delay;
    private HeartbeatMessage.Builder mHearbeatBuilder;

    public KeepAliveTask(int delay) {
      this.delay = delay;
      mHearbeatBuilder = new HeartbeatMessage.Builder();
    }

    @Override
    public void run() {
      try {
        // Sleep a minimum of 15 seconds plus delay before sending first heartbeat
        Thread.sleep(15000 + delay * 1000L);
      } catch (InterruptedException ie) {
        // Do nothing
      }

      while (!mDone) {
        synchronized (mOutput) {
          // Send heartbeat if no packet has been sent to the server for a given time
          if (System.currentTimeMillis() - mLastActive >= delay * 1000L) {
            sendMessage(mHearbeatBuilder.build());
          }
        }

        try {
          // Sleep until we should write the next keep-alive
          Thread.sleep(delay * 1000L);
        } catch (InterruptedException ie) {
          // Do nothing
        }
      }
    }

  }

}
