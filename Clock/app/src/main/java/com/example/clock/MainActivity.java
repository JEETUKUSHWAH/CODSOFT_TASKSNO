package com.example.clock;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.Listener {

    private TextView tvCurrentTime, tvCurrentDate, tvEmptyState;
    private RecyclerView rvAlarms;
    private AlarmAdapter adapter;
    private AlarmDatabaseHelper db;

    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockTick = new Runnable() {
        @Override
        public void run() {
            updateClock();
            clockHandler.postDelayed(this, 1000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new AlarmDatabaseHelper(this);

        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvAlarms = findViewById(R.id.rvAlarms);
        View fabAddAlarm = findViewById(R.id.fabAddAlarm);

        rvAlarms.setLayoutManager(new LinearLayoutManager(this));

        fabAddAlarm.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddAlarmActivity.class)));

        requestNotificationPermissionIfNeeded();
        requestExactAlarmPermissionIfNeeded();
        requestIgnoreBatteryOptimizationsIfNeeded();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlarms();
        clockHandler.post(clockTick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clockHandler.removeCallbacks(clockTick);
    }

    private void updateClock() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        long now = System.currentTimeMillis();
        tvCurrentTime.setText(timeFormat.format(now));
        tvCurrentDate.setText(dateFormat.format(now));
    }

    private void loadAlarms() {
        List<Alarm> alarms = db.getAllAlarms();
        tvEmptyState.setVisibility(alarms.isEmpty() ? View.VISIBLE : View.GONE);
        rvAlarms.setVisibility(alarms.isEmpty() ? View.GONE : View.VISIBLE);
        adapter = new AlarmAdapter(alarms, this);
        rvAlarms.setAdapter(adapter);
    }

    @Override
    public void onToggle(Alarm alarm, boolean enabled) {
        db.setEnabled(alarm.getId(), enabled);
        if (enabled) {
            AlarmScheduler.schedule(this, alarm);
        } else {
            AlarmScheduler.cancel(this, alarm);
        }
    }

    @Override
    public void onDelete(Alarm alarm) {
        AlarmScheduler.cancel(this, alarm);
        db.deleteAlarm(alarm.getId());
        loadAlarms();
    }

    @Override
    public void onEditRequested(Alarm alarm) {
        Intent intent = new Intent(this, AddAlarmActivity.class);
        intent.putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarm.getId());
        startActivity(intent);
    }
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
    private void requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = getSystemService(AlarmManager.class);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }
    private void requestIgnoreBatteryOptimizationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            String packageName = getPackageName();
            if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }
}