package com.deew.jt808;

import android.util.Log;

import com.deew.jt808.conn.Connection;
import com.deew.jt808.conn.ConnectionConfiguration;
import com.deew.jt808.conn.ConnectionStateCallback;
import com.deew.jt808.conn.MessageListener;
import com.deew.jt808.filter.MessageIdFilter;
import com.deew.jt808.msg.AuthenticateRequest;
import com.deew.jt808.msg.Message;
import com.deew.jt808.msg.RegisterReply;
import com.deew.jt808.msg.RegisterRequest;
import com.deew.jt808.msg.ServerGenericReply;
import com.deew.jt808.util.LogUtils;

/**
 * @author DeeW   (Find me on --> https://github.com/D-3)
 * @time 2017/4/1.
 */

// TODO: 2017/4/3  Add auto regist and authenticate
public class JT808Client implements ConnectionStateCallback{

    private static final String TAG = LogUtils.makeTag(JT808Client.class);

    private String mHost;
    private int mPort;

    private Connection mConnection;
    private ConnectionConfiguration mConnectionConfiguration;
    private ClientStateCallback mStateCallback;

    private boolean isAuthenticate = false;

    public JT808Client(){
        mConnection = new Connection();
    }

    /**
     * Connect to server
     *
     * @param host
     * @param port
     * @param stateCallback
     */
    public void connect(String host, int port, ClientStateCallback stateCallback){
        Log.d(TAG, "connect: " + host +":" +  port );
        //TODO check params whether right or throw IllegalArgumentException
        mHost = host;
        mPort = port;

        mStateCallback = stateCallback;

        mConnectionConfiguration = new ConnectionConfiguration(mHost, mPort);
        if(mConnection == null){
            mConnection = new Connection();
            mConnection.setStateCallback(this);
        }
        mConnection.setConfig(mConnectionConfiguration);
        mConnection.connect();
    }

    public boolean isConnected(){
        return mConnection == null ? false : mConnection.isConnected();
    }

    public boolean isClosed(){
        return mConnection == null ? true : mConnection.isClosed();
    }

    /**
     * close connection and reset other members
     */
    public void close(){
        mConnection.close();
        mConnection = null;
        mConnectionConfiguration = null;
        mStateCallback = null;

        isAuthenticate = false;
    }

    /**
     * Send a message to the server.
     *
     * Be careful with that you must wait until the connection connected (using ConnectionStateCallback)
     * before calling this method to send a message
     *
     * @param message
     */
    public void sendMessage(Message message) throws IllegalAccessException{
        if(isAuthenticate){
            mConnection.sendMessage(message);
        }else{
            throw new IllegalAccessException("Can not send any message before authenticated");
        }
    }

    @Override
    public void onSuccess() {
        if(mStateCallback != null){
            mStateCallback.connectSuccess();
        }
    }

    @Override
    public void onFail() {
        if(mStateCallback != null){
            mStateCallback.connectFail();
        }
    }

    public void registerClient(RegisterRequest registerRequest){
        Log.d(TAG, "registerClient " + registerRequest);
        mConnection.addRcvListener(mRegisterMsgListener, new MessageIdFilter(RegisterReply.ID));
        mConnection.sendMessage(registerRequest);
    }

    public void authenticate(String authCode){
        Log.d(TAG, "authenticate " + authCode);
        AuthenticateRequest request = new AuthenticateRequest.Builder(authCode).build();
        mConnection.addRcvListener(mAuthMsgListener, new MessageIdFilter(ServerGenericReply.ID));
        mConnection.sendMessage(request);
    }

    /** A message listener to process register reply. */
    private MessageListener mRegisterMsgListener =  new MessageListener(){

        @Override
        public void processMessage(Message msg) {
            Log.d(TAG, "processMessage: msg=" + msg);
//            if (RegisterReply.ID == msg.getId()) {
                RegisterReply reply = new RegisterReply.Builder(msg).build();
                mConnection.removeRcvListener(mRegisterMsgListener);
                if(mStateCallback != null){
                    mStateCallback.registerComplete(reply);
                }
//            }
        }

    };


    /** A message listener to process auth reply. */
    private MessageListener mAuthMsgListener =  new MessageListener(){

        @Override
        public void processMessage(Message msg) {
            Log.d(TAG, "processMessage: msg=" + msg);
//            if(ServerGenericReply.ID == msg.getId()){
                ServerGenericReply reply = new ServerGenericReply.Builder(msg).build();
                mConnection.removeRcvListener(mAuthMsgListener);
                if(reply.getResult() == ServerGenericReply.RESULT_OK){
                    isAuthenticate = true;
                }
                if(mStateCallback != null){
                    mStateCallback.authComplete(reply);
                }
//            }
        }

    };



}
