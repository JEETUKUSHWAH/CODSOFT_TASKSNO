package com.example.todoapplication;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    // Active tasks first (ordered by priority desc, then due date asc),
    // completed tasks last.
    @Query("SELECT * FROM tasks ORDER BY completed ASC, priority DESC, " +
            "CASE WHEN due_date = 0 THEN 1 ELSE 0 END ASC, due_date ASC, created_at DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    LiveData<Task> getTaskById(int taskId);

    @Insert
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("UPDATE tasks SET completed = :completed WHERE id = :taskId")
    void setCompleted(int taskId, boolean completed);
}
