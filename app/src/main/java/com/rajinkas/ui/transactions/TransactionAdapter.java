package com.rajinkas.ui.transactions;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.databinding.ItemTransactionBinding;

import android.view.View;
import java.text.NumberFormat;
import java.util.Locale;

public class TransactionAdapter extends ListAdapter<TransactionEntity, TransactionAdapter.TransactionViewHolder> {
    private OnTransactionClickListener listener;
    private static final NumberFormat RUPIAH_FORMAT = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionEntity transaction);
        void onOptionsClick(TransactionEntity transaction, View view);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public TransactionAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TransactionEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<TransactionEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionEntity oldItem, @NonNull TransactionEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionEntity oldItem, @NonNull TransactionEntity newItem) {
            return oldItem.getAmount() == newItem.getAmount() &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getTransactionDate().equals(newItem.getTransactionDate());
        }
    };

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionEntity transaction = getItem(position);
        holder.bind(transaction);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTransactionClick(transaction);
        });
        holder.binding.btnOptions.setOnClickListener(v -> {
            if (listener != null) listener.onOptionsClick(transaction, v);
        });
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionBinding binding;

        public TransactionViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TransactionEntity transaction) {
            binding.tvDescription.setText(transaction.getDescription());
            
            // Format: yyyy-MM-dd HH:mm
            String dateStr = transaction.getCreatedAt();
            if (dateStr != null && dateStr.contains("T")) {
                dateStr = dateStr.replace("T", " ").substring(0, 16);
            } else {
                dateStr = transaction.getTransactionDate();
            }
            binding.tvCategoryAndDate.setText(dateStr);

            String amountStr = RUPIAH_FORMAT.format(transaction.getAmount());
            
            if ("INCOME".equals(transaction.getType())) {
                binding.tvAmount.setText("+ " + amountStr);
                binding.tvAmount.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                binding.tvAmount.setText("- " + amountStr);
                binding.tvAmount.setTextColor(Color.parseColor("#F44336"));
            }
        }
    }
}
