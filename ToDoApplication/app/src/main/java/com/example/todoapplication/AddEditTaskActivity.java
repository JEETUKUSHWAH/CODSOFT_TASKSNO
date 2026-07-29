package com.example.todoapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;

    private EditText editTitle;
    private EditText editDescription;
    private Spinner spinnerPriority;
    private TextView textDueDate;
    private Button buttonPickDate;
    private Button buttonClearDate;

    private int taskId = -1;
    private Task existingTask;
    private long selectedDueDate = 0L;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editTitle = findViewById(R.id.edit_title);
        editDescription = findViewById(R.id.edit_description);
        spinnerPriority = findViewById(R.id.spinner_priority);
        textDueDate = findViewById(R.id.text_due_date_value);
        buttonPickDate = findViewById(R.id.button_pick_date);
        buttonClearDate = findViewById(R.id.button_clear_date);
        Button buttonSave = findViewById(R.id.button_save);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        taskId = getIntent().getIntExtra(MainActivity.EXTRA_TASK_ID, -1);
        boolean isEditMode = taskId != -1;
        setTitle(isEditMode ? R.string.edit_task : R.string.add_task);

        if (isEditMode) {
            taskViewModel.getTaskById(taskId).observe(this, task -> {
                if (task != null) {
                    existingTask = task;
                    populateFields(task);
                }
            });
        }

        buttonPickDate.setOnClickListener(v -> showDatePicker());
        buttonClearDate.setOnClickListener(v -> {
            selectedDueDate = 0L;
            textDueDate.setText(R.string.no_due_date);
        });

        buttonSave.setOnClickListener(v -> saveTask());
    }

    private void populateFields(Task task) {
        editTitle.setText(task.getTitle());
        editDescription.setText(task.getDescription());
        spinnerPriority.setSelection(task.getPriority());
        selectedDueDate = task.getDueDate();
        textDueDate.setText(selectedDueDate > 0
                ? dateFormat.format(selectedDueDate)
                : getString(R.string.no_due_date));
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDueDate > 0) {
            calendar.setTimeInMillis(selectedDueDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, dayOfMonth, 0, 0, 0);
                    picked.set(Calendar.MILLISECOND, 0);
                    selectedDueDate = picked.getTimeInMillis();
                    textDueDate.setText(dateFormat.format(selectedDueDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveTask() {
        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        int priority = spinnerPriority.getSelectedItemPosition();

        if (TextUtils.isEmpty(title)) {
            editTitle.setError(getString(R.string.title_required));
            return;
        }

        if (existingTask != null) {
            existingTask.setTitle(title);
            existingTask.setDescription(description);
            existingTask.setPriority(priority);
            existingTask.setDueDate(selectedDueDate);
            taskViewModel.update(existingTask);
        } else {
            Task newTask = new Task(title, description, priority, selectedDueDate);
            taskViewModel.insert(newTask);
        }

        Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
