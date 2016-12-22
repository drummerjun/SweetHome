package com.evsp.sweethome.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.evsp.sweethome.adapters.AlertItem;
import java.io.IOException;
import java.util.ArrayList;

public class AlertDataSource {
    private static final String TAG = AlertDataSource.class.getSimpleName();
    private SQLiteDatabase db;
    private AlertHelper helper;
    private String[] columns = {AlertHelper.COLUMN_ID, AlertHelper.COLUMN_DID,
            AlertHelper.COLUMN_TYPE, AlertHelper.COLUMN_MESSAGE,
            AlertHelper.COLUMN_TIME
    };

    public AlertDataSource(Context context) {
        helper = new AlertHelper(context);
    }

    public void open() throws IOException {
        db = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }

    public boolean insertLogtoDB(AlertItem item) {
        ContentValues values = new ContentValues();
        values.put(AlertHelper.COLUMN_DID, item.getId());
        values.put(AlertHelper.COLUMN_TYPE, item.getType());
        values.put(AlertHelper.COLUMN_MESSAGE, item.getMessage());
        values.put(AlertHelper.COLUMN_TIME, item.getTime());
        long insertId = db.insert(AlertHelper.TABLE_NAME, null, values);
        if(insertId > -1) {
            return true;
        } else {
            return false;
        }
    }

    public int clearLogDB() {
        return db.delete(AlertHelper.TABLE_NAME, null, null);
    }

    public ArrayList<AlertItem> loadLogs() {
        ArrayList<AlertItem> itemList = new ArrayList<>();
        Cursor cursor = db.query(AlertHelper.TABLE_NAME, columns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(AlertHelper.INDEX_DEVICE_ID);
            String type = cursor.getString(AlertHelper.INDEX_TYPE);
            String message = cursor.getString(AlertHelper.INDEX_MESSAGE);
            String time = cursor.getString(AlertHelper.INDEX_TIME);
            AlertItem item = new AlertItem(id, type, message, time);
            itemList.add(item);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return itemList;
    }
}
