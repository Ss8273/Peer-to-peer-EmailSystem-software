package com.app.emailsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.emailsystem.Util.DatabaseHelper;
import com.app.emailsystem.adapter.ContactAdapter;
import com.app.emailsystem.model.Contact;
import com.app.emailsystem.network.SocketManager;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SocketManager.setCurrentActivity(this);
        Intent serviceIntent = new Intent(this, WebRTCRecipent.class);
        startService(serviceIntent); // 启动后台服务

        EditText searchInput = findViewById(R.id.search_username);
        RecyclerView recyclerView = findViewById(R.id.search_results);
        ContactAdapter adapter = new ContactAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 从 SharedPreferences 中读取用户数据
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "默认用户名");

        // 在界面中显示用户名（例如显示在 TextView 中）
        TextView textView = findViewById(R.id.username);
        if (username != null) {
            textView.setText(username);  // 显示用户名
        } else {
            Toast.makeText(this, "no username", Toast.LENGTH_SHORT).show();
        }

        findViewById(R.id.contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.logout_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. 通知服务器退出登录
                logoutUser();

                // 2. 清除本地存储的会话数据
                clearSessionData();

                // 3. 跳转到登录页面
                redirectToLoginPage();
            }
        });

        findViewById(R.id.writemail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WriteActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.to_sended_box).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SendBoxActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.receive_box).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ReceivedBoxActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.inboxes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InboxesActivity.class);
                startActivity(intent);
            }
        });

        // 每次文字变化就触发
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (!keyword.isEmpty()) {
                    List<Contact> filteredList = DatabaseHelper.getInstance(getApplicationContext())
                            .searchContacts(keyword);
                    adapter.updateData(filteredList);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    adapter.updateData(new ArrayList<>());
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // 退出登录时清理连接和用户信息
    private void logoutUser() {
        // 断开 Socket 连接
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
        }
        mSocket = null;  // 清理现有的 Socket 实例
    }

    // 清除本地存储的会话数据
    private void clearSessionData() {
        // 清理 SharedPreferences 中保存的用户信息
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    // 跳转到登录页面
    private void redirectToLoginPage() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();  // 关闭当前页面，防止用户返回
    }
}
