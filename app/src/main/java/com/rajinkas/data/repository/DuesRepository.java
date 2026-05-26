package com.rajinkas.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.RajinKasDatabase;
import com.rajinkas.data.local.dao.AuditLogDao;
import com.rajinkas.data.local.dao.DuesConfigDao;
import com.rajinkas.data.local.dao.DuesPaymentDao;
import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.data.local.entity.CategoryEntity;
import com.rajinkas.data.local.entity.DuesConfigEntity;
import com.rajinkas.data.local.entity.DuesPaymentEntity;
import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.data.local.model.DuesPaymentWithConfig;
import com.rajinkas.util.AppConstants;
import com.rajinkas.util.DateUtils;
import com.rajinkas.util.JsonUtils;
import com.rajinkas.util.UuidUtils;

import java.util.List;

public class DuesRepository {
    private final DuesConfigDao duesConfigDao;
    private final DuesPaymentDao duesPaymentDao;
    private final AuditLogDao auditLogDao;
    private final RajinKasDatabase database;

    public DuesRepository(Application application) {
        database = RajinKasDatabase.getDatabase(application);
        duesConfigDao = database.duesConfigDao();
        duesPaymentDao = database.duesPaymentDao();
        auditLogDao = database.auditLogDao();
    }

    public LiveData<List<DuesConfigEntity>> getActiveConfigs() {
        return duesConfigDao.getActiveConfigs();
    }

    public LiveData<List<DuesConfigEntity>> getAllConfigs() {
        return duesConfigDao.getAllConfigs();
    }

    public LiveData<List<DuesPaymentEntity>> getPaymentsByPeriod(String periodLabel) {
        return duesPaymentDao.getPaymentsByPeriod(periodLabel);
    }

    public LiveData<Integer> getPaidCount(String periodLabel) {
        return duesPaymentDao.getPaidCount(periodLabel);
    }

    public LiveData<Integer> getUnpaidCount(String periodLabel) {
        return duesPaymentDao.getUnpaidCount(periodLabel);
    }

    public LiveData<Double> getTotalArrears() {
        return duesPaymentDao.getTotalArrears();
    }

    public LiveData<List<StudentEntity>> getAllStudentsWithArrears() {
        return duesPaymentDao.getAllStudentsWithArrears();
    }

    public LiveData<List<DuesPaymentWithConfig>> getAllPaymentsWithConfig() {
        return duesPaymentDao.getAllPaymentsWithConfig();
    }

    public LiveData<List<DuesPaymentWithConfig>> getUnpaidPaymentsWithConfigByStudent(int studentId) {
        return duesPaymentDao.getPaymentsWithConfigByStudent(studentId, "UNPAID");
    }

    public LiveData<List<DuesPaymentWithConfig>> getPaidPaymentsWithConfigByStudent(int studentId) {
        return duesPaymentDao.getPaymentsWithConfigByStudent(studentId, "PAID");
    }

    /**
     * Ensures all active students have UNPAID records for past weeks and 2 weeks ahead.
     */
    public void generateMissingArrears() {
        RajinKasDatabase.databaseWriteExecutor.execute(() -> {
            database.runInTransaction(() -> {
                List<DuesConfigEntity> activeConfigs = duesConfigDao.getActiveConfigsSync();
                if (activeConfigs == null || activeConfigs.isEmpty()) return;

                List<StudentEntity> students = database.studentDao().getAllStudentsSync();
                String now = DateUtils.nowIso8601();

                for (DuesConfigEntity config : activeConfigs) {
                    if ("WEEKLY".equals(config.getFrequency())) {
                        String startDate = config.getStartDate() != null ? config.getStartDate() : now.substring(0, 10);
                        List<String> requiredPeriods = DateUtils.generateWeekLabels(startDate, 2);

                        for (StudentEntity student : students) {
                            if (student.getIsActive() == 1) {
                                for (String period : requiredPeriods) {
                                    DuesPaymentEntity existing = duesPaymentDao.getPayment(student.getId(), period);
                                    if (existing == null) {
                                        createUnpaidRecord(student.getId(), config.getId(), period, now, config.getDueDate());
                                    }
                                }
                            }
                        }
                    } else if ("ONE_TIME".equals(config.getFrequency())) {
                        for (StudentEntity student : students) {
                            if (student.getIsActive() == 1) {
                                DuesPaymentEntity existing = duesPaymentDao.getPayment(student.getId(), config.getName());
                            if (existing == null) {
                                createUnpaidRecord(student.getId(), config.getId(), config.getName(), now, config.getDueDate());
                            }
                            }
                        }
                    }
                }
            });
        });
    }

    private void createUnpaidRecord(int studentId, int configId, String label, String now, String dueDate) {
        DuesPaymentEntity p = new DuesPaymentEntity();
        p.setUuid(UuidUtils.generateUuid());
        p.setStudentId(studentId);
        p.setDuesConfigId(configId);
        p.setPeriodLabel(label);
        p.setDueDate(dueDate);
        p.setStatus("UNPAID");
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        p.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
        database.duesPaymentDao().insert(p);
    }

    public void payArrears(DuesPaymentEntity payment, int actorUserId) {
        RajinKasDatabase.databaseWriteExecutor.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                DuesConfigEntity config = duesConfigDao.getByIdSync(payment.getDuesConfigId());
                StudentEntity student = database.studentDao().getByIdSync(payment.getStudentId());
                
                if (config == null || student == null) return;

                // 1. Create Transaction
                TransactionEntity transaction = new TransactionEntity();
                transaction.setUuid(UuidUtils.generateUuid());
                transaction.setAmount(config.getAmount());
                transaction.setType("INCOME");
                
                // Find "Iuran Kelas" category
                List<CategoryEntity> categories = database.categoryDao().getAllCategoriesSync();
                int categoryId = 1;
                for (CategoryEntity cat : categories) {
                    if ("Iuran Kelas".equals(cat.getName())) {
                        categoryId = cat.getId();
                        break;
                    }
                }
                
                transaction.setCategoryId(categoryId);
                transaction.setStudentId(student.getId());
                transaction.setDescription("Bayar " + config.getName() + ": " + payment.getPeriodLabel() + " - " + student.getName());
                transaction.setTransactionDate(now.substring(0, 10));
                transaction.setCreatedBy(actorUserId);
                transaction.setCreatedAt(now);
                transaction.setUpdatedAt(now);
                transaction.setIsActive(1);
                transaction.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                
                long transId = database.transactionDao().insert(transaction);

                // 2. Update Payment status
                payment.setStatus("PAID");
                payment.setTransactionId((int) transId);
                payment.setPaidAt(now);
                payment.setUpdatedAt(now);
                duesPaymentDao.update(payment);

                // 3. Log it
                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_CREATE);
                log.setEntityType("Transaction");
                log.setDescription("Pelunasan tunggakan " + payment.getPeriodLabel() + " untuk " + student.getName());
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                database.auditLogDao().insert(log);
            });
        });
    }

    public void insertConfig(DuesConfigEntity config, int actorUserId) {
        RajinKasDatabase.databaseWriteExecutor.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                
                config.setUuid(UuidUtils.generateUuid());
                config.setCreatedAt(now);
                config.setUpdatedAt(now);
                config.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                
                long newId = duesConfigDao.insert(config);

                // Auto-generate arrears for the new active config
                if (config.getIsActive() == 1) {
                    generateMissingArrears();
                }

                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_CREATE);
                log.setEntityType("DuesConfig");
                log.setEntityId((int) newId);
                log.setEntityUuid(config.getUuid());
                log.setOldValue(null);
                log.setNewValue(JsonUtils.toJson(config));
                log.setDescription("Konfigurasi iuran baru: " + config.getName());
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);

                auditLogDao.insert(log);
            });
        });
    }

    public void updateConfig(DuesConfigEntity config, int actorUserId) {
        RajinKasDatabase.databaseWriteExecutor.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                
                config.setUpdatedAt(now);
                config.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                duesConfigDao.update(config);

                // Audit Log
                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_UPDATE);
                log.setEntityType("DuesConfig");
                log.setEntityId(config.getId());
                log.setEntityUuid(config.getUuid());
                log.setNewValue(JsonUtils.toJson(config));
                log.setDescription("Konfigurasi iuran '" + config.getName() + "' diperbarui.");
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                auditLogDao.insert(log);
            });
        });
    }

    public void toggleActive(int configId, int actorUserId) {
        RajinKasDatabase.databaseWriteExecutor.execute(() -> {
            database.runInTransaction(() -> {
                DuesConfigEntity config = duesConfigDao.getByIdSync(configId);
                if (config != null) {
                    int newStatus = config.getIsActive() == 1 ? 0 : 1;
                    config.setIsActive(newStatus);
                    config.setUpdatedAt(DateUtils.nowIso8601());
                    duesConfigDao.update(config);
                    
                    if (newStatus == 1) {
                        generateMissingArrears();
                    }

                    // 3. Log it
                    AuditLogEntity log = new AuditLogEntity();
                    log.setUuid(UuidUtils.generateUuid());
                    log.setUserId(actorUserId);
                    log.setAction(AppConstants.ACTION_UPDATE);
                    log.setEntityType("DuesConfig");
                    log.setEntityId(configId);
                    log.setEntityUuid(config.getUuid());
                    log.setDescription("Status konfigurasi '" + config.getName() + "' diubah menjadi " + (newStatus == 1 ? "Aktif" : "Non-aktif"));
                    log.setCreatedAt(DateUtils.nowIso8601());
                    log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                    auditLogDao.insert(log);
                }
            });
        });
    }

    public void seedDefaultDuesConfig() {
        RajinKasDatabase.databaseWriteExecutor.execute(() -> {
            String now = DateUtils.nowIso8601();
            DuesConfigEntity config = new DuesConfigEntity();
            config.setName("Iuran Mingguan");
            config.setAmount(5000);
            config.setFrequency("WEEKLY");
            config.setStartDate(now.substring(0, 10));
            config.setDueDate(""); // Empty by default
            config.setDueDay(1); // Monday
            config.setIsActive(1);

            config.setUuid(UuidUtils.generateUuid());
            config.setCreatedAt(now);
            config.setUpdatedAt(now);
            config.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
            duesConfigDao.insert(config);
            
            // Seed arrears
            generateMissingArrears();
        });
    }
}
