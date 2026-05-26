package com.rajinkas.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.RajinKasDatabase;
import com.rajinkas.data.local.dao.AuditLogDao;
import com.rajinkas.data.local.dao.TransactionDao;
import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.util.AppConstants;
import com.rajinkas.util.DateUtils;
import com.rajinkas.util.JsonUtils;
import com.rajinkas.util.UuidUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class TransactionRepository {
    private final TransactionDao transactionDao;
    private final AuditLogDao auditLogDao;
    private final RajinKasDatabase database;
    private final ExecutorService executorService;

    public TransactionRepository(Application application) {
        database = RajinKasDatabase.getDatabase(application);
        transactionDao = database.transactionDao();
        auditLogDao = database.auditLogDao();
        executorService = RajinKasDatabase.databaseWriteExecutor;
    }

    public LiveData<List<TransactionEntity>> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    public LiveData<List<TransactionEntity>> searchTransactions(String query) {
        return transactionDao.searchTransactions(query);
    }

    public LiveData<Double> getTotalBalance() {
        return transactionDao.getTotalBalance();
    }

    public LiveData<Double> getTotalIncome() {
        return transactionDao.getTotalIncome();
    }

    public LiveData<Double> getTotalExpense() {
        return transactionDao.getTotalExpense();
    }

    public void insert(TransactionEntity transaction, int actorUserId) {
        executorService.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                transaction.setUuid(UuidUtils.generateUuid());
                transaction.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                transaction.setIsActive(1);
                transaction.setCreatedAt(now);
                transaction.setUpdatedAt(now);

                long newId = transactionDao.insert(transaction);

                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_CREATE);
                log.setEntityType("Transaction");
                log.setEntityId((int) newId);
                log.setEntityUuid(transaction.getUuid());
                log.setOldValue(null);
                log.setNewValue(JsonUtils.toJson(transaction));
                log.setDescription("Transaksi baru: " + transaction.getDescription());
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);

                auditLogDao.insert(log);
            });
        });
    }

    public void update(TransactionEntity transaction, int actorUserId) {
        executorService.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                String oldValueJson = JsonUtils.toJson(transactionDao.getByIdSync(transaction.getId()));
                
                transaction.setUpdatedAt(now);
                transaction.setSyncStatus(AppConstants.SYNC_STATUS_PENDING_SYNC);

                transactionDao.update(transaction);

                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_UPDATE);
                log.setEntityType("Transaction");
                log.setEntityId(transaction.getId());
                log.setEntityUuid(transaction.getUuid());
                log.setOldValue(oldValueJson);
                log.setNewValue(JsonUtils.toJson(transaction));
                log.setDescription("Transaksi diperbarui: " + transaction.getDescription());
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);

                auditLogDao.insert(log);
            });
        });
    }

    public void softDelete(TransactionEntity transaction, int actorUserId) {
        executorService.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                String oldValueJson = JsonUtils.toJson(transactionDao.getByIdSync(transaction.getId()));

                transactionDao.softDelete(transaction.getId());

                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_DELETE);
                log.setEntityType("Transaction");
                log.setEntityId(transaction.getId());
                log.setEntityUuid(transaction.getUuid());
                log.setOldValue(oldValueJson);
                log.setNewValue(null);
                log.setDescription("Transaksi dihapus: " + transaction.getDescription());
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);

                auditLogDao.insert(log);
            });
        });
    }
}
