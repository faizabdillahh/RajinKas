package com.rajinkas.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.RajinKasDatabase;
import com.rajinkas.data.local.dao.AuditLogDao;
import com.rajinkas.data.local.dao.StudentDao;
import com.rajinkas.data.local.entity.AuditLogEntity;
import com.rajinkas.data.local.entity.DuesConfigEntity;
import com.rajinkas.data.local.entity.DuesPaymentEntity;
import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.util.AppConstants;
import com.rajinkas.util.DateUtils;
import com.rajinkas.util.JsonUtils;
import com.rajinkas.util.UuidUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class StudentRepository {
    private final StudentDao studentDao;
    private final AuditLogDao auditLogDao;
    private final RajinKasDatabase database;
    private final ExecutorService executorService;

    public StudentRepository(Application application) {
        database = RajinKasDatabase.getDatabase(application);
        studentDao = database.studentDao();
        auditLogDao = database.auditLogDao();
        executorService = RajinKasDatabase.databaseWriteExecutor;
    }

    public LiveData<List<StudentEntity>> getAllActiveStudents() {
        return studentDao.getAllActiveStudents();
    }

    public LiveData<List<StudentEntity>> searchStudents(String query) {
        return studentDao.searchStudents(query);
    }

    public void insert(StudentEntity student, int actorUserId) {
        executorService.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                student.setUuid(UuidUtils.generateUuid());
                student.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                student.setCreatedAt(now);
                student.setUpdatedAt(now);
                student.setIsActive(1);

                long newId = studentDao.insert(student);

                // Auto-generate arrears for all active configs
                List<DuesConfigEntity> activeConfigs = database.duesConfigDao().getActiveConfigsSync();
                if (activeConfigs != null) {
                    for (DuesConfigEntity config : activeConfigs) {
                        DuesPaymentEntity p = new DuesPaymentEntity();
                        p.setUuid(UuidUtils.generateUuid());
                        p.setStudentId((int) newId);
                        p.setDuesConfigId(config.getId());
                        p.setPeriodLabel(DateUtils.getCurrentPeriodLabel());
                        p.setDueDate(config.getDueDate());
                        p.setStatus("UNPAID");
                        p.setCreatedAt(now);
                        p.setUpdatedAt(now);
                        p.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);
                        database.duesPaymentDao().insert(p);
                    }
                }

                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_CREATE);
                log.setEntityType("Student");
                log.setEntityId((int) newId);
                log.setEntityUuid(student.getUuid());
                log.setOldValue(null);
                log.setNewValue(JsonUtils.toJson(student));
                log.setDescription("Siswa baru ditambahkan: " + student.getName());
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);

                auditLogDao.insert(log);
            });
        });
    }

    public void update(StudentEntity student, int actorUserId) {
        executorService.execute(() -> {
            // Get old value for audit log
            // Note: In a real app, you might want to fetch the current version from DB first if not already in memory
            // For simplicity in MVP, we assume 'student' object passed has the correct ID
            
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                student.setUpdatedAt(now);
                // studentDao.update(student); // Need to get old value first for meaningful audit log
                
                // For demonstration, just update
                studentDao.update(student);

                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_UPDATE);
                log.setEntityType("Student");
                log.setEntityId(student.getId());
                log.setEntityUuid(student.getUuid());
                log.setOldValue("Update action performed"); // Ideally JSON of old record
                log.setNewValue(JsonUtils.toJson(student));
                log.setDescription("Data siswa diubah: " + student.getName());
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);

                auditLogDao.insert(log);
            });
        });
    }

    public void softDelete(int studentId, String studentName, String studentUuid, int actorUserId) {
        executorService.execute(() -> {
            database.runInTransaction(() -> {
                String now = DateUtils.nowIso8601();
                studentDao.softDelete(studentId);

                // Re-index remaining students to prevent gaps
                List<StudentEntity> activeStudents = studentDao.getAllActiveStudentsSync();
                // Sort by current student number
                activeStudents.sort((s1, s2) -> {
                    try {
                        return Integer.parseInt(s1.getStudentNumber()) - Integer.parseInt(s2.getStudentNumber());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                });

                for (int i = 0; i < activeStudents.size(); i++) {
                    StudentEntity s = activeStudents.get(i);
                    String newNumber = String.valueOf(i + 1);
                    if (!s.getStudentNumber().equals(newNumber)) {
                        s.setStudentNumber(newNumber);
                        s.setUpdatedAt(now);
                        studentDao.update(s);
                    }
                }

                AuditLogEntity log = new AuditLogEntity();
                log.setUuid(UuidUtils.generateUuid());
                log.setUserId(actorUserId);
                log.setAction(AppConstants.ACTION_DELETE);
                log.setEntityType("Student");
                log.setEntityId(studentId);
                log.setEntityUuid(studentUuid);
                log.setOldValue("is_active = 1");
                log.setNewValue("is_active = 0");
                log.setDescription("Siswa dinonaktifkan: " + studentName);
                log.setCreatedAt(now);
                log.setSyncStatus(AppConstants.SYNC_STATUS_LOCAL_ONLY);

                auditLogDao.insert(log);
            });
        });
    }
}
