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
import android.view.VelocityTracker;
import android.widget.OverScroller;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class GameHubView extends View {
    private static final int MENU = 0;
    public static final int XIANGQI = 1;
    public static final int GOMOKU = 2;
    public static final int GAME_2048 = 3;
    public static final int TAP_RUSH = 4;
    public static final int SNAKE = 5;
    public static final int SIMON = 6;
    public static final int TICTACTOE = 7;
    public static final int COLOR_HUNT = 8;
    public static final int BOUNCE = 9;
    public static final int SLIDE_PUZZLE = 10;
    public static final int LIGHTS_OUT = 11;
    public static final int MINI_MINES = 12;
    public static final int ROCK_PAPER_SCISSORS = 13;
    private static final int ABOUT = 14;
    private static final int DONATE = 15;
    private static final int FEEDBACK = 16;
    public static final int CONNECT4 = 17;
    public static final int REVERSI = 18;
    public static final int MEMORY_MATCH = 19;
    public static final int MAZE = 20;
    public static final int NUMBER_TAP = 21;
    public static final int QUICK_MATH = 22;
    public static final int NIM = 23;
    public static final int PONG = 24;
    public static final int BRICK_BREAKER = 25;
    public static final int DICE_DUEL = 26;
    public static final int SUDOKU = 27;
    private static final int RECORDS = 28;
    public static final int BLOCK_DROP = 29;
    public static final int FLAPPY = 30;
    public static final int WHACK_MOLE = 31;
    public static final int BLACKJACK = 32;
    public static final int SOKOBAN = 33;
    public static final int RUNNER = 34;
    public static final int DODGER = 35;
    public static final int STACK_TOWER = 36;
    private static final int RESUME_ITEM = -2;
    private static final long FRAME_DELAY_MS = 33L; // ~30 FPS: smoother on Wear OS with much lower GPU/CPU load.

    private static final String FEEDBACK_URL = "https://www.coolapk.com/u/22532694";
    private static final int GAME_MENU_COUNT = 30;
    private static final String[] MENU_TITLES = {
            "中国象棋", "五子棋", "井字棋", "四子棋", "黑白棋", "Nim 取石",
            "2048", "数字华容道", "熄灯解谜", "迷你扫雷", "记忆配对", "迷宫逃脱", "4×4 数独", "推箱子",
            "俄罗斯方块", "贪吃蛇", "跳跃小鸟", "腕上乒乓", "砖块破坏", "弹球挑战", "像素跑酷", "三道闪避", "叠塔",
            "反应点击", "记忆闪烁", "色块猎手", "数字连点", "极速心算", "打地鼠", "21 点",
            "战绩记录", "意见反馈", "关于"
    };
    private static final String[] MENU_SUBS = {
            "完整规则 · 双人对弈", "强化 AI / 双人 · 五连获胜", "Minimax AI / 双人", "强化 AI / 双人 · 四连珠", "强化 AI / 双人 · 翻转棋子", "最佳策略 AI / 双人",
            "经典滑动 · 保留原逻辑", "3×3 拼图 · 自动存档", "4×4 灯阵 · 自动存档", "5×5 雷区 · 自动存档", "翻牌配对 · 8 对图案", "滑动移动 · 找到出口", "可修改 / 清除 · 冲突提示", "滑动推箱 · 多关卡 · 自动存档",
            "旋转 / 横移 / 快速下落", "滑动转向 · 越吃越快", "轻点起飞 · 穿过障碍", "单人 / 双人同屏", "拖动挡板 · 清空砖块", "拖动挡板 · 连续反弹", "轻点跳跃 · 越跑越快", "左右切道 · 躲开障碍", "轻点叠放 · 登上 10 层",
            "30 秒手速挑战", "记住颜色 · 连续挑战", "找出不同色 · 12 关", "按 1→16 顺序点击", "20 秒快速计算", "30 秒连击挑战", "要牌 / 停牌 · 对战庄家",
            "游玩次数 · 胜负 · 最佳", "在配对手机打开反馈主页", "版本 · 设计说明"
    };
    private static final String[] MENU_CATEGORIES = {
            "棋盘对战", "棋盘对战", "棋盘对战", "棋盘对战", "棋盘对战", "棋盘对战",
            "益智解谜", "益智解谜", "益智解谜", "益智解谜", "益智解谜", "益智解谜", "益智解谜", "益智解谜",
            "街机经典", "街机经典", "街机经典", "街机经典", "街机经典", "街机经典", "街机经典", "街机经典", "街机经典",
            "轻松挑战", "轻松挑战", "轻松挑战", "轻松挑战", "轻松挑战", "轻松挑战", "轻松挑战",
            "工具", "工具", "工具"
    };
    private static final int[] MENU_MODES = {
            XIANGQI, GOMOKU, TICTACTOE, CONNECT4, REVERSI, NIM,
            GAME_2048, SLIDE_PUZZLE, LIGHTS_OUT, MINI_MINES, MEMORY_MATCH, MAZE, SUDOKU, SOKOBAN,
            BLOCK_DROP, SNAKE, FLAPPY, PONG, BRICK_BREAKER, BOUNCE, RUNNER, DODGER, STACK_TOWER,
            TAP_RUSH, SIMON, COLOR_HUNT, NUMBER_TAP, QUICK_MATH, WHACK_MOLE, BLACKJACK,
            RECORDS, FEEDBACK, ABOUT
    };

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float d;
    private final Random random = new Random();
    private final boolean roundScreen;
    private final SharedPreferences prefs;
    private final OverScroller menuScroller;
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
    private VelocityTracker menuVelocityTracker;
    private boolean menuSpringPending;
    private int pressedBottomIndex = -1;
    private float pressedBottomDownX, pressedBottomDownY;
    private int pendingModeChoice = -1;
    private final boolean[] aiMode = new boolean[40];
    private boolean roundResultRecorded;
    private boolean suppressResultRecord;
    private int savedResumeMode = -1;
    private HostListener hostListener;
    private boolean restartConfirm;
    private long restartConfirmUntil;

    public interface HostListener {
        void onExitToHub();
    }

    public void setHostListener(HostListener listener) { hostListener = listener; }

    public void openGame(int gameMode, boolean singlePlayer) {
        if (supportsModeChoice(gameMode)) aiMode[gameMode] = singlePlayer;
        startActualMode(gameMode);
    }

    public void resumeSavedGameExternal() { resumeSavedGame(); }

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
    private long gomokuAiDue;

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
    private int tttTurn = 1;

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

    // Connect Four. 0 empty, 1 red/player 1, 2 yellow/player 2/AI.
    private final int[] connect4 = new int[42];
    private int connect4Turn = 1;
    private boolean connect4Over;
    private int connect4Last = -1;
    private long connect4AiDue;

    // Reversi 6x6 for watch-sized interaction.
    private final int[] reversi = new int[36];
    private int reversiTurn = 1;
    private boolean reversiOver;
    private long reversiAiDue;

    // Memory pairs: 16 cards / 8 pairs.
    private final int[] memoryCards = new int[16];
    private final boolean[] memoryMatched = new boolean[16];
    private int memoryFirst = -1, memorySecond = -1, memoryPairs, memoryMoves;
    private long memoryHideAt;
    private boolean memoryWon;

    // Maze 9x9. Bitmask walls: 1 up, 2 right, 4 down, 8 left.
    private final int[] mazeWalls = new int[81];
    private int mazePlayer, mazeExit = 80, mazeMoves;
    private boolean mazeWon;

    // Number Tap 1..16.
    private final int[] numberTapOrder = new int[16];
    private int numberTapNext;
    private long numberTapStart, numberTapFinish;
    private boolean numberTapOver;

    // Quick Math.
    private int mathA, mathB, mathAnswer, mathScore, mathWrong, mathOp;
    private final int[] mathChoices = new int[4];
    private long mathDeadline;
    private boolean mathOver;

    // Nim: start with 21 stones, take 1..3, last stone wins.
    private int nimStones = 21, nimTurn = 1;
    private boolean nimOver;
    private long nimAiDue;

    // Pong.
    private float pongX, pongY, pongVx, pongVy, pongBottomX, pongTopX;
    private int pongBottomScore, pongTopScore;
    private boolean pongRunning, pongOver;
    private long pongLastTick;

    // Brick breaker.
    private final boolean[] bricks = new boolean[24];
    private float brickBallX, brickBallY, brickVx, brickVy, brickPaddleX;
    private int brickLeft, brickScore;
    private boolean brickRunning, brickOver, brickWon;
    private long brickLastTick;

    // Dice duel.
    private int diceP1, diceP2, diceP1Score, diceP2Score, diceTurn = 1;
    private boolean diceOver;

    // 4x4 Sudoku. 0 means empty.
    private final int[] sudoku = new int[16];
    private final int[] sudokuSolution = new int[16];
    private final boolean[] sudokuFixed = new boolean[16];
    private int sudokuSelected = -1, sudokuMistakes;
    private boolean sudokuWon;

    // About / donation.
    private Bitmap donateQr;


    // New v7 games.
    private static final int BLOCK_W = 10, BLOCK_H = 16;
    private final int[][] blockBoard = new int[BLOCK_H][BLOCK_W];
    private int blockType, blockRot, blockX, blockY, blockScore, blockLines;
    private long blockNextTick;
    private boolean blockOver;

    private float flappyY, flappyV, flappyPipeX, flappyGapY;
    private int flappyScore;
    private long flappyLastTick;
    private boolean flappyRunning, flappyOver;

    private int moleIndex, moleScore, moleMisses, moleCombo;
    private long moleDeadline, moleNextMove;
    private boolean moleOver;

    private int blackjackPlayer, blackjackDealer, blackjackPlayerCards, blackjackDealerCards, blackjackPlayerAces, blackjackDealerAces;
    private boolean blackjackOver, blackjackDealerHidden;

    // v7.0 additional watch-friendly classics.
    private static final int SOKOBAN_SIZE = 6;
    private final int[] sokobanMap = new int[SOKOBAN_SIZE * SOKOBAN_SIZE]; // 0 floor, 1 wall, 2 box
    private final boolean[] sokobanGoals = new boolean[SOKOBAN_SIZE * SOKOBAN_SIZE];
    private int sokobanPlayer, sokobanLevel = -1, sokobanMoves;
    private boolean sokobanWon;

    private float runnerY, runnerV, runnerObstacleX;
    private int runnerScore;
    private long runnerLastTick;
    private boolean runnerRunning, runnerOver;

    private int dodgerLane, dodgerObstacleLane, dodgerScore;
    private float dodgerObstacleY;
    private long dodgerLastTick;
    private boolean dodgerRunning, dodgerOver;

    private final float[] stackCenters = new float[12];
    private final float[] stackWidths = new float[12];
    private int stackLevel;
    private float stackMovingX, stackMovingW, stackV;
    private long stackLastTick;
    private boolean stackRunning, stackOver, stackWon;

    public GameHubView(Context context) {
        super(context);
        d = getResources().getDisplayMetrics().density;
        roundScreen = getResources().getConfiguration().isScreenRound();
        prefs = context.getSharedPreferences("wear_games", Context.MODE_PRIVATE);
        menuScroller = new OverScroller(context);
        Arrays.fill(aiMode, true);
        savedResumeMode = prefs.getBoolean("has_save", false) ? prefs.getInt("last_mode", -1) : -1;
        best2048 = prefs.getInt("best_2048", 0);
        phoneLinkOpener = new PhoneLinkOpener(context);
        setBackgroundColor(Color.BLACK);
        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        setFocusable(true);
        setHapticFeedbackEnabled(true);
        // Lazy initialization: the requested game is reset by openGame()/resumeSavedGameExternal().
        // Avoid generating every board/maze/puzzle on each game launch.
        mode = MENU;
        menuScroll = 0f;
        pageTransitionStart = 0L;
    }

    private float dp(float v) { return v * d; }
    private void scheduleFrame() {
        if (isAttachedToWindow() && getWindowVisibility() == VISIBLE) postInvalidateDelayed(FRAME_DELAY_MS);
    }
    private float minSide() { return Math.min(getWidth(), getHeight()); }
    private float safeWidthFactor() { return roundScreen ? 0.78f : 0.92f; }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        long now = SystemClock.elapsedRealtime();
        float t = pageTransitionStart == 0 ? 1f : clamp((now - pageTransitionStart) / 180f, 0f, 1f);
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
        if (t < 1f) scheduleFrame();
        drawResultOverlay(c);
        drawToastHint(c);
        if (pendingModeChoice >= 0) drawModePicker(c);
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
                    case CONNECT4: drawConnect4(c); break;
                    case REVERSI: drawReversi(c); break;
                    case MEMORY_MATCH: drawMemoryMatch(c); break;
                    case MAZE: drawMaze(c); break;
                    case NUMBER_TAP: drawNumberTap(c); break;
                    case QUICK_MATH: drawQuickMath(c); break;
                    case NIM: drawNim(c); break;
                    case PONG: drawPong(c); break;
                    case BRICK_BREAKER: drawBrickBreaker(c); break;
                    case DICE_DUEL: drawDiceDuel(c); break;
                    case SUDOKU: drawSudoku(c); break;
                    case BLOCK_DROP: drawBlockDrop(c); break;
                    case FLAPPY: drawFlappy(c); break;
                    case WHACK_MOLE: drawWhackMole(c); break;
                    case BLACKJACK: drawBlackjack(c); break;
                    case SOKOBAN: drawSokoban(c); break;
                    case RUNNER: drawRunner(c); break;
                    case DODGER: drawDodger(c); break;
                    case STACK_TOWER: drawStackTower(c); break;
                    case RECORDS: drawRecords(c); break;
                    default: drawMenu(c);
                }
        }
    }

    // ---------- Menu ----------

    private void drawMenu(Canvas c) {
        float s = minSide();
        float w = getWidth(), h = getHeight();
        float titleY = roundScreen ? s * 0.102f : dp(35);
        text(c, "腕上小游戏", w / 2f, titleY, clamp(s * 0.058f, dp(21), dp(29)), Color.WHITE, true);
        text(c, "30 款 · 4 分类 · 自动存档 / 战绩", w / 2f, titleY + clamp(s * .050f, dp(19), dp(26)),
                clamp(s * .027f, dp(10), dp(14)), Color.rgb(157, 169, 184), false);

        float viewportTop = titleY + s * .112f;
        float viewportBottom = h - (roundScreen ? s * .105f : s * .025f);
        float cardH = s * .185f;
        float gap = s * .022f;
        float sectionH = s * .078f;
        float cursor = viewportTop;
        if (savedResumeMode >= 0) cursor += cardH * .78f + gap;
        String prev = "";
        for (int i = 0; i < MENU_TITLES.length; i++) {
            if (!MENU_CATEGORIES[i].equals(prev)) { cursor += sectionH; prev = MENU_CATEGORIES[i]; }
            cursor += cardH + gap;
        }
        float contentH = Math.max(0, cursor - viewportTop - gap);
        menuMaxScroll = Math.max(0, contentH - (viewportBottom - viewportTop));
        float over = s * .13f;
        menuScroll = clamp(menuScroll, -over, menuMaxScroll + over);

        c.save();
        c.clipRect(0, viewportTop, w, viewportBottom);
        cursor = viewportTop - menuScroll;
        if (savedResumeMode >= 0) {
            float rh = cardH * .78f;
            RectF rr = menuCardRect(cursor, rh);
            p.setColor(Color.rgb(33, 67, 59)); c.drawRoundRect(rr, rh*.30f, rh*.30f, p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(s*.004f); p.setColor(Color.rgb(74, 210, 142)); c.drawRoundRect(rr, rh*.30f, rh*.30f, p); p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(74,210,142)); c.drawCircle(rr.left+rh*.42f, rr.centerY(), rh*.19f, p);
            text(c, "▶", rr.left+rh*.42f, rr.centerY()+rh*.09f, rh*.25f, Color.rgb(12,31,26), true);
            float tx=rr.left+rh*.75f;
            textFit(c,"继续上次 · "+gameName(savedResumeMode),tx,rr.top+rh*.43f,rh*.25f,Color.WHITE,true,Paint.Align.LEFT,rr.right-tx-rh*.12f);
            textFit(c,"自动存档已保留",tx,rr.top+rh*.70f,rh*.15f,Color.rgb(159,218,193),false,Paint.Align.LEFT,rr.right-tx-rh*.12f);
            cursor += rh + gap;
        }
        prev = "";
        for (int i = 0; i < MENU_TITLES.length; i++) {
            if (!MENU_CATEGORIES[i].equals(prev)) {
                text(c, MENU_CATEGORIES[i], w/2f, cursor + sectionH*.58f, sectionH*.31f, Color.rgb(122,139,160), true);
                cursor += sectionH; prev = MENU_CATEGORIES[i];
            }
            float y = cursor;
            if (y + cardH >= viewportTop - cardH && y <= viewportBottom + cardH) {
                RectF r = menuCardRect(y, cardH);
                float press = i == pressedMenuIndex && !menuDragging ? 0.955f : 1f;
                float edgeScale = 1f;
                if (roundScreen) {
                    float center = (viewportTop + viewportBottom) * .5f;
                    float half = Math.max(1f, (viewportBottom - viewportTop) * .5f);
                    float dist = Math.min(1f, Math.abs(r.centerY() - center) / half);
                    edgeScale = .90f + .10f * (1f - dist * dist);
                }
                c.save(); c.scale(press * edgeScale, press * edgeScale, r.centerX(), r.centerY());
                p.setColor(i == pressedMenuIndex ? Color.rgb(44, 53, 66) : Color.rgb(27, 33, 42));
                c.drawRoundRect(r, cardH * .28f, cardH * .28f, p);
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(1f, s * .003f)); p.setColor(Color.rgb(58, 68, 82)); c.drawRoundRect(r, cardH * .28f, cardH * .28f, p); p.setStyle(Paint.Style.FILL);
                float iconR = cardH * .235f, iconX = r.left + cardH * .43f, iconY = r.centerY();
                drawMenuIcon(c, MENU_MODES[i], iconX, iconY, iconR);
                float tx = r.left + cardH * .80f;
                textFit(c, MENU_TITLES[i], tx, y + cardH * .42f, cardH * .31f, Color.WHITE, true, Paint.Align.LEFT, r.right - tx - cardH * .12f);
                textFit(c, MENU_SUBS[i], tx, y + cardH * .72f, cardH * .16f, Color.rgb(162, 172, 186), false, Paint.Align.LEFT, r.right - tx - cardH * .12f);
                c.restore();
            }
            cursor += cardH + gap;
        }
        c.restore();

        if (menuMaxScroll > 0) {
            float trackTop = viewportTop + s * .018f, trackBottom = viewportBottom - s * .018f;
            float thumbH = Math.max(s * .055f, (trackBottom-trackTop)*(viewportBottom-viewportTop)/Math.max(contentH,1));
            float t = menuMaxScroll == 0 ? 0 : clamp(menuScroll/menuMaxScroll,0,1);
            float y = trackTop + (trackBottom-trackTop-thumbH)*t;
            p.setColor(Color.argb(125,155,166,181)); float x=w-(roundScreen?s*.060f:dp(7));
            c.drawRoundRect(new RectF(x-s*.006f,y,x+s*.006f,y+thumbH),s*.006f,s*.006f,p);
        }
    }

    private float menuViewportTop() { float s=minSide(); float titleY=roundScreen?s*.102f:dp(35); return titleY+s*.112f; }
    private float menuViewportBottom() { return getHeight()-(roundScreen?minSide()*.105f:minSide()*.025f); }

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
            case CONNECT4:
                for(int rr=0;rr<2;rr++)for(int cc=0;cc<3;cc++){p.setColor(((rr+cc)&1)==0?Color.rgb(255,91,91):Color.rgb(255,205,70));c.drawCircle(x+(cc-1)*r*.62f,y+(rr-.5f)*r*.58f,r*.23f,p);} break;
            case REVERSI:
                p.setColor(Color.rgb(34,143,84));c.drawCircle(x,y,r,p);p.setColor(Color.WHITE);c.drawCircle(x-r*.28f,y,r*.34f,p);p.setColor(Color.rgb(20,24,30));c.drawCircle(x+r*.28f,y,r*.34f,p); break;
            case MEMORY_MATCH:
                p.setColor(Color.rgb(137,98,255));c.drawRoundRect(new RectF(x-r*.86f,y-r*.72f,x-r*.06f,y+r*.72f),r*.16f,r*.16f,p);p.setColor(Color.rgb(255,113,151));c.drawRoundRect(new RectF(x+r*.06f,y-r*.72f,x+r*.86f,y+r*.72f),r*.16f,r*.16f,p);text(c,"★",x,y+r*.22f,r*.62f,Color.WHITE,true); break;
            case MAZE:
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.16f);p.setColor(Color.rgb(93,167,255));c.drawRect(x-r*.75f,y-r*.75f,x+r*.75f,y+r*.75f,p);c.drawLine(x-r*.25f,y-r*.75f,x-r*.25f,y+r*.20f,p);c.drawLine(x-r*.25f,y+r*.20f,x+r*.35f,y+r*.20f,p);c.drawLine(x+r*.35f,y-r*.20f,x+r*.35f,y+r*.75f,p);p.setStyle(Paint.Style.FILL); break;
            case NUMBER_TAP:
                p.setColor(Color.rgb(63,142,255));c.drawCircle(x-r*.38f,y-r*.30f,r*.38f,p);p.setColor(Color.rgb(77,199,132));c.drawCircle(x+r*.38f,y+r*.30f,r*.38f,p);text(c,"1",x-r*.38f,y-r*.12f,r*.46f,Color.WHITE,true);text(c,"16",x+r*.38f,y+r*.48f,r*.34f,Color.WHITE,true); break;
            case QUICK_MATH:
                p.setColor(Color.rgb(255,176,65));c.drawRoundRect(new RectF(x-r,y-r,x+r,y+r),r*.25f,r*.25f,p);text(c,"+×",x,y+r*.24f,r*.62f,Color.WHITE,true); break;
            case NIM:
                p.setColor(Color.rgb(255,190,72));for(int i=-1;i<=1;i++)for(int j=-1;j<=1;j++)if(!(i==1&&j==1))c.drawCircle(x+i*r*.48f,y+j*r*.48f,r*.18f,p); break;
            case PONG:
                p.setColor(Color.WHITE);c.drawRoundRect(new RectF(x-r*.82f,y-r*.62f,x-r*.64f,y+r*.62f),r*.09f,r*.09f,p);c.drawRoundRect(new RectF(x+r*.64f,y-r*.62f,x+r*.82f,y+r*.62f),r*.09f,r*.09f,p);p.setColor(Color.rgb(86,164,255));c.drawCircle(x,y,r*.22f,p); break;
            case BRICK_BREAKER:
                for(int rr=0;rr<2;rr++)for(int cc=0;cc<3;cc++){p.setColor(rr==0?Color.rgb(255,112,112):Color.rgb(255,190,70));float l=x-r*.88f+cc*r*.60f,t=y-r*.72f+rr*r*.42f;c.drawRoundRect(new RectF(l,t,l+r*.50f,t+r*.30f),r*.08f,r*.08f,p);}p.setColor(Color.WHITE);c.drawRoundRect(new RectF(x-r*.62f,y+r*.58f,x+r*.62f,y+r*.76f),r*.09f,r*.09f,p); break;
            case DICE_DUEL:
                p.setColor(Color.rgb(245,245,248));c.drawRoundRect(new RectF(x-r*.78f,y-r*.78f,x+r*.78f,y+r*.78f),r*.22f,r*.22f,p);p.setColor(Color.rgb(38,45,56));for(int sx=-1;sx<=1;sx+=2)for(int sy=-1;sy<=1;sy+=2)c.drawCircle(x+sx*r*.34f,y+sy*r*.34f,r*.12f,p);c.drawCircle(x,y,r*.12f,p); break;
            case SUDOKU:
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(r*.10f);p.setColor(Color.rgb(122,181,255));c.drawRect(x-r*.78f,y-r*.78f,x+r*.78f,y+r*.78f,p);c.drawLine(x,y-r*.78f,x,y+r*.78f,p);c.drawLine(x-r*.78f,y,x+r*.78f,y,p);p.setStyle(Paint.Style.FILL);text(c,"4",x-r*.38f,y-r*.16f,r*.42f,Color.WHITE,true);text(c,"2",x+r*.38f,y+r*.54f,r*.42f,Color.WHITE,true); break;
            case RECORDS:
                p.setColor(Color.rgb(72,153,255));for(int i=0;i<3;i++){float bh=r*(.45f+i*.30f);c.drawRoundRect(new RectF(x-r*.76f+i*r*.53f,y+r*.72f-bh,x-r*.39f+i*r*.53f,y+r*.72f),r*.08f,r*.08f,p);} break;
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
            scheduleFrame();
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
        updateGomokuAi();
        String gm = aiMode[GOMOKU] ? "单人 · " + message : "双人 · " + message;
        drawGameHeader(c, "五子棋", gm, false);
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
            if(x==gomokuLastX&&y==gomokuLastY){float t=clamp((now-gomokuStoneAnimStart)/150f,0,1);scale=overshoot(t);if(t<1)scheduleFrame();}
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
            scheduleFrame();
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
        if(!snakeOver)scheduleFrame();
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
        if(!simonOver)scheduleFrame();
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
        String sub;
        if (tttOver) {
            if (aiMode[TICTACTOE]) sub = tttResult == 1 ? "你赢了" : tttResult == -1 ? "手表获胜" : "平局";
            else sub = tttResult == 1 ? "X 获胜" : tttResult == -1 ? "O 获胜" : "平局";
        } else if (aiMode[TICTACTOE]) sub = tttAiDue > 0 ? "单人 · 手表思考中…" : "单人 · 你是 X";
        else sub = "双人 · " + (tttTurn == 1 ? "X 回合" : "O 回合");
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
                if (t < 1) scheduleFrame();
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
        if (tttOver || (aiMode[TICTACTOE] && tttAiDue > 0)) return;
        float s = minSide(), size = s * (roundScreen ? .49f : .57f), left = (getWidth() - size) / 2f, top = s * .225f;
        if (x < left || x >= left + size || y < top || y >= top + size) return;
        int col = Math.min(2, (int)((x - left) / (size / 3f)));
        int row = Math.min(2, (int)((y - top) / (size / 3f)));
        int idx = row * 3 + col;
        if (ttt[idx] != 0) return;
        placeTttMark(idx, tttTurn);
        int winner = tttWinner();
        if (winner != 0) finishTtt(winner == 1 ? 1 : -1);
        else if (tttFull()) finishTtt(2);
        else if (aiMode[TICTACTOE]) { tttTurn = 2; tttAiDue = SystemClock.elapsedRealtime() + 330; }
        else tttTurn = tttTurn == 1 ? 2 : 1;
        invalidate();
    }

    private void updateTicTacToeAi() {
        if (!aiMode[TICTACTOE] || tttOver || tttAiDue <= 0 || SystemClock.elapsedRealtime() < tttAiDue) return;
        tttAiDue = 0;
        int idx = chooseTttAiMove();
        if (idx >= 0) placeTttMark(idx, 2);
        int winner = tttWinner();
        if (winner == 2) finishTtt(-1);
        else if (tttFull()) finishTtt(2);
        else tttTurn = 1;
        scheduleFrame();
    }

    private void placeTttMark(int idx, int who) {
        ttt[idx] = who; tttLast = idx; tttMarkAnimStart = SystemClock.elapsedRealtime();
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
    }

    private int chooseTttAiMove() {
        int bestMove=-1,bestScore=Integer.MIN_VALUE;
        for(int i=0;i<9;i++)if(ttt[i]==0){ttt[i]=2;int score=tttMinimax(false,0);ttt[i]=0;if(score>bestScore){bestScore=score;bestMove=i;}}
        return bestMove;
    }

    private int tttMinimax(boolean aiTurn,int depth){
        int winner=tttWinner();
        if(winner==2)return 10-depth;
        if(winner==1)return depth-10;
        if(tttFull())return 0;
        int best=aiTurn?Integer.MIN_VALUE:Integer.MAX_VALUE;
        for(int i=0;i<9;i++)if(ttt[i]==0){ttt[i]=aiTurn?2:1;int score=tttMinimax(!aiTurn,depth+1);ttt[i]=0;if(aiTurn)best=Math.max(best,score);else best=Math.min(best,score);}
        return best;
    }

    private int tttWinner() {
        int[][] lines = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] l : lines) if (ttt[l[0]] != 0 && ttt[l[0]] == ttt[l[1]] && ttt[l[1]] == ttt[l[2]]) return ttt[l[0]];
        return 0;
    }

    private boolean tttFull() { for (int v : ttt) if (v == 0) return false; return true; }
    private void finishTtt(int result) {
        tttOver = true; tttResult = result; tttAiDue = 0;
        if (aiMode[TICTACTOE]) {
            if (result == 1) showResult(1, "你赢了！", "井字棋挑战成功");
            else if (result == -1) showResult(-1, "这局输了", "点“重开”再挑战一次");
            else showResult(2, "平局", "旗鼓相当，再来一局");
        } else {
            if (result == 1) showResult(1, "X 获胜！", "双人对局结束");
            else if (result == -1) showResult(1, "O 获胜！", "双人对局结束");
            else showResult(2, "平局", "双人对局结束");
        }
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
        if (!colorOver) scheduleFrame();
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
        if (!bounceOver) scheduleFrame();
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

    // ---------- Connect Four ----------

    private void drawConnect4(Canvas c){
        updateConnect4Ai();
        float s=minSide(),w=getWidth(),size=s*(roundScreen?.58f:.66f),left=(w-size)/2f,top=s*.205f,cell=size/7f;
        String sub=connect4Over?"对局结束":(aiMode[CONNECT4]?(connect4Turn==1?"单人 · 你是红方":"单人 · 手表思考中…"):("双人 · "+(connect4Turn==1?"红方回合":"黄方回合")));
        drawGameHeader(c,"四子棋",sub,false);
        p.setColor(Color.rgb(37,87,166));c.drawRoundRect(new RectF(left,top,left+size,top+cell*6),s*.03f,s*.03f,p);
        for(int row=0;row<6;row++)for(int col=0;col<7;col++){
            int idx=row*7+col;float cx=left+(col+.5f)*cell,cy=top+(row+.5f)*cell;
            p.setColor(connect4[idx]==0?Color.rgb(15,22,31):(connect4[idx]==1?Color.rgb(255,88,96):Color.rgb(255,207,71)));
            c.drawCircle(cx,cy,cell*.36f,p);
            if(idx==connect4Last)ring(c,cx,cy,cell*.39f,Color.WHITE);
        }
        drawBottomBar(c,new String[]{"菜单","重开"},false);
    }

    private void handleConnect4Tap(float x,float y){
        if(connect4Over||(aiMode[CONNECT4]&&connect4Turn==2))return;
        float s=minSide(),size=s*(roundScreen?.58f:.66f),left=(getWidth()-size)/2f,top=s*.205f,cell=size/7f;
        if(x<left||x>=left+size||y<top||y>top+cell*6)return;int col=Math.min(6,(int)((x-left)/cell));connect4Drop(col);
    }

    private boolean connect4Drop(int col){
        for(int row=5;row>=0;row--){int idx=row*7+col;if(connect4[idx]!=0)continue;int who=connect4Turn;connect4[idx]=who;connect4Last=idx;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            if(connect4Win(idx,who)){connect4Over=true;if(aiMode[CONNECT4]&&who==2)showResult(-1,"手表四连","这局由手表获胜");else showResult(1,(who==1?"红方":"黄方")+"获胜！","四子连线完成");}
            else if(connect4Full()){connect4Over=true;showResult(2,"平局","棋盘已经放满");}
            else{connect4Turn=3-connect4Turn;if(aiMode[CONNECT4]&&connect4Turn==2)connect4AiDue=SystemClock.elapsedRealtime()+260;}
            invalidate();return true;}
        showToastHint("这一列已经满了");return false;
    }

    private boolean connect4Win(int idx,int who){int r=idx/7,c=idx%7;int[][] ds={{1,0},{0,1},{1,1},{1,-1}};for(int[] d0:ds){int n=1;for(int sign:new int[]{-1,1}){int rr=r+d0[1]*sign,cc=c+d0[0]*sign;while(rr>=0&&rr<6&&cc>=0&&cc<7&&connect4[rr*7+cc]==who){n++;rr+=d0[1]*sign;cc+=d0[0]*sign;}}if(n>=4)return true;}return false;}
    private boolean connect4Full(){for(int i=0;i<7;i++)if(connect4[i]==0)return false;return true;}
    private void updateConnect4Ai(){if(!aiMode[CONNECT4]||connect4Over||connect4Turn!=2||connect4AiDue<=0)return;long now=SystemClock.elapsedRealtime();if(now<connect4AiDue){postInvalidateDelayed(Math.max(16,connect4AiDue-now));return;}connect4AiDue=0;int col=chooseConnect4Ai();connect4Drop(col);}
    private int chooseConnect4Ai(){
        int bestCol=-1,bestScore=Integer.MIN_VALUE;
        int[] pref={3,2,4,1,5,0,6};
        for(int col:pref){
            int idx=connect4SimDrop(col,2);if(idx<0)continue;
            if(connect4Win(idx,2)){connect4[idx]=0;return col;}
            int score=connect4Evaluate(2);
            int worstReply=0;
            for(int oc:pref){int oi=connect4SimDrop(oc,1);if(oi<0)continue;int threat=connect4Win(oi,1)?100000:connect4Evaluate(1);connect4[oi]=0;worstReply=Math.max(worstReply,threat);}
            connect4[idx]=0;
            score-=worstReply*2;
            if(col==3)score+=18;
            if(score>bestScore){bestScore=score;bestCol=col;}
        }
        return bestCol>=0?bestCol:3;
    }

    private int connect4Evaluate(int who){
        int score=0,other=3-who;
        for(int r=0;r<6;r++)if(connect4[r*7+3]==who)score+=6;
        int[][] ds={{1,0},{0,1},{1,1},{1,-1}};
        for(int r=0;r<6;r++)for(int c=0;c<7;c++)for(int[] d:ds){int endR=r+d[1]*3,endC=c+d[0]*3;if(endR<0||endR>=6||endC<0||endC>=7)continue;int mine=0,theirs=0,empty=0;for(int k=0;k<4;k++){int v=connect4[(r+d[1]*k)*7+c+d[0]*k];if(v==who)mine++;else if(v==other)theirs++;else empty++;}if(theirs==0){if(mine==3&&empty==1)score+=80;else if(mine==2&&empty==2)score+=14;else if(mine==1&&empty==3)score+=2;}if(mine==0&&theirs==3&&empty==1)score-=95;}
        return score;
    }

    private int connect4SimDrop(int col,int who){for(int row=5;row>=0;row--){int idx=row*7+col;if(connect4[idx]==0){connect4[idx]=who;return idx;}}return -1;}

    // ---------- Reversi 6x6 ----------

    private void drawReversi(Canvas c){
        updateReversiAi();
        float s=minSide(),w=getWidth(),size=s*(roundScreen?.56f:.64f),left=(w-size)/2f,top=s*.205f,cell=size/6f;
        int black=0,white=0;for(int v:reversi){if(v==1)black++;else if(v==2)white++;}
        String sub=reversiOver?("黑 "+black+" · 白 "+white):(aiMode[REVERSI]?(reversiTurn==1?"单人 · 你执黑":"单人 · 手表思考中…"):("双人 · "+(reversiTurn==1?"黑方回合":"白方回合")));
        drawGameHeader(c,"黑白棋",sub,false);
        p.setColor(Color.rgb(41,126,78));c.drawRoundRect(new RectF(left,top,left+size,top+size),s*.025f,s*.025f,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,s*.003f));p.setColor(Color.rgb(25,76,50));for(int i=1;i<6;i++){c.drawLine(left+i*cell,top,left+i*cell,top+size,p);c.drawLine(left,top+i*cell,left+size,top+i*cell,p);}p.setStyle(Paint.Style.FILL);
        for(int i=0;i<36;i++){int col=i%6,row=i/6;float cx=left+(col+.5f)*cell,cy=top+(row+.5f)*cell;if(reversi[i]!=0){p.setColor(reversi[i]==1?Color.rgb(25,28,31):Color.rgb(240,242,240));c.drawCircle(cx,cy,cell*.36f,p);}else if(!reversiOver&&(!aiMode[REVERSI]||reversiTurn==1)&&reversiFlips(i,reversiTurn,null)>0){p.setColor(Color.argb(125,220,235,225));c.drawCircle(cx,cy,cell*.09f,p);}}
        drawBottomBar(c,new String[]{"菜单","重开"},false);
    }

    private void handleReversiTap(float x,float y){if(reversiOver||(aiMode[REVERSI]&&reversiTurn==2))return;float s=minSide(),size=s*(roundScreen?.56f:.64f),left=(getWidth()-size)/2f,top=s*.205f,cell=size/6f;if(x<left||x>=left+size||y<top||y>=top+size)return;int col=Math.min(5,(int)((x-left)/cell)),row=Math.min(5,(int)((y-top)/cell));reversiMove(row*6+col);}
    private boolean reversiMove(int idx){if(idx<0||idx>=36||reversi[idx]!=0)return false;List<Integer> flips=new ArrayList<>();if(reversiFlips(idx,reversiTurn,flips)==0)return false;int who=reversiTurn;reversi[idx]=who;for(int f:flips)reversi[f]=who;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);reversiTurn=3-who;if(!reversiHasMove(reversiTurn)){reversiTurn=who;if(!reversiHasMove(reversiTurn)){finishReversi();return true;}showToastHint("对方无棋可下，继续");}if(aiMode[REVERSI]&&reversiTurn==2)reversiAiDue=SystemClock.elapsedRealtime()+300;invalidate();return true;}
    private int reversiFlips(int idx,int who,List<Integer> out){if(reversi[idx]!=0)return 0;int r=idx/6,c=idx%6,total=0;int other=3-who;for(int dr=-1;dr<=1;dr++)for(int dc=-1;dc<=1;dc++){if(dr==0&&dc==0)continue;int rr=r+dr,cc=c+dc;List<Integer> line=new ArrayList<>();while(rr>=0&&rr<6&&cc>=0&&cc<6&&reversi[rr*6+cc]==other){line.add(rr*6+cc);rr+=dr;cc+=dc;}if(!line.isEmpty()&&rr>=0&&rr<6&&cc>=0&&cc<6&&reversi[rr*6+cc]==who){total+=line.size();if(out!=null)out.addAll(line);}}return total;}
    private boolean reversiHasMove(int who){for(int i=0;i<36;i++)if(reversi[i]==0&&reversiFlips(i,who,null)>0)return true;return false;}
    private void updateReversiAi(){if(!aiMode[REVERSI]||reversiOver||reversiTurn!=2||reversiAiDue<=0)return;long now=SystemClock.elapsedRealtime();if(now<reversiAiDue){postInvalidateDelayed(Math.max(16,reversiAiDue-now));return;}reversiAiDue=0;int[] weight={60,-18,8,6,-18,60,-18,-28,-4,-4,-28,-18,8,-4,5,5,-4,8,6,-4,5,5,-4,6,-18,-28,-4,-4,-28,-18,60,-18,8,6,-18,60};int best=-1,bestScore=Integer.MIN_VALUE;for(int i=0;i<36;i++){List<Integer> flips=new ArrayList<>();int n=reversiFlips(i,2,flips);if(n<=0)continue;reversi[i]=2;for(int f:flips)reversi[f]=2;int oppMobility=0;for(int j=0;j<36;j++)if(reversi[j]==0&&reversiFlips(j,1,null)>0)oppMobility++;int score=weight[i]+n*3-oppMobility*4;reversi[i]=0;for(int f:flips)reversi[f]=1;if(score>bestScore){bestScore=score;best=i;}}if(best>=0)reversiMove(best);}
    private void finishReversi(){reversiOver=true;int b=0,w=0;for(int v:reversi){if(v==1)b++;else if(v==2)w++;}if(b==w)showResult(2,"平局",b+" : "+w);else if(aiMode[REVERSI]){if(b>w)showResult(1,"你赢了！","黑 "+b+" · 白 "+w);else showResult(-1,"手表获胜","黑 "+b+" · 白 "+w);}else showResult(1,(b>w?"黑方":"白方")+"获胜！","黑 "+b+" · 白 "+w);}

    // ---------- Memory Match ----------

    private void drawMemoryMatch(Canvas c){
        updateMemoryMatch();float s=minSide(),w=getWidth(),size=s*(roundScreen?.54f:.62f),left=(w-size)/2f,top=s*.205f,gap=s*.012f,cell=(size-gap*3)/4f;
        drawGameHeader(c,"记忆配对",memoryWon?("完成 · "+memoryMoves+" 次翻牌"):("已配对 "+memoryPairs+" / 8 · "+memoryMoves+" 次"),false);
        String[] marks={"●","▲","■","◆","★","♥","月","日"};
        for(int i=0;i<16;i++){int col=i%4,row=i/4;float l=left+col*(cell+gap),t=top+row*(cell+gap);boolean open=memoryMatched[i]||i==memoryFirst||i==memorySecond;p.setColor(open?Color.rgb(66,82,103):Color.rgb(35,43,55));c.drawRoundRect(new RectF(l,t,l+cell,t+cell),cell*.17f,cell*.17f,p);if(open)text(c,marks[memoryCards[i]],l+cell/2,t+cell*.65f,cell*.39f,Color.WHITE,true);else{text(c,"?",l+cell/2,t+cell*.66f,cell*.38f,Color.rgb(115,132,153),true);}}
        if(memoryHideAt>0)scheduleFrame();drawBottomBar(c,new String[]{"菜单","重开"},false);
    }
    private void updateMemoryMatch(){if(memoryHideAt>0&&SystemClock.elapsedRealtime()>=memoryHideAt){memoryFirst=memorySecond=-1;memoryHideAt=0;invalidate();}}
    private void handleMemoryTap(float x,float y){updateMemoryMatch();if(memoryWon||memoryHideAt>0)return;float s=minSide(),size=s*(roundScreen?.54f:.62f),left=(getWidth()-size)/2f,top=s*.205f,gap=s*.012f,cell=(size-gap*3)/4f;if(x<left||x>=left+size||y<top||y>=top+size)return;int col=(int)((x-left)/(cell+gap)),row=(int)((y-top)/(cell+gap));if(col>3||row>3)return;float ix=(x-left)-col*(cell+gap),iy=(y-top)-row*(cell+gap);if(ix>cell||iy>cell)return;int idx=row*4+col;if(memoryMatched[idx]||idx==memoryFirst)return;if(memoryFirst<0){memoryFirst=idx;}else{memorySecond=idx;memoryMoves++;if(memoryCards[memoryFirst]==memoryCards[memorySecond]){memoryMatched[memoryFirst]=memoryMatched[memorySecond]=true;memoryPairs++;memoryFirst=memorySecond=-1;if(memoryPairs==8){memoryWon=true;showResult(1,"全部配对！","用了 "+memoryMoves+" 次翻牌");}}else memoryHideAt=SystemClock.elapsedRealtime()+650;}performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);invalidate();}

    // ---------- Maze ----------

    private void drawMaze(Canvas c){float s=minSide(),w=getWidth(),size=s*(roundScreen?.56f:.64f),left=(w-size)/2f,top=s*.205f,cell=size/9f;drawGameHeader(c,"迷宫逃脱",mazeWon?("到达出口 · "+mazeMoves+" 步"):("滑动移动 · "+mazeMoves+" 步"),false);p.setColor(Color.rgb(20,27,36));c.drawRoundRect(new RectF(left,top,left+size,top+size),s*.02f,s*.02f,p);p.setColor(Color.rgb(104,126,151));p.setStrokeWidth(Math.max(1.5f,s*.006f));for(int i=0;i<81;i++){int r=i/9,col=i%9;float l=left+col*cell,t=top+r*cell;if((mazeWalls[i]&1)!=0)c.drawLine(l,t,l+cell,t,p);if((mazeWalls[i]&2)!=0)c.drawLine(l+cell,t,l+cell,t+cell,p);if((mazeWalls[i]&4)!=0)c.drawLine(l,t+cell,l+cell,t+cell,p);if((mazeWalls[i]&8)!=0)c.drawLine(l,t,l,t+cell,p);}float ex=left+(mazeExit%9+.5f)*cell,ey=top+(mazeExit/9+.5f)*cell;p.setColor(Color.rgb(74,210,142));c.drawCircle(ex,ey,cell*.22f,p);float px=left+(mazePlayer%9+.5f)*cell,py=top+(mazePlayer/9+.5f)*cell;p.setColor(Color.rgb(83,154,255));c.drawCircle(px,py,cell*.28f,p);drawBottomBar(c,new String[]{"菜单","重开"},false);}
    private void handleMazeSwipe(int dir){if(mazeWon)return;int wall=dir==0?1:dir==1?2:dir==2?4:8;if((mazeWalls[mazePlayer]&wall)!=0){performHapticFeedback(HapticFeedbackConstants.REJECT);return;}if(dir==0)mazePlayer-=9;else if(dir==1)mazePlayer++;else if(dir==2)mazePlayer+=9;else mazePlayer--;mazeMoves++;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);if(mazePlayer==mazeExit){mazeWon=true;showResult(1,"走出迷宫！","用了 "+mazeMoves+" 步");}invalidate();}

    // ---------- Number Tap ----------

    private void drawNumberTap(Canvas c){float s=minSide(),w=getWidth(),size=s*(roundScreen?.54f:.62f),left=(w-size)/2f,top=s*.205f,gap=s*.012f,cell=(size-gap*3)/4f;long elapsed=Math.max(0,(numberTapOver&&numberTapFinish>0?numberTapFinish:SystemClock.elapsedRealtime())-numberTapStart);drawGameHeader(c,"数字连点",numberTapOver?("完成 · "+(elapsed/1000f)+" 秒"):("下一个 "+(numberTapNext+1)+" · "+String.format("%.1f",elapsed/1000f)+" 秒"),false);for(int i=0;i<16;i++){int col=i%4,row=i/4;float l=left+col*(cell+gap),t=top+row*(cell+gap);int n=numberTapOrder[i];boolean done=n<=numberTapNext;p.setColor(done?Color.rgb(28,59,49):Color.rgb(42,51,65));c.drawRoundRect(new RectF(l,t,l+cell,t+cell),cell*.17f,cell*.17f,p);text(c,String.valueOf(n),l+cell/2,t+cell*.65f,cell*.34f,done?Color.rgb(84,188,131):Color.WHITE,true);}if(!numberTapOver)postInvalidateDelayed(100);drawBottomBar(c,new String[]{"菜单","重开"},false);}
    private void handleNumberTap(float x,float y){if(numberTapOver)return;float s=minSide(),size=s*(roundScreen?.54f:.62f),left=(getWidth()-size)/2f,top=s*.205f,gap=s*.012f,cell=(size-gap*3)/4f;if(x<left||x>=left+size||y<top||y>=top+size)return;int col=(int)((x-left)/(cell+gap)),row=(int)((y-top)/(cell+gap));if(col>3||row>3)return;int idx=row*4+col;if(numberTapOrder[idx]!=numberTapNext+1){performHapticFeedback(HapticFeedbackConstants.REJECT);return;}numberTapNext++;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);if(numberTapNext>=16){numberTapOver=true;numberTapFinish=SystemClock.elapsedRealtime();long ms=numberTapFinish-numberTapStart;showResult(1,"16 个数字完成！",String.format("%.1f 秒",ms/1000f));}invalidate();}

    // ---------- Quick Math ----------

    private void drawQuickMath(Canvas c){long now=SystemClock.elapsedRealtime(),remain=Math.max(0,mathDeadline-now);if(!mathOver&&remain==0){mathOver=true;showResult(mathScore>=10?1:2,"时间到","答对 "+mathScore+" · 答错 "+mathWrong);}drawGameHeader(c,"极速心算",mathOver?("得分 "+mathScore):("剩余 "+((remain+999)/1000)+" 秒 · "+mathScore+" 分"),false);float s=minSide(),w=getWidth();text(c,mathA+mathOperator()+mathB+" = ?",w/2,s*.34f,s*.075f,Color.WHITE,true);float bw=s*.25f,bh=s*.13f,gap=s*.025f,left=w/2-bw-gap/2,top=s*.43f;for(int i=0;i<4;i++){int col=i%2,row=i/2;RectF r=new RectF(left+col*(bw+gap),top+row*(bh+gap),left+col*(bw+gap)+bw,top+row*(bh+gap)+bh);p.setColor(Color.rgb(41,50,64));c.drawRoundRect(r,bh*.28f,bh*.28f,p);text(c,String.valueOf(mathChoices[i]),r.centerX(),r.centerY()+bh*.12f,bh*.38f,Color.WHITE,true);}if(!mathOver)postInvalidateDelayed(100);drawBottomBar(c,new String[]{"菜单","重开"},false);}
    private String mathOperator(){return mathOp==0?" + ":mathOp==1?" − ":" × ";}
    private void handleQuickMath(float x,float y){if(mathOver)return;float s=minSide(),w=getWidth(),bw=s*.25f,bh=s*.13f,gap=s*.025f,left=w/2-bw-gap/2,top=s*.43f;for(int i=0;i<4;i++){int col=i%2,row=i/2;RectF r=new RectF(left+col*(bw+gap),top+row*(bh+gap),left+col*(bw+gap)+bw,top+row*(bh+gap)+bh);if(r.contains(x,y)){if(mathChoices[i]==mathAnswer){mathScore++;performHapticFeedback(HapticFeedbackConstants.CONFIRM);}else{mathWrong++;performHapticFeedback(HapticFeedbackConstants.REJECT);}newMathProblem();invalidate();return;}}}
    private void newMathProblem(){int type=random.nextInt(3);mathOp=type;if(type==0){mathA=2+random.nextInt(18);mathB=1+random.nextInt(15);mathAnswer=mathA+mathB;}else if(type==1){mathA=5+random.nextInt(20);mathB=1+random.nextInt(mathA);mathAnswer=mathA-mathB;}else{mathA=2+random.nextInt(8);mathB=2+random.nextInt(8);mathAnswer=mathA*mathB;}for(int i=0;i<4;i++)mathChoices[i]=mathAnswer;int correct=random.nextInt(4);for(int i=0;i<4;i++)if(i!=correct){int v;do{v=Math.max(0,mathAnswer-5+random.nextInt(11));}while(v==mathAnswer||contains(mathChoices,v,i));mathChoices[i]=v;}mathChoices[correct]=mathAnswer;}
    private boolean contains(int[] a,int v,int before){for(int i=0;i<before;i++)if(a[i]==v)return true;return false;}

    // ---------- Nim ----------

    private void drawNim(Canvas c){updateNimAi();float s=minSide(),w=getWidth();String sub=nimOver?"对局结束":(aiMode[NIM]?(nimTurn==1?"单人 · 轮到你":"单人 · 手表思考中…"):("双人 · 玩家 "+nimTurn+" 回合"));drawGameHeader(c,"Nim 取石",sub,false);text(c,"剩余 "+nimStones+" 颗",w/2,s*.26f,s*.050f,Color.WHITE,true);float startX=w/2-s*.26f,startY=s*.32f;for(int i=0;i<nimStones;i++){int col=i%7,row=i/7;float cx=startX+col*s*.087f,cy=startY+row*s*.073f;p.setColor(Color.rgb(255,188,74));c.drawCircle(cx,cy,s*.023f,p);}float bw=s*.16f,bh=s*.12f,gap=s*.018f,total=bw*3+gap*2,left=(w-total)/2f,top=s*.55f;for(int i=0;i<3;i++){RectF r=new RectF(left+i*(bw+gap),top,left+i*(bw+gap)+bw,top+bh);p.setColor(Color.rgb(42,51,65));c.drawRoundRect(r,bh*.34f,bh*.34f,p);text(c,"取 "+(i+1),r.centerX(),r.centerY()+bh*.10f,bh*.29f,Color.WHITE,true);}drawBottomBar(c,new String[]{"菜单","重开"},false);}
    private void handleNimTap(float x,float y){if(nimOver||(aiMode[NIM]&&nimTurn==2))return;float s=minSide(),w=getWidth(),bw=s*.16f,bh=s*.12f,gap=s*.018f,total=bw*3+gap*2,left=(w-total)/2f,top=s*.55f;if(y<top||y>top+bh)return;for(int i=0;i<3;i++){RectF r=new RectF(left+i*(bw+gap),top,left+i*(bw+gap)+bw,top+bh);if(r.contains(x,y)){nimTake(i+1);return;}}}
    private void nimTake(int n){n=Math.min(n,nimStones);nimStones-=n;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);if(nimStones<=0){nimOver=true;if(aiMode[NIM]&&nimTurn==2)showResult(-1,"手表拿到最后一颗","这局由手表获胜");else showResult(1,(aiMode[NIM]?"你":"玩家 "+nimTurn)+"获胜！","拿到最后一颗石子");return;}nimTurn=3-nimTurn;if(aiMode[NIM]&&nimTurn==2)nimAiDue=SystemClock.elapsedRealtime()+350;invalidate();}
    private void updateNimAi(){if(!aiMode[NIM]||nimOver||nimTurn!=2||nimAiDue<=0)return;long now=SystemClock.elapsedRealtime();if(now<nimAiDue){postInvalidateDelayed(Math.max(16,nimAiDue-now));return;}nimAiDue=0;int take=nimStones%4;if(take==0)take=1+random.nextInt(Math.min(3,nimStones));nimTake(take);}

    // ---------- Pong ----------

    private RectF pongBoard(){float s=minSide(),size=s*(roundScreen?.52f:.60f);return new RectF((getWidth()-size)/2f,s*.205f,(getWidth()+size)/2f,s*.205f+size);}
    private void drawPong(Canvas c){RectF b=pongBoard();if(pongX==0&&b.width()>0)initPongGeometry();updatePong(b);String sub=aiMode[PONG]?"单人 · 下方挡板是你":"双人 · 上下半屏各控制一方";drawGameHeader(c,"腕上乒乓",pongOver?("比分 "+pongBottomScore+" : "+pongTopScore):sub,false);p.setColor(Color.rgb(17,25,35));c.drawRoundRect(b,minSide()*.025f,minSide()*.025f,p);p.setColor(Color.rgb(64,78,96));p.setStrokeWidth(minSide()*.004f);c.drawLine(b.left,b.centerY(),b.right,b.centerY(),p);float pw=b.width()*.30f,ph=minSide()*.025f;p.setColor(Color.WHITE);c.drawRoundRect(new RectF(pongBottomX-pw/2,b.bottom-ph*2,pongBottomX+pw/2,b.bottom-ph),ph/2,ph/2,p);c.drawRoundRect(new RectF(pongTopX-pw/2,b.top+ph,pongTopX+pw/2,b.top+ph*2),ph/2,ph/2,p);p.setColor(Color.rgb(88,191,255));c.drawCircle(pongX,pongY,minSide()*.018f,p);text(c,pongTopScore+"",b.centerX(),b.top+minSide()*.09f,minSide()*.040f,Color.rgb(145,160,180),true);text(c,pongBottomScore+"",b.centerX(),b.bottom-minSide()*.055f,minSide()*.040f,Color.rgb(145,160,180),true);if(!pongRunning&&!pongOver)text(c,"拖动挡板开始",b.centerX(),b.centerY()+minSide()*.012f,minSide()*.029f,Color.rgb(185,196,210),true);if(pongRunning)scheduleFrame();drawBottomBar(c,new String[]{"菜单","重开"},false);}
    private void handlePongTouch(float x,float y){if(pongOver)return;RectF b=pongBoard();float pw=b.width()*.30f;if(aiMode[PONG]||y>=b.centerY())pongBottomX=clamp(x,b.left+pw/2,b.right-pw/2);else pongTopX=clamp(x,b.left+pw/2,b.right-pw/2);if(!pongRunning){pongRunning=true;pongLastTick=SystemClock.elapsedRealtime();}invalidate();}
    private void updatePong(RectF b){if(!pongRunning||pongOver)return;long now=SystemClock.elapsedRealtime();if(pongLastTick==0)pongLastTick=now;float dt=Math.min(.035f,(now-pongLastTick)/1000f);pongLastTick=now;if(aiMode[PONG]){float pw=b.width()*.30f;pongTopX=clamp(pongTopX+(pongX-pongTopX)*Math.min(1f,dt*3.4f),b.left+pw/2,b.right-pw/2);}pongX+=pongVx*dt;pongY+=pongVy*dt;float r=minSide()*.018f;if(pongX-r<b.left){pongX=b.left+r;pongVx=Math.abs(pongVx);}if(pongX+r>b.right){pongX=b.right-r;pongVx=-Math.abs(pongVx);}float pw=b.width()*.30f,ph=minSide()*.025f;if(pongVy>0&&pongY+r>=b.bottom-ph*2&&pongY-r<=b.bottom-ph&&Math.abs(pongX-pongBottomX)<=pw*.58f){pongY=b.bottom-ph*2-r;pongVy=-Math.abs(pongVy)*1.025f;pongVx+=(pongX-pongBottomX)*1.2f;}if(pongVy<0&&pongY-r<=b.top+ph*2&&pongY+r>=b.top+ph&&Math.abs(pongX-pongTopX)<=pw*.58f){pongY=b.top+ph*2+r;pongVy=Math.abs(pongVy)*1.025f;pongVx+=(pongX-pongTopX)*1.2f;}if(pongY>b.bottom+r){pongTopScore++;resetPongBall(-1);}else if(pongY<b.top-r){pongBottomScore++;resetPongBall(1);}if(pongBottomScore>=5||pongTopScore>=5){pongOver=true;pongRunning=false;if(aiMode[PONG]){if(pongBottomScore>pongTopScore)showResult(1,"你赢了！",pongBottomScore+" : "+pongTopScore);else showResult(-1,"手表获胜",pongBottomScore+" : "+pongTopScore);}else showResult(1,(pongBottomScore>pongTopScore?"下方玩家":"上方玩家")+"获胜！",pongBottomScore+" : "+pongTopScore);}}
    private void resetPongBall(int direction){RectF b=pongBoard();pongX=b.centerX();pongY=b.centerY();float speed=minSide()*.50f;pongVx=(random.nextBoolean()?1:-1)*speed*.48f;pongVy=direction*speed;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);}

    // ---------- Brick Breaker ----------

    private RectF brickBoard(){float s=minSide(),size=s*(roundScreen?.52f:.60f);return new RectF((getWidth()-size)/2f,s*.205f,(getWidth()+size)/2f,s*.205f+size);}
    private void drawBrickBreaker(Canvas c){RectF b=brickBoard();if(brickBallX==0&&b.width()>0)initBrickGeometry();updateBrickBreaker(b);drawGameHeader(c,"砖块破坏",brickWon?"清空全部砖块":brickOver?"球掉下去了":"剩余 "+brickLeft+" 块 · "+brickScore+" 分",false);p.setColor(Color.rgb(16,23,32));c.drawRoundRect(b,minSide()*.025f,minSide()*.025f,p);int cols=6,rows=4;float gap=minSide()*.006f,bw=(b.width()-gap*(cols+1))/cols,bh=minSide()*.055f;int[] cs={Color.rgb(255,105,113),Color.rgb(255,174,74),Color.rgb(89,191,255),Color.rgb(82,211,145)};for(int i=0;i<24;i++)if(bricks[i]){int col=i%6,row=i/6;float l=b.left+gap+col*(bw+gap),t=b.top+gap+row*(bh+gap);p.setColor(cs[row]);c.drawRoundRect(new RectF(l,t,l+bw,t+bh),bh*.22f,bh*.22f,p);}float pw=b.width()*.32f,ph=minSide()*.025f;p.setColor(Color.WHITE);c.drawRoundRect(new RectF(brickPaddleX-pw/2,b.bottom-ph*2,brickPaddleX+pw/2,b.bottom-ph),ph/2,ph/2,p);p.setColor(Color.rgb(255,224,98));c.drawCircle(brickBallX,brickBallY,minSide()*.018f,p);if(!brickRunning&&!brickOver)text(c,"拖动挡板开始",b.centerX(),b.centerY()+minSide()*.09f,minSide()*.029f,Color.rgb(185,196,210),true);if(brickRunning)scheduleFrame();drawBottomBar(c,new String[]{"菜单","重开"},false);}
    private void handleBrickTouch(float x){if(brickOver)return;RectF b=brickBoard();float pw=b.width()*.32f;brickPaddleX=clamp(x,b.left+pw/2,b.right-pw/2);if(!brickRunning){brickRunning=true;brickLastTick=SystemClock.elapsedRealtime();}invalidate();}
    private void updateBrickBreaker(RectF b){if(!brickRunning||brickOver)return;long now=SystemClock.elapsedRealtime();if(brickLastTick==0)brickLastTick=now;float dt=Math.min(.032f,(now-brickLastTick)/1000f);brickLastTick=now;brickBallX+=brickVx*dt;brickBallY+=brickVy*dt;float r=minSide()*.018f;if(brickBallX-r<b.left){brickBallX=b.left+r;brickVx=Math.abs(brickVx);}if(brickBallX+r>b.right){brickBallX=b.right-r;brickVx=-Math.abs(brickVx);}if(brickBallY-r<b.top){brickBallY=b.top+r;brickVy=Math.abs(brickVy);}float pw=b.width()*.32f,ph=minSide()*.025f;if(brickVy>0&&brickBallY+r>=b.bottom-ph*2&&brickBallY-r<=b.bottom-ph&&Math.abs(brickBallX-brickPaddleX)<=pw*.56f){brickBallY=b.bottom-ph*2-r;brickVy=-Math.abs(brickVy);brickVx+=(brickBallX-brickPaddleX)*1.4f;}int cols=6;float gap=minSide()*.006f,bw=(b.width()-gap*(cols+1))/cols,bh=minSide()*.055f;for(int i=0;i<24;i++)if(bricks[i]){int col=i%6,row=i/6;RectF rr=new RectF(b.left+gap+col*(bw+gap),b.top+gap+row*(bh+gap),b.left+gap+col*(bw+gap)+bw,b.top+gap+row*(bh+gap)+bh);if(brickBallX+r>=rr.left&&brickBallX-r<=rr.right&&brickBallY+r>=rr.top&&brickBallY-r<=rr.bottom){bricks[i]=false;brickLeft--;brickScore+=10;brickVy=-brickVy;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);if(brickLeft==0){brickWon=true;brickOver=true;brickRunning=false;showResult(1,"砖块清空！",brickScore+" 分");}break;}}if(!brickOver&&brickBallY-r>b.bottom){brickOver=true;brickRunning=false;showResult(-1,"球掉下去了","击碎 "+(24-brickLeft)+" 块砖");}}

    // ---------- Dice Duel ----------

    private void drawDiceDuel(Canvas c){float s=minSide(),w=getWidth();String sub=diceOver?"对局结束":(aiMode[DICE_DUEL]?"单人 · 先到 20 分":"双人 · 玩家 "+diceTurn+" 掷骰");drawGameHeader(c,"骰子对决",sub,false);text(c,"P1  "+diceP1Score+"  :  "+diceP2Score+"  P2",w/2,s*.25f,s*.042f,Color.WHITE,true);float box=s*.19f;drawDie(c,w*.35f,s*.41f,box,diceP1);drawDie(c,w*.65f,s*.41f,box,diceP2);RectF roll=new RectF(w/2-s*.20f,s*.55f,w/2+s*.20f,s*.68f);p.setColor(Color.rgb(68,114,245));c.drawRoundRect(roll,roll.height()/2,roll.height()/2,p);text(c,diceOver?"本局结束":"掷骰子",roll.centerX(),roll.centerY()+roll.height()*.11f,roll.height()*.30f,Color.WHITE,true);drawBottomBar(c,new String[]{"菜单","重开"},false);}
    private void drawDie(Canvas c,float cx,float cy,float side,int value){RectF r=new RectF(cx-side/2,cy-side/2,cx+side/2,cy+side/2);p.setColor(Color.rgb(239,242,247));c.drawRoundRect(r,side*.18f,side*.18f,p);if(value<=0)return;float o=side*.23f,rad=side*.055f;p.setColor(Color.rgb(39,45,56));boolean center=value%2==1;if(center)c.drawCircle(cx,cy,rad,p);if(value>=2){c.drawCircle(cx-o,cy-o,rad,p);c.drawCircle(cx+o,cy+o,rad,p);}if(value>=4){c.drawCircle(cx+o,cy-o,rad,p);c.drawCircle(cx-o,cy+o,rad,p);}if(value==6){c.drawCircle(cx-o,cy,rad,p);c.drawCircle(cx+o,cy,rad,p);}}
    private void handleDiceTap(float x,float y){if(diceOver)return;float s=minSide(),w=getWidth();RectF roll=new RectF(w/2-s*.20f,s*.55f,w/2+s*.20f,s*.68f);if(!roll.contains(x,y))return;if(aiMode[DICE_DUEL]){diceP1=1+random.nextInt(6);diceP1Score+=diceP1;if(diceP1Score<20){diceP2=1+random.nextInt(6);diceP2Score+=diceP2;}finishDiceIfNeeded();}else{int v=1+random.nextInt(6);if(diceTurn==1){diceP1=v;diceP1Score+=v;}else{diceP2=v;diceP2Score+=v;}if(!finishDiceIfNeeded())diceTurn=3-diceTurn;}performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);invalidate();}
    private boolean finishDiceIfNeeded(){if(diceP1Score<20&&diceP2Score<20)return false;diceOver=true;if(diceP1Score==diceP2Score)showResult(2,"平局",diceP1Score+" : "+diceP2Score);else if(aiMode[DICE_DUEL]){if(diceP1Score>diceP2Score)showResult(1,"你先到 20 分！",diceP1Score+" : "+diceP2Score);else showResult(-1,"手表先到 20 分",diceP1Score+" : "+diceP2Score);}else showResult(1,(diceP1Score>diceP2Score?"玩家 1":"玩家 2")+"获胜！",diceP1Score+" : "+diceP2Score);return true;}

    // ---------- 4x4 Sudoku ----------

    private void drawSudoku(Canvas c){
        float s=minSide(),w=getWidth(),size=s*(roundScreen?.50f:.58f),left=(w-size)/2f,top=s*.175f,cell=size/4f;
        String sub=sudokuWon?("完成 · 冲突次数 "+sudokuMistakes):(sudokuSelected>=0?"填 1–4，× 可清除":"点任意空格开始");
        drawGameHeader(c,"4×4 数独",sub,false);
        p.setColor(Color.rgb(25,32,42));c.drawRoundRect(new RectF(left,top,left+size,top+size),s*.025f,s*.025f,p);
        for(int i=0;i<16;i++){
            int col=i%4,row=i/4;RectF r=new RectF(left+col*cell,top+row*cell,left+(col+1)*cell,top+(row+1)*cell);
            if(i==sudokuSelected){p.setColor(Color.rgb(48,76,112));c.drawRect(r,p);}
            if(sudoku[i]!=0){
                int color=sudokuFixed[i]?Color.rgb(205,214,225):(sudokuInvalidAt(i)?Color.rgb(255,104,112):Color.rgb(92,177,255));
                text(c,String.valueOf(sudoku[i]),r.centerX(),r.centerY()+cell*.16f,cell*.43f,color,true);
            }
        }
        p.setStyle(Paint.Style.STROKE);
        for(int i=0;i<=4;i++){
            p.setStrokeWidth(i%2==0?s*.009f:s*.003f);p.setColor(i%2==0?Color.rgb(116,132,153):Color.rgb(69,82,100));
            c.drawLine(left+i*cell,top,left+i*cell,top+size,p);c.drawLine(left,top+i*cell,left+size,top+i*cell,p);
        }
        p.setStyle(Paint.Style.FILL);
        float by=top+size+s*.026f,bh=s*.092f,gap=s*.009f,bw=s*(roundScreen?.086f:.095f),total=bw*5+gap*4,bx=(w-total)/2f;
        for(int i=0;i<5;i++){
            RectF r=new RectF(bx+i*(bw+gap),by,bx+i*(bw+gap)+bw,by+bh);boolean clear=i==4;
            p.setColor(clear?Color.rgb(55,61,72):Color.rgb(42,51,65));c.drawRoundRect(r,bh*.36f,bh*.36f,p);
            text(c,clear?"×":String.valueOf(i+1),r.centerX(),r.centerY()+bh*.12f,bh*.34f,Color.WHITE,true);
        }
        drawBottomBar(c,new String[]{"菜单","重开"},false);
    }

    private boolean sudokuInvalidAt(int idx){
        int value=sudoku[idx];if(value==0)return false;int row=idx/4,col=idx%4;
        for(int c=0;c<4;c++){int j=row*4+c;if(j!=idx&&sudoku[j]==value)return true;}
        for(int r=0;r<4;r++){int j=r*4+col;if(j!=idx&&sudoku[j]==value)return true;}
        int br=(row/2)*2,bc=(col/2)*2;
        for(int r=br;r<br+2;r++)for(int c=bc;c<bc+2;c++){int j=r*4+c;if(j!=idx&&sudoku[j]==value)return true;}
        return false;
    }

    private boolean sudokuCompleteAndValid(){
        for(int i=0;i<16;i++)if(sudoku[i]==0||sudokuInvalidAt(i))return false;
        return true;
    }

    private void handleSudokuTap(float x,float y){
        if(sudokuWon)return;
        float s=minSide(),w=getWidth(),size=s*(roundScreen?.50f:.58f),left=(w-size)/2f,top=s*.175f,cell=size/4f;
        if(x>=left&&x<left+size&&y>=top&&y<top+size){
            int col=Math.min(3,(int)((x-left)/cell)),row=Math.min(3,(int)((y-top)/cell)),idx=row*4+col;
            if(!sudokuFixed[idx]){sudokuSelected=idx;showToastHint(sudoku[idx]==0?"选择下方 1–4 填入":"可改数字或点 × 清除");}
            else showToastHint("题目数字不能修改");
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);invalidate();return;
        }
        float by=top+size+s*.026f,bh=s*.092f,gap=s*.009f,bw=s*(roundScreen?.086f:.095f),total=bw*5+gap*4,bx=(w-total)/2f;
        if(sudokuSelected<0||y<by||y>by+bh)return;
        for(int i=0;i<5;i++){
            RectF r=new RectF(bx+i*(bw+gap),by,bx+i*(bw+gap)+bw,by+bh);
            if(r.contains(x,y)){
                if(i==4){sudoku[sudokuSelected]=0;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);}
                else {sudoku[sudokuSelected]=i+1;if(sudokuInvalidAt(sudokuSelected)){sudokuMistakes++;performHapticFeedback(HapticFeedbackConstants.REJECT);showToastHint("数字冲突，可继续修改");}else performHapticFeedback(HapticFeedbackConstants.CONFIRM);}
                if(sudokuCompleteAndValid()){sudokuWon=true;sudokuSelected=-1;showResult(1,"数独完成！","冲突次数 "+sudokuMistakes);}
                invalidate();return;
            }
        }
    }


    // ---------- Block Drop (Tetris-style) ----------

    private static final int[][] BLOCK_MASKS = {
            {0x0F00,0x2222,0x00F0,0x4444}, // I
            {0x0660,0x0660,0x0660,0x0660}, // O
            {0x0E40,0x4C40,0x4E00,0x4640}, // T
            {0x06C0,0x8C40,0x06C0,0x8C40}, // S
            {0x0C60,0x4C80,0x0C60,0x4C80}, // Z
            {0x08E0,0x6440,0x0E20,0x44C0}, // J
            {0x02E0,0x4460,0x0E80,0xC440}  // L
    };

    private boolean blockMaskCell(int mask,int x,int y){int bit=15-(y*4+x);return bit>=0&&bit<16&&((mask>>bit)&1)==1;}
    private boolean blockFits(int x,int y,int rot){int mask=BLOCK_MASKS[blockType][rot&3];for(int py=0;py<4;py++)for(int px=0;px<4;px++)if(blockMaskCell(mask,px,py)){int bx=x+px,by=y+py;if(bx<0||bx>=BLOCK_W||by>=BLOCK_H)return false;if(by>=0&&blockBoard[by][bx]!=0)return false;}return true;}
    private void spawnBlock(){blockType=random.nextInt(BLOCK_MASKS.length);blockRot=0;blockX=3;blockY=-1;blockNextTick=SystemClock.elapsedRealtime()+520;if(!blockFits(blockX,blockY,blockRot)){blockOver=true;showResult(-1,"堆到顶部了","得分 "+blockScore+" · 消除 "+blockLines+" 行");}}
    private void lockBlock(){int mask=BLOCK_MASKS[blockType][blockRot&3];for(int py=0;py<4;py++)for(int px=0;px<4;px++)if(blockMaskCell(mask,px,py)){int bx=blockX+px,by=blockY+py;if(by>=0&&by<BLOCK_H&&bx>=0&&bx<BLOCK_W)blockBoard[by][bx]=blockType+1;}int cleared=0;for(int y=BLOCK_H-1;y>=0;y--){boolean full=true;for(int x=0;x<BLOCK_W;x++)if(blockBoard[y][x]==0){full=false;break;}if(full){cleared++;for(int yy=y;yy>0;yy--)System.arraycopy(blockBoard[yy-1],0,blockBoard[yy],0,BLOCK_W);Arrays.fill(blockBoard[0],0);y++;}}if(cleared>0){blockLines+=cleared;blockScore+=new int[]{0,100,300,500,800}[cleared];performHapticFeedback(HapticFeedbackConstants.CONFIRM);}spawnBlock();}
    private void blockStep(){if(blockOver)return;if(blockFits(blockX,blockY+1,blockRot))blockY++;else lockBlock();}
    private void updateBlockDrop(){if(blockOver)return;long now=SystemClock.elapsedRealtime();if(now>=blockNextTick){blockStep();long speed=Math.max(180,520-blockLines*14L);blockNextTick=now+speed;}scheduleFrame();}
    private void drawBlockDrop(Canvas c){updateBlockDrop();float s=minSide(),available=bottomBarTop()-s*.20f,cell=Math.min(s*.040f,available/BLOCK_H),bw=cell*BLOCK_W,bh=cell*BLOCK_H,left=(getWidth()-bw)/2f,top=s*.155f;drawGameHeader(c,"俄罗斯方块",blockOver?("结束 · "+blockScore+" 分"):("得分 "+blockScore+" · "+blockLines+" 行"),false);p.setColor(Color.rgb(17,24,33));c.drawRoundRect(new RectF(left-s*.01f,top-s*.01f,left+bw+s*.01f,top+bh+s*.01f),s*.025f,s*.025f,p);int[] colors={Color.TRANSPARENT,Color.rgb(70,202,235),Color.rgb(255,212,74),Color.rgb(185,102,255),Color.rgb(80,213,136),Color.rgb(255,95,111),Color.rgb(80,132,250),Color.rgb(255,151,70)};for(int y=0;y<BLOCK_H;y++)for(int x=0;x<BLOCK_W;x++){int v=blockBoard[y][x];if(v!=0)drawBlockCell(c,left+x*cell,top+y*cell,cell,colors[v]);}if(!blockOver){int mask=BLOCK_MASKS[blockType][blockRot&3];for(int py=0;py<4;py++)for(int px=0;px<4;px++)if(blockMaskCell(mask,px,py)){int x=blockX+px,y=blockY+py;if(y>=0)drawBlockCell(c,left+x*cell,top+y*cell,cell,colors[blockType+1]);}}float hintY=Math.min(bottomBarTop()-s*.035f,top+bh+s*.045f);text(c,"左侧◀  中间旋转  右侧▶ · 下滑直落",getWidth()/2f,hintY,s*.020f,Color.rgb(161,174,191),false);drawBottomBar(c,new String[]{"菜单","重开"},false);}
    private void drawBlockCell(Canvas c,float l,float t,float cell,int color){p.setColor(color);float g=Math.max(1f,cell*.08f);c.drawRoundRect(new RectF(l+g,t+g,l+cell-g,t+cell-g),cell*.18f,cell*.18f,p);}
    private void handleBlockTap(float x,float y){if(blockOver)return;if(x<getWidth()*.36f){if(blockFits(blockX-1,blockY,blockRot))blockX--;}else if(x>getWidth()*.64f){if(blockFits(blockX+1,blockY,blockRot))blockX++;}else{int nr=(blockRot+1)&3;if(blockFits(blockX,blockY,nr))blockRot=nr;else if(blockFits(blockX-1,blockY,nr)){blockX--;blockRot=nr;}else if(blockFits(blockX+1,blockY,nr)){blockX++;blockRot=nr;}}performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);invalidate();}
    private void hardDropBlock(){if(blockOver)return;while(blockFits(blockX,blockY+1,blockRot)){blockY++;blockScore+=2;}lockBlock();invalidate();}

    // ---------- Flappy ----------

    private RectF flappyBoard(){float s=minSide(),w=getWidth();return new RectF(w*(roundScreen?.18f:.12f),s*.19f,w*(roundScreen?.82f:.88f),bottomBarTop()-s*.06f);}
    private void drawFlappy(Canvas c){RectF b=flappyBoard();if(b.width()>0&&(flappyPipeX<=0||flappyY<=0)){flappyY=b.centerY();flappyPipeX=b.right;flappyGapY=b.centerY();flappyV=0;}updateFlappy(b);drawGameHeader(c,"跳跃小鸟",flappyOver?("结束 · "+flappyScore+" 分"):(flappyRunning?(flappyScore+" 分"):"轻点开始"),false);p.setColor(Color.rgb(22,38,54));c.drawRoundRect(b,minSide()*.025f,minSide()*.025f,p);float pipeW=b.width()*.17f,gap=b.height()*.30f;p.setColor(Color.rgb(67,195,111));c.drawRoundRect(new RectF(flappyPipeX,b.top,flappyPipeX+pipeW,flappyGapY-gap/2),minSide()*.015f,minSide()*.015f,p);c.drawRoundRect(new RectF(flappyPipeX,flappyGapY+gap/2,flappyPipeX+pipeW,b.bottom),minSide()*.015f,minSide()*.015f,p);float birdX=b.left+b.width()*.28f,r=minSide()*.026f;p.setColor(Color.rgb(255,205,69));c.drawCircle(birdX,flappyY,r,p);p.setColor(Color.WHITE);c.drawCircle(birdX+r*.35f,flappyY-r*.25f,r*.22f,p);if(!flappyRunning&&!flappyOver)text(c,"轻点任意位置起飞",b.centerX(),b.centerY(),minSide()*.030f,Color.WHITE,true);if(flappyRunning)scheduleFrame();drawBottomBar(c,new String[]{"菜单","重开"},false);}
    private void handleFlappyTap(){if(flappyOver)return;if(!flappyRunning){flappyRunning=true;flappyLastTick=SystemClock.elapsedRealtime();}flappyV=-minSide()*.48f;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);invalidate();}
    private void updateFlappy(RectF b){if(!flappyRunning||flappyOver)return;long now=SystemClock.elapsedRealtime();float dt=Math.min(.035f,(now-flappyLastTick)/1000f);flappyLastTick=now;flappyV+=minSide()*1.25f*dt;flappyY+=flappyV*dt;flappyPipeX-=minSide()*.24f*dt;float pipeW=b.width()*.17f,birdX=b.left+b.width()*.28f,r=minSide()*.026f,gap=b.height()*.30f;if(flappyPipeX+pipeW<b.left){flappyPipeX=b.right;flappyGapY=b.top+b.height()*(.30f+random.nextFloat()*.40f);flappyScore++;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);}boolean hitPipe=birdX+r>flappyPipeX&&birdX-r<flappyPipeX+pipeW&&(flappyY-r<flappyGapY-gap/2||flappyY+r>flappyGapY+gap/2);if(flappyY-r<b.top||flappyY+r>b.bottom||hitPipe){flappyOver=true;flappyRunning=false;showResult(-1,"撞到了","本局 "+flappyScore+" 分");}}

    // ---------- Whack-a-mole ----------

    private void drawWhackMole(Canvas c){long now=SystemClock.elapsedRealtime();if(!moleOver&&now>=moleDeadline){moleOver=true;showResult(moleScore>=18?1:2,"时间到","得分 "+moleScore+" · 连击最高靠手速");}if(!moleOver&&now>=moleNextMove){moleIndex=random.nextInt(9);moleNextMove=now+Math.max(360,760-moleScore*12L);scheduleFrame();}float s=minSide(),w=getWidth(),size=s*(roundScreen?.53f:.60f),left=(w-size)/2f,top=s*.205f,gap=s*.014f,cell=(size-gap*2)/3f;drawGameHeader(c,"打地鼠",moleOver?("得分 "+moleScore):("剩余 "+Math.max(0,(moleDeadline-now+999)/1000)+" 秒 · "+moleScore+" 分"),false);for(int i=0;i<9;i++){int col=i%3,row=i/3;RectF r=new RectF(left+col*(cell+gap),top+row*(cell+gap),left+col*(cell+gap)+cell,top+row*(cell+gap)+cell);p.setColor(Color.rgb(35,44,57));c.drawRoundRect(r,cell*.26f,cell*.26f,p);if(i==moleIndex&&!moleOver){p.setColor(Color.rgb(202,136,84));c.drawCircle(r.centerX(),r.centerY(),cell*.28f,p);p.setColor(Color.rgb(45,34,29));c.drawCircle(r.centerX()-cell*.10f,r.centerY()-cell*.06f,cell*.035f,p);c.drawCircle(r.centerX()+cell*.10f,r.centerY()-cell*.06f,cell*.035f,p);}}if(!moleOver)postInvalidateDelayed(100);drawBottomBar(c,new String[]{"菜单","重开"},false);}
    private void handleWhackMoleTap(float x,float y){if(moleOver)return;float s=minSide(),w=getWidth(),size=s*(roundScreen?.53f:.60f),left=(w-size)/2f,top=s*.205f,gap=s*.014f,cell=(size-gap*2)/3f;if(x<left||x>left+size||y<top||y>top+size)return;int col=(int)((x-left)/(cell+gap)),row=(int)((y-top)/(cell+gap));if(col<0||col>2||row<0||row>2)return;int idx=row*3+col;if(idx==moleIndex){moleScore+=1+Math.min(2,moleCombo/4);moleCombo++;moleIndex=-1;moleNextMove=SystemClock.elapsedRealtime()+180;performHapticFeedback(HapticFeedbackConstants.CONFIRM);}else{moleMisses++;moleCombo=0;performHapticFeedback(HapticFeedbackConstants.REJECT);}invalidate();}

    // ---------- Blackjack ----------

    private void addBlackjackCard(boolean player){int raw=1+random.nextInt(13),value=raw==1?11:Math.min(raw,10);if(player){blackjackPlayer+=value;blackjackPlayerCards++;if(raw==1)blackjackPlayerAces++;while(blackjackPlayer>21&&blackjackPlayerAces>0){blackjackPlayer-=10;blackjackPlayerAces--;}}else{blackjackDealer+=value;blackjackDealerCards++;if(raw==1)blackjackDealerAces++;while(blackjackDealer>21&&blackjackDealerAces>0){blackjackDealer-=10;blackjackDealerAces--;}}}
    private void drawBlackjack(Canvas c){float s=minSide(),w=getWidth();drawGameHeader(c,"21 点",blackjackOver?"本局结束":"尽量接近 21，超过即爆牌",false);float cardW=s*.23f,cardH=s*.18f;drawScoreBadge(c,w*.28f-cardW/2,s*.23f,cardW,cardH,"你的点数",String.valueOf(blackjackPlayer));drawScoreBadge(c,w*.72f-cardW/2,s*.23f,cardW,cardH,"庄家点数",blackjackDealerHidden&&!blackjackOver?"?":String.valueOf(blackjackDealer));float bh=s*.115f,gap=s*.025f,bw=s*.24f,top=s*.53f;RectF hit=new RectF(w/2-gap/2-bw,top,w/2-gap/2,top+bh),stand=new RectF(w/2+gap/2,top,w/2+gap/2+bw,top+bh);p.setColor(Color.rgb(67,121,246));c.drawRoundRect(hit,bh*.36f,bh*.36f,p);p.setColor(Color.rgb(48,59,74));c.drawRoundRect(stand,bh*.36f,bh*.36f,p);text(c,"要牌",hit.centerX(),hit.centerY()+bh*.11f,bh*.30f,Color.WHITE,true);text(c,"停牌",stand.centerX(),stand.centerY()+bh*.11f,bh*.30f,Color.WHITE,true);drawBottomBar(c,new String[]{"菜单","重开"},false);}
    private void handleBlackjackTap(float x,float y){if(blackjackOver)return;float s=minSide(),w=getWidth(),bh=s*.115f,gap=s*.025f,bw=s*.24f,top=s*.53f;RectF hit=new RectF(w/2-gap/2-bw,top,w/2-gap/2,top+bh),stand=new RectF(w/2+gap/2,top,w/2+gap/2+bw,top+bh);if(hit.contains(x,y)){addBlackjackCard(true);performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);if(blackjackPlayer>21){blackjackDealerHidden=false;blackjackOver=true;showResult(-1,"爆牌了","你 "+blackjackPlayer+" · 庄家 "+blackjackDealer);}}else if(stand.contains(x,y)){finishBlackjack();}invalidate();}
    private void finishBlackjack(){blackjackDealerHidden=false;while(blackjackDealer<17){addBlackjackCard(false);}blackjackOver=true;if(blackjackDealer>21||blackjackPlayer>blackjackDealer)showResult(1,"你赢了！","你 "+blackjackPlayer+" · 庄家 "+blackjackDealer);else if(blackjackPlayer==blackjackDealer)showResult(2,"平局","都是 "+blackjackPlayer);else showResult(-1,"庄家获胜","你 "+blackjackPlayer+" · 庄家 "+blackjackDealer);}


    // ---------- Sokoban ----------

    private static final String[][] SOKOBAN_LEVELS = {
            {"######","# @  #","# $  #","# .  #","#    #","######"},
            {"######","#    #","# $ .#","# @  #","#    #","######"},
            {"######","# .  #","# #$ #","#  @ #","#    #","######"},
            {"######","#  . #","#  $ #","# #@ #","#    #","######"}
    };

    private void loadSokobanLevel(int level){
        Arrays.fill(sokobanMap,0);Arrays.fill(sokobanGoals,false);sokobanLevel=((level%SOKOBAN_LEVELS.length)+SOKOBAN_LEVELS.length)%SOKOBAN_LEVELS.length;
        String[] rows=SOKOBAN_LEVELS[sokobanLevel];
        for(int y=0;y<SOKOBAN_SIZE;y++)for(int x=0;x<SOKOBAN_SIZE;x++){
            char ch=rows[y].charAt(x);int i=y*SOKOBAN_SIZE+x;
            if(ch=='#')sokobanMap[i]=1;else if(ch=='$'||ch=='*')sokobanMap[i]=2;
            if(ch=='.'||ch=='*'||ch=='+')sokobanGoals[i]=true;
            if(ch=='@'||ch=='+')sokobanPlayer=i;
        }
        sokobanMoves=0;sokobanWon=false;
    }
    private boolean sokobanHasBox(int i){return i>=0&&i<sokobanMap.length&&sokobanMap[i]==2;}
    private boolean sokobanBlocked(int i){return i<0||i>=sokobanMap.length||sokobanMap[i]==1||sokobanMap[i]==2;}
    private void handleSokobanSwipe(int dir){
        if(sokobanWon)return;int px=sokobanPlayer%SOKOBAN_SIZE,py=sokobanPlayer/SOKOBAN_SIZE;
        int dx=dir==1?1:dir==3?-1:0,dy=dir==2?1:dir==0?-1:0;int nx=px+dx,ny=py+dy;
        if(nx<0||nx>=SOKOBAN_SIZE||ny<0||ny>=SOKOBAN_SIZE)return;int ni=ny*SOKOBAN_SIZE+nx;
        if(sokobanMap[ni]==1){performHapticFeedback(HapticFeedbackConstants.REJECT);return;}
        if(sokobanHasBox(ni)){
            int bx=nx+dx,by=ny+dy;if(bx<0||bx>=SOKOBAN_SIZE||by<0||by>=SOKOBAN_SIZE)return;int bi=by*SOKOBAN_SIZE+bx;
            if(sokobanBlocked(bi)){performHapticFeedback(HapticFeedbackConstants.REJECT);return;}
            sokobanMap[ni]=0;sokobanMap[bi]=2;
        }
        sokobanPlayer=ni;sokobanMoves++;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        boolean done=true;for(int i=0;i<sokobanMap.length;i++)if(sokobanMap[i]==2&&!sokobanGoals[i]){done=false;break;}
        if(done){sokobanWon=true;showResult(1,"推箱成功！","第 "+(sokobanLevel+1)+" 关 · "+sokobanMoves+" 步");}
        invalidate();
    }
    private void drawSokoban(Canvas c){
        float s=minSide(),w=getWidth(),size=s*(roundScreen?.50f:.58f),cell=size/SOKOBAN_SIZE,left=(w-size)/2f,top=s*.19f;
        drawGameHeader(c,"推箱子",sokobanWon?"完成 · 点重开进入新局":"滑动移动 · 把箱子推到绿色目标",false);
        p.setColor(Color.rgb(19,27,36));c.drawRoundRect(new RectF(left,top,left+size,top+size),s*.025f,s*.025f,p);
        for(int i=0;i<sokobanMap.length;i++){
            int x=i%SOKOBAN_SIZE,y=i/SOKOBAN_SIZE;RectF r=new RectF(left+x*cell,top+y*cell,left+(x+1)*cell,top+(y+1)*cell);
            if(sokobanMap[i]==1){p.setColor(Color.rgb(65,78,96));c.drawRoundRect(new RectF(r.left+2,r.top+2,r.right-2,r.bottom-2),cell*.12f,cell*.12f,p);}
            else if(sokobanGoals[i]){p.setColor(Color.rgb(72,188,118));c.drawCircle(r.centerX(),r.centerY(),cell*.14f,p);}
            if(sokobanMap[i]==2){p.setColor(sokobanGoals[i]?Color.rgb(76,211,136):Color.rgb(224,158,81));c.drawRoundRect(new RectF(r.left+cell*.18f,r.top+cell*.18f,r.right-cell*.18f,r.bottom-cell*.18f),cell*.16f,cell*.16f,p);}
            if(i==sokobanPlayer){p.setColor(Color.rgb(91,162,255));c.drawCircle(r.centerX(),r.centerY(),cell*.24f,p);p.setColor(Color.WHITE);c.drawCircle(r.centerX()-cell*.08f,r.centerY()-cell*.05f,cell*.035f,p);c.drawCircle(r.centerX()+cell*.08f,r.centerY()-cell*.05f,cell*.035f,p);}
        }
        text(c,"第 "+(sokobanLevel+1)+"/"+SOKOBAN_LEVELS.length+" 关 · "+sokobanMoves+" 步",w/2,top+size+s*.045f,s*.023f,Color.rgb(160,174,191),false);
        drawBottomBar(c,new String[]{"菜单","重开"},false);
    }

    // ---------- Pixel runner ----------

    private RectF runnerBoard(){float s=minSide(),w=getWidth();return new RectF(w*(roundScreen?.16f:.10f),s*.20f,w*(roundScreen?.84f:.90f),bottomBarTop()-s*.055f);}
    private void updateRunner(RectF b){
        if(!runnerRunning||runnerOver)return;long now=SystemClock.elapsedRealtime();float dt=Math.min(.035f,(now-runnerLastTick)/1000f);runnerLastTick=now;
        float ground=b.bottom-b.height()*.13f,playerH=minSide()*.075f;runnerV+=minSide()*1.55f*dt;runnerY+=runnerV*dt;if(runnerY>ground-playerH){runnerY=ground-playerH;runnerV=0;}
        runnerObstacleX-=minSide()*(.29f+Math.min(.14f,runnerScore*.008f))*dt;float obsW=minSide()*.045f,playerX=b.left+b.width()*.20f;
        if(runnerObstacleX+obsW<b.left){runnerObstacleX=b.right+random.nextFloat()*b.width()*.28f;runnerScore++;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);}
        RectF pr=new RectF(playerX,runnerY,playerX+minSide()*.055f,runnerY+playerH);RectF or=new RectF(runnerObstacleX,ground-minSide()*.075f,runnerObstacleX+obsW,ground);
        if(RectF.intersects(pr,or)){runnerOver=true;runnerRunning=false;showResult(-1,"撞到障碍","得分 "+runnerScore);}
    }
    private void drawRunner(Canvas c){
        RectF b=runnerBoard();if(runnerY<=0&&b.height()>0)runnerY=b.bottom-b.height()*.13f-minSide()*.075f;updateRunner(b);
        drawGameHeader(c,"像素跑酷",runnerOver?("结束 · "+runnerScore+" 分"):(runnerRunning?(runnerScore+" 分"):"轻点起跑 / 跳跃"),false);
        p.setColor(Color.rgb(18,32,47));c.drawRoundRect(b,minSide()*.025f,minSide()*.025f,p);float ground=b.bottom-b.height()*.13f;p.setColor(Color.rgb(80,98,114));c.drawRect(b.left,ground,b.right,ground+minSide()*.009f,p);
        float px=b.left+b.width()*.20f,ph=minSide()*.075f;p.setColor(Color.rgb(255,208,82));c.drawRoundRect(new RectF(px,runnerY,px+minSide()*.055f,runnerY+ph),minSide()*.012f,minSide()*.012f,p);
        p.setColor(Color.rgb(224,102,79));c.drawRoundRect(new RectF(runnerObstacleX,ground-minSide()*.075f,runnerObstacleX+minSide()*.045f,ground),minSide()*.008f,minSide()*.008f,p);
        if(runnerRunning)scheduleFrame();drawBottomBar(c,new String[]{"菜单","重开"},false);
    }
    private void handleRunnerTap(){if(runnerOver)return;RectF b=runnerBoard();float ground=b.bottom-b.height()*.13f,playerH=minSide()*.075f;if(!runnerRunning){runnerRunning=true;runnerLastTick=SystemClock.elapsedRealtime();}if(runnerY>=ground-playerH-minSide()*.008f){runnerV=-minSide()*.62f;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);}invalidate();}

    // ---------- Three-lane dodger ----------

    private RectF dodgerBoard(){float s=minSide(),w=getWidth();return new RectF(w*(roundScreen?.19f:.14f),s*.20f,w*(roundScreen?.81f:.86f),bottomBarTop()-s*.055f);}
    private void updateDodger(RectF b){
        if(!dodgerRunning||dodgerOver)return;long now=SystemClock.elapsedRealtime();float dt=Math.min(.035f,(now-dodgerLastTick)/1000f);dodgerLastTick=now;
        dodgerObstacleY+=minSide()*(.28f+Math.min(.18f,dodgerScore*.01f))*dt;float cellW=b.width()/3f,playerY=b.bottom-minSide()*.090f,obsH=minSide()*.06f;
        if(dodgerObstacleY>b.bottom){dodgerObstacleY=b.top-minSide()*.08f;dodgerObstacleLane=random.nextInt(3);dodgerScore++;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);}
        if(dodgerObstacleLane==dodgerLane&&dodgerObstacleY+obsH>playerY&&dodgerObstacleY<playerY+minSide()*.06f){dodgerOver=true;dodgerRunning=false;showResult(-1,"躲避失败","得分 "+dodgerScore);}
    }
    private void drawDodger(Canvas c){
        RectF b=dodgerBoard();if(dodgerObstacleY<=0&&b.height()>0)dodgerObstacleY=b.top-minSide()*.06f;updateDodger(b);drawGameHeader(c,"三道闪避",dodgerOver?("结束 · "+dodgerScore+" 分"):(dodgerRunning?(dodgerScore+" 分"):"点左/右半屏移动"),false);
        p.setColor(Color.rgb(17,27,39));c.drawRoundRect(b,minSide()*.025f,minSide()*.025f,p);float laneW=b.width()/3f;p.setColor(Color.rgb(53,67,84));p.setStrokeWidth(minSide()*.004f);c.drawLine(b.left+laneW,b.top,b.left+laneW,b.bottom,p);c.drawLine(b.left+laneW*2,b.top,b.left+laneW*2,b.bottom,p);
        float px=b.left+dodgerLane*laneW+laneW*.5f,py=b.bottom-minSide()*.062f;p.setColor(Color.rgb(82,176,255));float rr=minSide()*.027f;c.drawCircle(px,py,rr,p);p.setColor(Color.rgb(255,112,103));float ox=b.left+dodgerObstacleLane*laneW+laneW*.5f;c.drawRoundRect(new RectF(ox-rr,dodgerObstacleY,ox+rr,dodgerObstacleY+minSide()*.06f),rr*.5f,rr*.5f,p);
        if(dodgerRunning)scheduleFrame();drawBottomBar(c,new String[]{"菜单","重开"},false);
    }
    private void handleDodgerTap(float x){if(dodgerOver)return;if(!dodgerRunning){dodgerRunning=true;dodgerLastTick=SystemClock.elapsedRealtime();}if(x<getWidth()/2f)dodgerLane=Math.max(0,dodgerLane-1);else dodgerLane=Math.min(2,dodgerLane+1);performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);invalidate();}

    // ---------- Stack tower ----------

    private RectF stackBoard(){float s=minSide(),w=getWidth();return new RectF(w*(roundScreen?.16f:.10f),s*.20f,w*(roundScreen?.84f:.90f),bottomBarTop()-s*.055f);}
    private void updateStackTower(RectF b){if(!stackRunning||stackOver)return;long now=SystemClock.elapsedRealtime();float dt=Math.min(.035f,(now-stackLastTick)/1000f);stackLastTick=now;stackMovingX+=stackV*dt;float half=stackMovingW/2;if(stackMovingX-half<b.left){stackMovingX=b.left+half;stackV=Math.abs(stackV);}else if(stackMovingX+half>b.right){stackMovingX=b.right-half;stackV=-Math.abs(stackV);}scheduleFrame();}
    private void drawStackTower(Canvas c){
        RectF b=stackBoard();if(stackMovingW<=0&&b.width()>0)resetStackTower();updateStackTower(b);drawGameHeader(c,"叠塔",stackWon?"完美登顶":stackOver?"没有重叠，塔倒了":"轻点放下移动方块",false);
        p.setColor(Color.rgb(18,29,41));c.drawRoundRect(b,minSide()*.025f,minSide()*.025f,p);float bh=Math.min(minSide()*.045f,b.height()/12f),baseY=b.bottom-bh;
        for(int i=0;i<stackLevel&&i<stackCenters.length;i++){float y=baseY-i*bh;p.setColor(Color.rgb(75+Math.min(i*8,100),128,230));c.drawRoundRect(new RectF(stackCenters[i]-stackWidths[i]/2,y,stackCenters[i]+stackWidths[i]/2,y+bh*.88f),bh*.18f,bh*.18f,p);}
        if(!stackOver&&!stackWon){float y=baseY-stackLevel*bh;p.setColor(Color.rgb(98,177,255));c.drawRoundRect(new RectF(stackMovingX-stackMovingW/2,y,stackMovingX+stackMovingW/2,y+bh*.88f),bh*.18f,bh*.18f,p);}
        text(c,"高度 "+Math.max(0,stackLevel-1)+" / 10",b.centerX(),b.top+minSide()*.045f,minSide()*.024f,Color.rgb(168,181,198),false);drawBottomBar(c,new String[]{"菜单","重开"},false);
    }
    private void handleStackTap(){
        if(stackOver||stackWon)return;RectF b=stackBoard();if(!stackRunning){stackRunning=true;stackLastTick=SystemClock.elapsedRealtime();}
        float prevC=stackCenters[stackLevel-1],prevW=stackWidths[stackLevel-1];
        float left=Math.max(stackMovingX-stackMovingW/2,prevC-prevW/2),right=Math.min(stackMovingX+stackMovingW/2,prevC+prevW/2),overlap=right-left;
        if(overlap<=minSide()*.012f){stackOver=true;stackRunning=false;showResult(-1,"塔倒了","高度 "+Math.max(0,stackLevel-1));invalidate();return;}
        stackCenters[stackLevel]=(left+right)/2;stackWidths[stackLevel]=overlap;stackLevel++;
        if(stackLevel>10){stackWon=true;stackRunning=false;showResult(1,"登顶成功！","高度 10 · 剩余宽度 "+(int)overlap);invalidate();return;}
        stackMovingW=overlap;stackMovingX=stackV>0?b.left+overlap/2:b.right-overlap/2;stackV=(stackV>0?1:-1)*minSide()*(.22f+stackLevel*.015f);performHapticFeedback(HapticFeedbackConstants.CONFIRM);invalidate();
    }

    // ---------- Records ----------

    private void drawRecords(Canvas c){float s=minSide(),w=getWidth(),h=getHeight();drawGameHeader(c,"战绩记录","游玩次数 · 完成/胜利 · 最佳记录",false);float top=s*.155f,bottom=bottomBarTop()-s*.025f,rowH=s*.128f,gap=s*.012f,sectionH=s*.058f,cursor=top;String prev="";for(int i=0;i<GAME_MENU_COUNT;i++){if(!MENU_CATEGORIES[i].equals(prev)){cursor+=sectionH;prev=MENU_CATEGORIES[i];}cursor+=rowH+gap;}float content=cursor-top-gap;menuMaxScroll=Math.max(0,content-(bottom-top));menuScroll=clamp(menuScroll,-s*.10f,menuMaxScroll+s*.10f);c.save();c.clipRect(0,top,w,bottom);cursor=top-menuScroll;prev="";for(int i=0;i<GAME_MENU_COUNT;i++){if(!MENU_CATEGORIES[i].equals(prev)){text(c,MENU_CATEGORIES[i],w/2,cursor+sectionH*.58f,sectionH*.31f,Color.rgb(120,138,160),true);cursor+=sectionH;prev=MENU_CATEGORIES[i];}RectF r=menuCardRect(cursor,rowH);p.setColor(Color.rgb(25,31,40));c.drawRoundRect(r,rowH*.25f,rowH*.25f,p);int gm=MENU_MODES[i],plays=prefs.getInt("stat_play_"+gm,0),wins=prefs.getInt("stat_win_"+gm,0),best=prefs.getInt("stat_best_"+gm,0);textFit(c,MENU_TITLES[i],r.left+rowH*.24f,r.top+rowH*.43f,rowH*.28f,Color.WHITE,true,Paint.Align.LEFT,r.width()*.46f);String bestText=best==0?"—":formatBest(gm,best);textFit(c,"游玩 "+plays+"  ·  完成/胜 "+wins+"  ·  最佳 "+bestText,r.left+rowH*.24f,r.top+rowH*.75f,rowH*.17f,Color.rgb(153,167,184),false,Paint.Align.LEFT,r.width()*.78f);cursor+=rowH+gap;}c.restore();drawBottomBar(c,new String[]{"返回"},false);}


    // ---------- About / donation ----------

    private void drawAbout(Canvas c){
        float s=minSide(),w=getWidth();
        drawGameHeader(c,"关于","腕上小游戏 · v7.0",false);
        float cardW=w*(roundScreen?.68f:.86f),left=(w-cardW)/2,top=s*.185f,bottom=bottomBarTop()-s*.030f;
        p.setColor(Color.rgb(25,31,40));c.drawRoundRect(new RectF(left,top,left+cardW,bottom),s*.045f,s*.045f,p);
        float y=top+s*.070f;
        text(c,"作者：黑白君",w/2,y,s*.040f,Color.WHITE,true);y+=s*.060f;
        drawWrappedCentered(c,"专为 Wear OS 小屏与圆屏优化的离线小游戏合集。",w/2,y,cardW*.84f,s*.026f,s*.039f,Color.rgb(190,199,212));
        y+=s*.090f;
        textFit(c,"30 款游戏 · 4 分类 · M3E · 自动存档 · 战绩",w/2,y,s*.025f,Color.rgb(150,165,183),false,Paint.Align.CENTER,cardW*.84f);

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
        if(donateQr==null) donateQr=BitmapFactory.decodeResource(getResources(), R.drawable.donate_qr);
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
        if(!suppressResultRecord && !roundResultRecorded && mode>0 && mode!=ABOUT && mode!=DONATE && mode!=RECORDS){recordResult(mode,kind,currentMetric());roundResultRecorded=true;}
        performHapticFeedback(kind == 1 ? HapticFeedbackConstants.CONFIRM : (kind == -1 ? HapticFeedbackConstants.REJECT : HapticFeedbackConstants.CLOCK_TICK));
        invalidate();
    }

    private void showTransientResult(int kind, String title, String sub) {
        suppressResultRecord=true;showResult(kind, title, sub);suppressResultRecord=false;resultOverlayAutoHide = true;
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
            scheduleFrame();
        } else if (resultOverlayKind == -1 && age < 1000) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(s*.008f); p.setColor(Color.argb(130,255,83,94));
            c.drawCircle(getWidth()/2f,getHeight()/2f,s*(.15f+.035f*(float)Math.sin(age/90.0)),p); p.setStyle(Paint.Style.FILL);
            scheduleFrame();
        } else if (t < 1f) scheduleFrame();
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
        // Keep 2048's existing visual scale unchanged; enlarge the shared header elsewhere
        // so game titles/instructions are easier to read on small Wear OS displays.
        float titleSize = mode==GAME_2048 ? s*.045f : s*.050f;
        float subSize = mode==GAME_2048 ? s*.026f : s*.029f;
        text(c,title,getWidth()/2,s*.075f,titleSize,titleColor,true);
        textFit(c,sub,getWidth()/2,s*.123f,subSize,subColor,false,Paint.Align.CENTER,getWidth()*(roundScreen?.68f:.88f));
    }

    private float headerBottom(){return minSide()*.145f;}

    private float bottomBarTop(){return bottomBarRect(1)[0].top;}

    private RectF[] bottomBarRect(int count){
        float s=minSide();
        float bottom=getHeight()-(roundScreen?s*.095f:s*.018f);
        float h=s*.155f;
        float gap=s*.020f;
        float usable=Math.min(getWidth()*(roundScreen?.76f:.92f),dp(330));
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
        if(restartConfirm && SystemClock.elapsedRealtime()>restartConfirmUntil) restartConfirm=false;
        RectF[] rects=bottomBarRect(labels.length);
        for(int i=0;i<labels.length;i++){
            String label=labels[i];
            boolean confirm=restartConfirm && "重开".equals(label);
            drawPill(c,rects[i],confirm?"确认重开":label,confirm,light);
        }
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
        if (pendingModeChoice >= 0) return handleModePickerTouch(e);
        int action=e.getActionMasked();
        boolean listScreen = mode==MENU || mode==RECORDS;
        if(action==MotionEvent.ACTION_DOWN){
            downX=lastX=e.getX();downY=lastY=e.getY();dragging=false;pinching=false;menuDragging=false;edgeBackOffset=0;edgeBackDragging=false;
            pressedBottomIndex=bottomButtonAt(downX,downY);pressedBottomDownX=downX;pressedBottomDownY=downY;
            gestureStartedOnControl=pressedBottomIndex>=0 || isConfirmArea(downY);menuScrollAtDown=menuScroll;
            if(listScreen && pressedBottomIndex<0){menuScroller.forceFinished(true);menuVelocityTracker=VelocityTracker.obtain();menuVelocityTracker.addMovement(e);if(mode==MENU)pressedMenuIndex=findMenuIndex(downX,downY);}
            if(mode==BOUNCE&&!gestureStartedOnControl)handleBounceTouch(downX);
            if(mode==PONG&&!gestureStartedOnControl)handlePongTouch(downX,downY);
            if(mode==BRICK_BREAKER&&!gestureStartedOnControl)handleBrickTouch(downX);
            invalidate();return true;
        }
        if(action==MotionEvent.ACTION_POINTER_DOWN&&e.getPointerCount()>=2&&(mode==XIANGQI||mode==GOMOKU)){
            pinching=true;dragging=false;pinchStart=distance(e);zoomStart=zoom;return true;
        }
        if(action==MotionEvent.ACTION_MOVE){
            if(listScreen && pressedBottomIndex<0){
                if(menuVelocityTracker!=null)menuVelocityTracker.addMovement(e);
                float dy=e.getY()-downY;
                if(Math.abs(dy)>minSide()*.015f){
                    menuDragging=true;pressedMenuIndex=-1;float target=menuScrollAtDown-dy,over=minSide()*.13f;
                    if(target<0)target*=.34f;else if(target>menuMaxScroll)target=menuMaxScroll+(target-menuMaxScroll)*.34f;
                    menuScroll=clamp(target,-over,menuMaxScroll+over);invalidate();
                }
                return true;
            }
            if(pressedBottomIndex>=0){if(Math.hypot(e.getX()-pressedBottomDownX,e.getY()-pressedBottomDownY)>minSide()*.04f)pressedBottomIndex=-1;return true;}
            if(mode==BOUNCE&&!gestureStartedOnControl){handleBounceTouch(e.getX());lastX=e.getX();lastY=e.getY();return true;}
            if(mode==PONG&&!gestureStartedOnControl){handlePongTouch(e.getX(),e.getY());lastX=e.getX();lastY=e.getY();return true;}
            if(mode==BRICK_BREAKER&&!gestureStartedOnControl){handleBrickTouch(e.getX());lastX=e.getX();lastY=e.getY();return true;}
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
            if(listScreen && pressedBottomIndex<0){
                if(menuVelocityTracker!=null){menuVelocityTracker.addMovement(e);menuVelocityTracker.computeCurrentVelocity(1000);float vy=menuVelocityTracker.getYVelocity();menuVelocityTracker.recycle();menuVelocityTracker=null;if(menuDragging&&Math.abs(vy)>220){menuScroller.fling(0,(int)menuScroll,0,(int)-vy,0,0,(int)(-minSide()*.10f),(int)(menuMaxScroll+minSide()*.10f));scheduleFrame();}else springMenuIfNeeded();}
                if(mode==MENU&&!menuDragging&&Math.hypot(ux-downX,uy-downY)<minSide()*.035f){int idx=findMenuIndex(ux,uy);pressedMenuIndex=-1;if(idx==RESUME_ITEM)resumeSavedGame();else if(idx>=0)startMode(MENU_MODES[idx]);}
                pressedMenuIndex=-1;invalidate();return true;
            }
            if(pressedBottomIndex>=0){int up=bottomButtonAt(ux,uy);int down=pressedBottomIndex;pressedBottomIndex=-1;if(up==down&&Math.hypot(ux-downX,uy-downY)<minSide()*.045f)performBottomAction(down);invalidate();return true;}
            if(!gestureStartedOnControl&&(mode==GAME_2048||mode==SNAKE||mode==MAZE||mode==BLOCK_DROP||mode==SOKOBAN)){
                float dx=ux-downX,dy=uy-downY;if(Math.hypot(dx,dy)>=minSide()*.065f){int dir=Math.abs(dx)>Math.abs(dy)?(dx>0?1:3):(dy>0?2:0);if(mode==GAME_2048)move2048(dir);else if(mode==SNAKE)setSnakeDirection(dir);else if(mode==MAZE)handleMazeSwipe(dir);else if(mode==SOKOBAN)handleSokobanSwipe(dir);else if(mode==BLOCK_DROP){if(dir==2)hardDropBlock();else if(dir==1&&blockFits(blockX+1,blockY,blockRot))blockX++;else if(dir==3&&blockFits(blockX-1,blockY,blockRot))blockX--;else if(dir==0){int nr=(blockRot+1)&3;if(blockFits(blockX,blockY,nr))blockRot=nr;}}performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);invalidate();pinching=false;dragging=false;return true;}
            }
            if(!dragging&&!pinching)handleTap(ux,uy);pinching=false;dragging=false;return true;
        }
        if(action==MotionEvent.ACTION_CANCEL){pinching=false;dragging=false;pressedMenuIndex=-1;pressedBottomIndex=-1;edgeBackDragging=false;edgeBackOffset=0;if(menuVelocityTracker!=null){menuVelocityTracker.recycle();menuVelocityTracker=null;}springMenuIfNeeded();invalidate();return true;}
        return true;
    }

    @Override public boolean onGenericMotionEvent(MotionEvent e){
        if((mode==MENU||mode==RECORDS)&&e.getAction()==MotionEvent.ACTION_SCROLL){float v=e.getAxisValue(MotionEvent.AXIS_SCROLL);if(v!=0){menuScroller.forceFinished(true);menuScroll-=v*minSide()*.16f;menuScroll=clamp(menuScroll,-minSide()*.10f,menuMaxScroll+minSide()*.10f);springMenuIfNeeded();invalidate();return true;}}
        return super.onGenericMotionEvent(e);
    }

    @Override public void computeScroll(){
        if(menuScroller.computeScrollOffset()){menuScroll=menuScroller.getCurrY();scheduleFrame();if(menuScroller.isFinished())springMenuIfNeeded();}
    }

    private void springMenuIfNeeded(){int target=(int)clamp(menuScroll,0,menuMaxScroll);if(Math.abs(menuScroll-target)>.5f){menuScroller.startScroll(0,(int)menuScroll,0,target-(int)menuScroll,260);scheduleFrame();}}

    private boolean isConfirmArea(float y){return (mode==XIANGQI&&pendingX>=0||mode==GOMOKU&&gomokuPendingX>=0)&&y>=bottomBarTop()-minSide()*.15f;}

    private int findMenuIndex(float x,float y){
        float s=minSide(),viewportTop=menuViewportTop(),cardH=s*.185f,gap=s*.022f,sectionH=s*.078f,cursor=viewportTop-menuScroll;
        if(savedResumeMode>=0){float rh=cardH*.78f;RectF rr=menuCardRect(cursor,rh);if(rr.contains(x,y))return RESUME_ITEM;cursor+=rh+gap;}
        String prev="";for(int i=0;i<MENU_TITLES.length;i++){if(!MENU_CATEGORIES[i].equals(prev)){cursor+=sectionH;prev=MENU_CATEGORIES[i];}RectF r=menuCardRect(cursor,cardH);RectF hit=new RectF(r);hit.inset(-s*.010f,-s*.006f);if(hit.contains(x,y))return i;cursor+=cardH+gap;}return -1;
    }

    private void handleTap(float x,float y){
        if(mode==XIANGQI){if(pendingX>=0&&y>=bottomBarTop()-minSide()*.15f){if(x<getWidth()/2)confirmXiangqi();else{pendingX=pendingY=-1;message="已取消";invalidate();}return;}selectXiangqiAt(x,y);}
        else if(mode==GOMOKU){if(gomokuPendingX>=0&&y>=bottomBarTop()-minSide()*.15f){if(x<getWidth()/2)confirmGomoku();else{gomokuPendingX=gomokuPendingY=-1;invalidate();}return;}selectGomokuAt(x,y);}
        else if(mode==TAP_RUSH){if(!tapOver&&Math.hypot(x-targetX,y-targetY)<=minSide()*.09f){tapScore++;performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);placeTapTarget();invalidate();}}
        else if(mode==SIMON)handleSimonTap(x,y);else if(mode==TICTACTOE)handleTicTacToeTap(x,y);else if(mode==COLOR_HUNT)handleColorHuntTap(x,y);else if(mode==BOUNCE)handleBounceTouch(x);else if(mode==SLIDE_PUZZLE)handleSlidePuzzleTap(x,y);else if(mode==LIGHTS_OUT)handleLightsOutTap(x,y);else if(mode==MINI_MINES)handleMiniMinesTap(x,y);else if(mode==ROCK_PAPER_SCISSORS)handleRpsTap(x,y);else if(mode==CONNECT4)handleConnect4Tap(x,y);else if(mode==REVERSI)handleReversiTap(x,y);else if(mode==MEMORY_MATCH)handleMemoryTap(x,y);else if(mode==NUMBER_TAP)handleNumberTap(x,y);else if(mode==QUICK_MATH)handleQuickMath(x,y);else if(mode==NIM)handleNimTap(x,y);else if(mode==PONG)handlePongTouch(x,y);else if(mode==BRICK_BREAKER)handleBrickTouch(x);else if(mode==DICE_DUEL)handleDiceTap(x,y);else if(mode==SUDOKU)handleSudokuTap(x,y);else if(mode==BLOCK_DROP)handleBlockTap(x,y);else if(mode==FLAPPY)handleFlappyTap();else if(mode==WHACK_MOLE)handleWhackMoleTap(x,y);else if(mode==BLACKJACK)handleBlackjackTap(x,y);else if(mode==RUNNER)handleRunnerTap();else if(mode==DODGER)handleDodgerTap(x);else if(mode==STACK_TOWER)handleStackTap();
        else if(mode==ABOUT){float s=minSide(),w=getWidth(),cardW=w*(roundScreen?.68f:.86f),bottom=bottomBarTop()-s*.030f;float btnH=s*.082f,gap=s*.018f,feedbackTop=bottom-s*.045f-btnH,donateTop=feedbackTop-gap-btnH;RectF donate=new RectF(w/2-cardW*.35f,donateTop,w/2+cardW*.35f,donateTop+btnH),feedback=new RectF(w/2-cardW*.35f,feedbackTop,w/2+cardW*.35f,feedbackTop+btnH);if(donate.contains(x,y)){navigateTo(DONATE,false);performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);}else if(feedback.contains(x,y))openFeedbackOnPhone();}
    }

    private int bottomButtonCount(){if(mode==MENU)return 0;if(mode==ABOUT||mode==DONATE||mode==RECORDS)return 1;if(mode==XIANGQI)return xiangqiOver?2:3;return 2;}
    private int bottomButtonAt(float x,float y){int count=bottomButtonCount();if(count<=0)return -1;RectF[] rs=bottomBarRect(count);for(int i=0;i<rs.length;i++){RectF hit=new RectF(rs[i]);hit.inset(minSide()*.010f,minSide()*.006f);if(hit.contains(x,y))return i;}return -1;}

    private void performBottomAction(int idx){
        if(mode==ABOUT||mode==DONATE||mode==RECORDS){
            if(idx==0){
                if(hostListener!=null){ hostListener.onExitToHub(); return; }
                menuScroll=0;navigateTo(mode==DONATE?ABOUT:MENU,true);
            }
            return;
        }
        if(idx==0){
            persistCurrentState();
            restartConfirm=false;
            if(hostListener!=null){ hostListener.onExitToHub(); return; }
            navigateTo(MENU,true);
            return;
        }
        if(mode==XIANGQI && idx==2 && !xiangqiOver){
            restartConfirm=false;
            if(xiangqi.undo()){selectedX=selectedY=pendingX=pendingY=-1;message="已撤销一步";}
            return;
        }
        if(idx!=1)return;
        long now=SystemClock.elapsedRealtime();
        if(!restartConfirm || now>restartConfirmUntil){
            restartConfirm=true;restartConfirmUntil=now+2200;
            showToastHint("再点一次确认重开");
            invalidate();
            return;
        }
        restartConfirm=false;roundResultRecorded=false;resetMode(mode);
    }

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
        if(gomokuOver || (aiMode[GOMOKU] && gomokuTurn==2))return;float cell=baseGomokuCell()*zoom,left=getWidth()/2f+panX-7*cell,top=headerBottom()+minSide()*.03f+panY;
        int x=Math.round((sx-left)/cell),y=Math.round((sy-top)/cell);if(x<0||x>=15||y<0||y>=15)return;
        if(gomoku[y][x]!=0)message="这里已有棋子";else{gomokuPendingX=x;gomokuPendingY=y;message="确认后落子";}performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);invalidate();
    }

    private void confirmGomoku(){
        if(gomokuPendingX<0||gomokuOver)return;int x=gomokuPendingX,y=gomokuPendingY;gomoku[y][x]=gomokuTurn;gomokuLastX=x;gomokuLastY=y;gomokuStoneAnimStart=SystemClock.elapsedRealtime();
        if(checkFive(x,y,gomokuTurn)){
            gomokuOver=true;message=(gomokuTurn==1?"黑方":"白方")+"获胜";
            if(aiMode[GOMOKU] && gomokuTurn==2) showResult(-1,"手表获胜","五子连珠，对局结束");
            else showResult(1,message+"！","五子连珠，对局结束");
        } else {
            gomokuTurn=3-gomokuTurn;message=gomokuTurn==1?"黑方回合":"白方回合";
            if(aiMode[GOMOKU] && gomokuTurn==2) { message="手表思考中…"; gomokuAiDue=SystemClock.elapsedRealtime()+260; }
        }
        gomokuPendingX=gomokuPendingY=-1;performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);invalidate();
    }

    private boolean checkFive(int x,int y,int who){int[][] dirs={{1,0},{0,1},{1,1},{1,-1}};for(int[] dir:dirs){int n=1;for(int s=1;s<5;s++){int xx=x+dir[0]*s,yy=y+dir[1]*s;if(xx<0||xx>=15||yy<0||yy>=15||gomoku[yy][xx]!=who)break;n++;}for(int s=1;s<5;s++){int xx=x-dir[0]*s,yy=y-dir[1]*s;if(xx<0||xx>=15||yy<0||yy>=15||gomoku[yy][xx]!=who)break;n++;}if(n>=5)return true;}return false;}

    private void updateGomokuAi(){
        if(!aiMode[GOMOKU]||gomokuOver||gomokuTurn!=2||gomokuAiDue<=0)return;
        long now=SystemClock.elapsedRealtime();if(now<gomokuAiDue){postInvalidateDelayed(Math.max(16,gomokuAiDue-now));return;}
        gomokuAiDue=0;int[] mv=chooseGomokuAiMove();if(mv==null)return;int x=mv[0],y=mv[1];gomoku[y][x]=2;gomokuLastX=x;gomokuLastY=y;gomokuStoneAnimStart=now;
        if(checkFive(x,y,2)){gomokuOver=true;message="手表获胜";showResult(-1,"手表获胜","五子连珠，对局结束");}
        else{gomokuTurn=1;message="黑方回合";}
        invalidate();
    }

    private int[] chooseGomokuAiMove(){
        for(int who:new int[]{2,1})for(int y=0;y<15;y++)for(int x=0;x<15;x++)if(gomoku[y][x]==0){gomoku[y][x]=who;boolean win=checkFive(x,y,who);gomoku[y][x]=0;if(win)return new int[]{x,y};}
        int best=Integer.MIN_VALUE;List<int[]> bests=new ArrayList<>();
        for(int y=0;y<15;y++)for(int x=0;x<15;x++)if(gomoku[y][x]==0){boolean near=false;for(int dy=-2;dy<=2&&!near;dy++)for(int dx=-2;dx<=2;dx++){int xx=x+dx,yy=y+dy;if(xx>=0&&xx<15&&yy>=0&&yy<15&&gomoku[yy][xx]!=0){near=true;break;}}if(!near)continue;int attack=gomokuCandidateScore(x,y,2),defense=gomokuCandidateScore(x,y,1);int score=attack*11+defense*10-(Math.abs(x-7)+Math.abs(y-7));if(score>best){best=score;bests.clear();bests.add(new int[]{x,y});}else if(score==best)bests.add(new int[]{x,y});}
        if(bests.isEmpty())return new int[]{7,7};
        return bests.get(random.nextInt(Math.min(2,bests.size())));
    }

    private int gomokuCandidateScore(int x,int y,int who){
        int total=0;int[][] ds={{1,0},{0,1},{1,1},{1,-1}};
        for(int[] d:ds){int count=1,open=0;for(int sign:new int[]{-1,1}){int xx=x+d[0]*sign,yy=y+d[1]*sign;while(xx>=0&&xx<15&&yy>=0&&yy<15&&gomoku[yy][xx]==who){count++;xx+=d[0]*sign;yy+=d[1]*sign;}if(xx>=0&&xx<15&&yy>=0&&yy<15&&gomoku[yy][xx]==0)open++;}if(count>=5)total+=100000;else if(count==4&&open==2)total+=16000;else if(count==4&&open==1)total+=6000;else if(count==3&&open==2)total+=1800;else if(count==3&&open==1)total+=500;else if(count==2&&open==2)total+=160;else total+=count*8+open*4;}
        return total;
    }

    private void move2048(int dir){
        if(over2048)return;Game2048Engine.MoveFrame f=game2048.move(dir);if(!f.changed){over2048=!game2048.canMove();if(over2048)showResult(-1,"没有可移动位置","本局 "+game2048.getScore()+" 分");invalidate();return;}frame2048=f;anim2048Start=SystemClock.elapsedRealtime();over2048=f.gameOver;
        if(game2048.getScore()>best2048){best2048=game2048.getScore();prefs.edit().putInt("best_2048",best2048).apply();}
        if(game2048.hasReached2048()&&!reached2048Shown){reached2048Shown=true;showTransientResult(1,"达成 2048！","可以继续挑战更高数字");}
        if(over2048)showResult(-1,"游戏结束","本局 "+game2048.getScore()+" 分");invalidate();
    }

    // ---------- Resets / navigation ----------

    private void navigateTo(int target, boolean back){restartConfirm=false;mode=target;pageBackTransition=back;pageTransitionStart=SystemClock.elapsedRealtime();clearResultOverlay();invalidate();}

    private boolean supportsModeChoice(int m){return m==GOMOKU||m==TICTACTOE||m==CONNECT4||m==REVERSI||m==NIM||m==PONG||m==DICE_DUEL;}

    private void startMode(int m){
        if(m==FEEDBACK){openFeedbackOnPhone();return;}
        if(m==RECORDS){menuScroll=0;navigateTo(RECORDS,false);return;}
        if(m==ABOUT){navigateTo(ABOUT,false);return;}
        if(supportsModeChoice(m)){pendingModeChoice=m;invalidate();return;}
        startActualMode(m);
    }

    private void startActualMode(int m){
        pendingModeChoice=-1;panX=panY=0;zoom=1f;roundResultRecorded=false;resetMode(m);recordLaunch(m);navigateTo(m,false);performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    private void resetMode(int m){
        if(m==XIANGQI)resetXiangqi();else if(m==GOMOKU)resetGomoku();else if(m==GAME_2048)reset2048();else if(m==TAP_RUSH)resetTapRush();else if(m==SNAKE)resetSnake();else if(m==SIMON)resetSimon();else if(m==TICTACTOE)resetTicTacToe();else if(m==COLOR_HUNT)resetColorHunt();else if(m==BOUNCE)resetBounce();else if(m==SLIDE_PUZZLE)resetSlidePuzzle();else if(m==LIGHTS_OUT)resetLightsOut();else if(m==MINI_MINES)resetMiniMines();else if(m==ROCK_PAPER_SCISSORS)resetRps();else if(m==CONNECT4)resetConnect4();else if(m==REVERSI)resetReversi();else if(m==MEMORY_MATCH)resetMemoryMatch();else if(m==MAZE)resetMaze();else if(m==NUMBER_TAP)resetNumberTap();else if(m==QUICK_MATH)resetQuickMath();else if(m==NIM)resetNim();else if(m==PONG)resetPong();else if(m==BRICK_BREAKER)resetBrickBreaker();else if(m==DICE_DUEL)resetDiceDuel();else if(m==SUDOKU)resetSudoku();else if(m==BLOCK_DROP)resetBlockDrop();else if(m==FLAPPY)resetFlappy();else if(m==WHACK_MOLE)resetWhackMole();else if(m==BLACKJACK)resetBlackjack();else if(m==SOKOBAN)resetSokoban();else if(m==RUNNER)resetRunner();else if(m==DODGER)resetDodger();else if(m==STACK_TOWER)resetStackTower();
    }

    public boolean goBack(){if(pendingModeChoice>=0){pendingModeChoice=-1;invalidate();return true;}if(hostListener!=null&&mode!=MENU){persistCurrentState();hostListener.onExitToHub();return true;}if(mode==DONATE){navigateTo(ABOUT,true);return true;}if(mode!=MENU){persistCurrentState();menuScroll=0;navigateTo(MENU,true);return true;}return false;}

    private void resetAll(){resetXiangqi();resetGomoku();reset2048();resetTapRush();resetSnake();resetSimon();resetTicTacToe();resetColorHunt();resetBounce();resetSlidePuzzle();resetLightsOut();resetMiniMines();resetRps();resetConnect4();resetReversi();resetMemoryMatch();resetMaze();resetNumberTap();resetQuickMath();resetNim();resetPong();resetBrickBreaker();resetDiceDuel();resetSudoku();resetBlockDrop();resetFlappy();resetWhackMole();resetBlackjack();resetSokoban();resetRunner();resetDodger();resetStackTower();mode=MENU;menuScroll=0;pageTransitionStart=SystemClock.elapsedRealtime();}
    private void resetXiangqi(){clearResultOverlay();xiangqi.reset();selectedX=selectedY=pendingX=pendingY=-1;xiangqiOver=false;xqAnimPiece=0;message="红方回合";panX=panY=0;zoom=1f;}
    private void resetGomoku(){clearResultOverlay();for(int y=0;y<15;y++)for(int x=0;x<15;x++)gomoku[y][x]=0;gomokuTurn=1;gomokuPendingX=gomokuPendingY=-1;gomokuOver=false;gomokuLastX=gomokuLastY=-1;gomokuAiDue=0;message="黑方回合";panX=panY=0;zoom=1f;}
    private void reset2048(){clearResultOverlay();game2048.reset();frame2048=null;over2048=false;reached2048Shown=false;}
    private void resetTapRush(){clearResultOverlay();tapScore=0;tapOver=false;tapResultShown=false;tapStart=SystemClock.elapsedRealtime();post(()->{placeTapTarget();invalidate();});}
    private void resetSnake(){clearResultOverlay();snake.clear();snake.addFirst(new Cell(5,6));snake.addLast(new Cell(4,6));snake.addLast(new Cell(3,6));snakeDir=1;snakeScore=0;snakeOver=false;snakeRunning=false;snakeWon=false;placeSnakeFood();}
    private void resetSimon(){clearResultOverlay();simonSeq.clear();simonSeq.add(random.nextInt(4));simonInputIndex=0;simonOver=false;simonWon=false;simonShowing=true;simonRoundStart=SystemClock.elapsedRealtime()+550;simonTapFlash=-1;}
    private void resetTicTacToe(){clearResultOverlay();Arrays.fill(ttt,0);tttOver=false;tttResult=0;tttLast=-1;tttAiDue=0;tttTurn=1;}
    private void resetColorHunt(){clearResultOverlay();colorGridN=2;colorScore=0;colorOver=false;colorWon=false;colorTarget=random.nextInt(4);colorDeadline=SystemClock.elapsedRealtime()+25000;colorFlash=-1;}
    private void resetBounce(){clearResultOverlay();bounceHits=0;bounceRunning=false;bounceOver=false;bounceWon=false;bounceLastTick=0;bounceBallX=bounceBallY=bounceVx=bounceVy=bouncePaddleX=0;}
    private void resetSlidePuzzle(){clearResultOverlay();for(int i=0;i<8;i++)slidePuzzle[i]=i+1;slidePuzzle[8]=0;slideMoves=0;slideWon=false;for(int i=0;i<80;i++){int z=findZero(slidePuzzle),zr=z/3,zc=z%3;int[] cand=new int[4];int n=0;if(zr>0)cand[n++]=z-3;if(zr<2)cand[n++]=z+3;if(zc>0)cand[n++]=z-1;if(zc<2)cand[n++]=z+1;int j=cand[random.nextInt(n)];slidePuzzle[z]=slidePuzzle[j];slidePuzzle[j]=0;}if(isSlideSolved())resetSlidePuzzle();}
    private void resetLightsOut(){clearResultOverlay();lightsMoves=0;lightsWon=false;Arrays.fill(lights,false);for(int i=0;i<18;i++)toggleLights(random.nextInt(16),false);if(allLightsOff())resetLightsOut();}
    private void resetMiniMines(){clearResultOverlay();Arrays.fill(mines,false);Arrays.fill(mineOpen,false);int placed=0;while(placed<5){int i=random.nextInt(25);if(!mines[i]){mines[i]=true;placed++;}}minesOver=false;minesWon=false;minesOpened=0;}
    private void resetRps(){clearResultOverlay();rpsPlayer=rpsAi=0;rpsPlayerScore=rpsAiScore=0;rpsOver=false;}
    private void resetConnect4(){clearResultOverlay();Arrays.fill(connect4,0);connect4Turn=1;connect4Over=false;connect4Last=-1;connect4AiDue=0;}
    private void resetReversi(){clearResultOverlay();Arrays.fill(reversi,0);reversi[2*6+2]=2;reversi[3*6+3]=2;reversi[2*6+3]=1;reversi[3*6+2]=1;reversiTurn=1;reversiOver=false;reversiAiDue=0;}
    private void resetMemoryMatch(){clearResultOverlay();for(int i=0;i<16;i++)memoryCards[i]=i/2;for(int i=15;i>0;i--){int j=random.nextInt(i+1),t=memoryCards[i];memoryCards[i]=memoryCards[j];memoryCards[j]=t;}Arrays.fill(memoryMatched,false);memoryFirst=memorySecond=-1;memoryPairs=memoryMoves=0;memoryHideAt=0;memoryWon=false;}
    private void resetMaze(){clearResultOverlay();Arrays.fill(mazeWalls,15);boolean[] seen=new boolean[81];ArrayDeque<Integer> stack=new ArrayDeque<>();int cur=0;seen[cur]=true;int seenCount=1;while(seenCount<81){int r=cur/9,c=cur%9;int[] cand=new int[4];int n=0;if(r>0&&!seen[cur-9])cand[n++]=0;if(c<8&&!seen[cur+1])cand[n++]=1;if(r<8&&!seen[cur+9])cand[n++]=2;if(c>0&&!seen[cur-1])cand[n++]=3;if(n>0){int dir=cand[random.nextInt(n)],next=dir==0?cur-9:dir==1?cur+1:dir==2?cur+9:cur-1;stack.push(cur);int bit=dir==0?1:dir==1?2:dir==2?4:8,opp=dir==0?4:dir==1?8:dir==2?1:2;mazeWalls[cur]&=~bit;mazeWalls[next]&=~opp;cur=next;seen[cur]=true;seenCount++;}else cur=stack.pop();}mazePlayer=0;mazeExit=80;mazeMoves=0;mazeWon=false;}
    private void resetNumberTap(){clearResultOverlay();for(int i=0;i<16;i++)numberTapOrder[i]=i+1;for(int i=15;i>0;i--){int j=random.nextInt(i+1),t=numberTapOrder[i];numberTapOrder[i]=numberTapOrder[j];numberTapOrder[j]=t;}numberTapNext=0;numberTapStart=SystemClock.elapsedRealtime();numberTapFinish=0;numberTapOver=false;}
    private void resetQuickMath(){clearResultOverlay();mathScore=mathWrong=0;mathOver=false;mathDeadline=SystemClock.elapsedRealtime()+20000;newMathProblem();}
    private void resetNim(){clearResultOverlay();nimStones=21;nimTurn=1;nimOver=false;nimAiDue=0;}
    private void resetPong(){clearResultOverlay();pongX=pongY=pongBottomX=pongTopX=0;pongVx=pongVy=0;pongBottomScore=pongTopScore=0;pongRunning=false;pongOver=false;pongLastTick=0;}
    private void resetBrickBreaker(){clearResultOverlay();Arrays.fill(bricks,true);brickLeft=24;brickScore=0;brickBallX=brickBallY=brickPaddleX=0;brickVx=brickVy=0;brickRunning=false;brickOver=false;brickWon=false;brickLastTick=0;}
    private void resetDiceDuel(){clearResultOverlay();diceP1=diceP2=diceP1Score=diceP2Score=0;diceTurn=1;diceOver=false;}
    private void resetSudoku(){clearResultOverlay();int[] base={1,2,3,4,3,4,1,2,2,1,4,3,4,3,2,1};int[] map={1,2,3,4};for(int i=3;i>0;i--){int j=random.nextInt(i+1),t=map[i];map[i]=map[j];map[j]=t;}for(int i=0;i<16;i++)sudokuSolution[i]=map[base[i]-1];System.arraycopy(sudokuSolution,0,sudoku,0,16);Arrays.fill(sudokuFixed,true);List<Integer> cells=new ArrayList<>();for(int i=0;i<16;i++)cells.add(i);for(int i=15;i>0;i--){int j=random.nextInt(i+1);int t=cells.get(i);cells.set(i,cells.get(j));cells.set(j,t);}for(int k=0;k<8;k++){int i=cells.get(k);sudoku[i]=0;sudokuFixed[i]=false;}sudokuSelected=-1;sudokuMistakes=0;sudokuWon=false;}
    private void resetBlockDrop(){clearResultOverlay();for(int y=0;y<BLOCK_H;y++)Arrays.fill(blockBoard[y],0);blockScore=0;blockLines=0;blockOver=false;spawnBlock();}
    private void resetFlappy(){clearResultOverlay();flappyScore=0;flappyV=0;flappyRunning=false;flappyOver=false;RectF b=flappyBoard();flappyY=b.height()>0?b.centerY():minSide()*.42f;flappyPipeX=b.width()>0?b.right:getWidth();flappyGapY=b.height()>0?b.centerY():minSide()*.42f;flappyLastTick=SystemClock.elapsedRealtime();}
    private void resetWhackMole(){clearResultOverlay();moleScore=0;moleMisses=0;moleCombo=0;moleIndex=random.nextInt(9);moleOver=false;long now=SystemClock.elapsedRealtime();moleDeadline=now+30000;moleNextMove=now+700;}
    private void resetBlackjack(){clearResultOverlay();blackjackPlayer=0;blackjackDealer=0;blackjackPlayerCards=blackjackDealerCards=0;blackjackPlayerAces=blackjackDealerAces=0;blackjackOver=false;blackjackDealerHidden=true;addBlackjackCard(true);addBlackjackCard(true);addBlackjackCard(false);addBlackjackCard(false);if(blackjackPlayer==21){finishBlackjack();}}
    private void resetSokoban(){clearResultOverlay();int level=sokobanWon?sokobanLevel+1:Math.max(0,sokobanLevel);loadSokobanLevel(level);}
    private void resetRunner(){clearResultOverlay();runnerScore=0;runnerV=0;runnerRunning=false;runnerOver=false;runnerY=0;RectF b=runnerBoard();runnerObstacleX=b.width()>0?b.right:getWidth();runnerLastTick=SystemClock.elapsedRealtime();}
    private void resetDodger(){clearResultOverlay();dodgerLane=1;dodgerObstacleLane=random.nextInt(3);dodgerScore=0;RectF b=dodgerBoard();dodgerObstacleY=b.height()>0?b.top-minSide()*.06f:0;dodgerRunning=false;dodgerOver=false;dodgerLastTick=SystemClock.elapsedRealtime();}
    private void resetStackTower(){clearResultOverlay();Arrays.fill(stackCenters,0);Arrays.fill(stackWidths,0);stackLevel=1;stackRunning=false;stackOver=false;stackWon=false;RectF b=stackBoard();float baseW=b.width()>0?b.width()*.62f:minSide()*.45f;stackCenters[0]=b.width()>0?b.centerX():getWidth()/2f;stackWidths[0]=baseW;stackMovingW=baseW;stackMovingX=b.width()>0?b.left+baseW/2:getWidth()/2f;stackV=minSide()*.22f;stackLastTick=SystemClock.elapsedRealtime();}

    private void initPongGeometry(){RectF b=pongBoard();if(b.width()<=0)return;pongBottomX=pongTopX=b.centerX();pongX=b.centerX();pongY=b.centerY();float speed=minSide()*.50f;pongVx=(random.nextBoolean()?1:-1)*speed*.48f;pongVy=speed;}
    private void initBrickGeometry(){RectF b=brickBoard();if(b.width()<=0)return;brickPaddleX=b.centerX();brickBallX=b.centerX();brickBallY=b.bottom-b.height()*.20f;float speed=minSide()*.46f;brickVx=speed*.46f;brickVy=-speed;}

    private void drawModePicker(Canvas c){
        float s=minSide(),w=getWidth(),h=getHeight();p.setColor(Color.argb(170,0,0,0));c.drawRect(0,0,w,h,p);float cardW=w*(roundScreen?.72f:.84f),cardH=s*.38f;RectF card=new RectF(w/2-cardW/2,h/2-cardH/2,w/2+cardW/2,h/2+cardH/2);p.setColor(Color.rgb(25,32,42));c.drawRoundRect(card,s*.045f,s*.045f,p);textFit(c,gameName(pendingModeChoice),w/2,card.top+cardH*.22f,s*.042f,Color.WHITE,true,Paint.Align.CENTER,cardW*.80f);text(c,"选择游戏模式",w/2,card.top+cardH*.36f,s*.025f,Color.rgb(160,173,190),false);RectF[] rs=modePickerRects(card);p.setColor(Color.rgb(56,112,240));c.drawRoundRect(rs[0],rs[0].height()/2,rs[0].height()/2,p);p.setColor(Color.rgb(46,55,69));c.drawRoundRect(rs[1],rs[1].height()/2,rs[1].height()/2,p);text(c,"单人 · 对战手表",rs[0].centerX(),rs[0].centerY()+rs[0].height()*.11f,rs[0].height()*.25f,Color.WHITE,true);text(c,"双人 · 同屏轮流",rs[1].centerX(),rs[1].centerY()+rs[1].height()*.11f,rs[1].height()*.25f,Color.WHITE,true);
    }
    private RectF[] modePickerRects(RectF card){float s=minSide(),gap=s*.020f,bh=s*.095f,left=card.left+s*.055f,right=card.right-s*.055f,top=card.top+card.height()*.47f;return new RectF[]{new RectF(left,top,right,top+bh),new RectF(left,top+bh+gap,right,top+bh*2+gap)};}
    private boolean handleModePickerTouch(MotionEvent e){if(e.getActionMasked()!=MotionEvent.ACTION_UP)return true;float s=minSide(),w=getWidth(),h=getHeight(),cardW=w*(roundScreen?.72f:.84f),cardH=s*.38f;RectF card=new RectF(w/2-cardW/2,h/2-cardH/2,w/2+cardW/2,h/2+cardH/2);RectF[] rs=modePickerRects(card);int m=pendingModeChoice;if(rs[0].contains(e.getX(),e.getY())){aiMode[m]=true;startActualMode(m);}else if(rs[1].contains(e.getX(),e.getY())){aiMode[m]=false;startActualMode(m);}else if(!card.contains(e.getX(),e.getY())){pendingModeChoice=-1;invalidate();}return true;}

    private String gameName(int gm){if(gm==BLOCK_DROP)return "俄罗斯方块";if(gm==FLAPPY)return "跳跃小鸟";if(gm==WHACK_MOLE)return "打地鼠";if(gm==BLACKJACK)return "21 点";if(gm==SOKOBAN)return "推箱子";if(gm==RUNNER)return "像素跑酷";if(gm==DODGER)return "三道闪避";if(gm==STACK_TOWER)return "叠塔";for(int i=0;i<MENU_MODES.length;i++)if(MENU_MODES[i]==gm)return MENU_TITLES[i];return "游戏";}

    private void placeTapTarget(){float s=minSide(),margin=s*.13f,top=headerBottom()+s*.05f,bottom=bottomBarTop()-s*.06f,left=margin,right=getWidth()-margin;if(right<=left||bottom<=top){targetX=getWidth()/2f;targetY=getHeight()/2f;return;}targetX=left+random.nextFloat()*(right-left);targetY=top+random.nextFloat()*(bottom-top);}

    // ---------- Save / records ----------

    private void recordLaunch(int gm){prefs.edit().putInt("stat_play_"+gm,prefs.getInt("stat_play_"+gm,0)+1).apply();}
    private void recordResult(int gm,int kind,int metric){SharedPreferences.Editor e=prefs.edit();if(kind==1)e.putInt("stat_win_"+gm,prefs.getInt("stat_win_"+gm,0)+1);if(metric>prefs.getInt("stat_best_"+gm,0))e.putInt("stat_best_"+gm,metric);e.apply();}
    private int currentMetric(){if(mode==GAME_2048)return game2048.getScore();if(mode==TAP_RUSH)return tapScore;if(mode==SNAKE)return snakeScore;if(mode==SIMON)return simonSeq.size();if(mode==COLOR_HUNT)return colorScore;if(mode==BOUNCE)return bounceHits;if(mode==MINI_MINES)return minesOpened;if(mode==MEMORY_MATCH)return memoryPairs;if(mode==QUICK_MATH)return mathScore;if(mode==BRICK_BREAKER)return brickScore;if(mode==NUMBER_TAP)return numberTapNext;if(mode==SUDOKU)return Math.max(1,20-sudokuMistakes);if(mode==BLOCK_DROP)return blockScore;if(mode==FLAPPY)return flappyScore;if(mode==WHACK_MOLE)return moleScore;if(mode==BLACKJACK)return blackjackPlayer;if(mode==SOKOBAN)return Math.max(1,100-sokobanMoves);if(mode==RUNNER)return runnerScore;if(mode==DODGER)return dodgerScore;if(mode==STACK_TOWER)return Math.max(0,stackLevel-1);return 1;}
    private String formatBest(int gm,int best){if(gm==GAME_2048||gm==TAP_RUSH||gm==SNAKE||gm==COLOR_HUNT||gm==BOUNCE||gm==QUICK_MATH||gm==BRICK_BREAKER||gm==BLOCK_DROP||gm==FLAPPY||gm==WHACK_MOLE||gm==RUNNER||gm==DODGER||gm==STACK_TOWER)return String.valueOf(best);if(gm==SIMON)return best+" 轮";if(gm==MEMORY_MATCH)return best+" 对";return String.valueOf(best);}

    public void persistCurrentState(){
        if(!canPersistMode(mode))return;
        if(isCurrentPersistedGameFinished()){prefs.edit().putBoolean("has_save",false).apply();savedResumeMode=-1;return;}
        SharedPreferences.Editor e=prefs.edit();e.putBoolean("has_save",true).putInt("last_mode",mode).putBoolean("save_ai",aiMode[mode]);
        if(mode==XIANGQI)e.putString("save_data",xiangqi.serialize());
        else if(mode==GOMOKU)e.putString("save_data",join2d(gomoku)).putInt("save_turn",gomokuTurn);
        else if(mode==GAME_2048)e.putString("save_data",game2048.serialize());
        else if(mode==TICTACTOE)e.putString("save_data",joinInts(ttt)).putInt("save_turn",tttTurn);
        else if(mode==SLIDE_PUZZLE)e.putString("save_data",joinInts(slidePuzzle)).putInt("save_aux",slideMoves);
        else if(mode==LIGHTS_OUT)e.putString("save_data",joinBools(lights)).putInt("save_aux",lightsMoves);
        else if(mode==MINI_MINES)e.putString("save_data",joinBools(mines)+"|"+joinBools(mineOpen)).putInt("save_aux",minesOpened);
        else if(mode==CONNECT4)e.putString("save_data",joinInts(connect4)).putInt("save_turn",connect4Turn);
        else if(mode==REVERSI)e.putString("save_data",joinInts(reversi)).putInt("save_turn",reversiTurn);
        else if(mode==MEMORY_MATCH)e.putString("save_data",joinInts(memoryCards)+"|"+joinBools(memoryMatched)).putInt("save_aux",memoryMoves);
        else if(mode==MAZE)e.putString("save_data",joinInts(mazeWalls)).putInt("save_aux",mazePlayer).putInt("save_aux2",mazeMoves);
        else if(mode==NIM)e.putInt("save_aux",nimStones).putInt("save_turn",nimTurn);
        else if(mode==SUDOKU)e.putString("save_data",joinInts(sudoku)+"|"+joinInts(sudokuSolution)+"|"+joinBools(sudokuFixed)).putInt("save_aux",sudokuMistakes);
        else if(mode==BLOCK_DROP)e.putString("save_data",join2d(blockBoard)).putInt("save_aux",blockScore).putInt("save_aux2",blockLines).putInt("save_turn",blockType*1000+blockRot*100+blockX*10+Math.max(0,blockY));
        else if(mode==FLAPPY)e.putString("save_data",flappyY+","+flappyV+","+flappyPipeX+","+flappyGapY).putInt("save_aux",flappyScore);
        else if(mode==BLACKJACK)e.putString("save_data",blackjackPlayer+","+blackjackDealer+","+blackjackPlayerCards+","+blackjackDealerCards+","+(blackjackDealerHidden?1:0)+","+blackjackPlayerAces+","+blackjackDealerAces);
        else if(mode==SOKOBAN)e.putString("save_data",joinInts(sokobanMap)+"|"+joinBools(sokobanGoals)).putInt("save_aux",sokobanPlayer).putInt("save_aux2",sokobanMoves).putInt("save_turn",sokobanLevel);
        e.apply();savedResumeMode=mode;
    }

    private boolean canPersistMode(int m){return m==XIANGQI||m==GOMOKU||m==GAME_2048||m==TICTACTOE||m==SLIDE_PUZZLE||m==LIGHTS_OUT||m==MINI_MINES||m==CONNECT4||m==REVERSI||m==MEMORY_MATCH||m==MAZE||m==NIM||m==SUDOKU||m==BLOCK_DROP||m==FLAPPY||m==BLACKJACK||m==SOKOBAN;}
    private boolean isCurrentPersistedGameFinished(){return mode==XIANGQI&&xiangqiOver||mode==GOMOKU&&gomokuOver||mode==GAME_2048&&over2048||mode==TICTACTOE&&tttOver||mode==SLIDE_PUZZLE&&slideWon||mode==LIGHTS_OUT&&lightsWon||mode==MINI_MINES&&minesOver||mode==CONNECT4&&connect4Over||mode==REVERSI&&reversiOver||mode==MEMORY_MATCH&&memoryWon||mode==MAZE&&mazeWon||mode==NIM&&nimOver||mode==SUDOKU&&sudokuWon||mode==BLOCK_DROP&&blockOver||mode==FLAPPY&&flappyOver||mode==BLACKJACK&&blackjackOver||mode==SOKOBAN&&sokobanWon;}

    private void resumeSavedGame(){int m=savedResumeMode;if(m<0||!canPersistMode(m))return;resetMode(m);aiMode[m]=prefs.getBoolean("save_ai",true);String data=prefs.getString("save_data","");boolean ok=true;try{
        if(m==XIANGQI)ok=xiangqi.restore(data);
        else if(m==GOMOKU){ok=parse2d(data,gomoku);gomokuTurn=prefs.getInt("save_turn",1);message=gomokuTurn==1?"黑方回合":"白方回合";}
        else if(m==GAME_2048)ok=game2048.restore(data);
        else if(m==TICTACTOE){ok=parseInts(data,ttt);tttTurn=prefs.getInt("save_turn",1);}
        else if(m==SLIDE_PUZZLE){ok=parseInts(data,slidePuzzle);slideMoves=prefs.getInt("save_aux",0);}
        else if(m==LIGHTS_OUT){ok=parseBools(data,lights);lightsMoves=prefs.getInt("save_aux",0);}
        else if(m==MINI_MINES){String[] q=data.split("\\|",-1);ok=q.length==2&&parseBools(q[0],mines)&&parseBools(q[1],mineOpen);minesOpened=prefs.getInt("save_aux",0);}
        else if(m==CONNECT4){ok=parseInts(data,connect4);connect4Turn=prefs.getInt("save_turn",1);}
        else if(m==REVERSI){ok=parseInts(data,reversi);reversiTurn=prefs.getInt("save_turn",1);}
        else if(m==MEMORY_MATCH){String[] q=data.split("\\|",-1);ok=q.length==2&&parseInts(q[0],memoryCards)&&parseBools(q[1],memoryMatched);memoryMoves=prefs.getInt("save_aux",0);memoryPairs=0;for(boolean b:memoryMatched)if(b)memoryPairs++;memoryPairs/=2;}
        else if(m==MAZE){ok=parseInts(data,mazeWalls);mazePlayer=prefs.getInt("save_aux",0);mazeMoves=prefs.getInt("save_aux2",0);}
        else if(m==NIM){nimStones=prefs.getInt("save_aux",21);nimTurn=prefs.getInt("save_turn",1);}
        else if(m==SUDOKU){String[] q=data.split("\\|",-1);ok=q.length==3&&parseInts(q[0],sudoku)&&parseInts(q[1],sudokuSolution)&&parseBools(q[2],sudokuFixed);sudokuMistakes=prefs.getInt("save_aux",0);}
        else if(m==BLOCK_DROP){ok=parse2d(data,blockBoard);blockScore=prefs.getInt("save_aux",0);blockLines=prefs.getInt("save_aux2",0);spawnBlock();}
        else if(m==FLAPPY){String[] q=data.split(",");ok=q.length==4;if(ok){flappyY=Float.parseFloat(q[0]);flappyV=Float.parseFloat(q[1]);flappyPipeX=Float.parseFloat(q[2]);flappyGapY=Float.parseFloat(q[3]);flappyScore=prefs.getInt("save_aux",0);flappyRunning=false;}}
        else if(m==BLACKJACK){String[] q=data.split(",");ok=q.length==5||q.length==7;if(ok){blackjackPlayer=Integer.parseInt(q[0]);blackjackDealer=Integer.parseInt(q[1]);blackjackPlayerCards=Integer.parseInt(q[2]);blackjackDealerCards=Integer.parseInt(q[3]);blackjackDealerHidden="1".equals(q[4]);blackjackPlayerAces=q.length==7?Integer.parseInt(q[5]):0;blackjackDealerAces=q.length==7?Integer.parseInt(q[6]):0;}}
        else if(m==SOKOBAN){String[] q=data.split("\\|",-1);ok=q.length==2&&parseInts(q[0],sokobanMap)&&parseBools(q[1],sokobanGoals);if(ok){sokobanPlayer=prefs.getInt("save_aux",0);sokobanMoves=prefs.getInt("save_aux2",0);sokobanLevel=prefs.getInt("save_turn",0);sokobanWon=false;}}
    }catch(Exception ex){ok=false;}if(!ok){showToastHint("存档损坏，已开始新游戏");resetMode(m);}
        if(ok&&aiMode[m]){long due=SystemClock.elapsedRealtime()+280;if(m==GOMOKU&&gomokuTurn==2)gomokuAiDue=due;else if(m==TICTACTOE&&tttTurn==2)tttAiDue=due;else if(m==CONNECT4&&connect4Turn==2)connect4AiDue=due;else if(m==REVERSI&&reversiTurn==2)reversiAiDue=due;else if(m==NIM&&nimTurn==2)nimAiDue=due;}
        roundResultRecorded=false;recordLaunch(m);navigateTo(m,false);performHapticFeedback(HapticFeedbackConstants.CONFIRM);}

    private String joinInts(int[] a){StringBuilder b=new StringBuilder();for(int i=0;i<a.length;i++){if(i>0)b.append(',');b.append(a[i]);}return b.toString();}
    private String join2d(int[][] a){StringBuilder b=new StringBuilder();for(int[] row:a)for(int v:row){if(b.length()>0)b.append(',');b.append(v);}return b.toString();}
    private boolean parseInts(String s,int[] out){String[] p=s.split(",");if(p.length!=out.length)return false;try{for(int i=0;i<out.length;i++)out[i]=Integer.parseInt(p[i]);return true;}catch(Exception e){return false;}}
    private boolean parse2d(String s,int[][] out){String[] p=s.split(",");int n=0;for(int[] r:out)n+=r.length;if(p.length!=n)return false;try{int k=0;for(int y=0;y<out.length;y++)for(int x=0;x<out[y].length;x++)out[y][x]=Integer.parseInt(p[k++]);return true;}catch(Exception e){return false;}}
    private String joinBools(boolean[] a){StringBuilder b=new StringBuilder();for(boolean v:a)b.append(v?'1':'0');return b.toString();}
    private boolean parseBools(String s,boolean[] out){if(s.length()!=out.length)return false;for(int i=0;i<out.length;i++)out[i]=s.charAt(i)=='1';return true;}

    // ---------- Math / gesture helpers ----------

    private float distance(MotionEvent e){float dx=e.getX(0)-e.getX(1),dy=e.getY(0)-e.getY(1);return(float)Math.hypot(dx,dy);}
    private float clamp(float v,float lo,float hi){return Math.max(lo,Math.min(hi,v));}
    private void clampPan(){float limX=getWidth()*.65f,limY=getHeight()*.65f;panX=clamp(panX,-limX,limX);panY=clamp(panY,-limY,limY);}
    private float lerp(float a,float b,float t){return a+(b-a)*t;}
    private float easeOutCubic(float t){float u=1-t;return 1-u*u*u;}
    private float overshoot(float t){t=clamp(t,0,1);float s=1.70158f;t-=1;return t*t*((s+1)*t+s)+1;}

    private static final class Cell {final int x,y;Cell(int x,int y){this.x=x;this.y=y;}}
}
