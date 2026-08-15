package com.deskmint.dashboard.games;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Simple 4x4 memory match / flip-card game, fully offline, tracks move count as score. */
public class MemoryMatchActivity extends Activity {

    private static final String[] SYMBOLS = {"★","♥","♦","♣","☀","☂","☘","♪"};

    private List<String> cardValues = new ArrayList<>();
    private Button[] buttons = new Button[16];
    private boolean[] matched = new boolean[16];
    private int firstPick = -1, secondPick = -1;
    private int moves = 0;
    private TextView movesLabel;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cardValues.addAll(java.util.Arrays.asList(SYMBOLS));
        cardValues.addAll(java.util.Arrays.asList(SYMBOLS));
        Collections.shuffle(cardValues);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setRowCount(4);

        for (int i = 0; i < 16; i++) {
            final int index = i;
            Button b = new Button(this);
            b.setTextSize(22);
            b.setText("?");
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { onCardTap(index); }
            });
            buttons[i] = b;
            grid.addView(b);
        }

        movesLabel = new TextView(this);
        updateMovesLabel();

        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.addView(movesLabel);
        root.addView(grid);
        setContentView(root);
    }

    private void onCardTap(int index) {
        if (matched[index] || index == firstPick) return;
        buttons[index].setText(cardValues.get(index));

        if (firstPick == -1) {
            firstPick = index;
            return;
        }

        secondPick = index;
        moves++;
        updateMovesLabel();

        if (cardValues.get(firstPick).equals(cardValues.get(secondPick))) {
            matched[firstPick] = true;
            matched[secondPick] = true;
            firstPick = -1;
            secondPick = -1;
            checkWin();
        } else {
            final int f = firstPick, s = secondPick;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    buttons[f].setText("?");
                    buttons[s].setText("?");
                }
            }, 700);
            firstPick = -1;
            secondPick = -1;
        }
    }

    private void checkWin() {
        for (boolean m : matched) if (!m) return;
        SharedPreferences prefs = getSharedPreferences("games", MODE_PRIVATE);
        int best = prefs.getInt("memory_best_moves", Integer.MAX_VALUE);
        if (moves < best) {
            prefs.edit().putInt("memory_best_moves", moves).apply();
        }
        movesLabel.setText("You won in " + moves + " moves! Best: " + Math.min(best, moves));
    }

    private void updateMovesLabel() {
        movesLabel.setText("Moves: " + moves);
    }
}
