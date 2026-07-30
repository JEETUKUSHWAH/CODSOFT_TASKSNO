package com.example.clock;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;

public class AddAlarmActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private EditText etLabel;
    private Button btnChooseTone;
    private Chip chipSun, chipMon, chipTue, chipWed, chipThu, chipFri, chipSat;

    private Uri selectedToneUri;
    private AlarmDatabaseHelper db;
    private long editingAlarmId = -1;
    private Alarm existingAlarm;
    private final ActivityResultLauncher<Intent> toneLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (uri != null) {
                        selectedToneUri = uri;
                        btnChooseTone.setText(RingtoneManager.getRingtone(this, uri).getTitle(this));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_alarm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new AlarmDatabaseHelper(this);

        timePicker = findViewById(R.id.timePicker);
        etLabel = findViewById(R.id.etLabel);
        btnChooseTone = findViewById(R.id.btnChooseTone);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnDeleteAlarm = findViewById(R.id.btnDeleteAlarm);
        android.widget.TextView tvScreenTitle = findViewById(R.id.tvScreenTitle);

        chipSun = findViewById(R.id.chipSun);
        chipMon = findViewById(R.id.chipMon);
        chipTue = findViewById(R.id.chipTue);
        chipWed = findViewById(R.id.chipWed);
        chipThu = findViewById(R.id.chipThu);
        chipFri = findViewById(R.id.chipFri);
        chipSat = findViewById(R.id.chipSat);

        timePicker.setIs24HourView(false);

        selectedToneUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);

        editingAlarmId = getIntent().getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1);
        if (editingAlarmId != -1) {
            existingAlarm = db.getAlarm(editingAlarmId);
            if (existingAlarm != null) {
                tvScreenTitle.setText("Edit Alarm");
                btnSave.setText("Update");
                btnDeleteAlarm.setVisibility(android.view.View.VISIBLE);
                prefillFromExistingAlarm();
            } else {
                // Alarm was deleted elsewhere; fall back to create mode.
                editingAlarmId = -1;
            }
        }

        btnChooseTone.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedToneUri);
            toneLauncher.launch(intent);
        });

        btnCancel.setOnClickListener(v -> finish());
        btnDeleteAlarm.setOnClickListener(v -> deleteAlarm());
        btnSave.setOnClickListener(v -> saveAlarm());

    }

    private void prefillFromExistingAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(existingAlarm.getHour());
            timePicker.setMinute(existingAlarm.getMinute());
        } else {
            timePicker.setCurrentHour(existingAlarm.getHour());
            timePicker.setCurrentMinute(existingAlarm.getMinute());
        }

        etLabel.setText(existingAlarm.getLabel());

        String repeatDays = existingAlarm.getRepeatDays();
        if (repeatDays != null && repeatDays.length() == 7) {
            chipSun.setChecked(repeatDays.charAt(0) == '1');
            chipMon.setChecked(repeatDays.charAt(1) == '1');
            chipTue.setChecked(repeatDays.charAt(2) == '1');
            chipWed.setChecked(repeatDays.charAt(3) == '1');
            chipThu.setChecked(repeatDays.charAt(4) == '1');
            chipFri.setChecked(repeatDays.charAt(5) == '1');
            chipSat.setChecked(repeatDays.charAt(6) == '1');
        }

        if (existingAlarm.getToneUri() != null) {
            selectedToneUri = Uri.parse(existingAlarm.getToneUri());
            try {
                btnChooseTone.setText(RingtoneManager.getRingtone(this, selectedToneUri).getTitle(this));
            } catch (Exception e) {
                btnChooseTone.setText("Default alarm tone");
            }
        }
    }

    private void saveAlarm() {
        int hour, minute;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }

        String repeatDays = "" +
                (chipSun.isChecked() ? '1' : '0') +
                (chipMon.isChecked() ? '1' : '0') +
                (chipTue.isChecked() ? '1' : '0') +
                (chipWed.isChecked() ? '1' : '0') +
                (chipThu.isChecked() ? '1' : '0') +
                (chipFri.isChecked() ? '1' : '0') +
                (chipSat.isChecked() ? '1' : '0');

        boolean isEditing = editingAlarmId != -1;

        Alarm alarm = isEditing ? existingAlarm : new Alarm();
        alarm.setHour(hour);
        alarm.setMinute(minute);
        alarm.setLabel(etLabel.getText().toString().trim());
        alarm.setToneUri(selectedToneUri != null ? selectedToneUri.toString() : null);
        alarm.setEnabled(true);
        alarm.setRepeatDays(repeatDays);

        if (!isEditing) {
            alarm.setEnabled(true);
        }

        if (isEditing) {
            AlarmScheduler.cancel(this, alarm);
            db.updateAlarm(alarm);
        } else {
            long id = db.insertAlarm(alarm);
            alarm.setId(id);
        }

        if (alarm.isEnabled()) {
            AlarmScheduler.schedule(this, alarm);
        }

        Toast.makeText(this,
                (isEditing ? "Alarm updated for " : "Alarm set for ") + alarm.getFormattedTime(),
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void deleteAlarm() {
        if (existingAlarm != null) {
            AlarmScheduler.cancel(this, existingAlarm);
            db.deleteAlarm(existingAlarm.getId());
        }
        finish();
    }

}