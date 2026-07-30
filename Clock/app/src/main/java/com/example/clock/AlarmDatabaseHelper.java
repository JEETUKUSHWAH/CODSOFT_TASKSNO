package com.example.clock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class AlarmDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "alarms.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE = "alarms";
    public static final String COL_ID = "_id";
    public static final String COL_HOUR = "hour";
    public static final String COL_MINUTE = "minute";
    public static final String COL_LABEL = "label";
    public static final String COL_TONE = "tone_uri";
    public static final String COL_ENABLED = "enabled";
    public static final String COL_REPEAT = "repeat_days";

    public AlarmDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HOUR + " INTEGER NOT NULL, " +
                COL_MINUTE + " INTEGER NOT NULL, " +
                COL_LABEL + " TEXT, " +
                COL_TONE + " TEXT, " +
                COL_ENABLED + " INTEGER NOT NULL DEFAULT 1, " +
                COL_REPEAT + " TEXT DEFAULT '0000000')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public long insertAlarm(Alarm alarm) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = toContentValues(alarm);
        long id = db.insert(TABLE, null, cv);
        db.close();
        return id;
    }

    public void updateAlarm(Alarm alarm) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = toContentValues(alarm);
        db.update(TABLE, cv, COL_ID + "=?", new String[]{String.valueOf(alarm.getId())});
        db.close();
    }

    public void setEnabled(long id, boolean enabled) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ENABLED, enabled ? 1 : 0);
        db.update(TABLE, cv, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteAlarm(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public Alarm getAlarm(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, null, COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        Alarm alarm = null;
        if (c.moveToFirst()) {
            alarm = fromCursor(c);
        }
        c.close();
        db.close();
        return alarm;
    }

    public List<Alarm> getAllAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, null, null, null, null, null,
                COL_HOUR + " ASC, " + COL_MINUTE + " ASC");
        while (c.moveToNext()) {
            alarms.add(fromCursor(c));
        }
        c.close();
        db.close();
        return alarms;
    }

    private ContentValues toContentValues(Alarm alarm) {
        ContentValues cv = new ContentValues();
        cv.put(COL_HOUR, alarm.getHour());
        cv.put(COL_MINUTE, alarm.getMinute());
        cv.put(COL_LABEL, alarm.getLabel());
        cv.put(COL_TONE, alarm.getToneUri());
        cv.put(COL_ENABLED, alarm.isEnabled() ? 1 : 0);
        cv.put(COL_REPEAT, alarm.getRepeatDays());
        return cv;
    }

    private Alarm fromCursor(Cursor c) {
        Alarm alarm = new Alarm();
        alarm.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        alarm.setHour(c.getInt(c.getColumnIndexOrThrow(COL_HOUR)));
        alarm.setMinute(c.getInt(c.getColumnIndexOrThrow(COL_MINUTE)));
        alarm.setLabel(c.getString(c.getColumnIndexOrThrow(COL_LABEL)));
        alarm.setToneUri(c.getString(c.getColumnIndexOrThrow(COL_TONE)));
        alarm.setEnabled(c.getInt(c.getColumnIndexOrThrow(COL_ENABLED)) == 1);
        alarm.setRepeatDays(c.getString(c.getColumnIndexOrThrow(COL_REPEAT)));
        return alarm;
    }
}
