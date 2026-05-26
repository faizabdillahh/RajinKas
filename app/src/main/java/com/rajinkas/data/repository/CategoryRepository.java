package com.rajinkas.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.RajinKasDatabase;
import com.rajinkas.data.local.dao.AuditLogDao;
import com.rajinkas.data.local.dao.CategoryDao;
import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.data.local.entity.CategoryEntity;
import com.rajinkas.util.AppConstants;
import com.rajinkas.util.DateUtils;
import com.rajinkas.util.JsonUtils;
import com.rajinkas.util.UuidUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class CategoryRepository {
    private final CategoryDao categoryDao;
    private final AuditLogDao auditLogDao;
    private final RajinKasDatabase database;
    private final ExecutorService executorService;

    public CategoryRepository(Application application) {
        database = RajinKasDatabase.getDatabase(application);
        categoryDao = database.categoryDao();
        auditLogDao = database.auditLogDao();
        executorService = RajinKasDatabase.databaseWriteExecutor;
    }

    public LiveData<List<CategoryEntity>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public LiveData<List<CategoryEntity>> getAllActiveCategories() {
        return categoryDao.getAllActiveCategories();
    }

    public LiveData<List<CategoryEntity>> getActiveCategoriesByType(String type) {
        return categoryDao.getActiveCategoriesByType(type);
    }

    public void insert(CategoryEntity category, int actorUserId) {
        executorService.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                category.setUuid(UuidUtils.generateUuid());
                category.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                category.setIsActive(1);
                category.setCreatedAt(now);
                category.setUpdatedAt(now);

                long newId = categoryDao.insert(category);

                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_CREATE);
                log.setEntityType("Category");
                log.setEntityId((int) newId);
                log.setEntityUuid(category.getUuid());
                log.setOldValue(null);
                log.setNewValue(JsonUtils.toJson(category));
                log.setDescription("Kategori baru: " + category.getName());
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);

                auditLogDao.insert(log);
            });
        });
    }

    public void update(CategoryEntity category, int actorUserId) {
        executorService.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                String oldValueJson = JsonUtils.toJson(categoryDao.getByIdSync(category.getId()));
                
                category.setUpdatedAt(now);
                category.setSyncStatus(AppConstants.SYNC_STATUS_PENDING_SYNC);

                categoryDao.update(category);

                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_UPDATE);
                log.setEntityType("Category");
                log.setEntityId(category.getId());
                log.setEntityUuid(category.getUuid());
                log.setOldValue(oldValueJson);
                log.setNewValue(JsonUtils.toJson(category));
                log.setDescription("Kategori diperbarui: " + category.getName());
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);

                auditLogDao.insert(log);
            });
        });
    }

    public void softDelete(CategoryEntity category, int actorUserId) {
        executorService.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                String oldValueJson = JsonUtils.toJson(categoryDao.getByIdSync(category.getId()));
                
                categoryDao.softDelete(category.getId());

                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_DELETE);
                log.setEntityType("Category");
                log.setEntityId(category.getId());
                log.setEntityUuid(category.getUuid());
                log.setOldValue(oldValueJson);
                log.setNewValue(null); // Or state it's inactive
                log.setDescription("Kategori dinonaktifkan: " + category.getName());
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);

                auditLogDao.insert(log);
            });
        });
    }

    public void seedDefaultCategories() {
        executorService.execute(() -> {
            // Check if categories already exist
            // For MVP, just simple check or use Room's OnConflictStrategy
            String now = DateUtils.nowIso8601();
            
            // Income Categories
            addDefaultCategory("Iuran Kelas", "INCOME", "#4CAF50", "ic_payments", now);
            addDefaultCategory("Donasi", "INCOME", "#8BC34A", "ic_volunteer", now);
            addDefaultCategory("Denda", "INCOME", "#FF9800", "ic_gavel", now);
            addDefaultCategory("Lain-lain Masuk", "INCOME", "#9E9E9E", "ic_more", now);

            // Expense Categories
            addDefaultCategory("Alat Tulis", "EXPENSE", "#F44336", "ic_edit", now);
            addDefaultCategory("Konsumsi", "EXPENSE", "#E91E63", "ic_restaurant", now);
            addDefaultCategory("Acara Kelas", "EXPENSE", "#9C27B0", "ic_event", now);
            addDefaultCategory("Kebersihan", "EXPENSE", "#3F51B5", "ic_cleaning", now);
            addDefaultCategory("Lain-lain Keluar", "EXPENSE", "#9E9E9E", "ic_more", now);
        });
    }

    private void addDefaultCategory(String name, String type, String color, String icon, String now) {
        CategoryEntity category = new CategoryEntity();
        category.setName(name);
        category.setType(type);
        category.setColor(color);
        category.setIcon(icon);
        category.setIsDefault(1);
        category.setIsActive(1);
        category.setUuid(UuidUtils.generateUuid());
        category.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        categoryDao.insert(category);
    }
}
