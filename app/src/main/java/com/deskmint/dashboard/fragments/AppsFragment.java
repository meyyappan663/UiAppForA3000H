package com.deskmint.dashboard.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.deskmint.dashboard.alarm.AddAlarmActivity;
import com.deskmint.dashboard.alarm.AlarmReceiver;
import com.deskmint.dashboard.apps.AppDrawerActivity;
import com.deskmint.dashboard.db.DbHelper;

import java.util.ArrayList;
import java.util.List;

/** Apps panel: shortcut into the full app drawer/search, plus alarm list management. */
public class AppsFragment extends Fragment {

    private DbHelper db;
    private ListView alarmList;
    private List<Long> alarmIds = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = new DbHelper(getActivity());

        LinearLayout root = new LinearLayout(getActivity());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        Button openDrawer = new Button(getActivity());
        openDrawer.setText("🔍 Open App Drawer / Search");
        openDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AppDrawerActivity.class));
            }
        });
        root.addView(openDrawer);

        Button addAlarm = new Button(getActivity());
        addAlarm.setText("⏰ Add Alarm");
        addAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddAlarmActivity.class));
            }
        });
        root.addView(addAlarm);

        alarmList = new ListView(getActivity());
        root.addView(alarmList);

        alarmList.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                long alarmId = alarmIds.get(position);
                db.deleteAlarm(alarmId);
                AlarmReceiver.cancel(getActivity(), alarmId);
                refreshAlarms();
                return true;
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAlarms();
    }

    private void refreshAlarms() {
        List<String> labels = new ArrayList<>();
        alarmIds.clear();
        Cursor c = db.getAllAlarms();
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndex("id"));
            String label = c.getString(c.getColumnIndex("label"));
            int hour = c.getInt(c.getColumnIndex("hour"));
            int minute = c.getInt(c.getColumnIndex("minute"));
            labels.add(String.format("%02d:%02d — %s (long-press to delete)", hour, minute,
                    label == null || label.isEmpty() ? "Alarm" : label));
            alarmIds.add(id);
        }
        c.close();
        alarmList.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, labels));
    }
}
