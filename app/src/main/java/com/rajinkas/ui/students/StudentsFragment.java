package com.rajinkas.ui.students;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rajinkas.R;
import com.rajinkas.data.local.SessionManager;
import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.databinding.DialogStudentBinding;
import com.rajinkas.databinding.FragmentStudentsBinding;
import com.rajinkas.util.ClickUtils;

public class StudentsFragment extends Fragment {
    private FragmentStudentsBinding binding;
    private StudentViewModel viewModel;
    private StudentAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StudentViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());

        adapter = new StudentAdapter();
        binding.rvStudents.setAdapter(adapter);
        adapter.setOnStudentClickListener(new StudentAdapter.OnStudentClickListener() {
            @Override
            public void onStudentClick(StudentEntity student) {
                showStudentDialog(student);
            }

            @Override
            public void onStudentOptionsClick(StudentEntity student, View view) {
                showOptionsPopup(student, view);
            }
        });

        observeStudents("");

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                observeStudents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ClickUtils.applySingleClick(binding.fabAddStudent, v -> showStudentDialog(null));
    }

    private void observeStudents(String query) {
        viewModel.searchStudents(query).observe(getViewLifecycleOwner(), students -> {
            adapter.submitList(students);
            if (students == null || students.isEmpty()) {
                binding.rvStudents.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.rvStudents.setVisibility(View.VISIBLE);
                binding.layoutEmpty.setVisibility(View.GONE);
            }
        });
    }

    private void showStudentDialog(@Nullable StudentEntity student) {
        DialogStudentBinding dialogBinding = DialogStudentBinding.inflate(getLayoutInflater());
        String title = (student == null) ? getString(R.string.add) : getString(R.string.edit);
        dialogBinding.tvDialogTitle.setText(title);

        if (student != null) {
            dialogBinding.etStudentName.setText(student.getName());
            dialogBinding.etStudentNumber.setText(student.getStudentNumber());
            dialogBinding.etStudentPhone.setText(student.getPhone());
        } else {
            // Suggest next number
            int nextNumber = viewModel.getNextStudentNumber();
            dialogBinding.etStudentNumber.setText(String.valueOf(nextNumber));
        }

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, null) // Set null to handle manually
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = dialogBinding.etStudentName.getText().toString().trim();
            String number = dialogBinding.etStudentNumber.getText().toString().trim();
            String phone = dialogBinding.etStudentPhone.getText().toString().trim();

            if (name.isEmpty() || number.isEmpty()) {
                Toast.makeText(requireContext(), R.string.empty_data_warning, Toast.LENGTH_SHORT).show();
                return;
            }

            // Validation
            String error = viewModel.validateStudentNumber(number, student != null ? student.getId() : null);
            if (error != null) {
                dialogBinding.etStudentNumber.setError(error);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                return;
            }

            if (student == null) {
                StudentEntity newStudent = new StudentEntity();
                newStudent.setName(name);
                newStudent.setStudentNumber(number);
                newStudent.setPhone(phone);
                viewModel.insert(newStudent, sessionManager.getUserId());
            } else {
                // Create a copy to avoid in-place modification of the list item
                StudentEntity updatedStudent = new StudentEntity();
                updatedStudent.setId(student.getId());
                updatedStudent.setUuid(student.getUuid());
                updatedStudent.setCreatedAt(student.getCreatedAt());
                updatedStudent.setIsActive(student.getIsActive());
                updatedStudent.setSyncStatus(student.getSyncStatus());
                
                updatedStudent.setName(name);
                updatedStudent.setStudentNumber(number);
                updatedStudent.setPhone(phone);
                viewModel.update(updatedStudent, sessionManager.getUserId());
            }
            dialog.dismiss();
        });
    }

    private void showOptionsPopup(StudentEntity student, View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenu().add(getString(R.string.edit));
        popup.getMenu().add(getString(R.string.delete));
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals(getString(R.string.edit))) {
                showStudentDialog(student);
            } else if (item.getTitle().equals(getString(R.string.delete))) {
                confirmDelete(student);
            }
            return true;
        });
        popup.show();
    }

    private void confirmDelete(StudentEntity student) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.delete_student_confirm, student.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.softDelete(student, sessionManager.getUserId());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
