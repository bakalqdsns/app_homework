package com.example.anifocus.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.anifocus.data.local.entity.TaskEntity;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    long insert(TaskEntity task);

    @Update
    void update(TaskEntity task);

    @Delete
    void delete(TaskEntity task);

    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    List<TaskEntity> getAllTasks();

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY deadline ASC")
    List<TaskEntity> getPendingTasks();

    @Query("SELECT * FROM tasks WHERE id = :id")
    TaskEntity getTaskById(long id);

    @Query("UPDATE tasks SET isCompleted = 1 WHERE id = :id")
    void markAsCompleted(long id);

    @Query("UPDATE tasks SET isCompleted = 0 WHERE id = :id")
    void markAsUncompleted(long id);

    @Query("UPDATE tasks SET progress = :progress WHERE id = :id")
    void updateProgress(long id, int progress);

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    int getPendingCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    int getCompletedCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0 AND progress > 0")
    int getInProgressCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0 AND progress >= 100")
    int getNearlyDoneCount();

    @Query("DELETE FROM tasks WHERE id = :id")
    void deleteById(long id);
}
