package com.rajinkas.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.rajinkas.data.local.entity.AuditLogEntity;

import java.util.List;

@Dao
public interface AuditLogDao {
    @Insert
    long insert(AuditLogEntity log);

    @Query("SELECT * FROM audit_logs ORDER BY created_at DESC")
    LiveData<List<AuditLogEntity>> getAllLogs();

    @Query("SELECT * FROM audit_logs WHERE entity_type = :entityType AND entity_id = :entityId ORDER BY created_at DESC")
    LiveData<List<AuditLogEntity>> getLogsForEntity(String entityType, int entityId);

    @Query("SELECT * FROM audit_logs")
    List<AuditLogEntity> getAllLogsSync();

    @Insert
    void insertAll(List<AuditLogEntity> logs);
}
