package com.rajinkas.ui.transactions;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.entity.CategoryEntity;
import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.data.repository.CategoryRepository;
import com.rajinkas.data.repository.TransactionRepository;

import java.util.List;

public class TransactionViewModel extends AndroidViewModel {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        categoryRepository = new CategoryRepository(application);
    }

    public LiveData<List<TransactionEntity>> getAllTransactions() {
        return transactionRepository.getAllTransactions();
    }

    public LiveData<List<TransactionEntity>> searchTransactions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return transactionRepository.getAllTransactions();
        }
        return transactionRepository.searchTransactions(query);
    }

    public LiveData<List<CategoryEntity>> getAllCategories() {
        return categoryRepository.getAllCategories();
    }

    public LiveData<Double> getTotalBalance() {
        return transactionRepository.getTotalBalance();
    }

    public void insertTransaction(TransactionEntity transaction, int actorUserId) {
        transactionRepository.insert(transaction, actorUserId);
    }

    public void softDeleteTransaction(TransactionEntity transaction, int actorUserId) {
        transactionRepository.softDelete(transaction, actorUserId);
    }
}
