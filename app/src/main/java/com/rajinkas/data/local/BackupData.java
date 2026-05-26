package com.rajinkas.data.local;

import com.rajinkas.data.local.entity.AppSettingEntity;
import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.data.local.entity.CategoryEntity;
import com.rajinkas.data.local.entity.DuesConfigEntity;
import com.rajinkas.data.local.entity.DuesPaymentEntity;
import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.data.local.entity.UserEntity;

import java.util.List;

public class BackupData {
    public List<UserEntity> users;
    public List<StudentEntity> students;
    public List<CategoryEntity> categories;
    public List<TransactionEntity> transactions;
    public List<DuesConfigEntity> duesConfigs;
    public List<DuesPaymentEntity> duesPayments;
    public List<AuditLogEntity> auditLogs;
    public List<AppSettingEntity> appSettings;
}
