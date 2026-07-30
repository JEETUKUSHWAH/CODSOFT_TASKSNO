package com.example.clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1);
        if (alarmId == -1) return;

        AlarmDatabaseHelper db = new AlarmDatabaseHelper(context);
        Alarm alarm = db.getAlarm(alarmId);
        if (alarm == null) return;

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, "AlarmClock:AlarmWakeLock");
            wakeLock.acquire(10_000);
        }

        Intent serviceIntent = new Intent(context, AlarmSoundService.class);
        serviceIntent.putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        if (alarm.isRepeating()) {
            AlarmScheduler.schedule(context, alarm);
        } else {
            // One-off alarms auto-disable once they've fired.
            db.setEnabled(alarmId, false);
        }
    }
}
