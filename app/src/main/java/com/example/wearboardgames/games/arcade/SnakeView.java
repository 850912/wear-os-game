package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** Standalone snake game with event-driven ticks instead of idle frame polling. */
public final class SnakeView extends BaseGameView {
    private static final int MODE = GameModes.SNAKE;
    private static final int N = 12;
    private final ArrayDeque<Cell> snake = new ArrayDeque<>();
    private int direction, queuedDirection, foodX, foodY, score;
    private boolean running, over, won;
    private long nextTick, moveAnimStart, moveAnimDuration, eatAnimStart, deathAnimStart;
    private float deathX, deathY;
    private final int[] previousX = new int[N*N], previousY = new int[N*N];
    private int previousCount;
    private final RectF boardRect = new RectF();
    private final RectF segmentRect = new RectF();

    public SnakeView(Context context) { super(context); }
    public static boolean supportsMode(int mode) { return mode == MODE; }
    @Override protected int gameMode() { return MODE; }

    @Override protected void resetGame() {
        snake.clear();
        snake.addFirst(new Cell(5,6)); snake.addLast(new Cell(4,6)); snake.addLast(new Cell(3,6));
        direction=queuedDirection=1; score=0; running=false; over=false; won=false; previousCount=0; moveAnimStart=0; eatAnimStart=0; deathAnimStart=0;
        placeFood();
    }

    @Override protected void drawGame(Canvas c) {
        update();
        drawHeader(c,"贪吃蛇", over ? (won?"通关 · ":"撞到了 · ")+score+" 分" : running ? "分数 "+score+" · 滑动转向" : "滑动任意方向开始");
        RectF available=gamePanel(roundScreen ? .155f : .20f);
        float size=Math.min(available.width(),available.height());
        float left=getWidth()/2f-size/2f,top=available.top+(available.height()-size)/2f,cell=size/N;
        boardRect.set(left,top,left+size,top+size); RectF board=boardRect;
        p.setColor(Color.rgb(18,29,35));c.drawRoundRect(board,cell*.55f,cell*.55f,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,cell*.03f));p.setColor(Color.rgb(35,50,56));
        for(int i=1;i<N;i++){c.drawLine(left+i*cell,top,left+i*cell,top+size,p);c.drawLine(left,top+i*cell,left+size,top+i*cell,p);}p.setStyle(Paint.Style.FILL);
        float pulse=.88f+.12f*(float)Math.sin(now()/135.0);
        p.setColor(Color.rgb(255,102,112));c.drawCircle(left+(foodX+.5f)*cell,top+(foodY+.5f)*cell,cell*.29f*pulse,p);
        float moveT=moveAnimStart==0?1f:clamp((now()-moveAnimStart)/(float)Math.max(1,moveAnimDuration),0f,1f);
        float eased=1f-(1f-moveT)*(1f-moveT);
        int i=0;for(Cell q:snake){float qx=q.x,qy=q.y;if(i<previousCount){qx=previousX[i]+(q.x-previousX[i])*eased;qy=previousY[i]+(q.y-previousY[i])*eased;}float inset=cell*(i==0 ? .11f : .16f);
            float eatScale=1f;if(i==0&&eatAnimStart>0){float et=clamp((now()-eatAnimStart)/260f,0f,1f);eatScale=1f-.18f*(float)Math.sin(et*Math.PI);if(et<1f)animateNext();else eatAnimStart=0;}
            p.setColor(i==0?Color.rgb(111,231,163):Color.rgb(70,197,130));segmentRect.set(left+qx*cell+inset,top+qy*cell+inset,left+(qx+1)*cell-inset,top+(qy+1)*cell-inset);
            if(eatScale<1f){float dx=segmentRect.width()*(1f-eatScale)/2f,dy=segmentRect.height()*(1f-eatScale)/2f;segmentRect.inset(dx,dy);}
            c.drawRoundRect(segmentRect,cell*.21f,cell*.21f,p);i++;}
        if(deathAnimStart>0){float dt=clamp((now()-deathAnimStart)/520f,0f,1f);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3)*(1f-dt));p.setColor(Color.argb((int)(220*(1f-dt)),255,118,127));c.drawCircle(left+(deathX+.5f)*cell,top+(deathY+.5f)*cell,cell*(.25f+1.6f*dt),p);p.setStyle(Paint.Style.FILL);if(dt<1f)animateNext();else deathAnimStart=0;}
        if(running&&!over){if(moveT<1f)animateNext();else invalidateSoon(Math.max(16,Math.min(80,nextTick-now())));}
    }

    private void update(){if(!running||over||startCountdownActive())return;long t=now();if(t<nextTick)return;direction=queuedDirection;advance();nextTick=t+Math.max(105,225-score*7L);}
    private void advance(){Cell head=snake.peekFirst();if(head==null)return;previousCount=0;for(Cell q:snake){previousX[previousCount]=q.x;previousY[previousCount]=q.y;previousCount++;}moveAnimStart=now();moveAnimDuration=(long)(Math.max(105,225-score*7L)*.78f);int nx=head.x,ny=head.y;if(direction==0)ny--;else if(direction==1)nx++;else if(direction==2)ny++;else nx--;boolean growing=nx==foodX&&ny==foodY;if(nx<0||nx>=N||ny<0||ny>=N||contains(nx,ny,!growing)){over=true;running=false;won=false;deathX=head.x;deathY=head.y;deathAnimStart=now();GameStats.recordMaxMetric(prefs,gameMode(),"length",snake.size());finishRound(-1,"撞到了","本局 "+score+" 分",score);return;}snake.addFirst(new Cell(nx,ny));if(growing){score++;eatAnimStart=now();showScorePopup("+1");GameStats.recordMaxMetric(prefs,gameMode(),"length",snake.size()+1);haptic(HapticFeedbackConstants.CLOCK_TICK);if(score>=12){over=true;running=false;won=true;finishRound(1,"贪吃蛇通关！","吃到 12 个食物",score);}else placeFood();}else snake.removeLast();}
    private boolean contains(int x,int y,boolean ignoreTail){int i=0,size=snake.size();for(Cell q:snake){if(ignoreTail&&i==size-1)break;if(q.x==x&&q.y==y)return true;i++;}return false;}
    private void placeFood(){Set<Integer> used=new HashSet<>();for(Cell q:snake)used.add(q.y*N+q.x);if(used.size()>=N*N){over=true;won=true;finishRound(1,"占满棋盘！","完美通关",score);return;}int v;do{v=(int)(Math.random()*N*N);}while(used.contains(v));foodX=v%N;foodY=v/N;}
    private void setDirection(int dir){if(((direction+2)&3)==dir&&snake.size()>1)return;queuedDirection=dir;if(!running&&!over){running=true;nextTick=now()+2780;beginStartCountdown();}haptic(HapticFeedbackConstants.CLOCK_TICK);invalidate();}
    @Override protected void onGameSwipe(float dx,float dy){int dir=Math.abs(dx)>Math.abs(dy)?(dx>0?1:3):(dy>0?2:0);setDirection(dir);}
    private static final class Cell{final int x,y;Cell(int x,int y){this.x=x;this.y=y;}}
}
