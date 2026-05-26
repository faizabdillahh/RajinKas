package com.rajinkas.ui.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.rajinkas.R;
import com.rajinkas.data.local.SessionManager;
import com.rajinkas.data.local.entity.DuesConfigEntity;
import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.databinding.FragmentBulkPaymentBinding;
import com.rajinkas.util.ClickUtils;
import com.rajinkas.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class BulkPaymentFragment extends Fragment {
    private FragmentBulkPaymentBinding binding;
    private BulkPaymentViewModel viewModel;
    private BulkPaymentAdapter adapter;
    private DuesConfigEntity selectedConfig;
    private List<DuesConfigEntity> activeConfigs = new ArrayList<>();
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBulkPaymentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BulkPaymentViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());

        adapter = new BulkPaymentAdapter();
        binding.rvBulkStudents.setAdapter(adapter);

        adapter.setOnSelectionChangedListener(count -> {
            binding.tvTotalSelected.setText(getString(R.string.selected_students_count, count));
        });

        viewModel.getActiveDuesConfigs().observe(getViewLifecycleOwner(), configs -> {
            this.activeConfigs = configs;
            updateConfigSpinner();
        });

        binding.spinnerBulkConfig.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            for (DuesConfigEntity config : activeConfigs) {
                if (config.getName().equals(selectedName)) {
                    selectedConfig = config;
                    observeStudents();
                    break;
                }
            }
        });

        binding.etPeriodLabel.setText(DateUtils.getCurrentPeriodLabel());
        binding.etPeriodLabel.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                observeStudents();
            }
        });

        ClickUtils.applySingleClick(binding.btnConfirmBulk, v -> {
            String period = binding.etPeriodLabel.getText().toString().trim();
            List<StudentEntity> selected = adapter.getSelectedStudents();

            if (period.isEmpty() || selected.isEmpty() || selectedConfig == null) {
                Toast.makeText(requireContext(), "Lengkapi data dan pilih siswa", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.processBulkPayment(selected, selectedConfig, period, sessionManager.getUserId());
            Toast.makeText(requireContext(), "Pembayaran massal berhasil diproses", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigateUp();
        });
    }

    private void updateConfigSpinner() {
        List<String> names = new ArrayList<>();
        for (DuesConfigEntity config : activeConfigs) {
            names.add(config.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names);
        binding.spinnerBulkConfig.setAdapter(adapter);
    }

    private void observeStudents() {
        if (selectedConfig == null) return;
        String period = binding.etPeriodLabel.getText().toString().trim();
        if (period.isEmpty()) return;

        viewModel.getStudentsWhoNotPaid(selectedConfig.getId(), period).observe(getViewLifecycleOwner(), students -> {
            adapter.setStudents(students);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
