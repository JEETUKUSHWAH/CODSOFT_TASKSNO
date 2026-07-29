package com.example.todoapplication;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class}, version = 1, exportSchema = false)
public abstract class TaskDatabase extends RoomDatabase {

    private static volatile TaskDatabase INSTANCE;

    public abstract TaskDao taskDao();

    public static TaskDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (TaskDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    TaskDatabase.class,
                                    "todo_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
