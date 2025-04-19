package com.app.emailsystem.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.emailsystem.R;
import com.app.emailsystem.model.Contact;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private final List<Contact> contactList;

    public ContactAdapter(List<Contact> contactList) {
        this.contactList = contactList;
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView emailText;
        TextView initialText;

        public ContactViewHolder(View itemView) {
            super(itemView);
            emailText = itemView.findViewById(R.id.email_text);
            initialText = itemView.findViewById(R.id.tv_initial);
        }
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        String email = contact.getEmail();
        holder.emailText.setText(email);

        // 设置首字母
        if (email != null && !email.isEmpty()) {
            String initial = email.substring(0, 1).toUpperCase();
            holder.initialText.setText(initial);
        } else {
            holder.initialText.setText("?");
        }
    }

    public void updateData(List<Contact> newList) {
        contactList.clear();
        contactList.addAll(newList);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return contactList != null ? contactList.size() : 0;
    }
}
