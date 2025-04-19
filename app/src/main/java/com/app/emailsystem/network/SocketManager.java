package com.app.emailsystem.network;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.util.Log;
import android.app.AlertDialog;
import com.app.emailsystem.Util.DatabaseHelper;
import com.app.emailsystem.WriteActivity;
import com.app.emailsystem.database.Message;
import com.app.emailsystem.database.MessageRepository;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.Polling;
import io.socket.engineio.client.transports.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;

public class SocketManager {
    private static Socket mSocket;
    //private static final String SERVER_URL = "http://172.17.22.24:8080";
    // 真机需要连接真实的服务器地址
    //private static final String SERVER_URL = "http://172.17.22.24:8080";
    // 本地模拟
    //private static final String SERVER_URL = "http://10.0.2.2:8080";
    // 阿里云服务器
    private static final String SERVER_URL = "http://47.96.106.242:8080";
    private static String username;  // 当前用户名
    private static MessageListener messageListener;  // 监听消息的回调
    private static Activity currentActivity; // 软件当前的页面
    private static MessageRepository messageRepository; // 添加 MessageRepository 实例
    private static String currentSender;
    private static String currentReceiver;
    private static String currentSubject;
    private static String currentTime;


    public static void setCurrentEmailInfo(String sender, String receiver, String subject, String time) {
        currentSender = sender;
        currentReceiver = receiver;
        currentSubject = subject;
        currentTime = time;
    }

    public static String getUsernameFromSharedPrefs(Activity activity) {
        // 从 SharedPreferences 获取用户名
        SharedPreferences sharedPreferences = activity.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("username", ""); // 如果没有用户名则返回空字符串
    }



    public static String getCurrentSender() {
        return currentSender;
    }

    public static String getCurrentReceiver() {
        return currentReceiver;
    }

    public static String getCurrentSubject() {
        return currentSubject;
    }

    public static String getCurrentTime() {
        return currentTime;
    }

    // 设置当前活动
    public static void setCurrentActivity(Activity activity) {
        currentActivity = activity;
        // 初始化 MessageRepository
        if (messageRepository == null && currentActivity != null) {
            messageRepository = new MessageRepository(currentActivity.getApplicationContext());
        }
    }

    // 获取当前活动
    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    // 设置监听器
    public static void setMessageListener(MessageListener listener) {
        messageListener = listener;
    }

    public static Socket getSocket() {
        if (mSocket == null) {
            try {
                IO.Options options = new IO.Options();
                options.transports = new String[]{Polling.NAME, WebSocket.NAME};
                options.reconnection = true;
                options.forceNew = true;

                mSocket = IO.socket(SERVER_URL, options);

                // 监听服务器消息
                mSocket.on("message", args -> {
                    if (args.length > 0) {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            String sender = json.getString("sender");
                            String content = json.getString("subject");  // 从 subject 中获取内容
                            String timestamp = json.getString("time");     // 从 time 中获取时间
                            Log.d("message", json.toString());
                            username = getUsernameFromSharedPrefs(currentActivity);
                            // 假设 username 是当前用户的用户名
                            Message message = new Message(sender, username, content, timestamp);

                            // 存入数据库
                            if (messageRepository != null) {
                                messageRepository.insertMessage(message);
                            } else {
                                Log.e("SocketManager", "MessageRepository is not initialized");
                            }

                            // 通知 UI 层有新消息
                            if (messageListener != null) {
                                messageListener.onNewMessage(message);
                            }

                        } catch (JSONException e) {
                            Log.e("SocketManager", "Error parsing message JSON", e);
                        }
                    }
                });
/*
                // 监听 "send_message_response" 事件
                mSocket.on("send_message_response", args -> {
                    if (args.length > 0) {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            String status = json.getString("status");
                            String responseMessage = json.getString("message");

                            // 仅在 WriteActivity 中弹出提示
                            if (getCurrentActivity() instanceof WriteActivity) {
                                // 确保在 UI 线程中执行
                                ((WriteActivity) getCurrentActivity()).runOnUiThread(() -> {
                                    new AlertDialog.Builder(getCurrentActivity())
                                            .setTitle("Message Send Response")
                                            .setMessage(responseMessage)
                                            .setPositiveButton("OK", null)
                                            .show();
                                });
                            }

                        } catch (JSONException e) {
                            Log.e("SocketManager", "Error parsing send_message_response JSON", e);
                        }
                    }
                });

 */
                // 监听 "send_message_response" 事件
                mSocket.on("send_message_response", args -> {
                    if (args.length > 0) {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            String status = json.getString("status");
                            String responseMessage = json.getString("message");
                            Log.d("send_message_response",responseMessage);

                            // 仅在 WriteActivity 中弹出提示
                            if (getCurrentActivity() instanceof WriteActivity) {
                                // 确保在 UI 线程中执行
                                ((WriteActivity) getCurrentActivity()).runOnUiThread(() -> {
                                    new AlertDialog.Builder(getCurrentActivity())
                                            .setTitle("Message Send Response")
                                            .setMessage(responseMessage)
                                            .setPositiveButton("OK", null)
                                            .show();

                                    // 根据服务器响应更新邮件状态
                                    if ("success".equals(status)) {
                                        // 更新数据库中的邮件状态为 1（发送成功）
                                        DatabaseHelper dbHelper = new DatabaseHelper(getCurrentActivity());
                                        dbHelper.updateEmailStatus(currentSender, currentReceiver, currentSubject, currentTime, 1);
                                        Log.d("update database:","status change from 4 to 1");
                                    } else if ("error".equals(status)) {
                                        // 更新数据库中的邮件状态为 0（发送失败）
                                        DatabaseHelper dbHelper = new DatabaseHelper(getCurrentActivity());
                                        dbHelper.updateEmailStatus(currentSender, currentReceiver, currentSubject, currentTime, 0);
                                        Log.d("update database:","status change from 4 to 0");
                                    }
                                    /*
                                    else if ("rejected".equals(status)) {
                                        // 更新数据库中的邮件状态为 2（对方拒收）
                                        DatabaseHelper dbHelper = new DatabaseHelper(getCurrentActivity());
                                        dbHelper.updateEmailStatus(currentSender, currentReceiver, currentSubject, currentTime, 2);
                                    }

                                     */
                                });
                            }

                        } catch (JSONException e) {
                            Log.e("SocketManager", "Error parsing send_message_response JSON", e);
                        }
                    }
                });

                // 监听 "message_action" 事件
                mSocket.on("message_action", args -> {
                    if (args.length > 0) {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            String receiver = json.getString("receiver");
                            String action = json.getString("action");
                            String subject = json.getString("subject");
                            String time = json.getString("time");

                            // 处理接收或拒收逻辑
                            handleMessageAction(receiver, action,subject,time);

                        } catch (JSONException e) {
                            Log.e("SocketManager", "Error parsing message_action JSON", e);
                        }
                    }
                });

                mSocket.connect();

            } catch (URISyntaxException e) {
                Log.e("SocketManager", "Socket.IO connection error", e);
            }
        }
        return mSocket;
    }

    public static void disconnectSocket() {
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
        }
    }

    // 增加一个getinstance方法，messageadapter

    private static void handleMessageAction(String receiver, String action,String subject, String time) {
        // 在这里实现接收或拒收的逻辑
        // 例如，发送通知给发送方，建立点对点通道等
        // 实际上这里是需要开始建立连接了
        Log.d("SocketManager", "Message action received: " + action + " from " + receiver);
        if ("accpet".equals(action)) {
            // 更新数据库中的邮件状态为 4（对方接收）
            DatabaseHelper dbHelper = new DatabaseHelper(getCurrentActivity());
            dbHelper.updateEmailStatus(username, receiver, subject, time, 3);
            Log.d("update database:","status change from 4 to 1");
        } else if ("reject".equals(action)) {
            // 更新数据库中的邮件状态为 2（对方拒收）
            DatabaseHelper dbHelper = new DatabaseHelper(getCurrentActivity());
            dbHelper.updateEmailStatus(username, receiver, subject, time, 2);
            Log.d("update database:","status change from 4 to 2");
        }
        // 你可以在这里添加代码，通过服务器通知发送方建立点对点通道
        // 例如：
        // SocketManager.getSocket().emit("notify_sender", json);
    }


    public static boolean isSocketConnected() {
        return mSocket != null && mSocket.connected();
    }

    public static void setUsername(String currentUsername) {
        username = currentUsername;
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit("reconnect", username);
        }
    }

    public static String getUsername() {
        return username;
    }

    // 定义回调接口
    public interface MessageListener {
        void onNewMessage(Message message);
    }
}
