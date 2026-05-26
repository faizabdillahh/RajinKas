package com.rajinkas.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.rajinkas.data.local.dao.AppSettingDao;
import com.rajinkas.data.local.dao.AuditLogDao;
import com.rajinkas.data.local.dao.CategoryDao;
import com.rajinkas.data.local.dao.DuesConfigDao;
import com.rajinkas.data.local.dao.DuesPaymentDao;
import com.rajinkas.data.local.dao.StudentDao;
import com.rajinkas.data.local.dao.TransactionDao;
import com.rajinkas.data.local.dao.UserDao;
import com.rajinkas.data.local.entity.AppSettingEntity;
import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.data.local.entity.CategoryEntity;
import com.rajinkas.data.local.entity.DuesConfigEntity;
import com.rajinkas.data.local.entity.DuesPaymentEntity;
import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.data.local.entity.TransactionEntity;
import com.rajinkas.data.local.entity.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        UserEntity.class,
        StudentEntity.class,
        DuesConfigEntity.class,
        CategoryEntity.class,
        TransactionEntity.class,
        DuesPaymentEntity.class,
        AuditLogEntity.class,
    AppSettingEntity.class
}, version = 3, exportSchema = false)
public abstract class RajinKasDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract StudentDao studentDao();
    public abstract DuesConfigDao duesConfigDao();
    public abstract CategoryDao categoryDao();
    public abstract TransactionDao transactionDao();
    public abstract DuesPaymentDao duesPaymentDao();
    public abstract AuditLogDao auditLogDao();
    public abstract AppSettingDao appSettingDao();

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE categories ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE transactions ADD COLUMN is_active INTEGER NOT NULL DEFAULT 1");
        }
    };

    private static volatile RajinKasDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static RajinKasDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RajinKasDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    RajinKasDatabase.class, "rajinkas_db")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
