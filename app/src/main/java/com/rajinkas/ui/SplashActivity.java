package com.rajinkas.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rajinkas.MainActivity;
import com.rajinkas.ui.auth.AuthViewModel;
import com.rajinkas.ui.auth.LoginActivity;
import com.rajinkas.ui.auth.OnboardingActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthViewModel viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        viewModel.onboardingComplete.observe(this, complete -> {
            if (complete == null) return;

            if (complete) {
                if (viewModel.isLoggedIn()) {
                    startActivity(new Intent(this, MainActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
            } else {
                startActivity(new Intent(this, OnboardingActivity.class));
            }
            finish();
        });

        viewModel.checkOnboardingStatus();
    }
}
