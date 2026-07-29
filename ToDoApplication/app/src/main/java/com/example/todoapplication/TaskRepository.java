package com.example.todoapplication;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Mediates between the UI layer and the Room database, ensuring all
 * write operations happen off the main thread.
 */
public class TaskRepository {

    private final TaskDao taskDao;
    private final LiveData<List<Task>> allTasks;
    private final ExecutorService executorService;

    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

    public LiveData<Task> getTaskById(int taskId) {
        return taskDao.getTaskById(taskId);
    }

    public void insert(Task task) {
        executorService.execute(() -> taskDao.insert(task));
    }

    public void update(Task task) {
        executorService.execute(() -> taskDao.update(task));
    }

    public void delete(Task task) {
        executorService.execute(() -> taskDao.delete(task));
    }

    public void setCompleted(int taskId, boolean completed) {
        executorService.execute(() -> taskDao.setCompleted(taskId, completed));
    }
}
