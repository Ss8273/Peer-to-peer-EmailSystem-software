package com.app.emailsystem.Util;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.emailsystem.EmailDetailActivity;
import com.app.emailsystem.R;

import java.util.List;

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.EmailViewHolder> {

    private List<Email> emails;

    public void setEmails(List<Email> emails) {
        this.emails = emails;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (emails != null && position >= 0 && position < emails.size()) {
            emails.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public EmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_email, parent, false);
        return new EmailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmailViewHolder holder, int position) {
        Email email = emails.get(position);
        int status = email.getStatus();
        Log.d("email status:"+status,"------------------------");
        // 设置状态图标
        switch (status) {
            case 0:
                holder.statusIcon.setImageResource(R.drawable.ic_status_failed);
                break;
            case 1:
                holder.statusIcon.setImageResource(R.drawable.ic_status_success);
                break;
            case 2:
                holder.statusIcon.setImageResource(R.drawable.ic_status_rejected);
                break;
            case 3:
                holder.statusIcon.setImageResource(R.drawable.ic_status_download);
                break;
            case 4:
            default:
                holder.statusIcon.setImageResource(R.drawable.ic_status_default);
                break;
        }

        // 设置状态文字
        switch (email.getStatus()) {
            case 0:
                holder.statusText.setText("Fail");
                break;
            case 1:
                holder.statusText.setText("Send");
                break;
            case 2:
                holder.statusText.setText("Reject");
                break;
            case 3:
                holder.statusText.setText("Accept");
                break;
            default:
                holder.statusText.setText("Waiting");
                break;
        }

        // 设置收件人信息
        holder.receiverName.setText(email.getReceiver());
        holder.receiverInitial.setText(getInitial(email.getReceiver()));

        // 设置主题
        holder.subject.setText(email.getTheme());

        // 设置时间
        holder.time.setText(email.getTime());

        // 点击跳转详情页
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EmailDetailActivity.class);
            intent.putExtra("sender", email.getSender());
            intent.putExtra("receiver", email.getReceiver());
            intent.putExtra("subject", email.getSubject());
            intent.putExtra("time", email.getTime());
            intent.putExtra("filepath", email.getFilepath());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return emails == null ? 0 : emails.size();
    }

    public static class EmailViewHolder extends RecyclerView.ViewHolder {
        public TextView receiverInitial;
        public TextView receiverName;
        public TextView subject;
        public TextView time;
        public TextView statusText;
        public ImageView statusIcon;

        public EmailViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverInitial = itemView.findViewById(R.id.receiver_initial);
            receiverName = itemView.findViewById(R.id.receiver_name);
            subject = itemView.findViewById(R.id.subject);
            time = itemView.findViewById(R.id.time);
            statusText = itemView.findViewById(R.id.email_status_text);
            statusIcon = itemView.findViewById(R.id.email_status_icon);
        }
    }

    private String getInitial(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return String.valueOf(name.charAt(0)).toUpperCase();
    }
}
