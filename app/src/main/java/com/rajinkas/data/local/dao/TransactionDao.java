package com.rajinkas.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rajinkas.data.local.entity.TransactionEntity;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    long insert(TransactionEntity transaction);

    @Update
    void update(TransactionEntity transaction);

    @Query("SELECT * FROM transactions WHERE id = :id")
    TransactionEntity getByIdSync(int id);

    @Query("UPDATE transactions SET is_active = 0 WHERE id = :id")
    void softDelete(int id);

    @Query("SELECT * FROM transactions WHERE is_active = 1 ORDER BY transaction_date DESC, created_at DESC")
    LiveData<List<TransactionEntity>> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE student_id = :studentId AND is_active = 1 ORDER BY transaction_date DESC")
    LiveData<List<TransactionEntity>> getTransactionsByStudent(int studentId);

    @Query("SELECT SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) - SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) FROM transactions WHERE is_active = 1")
    LiveData<Double> getTotalBalance();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND is_active = 1")
    LiveData<Double> getTotalIncome();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND is_active = 1")
    LiveData<Double> getTotalExpense();

    @Query("SELECT * FROM transactions WHERE is_active = 1 AND (description LIKE '%' || :query || '%' OR amount LIKE '%' || :query || '%') ORDER BY transaction_date DESC, created_at DESC")
    LiveData<List<TransactionEntity>> searchTransactions(String query);

    @Query("SELECT * FROM transactions")
    List<TransactionEntity> getAllTransactionsSync();

    @Insert
    void insertAll(List<TransactionEntity> transactions);
}
