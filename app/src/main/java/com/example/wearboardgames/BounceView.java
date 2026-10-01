package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

/** Survival pinball/bounce game extracted from the legacy hub. */
public final class BounceView extends BaseGameView {
    private static final int MODE=GameModes.BOUNCE;
    private float x,y,vx,vy,paddleX;private int hits;private long lastTick;private boolean running,over,initialized;
    public BounceView(Context c){super(c);}public static boolean supportsMode(int m){return m==MODE;}@Override protected int gameMode(){return MODE;}private RectF board(){return gamePanel(roundScreen?.10f:.07f);}
    @Override protected void resetGame(){hits=0;running=false;over=false;initialized=false;lastTick=now();}
    private void init(RectF b){x=b.centerX();y=b.centerY();paddleX=b.centerX();float sp=s()*.44f;vx=sp*.58f;vy=-sp;lastTick=now();initialized=true;}
    @Override protected void drawGame(Canvas c){RectF b=board();if(!initialized&&b.width()>0)init(b);update(b);drawHeader(c,"弹球挑战",over?"结束 · "+hits+" 次":"连续反弹 "+hits+" / 25");panel(c,b);float pw=b.width()*.32f,ph=dp(7);p.setColor(PRIMARY);c.drawRoundRect(new RectF(paddleX-pw/2,b.bottom-ph*2.1f,paddleX+pw/2,b.bottom-ph*1.1f),ph,ph,p);p.setColor(SECONDARY);c.drawCircle(x,y,s()*.024f,p);if(!running&&!over)text(c,"拖动挡板开始",b.centerX(),b.centerY()+s()*.12f,s()*.027f,MUTED,false,Paint.Align.CENTER);if(running)animateNext();}
    private void update(RectF b){if(!running||over)return;long t=now();float dt=Math.min(.03f,(t-lastTick)/1000f);lastTick=t;float r=s()*.024f,pw=b.width()*.32f,ph=dp(7);x+=vx*dt;y+=vy*dt;if(x-r<=b.left&&vx<0||x+r>=b.right&&vx>0){vx=-vx;x=clamp(x,b.left+r,b.right-r);}if(y-r<=b.top&&vy<0){vy=-vy;y=b.top+r;}float py=b.bottom-ph*2.1f;if(vy>0&&y+r>=py&&y-r<=py+ph&&Math.abs(x-paddleX)<=pw*.56f){y=py-r;vy=-Math.abs(vy)*1.018f;vx+=(x-paddleX)*2.0f;hits++;haptic(HapticFeedbackConstants.CLOCK_TICK);if(hits>=25){over=true;running=false;finishRound(1,"弹球达人！","连续反弹 25 次",hits);}}if(y-r>b.bottom){over=true;running=false;finishRound(-1,"没接住","连续 "+hits+" 次",hits);}}
    private void control(float tx){RectF b=board();if(!initialized)init(b);float pw=b.width()*.32f;paddleX=clamp(tx,b.left+pw/2,b.right-pw/2);if(!running){running=true;lastTick=now();}invalidate();}@Override protected void onGameTouchDown(float x,float y){control(x);}@Override protected void onGameTouchMove(float x,float y){control(x);}@Override protected void onGameTap(float x,float y){control(x);}
}
