package com.example.whatschat;

import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> chatMessages;

    private static final int LEFT_MESSAGE = 1;
    private static final int RIGHT_MESSAGE = 2;
    private static final int INFO_MESSAGE = 3;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);

        if (message.getType() == ChatMessage.TYPE_LEFT) {
            return LEFT_MESSAGE;
        } else if (message.getType() == ChatMessage.TYPE_RIGHT) {
            return RIGHT_MESSAGE;
        } else if (message.getType() == ChatMessage.TYPE_INFO) {
            return INFO_MESSAGE;
        }

        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == LEFT_MESSAGE) {
            View view = inflater.inflate(R.layout.left_message, parent, false);
            return new LeftMessageViewHolder(view);
        } else if (viewType == RIGHT_MESSAGE) {
            View view = inflater.inflate(R.layout.right_message, parent, false);
            return new RightMessageViewHolder(view);
        } else if (viewType == INFO_MESSAGE) {
            View view = inflater.inflate(R.layout.middle_message, parent, false);
            return new InfoMessageViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        long timestamp = chatMessage.getTimestamp();
        Date date = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(date);

        if (viewHolder instanceof LeftMessageViewHolder) {
            LeftMessageViewHolder holder = (LeftMessageViewHolder) viewHolder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.messageTextView.setText(Html.fromHtml(chatMessage.getMessage(), Html.FROM_HTML_MODE_COMPACT));
            } else {
                holder.messageTextView.setText(Html.fromHtml(chatMessage.getMessage()));
            }
            holder.senderTextView.setText(chatMessage.getSender());
            holder.timestampTextView.setText(formattedDate);
        } else if (viewHolder instanceof RightMessageViewHolder) {
            RightMessageViewHolder holder = (RightMessageViewHolder) viewHolder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.messageTextView.setText(Html.fromHtml(chatMessage.getMessage(), Html.FROM_HTML_MODE_COMPACT));
            } else {
                holder.messageTextView.setText(Html.fromHtml(chatMessage.getMessage()));
            }
            holder.timestampTextView.setText(formattedDate);
        } else if (viewHolder instanceof InfoMessageViewHolder) {
            InfoMessageViewHolder holder = (InfoMessageViewHolder) viewHolder;
            holder.messageTextView.setText(chatMessage.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class LeftMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView senderTextView;
        public TextView timestampTextView;

        public LeftMessageViewHolder(View itemView) {
            super(itemView);

            messageTextView = itemView.findViewById(R.id.message_text_view);
            senderTextView = itemView.findViewById(R.id.sender_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class RightMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView timestampTextView;

        public RightMessageViewHolder(View itemView) {
            super(itemView);

            messageTextView = itemView.findViewById(R.id.message_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class InfoMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView timestampTextView;

        public InfoMessageViewHolder(View itemView) {
            super(itemView);

            messageTextView = itemView.findViewById(R.id.middle_chat_message);
        }
    }
}
