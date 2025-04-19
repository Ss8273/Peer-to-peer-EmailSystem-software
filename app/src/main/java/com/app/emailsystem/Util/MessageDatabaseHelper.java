package com.app.emailsystem.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
public class MessageDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "messages.db";
    private static final int DATABASE_VERSION = 1;

    // 定义表名和列名
    public static final String TABLE_NAME = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FROM = "from";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public MessageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FROM + " TEXT NOT NULL,"
                + COLUMN_MESSAGE + " TEXT NOT NULL,"
                + COLUMN_TIMESTAMP + " INTEGER NOT NULL" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    // 插入消息的方法
    public void insertMessage(String from, String message) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FROM, from);
        values.put(COLUMN_MESSAGE, message);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }
    *
 */

    /*
    // 查询消息的方法
    public Cursor getMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, COLUMN_TIMESTAMP + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String from = cursor.getString(cursor.getColumnIndex(COLUMN_FROM));
                String message = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE));
                long timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP));
                // 处理消息
            } while(cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return null;
    }

     */

/*
    public Cursor getMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, COLUMN_TIMESTAMP + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            int fromColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_FROM); // 获取 "from" 列的索引，如果列不存在会抛出异常
            int messageColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_MESSAGE); // 获取 "message" 列的索引，如果列不存在会抛出异常
            int timestampColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP); // 获取 "timestamp" 列的索引，如果列不存在会抛出异常

            do {
                String from = cursor.getString(fromColumnIndex);
                String message = cursor.getString(messageColumnIndex);
                long timestamp = cursor.getLong(timestampColumnIndex);
                // 处理消息
            } while(cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
}

*/


import java.util.ArrayList;
import java.util.List;

/*
public class MessageDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "messages.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FROM = "from";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public MessageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FROM + " TEXT NOT NULL,"
                + COLUMN_MESSAGE + " TEXT NOT NULL,"
                + COLUMN_TIMESTAMP + " INTEGER NOT NULL" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    public void insertMessage(String from, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FROM, from);
        values.put(COLUMN_MESSAGE, message);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public Cursor getMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, COLUMN_TIMESTAMP + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            int fromColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_FROM); // 获取 "from" 列的索引，如果列不存在会抛出异常
            int messageColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_MESSAGE); // 获取 "message" 列的索引，如果列不存在会抛出异常
            int timestampColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP); // 获取 "timestamp" 列的索引，如果列不存在会抛出异常

            do {
                String from = cursor.getString(fromColumnIndex);
                String message = cursor.getString(messageColumnIndex);
                long timestamp = cursor.getLong(timestampColumnIndex);
                // 处理消息
            } while(cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
}

 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "email_system.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_RECEIVER = "receiver";
    public static final String COLUMN_SUBJECT = "subject";
    public static final String COLUMN_TIME = "time";

    public MessageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SENDER + " TEXT,"
                + COLUMN_RECEIVER + " TEXT,"
                + COLUMN_SUBJECT + " TEXT,"
                + COLUMN_TIME + " TEXT" + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }
    public void insertMessage(String from, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RECEIVER, from);
        values.put(COLUMN_SUBJECT, message);
        values.put(COLUMN_TIME, System.currentTimeMillis());
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }
}