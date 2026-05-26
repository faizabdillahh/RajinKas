package com.rajinkas;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.rajinkas.data.repository.AppSettingRepository;
import com.rajinkas.data.local.RajinKasDatabase;
import com.rajinkas.worker.BackupReminderWorker;
import com.rajinkas.worker.DuesReminderWorker;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class RajinKasApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        applySavedTheme();
        
        // Optimization: Defer WorkManager initialization to avoid blocking startup
        RajinKasDatabase.databaseWriteExecutor.execute(() -> {
            scheduleBackupReminder();
            scheduleDuesReminder();
        });
    }

    private void scheduleDuesReminder() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(DuesReminderWorker.class, 7, TimeUnit.DAYS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "dues_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    private void scheduleBackupReminder() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(BackupReminderWorker.class, 7, TimeUnit.DAYS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "backup_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    private void applySavedTheme() {
        // High performance theme application on startup
        String mode = getSharedPreferences("theme_prefs", MODE_PRIVATE)
                .getString("theme_mode", "system");
        
        if ("light".equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("dark".equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
