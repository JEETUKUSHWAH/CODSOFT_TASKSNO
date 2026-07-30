package com.example.clock;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.window.OnBackInvokedDispatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class AlarmRingActivity extends AppCompatActivity {

    private static final int SNOOZE_MINUTES = 10;

    private Alarm alarm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showOverLockScreen();
        setContentView(R.layout.activity_alarm_ring);

        long alarmId = getIntent().getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1);
        AlarmDatabaseHelper db = new AlarmDatabaseHelper(this);
        alarm = db.getAlarm(alarmId);
        if (alarm == null) {
            AlarmSoundService.stopRinging(this);
            finish();
            return;
        }

        TextView tvTime = findViewById(R.id.tvRingTime);
        TextView tvLabel = findViewById(R.id.tvRingLabel);
        tvTime.setText(alarm.getFormattedTime());
        String label = alarm.getLabel();
        tvLabel.setText((label == null || label.trim().isEmpty()) ? "Alarm" : label);

        findViewById(R.id.btnSnooze).setOnClickListener(v -> snooze());
        findViewById(R.id.btnDismiss).setOnClickListener(v -> dismiss());
    }

    @SuppressWarnings("deprecation")
    private void showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void snooze() {
        AlarmSoundService.stopRinging(this);
        AlarmScheduler.scheduleSnooze(this, alarm, SNOOZE_MINUTES);
        finish();
    }

    private void dismiss() {
        AlarmSoundService.stopRinging(this);
        AlarmDatabaseHelper db = new AlarmDatabaseHelper(this);
        if (!alarm.isRepeating()) {
            db.setEnabled(alarm.getId(), false);
        }
        finish();
    }

    @NonNull
    @Override
    public OnBackInvokedDispatcher getOnBackInvokedDispatcher() {
        return super.getOnBackInvokedDispatcher();
    }
}
