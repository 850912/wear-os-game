package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

/** Standalone View for Game2048Engine. */
public final class Game2048View extends BaseGameView {
    private static final int MODE=GameModes.GAME_2048;private final Game2048Engine engine=new Game2048Engine();private boolean over;
    public Game2048View(Context c){super(c);}public static boolean supportsMode(int m){return m==MODE;}@Override protected int gameMode(){return MODE;}private RectF board(){return gamePanel(roundScreen?.16f:.12f);}
    @Override protected void resetGame(){engine.reset();over=false;}
    @Override protected void drawGame(Canvas c){RectF b=board();drawHeader(c,"2048",over?"没有可移动位置":"得分 "+engine.getScore());panel(c,b);float gap=dp(4),cell=(Math.min(b.width(),b.height())-gap*5)/4f,left=b.centerX()-(cell*4+gap*3)/2,top=b.centerY()-(cell*4+gap*3)/2;for(int y=0;y<4;y++)for(int x=0;x<4;x++){int v=engine.valueAt(x,y);RectF r=new RectF(left+x*(cell+gap),top+y*(cell+gap),left+x*(cell+gap)+cell,top+y*(cell+gap)+cell);p.setColor(tileColor(v));c.drawRoundRect(r,dp(8),dp(8),p);if(v>0)textFit(c,String.valueOf(v),r.centerX(),r.centerY()+cell*.12f,v<100?s()*.050f:v<1000?s()*.043f:s()*.033f,v<=4?Color.rgb(55,58,64):Color.WHITE,true,Paint.Align.CENTER,cell*.82f);}}
    private int tileColor(int v){if(v==0)return Color.rgb(54,58,68);int[]c={0,0,Color.rgb(238,228,210),Color.rgb(235,219,187),Color.rgb(242,177,121),Color.rgb(245,149,99),Color.rgb(246,124,95),Color.rgb(246,94,59),Color.rgb(237,207,114),Color.rgb(237,204,97),Color.rgb(237,200,80),Color.rgb(237,197,63)};int p=31-Integer.numberOfLeadingZeros(v);return c[Math.min(c.length-1,p)];}
    @Override protected void onGameSwipe(float dx,float dy){if(over)return;int dir=Math.abs(dx)>Math.abs(dy)?(dx>0?1:3):(dy>0?2:0);Game2048Engine.MoveFrame f=engine.move(dir);if(f.changed){haptic(HapticFeedbackConstants.CLOCK_TICK);if(f.scoreGain>0)sound(SoundManager.SCORE);over=f.gameOver;if(over)finishRound(-1,"没有可移动位置","得分 "+engine.getScore(),engine.getScore());invalidate();}}
    @Override protected void saveGame(){if(over){clearSavedGameIfMine();return;}GameSaveManager.save(prefs,MODE,2,false,engine.serialize());}
    @Override protected boolean restoreGame(){GameSaveManager.SaveRecord r=GameSaveManager.load(prefs,MODE);if(r==null)return false;over=false;return engine.restore(r.payload);}
}
