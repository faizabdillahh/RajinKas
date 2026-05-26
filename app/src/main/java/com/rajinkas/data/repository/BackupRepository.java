package com.rajinkas.data.repository;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.rajinkas.data.local.BackupData;
import com.rajinkas.data.local.RajinKasDatabase;
import com.rajinkas.data.local.entity.AppSettingEntity;
import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.data.local.entity.CategoryEntity;
import com.rajinkas.data.local.entity.DuesConfigEntity;
import com.rajinkas.data.local.entity.DuesPaymentEntity;
import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.data.local.entity.UserEntity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackupRepository {
    private final RajinKasDatabase database;
    private final Gson gson = new Gson();

    public BackupRepository(Application application) {
        this.database = RajinKasDatabase.getDatabase(application);
    }

    public void backupToUri(Context context, Uri uri, BackupCallback callback) {
        RajinKasDatabase.databaseWriteExecutor.execute(() -> {
            try {
                BackupData data = new BackupData();
                data.users = database.userDao().getAllUsersSync();
                data.students = database.studentDao().getAllStudentsSync();
                data.categories = database.categoryDao().getAllCategoriesSync();
                data.transactions = database.transactionDao().getAllTransactionsSync();
                data.duesConfigs = database.duesConfigDao().getAllConfigsSync();
                data.duesPayments = database.duesPaymentDao().getAllPaymentsSync();
                data.auditLogs = database.auditLogDao().getAllLogsSync();
                data.appSettings = database.appSettingDao().getAllSettingsSync();

                String json = gson.toJson(data);
                
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        outputStream.write(json.getBytes(StandardCharsets.UTF_8));
                        callback.onSuccess();
                    } else {
                        callback.onError("Could not open output stream");
                    }
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void restoreFromUri(Context context, Uri uri, BackupCallback callback) {
        RajinKasDatabase.databaseWriteExecutor.execute(() -> {
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                if (inputStream == null) {
                    callback.onError("Could not open input stream");
                    return;
                }

                BackupData data = gson.fromJson(new InputStreamReader(inputStream), BackupData.class);
                if (data == null) {
                    callback.onError("Invalid backup file");
                    return;
                }
                
                database.runInTransaction(() -> {
                    database.clearAllTables();

                    if (data.users != null) database.userDao().insertAll(data.users);
                    if (data.students != null) database.studentDao().insertAll(data.students);
                    if (data.categories != null) database.categoryDao().insertAll(data.categories);
                    if (data.transactions != null) database.transactionDao().insertAll(data.transactions);
                    if (data.duesConfigs != null) database.duesConfigDao().insertAll(data.duesConfigs);
                    if (data.duesPayments != null) database.duesPaymentDao().insertAll(data.duesPayments);
                    if (data.auditLogs != null) database.auditLogDao().insertAll(data.auditLogs);
                    if (data.appSettings != null) database.appSettingDao().insertAll(data.appSettings);
                });
                
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public interface BackupCallback {
        void onSuccess();
        void onError(String message);
    }
}
