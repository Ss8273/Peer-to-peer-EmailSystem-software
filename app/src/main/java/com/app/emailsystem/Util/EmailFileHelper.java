package com.app.emailsystem.Util;

import static android.content.Intent.getIntent;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.app.emailsystem.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class EmailFileHelper {
    public static String getFilePath(String sender, String receiver, String time, String subject) {
        // 获取应用私有存储的 Downloads 目录
        File baseDir = MyApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (baseDir == null) {
            Log.e("EmailFileHelper", "❌ 无法获取私有目录路径");
            return null;
        }

        // 拼接完整路径：mPath/sender/time/receiver/subject.txt
        File file = new File(baseDir, sender + File.separator + time + File.separator + receiver + File.separator + subject + ".txt");

        String path = file.getAbsolutePath();
        Log.d("email detail filepath", path);
        return path;
    }
/*
    public static void saveContentToFile(Context context, String filePath, String content) {
        try {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();

            Toast.makeText(context, "邮件下载成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

 */

    public static void saveContentToFile(Context context, String filePath, String content) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs(); // 确保父文件夹存在
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.close();

            // ✅ 在主线程显示 Toast
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(context, "Email saved successfully", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String readFileContent(File file) {
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                content.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "⚠ Error reading file: " + e.getMessage();
        }
        return content.toString();
    }

}
