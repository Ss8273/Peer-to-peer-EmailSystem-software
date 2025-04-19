package com.app.emailsystem.Util;


import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.app.emailsystem.MyApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Email {
    private String subject;
    private String date;
    private String sender;
    private String receiver;
    private String filepath;
    private String content;
    private static String privatepath = MyApplication.getAppContext().getFilesDir().getAbsolutePath();
    private int status = 4; //0:发送失败(对方用户不在线) 1:发送成功 2:拒收 3:下载的邮件

    // 构造函数
    public Email(String subject, String date, String sender, String receiver, String filepath, int status, String content) {
        this.subject = subject;
        this.date = date;
        this.sender = sender;
        this.receiver = receiver;
        this.filepath = filepath;
        this.content = content;
        this.status = status;
    }
    public Email(String subject, String date, String sender, String receiver){
        // 获取当前系统的内部存储路径mpath
        this.subject = subject;
        this.date = date;
        this.sender = sender;
        this.receiver = receiver;
        //this.filepath = privatepath + sender + File.separator + date + File.separator + receiver + File.separator + subject + ".txt";
    }



    // Getter 和 Setter 方法
    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getTheme() {
        return subject;
    }

    public int getStatus() {
        Log.d("status"+status,"!!!!!!!!!!!!!");
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTime() {
        return date;
    }

    public String getId() {
        return sender;
    }

    public String getContent(){
        StringBuilder fileContent = new StringBuilder();
        try {
            File file = new File(this.filepath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent.toString();
    }
}
