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
import com.app.emailsystem.Util.Email;
import com.app.emailsystem.Util.EmailAdapter;
import com.app.emailsystem.network.SocketManager;

import java.util.ArrayList;
import java.util.List;

public class SendBoxActivity extends AppCompatActivity {
    private TextView tvUsername;
    private RecyclerView rvEmails;
    private DatabaseHelper dbHelper;
    private EmailAdapter emailAdapter;
    private List<Email> emailList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_box);
        SocketManager.setCurrentActivity(this);

        tvUsername = findViewById(R.id.username);
        rvEmails = findViewById(R.id.rv_emails);

        findViewById(R.id.toolbar).setOnClickListener(view -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "默认用户名");
        tvUsername.setText(username);

        dbHelper = DatabaseHelper.getInstance(MyApplication.getAppContext());

        rvEmails.setLayoutManager(new LinearLayoutManager(this));
        emailAdapter = new EmailAdapter();
        rvEmails.setAdapter(emailAdapter);

        emailList = getEmailsBySender(username);
        showEmails(emailList);

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Email emailToDelete = emailList.get(position);

                emailList.remove(position);
                emailAdapter.removeItem(position);

                boolean success = dbHelper.deleteEmail(emailToDelete);
                Log.d("SendBox", "邮件删除成功？" + success);
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

    public List<Email> getEmailsBySender(String sender) {
        List<Email> emailList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = dbHelper.getEmailsByUsername(sender);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int themeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_THEME);
                    int timeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TIME);
                    int receiverIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIVER);
                    int file_pathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FILE_PATH);
                    int statusIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS);

                    String theme = themeIndex != -1 ? cursor.getString(themeIndex) : null;
                    String time = timeIndex != -1 ? cursor.getString(timeIndex) : null;
                    String receiver = receiverIndex != -1 ? cursor.getString(receiverIndex) : null;
                    String file_path = file_pathIndex != -1 ? cursor.getString(file_pathIndex) : null;
                    int status = statusIndex != -1 ? cursor.getInt(statusIndex) : 0;

                    Log.d("sendbox", "status=" + status);
                    emailList.add(new Email(theme, time, sender, receiver, file_path, status, null));
                }
            } finally {
                cursor.close();
            }
        }

        return emailList;
    }
}
