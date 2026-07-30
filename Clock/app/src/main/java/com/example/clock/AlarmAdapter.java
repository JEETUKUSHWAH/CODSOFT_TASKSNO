package com.example.clock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    public interface Listener {
        void onToggle(Alarm alarm, boolean enabled);
        void onDelete(Alarm alarm);
        void onEditRequested(Alarm alarm);
    }

    private final List<Alarm> alarms;
    private final Listener listener;

    public AlarmAdapter(List<Alarm> alarms, Listener listener) {
        this.alarms = alarms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarms.get(position);

        holder.tvTime.setText(alarm.getFormattedTime());
        String label = alarm.getLabel();
        holder.tvLabel.setText((label == null || label.trim().isEmpty()) ? "Alarm" : label);
        holder.tvRepeat.setText(alarm.getRepeatSummary());

        // Avoid firing the listener while we set the initial checked state.
        holder.switchEnabled.setOnCheckedChangeListener(null);
        holder.switchEnabled.setChecked(alarm.isEnabled());
        holder.switchEnabled.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            alarm.setEnabled(isChecked);
            if (listener != null) listener.onToggle(alarm, isChecked);
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(alarm);
        });

        holder.rowContent.setOnClickListener(v -> {
            if (listener != null) listener.onEditRequested(alarm);
        });

    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvLabel, tvRepeat;
        SwitchMaterial switchEnabled;
        ImageView ivDelete;

        View rowContent;

        AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            rowContent = itemView.findViewById(R.id.rowContent);
            tvTime = itemView.findViewById(R.id.tvAlarmTime);
            tvLabel = itemView.findViewById(R.id.tvAlarmLabel);
            tvRepeat = itemView.findViewById(R.id.tvAlarmRepeat);
            switchEnabled = itemView.findViewById(R.id.switchEnabled);
            ivDelete = itemView.findViewById(R.id.ivDelete);}
    }
}
