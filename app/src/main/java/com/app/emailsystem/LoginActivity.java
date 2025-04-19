package com.app.emailsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.app.emailsystem.network.SocketManager;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.Polling;
import io.socket.engineio.client.transports.WebSocket;

import java.net.URI;

/*
public class LoginActivity extends AppCompatActivity {

    private EditText et_username;
    private EditText et_password;
    private Handler mHandler;
    private boolean loginCompleted = false;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化控件
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);

        // 初始化 Handler
        mHandler = new Handler(Looper.getMainLooper());

        // 获取 Socket 实例
        mSocket = SocketManager.getSocket(); // 初始时传 null，之后登录时会更新用户名

        // 监听服务器的登录响应
        mSocket.on("login_response", args -> {
            if (args.length > 0) {
                JSONObject response = (JSONObject) args[0];
                handleLoginResponse(response);
            }
        });

        // 处理注册按钮点击
        findViewById(R.id.register).setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 处理登录按钮点击
        findViewById(R.id.login).setOnClickListener(view -> {
            String username = et_username.getText().toString();
            String password = et_password.getText().toString();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "请输入用户名或者密码", Toast.LENGTH_SHORT).show();
            } else {
                login(username, password);
            }
        });
    }

    // 发送登录请求
    private void login(String username, String password) {
        // 更新 SocketManager 中的用户名
        SocketManager.setUsername(username);  // 更新用户名

        JSONObject loginData = new JSONObject();
        try {
            loginData.put("type", "login");
            loginData.put("username", username);
            loginData.put("password", password);
            loginCompleted = false;

            // 获取新的 Socket 实例，传入已设置的用户名
            mSocket = SocketManager.getSocket(); // 确保 socket 使用最新的用户名

            // 发送请求并监听回调
            mSocket.emit("login", loginData, (Ack) args -> {
                if (args.length > 0) {
                    JSONObject response = (JSONObject) args[0];
                    handleLoginResponse(response);
                }
            });

            // 设定超时检测
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!loginCompleted && !SocketManager.isSocketConnected()) { // 若超时
                    Log.e("Socket", "Login timeout");
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "登录超时，请检查网络连接", Toast.LENGTH_SHORT).show());
                }
            }, 5000);

        } catch (Exception e) {
            Log.e("Socket", "Login request error: ", e);
            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "登录请求失败", Toast.LENGTH_SHORT).show());
        }
    }

    // 处理服务器的登录响应
    private void handleLoginResponse(JSONObject response) {
        loginCompleted = true;
        Log.d("Socket", "Received response: " + response.toString());

        String status = response.optString("status");
        String message = response.optString("message");

        runOnUiThread(() -> {
            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            if ("success".equals(status)) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 断开 Socket 连接
        SocketManager.disconnectSocket();
    }
}
*/
public class LoginActivity extends AppCompatActivity {

    private EditText et_username;
    private EditText et_password;
    private Handler mHandler;
    private boolean loginCompleted = false;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SocketManager.setCurrentActivity(this);

        // 初始化控件
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        // 保存数据到 SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // 初始化 Handler
        mHandler = new Handler(Looper.getMainLooper());

        // 获取 Socket 实例
        mSocket = SocketManager.getSocket();

        // 监听服务器的登录响应
        mSocket.on("login_response", args -> {
            if (args.length > 0) {
                JSONObject response = (JSONObject) args[0];
                handleLoginResponse(response);
            }
        });

        // 处理注册按钮点击
        findViewById(R.id.register).setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 处理登录按钮点击
        findViewById(R.id.login).setOnClickListener(view -> {
            String username = et_username.getText().toString();
            String password = et_password.getText().toString();

            //将username存储到shareoreferences中
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", username);
            editor.apply();//异步保存


            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Please enter your email address or password", Toast.LENGTH_SHORT).show();
            } else {
                login(username, password);
            }
        });
    }

    // 发送登录请求
    private void login(String username, String password) {
        JSONObject loginData = new JSONObject();
        try {
            loginData.put("type", "login");
            loginData.put("username", username);
            loginData.put("password", password);
            loginCompleted = false;

            // 发送请求并监听回调
            mSocket.emit("login", loginData, (Ack) args -> {
                if (args.length > 0) {
                    JSONObject response = (JSONObject) args[0];
                    handleLoginResponse(response);
                }
            });

            // 设定超时检测
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!loginCompleted && !SocketManager.isSocketConnected()) { // 若超时
                    Log.e("Socket", "Login timeout");
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login timeout, please check network connection", Toast.LENGTH_SHORT).show());
                }
            }, 5000);

        } catch (Exception e) {
            Log.e("Socket", "Login request error: ", e);
            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login request failed", Toast.LENGTH_SHORT).show());
        }
    }
/*
    // 处理服务器的登录响应
    private void handleLoginResponse(JSONObject response) {
        loginCompleted = true;
        Log.d("Socket", "Received response: " + response.toString());

        String status = response.optString("status");
        String message = response.optString("message");

        runOnUiThread(() -> {
            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            if ("success".equals(status)) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

 */
    //增加接收message的代码
private void handleLoginResponse(JSONObject response) {
    loginCompleted = true;
    Log.d("Socket", "Received response: " + response.toString());

    String status = response.optString("status");
    String message = response.optString("message");

    runOnUiThread(() -> {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
        if ("success".equals(status)) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            //startService(new Intent(LoginActivity.this, MessageService.class)); // 启动MessageService
            finish();
        }
    });
}
/*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 断开 Socket 连接
        SocketManager.disconnectSocket();
    }
    */

}


/*
public class LoginActivity extends AppCompatActivity {

    private static final String SERVER_URL = "http://10.0.2.2:8080"; // 服务器URL
    private EditText et_username;
    private EditText et_password;
    private Handler mHandler; // 用于更新 UI 的 Handler
    private boolean loginCompleted = false; // 标记是否完成登录
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化控件
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);

        // 初始化 Handler
        mHandler = new Handler(Looper.getMainLooper());

        // 连接到 Socket.io 服务器
        try {
            IO.Options opts = new IO.Options();
            opts.transports = new String[]{Polling.NAME, WebSocket.NAME};
            opts.reconnection = true;
            opts.forceNew = true;
            opts.timeout = 5000;
            mSocket = IO.socket(new URI(SERVER_URL));
            mSocket.connect();

            // 监听连接状态
            mSocket.on(Socket.EVENT_CONNECT, args -> Log.d("Socket", "Connected to server: " + SERVER_URL));
            mSocket.on(Socket.EVENT_CONNECT_ERROR, args -> Log.e("Socket", "Connection failed: ", (Throwable) args[0]));
            mSocket.on(Socket.EVENT_DISCONNECT, args -> Log.e("Socket", "Connection lost"));

        } catch (Exception e) {
            Log.e("Socket", "Connection error: ", e);
        }

        // 监听服务器的登录响应
        mSocket.on("login_response", args -> {
            if (args.length > 0) {
                JSONObject response = (JSONObject) args[0];
                handleLoginResponse(response);
            }
        });

        // 处理注册按钮点击
        findViewById(R.id.register).setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 处理登录按钮点击
        findViewById(R.id.login).setOnClickListener(view -> {
            String username = et_username.getText().toString();
            String password = et_password.getText().toString();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "请输入用户名或者密码", Toast.LENGTH_SHORT).show();
            } else {
                login(username, password);
            }
        });
    }

    // 发送登录请求
    private void login(String username, String password) {
        JSONObject loginData = new JSONObject();
        try {
            loginData.put("type", "login");
            loginData.put("username", username);
            loginData.put("password", password);
            loginCompleted = false;

            // 发送请求并监听回调
            mSocket.emit("login", loginData, (Ack) args -> {
                if (args.length > 0) {
                    JSONObject response = (JSONObject) args[0];
                    handleLoginResponse(response);
                }
            });

            // 设定超时检测
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!loginCompleted) { // 若超时
                    Log.e("Socket", "Login timeout");
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "登录超时，请检查网络连接", Toast.LENGTH_SHORT).show());
                }
            }, 5000);

        } catch (Exception e) {
            Log.e("Socket", "Login request error: ", e);
            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "登录请求失败", Toast.LENGTH_SHORT).show());
        }
    }

    // 处理服务器的登录响应
    private void handleLoginResponse(JSONObject response) {
        loginCompleted = true;
        Log.d("Socket", "Received response: " + response.toString());

        String status = response.optString("status");
        String message = response.optString("message");

        runOnUiThread(() -> {
            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            if ("success".equals(status)) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
        }
    }
}

/*
public class LoginActivity extends AppCompatActivity {

    private static final String SERVER_URL = "http://10.0.2.2:8080"; // 服务器URL
    private EditText et_username;
    private EditText et_password;
    private Handler mHandler; // 用于更新 UI 的 Handler
    public String username;

    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化控件
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);

        // 初始化 Handler
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // 在主线程中处理消息
                String result = (String) msg.obj;

                if(result.equals("Login successful: Login successful!")){
                    Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "Wrong account or password", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // 连接到 Socket.io 服务器
        try {
            IO.Options opts = new IO.Options();
            //opts.transports = new String[]{WebSocket.NAME};  // 指定仅使用 WebSocket 传输方式
            opts.transports = new String[]{Polling.NAME, WebSocket.NAME};
            opts.reconnection = true;  // 允许自动重连
            opts.forceNew = true;  // 每次都创建新的连接
            opts.timeout = 5000;   // 5 秒超时
            mSocket = IO.socket(new URI(SERVER_URL));
            mSocket.connect();

            // 监听连接状态
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("Socket", "success connect to server: " + SERVER_URL);
                }
            });

            mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("Socket", "connection false: ", (Throwable) args[0]);
                }
            });

            mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e("Socket", "connect break");
                }
            });

        } catch (Exception e) {
            Log.e("Socket", "exception of connection: ", e);
        }

        // 监听服务器的登录响应
        mSocket.on("login_response", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    String result = (String) args[0];
                    Message msg = mHandler.obtainMessage();
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                }
            }
        });

        // 处理注册按钮点击
        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 处理登录按钮点击
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = et_username.getText().toString();
                String password = et_password.getText().toString();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "请输入用户名或者密码", Toast.LENGTH_SHORT).show();
                } else {
                    login(username, password);
                }
            }
        });
    }

    // 发送登录请求
    /*
    private void login(String username, String password) {
        JSONObject loginData = new JSONObject();
        try {
            loginData.put("type", "login");
            loginData.put("username", username);
            loginData.put("password", password);

            mSocket.emit("login", loginData);
        } catch (Exception e) {
            Log.e("Socket", "发送登录请求时发生错误: ", e);
        }
    }
     */

/*
    private void login(String username, String password) {
        JSONObject loginData = new JSONObject();
        try {
            loginData.put("type", "login");
            loginData.put("username", username);
            loginData.put("password", password);

            // 设定超时时间，比如 5 秒
            int timeoutMillis = 5000;

            // 发送请求，并传入回调函数
            mSocket.emit("login", loginData, (Ack) args -> {
                if (args.length > 0) {
                    JSONObject response = (JSONObject) args[0];
                    handleLoginResponse(response);
                }
            });

            // 5 秒后检查是否有收到服务器响应
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!loginCompleted) { // 如果还没收到回应
                    Log.e("Socket", "login timeout");
                    handleLoginTimeout();
                }
            }, timeoutMillis);

        } catch (Exception e) {
            Log.e("Socket", "send login mistake: ", e);
            Toast.makeText(LoginActivity.this,e.toString() , Toast.LENGTH_SHORT).show();
        }
    }

    // 标记是否完成登录
    private boolean loginCompleted = false;

    // 处理登录响应
    private void handleLoginResponse(JSONObject response) {
        loginCompleted = true;
        Log.d("Socket", "recived response: " + response.toString());
        String status = response.optString("status");
        String message = response.optString("message");

        if(status == "status"){
            Toast.makeText(LoginActivity.this,message , Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(LoginActivity.this,message , Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

    }

    // 处理超时
    private void handleLoginTimeout() {
        Log.e("Socket", "login timeout server no response");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
        }
    }
}
*/

/*
public class LoginActivity extends AppCompatActivity {

    private static final String SERVER_URL = "http://10.0.2.2:8080"; // 服务器URL
    private EditText et_username;
    private EditText et_password;
    private Handler mHandler; // 用于更新 UI 的 Handler
    public String username;

    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化控件
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);

        // 初始化 Handler
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // 在主线程中处理消息
                String result = (String) msg.obj;

                if(result.equals("Login successful: Login successful!")){
                    // 假设登录成功，则跳转到主页面
                    Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(LoginActivity.this, "Wrong account or password", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // 连接到 Socket.io 服务器
        try {
            mSocket = IO.socket(new URI(SERVER_URL));
            mSocket.connect();
        } catch (Exception e) {
            Log.e("Socket", "Error connecting to server", e);
        }

        // 监听服务器的登录响应
        mSocket.on("login_response", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    String result = (String) args[0];
                    // 使用 Handler 将结果发送到主线程
                    Message msg = mHandler.obtainMessage();
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                }
            }
        });

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 登录
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = et_username.getText().toString();
                String password = et_password.getText().toString();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "请输入用户名或者密码", Toast.LENGTH_SHORT).show();
                } else {
                    // 发起登录请求
                    login(username, password);
                }
            }
        });
    }

    // 登录方法，连接到服务器并发送消息
    private void login(String username, String password) {
        // 构造登录请求数据，包含 type 字段
        JSONObject loginData = new JSONObject();
        try {
            loginData.put("type", "login");
            loginData.put("username", username);
            loginData.put("password", password);

            // 发送登录请求
            mSocket.emit("login", loginData);
        } catch (Exception e) {
            Log.e("Socket", "Error creating JSON for login", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 断开与服务器的连接
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
        }
    }
}
*/