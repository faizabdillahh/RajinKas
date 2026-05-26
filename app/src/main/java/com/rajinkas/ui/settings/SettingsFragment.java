package com.rajinkas.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.rajinkas.R;
import com.rajinkas.data.local.SessionManager;
import com.rajinkas.databinding.FragmentSettingsBinding;
import com.rajinkas.ui.auth.LoginActivity;

import android.content.Intent;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.rajinkas.data.repository.BackupRepository;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private SessionManager sessionManager;
    private SettingsViewModel viewModel;

    private final ActivityResultLauncher<String> createBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"),
            uri -> {
                if (uri != null) {
                    viewModel.backup(requireContext(), uri, new BackupRepository.BackupCallback() {
                        @Override
                        public void onSuccess() {
                            requireActivity().runOnUiThread(() -> 
                                Toast.makeText(requireContext(), "Backup berhasil", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onError(String message) {
                            requireActivity().runOnUiThread(() -> 
                                Toast.makeText(requireContext(), "Backup gagal: " + message, Toast.LENGTH_SHORT).show());
                        }
                    });
                }
            }
    );

    private final ActivityResultLauncher<String[]> restoreBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    viewModel.restore(requireContext(), uri, new BackupRepository.BackupCallback() {
                        @Override
                        public void onSuccess() {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Restore berhasil. Aplikasi akan dimuat ulang.", Toast.LENGTH_LONG).show();
                                // Restart app to refresh all data
                                Intent intent = new Intent(requireActivity(), com.rajinkas.ui.SplashActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            });
                        }

                        @Override
                        public void onError(String message) {
                            requireActivity().runOnUiThread(() -> 
                                Toast.makeText(requireContext(), "Restore gagal: " + message, Toast.LENGTH_SHORT).show());
                        }
                    });
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = SessionManager.getInstance(requireContext());
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // FIX: Set initial state from SharedPreferences immediately to prevent flickering
        updateThemeToggle(viewModel.getThemeModeSync());
        
        setupListeners();
    }

    private void updateThemeToggle(String mode) {
        if ("light".equals(mode)) {
            binding.toggleTheme.check(R.id.btnThemeLight);
        } else if ("dark".equals(mode)) {
            binding.toggleTheme.check(R.id.btnThemeDark);
        } else {
            binding.toggleTheme.check(R.id.btnThemeFollowSystem);
        }
    }

    private void setupListeners() {
        binding.btnAuditLog.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_audit_log);
        });

        binding.btnCategories.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_categories);
        });

        binding.btnDuesConfig.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_dues_config);
        });

        binding.btnBackup.setOnClickListener(v -> {
            createBackupLauncher.launch("rajinkas_backup_" + System.currentTimeMillis() + ".json");
        });

        binding.btnRestore.setOnClickListener(v -> {
            restoreBackupLauncher.launch(new String[]{"application/json", "application/octet-stream"});
        });

        binding.toggleTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            // FIX: Only trigger if the view is actually pressed by the user
            // This prevents an infinite loop when 'check()' is called programmatically
            if (!isChecked || !group.findViewById(checkedId).isPressed()) return;

            if (checkedId == R.id.btnThemeLight) {
                viewModel.setTheme("light");
            } else if (checkedId == R.id.btnThemeDark) {
                viewModel.setTheme("dark");
            } else if (checkedId == R.id.btnThemeFollowSystem) {
                viewModel.setTheme("system");
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
