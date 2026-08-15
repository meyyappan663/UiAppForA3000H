package com.deskmint.dashboard.games;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

/** Classic offline Snake game. Swipe to change direction. High score saved locally. */
public class SnakeActivity extends Activity {

    private SnakeView snakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        snakeView = new SnakeView(this);
        setContentView(snakeView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeView.pauseGame();
    }

    static class SnakeView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

        private static final int CELL = 40;
        private Thread thread;
        private volatile boolean running = false;
        private List<int[]> snake = new ArrayList<>();
        private int[] food = new int[2];
        private int dirX = 1, dirY = 0;
        private int cols, rows;
        private Paint paint = new Paint();
        private long lastMoveTime = 0;
        private long moveIntervalMs = 180;
        private boolean gameOver = false;
        private float startX, startY;

        SnakeView(Context context) {
            super(context);
            getHolder().addCallback(this);
            setFocusable(true);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            cols = getWidth() / CELL;
            rows = getHeight() / CELL;
            resetGame();
            running = true;
            thread = new Thread(this);
            thread.start();
        }

        private void resetGame() {
            snake.clear();
            snake.add(new int[]{cols / 2, rows / 2});
            dirX = 1; dirY = 0;
            spawnFood();
            gameOver = false;
        }

        private void spawnFood() {
            food[0] = (int) (Math.random() * Math.max(1, cols));
            food[1] = (int) (Math.random() * Math.max(1, rows));
        }

        @Override
        public void run() {
            while (running) {
                if (!getHolder().getSurface().isValid()) continue;
                long now = System.currentTimeMillis();
                if (!gameOver && now - lastMoveTime > moveIntervalMs) {
                    step();
                    lastMoveTime = now;
                }
                Canvas canvas = getHolder().lockCanvas();
                if (canvas != null) {
                    draw(canvas);
                    getHolder().unlockCanvasAndPost(canvas);
                }
            }
        }

        private void step() {
            int[] head = snake.get(0);
            int[] newHead = {head[0] + dirX, head[1] + dirY};

            if (newHead[0] < 0 || newHead[1] < 0 || newHead[0] >= cols || newHead[1] >= rows) {
                endGame();
                return;
            }
            for (int[] segment : snake) {
                if (segment[0] == newHead[0] && segment[1] == newHead[1]) {
                    endGame();
                    return;
                }
            }

            snake.add(0, newHead);
            if (newHead[0] == food[0] && newHead[1] == food[1]) {
                spawnFood();
            } else {
                snake.remove(snake.size() - 1);
            }
        }

        private void endGame() {
            gameOver = true;
            SharedPreferences prefs = getContext().getSharedPreferences("games", Context.MODE_PRIVATE);
            int best = prefs.getInt("snake_high_score", 0);
            if (snake.size() > best) {
                prefs.edit().putInt("snake_high_score", snake.size()).apply();
            }
        }

        private void draw(Canvas canvas) {
            canvas.drawColor(Color.BLACK);
            paint.setColor(Color.parseColor("#00E5C7"));
            for (int[] segment : snake) {
                canvas.drawRect(segment[0] * CELL, segment[1] * CELL,
                        segment[0] * CELL + CELL - 2, segment[1] * CELL + CELL - 2, paint);
            }
            paint.setColor(Color.RED);
            canvas.drawRect(food[0] * CELL, food[1] * CELL, food[0] * CELL + CELL - 2, food[1] * CELL + CELL - 2, paint);

            if (gameOver) {
                paint.setColor(Color.WHITE);
                paint.setTextSize(48);
                canvas.drawText("Game Over - tap to restart", 40, getHeight() / 2, paint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (gameOver && event.getAction() == MotionEvent.ACTION_DOWN) {
                resetGame();
                return true;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    startY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    float dx = event.getX() - startX;
                    float dy = event.getY() - startY;
                    if (Math.abs(dx) > Math.abs(dy)) {
                        if (dx > 0 && dirX == 0) { dirX = 1; dirY = 0; }
                        else if (dx < 0 && dirX == 0) { dirX = -1; dirY = 0; }
                    } else {
                        if (dy > 0 && dirY == 0) { dirX = 0; dirY = 1; }
                        else if (dy < 0 && dirY == 0) { dirX = 0; dirY = -1; }
                    }
                    break;
            }
            return true;
        }

        void pauseGame() {
            running = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            running = false;
        }
    }
}
