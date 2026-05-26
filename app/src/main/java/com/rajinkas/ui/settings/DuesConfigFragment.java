package com.rajinkas.ui.settings;

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

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rajinkas.R;
import com.rajinkas.data.local.SessionManager;
import com.rajinkas.data.local.entity.DuesConfigEntity;
import com.rajinkas.databinding.DialogDuesConfigBinding;
import com.rajinkas.databinding.FragmentDuesConfigBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DuesConfigFragment extends Fragment {
    private FragmentDuesConfigBinding binding;
    private DuesConfigViewModel viewModel;
    private DuesConfigAdapter adapter;
    private SessionManager sessionManager;
    private String selectedStartDate;
    private String selectedDueDate;

    private static final String[] DAYS = {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"};
    private static final String[] FREQUENCIES = {"WEEKLY", "ONE_TIME"};
    private static final String[] FREQUENCY_LABELS = {"Mingguan", "Sekali Bayar"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDuesConfigBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DuesConfigViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());

        adapter = new DuesConfigAdapter();
        binding.rvDuesConfig.setAdapter(adapter);
        adapter.setOnConfigClickListener(new DuesConfigAdapter.OnConfigClickListener() {
            @Override
            public void onConfigClick(DuesConfigEntity config) {
                viewModel.toggleActive(config.getId(), sessionManager.getUserId());
            }

            @Override
            public void onEditClick(DuesConfigEntity config) {
                showConfigDialog(config);
            }
        });

        viewModel.getAllConfigs().observe(getViewLifecycleOwner(), configs -> {
            adapter.submitList(configs);
        });

        binding.fabAddConfig.setOnClickListener(v -> showConfigDialog(null));
    }

    private void showConfigDialog(@Nullable DuesConfigEntity config) {
        DialogDuesConfigBinding dialogBinding = DialogDuesConfigBinding.inflate(getLayoutInflater());
        String title = (config == null) ? getString(R.string.add) : getString(R.string.edit);
        dialogBinding.tvDialogTitle.setText(title);

        ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, DAYS);
        dialogBinding.spinnerDueDay.setAdapter(daysAdapter);
        
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, FREQUENCY_LABELS);
        dialogBinding.spinnerFrequency.setAdapter(freqAdapter);

        if (config != null) {
            dialogBinding.etConfigName.setText(config.getName());
            dialogBinding.etConfigAmount.setText(String.valueOf((int)config.getAmount()));
            dialogBinding.spinnerDueDay.setText(DAYS[config.getDueDay() - 1], false);
            dialogBinding.switchActive.setChecked(config.getIsActive() == 1);
            
            selectedStartDate = config.getStartDate();
            dialogBinding.etStartDate.setText(selectedStartDate);

            selectedDueDate = config.getDueDate();
            dialogBinding.etDueDate.setText(selectedDueDate);
            
            int freqIdx = "WEEKLY".equals(config.getFrequency()) ? 0 : 1;
            dialogBinding.spinnerFrequency.setText(FREQUENCY_LABELS[freqIdx], false);
        } else {
            selectedStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
            dialogBinding.etStartDate.setText(selectedStartDate);
            selectedDueDate = ""; // Default empty
            dialogBinding.etDueDate.setText("");
            dialogBinding.spinnerFrequency.setText(FREQUENCY_LABELS[0], false);
        }

        dialogBinding.etStartDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pilih Tanggal Mulai")
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(selection);
                selectedStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
                dialogBinding.etStartDate.setText(selectedStartDate);
            });
            datePicker.show(getParentFragmentManager(), "START_DATE");
        });

        dialogBinding.etDueDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pilih Tanggal Jatuh Tempo")
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(selection);
                selectedDueDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
                dialogBinding.etDueDate.setText(selectedDueDate);
            });
            datePicker.show(getParentFragmentManager(), "DUE_DATE");
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = dialogBinding.etConfigName.getText().toString().trim();
                    String amountStr = dialogBinding.etConfigAmount.getText().toString().trim();
                    String dayStr = dialogBinding.spinnerDueDay.getText().toString();
                    String freqLabel = dialogBinding.spinnerFrequency.getText().toString();
                    
                    int dueDay = 1;
                    for (int i = 0; i < DAYS.length; i++) {
                        if (DAYS[i].equals(dayStr)) {
                            dueDay = i + 1;
                            break;
                        }
                    }
                    
                    String frequency = "WEEKLY";
                    if (FREQUENCY_LABELS[1].equals(freqLabel)) frequency = "ONE_TIME";

                    if (name.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.empty_data_warning, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amountStr);
                    int isActive = dialogBinding.switchActive.isChecked() ? 1 : 0;

                    if (config == null) {
                        DuesConfigEntity newConfig = new DuesConfigEntity();
                        newConfig.setName(name);
                        newConfig.setAmount(amount);
                        newConfig.setFrequency(frequency);
                        newConfig.setStartDate(selectedStartDate);
                        newConfig.setDueDate(selectedDueDate);
                        newConfig.setDueDay(dueDay);
                        newConfig.setIsActive(isActive);
                        viewModel.insertConfig(newConfig, sessionManager.getUserId());
                    } else {
                        config.setName(name);
                        config.setAmount(amount);
                        config.setFrequency(frequency);
                        config.setStartDate(selectedStartDate);
                        config.setDueDate(selectedDueDate);
                        config.setDueDay(dueDay);
                        config.setIsActive(isActive);
                        viewModel.updateConfig(config, sessionManager.getUserId());
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
