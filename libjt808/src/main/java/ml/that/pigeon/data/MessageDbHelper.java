package ml.that.pigeon.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ml.that.pigeon.data.MessageContract.MessageEntry;

public class MessageDbHelper extends SQLiteOpenHelper {

  private static final int DATABASE_VERSION = 1;

  private static final String DATABASE_NAME = "message.db";

  public MessageDbHelper(Context ctx) {
    super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    final String SQL_CREATE_MESSAGE_TABLE = "CREATE TABLE " + MessageEntry.TABLE_NAME + " ( "
                                            + MessageEntry._ID + " INTEGER PRIMARY KEY, "
                                            + MessageEntry.COLUMN_MESSAGE_ID + " INTEGER, "
                                            + MessageEntry.COLUMN_PHONE_NUMBER + " BLOB, "
                                            + MessageEntry.COLUMN_MESSAGE_BODY + " BLOB, "
                                            + MessageEntry.COLUMN_UPLOADED + " INTEGER );";
    db.execSQL(SQL_CREATE_MESSAGE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    // TODO: 10/30/2016 implement this method
  }

}
