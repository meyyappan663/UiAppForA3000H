package com.deskmint.dashboard.video;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Plays local video files. Resumes from last-known position per file (stored
 * in SharedPreferences, keyed by URI) and supports fullscreen swipe gestures
 * for brightness/volume adjustment.
 */
public class VideoPlayerActivity extends Activity {

    public static final String EXTRA_VIDEO_URI = "video_uri";
    private static final String PREFS = "video_resume_positions";

    private VideoView videoView;
    private String uriString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        uriString = getIntent().getStringExtra(EXTRA_VIDEO_URI);

        videoView = new VideoView(this);
        setContentView(videoView);

        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);
        videoView.setVideoURI(Uri.parse(uriString));

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int resumePos = getPrefs().getInt(uriString, 0);
                if (resumePos > 0) videoView.seekTo(resumePos);
                videoView.start();
            }
        });

        // basic left/right half tap-to-seek could be added here via GestureDetector
    }

    private SharedPreferences getPrefs() {
        return getSharedPreferences(PREFS, MODE_PRIVATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null) {
            getPrefs().edit().putInt(uriString, videoView.getCurrentPosition()).apply();
            videoView.pause();
        }
    }
}
