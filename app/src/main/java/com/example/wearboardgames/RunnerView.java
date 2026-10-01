package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

/** Standalone runner with a larger jump arc and gentler acceleration. */
public final class RunnerView extends BaseGameView {
    private static final int MODE=GameModes.RUNNER;
    private float playerY,velocity,obstacleX;
    private int score;
    private long lastTick;
    private boolean running,over;

    public RunnerView(Context context){super(context);}
    public static boolean supportsMode(int mode){return mode==MODE;}
    @Override protected int gameMode(){return MODE;}
    private RectF board(){return gamePanel(roundScreen ? .09f : .065f);}
    @Override protected void resetGame(){score=0;velocity=0;running=false;over=false;playerY=0;RectF b=board();obstacleX=b.width()>0?b.right:getWidth();lastTick=now();}

    @Override protected void drawGame(Canvas c){RectF b=board();float ground=b.bottom-b.height()*.14f,ph=s()*.075f;if(playerY<=0&&b.height()>0)playerY=ground-ph;update(b);drawHeader(c,"像素跑酷",over?"结束 · "+score+" 分":running?score+" 分 · 轻点跳跃":"轻点起跑");panel(c,b);p.setColor(Color.rgb(76,93,111));c.drawRoundRect(new RectF(b.left,ground,b.right,ground+s()*.010f),s()*.005f,s()*.005f,p);for(int i=0;i<4;i++){float x=b.left+(i*.31f+(now()%1800)/1800f*.31f)%1f*b.width();p.setColor(Color.rgb(48,57,69));c.drawCircle(x,b.top+b.height()*.25f,s()*.007f,p);}float px=b.left+b.width()*.18f,pw=s()*.056f;float squash=Math.min(.10f,Math.abs(velocity)/(s()*6f));p.setColor(Color.rgb(255,210,88));c.drawRoundRect(new RectF(px-pw*squash,playerY+ph*squash,px+pw+pw*squash,playerY+ph-ph*squash),s()*.012f,s()*.012f,p);float obsW=s()*.046f,obsH=s()*.072f;p.setColor(Color.rgb(235,111,91));c.drawRoundRect(new RectF(obstacleX,ground-obsH,obstacleX+obsW,ground),s()*.008f,s()*.008f,p);if(!running&&!over)text(c,"跳幅已加大 · 节奏更宽松",b.centerX(),b.centerY(),s()*.028f,MUTED,false,Paint.Align.CENTER);if(running)animateNext();}
    private void update(RectF b){if(!running||over)return;long t=now();float dt=Math.min(.030f,(t-lastTick)/1000f);lastTick=t;float ground=b.bottom-b.height()*.14f,ph=s()*.075f;velocity+=s()*1.10f*dt;playerY+=velocity*dt;if(playerY>ground-ph){playerY=ground-ph;velocity=0;}float speed=s()*(.205f+Math.min(.07f,score*.004f));obstacleX-=speed*dt;float obsW=s()*.046f,playerX=b.left+b.width()*.18f;if(obstacleX+obsW<b.left){obstacleX=b.right+b.width()*(.12f+(float)Math.random()*.24f);score++;haptic(HapticFeedbackConstants.CLOCK_TICK);}RectF pr=new RectF(playerX+s()*.006f,playerY+s()*.004f,playerX+s()*.050f,playerY+ph);RectF or=new RectF(obstacleX,ground-s()*.072f,obstacleX+obsW,ground);if(RectF.intersects(pr,or)){over=true;running=false;finishRound(-1,"撞到障碍","本局 "+score+" 分",score);}}
    @Override protected void onGameTap(float x,float y){if(over)return;RectF b=board();float ground=b.bottom-b.height()*.14f,ph=s()*.075f;if(!running){running=true;lastTick=now();}if(playerY>=ground-ph-s()*.010f){velocity=-s()*.72f;haptic(HapticFeedbackConstants.CLOCK_TICK);}animateNext();}
}
