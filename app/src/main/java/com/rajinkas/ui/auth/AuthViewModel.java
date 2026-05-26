package com.rajinkas.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rajinkas.data.local.RajinKasDatabase;
import com.rajinkas.data.local.SessionManager;
import com.rajinkas.data.local.entity.UserEntity;
import com.rajinkas.data.repository.AppSettingRepository;
import com.rajinkas.data.repository.CategoryRepository;
import com.rajinkas.data.repository.DuesRepository;
import com.rajinkas.data.repository.UserRepository;
import com.rajinkas.util.AppConstants;

public class AuthViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final AppSettingRepository appSettingRepository;
    private final CategoryRepository categoryRepository;
    private final DuesRepository duesRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<Boolean> _onboardingComplete = new MutableLiveData<>();
    public LiveData<Boolean> onboardingComplete = _onboardingComplete;

    private final MutableLiveData<UserEntity> _loginResult = new MutableLiveData<>();
    public LiveData<UserEntity> loginResult = _loginResult;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        appSettingRepository = new AppSettingRepository(application);
        categoryRepository = new CategoryRepository(application);
        duesRepository = new DuesRepository(application);
        sessionManager = SessionManager.getInstance(application);
    }

    public void checkOnboardingStatus() {
        // Run on background thread
        RajinKasDatabase.databaseWriteExecutor.execute(() -> {
            boolean complete = appSettingRepository.isOnboardingComplete();
            _onboardingComplete.postValue(complete);
        });
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public void login(String username, String password) {
        RajinKasDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity user = userRepository.login(username, password);
            if (user != null) {
                sessionManager.createSession(user.getId(), user.getName(), user.getRole());
            }
            _loginResult.postValue(user);
        });
    }

    public void completeOnboarding(String className, String schoolName, String academicYear, 
                                   String adminName, String adminUsername, String adminPassword) {
        RajinKasDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Save App Settings
            appSettingRepository.saveSetting("class_name", className);
            appSettingRepository.saveSetting("school_name", schoolName);
            appSettingRepository.saveSetting("academic_year", academicYear);
            
            // 2. Create Admin (Bendahara)
            UserEntity admin = new UserEntity();
            admin.setName(adminName);
            admin.setUsername(adminUsername);
            admin.setRole(AppConstants.ROLE_BENDAHARA);
            // Actor is 0 for initial setup
            userRepository.insert(admin, adminPassword, 0);

            // 3. Seed Defaults
            categoryRepository.seedDefaultCategories();
            duesRepository.seedDefaultDuesConfig();

            // 4. Mark Onboarding as Complete
            appSettingRepository.saveSetting("onboarding_complete", "true");
            
            _onboardingComplete.postValue(true);
        });
    }
}
