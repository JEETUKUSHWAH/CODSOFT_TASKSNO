package com.example.clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        AlarmDatabaseHelper db = new AlarmDatabaseHelper(context);
        List<Alarm> alarms = db.getAllAlarms();
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {
                AlarmScheduler.schedule(context, alarm);
            }
        }
    }
}
