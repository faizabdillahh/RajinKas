package com.rajinkas.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.RajinKasDatabase;
import com.rajinkas.data.local.dao.AuditLogDao;
import com.rajinkas.data.local.entity.AuditLogEntity;

import java.util.List;

public class AuditLogRepository {
    private final AuditLogDao auditLogDao;

    public AuditLogRepository(Application application) {
        RajinKasDatabase database = RajinKasDatabase.getDatabase(application);
        auditLogDao = database.auditLogDao();
    }

    public LiveData<List<AuditLogEntity>> getAllLogs() {
        return auditLogDao.getAllLogs();
    }
}
