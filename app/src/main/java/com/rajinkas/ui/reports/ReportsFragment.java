package com.rajinkas.ui.reports;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.rajinkas.databinding.FragmentReportsBinding;
import com.rajinkas.data.repository.AppSettingRepository;
import com.rajinkas.ui.settings.AuditLogViewModel;
import com.rajinkas.ui.students.StudentViewModel;
import com.rajinkas.ui.transactions.TransactionViewModel;
import com.rajinkas.util.PdfGenerator;

import java.io.File;

public class ReportsFragment extends Fragment {
    private FragmentReportsBinding binding;
    private TransactionViewModel transactionViewModel;
    private StudentViewModel studentViewModel;
    private DuesViewModel duesViewModel;
    private AuditLogViewModel auditLogViewModel;
    private AppSettingRepository appSettingRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        studentViewModel = new ViewModelProvider(this).get(StudentViewModel.class);
        duesViewModel = new ViewModelProvider(this).get(DuesViewModel.class);
        auditLogViewModel = new ViewModelProvider(this).get(AuditLogViewModel.class);
        appSettingRepository = new AppSettingRepository(requireActivity().getApplication());

        binding.btnExportTransactions.setOnClickListener(v -> {
            transactionViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
                if (transactions == null || transactions.isEmpty()) {
                    Toast.makeText(requireContext(), "Tidak ada data transaksi untuk diekspor", Toast.LENGTH_SHORT).show();
                    return;
                }

                appSettingRepository.getSetting("class_name").observe(getViewLifecycleOwner(), setting -> {
                    String className = (setting != null) ? setting.getValue() : "Kelas Saya";
                    String path = PdfGenerator.generateTransactionReport(requireContext(), className, "Semua Waktu", transactions);
                    if (path != null) {
                        sharePdf(path);
                    } else {
                        Toast.makeText(requireContext(), "Gagal membuat laporan", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        binding.btnExportDuesStatus.setOnClickListener(v -> {
            studentViewModel.getAllActiveStudents().observe(getViewLifecycleOwner(), students -> {
                duesViewModel.getAllPaymentsWithConfig().observe(getViewLifecycleOwner(), allPayments -> {
                    if (students == null || students.isEmpty()) {
                        Toast.makeText(requireContext(), "Tidak ada data siswa", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    appSettingRepository.getSetting("class_name").observe(getViewLifecycleOwner(), setting -> {
                        String className = (setting != null) ? setting.getValue() : "Kelas Saya";
                        String path = PdfGenerator.generateDuesStatusReport(requireContext(), className, students, allPayments);
                        if (path != null) {
                            sharePdf(path);
                        } else {
                            Toast.makeText(requireContext(), "Gagal membuat laporan iuran", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        });

        binding.btnExportAuditLogs.setOnClickListener(v -> {
            auditLogViewModel.getAllLogs().observe(getViewLifecycleOwner(), logs -> {
                if (logs == null || logs.isEmpty()) {
                    Toast.makeText(requireContext(), "Tidak ada data audit log", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                appSettingRepository.getSetting("class_name").observe(getViewLifecycleOwner(), setting -> {
                    String className = (setting != null) ? setting.getValue() : "Kelas Saya";
                    String path = PdfGenerator.generateAuditLogReport(requireContext(), className, logs);
                    if (path != null) {
                        sharePdf(path);
                    } else {
                        Toast.makeText(requireContext(), "Gagal membuat laporan audit", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void sharePdf(String path) {
        File file = new File(path);
        Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", file);
        
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Bagikan Laporan"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
