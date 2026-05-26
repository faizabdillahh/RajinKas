package com.rajinkas.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "audit_logs")
public class AuditLogEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "uuid")
    private String uuid;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "action")
    private String action;

    @ColumnInfo(name = "entity_type")
    private String entityType;

    @ColumnInfo(name = "entity_id")
    private int entityId;

    @ColumnInfo(name = "entity_uuid")
    private String entityUuid;

    @ColumnInfo(name = "old_value")
    private String oldValue;

    @ColumnInfo(name = "new_value")
    private String newValue;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "created_at")
    private String createdAt;

    @ColumnInfo(name = "sync_status")
    private String syncStatus;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public int getEntityId() { return entityId; }
    public void setEntityId(int entityId) { this.entityId = entityId; }

    public String getEntityUuid() { return entityUuid; }
    public void setEntityUuid(String entityUuid) { this.entityUuid = entityUuid; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}
