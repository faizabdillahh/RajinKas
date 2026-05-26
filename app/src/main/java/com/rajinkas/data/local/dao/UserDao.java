package com.rajinkas.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rajinkas.data.local.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<UserEntity> getUserById(int id);

    @Query("SELECT * FROM users WHERE is_active = 1")
    LiveData<List<UserEntity>> getAllActiveUsers();

    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsersSync();

    @Insert
    void insertAll(List<UserEntity> users);
}
