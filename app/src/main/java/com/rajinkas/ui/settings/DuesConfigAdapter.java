package com.rajinkas.ui.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rajinkas.data.local.entity.DuesConfigEntity;
import com.rajinkas.databinding.ItemDuesConfigBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class DuesConfigAdapter extends ListAdapter<DuesConfigEntity, DuesConfigAdapter.ViewHolder> {
    private OnConfigClickListener listener;

    public DuesConfigAdapter() {
        super(DIFF_CALLBACK);
    }

    public interface OnConfigClickListener {
        void onConfigClick(DuesConfigEntity config);
        void onEditClick(DuesConfigEntity config);
    }

    public void setOnConfigClickListener(OnConfigClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDuesConfigBinding binding = ItemDuesConfigBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DuesConfigEntity config = getItem(position);
        holder.binding.tvConfigName.setText(config.getName());
        holder.binding.tvConfigAmount.setText(formatRupiah(config.getAmount()));
        
        String detail = config.getFrequency();
        if ("WEEKLY".equals(config.getFrequency())) {
            detail = "Mingguan (Hari " + getDayName(config.getDueDay()) + ")";
        }
        holder.binding.tvConfigDetail.setText(detail);
        
        holder.binding.chipActive.setVisibility(config.getIsActive() == 1 ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onConfigClick(config);
        });

        holder.binding.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(config);
        });
    }

    private String formatRupiah(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount);
    }

    private String getDayName(int day) {
        switch (day) {
            case 1: return "Senin";
            case 2: return "Selasa";
            case 3: return "Rabu";
            case 4: return "Kamis";
            case 5: return "Jumat";
            case 6: return "Sabtu";
            case 7: return "Minggu";
            default: return "Senin";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemDuesConfigBinding binding;

        ViewHolder(ItemDuesConfigBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<DuesConfigEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<DuesConfigEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull DuesConfigEntity oldItem, @NonNull DuesConfigEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull DuesConfigEntity oldItem, @NonNull DuesConfigEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getAmount() == newItem.getAmount() &&
                    oldItem.getIsActive() == newItem.getIsActive();
        }
    };
}
