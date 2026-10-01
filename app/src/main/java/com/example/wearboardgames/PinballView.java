package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

/** Compact two-flipper pinball table. Physics and animation are isolated from the legacy hub. */
public final class PinballView extends BaseGameView {
    public static final int MODE = 114;
    private float x, y, vx, vy;
    private int score, balls;
    private long lastTick, leftFlipUntil, rightFlipUntil;
    private boolean over;
    private final float[] bumperX = {.28f, .70f, .50f};
    private final float[] bumperY = {.34f, .34f, .55f};

    public PinballView(Context context) { super(context); }
    public static boolean supportsMode(int mode) { return mode == MODE; }
    @Override protected boolean supportsGameMode(int candidate) { return supportsMode(candidate); }
    @Override protected int gameMode() { return MODE; }

    @Override protected void resetGame() {
        score = 0; balls = 3; over = false;
        leftFlipUntil = rightFlipUntil = 0;
        launchBall();
    }

    private void launchBall() {
        x = .72f; y = .18f; vx = -.18f; vy = .08f; lastTick = now();
    }

    @Override protected void drawGame(Canvas canvas) {
        RectF r = gamePanel(roundScreen ? .11f : .07f);
        update();
        drawHeader(canvas, "迷你弹珠台", "得分 " + score + " · 剩余 " + balls + " 球");
        panel(canvas, r);

        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(3)); p.setColor(MUTED);
        canvas.drawRoundRect(new RectF(r.left + dp(5), r.top + dp(5), r.right - dp(5), r.bottom - dp(5)), dp(18), dp(18), p);
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < bumperX.length; i++) {
            float bx = r.left + bumperX[i] * r.width(), by = r.top + bumperY[i] * r.height();
            p.setColor(i == 2 ? SECONDARY : PRIMARY); canvas.drawCircle(bx, by, s() * .042f, p);
            p.setColor(SURFACE); canvas.drawCircle(bx, by, s() * .020f, p);
        }

        long t = now();
        float fy = r.top + r.height() * .84f;
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(s() * .030f);
        p.setColor(t < leftFlipUntil ? GOOD : TEXT);
        canvas.drawLine(r.left + r.width() * .25f, fy, r.left + r.width() * .46f, fy - (t < leftFlipUntil ? r.height() * .09f : 0), p);
        p.setColor(t < rightFlipUntil ? GOOD : TEXT);
        canvas.drawLine(r.right - r.width() * .25f, fy, r.right - r.width() * .46f, fy - (t < rightFlipUntil ? r.height() * .09f : 0), p);
        p.setStyle(Paint.Style.FILL);

        p.setColor(TEXT); canvas.drawCircle(r.left + x * r.width(), r.top + y * r.height(), s() * .022f, p);
        text(canvas, "左半屏 / 右半屏控制挡板", r.centerX(), r.bottom - r.height() * .025f, s() * .020f, MUTED, false, Paint.Align.CENTER);
        if (!over) animateNext();
    }

    private void update() {
        if (over) return;
        long t = now();
        float dt = Math.min(.035f, Math.max(0f, (t - lastTick) / 1000f));
        lastTick = t;
        if (dt <= 0) return;
        vy += .54f * dt; x += vx * dt; y += vy * dt;
        if (x < .04f) { x = .04f; vx = Math.abs(vx) * .92f; }
        if (x > .96f) { x = .96f; vx = -Math.abs(vx) * .92f; }
        if (y < .04f) { y = .04f; vy = Math.abs(vy) * .88f; }

        for (int i = 0; i < bumperX.length; i++) {
            float dx = x - bumperX[i], dy = y - bumperY[i];
            float d2 = dx * dx + dy * dy, rr = .075f * .075f;
            if (d2 < rr && d2 > .0001f) {
                float d = (float)Math.sqrt(d2), nx = dx / d, ny = dy / d;
                float dot = vx * nx + vy * ny;
                if (dot < .15f) { vx -= 2f * dot * nx; vy -= 2f * dot * ny; }
                vx += nx * .12f; vy += ny * .12f; x = bumperX[i] + nx * .078f; y = bumperY[i] + ny * .078f;
                score += i == 2 ? 25 : 10; haptic(HapticFeedbackConstants.CLOCK_TICK);
            }
        }

        boolean left = x < .52f && y > .72f && y < .94f;
        boolean right = x >= .48f && y > .72f && y < .94f;
        if (vy > 0 && ((left && t < leftFlipUntil) || (right && t < rightFlipUntil))) {
            vy = -.88f; vx += left ? .18f : -.18f; score += 5;
            haptic(HapticFeedbackConstants.CONFIRM);
        }

        if (y > 1.04f) {
            balls--;
            if (balls <= 0) { over = true; finishRound(score >= 150 ? 1 : 0, "弹珠结束", "总分 " + score, score); }
            else launchBall();
        }
        vx = clamp(vx, -.78f, .78f); vy = clamp(vy, -1.05f, 1.05f);
    }

    @Override protected void onGameTouchDown(float tx, float ty) { flip(tx); }
    @Override protected void onGameTap(float tx, float ty) { flip(tx); }
    private void flip(float tx) {
        long until = now() + 170;
        if (tx < getWidth() / 2f) leftFlipUntil = until; else rightFlipUntil = until;
        haptic(HapticFeedbackConstants.CLOCK_TICK); invalidate();
    }
}
