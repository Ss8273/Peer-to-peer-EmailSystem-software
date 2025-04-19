package com.app.emailsystem.database;

import android.content.ContentValues;
import android.content.Context;
import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.app.emailsystem.Util.MessageDatabaseHelper;

import java.util.ArrayList;
/*
public class MessageRepository {
    private static MessageDatabase database;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void initialize(Context context) {
        database = MessageDatabase.getInstance(context);
    }

    public static void insertMessage(Message message) {
        executor.execute(() -> database.messageDao().insert(message));
    }

    public static List<Message> getAllMessages() {
        return database.messageDao().getAllMessages();
    }
}

 */

/*
public class MessageRepository {
    private static MessageDatabaseHelper dbHelper;

    public MessageRepository(Context context) {
        dbHelper = new MessageDatabaseHelper(context);
    }

    public static void insertMessage(Message message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MessageDatabaseHelper.COLUMN_SENDER, message.getSender());
        values.put(MessageDatabaseHelper.COLUMN_RECEIVER, message.getReceiver());
        values.put(MessageDatabaseHelper.COLUMN_SUBJECT, message.getSubject());
        values.put(MessageDatabaseHelper.COLUMN_TIME, message.getTime());
        db.insert(MessageDatabaseHelper.TABLE_MESSAGES, null, values);
        db.close();
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                MessageDatabaseHelper.TABLE_MESSAGES,
                new String[]{
                        MessageDatabaseHelper.COLUMN_ID,
                        MessageDatabaseHelper.COLUMN_SENDER,
                        MessageDatabaseHelper.COLUMN_RECEIVER,
                        MessageDatabaseHelper.COLUMN_SUBJECT,
                        MessageDatabaseHelper.COLUMN_TIME
                },
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_ID);
                int senderIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_SENDER);
                int receiverIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_RECEIVER);
                int subjectIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_SUBJECT);
                int timeIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_SUBJECT);

                int id = idIndex!= -1 ? cursor.getInt(idIndex) : null;
                String sender = senderIndex != -1 ? cursor.getString(senderIndex) : null;
                String receiver = receiverIndex != -1 ? cursor.getString(receiverIndex) : null;
                String subject = subjectIndex != -1 ? cursor.getString(subjectIndex) : null;
                String time = timeIndex != -1 ? cursor.getString(timeIndex) : null;
                messages.add(new Message(sender, receiver, subject, time));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messages;
    }

    public void deleteMessage(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(MessageDatabaseHelper.TABLE_MESSAGES, MessageDatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateMessage(Message message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MessageDatabaseHelper.COLUMN_SENDER, message.getSender());
        values.put(MessageDatabaseHelper.COLUMN_RECEIVER, message.getReceiver());
        values.put(MessageDatabaseHelper.COLUMN_SUBJECT, message.getSubject());
        values.put(MessageDatabaseHelper.COLUMN_TIME, message.getTime());
        db.update(MessageDatabaseHelper.TABLE_MESSAGES, values, MessageDatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(message.getId())});
        db.close();
    }
}

 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;

public class MessageRepository {
    private MessageDatabaseHelper dbHelper;

    public MessageRepository(Context context) {
        dbHelper = new MessageDatabaseHelper(context);
    }

    public void insertMessage(Message message) {
        if (dbHelper == null) {
            throw new IllegalStateException("Database helper is not initialized");
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MessageDatabaseHelper.COLUMN_SENDER, message.getSender());
        values.put(MessageDatabaseHelper.COLUMN_RECEIVER, message.getReceiver());
        values.put(MessageDatabaseHelper.COLUMN_SUBJECT, message.getSubject());
        values.put(MessageDatabaseHelper.COLUMN_TIME, message.getTime());
        db.insert(MessageDatabaseHelper.TABLE_MESSAGES, null, values);
        db.close();
    }
/*
    public List<Message> getAllMessages() {
        if (dbHelper == null) {
            throw new IllegalStateException("Database helper is not initialized");
        }
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                MessageDatabaseHelper.TABLE_MESSAGES,
                new String[]{
                        MessageDatabaseHelper.COLUMN_ID,
                        MessageDatabaseHelper.COLUMN_SENDER,
                        MessageDatabaseHelper.COLUMN_RECEIVER,
                        MessageDatabaseHelper.COLUMN_SUBJECT,
                        MessageDatabaseHelper.COLUMN_TIME
                },
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_ID);
                int senderIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_SENDER);
                int receiverIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_RECEIVER);
                int subjectIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_SUBJECT);
                int timeIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_TIME);

                int id = idIndex != -1 ? cursor.getInt(idIndex) : null;
                String sender = senderIndex != -1 ? cursor.getString(senderIndex) : null;
                String receiver = receiverIndex != -1 ? cursor.getString(receiverIndex) : null;
                String subject = subjectIndex != -1 ? cursor.getString(subjectIndex) : null;
                String time = timeIndex != -1 ? cursor.getString(timeIndex) : null;
                messages.add(new Message(sender, receiver, subject, time));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return messages;
    }


 */
public List<Message> getMessagesByReceiver(String receiver) {
    if (dbHelper == null) {
        throw new IllegalStateException("Database helper is not initialized");
    }

    // 创建一个列表来存储查询结果
    List<Message> messages = new ArrayList<>();

    // 获取可读的数据库实例
    SQLiteDatabase db = dbHelper.getReadableDatabase();

    // 查询条件：根据接收者 (receiver) 过滤
    String selection = MessageDatabaseHelper.COLUMN_RECEIVER + " = ?";
    String[] selectionArgs = { receiver };

    // 执行查询
    Cursor cursor = db.query(
            MessageDatabaseHelper.TABLE_MESSAGES,
            new String[]{
                    MessageDatabaseHelper.COLUMN_ID,
                    MessageDatabaseHelper.COLUMN_SENDER,
                    MessageDatabaseHelper.COLUMN_RECEIVER,
                    MessageDatabaseHelper.COLUMN_SUBJECT,
                    MessageDatabaseHelper.COLUMN_TIME
            },
            selection, // 传入查询条件
            selectionArgs, // 传入查询条件的参数
            null,
            null,
            null
    );

    // 处理查询结果
    if (cursor.moveToFirst()) {
        do {
            // 获取各列的索引
            int idIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_ID);
            int senderIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_SENDER);
            int receiverIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_RECEIVER);
            int subjectIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_SUBJECT);
            int timeIndex = cursor.getColumnIndex(MessageDatabaseHelper.COLUMN_TIME);

            // 读取数据
            int id = idIndex != -1 ? cursor.getInt(idIndex) : -1;
            String sender = senderIndex != -1 ? cursor.getString(senderIndex) : null;
            String receiverFromDb = receiverIndex != -1 ? cursor.getString(receiverIndex) : null;
            String subject = subjectIndex != -1 ? cursor.getString(subjectIndex) : null;
            String time = timeIndex != -1 ? cursor.getString(timeIndex) : null;

            // 将读取到的消息数据添加到列表中
            messages.add(new Message(sender, receiverFromDb, subject, time));
        } while (cursor.moveToNext());
    }

    Log.d("Message length",String.valueOf(messages.size()));
    // 关闭资源
    cursor.close();
    db.close();

    // 返回查询结果
    return messages;
}

    public void deleteMessage(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("messages", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

}