package com.app.emailsystem;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.emailsystem.Util.DatabaseHelper;
import com.app.emailsystem.Util.DownloadEmailAdapter;
import com.app.emailsystem.Util.Email;
import com.app.emailsystem.network.SocketManager;

import java.util.ArrayList;
import java.util.List;

public class ReceivedBoxActivity extends AppCompatActivity {
    private TextView tvUsername;
    private RecyclerView rvEmails;
    private DatabaseHelper dbHelper;
    private DownloadEmailAdapter emailAdapter;
    private List<Email> emailList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recieved_box);
        SocketManager.setCurrentActivity(this);

        tvUsername = findViewById(R.id.username);
        rvEmails = findViewById(R.id.rv_emails);

        // 设置返回按钮
        findViewById(R.id.toolbar).setOnClickListener(view -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 获取用户名
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "默认用户名");
        tvUsername.setText(username);

        // 初始化数据库和 RecyclerView
        dbHelper = DatabaseHelper.getInstance(MyApplication.getAppContext());
        rvEmails.setLayoutManager(new LinearLayoutManager(this));
        emailAdapter = new DownloadEmailAdapter();
        rvEmails.setAdapter(emailAdapter);

        // 加载数据并展示
        emailList = getEmailsByReceiver(username);
        showEmails(emailList);

        // 添加左滑删除功能
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Email emailToDelete = emailList.get(position);

                // 从 UI 列表中删除
                emailList.remove(position);
                emailAdapter.removeItem(position);

                // 数据库同步删除
                boolean deleted = dbHelper.deleteEmail(emailToDelete);
                Log.d("Delete", "删除成功？" + deleted);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvEmails);
    }

    private void showEmails(List<Email> emails) {
        if (emails.isEmpty()) {
            findViewById(R.id.emptyView).setVisibility(View.VISIBLE);
            rvEmails.setVisibility(View.GONE);
        } else {
            findViewById(R.id.emptyView).setVisibility(View.GONE);
            rvEmails.setVisibility(View.VISIBLE);
            emailAdapter.setEmails(emails);
        }
    }

    public List<Email> getEmailsByReceiver(String receiver) {
        List<Email> emailList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.getEmailsByRcvname(receiver);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int themeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_THEME);
                    int timeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TIME);
                    int senderIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SENDER);
                    int file_pathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FILE_PATH);
                    int statusIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS);

                    String theme = themeIndex != -1 ? cursor.getString(themeIndex) : null;
                    String time = timeIndex != -1 ? cursor.getString(timeIndex) : null;
                    String sender = senderIndex != -1 ? cursor.getString(senderIndex) : null;
                    String filePath = file_pathIndex != -1 ? cursor.getString(file_pathIndex) : null;
                    int status = statusIndex != -1 ? cursor.getInt(statusIndex) : 0;

                    emailList.add(new Email(theme, time, sender, receiver, filePath, status, null));
                }
            } finally {
                cursor.close();
            }
        }

        return emailList;
    }
}
