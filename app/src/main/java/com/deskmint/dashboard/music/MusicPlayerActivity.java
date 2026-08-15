package com.deskmint.dashboard.music;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Scans local storage for audio files (MediaStore.Audio) and lets the user
 * browse/play them. Binds to MusicService so playback survives navigating
 * away from this screen.
 */
public class MusicPlayerActivity extends ListActivity {

    private List<MusicService.Track> tracks = new ArrayList<>();
    private MusicService musicService;
    private boolean bound = false;
    private TextView nowPlayingLabel;
    private SeekBar seekBar;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicService.LocalBinder) service).getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);

        nowPlayingLabel = new TextView(this);
        nowPlayingLabel.setText("Nothing playing");
        header.addView(nowPlayingLabel);

        LinearLayout controls = new LinearLayout(this);
        Button prev = new Button(this);
        prev.setText("⏮");
        prev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { if (bound) musicService.playPrevious(); }
        });
        Button playPause = new Button(this);
        playPause.setText("⏯");
        playPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!bound) return;
                if (musicService.isPlaying()) musicService.pause(); else musicService.resume();
            }
        });
        Button next = new Button(this);
        next.setText("⏭");
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { if (bound) musicService.playNext(); }
        });
        controls.addView(prev);
        controls.addView(playPause);
        controls.addView(next);
        header.addView(controls);

        getListView().addHeaderView(header);

        scanAudioFiles();

        List<String> titles = new ArrayList<>();
        for (MusicService.Track t : tracks) titles.add(t.title + " — " + t.artist);
        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles));

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int trackIndex = position - 1; // account for header
                if (trackIndex < 0 || !bound) return;
                musicService.setQueue(tracks, trackIndex);
                nowPlayingLabel.setText("Playing: " + tracks.get(trackIndex).title);
            }
        });

        Intent svc = new Intent(this, MusicService.class);
        startService(svc);
        bindService(svc, connection, Context.BIND_AUTO_CREATE);
    }

    /** Queries MediaStore.Audio for all local music files -- fully offline. */
    private void scanAudioFiles() {
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
        };
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Audio.Media.IS_MUSIC + " != 0",
                null,
                MediaStore.Audio.Media.TITLE + " ASC");

        if (cursor == null) return;
        while (cursor.moveToNext()) {
            MusicService.Track t = new MusicService.Track();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            t.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            t.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            t.durationMs = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            t.uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
            tracks.add(t);
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        if (bound) unbindService(connection);
        super.onDestroy();
    }
}
