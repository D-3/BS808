package com.deew.jt808;

import android.content.SharedPreferences;
import android.util.Log;

import com.deew.jt808.conn.Connection;
import com.deew.jt808.conn.ConnectionConfiguration;
import com.deew.jt808.conn.MessageListener;
import com.deew.jt808.msg.LocationMessage;
import com.deew.jt808.msg.Message;
import com.deew.jt808.msg.RegisterReply;
import com.deew.jt808.msg.RegisterRequest;
import com.deew.jt808.util.LogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.deew.jt808.filter.MessageIdFilter;
import com.deew.jt808.util.TimeUtils;

/**
 * This class is to manage the JT/T808 connection between client and server.
 *
 * @author That Mr.L (thatmr.l@gmail.com)
 */
public class ConnectionManager {

    private static final String TAG = LogUtils.makeTag(ConnectionManager.class);

    private SharedPreferences mPrefs;
    private String mHost;
    private int mPort;
    private String mAuthCode;

    private Connection mConnection;

    private List<Runnable> mTasks;
    private MessageService.TaskSubmitter mSubmitter;
    private MessageService.TaskTracker mTracker;
    private Future mFuture;

    private static final byte CONNECTED	= 0;
    private static final byte CONNECTING	= 1;
    private static final byte DISCONNECTING	= 2;
    private static final byte DISCONNECTED	= 3;
    private static final byte CLOSED	= 4;

    private boolean mRunning = false;

    public ConnectionManager(MessageService svc) {
        mPrefs = svc.getPrefs();
        mHost = mPrefs.getString(ClientConstants.PREF_KEY_HOST, ClientConstants.PREF_DEFAULT_HOST);
        mPort = mPrefs.getInt(ClientConstants.PREF_KEY_PORT, ClientConstants.PREF_DEFAULT_PORT);
        mAuthCode = mPrefs.getString(ClientConstants.PREF_KEY_AUTH_CODE, null);

        mTasks = new ArrayList<>();
        mSubmitter = svc.getSubmitter();
        mTracker = svc.getTracker();
    }

    public void connect() {
        Log.d(TAG, "connect: ");

        addTask(new ConnectTask());
        addTask(new RegisterTask());
        addTask(new LoginTask());
        addTask(new UploadLocationTask());

    }

    public boolean isConnected() {
        return mConnection != null && mConnection.isConnected();
    }

    private boolean isRegistered() {
        return mPrefs.contains(ClientConstants.PREF_KEY_AUTH_CODE);
    }

    private boolean isAuthenticated() {
        return mConnection != null && mConnection.isConnected() && mConnection.isAuthenticated();
    }

    public void addTask(Runnable task) {

        mTracker.increase();
        synchronized (mTasks) {
            Log.d(TAG, "addTask: tasks isEmpty=" + mTasks.isEmpty() + " isRuning=" + mRunning);
            if (mTasks.isEmpty() && !mRunning) {
                mRunning = true;
                mFuture = mSubmitter.submit(task);
                Log.d(TAG, "submit ----> future=" + mFuture);
                if (mFuture == null) {
                    mTracker.decrease();
                }
            } else {
                mTasks.add(task);
            }
        }

        Log.d(TAG, "addTask: Done.");
    }

    private void runTask() {
        Log.d(TAG, "runTask: ");

        synchronized (mTasks) {
            if (!mTasks.isEmpty()) {
                Runnable task = mTasks.get(0);
                mTasks.remove(0);
                mRunning = true;
                mFuture = mSubmitter.submit(task);
                if (mFuture == null) {
                    mTracker.decrease();
                }
            } else {
                mRunning = false;
                mFuture = null;
            }
        }
        mTracker.decrease();

        Log.d(TAG, "runTask: Done.");
    }

    /**
     * A runnable task to connect to the server.
     */
    private class ConnectTask implements Runnable {

        @Override
        public void run() {
            Log.i(TAG, "run: Connecting...");

            if (!isConnected()) {
                // Create the configuration for this new connection
                ConnectionConfiguration cfg = new ConnectionConfiguration(mHost, mPort);
                // Create a new connection
                mConnection = new Connection(cfg);
                // Connect to the server
                try {
                    mConnection.connect();
                    Log.i(TAG, "run: Connected successfully.");

                } catch (IOException ioe) {
                    Log.e(TAG, "run: Connection failed.", ioe);
                }
                runTask();
            } else {
                Log.i(TAG, "run: Connected already.");
                runTask();
            }
        }

    }


    /**
     * A runnable task to log into the server.
     */
    private class LoginTask implements Runnable {

        @Override
        public void run() {
            Log.i(TAG, "run: Logging in...");

            if (!isAuthenticated()) {
                Log.d(TAG, "run: auth=" + mAuthCode);

                mConnection.login(mAuthCode);
                Log.d(TAG, "run: Logged in successfully.");
                runTask();
            }
        }

    }


    /**
     * A runnable task to register a new client onto the server.
     */
    private class RegisterTask implements Runnable {

        @Override
        public void run() {
            Log.i(TAG, "run: Registering...");

            if (!isRegistered()) {
                mConnection.addRcvListener(new RegisterListener(), new MessageIdFilter(RegisterReply.ID));
                RegisterRequest request = new RegisterRequest.Builder().build();
                mConnection.sendMessage(request);
            } else {
                Log.i(TAG, "run: Client registered already.");
                runTask();
            }
        }

    }

    /**
     * A message listener to process register reply.
     */
    private class RegisterListener implements MessageListener {

        @Override
        public void processMessage(Message msg) {
            Log.d(TAG, "processMessage: msg=" + msg);

            if (RegisterReply.ID == msg.getId()) {
                RegisterReply reply = new RegisterReply.Builder(msg).build();
                switch (reply.getResult()) {
                    case RegisterReply.RESULT_OK:
                        mAuthCode = reply.getAuthCode();
                        Log.d(TAG, "processMessage: authCode=" + mAuthCode);
                        mPrefs.edit().putString(ClientConstants.PREF_KEY_AUTH_CODE, mAuthCode).commit();
                        Log.i(TAG, "processMessage: Client registered successfully.");
                        runTask();
                        break;
                    case RegisterReply.RESULT_VEH_NOT_FOUND:
                        Log.w(TAG, "processMessage: Registration failed - vehicle not found.");
                        break;
                    case RegisterReply.RESULT_VEH_REGISTERED:
                        Log.w(TAG, "processMessage: Registration failed - vehicle registered.");
                        break;
                    case RegisterReply.RESULT_CLT_NOT_FOUND:
                        Log.w(TAG, "processMessage: Registration failed - client not found.");
                        break;
                    case RegisterReply.RESULT_CLT_REGISTERED:
                        Log.w(TAG, "processMessage: Registration failed - client registered.");
                        break;
                    default:
                        Log.e(TAG, "processMessage: Unknown registration result.");
                }
            }
        }

    }


    public class UploadLocationTask implements Runnable {

        @Override
        public void run() {
            Log.i(TAG, "run: upload location");
            if (mConnection.isConnected()) {
                long timestamp = TimeUtils.getCurrentTimeJT808form();
                Log.d(TAG, "time=" + timestamp);
                LocationMessage.Builder builder = new LocationMessage.Builder();
                builder.setLatitude(22.5409180000);
                builder.setLongitude(114.0560200000);
                builder.setTimestamp(timestamp);
                LocationMessage message = builder.build();
                mConnection.sendMessage(message);
            }
            runTask();
        }

    }

}
