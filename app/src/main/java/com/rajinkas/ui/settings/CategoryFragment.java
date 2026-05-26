package com.rajinkas.ui.settings;

import android.os.Bundle;
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
import com.rajinkas.data.local.entity.CategoryEntity;
import com.rajinkas.databinding.DialogCategoryBinding;
import com.rajinkas.databinding.FragmentCategoriesBinding;
import com.rajinkas.util.ClickUtils;

public class CategoryFragment extends Fragment {
    private FragmentCategoriesBinding binding;
    private CategoryViewModel viewModel;
    private CategoryAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        sessionManager = SessionManager.getInstance(requireContext());

        adapter = new CategoryAdapter();
        binding.rvCategories.setAdapter(adapter);
        adapter.setOnCategoryClickListener(new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(CategoryEntity category) {
                if (category.getIsDefault() == 0) {
                    showCategoryDialog(category);
                } else {
                    Toast.makeText(requireContext(), "Kategori bawaan tidak dapat diubah", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCategoryOptionsClick(CategoryEntity category, View view) {
                if (category.getIsDefault() == 0) {
                    showOptionsPopup(category, view);
                } else {
                    Toast.makeText(requireContext(), "Kategori bawaan tidak memiliki opsi tambahan", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getAllActiveCategories().observe(getViewLifecycleOwner(), categories -> {
            adapter.submitList(categories);
        });

        ClickUtils.applySingleClick(binding.fabAddCategory, v -> showCategoryDialog(null));
    }

    private void showCategoryDialog(@Nullable CategoryEntity category) {
        DialogCategoryBinding dialogBinding = DialogCategoryBinding.inflate(getLayoutInflater());
        String title = (category == null) ? getString(R.string.add) : getString(R.string.edit);
        dialogBinding.tvDialogTitle.setText(title);

        if (category != null) {
            dialogBinding.etCategoryName.setText(category.getName());
            if (category.getType().equals("EXPENSE")) {
                dialogBinding.btnExpense.setChecked(true);
            } else {
                dialogBinding.btnIncome.setChecked(true);
            }
            // Logic for selecting the right color chip could go here
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = dialogBinding.etCategoryName.getText().toString().trim();
                    String type = dialogBinding.btnIncome.isChecked() ? "INCOME" : "EXPENSE";
                    
                    // Simple color selection based on checked chip
                    String color = "#006B5B"; // Default teal
                    int checkedId = dialogBinding.chipGroupColors.getCheckedChipId();
                    if (checkedId == R.id.chipRed) color = "#F44336";
                    else if (checkedId == R.id.chipBlue) color = "#2196F3";
                    else if (checkedId == R.id.chipOrange) color = "#FF9800";

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.empty_data_warning, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (category == null) {
                        CategoryEntity newCat = new CategoryEntity();
                        newCat.setName(name);
                        newCat.setType(type);
                        newCat.setColor(color);
                        newCat.setIcon("ic_dashboard"); // Default icon
                        newCat.setIsDefault(0);
                        viewModel.insert(newCat, sessionManager.getUserId());
                    } else {
                        category.setName(name);
                        category.setType(type);
                        category.setColor(color);
                        viewModel.update(category, sessionManager.getUserId());
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showOptionsPopup(CategoryEntity category, View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenu().add(getString(R.string.edit));
        popup.getMenu().add(getString(R.string.delete));
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals(getString(R.string.edit))) {
                showCategoryDialog(category);
            } else if (item.getTitle().equals(getString(R.string.delete))) {
                confirmDelete(category);
            }
            return true;
        });
        popup.show();
    }

    private void confirmDelete(CategoryEntity category) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage("Apakah Anda yakin ingin menghapus kategori '" + category.getName() + "'? Kategori tidak akan dihapus permanen tetapi tidak akan muncul lagi di pilihan transaksi.")
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.softDelete(category, sessionManager.getUserId());
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
