package com.rajinkas.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rajinkas.data.local.entity.DuesConfigEntity;

import java.util.List;

@Dao
public interface DuesConfigDao {
    @Insert
    long insert(DuesConfigEntity config);

    @Update
    void update(DuesConfigEntity config);

    @Query("SELECT * FROM dues_config WHERE id = :id")
    DuesConfigEntity getByIdSync(int id);

    @Query("UPDATE dues_config SET is_active = 0")
    void deactivateAll();

    @Query("SELECT * FROM dues_config WHERE is_active = 1")
    LiveData<List<DuesConfigEntity>> getActiveConfigs();

    @Query("SELECT * FROM dues_config WHERE is_active = 1")
    List<DuesConfigEntity> getActiveConfigsSync();

    @Query("SELECT * FROM dues_config ORDER BY created_at DESC")
    LiveData<List<DuesConfigEntity>> getAllConfigs();

    @Query("SELECT * FROM dues_config")
    List<DuesConfigEntity> getAllConfigsSync();

    @Insert
    void insertAll(List<DuesConfigEntity> configs);
}
