package com.rajinkas.ui.reports;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.entity.DuesPaymentEntity;
import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.data.local.model.DuesPaymentWithConfig;
import com.rajinkas.data.repository.DuesRepository;

import java.util.List;

public class ArrearsViewModel extends AndroidViewModel {
    private final DuesRepository repository;

    public ArrearsViewModel(@NonNull Application application) {
        super(application);
        repository = new DuesRepository(application);
    }

    public LiveData<List<StudentEntity>> getAllStudentsWithArrears() {
        return repository.getAllStudentsWithArrears();
    }

    public LiveData<List<DuesPaymentWithConfig>> getUnpaidPaymentsWithConfigByStudent(int studentId) {
        return repository.getUnpaidPaymentsWithConfigByStudent(studentId);
    }

    public LiveData<List<DuesPaymentWithConfig>> getPaidPaymentsWithConfigByStudent(int studentId) {
        return repository.getPaidPaymentsWithConfigByStudent(studentId);
    }

    public void payArrears(DuesPaymentEntity payment, int actorUserId) {
        repository.payArrears(payment, actorUserId);
    }
}
