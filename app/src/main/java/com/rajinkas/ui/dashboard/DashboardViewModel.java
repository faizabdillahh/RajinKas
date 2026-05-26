package com.rajinkas.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.RajinKasDatabase;
import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.data.repository.DuesRepository;
import com.rajinkas.data.repository.TransactionRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    private final TransactionRepository transactionRepository;
    private final DuesRepository duesRepository;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        duesRepository = new DuesRepository(application);
        
        // Ensure arrears are up to date for the current period
        duesRepository.generateMissingArrears();
    }

    public LiveData<Double> getTotalBalance() {
        return transactionRepository.getTotalBalance();
    }

    public LiveData<Double> getTotalIncome() {
        return transactionRepository.getTotalIncome();
    }

    public LiveData<Double> getTotalExpense() {
        return transactionRepository.getTotalExpense();
    }

    public LiveData<List<TransactionEntity>> getRecentTransactions() {
        return transactionRepository.getAllTransactions(); 
    }

    public LiveData<Integer> getPaidCount(String period) {
        return duesRepository.getPaidCount(period);
    }

    public LiveData<Integer> getUnpaidCount(String period) {
        return duesRepository.getUnpaidCount(period);
    }

    public LiveData<Double> getTotalArrears() {
        return duesRepository.getTotalArrears();
    }
}
