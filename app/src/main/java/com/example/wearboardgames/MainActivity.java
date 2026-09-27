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
    private int[][] gomoku = new int[11][11];
    private String[][] xiangqi = new String[10][9];
    private boolean gameOver;
    private String message = "你的回合";
    private int turn = 1;
    private int selectedX = -1, selectedY = -1;
    private float boardLeft, boardTop, cell;

    BoardView(Context context) { super(context); p.setTypeface(Typeface.create("sans", Typeface.NORMAL)); reset(); }

    void reset() {
        gameOver = false; turn = 1; selectedX = selectedY = -1; message = mode == GOMOKU ? "黑方回合" : "红方回合";
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
        p.setTextAlign(Paint.Align.CENTER); p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(Math.max(15, w * .055f)); p.setColor(Color.WHITE);
        c.drawText("棋盘游戏", w/2, 27, p);
        drawTab(c, w*.24f, 53, "五子棋", mode == GOMOKU);
        drawTab(c, w*.76f, 53, "象棋", mode == XIANGQI);
        boardTop = 75; float bottom = h - 43;
        int cols = mode == GOMOKU ? 11 : 9, rows = mode == GOMOKU ? 11 : 10;
        cell = Math.min((w - 28) / (cols - 1f), (bottom - boardTop - 4) / (rows - 1f));
        boardLeft = (w - cell*(cols-1))/2;
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(222, 177, 93));
        c.drawRoundRect(boardLeft-12, boardTop-12, boardLeft+cell*(cols-1)+12, boardTop+cell*(rows-1)+12, 12,12,p);
        p.setStrokeWidth(1.4f); p.setColor(Color.rgb(93,65,37)); p.setStyle(Paint.Style.STROKE);
        for (int x=0;x<cols;x++) c.drawLine(boardLeft+x*cell,boardTop,boardLeft+x*cell,boardTop+(rows-1)*cell,p);
        for (int y=0;y<rows;y++) c.drawLine(boardLeft,boardTop+y*cell,boardLeft+(cols-1)*cell,boardTop+y*cell,p);
        p.setStyle(Paint.Style.FILL);
        if (mode == GOMOKU) drawGomoku(c); else drawXiangqi(c);
        p.setTextSize(13); p.setColor(gameOver ? Color.rgb(244,194,78) : Color.LTGRAY); c.drawText(message,w/2,h-17,p);
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
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if(e.getAction()!=MotionEvent.ACTION_UP) return true;
        float x=e.getX(), y=e.getY();
        if(y<70){ mode = x < getWidth()/2 ? GOMOKU : XIANGQI; reset(); return true; }
        if(y>getHeight()-45){ reset(); return true; }
        if(gameOver || y<boardTop-cell*.55f) return true;
        int gx=Math.round((x-boardLeft)/cell), gy=Math.round((y-boardTop)/cell);
        int cols=mode==GOMOKU?11:9, rows=mode==GOMOKU?11:10;
        if(gx<0||gx>=cols||gy<0||gy>=rows||Math.abs(x-(boardLeft+gx*cell))>cell*.48f||Math.abs(y-(boardTop+gy*cell))>cell*.48f) return true;
        if(mode==GOMOKU) playGomoku(gx,gy); else playXiangqi(gx,gy);
        invalidate(); return true;
    }
    private void playGomoku(int x,int y){
        if(gomoku[y][x]!=0)return; gomoku[y][x]=turn;
        if(checkFive(x,y,turn)){message=(turn==1?"黑方":"白方")+"获胜";gameOver=true;return;}
        turn = 3-turn; message = turn==1 ? "黑方回合" : "白方回合";
    }
    private int[] bestEmpty(){int bestX=-1,bestY=-1,score=-1;for(int y=0;y<11;y++)for(int x=0;x<11;x++)if(gomoku[y][x]==0){int s=0;for(int[]d:new int[][]{{1,0},{0,1},{1,1},{1,-1}}){for(int k=1;k<3;k++){int xx=x+d[0]*k,yy=y+d[1]*k;if(xx>=0&&xx<11&&yy>=0&&yy<11&&gomoku[yy][xx]==1)s+=3;}}if(s>score){score=s;bestX=x;bestY=y;}}return bestX<0?null:new int[]{bestX,bestY};}
    private boolean checkFive(int x,int y,int who){for(int[]d:new int[][]{{1,0},{0,1},{1,1},{1,-1}}){int n=1;for(int k=1;k<5;k++){if(cell(x+d[0]*k,y+d[1]*k)==who)n++;else break;}for(int k=1;k<5;k++){if(cell(x-d[0]*k,y-d[1]*k)==who)n++;else break;}if(n>=5)return true;}return false;}
    private int cell(int x,int y){return x<0||x>=11||y<0||y>=11?0:gomoku[y][x];}
    private void playXiangqi(int x,int y){
        if (selectedX < 0) {
            if (xiangqi[y][x] == null) { message="请选择己方棋子"; return; }
            if (!belongsToTurn(xiangqi[y][x])) { message="请选"+(turn==1?"红方":"黑方")+"棋子"; return; }
            selectedX=x; selectedY=y; message="选择目标位置"; invalidate(); return;
        }
        if (x==selectedX && y==selectedY) { selectedX=selectedY=-1; message="已取消选择"; return; }
        if (xiangqi[y][x] != null && belongsToTurn(xiangqi[y][x])) { selectedX=x; selectedY=y; message="选择目标位置"; return; }
        xiangqi[y][x] = xiangqi[selectedY][selectedX]; xiangqi[selectedY][selectedX] = null;
        selectedX=selectedY=-1; turn=3-turn; message=turn==1?"红方回合":"黑方回合";
    }
    private boolean belongsToTurn(String piece) {
        boolean red = piece.equals("帥") || piece.equals("仕") || piece.equals("相") || piece.equals("兵") || piece.equals("炮");
        return turn==1 ? red : !red;
    }
}
