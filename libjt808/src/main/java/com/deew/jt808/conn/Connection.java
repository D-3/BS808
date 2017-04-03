package com.deew.jt808.conn;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.deew.jt808.filter.MessageFilter;
import com.deew.jt808.msg.Message;
import com.deew.jt808.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Creates a socket connection to a JT/T808 server.
 * <p>
 * To create a connection to a JT/T808 server a simple usage of this API might looks like the
 * following:
 * <p>
 * <pre>
 *   // Create a configuration for new connection
 *   ConnectionConfiguration cfg = new ConnectionConfiguration("10.1.5.21", 29930);
 *   // Create a connection to the JT/T808 server
 *   Connection conn = new Connection(cfg);
 *   // Connect to the server
 *   conn.connect();
 *   // JT/T808 servers require you to login before performing other tasks
 *   conn.login();
 *   // Create a message to send
 *   Message msg = new Message.Builder().build();
 *   // Send the server a message
 *   conn.sendMessage(msg);
 *   // Disconnect from the server
 *   conn.disconnect();
 * </pre>
 * <p>
 * Connection can be reused between connections. This means that a Connection may be connected,
 * disconnected, and then connected again. Listeners of the Connection will be retained across
 * connections.
 * <p>
 * If a connected Connection gets disconnected abruptly and automatic reconnection is enabled
 * ({@link ConnectionConfiguration#isReconnectionAllowed()}, the default), then it will try to
 * reconnect again. To stop the reconnection process, use {@link #disconnect()}. Once stopped you
 * can use {@link #connect()} to manually connect to the server.
 *
 */

// TODO: 2017/4/3  Add auto reconnect
public class Connection {

  private static final String TAG = LogUtils.makeTag(Connection.class);

  // Holds the initial configuration used while creating the connection
  private ConnectionConfiguration mConfig;

  // The socket which is used for this connection
  private Socket        mSocket;
  private InputStream   mInput;
  private OutputStream  mOutput;
  private MessageReader mReader;
  private MessageWriter mWriter;
  private Handler mCallbackHandler;

  private static final byte CONNECTED = 0;
  private static final byte CONNECTING = 1;
//  private static final byte DISCONNECTING = 2;
//  private static final byte DISCONNECTED = 3;
  private static final byte CLOSEING = 4;
  private static final byte CLOSED = 5;

  private byte mConnState = CLOSED;

  private Object	conLock = new Object();  	// Used to synchronize connection state

//  private boolean mConnected     = false;

  // Flag that indicates if the client is currently authenticated with the server
  private boolean mAuthenticated = false;


  private ConnectionStateCallback mStateCallback;

  /**
   * Creates a new JT/T808 connection using the specified connection configuration.
   * <p>
   * Note that Connection constructors do not establish a connection to the server and you must call
   * {@link #connect()}.
   *
   */
  public Connection() {
    mCallbackHandler = new Handler(Looper.getMainLooper());
  }


  public void setStateCallback(ConnectionStateCallback stateCallback){
    mStateCallback = stateCallback;
  }

  public void setConfig(ConnectionConfiguration config){
    mConfig = config;
  }

  /**
   * Returns the configuration used to connect to the server.
   *
   * @return the configuration used to connect to the server
   */
  public ConnectionConfiguration getConfig() {
    return mConfig;
  }

  public InputStream getInput() {
    return mInput;
  }

  public OutputStream getOutput() {
    return mOutput;
  }

  public boolean isConnecting() {
    synchronized (conLock){
      return mConnState == CONNECTING;
    }
  }

  public boolean isConnected() {
    synchronized (conLock){
      return mConnState == CONNECTED;
    }
  }

//  public boolean isDisconnecting() {
//    synchronized (conLock){
//      return mConnState == DISCONNECTING;
//    }
//  }
//
//  public boolean isDisconnected() {
//    synchronized (conLock){
//      return mConnState == DISCONNECTED;
//    }
//  }

  public boolean isClosing() {
    synchronized (conLock){
      return mConnState == CLOSEING;
    }
  }

  public boolean isClosed() {
    synchronized (conLock){
      return mConnState == CLOSED;
    }
  }


//  public boolean isAuthenticated() {
//    return mAuthenticated;
//  }

//  public boolean isSocketClosed() {
//    return mSocketClosed;
//  }



  /**
   * Establishes a connection to the JT/T808 server in a background thread
   */
  public void connect() throws IllegalStateException{
    synchronized (conLock){
      Log.d(TAG, "connect called");
//      if(isDisconnected()){
//        mConnState = CONNECTING;
//        ConnectBG connectBg = new ConnectBG();
//        connectBg.start();
//      }else if(isClosed()){
//        throw new IllegalStateException("connection is closed");
//      }
      if(isClosed()){
        mConnState = CONNECTING;
        ConnectBG connectBg = new ConnectBG();
        connectBg.start();
      }
      else if(isConnecting()){
        throw new IllegalStateException("connection is connecting");
      }
      else if(isConnected()){
        throw new IllegalStateException("connection is connected");
      }
//      else if(isDisconnecting()){
//        throw new IllegalStateException("connection is disconnecting");
//      }
      else if(isClosing()){
        throw new IllegalStateException("connection is disconnecting");
      }
    }
  }

  /**
   *
   */
  private class ConnectBG implements Runnable {
    Thread mConnectThread;

    ConnectBG(){
      mConnectThread = new Thread(this, "Connect thread");
    };

    void start(){
      Log.d(TAG, "start connect thread");
      mConnectThread.start();
    }

    @Override
    public void run() {
      Log.d(TAG, "connecting");
      String host = getConfig().getHost();
      int port = getConfig().getPort();
      try {
        Log.d(TAG, "create socket");
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        Socket socket = new Socket();
        //TODO read the timeout from  connection config
        socket.connect(socketAddress, 5000);
        connectComplete(socket);
      }catch(IOException e) {
        Log.e(TAG, "Connect FAIL !!!", e);

        // Close all the writer, reader, iostream and socket, Reset other component
        shutDown();

        // Notify connect fail
        onConnectFail();
      }
    }
  }

  /**
   *
   * @param socket
   * @throws IOException
   */
  private void connectComplete(Socket socket) throws IOException{
    synchronized (conLock){
      mSocket = socket;

      boolean isFirstInit = (mReader == null || mWriter == null);

      // Set the input stream and output stream instance variables
      mInput = mSocket.getInputStream();
      mOutput = mSocket.getOutputStream();

      if (isFirstInit) {
        mWriter = new MessageWriter(this);
        mReader = new MessageReader(this);
      } else {
        mWriter.init();
        mReader.init();
      }

      // Start the message writer
      mWriter.startup();
      // Start the message reader, the startup() method will block until we get a packet from server
      mReader.startup();

      // TODO: 2016/11/1 move this to when logged in
      // Start keep alive process
      mWriter.keepAlive();

      // Make note of the fact that we're now connected
      mConnState = CONNECTED;
      Log.d(TAG, "connect SUCCESS !!!");

      onConnectSuccess();
    }
  }

  /**
   * disconnect the connection. The Connection can still be used for connecting to the server again.
   */
  private void disconnect(){
    //TODO There is no disconnect now. To be added in the future
  }


  public void close(){
    synchronized (conLock){
      Log.w(TAG, "close called");
      //TODO change state here. connState = CLOSING
      CloseBG closeBG = new CloseBG();
      closeBG.start();
    }
  }

  private class CloseBG implements Runnable {
    Thread mDisConnThread;

    CloseBG(){
      mDisConnThread = new Thread(this, "Close thread");
    };

    void start(){
      Log.w(TAG, "start close thread");
      mDisConnThread.start();
    }

    @Override
    public void run() {
      Log.w(TAG, "closing");
      shutDown();
    }
  }


  /**
   * Close connection and clean up
   *
   * Note that the function is called by the close thread or writer and reader
   * when unexpected exception happened handling in/out stream. Users should never touch it
   *
   */
  public void shutDown(){
    synchronized (conLock){
//    if(!isDisconnected()){
        if(!isClosed()){
          if (mWriter != null) {
            mWriter.shutdown();
            mWriter = null;
          }
          if (mReader != null) {
            mReader.shutdown();
            mReader = null;
          }
          if (mInput != null) {
            try {
              mInput.close();
            } catch (IOException e) {
              // Ignore
            }
            mInput = null;
          }
          if (mOutput != null) {
            try {
              mOutput.close();
            } catch (IOException e) {
              // Ignore
            }
            mOutput = null;
          }
          if (mSocket != null) {
            try {
              mSocket.close();
            } catch (IOException e) {
              // Ignore
            }
            mSocket = null;
          }

          mStateCallback = null;
          mCallbackHandler = null;
          mCollectors.clear();
          mRcvListeners.clear();
          mSndListeners.clear();

          mConnState = CLOSED;
          Log.w(TAG, "closed");
        }
//    }
    }
  }

  private Socket getSocket(){
    return mSocket;
  }

  /**
   * Send a message to server. Remember Checking if connected first
   *
   * @param msg
   */
  public void sendMessage(Message msg) {
    if (!isConnected()) {
      throw new IllegalStateException("Not connected to server.");
    }
    if (msg == null) {
      throw new NullPointerException("Message is null.");
    }

    mWriter.sendMessage(msg);
  }

  private void onConnectSuccess(){
    if(mStateCallback != null){
      mCallbackHandler.post(new Runnable() {
        @Override
        public void run() {
          mStateCallback.onSuccess();
        }
      });
    }
  }

  private void onConnectFail(){
    if(mStateCallback != null){
      mCallbackHandler.post(new Runnable() {
        @Override
        public void run() {
          mStateCallback.onFail();
        }
      });
    }
  }


  //  /**
//   * Logs in to the server using the strongest authentication mode supported by the server. If the
//   * server supports advanced authentication then the client will be authenticated using advanced if
//   * not basic authentication will be tried. If more than five seconds (default timeout) elapses in
//   * each step of the authentication process without a reply from the server, or if an error
//   * occurs.
//   * <p>
//   * Before logging in (i.e. authenticate) to the server the connection must be connected.
//   *
//   * @param auth the authentication code
//   */
//  public synchronized void login(String auth) {
//    if (!isConnected()) {
//      throw new IllegalStateException("Not connected to server.");
//    }
//    if (isAuthenticated()) {
//      throw new IllegalStateException("Already logged in to server.");
//    }
//
//    // TODO: 2016/10/28 choose from basic and advanced authentication
//    // Authenticate using basic
//    boolean result = new AdvancedAuthentication(this).authenticate(auth);
//
//    if (result == true) {
//      mAuthenticated = true;
//    } else {
//      mAuthenticated = false;
//    }
//  }


  // A collection of MessageCollectors which collects messages for a specified filter and perform
  // blocking and polling operations on the result queue.
  private final Collection<MessageCollector>          mCollectors   = new ConcurrentLinkedQueue<>();
  // List of MessageListeners that will be notified when a new message was received
  private final Map<MessageListener, ListenerWrapper> mRcvListeners = new ConcurrentHashMap<>();
  // List of MessageListeners that will be notified when a new messgae was sent
  private final Map<MessageListener, ListenerWrapper> mSndListeners = new ConcurrentHashMap<>();


  /**
   * Creates a new message collector for this connection. A message filter determines which messages
   * will be accumulated by the collector. A MessageCollector is more suitable to use than a {@link
   * MessageListener} when you need to wait for a specific result.
   *
   * @param filter the message filter to use
   * @return a new message collector
   */
  public MessageCollector createMessageCollector(MessageFilter filter) {
    MessageCollector collector = new MessageCollector(this, filter);
    // Add the collector to the list of active collectors
    mCollectors.add(collector);
    return collector;
  }

  /**
   * Removes a message collector of this connection.
   *
   * @param collector a message collector which was created for this connection
   */
  void removeMessageCollector(MessageCollector collector) {
    mCollectors.remove(collector);
  }

  /**
   * Get the collection of all message collectors for this connection.
   *
   * @return a collection of message collectors for this connection
   */
  public Collection<MessageCollector> getCollectors() {
    return mCollectors;
  }

  /**
   * Registers a message listener with this connection. A message filter determines which messages
   * will be delivered to the listener. If the same message listener is added again with a different
   * filter, only the new filter will be used.
   *
   * @param listener the message listener to notify of new received messages
   * @param filter   the message filter to use
   */
  public void addRcvListener(MessageListener listener, MessageFilter filter) {
    if (listener == null) {
      throw new NullPointerException("Message listener is null.");
    }

    ListenerWrapper wrapper = new ListenerWrapper(listener, filter);
    mRcvListeners.put(listener, wrapper);
  }

  /**
   * Removes a message listener for received messages from this connection.
   *
   * @param listener the message listener to remove
   */
  public void removeRcvListener(MessageListener listener) {
    mRcvListeners.remove(listener);
  }

  /**
   * Get a map of all message listeners for received messages of this connection.
   *
   * @return a map of all message listeners for received messages
   */
  Map<MessageListener, ListenerWrapper> getRcvListeners() {
    return mRcvListeners;
  }

  /**
   * Registers a message listener with this connection. The listener will be notified of every
   * message that this connection sends. A message filter determines which messages will be
   * delivered to the listener. Note that the thread that writes messages will be used to invoke the
   * listeners. Therefore, each message listener should complete all operations quickly or use a
   * different thread for processing.
   *
   * @param listener the message listener to notify of sent messages
   * @param filter   the message filter to use
   */
  public void addSndListener(MessageListener listener, MessageFilter filter) {
    if (listener == null) {
      throw new NullPointerException("Message listener is null.");
    }

    ListenerWrapper wrapper = new ListenerWrapper(listener, filter);
    mSndListeners.put(listener, wrapper);
  }

  /**
   * Removes a message listener for sending message from this connection.
   *
   * @param listener the message listener to remove
   */
  public void removeSndListener(MessageListener listener) {
    mSndListeners.remove(listener);
  }

  /**
   * Get a map of all message listeners for sending messages of this connection.
   *
   * @return a map of all message listeners for sent messages
   */
  Map<MessageListener, ListenerWrapper> getSndListeners() {
    return mSndListeners;
  }


  /** A wrapper class to associate a message filter with a listener. */
  static class ListenerWrapper {

    private MessageListener listener;
    private MessageFilter   filter;

    /**
     * Creates a class which associates a message filter with a listener.
     *
     * @param listener the message listener
     * @param filter   the associated filter or {@code null} if it listen for all messages
     */
    public ListenerWrapper(MessageListener listener, MessageFilter filter) {
      this.listener = listener;
      this.filter = filter;
    }

    /**
     * Notify and process the message listener if the filter matches the message.
     *
     * @param msg the message which was sent or received
     */
    public void notifyListener(Message msg) {
      if (this.filter == null || this.filter.accept(msg)) {
        listener.processMessage(msg);
      }
    }

  }

}
