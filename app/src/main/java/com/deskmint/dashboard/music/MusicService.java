package com.deskmint.dashboard.music;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.deskmint.dashboard.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Foreground service that keeps music playing in the background, independent
 * of whichever dashboard panel is currently visible. Shows persistent controls
 * in the notification shade, similar to a lock-screen player.
 */
public class MusicService extends Service {

    public static final String ACTION_PLAY = "com.deskmint.dashboard.music.PLAY";
    public static final String ACTION_PAUSE = "com.deskmint.dashboard.music.PAUSE";
    public static final String ACTION_NEXT = "com.deskmint.dashboard.music.NEXT";
    public static final String ACTION_PREV = "com.deskmint.dashboard.music.PREV";
    public static final String EXTRA_TRACK_URI = "track_uri";

    private final IBinder binder = new LocalBinder();
    private MediaPlayer player;
    private List<Track> queue = new ArrayList<>();
    private int currentIndex = 0;
    private boolean shuffle = false;
    private boolean repeat = false;

    public class LocalBinder extends Binder {
        public MusicService getService() { return MusicService.this; }
    }

    public static class Track {
        public String title;
        public String artist;
        public Uri uri;
        public long durationMs;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PLAY: resume(); break;
                case ACTION_PAUSE: pause(); break;
                case ACTION_NEXT: playNext(); break;
                case ACTION_PREV: playPrevious(); break;
            }
        }
        return START_STICKY; // survive being killed under memory pressure
    }

    public void setQueue(List<Track> tracks, int startIndex) {
        this.queue = tracks;
        this.currentIndex = startIndex;
        playCurrent();
    }

    private void playCurrent() {
        if (queue.isEmpty()) return;
        try {
            if (player != null) {
                player.reset();
            } else {
                player = new MediaPlayer();
            }
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, queue.get(currentIndex).uri);
            player.prepare();
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (repeat) {
                        playCurrent();
                    } else {
                        playNext();
                    }
                }
            });
            showNotification();
        } catch (Exception ignored) { }
    }

    public void resume() {
        if (player != null && !player.isPlaying()) player.start();
    }

    public void pause() {
        if (player != null && player.isPlaying()) player.pause();
    }

    public void playNext() {
        if (queue.isEmpty()) return;
        if (shuffle) {
            currentIndex = (int) (Math.random() * queue.size());
        } else {
            currentIndex = (currentIndex + 1) % queue.size();
        }
        playCurrent();
    }

    public void playPrevious() {
        if (queue.isEmpty()) return;
        currentIndex = (currentIndex - 1 + queue.size()) % queue.size();
        playCurrent();
    }

    public void seekTo(int ms) {
        if (player != null) player.seekTo(ms);
    }

    public int getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public Track getCurrentTrack() {
        return queue.isEmpty() ? null : queue.get(currentIndex);
    }

    public void setShuffle(boolean s) { this.shuffle = s; }
    public void setRepeat(boolean r) { this.repeat = r; }

    private void showNotification() {
        Track t = getCurrentTrack();
        if (t == null) return;

        Intent openApp = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, openApp, 0);

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(t.title)
                .setContentText(t.artist)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(contentIntent)
                .setOngoing(true);
        Notification notification = builder.build();

        startForeground(1001, notification);
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
