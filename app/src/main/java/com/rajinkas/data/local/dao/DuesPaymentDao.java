package com.rajinkas.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.rajinkas.data.local.entity.DuesPaymentEntity;
import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.data.local.model.DuesPaymentWithConfig;

import java.util.List;

@Dao
public interface DuesPaymentDao {
    @Insert
    long insert(DuesPaymentEntity payment);

    @Update
    void update(DuesPaymentEntity payment);

    @Transaction
    @Query("SELECT * FROM dues_payments ORDER BY period_label DESC")
    LiveData<List<DuesPaymentWithConfig>> getAllPaymentsWithConfig();

    @Transaction
    @Query("SELECT * FROM dues_payments WHERE student_id = :studentId AND status = :status ORDER BY period_label ASC")
    LiveData<List<DuesPaymentWithConfig>> getPaymentsWithConfigByStudent(int studentId, String status);

    @Query("SELECT * FROM dues_payments WHERE student_id = :studentId ORDER BY period_start DESC")
    LiveData<List<DuesPaymentEntity>> getPaymentsByStudent(int studentId);

    @Query("SELECT * FROM dues_payments WHERE period_label = :periodLabel")
    LiveData<List<DuesPaymentEntity>> getPaymentsByPeriod(String periodLabel);

    @Query("SELECT s.* FROM students s JOIN dues_payments dp ON s.id = dp.student_id WHERE dp.status = 'UNPAID' AND s.is_active = 1 GROUP BY s.id")
    LiveData<List<StudentEntity>> getAllStudentsWithArrears();

    @Query("SELECT * FROM dues_payments WHERE student_id = :studentId AND status = 'UNPAID' ORDER BY period_label ASC")
    LiveData<List<DuesPaymentEntity>> getUnpaidPaymentsByStudent(int studentId);

    @Query("SELECT * FROM dues_payments WHERE student_id = :studentId AND status = 'PAID' ORDER BY paid_at DESC")
    LiveData<List<DuesPaymentEntity>> getPaidPaymentsByStudent(int studentId);

    @Query("SELECT * FROM dues_payments WHERE student_id = :studentId AND status = 'UNPAID' ORDER BY period_label ASC")
    List<DuesPaymentEntity> getUnpaidPaymentsByStudentSync(int studentId);

    @Query("SELECT * FROM dues_payments WHERE student_id = :studentId AND period_label = :periodLabel LIMIT 1")
    DuesPaymentEntity getPayment(int studentId, String periodLabel);

    @Query("SELECT COUNT(*) FROM dues_payments WHERE period_label = :periodLabel AND status = 'PAID'")
    LiveData<Integer> getPaidCount(String periodLabel);

    @Query("SELECT COUNT(*) FROM dues_payments WHERE period_label = :periodLabel AND status = 'UNPAID'")
    LiveData<Integer> getUnpaidCount(String periodLabel);

    @Query("SELECT SUM(dc.amount) FROM dues_payments dp JOIN dues_config dc ON dp.dues_config_id = dc.id WHERE dp.status = 'UNPAID'")
    LiveData<Double> getTotalArrears();

    @Query("SELECT s.* FROM students s LEFT JOIN dues_payments dp ON s.id = dp.student_id AND dp.dues_config_id = :configId AND dp.period_label = :periodLabel WHERE (dp.status IS NULL OR dp.status = 'UNPAID') AND s.is_active = 1 ORDER BY s.name ASC")
    LiveData<List<StudentEntity>> getStudentsWhoNotPaid(int configId, String periodLabel);

    @Query("SELECT * FROM dues_payments")
    List<DuesPaymentEntity> getAllPaymentsSync();

    @Insert
    void insertAll(List<DuesPaymentEntity> payments);
}
