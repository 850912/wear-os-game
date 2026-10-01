package com.example.wearboardgames;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Lightweight home for short-session games added after v7.1.
 *
 * Keeping these games outside GameHubView prevents the legacy all-in-one view from growing
 * indefinitely. The class uses one shared renderer/input shell, event-driven invalidation for
 * static screens and VSYNC-aligned animation only for games that are currently moving.
 */
public final class MicroGameView extends View {
    public static final int PRECISION_TIMER = 41;
    public static final int LEFT_RIGHT = 42;
    public static final int STAR_CATCH = 43;
    public static final int METEOR_DODGE = 44;
    public static final int LUNAR_LANDER = 45;
    public static final int BUBBLE_POP = 46;
    public static final int RHYTHM_TAP = 47;
    public static final int SPIN_LOCK = 48;
    public static final int MEMORY_PATH = 49;
    public static final int NUMBER_SORT = 50;
    public static final int COLOR_STROOP = 51;
    public static final int ODD_EVEN = 52;

    private static final Typeface NORMAL = Typeface.create("sans", Typeface.NORMAL);
    private static final Typeface BOLD = Typeface.create("sans", Typeface.BOLD);
    private static final int BG = Color.rgb(7, 9, 13);
    private static final int SURFACE = Color.rgb(27, 31, 39);
    private static final int SURFACE_2 = Color.rgb(39, 45, 55);
    private static final int PRIMARY = Color.rgb(137, 210, 255);
    private static final int SECONDARY = Color.rgb(179, 157, 255);
    private static final int GOOD = Color.rgb(95, 218, 155);
    private static final int BAD = Color.rgb(255, 111, 126);
    private static final int TEXT = Color.rgb(242, 246, 250);
    private static final int MUTED = Color.rgb(167, 178, 191);

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final SharedPreferences prefs;
    private final float d;
    private final boolean roundScreen;
    private HostListener hostListener;
    private int mode = PRECISION_TIMER;
    private boolean roundRecorded;
    private long statsCheckpoint;

    // Shared result layer.
    private boolean resultVisible;
    private String resultTitle = "";
    private String resultSub = "";
    private int resultKind;
    private long resultShownAt;

    // Bottom controls require a deliberate second tap.
    private int pendingControl = -1; // 0 restart, 1 menu
    private long pendingControlUntil;
    private int pressedControl = -1;

    // Precision timer.
    private boolean precisionRunning, precisionDone;
    private long precisionStart, precisionElapsed;

    // Left/right reflex.
    private int lrDirection, lrScore, lrRounds;
    private boolean lrWaiting, lrOver;
    private long lrReadyAt, lrShownAt;

    // Star catch.
    private float catchBasketX, catchStarX, catchStarY, catchStarSpeed;
    private int catchScore, catchMiss;
    private long catchDeadline, catchLastTick;
    private boolean catchRunning, catchOver;

    // Meteor dodge.
    private float dodgeShipX, dodgeMeteorX, dodgeMeteorY, dodgeMeteorSpeed;
    private int dodgeScore;
    private long dodgeStart, dodgeLastTick;
    private boolean dodgeRunning, dodgeOver;

    // Lunar lander.
    private float landerX, landerY, landerVY;
    private long landerLastTick;
    private boolean landerRunning, landerThrust, landerOver, landerWon;

    // Bubble pop.
    private float bubbleX, bubbleY, bubbleR;
    private int bubbleScore, bubbleMiss;
    private long bubbleDeadline, bubbleExpireAt;
    private boolean bubbleOver;

    // Rhythm.
    private float rhythmPhase;
    private int rhythmBeat, rhythmScore;
    private long rhythmLastTick, rhythmHoldUntil;
    private boolean rhythmOver;

    // Spin lock.
    private float spinAngle, spinSpeed, spinTarget;
    private int spinRound, spinScore;
    private long spinLastTick;
    private boolean spinOver;

    // Memory path 3x3.
    private final int[] memoryPath = new int[8];
    private int memoryLength, memoryInput;
    private boolean memoryShowing, memoryOver;
    private long memoryShowStart;

    // Number sort.
    private final int[] sortValues = new int[3];
    private final boolean[] sortUsed = new boolean[3];
    private int sortStep, sortRounds, sortScore;
    private long sortDeadline;
    private boolean sortOver;

    // Stroop.
    private static final String[] COLOR_NAMES = {"红", "蓝", "绿", "黄"};
    private static final int[] COLOR_VALUES = {
            Color.rgb(255, 101, 116), Color.rgb(93, 171, 255),
            Color.rgb(83, 214, 145), Color.rgb(255, 204, 91)
    };
    private int stroopWord, stroopInk, stroopScore, stroopRounds;
    private long stroopDeadline;
    private boolean stroopOver;

    // Odd/even.
    private int oddEvenNumber, oddEvenScore, oddEvenRounds;
    private long oddEvenDeadline;
    private boolean oddEvenOver;

    public interface HostListener { void onExitToHub(); }

    public MicroGameView(Context context) {
        super(context);
        d = getResources().getDisplayMetrics().density;
        roundScreen = getResources().getConfiguration().isScreenRound();
        prefs = context.getSharedPreferences("wear_games", Context.MODE_PRIVATE);
        p.setTypeface(NORMAL);
        setBackgroundColor(BG);
        setFocusable(true);
        setHapticFeedbackEnabled(AppSettings.haptics(prefs));
    }

    public static boolean supportsMode(int mode) { return mode >= PRECISION_TIMER && mode <= ODD_EVEN; }
    public void setHostListener(HostListener listener) { hostListener = listener; }
    public void persistCurrentState() { long t=SystemClock.elapsedRealtime(); if(statsCheckpoint>0&&t>statsCheckpoint) GameStats.addPlayTime(prefs,mode,t-statsCheckpoint); statsCheckpoint=t; }
    public void resumeSavedGameExternal() { resetCurrent(); }

    public void openGame(int gameMode, boolean singlePlayer) {
        if (!supportsMode(gameMode)) throw new IllegalArgumentException("Unsupported micro game mode: " + gameMode);
        mode = gameMode;
        setHapticFeedbackEnabled(AppSettings.haptics(prefs));
        recordLaunch();
        statsCheckpoint=SystemClock.elapsedRealtime();
        resetCurrent();
    }

    private float dp(float v) { return v * d; }
    private float s() { return Math.min(getWidth(), getHeight()); }
    private float contentTop() { return s() * (roundScreen ? .155f : .13f); }
    private float contentBottom() { return getHeight() - dp(50); }
    private void animateNext() { if (isAttachedToWindow() && getWindowVisibility() == VISIBLE) { if(AppSettings.saver(prefs)) postInvalidateDelayed(33); else postInvalidateOnAnimation(); } }
    private void invalidateSoon(long delayMs) { if (isAttachedToWindow() && getWindowVisibility() == VISIBLE) postInvalidateDelayed(delayMs); }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        c.drawColor(BG);
        drawHeader(c);
        switch (mode) {
            case PRECISION_TIMER: drawPrecisionTimer(c); break;
            case LEFT_RIGHT: drawLeftRight(c); break;
            case STAR_CATCH: drawStarCatch(c); break;
            case METEOR_DODGE: drawMeteorDodge(c); break;
            case LUNAR_LANDER: drawLunarLander(c); break;
            case BUBBLE_POP: drawBubblePop(c); break;
            case RHYTHM_TAP: drawRhythmTap(c); break;
            case SPIN_LOCK: drawSpinLock(c); break;
            case MEMORY_PATH: drawMemoryPath(c); break;
            case NUMBER_SORT: drawNumberSort(c); break;
            case COLOR_STROOP: drawColorStroop(c); break;
            case ODD_EVEN: drawOddEven(c); break;
        }
        drawBottomControls(c);
        drawResult(c);
    }

    private void drawHeader(Canvas c) {
        text(c, titleFor(mode), getWidth()/2f, s()*.078f, s()*.055f, TEXT, true, Paint.Align.CENTER);
        String sub = subtitleFor(mode);
        if (!sub.isEmpty()) text(c, sub, getWidth()/2f, s()*.122f, s()*.030f, MUTED, false, Paint.Align.CENTER);
    }

    private String titleFor(int m) {
        switch (m) {
            case PRECISION_TIMER:return "精准计时"; case LEFT_RIGHT:return "左右反应";
            case STAR_CATCH:return "接星星"; case METEOR_DODGE:return "陨石闪避";
            case LUNAR_LANDER:return "月球着陆"; case BUBBLE_POP:return "泡泡连击";
            case RHYTHM_TAP:return "节奏点击"; case SPIN_LOCK:return "旋转锁";
            case MEMORY_PATH:return "路径记忆"; case NUMBER_SORT:return "数字排序";
            case COLOR_STROOP:return "颜色冲突"; default:return "奇偶快答";
        }
    }

    private String subtitleFor(int m) {
        switch (m) {
            case PRECISION_TIMER:return "目标 5.00 秒"; case LEFT_RIGHT:return "看箭头 · 点对应半屏";
            case STAR_CATCH:return "拖动篮子 · 20 秒"; case METEOR_DODGE:return "拖动飞船 · 活得更久";
            case LUNAR_LANDER:return "按住点火 · 轻柔着陆"; case BUBBLE_POP:return "泡泡消失前点中";
            case RHYTHM_TAP:return "圆环重合时点击"; case SPIN_LOCK:return "指针进入亮区时点击";
            case MEMORY_PATH:return "记住 3×3 路径"; case NUMBER_SORT:return "从小到大依次点击";
            case COLOR_STROOP:return "选文字真正的颜色"; default:return "快速判断奇数 / 偶数";
        }
    }

    private void drawPrecisionTimer(Canvas c) {
        long shown = precisionRunning ? SystemClock.elapsedRealtime() - precisionStart : precisionElapsed;
        String value = String.format(java.util.Locale.US, "%.2f", shown / 1000f);
        text(c, value, getWidth()/2f, s()*.44f, s()*.145f, precisionDone ? GOOD : PRIMARY, true, Paint.Align.CENTER);
        RectF r = centerButton(.60f, .25f, .13f);
        button(c, r, precisionRunning ? "停止" : (precisionDone ? "再来一次" : "开始"), PRIMARY, false);
        if (precisionRunning) animateNext();
    }

    private void drawLeftRight(Canvas c) {
        long now = SystemClock.elapsedRealtime();
        if (!lrOver && lrWaiting && now >= lrReadyAt) { lrWaiting=false; lrDirection=random.nextBoolean()?-1:1; lrShownAt=now; haptic(HapticFeedbackConstants.CLOCK_TICK); }
        if (!lrOver && !lrWaiting && lrShownAt>0 && now-lrShownAt>1300) { lrRounds++; if(lrRounds>=12){lrOver=true;finish(1,"反应完成",lrScore+" / 12",lrScore);}else nextLeftRight(); }
        text(c, "得分 " + lrScore + "  ·  " + lrRounds + "/12", getWidth()/2f, s()*.19f, s()*.038f, MUTED, false, Paint.Align.CENTER);
        String glyph = lrWaiting ? "…" : (lrDirection<0 ? "←" : "→");
        text(c, glyph, getWidth()/2f, s()*.47f, s()*.20f, lrWaiting?MUTED:PRIMARY, true, Paint.Align.CENTER);
        text(c, "左半屏", getWidth()*.27f, s()*.67f, s()*.033f, MUTED, false, Paint.Align.CENTER);
        text(c, "右半屏", getWidth()*.73f, s()*.67f, s()*.033f, MUTED, false, Paint.Align.CENTER);
        if (!lrOver) invalidateSoon(40);
    }

    private RectF arcadeBoard() {
        float side = s() * (roundScreen ? .67f : .72f);
        float top = contentTop() + s()*.015f;
        return new RectF((getWidth()-side)/2f, top, (getWidth()+side)/2f, Math.min(contentBottom()-dp(4), top+side));
    }

    private void drawStarCatch(Canvas c) {
        RectF b=arcadeBoard(); panel(c,b); long now=SystemClock.elapsedRealtime();
        if(!catchOver&&catchRunning){float dt=Math.min(.035f,(now-catchLastTick)/1000f);catchLastTick=now;catchStarY+=catchStarSpeed*dt;if(catchStarY>b.bottom){catchMiss++;spawnCatchStar(b);}float basketY=b.bottom-dp(13);if(catchStarY+b.width()*.035f>=basketY-dp(5)&&catchStarY<=basketY+dp(8)&&Math.abs(catchStarX-catchBasketX)<b.width()*.12f){catchScore++;haptic(HapticFeedbackConstants.CLOCK_TICK);spawnCatchStar(b);}if(now>=catchDeadline){catchOver=true;catchRunning=false;finish(1,"时间到","接到 "+catchScore+" 颗星",catchScore);}else animateNext();}
        p.setColor(Color.rgb(255,214,92));c.drawCircle(catchStarX,catchStarY,b.width()*.035f,p);
        p.setColor(PRIMARY);c.drawRoundRect(new RectF(catchBasketX-b.width()*.12f,b.bottom-dp(14),catchBasketX+b.width()*.12f,b.bottom-dp(5)),dp(6),dp(6),p);
        text(c,"★ "+catchScore+"   漏 "+catchMiss,getWidth()/2f,b.top+dp(18),s()*.034f,TEXT,true,Paint.Align.CENTER);
        if(!catchRunning&&!catchOver) text(c,"拖动开始",getWidth()/2f,b.centerY(),s()*.050f,MUTED,true,Paint.Align.CENTER);
    }

    private void drawMeteorDodge(Canvas c) {
        RectF b=arcadeBoard();panel(c,b);long now=SystemClock.elapsedRealtime();
        if(dodgeRunning&&!dodgeOver){float dt=Math.min(.035f,(now-dodgeLastTick)/1000f);dodgeLastTick=now;dodgeMeteorY+=dodgeMeteorSpeed*dt;if(dodgeMeteorY>b.bottom){dodgeScore++;spawnMeteor(b);}float shipY=b.bottom-dp(20),rr=b.width()*.042f;if(Math.abs(dodgeMeteorX-dodgeShipX)<b.width()*.085f&&Math.abs(dodgeMeteorY-shipY)<b.width()*.085f){dodgeOver=true;dodgeRunning=false;finish(-1,"被击中了","坚持 "+String.format(java.util.Locale.US,"%.1f",(now-dodgeStart)/1000f)+" 秒",dodgeScore);}else animateNext();}
        p.setColor(BAD);c.drawCircle(dodgeMeteorX,dodgeMeteorY,b.width()*.045f,p);
        p.setColor(PRIMARY);float sy=b.bottom-dp(18);android.graphics.Path ship=new android.graphics.Path();ship.moveTo(dodgeShipX,sy-b.width()*.055f);ship.lineTo(dodgeShipX-b.width()*.055f,sy+b.width()*.045f);ship.lineTo(dodgeShipX+b.width()*.055f,sy+b.width()*.045f);ship.close();c.drawPath(ship,p);
        text(c,"躲过 "+dodgeScore,getWidth()/2f,b.top+dp(18),s()*.034f,TEXT,true,Paint.Align.CENTER);
        if(!dodgeRunning&&!dodgeOver)text(c,"拖动开始",getWidth()/2f,b.centerY(),s()*.050f,MUTED,true,Paint.Align.CENTER);
    }

    private void drawLunarLander(Canvas c) {
        RectF b=arcadeBoard();panel(c,b);long now=SystemClock.elapsedRealtime();
        if(landerRunning&&!landerOver){float dt=Math.min(.032f,(now-landerLastTick)/1000f);landerLastTick=now;float gravity=b.height()*.42f;float thrust=b.height()*.72f;landerVY+=(gravity-(landerThrust?thrust:0))*dt;landerVY=Math.max(-b.height()*.38f,Math.min(b.height()*.55f,landerVY));landerY+=landerVY*dt;if(landerY>=b.bottom-dp(21)){landerY=b.bottom-dp(21);landerOver=true;landerRunning=false;landerWon=Math.abs(landerVY)<b.height()*.18f;if(landerWon)finish(1,"着陆成功",String.format(java.util.Locale.US,"速度 %.0f",Math.abs(landerVY)),1000-(int)(Math.abs(landerVY)*3));else finish(-1,"着陆过快","落地速度太高",0);}else animateNext();}
        p.setColor(Color.rgb(95,103,118));c.drawRect(b.left,b.bottom-dp(9),b.right,b.bottom,p);
        p.setColor(landerThrust?Color.rgb(255,180,76):PRIMARY);c.drawRoundRect(new RectF(landerX-dp(13),landerY-dp(10),landerX+dp(13),landerY+dp(10)),dp(5),dp(5),p);
        if(landerThrust&&!landerOver){p.setColor(Color.rgb(255,151,65));c.drawCircle(landerX,landerY+dp(16),dp(5),p);}
        text(c,"速度 "+(int)Math.abs(landerVY),getWidth()/2f,b.top+dp(18),s()*.034f,TEXT,true,Paint.Align.CENTER);
        if(!landerRunning&&!landerOver)text(c,"按住屏幕点火",getWidth()/2f,b.centerY(),s()*.045f,MUTED,true,Paint.Align.CENTER);
    }

    private void drawBubblePop(Canvas c) {
        RectF b=arcadeBoard();panel(c,b);long now=SystemClock.elapsedRealtime();
        if(!bubbleOver){if(now>=bubbleDeadline){bubbleOver=true;finish(1,"挑战结束","命中 "+bubbleScore+" · 漏 "+bubbleMiss,bubbleScore);}else if(now>=bubbleExpireAt){bubbleMiss++;spawnBubble(b);}}
        float pulse=1f+.08f*(float)Math.sin(now/90.0);p.setStyle(Paint.Style.FILL);p.setColor(PRIMARY);p.setAlpha(205);c.drawCircle(bubbleX,bubbleY,bubbleR*pulse,p);p.setAlpha(255);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));p.setColor(Color.WHITE);c.drawCircle(bubbleX,bubbleY,bubbleR*.55f*pulse,p);p.setStyle(Paint.Style.FILL);
        text(c,"命中 "+bubbleScore+"   漏 "+bubbleMiss,getWidth()/2f,b.top+dp(18),s()*.034f,TEXT,true,Paint.Align.CENTER);
        if(!bubbleOver)animateNext();
    }

    private void drawRhythmTap(Canvas c) {
        RectF b=arcadeBoard();panel(c,b);long now=SystemClock.elapsedRealtime();
        if(!rhythmOver){float dt=Math.min(.035f,(now-rhythmLastTick)/1000f);rhythmLastTick=now;if(now>=rhythmHoldUntil){rhythmPhase+=dt*.62f;if(rhythmPhase>1.18f){scoreRhythm(false);}}if(rhythmBeat>=10){rhythmOver=true;finish(1,"节奏完成","得分 "+rhythmScore,rhythmScore);}else animateNext();}
        float target=b.width()*.18f;float rr=b.width()*(.38f-.20f*Math.min(1f,rhythmPhase));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(5));p.setColor(SECONDARY);c.drawCircle(b.centerX(),b.centerY()+dp(6),target,p);p.setStrokeWidth(dp(3));p.setColor(PRIMARY);c.drawCircle(b.centerX(),b.centerY()+dp(6),rr,p);p.setStyle(Paint.Style.FILL);
        text(c,"节拍 "+Math.min(10,rhythmBeat+1)+" / 10   "+rhythmScore+" 分",getWidth()/2f,b.top+dp(18),s()*.032f,TEXT,true,Paint.Align.CENTER);
    }

    private void drawSpinLock(Canvas c) {
        RectF b=arcadeBoard();panel(c,b);long now=SystemClock.elapsedRealtime();
        if(!spinOver){float dt=Math.min(.035f,(now-spinLastTick)/1000f);spinLastTick=now;spinAngle=(spinAngle+spinSpeed*dt)%360f;if(spinRound>=8){spinOver=true;finish(1,"解锁完成","得分 "+spinScore,spinScore);}else animateNext();}
        float cx=b.centerX(),cy=b.centerY()+dp(5),r=b.width()*.28f;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(8));p.setColor(SURFACE_2);c.drawCircle(cx,cy,r,p);p.setColor(GOOD);RectF arc=new RectF(cx-r,cy-r,cx+r,cy+r);c.drawArc(arc,spinTarget-15,30,false,p);p.setStrokeWidth(dp(4));p.setColor(PRIMARY);double rad=Math.toRadians(spinAngle);c.drawLine(cx,cy,cx+(float)Math.cos(rad)*r,cy+(float)Math.sin(rad)*r,p);p.setStyle(Paint.Style.FILL);c.drawCircle(cx,cy,dp(7),p);
        text(c,"锁芯 "+Math.min(8,spinRound+1)+" / 8   "+spinScore+" 分",getWidth()/2f,b.top+dp(18),s()*.032f,TEXT,true,Paint.Align.CENTER);
    }

    private void drawMemoryPath(Canvas c) {
        RectF b=arcadeBoard();panel(c,b);long now=SystemClock.elapsedRealtime();float gap=dp(7),cell=(b.width()-gap*4)/3f;int flash=-1;
        if(memoryShowing&&!memoryOver){long rel=now-memoryShowStart;int slot=(int)(rel/520);if(slot<memoryLength){if(rel%520<310)flash=memoryPath[slot];invalidateSoon(35);}else{memoryShowing=false;memoryInput=0;haptic(HapticFeedbackConstants.CLOCK_TICK);invalidate();}}
        for(int i=0;i<9;i++){int row=i/3,col=i%3;RectF r=new RectF(b.left+gap+col*(cell+gap),b.top+dp(30)+row*(cell+gap),b.left+gap+col*(cell+gap)+cell,b.top+dp(30)+row*(cell+gap)+cell);p.setColor(i==flash?PRIMARY:SURFACE_2);c.drawRoundRect(r,dp(10),dp(10),p);}
        text(c,memoryShowing?"记住路径":"轮到你 · 长度 "+memoryLength,getWidth()/2f,b.top+dp(18),s()*.031f,memoryShowing?SECONDARY:TEXT,true,Paint.Align.CENTER);
    }

    private void drawNumberSort(Canvas c) {
        RectF b=arcadeBoard();panel(c,b);long now=SystemClock.elapsedRealtime();if(!sortOver&&now>=sortDeadline){sortOver=true;finish(1,"时间到","完成 "+sortScore+" 组",sortScore);}float w=b.width()*.24f,gap=b.width()*.045f,total=w*3+gap*2,left=b.centerX()-total/2f,y=b.centerY()-w/2f;
        for(int i=0;i<3;i++){RectF r=new RectF(left+i*(w+gap),y,left+i*(w+gap)+w,y+w);p.setColor(sortUsed[i]?Color.rgb(52,72,66):SURFACE_2);c.drawRoundRect(r,dp(12),dp(12),p);text(c,String.valueOf(sortValues[i]),r.centerX(),r.centerY()+s()*.025f,s()*.065f,sortUsed[i]?MUTED:TEXT,true,Paint.Align.CENTER);}
        text(c,"完成 "+sortScore+" 组",getWidth()/2f,b.top+dp(20),s()*.034f,TEXT,true,Paint.Align.CENTER);if(!sortOver)invalidateSoon(180);
    }

    private void drawColorStroop(Canvas c) {
        RectF b=arcadeBoard();panel(c,b);long now=SystemClock.elapsedRealtime();if(!stroopOver&&now>=stroopDeadline){stroopOver=true;finish(1,"挑战结束","答对 "+stroopScore+" / "+stroopRounds,stroopScore);}text(c,COLOR_NAMES[stroopWord],b.centerX(),b.top+b.height()*.31f,s()*.13f,COLOR_VALUES[stroopInk],true,Paint.Align.CENTER);float w=b.width()*.39f,h=b.height()*.16f;for(int i=0;i<4;i++){int row=i/2,col=i%2;RectF r=new RectF(b.left+b.width()*.08f+col*(w+b.width()*.06f),b.top+b.height()*.52f+row*(h+dp(6)),b.left+b.width()*.08f+col*(w+b.width()*.06f)+w,b.top+b.height()*.52f+row*(h+dp(6))+h);button(c,r,COLOR_NAMES[i],COLOR_VALUES[i],false);}if(!stroopOver)invalidateSoon(180);
    }

    private void drawOddEven(Canvas c) {
        RectF b=arcadeBoard();panel(c,b);long now=SystemClock.elapsedRealtime();if(!oddEvenOver&&now>=oddEvenDeadline){oddEvenOver=true;finish(1,"时间到","答对 "+oddEvenScore+" / "+oddEvenRounds,oddEvenScore);}text(c,String.valueOf(oddEvenNumber),b.centerX(),b.top+b.height()*.37f,s()*.16f,PRIMARY,true,Paint.Align.CENTER);float w=b.width()*.36f,h=b.height()*.17f;RectF left=new RectF(b.left+b.width()*.09f,b.top+b.height()*.63f,b.left+b.width()*.09f+w,b.top+b.height()*.63f+h);RectF right=new RectF(b.right-b.width()*.09f-w,left.top,b.right-b.width()*.09f,left.bottom);button(c,left,"奇数",SECONDARY,false);button(c,right,"偶数",GOOD,false);if(!oddEvenOver)invalidateSoon(180);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        float x=e.getX(),y=e.getY();
        if(resultVisible && e.getActionMasked()==MotionEvent.ACTION_UP){
            if(SystemClock.elapsedRealtime()-resultShownAt>220){resultVisible=false;resetCurrent();}
            return true;
        }
        int control=bottomControlAt(x,y);
        if(e.getActionMasked()==MotionEvent.ACTION_DOWN){
            pressedControl=control;
            boolean insideGameArea = y >= contentTop() && y < contentBottom();
            if(mode==LUNAR_LANDER&&control<0&&insideGameArea){if(!landerRunning&&!landerOver){landerRunning=true;landerLastTick=SystemClock.elapsedRealtime();}landerThrust=true;animateNext();}
            if((mode==STAR_CATCH||mode==METEOR_DODGE)&&control<0&&insideGameArea)handleDragGame(x);
            invalidate();return true;
        }
        if(e.getActionMasked()==MotionEvent.ACTION_MOVE){
            if((mode==STAR_CATCH||mode==METEOR_DODGE)&&y>=contentTop()&&y<contentBottom())handleDragGame(x);
            return true;
        }
        if(e.getActionMasked()==MotionEvent.ACTION_CANCEL){pressedControl=-1;if(mode==LUNAR_LANDER)landerThrust=false;invalidate();return true;}
        if(e.getActionMasked()!=MotionEvent.ACTION_UP)return true;
        if(mode==LUNAR_LANDER)landerThrust=false;
        if(control>=0&&control==pressedControl){pressedControl=-1;handleBottomControl(control);return true;}
        pressedControl=-1;
        if(y>=contentBottom())return true;
        switch(mode){
            case PRECISION_TIMER: handlePrecisionTap(x,y);break;
            case LEFT_RIGHT: handleLeftRightTap(x);break;
            case BUBBLE_POP: handleBubbleTap(x,y);break;
            case RHYTHM_TAP: scoreRhythm(true);break;
            case SPIN_LOCK: handleSpinTap();break;
            case MEMORY_PATH: handleMemoryTap(x,y);break;
            case NUMBER_SORT: handleSortTap(x,y);break;
            case COLOR_STROOP: handleStroopTap(x,y);break;
            case ODD_EVEN: handleOddEvenTap(x,y);break;
        }
        return true;
    }

    private void handlePrecisionTap(float x,float y){RectF r=centerButton(.60f,.25f,.13f);if(!r.contains(x,y))return;long now=SystemClock.elapsedRealtime();if(precisionRunning){precisionElapsed=now-precisionStart;precisionRunning=false;precisionDone=true;int error=(int)Math.abs(5000-precisionElapsed);int score=Math.max(0,1000-error);haptic(error<=100?HapticFeedbackConstants.CONFIRM:HapticFeedbackConstants.CLOCK_TICK);finish(1,"误差 "+error+" ms","越接近 5.00 秒越好",score);}else{precisionElapsed=0;precisionDone=false;precisionRunning=true;precisionStart=now;animateNext();}}

    private void handleLeftRightTap(float x){if(lrOver||lrWaiting||lrShownAt==0)return;boolean ok=(x<getWidth()/2f&&lrDirection<0)||(x>=getWidth()/2f&&lrDirection>0);lrRounds++;if(ok){lrScore++;haptic(HapticFeedbackConstants.CLOCK_TICK);}else haptic(HapticFeedbackConstants.REJECT);if(lrRounds>=12){lrOver=true;finish(1,"反应完成",lrScore+" / 12",lrScore);}else nextLeftRight();invalidate();}
    private void nextLeftRight(){lrWaiting=true;lrShownAt=0;lrReadyAt=SystemClock.elapsedRealtime()+250+random.nextInt(550);}

    private void handleDragGame(float x){RectF b=arcadeBoard();float clamped=Math.max(b.left+b.width()*.12f,Math.min(b.right-b.width()*.12f,x));if(mode==STAR_CATCH){catchBasketX=clamped;if(!catchRunning&&!catchOver){catchRunning=true;catchLastTick=SystemClock.elapsedRealtime();catchDeadline=catchLastTick+20000;spawnCatchStar(b);}animateNext();}else{dodgeShipX=clamped;if(!dodgeRunning&&!dodgeOver){dodgeRunning=true;dodgeLastTick=dodgeStart=SystemClock.elapsedRealtime();spawnMeteor(b);}animateNext();}invalidate();}
    private void spawnCatchStar(RectF b){catchStarX=b.left+b.width()*(.12f+random.nextFloat()*.76f);catchStarY=b.top+dp(26);catchStarSpeed=b.height()*(.34f+Math.min(.35f,catchScore*.012f));}
    private void spawnMeteor(RectF b){dodgeMeteorX=b.left+b.width()*(.10f+random.nextFloat()*.80f);dodgeMeteorY=b.top+dp(23);dodgeMeteorSpeed=b.height()*(.32f+Math.min(.45f,dodgeScore*.018f));}

    private void handleBubbleTap(float x,float y){if(bubbleOver)return;float dx=x-bubbleX,dy=y-bubbleY;if(dx*dx+dy*dy<=bubbleR*bubbleR*1.15f){bubbleScore++;haptic(HapticFeedbackConstants.CLOCK_TICK);spawnBubble(arcadeBoard());}else{bubbleMiss++;haptic(HapticFeedbackConstants.REJECT);}invalidate();}
    private void spawnBubble(RectF b){bubbleR=b.width()*(.06f+random.nextFloat()*.035f);bubbleX=b.left+bubbleR+random.nextFloat()*Math.max(1,b.width()-2*bubbleR);bubbleY=b.top+dp(28)+bubbleR+random.nextFloat()*Math.max(1,b.height()-dp(36)-2*bubbleR);bubbleExpireAt=SystemClock.elapsedRealtime()+Math.max(520,1050-bubbleScore*18);}

    private void scoreRhythm(boolean tapped){if(rhythmOver)return;float error=Math.abs(rhythmPhase-1f);if(tapped){int add=error<.07f?100:error<.14f?70:error<.23f?35:0;rhythmScore+=add;haptic(add>=70?HapticFeedbackConstants.CONFIRM:(add>0?HapticFeedbackConstants.CLOCK_TICK:HapticFeedbackConstants.REJECT));}rhythmBeat++;rhythmPhase=0;rhythmHoldUntil=SystemClock.elapsedRealtime()+180;if(rhythmBeat>=10){rhythmOver=true;finish(1,"节奏完成","得分 "+rhythmScore,rhythmScore);}invalidate();}

    private void handleSpinTap(){if(spinOver)return;float diff=angleDiff(spinAngle,spinTarget);int add=diff<=5?100:diff<=10?70:diff<=15?35:0;spinScore+=add;spinRound++;haptic(add>=70?HapticFeedbackConstants.CONFIRM:(add>0?HapticFeedbackConstants.CLOCK_TICK:HapticFeedbackConstants.REJECT));spinTarget=random.nextInt(360);spinSpeed=(random.nextBoolean()?1:-1)*(105+spinRound*12);if(spinRound>=8){spinOver=true;finish(1,"解锁完成","得分 "+spinScore,spinScore);}invalidate();}
    private float angleDiff(float a,float b){float d=Math.abs(a-b)%360f;return Math.min(d,360f-d);}

    private void handleMemoryTap(float x,float y){if(memoryOver||memoryShowing)return;RectF b=arcadeBoard();float gap=dp(7),cell=(b.width()-gap*4)/3f,top=b.top+dp(30);int col=(int)((x-(b.left+gap))/(cell+gap)),row=(int)((y-top)/(cell+gap));if(col<0||col>2||row<0||row>2)return;float lx=(x-(b.left+gap))-col*(cell+gap),ly=(y-top)-row*(cell+gap);if(lx<0||ly<0||lx>cell||ly>cell)return;int idx=row*3+col;if(idx!=memoryPath[memoryInput]){memoryOver=true;haptic(HapticFeedbackConstants.REJECT);finish(-1,"路径错了","记到长度 "+memoryLength,Math.max(0,memoryLength-3));return;}memoryInput++;haptic(HapticFeedbackConstants.CLOCK_TICK);if(memoryInput>=memoryLength){if(memoryLength>=8){memoryOver=true;finish(1,"全部记住！","完成 8 格路径",8);}else{memoryLength++;memoryPath[memoryLength-1]=nextMemoryCell();memoryShowing=true;memoryShowStart=SystemClock.elapsedRealtime()+420;}}invalidate();}
    private int nextMemoryCell(){int next;do{next=random.nextInt(9);}while(memoryLength>1&&next==memoryPath[memoryLength-2]);return next;}

    private void handleSortTap(float x,float y){if(sortOver)return;RectF b=arcadeBoard();float w=b.width()*.24f,gap=b.width()*.045f,total=w*3+gap*2,left=b.centerX()-total/2f,top=b.centerY()-w/2f;for(int i=0;i<3;i++){RectF r=new RectF(left+i*(w+gap),top,left+i*(w+gap)+w,top+w);if(r.contains(x,y)&&!sortUsed[i]){int min=Integer.MAX_VALUE,minIdx=-1;for(int j=0;j<3;j++)if(!sortUsed[j]&&sortValues[j]<min){min=sortValues[j];minIdx=j;}if(i==minIdx){sortUsed[i]=true;sortStep++;haptic(HapticFeedbackConstants.CLOCK_TICK);if(sortStep==3){sortScore++;sortRounds++;newSortRound();}}else{haptic(HapticFeedbackConstants.REJECT);sortRounds++;newSortRound();}invalidate();return;}}}
    private void newSortRound(){sortStep=0;Arrays.fill(sortUsed,false);List<Integer> values=new ArrayList<>();while(values.size()<3){int v=10+random.nextInt(90);if(!values.contains(v))values.add(v);}Collections.shuffle(values,random);for(int i=0;i<3;i++)sortValues[i]=values.get(i);}

    private void handleStroopTap(float x,float y){if(stroopOver)return;RectF b=arcadeBoard();float w=b.width()*.39f,h=b.height()*.16f;for(int i=0;i<4;i++){int row=i/2,col=i%2;RectF r=new RectF(b.left+b.width()*.08f+col*(w+b.width()*.06f),b.top+b.height()*.52f+row*(h+dp(6)),b.left+b.width()*.08f+col*(w+b.width()*.06f)+w,b.top+b.height()*.52f+row*(h+dp(6))+h);if(r.contains(x,y)){stroopRounds++;if(i==stroopInk){stroopScore++;haptic(HapticFeedbackConstants.CONFIRM);}else haptic(HapticFeedbackConstants.REJECT);newStroop();invalidate();return;}}}
    private void newStroop(){stroopWord=random.nextInt(4);do{stroopInk=random.nextInt(4);}while(random.nextBoolean()&&stroopInk==stroopWord);}

    private void handleOddEvenTap(float x,float y){if(oddEvenOver)return;RectF b=arcadeBoard();float w=b.width()*.36f,h=b.height()*.17f;RectF left=new RectF(b.left+b.width()*.09f,b.top+b.height()*.63f,b.left+b.width()*.09f+w,b.top+b.height()*.63f+h);RectF right=new RectF(b.right-b.width()*.09f-w,left.top,b.right-b.width()*.09f,left.bottom);boolean answeredOdd=left.contains(x,y),answeredEven=right.contains(x,y);if(!answeredOdd&&!answeredEven)return;oddEvenRounds++;boolean isOdd=(oddEvenNumber&1)==1;if((answeredOdd&&isOdd)||(answeredEven&&!isOdd)){oddEvenScore++;haptic(HapticFeedbackConstants.CONFIRM);}else haptic(HapticFeedbackConstants.REJECT);oddEvenNumber=1+random.nextInt(99);invalidate();}

    private void handleBottomControl(int which){long now=SystemClock.elapsedRealtime();if(pendingControl==which&&now<=pendingControlUntil){pendingControl=-1;if(which==0){resetCurrent();}else if(hostListener!=null){hostListener.onExitToHub();}return;}pendingControl=which;pendingControlUntil=now+1400;haptic(HapticFeedbackConstants.CLOCK_TICK);invalidate();}
    private int bottomControlAt(float x,float y){RectF[] r=bottomRects();if(r[0].contains(x,y))return 0;if(r[1].contains(x,y))return 1;return -1;}
    private RectF[] bottomRects(){float h=dp(42),bottom=getHeight()-dp(3),gap=dp(7),w=Math.min(s()*.34f,(getWidth()-gap*3)/2f),cx=getWidth()/2f;return new RectF[]{new RectF(cx-gap/2-w,bottom-h,cx-gap/2,bottom),new RectF(cx+gap/2,bottom-h,cx+gap/2+w,bottom)};}
    private void drawBottomControls(Canvas c){RectF[] rs=bottomRects();long now=SystemClock.elapsedRealtime();if(pendingControl>=0&&now>pendingControlUntil)pendingControl=-1;button(c,rs[0],pendingControl==0?"确认重开":"重开",pendingControl==0?BAD:SURFACE_2,pressedControl==0);button(c,rs[1],pendingControl==1?"确认返回":"菜单",pendingControl==1?BAD:SURFACE_2,pressedControl==1);if(pendingControl>=0&&now<=pendingControlUntil)invalidateSoon(180);}

    private void resetCurrent(){resultVisible=false;roundRecorded=false;pendingControl=-1;long now=SystemClock.elapsedRealtime();switch(mode){
        case PRECISION_TIMER:precisionRunning=false;precisionDone=false;precisionStart=0;precisionElapsed=0;break;
        case LEFT_RIGHT:lrScore=lrRounds=0;lrOver=false;nextLeftRight();break;
        case STAR_CATCH:{RectF b=arcadeBoard();catchScore=catchMiss=0;catchRunning=catchOver=false;catchBasketX=b.centerX();catchStarX=b.centerX();catchStarY=b.top+dp(28);catchStarSpeed=Math.max(90,b.height()*.34f);break;}
        case METEOR_DODGE:{RectF b=arcadeBoard();dodgeScore=0;dodgeRunning=dodgeOver=false;dodgeShipX=b.centerX();dodgeMeteorX=b.centerX();dodgeMeteorY=b.top+dp(25);dodgeMeteorSpeed=Math.max(95,b.height()*.33f);break;}
        case LUNAR_LANDER:{RectF b=arcadeBoard();landerX=b.centerX();landerY=b.top+b.height()*.21f;landerVY=0;landerRunning=landerThrust=landerOver=landerWon=false;landerLastTick=now;break;}
        case BUBBLE_POP:{bubbleScore=bubbleMiss=0;bubbleOver=false;bubbleDeadline=now+20000;spawnBubble(arcadeBoard());break;}
        case RHYTHM_TAP:rhythmPhase=0;rhythmBeat=rhythmScore=0;rhythmLastTick=now;rhythmHoldUntil=now+500;rhythmOver=false;break;
        case SPIN_LOCK:spinAngle=random.nextInt(360);spinTarget=random.nextInt(360);spinSpeed=105;spinRound=spinScore=0;spinLastTick=now;spinOver=false;break;
        case MEMORY_PATH:memoryLength=3;memoryInput=0;memoryOver=false;for(int i=0;i<memoryLength;i++){int next;do{next=random.nextInt(9);}while(i>0&&next==memoryPath[i-1]);memoryPath[i]=next;}memoryShowing=true;memoryShowStart=now+500;break;
        case NUMBER_SORT:sortStep=sortRounds=sortScore=0;sortDeadline=now+20000;sortOver=false;newSortRound();break;
        case COLOR_STROOP:stroopScore=stroopRounds=0;stroopDeadline=now+20000;stroopOver=false;newStroop();break;
        case ODD_EVEN:oddEvenScore=oddEvenRounds=0;oddEvenDeadline=now+20000;oddEvenOver=false;oddEvenNumber=1+random.nextInt(99);break;
    }invalidate();}

    private void finish(int kind,String title,String sub,int metric){resultKind=kind;resultTitle=title;resultSub=sub;resultVisible=true;resultShownAt=SystemClock.elapsedRealtime();if(!roundRecorded){roundRecorded=true;GameStats.recordResult(prefs,mode,kind,metric,false);}haptic(kind>0?HapticFeedbackConstants.CONFIRM:HapticFeedbackConstants.REJECT);SoundManager.play(prefs,kind>0?SoundManager.CLEAR:SoundManager.GAME_OVER);invalidate();}
    private void recordLaunch(){GameStats.recordLaunch(prefs,mode);}

    private boolean haptic(int constant){return HapticsManager.perform(this,prefs,constant);}

    private void drawResult(Canvas c){if(!resultVisible)return;p.setColor(Color.argb(205,0,0,0));c.drawRect(0,0,getWidth(),getHeight(),p);float w=s()*(roundScreen ? .74f : .82f),h=s()*.36f;RectF r=new RectF((getWidth()-w)/2f,(getHeight()-h)/2f,(getWidth()+w)/2f,(getHeight()+h)/2f);p.setColor(SURFACE);c.drawRoundRect(r,dp(22),dp(22),p);text(c,resultTitle,r.centerX(),r.top+h*.34f,s()*.060f,resultKind>0?GOOD:BAD,true,Paint.Align.CENTER);text(c,resultSub,r.centerX(),r.top+h*.57f,s()*.034f,TEXT,false,Paint.Align.CENTER);text(c,"轻点继续",r.centerX(),r.bottom-h*.14f,s()*.029f,MUTED,false,Paint.Align.CENTER);}
    private void panel(Canvas c,RectF r){p.setStyle(Paint.Style.FILL);p.setColor(SURFACE);c.drawRoundRect(r,dp(22),dp(22),p);}
    private RectF centerButton(float yFrac,float widthFrac,float heightFrac){float w=s()*widthFrac,h=s()*heightFrac,cx=getWidth()/2f,cy=s()*yFrac;return new RectF(cx-w/2f,cy-h/2f,cx+w/2f,cy+h/2f);}
    private void button(Canvas c,RectF r,String label,int color,boolean pressed){p.setStyle(Paint.Style.FILL);p.setColor(color);p.setAlpha(pressed?165:255);c.drawRoundRect(r,Math.min(r.height()/2f,dp(18)),Math.min(r.height()/2f,dp(18)),p);p.setAlpha(255);text(c,label,r.centerX(),r.centerY()+s()*.013f,Math.min(s()*.035f,r.height()*.38f),TEXT,true,Paint.Align.CENTER);}
    private void text(Canvas c,String str,float x,float y,float size,int color,boolean bold,Paint.Align align){p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);p.setTextAlign(align);p.setTypeface(bold?BOLD:NORMAL);c.drawText(str,x,y,p);}
}
