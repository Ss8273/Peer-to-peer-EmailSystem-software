package com.app.emailsystem.adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.emailsystem.MessageDetailActivity;
import com.app.emailsystem.R;
import com.app.emailsystem.WebRTC.PeerManager;
import com.app.emailsystem.database.Message;
import com.app.emailsystem.network.SocketManager;

import org.json.JSONObject;
import org.webrtc.DataChannel;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.socket.client.Socket;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messageList;
    private Context context;
    private PeerManager peerManager;
    private String username;

    public MessageAdapter(List<Message> messageList, Context context, String myname) {
        this.messageList = messageList;
        this.context = context;
        this.username = myname;
        this.peerManager = PeerManager.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Message message = messageList.get(position);
        sendMessageToServer(message, "accept");

        holder.senderTextView.setText("From: " + message.getSender());
        holder.contentTextView.setText("Body: " + message.getSubject());

        // 💡 添加点击事件 → 跳转到 MessageDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MessageDetailActivity.class);
            intent.putExtra("sender", message.getSender());
            intent.putExtra("receiver", message.getReceiver());
            intent.putExtra("subject", message.getSubject());
            intent.putExtra("time", message.getTime());
            v.getContext().startActivity(intent); // 🔥 启动详情页
        });

        holder.senderTextView.setText(message.getSender());

        String initial = getInitial(message.getSender());
        holder.senderInitial.setText(initial);

        holder.contentTextView.setText(message.getSubject());

        String originalTimestamp = message.getTime();
        String formattedTime = formatTimestamp(originalTimestamp);
        holder.timestampTextView.setText(formattedTime);

        holder.btnAccept.setOnClickListener(v -> {
            if (peerManager.isBusy()) {
                Toast.makeText(context, "Download task in progress, please wait...", Toast.LENGTH_SHORT).show();
                return;
            }

            DataChannel mChannel = peerManager.startPeerConnection(username, message.getSender());
            peerManager.setBusy(true);
            Toast.makeText(context, "Connection initiated, please wait...", Toast.LENGTH_SHORT).show();

            android.os.Handler handler = new android.os.Handler();
            Runnable checkChannel = new Runnable() {
                @Override
                public void run() {
                    if (mChannel != null && mChannel.state() == DataChannel.State.OPEN) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("type", "emailInfo");
                            jsonObject.put("sender", message.getSender());
                            jsonObject.put("receiver", username);
                            jsonObject.put("subject", message.getSubject());
                            jsonObject.put("time", message.getTime());

                            ByteBuffer byteBuffer = ByteBuffer.wrap(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
                            DataChannel.Buffer buffer = new DataChannel.Buffer(byteBuffer, false);
                            mChannel.send(buffer);

                            Log.d("MessageAdapter", "✅ send email info successfully");
                            Toast.makeText(context, "Download request initiated", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("MessageAdapter", "❌ Failed to construct or send emailInfo", e);
                        }
                    } else {
                        // 继续检查，每隔 200 毫秒重新执行
                        handler.postDelayed(this, 200);
                    }
                }
            };

            handler.postDelayed(checkChannel, 200); // 启动轮询
        });

/*
        holder.btnReject.setOnClickListener(v -> {
            sendMessageToServer(message, "reject");
        });

 */
    }


    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView senderInitial;
        public TextView senderTextView;
        public TextView contentTextView;
        public TextView timestampTextView;
        public TextView btnAccept;
        //public TextView btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            senderInitial = itemView.findViewById(R.id.sender_initial);
            senderTextView = itemView.findViewById(R.id.sender_name);
            contentTextView = itemView.findViewById(R.id.subject);
            timestampTextView = itemView.findViewById(R.id.time);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            //btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }

    private String getInitial(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return String.valueOf(name.charAt(0));
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
        } catch (Exception e) {
            e.printStackTrace();
            return originalTimestamp;
        }
    }

    private void sendMessageToServer(Message message, String action) {
        try {
            JSONObject json = new JSONObject();
            json.put("action", action);
            json.put("sender", message.getSender());
            json.put("receiver", message.getReceiver());
            json.put("subject", message.getSubject());
            json.put("time", message.getTime());

            Socket socket= SocketManager.getSocket();
            //socket.emit("message_action", json);
            // 监听服务器响应
            socket.once("message_action_response", args -> {
                if (args.length > 0) {
                    JSONObject response = (JSONObject) args[0];
                    String status = response.optString("status");
                    String msg = response.optString("message");

                    if ("success".equals(status)) {
                        // 弹窗提醒：拒绝成功
                        new android.os.Handler(Looper.getMainLooper()).post(() ->
                                new AlertDialog.Builder(context)
                                        .setTitle("Message Action")
                                        .setMessage("The message was successfully rejected.")
                                        .setPositiveButton("OK", null)
                                        .show()
                        );
                    } else {
                        Log.w("MessageAdapter", "❗Reject failed: " + msg);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MessageAdapter", "❌ Failed to send reject message", e);
        }
    }


    public void addMessage(Message newMessage) {
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        messageList.add(newMessage);
        notifyItemInserted(messageList.size() - 1);
    }

    public void removeMessage(int position) {
        if (messageList != null && position >= 0 && position < messageList.size()) {
            messageList.remove(position);
            notifyItemRemoved(position);
            // 同时从数据库中删除
        }
    }

    public Message getMessageAt(int position) {
        if (messageList != null && position >= 0 && position < messageList.size()) {
            return messageList.get(position);
        }
        return null;
    }

}
