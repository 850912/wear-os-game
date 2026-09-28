package com.example.wearboardgames;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class GameHubView extends View {
    private static final int MENU = 0;
    private static final int XIANGQI = 1;
    private static final int GOMOKU = 2;
    private static final int GAME_2048 = 3;
    private static final int TAP_RUSH = 4;
    private static final int SNAKE = 5;
    private static final int SIMON = 6;
    private static final int TICTACTOE = 7;
    private static final int COLOR_HUNT = 8;
    private static final int BOUNCE = 9;
    private static final int SLIDE_PUZZLE = 10;
    private static final int LIGHTS_OUT = 11;
    private static final int MINI_MINES = 12;
    private static final int ROCK_PAPER_SCISSORS = 13;
    private static final int ABOUT = 14;
    private static final int DONATE = 15;
    private static final int FEEDBACK = 16;

    private static final String FEEDBACK_URL = "https://www.coolapk.com/u/22532694";
    private static final String[] MENU_TITLES = {
            "中国象棋", "五子棋", "2048", "反应点击", "贪吃蛇", "记忆闪烁",
            "井字棋", "色块猎手", "弹球挑战", "数字华容道", "熄灯解谜", "迷你扫雷", "猜拳挑战", "意见反馈", "关于"
    };
    private static final String[] MENU_SUBS = {
            "完整规则 · 双人对弈", "双人轮流 · 五连获胜", "经典滑动 · 合并动画",
            "30 秒手速挑战", "滑动转向 · 10 分通关", "记住颜色 · 8 轮通关",
            "你先手 · 对战手表", "找出不同色 · 12 关通关", "拖动挡板 · 连续反弹 12 次",
            "3×3 拼图 · 还原数字顺序", "4×4 灯阵 · 全部熄灭", "5×5 雷区 · 5 颗地雷", "三局两胜 · 对战手表",
            "在配对手机打开酷安主页", "作者 · 支持 · 开源说明"
    };
    private static final int[] MENU_MODES = {
            XIANGQI, GOMOKU, GAME_2048, TAP_RUSH, SNAKE, SIMON,
            TICTACTOE, COLOR_HUNT, BOUNCE, SLIDE_PUZZLE, LIGHTS_OUT, MINI_MINES, ROCK_PAPER_SCISSORS, FEEDBACK, ABOUT
    };

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float d;
    private final Random random = new Random();
    private final boolean roundScreen;
    private final SharedPreferences prefs;
    private final PhoneLinkOpener phoneLinkOpener;

    private int mode = MENU;
    private String message = "选择一个游戏开始";

    // Page / navigation motion. Back transitions enter from the left, forward transitions from the right.
    private long pageTransitionStart;
    private boolean pageBackTransition;
    private boolean edgeBackDragging;
    private float edgeBackOffset;

    // Shared result overlay for clear success / failure feedback.
    private long resultOverlayStart;
    private int resultOverlayKind; // 1 success, -1 failure, 2 neutral
    private String resultOverlayTitle = "";
    private String resultOverlaySub = "";
    private boolean resultOverlayAutoHide;

    // Temporary in-app hint, mainly for remote phone actions.
    private String toastHint = "";
    private long toastHintUntil;

    // Menu scrolling / press feedback.
    private float menuScroll;
    private float menuScrollAtDown;
    private float menuMaxScroll;
    private boolean menuDragging;
    private int pressedMenuIndex = -1;

    // Shared touch state.
    private float panX, panY, zoom = 1f;
    private float downX, downY, lastX, lastY;
    private boolean dragging, pinching, gestureStartedOnControl;
    private float pinchStart, zoomStart;

    // Chinese chess.
    private final XiangqiEngine xiangqi = new XiangqiEngine();
    private int selectedX = -1, selectedY = -1;
    private int pendingX = -1, pendingY = -1;
    private boolean xiangqiOver;
    private long xqMoveAnimStart;
    private int xqAnimSX = -1, xqAnimSY = -1, xqAnimTX = -1, xqAnimTY = -1;
    private char xqAnimPiece;

    // Gomoku.
    private final int[][] gomoku = new int[15][15];
    private int gomokuTurn = 1;
    private int gomokuPendingX = -1, gomokuPendingY = -1;
    private boolean gomokuOver;
    private int gomokuLastX = -1, gomokuLastY = -1;
    private long gomokuStoneAnimStart;

    // 2048.
    private final Game2048Engine game2048 = new Game2048Engine();
    private Game2048Engine.MoveFrame frame2048;
    private long anim2048Start;
    private boolean over2048;
    private int best2048;
    private boolean reached2048Shown;

    // Tap Rush.
    private float targetX, targetY;
    private int tapScore;
    private long tapStart;
    private boolean tapOver, tapResultShown;

    // Snake.
    private static final int SNAKE_N = 12;
    private final ArrayDeque<Cell> snake = new ArrayDeque<>();
    private int snakeDir = 1; // 0 up, 1 right, 2 down, 3 left
    private int snakeFoodX, snakeFoodY, snakeScore;
    private boolean snakeOver, snakeRunning, snakeWon;
    private long snakeNextTick;

    // Simon memory.
    private final List<Integer> simonSeq = new ArrayList<>();
    private boolean simonShowing, simonOver, simonWon;
    private int simonInputIndex;
    private long simonRoundStart;
    private int simonTapFlash = -1;
    private long simonTapFlashUntil;

    // Tic-tac-toe: player X versus a small local AI.
    private final int[] ttt = new int[9];
    private boolean tttOver;
    private int tttResult; // 1 player win, -1 AI win, 2 draw
    private int tttLast = -1;
    private long tttMarkAnimStart;
    private long tttAiDue;

    // Color Hunt.
    private int colorGridN, colorTarget, colorScore;
    private long colorDeadline;
    private boolean colorOver, colorWon, colorFlashGood;
    private int colorFlash = -1;
    private long colorFlashUntil;

    // Bounce challenge.
    private float bounceBallX, bounceBallY, bounceVx, bounceVy, bouncePaddleX;
    private int bounceHits;
    private boolean bounceRunning, bounceOver, bounceWon;
    private long bounceLastTick;

    // 3x3 slide puzzle. 0 is the empty tile.
    private final int[] slidePuzzle = new int[9];
    private int slideMoves;
    private boolean slideWon;

    // Lights Out 4x4.
    private final boolean[] lights = new boolean[16];
    private int lightsMoves;
    private boolean lightsWon;

    // Mini Mines 5x5 with five mines.
    private final boolean[] mines = new boolean[25];
    private final boolean[] mineOpen = new boolean[25];
    private boolean minesOver, minesWon;
    private int minesOpened;

    // Rock-paper-scissors, first to two points. 0 none, 1 rock, 2 paper, 3 scissors.
    private int rpsPlayer, rpsAi, rpsPlayerScore, rpsAiScore;
    private boolean rpsOver;

    // About / donation.
    private final Bitmap donateQr;

    public GameHubView(Context context) {
        super(context);
        d = getResources().getDisplayMetrics().density;
        roundScreen = getResources().getConfiguration().isScreenRound();
        prefs = context.getSharedPreferences("wear_games", Context.MODE_PRIVATE);
        best2048 = prefs.getInt("best_2048", 0);
        phoneLinkOpener = new PhoneLinkOpener(context);
        donateQr = BitmapFactory.decodeResource(getResources(), R.drawable.donate_qr);
        setBackgroundColor(Color.BLACK);
        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        setFocusable(true);
        setHapticFeedbackEnabled(true);
        resetAll();
    }

    private float dp(float v) { return v * d; }
    private float minSide() { return Math.min(getWidth(), getHeight()); }
    private float safeWidthFactor() { return roundScreen ? 0.70f : 0.90f; }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        long now = SystemClock.elapsedRealtime();
        float t = pageTransitionStart == 0 ? 1f : clamp((now - pageTransitionStart) / 230f, 0f, 1f);
        float eased = easeOutCubic(t);
        c.save();
        if (t < 1f) {
            float sign = pageBackTransition ? -1f : 1f;
            c.translate(sign * (1f - eased) * minSide() * .11f, 0);
            float scale = .955f + .045f * eased;
            c.scale(scale, scale, getWidth() / 2f, getHeight() / 2f);
        }
        if(edgeBackDragging)c.translate(edgeBackOffset,0);
        drawCurrentScreen(c);
        c.restore();
        if (t < 1f) postInvalidateOnAnimation();
        drawResultOverlay(c);
        drawToastHint(c);
    }

    private void drawCurrentScreen(Canvas c) {
        switch (mode) {
            case GAME_2048:
                c.drawColor(Color.rgb(250, 248, 239));
                draw2048(c);
                break;
            case ABOUT:
            case DONATE:
                c.drawColor(Color.rgb(9, 12, 17));
                if (mode == ABOUT) drawAbout(c); else drawDonate(c);
                break;
            default:
                c.drawColor(Color.rgb(7, 9, 12));
                switch (mode) {
                    case XIANGQI: drawXiangqi(c); break;
                    case GOMOKU: drawGomoku(c); break;
                    case TAP_RUSH: drawTapRush(c); break;
                    case SNAKE: drawSnake(c); break;
                    case SIMON: drawSimon(c); break;
                    case TICTACTOE: drawTicTacToe(c); break;
                    case COLOR_HUNT: drawColorHunt(c); break;
                    case BOUNCE: drawBounce(c); break;
                    case SLIDE_PUZZLE: drawSlidePuzzle(c); break;
                    case LIGHTS_OUT: drawLightsOut(c); break;
                    case MINI_MINES: drawMiniMines(c); break;
                    case ROCK_PAPER_SCISSORS: drawRockPaperScissors(c); break;
                    default: drawMenu(c);
                }
        }
    }

    // ---------- Menu ----------

    private void drawMenu(Canvas c) {
        float s = minSide();
        float w = getWidth(), h = getHeight();
        float titleY = roundScreen ? s * 0.105f : dp(34);
        text(c, "腕上小游戏", w / 2f, titleY, clamp(s * 0.054f, dp(20), dp(27)), Color.WHITE, true);
        text(c, "为小屏和旋钮手表优化", w / 2f, titleY + clamp(s * .045f, dp(18), dp(25)),
                clamp(s * .026f, dp(10), dp(13)), Color.rgb(157, 169, 184), false);

        float viewportTop = titleY + s * .105f;
        float viewportBottom = h - (roundScreen ? s * .15f : s * .04f);
        float cardH = s * .17f;
        float gap = s * .026f;
        float contentH = MENU_TITLES.length * cardH + (MENU_TITLES.length - 1) * gap;
        menuMaxScroll = Math.max(0, contentH - (viewportBottom - viewportTop));
        menuScroll = clamp(menuScroll, 0, menuMaxScroll);

        c.save();
        c.clipRect(0, viewportTop, w, viewportBottom);
        for (int i = 0; i < MENU_TITLES.length; i++) {
            float y = viewportTop + i * (cardH + gap) - menuScroll;
            if (y + cardH < viewportTop - cardH || y > viewportBottom + cardH) continue;
            RectF r = menuCardRect(y, cardH);
            float press = i == pressedMenuIndex && !menuDragging ? 0.96f : 1f;
            float edgeScale = 1f;
            if (roundScreen) {
                float center = (viewportTop + viewportBottom) * .5f;
                float half = Math.max(1f, (viewportBottom - viewportTop) * .5f);
                float dist = Math.min(1f, Math.abs(r.centerY() - center) / half);
                edgeScale = .88f + .12f * (1f - dist * dist);
            }
            c.save();
            c.scale(press * edgeScale, press * edgeScale, r.centerX(), r.centerY());
            p.setColor(i == pressedMenuIndex ? Color.rgb(41, 49, 61) : Color.rgb(27, 33, 42));
            c.drawRoundRect(r, cardH * .28f, cardH * .28f, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, s * .0025f));
            p.setColor(Color.rgb(58, 68, 82));
            c.drawRoundRect(r, cardH * .28f, cardH * .28f, p);
            p.setStyle(Paint.Style.FILL);

            float iconR = cardH * .24f;
            float iconX = r.left + cardH * .45f;
            float iconY = r.centerY();
            drawMenuIcon(c, MENU_MODES[i], iconX, iconY, iconR);

            float tx = r.left + cardH * .83f;
            textFit(c, MENU_TITLES[i], tx, y + cardH * .42f, cardH * .30f, Color.WHITE,
                    true, Paint.Align.LEFT, r.right - tx - cardH * .14f);
            textFit(c, MENU_SUBS[i], tx, y + cardH * .72f, cardH * .16f, Color.rgb(162, 172, 186),
                    false, Paint.Align.LEFT, r.right - tx - cardH * .14f);
            c.restore();
        }
        c.restore();

        if (menuMaxScroll > 0) {
            float trackTop = viewportTop + s * .02f;
            float trackBottom = viewportBottom - s * .02f;
            float thumbH = Math.max(s * .06f, (trackBottom - trackTop) * (viewportBottom - viewportTop) / contentH);
            float t = menuMaxScroll == 0 ? 0 : menuScroll / menuMaxScroll;
            float y = trackTop + (trackBottom - trackTop - thumbH) * t;
            p.setColor(Color.argb(110, 155, 166, 181));
            float x = w - (roundScreen ? s * .075f : dp(8));
            c.drawRoundRect(new RectF(x - s * .006f, y, x + s * .006f, y + thumbH), s * .006f, s * .006f, p);
        }
    }

    private RectF menuCardRect(float y, float cardH) {
        float cardW = Math.min(getWidth() * safeWidthFactor(), dp(310));
        return new RectF((getWidth() - cardW) / 2f, y, (getWidth() + cardW) / 2f, y + cardH);
    }

    private void drawMenuIcon(Canvas c, int menuMode, float x, float y, float r) {
        switch (menuMode) {
            case XIANGQI:
                p.setColor(Color.rgb(181, 45, 45)); c.drawCircle(x, y, r, p);
                text(c, "象", x, y + r * .34f, r * 1.05f, Color.WHITE, true); break;
            case GOMOKU:
                p.setColor(Color.rgb(214, 174, 110)); c.drawCircle(x, y, r, p);
                p.setColor(Color.rgb(26, 28, 31)); c.drawCircle(x - r * .28f, y - r * .12f, r * .34f, p);
                p.setColor(Color.WHITE); c.drawCircle(x + r * .28f, y + r * .13f, r * .34f, p); break;
            case GAME_2048:
                p.setColor(Color.rgb(237, 194, 46)); c.drawRoundRect(new RectF(x-r,y-r,x+r,y+r), r*.28f,r*.28f,p);
                text(c, "2048", x, y + r * .20f, r * .48f, Color.WHITE, true); break;
            case TAP_RUSH:
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(r * .23f); p.setColor(Color.rgb(255, 87, 92)); c.drawCircle(x,y,r*.72f,p);
                p.setStrokeWidth(r * .13f); p.setColor(Color.WHITE); c.drawCircle(x,y,r*.33f,p); p.setStyle(Paint.Style.FILL); break;
            case SNAKE:
                p.setColor(Color.rgb(55, 196, 117));
                for (int i=0;i<4;i++) c.drawCircle(x-r*.55f+i*r*.37f,y+(i%2==0?-r*.18f:r*.18f),r*.25f,p);
                p.setColor(Color.rgb(255, 94, 94)); c.drawCircle(x+r*.62f,y-r*.42f,r*.17f,p); break;
            case SIMON:
                int[] cols={Color.rgb(255,91,91),Color.rgb(76,196,107),Color.rgb(77,142,255),Color.rgb(255,199,65)};
                for(int q=0;q<4;q++){p.setColor(cols[q]);float ox=(q%2==0?-1:1)*r*.42f,oy=(q<2?-1:1)*r*.42f;c.drawCircle(x+ox,y+oy,r*.32f,p);} break;
            case TICTACTOE:
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(r*.18f); p.setColor(Color.rgb(94,174,255));
                c.drawLine(x-r*.55f,y-r*.55f,x-r*.05f,y-r*.05f,p);c.drawLine(x-r*.05f,y-r*.55f,x-r*.55f,y-r*.05f,p);
                p.setColor(Color.rgb(255,110,118));c.drawCircle(x+r*.35f,y+r*.30f,r*.32f,p);p.setStyle(Paint.Style.FILL); break;
            case COLOR_HUNT:
                p.setColor(Color.rgb(87,184,255));c.drawRoundRect(new RectF(x-r,y-r,x-r*.08f,y-r*.08f),r*.2f,r*.2f,p);
                p.setColor(Color.rgb(87,224,167));c.drawRoundRect(new RectF(x+r*.08f,y-r,x+r,y-r*.08f),r*.2f,r*.2f,p);
                p.setColor(Color.rgb(255,190,73));c.drawRoundRect(new RectF(x-r,y+r*.08f,x-r*.08f,y+r),r*.2f,r*.2f,p);
                p.setColor(Color.rgb(255,92,122));c.drawRoundRect(new RectF(x+r*.08f,y+r*.08f,x+r,y+r),r*.2f,r*.2f,p); break;
            case BOUNCE:
                p.setColor(Color.rgb(87,211,146));c.drawCircle(x,y-r*.25f,r*.27f,p);
                p.setColor(Color.WHITE);c.drawRoundRect(new RectF(x-r*.72f,y+r*.34f,x+r*.72f,y+r*.57f),r*.12f,r*.12f,p); break;
            case SLIDE_PUZZLE:
                for(int i=0;i<4;i++){float ox=(i%2==0?-1:1)*r*.42f,oy=(i<2?-1:1)*r*.42f;p.setColor(i==3?Color.rgb(34,39,48):Color.rgb(255,177,72));c.drawRoundRect(new RectF(x+ox-r*.30f,y+oy-r*.30f,x+ox+r*.30f,y+oy+r*.30f),r*.11f,r*.11f,p);} break;
            case LIGHTS_OUT:
                for(int i=0;i<4;i++){float ox=(i%2==0?-1:1)*r*.42f,oy=(i<2?-1:1)*r*.42f;p.setColor(i==0||i==3?Color.rgb(255,225,97):Color.rgb(59,68,82));c.drawCircle(x+ox,y+oy,r*.28f,p);} break;
            case MINI_MINES:
                p.setColor(Color.rgb(72,82,96));c.drawCircle(x,y,r*.55f,p);p.setColor(Color.rgb(255,91,91));c.drawCircle(x,y,r*.23f,p);for(int i=0;i<8;i++){double a=i*Math.PI/4;c.drawRect((float)(x+Math.cos(a)*r*.72-r*.06),(float)(y+Math.sin(a)*r*.72-r*.06),(float)(x+Math.cos(a)*r*.72+r*.06),(float)(y+Math.sin(a)*r*.72+r*.06),p);} break;
            case ROCK_PAPER_SCISSORS:
                text(c,"拳",x-r*.48f,y+r*.18f,r*.62f,Color.WHITE,true);text(c,"剪",x+r*.42f,y+r*.18f,r*.62f,Color.rgb(255,204,86),true); break;
            case FEEDBACK:
                p.setColor(Color.rgb(74,148,255));c.drawRoundRect(new RectF(x-r,y-r*.72f,x+r,y+r*.52f),r*.35f,r*.35f,p);
                p.setColor(Color.WHITE);for(int i=-1;i<=1;i++)c.drawCircle(x+i*r*.34f,y-r*.08f,r*.09f,p);
                p.setColor(Color.rgb(74,148,255));c.drawCircle(x-r*.45f,y+r*.58f,r*.18f,p); break;
            default:
                p.setColor(Color.rgb(101, 119, 255)); c.drawCircle(x,y,r,p); text(c,"i",x,y+r*.36f,r*1.15f,Color.WHITE,true);
        }
    }

    // ---------- Chinese chess ----------

    private void drawXiangqi(Canvas c) {
        drawGameHeader(c, "中国象棋", message, false);
        float cell = baseXiangqiCell() * zoom;
        float cx = getWidth()/2f + panX;
        float top = headerBottom() + minSide() * .035f + panY;
        float left = cx - 4f*cell;
        float bottom = top + 9f*cell;

        RectF bg = new RectF(left-cell*.52f, top-cell*.52f, left+8*cell+cell*.52f, bottom+cell*.52f);
        p.setColor(Color.rgb(219, 177, 111)); c.drawRoundRect(bg, cell*.18f, cell*.18f, p);
        p.setColor(Color.rgb(73, 49, 28)); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(1f, cell*.035f));
        for (int y=0;y<10;y++) c.drawLine(left, top+y*cell, left+8*cell, top+y*cell, p);
        for (int x=0;x<9;x++) {
            float xx=left+x*cell;
            c.drawLine(xx, top, xx, top+4*cell, p);
            c.drawLine(xx, top+5*cell, xx, bottom, p);
        }
        c.drawLine(left+3*cell, top, left+5*cell, top+2*cell, p);
        c.drawLine(left+5*cell, top, left+3*cell, top+2*cell, p);
        c.drawLine(left+3*cell, top+7*cell, left+5*cell, top+9*cell, p);
        c.drawLine(left+5*cell, top+7*cell, left+3*cell, top+9*cell, p);
        p.setStyle(Paint.Style.FILL);
        text(c, "楚 河", left+2*cell, top+4.65f*cell, cell*.42f, Color.rgb(83,52,27), true);
        text(c, "汉 界", left+6*cell, top+4.65f*cell, cell*.42f, Color.rgb(83,52,27), true);

        long now = SystemClock.elapsedRealtime();
        float moveT = clamp((now - xqMoveAnimStart) / 160f, 0, 1);
        boolean animating = xqAnimPiece != 0 && moveT < 1f;
        for (int y=0;y<10;y++) for (int x=0;x<9;x++) {
            char pc=xiangqi.pieceAt(x,y); if (pc==0) continue;
            if (animating && x == xqAnimTX && y == xqAnimTY) continue;
            drawXiangqiPiece(c, pc, left+x*cell, top+y*cell, cell*.40f);
        }
        if (animating) {
            float t = easeOutCubic(moveT);
            float px = left + lerp(xqAnimSX, xqAnimTX, t) * cell;
            float py = top + lerp(xqAnimSY, xqAnimTY, t) * cell;
            float pulse = 1f + .05f * (float)Math.sin(Math.PI * moveT);
            drawXiangqiPiece(c, xqAnimPiece, px, py, cell*.40f*pulse);
            postInvalidateOnAnimation();
        }

        if (selectedX>=0) ring(c,left+selectedX*cell,top+selectedY*cell,cell*.47f,Color.rgb(60,150,255));
        if (pendingX>=0) ring(c,left+pendingX*cell,top+pendingY*cell,cell*.48f,Color.rgb(255,202,64));

        if (pendingX>=0 && !xiangqiOver) drawConfirmRow(c, "确认移动", "取消");
        drawBottomBar(c, xiangqiOver ? new String[]{"菜单","重开"} : new String[]{"菜单","重开","撤销"}, false);
    }

    private float baseXiangqiCell() {
        float s=minSide();
        return Math.min(getWidth()*(roundScreen?.67f:.83f)/8.7f, (bottomBarTop()-headerBottom()-s*.045f)/9.5f);
    }

    private void drawXiangqiPiece(Canvas c,char pc,float px,float py,float radius){
        boolean red=Character.isUpperCase(pc);
        p.setColor(Color.rgb(247, 224, 172)); c.drawCircle(px,py,radius,p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(1.3f,radius*.12f));
        p.setColor(red?Color.rgb(180,35,34):Color.rgb(28,31,33)); c.drawCircle(px,py,radius,p);
        p.setStyle(Paint.Style.FILL);
        text(c, pieceName(pc), px, py + radius*.34f, radius*.94f, red?Color.rgb(190,35,35):Color.rgb(25,28,30), true);
    }

    private String pieceName(char pce) {
        switch (pce) {
            case 'K': return "帅"; case 'k': return "将";
            case 'A': return "仕"; case 'a': return "士";
            case 'E': return "相"; case 'e': return "象";
            case 'H': case 'h': return "马";
            case 'R': case 'r': return "车";
            case 'C': case 'c': return "炮";
            case 'P': return "兵"; case 'p': return "卒";
            default: return "";
        }
    }

    // ---------- Gomoku ----------

    private void drawGomoku(Canvas c) {
        drawGameHeader(c, "五子棋", message, false);
        float cell=baseGomokuCell()*zoom;
        float cx=getWidth()/2f+panX, top=headerBottom()+minSide()*.03f+panY, left=cx-7f*cell;
        RectF bg=new RectF(left-cell*.5f,top-cell*.5f,left+14*cell+cell*.5f,top+14*cell+cell*.5f);
        p.setColor(Color.rgb(216,174,105)); c.drawRoundRect(bg,cell*.20f,cell*.20f,p);
        p.setColor(Color.rgb(75,51,31)); p.setStrokeWidth(Math.max(1f,cell*.035f));
        for(int i=0;i<15;i++){
            c.drawLine(left,top+i*cell,left+14*cell,top+i*cell,p);
            c.drawLine(left+i*cell,top,left+i*cell,top+14*cell,p);
        }
        int[] stars={3,7,11};
        for(int sx:stars) for(int sy:stars) c.drawCircle(left+sx*cell,top+sy*cell,Math.max(1.7f,cell*.07f),p);
        long now=SystemClock.elapsedRealtime();
        for(int y=0;y<15;y++) for(int x=0;x<15;x++) if(gomoku[y][x]!=0){
            float scale=1f;
            if(x==gomokuLastX&&y==gomokuLastY){float t=clamp((now-gomokuStoneAnimStart)/150f,0,1);scale=overshoot(t);if(t<1)postInvalidateOnAnimation();}
            float r=cell*.40f*scale;
            p.setColor(gomoku[y][x]==1?Color.rgb(24,26,29):Color.rgb(242,242,238));
            c.drawCircle(left+x*cell,top+y*cell,r,p);
            if(gomoku[y][x]==2){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,cell*.025f));p.setColor(Color.rgb(120,120,120));c.drawCircle(left+x*cell,top+y*cell,r,p);p.setStyle(Paint.Style.FILL);}
        }
        if(gomokuPendingX>=0) ring(c,left+gomokuPendingX*cell,top+gomokuPendingY*cell,cell*.47f,Color.rgb(255,202,64));
        if(gomokuPendingX>=0 && !gomokuOver) drawConfirmRow(c,"确认落子","取消");
        drawBottomBar(c,new String[]{"菜单","重开"},false);
    }

    private float baseGomokuCell(){ return minSide()*(roundScreen?.040f:.045f); }

    // ---------- 2048 ----------

    private void draw2048(Canvas c) {
        float s=minSide(), w=getWidth();
        int dark=Color.rgb(119,110,101), cream=Color.rgb(250,248,239);
        text(c,"2048",w*.25f,s*.105f,s*.092f,dark,true);

        float badgeW=s*.20f,badgeH=s*.095f,badgeTop=s*.045f;
        drawScoreBadge(c,w*.58f-badgeW/2,badgeTop,badgeW,badgeH,"分数",String.valueOf(game2048.getScore()));
        drawScoreBadge(c,w*.80f-badgeW/2,badgeTop,badgeW,badgeH,"最佳",String.valueOf(best2048));

        String status=over2048?"没有可移动的位置":(reached2048Shown?"已合成 2048，继续挑战更高！":"上下左右滑动合并数字");
        textFit(c,status,w/2f,s*.195f,s*.027f,dark,false,Paint.Align.CENTER,w*(roundScreen?.67f:.88f));

        float size=s*(roundScreen?.485f:.56f);
        float top=s*.225f;
        float left=(w-size)/2f;
        float gap=size*.022f;
        float cell=(size-gap*5)/4f;
        float radius=cell*.08f;
        p.setColor(Color.rgb(187,173,160)); c.drawRoundRect(new RectF(left,top,left+size,top+size),size*.035f,size*.035f,p);
        for(int y=0;y<4;y++)for(int x=0;x<4;x++){
            RectF r=tileRect(left,top,gap,cell,x,y);
            p.setColor(Color.rgb(205,193,180)); c.drawRoundRect(r,radius,radius,p);
        }

        long now=SystemClock.elapsedRealtime();
        boolean animating=frame2048!=null&&frame2048.changed&&now-anim2048Start<260;
        if(animating){
            float elapsed=now-anim2048Start;
            if(elapsed<125){
                float t=easeOutCubic(clamp(elapsed/125f,0,1));
                for(Game2048Engine.TileMotion m:frame2048.motions){
                    float x=lerp(m.fromX,m.toX,t),y=lerp(m.fromY,m.toY,t);
                    draw2048Tile(c,m.value,left+gap+x*(cell+gap),top+gap+y*(cell+gap),cell,1f);
                }
            } else {
                float popT=clamp((elapsed-125f)/135f,0,1);
                for(int y=0;y<4;y++)for(int x=0;x<4;x++){
                    int v=game2048.valueAt(x,y);if(v==0)continue;
                    float scale=1f;
                    if(frame2048.merged[y][x]) scale=mergePop(popT);
                    if(frame2048.spawn!=null&&frame2048.spawn.x==x&&frame2048.spawn.y==y) scale=newTilePop(popT);
                    RectF r=tileRect(left,top,gap,cell,x,y);
                    draw2048Tile(c,v,r.left,r.top,cell,scale);
                }
            }
            postInvalidateOnAnimation();
        }else{
            frame2048=null;
            for(int y=0;y<4;y++)for(int x=0;x<4;x++){
                int v=game2048.valueAt(x,y);if(v==0)continue;
                RectF r=tileRect(left,top,gap,cell,x,y);
                draw2048Tile(c,v,r.left,r.top,cell,1f);
            }
        }

        drawBottomBar(c,new String[]{"菜单","重开"},true);
    }

    private void drawScoreBadge(Canvas c,float left,float top,float width,float height,String label,String value){
        p.setColor(Color.rgb(187,173,160));c.drawRoundRect(new RectF(left,top,left+width,top+height),height*.16f,height*.16f,p);
        text(c,label,left+width/2,top+height*.35f,height*.22f,Color.rgb(238,228,218),true);
        textFit(c,value,left+width/2,top+height*.75f,height*.31f,Color.WHITE,true,Paint.Align.CENTER,width*.82f);
    }

    private RectF tileRect(float left,float top,float gap,float cell,int x,int y){
        float l=left+gap+x*(cell+gap),t=top+gap+y*(cell+gap);return new RectF(l,t,l+cell,t+cell);
    }

    private void draw2048Tile(Canvas c,int v,float left,float top,float cell,float scale){
        float cx=left+cell/2,cy=top+cell/2;
        float side=cell*scale;
        RectF r=new RectF(cx-side/2,cy-side/2,cx+side/2,cy+side/2);
        p.setColor(tileColor(v));c.drawRoundRect(r,cell*.08f,cell*.08f,p);
        int digits=String.valueOf(v).length();
        float fs=cell*(digits<=2?.40f:digits==3?.34f:digits==4?.29f:.23f)*Math.min(1f,scale+.10f);
        int textColor=v<=4?Color.rgb(119,110,101):Color.rgb(249,246,242);
        text(c,String.valueOf(v),cx,cy+fs*.35f,fs,textColor,true);
    }

    private int tileColor(int v){
        switch(v){
            case 2:return Color.rgb(238,228,218); case 4:return Color.rgb(237,224,200);
            case 8:return Color.rgb(242,177,121); case 16:return Color.rgb(245,149,99);
            case 32:return Color.rgb(246,124,95); case 64:return Color.rgb(246,94,59);
            case 128:return Color.rgb(237,207,114); case 256:return Color.rgb(237,204,97);
            case 512:return Color.rgb(237,200,80); case 1024:return Color.rgb(237,197,63);
            case 2048:return Color.rgb(237,194,46); default:return Color.rgb(60,58,50);
        }
    }

    private float mergePop(float t){
        if(t<.55f)return lerp(.88f,1.13f,easeOutCubic(t/.55f));
        return lerp(1.13f,1f,easeOutCubic((t-.55f)/.45f));
    }
    private float newTilePop(float t){return .25f+.75f*overshoot(t);}

    // ---------- Tap Rush ----------

    private void drawTapRush(Canvas c) {
        long elapsed=SystemClock.elapsedRealtime()-tapStart;
        long remain=Math.max(0,30000-elapsed);
        if(remain==0 && !tapOver) {
            tapOver=true;
            if(!tapResultShown){tapResultShown=true;if(tapScore>=20)showResult(1,"手速达标！",tapScore+" 分 · 超过 20 分");else showResult(-1,"时间到",tapScore+" 分 · 20 分即可通关");}
        }
        drawGameHeader(c,"反应点击",tapOver?("结束 · "+tapScore+" 分"):("剩余 "+((remain+999)/1000)+" 秒 · "+tapScore+" 分"),false);
        if(!tapOver){
            float pulse=1f+.10f*(float)Math.sin(SystemClock.elapsedRealtime()/120.0);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(minSide()*.012f);p.setColor(Color.argb(110,255,86,91));c.drawCircle(targetX,targetY,minSide()*.075f*pulse,p);p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(255,86,91)); c.drawCircle(targetX,targetY,minSide()*.055f,p);
            p.setColor(Color.rgb(255,235,235)); c.drawCircle(targetX,targetY,minSide()*.023f,p);
            p.setColor(Color.rgb(255,86,91)); c.drawCircle(targetX,targetY,minSide()*.009f,p);
            postInvalidateDelayed(32);
        } else {
            text(c,"点“重开”再来一局",getWidth()/2f,getHeight()/2f,minSide()*.038f,Color.WHITE,true);
        }
        drawBottomBar(c,new String[]{"菜单","重开"},false);
    }

    // ---------- Snake ----------

    private void drawSnake(Canvas c){
        updateSnake();
        drawGameHeader(c,"贪吃蛇",snakeOver?(snakeWon?("通关 · "+snakeScore+" 分"):("撞到了 · "+snakeScore+" 分")):(snakeRunning?("分数 "+snakeScore+" / 10"):"滑动任意方向开始"),false);
        float s=minSide(),size=s*(roundScreen?.47f:.54f),left=(getWidth()-size)/2,top=s*.225f,cell=size/SNAKE_N;
        p.setColor(Color.rgb(20,29,35));c.drawRoundRect(new RectF(left,top,left+size,top+size),cell*.45f,cell*.45f,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,s*.0018f));p.setColor(Color.rgb(32,45,52));
        for(int i=1;i<SNAKE_N;i++){c.drawLine(left+i*cell,top,left+i*cell,top+size,p);c.drawLine(left,top+i*cell,left+size,top+i*cell,p);}p.setStyle(Paint.Style.FILL);
        float foodPulse=.88f+.12f*(float)Math.sin(SystemClock.elapsedRealtime()/130.0);
        p.setColor(Color.rgb(255,82,87));c.drawCircle(left+(snakeFoodX+.5f)*cell,top+(snakeFoodY+.5f)*cell,cell*.30f*foodPulse,p);
        int i=0;
        for(Cell q:snake){
            float inset=cell*(i==0?.13f:.17f);
            p.setColor(i==0?Color.rgb(92,231,146):Color.rgb(51,193,115));
            c.drawRoundRect(new RectF(left+q.x*cell+inset,top+q.y*cell+inset,left+(q.x+1)*cell-inset,top+(q.y+1)*cell-inset),cell*.20f,cell*.20f,p);i++;
        }
        if(!snakeOver)postInvalidateDelayed(32);
        drawBottomBar(c,new String[]{"菜单","重开"},false);
    }

    private void updateSnake(){
        if(!snakeRunning||snakeOver)return;
        long now=SystemClock.elapsedRealtime();
        if(now<snakeNextTick)return;
        advanceSnake();
        long interval=Math.max(95,210-snakeScore*6L);
        snakeNextTick=now+interval;
    }

    private void advanceSnake(){
        Cell head=snake.peekFirst();if(head==null)return;
        int nx=head.x,ny=head.y;
        if(snakeDir==0)ny--;else if(snakeDir==1)nx++;else if(snakeDir==2)ny++;else nx--;
        boolean growing=nx==snakeFoodX&&ny==snakeFoodY;
        if(nx<0||nx>=SNAKE_N||ny<0||ny>=SNAKE_N||snakeContains(nx,ny,!growing)){
            snakeOver=true;snakeRunning=false;snakeWon=false;showResult(-1,"撞到了",snakeScore+" 分 · 10 分即可通关");return;
        }
        snake.addFirst(new Cell(nx,ny));
        if(growing){snakeScore++;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);if(snakeScore>=10){snakeOver=true;snakeRunning=false;snakeWon=true;showResult(1,"贪吃蛇通关！","成功吃到 10 个食物");}else placeSnakeFood();}else snake.removeLast();
    }

    private boolean snakeContains(int x,int y,boolean ignoreTail){
        int i=0,size=snake.size();
        for(Cell q:snake){
            if(ignoreTail&&i==size-1) break;
            if(q.x==x&&q.y==y)return true;
            i++;
        }
        return false;
    }

    private void setSnakeDirection(int dir){
        if((snakeDir+2)%4==dir&&snake.size()>1)return;
        snakeDir=dir;
        if(!snakeRunning&&!snakeOver){snakeRunning=true;snakeNextTick=SystemClock.elapsedRealtime()+160;}
    }

    private void placeSnakeFood(){
        Set<Integer> used=new HashSet<>();for(Cell q:snake)used.add(q.y*SNAKE_N+q.x);
        List<Integer> free=new ArrayList<>();for(int i=0;i<SNAKE_N*SNAKE_N;i++)if(!used.contains(i))free.add(i);
        if(free.isEmpty()){snakeOver=true;return;}int v=free.get(random.nextInt(free.size()));snakeFoodX=v%SNAKE_N;snakeFoodY=v/SNAKE_N;
    }

    // ---------- Simon ----------

    private void drawSimon(Canvas c){
        updateSimon();
        String sub=simonOver?(simonWon?"挑战成功 · 8 轮":("记到第 "+simonSeq.size()+" 轮")):(simonShowing?"看清闪烁顺序":"轮到你 · 第 "+simonSeq.size()+" 轮");
        drawGameHeader(c,"记忆闪烁",sub,false);
        float s=minSide(),size=s*(roundScreen?.48f:.56f),gap=s*.025f,cell=(size-gap)/2,left=(getWidth()-size)/2,top=s*.23f;
        int[] base={Color.rgb(118,45,49),Color.rgb(35,98,62),Color.rgb(40,71,127),Color.rgb(118,93,34)};
        int[] bright={Color.rgb(255,83,88),Color.rgb(73,226,129),Color.rgb(75,145,255),Color.rgb(255,205,67)};
        int active=currentSimonFlash();
        for(int q=0;q<4;q++){
            int col=q%2,row=q/2;float l=left+col*(cell+gap),t=top+row*(cell+gap);
            p.setColor(q==active?bright[q]:base[q]);
            float shrink=q==active?-.012f*s:0;
            c.drawRoundRect(new RectF(l-shrink,t-shrink,l+cell+shrink,t+cell+shrink),cell*.18f,cell*.18f,p);
        }
        if(simonOver)text(c,simonWon?"完成！点“重开”可再次挑战":"顺序错了，点“重开”再试",getWidth()/2,top+size+s*.07f,s*.029f,Color.rgb(210,216,226),true);
        if(!simonOver)postInvalidateDelayed(32);
        drawBottomBar(c,new String[]{"菜单","重开"},false);
    }

    private int currentSimonFlash(){
        long now=SystemClock.elapsedRealtime();
        if(simonTapFlash>=0&&now<simonTapFlashUntil)return simonTapFlash;
        if(!simonShowing||now<simonRoundStart)return -1;
        long elapsed=now-simonRoundStart;int idx=(int)(elapsed/520);long phase=elapsed%520;
        if(idx>=simonSeq.size()||phase>315)return -1;return simonSeq.get(idx);
    }

    private void updateSimon(){
        if(!simonShowing||simonOver)return;
        long now=SystemClock.elapsedRealtime();
        if(now<simonRoundStart)return;
        long elapsed=now-simonRoundStart;
        if(elapsed>=simonSeq.size()*520L){simonShowing=false;simonInputIndex=0;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);}
    }

    private void handleSimonTap(float x,float y){
        if(simonShowing||simonOver)return;
        float s=minSide(),size=s*(roundScreen?.48f:.56f),gap=s*.025f,cell=(size-gap)/2,left=(getWidth()-size)/2,top=s*.23f;
        if(x<left||x>left+size||y<top||y>top+size)return;
        int col=x<left+cell?0:(x>left+cell+gap?1:-1);int row=y<top+cell?0:(y>top+cell+gap?1:-1);if(col<0||row<0)return;
        int q=row*2+col;simonTapFlash=q;simonTapFlashUntil=SystemClock.elapsedRealtime()+170;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        if(q!=simonSeq.get(simonInputIndex)){simonOver=true;simonWon=false;showResult(-1,"顺序错了","坚持到第 "+simonSeq.size()+" 轮");invalidate();return;}
        simonInputIndex++;
        if(simonInputIndex>=simonSeq.size()){
            if(simonSeq.size()>=8){simonOver=true;simonWon=true;showResult(1,"记忆通关！","连续完成 8 轮");}
            else{simonSeq.add(random.nextInt(4));simonShowing=true;simonRoundStart=SystemClock.elapsedRealtime()+650;simonInputIndex=0;}
        }
        invalidate();
    }

    // ---------- Tic-tac-toe ----------

    private void drawTicTacToe(Canvas c) {
        updateTicTacToeAi();
        float s = minSide(), w = getWidth();
        String sub = tttOver ? (tttResult == 1 ? "你赢了" : tttResult == -1 ? "手表获胜" : "平局")
                : (tttAiDue > 0 ? "手表思考中…" : "你是 X · 点格子落子");
        drawGameHeader(c, "井字棋", sub, false);
        float size = s * (roundScreen ? .49f : .57f);
        float left = (w - size) / 2f, top = s * .225f, cell = size / 3f;
        RectF bg = new RectF(left, top, left + size, top + size);
        p.setColor(Color.rgb(22, 29, 38)); c.drawRoundRect(bg, s * .035f, s * .035f, p);
        p.setColor(Color.rgb(77, 90, 108)); p.setStrokeWidth(Math.max(2f, s * .007f));
        for (int i = 1; i < 3; i++) {
            c.drawLine(left + i * cell, top + s * .025f, left + i * cell, top + size - s * .025f, p);
            c.drawLine(left + s * .025f, top + i * cell, left + size - s * .025f, top + i * cell, p);
        }
        long now = SystemClock.elapsedRealtime();
        if(tttAiDue>now)postInvalidateDelayed(Math.max(16,tttAiDue-now));
        for (int i = 0; i < 9; i++) {
            if (ttt[i] == 0) continue;
            int col = i % 3, row = i / 3;
            float cx = left + (col + .5f) * cell, cy = top + (row + .5f) * cell;
            float scale = 1f;
            if (i == tttLast) {
                float t = clamp((now - tttMarkAnimStart) / 180f, 0, 1);
                scale = overshoot(t);
                if (t < 1) postInvalidateOnAnimation();
            }
            float r = cell * .25f * scale;
            p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeWidth(cell * .095f);
            if (ttt[i] == 1) {
                p.setColor(Color.rgb(86, 169, 255));
                c.drawLine(cx - r, cy - r, cx + r, cy + r, p);
                c.drawLine(cx + r, cy - r, cx - r, cy + r, p);
            } else {
                p.setColor(Color.rgb(255, 103, 117)); c.drawCircle(cx, cy, r, p);
            }
            p.setStrokeCap(Paint.Cap.BUTT); p.setStyle(Paint.Style.FILL);
        }
        drawBottomBar(c, new String[]{"菜单", "重开"}, false);
    }

    private void handleTicTacToeTap(float x, float y) {
        if (tttOver || tttAiDue > 0) return;
        float s = minSide(), size = s * (roundScreen ? .49f : .57f), left = (getWidth() - size) / 2f, top = s * .225f;
        if (x < left || x >= left + size || y < top || y >= top + size) return;
        int col = Math.min(2, (int)((x - left) / (size / 3f)));
        int row = Math.min(2, (int)((y - top) / (size / 3f)));
        int idx = row * 3 + col;
        if (ttt[idx] != 0) return;
        placeTttMark(idx, 1);
        int winner = tttWinner();
        if (winner == 1) finishTtt(1);
        else if (tttFull()) finishTtt(2);
        else tttAiDue = SystemClock.elapsedRealtime() + 330;
        invalidate();
    }

    private void updateTicTacToeAi() {
        if (tttOver || tttAiDue <= 0 || SystemClock.elapsedRealtime() < tttAiDue) return;
        tttAiDue = 0;
        int idx = chooseTttAiMove();
        if (idx >= 0) placeTttMark(idx, 2);
        int winner = tttWinner();
        if (winner == 2) finishTtt(-1);
        else if (tttFull()) finishTtt(2);
        postInvalidateOnAnimation();
    }

    private void placeTttMark(int idx, int who) {
        ttt[idx] = who; tttLast = idx; tttMarkAnimStart = SystemClock.elapsedRealtime();
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
    }

    private int chooseTttAiMove() {
        for (int who : new int[]{2, 1}) for (int i = 0; i < 9; i++) if (ttt[i] == 0) {
            ttt[i] = who; boolean wins = tttWinner() == who; ttt[i] = 0; if (wins) return i;
        }
        if (ttt[4] == 0) return 4;
        int[] corners = {0, 2, 6, 8};
        List<Integer> free = new ArrayList<>();
        for (int i : corners) if (ttt[i] == 0) free.add(i);
        if (!free.isEmpty()) return free.get(random.nextInt(free.size()));
        free.clear(); for (int i = 0; i < 9; i++) if (ttt[i] == 0) free.add(i);
        return free.isEmpty() ? -1 : free.get(random.nextInt(free.size()));
    }

    private int tttWinner() {
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] l : lines) if (ttt[l[0]] != 0 && ttt[l[0]] == ttt[l[1]] && ttt[l[1]] == ttt[l[2]]) return ttt[l[0]];
        return 0;
    }

    private boolean tttFull() { for (int v : ttt) if (v == 0) return false; return true; }
    private void finishTtt(int result) {
        tttOver = true; tttResult = result; tttAiDue = 0;
        if (result == 1) showResult(1, "你赢了！", "井字棋挑战成功");
        else if (result == -1) showResult(-1, "这局输了", "点“重开”再挑战一次");
        else showResult(2, "平局", "旗鼓相当，再来一局");
    }

    // ---------- Color Hunt ----------

    private void drawColorHunt(Canvas c) {
        long now = SystemClock.elapsedRealtime();
        long remain = Math.max(0, colorDeadline - now);
        if (!colorOver && remain == 0) {
            colorOver = true; colorWon = false;
            showResult(-1, "时间到", "找到 " + colorScore + " / 12 个不同色块");
        }
        drawGameHeader(c, "色块猎手", colorOver ? (colorWon ? "挑战成功" : "挑战结束") : ("剩余 " + ((remain + 999) / 1000) + " 秒 · " + colorScore + "/12"), false);
        float s = minSide(), size = s * (roundScreen ? .50f : .58f), left = (getWidth() - size) / 2f, top = s * .22f;
        float gap = s * .012f, cell = (size - gap * (colorGridN - 1)) / colorGridN;
        float hue = (colorScore * 43f + 195f) % 360f;
        float[] hsv = {hue, .58f, .90f};
        int base = Color.HSVToColor(hsv);
        float diff = Math.max(.065f, .22f - colorScore * .011f);
        float[] oddHsv = {hue, .58f, Math.max(.45f, .90f - diff)};
        int odd = Color.HSVToColor(oddHsv);
        for (int i = 0; i < colorGridN * colorGridN; i++) {
            int col = i % colorGridN, row = i / colorGridN;
            float l = left + col * (cell + gap), t = top + row * (cell + gap);
            float scale = 1f;
            if (i == colorFlash && now < colorFlashUntil) scale = colorFlashGood ? 1.10f : .90f;
            float cx = l + cell / 2, cy = t + cell / 2, side = cell * scale;
            p.setColor(i == colorTarget ? odd : base);
            c.drawRoundRect(new RectF(cx - side/2, cy - side/2, cx + side/2, cy + side/2), cell * .18f, cell * .18f, p);
            if (i == colorFlash && now < colorFlashUntil && !colorFlashGood) {
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(s * .012f); p.setColor(Color.rgb(255, 90, 98));
                c.drawRoundRect(new RectF(l, t, l + cell, t + cell), cell * .18f, cell * .18f, p); p.setStyle(Paint.Style.FILL);
            }
        }
        if (!colorOver) postInvalidateDelayed(32);
        drawBottomBar(c, new String[]{"菜单", "重开"}, false);
    }

    private void handleColorHuntTap(float x, float y) {
        if (colorOver) return;
        float s = minSide(), size = s * (roundScreen ? .50f : .58f), left = (getWidth() - size) / 2f, top = s * .22f;
        if (x < left || x >= left + size || y < top || y >= top + size) return;
        float gap = s * .012f, cell = (size - gap * (colorGridN - 1)) / colorGridN;
        int col = (int)((x - left) / (cell + gap)), row = (int)((y - top) / (cell + gap));
        if (col < 0 || col >= colorGridN || row < 0 || row >= colorGridN) return;
        float inX = (x - left) - col * (cell + gap), inY = (y - top) - row * (cell + gap);
        if (inX > cell || inY > cell) return;
        int idx = row * colorGridN + col;
        colorFlash = idx; colorFlashUntil = SystemClock.elapsedRealtime() + 180;
        if (idx == colorTarget) {
            colorFlashGood = true; colorScore++; performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            if (colorScore >= 12) {
                colorOver = true; colorWon = true; showResult(1, "眼力通关！", "连续找出 12 个不同色块");
            } else {
                colorGridN = Math.min(5, 2 + colorScore / 3);
                colorTarget = random.nextInt(colorGridN * colorGridN);
                colorDeadline += 1100;
            }
        } else {
            colorFlashGood = false; colorDeadline = Math.max(SystemClock.elapsedRealtime() + 500, colorDeadline - 2200);
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        invalidate();
    }

    // ---------- Bounce challenge ----------

    private RectF bounceBoardRect() {
        float s = minSide(), sizeW = getWidth() * (roundScreen ? .68f : .84f), top = s * .19f, bottom = bottomBarTop() - s * .04f;
        return new RectF((getWidth() - sizeW) / 2f, top, (getWidth() + sizeW) / 2f, bottom);
    }

    private void drawBounce(Canvas c) {
        float s = minSide(); RectF board = bounceBoardRect();
        if (bounceLastTick == 0) initBounceGeometry(board);
        updateBounce(board);
        String sub = bounceOver ? (bounceWon ? "挑战成功 · 12 次" : "漏接了 · " + bounceHits + " 次") : (bounceRunning ? "连续反弹 " + bounceHits + "/12" : "触摸挡板开始");
        drawGameHeader(c, "弹球挑战", sub, false);
        p.setColor(Color.rgb(18, 27, 36)); c.drawRoundRect(board, s * .035f, s * .035f, p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(s * .005f); p.setColor(Color.rgb(42, 58, 72)); c.drawRoundRect(board, s * .035f, s * .035f, p); p.setStyle(Paint.Style.FILL);
        float paddleW = board.width() * .34f, paddleH = s * .035f, py = board.bottom - s * .055f;
        RectF paddle = new RectF(bouncePaddleX - paddleW/2, py - paddleH/2, bouncePaddleX + paddleW/2, py + paddleH/2);
        p.setColor(Color.rgb(80, 217, 150)); c.drawRoundRect(paddle, paddleH/2, paddleH/2, p);
        float pulse = 1f + .06f * (float)Math.sin(SystemClock.elapsedRealtime() / 110.0);
        p.setColor(Color.rgb(255, 211, 72)); c.drawCircle(bounceBallX, bounceBallY, s * .025f * pulse, p);
        p.setColor(Color.argb(90, 255, 211, 72)); c.drawCircle(bounceBallX, bounceBallY, s * .043f * pulse, p);
        if (!bounceOver) postInvalidateOnAnimation();
        drawBottomBar(c, new String[]{"菜单", "重开"}, false);
    }

    private void initBounceGeometry(RectF board) {
        float s = minSide();
        bouncePaddleX = board.centerX(); bounceBallX = board.centerX(); bounceBallY = board.top + board.height() * .34f;
        bounceVx = s * .36f; bounceVy = s * .42f; bounceLastTick = SystemClock.elapsedRealtime();
    }

    private void updateBounce(RectF board) {
        if (!bounceRunning || bounceOver) { bounceLastTick = SystemClock.elapsedRealtime(); return; }
        long now = SystemClock.elapsedRealtime(); float dt = Math.min(.035f, Math.max(.001f, (now - bounceLastTick) / 1000f)); bounceLastTick = now;
        float s = minSide(), r = s * .025f, paddleW = board.width() * .34f, paddleH = s * .035f, py = board.bottom - s * .055f;
        bounceBallX += bounceVx * dt; bounceBallY += bounceVy * dt;
        if (bounceBallX - r < board.left) { bounceBallX = board.left + r; bounceVx = Math.abs(bounceVx); }
        if (bounceBallX + r > board.right) { bounceBallX = board.right - r; bounceVx = -Math.abs(bounceVx); }
        if (bounceBallY - r < board.top) { bounceBallY = board.top + r; bounceVy = Math.abs(bounceVy); }
        if (bounceVy > 0 && bounceBallY + r >= py - paddleH/2 && bounceBallY - r <= py + paddleH/2 &&
                bounceBallX >= bouncePaddleX - paddleW/2 - r && bounceBallX <= bouncePaddleX + paddleW/2 + r) {
            bounceBallY = py - paddleH/2 - r; bounceVy = -Math.abs(bounceVy) * 1.025f;
            float offset = (bounceBallX - bouncePaddleX) / (paddleW/2); bounceVx += offset * s * .12f;
            bounceHits++; performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            if (bounceHits >= 12) { bounceOver = true; bounceWon = true; bounceRunning = false; showResult(1, "弹球通关！", "连续接住 12 次"); }
        }
        if (!bounceOver && bounceBallY - r > board.bottom) {
            bounceOver = true; bounceWon = false; bounceRunning = false; showResult(-1, "漏接了", "本局连续反弹 " + bounceHits + " 次");
        }
    }

    private void handleBounceTouch(float x) {
        if (bounceOver) return;
        RectF board = bounceBoardRect(); float paddleW = board.width() * .34f;
        bouncePaddleX = clamp(x, board.left + paddleW/2, board.right - paddleW/2);
        if (!bounceRunning) { bounceRunning = true; bounceLastTick = SystemClock.elapsedRealtime(); }
        invalidate();
    }

    // ---------- Slide puzzle ----------

    private void drawSlidePuzzle(Canvas c){
        float s=minSide(),size=s*(roundScreen?.52f:.60f),left=(getWidth()-size)/2f,top=s*.215f,gap=s*.012f,cell=(size-gap*2)/3f;
        drawGameHeader(c,"数字华容道",slideWon?"完成 · "+slideMoves+" 步":"点击空格旁的数字 · "+slideMoves+" 步",false);
        p.setColor(Color.rgb(20,27,36));c.drawRoundRect(new RectF(left,top,left+size,top+size),s*.035f,s*.035f,p);
        for(int i=0;i<9;i++){int v=slidePuzzle[i];int col=i%3,row=i/3;float l=left+col*(cell+gap),t=top+row*(cell+gap);if(v==0){p.setColor(Color.rgb(12,16,22));c.drawRoundRect(new RectF(l,t,l+cell,t+cell),cell*.18f,cell*.18f,p);continue;}p.setColor(Color.rgb(255,174,66));c.drawRoundRect(new RectF(l,t,l+cell,t+cell),cell*.18f,cell*.18f,p);text(c,String.valueOf(v),l+cell/2,t+cell*.62f,cell*.38f,Color.rgb(26,31,38),true);}
        drawBottomBar(c,new String[]{"菜单","重开"},false);
    }

    private void handleSlidePuzzleTap(float x,float y){
        if(slideWon)return;float s=minSide(),size=s*(roundScreen?.52f:.60f),left=(getWidth()-size)/2f,top=s*.215f,gap=s*.012f,cell=(size-gap*2)/3f;if(x<left||x>=left+size||y<top||y>=top+size)return;int col=(int)((x-left)/(cell+gap)),row=(int)((y-top)/(cell+gap));if(col>2||row>2)return;float ix=(x-left)-col*(cell+gap),iy=(y-top)-row*(cell+gap);if(ix>cell||iy>cell)return;int idx=row*3+col,z=findZero(slidePuzzle);if(Math.abs(idx/3-z/3)+Math.abs(idx%3-z%3)!=1)return;slidePuzzle[z]=slidePuzzle[idx];slidePuzzle[idx]=0;slideMoves++;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);if(isSlideSolved()){slideWon=true;showResult(1,"拼图完成！","用了 "+slideMoves+" 步");}invalidate();
    }
    private int findZero(int[] a){for(int i=0;i<a.length;i++)if(a[i]==0)return i;return -1;}
    private boolean isSlideSolved(){for(int i=0;i<8;i++)if(slidePuzzle[i]!=i+1)return false;return slidePuzzle[8]==0;}

    // ---------- Lights Out ----------

    private void drawLightsOut(Canvas c){
        float s=minSide(),size=s*(roundScreen?.50f:.58f),left=(getWidth()-size)/2f,top=s*.22f,gap=s*.012f,cell=(size-gap*3)/4f;
        drawGameHeader(c,"熄灯解谜",lightsWon?"全部熄灭 · "+lightsMoves+" 步":"点亮格会连动上下左右 · "+lightsMoves+" 步",false);
        for(int i=0;i<16;i++){int col=i%4,row=i/4;float l=left+col*(cell+gap),t=top+row*(cell+gap);p.setColor(lights[i]?Color.rgb(255,218,91):Color.rgb(41,50,63));c.drawRoundRect(new RectF(l,t,l+cell,t+cell),cell*.18f,cell*.18f,p);if(lights[i]){p.setColor(Color.argb(80,255,226,116));c.drawCircle(l+cell/2,t+cell/2,cell*.44f,p);}}
        drawBottomBar(c,new String[]{"菜单","重开"},false);
    }
    private void handleLightsOutTap(float x,float y){if(lightsWon)return;float s=minSide(),size=s*(roundScreen?.50f:.58f),left=(getWidth()-size)/2f,top=s*.22f,gap=s*.012f,cell=(size-gap*3)/4f;if(x<left||x>=left+size||y<top||y>=top+size)return;int col=(int)((x-left)/(cell+gap)),row=(int)((y-top)/(cell+gap));if(col>3||row>3)return;float ix=(x-left)-col*(cell+gap),iy=(y-top)-row*(cell+gap);if(ix>cell||iy>cell)return;toggleLights(row*4+col,true);lightsMoves++;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);if(allLightsOff()){lightsWon=true;showResult(1,"全部熄灭！","用了 "+lightsMoves+" 步");}invalidate();}
    private void toggleLights(int idx,boolean feedback){int r=idx/4,c=idx%4;int[][] d={{0,0},{1,0},{-1,0},{0,1},{0,-1}};for(int[] q:d){int rr=r+q[1],cc=c+q[0];if(rr>=0&&rr<4&&cc>=0&&cc<4)lights[rr*4+cc]=!lights[rr*4+cc];}}
    private boolean allLightsOff(){for(boolean b:lights)if(b)return false;return true;}

    // ---------- Mini mines ----------

    private void drawMiniMines(Canvas c){
        float s=minSide(),size=s*(roundScreen?.52f:.60f),left=(getWidth()-size)/2f,top=s*.215f,gap=s*.008f,cell=(size-gap*4)/5f;
        String sub=minesOver?(minesWon?"排雷成功":"踩到地雷"):("安全格 "+minesOpened+"/20");drawGameHeader(c,"迷你扫雷",sub,false);
        for(int i=0;i<25;i++){int col=i%5,row=i/5;float l=left+col*(cell+gap),t=top+row*(cell+gap);if(!mineOpen[i]){p.setColor(Color.rgb(62,74,90));c.drawRoundRect(new RectF(l,t,l+cell,t+cell),cell*.14f,cell*.14f,p);continue;}if(mines[i]){p.setColor(Color.rgb(255,91,101));c.drawRoundRect(new RectF(l,t,l+cell,t+cell),cell*.14f,cell*.14f,p);text(c,"雷",l+cell/2,t+cell*.66f,cell*.38f,Color.WHITE,true);}else{p.setColor(Color.rgb(31,39,50));c.drawRoundRect(new RectF(l,t,l+cell,t+cell),cell*.14f,cell*.14f,p);int n=countNeighborMines(i);if(n>0)text(c,String.valueOf(n),l+cell/2,t+cell*.66f,cell*.42f,mineNumberColor(n),true);}}
        drawBottomBar(c,new String[]{"菜单","重开"},false);
    }
    private void handleMiniMinesTap(float x,float y){if(minesOver)return;float s=minSide(),size=s*(roundScreen?.52f:.60f),left=(getWidth()-size)/2f,top=s*.215f,gap=s*.008f,cell=(size-gap*4)/5f;if(x<left||x>=left+size||y<top||y>=top+size)return;int col=(int)((x-left)/(cell+gap)),row=(int)((y-top)/(cell+gap));if(col>4||row>4)return;float ix=(x-left)-col*(cell+gap),iy=(y-top)-row*(cell+gap);if(ix>cell||iy>cell)return;int idx=row*5+col;if(mineOpen[idx])return;mineOpen[idx]=true;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);if(mines[idx]){minesOver=true;minesWon=false;for(int i=0;i<25;i++)if(mines[i])mineOpen[i]=true;showResult(-1,"踩到地雷","点“重开”重新布雷");}else{minesOpened++;if(countNeighborMines(idx)==0)openMineZeros(idx);if(minesOpened>=20){minesOver=true;minesWon=true;showResult(1,"排雷成功！","找出了所有安全格");}}invalidate();}
    private int countNeighborMines(int idx){int r=idx/5,c=idx%5,n=0;for(int dr=-1;dr<=1;dr++)for(int dc=-1;dc<=1;dc++){if(dr==0&&dc==0)continue;int rr=r+dr,cc=c+dc;if(rr>=0&&rr<5&&cc>=0&&cc<5&&mines[rr*5+cc])n++;}return n;}
    private void openMineZeros(int start){ArrayDeque<Integer> q=new ArrayDeque<>();q.add(start);while(!q.isEmpty()){int idx=q.removeFirst(),r=idx/5,c=idx%5;if(countNeighborMines(idx)!=0)continue;for(int dr=-1;dr<=1;dr++)for(int dc=-1;dc<=1;dc++){int rr=r+dr,cc=c+dc;if(rr<0||rr>=5||cc<0||cc>=5)continue;int j=rr*5+cc;if(!mines[j]&&!mineOpen[j]){mineOpen[j]=true;minesOpened++;if(countNeighborMines(j)==0)q.add(j);}}}}
    private int mineNumberColor(int n){int[] cs={Color.WHITE,Color.rgb(86,169,255),Color.rgb(82,211,145),Color.rgb(255,191,80),Color.rgb(255,103,117),Color.rgb(196,132,255),Color.rgb(103,220,225),Color.LTGRAY,Color.WHITE};return cs[Math.min(n,8)];}

    // ---------- Rock paper scissors ----------

    private void drawRockPaperScissors(Canvas c){
        float s=minSide(),w=getWidth();String sub=rpsOver?(rpsPlayerScore>rpsAiScore?"你赢下本轮":"手表赢下本轮"):("比分 "+rpsPlayerScore+" : "+rpsAiScore+" · 三局两胜");drawGameHeader(c,"猜拳挑战",sub,false);
        float top=s*.24f;if(rpsPlayer>0){text(c,rpsEmoji(rpsPlayer),w*.34f,top+s*.10f,s*.10f,Color.WHITE,true);text(c,rpsEmoji(rpsAi),w*.66f,top+s*.10f,s*.10f,Color.WHITE,true);text(c,"你",w*.34f,top+s*.16f,s*.026f,Color.rgb(165,177,193),false);text(c,"手表",w*.66f,top+s*.16f,s*.026f,Color.rgb(165,177,193),false);}else{text(c,"选择出拳",w/2,top+s*.10f,s*.046f,Color.WHITE,true);}
        String[] names={"石头","布","剪刀"};String[] em={"拳","掌","剪"};float btnY=s*.49f,bw=s*.18f,gap=s*.018f;float total=bw*3+gap*2,left=(w-total)/2f;for(int i=0;i<3;i++){RectF r=new RectF(left+i*(bw+gap),btnY,left+i*(bw+gap)+bw,btnY+bw);p.setColor(Color.rgb(39,48,61));c.drawRoundRect(r,bw*.24f,bw*.24f,p);text(c,em[i],r.centerX(),r.top+bw*.52f,bw*.42f,Color.WHITE,true);text(c,names[i],r.centerX(),r.bottom-bw*.12f,bw*.16f,Color.rgb(176,187,201),true);}
        drawBottomBar(c,new String[]{"菜单","重开"},false);
    }
    private void handleRpsTap(float x,float y){if(rpsOver)return;float s=minSide(),w=getWidth(),btnY=s*.49f,bw=s*.18f,gap=s*.018f,total=bw*3+gap*2,left=(w-total)/2f;if(y<btnY||y>btnY+bw)return;int choice=-1;for(int i=0;i<3;i++)if(x>=left+i*(bw+gap)&&x<=left+i*(bw+gap)+bw){choice=i+1;break;}if(choice<0)return;rpsPlayer=choice;rpsAi=1+random.nextInt(3);int result=rpsRound(rpsPlayer,rpsAi);if(result>0)rpsPlayerScore++;else if(result<0)rpsAiScore++;performHapticFeedback(result>0?HapticFeedbackConstants.CONFIRM:(result<0?HapticFeedbackConstants.REJECT:HapticFeedbackConstants.CLOCK_TICK));if(rpsPlayerScore>=2||rpsAiScore>=2){rpsOver=true;if(rpsPlayerScore>rpsAiScore)showResult(1,"你赢了！",rpsPlayerScore+" : "+rpsAiScore);else showResult(-1,"手表赢了",rpsPlayerScore+" : "+rpsAiScore);}invalidate();}
    private int rpsRound(int a,int b){if(a==b)return 0;return(a==1&&b==3)||(a==2&&b==1)||(a==3&&b==2)?1:-1;}
    private String rpsEmoji(int v){return v==1?"拳":v==2?"掌":"剪";}

    // ---------- About / donation ----------

    private void drawAbout(Canvas c){
        float s=minSide(),w=getWidth();
        drawGameHeader(c,"关于","腕上小游戏 · v5.0",false);
        float cardW=w*(roundScreen?.68f:.86f),left=(w-cardW)/2,top=s*.185f,bottom=bottomBarTop()-s*.030f;
        p.setColor(Color.rgb(25,31,40));c.drawRoundRect(new RectF(left,top,left+cardW,bottom),s*.045f,s*.045f,p);
        float y=top+s*.070f;
        text(c,"作者：黑白君",w/2,y,s*.040f,Color.WHITE,true);y+=s*.060f;
        drawWrappedCentered(c,"专为 Wear OS 小屏与圆屏优化的离线小游戏合集。",w/2,y,cardW*.84f,s*.026f,s*.039f,Color.rgb(190,199,212));
        y+=s*.090f;
        textFit(c,"13 款游戏 · 动画反馈 · 圆屏安全区",w/2,y,s*.025f,Color.rgb(150,165,183),false,Paint.Align.CENTER,cardW*.84f);

        float btnH=s*.082f,gap=s*.018f;
        float feedbackTop=bottom-s*.045f-btnH;
        float donateTop=feedbackTop-gap-btnH;
        RectF donate=new RectF(w/2-cardW*.35f,donateTop,w/2+cardW*.35f,donateTop+btnH);
        RectF feedback=new RectF(w/2-cardW*.35f,feedbackTop,w/2+cardW*.35f,feedbackTop+btnH);
        p.setColor(Color.rgb(7,193,96));c.drawRoundRect(donate,btnH/2,btnH/2,p);
        text(c,"支持作者",donate.centerX(),donate.centerY()+s*.010f,s*.030f,Color.WHITE,true);
        p.setColor(Color.rgb(65,132,245));c.drawRoundRect(feedback,btnH/2,btnH/2,p);
        text(c,"意见反馈 · 手机打开",feedback.centerX(),feedback.centerY()+s*.010f,s*.027f,Color.WHITE,true);
        drawBottomBar(c,new String[]{"返回"},false);
    }

    private void drawDonate(Canvas c){
        float s=minSide(),w=getWidth();
        text(c,"请黑白君喝杯饮料",w/2,s*.10f,s*.047f,Color.WHITE,true);
        textFit(c,"如果这个小工具让你开心了一下，谢谢你的支持 ☺",w/2,s*.155f,s*.026f,Color.rgb(175,186,200),false,Paint.Align.CENTER,w*(roundScreen?.67f:.86f));
        float maxBottom=bottomBarTop()-s*.035f;
        float top=s*.19f;
        float maxH=maxBottom-top;
        float ratio=donateQr==null?1f:(float)donateQr.getWidth()/donateQr.getHeight();
        float destH=maxH,destW=destH*ratio;
        float maxW=w*(roundScreen?.65f:.80f);
        if(destW>maxW){destW=maxW;destH=destW/ratio;}
        RectF dest=new RectF(w/2-destW/2,top,w/2+destW/2,top+destH);
        if(donateQr!=null){p.setFilterBitmap(false);c.drawBitmap(donateQr,null,dest,p);}
        else {p.setColor(Color.rgb(240,240,240));c.drawRoundRect(dest,s*.03f,s*.03f,p);text(c,"收款码加载失败",w/2,dest.centerY(),s*.035f,Color.DKGRAY,true);}
        drawBottomBar(c,new String[]{"返回"},false);
    }

    // ---------- Result / transient feedback ----------

    private void showResult(int kind, String title, String sub) {
        resultOverlayKind = kind; resultOverlayTitle = title; resultOverlaySub = sub; resultOverlayAutoHide = false;
        resultOverlayStart = SystemClock.elapsedRealtime();
        performHapticFeedback(kind == 1 ? HapticFeedbackConstants.CONFIRM : (kind == -1 ? HapticFeedbackConstants.REJECT : HapticFeedbackConstants.CLOCK_TICK));
        invalidate();
    }

    private void showTransientResult(int kind, String title, String sub) {
        showResult(kind, title, sub); resultOverlayAutoHide = true;
    }

    private void clearResultOverlay() {
        resultOverlayKind = 0; resultOverlayStart = 0; resultOverlayTitle = ""; resultOverlaySub = ""; resultOverlayAutoHide = false;
    }

    private void drawResultOverlay(Canvas c) {
        if (resultOverlayKind == 0 || mode == MENU || mode == ABOUT || mode == DONATE) return;
        long now = SystemClock.elapsedRealtime(); float s = minSide();
        if(resultOverlayAutoHide && now-resultOverlayStart>1750){clearResultOverlay();return;}
        float t = clamp((now - resultOverlayStart) / 360f, 0, 1), scale = .72f + .28f * overshoot(t);
        p.setColor(Color.argb((int)(105 * t), 0, 0, 0)); c.drawRect(0, 0, getWidth(), getHeight(), p);
        float cardW = getWidth() * (roundScreen ? .63f : .77f), cardH = s * .245f;
        RectF r = new RectF(getWidth()/2-cardW/2, getHeight()/2-cardH/2, getWidth()/2+cardW/2, getHeight()/2+cardH/2);
        c.save(); c.scale(scale, scale, r.centerX(), r.centerY());
        int accent = resultOverlayKind == 1 ? Color.rgb(66, 211, 133) : resultOverlayKind == -1 ? Color.rgb(255, 83, 94) : Color.rgb(255, 191, 62);
        p.setColor(Color.rgb(26, 33, 42)); c.drawRoundRect(r, s*.050f, s*.050f, p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(s*.009f); p.setColor(accent); c.drawRoundRect(r, s*.050f, s*.050f, p); p.setStyle(Paint.Style.FILL);
        float iconY = r.top + cardH*.30f;
        p.setColor(accent); c.drawCircle(r.centerX(), iconY, s*.050f, p);
        text(c, resultOverlayKind == 1 ? "✓" : resultOverlayKind == -1 ? "×" : "★", r.centerX(), iconY+s*.018f, s*.057f, Color.WHITE, true);
        textFit(c, resultOverlayTitle, r.centerX(), r.top+cardH*.62f, s*.041f, Color.WHITE, true, Paint.Align.CENTER, cardW*.82f);
        textFit(c, resultOverlaySub, r.centerX(), r.top+cardH*.80f, s*.025f, Color.rgb(190,200,213), false, Paint.Align.CENTER, cardW*.84f);
        c.restore();
        float age = now - resultOverlayStart;
        if (resultOverlayKind == 1 && age < 1350) {
            for (int i=0;i<14;i++) {
                double a=i*2.399963+age*.0025; float rr=s*(.12f+(i%5)*.026f)*Math.min(1f,age/450f);
                float x=getWidth()/2f+(float)Math.cos(a)*rr, y=getHeight()/2f+(float)Math.sin(a)*rr;
                int[] cols={Color.rgb(255,91,91),Color.rgb(74,204,122),Color.rgb(75,145,255),Color.rgb(255,202,64)};
                p.setColor(cols[i%cols.length]); c.drawCircle(x,y,s*.008f,p);
            }
            postInvalidateOnAnimation();
        } else if (resultOverlayKind == -1 && age < 1000) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(s*.008f); p.setColor(Color.argb(130,255,83,94));
            c.drawCircle(getWidth()/2f,getHeight()/2f,s*(.15f+.035f*(float)Math.sin(age/90.0)),p); p.setStyle(Paint.Style.FILL);
            postInvalidateOnAnimation();
        } else if (t < 1f) postInvalidateOnAnimation();
    }

    private void showToastHint(String text) {
        toastHint = text; toastHintUntil = SystemClock.elapsedRealtime() + 2200; invalidate();
    }

    private void drawToastHint(Canvas c) {
        long now = SystemClock.elapsedRealtime(); if (toastHint.length() == 0 || now >= toastHintUntil) return;
        float s=minSide(), w=Math.min(getWidth()*(roundScreen?.68f:.84f),dp(300)),h=s*.085f;
        float bottom=mode==MENU?getHeight()-s*(roundScreen?.16f:.06f):bottomBarTop()-s*.025f;
        RectF r=new RectF(getWidth()/2-w/2,bottom-h,getWidth()/2+w/2,bottom);
        p.setColor(Color.argb(225,38,46,58));c.drawRoundRect(r,h/2,h/2,p);
        textFit(c,toastHint,r.centerX(),r.centerY()+h*.10f,h*.27f,Color.WHITE,true,Paint.Align.CENTER,w*.86f);
        postInvalidateDelayed(80);
    }

    private void openFeedbackOnPhone() {
        boolean sent = phoneLinkOpener.openOnPhone(FEEDBACK_URL);
        showToastHint(sent ? "已请求在配对手机打开酷安" : "未能连接手机，请确认蓝牙连接");
        performHapticFeedback(sent ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.REJECT);
    }

    // ---------- Shared drawing helpers ----------

    private void drawGameHeader(Canvas c,String title,String sub,boolean light){
        float s=minSide(); int titleColor=light?Color.rgb(119,110,101):Color.WHITE; int subColor=light?Color.rgb(145,135,125):Color.rgb(158,168,182);
        text(c,title,getWidth()/2,s*.075f,s*.045f,titleColor,true);
        textFit(c,sub,getWidth()/2,s*.123f,s*.026f,subColor,false,Paint.Align.CENTER,getWidth()*(roundScreen?.68f:.88f));
    }

    private float headerBottom(){return minSide()*.145f;}

    private float bottomBarTop(){return bottomBarRect(1)[0].top;}

    private RectF[] bottomBarRect(int count){
        float s=minSide();
        float bottom=getHeight()-(roundScreen?s*.13f:s*.025f);
        float h=s*.14f;
        float gap=s*.016f;
        float usable=Math.min(getWidth()*(roundScreen?.66f:.88f),dp(310));
        float bw=(usable-gap*(count-1))/count,left=(getWidth()-usable)/2f;
        RectF[] out=new RectF[count];for(int i=0;i<count;i++)out[i]=new RectF(left+i*(bw+gap),bottom-h,left+i*(bw+gap)+bw,bottom);return out;
    }

    private void drawConfirmRow(Canvas c,String yes,String no){
        float s=minSide();
        float h=Math.max(s*.10f,Math.min(dp(42),s*.13f));
        float y=bottomBarTop()-h-s*.025f,gap=s*.02f,w=Math.min(s*.25f,(getWidth()*(roundScreen?.66f:.85f)-gap)/2f),cx=getWidth()/2f;
        drawPill(c,new RectF(cx-gap/2-w,y,cx-gap/2,y+h),yes,true,false);
        drawPill(c,new RectF(cx+gap/2,y,cx+gap/2+w,y+h),no,false,false);
    }

    private void drawBottomBar(Canvas c,String[] labels,boolean light){
        RectF[] rects=bottomBarRect(labels.length);for(int i=0;i<labels.length;i++)drawPill(c,rects[i],labels[i],false,light);
    }

    private void drawPill(Canvas c,RectF r,String label,boolean accent,boolean light){
        if(accent)p.setColor(Color.rgb(225,68,73));else p.setColor(light?Color.rgb(143,122,102):Color.rgb(38,45,56));
        c.drawRoundRect(r,r.height()/2,r.height()/2,p);
        textFit(c,label,r.centerX(),r.centerY()+r.height()*.12f,r.height()*.28f,Color.WHITE,true,Paint.Align.CENTER,r.width()*.82f);
    }

    private void ring(Canvas c,float x,float y,float radius,int color){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2f,minSide()*.006f));p.setColor(color);c.drawCircle(x,y,radius,p);p.setStyle(Paint.Style.FILL);}

    private void text(Canvas c,String s,float x,float y,float size,int color,boolean bold){text(c,s,x,y,size,color,bold,Paint.Align.CENTER);}
    private void text(Canvas c,String s,float x,float y,float size,int color,boolean bold,Paint.Align align){p.setColor(color);p.setTextSize(size);p.setTextAlign(align);p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,x,y,p);}

    private void textFit(Canvas c,String s,float x,float y,float size,int color,boolean bold,Paint.Align align,float maxWidth){
        p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));float fs=size;p.setTextSize(fs);
        while(fs>minSide()*.018f&&p.measureText(s)>maxWidth){fs*=.93f;p.setTextSize(fs);}text(c,s,x,y,fs,color,bold,align);
    }

    private void drawWrappedCentered(Canvas c,String s,float cx,float startY,float maxWidth,float size,float lineH,int color){
        List<String> lines=wrapText(s,maxWidth,size);float y=startY;for(String line:lines){text(c,line,cx,y,size,color,false);y+=lineH;}
    }

    private List<String> wrapText(String s,float maxWidth,float size){
        p.setTextSize(size);p.setTypeface(Typeface.create("sans",Typeface.NORMAL));List<String> out=new ArrayList<>();StringBuilder line=new StringBuilder();
        for(int i=0;i<s.length();i++){char ch=s.charAt(i);String cand=line.toString()+ch;if(line.length()>0&&p.measureText(cand)>maxWidth){out.add(line.toString());line.setLength(0);}line.append(ch);}if(line.length()>0)out.add(line.toString());return out;
    }

    // ---------- Touch ----------

    @Override public boolean onTouchEvent(MotionEvent e) {
        int action=e.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN){
            downX=lastX=e.getX();downY=lastY=e.getY();dragging=false;pinching=false;menuDragging=false;edgeBackOffset=0;
            edgeBackDragging=false;
            gestureStartedOnControl=isControlArea(downX,downY);menuScrollAtDown=menuScroll;
            if(mode==MENU)pressedMenuIndex=findMenuIndex(downX,downY);
            if(mode==BOUNCE&&!gestureStartedOnControl)handleBounceTouch(downX);
            invalidate();return true;
        }
        if(action==MotionEvent.ACTION_POINTER_DOWN&&e.getPointerCount()>=2&&(mode==XIANGQI||mode==GOMOKU)){
            pinching=true;dragging=false;pinchStart=distance(e);zoomStart=zoom;return true;
        }
        if(action==MotionEvent.ACTION_MOVE){
            if(mode==MENU){
                float dy=e.getY()-downY;if(Math.abs(dy)>minSide()*.018f){menuDragging=true;pressedMenuIndex=-1;menuScroll=clamp(menuScrollAtDown-dy,0,menuMaxScroll);invalidate();}return true;
            }
            if(mode==BOUNCE&&!gestureStartedOnControl){handleBounceTouch(e.getX());lastX=e.getX();lastY=e.getY();return true;}
            if(pinching&&e.getPointerCount()>=2){float dist=distance(e);if(pinchStart>1){zoom=clamp(zoomStart*dist/pinchStart,.72f,2.15f);invalidate();}}
            else if(!gestureStartedOnControl&&(mode==XIANGQI||mode==GOMOKU)){
                float dx=e.getX()-lastX,dy=e.getY()-lastY;if(Math.hypot(e.getX()-downX,e.getY()-downY)>minSide()*.018f)dragging=true;
                if(dragging){panX+=dx;panY+=dy;clampPan();invalidate();}lastX=e.getX();lastY=e.getY();
            }
            return true;
        }
        if(action==MotionEvent.ACTION_POINTER_UP){pinching=false;return true;}
        if(action==MotionEvent.ACTION_UP){
            float ux=e.getX(),uy=e.getY();
            if(mode==MENU){int idx=(!menuDragging&&Math.hypot(ux-downX,uy-downY)<minSide()*.035f)?findMenuIndex(ux,uy):-1;pressedMenuIndex=-1;if(idx>=0)startMode(MENU_MODES[idx]);invalidate();return true;}
            if(!gestureStartedOnControl&&(mode==GAME_2048||mode==SNAKE)){
                float dx=ux-downX,dy=uy-downY;
                if(Math.hypot(dx,dy)>=minSide()*.075f){
                    int dir;if(Math.abs(dx)>Math.abs(dy))dir=dx>0?1:3;else dir=dy>0?2:0;
                    if(mode==GAME_2048)move2048(dir);else setSnakeDirection(dir);
                    performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);invalidate();pinching=false;dragging=false;return true;
                }
            }
            if(!dragging&&!pinching)handleTap(ux,uy);
            pinching=false;dragging=false;return true;
        }
        if(action==MotionEvent.ACTION_CANCEL){pinching=false;dragging=false;pressedMenuIndex=-1;edgeBackDragging=false;edgeBackOffset=0;invalidate();return true;}
        return true;
    }

    private boolean isControlArea(float x,float y){
        if(mode==MENU||mode==ABOUT||mode==DONATE)return true;
        if(y>=bottomBarTop()-minSide()*.02f)return true;
        return (mode==XIANGQI&&pendingX>=0||mode==GOMOKU&&gomokuPendingX>=0)&&y>=bottomBarTop()-minSide()*.15f;
    }

    private int findMenuIndex(float x,float y){
        float s=minSide(),titleY=roundScreen?s*.105f:dp(34),viewportTop=titleY+s*.105f;
        float cardH=s*.17f,gap=s*.026f;
        for(int i=0;i<MENU_TITLES.length;i++){float yy=viewportTop+i*(cardH+gap)-menuScroll;RectF r=menuCardRect(yy,cardH);r.inset(-s*.012f,-s*.008f);if(r.contains(x,y))return i;}return -1;
    }

    private void handleTap(float x,float y){
        if(handleBottomBarTap(x,y))return;
        if(mode==XIANGQI){
            if(pendingX>=0&&y>=bottomBarTop()-minSide()*.15f){if(x<getWidth()/2)confirmXiangqi();else{pendingX=pendingY=-1;message="已取消";invalidate();}return;}selectXiangqiAt(x,y);
        }else if(mode==GOMOKU){
            if(gomokuPendingX>=0&&y>=bottomBarTop()-minSide()*.15f){if(x<getWidth()/2)confirmGomoku();else{gomokuPendingX=gomokuPendingY=-1;invalidate();}return;}selectGomokuAt(x,y);
        }else if(mode==TAP_RUSH){
            if(!tapOver&&Math.hypot(x-targetX,y-targetY)<=minSide()*.09f){tapScore++;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);placeTapTarget();invalidate();}
        }else if(mode==SIMON)handleSimonTap(x,y);
        else if(mode==TICTACTOE)handleTicTacToeTap(x,y);
        else if(mode==COLOR_HUNT)handleColorHuntTap(x,y);
        else if(mode==BOUNCE)handleBounceTouch(x);
        else if(mode==SLIDE_PUZZLE)handleSlidePuzzleTap(x,y);
        else if(mode==LIGHTS_OUT)handleLightsOutTap(x,y);
        else if(mode==MINI_MINES)handleMiniMinesTap(x,y);
        else if(mode==ROCK_PAPER_SCISSORS)handleRpsTap(x,y);
        else if(mode==ABOUT){
            float s=minSide(),w=getWidth(),cardW=w*(roundScreen?.68f:.86f),top=s*.185f,bottom=bottomBarTop()-s*.030f;
            float btnH=s*.082f,gap=s*.018f,feedbackTop=bottom-s*.045f-btnH,donateTop=feedbackTop-gap-btnH;
            RectF donate=new RectF(w/2-cardW*.35f,donateTop,w/2+cardW*.35f,donateTop+btnH);
            RectF feedback=new RectF(w/2-cardW*.35f,feedbackTop,w/2+cardW*.35f,feedbackTop+btnH);
            if(donate.contains(x,y)){navigateTo(DONATE,false);performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);invalidate();}
            else if(feedback.contains(x,y))openFeedbackOnPhone();
        }
    }

    private boolean handleBottomBarTap(float x,float y){
        if(y<bottomBarTop()-minSide()*.02f)return false;
        if(mode==XIANGQI){int idx=barIndex(x,y,xiangqiOver?2:3);if(idx<0)return false;if(idx==0)navigateTo(MENU,true);else if(idx==1)resetXiangqi();else if(idx==2&&!xiangqiOver&&xiangqi.undo()){selectedX=selectedY=pendingX=pendingY=-1;message="已撤销一步";}invalidate();return true;}
        if(mode==GOMOKU||mode==GAME_2048||mode==TAP_RUSH||mode==SNAKE||mode==SIMON||mode==TICTACTOE||mode==COLOR_HUNT||mode==BOUNCE||mode==SLIDE_PUZZLE||mode==LIGHTS_OUT||mode==MINI_MINES||mode==ROCK_PAPER_SCISSORS){
            int idx=barIndex(x,y,2);if(idx<0)return false;if(idx==0)navigateTo(MENU,true);
            else if(mode==GOMOKU)resetGomoku();else if(mode==GAME_2048)reset2048();else if(mode==TAP_RUSH)resetTapRush();else if(mode==SNAKE)resetSnake();else if(mode==SIMON)resetSimon();else if(mode==TICTACTOE)resetTicTacToe();else if(mode==COLOR_HUNT)resetColorHunt();else if(mode==BOUNCE)resetBounce();else if(mode==SLIDE_PUZZLE)resetSlidePuzzle();else if(mode==LIGHTS_OUT)resetLightsOut();else if(mode==MINI_MINES)resetMiniMines();else resetRps();
            invalidate();return true;
        }
        if(mode==ABOUT||mode==DONATE){int idx=barIndex(x,y,1);if(idx==0){navigateTo(mode==DONATE?ABOUT:MENU,true);invalidate();return true;}}
        return false;
    }

    private int barIndex(float x,float y,int count){RectF[] rs=bottomBarRect(count);for(int i=0;i<rs.length;i++){RectF hit=new RectF(rs[i]);hit.inset(-minSide()*.012f,-minSide()*.015f);if(hit.contains(x,y))return i;}return -1;}

    // ---------- Game actions ----------

    private void selectXiangqiAt(float sx,float sy){
        if(xiangqiOver)return;float cell=baseXiangqiCell()*zoom,left=getWidth()/2f+panX-4*cell,top=headerBottom()+minSide()*.035f+panY;
        int x=Math.round((sx-left)/cell),y=Math.round((sy-top)/cell);if(x<0||x>8||y<0||y>9)return;
        if(selectedX<0){if(xiangqi.belongsToTurn(x,y)){selectedX=x;selectedY=y;message="请选择目标位置";}else message="请选择己方棋子";}
        else if(xiangqi.belongsToTurn(x,y)){selectedX=x;selectedY=y;pendingX=pendingY=-1;message="已更换棋子";}
        else if(xiangqi.isLegalMove(selectedX,selectedY,x,y)){pendingX=x;pendingY=y;message="确认后落子";}
        else{pendingX=pendingY=-1;message="该走法不合法";}
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);invalidate();
    }

    private void confirmXiangqi(){
        if(selectedX<0||pendingX<0)return;char piece=xiangqi.pieceAt(selectedX,selectedY);int sx=selectedX,sy=selectedY,tx=pendingX,ty=pendingY;
        XiangqiEngine.MoveResult r=xiangqi.move(sx,sy,tx,ty);selectedX=selectedY=pendingX=pendingY=-1;
        if(r!=XiangqiEngine.MoveResult.ILLEGAL){xqAnimPiece=piece;xqAnimSX=sx;xqAnimSY=sy;xqAnimTX=tx;xqAnimTY=ty;xqMoveAnimStart=SystemClock.elapsedRealtime();}
        switch(r){case RED_WINS:message="红方获胜";xiangqiOver=true;showResult(1,"红方获胜！","中国象棋对局结束");break;case BLACK_WINS:message="黑方获胜";xiangqiOver=true;showResult(1,"黑方获胜！","中国象棋对局结束");break;case CHECK:message=xiangqi.isRedTurn()?"红方被将军":"黑方被将军";break;case OK:message=xiangqi.isRedTurn()?"红方回合":"黑方回合";break;default:message="走法不合法";}
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);invalidate();
    }

    private void selectGomokuAt(float sx,float sy){
        if(gomokuOver)return;float cell=baseGomokuCell()*zoom,left=getWidth()/2f+panX-7*cell,top=headerBottom()+minSide()*.03f+panY;
        int x=Math.round((sx-left)/cell),y=Math.round((sy-top)/cell);if(x<0||x>=15||y<0||y>=15)return;
        if(gomoku[y][x]!=0)message="这里已有棋子";else{gomokuPendingX=x;gomokuPendingY=y;message="确认后落子";}performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);invalidate();
    }

    private void confirmGomoku(){
        if(gomokuPendingX<0||gomokuOver)return;int x=gomokuPendingX,y=gomokuPendingY;gomoku[y][x]=gomokuTurn;gomokuLastX=x;gomokuLastY=y;gomokuStoneAnimStart=SystemClock.elapsedRealtime();
        if(checkFive(x,y,gomokuTurn)){gomokuOver=true;message=(gomokuTurn==1?"黑方":"白方")+"获胜";showResult(1,message+"！","五子连珠，对局结束");}else{gomokuTurn=3-gomokuTurn;message=gomokuTurn==1?"黑方回合":"白方回合";}
        gomokuPendingX=gomokuPendingY=-1;performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);invalidate();
    }

    private boolean checkFive(int x,int y,int who){int[][] dirs={{1,0},{0,1},{1,1},{1,-1}};for(int[] dir:dirs){int n=1;for(int s=1;s<5;s++){int xx=x+dir[0]*s,yy=y+dir[1]*s;if(xx<0||xx>=15||yy<0||yy>=15||gomoku[yy][xx]!=who)break;n++;}for(int s=1;s<5;s++){int xx=x-dir[0]*s,yy=y-dir[1]*s;if(xx<0||xx>=15||yy<0||yy>=15||gomoku[yy][xx]!=who)break;n++;}if(n>=5)return true;}return false;}

    private void move2048(int dir){
        if(over2048)return;Game2048Engine.MoveFrame f=game2048.move(dir);if(!f.changed){over2048=!game2048.canMove();if(over2048)showResult(-1,"没有可移动位置","本局 "+game2048.getScore()+" 分");invalidate();return;}frame2048=f;anim2048Start=SystemClock.elapsedRealtime();over2048=f.gameOver;
        if(game2048.getScore()>best2048){best2048=game2048.getScore();prefs.edit().putInt("best_2048",best2048).apply();}
        if(game2048.hasReached2048()&&!reached2048Shown){reached2048Shown=true;showTransientResult(1,"达成 2048！","可以继续挑战更高数字");}
        if(over2048)showResult(-1,"游戏结束","本局 "+game2048.getScore()+" 分");invalidate();
    }

    // ---------- Resets / navigation ----------

    private void navigateTo(int target, boolean back){mode=target;pageBackTransition=back;pageTransitionStart=SystemClock.elapsedRealtime();clearResultOverlay();invalidate();}
    private void startMode(int m){
        if(m==FEEDBACK){openFeedbackOnPhone();return;}
        panX=panY=0;zoom=1f;
        if(m==XIANGQI)resetXiangqi();else if(m==GOMOKU)resetGomoku();else if(m==GAME_2048)reset2048();else if(m==TAP_RUSH)resetTapRush();else if(m==SNAKE)resetSnake();else if(m==SIMON)resetSimon();else if(m==TICTACTOE)resetTicTacToe();else if(m==COLOR_HUNT)resetColorHunt();else if(m==BOUNCE)resetBounce();else if(m==SLIDE_PUZZLE)resetSlidePuzzle();else if(m==LIGHTS_OUT)resetLightsOut();else if(m==MINI_MINES)resetMiniMines();else if(m==ROCK_PAPER_SCISSORS)resetRps();
        navigateTo(m,false);performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }
    public boolean goBack(){if(mode==DONATE){navigateTo(ABOUT,true);return true;}if(mode!=MENU){navigateTo(MENU,true);return true;}return false;}

    private void resetAll(){resetXiangqi();resetGomoku();reset2048();resetTapRush();resetSnake();resetSimon();resetTicTacToe();resetColorHunt();resetBounce();resetSlidePuzzle();resetLightsOut();resetMiniMines();resetRps();mode=MENU;menuScroll=0;pageTransitionStart=SystemClock.elapsedRealtime();}
    private void resetXiangqi(){clearResultOverlay();xiangqi.reset();selectedX=selectedY=pendingX=pendingY=-1;xiangqiOver=false;xqAnimPiece=0;message="红方回合";panX=panY=0;zoom=1f;}
    private void resetGomoku(){clearResultOverlay();for(int y=0;y<15;y++)for(int x=0;x<15;x++)gomoku[y][x]=0;gomokuTurn=1;gomokuPendingX=gomokuPendingY=-1;gomokuOver=false;gomokuLastX=gomokuLastY=-1;message="黑方回合";panX=panY=0;zoom=1f;}
    private void reset2048(){clearResultOverlay();game2048.reset();frame2048=null;over2048=false;reached2048Shown=false;}
    private void resetTapRush(){clearResultOverlay();tapScore=0;tapOver=false;tapResultShown=false;tapStart=SystemClock.elapsedRealtime();post(()->{placeTapTarget();invalidate();});}
    private void resetSnake(){clearResultOverlay();snake.clear();snake.addFirst(new Cell(5,6));snake.addLast(new Cell(4,6));snake.addLast(new Cell(3,6));snakeDir=1;snakeScore=0;snakeOver=false;snakeRunning=false;snakeWon=false;placeSnakeFood();}
    private void resetSimon(){clearResultOverlay();simonSeq.clear();simonSeq.add(random.nextInt(4));simonInputIndex=0;simonOver=false;simonWon=false;simonShowing=true;simonRoundStart=SystemClock.elapsedRealtime()+550;simonTapFlash=-1;}
    private void resetTicTacToe(){clearResultOverlay();for(int i=0;i<9;i++)ttt[i]=0;tttOver=false;tttResult=0;tttLast=-1;tttAiDue=0;}
    private void resetColorHunt(){clearResultOverlay();colorGridN=2;colorScore=0;colorOver=false;colorWon=false;colorTarget=random.nextInt(4);colorDeadline=SystemClock.elapsedRealtime()+25000;colorFlash=-1;}
    private void resetBounce(){clearResultOverlay();bounceHits=0;bounceRunning=false;bounceOver=false;bounceWon=false;bounceLastTick=0;bounceBallX=bounceBallY=bounceVx=bounceVy=bouncePaddleX=0;}
    private void resetSlidePuzzle(){clearResultOverlay();for(int i=0;i<8;i++)slidePuzzle[i]=i+1;slidePuzzle[8]=0;slideMoves=0;slideWon=false;for(int i=0;i<80;i++){int z=findZero(slidePuzzle),zr=z/3,zc=z%3;int[] cand=new int[4];int n=0;if(zr>0)cand[n++]=z-3;if(zr<2)cand[n++]=z+3;if(zc>0)cand[n++]=z-1;if(zc<2)cand[n++]=z+1;int j=cand[random.nextInt(n)];slidePuzzle[z]=slidePuzzle[j];slidePuzzle[j]=0;}if(isSlideSolved())resetSlidePuzzle();}
    private void resetLightsOut(){clearResultOverlay();lightsMoves=0;lightsWon=false;for(int i=0;i<16;i++)lights[i]=false;for(int i=0;i<18;i++)toggleLights(random.nextInt(16),false);if(allLightsOff())resetLightsOut();}
    private void resetMiniMines(){clearResultOverlay();for(int i=0;i<25;i++){mines[i]=false;mineOpen[i]=false;}int placed=0;while(placed<5){int i=random.nextInt(25);if(!mines[i]){mines[i]=true;placed++;}}minesOver=false;minesWon=false;minesOpened=0;}
    private void resetRps(){clearResultOverlay();rpsPlayer=rpsAi=0;rpsPlayerScore=rpsAiScore=0;rpsOver=false;}

    private void placeTapTarget(){float s=minSide(),margin=s*.13f,top=headerBottom()+s*.05f,bottom=bottomBarTop()-s*.06f,left=margin,right=getWidth()-margin;if(right<=left||bottom<=top){targetX=getWidth()/2f;targetY=getHeight()/2f;return;}targetX=left+random.nextFloat()*(right-left);targetY=top+random.nextFloat()*(bottom-top);}

    // ---------- Math / gesture helpers ----------

    private float distance(MotionEvent e){float dx=e.getX(0)-e.getX(1),dy=e.getY(0)-e.getY(1);return(float)Math.hypot(dx,dy);}
    private float clamp(float v,float lo,float hi){return Math.max(lo,Math.min(hi,v));}
    private void clampPan(){float limX=getWidth()*.65f,limY=getHeight()*.65f;panX=clamp(panX,-limX,limX);panY=clamp(panY,-limY,limY);}
    private float lerp(float a,float b,float t){return a+(b-a)*t;}
    private float easeOutCubic(float t){float u=1-t;return 1-u*u*u;}
    private float overshoot(float t){t=clamp(t,0,1);float s=1.70158f;t-=1;return t*t*((s+1)*t+s)+1;}

    private static final class Cell {final int x,y;Cell(int x,int y){this.x=x;this.y=y;}}
}
