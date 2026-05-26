package com.rajinkas.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.data.repository.AuditLogRepository;

import java.util.List;

public class AuditLogViewModel extends AndroidViewModel {
    private final AuditLogRepository repository;

    public AuditLogViewModel(@NonNull Application application) {
        super(application);
        repository = new AuditLogRepository(application);
    }

    public LiveData<List<AuditLogEntity>> getAllLogs() {
        return repository.getAllLogs();
    }
}
