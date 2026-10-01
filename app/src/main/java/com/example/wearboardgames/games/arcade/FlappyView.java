package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

/** Easier, smoother standalone flappy game. */
public final class FlappyView extends BaseGameView {
    private static final int MODE = GameModes.FLAPPY;
    private float birdY, velocity, pipeX, gapY;
    private int score;
    private long lastTick;
    private boolean running, over;
    private final RectF drawRect = new RectF();

    public FlappyView(Context context){super(context);}
    public static boolean supportsMode(int mode){return mode==MODE;}
    @Override protected int gameMode(){return MODE;}

    private RectF board(){return gamePanel(roundScreen ? .105f : .075f);}
    @Override protected void resetGame(){RectF b=board();birdY=b.height()>0?b.centerY():s()*.43f;velocity=0;pipeX=b.width()>0?b.right:getWidth();gapY=b.height()>0?b.centerY():s()*.43f;score=0;running=false;over=false;lastTick=now();}

    @Override protected void drawGame(Canvas c){RectF b=board();if((birdY<=0||pipeX<=0)&&b.width()>0){birdY=b.centerY();pipeX=b.right;gapY=b.centerY();}update(b);drawHeader(c,"跳跃小鸟",over?"结束 · "+score+" 分":running?score+" 分 · 轻点上升":"轻点开始");panel(c,b);float pipeW=b.width()*.14f,gap=gapSize(b);p.setColor(Color.rgb(78,192,123));drawRect.set(pipeX,b.top,pipeX+pipeW,gapY-gap/2);c.drawRoundRect(drawRect,dp(8),dp(8),p);drawRect.set(pipeX,gapY+gap/2,pipeX+pipeW,b.bottom);c.drawRoundRect(drawRect,dp(8),dp(8),p);float birdX=b.left+b.width()*.27f,r=s()*.026f;float tilt=clamp(velocity/(s()*.42f),-1,1);c.save();c.rotate(tilt*24f,birdX,birdY);p.setColor(Color.rgb(255,210,78));drawRect.set(birdX-r*1.2f,birdY-r*.82f,birdX+r*1.2f,birdY+r*.82f);c.drawOval(drawRect,p);p.setColor(Color.rgb(255,242,196));c.drawCircle(birdX+r*.42f,birdY-r*.22f,r*.23f,p);p.setColor(Color.rgb(43,48,57));c.drawCircle(birdX+r*.49f,birdY-r*.25f,r*.08f,p);c.restore();if(!running&&!over)text(c,"节奏点按，不需要连续猛点",b.centerX(),b.centerY()+s()*.12f,s()*.026f,MUTED,false,Paint.Align.CENTER);if(running)animateNext();}
    private float gapSize(RectF b){return b.height()*Math.max(.34f,.43f-Math.min(.07f,score*.004f));}
    private void update(RectF b){if(!running||over||startCountdownActive())return;long t=now();if(lastTick==0)lastTick=t;float dt=Math.min(.030f,(t-lastTick)/1000f);lastTick=t;velocity+=s()*.83f*dt;birdY+=velocity*dt;float speed=s()*(.165f+Math.min(.035f,score*.0025f));pipeX-=speed*dt;float pipeW=b.width()*.14f,birdX=b.left+b.width()*.27f,r=s()*.023f,gap=gapSize(b);if(pipeX+pipeW<b.left){pipeX=b.right+b.width()*.05f;gapY=b.top+b.height()*(.31f+(float)Math.random()*.38f);score++;showScorePopup("+1");GameStats.recordMaxMetric(prefs,gameMode(),"pipes",score);haptic(HapticFeedbackConstants.CLOCK_TICK);}boolean hitPipe=birdX+r>pipeX&&birdX-r<pipeX+pipeW&&(birdY-r<gapY-gap/2||birdY+r>gapY+gap/2);if(birdY-r<b.top||birdY+r>b.bottom||hitPipe){over=true;running=false;finishRound(-1,"撞到了","本局 "+score+" 分",score);}}
    @Override protected void onGameTap(float x,float y){if(over)return;if(!running){running=true;lastTick=now();beginStartCountdown();}velocity=-s()*.285f;haptic(HapticFeedbackConstants.CLOCK_TICK);animateNext();}

    @Override protected void saveGame(){if(over){clearSavedGameIfMine();return;}GameSaveManager.save(prefs,MODE,3,false,birdY+","+velocity+","+pipeX+","+gapY);prefs.edit().putInt("save_aux",score).apply();}
    @Override protected boolean restoreGame(){GameSaveManager.SaveRecord rec=GameSaveManager.load(prefs,MODE);if(rec==null)return false;String[] q=rec.payload.split(",");if(q.length!=4)return false;try{float by=Float.parseFloat(q[0]),v=Float.parseFloat(q[1]),px=Float.parseFloat(q[2]),gy=Float.parseFloat(q[3]);if(!Float.isFinite(by)||!Float.isFinite(v)||!Float.isFinite(px)||!Float.isFinite(gy))return false;RectF b=board();if(b.height()>0&&(by<b.top-s()*.1f||by>b.bottom+s()*.1f||gy<b.top||gy>b.bottom))return false;birdY=by;velocity=v;pipeX=px;gapY=gy;score=Math.max(0,prefs.getInt("save_aux",0));running=false;over=false;lastTick=now();return true;}catch(RuntimeException ex){return false;}}
}
