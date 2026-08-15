package com.deskmint.dashboard.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.deskmint.dashboard.games.MemoryMatchActivity;
import com.deskmint.dashboard.games.SnakeActivity;
import com.deskmint.dashboard.games.TicTacToeActivity;

public class GamesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(getActivity());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        root.addView(gameButton("🐍 Snake", SnakeActivity.class));
        root.addView(gameButton("❌⭕ Tic-Tac-Toe", TicTacToeActivity.class));
        root.addView(gameButton("🃏 Memory Match", MemoryMatchActivity.class));

        return root;
    }

    private Button gameButton(String label, final Class<?> target) {
        Button b = new Button(getActivity());
        b.setText(label);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), target));
            }
        });
        return b;
    }
}
