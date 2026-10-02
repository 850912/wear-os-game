package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

public final class ReversiView extends BaseGameView {
    private static final int MODE = GameModes.REVERSI;
    private final ReversiEngine engine = new ReversiEngine();
    private int pendingX = -1, pendingY = -1;

    public ReversiView(Context context) { super(context); }
    public static boolean supportsMode(int mode) { return mode == MODE; }
    @Override protected int gameMode() { return MODE; }
    private RectF board() { return gamePanel(roundScreen ? .12f : .09f); }
    @Override protected void resetGame() { engine.reset(); clearPreview(); }

    @Override protected void drawGame(Canvas c) {
        playAiTurns();
        RectF b = board();
        String state = "黑 " + engine.count(1) + " · 白 " + engine.count(2);
        if (engine.winner() == 0) state += pendingX >= 0 ? " · 再点预览格确认" : " · " + (engine.turn() == 1 ? "黑" : "白") + "走";
        drawHeader(c, "黑白棋", state);
        panel(c, b);
        float cell = Math.min(b.width(), b.height()) / 6f, size = cell * 6;
        float left = b.centerX() - size / 2f, top = b.centerY() - size / 2f;
        p.setColor(Color.rgb(38, 131, 83)); c.drawRoundRect(new RectF(left, top, left + size, top + size), dp(10), dp(10), p);
        p.setColor(Color.rgb(26, 91, 60)); p.setStrokeWidth(dp(1));
        for (int i = 1; i < 6; i++) { c.drawLine(left + i * cell, top, left + i * cell, top + size, p); c.drawLine(left, top + i * cell, left + size, top + i * cell, p); }
        for (int y = 0; y < 6; y++) for (int x = 0; x < 6; x++) {
            int v = engine.at(x, y); float cx = left + (x + .5f) * cell, cy = top + (y + .5f) * cell;
            if (v > 0) {
                p.setColor(v == 1 ? Color.rgb(24, 26, 29) : Color.rgb(244, 244, 239)); c.drawCircle(cx, cy, cell * .36f, p);
                if (richEffectsEnabled()) { p.setColor(v == 1 ? Color.rgb(76, 80, 84) : Color.WHITE); p.setAlpha(105); c.drawCircle(cx - cell * .09f, cy - cell * .10f, cell * .11f, p); p.setAlpha(255); }
            } else if (x == pendingX && y == pendingY) {
                p.setColor(engine.turn() == 1 ? Color.argb(135, 25, 27, 30) : Color.argb(155, 245, 245, 240));
                c.drawCircle(cx, cy, cell * .32f, p); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); p.setColor(PRIMARY); c.drawCircle(cx, cy, cell * .42f, p); p.setStyle(Paint.Style.FILL);
            } else if (engine.legal(x, y)) {
                p.setColor(Color.argb(95, 190, 240, 205)); c.drawCircle(cx, cy, cell * .10f, p);
            }
        }
    }

    @Override protected void onGameTap(float x, float y) {
        if (engine.winner() != 0) return;
        RectF b = board();
        float cell = Math.min(b.width(), b.height()) / 6f, left = b.centerX() - cell * 3, top = b.centerY() - cell * 3;
        int gx = (int)((x - left) / cell), gy = (int)((y - top) / cell);
        if (gx < 0 || gx >= 6 || gy < 0 || gy >= 6 || !engine.legal(gx, gy)) return;
        if (moveConfirmationEnabled() && (pendingX != gx || pendingY != gy)) {
            pendingX = gx; pendingY = gy; haptic(HapticFeedbackConstants.CLOCK_TICK); invalidate(); return;
        }
        clearPreview();
        if (!engine.move(gx, gy)) return;
        haptic(HapticFeedbackConstants.CONFIRM);
        playAiTurns();
        check(); invalidate();
    }

    private void playAiTurns() {
        // A pass can leave white on turn after its move; finish all forced AI turns.
        while (isSinglePlayer() && engine.winner() == 0 && engine.turn() == 2) {
            int[] move = engine.chooseAiMove();
            if (move == null || !engine.move(move[0], move[1])) break;
        }
        check();
    }
    private void clearPreview() { pendingX = pendingY = -1; }
    private void check() {
        int w = engine.winner();
        if (w != 0) finishRound(w == 3 ? 0 : w == 1 ? 1 : w == 2 && isSinglePlayer() ? -1 : 1,
                w == 3 ? "和棋" : w == 1 ? "黑方获胜" : "白方获胜",
                "黑 " + engine.count(1) + " · 白 " + engine.count(2), Math.max(engine.count(1), engine.count(2)));
    }
    @Override protected void saveGame() { if (engine.winner() != 0) { clearSavedGameIfMine(); return; } GameSaveManager.save(prefs, MODE, 1, isSinglePlayer(), engine.serialize()); }
    @Override protected boolean restoreGame() {
        clearPreview(); GameSaveManager.SaveRecord r = GameSaveManager.load(prefs, MODE); if (r == null) return false;
        if (engine.restore(r.payload)) return true; String migrated = LegacySaveMigrator.enginePayload(prefs, MODE, r.payload);
        if (migrated != null && engine.restore(migrated)) { GameSaveManager.save(prefs, MODE, 2, r.ai, migrated); return true; } return false;
    }
}
