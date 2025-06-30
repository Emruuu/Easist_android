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

    public SavedEventsAdapter(List<SavedItem> savedItems) {
        this.savedItems = savedItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedItem item = savedItems.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvType.setText(item.getType());
        if (!item.getDate().isEmpty() && !item.getTime().isEmpty()) {
            holder.tvDateTime.setText(item.getDate() + " " + item.getTime());
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvType = itemView.findViewById(R.id.tvType);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
        }
    }
}
