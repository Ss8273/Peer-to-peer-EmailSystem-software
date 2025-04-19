package com.app.emailsystem.database;


/*
@Entity(tableName = "messages")
public class Message {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String sender;
    public String content;
    public String timestamp;

    public Message(String sender, String content, String timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public String getSender(){
        return sender;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

 */
/*
@Entity(tableName = "messages")
public class Message {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "sender")
    public String sender;

    @ColumnInfo(name = "receiver")
    public String receiver;

    @ColumnInfo(name = "subject")
    public String subject;

    @ColumnInfo(name = "time")
    public String time;

    // 构造函数
    public Message(String sender, String subject, String time, String receiver) {
        this.sender = sender;
        this.subject = subject;
        this.time = time;
        this.receiver = receiver;
    }

    // Getter 方法
    public int getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getSubject() {
        return subject;
    }

    public String getTime() {
        return time;
    }

    // Setter 方法
    public void setId(int id) {
        this.id = id;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

 */
/**用普通的数据库方法存放，放弃orm*/
public class Message {
    private static int nextId = 1; // 静态变量自动计数，初始值为1
    private int id;
    private String sender;
    private String receiver;
    private String subject;
    private String time;

    public Message(int id, String sender, String receiver, String subject, String time) {
        // 确保静态计数器永远大于当前最大的id
        if (id >= nextId) {
            nextId = id + 1;
        }
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.time = time;
    }

    public Message(String sender, String receiver, String subject, String time) {
        this.id = nextId++;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}