package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

/**
 * Native Wear port of the visual language and motion timing from Gabriele Cirulli's MIT-licensed
 * 2048 web game. The engine remains native so the watch does not pay the cost of a WebView.
 */
public final class Game2048View extends BaseGameView {
    private static final int MODE = GameModes.GAME_2048;
    private static final String BEST_KEY = "2048_best_score";

    // Canonical 2048 palette from the upstream stylesheet.
    private static final int PAGE_BG = Color.rgb(250, 248, 239);      // #faf8ef
    private static final int TEXT_DARK = Color.rgb(119, 110, 101);   // #776e65
    private static final int GRID_BG = Color.rgb(187, 173, 160);     // #bbada0
    private static final int GRID_CELL = Color.rgb(205, 193, 180);   // visual equivalent of rgba(#eee4da,.35)
    private static final int LIGHT_TEXT = Color.rgb(249, 246, 242);  // #f9f6f2
    private static final int SCORE_LABEL = Color.rgb(238, 228, 218); // #eee4da
    private static final int BUTTON = Color.rgb(143, 122, 102);      // #8f7a66

    private static final long SLIDE_MS = 100L;
    private static final long POP_MS = 200L;
    private static final long SCORE_FLOAT_MS = 600L;

    private final Game2048Engine engine = new Game2048Engine();
    private final RectF tileRect = new RectF();
    private final RectF boardRect = new RectF();
    private Game2048Engine.MoveFrame moveFrame;
    private long moveStartedAt;
    private boolean over;
    private int best;

    public Game2048View(Context context) { super(context); }
    public static boolean supportsMode(int mode) { return mode == MODE; }
    @Override protected int gameMode() { return MODE; }

    @Override protected void drawBackground(Canvas canvas) { canvas.drawColor(PAGE_BG); }
    @Override protected int chromeSurfaceColor() { return BUTTON; }
    @Override protected int chromeTextColor() { return LIGHT_TEXT; }
    @Override protected int chromeDangerColor() { return Color.rgb(165, 88, 72); }

    private RectF board() {
        RectF area = gamePanel(roundScreen ? .13f : .10f);
        float side = Math.min(area.width(), area.height());
        float top = area.bottom - side;
        boardRect.set(getWidth() / 2f - side / 2f, top, getWidth() / 2f + side / 2f, top + side);
        return boardRect;
    }

    @Override protected void resetGame() {
        engine.reset();
        over = false;
        moveFrame = null;
        best = Math.max(prefs.getInt(BEST_KEY, 0), engine.getScore());
    }

    @Override protected void drawGame(Canvas c) {
        drawCanonicalHeading(c);
        RectF b = board();
        float gap = b.width() * .03f;
        float cell = (b.width() - gap * 5f) / 4f;
        float left = b.left + gap, top = b.top + gap;

        p.setStyle(Paint.Style.FILL);
        p.setColor(GRID_BG);
        c.drawRoundRect(b, dp(5), dp(5), p);
        p.setColor(GRID_CELL);
        for (int y = 0; y < 4; y++) for (int x = 0; x < 4; x++) {
            tileRect.set(left + x * (cell + gap), top + y * (cell + gap),
                    left + x * (cell + gap) + cell, top + y * (cell + gap) + cell);
            c.drawRoundRect(tileRect, dp(3), dp(3), p);
        }

        long elapsed = now() - moveStartedAt;
        boolean animate = animationsEnabled() && moveFrame != null;
        if (animate && elapsed < SLIDE_MS) {
            float t = easeOutCubic(elapsed / (float)SLIDE_MS);
            for (Game2048Engine.TileMotion motion : moveFrame.motions) {
                float gx = motion.fromX + (motion.toX - motion.fromX) * t;
                float gy = motion.fromY + (motion.toY - motion.fromY) * t;
                drawTile(c, left + gx * (cell + gap), top + gy * (cell + gap), cell, motion.value, 1f);
            }
            animateNext();
        } else {
            float popT = animate ? clamp((elapsed - SLIDE_MS) / (float)POP_MS, 0f, 1f) : 1f;
            for (int y = 0; y < 4; y++) for (int x = 0; x < 4; x++) {
                int value = engine.valueAt(x, y);
                if (value == 0) continue;
                float scale = 1f;
                if (animate && moveFrame.spawn != null && moveFrame.spawn.x == x && moveFrame.spawn.y == y) {
                    scale = easeOutCubic(popT); // upstream tile-new "appear"
                } else if (animate && moveFrame.merged[y][x]) {
                    // Upstream tile-merged "pop": 0 -> 1.2 -> 1.
                    scale = popT < .5f ? 2.4f * popT : 1.2f - .2f * ((popT - .5f) / .5f);
                }
                drawTile(c, left + x * (cell + gap), top + y * (cell + gap), cell, value, scale);
            }
            if (animate && elapsed < SLIDE_MS + POP_MS) animateNext();
        }

        if (animate && moveFrame.scoreGain > 0 && elapsed < SCORE_FLOAT_MS) {
            float t = clamp(elapsed / (float)SCORE_FLOAT_MS, 0f, 1f);
            int alpha = (int)(220 * (1f - t));
            text(c, "+" + moveFrame.scoreGain, getWidth() * .68f, s() * (.105f - .045f * t),
                    s() * .028f, Color.argb(alpha, 119, 110, 101), true, Paint.Align.CENTER);
            animateNext();
        } else if (animate && elapsed >= SCORE_FLOAT_MS) {
            moveFrame = null;
        }
    }

    private void drawCanonicalHeading(Canvas c) {
        float side = s();
        text(c, "2048", getWidth() * .22f, side * .083f, side * .073f, TEXT_DARK, true, Paint.Align.CENTER);
        drawScoreBox(c, getWidth() * .58f, side * .025f, getWidth() * .76f, side * .108f, "SCORE", engine.getScore());
        drawScoreBox(c, getWidth() * .78f, side * .025f, getWidth() * .96f, side * .108f, "BEST", best);
    }

    private void drawScoreBox(Canvas c, float l, float t, float r, float b, String label, int value) {
        tileRect.set(l, t, r, b);
        p.setColor(GRID_BG);
        c.drawRoundRect(tileRect, dp(4), dp(4), p);
        text(c, label, tileRect.centerX(), t + tileRect.height() * .35f, s() * .0145f, SCORE_LABEL, true, Paint.Align.CENTER);
        textFit(c, String.valueOf(value), tileRect.centerX(), b - tileRect.height() * .16f,
                s() * .026f, LIGHT_TEXT, true, Paint.Align.CENTER, tileRect.width() * .86f);
    }

    private void drawTile(Canvas c, float l, float t, float cell, int value, float scale) {
        if (scale <= .01f) return;
        float cx = l + cell / 2f, cy = t + cell / 2f, half = cell * .5f * scale;
        tileRect.set(cx - half, cy - half, cx + half, cy + half);
        p.setColor(tileColor(value));
        c.drawRoundRect(tileRect, dp(3), dp(3), p);
        int digits = String.valueOf(value).length();
        float font = digits <= 2 ? cell * .43f : digits == 3 ? cell * .36f : digits == 4 ? cell * .29f : cell * .23f;
        int textColor = value <= 4 ? TEXT_DARK : LIGHT_TEXT;
        textFit(c, String.valueOf(value), cx, cy + font * .34f, font * scale, textColor, true, Paint.Align.CENTER, cell * .86f * scale);
    }

    private int tileColor(int value) {
        switch (value) {
            case 2: return Color.rgb(238, 228, 218);
            case 4: return Color.rgb(237, 224, 200);
            case 8: return Color.rgb(242, 177, 121);
            case 16: return Color.rgb(245, 149, 99);
            case 32: return Color.rgb(246, 124, 95);
            case 64: return Color.rgb(246, 94, 59);
            case 128: return Color.rgb(237, 207, 114);
            case 256: return Color.rgb(237, 204, 97);
            case 512: return Color.rgb(237, 200, 80);
            case 1024: return Color.rgb(237, 197, 63);
            case 2048: return Color.rgb(237, 194, 46);
            default: return Color.rgb(60, 58, 50);
        }
    }

    @Override protected void onGameSwipe(float dx, float dy) {
        if (over) return;
        int direction = Math.abs(dx) > Math.abs(dy) ? (dx > 0 ? 1 : 3) : (dy > 0 ? 2 : 0);
        Game2048Engine.MoveFrame frame = engine.move(direction);
        if (!frame.changed) { haptic(HapticFeedbackConstants.CLOCK_TICK); return; }

        moveFrame = animationsEnabled() ? frame : null;
        moveStartedAt = now();
        best = Math.max(best, engine.getScore());
        prefs.edit().putInt(BEST_KEY, best).apply();
        haptic(HapticFeedbackConstants.CLOCK_TICK);
        if (frame.scoreGain > 0) sound(SoundManager.SCORE);
        over = frame.gameOver;
        if (over) finishRound(-1, "Game over!", "得分 " + engine.getScore(), engine.getScore());
        invalidate();
    }

    @Override protected void saveGame() {
        if (over) { clearSavedGameIfMine(); return; }
        GameSaveManager.save(prefs, MODE, 2, false, engine.serialize());
    }

    @Override protected boolean restoreGame() {
        GameSaveManager.SaveRecord r = GameSaveManager.load(prefs, MODE);
        if (r == null) return false;
        over = false;
        moveFrame = null;
        if (!engine.restore(r.payload)) return false;
        best = Math.max(prefs.getInt(BEST_KEY, 0), engine.getScore());
        prefs.edit().putInt(BEST_KEY, best).apply();
        return true;
    }
}
