package com.rajinkas.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.entity.DuesConfigEntity;
import com.rajinkas.data.repository.DuesRepository;

import java.util.List;

public class DuesConfigViewModel extends AndroidViewModel {
    private final DuesRepository repository;

    public DuesConfigViewModel(@NonNull Application application) {
        super(application);
        repository = new DuesRepository(application);
    }

    public LiveData<List<DuesConfigEntity>> getAllConfigs() {
        return repository.getAllConfigs();
    }

    public LiveData<List<DuesConfigEntity>> getActiveConfigs() {
        return repository.getActiveConfigs();
    }

    public void insertConfig(DuesConfigEntity config, int actorUserId) {
        repository.insertConfig(config, actorUserId);
    }

    public void updateConfig(DuesConfigEntity config, int actorUserId) {
        repository.updateConfig(config, actorUserId);
    }

    public void toggleActive(int configId, int actorUserId) {
        repository.toggleActive(configId, actorUserId);
    }
}
