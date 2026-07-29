package com.example.todoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "com.example.todoapp.EXTRA_TASK_ID";

    private TaskViewModel taskViewModel;
    private TaskAdapter adapter;
    private TextView emptyStateText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        });


        RecyclerView recyclerView = findViewById(R.id.recycler_tasks);
        emptyStateText = findViewById(R.id.text_empty_state);
        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);

        adapter = new TaskAdapter(
                this::openTaskForEdit,
                (task, isChecked) -> taskViewModel.setCompleted(task.getId(), isChecked),
                this::confirmDelete
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Swipe-to-delete gesture
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@androidx.annotation.NonNull RecyclerView rv,
                                  @androidx.annotation.NonNull RecyclerView.ViewHolder vh,
                                  @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Task task = adapter.getTaskAt(viewHolder.getBindingAdapterPosition());
                deleteTask(task);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        taskViewModel.getAllTasks().observe(this, this::onTasksChanged);

        fabAddTask.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddEditTaskActivity.class)));



    }

    private void onTasksChanged(List<Task> tasks) {
        adapter.setTasks(tasks);
        boolean isEmpty = tasks == null || tasks.isEmpty();
        emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void openTaskForEdit(Task task) {
        Intent intent = new Intent(this, AddEditTaskActivity.class);
        intent.putExtra(EXTRA_TASK_ID, task.getId());
        startActivity(intent);
    }

    private void confirmDelete(Task task) {
        deleteTask(task);
    }

    private void deleteTask(Task task) {
        taskViewModel.delete(task);
        Snackbar.make(findViewById(R.id.main), R.string.task_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> taskViewModel.insert(task))
                .show();
    }

}