package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

/** View shell for the pure XiangqiEngine. */
public final class XiangqiView extends BaseGameView {
    private static final int MODE=GameModes.XIANGQI;private final XiangqiEngine engine=new XiangqiEngine();private int sx=-1,sy=-1;private boolean over;
    public XiangqiView(Context c){super(c);}public static boolean supportsMode(int m){return m==MODE;}@Override protected int gameMode(){return MODE;}
    private RectF board(){return gamePanel(roundScreen?.16f:.12f);}
    @Override protected void resetGame(){engine.reset();sx=sy=-1;over=false;}
    @Override protected void drawGame(Canvas c){RectF b=board();drawHeader(c,"中国象棋",over?"本局结束":engine.isRedTurn()?"红方走":"黑方走");panel(c,b);float cell=Math.min(b.width()/8f,b.height()/9f),w=cell*8,h=cell*9,left=b.centerX()-w/2,top=b.centerY()-h/2;p.setColor(Color.rgb(193,153,100));p.setStrokeWidth(dp(1));for(int x=0;x<9;x++)c.drawLine(left+x*cell,top,left+x*cell,top+h,p);for(int y=0;y<10;y++)c.drawLine(left,top+y*cell,left+w,top+y*cell,p);for(int y=0;y<10;y++)for(int x=0;x<9;x++){char pc=engine.pieceAt(x,y);if(pc==0)continue;float cx=left+x*cell,cy=top+y*cell,r=cell*.39f;p.setColor(Color.rgb(242,223,181));c.drawCircle(cx,cy,r,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1.2f));p.setColor(Character.isUpperCase(pc)?Color.rgb(196,68,62):Color.rgb(48,48,48));c.drawCircle(cx,cy,r,p);p.setStyle(Paint.Style.FILL);text(c,name(pc),cx,cy+cell*.12f,cell*.38f,Character.isUpperCase(pc)?Color.rgb(190,54,52):Color.rgb(34,34,34),true,Paint.Align.CENTER);}if(sx>=0){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));p.setColor(PRIMARY);c.drawCircle(left+sx*cell,top+sy*cell,cell*.47f,p);p.setStyle(Paint.Style.FILL);}}
    private String name(char p){switch(Character.toUpperCase(p)){case'R':return"车";case'H':return"马";case'E':return"相";case'A':return"士";case'K':return Character.isUpperCase(p)?"帅":"将";case'C':return"炮";default:return"兵";}}
    @Override protected void onGameTap(float x,float y){if(over)return;RectF b=board();float cell=Math.min(b.width()/8f,b.height()/9f),left=b.centerX()-cell*4,top=b.centerY()-cell*4.5f;int tx=Math.round((x-left)/cell),ty=Math.round((y-top)/cell);if(tx<0||tx>8||ty<0||ty>9)return;if(sx<0){if(engine.belongsToTurn(tx,ty)){sx=tx;sy=ty;haptic(HapticFeedbackConstants.CLOCK_TICK);}}else{XiangqiEngine.MoveResult r=engine.move(sx,sy,tx,ty);if(r==XiangqiEngine.MoveResult.ILLEGAL){if(engine.belongsToTurn(tx,ty)){sx=tx;sy=ty;}else{sx=sy=-1;}}else{sx=sy=-1;haptic(HapticFeedbackConstants.CONFIRM);if(r==XiangqiEngine.MoveResult.RED_WINS||r==XiangqiEngine.MoveResult.BLACK_WINS){over=true;finishRound(1,r==XiangqiEngine.MoveResult.RED_WINS?"红方获胜":"黑方获胜","完整规则对局",1);}}}invalidate();}
    @Override protected void saveGame(){if(over){clearSavedGameIfMine();return;}GameSaveManager.save(prefs,MODE,1,false,engine.serialize());}
    @Override protected boolean restoreGame(){GameSaveManager.SaveRecord r=GameSaveManager.load(prefs,MODE);if(r==null)return false;over=false;sx=sy=-1;return engine.restore(r.payload);}
}
