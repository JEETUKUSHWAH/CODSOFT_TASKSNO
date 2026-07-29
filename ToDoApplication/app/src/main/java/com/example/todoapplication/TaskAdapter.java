package com.example.todoapplication;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskCheckedListener {
        void onTaskChecked(Task task, boolean isChecked);
    }

    public interface OnTaskDeleteListener {
        void onTaskDelete(Task task);
    }

    private List<Task> tasks = new ArrayList<>();
    private final OnTaskClickListener clickListener;
    private final OnTaskCheckedListener checkedListener;
    private final OnTaskDeleteListener deleteListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public TaskAdapter(OnTaskClickListener clickListener,
                        OnTaskCheckedListener checkedListener,
                        OnTaskDeleteListener deleteListener) {
        this.clickListener = clickListener;
        this.checkedListener = checkedListener;
        this.deleteListener = deleteListener;
    }

    public void setTasks(List<Task> newTasks) {
        TaskDiffCallback diffCallback = new TaskDiffCallback(this.tasks, newTasks);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.tasks = newTasks;
        diffResult.dispatchUpdatesTo(this);
    }

    public Task getTaskAt(int position) {
        return tasks.get(position);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox checkBoxCompleted;
        private final TextView textTitle;
        private final TextView textDueDate;
        private final View priorityIndicator;
        private final View buttonDelete;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxCompleted = itemView.findViewById(R.id.checkbox_completed);
            textTitle = itemView.findViewById(R.id.text_title);
            textDueDate = itemView.findViewById(R.id.text_due_date);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);
            buttonDelete = itemView.findViewById(R.id.button_delete);
        }

        void bind(Task task) {
            textTitle.setText(task.getTitle());

            if (task.isCompleted()) {
                textTitle.setPaintFlags(textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                textTitle.setPaintFlags(textTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }

            if (task.getDueDate() > 0) {
                textDueDate.setVisibility(View.VISIBLE);
                textDueDate.setText(dateFormat.format(task.getDueDate()));
            } else {
                textDueDate.setVisibility(View.GONE);
            }

            int color;
            switch (task.getPriority()) {
                case Task.PRIORITY_HIGH:
                    color = itemView.getContext().getColor(R.color.priority_high);
                    break;
                case Task.PRIORITY_MEDIUM:
                    color = itemView.getContext().getColor(R.color.priority_medium);
                    break;
                default:
                    color = itemView.getContext().getColor(R.color.priority_low);
                    break;
            }
            priorityIndicator.setBackgroundColor(color);

            // Avoid triggering the listener while binding
            checkBoxCompleted.setOnCheckedChangeListener(null);
            checkBoxCompleted.setChecked(task.isCompleted());
            checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (checkedListener != null) {
                    checkedListener.onTaskChecked(task, isChecked);
                }
            });

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTaskClick(task);
                }
            });

            buttonDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onTaskDelete(task);
                }
            });
        }
    }

    private static class TaskDiffCallback extends DiffUtil.Callback {
        private final List<Task> oldList;
        private final List<Task> newList;

        TaskDiffCallback(List<Task> oldList, List<Task> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Task oldTask = oldList.get(oldItemPosition);
            Task newTask = newList.get(newItemPosition);
            return oldTask.getTitle().equals(newTask.getTitle())
                    && (oldTask.getDescription() == null
                        ? newTask.getDescription() == null
                        : oldTask.getDescription().equals(newTask.getDescription()))
                    && oldTask.getPriority() == newTask.getPriority()
                    && oldTask.getDueDate() == newTask.getDueDate()
                    && oldTask.isCompleted() == newTask.isCompleted();
        }
    }
}
