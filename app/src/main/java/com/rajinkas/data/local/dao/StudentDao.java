package com.rajinkas.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rajinkas.data.local.entity.StudentEntity;

import java.util.List;

@Dao
public interface StudentDao {
    @Insert
    long insert(StudentEntity student);

    @Update
    void update(StudentEntity student);

    @Query("SELECT * FROM students WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<StudentEntity>> getAllActiveStudents();

    @Query("SELECT * FROM students WHERE is_active = 1")
    List<StudentEntity> getAllActiveStudentsSync();

    @Query("SELECT * FROM students WHERE id = :id")
    LiveData<StudentEntity> getStudentById(int id);

    @Query("SELECT * FROM students WHERE id = :id")
    StudentEntity getByIdSync(int id);

    @Query("UPDATE students SET is_active = 0 WHERE id = :id")
    void softDelete(int id);

    @Query("SELECT * FROM students WHERE is_active = 1 AND (name LIKE '%' || :query || '%' OR student_number LIKE '%' || :query || '%') ORDER BY name ASC")
    LiveData<List<StudentEntity>> searchStudents(String query);

    @Query("SELECT * FROM students")
    List<StudentEntity> getAllStudentsSync();

    @Query("SELECT student_number FROM students WHERE is_active = 1")
    List<String> getAllActiveStudentNumbersSync();

    @Query("SELECT COUNT(*) FROM students WHERE is_active = 1")
    int getActiveStudentCountSync();

    @Insert
    void insertAll(List<StudentEntity> students);
}
