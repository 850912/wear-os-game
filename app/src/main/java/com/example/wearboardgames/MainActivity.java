package com.example.wearboardgames;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.rgb(16, 18, 22)));
        setContentView(new BoardView(this));
    }
}

final class BoardView extends View {
    private static final int GOMOKU = 0, XIANGQI = 1;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mode = GOMOKU;
    private boolean menu = true;
    private int[][] gomoku = new int[11][11];
    private String[][] xiangqi = new String[10][9];
    private boolean gameOver;
    private String message = "你的回合";
    private int turn = 1;
    private int selectedX = -1, selectedY = -1;
    private int pendingX = -1, pendingY = -1;
    private float boardLeft, boardTop, cell;
    private float zoom = 1f, panX = 0f, panY = 0f;
    private float downX, downY, lastX, lastY, pinchStart, zoomStart;
    private boolean dragging, pinching;

    BoardView(Context context) { super(context); p.setTypeface(Typeface.create("sans", Typeface.NORMAL)); reset(); }

    void reset() {
        gameOver = false; turn = 1; selectedX = selectedY = pendingX = pendingY = -1; message = mode == GOMOKU ? "黑方回合 · 点选后按确认" : "红方回合 · 选子后按确认";
        gomoku = new int[11][11];
        xiangqi = new String[10][9];
        String[] top = {"車","馬","象","士","將","士","象","馬","車"};
        String[] bot = {"車","馬","相","仕","帥","仕","相","馬","車"};
        for (int x=0; x<9; x++) { xiangqi[0][x] = top[x]; xiangqi[9][x] = bot[x]; }
        xiangqi[0][1] = xiangqi[0][7] = "馬"; xiangqi[9][1] = xiangqi[9][7] = "馬";
        xiangqi[2][1] = xiangqi[2][7] = "砲"; xiangqi[7][1] = xiangqi[7][7] = "炮";
        for (int x=0; x<9; x+=2) { xiangqi[3][x] = "卒"; xiangqi[6][x] = "兵"; }
        invalidate();
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c); c.drawColor(Color.rgb(16,18,22));
        float w = getWidth(), h = getHeight();
        if (menu) { drawMenu(c,w,h); return; }
        p.setTextAlign(Paint.Align.CENTER); p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(Math.max(15, w * .055f)); p.setColor(Color.WHITE);
        c.drawText("棋盘游戏", w/2, 27, p);
        drawTab(c, w*.24f, 53, "五子棋", mode == GOMOKU);
        drawTab(c, w*.76f, 53, "象棋", mode == XIANGQI);
        boardTop = 75; float bottom = h - 58;
        int cols = mode == GOMOKU ? 11 : 9, rows = mode == GOMOKU ? 11 : 10;
        cell = Math.min((w - 28) / (cols - 1f), (bottom - boardTop - 4) / (rows - 1f));
        boardLeft = (w - cell*(cols-1))/2;
        c.save();
        c.translate(panX, panY); c.scale(zoom, zoom, w/2f, (boardTop + bottom)/2f);
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(222, 177, 93));
        c.drawRoundRect(boardLeft-12, boardTop-12, boardLeft+cell*(cols-1)+12, boardTop+cell*(rows-1)+12, 12,12,p);
        p.setStrokeWidth(1.4f); p.setColor(Color.rgb(93,65,37)); p.setStyle(Paint.Style.STROKE);
        for (int x=0;x<cols;x++) c.drawLine(boardLeft+x*cell,boardTop,boardLeft+x*cell,boardTop+(rows-1)*cell,p);
        for (int y=0;y<rows;y++) c.drawLine(boardLeft,boardTop+y*cell,boardLeft+(cols-1)*cell,boardTop+y*cell,p);
        p.setStyle(Paint.Style.FILL);
        if (mode == GOMOKU) drawGomoku(c); else drawXiangqi(c);
        c.restore();
        drawControls(c, w, h);
        p.setTextSize(12); p.setColor(gameOver ? Color.rgb(244,194,78) : Color.LTGRAY); c.drawText(message,w/2,h-39,p);
    }

    private void drawMenu(Canvas c, float w, float h) {
        p.setTextAlign(Paint.Align.CENTER); p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(Math.max(18, w*.075f)); p.setColor(Color.WHITE); c.drawText("棋盘游戏", w/2, h*.20f, p);
        p.setTypeface(Typeface.DEFAULT); p.setTextSize(12); p.setColor(Color.rgb(165,170,180)); c.drawText("选择一个游戏开始", w/2, h*.27f, p);
        drawMenuButton(c,w/2,h*.43f,"五子棋", "双人轮流 · 五连获胜");
        drawMenuButton(c,w/2,h*.66f,"中国象棋", "红黑轮流 · 点击确认移动");
    }
    private void drawMenuButton(Canvas c,float x,float y,String title,String subtitle) {
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(42,48,58)); c.drawRoundRect(x-82,y-34,x+82,y+34,16,16,p);
        p.setColor(Color.rgb(242,193,78)); p.setTypeface(Typeface.DEFAULT_BOLD); p.setTextSize(17); c.drawText(title,x,y-3,p);
        p.setColor(Color.rgb(185,190,200)); p.setTypeface(Typeface.DEFAULT); p.setTextSize(10); c.drawText(subtitle,x,y+17,p);
    }

    private void drawTab(Canvas c,float x,float y,String text,boolean selected) {
        p.setColor(selected ? Color.rgb(242,193,78) : Color.rgb(92,98,108)); p.setTextSize(14); p.setStyle(Paint.Style.FILL); c.drawText(text,x,y,p);
        if(selected){p.setStrokeWidth(2.5f); c.drawLine(x-25,y+7,x+25,y+7,p);}
    }
    private void drawGomoku(Canvas c) {
        for(int y=0;y<11;y++) for(int x=0;x<11;x++) if(gomoku[y][x]!=0){
            p.setColor(gomoku[y][x]==1?Color.rgb(29,31,35):Color.rgb(239,239,234)); c.drawCircle(boardLeft+x*cell,boardTop+y*cell,cell*.39f,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1); p.setColor(Color.rgb(80,80,80)); c.drawCircle(boardLeft+x*cell,boardTop+y*cell,cell*.39f,p); p.setStyle(Paint.Style.FILL);
        }
        if (pendingX >= 0 && gomoku[pendingY][pendingX] == 0) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3); p.setColor(Color.rgb(242,193,78));
            c.drawCircle(boardLeft+pendingX*cell, boardTop+pendingY*cell, cell*.44f, p); p.setStyle(Paint.Style.FILL);
        }
    }
    private void drawXiangqi(Canvas c) {
        p.setTextSize(cell*.62f); p.setTypeface(Typeface.DEFAULT_BOLD); p.setTextAlign(Paint.Align.CENTER);
        for(int y=0;y<10;y++) for(int x=0;x<9;x++) if(xiangqi[y][x]!=null){
            boolean red = y>=5 || xiangqi[y][x].equals("帥") || xiangqi[y][x].equals("仕") || xiangqi[y][x].equals("相") || xiangqi[y][x].equals("兵") || xiangqi[y][x].equals("炮");
            p.setColor(Color.rgb(239,222,176)); c.drawCircle(boardLeft+x*cell,boardTop+y*cell,cell*.42f,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.2f); p.setColor(Color.rgb(80,55,32)); c.drawCircle(boardLeft+x*cell,boardTop+y*cell,cell*.42f,p); p.setStyle(Paint.Style.FILL);
            p.setColor(red?Color.rgb(177,45,36):Color.rgb(30,30,30)); c.drawText(xiangqi[y][x],boardLeft+x*cell,boardTop+y*cell+cell*.21f,p);
        }
        if (selectedX >= 0) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3); p.setColor(Color.rgb(242,193,78));
            c.drawCircle(boardLeft+selectedX*cell, boardTop+selectedY*cell, cell*.48f, p); p.setStyle(Paint.Style.FILL);
        }
        if (pendingX >= 0) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3); p.setColor(Color.rgb(242,193,78));
            c.drawCircle(boardLeft+pendingX*cell, boardTop+pendingY*cell, cell*.48f, p); p.setStyle(Paint.Style.FILL);
        }
    }

    private void drawControls(Canvas c, float w, float h) {
        drawButton(c, 25, h-20, "−", true);
        drawButton(c, w-25, h-20, "+", true);
        drawButton(c, w/2, h-20, "确认", pendingX >= 0);
        p.setTextSize(10); p.setColor(Color.rgb(150,155,165)); c.drawText("拖动移动 · 双指或 +/- 缩放", w/2, h-4, p);
    }
    private void drawButton(Canvas c, float x, float y, String text, boolean enabled) {
        p.setColor(enabled ? Color.rgb(55,61,70) : Color.rgb(38,42,48)); p.setStyle(Paint.Style.FILL); c.drawRoundRect(x-25,y-15,x+25,y+15,10,10,p);
        p.setColor(enabled ? Color.WHITE : Color.rgb(100,104,110)); p.setTextSize(text.equals("确认")?12:22); p.setTextAlign(Paint.Align.CENTER); c.drawText(text,x,y+5,p);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        float x=e.getX(), y=e.getY();
        if (menu) {
            if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                if (y > getHeight()*.34f && y < getHeight()*.53f) { mode=GOMOKU; menu=false; reset(); }
                else if (y > getHeight()*.56f && y < getHeight()*.76f) { mode=XIANGQI; menu=false; reset(); }
                invalidate();
            }
            return true;
        }
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX=lastX=x; downY=lastY=y; dragging=false; pinching=false; return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (e.getPointerCount() >= 2) { pinching=true; pinchStart=distance(e); zoomStart=zoom; } return true;
            case MotionEvent.ACTION_MOVE:
                if (pinching && e.getPointerCount() >= 2) { zoom = clamp(zoomStart * distance(e)/Math.max(1f,pinchStart), .85f, 2.2f); invalidate(); return true; }
                if (Math.hypot(x-downX,y-downY) > 8) dragging=true;
                if (dragging) { panX += x-lastX; panY += y-lastY; lastX=x; lastY=y; invalidate(); }
                return true;
            case MotionEvent.ACTION_POINTER_UP: pinching=false; return true;
            case MotionEvent.ACTION_UP: break;
            default: return true;
        }
        if (pinching || dragging) return true;
        if(y<70){ mode = x < getWidth()/2 ? GOMOKU : XIANGQI; reset(); return true; }
        if(y>getHeight()-48){
            if (x < 55) { zoom=clamp(zoom-.15f,.85f,2.2f); invalidate(); return true; }
            if (x > getWidth()-55) { zoom=clamp(zoom+.15f,.85f,2.2f); invalidate(); return true; }
            if (pendingX >= 0) { if(mode==GOMOKU) confirmGomoku(); else confirmXiangqi(); invalidate(); return true; }
            return true;
        }
        if(gameOver) return true;
        float cx=getWidth()/2f, cy=(boardTop+getHeight()-58)/2f;
        float ux=cx+(x-panX-cx)/zoom, uy=cy+(y-panY-cy)/zoom;
        int gx=Math.round((ux-boardLeft)/cell), gy=Math.round((uy-boardTop)/cell);
        int cols=mode==GOMOKU?11:9, rows=mode==GOMOKU?11:10;
        if(gx<0||gx>=cols||gy<0||gy>=rows||Math.abs(ux-(boardLeft+gx*cell))>cell*.48f||Math.abs(uy-(boardTop+gy*cell))>cell*.48f) return true;
        if(mode==GOMOKU) selectGomoku(gx,gy); else selectXiangqi(gx,gy);
        invalidate(); return true;
    }
    private float distance(MotionEvent e){float dx=e.getX(0)-e.getX(1),dy=e.getY(0)-e.getY(1);return (float)Math.hypot(dx,dy);}
    private float clamp(float v,float lo,float hi){return Math.max(lo,Math.min(hi,v));}
    private void selectGomoku(int x,int y){if(gomoku[y][x]!=0){message="这里已有棋子";return;}pendingX=x;pendingY=y;message=(turn==1?"黑方":"白方")+"落在此处？按确认";}
    private void confirmGomoku(){int x=pendingX,y=pendingY;if(x<0||gomoku[y][x]!=0)return;gomoku[y][x]=turn;pendingX=pendingY=-1;if(checkFive(x,y,turn)){message=(turn==1?"黑方":"白方")+"获胜";gameOver=true;return;}turn=3-turn;message=(turn==1?"黑方":"白方")+"回合 · 点选后按确认";}
    private void playGomoku(int x,int y){
        if(gomoku[y][x]!=0)return; gomoku[y][x]=turn;
        if(checkFive(x,y,turn)){message=(turn==1?"黑方":"白方")+"获胜";gameOver=true;return;}
        turn = 3-turn; message = turn==1 ? "黑方回合" : "白方回合";
    }
    private int[] bestEmpty(){int bestX=-1,bestY=-1,score=-1;for(int y=0;y<11;y++)for(int x=0;x<11;x++)if(gomoku[y][x]==0){int s=0;for(int[]d:new int[][]{{1,0},{0,1},{1,1},{1,-1}}){for(int k=1;k<3;k++){int xx=x+d[0]*k,yy=y+d[1]*k;if(xx>=0&&xx<11&&yy>=0&&yy<11&&gomoku[yy][xx]==1)s+=3;}}if(s>score){score=s;bestX=x;bestY=y;}}return bestX<0?null:new int[]{bestX,bestY};}
    private boolean checkFive(int x,int y,int who){for(int[]d:new int[][]{{1,0},{0,1},{1,1},{1,-1}}){int n=1;for(int k=1;k<5;k++){if(cell(x+d[0]*k,y+d[1]*k)==who)n++;else break;}for(int k=1;k<5;k++){if(cell(x-d[0]*k,y-d[1]*k)==who)n++;else break;}if(n>=5)return true;}return false;}
    private int cell(int x,int y){return x<0||x>=11||y<0||y>=11?0:gomoku[y][x];}
    private void selectXiangqi(int x,int y){
        if (selectedX < 0) {
            if (xiangqi[y][x] == null) { message="请选择己方棋子"; return; }
            if (!belongsToTurn(xiangqi[y][x])) { message="请选"+(turn==1?"红方":"黑方")+"棋子"; return; }
            selectedX=x; selectedY=y; message="选择目标位置"; invalidate(); return;
        }
        if (x==selectedX && y==selectedY) { selectedX=selectedY=-1; message="已取消选择"; return; }
        if (xiangqi[y][x] != null && belongsToTurn(xiangqi[y][x])) { selectedX=x; selectedY=y; message="选择目标位置"; return; }
        pendingX=x; pendingY=y; message="移动到此处？按确认";
    }
    private void confirmXiangqi(){
        if(selectedX<0||pendingX<0)return;
        xiangqi[pendingY][pendingX] = xiangqi[selectedY][selectedX]; xiangqi[selectedY][selectedX] = null;
        selectedX=selectedY=pendingX=pendingY=-1; turn=3-turn; message=turn==1?"红方回合 · 选子后按确认":"黑方回合 · 选子后按确认";
    }
    private boolean belongsToTurn(String piece) {
        boolean red = piece.equals("帥") || piece.equals("仕") || piece.equals("相") || piece.equals("兵") || piece.equals("炮");
        return turn==1 ? red : !red;
    }
}
