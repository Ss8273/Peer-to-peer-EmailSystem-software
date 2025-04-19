package com.app.emailsystem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.widget.TextView;
import com.app.emailsystem.Util.EmailFileHelper;
import java.io.File;

public class EmailDetailActivity extends AppCompatActivity {

    private TextView senderView, receiverView, subjectView, timeView, contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_detail); // ⚠ xml 文件名

        senderView = findViewById(R.id.senderView);
        receiverView = findViewById(R.id.receiverView);
        subjectView = findViewById(R.id.subjectView);
        timeView = findViewById(R.id.timeView);
        contentView = findViewById(R.id.contentView);

        Toolbar toolbar = findViewById(R.id.emailToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Email Detail");
        }

        String sender = getIntent().getStringExtra("sender");
        String receiver = getIntent().getStringExtra("receiver");
        String subject = getIntent().getStringExtra("subject");
        String time = getIntent().getStringExtra("time");

        senderView.setText(sender);
        receiverView.setText(receiver);
        subjectView.setText(subject);
        timeView.setText(time);

        String path = EmailFileHelper.getFilePath(sender, receiver, time, subject);
        File file = new File(path);
        if (file.exists()) {
            String content = EmailFileHelper.readFileContent(file);
            contentView.setText(content);
        } else {
            contentView.setText("⚠ can't find content of email");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
