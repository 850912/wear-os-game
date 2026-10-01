package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

/** Three-lane dodge game with event-driven lane changes and VSYNC motion. */
public final class LaneDodgeView extends BaseGameView {
    private static final int MODE=GameModes.DODGER;private int lane,obstacleLane,score;private float obstacleY;private long lastTick;private boolean running,over;
    private final RectF drawRect = new RectF();
    public LaneDodgeView(Context c){super(c);}public static boolean supportsMode(int m){return m==MODE;}@Override protected int gameMode(){return MODE;}private RectF board(){return gamePanel(roundScreen?.15f:.10f);}
    @Override protected void resetGame(){lane=1;obstacleLane=(int)(Math.random()*3);score=0;obstacleY=0;running=false;over=false;lastTick=now();}
    @Override protected void drawGame(Canvas c){RectF b=board();if(obstacleY==0&&b.height()>0)obstacleY=b.top-s()*.06f;update(b);drawHeader(c,"三道闪避",over?"结束 · "+score+" 分":"得分 "+score);panel(c,b);float cw=b.width()/3f;p.setColor(Color.rgb(58,64,76));p.setStrokeWidth(dp(1));for(int i=1;i<3;i++)c.drawLine(b.left+i*cw,b.top,b.left+i*cw,b.bottom,p);float py=b.bottom-b.height()*.13f,sz=s()*.052f,px=b.left+(lane+.5f)*cw;p.setColor(PRIMARY);drawRect.set(px-sz/2,py-sz/2,px+sz/2,py+sz/2);c.drawRoundRect(drawRect,sz*.28f,sz*.28f,p);float ox=b.left+(obstacleLane+.5f)*cw;p.setColor(BAD);drawRect.set(ox-sz/2,obstacleY-sz/2,ox+sz/2,obstacleY+sz/2);c.drawRoundRect(drawRect,sz*.23f,sz*.23f,p);if(!running&&!over)text(c,"点左右半屏或横滑",b.centerX(),b.centerY(),s()*.025f,MUTED,false,Paint.Align.CENTER);if(running)animateNext();}
    private void update(RectF b){if(!running||over||startCountdownActive())return;long t=now();float dt=Math.min(.035f,(t-lastTick)/1000f);lastTick=t;float speed=s()*(.27f+Math.min(.16f,score*.008f));obstacleY+=speed*dt;float py=b.bottom-b.height()*.13f,sz=s()*.052f;if(obstacleY+sz/2>=py-sz/2&&obstacleY-sz/2<=py+sz/2&&obstacleLane==lane){over=true;running=false;finishRound(-1,"撞到了","得分 "+score,score);return;}if(obstacleY-sz/2>b.bottom){score++;showScorePopup("+1");obstacleLane=(int)(Math.random()*3);obstacleY=b.top-sz;haptic(HapticFeedbackConstants.CLOCK_TICK);sound(SoundManager.SCORE);}}
    private void shift(int d){if(over)return;if(!running){running=true;lastTick=now();beginStartCountdown();}lane=Math.max(0,Math.min(2,lane+d));haptic(HapticFeedbackConstants.CLOCK_TICK);invalidate();}@Override protected void onGameTap(float x,float y){shift(x<getWidth()/2f?-1:1);}@Override protected void onGameSwipe(float dx,float dy){if(Math.abs(dx)>Math.abs(dy))shift(dx<0?-1:1);}
}
