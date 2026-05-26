package com.rajinkas.ui.reports;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.entity.DuesPaymentEntity;
import com.rajinkas.data.local.model.DuesPaymentWithConfig;
import com.rajinkas.data.repository.DuesRepository;

import java.util.List;

public class DuesViewModel extends AndroidViewModel {
    private final DuesRepository repository;

    public DuesViewModel(@NonNull Application application) {
        super(application);
        repository = new DuesRepository(application);
    }

    public LiveData<List<DuesPaymentWithConfig>> getAllPaymentsWithConfig() {
        return repository.getAllPaymentsWithConfig();
    }

    public LiveData<List<DuesPaymentEntity>> getPaymentsByPeriod(String periodLabel) {
        return repository.getPaymentsByPeriod(periodLabel);
    }
}
