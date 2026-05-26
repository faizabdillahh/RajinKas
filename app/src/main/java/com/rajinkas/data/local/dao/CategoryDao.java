package com.rajinkas.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rajinkas.data.local.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(CategoryEntity category);

    @Update
    void update(CategoryEntity category);

    @Query("SELECT * FROM categories WHERE id = :id")
    CategoryEntity getByIdSync(int id);

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    LiveData<List<CategoryEntity>> getCategoriesByType(String type);

    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<CategoryEntity>> getAllCategories();

    @Query("SELECT * FROM categories WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<CategoryEntity>> getAllActiveCategories();

    @Query("SELECT * FROM categories WHERE type = :type AND is_active = 1 ORDER BY name ASC")
    LiveData<List<CategoryEntity>> getActiveCategoriesByType(String type);

    @Query("UPDATE categories SET is_active = 0 WHERE id = :id")
    void softDelete(int id);

    @Query("SELECT * FROM categories")
    List<CategoryEntity> getAllCategoriesSync();

    @Insert
    void insertAll(List<CategoryEntity> categories);
}
