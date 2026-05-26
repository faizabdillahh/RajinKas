package com.rajinkas.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.RajinKasDatabase;
import com.rajinkas.data.local.dao.AuditLogDao;
import com.rajinkas.data.local.dao.UserDao;
import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.data.local.entity.UserEntity;
import com.rajinkas.util.AppConstants;
import com.rajinkas.util.DateUtils;
import com.rajinkas.util.JsonUtils;
import com.rajinkas.util.UuidUtils;

import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class UserRepository {
    private final UserDao userDao;
    private final AuditLogDao auditLogDao;
    private final RajinKasDatabase database;
    private final ExecutorService executorService;

    public UserRepository(Application application) {
        database = RajinKasDatabase.getDatabase(application);
        userDao = database.userDao();
        auditLogDao = database.auditLogDao();
        executorService = RajinKasDatabase.databaseWriteExecutor;
    }

    public LiveData<List<UserEntity>> getAllActiveUsers() {
        return userDao.getAllActiveUsers();
    }

    public LiveData<UserEntity> getUserById(int id) {
        return userDao.getUserById(id);
    }

    public void insert(UserEntity user, String plainPassword, int actorUserId) {
        executorService.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                user.setUuid(UuidUtils.generateUuid());
                user.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                user.setIsActive(1);

                // Hash password
                String salt = BCrypt.gensalt(10);
                String hash = BCrypt.hashpw(plainPassword, salt);
                user.setPasswordHash(hash);

                long newId = userDao.insert(user);

                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_CREATE);
                log.setEntityType("User");
                log.setEntityId((int) newId);
                log.setEntityUuid(user.getUuid());
                log.setOldValue(null);
                log.setNewValue(JsonUtils.toJson(user));
                log.setDescription("User baru ditambahkan: " + user.getName());
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);

                auditLogDao.insert(log);
            });
        });
    }

    public UserEntity login(String username, String password) {
        UserEntity user = userDao.getUserByUsername(username);
        if (user != null && user.getIsActive() == 1) {
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                // Log the login action
                executorService.execute(() -> {
                    String now = DateUtils.nowIso8601();
                    AuditLogEntity log = new AuditLogEntity();
                    log.setUuid(UuidUtils.generateUuid());
                    log.setUserId(user.getId());
                    log.setAction(AppConstants.ACTION_LOGIN);
                    log.setEntityType("User");
                    log.setEntityId(user.getId());
                    log.setEntityUuid(user.getUuid());
                    log.setDescription("User login: " + user.getUsername());
                    log.setCreatedAt(now);
                    log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                    auditLogDao.insert(log);
                });
                return user;
            }
        }
        return null;
    }

    // Add more methods as needed (update, delete with audit log)
}
