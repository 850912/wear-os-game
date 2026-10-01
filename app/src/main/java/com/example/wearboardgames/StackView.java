package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;
import java.util.Arrays;

/** Ten-floor precision stacking game. */
public final class StackView extends BaseGameView {
    private static final int MODE=GameModes.STACK_TOWER;private final float[] centers=new float[11],widths=new float[11];private int level;private float movingX,movingW,vx;private long lastTick;private boolean running,over,won,initialized;
    public StackView(Context c){super(c);}public static boolean supportsMode(int m){return m==MODE;}@Override protected int gameMode(){return MODE;}private RectF board(){return gamePanel(roundScreen?.11f:.075f);}
    @Override protected void resetGame(){Arrays.fill(centers,0);Arrays.fill(widths,0);level=1;running=false;over=won=false;initialized=false;lastTick=now();}
    private void init(RectF b){float w=b.width()*.64f;centers[0]=b.centerX();widths[0]=w;movingW=w;movingX=b.left+w/2;vx=s()*.26f;initialized=true;lastTick=now();}
    @Override protected void drawGame(Canvas c){RectF b=board();if(!initialized&&b.width()>0)init(b);update(b);drawHeader(c,"叠塔",over?(won?"完成 10 层":"停在第 "+level+" 层"):"第 "+level+" / 10 层");panel(c,b);float bh=Math.min(s()*.044f,b.height()/13f),baseY=b.bottom-bh*.6f;for(int i=0;i<level;i++){float y=baseY-i*bh;float w=widths[i];p.setColor(i%2==0?PRIMARY:SECONDARY);c.drawRoundRect(new RectF(centers[i]-w/2,y-bh,centers[i]+w/2,y-dp(1)),dp(4),dp(4),p);}if(!over){float y=baseY-level*bh;p.setColor(GOOD);c.drawRoundRect(new RectF(movingX-movingW/2,y-bh,movingX+movingW/2,y-dp(1)),dp(4),dp(4),p);}if(!running&&!over)text(c,"轻点开始移动，再点落下",b.centerX(),b.top+b.height()*.20f,s()*.024f,MUTED,false,Paint.Align.CENTER);if(running&&!over)animateNext();}
    private void update(RectF b){if(!running||over)return;long t=now();float dt=Math.min(.035f,(t-lastTick)/1000f);lastTick=t;movingX+=vx*dt;if(movingX-movingW/2<b.left){movingX=b.left+movingW/2;vx=Math.abs(vx);}else if(movingX+movingW/2>b.right){movingX=b.right-movingW/2;vx=-Math.abs(vx);}}
    private void place(){if(over)return;RectF b=board();if(!initialized)init(b);if(!running){running=true;lastTick=now();return;}float prevL=centers[level-1]-widths[level-1]/2,prevR=centers[level-1]+widths[level-1]/2,l=movingX-movingW/2,r=movingX+movingW/2,ol=Math.max(l,prevL),or=Math.min(r,prevR);if(or<=ol){over=true;running=false;finishRound(-1,"叠歪了","到达第 "+level+" 层",level-1);return;}centers[level]=(ol+or)/2;widths[level]=or-ol;haptic(HapticFeedbackConstants.CONFIRM);if(level>=10){won=over=true;running=false;finishRound(1,"登顶成功！","完成 10 层",10);return;}level++;movingW=widths[level-1];movingX=vx>0?b.left+movingW/2:b.right-movingW/2;vx=Math.copySign(Math.min(s()*.52f,Math.abs(vx)*1.08f),vx);lastTick=now();invalidate();}@Override protected void onGameTap(float x,float y){place();}
}
