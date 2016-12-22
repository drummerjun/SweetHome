package com.evsp.sweethome.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.evsp.sweethome.adapters.MessageItem;
import java.io.IOException;
import java.util.ArrayList;

public class LogDataSource {
    private static final String TAG = LogDataSource.class.getSimpleName();
    private SQLiteDatabase db;
    private LogHelper helper;
    private String[] columns = {LogHelper.COLUMN_ID, LogHelper.COLUMN_ORIGIN,
            LogHelper.COLUMN_MESSAGE, LogHelper.COLUMN_TIME,
            LogHelper.COLUMN_INCOMING
    };

    public LogDataSource(Context context) {
        helper = new LogHelper(context);
    }

    public void open() throws IOException {
        db = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }

    public boolean insertLogtoDB(MessageItem item) {
        ContentValues values = new ContentValues();
        values.put(LogHelper.COLUMN_ORIGIN, item.getOrigin());
        values.put(LogHelper.COLUMN_MESSAGE, item.getMessage());
        values.put(LogHelper.COLUMN_TIME, item.getTime());
        values.put(LogHelper.COLUMN_INCOMING, item.getIncoming() == true ? 1 : 0);
        long insertId = db.insert(LogHelper.TABLE_NAME, null, values);
        if(insertId > -1) {
            return true;
        } else {
            return false;
        }
    }

    public int clearLogDB() {
        return db.delete(LogHelper.TABLE_NAME, null, null);
    }

    public ArrayList<MessageItem> loadLogs() {
        ArrayList<MessageItem> itemList = new ArrayList<>();
        Cursor cursor = db.query(LogHelper.TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String origin = cursor.getString(LogHelper.INDEX_ORIGIN);
            String message = cursor.getString(LogHelper.INDEX_MESSAGE);
            String time = cursor.getString(LogHelper.INDEX_TIME);
            int i = cursor.getInt(LogHelper.INDEX_INCOMING);
            boolean isIncoming = false;
            if(i == 1) {
                isIncoming = true;
            }
            MessageItem item = new MessageItem(message, isIncoming, time);
            item.setOrigin(origin);
            itemList.add(item);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return itemList;
    }
}
