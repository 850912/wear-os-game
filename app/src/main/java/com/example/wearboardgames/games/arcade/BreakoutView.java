package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;
import java.util.Arrays;

/** Standalone brick breaker. */
public final class BreakoutView extends BaseGameView {
    private static final int MODE=GameModes.BRICK_BREAKER, COLS=6, ROWS=4;
    private final boolean[] bricks=new boolean[COLS*ROWS];
    private final RectF drawRect=new RectF();
    private float ballX,ballY,vx,vy,paddleX; private int left,score; private long lastTick; private boolean running,over,initialized;
    public BreakoutView(Context c){super(c);} public static boolean supportsMode(int m){return m==MODE;} @Override protected int gameMode(){return MODE;}
    private RectF board(){return gamePanel(roundScreen?.09f:.06f);}
    @Override protected void resetGame(){Arrays.fill(bricks,true);left=bricks.length;score=0;running=false;over=false;initialized=false;lastTick=now();}
    private void init(RectF b){paddleX=b.centerX();ballX=b.centerX();ballY=b.bottom-b.height()*.18f;float sp=s()*.48f;vx=sp*.44f;vy=-sp;initialized=true;lastTick=now();}
    @Override protected void drawGame(Canvas c){RectF b=board();if(!initialized&&b.width()>0)init(b);update(b);drawHeader(c,"砖块破坏",over?"结束 · "+score+" 分":"剩余 "+left+" · 得分 "+score);panel(c,b);float gap=dp(2),bw=(b.width()-gap*(COLS+1))/COLS,bh=b.height()*.055f,top=b.top+b.height()*.06f;for(int r=0;r<ROWS;r++)for(int col=0;col<COLS;col++){int i=r*COLS+col;if(!bricks[i])continue;float l=b.left+gap+col*(bw+gap),t=top+r*(bh+gap);p.setColor(Color.rgb(80+25*r,135+12*col,210-18*r));drawRect.set(l,t,l+bw,t+bh);c.drawRoundRect(drawRect,dp(3),dp(3),p);}float pw=b.width()*.30f,ph=Math.max(dp(6),s()*.018f);p.setColor(PRIMARY);drawRect.set(paddleX-pw/2,b.bottom-ph*2,paddleX+pw/2,b.bottom-ph);c.drawRoundRect(drawRect,ph,ph,p);p.setColor(TEXT);c.drawCircle(ballX,ballY,s()*.019f,p);if(!running&&!over)text(c,"拖动挡板 · 轻点开球",b.centerX(),b.centerY()+s()*.13f,s()*.025f,MUTED,false,Paint.Align.CENTER);if(running)animateNext();}
    private void update(RectF b){if(!running||over||!initialized)return;long t=now();float dt=Math.min(.03f,(t-lastTick)/1000f);lastTick=t;float r=s()*.019f,pw=b.width()*.30f,ph=Math.max(dp(6),s()*.018f);ballX+=vx*dt;ballY+=vy*dt;if(ballX-r<=b.left&&vx<0||ballX+r>=b.right&&vx>0){vx=-vx;ballX=clamp(ballX,b.left+r,b.right-r);}if(ballY-r<=b.top&&vy<0){vy=-vy;ballY=b.top+r;}float py=b.bottom-ph*2;if(vy>0&&ballY+r>=py&&ballY-r<=py+ph&&Math.abs(ballX-paddleX)<=pw*.56f){ballY=py-r;vy=-Math.abs(vy);vx+=(ballX-paddleX)*1.9f;haptic(HapticFeedbackConstants.CLOCK_TICK);}float gap=dp(2),brickW=(b.width()-gap*(COLS+1))/COLS,bh=b.height()*.055f,top=b.top+b.height()*.06f;for(int rr=0;rr<ROWS;rr++)for(int cc=0;cc<COLS;cc++){int i=rr*COLS+cc;if(!bricks[i])continue;float bl=b.left+gap+cc*(brickW+gap),bt=top+rr*(bh+gap),br=bl+brickW,bb=bt+bh;if(ballX+r>=bl&&ballX-r<=br&&ballY+r>=bt&&ballY-r<=bb){bricks[i]=false;left--;score+=10;vy=-vy;haptic(HapticFeedbackConstants.CLOCK_TICK);sound(SoundManager.SCORE);if(left==0){over=true;running=false;finishRound(1,"清空砖块！","得分 "+score,score);}return;}}if(ballY-r>b.bottom){over=true;running=false;finishRound(-1,"球掉下去了","得分 "+score,score);}}
    private void control(float x){RectF b=board();if(!initialized)init(b);float pw=b.width()*.30f;paddleX=clamp(x,b.left+pw/2,b.right-pw/2);if(!running){running=true;lastTick=now();}invalidate();}
    @Override protected void onGameTouchDown(float x,float y){control(x);} @Override protected void onGameTouchMove(float x,float y){control(x);} @Override protected void onGameTap(float x,float y){control(x);}
}
