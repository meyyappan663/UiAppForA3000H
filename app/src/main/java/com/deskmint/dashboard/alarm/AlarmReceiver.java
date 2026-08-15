package com.deskmint.dashboard.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_ALARM_ID = "alarm_id";
    public static final String EXTRA_LABEL = "alarm_label";
    public static final String EXTRA_RINGTONE = "alarm_ringtone";
    public static final String EXTRA_VIBRATE = "alarm_vibrate";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Wake the device up even if screen is off / locked
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        @SuppressWarnings("deprecation")
        PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "deskmint:alarm");
        wl.acquire(10000);

        Intent ring = new Intent(context, AlarmRingActivity.class);
        ring.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ring.putExtra(EXTRA_ALARM_ID, intent.getLongExtra(EXTRA_ALARM_ID, -1));
        ring.putExtra(EXTRA_LABEL, intent.getStringExtra(EXTRA_LABEL));
        ring.putExtra(EXTRA_RINGTONE, intent.getStringExtra(EXTRA_RINGTONE));
        ring.putExtra(EXTRA_VIBRATE, intent.getBooleanExtra(EXTRA_VIBRATE, true));
        context.startActivity(ring);

        wl.release();
    }

    /** Schedules (or re-schedules) an exact alarm for the given time in millis. */
    public static void schedule(Context context, long id, String label, long timeMillis,
                                 String ringtoneUri, boolean vibrate) {
        android.app.AlarmManager am = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_ALARM_ID, id);
        intent.putExtra(EXTRA_LABEL, label);
        intent.putExtra(EXTRA_RINGTONE, ringtoneUri);
        intent.putExtra(EXTRA_VIBRATE, vibrate);
        android.app.PendingIntent pi = android.app.PendingIntent.getBroadcast(
                context, (int) id, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);

        // setExact is API 19+; on API 17/18 fall back to plain set()
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            am.setExact(android.app.AlarmManager.RTC_WAKEUP, timeMillis, pi);
        } else {
            am.set(android.app.AlarmManager.RTC_WAKEUP, timeMillis, pi);
        }
    }

    public static void cancel(Context context, long id) {
        android.app.AlarmManager am = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        android.app.PendingIntent pi = android.app.PendingIntent.getBroadcast(
                context, (int) id, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pi);
    }
}
