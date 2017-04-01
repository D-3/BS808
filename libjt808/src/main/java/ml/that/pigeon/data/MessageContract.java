package ml.that.pigeon.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MessageContract {

  public static final String CONTENT_AUTHORITY = "ml.that.pigeon";
  public static final Uri    BASE_CONTENT_URI  = Uri.parse("content://" + CONTENT_AUTHORITY);
  public static final String PATH_MESSAGE      = "message";

  public static final class MessageEntry implements BaseColumns {

    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                                                          .appendPath(PATH_MESSAGE)
                                                          .build();

    public static final String CONTENT_DIR_TYPE  = ContentResolver.CURSOR_DIR_BASE_TYPE
                                                   + "/" + CONTENT_AUTHORITY
                                                   + "/" + PATH_MESSAGE;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                                                   + "/" + CONTENT_AUTHORITY
                                                   + "/" + PATH_MESSAGE;

    public static final String TABLE_NAME = PATH_MESSAGE;

    public static final String COLUMN_MESSAGE_ID   = "msg_id";
    public static final String COLUMN_PHONE_NUMBER = "phone";
    public static final String COLUMN_MESSAGE_BODY = "msg_body";
    public static final String COLUMN_UPLOADED     = "uploaded";

    public static Uri buildUri(long id) {
      return ContentUris.withAppendedId(CONTENT_URI, id);
    }

  }

}
