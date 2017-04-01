package ml.that.pigeon;

import android.content.Context;

import ml.that.pigeon.util.LogUtils;

/**
 * This class is to manage the message service and to load the configuration.
 *
 * @author That Mr.L (thatmr.l@gmail.com)
 */
public class ServiceManager {

  private static final String TAG = LogUtils.makeTag(ServiceManager.class);

  private Context mContext;

  public ServiceManager(Context ctx) {
    mContext = ctx;
  }

  /** Starts the message service. */
  public void startService() {
    mContext.startService(MessageService.getIntent(mContext));
  }

  /** Stops the message service. */
  public void stopService() {
    mContext.stopService(MessageService.getIntent(mContext));
  }

}
