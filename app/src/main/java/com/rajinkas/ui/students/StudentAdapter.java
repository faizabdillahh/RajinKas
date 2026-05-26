package com.rajinkas.ui.students;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.databinding.ItemStudentBinding;

public class StudentAdapter extends ListAdapter<StudentEntity, StudentAdapter.StudentViewHolder> {
    private OnStudentClickListener listener;

    public interface OnStudentClickListener {
        void onStudentClick(StudentEntity student);
        void onStudentOptionsClick(StudentEntity student, View view);
    }

    public void setOnStudentClickListener(OnStudentClickListener listener) {
        this.listener = listener;
    }

    public StudentAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<StudentEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<StudentEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull StudentEntity oldItem, @NonNull StudentEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull StudentEntity oldItem, @NonNull StudentEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getStudentNumber().equals(newItem.getStudentNumber());
        }
    };

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStudentBinding binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new StudentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        StudentEntity student = getItem(position);
        holder.bind(student);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onStudentClick(student);
        });
        holder.binding.btnOptions.setOnClickListener(v -> {
            if (listener != null) listener.onStudentOptionsClick(student, v);
        });
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        private final ItemStudentBinding binding;

        public StudentViewHolder(ItemStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(StudentEntity student) {
            binding.tvStudentName.setText(student.getName());
            binding.tvStudentNumber.setText("No. Absen: " + student.getStudentNumber());
            if (student.getName().length() > 0) {
                binding.tvStudentInitial.setText(student.getName().substring(0, 1).toUpperCase());
            }
        }
    }
}
