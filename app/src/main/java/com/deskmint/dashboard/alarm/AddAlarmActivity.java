package com.deskmint.dashboard.alarm;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.deskmint.dashboard.db.DbHelper;

import java.util.Calendar;

public class AddAlarmActivity extends Activity {

    private int hour = 7, minute = 0;
    private TextView timeLabel;
    private EditText labelInput;
    private CheckBox vibrateCheck;
    private CheckBox[] dayChecks = new CheckBox[7];
    private static final String[] DAY_NAMES = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        timeLabel = new TextView(this);
        timeLabel.setTextSize(40);
        timeLabel.setGravity(Gravity.CENTER);
        updateTimeLabel();
        timeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(AddAlarmActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int h, int m) {
                        hour = h; minute = m; updateTimeLabel();
                    }
                }, hour, minute, true).show();
            }
        });
        root.addView(timeLabel);

        labelInput = new EditText(this);
        labelInput.setHint("Alarm label (e.g. Wake up)");
        root.addView(labelInput);

        LinearLayout dayRow = new LinearLayout(this);
        dayRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < 7; i++) {
            CheckBox cb = new CheckBox(this);
            cb.setText(DAY_NAMES[i]);
            dayChecks[i] = cb;
            dayRow.addView(cb);
        }
        root.addView(dayRow);

        vibrateCheck = new CheckBox(this);
        vibrateCheck.setText("Vibrate");
        vibrateCheck.setChecked(true);
        root.addView(vibrateCheck);

        Button save = new Button(this);
        save.setText("Save Alarm");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAlarm();
            }
        });
        root.addView(save);

        setContentView(root);
    }

    private void updateTimeLabel() {
        timeLabel.setText(String.format("%02d:%02d", hour, minute));
    }

    private void saveAlarm() {
        StringBuilder repeatDays = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (dayChecks[i].isChecked()) repeatDays.append(i).append(",");
        }

        DbHelper db = new DbHelper(this);
        String label = labelInput.getText().toString();
        boolean vibrate = vibrateCheck.isChecked();
        long id = db.addAlarm(label, hour, minute, repeatDays.toString(), null, vibrate);

        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);
        next.set(Calendar.SECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }
        AlarmReceiver.schedule(this, id, label, next.getTimeInMillis(), null, vibrate);

        finish();
    }
}
