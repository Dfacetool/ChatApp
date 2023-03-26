package com.example.chatapp;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    // ...
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }
    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = viewType == ChatMessageRole.USER.ordinal() ? R.layout.item_user_message : R.layout.item_chatgpt_message;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ChatViewHolder(view);
    }


    @Override
    public void onViewAttachedToWindow(ChatViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.textView.setEnabled(false);
        holder.textView.setEnabled(true);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.textView.setText(message.getContent());
        holder.textView.setTextSize(20);
        // 设置背景
        if (message.getRole().equals(ChatMessageRole.USER.value())) {
            Drawable userBackground = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.user_message_background);
            holder.textView.setBackground(userBackground);
        } else {
            Drawable assistantBackground = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.chatgpt_message_background);
            holder.textView.setBackground(assistantBackground);
        }
        //onViewAttachedToWindow(holder);
        holder.textView.setTextIsSelectable(true);
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        String a = messages.get(position).getRole();
        if(a == ChatMessageRole.USER.value()) return ChatMessageRole.USER.ordinal();
        else  return ChatMessageRole.ASSISTANT.ordinal();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void removeMessage() {
        messages.remove(messages.size()-1);
        notifyItemRemoved(messages.size() - 1);
    }
}
