package com.rajinkas.ui.transactions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rajinkas.data.local.RajinKasDatabase;
import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.data.local.entity.CategoryEntity;
import com.rajinkas.data.local.entity.DuesConfigEntity;
import com.rajinkas.data.local.entity.DuesPaymentEntity;
import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.data.repository.DuesRepository;
import com.rajinkas.data.repository.StudentRepository;
import com.rajinkas.data.repository.TransactionRepository;
import com.rajinkas.util.AppConstants;
import com.rajinkas.util.DateUtils;
import com.rajinkas.util.JsonUtils;
import com.rajinkas.util.UuidUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class BulkPaymentViewModel extends AndroidViewModel {
    private final StudentRepository studentRepository;
    private final TransactionRepository transactionRepository;
    private final DuesRepository duesRepository;
    private final RajinKasDatabase database;
    private final ExecutorService executorService;

    public BulkPaymentViewModel(@NonNull Application application) {
        super(application);
        studentRepository = new StudentRepository(application);
        transactionRepository = new TransactionRepository(application);
        duesRepository = new DuesRepository(application);
        database = RajinKasDatabase.getDatabase(application);
        executorService = RajinKasDatabase.databaseWriteExecutor;
    }

    public LiveData<List<DuesConfigEntity>> getActiveDuesConfigs() {
        return duesRepository.getActiveConfigs();
    }

    public LiveData<List<StudentEntity>> getStudentsWhoNotPaid(int configId, String periodLabel) {
        // We can get this from database or filter in memory. 
        // Better to have a DAO method.
        return database.duesPaymentDao().getStudentsWhoNotPaid(configId, periodLabel);
    }

    public void processBulkPayment(List<StudentEntity> selectedStudents, DuesConfigEntity config, String periodLabel, int actorUserId) {
        executorService.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                double amount = config.getAmount();

                // Find "Iuran Kelas" category
                List<CategoryEntity> categories = database.categoryDao().getAllCategoriesSync();
                int categoryId = 1; // Default fallback
                for (CategoryEntity cat : categories) {
                    if ("Iuran Kelas".equals(cat.getName())) {
                        categoryId = cat.getId();
                        break;
                    }
                }

                for (StudentEntity student : selectedStudents) {
                    // 1. Create Transaction
                    TransactionEntity transaction = new TransactionEntity();
                    transaction.setUuid(UuidUtils.generateUuid());
                    transaction.setAmount(amount);
                    transaction.setType("INCOME");
                    transaction.setCategoryId(categoryId);
                    transaction.setStudentId(student.getId());
                    transaction.setDescription("Iuran " + periodLabel + " - " + student.getName());
                    transaction.setTransactionDate(now.substring(0, 10));
                    transaction.setCreatedBy(actorUserId);
                    transaction.setCreatedAt(now);
                    transaction.setUpdatedAt(now);
                    transaction.setIsActive(1);
                    transaction.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                    
                    long transId = database.transactionDao().insert(transaction);

                    // 2. Create/Update Dues Payment
                    DuesPaymentEntity payment = database.duesPaymentDao().getPayment(student.getId(), periodLabel);
                    if (payment == null) {
                        payment = new DuesPaymentEntity();
                        payment.setUuid(UuidUtils.generateUuid());
                        payment.setStudentId(student.getId());
                        payment.setDuesConfigId(config.getId());
                        payment.setCreatedAt(now);
                    }
                    
                    payment.setTransactionId((int) transId);
                    payment.setPeriodLabel(periodLabel);
                    payment.setStatus("PAID");
                    payment.setPaidAt(now);
                    payment.setUpdatedAt(now);
                    payment.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);

                    if (payment.getId() > 0) {
                        database.duesPaymentDao().update(payment);
                    } else {
                        database.duesPaymentDao().insert(payment);
                    }
                }

                // 3. Audit Log
                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_BULK_PAYMENT);
                log.setEntityType("Transaction");
                log.setDescription("Penarikan kas massal: " + selectedStudents.size() + " siswa untuk " + periodLabel);
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                database.auditLogDao().insert(log);
            });
        });
    }
}
