package com.rajinkas.ui.settings;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.databinding.ItemAuditLogBinding;

public class AuditLogAdapter extends ListAdapter<AuditLogEntity, AuditLogAdapter.ViewHolder> {
    private OnLogClickListener listener;

    public interface OnLogClickListener {
        void onLogClick(AuditLogEntity log);
    }

    public void setOnLogClickListener(OnLogClickListener listener) {
        this.listener = listener;
    }

    protected AuditLogAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<AuditLogEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<AuditLogEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull AuditLogEntity oldItem, @NonNull AuditLogEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull AuditLogEntity oldItem, @NonNull AuditLogEntity newItem) {
            return oldItem.getUuid().equals(newItem.getUuid());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAuditLogBinding binding = ItemAuditLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AuditLogEntity log = getItem(position);
        holder.binding.tvAction.setText(log.getAction() + ": " + log.getEntityType());
        holder.binding.tvDescription.setText(log.getDescription());
        holder.binding.tvDate.setText(log.getCreatedAt());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLogClick(log);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemAuditLogBinding binding;

        ViewHolder(ItemAuditLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
