// MessageDetailActivity.java
package com.app.emailsystem;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MessageDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        // 设置 Toolbar
        findViewById(R.id.toolbar).setOnClickListener(view -> finish());

        // 添加返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 隐藏默认标题
        }

        TextView senderView = findViewById(R.id.senderView);
        TextView receiverView = findViewById(R.id.receiverView);
        TextView subjectView = findViewById(R.id.subjectView);
        TextView timeView = findViewById(R.id.timeView);

        // 从 Intent 中获取消息数据
        String sender = getIntent().getStringExtra("sender");
        String receiver = getIntent().getStringExtra("receiver");
        String subject = getIntent().getStringExtra("subject");
        String time = getIntent().getStringExtra("time");

        senderView.setText(sender);
        receiverView.setText(receiver);
        subjectView.setText(subject);
        timeView.setText(time);
    }


}
