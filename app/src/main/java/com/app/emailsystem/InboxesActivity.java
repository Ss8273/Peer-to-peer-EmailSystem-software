package com.app.emailsystem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.emailsystem.adapter.MessageAdapter;
import com.app.emailsystem.database.Message;
import com.app.emailsystem.database.MessageRepository;
import com.app.emailsystem.network.SocketManager;

import java.util.List;
import androidx.recyclerview.widget.ItemTouchHelper;
import io.socket.client.Socket;

/*
public class InboxesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inboxes);
        SocketManager.setCurrentActivity(this);
        // 初始化消息数据库
        MessageRepository.initialize(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Message> messages = MessageRepository.getAllMessages();
        adapter = new MessageAdapter(messages);
        recyclerView.setAdapter(adapter);

        // 监听新消息
        SocketManager.setMessageListener(message -> {
            runOnUiThread(() -> {
                adapter.addMessage(message);
            });
        });
    }
}
 */
/*
public class InboxesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private MessageRepository messageRepository;
    public TextView user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inboxes);
        SocketManager.setCurrentActivity(this);

        user = findViewById(R.id.title);
        // 获取用户名
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "默认用户名");
        user.setText(username);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 设置 Toolbar
        findViewById(R.id.toolbar).setOnClickListener(view -> finish());

        messageRepository = new MessageRepository(this);
        List<Message> messages = messageRepository.getAllMessages();
        adapter = new MessageAdapter(messages);
        recyclerView.setAdapter(adapter);

        // 监听新消息
        SocketManager.setMessageListener(message -> {
            runOnUiThread(() -> {
                messageRepository.insertMessage(message);
                adapter.addMessage(message);
            });
        });
    }
}

 */
public class InboxesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private MessageRepository messageRepository;
    public TextView user;
    private Socket msocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inboxes);
        SocketManager.setCurrentActivity(this);
        // 获取 Socket 实例
        msocket = SocketManager.getSocket();

        user = findViewById(R.id.title);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "默认用户名");
        user.setText(username);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.toolbar).setOnClickListener(view -> finish());

        messageRepository = new MessageRepository(this);
        List<Message> messages = messageRepository.getMessagesByReceiver(username);
        adapter = new MessageAdapter(messages, this, username); // 传递 Context 参数
        recyclerView.setAdapter(adapter);

        SocketManager.setMessageListener(message -> {
            runOnUiThread(() -> {
                messageRepository.insertMessage(message);
                adapter.addMessage(message);
            });
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // 获取被删除的消息对象
                Message deletedMessage = adapter.getMessageAt(position);

                // 从数据库中删除
                messageRepository.deleteMessage(deletedMessage.getId()); // 你需要实现这个方法
                // 从适配器中删除
                adapter.removeMessage(position);

                // 可选：提示
                Toast.makeText(InboxesActivity.this, "Message deleted", Toast.LENGTH_SHORT).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

}