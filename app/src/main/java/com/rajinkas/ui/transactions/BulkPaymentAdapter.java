package com.rajinkas.ui.transactions;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.databinding.ItemBulkStudentBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BulkPaymentAdapter extends RecyclerView.Adapter<BulkPaymentAdapter.ViewHolder> {
    private List<StudentEntity> students = new ArrayList<>();
    private final Set<Integer> selectedIds = new HashSet<>();
    private OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    public void setStudents(List<StudentEntity> newStudents) {
        this.students = newStudents;
        notifyItemRangeChanged(0, students.size());
    }

    public List<StudentEntity> getSelectedStudents() {
        List<StudentEntity> selected = new ArrayList<>();
        for (StudentEntity s : students) {
            if (selectedIds.contains(s.getId())) {
                selected.add(s);
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBulkStudentBinding binding = ItemBulkStudentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentEntity student = students.get(position);
        holder.binding.tvStudentName.setText(student.getName());
        holder.binding.cbSelected.setOnCheckedChangeListener(null);
        holder.binding.cbSelected.setChecked(selectedIds.contains(student.getId()));
        
        holder.binding.cbSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedIds.add(student.getId());
            } else {
                selectedIds.remove(student.getId());
            }
            if (listener != null) listener.onSelectionChanged(selectedIds.size());
        });

        holder.itemView.setOnClickListener(v -> {
            holder.binding.cbSelected.setChecked(!holder.binding.cbSelected.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemBulkStudentBinding binding;

        ViewHolder(ItemBulkStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
