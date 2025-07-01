package com.example.easist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavedEventsAdapter extends RecyclerView.Adapter<SavedEventsAdapter.ViewHolder> {

    private final List<SavedItem> savedItems;
    private final OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public SavedEventsAdapter(List<SavedItem> savedItems, OnItemLongClickListener listener) {
        this.savedItems = savedItems;
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public SavedEventsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_event, parent, false);
        return new ViewHolder(view, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedEventsAdapter.ViewHolder holder, int position) {
        SavedItem item = savedItems.get(position);
        holder.tvTitle.setText("Tytuł: " + item.getTitle());
        holder.tvType.setText("Typ: " + item.getType());
        if (!item.getDate().isEmpty() && !item.getTime().isEmpty()) {
            holder.tvDateTime.setText("Data: " + item.getDate() + " " + item.getTime());
        } else {
            holder.tvDateTime.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return savedItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvType, tvDateTime;

        public ViewHolder(@NonNull View itemView, OnItemLongClickListener listener) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvType = itemView.findViewById(R.id.tv_event_type);
            tvDateTime = itemView.findViewById(R.id.tv_event_date);

            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(getAdapterPosition());
                return true;
            });
        }
    }
    public void updateList(List<SavedItem> newList) {
        this.savedItems.clear();
        this.savedItems.addAll(newList);
        notifyDataSetChanged();
    }
}
