package com.deskmint.dashboard.video;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/** Thumbnail-free simple list gallery of all local videos found via MediaStore. */
public class VideoGalleryActivity extends ListActivity {

    private List<Uri> videoUris = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> names = new ArrayList<>();
        String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                MediaStore.Video.Media.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                names.add(name);
                videoUris.add(Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id)));
            }
            cursor.close();
        }

        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names));

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(VideoGalleryActivity.this, VideoPlayerActivity.class);
                i.putExtra(VideoPlayerActivity.EXTRA_VIDEO_URI, videoUris.get(position).toString());
                startActivity(i);
            }
        });
    }
}
