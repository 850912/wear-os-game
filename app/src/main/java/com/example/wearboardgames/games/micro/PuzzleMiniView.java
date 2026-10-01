package com.example.wearboardgames;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/**
 * Second lightweight game renderer for short puzzle/reflex games.
 *
 * New games live here instead of growing the legacy GameModes. Static games invalidate only
 * after input/timers; the two moving timing games use VSYNC-aligned invalidation while active.
 */
public final class PuzzleMiniView extends View {
    public static final int TARGET_TAP = 53;
    public static final int SWIPE_ARROW = 54;
    public static final int STOP_BAR = 55;
    public static final int PAIR_SUM = 56;
    public static final int SEQUENCE_NEXT = 57;
    public static final int SHAPE_MATCH = 58;
    public static final int QUICK_COUNT = 59;
    public static final int SAFE_ZONE = 60;

    private static final Typeface NORMAL = Typeface.create("sans", Typeface.NORMAL);
    private static final Typeface BOLD = Typeface.create("sans", Typeface.BOLD);
    private static final int BG = Color.rgb(7, 9, 13);
    private static final int SURFACE = Color.rgb(27, 31, 39);
    private static final int SURFACE_2 = Color.rgb(42, 48, 59);
    private static final int PRIMARY = Color.rgb(137, 210, 255);
    private static final int SECONDARY = Color.rgb(184, 159, 255);
    private static final int GOOD = Color.rgb(91, 218, 153);
    private static final int BAD = Color.rgb(255, 111, 126);
    private static final int TEXT = Color.rgb(243, 247, 250);
    private static final int MUTED = Color.rgb(166, 178, 191);

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final Random random = new Random();
    private final SharedPreferences prefs;
    private final float density;
    private final boolean roundScreen;

    private HostListener hostListener;
    private int mode = TARGET_TAP;
    private boolean roundRecorded;
    private long statsCheckpoint;

    // Result overlay.
    private boolean resultVisible;
    private int resultKind;
    private String resultTitle = "";
    private String resultSub = "";
    private long resultShownAt;

    // Deliberate bottom controls.
    private int pendingControl = -1;
    private long pendingControlUntil;
    private int pressedControl = -1;
    private final RectF restartRect = new RectF();
    private final RectF menuRect = new RectF();
    private final RectF boardRect = new RectF();
    private final RectF scratchRect = new RectF();
    private final RectF scratchRect2 = new RectF();
    private final RectF resultRect = new RectF();
    private final RectF pressedRect = new RectF();
    private int controlWidth = -1, controlHeight = -1;
    private boolean controlLeftHanded;

    // Gesture tracking.
    private float downX, downY;
    private boolean downInGame;

    // Target tap.
    private float targetX, targetY, targetR;
    private int targetHits, targetMisses;
    private long targetDeadline;
    private boolean targetOver;

    // Swipe arrow.
    private int swipeDirection, swipeScore, swipeRound;
    private boolean swipeOver;
    private long swipePulseAt;

    // Stop bar.
    private float stopPosition, stopVelocity;
    private int stopRound, stopScore;
    private long stopLastTick;
    private boolean stopOver;

    // Pair sum.
    private final int[] pairValues = new int[6];
    private int pairTarget, pairFirst = -1, pairRound, pairScore;
    private boolean pairOver;

    // Sequence next.
    private final int[] sequenceOptions = new int[4];
    private int sequenceStart, sequenceStep, sequenceCorrect, sequenceRound, sequenceScore;
    private boolean sequenceOver;

    // Shape match.
    private int shapeA, shapeB, shapeColorA, shapeColorB, shapeRound, shapeScore;
    private boolean shapeShouldMatch, shapeOver;

    // Quick count.
    private final float[] countXs = new float[9];
    private final float[] countYs = new float[9];
    private final int[] countOptions = new int[4];
    private int countDots, countCorrect, countRound, countScore;
    private boolean countOver;

    // Safe zone.
    private float safePosition, safeVelocity, safeCenter, safeWidth;
    private int safeRound, safeScore;
    private long safeLastTick;
    private boolean safeOver;

    public interface HostListener { void onExitToHub(); }

    public PuzzleMiniView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        roundScreen = getResources().getConfiguration().isScreenRound();
        prefs = AppSettings.prefs(context);
        p.setTypeface(NORMAL);
        setBackgroundColor(BG);
        setFocusable(true);
        setHapticFeedbackEnabled(AppSettings.haptics(prefs));
    }

    public static boolean supportsMode(int mode) {
        return mode >= TARGET_TAP && mode <= SAFE_ZONE;
    }

    public void setHostListener(HostListener listener) { hostListener = listener; }
    public void persistCurrentState() { long t=SystemClock.elapsedRealtime(); if(statsCheckpoint>0&&t>statsCheckpoint) GameStats.addPlayTime(prefs,mode,t-statsCheckpoint); statsCheckpoint=t; }
    public void resumeSavedGameExternal() { resetCurrent(); }

    public void openGame(int gameMode, boolean singlePlayer) {
        if (!supportsMode(gameMode)) throw new IllegalArgumentException("Unsupported puzzle mini mode: " + gameMode);
        mode = gameMode;
        setHapticFeedbackEnabled(AppSettings.haptics(prefs));
        GameStats.recordLaunch(prefs, mode);
        statsCheckpoint=SystemClock.elapsedRealtime();
        resetCurrent();
    }

    private float dp(float value) { return value * density; }
    private float side() { return Math.min(getWidth(), getHeight()); }
    private float contentTop() { return side() * (roundScreen ? .155f : .13f); }
    private float contentBottom() { return getHeight() - dp(50); }

    private void nextFrame() {
        if (isAttachedToWindow() && getWindowVisibility() == VISIBLE) {
            long delay = AppSettings.frameDelayMs(prefs);
            if (delay <= 0L) postInvalidateOnAnimation(); else postInvalidateDelayed(delay);
        }
    }

    private void invalidateLater(long delayMs) {
        if (isAttachedToWindow() && getWindowVisibility() == VISIBLE) postInvalidateDelayed(delayMs);
    }

    private RectF board() {
        float s = side();
        float width = s * (roundScreen ? .68f : .76f);
        float top = contentTop() + s * .01f;
        boardRect.set((getWidth() - width) / 2f, top, (getWidth() + width) / 2f,
                Math.min(contentBottom() - dp(4), top + width));
        return boardRect;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(BG);
        if (AppSettings.richEffects(prefs)) {
            float size = side();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(28, 82, 145, 220));
            canvas.drawCircle(getWidth() * .50f, -size * .18f, size * .70f, p);
            p.setColor(Color.argb(18, 157, 106, 255));
            canvas.drawCircle(getWidth() * .90f, getHeight() * .55f, size * .52f, p);
        }
        drawHeader(canvas);
        switch (mode) {
            case TARGET_TAP: drawTargetTap(canvas); break;
            case SWIPE_ARROW: drawSwipeArrow(canvas); break;
            case STOP_BAR: drawStopBar(canvas); break;
            case PAIR_SUM: drawPairSum(canvas); break;
            case SEQUENCE_NEXT: drawSequenceNext(canvas); break;
            case SHAPE_MATCH: drawShapeMatch(canvas); break;
            case QUICK_COUNT: drawQuickCount(canvas); break;
            case SAFE_ZONE: drawSafeZone(canvas); break;
        }
        drawBottomControls(canvas);
        drawResult(canvas);
    }

    private void drawHeader(Canvas c) {
        text(c, titleFor(mode), getWidth() / 2f, side() * .078f, side() * .054f, TEXT, true, Paint.Align.CENTER);
        text(c, subtitleFor(mode), getWidth() / 2f, side() * .122f, side() * .029f, MUTED, false, Paint.Align.CENTER);
    }

    private String titleFor(int value) {
        switch (value) {
            case TARGET_TAP: return "移动靶心";
            case SWIPE_ARROW: return "方向滑动";
            case STOP_BAR: return "中心急停";
            case PAIR_SUM: return "两数凑和";
            case SEQUENCE_NEXT: return "数列下一项";
            case SHAPE_MATCH: return "形色判断";
            case QUICK_COUNT: return "闪速数点";
            default: return "安全区急停";
        }
    }

    private String subtitleFor(int value) {
        switch (value) {
            case TARGET_TAP: return "20 秒 · 点中随机靶心";
            case SWIPE_ARROW: return "看箭头 · 向对应方向滑";
            case STOP_BAR: return "移动线靠近中心时点击";
            case PAIR_SUM: return "选两个数 · 相加等于目标";
            case SEQUENCE_NEXT: return "找规律 · 选择下一项";
            case SHAPE_MATCH: return "判断颜色和形状是否完全相同";
            case QUICK_COUNT: return "快速数清屏幕里的点";
            default: return "移动线进入绿色区域时点击";
        }
    }

    private void drawTargetTap(Canvas c) {
        RectF b = board();
        panel(c, b);
        long now = SystemClock.elapsedRealtime();
        if (!targetOver && now >= targetDeadline) {
            targetOver = true;
            finish(1, "时间到", "命中 " + targetHits + " · 空点 " + targetMisses, targetHits);
        }
        p.setStyle(Paint.Style.FILL);
        p.setColor(PRIMARY);
        c.drawCircle(targetX, targetY, targetR, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(dp(3));
        p.setColor(Color.WHITE);
        c.drawCircle(targetX, targetY, targetR * .55f, p);
        p.setStyle(Paint.Style.FILL);
        text(c, "命中 " + targetHits + "   空点 " + targetMisses, b.centerX(), b.top + dp(18), side() * .033f, TEXT, true, Paint.Align.CENTER);
        if (!targetOver) invalidateLater(100);
    }

    private void drawSwipeArrow(Canvas c) {
        RectF b = board();
        panel(c, b);
        long elapsed = SystemClock.elapsedRealtime() - swipePulseAt;
        float scale = elapsed < 170 ? 1f + .18f * (1f - elapsed / 170f) : 1f;
        String arrow = swipeDirection == 0 ? "↑" : swipeDirection == 1 ? "→" : swipeDirection == 2 ? "↓" : "←";
        text(c, arrow, b.centerX(), b.centerY() + side() * .08f, side() * .22f * scale, PRIMARY, true, Paint.Align.CENTER);
        text(c, "得分 " + swipeScore + "  ·  " + swipeRound + "/12", b.centerX(), b.top + dp(19), side() * .033f, TEXT, true, Paint.Align.CENTER);
        if (elapsed < 170 && !swipeOver) nextFrame();
    }

    private void drawStopBar(Canvas c) {
        RectF b = board();
        panel(c, b);
        long now = SystemClock.elapsedRealtime();
        if (!stopOver) {
            float dt = Math.min(.035f, (now - stopLastTick) / 1000f);
            stopLastTick = now;
            stopPosition += stopVelocity * dt;
            if (stopPosition <= 0f) { stopPosition = 0f; stopVelocity = Math.abs(stopVelocity); }
            if (stopPosition >= 1f) { stopPosition = 1f; stopVelocity = -Math.abs(stopVelocity); }
            nextFrame();
        }
        float left = b.left + b.width() * .10f;
        float right = b.right - b.width() * .10f;
        float y = b.centerY();
        p.setColor(SURFACE_2);
        scratchRect.set(left, y - dp(8), right, y + dp(8)); c.drawRoundRect(scratchRect, dp(8), dp(8), p);
        float centerX = (left + right) / 2f;
        p.setColor(GOOD);
        scratchRect.set(centerX - b.width() * .07f, y - dp(12), centerX + b.width() * .07f, y + dp(12)); c.drawRoundRect(scratchRect, dp(9), dp(9), p);
        float x = left + (right - left) * stopPosition;
        p.setColor(PRIMARY);
        scratchRect.set(x - dp(3), y - dp(28), x + dp(3), y + dp(28)); c.drawRoundRect(scratchRect, dp(3), dp(3), p);
        text(c, "第 " + Math.min(10, stopRound + 1) + "/10 次   " + stopScore + " 分", b.centerX(), b.top + dp(19), side() * .032f, TEXT, true, Paint.Align.CENTER);
    }

    private void drawPairSum(Canvas c) {
        RectF b = board();
        panel(c, b);
        text(c, "目标  " + pairTarget, b.centerX(), b.top + b.height() * .20f, side() * .083f, PRIMARY, true, Paint.Align.CENTER);
        float gap = dp(6);
        float cell = (b.width() - gap * 4) / 3f;
        float top = b.top + b.height() * .33f;
        for (int i = 0; i < 6; i++) {
            int row = i / 3, col = i % 3;
            scratchRect.set(b.left + gap + col * (cell + gap), top + row * (cell * .72f + gap),
                    b.left + gap + col * (cell + gap) + cell, top + row * (cell * .72f + gap) + cell * .72f);
            RectF r = scratchRect;
            button(c, r, String.valueOf(pairValues[i]), i == pairFirst ? SECONDARY : SURFACE_2, false);
        }
        text(c, "答对 " + pairScore + "  ·  " + pairRound + "/10", b.centerX(), b.bottom - dp(13), side() * .030f, MUTED, false, Paint.Align.CENTER);
    }

    private void drawSequenceNext(Canvas c) {
        RectF b = board();
        panel(c, b);
        String sequence = sequenceStart + "  ·  " + (sequenceStart + sequenceStep) + "  ·  " +
                (sequenceStart + sequenceStep * 2) + "  ·  " + (sequenceStart + sequenceStep * 3) + "  ·  ?";
        text(c, sequence, b.centerX(), b.top + b.height() * .25f, side() * .045f, TEXT, true, Paint.Align.CENTER);
        float w = b.width() * .37f, h = b.height() * .17f;
        for (int i = 0; i < 4; i++) {
            int row = i / 2, col = i % 2;
            float left = b.left + b.width() * .09f + col * (w + b.width() * .08f);
            float top = b.top + b.height() * .45f + row * (h + dp(7));
            scratchRect.set(left, top, left + w, top + h); button(c, scratchRect, String.valueOf(sequenceOptions[i]), SURFACE_2, false);
        }
        text(c, "答对 " + sequenceScore + "  ·  " + sequenceRound + "/10", b.centerX(), b.bottom - dp(10), side() * .030f, MUTED, false, Paint.Align.CENTER);
    }

    private void drawShapeMatch(Canvas c) {
        RectF b = board();
        panel(c, b);
        float cy = b.top + b.height() * .37f;
        drawShape(c, b.centerX() - b.width() * .19f, cy, b.width() * .10f, shapeA, shapeColorA);
        drawShape(c, b.centerX() + b.width() * .19f, cy, b.width() * .10f, shapeB, shapeColorB);
        float w = b.width() * .35f, h = b.height() * .17f;
        scratchRect.set(b.left + b.width() * .10f, b.top + b.height() * .64f, b.left + b.width() * .10f + w, b.top + b.height() * .64f + h);
        RectF yes = scratchRect;
        scratchRect2.set(b.right - b.width() * .10f - w, yes.top, b.right - b.width() * .10f, yes.bottom);
        RectF no = scratchRect2;
        button(c, yes, "相同", GOOD, false);
        button(c, no, "不同", SECONDARY, false);
        text(c, "答对 " + shapeScore + "  ·  " + shapeRound + "/15", b.centerX(), b.top + dp(19), side() * .032f, TEXT, true, Paint.Align.CENTER);
    }

    private void drawQuickCount(Canvas c) {
        RectF b = board();
        panel(c, b);
        float dotAreaTop = b.top + dp(27);
        float dotAreaHeight = b.height() * .48f;
        p.setColor(PRIMARY);
        for (int i = 0; i < countDots; i++) {
            c.drawCircle(b.left + b.width() * countXs[i], dotAreaTop + dotAreaHeight * countYs[i], dp(5.5f), p);
        }
        float w = b.width() * .18f, gap = b.width() * .035f;
        float total = w * 4 + gap * 3;
        float left = b.centerX() - total / 2f;
        float top = b.top + b.height() * .69f;
        for (int i = 0; i < 4; i++) {
            scratchRect.set(left + i * (w + gap), top, left + i * (w + gap) + w, top + b.height() * .16f);
            button(c, scratchRect, String.valueOf(countOptions[i]), SURFACE_2, false);
        }
        text(c, "第 " + Math.min(12, countRound + 1) + "/12   " + countScore + " 分", b.centerX(), b.top + dp(18), side() * .031f, TEXT, true, Paint.Align.CENTER);
    }

    private void drawSafeZone(Canvas c) {
        RectF b = board();
        panel(c, b);
        long now = SystemClock.elapsedRealtime();
        if (!safeOver) {
            float dt = Math.min(.035f, (now - safeLastTick) / 1000f);
            safeLastTick = now;
            safePosition += safeVelocity * dt;
            if (safePosition <= 0f) { safePosition = 0f; safeVelocity = Math.abs(safeVelocity); }
            if (safePosition >= 1f) { safePosition = 1f; safeVelocity = -Math.abs(safeVelocity); }
            nextFrame();
        }
        float left = b.left + b.width() * .08f, right = b.right - b.width() * .08f, y = b.centerY();
        p.setColor(SURFACE_2);
        scratchRect.set(left, y - dp(9), right, y + dp(9)); c.drawRoundRect(scratchRect, dp(9), dp(9), p);
        float zoneLeft = left + (right - left) * Math.max(0f, safeCenter - safeWidth / 2f);
        float zoneRight = left + (right - left) * Math.min(1f, safeCenter + safeWidth / 2f);
        p.setColor(GOOD);
        scratchRect.set(zoneLeft, y - dp(13), zoneRight, y + dp(13)); c.drawRoundRect(scratchRect, dp(9), dp(9), p);
        float x = left + (right - left) * safePosition;
        p.setColor(PRIMARY);
        c.drawCircle(x, y, dp(9), p);
        text(c, "第 " + Math.min(10, safeRound + 1) + "/10   " + safeScore + " 分", b.centerX(), b.top + dp(19), side() * .032f, TEXT, true, Paint.Align.CENTER);
    }

    private void drawShape(Canvas c, float cx, float cy, float r, int shape, int color) {
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        if (shape == 0) {
            c.drawCircle(cx, cy, r, p);
        } else if (shape == 1) {
            scratchRect.set(cx - r, cy - r, cx + r, cy + r); c.drawRoundRect(scratchRect, dp(7), dp(7), p);
        } else {
            path.reset();
            path.moveTo(cx, cy - r);
            path.lineTo(cx - r * .95f, cy + r * .85f);
            path.lineTo(cx + r * .95f, cy + r * .85f);
            path.close();
            c.drawPath(path, p);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        if (resultVisible && event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (SystemClock.elapsedRealtime() - resultShownAt > 220) resetCurrent();
            return true;
        }

        int control = bottomControlAt(x, y);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            downX = x;
            downY = y;
            downInGame = control < 0 && y >= contentTop() && y < contentBottom();
            pressedControl = control;
            invalidate();
            return true;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            pressedControl = -1;
            downInGame = false;
            invalidate();
            return true;
        }
        if (event.getActionMasked() != MotionEvent.ACTION_UP) return true;

        if (control >= 0 && control == pressedControl) {
            pressedControl = -1;
            handleBottomControl(control);
            return true;
        }
        pressedControl = -1;
        if (!downInGame || y >= contentBottom()) return true;

        switch (mode) {
            case TARGET_TAP: handleTargetTap(x, y); break;
            case SWIPE_ARROW: handleSwipe(x - downX, y - downY); break;
            case STOP_BAR: handleStopBar(); break;
            case PAIR_SUM: handlePairTap(x, y); break;
            case SEQUENCE_NEXT: handleSequenceTap(x, y); break;
            case SHAPE_MATCH: handleShapeTap(x, y); break;
            case QUICK_COUNT: handleCountTap(x, y); break;
            case SAFE_ZONE: handleSafeTap(); break;
        }
        downInGame = false;
        return true;
    }

    private void handleTargetTap(float x, float y) {
        if (targetOver) return;
        float dx = x - targetX, dy = y - targetY;
        if (dx * dx + dy * dy <= targetR * targetR * 1.22f) {
            targetHits++;
            haptic(HapticFeedbackConstants.CLOCK_TICK);
            newTarget();
        } else {
            targetMisses++;
            haptic(HapticFeedbackConstants.REJECT);
        }
        invalidate();
    }

    private void handleSwipe(float dx, float dy) {
        if (swipeOver) return;
        float threshold = dp(26);
        if (Math.max(Math.abs(dx), Math.abs(dy)) < threshold) return;
        int answer;
        if (Math.abs(dx) > Math.abs(dy)) answer = dx > 0 ? 1 : 3;
        else answer = dy > 0 ? 2 : 0;
        swipeRound++;
        if (answer == swipeDirection) {
            swipeScore++;
            haptic(HapticFeedbackConstants.CONFIRM);
        } else haptic(HapticFeedbackConstants.REJECT);
        if (swipeRound >= 12) {
            swipeOver = true;
            finish(1, "方向完成", swipeScore + " / 12", swipeScore);
        } else {
            int next;
            do { next = random.nextInt(4); } while (next == swipeDirection);
            swipeDirection = next;
            swipePulseAt = SystemClock.elapsedRealtime();
            nextFrame();
        }
        invalidate();
    }

    private void handleStopBar() {
        if (stopOver) return;
        float error = Math.abs(stopPosition - .5f);
        int add = error <= .035f ? 100 : error <= .075f ? 70 : error <= .13f ? 35 : 0;
        stopScore += add;
        stopRound++;
        haptic(add >= 70 ? HapticFeedbackConstants.CONFIRM : add > 0 ? HapticFeedbackConstants.CLOCK_TICK : HapticFeedbackConstants.REJECT);
        if (stopRound >= 10) {
            stopOver = true;
            finish(1, "急停完成", "得分 " + stopScore, stopScore);
        } else {
            stopPosition = random.nextBoolean() ? 0f : 1f;
            stopVelocity = (stopPosition == 0f ? 1f : -1f) * (.64f + stopRound * .045f);
            stopLastTick = SystemClock.elapsedRealtime();
        }
        invalidate();
    }

    private void handlePairTap(float x, float y) {
        if (pairOver) return;
        RectF b = board();
        float gap = dp(6), cell = (b.width() - gap * 4) / 3f, top = b.top + b.height() * .33f;
        for (int i = 0; i < 6; i++) {
            int row = i / 3, col = i % 3;
            scratchRect.set(b.left + gap + col * (cell + gap), top + row * (cell * .72f + gap),
                    b.left + gap + col * (cell + gap) + cell, top + row * (cell * .72f + gap) + cell * .72f);
            RectF r = scratchRect;
            if (!r.contains(x, y)) continue;
            if (pairFirst < 0) {
                pairFirst = i;
                haptic(HapticFeedbackConstants.CLOCK_TICK);
            } else if (i != pairFirst) {
                pairRound++;
                if (pairValues[pairFirst] + pairValues[i] == pairTarget) {
                    pairScore++;
                    haptic(HapticFeedbackConstants.CONFIRM);
                } else haptic(HapticFeedbackConstants.REJECT);
                pairFirst = -1;
                if (pairRound >= 10) {
                    pairOver = true;
                    finish(1, "凑和完成", "答对 " + pairScore + " / 10", pairScore);
                } else newPairRound();
            }
            invalidate();
            return;
        }
    }

    private void handleSequenceTap(float x, float y) {
        if (sequenceOver) return;
        RectF b = board();
        float w = b.width() * .37f, h = b.height() * .17f;
        for (int i = 0; i < 4; i++) {
            int row = i / 2, col = i % 2;
            float left = b.left + b.width() * .09f + col * (w + b.width() * .08f);
            float top = b.top + b.height() * .45f + row * (h + dp(7));
            scratchRect.set(left, top, left + w, top + h); if (!scratchRect.contains(x, y)) continue;
            sequenceRound++;
            if (sequenceOptions[i] == sequenceCorrect) {
                sequenceScore++;
                haptic(HapticFeedbackConstants.CONFIRM);
            } else haptic(HapticFeedbackConstants.REJECT);
            if (sequenceRound >= 10) {
                sequenceOver = true;
                finish(1, "数列完成", "答对 " + sequenceScore + " / 10", sequenceScore);
            } else newSequenceRound();
            invalidate();
            return;
        }
    }

    private void handleShapeTap(float x, float y) {
        if (shapeOver) return;
        RectF b = board();
        float w = b.width() * .35f, h = b.height() * .17f;
        scratchRect.set(b.left + b.width() * .10f, b.top + b.height() * .64f, b.left + b.width() * .10f + w, b.top + b.height() * .64f + h);
        RectF yes = scratchRect;
        scratchRect2.set(b.right - b.width() * .10f - w, yes.top, b.right - b.width() * .10f, yes.bottom);
        RectF no = scratchRect2;
        boolean answer;
        if (yes.contains(x, y)) answer = true;
        else if (no.contains(x, y)) answer = false;
        else return;
        shapeRound++;
        if (answer == shapeShouldMatch) {
            shapeScore++;
            haptic(HapticFeedbackConstants.CONFIRM);
        } else haptic(HapticFeedbackConstants.REJECT);
        if (shapeRound >= 15) {
            shapeOver = true;
            finish(1, "判断完成", "答对 " + shapeScore + " / 15", shapeScore);
        } else newShapeRound();
        invalidate();
    }

    private void handleCountTap(float x, float y) {
        if (countOver) return;
        RectF b = board();
        float w = b.width() * .18f, gap = b.width() * .035f;
        float total = w * 4 + gap * 3, left = b.centerX() - total / 2f, top = b.top + b.height() * .69f;
        for (int i = 0; i < 4; i++) {
            scratchRect.set(left + i * (w + gap), top, left + i * (w + gap) + w, top + b.height() * .16f); RectF r = scratchRect;
            if (!r.contains(x, y)) continue;
            countRound++;
            if (i == countCorrect) {
                countScore++;
                haptic(HapticFeedbackConstants.CONFIRM);
            } else haptic(HapticFeedbackConstants.REJECT);
            if (countRound >= 12) {
                countOver = true;
                finish(1, "数点完成", "答对 " + countScore + " / 12", countScore);
            } else newCountRound();
            invalidate();
            return;
        }
    }

    private void handleSafeTap() {
        if (safeOver) return;
        float error = Math.abs(safePosition - safeCenter);
        float half = safeWidth / 2f;
        int add = error <= half * .35f ? 100 : error <= half ? 60 : 0;
        safeScore += add;
        safeRound++;
        haptic(add >= 100 ? HapticFeedbackConstants.CONFIRM : add > 0 ? HapticFeedbackConstants.CLOCK_TICK : HapticFeedbackConstants.REJECT);
        if (safeRound >= 10) {
            safeOver = true;
            finish(1, "安全区完成", "得分 " + safeScore, safeScore);
        } else {
            newSafeRound();
        }
        invalidate();
    }

    private void resetCurrent() {
        resultVisible = false;
        roundRecorded = false;
        pendingControl = -1;
        pressedControl = -1;
        long now = SystemClock.elapsedRealtime();
        switch (mode) {
            case TARGET_TAP:
                targetHits = targetMisses = 0;
                targetDeadline = now + 20_000L;
                targetOver = false;
                newTarget();
                break;
            case SWIPE_ARROW:
                swipeScore = swipeRound = 0;
                swipeOver = false;
                swipeDirection = random.nextInt(4);
                swipePulseAt = now;
                break;
            case STOP_BAR:
                stopPosition = 0f;
                stopVelocity = .64f;
                stopRound = stopScore = 0;
                stopLastTick = now;
                stopOver = false;
                break;
            case PAIR_SUM:
                pairRound = pairScore = 0;
                pairFirst = -1;
                pairOver = false;
                newPairRound();
                break;
            case SEQUENCE_NEXT:
                sequenceRound = sequenceScore = 0;
                sequenceOver = false;
                newSequenceRound();
                break;
            case SHAPE_MATCH:
                shapeRound = shapeScore = 0;
                shapeOver = false;
                newShapeRound();
                break;
            case QUICK_COUNT:
                countRound = countScore = 0;
                countOver = false;
                newCountRound();
                break;
            case SAFE_ZONE:
                safeRound = safeScore = 0;
                safePosition = 0f;
                safeVelocity = .58f;
                safeLastTick = now;
                safeOver = false;
                newSafeRound();
                break;
        }
        invalidate();
    }

    private void newTarget() {
        RectF b = board();
        targetR = Math.max(dp(13), b.width() * (.07f - Math.min(.018f, targetHits * .0012f)));
        float margin = targetR + dp(8);
        targetX = b.left + margin + random.nextFloat() * Math.max(1f, b.width() - margin * 2f);
        targetY = b.top + dp(28) + margin + random.nextFloat() * Math.max(1f, b.height() - dp(38) - margin * 2f);
    }

    private void newPairRound() {
        pairFirst = -1;
        for (int i = 0; i < pairValues.length; i++) pairValues[i] = 1 + random.nextInt(12);
        int a = random.nextInt(pairValues.length), b;
        do { b = random.nextInt(pairValues.length); } while (b == a);
        pairTarget = pairValues[a] + pairValues[b];
    }

    private void newSequenceRound() {
        sequenceStart = 1 + random.nextInt(12);
        sequenceStep = 1 + random.nextInt(7);
        sequenceCorrect = sequenceStart + sequenceStep * 4;
        sequenceOptions[0] = sequenceCorrect;
        for (int i = 1; i < sequenceOptions.length; i++) {
            int value;
            boolean duplicate;
            do {
                int delta = 1 + random.nextInt(sequenceStep + 4);
                value = Math.max(1, sequenceCorrect + (random.nextBoolean() ? delta : -delta));
                duplicate = false;
                for (int j = 0; j < i; j++) if (sequenceOptions[j] == value) duplicate = true;
            } while (duplicate);
            sequenceOptions[i] = value;
        }
        shuffle(sequenceOptions);
    }

    private void newShapeRound() {
        int[] colors = {PRIMARY, SECONDARY, GOOD, Color.rgb(255, 204, 91)};
        shapeA = random.nextInt(3);
        shapeColorA = colors[random.nextInt(colors.length)];
        shapeShouldMatch = random.nextBoolean();
        if (shapeShouldMatch) {
            shapeB = shapeA;
            shapeColorB = shapeColorA;
        } else {
            shapeB = random.nextInt(3);
            shapeColorB = colors[random.nextInt(colors.length)];
            if (shapeB == shapeA && shapeColorB == shapeColorA) shapeB = (shapeA + 1) % 3;
        }
    }

    private void newCountRound() {
        countDots = 1 + random.nextInt(9);
        for (int i = 0; i < countDots; i++) {
            float x = .5f, y = .5f;
            for (int attempt = 0; attempt < 80; attempt++) {
                x = .12f + random.nextFloat() * .76f;
                y = .12f + random.nextFloat() * .76f;
                boolean clear = true;
                for (int j = 0; j < i; j++) {
                    float dx = x - countXs[j], dy = y - countYs[j];
                    if (dx * dx + dy * dy < .018f) { clear = false; break; }
                }
                if (clear) break;
            }
            countXs[i] = x;
            countYs[i] = y;
        }
        int[] candidates = {countDots, Math.max(1, countDots - 1), Math.min(9, countDots + 1), 1 + random.nextInt(9)};
        // Make all answers unique while keeping the correct value.
        boolean[] used = new boolean[10];
        used[countDots] = true;
        for (int i = 1; i < candidates.length; i++) {
            int value = candidates[i];
            while (value == countDots || used[value]) value = 1 + random.nextInt(9);
            candidates[i] = value;
            used[value] = true;
        }
        System.arraycopy(candidates, 0, countOptions, 0, 4);
        shuffle(countOptions);
        for (int i = 0; i < 4; i++) if (countOptions[i] == countDots) countCorrect = i;
    }

    private void newSafeRound() {
        safeWidth = Math.max(.13f, .24f - safeRound * .009f);
        safeCenter = safeWidth / 2f + random.nextFloat() * (1f - safeWidth);
        if (safeRound > 0) safeVelocity = Math.copySign(.58f + safeRound * .055f, safeVelocity == 0 ? 1f : safeVelocity);
        safeLastTick = SystemClock.elapsedRealtime();
    }

    private void shuffle(int[] values) {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = values[i]; values[i] = values[j]; values[j] = tmp;
        }
    }

    private void finish(int kind, String title, String sub, int metric) {
        resultKind = kind;
        resultTitle = title;
        resultSub = sub;
        resultVisible = true;
        resultShownAt = SystemClock.elapsedRealtime();
        if (!roundRecorded) {
            roundRecorded = true;
            GameStats.recordResult(prefs, mode, kind, metric, false);
        }
        haptic(kind > 0 ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.REJECT);
        SoundManager.play(prefs, kind > 0 ? SoundManager.CLEAR : SoundManager.GAME_OVER);
        invalidate();
    }

    private boolean haptic(int constant) { return HapticsManager.perform(this, prefs, constant); }

    private void handleBottomControl(int which) {
        long now = SystemClock.elapsedRealtime();
        if (pendingControl == which && now <= pendingControlUntil) {
            pendingControl = -1;
            if (which == 0) resetCurrent();
            else if (hostListener != null) hostListener.onExitToHub();
            return;
        }
        pendingControl = which;
        pendingControlUntil = now + 1400L;
        haptic(HapticFeedbackConstants.CLOCK_TICK);
        invalidate();
    }

    private void updateBottomRects() {
        boolean leftHanded = AppSettings.leftHanded(prefs);
        if (controlWidth == getWidth() && controlHeight == getHeight() && controlLeftHanded == leftHanded) return;
        controlWidth = getWidth(); controlHeight = getHeight(); controlLeftHanded = leftHanded;
        float h = dp(42), bottom = getHeight() - dp(3), gap = dp(7);
        float w = Math.min(side() * .34f, (getWidth() - gap * 3) / 2f), cx = getWidth() / 2f;
        RectF physicalLeft = leftHanded ? menuRect : restartRect;
        RectF physicalRight = leftHanded ? restartRect : menuRect;
        physicalLeft.set(cx - gap / 2f - w, bottom - h, cx - gap / 2f, bottom);
        physicalRight.set(cx + gap / 2f, bottom - h, cx + gap / 2f + w, bottom);
    }

    private int bottomControlAt(float x, float y) {
        updateBottomRects();
        if (restartRect.contains(x, y)) return 0;
        if (menuRect.contains(x, y)) return 1;
        return -1;
    }

    private void drawBottomControls(Canvas c) {
        updateBottomRects();
        long now = SystemClock.elapsedRealtime();
        if (pendingControl >= 0 && now > pendingControlUntil) pendingControl = -1;
        button(c, restartRect, pendingControl == 0 ? "确认重开" : "重开", pendingControl == 0 ? BAD : SURFACE_2, pressedControl == 0);
        button(c, menuRect, pendingControl == 1 ? "确认返回" : "菜单", pendingControl == 1 ? BAD : SURFACE_2, pressedControl == 1);
        if (pendingControl >= 0 && now <= pendingControlUntil) invalidateLater(180);
    }

    private void drawResult(Canvas c) {
        if (!resultVisible) return;
        float t = AppSettings.animations(prefs) ? Math.max(0f, Math.min(1f, (SystemClock.elapsedRealtime() - resultShownAt) / 250f)) : 1f;
        float u = 1f - t, eased = 1f + 2.55f * u * u * u + 1.55f * u * u, alpha = 1f - u * u * u;
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb((int)(210 * alpha), 0, 0, 0));
        c.drawRect(0, 0, getWidth(), getHeight(), p);
        float w = side() * (roundScreen ? .75f : .83f), h = side() * .36f, scale = .88f + .12f * eased;
        float sw = w * scale, sh = h * scale;
        resultRect.set((getWidth() - sw) / 2f, (getHeight() - sh) / 2f, (getWidth() + sw) / 2f, (getHeight() + sh) / 2f); RectF r = resultRect;
        p.setColor(SURFACE);
        c.drawRoundRect(r, dp(22), dp(22), p);
        text(c, resultTitle, r.centerX(), r.top + sh * .34f, side() * .058f, resultKind > 0 ? GOOD : BAD, true, Paint.Align.CENTER);
        text(c, resultSub, r.centerX(), r.top + sh * .57f, side() * .033f, TEXT, false, Paint.Align.CENTER);
        text(c, "轻点继续", r.centerX(), r.bottom - sh * .14f, side() * .029f, MUTED, false, Paint.Align.CENTER);
        if (t < 1f) nextFrame();
    }

    private void panel(Canvas c, RectF r) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(SURFACE);
        c.drawRoundRect(r, dp(22), dp(22), p);
        if (AppSettings.richEffects(prefs)) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(38, 255, 255, 255));
            c.drawRoundRect(r, dp(22), dp(22), p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    private void button(Canvas c, RectF r, String label, int color, boolean pressed) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        float scale = pressed && AppSettings.animations(prefs) ? .94f : 1f;
        float dx = r.width() * (1f - scale) / 2f, dy = r.height() * (1f - scale) / 2f;
        RectF rr = r; if (pressed) { pressedRect.set(r.left + dx, r.top + dy, r.right - dx, r.bottom - dy); rr = pressedRect; }
        p.setAlpha(pressed ? 190 : 255);
        c.drawRoundRect(rr, Math.min(rr.height() / 2f, dp(18)), Math.min(rr.height() / 2f, dp(18)), p);
        p.setAlpha(255);
        text(c, label, rr.centerX(), rr.centerY() + side() * .013f, Math.min(side() * .034f, rr.height() * .38f), TEXT, true, Paint.Align.CENTER);
    }

    private void text(Canvas c, String value, float x, float y, float size, int color, boolean bold, Paint.Align align) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        p.setTextSize(size);
        p.setTextAlign(align);
        p.setTypeface(bold ? BOLD : NORMAL);
        c.drawText(value, x, y, p);
    }
}
