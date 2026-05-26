package com.rajinkas.ui.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.rajinkas.R;
import com.rajinkas.data.local.SessionManager;
import com.rajinkas.data.local.entity.DuesPaymentEntity;
import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.data.local.model.DuesPaymentWithConfig;
import com.rajinkas.databinding.FragmentArrearsBinding;
import com.rajinkas.ui.students.StudentAdapter;
import com.rajinkas.util.DateUtils;

import java.util.List;

public class ArrearsFragment extends Fragment {
    private FragmentArrearsBinding binding;
    private ArrearsViewModel viewModel;
    private StudentAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentArrearsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ArrearsViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());
        
        adapter = new StudentAdapter();
        binding.rvArrears.setAdapter(adapter);
        
        adapter.setOnStudentClickListener(new StudentAdapter.OnStudentClickListener() {
            @Override
            public void onStudentClick(StudentEntity student) {
                showStudentPaymentDetails(student);
            }

            @Override
            public void onStudentOptionsClick(StudentEntity student, View view) {
                showArrearsMenu(student, view);
            }
        });

        binding.tvPeriodInfo.setText("Daftar Siswa Menunggak");

        viewModel.getAllStudentsWithArrears().observe(getViewLifecycleOwner(), students -> {
            adapter.submitList(students);
            if (students == null || students.isEmpty()) {
                binding.rvArrears.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.rvArrears.setVisibility(View.VISIBLE);
                binding.layoutEmpty.setVisibility(View.GONE);
            }
        });
    }

    private void showStudentPaymentDetails(StudentEntity student) {
        viewModel.getPaidPaymentsWithConfigByStudent(student.getId()).observe(getViewLifecycleOwner(), payments -> {
            StringBuilder sb = new StringBuilder();
            if (payments == null || payments.isEmpty()) {
                sb.append("Belum ada riwayat pembayaran yang lunas.");
            } else {
                for (DuesPaymentWithConfig pc : payments) {
                    sb.append("✅ ")
                      .append(pc.config.getName())
                      .append(" - ")
                      .append(pc.payment.getPeriodLabel())
                      .append("\nDibayar pada: ")
                      .append(pc.payment.getPaidAt().replace("T", " ").substring(0, 16))
                      .append("\n\n");
                }
            }

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Riwayat Pembayaran: " + student.getName())
                    .setMessage(sb.toString())
                    .setPositiveButton("Tutup", null)
                    .show();
        });
    }

    private void showArrearsMenu(StudentEntity student, View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenu().add("Bayar Tunggakan");
        popup.setOnMenuItemClickListener(item -> {
            if ("Bayar Tunggakan".equals(item.getTitle())) {
                showUnpaidPeriodsDialog(student);
            }
            return true;
        });
        popup.show();
    }

    private void showUnpaidPeriodsDialog(StudentEntity student) {
        viewModel.getUnpaidPaymentsWithConfigByStudent(student.getId()).observe(getViewLifecycleOwner(), payments -> {
            if (payments == null || payments.isEmpty()) return;

            String[] items = new String[payments.size()];
            for (int i = 0; i < payments.size(); i++) {
                DuesPaymentWithConfig pc = payments.get(i);
                String item = pc.config.getName() + " - " + pc.payment.getPeriodLabel() + " (Rp " + (int)pc.config.getAmount() + ")";
                if (pc.payment.getDueDate() != null && !pc.payment.getDueDate().isEmpty()) {
                    item += "\nJatuh tempo: " + pc.payment.getDueDate();
                }
                items[i] = item;
            }
            
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Pilih Tunggakan " + student.getName())
                    .setItems(items, (dialog, which) -> {
                        DuesPaymentEntity selectedPayment = payments.get(which).payment;
                        viewModel.payArrears(selectedPayment, sessionManager.getUserId());
                        android.widget.Toast.makeText(requireContext(), 
                            "Berhasil melunasi " + selectedPayment.getPeriodLabel(),
                            android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
