package com.example.clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class AlarmScheduler {

    public static final String EXTRA_ALARM_ID = "extra_alarm_id";

    public static void schedule(Context context, Alarm alarm) {
        if (!alarm.isEnabled()) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        long triggerAtMillis = nextTriggerTime(alarm);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_ALARM_ID, alarm.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    public static void cancel(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    public static void scheduleSnooze(Context context, Alarm alarm, int minutes) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        long triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_ALARM_ID, alarm.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    private static long nextTriggerTime(Alarm alarm) {
        Calendar now = Calendar.getInstance();
        Calendar candidate = Calendar.getInstance();
        candidate.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        candidate.set(Calendar.MINUTE, alarm.getMinute());
        candidate.set(Calendar.SECOND, 0);
        candidate.set(Calendar.MILLISECOND, 0);

        String repeat = alarm.getRepeatDays();
        boolean hasRepeat = repeat != null && repeat.contains("1");

        if (!hasRepeat) {
            if (candidate.before(now) || candidate.equals(now)) {
                candidate.add(Calendar.DAY_OF_YEAR, 1);
            }
            return candidate.getTimeInMillis();
        }

        for (int i = 0; i < 8; i++) {
            int dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK) - 1;
            boolean dayEnabled = repeat.charAt(dayOfWeek) == '1';
            boolean isFuture = candidate.after(now);
            if (dayEnabled && isFuture) {
                return candidate.getTimeInMillis();
            }
            candidate.add(Calendar.DAY_OF_YEAR, 1);
        }
        // Fallback: one week from now.
        candidate.add(Calendar.DAY_OF_YEAR, 7);
        return candidate.getTimeInMillis();
    }
}
