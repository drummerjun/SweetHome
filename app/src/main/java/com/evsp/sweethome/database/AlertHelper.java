package com.evsp.sweethome.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AlertHelper extends SQLiteOpenHelper {
    private static final String TAG = AlertHelper.class.getSimpleName();
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "alerts.db";
    public static final String TABLE_NAME = "alerts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DID = "device_id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_MESSAGE = "string";
    public static final String COLUMN_TIME = "time";

    public static final int INDEX_ID = 0;
    public static final int INDEX_DEVICE_ID = INDEX_ID + 1;
    public static final int INDEX_TYPE = INDEX_DEVICE_ID + 1;
    public static final int INDEX_MESSAGE = INDEX_TYPE + 1;
    public static final int INDEX_TIME = INDEX_MESSAGE + 1;

    private static final String DB_CREATE = "create table " + TABLE_NAME
            + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_DID + " text not null, "
            + COLUMN_TYPE + " text not null, "
            + COLUMN_MESSAGE + " text not null, "
            + COLUMN_TIME + " integer);";

    public AlertHelper(Context context) {
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
