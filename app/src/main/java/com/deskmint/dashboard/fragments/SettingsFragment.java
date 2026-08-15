package com.deskmint.dashboard.fragments;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.Camera;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Quick-settings panel: Wi-Fi / Bluetooth toggles, screen brightness, media
 * volume, and a flashlight toggle -- everything a desk dashboard needs without
 * digging into the full Android settings app.
 */
public class SettingsFragment extends Fragment {

    private Camera camera;
    private boolean flashOn = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(getActivity());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        root.addView(buildWifiRow());
        root.addView(buildBluetoothRow());
        root.addView(buildBrightnessRow());
        root.addView(buildVolumeRow());
        root.addView(buildClockColorRow());
        root.addView(buildFlashlightRow());

        return root;
    }

    private View buildWifiRow() {
        final WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        Switch toggle = new Switch(getActivity());
        toggle.setText("Wi-Fi");
        toggle.setChecked(wifiManager.isWifiEnabled());
        toggle.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                wifiManager.setWifiEnabled(isChecked);
            }
        });
        return toggle;
    }

    private View buildBluetoothRow() {
        final BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        Switch toggle = new Switch(getActivity());
        toggle.setText("Bluetooth");
        if (bt != null) {
            toggle.setChecked(bt.isEnabled());
            toggle.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) bt.enable(); else bt.disable();
                }
            });
        } else {
            toggle.setEnabled(false);
        }
        return toggle;
    }

    private View buildBrightnessRow() {
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.VERTICAL);
        TextView label = new TextView(getActivity());
        label.setText("Brightness");
        row.addView(label);

        SeekBar seekBar = new SeekBar(getActivity());
        seekBar.setMax(255);
        try {
            int current = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            seekBar.setProgress(current);
        } catch (Exception ignored) { }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                try {
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS, progress);
                    android.view.WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
                    params.screenBrightness = progress / 255f;
                    getActivity().getWindow().setAttributes(params);
                } catch (Exception ignored) { }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        row.addView(seekBar);
        return row;
    }

    private View buildVolumeRow() {
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.VERTICAL);
        TextView label = new TextView(getActivity());
        label.setText("Media Volume");
        row.addView(label);

        final android.media.AudioManager am = (android.media.AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        SeekBar seekBar = new SeekBar(getActivity());
        seekBar.setMax(am.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC));
        seekBar.setProgress(am.getStreamVolume(android.media.AudioManager.STREAM_MUSIC));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) am.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, progress, 0);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        row.addView(seekBar);
        return row;
    }


    private View buildClockColorRow() {
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.VERTICAL);
        TextView label = new TextView(getActivity());
        label.setText("Clock letter colour");
        label.setTextSize(18);
        row.addView(label);
        final String[] names = {"Black (default)", "White", "Cyan", "Green", "Blue", "Red", "Yellow"};
        final int[] colors = {android.graphics.Color.BLACK, android.graphics.Color.WHITE,
                android.graphics.Color.rgb(0,229,199), android.graphics.Color.rgb(90,230,120),
                android.graphics.Color.rgb(80,140,255), android.graphics.Color.rgb(255,80,90),
                android.graphics.Color.rgb(255,220,70)};
        android.widget.Spinner spinner = new android.widget.Spinner(getActivity());
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int saved = getActivity().getSharedPreferences("deskmint", Context.MODE_PRIVATE)
                .getInt("clock_color", android.graphics.Color.BLACK);
        int selected=0; for(int i=0;i<colors.length;i++) if(colors[i]==saved) selected=i;
        spinner.setSelection(selected);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                getActivity().getSharedPreferences("deskmint", Context.MODE_PRIVATE).edit()
                        .putInt("clock_color", colors[pos]).apply();
            }
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        row.addView(spinner);
        return row;
    }

    private View buildFlashlightRow() {
        Switch toggle = new Switch(getActivity());
        toggle.setText("Flashlight");
        toggle.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                toggleFlashlight(isChecked);
            }
        });
        return toggle;
    }

    @SuppressWarnings("deprecation")
    private void toggleFlashlight(boolean on) {
        try {
            if (on) {
                if (camera == null) camera = Camera.open();
                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                camera.startPreview();
            } else if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
            flashOn = on;
        } catch (Exception ignored) {
            // Device may not support torch mode via Camera API; ignore gracefully.
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (flashOn) toggleFlashlight(false);
    }
}
