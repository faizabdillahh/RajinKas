package com.rajinkas.ui.settings;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rajinkas.data.local.entity.CategoryEntity;
import com.rajinkas.databinding.ItemCategoryBinding;

public class CategoryAdapter extends ListAdapter<CategoryEntity, CategoryAdapter.ViewHolder> {
    private OnCategoryClickListener listener;

    public CategoryAdapter() {
        super(DIFF_CALLBACK);
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryEntity category);
        void onCategoryOptionsClick(CategoryEntity category, View view);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryEntity category = getItem(position);
        holder.binding.tvCategoryName.setText(category.getName());
        holder.binding.tvCategoryType.setText(category.getType().equals("INCOME") ? "Pemasukan" : "Pengeluaran");
        
        try {
            holder.binding.viewColor.setBackgroundColor(Color.parseColor(category.getColor()));
        } catch (Exception e) {
            // Fallback color
        }

        holder.binding.ivDefault.setVisibility(category.getIsDefault() == 1 ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClick(category);
        });

        holder.binding.btnOptions.setOnClickListener(v -> {
            if (listener != null) listener.onCategoryOptionsClick(category, v);
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCategoryBinding binding;

        ViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<CategoryEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<CategoryEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull CategoryEntity oldItem, @NonNull CategoryEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CategoryEntity oldItem, @NonNull CategoryEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getType().equals(newItem.getType()) &&
                    oldItem.getColor().equals(newItem.getColor());
        }
    };
}
