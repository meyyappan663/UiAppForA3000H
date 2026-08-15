package com.deskmint.dashboard.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Central local database for DeskMint Dashboard.
 * Stores todos, events, alarms and notes. Fully offline, no network required.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "deskmint.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_TODO = "todo";
    public static final String TABLE_EVENT = "event";
    public static final String TABLE_ALARM = "alarm";
    public static final String TABLE_NOTE = "note";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TODO + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "category TEXT," +
                "priority INTEGER DEFAULT 0," +
                "due_time INTEGER," +
                "done INTEGER DEFAULT 0," +
                "sort_order INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_EVENT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "location TEXT," +
                "start_time INTEGER," +
                "end_time INTEGER," +
                "repeat_rule TEXT," +
                "color INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_ALARM + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "label TEXT," +
                "hour INTEGER," +
                "minute INTEGER," +
                "repeat_days TEXT," +
                "ringtone_uri TEXT," +
                "vibrate INTEGER DEFAULT 1," +
                "enabled INTEGER DEFAULT 1)");

        db.execSQL("CREATE TABLE " + TABLE_NOTE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "text TEXT," +
                "created_at INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);
        onCreate(db);
    }

    // ---------- TODO ----------
    public long addTodo(String title, String category, int priority, long dueTime) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("category", category);
        cv.put("priority", priority);
        cv.put("due_time", dueTime);
        return getWritableDatabase().insert(TABLE_TODO, null, cv);
    }

    public void setTodoDone(long id, boolean done) {
        ContentValues cv = new ContentValues();
        cv.put("done", done ? 1 : 0);
        getWritableDatabase().update(TABLE_TODO, cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteTodo(long id) {
        getWritableDatabase().delete(TABLE_TODO, "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor getAllTodos() {
        return getReadableDatabase().query(TABLE_TODO, null, null, null, null, null,
                "done ASC, priority DESC, due_time ASC");
    }

    // ---------- EVENT ----------
    public long addEvent(String title, String location, long start, long end, String repeatRule, int color) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("location", location);
        cv.put("start_time", start);
        cv.put("end_time", end);
        cv.put("repeat_rule", repeatRule);
        cv.put("color", color);
        return getWritableDatabase().insert(TABLE_EVENT, null, cv);
    }

    public Cursor getEventsForDay(long dayStart, long dayEnd) {
        return getReadableDatabase().query(TABLE_EVENT, null,
                "start_time >= ? AND start_time < ?",
                new String[]{String.valueOf(dayStart), String.valueOf(dayEnd)},
                null, null, "start_time ASC");
    }

    // ---------- ALARM ----------
    public long addAlarm(String label, int hour, int minute, String repeatDays, String ringtoneUri, boolean vibrate) {
        ContentValues cv = new ContentValues();
        cv.put("label", label);
        cv.put("hour", hour);
        cv.put("minute", minute);
        cv.put("repeat_days", repeatDays);
        cv.put("ringtone_uri", ringtoneUri);
        cv.put("vibrate", vibrate ? 1 : 0);
        cv.put("enabled", 1);
        return getWritableDatabase().insert(TABLE_ALARM, null, cv);
    }

    public void setAlarmEnabled(long id, boolean enabled) {
        ContentValues cv = new ContentValues();
        cv.put("enabled", enabled ? 1 : 0);
        getWritableDatabase().update(TABLE_ALARM, cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteAlarm(long id) {
        getWritableDatabase().delete(TABLE_ALARM, "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor getAllAlarms() {
        return getReadableDatabase().query(TABLE_ALARM, null, null, null, null, null, "hour ASC, minute ASC");
    }

    // ---------- NOTE ----------
    public long addNote(String text) {
        ContentValues cv = new ContentValues();
        cv.put("text", text);
        cv.put("created_at", System.currentTimeMillis());
        return getWritableDatabase().insert(TABLE_NOTE, null, cv);
    }

    public List<String> getAllNotes() {
        List<String> notes = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLE_NOTE, new String[]{"text"}, null, null, null, null, "created_at DESC");
        while (c.moveToNext()) {
            notes.add(c.getString(0));
        }
        c.close();
        return notes;
    }
}
