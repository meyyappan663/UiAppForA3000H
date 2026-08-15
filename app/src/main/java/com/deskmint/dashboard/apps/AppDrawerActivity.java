package com.deskmint.dashboard.apps;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Full app drawer with a universal search bar. Typing filters installed apps
 * live; typing recognized keywords (wifi, bt/bluetooth, calc) jumps straight
 * to that system panel/tool instead of listing apps.
 */
public class AppDrawerActivity extends ListActivity {

    private List<ResolveInfo> allApps = new ArrayList<>();
    private List<ResolveInfo> filteredApps = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private PackageManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pm = getPackageManager();

        LinearLayout header = new LinearLayout(this);
        final EditText search = new EditText(this);
        search.setHint(getString(com.deskmint.dashboard.R.string.search_hint));
        header.addView(search);
        getListView().addHeaderView(header);

        loadApps();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        refreshAdapter("");
        setListAdapter(adapter);

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                handleSearch(s.toString());
            }
        });

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int index = position - 1;
                if (index < 0 || index >= filteredApps.size()) return;
                ResolveInfo info = filteredApps.get(index);
                Intent launch = pm.getLaunchIntentForPackage(info.activityInfo.packageName);
                if (launch != null) startActivity(launch);
            }
        });
    }

    private void loadApps() {
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        allApps = pm.queryIntentActivities(main, 0);
        Collections.sort(allApps, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo a, ResolveInfo b) {
                return a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString());
            }
        });
    }

    /** Handles the dual-purpose search: quick-action commands or app filtering. */
    private void handleSearch(String query) {
        String q = query.trim().toLowerCase();

        // Quick-action command shortcuts
        if (q.equals("wifi")) {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            return;
        }
        if (q.equals("bt") || q.equals("bluetooth")) {
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            return;
        }
        if (q.startsWith("calc:")) {
            String expr = q.substring(5).trim();
            try {
                double result = evalSimple(expr);
                Toast.makeText(this, expr + " = " + result, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Can't evaluate that", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        refreshAdapter(q);
    }

    private void refreshAdapter(String query) {
        filteredApps.clear();
        List<String> labels = new ArrayList<>();
        for (ResolveInfo info : allApps) {
            String label = info.loadLabel(pm).toString();
            if (query.isEmpty() || label.toLowerCase().contains(query)) {
                filteredApps.add(info);
                labels.add(label);
            }
        }
        adapter.clear();
        adapter.addAll(labels);
        adapter.notifyDataSetChanged();
    }

    /** Very small calculator fallback for "calc: 2+2" style queries -- offline, no external libs. */
    private double evalSimple(String expr) {
        // supports simple +,-,*,/ between two numbers only, e.g. "12*7"
        for (String op : new String[]{"+", "-", "*", "/"}) {
            if (expr.contains(op)) {
                String[] parts = expr.split("\\" + op);
                double a = Double.parseDouble(parts[0].trim());
                double b = Double.parseDouble(parts[1].trim());
                switch (op) {
                    case "+": return a + b;
                    case "-": return a - b;
                    case "*": return a * b;
                    case "/": return a / b;
                }
            }
        }
        return Double.parseDouble(expr);
    }
}
