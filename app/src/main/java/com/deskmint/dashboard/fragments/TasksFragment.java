package com.deskmint.dashboard.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.deskmint.dashboard.db.DbHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Combined To-Do + Events panel. Tap the "+" to add a task; tap a task's
 * checkbox to mark it complete; long-press to delete. All data is local
 * (SQLite via DbHelper) -- no account or sync needed.
 */
public class TasksFragment extends Fragment {

    private DbHelper db;
    private ListView listView;
    private List<Long> rowIds = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = new DbHelper(getActivity());

        LinearLayout root = new LinearLayout(getActivity());
        root.setOrientation(LinearLayout.VERTICAL);

        Button addButton = new Button(getActivity());
        addButton.setText("+ Add Task");
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showAddDialog(); }
        });
        root.addView(addButton);

        listView = new ListView(getActivity());
        root.addView(listView);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        List<String> labels = new ArrayList<>();
        rowIds.clear();
        Cursor c = db.getAllTodos();
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndex("id"));
            String title = c.getString(c.getColumnIndex("title"));
            int done = c.getInt(c.getColumnIndex("done"));
            labels.add((done == 1 ? "[x] " : "[ ] ") + title);
            rowIds.add(id);
        }
        c.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, labels);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                long rowId = rowIds.get(position);
                toggleDone(rowId, position, labels);
            }
        });

        listView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                db.deleteTodo(rowIds.get(position));
                refreshList();
                return true;
            }
        });
    }

    private void toggleDone(long rowId, int position, List<String> labels) {
        String current = labels.get(position);
        boolean wasDone = current.startsWith("[x]");
        db.setTodoDone(rowId, !wasDone);
        refreshList();
    }

    private void showAddDialog() {
        final EditText input = new EditText(getActivity());
        input.setHint("Task title");

        final CheckBox highPriority = new CheckBox(getActivity());
        highPriority.setText("High priority");

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.addView(input);
        layout.addView(highPriority);

        new AlertDialog.Builder(getActivity())
                .setTitle("New Task")
                .setView(layout)
                .setPositiveButton("Add", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String title = input.getText().toString().trim();
                        if (!title.isEmpty()) {
                            int priority = highPriority.isChecked() ? 1 : 0;
                            db.addTodo(title, "General", priority, 0);
                            refreshList();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
