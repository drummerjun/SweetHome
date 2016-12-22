package com.evsp.sweethome.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LogHelper extends SQLiteOpenHelper {
    private static final String TAG = LogHelper.class.getSimpleName();
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "homelog.db";
    public static final String TABLE_NAME = "homelog";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ORIGIN = "origin";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_INCOMING = "isincoming";

    public static final int INDEX_ID = 0;
    public static final int INDEX_ORIGIN = INDEX_ID + 1;
    public static final int INDEX_MESSAGE = INDEX_ORIGIN + 1;
    public static final int INDEX_TIME = INDEX_MESSAGE + 1;
    public static final int INDEX_INCOMING = INDEX_TIME + 1;

    private static final String DB_CREATE = "create table " + TABLE_NAME
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_ORIGIN + " text not null, "
            + COLUMN_MESSAGE + " text not null, "
            + COLUMN_TIME + " text not null, "
            + COLUMN_INCOMING + " integer);";

    public LogHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version "
                + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
