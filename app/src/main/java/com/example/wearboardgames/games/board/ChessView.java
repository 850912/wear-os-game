package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import java.util.Collections;
import java.util.List;

public final class ChessView extends ZoomBoardView {
    private final ChessEngine engine = new ChessEngine();
    private final RectF squareRect = new RectF(), promotionRect = new RectF();
    private int sx=-1, sy=-1, tx=-1, ty=-1;
    private boolean over;
    private List<Move> promotions = Collections.emptyList();
    private final RectF[] promotionTargets = {new RectF(),new RectF(),new RectF(),new RectF()};

    public ChessView(Context context) { super(context); }
    public static boolean supportsMode(int mode) { return mode==GameModes.CHESS; }
    @Override protected int gameMode() { return GameModes.CHESS; }
    @Override protected RectF board() { return gamePanel(roundScreen?.10f:.06f); }
    @Override protected void resetGame() {
        cancelAi(); viewport.reset(); engine.reset(); over=false; clearSelection();
    }
    private void clearSelection() { sx=sy=tx=ty=-1; promotions=Collections.emptyList(); }
    @Override protected void drawGame(Canvas canvas) {
        startAi();
        drawHeader(canvas,"国际象棋",aiThinking?"黑方思考中":over?"本局结束":engine.check()?"将军":tx>=0?"再次点击目标确认":engine.whiteTurn()?"白方走":"黑方走");
        RectF b=board(); float cell=Math.min(b.width(),b.height())/8;
        float left=b.centerX()-cell*4, top=b.centerY()-cell*4;
        beginBoard(canvas,b);
        for (int y=0;y<8;y++) for (int x=0;x<8;x++) {
            squareRect.set(left+x*cell,top+y*cell,left+(x+1)*cell,top+(y+1)*cell);
            p.setColor((x+y)%2==0?Color.rgb(221,231,225):Color.rgb(72,112,98));
            p.setStyle(Paint.Style.FILL); canvas.drawRect(squareRect,p);
            if ((x==sx && y==sy)||(x==tx && y==ty)) {
                p.setColor(Color.argb(170,244,207,77)); canvas.drawRect(squareRect,p);
            }
            Piece piece=engine.piece(x,y);
            if (piece!=Piece.NONE) {
                // A contrasting disc keeps both colors readable on either square.
                boolean white=piece.getPieceSide()==Side.WHITE;
                p.setColor(white?Color.WHITE:Color.rgb(25,29,27));
                canvas.drawCircle(squareRect.centerX(),squareRect.centerY(),cell*.42f,p);
                text(canvas,piece.getFanSymbol(),squareRect.centerX(),squareRect.centerY()+cell*.22f,
                        cell*.70f,white?Color.rgb(25,29,27):Color.WHITE,false,Paint.Align.CENTER);
            }
        }
        if (sx>=0) for (Move move:engine.legalMoves()) if (ChessEngine.x(move.getFrom())==sx && ChessEngine.y(move.getFrom())==sy) {
            p.setColor(Color.argb(190,244,207,77));
            canvas.drawCircle(left+(ChessEngine.x(move.getTo())+.5f)*cell,top+(ChessEngine.y(move.getTo())+.5f)*cell,cell*.10f,p);
        }
        endBoard(canvas);
        if (!promotions.isEmpty()) drawPromotions(canvas,b);
    }
    private void drawPromotions(Canvas canvas,RectF b) {
        p.setColor(Color.argb(225,0,0,0)); canvas.drawRect(b,p);
        float cell=Math.min(b.width(),b.height())*.36f;
        promotionRect.set(b.centerX()-cell,b.centerY()-cell,b.centerX()+cell,b.centerY()+cell);
        for (int i=0;i<promotions.size();i++) {
            RectF r=promotionTargets[i]; float x=promotionRect.left+(i%2)*cell,y=promotionRect.top+(i/2)*cell;
            r.set(x,y,x+cell,y+cell); p.setColor(SURFACE_HIGH); canvas.drawRoundRect(r,dp(6),dp(6),p);
            text(canvas,promotions.get(i).getPromotion().getFanSymbol(),r.centerX(),r.centerY()+cell*.18f,cell*.55f,TEXT,false,Paint.Align.CENTER);
        }
    }
    @Override protected boolean onGamePan(float dx,float dy) { return !promotions.isEmpty() || super.onGamePan(dx,dy); }
    @Override protected void onGamePinchZoom(float factor) { if (promotions.isEmpty()) super.onGamePinchZoom(factor); }
    @Override protected void onGameTap(float x,float y) {
        if (over || (isSinglePlayer()&&!engine.whiteTurn())) return;
        if (!promotions.isEmpty()) {
            for (int i=0;i<promotions.size();i++) if (promotionTargets[i].contains(x,y)) { commit(promotions.get(i)); return; }
            return;
        }
        RectF b=board(); float cell=Math.min(b.width(),b.height())/8;
        x=viewport.boardX(x,b.centerX()); y=viewport.boardY(y,b.centerY());
        int gx=(int)Math.floor((x-b.centerX()+cell*4)/cell),gy=(int)Math.floor((y-b.centerY()+cell*4)/cell);
        if(gx<0||gx>7||gy<0||gy>7)return;
        if(engine.belongsToTurn(gx,gy)) { sx=gx;sy=gy;tx=ty=-1;invalidate();return; }
        if(sx<0)return;
        List<Move> options=engine.candidates(sx,sy,gx,gy);
        if(options.isEmpty()){haptic(HapticFeedbackConstants.REJECT);return;}
        if(moveConfirmationEnabled()&&(tx!=gx||ty!=gy)){tx=gx;ty=gy;invalidate();return;}
        if(options.size()>1){promotions=options;invalidate();return;}
        commit(options.get(0));
    }
    private void commit(Move move) {
        if(!engine.move(move))return;
        clearSelection(); haptic(HapticFeedbackConstants.CONFIRM); checkResult(); startAi(); invalidate();
    }
    private void checkResult() {
        if(engine.mate()) {
            over=true;
            boolean whiteWon=!engine.whiteTurn();
            finishRound(isSinglePlayer()&&!whiteWon?-1:1,whiteWon?"白方获胜":"黑方获胜","将死",1);
        } else if(engine.draw()){over=true;finishRound(0,"和棋","本局结束",1);}
    }
    private void startAi() {
        if(over||!isSinglePlayer()||engine.whiteTurn()||aiThinking)return;
        String snapshot=engine.serialize();
        requestAi(()->{ChessEngine copy=new ChessEngine();if(!copy.restore(snapshot))throw new IllegalStateException("Invalid chess snapshot");return copy.chooseAiMove();},move->{
            if(move!=null)engine.move(move);checkResult();
        });
    }
    @Override protected void saveGame() {
        if(over){clearSavedGameIfMine();return;}
        GameSaveManager.save(prefs,gameMode(),1,isSinglePlayer(),engine.serialize());
    }
    @Override protected boolean restoreGame() {
        cancelAi();viewport.reset();clearSelection();over=false;
        GameSaveManager.SaveRecord record=GameSaveManager.load(prefs,gameMode());
        if(record==null||!engine.restore(record.payload))return false;
        checkResult();return true;
    }
}
