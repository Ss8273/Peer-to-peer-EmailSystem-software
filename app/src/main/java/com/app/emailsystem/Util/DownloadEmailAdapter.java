package com.app.emailsystem.Util;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.emailsystem.EmailDetailActivity;
import com.app.emailsystem.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DownloadEmailAdapter extends RecyclerView.Adapter<DownloadEmailAdapter.EmailViewHolder> {
    private List<Email> emails;

    public void setEmails(List<Email> emails) {
        this.emails = emails;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_downloademail, parent, false);
        return new EmailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmailViewHolder holder, int position) {
        Email email = emails.get(position);

        holder.statusIcon.setImageResource(R.drawable.ic_status_download);
        holder.statusText.setText("Downloaded");

        holder.receiverName.setText(email.getSender());
        holder.receiverInitial.setText(getInitial(email.getSender()));
        holder.subject.setText(email.getTheme());
        holder.time.setText(formatTimestamp(email.getTime()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EmailDetailActivity.class);
            intent.putExtra("sender", email.getSender());
            intent.putExtra("receiver", email.getReceiver());
            intent.putExtra("subject", email.getTheme());
            intent.putExtra("time", email.getTime());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return emails == null ? 0 : emails.size();
    }

    // ✅ 已有：滑动删除调用
    public void removeItem(int position) {
        if (emails != null && position >= 0 && position < emails.size()) {
            emails.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ✅ 新增：用于撤销功能（或外部获取被删项）
    public Email getEmailAt(int position) {
        if (emails != null && position >= 0 && position < emails.size()) {
            return emails.get(position);
        }
        return null;
    }

    public static class EmailViewHolder extends RecyclerView.ViewHolder {
        public TextView receiverInitial;
        public TextView receiverName;
        public TextView subject;
        public TextView time;
        public ImageView statusIcon;
        public TextView statusText;

        public EmailViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverInitial = itemView.findViewById(R.id.receiver_initial);
            receiverName = itemView.findViewById(R.id.receiver_name);
            subject = itemView.findViewById(R.id.subject);
            time = itemView.findViewById(R.id.time);
            statusIcon = itemView.findViewById(R.id.email_status_icon);
            statusText = itemView.findViewById(R.id.email_status_text);
        }
    }

    private String getInitial(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return String.valueOf(name.charAt(0)).toUpperCase();
    }

    private String formatTimestamp(String originalTimestamp) {
        if (originalTimestamp == null || originalTimestamp.isEmpty()) {
            return "";
        }

        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault());
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        try {
            Date date = originalFormat.parse(originalTimestamp);
            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return originalTimestamp;
        }
    }
}
