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

    // Shared short-game motion language from the v8 roadmap: 3-2-1-GO and score popups.
    private long countdownStartedAt;
    private long scorePopupStartedAt;
    private String scorePopupText = "";

    private int pendingControl = -1; // 0 restart, 1 menu
    private long pendingControlUntil;
    private int pressedControl = -1;
    private float downX, downY;
    private boolean downInGame;
    private boolean pinching;
    private boolean pinchConsumed;
    private float pinchDistance;
    private boolean animationPosted;

    // Reused geometry: avoid allocating RectF arrays/objects during every watch frame.
    private final RectF restartRect = new RectF();
    private final RectF menuRect = new RectF();
    private final RectF pillRect = new RectF();
    private final RectF gamePanelRect = new RectF();
    private final RectF resultRect = new RectF();
    private int rectWidth = -1, rectHeight = -1;
    private boolean rectLeftHanded;
    private int releaseControl = -1;
    private long releaseControlAt;

    public interface HostListener { void onExitToHub(); }

    @SuppressWarnings("this-escape") // Android View construction configures inherited View properties; no subclass callback is invoked here.
    protected BaseGameView(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        roundScreen = context.getResources().getConfiguration().isScreenRound();
        prefs = AppSettings.prefs(context);
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
    protected void onGamePinchZoom(float factor) { }
    protected boolean lowerMetricIsBetter() { return false; }

    protected final boolean isSinglePlayer() { return singlePlayer; }
    protected final boolean animationsEnabled() { return AppSettings.animations(prefs); }
    protected final boolean powerSaver() { return AppSettings.saver(prefs); }
    protected final boolean smoothMode() { return AppSettings.smooth(prefs); }
    protected final boolean moveConfirmationEnabled() { return AppSettings.moveConfirm(prefs); }
    protected final boolean richEffectsEnabled() { return AppSettings.richEffects(prefs); }
    protected final boolean haptic(int constant) { return HapticsManager.perform(this, prefs, constant); }
    protected final void sound(int event) { SoundManager.play(prefs, event); }

    protected final float dp(float value) { return value * density; }
    protected final float s() { return Math.min(getWidth(), getHeight()); }
    protected final float clamp(float value, float low, float high) { return Math.max(low, Math.min(high, value)); }
    protected final long now() { return SystemClock.elapsedRealtime(); }

    /** Lightly overshooting easing used for watch-size press/result feedback. */
    protected final float easeOutBack(float t) {
        t = clamp(t, 0f, 1f);
        float c1 = 1.55f, c3 = c1 + 1f, u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }

    protected final float easeOutCubic(float t) {
        t = clamp(t, 0f, 1f);
        float u = 1f - t;
        return 1f - u * u * u;
    }

    protected final void animateNext() {
        if (!isAttachedToWindow() || getWindowVisibility() != VISIBLE) return;
        if (animationPosted) return;
        animationPosted = true;
        long delay = AppSettings.frameDelayMs(prefs);
        if (delay <= 0L) postInvalidateOnAnimation();
        else postInvalidateDelayed(delay);
    }

    protected final void invalidateSoon(long delayMs) {
        if (isAttachedToWindow() && getWindowVisibility() == VISIBLE && !animationPosted) {
            animationPosted = true;
            postInvalidateDelayed(delayMs);
        }
    }

    /** Starts the common 3-2-1-GO overlay used by reaction/action games. */
    protected final void beginStartCountdown() {
        countdownStartedAt = animationsEnabled() ? now() : 0L;
        if (countdownStartedAt != 0L) animateNext();
    }

    /** While true, gameplay physics should remain frozen although the running state may already be armed. */
    protected final boolean startCountdownActive() {
        return countdownStartedAt != 0L && now() - countdownStartedAt < 2600L;
    }

    /** Common score/combo feedback: +1, +10, COMBO xN, etc. */
    protected final void showScorePopup(String label) {
        if (!animationsEnabled() || label == null || label.isEmpty()) return;
        scorePopupText = label;
        scorePopupStartedAt = now();
        animateNext();
    }

    protected final float gameTop() { return s() * (roundScreen ? .115f : .095f); }
    protected final float gameBottom() { updateBottomRects(); return restartRect.top - Math.max(dp(5), s() * .012f); }

    protected final RectF gamePanel(float sideInsetFraction) {
        float inset = getWidth() * sideInsetFraction;
        gamePanelRect.set(inset, gameTop(), getWidth() - inset, gameBottom());
        return gamePanelRect;
    }

    @Override protected final void onDraw(Canvas canvas) {
        animationPosted = false;
        super.onDraw(canvas);
        drawBackground(canvas);
        drawGame(canvas);
        drawStartCountdown(canvas);
        drawScorePopup(canvas);
        drawBottomControls(canvas);
        drawResult(canvas);
    }

    /** Shared chrome can be overridden by games with a canonical visual identity (for example 2048). */
    protected void drawBackground(Canvas canvas) {
        canvas.drawColor(BG);
        if (richEffectsEnabled()) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(34, 92, 145, 220));
            canvas.drawCircle(getWidth() * .50f, -s() * .18f, s() * .72f, p);
            p.setColor(Color.argb(20, 157, 106, 255));
            canvas.drawCircle(getWidth() * .88f, getHeight() * .54f, s() * .55f, p);
        }
    }

    protected int chromeSurfaceColor() { return SURFACE_HIGH; }
    protected int chromeTextColor() { return TEXT; }
    protected int chromeDangerColor() { return Color.rgb(187, 52, 61); }

    protected final void drawHeader(Canvas c, String title, String subtitle) {
        float size = s();
        text(c, title, getWidth() / 2f, size * .061f, size * .041f, TEXT, true, Paint.Align.CENTER);
        if (subtitle != null && !subtitle.isEmpty()) {
            textFit(c, subtitle, getWidth() / 2f, size * .101f, size * .0235f, MUTED, false,
                    Paint.Align.CENTER, getWidth() * (roundScreen ? .66f : .88f));
        }
    }

    protected final void panel(Canvas c, RectF rect) {
        float radius = Math.min(dp(22), rect.height() * .10f);
        p.setStyle(Paint.Style.FILL);
        p.setColor(SURFACE);
        c.drawRoundRect(rect, radius, radius, p);
        if (richEffectsEnabled()) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, dp(.7f)));
            p.setColor(Color.argb(72, 255, 255, 255));
            c.drawRoundRect(rect, radius, radius, p);
            p.setStyle(Paint.Style.FILL);
        }
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
        countdownStartedAt = 0L;
        scorePopupStartedAt = 0L;
        scorePopupText = "";
        pendingControl = -1;
    }

    private void clearTransientUi() {
        roundRecorded = false;
        resultVisible = false;
        countdownStartedAt = 0L;
        scorePopupStartedAt = 0L;
        scorePopupText = "";
        pendingControl = -1;
        pressedControl = -1;
    }

    private void updateBottomRects() {
        boolean leftHanded = AppSettings.leftHanded(prefs);
        if (rectWidth == getWidth() && rectHeight == getHeight() && rectLeftHanded == leftHanded) return;
        rectWidth = getWidth(); rectHeight = getHeight(); rectLeftHanded = leftHanded;
        float size = s();
        float bottom = getHeight() - (roundScreen ? size * .052f : dp(3));
        float h = Math.max(dp(48), Math.min(dp(52), size * .21f));
        float gap = Math.max(dp(6), size * .016f);
        float usable = Math.min(getWidth() * (roundScreen ? .74f : .90f), dp(320));
        float w = (usable - gap) / 2f;
        float left = (getWidth() - usable) / 2f;
        RectF physicalLeft = leftHanded ? menuRect : restartRect;
        RectF physicalRight = leftHanded ? restartRect : menuRect;
        physicalLeft.set(left, bottom - h, left + w, bottom);
        physicalRight.set(left + w + gap, bottom - h, left + w + gap + w, bottom);
    }

    private int bottomControlAt(float x, float y) {
        updateBottomRects();
        if (restartRect.contains(x, y)) return 0;
        if (menuRect.contains(x, y)) return 1;
        return -1;
    }

    private void drawBottomControls(Canvas c) {
        long time = now();
        if (pendingControl >= 0 && time > pendingControlUntil) pendingControl = -1;
        updateBottomRects();
        drawPill(c, restartRect, 0, pendingControl == 0 ? "确认重开" : "重开", pendingControl == 0, pressedControl == 0);
        drawPill(c, menuRect, 1, pendingControl == 1 ? "确认返回" : "菜单", pendingControl == 1, pressedControl == 1);
        if (pendingControl >= 0) invalidateSoon(150);
    }

    private void drawPill(Canvas c, RectF rect, int which, String label, boolean danger, boolean pressed) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(danger ? chromeDangerColor() : chromeSurfaceColor());
        float scale = 1f;
        if (animationsEnabled()) {
            if (pressed) scale = .935f;
            else if (releaseControl == which) {
                float t = clamp((now() - releaseControlAt) / 210f, 0f, 1f);
                scale = .935f + .065f * easeOutBack(t);
                if (t < 1f) animateNext(); else releaseControl = -1;
            }
        }
        pillRect.set(rect);
        float dx = rect.width() * (1f - scale) / 2f, dy = rect.height() * (1f - scale) / 2f;
        pillRect.inset(dx, dy);
        p.setAlpha(pressed ? 210 : 255);
        c.drawRoundRect(pillRect, pillRect.height() / 2f, pillRect.height() / 2f, p);
        p.setAlpha(255);
        textFit(c, label, pillRect.centerX(), pillRect.centerY() + pillRect.height() * .11f,
                pillRect.height() * .27f, chromeTextColor(), true, Paint.Align.CENTER, pillRect.width() * .82f);
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


    private void drawStartCountdown(Canvas c) {
        if (countdownStartedAt == 0L) return;
        long elapsed = now() - countdownStartedAt;
        if (elapsed >= 2600L) { countdownStartedAt = 0L; return; }
        int phase = (int)(elapsed / 650L);
        String label = phase == 0 ? "3" : phase == 1 ? "2" : phase == 2 ? "1" : "GO";
        float local = (elapsed % 650L) / 650f;
        float alpha = 1f - clamp((local - .55f) / .45f, 0f, 1f);
        float scale = .84f + .16f * easeOutBack(clamp(local * 1.7f, 0f, 1f));
        p.setColor(Color.argb((int)(120 * alpha), 0, 0, 0));
        c.drawCircle(getWidth()/2f, getHeight()/2f, s()*.17f, p);
        p.setAlpha((int)(255 * alpha));
        text(c, label, getWidth()/2f, getHeight()/2f + s()*.035f, s()*.13f*scale, TEXT, true, Paint.Align.CENTER);
        p.setAlpha(255);
        animateNext();
    }

    private void drawScorePopup(Canvas c) {
        if (scorePopupStartedAt == 0L) return;
        float t = clamp((now() - scorePopupStartedAt) / 720f, 0f, 1f);
        if (t >= 1f) { scorePopupStartedAt = 0L; scorePopupText = ""; return; }
        float y = gameTop() + s()*.16f - s()*.06f*easeOutCubic(t);
        p.setAlpha((int)(255 * (1f-t)));
        text(c, scorePopupText, getWidth()/2f, y, s()*.038f, GOOD, true, Paint.Align.CENTER);
        p.setAlpha(255);
        animateNext();
    }

    private void drawResult(Canvas c) {
        if (!resultVisible) return;
        float elapsed = now() - resultShownAt;
        float dimIntro = animationsEnabled() ? clamp(elapsed / 180f, 0f, 1f) : 1f;
        float intro = animationsEnabled() ? clamp((elapsed - 110f) / 280f, 0f, 1f) : 1f;
        float eased = animationsEnabled() ? easeOutBack(intro) : 1f;
        float alphaEase = easeOutCubic(dimIntro);
        p.setColor(Color.argb((int)(205 * alphaEase), 0, 0, 0));
        c.drawRect(0, 0, getWidth(), getHeight(), p);
        float width = s() * (roundScreen ? .73f : .82f);
        float height = s() * .34f;
        float scale = .88f + .12f * eased;
        float sw = width * scale, sh = height * scale;
        resultRect.set((getWidth() - sw) / 2f, (getHeight() - sh) / 2f,
                (getWidth() + sw) / 2f, (getHeight() + sh) / 2f);
        RectF card = resultRect;
        if (intro <= 0f) { animateNext(); return; }
        p.setColor(SURFACE_HIGH);
        c.drawRoundRect(card, Math.min(dp(24), sh * .18f), Math.min(dp(24), sh * .18f), p);
        if (intro < 1f || dimIntro < 1f) animateNext();
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
        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            if (downInGame && event.getPointerCount() >= 2) {
                pinching = true;
                pinchDistance = pointerDistance(event);
            }
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (pinching && event.getPointerCount() >= 2) {
                float distance = pointerDistance(event);
                if (pinchDistance > 1f && distance > 1f) onGamePinchZoom(distance / pinchDistance);
                pinchDistance = distance;
                invalidate();
                return true;
            }
            if (downInGame && pressedControl < 0) onGameTouchMove(x, y);
            return true;
        }
        if (action == MotionEvent.ACTION_POINTER_UP) {
            if (pinching) pinchConsumed = true;
            pinching = false;
            return true;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            pressedControl = -1;
            downInGame = false;
            invalidate();
            return true;
        }
        if (action != MotionEvent.ACTION_UP) return true;
        if (pinching || pinchConsumed) {
            pinching = false;
            pinchConsumed = false;
            downInGame = false;
            return true;
        }

        if (control >= 0 && control == pressedControl) {
            pressedControl = -1;
            downInGame = false;
            if (animationsEnabled()) { releaseControl = control; releaseControlAt = now(); }
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

    private float pointerDistance(MotionEvent event) {
        if (event.getPointerCount() < 2) return 0f;
        float dx = event.getX(0) - event.getX(1);
        float dy = event.getY(0) - event.getY(1);
        return (float) Math.hypot(dx, dy);
    }
}
