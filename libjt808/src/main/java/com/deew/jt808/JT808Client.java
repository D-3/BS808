package com.deew.jt808;

import android.util.Log;

import com.deew.jt808.conn.Connection;
import com.deew.jt808.conn.ConnectionConfiguration;
import com.deew.jt808.conn.ConnectionStateCallback;
import com.deew.jt808.msg.Message;
import com.deew.jt808.util.LogUtils;

/**
 * @author DeeW   (Find me on --> https://github.com/D-3)
 * @time 2017/4/1.
 */

public class JT808Client {

    private static final String TAG = LogUtils.makeTag(JT808Client.class);

    private String mHost;
    private int mPort;
    private String mAuthCode;

    private Connection mConnection;
    private ConnectionConfiguration mConnectionConfiguration;

    public JT808Client(){
        mConnection = new Connection();
    }

    public void connect(String host, int port, ConnectionStateCallback stateCallback){
        Log.d(TAG, "connect: " + host +":" +  port );
        //TODO check params whether right or throw IllegalArgumentException
        mHost = host;
        mPort = port;

        mConnectionConfiguration = new ConnectionConfiguration(mHost, mPort);
        if(mConnection == null){
            mConnection = new Connection();
        }
        mConnection.setConfig(mConnectionConfiguration);
        mConnection.setStateCallback(stateCallback);
        mConnection.connect();
    }

    public void setConnectionStateCallback(ConnectionStateCallback callback){
        mConnection.setStateCallback(callback);
    }

    public boolean isConnected(){
        return mConnection == null ? false : mConnection.isConnected();
    }

    public boolean isClosed(){
        return mConnection == null ? true : mConnection.isClosed();
    }

    public void close(){
        mConnection.close();
        mConnection = null;
    }

    /**
     * Send a message to the server.
     *
     * Be careful with that you must wait until the connection connected (using ConnectionStateCallback)
     * before calling this method to send a message
     *
     * @param message
     */
    public void sendMessage(Message message){
        mConnection.sendMessage(message);
    }
}
