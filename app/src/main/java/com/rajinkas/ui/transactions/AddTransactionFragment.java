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

import com.google.android.material.datepicker.MaterialDatePicker;
import com.rajinkas.data.local.SessionManager;
import com.rajinkas.data.local.entity.CategoryEntity;
import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.databinding.FragmentAddTransactionBinding;
import com.rajinkas.util.ClickUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AddTransactionFragment extends Fragment {
    private FragmentAddTransactionBinding binding;
    private TransactionViewModel viewModel;
    private SessionManager sessionManager;
    private List<CategoryEntity> categories = new ArrayList<>();
    private CategoryEntity selectedCategory;
    private long selectedDateMillis = System.currentTimeMillis();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());

        updateDateDisplay();

        binding.etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Pilih Tanggal")
                    .setSelection(selectedDateMillis)
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDateMillis = selection;
                updateDateDisplay();
            });
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categoryEntities -> {
            this.categories = categoryEntities;
            updateCategorySpinner();
        });

        binding.toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) updateCategorySpinner();
        });

        binding.spinnerCategory.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            for (CategoryEntity cat : categories) {
                if (cat.getName().equals(selectedName)) {
                    selectedCategory = cat;
                    break;
                }
            }
        });

        ClickUtils.applySingleClick(binding.btnSave, v -> saveTransaction());
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(selectedDateMillis);
        binding.etDate.setText(sdf.format(calendar.getTime()));
    }

    private void updateCategorySpinner() {
        String type = binding.btnIncome.isChecked() ? "INCOME" : "EXPENSE";
        List<String> names = new ArrayList<>();
        for (CategoryEntity cat : categories) {
            if (cat.getType().equals(type)) {
                names.add(cat.getName());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names);
        binding.spinnerCategory.setAdapter(adapter);
        binding.spinnerCategory.setText("", false);
        selectedCategory = null;
    }

    private void saveTransaction() {
        String amountStr = binding.etAmount.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        if (amountStr.isEmpty() || selectedCategory == null) {
            Toast.makeText(requireContext(), "Nominal dan Kategori wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String type = binding.btnIncome.isChecked() ? "INCOME" : "EXPENSE";

        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(amount);
        transaction.setCategoryId(selectedCategory.getId());
        transaction.setType(type);
        transaction.setDescription(description);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(selectedDateMillis);
        transaction.setTransactionDate(sdf.format(calendar.getTime()));
        
        transaction.setCreatedBy(sessionManager.getUserId());

        viewModel.insertTransaction(transaction, sessionManager.getUserId());
        Toast.makeText(requireContext(), "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
