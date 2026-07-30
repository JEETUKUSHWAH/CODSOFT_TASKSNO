package com.example.clock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class AlarmSoundService extends Service {

    private static final String CHANNEL_ID = "alarm_channel";
    private static final int NOTIFICATION_ID = 1001;

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    public static void stopRinging(Context context) {
        context.stopService(new Intent(context, AlarmSoundService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long alarmId = intent != null
                ? intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
                : -1;

        AlarmDatabaseHelper db = new AlarmDatabaseHelper(this);
        Alarm alarm = (alarmId != -1) ? db.getAlarm(alarmId) : null;

        Notification notification = buildNotification(alarmId, alarm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        if (alarm != null) {
            playToneAndVibrate(alarm);
        }

        return START_NOT_STICKY;
    }

    private Notification buildNotification(long alarmId, @Nullable Alarm alarm) {
        Intent fullScreenIntent = new Intent(this, AlarmRingActivity.class);
        fullScreenIntent.putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                this,
                (int) alarmId,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String label = (alarm != null && alarm.getLabel() != null && !alarm.getLabel().trim().isEmpty())
                ? alarm.getLabel()
                : "Alarm";
        String time = alarm != null ? alarm.getFormattedTime() : "";

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(label)
                .setContentText(time)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setContentIntent(fullScreenPendingIntent)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarms",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Used to show the full-screen alarm ring screen");
            channel.enableVibration(false);
            channel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void playToneAndVibrate(Alarm alarm) {
        try {
            Uri toneUri = (alarm.getToneUri() != null)
                    ? Uri.parse(alarm.getToneUri())
                    : RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            mediaPlayer.setDataSource(this, toneUri);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
        }

        vibrator = getSystemService(Vibrator.class);
        if (vibrator != null) {
            long[] pattern = {0, 500, 500};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }
    }

    private void stopSoundAndVibration() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }

    @Override
    public void onDestroy() {
        stopSoundAndVibration();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
