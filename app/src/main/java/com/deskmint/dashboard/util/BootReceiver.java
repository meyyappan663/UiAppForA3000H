package com.deskmint.dashboard.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.deskmint.dashboard.alarm.AlarmReceiver;
import com.deskmint.dashboard.db.DbHelper;

import java.util.Calendar;

/**
 * Re-schedules all enabled alarms after the tablet reboots or loses power.
 * Since this device runs 24/7 on a desk, this keeps alarms reliable across restarts.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        DbHelper db = new DbHelper(context);
        Cursor c = db.getAllAlarms();
        while (c.moveToNext()) {
            int enabled = c.getInt(c.getColumnIndex("enabled"));
            if (enabled != 1) continue;

            long id = c.getLong(c.getColumnIndex("id"));
            String label = c.getString(c.getColumnIndex("label"));
            int hour = c.getInt(c.getColumnIndex("hour"));
            int minute = c.getInt(c.getColumnIndex("minute"));
            String ringtone = c.getString(c.getColumnIndex("ringtone_uri"));
            boolean vibrate = c.getInt(c.getColumnIndex("vibrate")) == 1;

            Calendar next = Calendar.getInstance();
            next.set(Calendar.HOUR_OF_DAY, hour);
            next.set(Calendar.MINUTE, minute);
            next.set(Calendar.SECOND, 0);
            if (next.getTimeInMillis() <= System.currentTimeMillis()) {
                next.add(Calendar.DAY_OF_YEAR, 1);
            }

            AlarmReceiver.schedule(context, id, label, next.getTimeInMillis(), ringtone, vibrate);
        }
        c.close();
    }
}
