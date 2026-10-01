package com.example.wearboardgames;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

/**
 * Small shared shell for real-time games.
 *
 * The shell deliberately owns only common watch UI/input behavior: Material-3-inspired HUD,
 * deliberate restart/exit controls, result recording, and VSYNC scheduling. Game state and
 * physics stay in each concrete View so one game cannot accidentally mutate another one.
 */
public abstract class BaseGameView extends View {
    protected static final Typeface NORMAL = Typeface.create("sans", Typeface.NORMAL);
    protected static final Typeface BOLD = Typeface.create("sans", Typeface.BOLD);

    protected static final int BG = Color.rgb(8, 10, 14);
    protected static final int SURFACE = Color.rgb(29, 32, 40);
    protected static final int SURFACE_HIGH = Color.rgb(43, 47, 57);
    protected static final int PRIMARY = Color.rgb(154, 203, 255);
    protected static final int SECONDARY = Color.rgb(207, 181, 255);
    protected static final int GOOD = Color.rgb(104, 219, 159);
    protected static final int BAD = Color.rgb(255, 118, 127);
    protected static final int TEXT = Color.rgb(245, 247, 250);
    protected static final int MUTED = Color.rgb(170, 180, 193);

    protected final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final SharedPreferences prefs;
    protected final float density;
    protected final boolean roundScreen;

    private HostListener hostListener;
    private int mode;
    private boolean singlePlayer = true;
    private boolean roundRecorded;
    private long statsCheckpoint;

    private boolean resultVisible;
    private int resultKind;
    private String resultTitle = "";
    private String resultSubtitle = "";
    private long resultShownAt;

    private int pendingControl = -1; // 0 restart, 1 menu
    private long pendingControlUntil;
    private int pressedControl = -1;
    private float downX, downY;
    private boolean downInGame;

    public interface HostListener { void onExitToHub(); }

    @SuppressWarnings("this-escape") // Android View construction configures inherited View properties; no subclass callback is invoked here.
    protected BaseGameView(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        roundScreen = context.getResources().getConfiguration().isScreenRound();
        prefs = context.getSharedPreferences("wear_games", Context.MODE_PRIVATE);
        p.setTypeface(NORMAL);
        setBackgroundColor(BG);
        setFocusable(true);
        setHapticFeedbackEnabled(AppSettings.haptics(prefs));
    }

    public final void setHostListener(HostListener listener) { hostListener = listener; }

    public final void openGame(int gameMode, boolean singlePlayerMode) {
        if (!supportsGameMode(gameMode)) throw new IllegalArgumentException("Unsupported game mode: " + gameMode);
        mode = gameMode;
        singlePlayer = singlePlayerMode;
        setHapticFeedbackEnabled(AppSettings.haptics(prefs));
        clearTransientUi();
        clearSavedGameIfMine();
        GameStats.recordLaunch(prefs, gameMode());
        statsCheckpoint = now();
        resetGame();
        invalidate();
    }

    public void resumeSavedGameExternal() {
        clearTransientUi();
        statsCheckpoint = now();
        if (!restoreGame()) {
            clearSavedGameIfMine();
            resetGame();
        }
        invalidate();
    }

    /** Resume entry point for grouped Views whose game id is selected at runtime. */
    public final void resumeSavedGameExternal(int gameMode, boolean singlePlayerMode) {
        if (!supportsGameMode(gameMode)) throw new IllegalArgumentException("Unsupported game mode: " + gameMode);
        mode = gameMode;
        singlePlayer = singlePlayerMode;
        setHapticFeedbackEnabled(AppSettings.haptics(prefs));
        resumeSavedGameExternal();
    }

    public void persistCurrentState() {
        saveGame();
        long t = now();
        if (statsCheckpoint > 0 && t > statsCheckpoint) GameStats.addPlayTime(prefs, gameMode(), t - statsCheckpoint);
        statsCheckpoint = t;
    }

    protected abstract int gameMode();
    protected boolean supportsGameMode(int candidate) { return candidate == gameMode(); }
    protected final int activeGameMode() { return mode; }
    protected abstract void resetGame();
    protected abstract void drawGame(Canvas canvas);

    protected boolean restoreGame() { return false; }
    protected void saveGame() { }
    protected void onGameTap(float x, float y) { }
    protected void onGameSwipe(float dx, float dy) { }
    protected void onGameTouchDown(float x, float y) { }
    protected void onGameTouchMove(float x, float y) { }
    protected void onGameTouchUp(float x, float y) { }
    protected boolean lowerMetricIsBetter() { return false; }

    protected final boolean isSinglePlayer() { return singlePlayer; }
    protected final boolean animationsEnabled() { return AppSettings.animations(prefs); }
    protected final boolean powerSaver() { return AppSettings.saver(prefs); }
    protected final boolean smoothMode() { return AppSettings.smooth(prefs); }
    protected final boolean haptic(int constant) { return HapticsManager.perform(this, prefs, constant); }
    protected final void sound(int event) { SoundManager.play(prefs, event); }

    protected final float dp(float value) { return value * density; }
    protected final float s() { return Math.min(getWidth(), getHeight()); }
    protected final float clamp(float value, float low, float high) { return Math.max(low, Math.min(high, value)); }
    protected final long now() { return SystemClock.elapsedRealtime(); }

    protected final void animateNext() {
        if (!isAttachedToWindow() || getWindowVisibility() != VISIBLE) return;
        if (powerSaver()) postInvalidateDelayed(33);
        else postInvalidateOnAnimation();
    }

    protected final void invalidateSoon(long delayMs) {
        if (isAttachedToWindow() && getWindowVisibility() == VISIBLE) postInvalidateDelayed(delayMs);
    }

    protected final float gameTop() { return s() * (roundScreen ? .115f : .095f); }
    protected final float gameBottom() { return bottomRects()[0].top - Math.max(dp(5), s() * .012f); }

    protected final RectF gamePanel(float sideInsetFraction) {
        float inset = getWidth() * sideInsetFraction;
        return new RectF(inset, gameTop(), getWidth() - inset, gameBottom());
    }

    @Override protected final void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(BG);
        drawGame(canvas);
        drawBottomControls(canvas);
        drawResult(canvas);
    }

    protected final void drawHeader(Canvas c, String title, String subtitle) {
        float size = s();
        text(c, title, getWidth() / 2f, size * .061f, size * .041f, TEXT, true, Paint.Align.CENTER);
        if (subtitle != null && !subtitle.isEmpty()) {
            textFit(c, subtitle, getWidth() / 2f, size * .101f, size * .0235f, MUTED, false,
                    Paint.Align.CENTER, getWidth() * (roundScreen ? .66f : .88f));
        }
    }

    protected final void panel(Canvas c, RectF rect) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(SURFACE);
        c.drawRoundRect(rect, Math.min(dp(22), rect.height() * .10f), Math.min(dp(22), rect.height() * .10f), p);
    }

    protected final void text(Canvas c, String value, float x, float y, float size, int color, boolean bold, Paint.Align align) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        p.setTextSize(size);
        p.setTextAlign(align);
        p.setTypeface(bold ? BOLD : NORMAL);
        c.drawText(value, x, y, p);
    }

    protected final void textFit(Canvas c, String value, float x, float y, float size, int color,
                                 boolean bold, Paint.Align align, float maxWidth) {
        p.setTypeface(bold ? BOLD : NORMAL);
        float fs = size;
        p.setTextSize(fs);
        while (fs > s() * .017f && p.measureText(value) > maxWidth) {
            fs *= .93f;
            p.setTextSize(fs);
        }
        text(c, value, x, y, fs, color, bold, align);
    }

    protected final void finishRound(int kind, String title, String subtitle, int metric) {
        resultKind = kind;
        resultTitle = title;
        resultSubtitle = subtitle;
        resultVisible = true;
        resultShownAt = now();
        if (!roundRecorded) {
            roundRecorded = true;
            GameStats.recordResult(prefs, gameMode(), kind, metric, lowerMetricIsBetter());
        }
        if (kind != 0) haptic(kind > 0 ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.REJECT);
        sound(kind > 0 ? SoundManager.CLEAR : SoundManager.GAME_OVER);
        clearSavedGameIfMine();
        invalidate();
    }

    protected final void clearSavedGameIfMine() {
        if (prefs.getBoolean("has_save", false) && prefs.getInt("last_mode", -1) == gameMode()) {
            GameSaveManager.clearIfGame(prefs, gameMode());
        }
    }

    protected final void prepareForFreshRound() {
        roundRecorded = false;
        resultVisible = false;
        pendingControl = -1;
    }

    private void clearTransientUi() {
        roundRecorded = false;
        resultVisible = false;
        pendingControl = -1;
        pressedControl = -1;
    }

    private RectF[] bottomRects() {
        float size = s();
        float bottom = getHeight() - (roundScreen ? size * .052f : dp(3));
        float h = Math.max(dp(40), Math.min(dp(44), size * .19f));
        float gap = Math.max(dp(6), size * .016f);
        float usable = Math.min(getWidth() * (roundScreen ? .74f : .90f), dp(320));
        float w = (usable - gap) / 2f;
        float left = (getWidth() - usable) / 2f;
        RectF leftRect = new RectF(left, bottom - h, left + w, bottom);
        RectF rightRect = new RectF(left + w + gap, bottom - h, left + w + gap + w, bottom);
        // Left-handed mode puts the safer "menu" action on the thumb-side left and restart on the right.
        return AppSettings.leftHanded(prefs) ? new RectF[]{rightRect, leftRect} : new RectF[]{leftRect, rightRect};
    }

    private int bottomControlAt(float x, float y) {
        RectF[] rects = bottomRects();
        if (rects[0].contains(x, y)) return 0;
        if (rects[1].contains(x, y)) return 1;
        return -1;
    }

    private void drawBottomControls(Canvas c) {
        long time = now();
        if (pendingControl >= 0 && time > pendingControlUntil) pendingControl = -1;
        RectF[] rects = bottomRects();
        drawPill(c, rects[0], pendingControl == 0 ? "确认重开" : "重开", pendingControl == 0, pressedControl == 0);
        drawPill(c, rects[1], pendingControl == 1 ? "确认返回" : "菜单", pendingControl == 1, pressedControl == 1);
        if (pendingControl >= 0) invalidateSoon(150);
    }

    private void drawPill(Canvas c, RectF rect, String label, boolean danger, boolean pressed) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(danger ? Color.rgb(187, 52, 61) : SURFACE_HIGH);
        float scale = pressed && animationsEnabled() ? .96f : 1f;
        RectF r = new RectF(rect);
        float dx = rect.width() * (1f - scale) / 2f, dy = rect.height() * (1f - scale) / 2f;
        r.inset(dx, dy);
        p.setAlpha(pressed ? 205 : 255);
        c.drawRoundRect(r, r.height() / 2f, r.height() / 2f, p);
        p.setAlpha(255);
        textFit(c, label, r.centerX(), r.centerY() + r.height() * .11f,
                r.height() * .27f, TEXT, true, Paint.Align.CENTER, r.width() * .82f);
    }

    private void handleBottomControl(int which) {
        long time = now();
        if (pendingControl == which && time <= pendingControlUntil) {
            pendingControl = -1;
            if (which == 0) {
                clearSavedGameIfMine();
                prepareForFreshRound();
                resetGame();
                invalidate();
            } else {
                saveGame();
                if (hostListener != null) hostListener.onExitToHub();
            }
            return;
        }
        pendingControl = which;
        pendingControlUntil = time + 1400;
        haptic(HapticFeedbackConstants.CLOCK_TICK);
        sound(SoundManager.CLICK);
        invalidate();
    }

    private void drawResult(Canvas c) {
        if (!resultVisible) return;
        float intro = animationsEnabled() ? clamp((now() - resultShownAt) / 220f, 0f, 1f) : 1f;
        float eased = 1f - (1f - intro) * (1f - intro);
        p.setColor(Color.argb((int)(205 * eased), 0, 0, 0));
        c.drawRect(0, 0, getWidth(), getHeight(), p);
        float width = s() * (roundScreen ? .73f : .82f);
        float height = s() * .34f;
        float scale = .90f + .10f * eased;
        float sw = width * scale, sh = height * scale;
        RectF card = new RectF((getWidth() - sw) / 2f, (getHeight() - sh) / 2f,
                (getWidth() + sw) / 2f, (getHeight() + sh) / 2f);
        p.setColor(SURFACE_HIGH);
        c.drawRoundRect(card, Math.min(dp(24), sh * .18f), Math.min(dp(24), sh * .18f), p);
        if (intro < 1f) animateNext();
        int accent = resultKind > 0 ? GOOD : resultKind < 0 ? BAD : PRIMARY;
        textFit(c, resultTitle, card.centerX(), card.top + height * .34f, s() * .056f,
                accent, true, Paint.Align.CENTER, width * .82f);
        textFit(c, resultSubtitle, card.centerX(), card.top + height * .57f, s() * .031f,
                TEXT, false, Paint.Align.CENTER, width * .84f);
        text(c, "轻点再来一局", card.centerX(), card.bottom - height * .13f, s() * .027f,
                MUTED, false, Paint.Align.CENTER);
    }

    @Override public final boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        int action = event.getActionMasked();
        if (resultVisible) {
            if (action == MotionEvent.ACTION_UP && now() - resultShownAt > 220) {
                prepareForFreshRound();
                resetGame();
                invalidate();
            }
            return true;
        }

        int control = bottomControlAt(x, y);
        if (action == MotionEvent.ACTION_DOWN) {
            downX = x;
            downY = y;
            pressedControl = control;
            downInGame = control < 0 && y >= gameTop() && y < gameBottom();
            if (downInGame) onGameTouchDown(x, y);
            invalidate();
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (downInGame && pressedControl < 0) onGameTouchMove(x, y);
            return true;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            pressedControl = -1;
            downInGame = false;
            invalidate();
            return true;
        }
        if (action != MotionEvent.ACTION_UP) return true;

        if (control >= 0 && control == pressedControl) {
            pressedControl = -1;
            downInGame = false;
            handleBottomControl(control);
            return true;
        }

        pressedControl = -1;
        if (!downInGame || y >= gameBottom()) {
            downInGame = false;
            return true;
        }
        onGameTouchUp(x, y);
        float dx = x - downX, dy = y - downY;
        float threshold = Math.max(dp(18), s() * .055f);
        if (Math.hypot(dx, dy) >= threshold) onGameSwipe(dx, dy);
        else onGameTap(x, y);
        downInGame = false;
        return true;
    }
}
