package com.deew.jt808;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deew.jt808.util.IntentUtils;
import com.deew.jt808.util.LogUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Service that continues to run in background and send and receive messages from the server.
 * <p>
 * This should be registered as service in AndroidManifest.xml.
 *
 */
public class MessageService extends Service {

  private static final String TAG          = LogUtils.makeTag(MessageService.class);
  private static final String ACTION_START = IntentUtils.makeAction(MessageService.class, "START");

  private SharedPreferences mPrefs;

  private ConnectionManager mConnMgr;

  private ExecutorService mExecutor;
  private TaskSubmitter   mSubmitter;
  private TaskTracker     mTracker;

  static Intent getIntent(Context context) {
    return new Intent(context, MessageService.class);
  }

  public MessageService() {
    mExecutor = Executors.newSingleThreadExecutor();
    mSubmitter = new TaskSubmitter();
    mTracker = new TaskTracker();
  }

  @Override
  public void onCreate() {
    Log.d(TAG, "onCreate: ");
    super.onCreate();

    mPrefs = getSharedPreferences(ClientConstants.PREF_FILE_NAME, MODE_PRIVATE);
//    mConnMgr = new ConnectionManager(this);

    start();
  }

  @Override
  public void onRebind(Intent intent) {
    Log.d(TAG, "onRebind: ");
    super.onRebind(intent);
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    Log.d(TAG, "onBind: ");
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand: ");
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public boolean onUnbind(Intent intent) {
    Log.d(TAG, "onUnbind: ");
    return super.onUnbind(intent);
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy: ");
    super.onDestroy();
  }

  public SharedPreferences getPrefs() {
    return mPrefs;
  }

  public TaskSubmitter getSubmitter() {
    return mSubmitter;
  }

  public TaskTracker getTracker() {
    return mTracker;
  }

  private void start() {
    Log.d(TAG, "start: ");
//    mConnMgr.connect();
  }

  private void stop() {
    Log.d(TAG, "stop: ");
    mExecutor.shutdown();
  }

  /** Inner class for submitting a new runnable task. */
  class TaskSubmitter {

    private TaskSubmitter() {
    }

    public Future submit(Runnable task) {
      Future result = null;

      if (!mExecutor.isTerminated() && !mExecutor.isShutdown() && task != null) {
//        Log.d(TAG, "excutor task: " + task);
        result = mExecutor.submit(task);
      }

      return result;
    }

  }

  /** Inner class for monitoring the running task count. */
  class TaskTracker {

    private int mCount;

    private TaskTracker() {
    }

    public void increase() {
      synchronized (this) {
        Log.d(TAG, "increase: Incremented task count to " + ++mCount);
      }
    }

    public void decrease() {
      synchronized (this) {
        Log.d(TAG, "decrease: Decremented task count to " + --mCount);
      }
    }

  }

}
