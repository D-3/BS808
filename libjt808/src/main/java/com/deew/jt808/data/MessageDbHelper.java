package com.deew.jt808.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageDbHelper extends SQLiteOpenHelper {

  private static final int DATABASE_VERSION = 1;

  private static final String DATABASE_NAME = "message.db";

  public MessageDbHelper(Context ctx) {
    super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    final String SQL_CREATE_MESSAGE_TABLE = "CREATE TABLE " + MessageContract.MessageEntry.TABLE_NAME + " ( "
                                            + MessageContract.MessageEntry._ID + " INTEGER PRIMARY KEY, "
                                            + MessageContract.MessageEntry.COLUMN_MESSAGE_ID + " INTEGER, "
                                            + MessageContract.MessageEntry.COLUMN_PHONE_NUMBER + " BLOB, "
                                            + MessageContract.MessageEntry.COLUMN_MESSAGE_BODY + " BLOB, "
                                            + MessageContract.MessageEntry.COLUMN_UPLOADED + " INTEGER );";
    db.execSQL(SQL_CREATE_MESSAGE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    // TODO: 10/30/2016 implement this method
  }

}
