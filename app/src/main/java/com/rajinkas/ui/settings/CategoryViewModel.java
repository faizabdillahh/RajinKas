package com.rajinkas.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.entity.CategoryEntity;
import com.rajinkas.data.repository.CategoryRepository;

import java.util.List;

public class CategoryViewModel extends AndroidViewModel {
    private final CategoryRepository repository;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        repository = new CategoryRepository(application);
    }

    public LiveData<List<CategoryEntity>> getAllActiveCategories() {
        return repository.getAllActiveCategories();
    }

    public void insert(CategoryEntity category, int actorUserId) {
        repository.insert(category, actorUserId);
    }

    public void update(CategoryEntity category, int actorUserId) {
        repository.update(category, actorUserId);
    }

    public void softDelete(CategoryEntity category, int actorUserId) {
        repository.softDelete(category, actorUserId);
    }
}
