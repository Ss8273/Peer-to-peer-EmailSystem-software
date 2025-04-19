package com.app.emailsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.emailsystem.Util.DatabaseHelper;
import com.app.emailsystem.Util.FileUtil;
import com.app.emailsystem.database.Message;
import com.app.emailsystem.network.SocketManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.socket.client.Socket;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

public class WriteActivity extends AppCompatActivity implements View.OnClickListener, SocketManager.MessageListener {
    private EditText et_receiver;
    private EditText et_content;
    private EditText et_subject;
    private String username;
    private String mPath; // 私有目录路径
    private Socket mSocket;
    //private static final String SERVER_URL = "http://10.0.2.2:8080";  // 你的服务器地址

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        // 设置当前活动
        SocketManager.setCurrentActivity(this);

        // 设置 SocketManager 监听器
        SocketManager.setMessageListener(this);

        // 从 SharedPreferences 中读取用户数据
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", null); // 将值赋给类变量 username

        // Initialize UI components
        et_receiver = findViewById(R.id.receiver_username);
        et_content = findViewById(R.id.email_content);
        et_subject = findViewById(R.id.theme);

        // 在界面中显示用户名（例如显示在 TextView 中）
        TextView textView = findViewById(R.id.username);
        if (username != null) {
            textView.setText(username);  // 显示用户名
        } else {
            Toast.makeText(this, "no username", Toast.LENGTH_SHORT).show();
        }

        // 获取 Socket 实例
        mSocket = SocketManager.getSocket();

        // 设置按钮点击事件
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());


        findViewById(R.id.send).setOnClickListener(this);

        if (getIntent().getBooleanExtra("is_external", false)) {
            mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
        } else {
            mPath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
        }

        Log.d("FilePath", "Current download path: " + mPath);
    }

    @Override
    public void onNewMessage(Message message) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(WriteActivity.this)
                    .setTitle("Server Response")
                    .setMessage(message.getSubject())
                    .setPositiveButton("OK", (dialog, which) -> {
                        // 直接关闭页面
                        finish();
                    })
                    .show();
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        // 在 WriteActivity 可见时设置当前活动
        SocketManager.setCurrentActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 可选：当 WriteActivity 不再可见时重置当前活动
        if (SocketManager.getCurrentActivity() == this) {
            SocketManager.setCurrentActivity(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketManager.setMessageListener(null); // 避免内存泄漏
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_cancel) {
            Intent intent = new Intent(WriteActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (v.getId() == R.id.send) {
            String receiver = et_receiver.getText().toString();
            String content = et_content.getText().toString();
            String subject = et_subject.getText().toString();
            String sender = username;

            if (receiver.isEmpty()) {
                Toast.makeText(this, "Receiver cannot be empty!", Toast.LENGTH_SHORT).show();
            } else {
                if (content.isEmpty()) {
                    Toast.makeText(this, "Email should not be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    // 获取当前时间戳
                    String currentTime = formatTimestamp(DateUtil.getNowDateTime(""));
                    Log.d("save email time",currentTime);
                    String file_path = mPath + sender + File.separator + currentTime + File.separator + receiver + File.separator + subject + ".txt";

                    // 获取文件所在的目录路径
                    File fileDir = new File(file_path).getParentFile();

                    // 检查目录是否存在，如果不存在则创建
                    if (!fileDir.exists()) {
                        fileDir.mkdirs();  // 创建所有必要的父目录
                    }
                    // 保存邮件内容到本地
                    FileUtil.saveText(file_path, content);
                    Log.d("fileDir", file_path);

                    // 将邮件信息存储到数据库，初始状态为 3（用户下载的别人的邮件）
                    DatabaseHelper dbHelper = new DatabaseHelper(this);
                    dbHelper.insertEmail(sender, receiver, subject, currentTime, file_path, 4);//default

                    dbHelper.insertContact(receiver);

                    // 发送邮件信息到服务器
                    JSONObject emailData = new JSONObject();
                    try {
                        emailData.put("to", receiver);
                        emailData.put("from", username);
                        emailData.put("message", subject);
                        emailData.put("timeslot", currentTime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // 设置当前邮件信息到 SocketManager
                    SocketManager.setCurrentEmailInfo(username, receiver, subject, currentTime);

                    mSocket.emit("send_message", emailData);



                    Toast.makeText(this, "Email saved to database and sent to server", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static class DateUtil {
        public static String getNowDateTime(String format) {
            if (format.isEmpty()) {
                format = "yyyy-MM-dd_HH-mm";  // 只包含年月日和时分
            }
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            return sdf.format(new Date());
        }
    }

    private String formatTimestamp(String originalTimestamp) {
        if (originalTimestamp == null || originalTimestamp.isEmpty()) {
            return "";
        }

        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        try {
            Date date = originalFormat.parse(originalTimestamp);
            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            // 如果解析失败，返回原始时间戳
            return originalTimestamp;
        }
    }
}



