package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

public final class Connect4View extends BaseGameView {
    private static final int MODE = GameModes.CONNECT4;
    private final Connect4Engine engine = new Connect4Engine();
    private int pendingCol = -1;

    public Connect4View(Context context) { super(context); }
    public static boolean supportsMode(int mode) { return mode == MODE; }
    @Override protected int gameMode() { return MODE; }
    private RectF board() { return gamePanel(roundScreen ? .11f : .08f); }

    @Override protected void resetGame() { engine.reset(); pendingCol = -1; }

    @Override protected void drawGame(Canvas c) {
        RectF b = board();
        String state = engine.winner() != 0 ? "本局结束" : pendingCol >= 0 ? "再次点击该列确认" :
                (engine.turn() == 1 ? "黄方" : "红方") + "走";
        drawHeader(c, "四子棋", state);
        panel(c, b);
        float cell = Math.min(b.width() / 7f, b.height() / 6f), w = cell * 7, h = cell * 6;
        float left = b.centerX() - w / 2f, top = b.centerY() - h / 2f;
        p.setColor(Color.rgb(38, 91, 193));
        c.drawRoundRect(new RectF(left, top, left + w, top + h), dp(12), dp(12), p);
        for (int y = 0; y < 6; y++) for (int x = 0; x < 7; x++) {
            int v = engine.at(x, y);
            p.setColor(v == 0 ? Color.rgb(21, 31, 48) : v == 1 ? Color.rgb(251, 209, 70) : Color.rgb(245, 89, 92));
            c.drawCircle(left + (x + .5f) * cell, top + (y + .5f) * cell, cell * .37f, p);
        }
        if (pendingCol >= 0) {
            int row = dropRow(pendingCol);
            if (row >= 0) {
                p.setColor(engine.turn() == 1 ? Color.argb(150, 251, 209, 70) : Color.argb(150, 245, 89, 92));
                c.drawCircle(left + (pendingCol + .5f) * cell, top + (row + .5f) * cell, cell * .33f, p);
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); p.setColor(PRIMARY);
                c.drawCircle(left + (pendingCol + .5f) * cell, top + (row + .5f) * cell, cell * .43f, p);
                p.setStyle(Paint.Style.FILL);
            }
        }
    }

    private int dropRow(int col) {
        if (col < 0 || col >= 7) return -1;
        for (int y = 5; y >= 0; y--) if (engine.at(col, y) == 0) return y;
        return -1;
    }

    @Override protected void onGameTap(float x, float y) {
        if (engine.winner() != 0) return;
        RectF b = board();
        float cell = Math.min(b.width() / 7f, b.height() / 6f), left = b.centerX() - cell * 3.5f;
        int col = (int)((x - left) / cell);
        if (dropRow(col) < 0) return;
        if (moveConfirmationEnabled() && pendingCol != col) {
            pendingCol = col;
            haptic(HapticFeedbackConstants.CLOCK_TICK);
            invalidate();
            return;
        }
        pendingCol = -1;
        if (!engine.drop(col)) return;
        haptic(HapticFeedbackConstants.CONFIRM);
        check();
        if (isSinglePlayer() && engine.winner() == 0 && engine.turn() == 2) {
            int ai = engine.chooseAiMove();
            if (ai >= 0) engine.drop(ai);
            check();
        }
        invalidate();
    }

    private void check() {
        int w = engine.winner();
        if (w == 1) finishRound(1, "黄方四连", "本局结束", 1);
        else if (w == 2) finishRound(isSinglePlayer() ? -1 : 1, "红方四连", "本局结束", 1);
        else if (w == 3) finishRound(0, "和棋", "棋盘已满", 1);
    }

    @Override protected void saveGame() {
        if (engine.winner() != 0) { clearSavedGameIfMine(); return; }
        GameSaveManager.save(prefs, MODE, 1, isSinglePlayer(), engine.serialize());
    }

    @Override protected boolean restoreGame() {
        pendingCol = -1;
        GameSaveManager.SaveRecord r = GameSaveManager.load(prefs, MODE);
        if (r == null) return false;
        if (engine.restore(r.payload)) return true;
        String migrated = LegacySaveMigrator.enginePayload(prefs, MODE, r.payload);
        if (migrated != null && engine.restore(migrated)) {
            GameSaveManager.save(prefs, MODE, 2, r.ai, migrated);
            return true;
        }
        return false;
    }
}
