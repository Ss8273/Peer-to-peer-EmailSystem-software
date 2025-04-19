package com.app.emailsystem.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.app.emailsystem.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper instance;
    private static final String DATABASE_NAME = "EmailDatabase.db";
    private static final int DATABASE_VERSION = 2;

    // 邮件表
    public static final String TABLE_NAME = "emails";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_RECEIVER = "receiver";
    public static final String COLUMN_THEME = "theme";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_FILE_PATH = "file_path";
    public static final String COLUMN_STATUS = "status"; //


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SENDER + " TEXT,"
                + COLUMN_RECEIVER  + " TEXT,"
                + COLUMN_STATUS + " INTEGER," // 修正这里的拼写错误
                + COLUMN_THEME + " TEXT,"
                + COLUMN_TIME + " TEXT,"
                + COLUMN_FILE_PATH + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);

        String CREATE_CONTACT_TABLE = "CREATE TABLE contacts (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE)";
        db.execSQL(CREATE_CONTACT_TABLE);
        Log.d("create table","contacts");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS contacts (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE)");
        }
    }

    // 更新邮件状态
    public void updateEmailStatus(String sender, String receiver, String subject, String time, int status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", status);

        String selection = "sender = ? AND receiver = ? AND theme = ? AND time = ?";
        String[] selectionArgs = {sender, receiver, subject, time};

        SQLiteDatabase db = this.getWritableDatabase();
        db.update("emails", contentValues, selection, selectionArgs);
        Log.d("DatabaseHelper", "Updating email status: sender=" + sender + ", receiver=" + receiver + ", theme=" + subject + ", time=" + time + ", new status=" + status);
        db.close();
    }

    // 添加联系人
    public void insertContact(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);

        db.insertWithOnConflict("contacts", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        Log.d("insert contact","successfully");
        db.close();
    }

    // 获取所有联系人
    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM contacts", null);

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex("id");
                int id = idIndex != -1 ? cursor.getInt(idIndex) : null;
                int emailIndex = cursor.getColumnIndex("email");
                String email = emailIndex != -1 ? cursor.getString(emailIndex) : null;
                contactList.add(new Contact(id, email));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return contactList;
    }


/*
    // 插入数据的方法
    public void insertEmail(String sender, String receiver, String theme, String time, String filePath, int s) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_RECEIVER, receiver);
        values.put(COLUMN_THEME, theme);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_FILE_PATH, filePath);
        values.put(COLUMN_STATUS,s);

        long id = db.insert(TABLE_NAME, null, values);
        if (id == -1) {
            Log.e("DatabaseHelper", "Failed to insert email data");
        }
        db.close();
    }

 */

    // 插入数据的方法 (避免冲突)
    public void insertEmail(String sender, String receiver, String theme, String time, String filePath, int s) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_RECEIVER, receiver);
        values.put(COLUMN_THEME, theme);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_FILE_PATH, filePath);
        values.put(COLUMN_STATUS, s);

        // 使用insertWithOnConflict，避免插入重复数据
        long id = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        if (id == -1) {
            Log.d("DatabaseHelper", "Email data already exists or conflict occurred, ignoring insert.");
        } else {
            Log.d("DatabaseHelper", "Inserted email data successfully, id: " + id);
        }

        db.close();
    }


    public boolean deleteEmail(Email email) {
        SQLiteDatabase db = getWritableDatabase();
        int deletedRows = db.delete(
                TABLE_NAME,
                COLUMN_THEME + "=? AND " +
                        COLUMN_TIME + "=? AND " +
                        COLUMN_SENDER + "=?",
                new String[]{email.getTheme(), email.getTime(), email.getSender()}
        );
        return deletedRows > 0;
    }


    // 查询所有邮件
    public Cursor getAllEmails() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, null);
    }

    // 查询所有邮件
    public Cursor getEmailsByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_SENDER + " = ?";
        String[] selectionArgs = { username };

        // 查询所有列
        return db.query(
                TABLE_NAME,
                null,  // 查询所有列
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    // 查询下载的邮件
    public Cursor getEmailsByRcvname(String receiver) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_RECEIVER + " = ?";
        String[] selectionArgs = { receiver };

        // 查询所有列
        return db.query(
                TABLE_NAME,
                null,  // 查询所有列
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    public String getFilepath(String sender, String receiver, String time, String subject){
        SQLiteDatabase db = this.getReadableDatabase();

        // 创建查询条件（多个字段进行组合）
        String selection = COLUMN_SENDER + " = ? AND " + COLUMN_RECEIVER + " = ? AND " + COLUMN_TIME + " = ? AND " + COLUMN_THEME + " = ?";
        String[] selectionArgs = { sender, receiver, time, subject };

        // 查询 filepath 列
        String[] columns = { COLUMN_FILE_PATH };
        // 执行查询并返回文件路径
        Cursor cursor = db.query(
                TABLE_NAME,   // 表名
                columns,      // 查询的列，只返回 filepath
                selection,    // 查询条件
                selectionArgs,// 查询条件的参数
                null,         // 不使用 group by
                null,         // 不使用 having
                null          // 不使用 order by
        );
        int filepathIndex = cursor.getColumnIndex(COLUMN_FILE_PATH);
        String filePath = filepathIndex != -1 ? cursor.getString(filepathIndex) : null;

        return filePath;
    }

    public List<Contact> searchContacts(String keyword) {
        List<Contact> resultList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM contacts WHERE email LIKE ?", new String[]{"%" + keyword + "%"});

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex("id");
                int id = idIndex != -1 ? cursor.getInt(idIndex) : null;
                int emailIndex = cursor.getColumnIndex("email");
                String email = emailIndex != -1 ? cursor.getString(emailIndex) : null;
                resultList.add(new Contact(id, email));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return resultList;
    }


}
