package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

/** Standalone watch-sized Pong with AI or same-watch two player mode. */
public final class PongView extends BaseGameView {
    private static final int MODE = GameModes.PONG;
    private float ballX, ballY, vx, vy, bottomX, topX;
    private int bottomScore, topScore;
    private long lastTick;
    private boolean running, over, initialized;

    public PongView(Context context) { super(context); }
    public static boolean supportsMode(int mode) { return mode == MODE; }
    @Override protected int gameMode() { return MODE; }
    private RectF board() { return gamePanel(roundScreen ? .095f : .065f); }

    @Override protected void resetGame() {
        bottomScore = topScore = 0; running = false; over = false; initialized = false; lastTick = now();
    }

    private void init(RectF b, boolean towardBottom) {
        bottomX = topX = b.centerX(); ballX = b.centerX(); ballY = b.centerY();
        float speed = s() * .52f;
        vx = (Math.random() < .5 ? -1 : 1) * speed * .46f;
        vy = (towardBottom ? 1 : -1) * speed;
        lastTick = now(); initialized = true;
    }

    @Override protected void drawGame(Canvas c) {
        RectF b = board(); if (!initialized && b.width() > 0) init(b, true); update(b);
        drawHeader(c, "腕上乒乓", bottomScore + " : " + topScore + (isSinglePlayer() ? " · AI" : " · 双人"));
        panel(c, b);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1)); p.setColor(Color.rgb(67,74,86));
        c.drawLine(b.left, b.centerY(), b.right, b.centerY(), p); p.setStyle(Paint.Style.FILL);
        float paddleW=b.width()*.30f, paddleH=Math.max(dp(6),s()*.018f), r=s()*.020f;
        p.setColor(PRIMARY); c.drawRoundRect(new RectF(bottomX-paddleW/2,b.bottom-paddleH*2,bottomX+paddleW/2,b.bottom-paddleH),paddleH,paddleH,p);
        p.setColor(SECONDARY); c.drawRoundRect(new RectF(topX-paddleW/2,b.top+paddleH,topX+paddleW/2,b.top+paddleH*2),paddleH,paddleH,p);
        p.setColor(TEXT); c.drawCircle(ballX,ballY,r,p);
        if (!running && !over) text(c,"轻点或拖动开始",b.centerX(),b.centerY()+s()*.010f,s()*.027f,MUTED,false,Paint.Align.CENTER);
        if (running) animateNext();
    }

    private void update(RectF b) {
        if (!running || over || !initialized) return;
        long t=now(); float dt=Math.min(.032f,(t-lastTick)/1000f); lastTick=t;
        float paddleW=b.width()*.30f, paddleH=Math.max(dp(6),s()*.018f), r=s()*.020f;
        if (isSinglePlayer()) topX += (ballX-topX)*Math.min(1f,dt*4.8f);
        bottomX=clamp(bottomX,b.left+paddleW/2,b.right-paddleW/2); topX=clamp(topX,b.left+paddleW/2,b.right-paddleW/2);
        ballX+=vx*dt; ballY+=vy*dt;
        if (ballX-r<=b.left && vx<0 || ballX+r>=b.right && vx>0) { vx=-vx; ballX=clamp(ballX,b.left+r,b.right-r); haptic(HapticFeedbackConstants.CLOCK_TICK); }
        float bottomY=b.bottom-paddleH*2, topY=b.top+paddleH*2;
        if (vy>0 && ballY+r>=bottomY && ballY-r<=bottomY+paddleH && Math.abs(ballX-bottomX)<=paddleW*.56f) {
            ballY=bottomY-r; vy=-Math.abs(vy)*1.015f; vx+=(ballX-bottomX)*2.1f; haptic(HapticFeedbackConstants.CLOCK_TICK); sound(SoundManager.CLICK);
        } else if (vy<0 && ballY-r<=topY && ballY+r>=topY-paddleH && Math.abs(ballX-topX)<=paddleW*.56f) {
            ballY=topY+r; vy=Math.abs(vy)*1.015f; vx+=(ballX-topX)*2.1f; haptic(HapticFeedbackConstants.CLOCK_TICK);
        }
        if (ballY>b.bottom+r*2) { topScore++; point(b,false); }
        else if (ballY<b.top-r*2) { bottomScore++; point(b,true); }
    }

    private void point(RectF b, boolean playerScored) {
        sound(SoundManager.SCORE); haptic(HapticFeedbackConstants.CONFIRM);
        if (bottomScore>=7 || topScore>=7) { over=true; running=false; finishRound(bottomScore>topScore?1:-1,bottomScore>topScore?"你赢了！":"对手获胜",bottomScore+" : "+topScore,bottomScore); }
        else init(b,!playerScored);
    }

    private void control(float x,float y) {
        RectF b=board(); if (!initialized) init(b,true); running=true; lastTick=now();
        if (!isSinglePlayer() && y<b.centerY()) topX=x; else bottomX=x;
        invalidate();
    }
    @Override protected void onGameTouchDown(float x,float y){control(x,y);} @Override protected void onGameTouchMove(float x,float y){control(x,y);} @Override protected void onGameTap(float x,float y){control(x,y);}
}
