package com.deskmint.dashboard.alarm;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Full-screen alarm UI. Shows even when the device is locked, plays the chosen
 * ringtone with a gentle wake (gradually increasing volume), and vibrates.
 */
public class AlarmRingActivity extends Activity {

    private MediaPlayer player;
    private Vibrator vibrator;
    private Handler volumeHandler = new Handler();
    private float currentVolume = 0.1f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String label = getIntent().getStringExtra(AlarmReceiver.EXTRA_LABEL);
        String ringtoneUriStr = getIntent().getStringExtra(AlarmReceiver.EXTRA_RINGTONE);
        boolean vibrate = getIntent().getBooleanExtra(AlarmReceiver.EXTRA_VIBRATE, true);
        final long alarmId = getIntent().getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(android.view.Gravity.CENTER);
        root.setPadding(48, 48, 48, 48);

        TextView title = new TextView(this);
        title.setText(label == null || label.isEmpty() ? "Alarm" : label);
        title.setTextSize(28);
        root.addView(title);

        TextView time = new TextView(this);
        Calendar now = Calendar.getInstance();
        time.setText(String.format("%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)));
        time.setTextSize(56);
        root.addView(time);

        Button snooze = new Button(this);
        snooze.setText("Snooze 10 min");
        snooze.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Calendar snoozeTime = Calendar.getInstance();
                snoozeTime.add(Calendar.MINUTE, 10);
                AlarmReceiver.schedule(AlarmRingActivity.this, alarmId, "Snoozed alarm",
                        snoozeTime.getTimeInMillis(), null, true);
                stopAndFinish();
            }
        });
        root.addView(snooze);

        Button dismiss = new Button(this);
        dismiss.setText("Dismiss");
        dismiss.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                stopAndFinish();
            }
        });
        root.addView(dismiss);

        setContentView(root);

        // Play ringtone (gentle wake: start quiet, ramp volume over ~20s)
        try {
            Uri soundUri = (ringtoneUriStr != null && !ringtoneUriStr.isEmpty())
                    ? Uri.parse(ringtoneUriStr)
                    : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            player = new MediaPlayer();
            player.setDataSource(this, soundUri);
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.setVolume(currentVolume, currentVolume);
            player.prepare();
            player.start();
            rampVolume();
        } catch (Exception e) {
            // Fallback silently; the vibration + on-screen UI still wakes the user
        }

        if (vibrate) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {0, 800, 400};
            vibrator.vibrate(pattern, 0);
        }
    }

    private void rampVolume() {
        volumeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player == null) return;
                currentVolume = Math.min(1f, currentVolume + 0.1f);
                player.setVolume(currentVolume, currentVolume);
                if (currentVolume < 1f) {
                    volumeHandler.postDelayed(this, 2000);
                }
            }
        }, 2000);
    }

    private void stopAndFinish() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        if (vibrator != null) vibrator.cancel();
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent dismissing the alarm accidentally with back button
    }
}
