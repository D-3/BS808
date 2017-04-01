package ml.that.pigeon.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import ml.that.pigeon.data.MessageContract.MessageEntry;

public class MessageProvider extends ContentProvider {

  private static final int MESSAGE_DIR  = 0;
  private static final int MESSAGE_ITEM = 1;

  private static UriMatcher sMatcher;

  private MessageDbHelper mDbHelper;

  static {
    sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    sMatcher.addURI("ml.that.pigeon", MessageContract.PATH_MESSAGE, MESSAGE_DIR);
    sMatcher.addURI("ml.that.pigeon", MessageContract.PATH_MESSAGE + "/#", MESSAGE_ITEM);
  }

  @Override
  public boolean onCreate() {
    mDbHelper = new MessageDbHelper(getContext());
    return true;
  }

  @Nullable
  @Override
  public Uri insert(Uri uri, ContentValues values) {
    SQLiteDatabase db = mDbHelper.getWritableDatabase();
    Uri returnUri = null;
    switch (sMatcher.match(uri)) {
      case MESSAGE_DIR:
      case MESSAGE_ITEM:
        long id = db.insert(MessageEntry.TABLE_NAME, null, values);
        returnUri = Uri.parse(MessageContract.CONTENT_AUTHORITY
                              + "/" + MessageContract.PATH_MESSAGE
                              + "/" + id);
        break;
      default:
    }
    db.close();
    return returnUri;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    SQLiteDatabase db = mDbHelper.getWritableDatabase();
    int deletedRows = 0;
    switch (sMatcher.match(uri)) {
      case MESSAGE_DIR:
        deletedRows = db.delete(MessageContract.PATH_MESSAGE, selection, selectionArgs);
        break;
      case MESSAGE_ITEM:
        String id = uri.getPathSegments().get(1);
        deletedRows = db.delete(MessageContract.PATH_MESSAGE, "id = ?", new String[]{ id });
        break;
      default:
    }
    db.close();
    return deletedRows;
  }

  @Nullable
  @Override
  public Cursor query(Uri uri,
                      String[] projection,
                      String selection,
                      String[] selectionArgs,
                      String sortOrder) {
    SQLiteDatabase db = mDbHelper.getReadableDatabase();
    Cursor c = null;
    switch (sMatcher.match(uri)) {
      case MESSAGE_DIR:
        c = db.query(MessageEntry.TABLE_NAME,
                     projection,
                     selection,
                     selectionArgs,
                     null,
                     null,
                     sortOrder);
        break;
      case MESSAGE_ITEM:
        String id = uri.getPathSegments().get(1);
        c = db.query(MessageEntry.TABLE_NAME,
                     projection,
                     "id = ?",
                     new String[]{ id },
                     null,
                     null,
                     sortOrder);
        break;
      default:
    }
    db.close();
    return c;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    SQLiteDatabase db = mDbHelper.getWritableDatabase();
    int updatedRows = 0;
    switch (sMatcher.match(uri)) {
      case MESSAGE_DIR:
        updatedRows = db.update(MessageContract.PATH_MESSAGE, values, selection, selectionArgs);
        break;
      case MESSAGE_ITEM:
        String id = uri.getPathSegments().get(1);
        updatedRows = db.update(MessageContract.PATH_MESSAGE, values, "id = ?", new String[]{ id });
        break;
      default:
    }
    db.close();
    return updatedRows;
  }

  @Nullable
  @Override
  public String getType(Uri uri) {
    switch (sMatcher.match(uri)) {
      case MESSAGE_DIR:
        return "vnd.android.cursor.dir/vnd.ml.that.pigeon." + MessageContract.PATH_MESSAGE;
      case MESSAGE_ITEM:
        return "vnd.android.cursor.item/vnd.ml.that.pigeon." + MessageContract.PATH_MESSAGE;
      default:
        return null;
    }
  }

}
