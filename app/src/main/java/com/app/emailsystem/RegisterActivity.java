package com.app.emailsystem;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.emailsystem.network.SocketManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class RegisterActivity extends AppCompatActivity {

    private EditText et_username;
    private EditText et_password;
    private Socket mSocket;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);

        mHandler = new Handler(Looper.getMainLooper());

        // 获取 socket
        mSocket = SocketManager.getSocket();
        if (!mSocket.connected()) {
            mSocket.connect();
        }

        // 注册监听器
        mSocket.on("register_response", onRegisterResponse);

        // 返回按钮
        findViewById(R.id.toolbar).setOnClickListener(view -> finish());

        // 注册按钮点击
        findViewById(R.id.register).setOnClickListener(view -> {
            String username = et_username.getText().toString().trim();
            String password = et_password.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(RegisterActivity.this, "Please enter your email address or password", Toast.LENGTH_SHORT).show();
            } else if (!isValidEmail(username)) {
                Toast.makeText(RegisterActivity.this, "Email address must end with @email.com", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(username, password);
            }
        });
    }

    private void registerUser(String username, String password) {
        try {
            JSONObject data = new JSONObject();
            data.put("username", username);
            data.put("password", password);
            Log.d("RegisterEmit", "Sending: " + data.toString());
            mSocket.emit("register", data);
        } catch (JSONException e) {
            Log.e("RegisterActivity", "JSON Error", e);
        }
    }

    private final Emitter.Listener onRegisterResponse = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (args.length > 0) {
                try {
                    JSONObject response = new JSONObject(args[0].toString());
                    String status = response.optString("status", "error");
                    String message = response.optString("message", "unknown error");

                    Log.d("RegisterResponse", "Response: " + response.toString());

                    mHandler.post(() -> {
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();

                        if ("success".equals(status)) {
                            // 注册成功，1秒后跳转登录页面
                            mHandler.postDelayed(() -> {
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            }, 1000);
                        }
                    });

                } catch (JSONException e) {
                    Log.e("RegisterActivity", "JSON Parsing Error", e);
                    mHandler.post(() -> Toast.makeText(RegisterActivity.this, "服务器返回数据格式错误", Toast.LENGTH_SHORT).show());
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.off("register_response", onRegisterResponse);
            // 不要断开连接！这是共享的 socket
            // mSocket.disconnect(); ❌ 不要写这一行
        }
    }

    // 使用正则表达式验证邮箱格式
    private boolean isValidEmail(String email) {
        String emailPattern = "^[A-Za-z0-9._%+-]+@email\\.com$";  // 正则表达式确保邮箱以 @email.com 结尾
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
