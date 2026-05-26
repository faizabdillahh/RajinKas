package com.rajinkas.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.databinding.DialogAuditLogDetailBinding;
import com.rajinkas.databinding.FragmentAuditLogBinding;

public class AuditLogFragment extends Fragment {
    private FragmentAuditLogBinding binding;
    private AuditLogViewModel viewModel;
    private AuditLogAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAuditLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuditLogViewModel.class);

        adapter = new AuditLogAdapter();
        binding.rvAuditLogs.setAdapter(adapter);
        adapter.setOnLogClickListener(this::showLogDetail);

        viewModel.getAllLogs().observe(getViewLifecycleOwner(), logs -> {
            adapter.submitList(logs);
        });
    }

    private void showLogDetail(AuditLogEntity log) {
        DialogAuditLogDetailBinding dialogBinding = DialogAuditLogDetailBinding.inflate(getLayoutInflater());
        String title = log.getAction() + ": " + log.getEntityType();
        dialogBinding.tvActionTitle.setText(title);
        dialogBinding.tvOldValue.setText(log.getOldValue() != null ? log.getOldValue() : "None");
        dialogBinding.tvNewValue.setText(log.getNewValue() != null ? log.getNewValue() : "None");

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Tutup", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
