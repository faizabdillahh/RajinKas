package com.rajinkas.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.entity.AppSettingEntity;
import com.rajinkas.data.repository.AppSettingRepository;
import com.rajinkas.data.repository.BackupRepository;

import android.net.Uri;
import android.content.Context;

public class SettingsViewModel extends AndroidViewModel {
    private final AppSettingRepository repository;
    private final BackupRepository backupRepository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        repository = new AppSettingRepository(application);
        backupRepository = new BackupRepository(application);
    }

    public LiveData<AppSettingEntity> getThemeSetting() {
        return repository.getSetting("theme_mode");
    }

    public String getThemeModeSync() {
        return getApplication().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
                .getString("theme_mode", "system");
    }

    public void setTheme(String mode) {
        // High performance theme saving for app startup - Use commit() to ensure it's saved before recreation
        getApplication().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
                .edit().putString("theme_mode", mode).commit();

        repository.saveSetting("theme_mode", mode);
        applyTheme(mode);
    }

    public void backup(Context context, Uri uri, BackupRepository.BackupCallback callback) {
        backupRepository.backupToUri(context, uri, callback);
    }

    public void restore(Context context, Uri uri, BackupRepository.BackupCallback callback) {
        backupRepository.restoreFromUri(context, uri, callback);
    }

    public void applyTheme(String mode) {
        if ("light".equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("dark".equals(mode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
