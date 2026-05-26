package com.rajinkas.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rajinkas.R;
import com.rajinkas.MainActivity;
import com.rajinkas.databinding.ActivityOnboardingBinding;
import com.rajinkas.util.ClickUtils;

public class OnboardingActivity extends AppCompatActivity {
    private ActivityOnboardingBinding binding;
    private AuthViewModel viewModel;
    private int currentStep = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        ClickUtils.applySingleClick(binding.btnNext, v -> {
            if (currentStep == 1) {
                if (validateStep1()) {
                    currentStep = 2;
                    updateUI();
                }
            } else if (currentStep == 2) {
                if (validateStep2()) {
                    finishOnboarding();
                }
            }
        });

        binding.btnBack.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                updateUI();
            }
        });

        viewModel.onboardingComplete.observe(this, complete -> {
            if (complete) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });
    }

    private void updateUI() {
        binding.step1.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        binding.step2.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        binding.btnBack.setVisibility(currentStep == 1 ? View.INVISIBLE : View.VISIBLE);
        binding.btnNext.setText(currentStep == 2 ? getString(R.string.finish) : getString(R.string.next));
    }

    private boolean validateStep1() {
        String className = binding.etClassName.getText().toString().trim();
        String schoolName = binding.etSchoolName.getText().toString().trim();
        String academicYear = binding.etAcademicYear.getText().toString().trim();

        if (className.isEmpty() || schoolName.isEmpty() || academicYear.isEmpty()) {
            Toast.makeText(this, R.string.empty_data_warning, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        String name = binding.etAdminName.getText().toString().trim();
        String username = binding.etAdminUsername.getText().toString().trim();
        String password = binding.etAdminPassword.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.empty_data_warning, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void finishOnboarding() {
        viewModel.completeOnboarding(
                binding.etClassName.getText().toString().trim(),
                binding.etSchoolName.getText().toString().trim(),
                binding.etAcademicYear.getText().toString().trim(),
                binding.etAdminName.getText().toString().trim(),
                binding.etAdminUsername.getText().toString().trim(),
                binding.etAdminPassword.getText().toString().trim()
        );
    }
}
