package com.deskmint.dashboard.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.deskmint.dashboard.music.MusicPlayerActivity;
import com.deskmint.dashboard.video.VideoGalleryActivity;

public class MediaFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(getActivity());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        Button musicBtn = new Button(getActivity());
        musicBtn.setText("🎵 Music Player");
        musicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MusicPlayerActivity.class));
            }
        });
        root.addView(musicBtn);

        Button videoBtn = new Button(getActivity());
        videoBtn.setText("🎬 Video Gallery");
        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), VideoGalleryActivity.class));
            }
        });
        root.addView(videoBtn);

        return root;
    }
}
