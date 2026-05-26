package com.rajinkas.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.RajinKasDatabase;
import com.rajinkas.data.local.dao.AppSettingDao;
import com.rajinkas.data.local.entity.AppSettingEntity;
import com.rajinkas.util.DateUtils;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public class AppSettingRepository {
    private final AppSettingDao appSettingDao;
    private final ExecutorService executorService;
    private static final Map<String, String> cache = new java.util.concurrent.ConcurrentHashMap<>();

    public AppSettingRepository(Application application) {
        RajinKasDatabase database = RajinKasDatabase.getDatabase(application);
        appSettingDao = database.appSettingDao();
        executorService = RajinKasDatabase.databaseWriteExecutor;
    }

    public void saveSetting(String key, String value) {
        cache.put(key, value);
        executorService.execute(() -> {
            AppSettingEntity setting = new AppSettingEntity();
            setting.setKey(key);
            setting.setValue(value);
            setting.setUpdatedAt(DateUtils.nowIso8601());
            appSettingDao.saveSetting(setting);
        });
    }

    public LiveData<AppSettingEntity> getSetting(String key) {
        return appSettingDao.getSetting(key);
    }

    public String getSettingSync(String key) {
        if (cache.containsKey(key)) return cache.get(key);
        AppSettingEntity setting = appSettingDao.getSettingSync(key);
        if (setting != null) {
            cache.put(key, setting.getValue());
            return setting.getValue();
        }
        return null;
    }

    public boolean isOnboardingComplete() {
        // This is tricky because getSettingSync is synchronous but must be called from background thread
        // Or we use a non-live data approach for startup checks
        AppSettingEntity setting = appSettingDao.getSettingSync("onboarding_complete");
        return setting != null && "true".equals(setting.getValue());
    }
}
