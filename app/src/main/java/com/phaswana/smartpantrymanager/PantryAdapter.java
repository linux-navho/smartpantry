package com.phaswana.smartpantrymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private final Context context;
    private List<PantryItem> items;
    private final OnItemClickListener listener;

    public PantryAdapter(Context context, List<PantryItem> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public void updateList(List<PantryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.tvName.setText(item.getName());

        String qtyFormatted = (item.getQuantity() % 1 == 0)
                ? String.valueOf((long) item.getQuantity())
                : String.valueOf(item.getQuantity());
        holder.tvQuantity.setText(qtyFormatted + " " + item.getUnit());

        long daysLeft = item.getDaysUntilExpiry();
        if (daysLeft < 0) {
            holder.tvBadge.setText("Expired");
            holder.tvBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.badge_expired));
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.status_expired_text));
        } else if (daysLeft <= 3) {
            holder.tvBadge.setText(daysLeft == 0 ? "Expires today" : "Expires in " + daysLeft + "d");
            holder.tvBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.badge_warning));
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.status_warning_text));
        } else {
            holder.tvBadge.setText("Fresh • " + daysLeft + "d");
            holder.tvBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.badge_fresh));
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.status_fresh_text));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvQuantity;
        final TextView tvBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPantryItemName);
            tvQuantity = itemView.findViewById(R.id.tvPantryItemQuantity);
            tvBadge = itemView.findViewById(R.id.tvExpiryBadge);
        }
    }
}
