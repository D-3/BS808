package com.deew.jt808;

import android.util.Log;

import com.deew.jt808.conn.Connection;
import com.deew.jt808.conn.ConnectionConfiguration;
import com.deew.jt808.util.LogUtils;

import java.util.List;

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

    private List<Runnable> mTasks;



    private boolean mRunning = false;

    public void connect() {
        Log.d(TAG, "connect: ");

    }

    public boolean isConnected(){

    }



}
