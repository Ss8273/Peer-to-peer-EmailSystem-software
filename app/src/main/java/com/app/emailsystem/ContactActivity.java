package com.app.emailsystem;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.emailsystem.Util.DatabaseHelper;
import com.app.emailsystem.adapter.ContactAdapter;
import com.app.emailsystem.model.Contact;

import java.util.List;

public class ContactActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_acitivty); // 请确保你创建了对应的 XML

        recyclerView = findViewById(R.id.contact_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = DatabaseHelper.getInstance(this);
        List<Contact> contactList = dbHelper.getAllContacts();
        Log.d("contacts", "getAllContacts: size = " + contactList.size());


        adapter = new ContactAdapter(contactList);
        recyclerView.setAdapter(adapter);

        // 设置 Toolbar
        findViewById(R.id.toolbar).setOnClickListener(view -> finish());

        // 添加返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 隐藏默认标题
        }
    }
}
