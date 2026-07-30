package com.ghostmax;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    public static final int TYPE_SYSTEM = 2;

    public static class DisplayMessage {
        public int type;
        public String text;
        public String meta;
        public boolean isError;

        public DisplayMessage(int type, String text, String meta, boolean isError) {
            this.type = type; this.text = text; this.meta = meta; this.isError = isError;
        }
    }

    private final List<DisplayMessage> items = new ArrayList<>();

    public void add(DisplayMessage m) {
        items.add(m);
        notifyItemInserted(items.size() - 1);
    }

    public void removeLast() {
        if (!items.isEmpty()) {
            int idx = items.size() - 1;
            items.remove(idx);
            notifyItemRemoved(idx);
        }
    }

    public DisplayMessage last() {
        return items.isEmpty() ? null : items.get(items.size() - 1);
    }

    public void clear() {
        int n = items.size();
        items.clear();
        notifyItemRangeRemoved(0, n);
    }

    public int size() { return items.size(); }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        if (viewType == TYPE_USER) layoutRes = R.layout.item_message_user;
        else if (viewType == TYPE_BOT) layoutRes = R.layout.item_message_bot;
        else layoutRes = R.layout.item_message_system;
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayMessage m = items.get(position);
        MessageViewHolder h = (MessageViewHolder) holder;
        h.tvText.setText(m.text);
        if (h.tvMeta != null) {
            if (m.meta != null && !m.meta.isEmpty()) {
                h.tvMeta.setVisibility(View.VISIBLE);
                h.tvMeta.setText(m.meta);
            } else {
                h.tvMeta.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        TextView tvMeta;
        MessageViewHolder(View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            tvMeta = itemView.findViewById(R.id.tvMeta);
        }
    }
}
