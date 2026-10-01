package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Standalone Simon memory sequence game. */
public final class SimonView extends BaseGameView {
    private static final int MODE=GameModes.SIMON;private final List<Integer> seq=new ArrayList<>();private final Random random=new Random();private final RectF drawRect=new RectF();private int input,flash=-1,round;private long phaseStart,flashUntil;private boolean showing,over;
    private static final int[] COLORS={Color.rgb(91,198,126),Color.rgb(223,96,100),Color.rgb(85,144,230),Color.rgb(232,190,75)};
    public SimonView(Context c){super(c);}public static boolean supportsMode(int m){return m==MODE;}@Override protected int gameMode(){return MODE;}private RectF board(){return gamePanel(roundScreen?.16f:.12f);}
    @Override protected void resetGame(){seq.clear();input=0;round=0;flash=-1;over=false;showing=true;addRound();}
    private void addRound(){seq.add(random.nextInt(4));round=seq.size();input=0;showing=true;phaseStart=now()+450;invalidateSoon(450);}
    private int showIndex(){if(!showing)return -1;long elapsed=now()-phaseStart;if(elapsed<0)return -1;int slot=(int)(elapsed/620);if(slot>=seq.size()){showing=false;return -1;}long within=elapsed%620;return within<360?seq.get(slot):-1;}
    @Override protected void drawGame(Canvas c){RectF b=board();drawHeader(c,"记忆闪烁",over?"结束 · "+Math.max(0,round-1)+" 轮":showing?"看清顺序 · 第 "+round+" 轮":"轮到你 · "+input+" / "+seq.size());panel(c,b);int active=showIndex();if(flash>=0&&now()<flashUntil)active=flash;else if(flash>=0)flash=-1;float gap=dp(5),w=(b.width()-gap*3)/2,h=(b.height()-gap*3)/2;for(int i=0;i<4;i++){int row=i/2,col=i%2;drawRect.set(b.left+gap+col*(w+gap),b.top+gap+row*(h+gap),b.left+gap+col*(w+gap)+w,b.top+gap+row*(h+gap)+h);int color=COLORS[i];if(i!=active)color=Color.rgb((int)(Color.red(color)*.55f),(int)(Color.green(color)*.55f),(int)(Color.blue(color)*.55f));p.setColor(color);c.drawRoundRect(drawRect,dp(18),dp(18),p);}if(showing||flash>=0)animateNext();}
    private int hit(float x,float y){RectF b=board();if(!b.contains(x,y))return -1;return (y<b.centerY()?0:2)+(x<b.centerX()?0:1);}
    @Override protected void onGameTap(float x,float y){if(over||showing)return;int q=hit(x,y);if(q<0)return;flash=q;flashUntil=now()+180;haptic(HapticFeedbackConstants.CLOCK_TICK);if(q!=seq.get(input)){over=true;finishRound(-1,"顺序错了","完成 "+Math.max(0,round-1)+" 轮",Math.max(0,round-1));return;}input++;if(input>=seq.size()){sound(SoundManager.CLEAR);if(round>=12){over=true;finishRound(1,"记忆大师！","连续完成 12 轮",12);}else{showing=true;phaseStart=now()+650;postDelayed(this::addRound,650);}}invalidate();}
}
