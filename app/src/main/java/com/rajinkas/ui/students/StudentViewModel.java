package com.rajinkas.ui.students;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.rajinkas.data.local.entity.StudentEntity;
import com.rajinkas.data.repository.StudentRepository;

import java.util.List;

public class StudentViewModel extends AndroidViewModel {
    private final StudentRepository repository;
    private final LiveData<List<StudentEntity>> allActiveStudents;

    public StudentViewModel(@NonNull Application application) {
        super(application);
        repository = new StudentRepository(application);
        allActiveStudents = repository.getAllActiveStudents();
    }

    public LiveData<List<StudentEntity>> getAllActiveStudents() {
        return allActiveStudents;
    }

    public LiveData<List<StudentEntity>> searchStudents(String query) {
        if (query == null || query.trim().isEmpty()) {
            return allActiveStudents;
        }
        return repository.searchStudents(query);
    }

    public int getNextStudentNumber() {
        List<StudentEntity> current = allActiveStudents.getValue();
        if (current == null || current.isEmpty()) return 1;
        int max = 0;
        for (StudentEntity s : current) {
            try {
                int n = Integer.parseInt(s.getStudentNumber());
                if (n > max) max = n;
            } catch (NumberFormatException ignored) {}
        }
        return max + 1;
    }

    public void insert(StudentEntity student, int actorUserId) {
        repository.insert(student, actorUserId);
    }

    public void update(StudentEntity student, int actorUserId) {
        repository.update(student, actorUserId);
    }

    public void softDelete(StudentEntity student, int actorUserId) {
        repository.softDelete(student.getId(), student.getName(), student.getUuid(), actorUserId);
    }

    public String validateStudentNumber(String numberStr, Integer currentId) {
        int number;
        try {
            number = Integer.parseInt(numberStr);
        } catch (NumberFormatException e) {
            return "Nomor absen harus berupa angka";
        }

        if (number <= 0) {
            return "Nomor absen tidak boleh 0 atau minus";
        }

        List<StudentEntity> currentStudents = allActiveStudents.getValue();
        if (currentStudents == null) return null;

        // Check for duplicates
        for (StudentEntity s : currentStudents) {
            if (s.getStudentNumber().equals(numberStr)) {
                if (currentId == null || s.getId() != currentId) {
                    return "Nomor absen " + numberStr + " sudah digunakan oleh " + s.getName();
                }
            }
        }

        // Check for gaps (lompat-lompat)
        // Rule: A new number can only be added if it is exactly count + 1
        // Or if editing, it must already exist in the sequence (handled by duplicate check above)
        
        int max = 0;
        for (StudentEntity s : currentStudents) {
            try {
                int n = Integer.parseInt(s.getStudentNumber());
                if (n > max) max = n;
            } catch (NumberFormatException ignored) {
            }
        }

        if (currentId == null) {
            // Adding new
            if (number != max + 1) {
                return "Nomor absen harus urut. Nomor selanjutnya adalah " + (max + 1);
            }
        } else {
            // Editing existing
            // Allow changing name but if changing number, check if it's within range and not duplicate
            if (number > max) {
                return "Nomor absen tidak boleh melebihi jumlah siswa (" + max + ")";
            }
        }

        return null;
    }
}
