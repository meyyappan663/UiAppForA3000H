package com.deskmint.dashboard.games;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Simple offline Tic-Tac-Toe vs a basic AI (blocks/wins when possible, else random). */
public class TicTacToeActivity extends Activity {

    private String[] board = new String[9];
    private Button[] cells = new Button[9];
    private boolean playerTurn = true;
    private TextView scoreView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        grid.setRowCount(3);

        for (int i = 0; i < 9; i++) {
            final int index = i;
            Button b = new Button(this);
            b.setTextSize(28);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCellTap(index);
                }
            });
            cells[i] = b;
            grid.addView(b);
        }

        scoreView = new TextView(this);
        updateScoreLabel();

        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.addView(scoreView);
        root.addView(grid);
        setContentView(root);

        resetBoard();
    }

    private void resetBoard() {
        for (int i = 0; i < 9; i++) {
            board[i] = "";
            cells[i].setText("");
        }
        playerTurn = true;
    }

    private void onCellTap(int index) {
        if (!playerTurn || !board[index].isEmpty()) return;
        board[index] = "X";
        cells[index].setText("X");
        if (checkWin("X")) { onGameEnd("You win!"); return; }
        if (isFull()) { onGameEnd("Draw!"); return; }

        playerTurn = false;
        aiMove();
    }

    private void aiMove() {
        int move = findWinningMove("O");
        if (move == -1) move = findWinningMove("X"); // block
        if (move == -1) {
            java.util.List<Integer> empty = new java.util.ArrayList<>();
            for (int i = 0; i < 9; i++) if (board[i].isEmpty()) empty.add(i);
            if (empty.isEmpty()) return;
            move = empty.get((int) (Math.random() * empty.size()));
        }
        board[move] = "O";
        cells[move].setText("O");
        if (checkWin("O")) { onGameEnd("AI wins!"); return; }
        if (isFull()) { onGameEnd("Draw!"); return; }
        playerTurn = true;
    }

    private int findWinningMove(String mark) {
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] line : lines) {
            String a = board[line[0]], b = board[line[1]], c = board[line[2]];
            int emptyIdx = -1, filled = 0;
            for (int idx : line) {
                if (board[idx].isEmpty()) emptyIdx = idx;
                else if (board[idx].equals(mark)) filled++;
            }
            if (filled == 2 && emptyIdx != -1) return emptyIdx;
        }
        return -1;
    }

    private boolean checkWin(String mark) {
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] line : lines) {
            if (board[line[0]].equals(mark) && board[line[1]].equals(mark) && board[line[2]].equals(mark)) return true;
        }
        return false;
    }

    private boolean isFull() {
        for (String s : board) if (s.isEmpty()) return false;
        return true;
    }

    private void onGameEnd(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (message.equals("You win!")) {
            SharedPreferences prefs = getSharedPreferences("games", MODE_PRIVATE);
            int wins = prefs.getInt("tictactoe_wins", 0) + 1;
            prefs.edit().putInt("tictactoe_wins", wins).apply();
            updateScoreLabel();
        }
        resetBoard();
    }

    private void updateScoreLabel() {
        int wins = getSharedPreferences("games", MODE_PRIVATE).getInt("tictactoe_wins", 0);
        scoreView.setText("Wins: " + wins);
    }
}
