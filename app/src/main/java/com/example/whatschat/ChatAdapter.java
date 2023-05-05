package com.example.whatschat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import androidx.appcompat.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatschat.fragments.ChatHomeFragment;

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

    public static class ActionModeCallback implements ActionMode.Callback {

        View view;

        ActionModeCallback(View view){
            this.view = view;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.message_action_mode, menu);
            mode.setTitle("1");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // This method is called every time the action mode is shown. You can update the menu here if needed.
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Handle the menu item clicks here
            TextView messageTextView = view.findViewById(R.id.message_text_view);
            switch (item.getItemId()) {
                case R.id.action_copy:
                    ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newPlainText("message", messageTextView.getText()));
                    mode.finish();
                    return true;
                case R.id.action_delete:
                    // Delete the selected message
                    // ...
                    Toast.makeText(view.getContext(), "Delete action called", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {}
    }

    public static class LeftMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView senderTextView;
        public TextView timestampTextView;
        private Toolbar normalModeToolbar;
        private Toolbar actionModeToolbar;

        public LeftMessageViewHolder(View itemView) {
            super(itemView);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ActionModeCallback mActionModeCallback = new ActionModeCallback(view);
                    ((AppCompatActivity) view.getContext()).startSupportActionMode(mActionModeCallback);
                    return true;
                }
            });

            messageTextView = itemView.findViewById(R.id.message_text_view);
            senderTextView = itemView.findViewById(R.id.sender_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class RightMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView timestampTextView;
        private Toolbar toolbar;

        public RightMessageViewHolder(View itemView) {
            super(itemView);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ActionModeCallback mActionModeCallback = new ActionModeCallback(view);
                    ((AppCompatActivity) view.getContext()).startSupportActionMode(mActionModeCallback);
                    return true;
                }
            });

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
