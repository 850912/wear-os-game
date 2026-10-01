package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

/** 15x15 Gomoku View + pure engine, with optional tap-twice placement confirmation. */
public final class GomokuView extends BaseGameView {
    private static final int MODE = GameModes.GOMOKU;
    private final GomokuEngine engine = new GomokuEngine();
    private int pendingX = -1, pendingY = -1;

    public GomokuView(Context context) { super(context); }
    public static boolean supportsMode(int mode) { return mode == MODE; }
    @Override protected int gameMode() { return MODE; }
    private RectF board() { return gamePanel(roundScreen ? .08f : .06f); }

    @Override protected void resetGame() {
        engine.reset();
        clearPreview();
    }

    @Override protected void drawGame(Canvas c) {
        RectF b = board();
        String state = engine.winner() != 0 ? "本局结束" :
                pendingX >= 0 ? "再次点击预览位置确认" : (engine.turn() == 1 ? "黑方" : "白方") + "走";
        drawHeader(c, "五子棋", state);
        panel(c, b);

        float cell = Math.min(b.width(), b.height()) / 15f;
        float size = cell * 14f;
        float left = b.centerX() - size / 2f, top = b.centerY() - size / 2f;

        p.setColor(Color.rgb(199, 158, 101));
        c.drawRoundRect(new RectF(left - cell * .48f, top - cell * .48f,
                left + size + cell * .48f, top + size + cell * .48f), cell * .24f, cell * .24f, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1f, dp(.8f)));
        p.setColor(Color.rgb(96, 67, 39));
        for (int i = 0; i < 15; i++) {
            c.drawLine(left, top + i * cell, left + size, top + i * cell, p);
            c.drawLine(left + i * cell, top, left + i * cell, top + size, p);
        }
        p.setStyle(Paint.Style.FILL);

        for (int y = 0; y < 15; y++) for (int x = 0; x < 15; x++) {
            int v = engine.at(x, y);
            if (v == 0) continue;
            drawStone(c, left + x * cell, top + y * cell, cell, v, 255);
            if (x == engine.lastX() && y == engine.lastY()) {
                p.setColor(v == 1 ? Color.WHITE : Color.rgb(35, 35, 35));
                c.drawCircle(left + x * cell, top + y * cell, cell * .075f, p);
            }
        }

        if (pendingX >= 0 && engine.at(pendingX, pendingY) == 0) {
            int who = engine.turn();
            drawStone(c, left + pendingX * cell, top + pendingY * cell, cell, who, 135);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1.8f));
            p.setColor(PRIMARY);
            c.drawCircle(left + pendingX * cell, top + pendingY * cell, cell * .48f, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    private void drawStone(Canvas c, float cx, float cy, float cell, int who, int alpha) {
        p.setColor(who == 1 ? Color.rgb(24, 26, 29) : Color.rgb(244, 242, 235));
        p.setAlpha(alpha);
        c.drawCircle(cx, cy, cell * .39f, p);
        if (richEffectsEnabled()) {
            p.setColor(who == 1 ? Color.rgb(70, 74, 80) : Color.WHITE);
            p.setAlpha((int)(alpha * .45f));
            c.drawCircle(cx - cell * .10f, cy - cell * .11f, cell * .12f, p);
        }
        p.setAlpha(255);
    }

    @Override protected void onGameTap(float x, float y) {
        if (engine.winner() != 0) return;
        RectF b = board();
        float cell = Math.min(b.width(), b.height()) / 15f, size = cell * 14f;
        float left = b.centerX() - size / 2f, top = b.centerY() - size / 2f;
        int gx = Math.round((x - left) / cell), gy = Math.round((y - top) / cell);
        if (gx < 0 || gx >= 15 || gy < 0 || gy >= 15 || engine.at(gx, gy) != 0) return;

        if (moveConfirmationEnabled() && (pendingX != gx || pendingY != gy)) {
            pendingX = gx; pendingY = gy;
            haptic(HapticFeedbackConstants.CLOCK_TICK);
            invalidate();
            return;
        }

        clearPreview();
        if (!engine.place(gx, gy)) return;
        haptic(HapticFeedbackConstants.CONFIRM);
        check();
        if (isSinglePlayer() && engine.winner() == 0 && engine.turn() == 2) {
            int[] move = engine.chooseAiMove();
            if (move != null) engine.place(move[0], move[1]);
            check();
        }
        invalidate();
    }

    private void clearPreview() { pendingX = pendingY = -1; }

    private void check() {
        if (engine.winner() == 1) finishRound(1, "黑方五连", "本局结束", 1);
        else if (engine.winner() == 2) finishRound(isSinglePlayer() ? -1 : 1, "白方五连", "本局结束", 1);
        else if (engine.winner() == 3) finishRound(0, "和棋", "棋盘已满", 1);
    }

    @Override protected void saveGame() {
        if (engine.winner() != 0) { clearSavedGameIfMine(); return; }
        GameSaveManager.save(prefs, MODE, 1, isSinglePlayer(), engine.serialize());
    }

    @Override protected boolean restoreGame() {
        clearPreview();
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
