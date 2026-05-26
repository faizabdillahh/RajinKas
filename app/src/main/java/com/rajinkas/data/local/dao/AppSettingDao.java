package com.rajinkas.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.rajinkas.data.local.entity.AppSettingEntity;

import java.util.List;

@Dao
public interface AppSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveSetting(AppSettingEntity setting);

    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    LiveData<AppSettingEntity> getSetting(String key);

    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    AppSettingEntity getSettingSync(String key);

    @Query("SELECT * FROM app_settings")
    LiveData<List<AppSettingEntity>> getAllSettings();

    @Query("SELECT * FROM app_settings")
    List<AppSettingEntity> getAllSettingsSync();

    @Insert
    void insertAll(List<AppSettingEntity> settings);
}
