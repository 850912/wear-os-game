package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Compact home for the remaining short-session v7 games.
 *
 * These games intentionally share one renderer because their state machines are tiny. Complex
 * real-time and board games live in dedicated Views/Engines. This removes the final runtime
 * dependency on the old 2k-line GameHubView without replacing it with another monolith.
 */
public final class ClassicMiniGameView extends BaseGameView {
    private static final int[] MODES = {
            GameModes.TAP_RUSH, GameModes.TICTACTOE, GameModes.COLOR_HUNT,
            GameModes.SLIDE_PUZZLE, GameModes.LIGHTS_OUT, GameModes.ROCK_PAPER_SCISSORS,
            GameModes.MEMORY_MATCH, GameModes.NUMBER_TAP, GameModes.QUICK_MATH,
            GameModes.NIM, GameModes.DICE_DUEL, GameModes.WHACK_MOLE, GameModes.BLACKJACK,
            GameModes.QUICK_DRAW, GameModes.HIGH_LOW, GameModes.ORBIT_TAP, GameModes.RING_TIMING
    };

    private final Random random = new Random();
    private final int[] a = new int[32];
    private final int[] b = new int[32];
    private int score, round, turn, next, selected = -1, second = -1, moves;
    private int target, target2, choiceA, choiceB, choiceC;
    private long startedAt, phaseAt, deadline;
    private boolean waiting, ready, pausedPhase;
    private float angle, ringRadius;
    private final RectF scratchRect = new RectF();
    private final RectF scratchRect2 = new RectF();
    private final RectF squareRect = new RectF();
    private final RectF buttonRect = new RectF();

    public ClassicMiniGameView(Context context) { super(context); }

    public static boolean supportsMode(int mode) {
        for (int value : MODES) if (value == mode) return true;
        return false;
    }

    @Override protected boolean supportsGameMode(int candidate) { return supportsMode(candidate); }
    @Override protected int gameMode() { return activeGameMode(); }
    @Override protected boolean lowerMetricIsBetter() {
        int m = activeGameMode();
        return m == GameModes.SLIDE_PUZZLE || m == GameModes.LIGHTS_OUT ||
                m == GameModes.MEMORY_MATCH || m == GameModes.NUMBER_TAP || m == GameModes.QUICK_DRAW;
    }

    @Override protected void resetGame() {
        prepareForFreshRound();
        Arrays.fill(a, 0); Arrays.fill(b, 0);
        score = round = turn = moves = 0; next = 1; selected = second = -1;
        waiting = ready = pausedPhase = false;
        startedAt = phaseAt = now(); deadline = 0;
        int m = activeGameMode();
        if (m == GameModes.TICTACTOE) {
            Arrays.fill(a, 0);
        } else if (m == GameModes.COLOR_HUNT) {
            target = random.nextInt(9);
        } else if (m == GameModes.SLIDE_PUZZLE) {
            for (int i=0;i<9;i++) a[i]=(i+1)%9;
            for (int i=0;i<90;i++) slideMove(random.nextInt(4), false);
            moves = 0;
        } else if (m == GameModes.LIGHTS_OUT) {
            for (int i=0;i<10;i++) toggleLight(random.nextInt(16));
            moves = 0;
        } else if (m == GameModes.MEMORY_MATCH) {
            List<Integer> cards = new ArrayList<>();
            for(int i=1;i<=8;i++){cards.add(i);cards.add(i);} Collections.shuffle(cards);
            for(int i=0;i<16;i++) a[i]=cards.get(i);
        } else if (m == GameModes.NUMBER_TAP) {
            List<Integer> nums=new ArrayList<>(); for(int i=1;i<=16;i++) nums.add(i);
            Collections.shuffle(nums); for(int i=0;i<16;i++) a[i]=nums.get(i);
        } else if (m == GameModes.QUICK_MATH) {
            newMath(); deadline = now()+20_000;
        } else if (m == GameModes.NIM) {
            target = 15; turn = 0;
        } else if (m == GameModes.DICE_DUEL) {
            a[0]=a[1]=0; turn=0;
        } else if (m == GameModes.WHACK_MOLE) {
            target=random.nextInt(9); phaseAt=now(); deadline=now()+30_000;
        } else if (m == GameModes.BLACKJACK) {
            a[0]=drawCard();a[1]=drawCard(); b[0]=drawCard();b[1]=drawCard();
            target=2;target2=2;
        } else if (m == GameModes.QUICK_DRAW) {
            waiting=true; ready=false; deadline=now()+1500+random.nextInt(2500);
        } else if (m == GameModes.HIGH_LOW) {
            target=1+random.nextInt(13); round=0;
        } else if (m == GameModes.ORBIT_TAP) {
            angle=0; target=(int)(random.nextFloat()*360f); deadline=now()+20_000;
        } else if (m == GameModes.RING_TIMING) {
            ringRadius=.15f; round=0;
        } else if (m == GameModes.ROCK_PAPER_SCISSORS) {
            a[0]=a[1]=0;
        } else if (m == GameModes.TAP_RUSH) {
            deadline=now()+30_000;
        }
    }

    private RectF area(){return gamePanel(roundScreen ? .10f : .07f);}

    @Override protected void drawGame(Canvas c) {
        int m=activeGameMode();
        if(m==GameModes.TAP_RUSH) drawTapRush(c);
        else if(m==GameModes.TICTACTOE) drawTicTacToe(c);
        else if(m==GameModes.COLOR_HUNT) drawColorHunt(c);
        else if(m==GameModes.SLIDE_PUZZLE) drawGridNumbers(c,3,"数字华容道","步数 "+moves,true);
        else if(m==GameModes.LIGHTS_OUT) drawLights(c);
        else if(m==GameModes.ROCK_PAPER_SCISSORS) drawRps(c);
        else if(m==GameModes.MEMORY_MATCH) drawMemory(c);
        else if(m==GameModes.NUMBER_TAP) drawGridNumbers(c,4,"数字连点","下一个 "+next,false);
        else if(m==GameModes.QUICK_MATH) drawMath(c);
        else if(m==GameModes.NIM) drawNim(c);
        else if(m==GameModes.DICE_DUEL) drawDice(c);
        else if(m==GameModes.WHACK_MOLE) drawWhack(c);
        else if(m==GameModes.BLACKJACK) drawBlackjack(c);
        else if(m==GameModes.QUICK_DRAW) drawQuickDraw(c);
        else if(m==GameModes.HIGH_LOW) drawHighLow(c);
        else if(m==GameModes.ORBIT_TAP) drawOrbit(c);
        else if(m==GameModes.RING_TIMING) drawRing(c);
    }

    private void drawTapRush(Canvas c){
        long left=Math.max(0,deadline-now());
        drawHeader(c,"反应点击",String.format(Locale.US,"%.1fs · %d",left/1000f,score));
        RectF r=area(); panel(c,r); p.setColor(PRIMARY); c.drawCircle(r.centerX(),r.centerY(),Math.min(r.width(),r.height())*.22f,p);
        text(c,"TAP",r.centerX(),r.centerY()+s()*.018f,s()*.055f,BG,true,Paint.Align.CENTER);
        if(left<=0){finishRound(1,"时间到","点击 "+score+" 次",score);return;} animateNext();
    }

    private void drawTicTacToe(Canvas c){
        drawHeader(c,"井字棋",isSinglePlayer()?"你是 X · 对战手表":"双人同屏");
        RectF r=squareArea(.78f); panel(c,r); float cell=r.width()/3f;
        p.setStrokeWidth(dp(2));p.setColor(MUTED);p.setStyle(Paint.Style.STROKE);
        for(int i=1;i<3;i++){c.drawLine(r.left+i*cell,r.top,r.left+i*cell,r.bottom,p);c.drawLine(r.left,r.top+i*cell,r.right,r.top+i*cell,p);}p.setStyle(Paint.Style.FILL);
        for(int i=0;i<9;i++) if(a[i]!=0) text(c,a[i]==1?"×":"○",r.left+(i%3+.5f)*cell,r.top+(i/3+.65f)*cell,cell*.55f,a[i]==1?PRIMARY:SECONDARY,true,Paint.Align.CENTER);
    }

    private void drawColorHunt(Canvas c){
        drawHeader(c,"色块猎手","关卡 "+(round+1)+" / 12"); RectF r=squareArea(.76f);float gap=dp(5),cell=(r.width()-gap*2)/3f;
        int base=Color.rgb(70+round*5%80,130,190); int odd=Color.rgb(95+round*5%80,150,205);
        for(int i=0;i<9;i++){float x=r.left+(i%3)*(cell+gap),y=r.top+(i/3)*(cell+gap);p.setColor(i==target?odd:base);scratchRect.set(x,y,x+cell,y+cell);c.drawRoundRect(scratchRect,dp(10),dp(10),p);}
    }

    private void drawGridNumbers(Canvas c,int n,String title,String sub,boolean blankZero){
        drawHeader(c,title,sub);RectF r=squareArea(.78f);float gap=dp(4),cell=(r.width()-gap*(n-1))/n;
        for(int i=0;i<n*n;i++){int v=a[i];float x=r.left+(i%n)*(cell+gap),y=r.top+(i/n)*(cell+gap);scratchRect.set(x,y,x+cell,y+cell);RectF q=scratchRect;p.setColor(v==0&&blankZero?BG:SURFACE_HIGH);c.drawRoundRect(q,dp(9),dp(9),p);if(!(v==0&&blankZero))text(c,String.valueOf(v),q.centerX(),q.centerY()+cell*.13f,cell*.34f,TEXT,true,Paint.Align.CENTER);}
    }

    private void drawLights(Canvas c){
        drawHeader(c,"熄灯解谜","步数 "+moves+" · 全部熄灭即完成");RectF r=squareArea(.76f);float gap=dp(5),cell=(r.width()-gap*3)/4f;
        for(int i=0;i<16;i++){float x=r.left+(i%4)*(cell+gap),y=r.top+(i/4)*(cell+gap);p.setColor(a[i]==1?Color.rgb(255,211,84):SURFACE_HIGH);scratchRect.set(x,y,x+cell,y+cell);c.drawRoundRect(scratchRect,dp(10),dp(10),p);}
    }

    private void drawRps(Canvas c){
        drawHeader(c,"石头剪刀布","你 "+a[0]+" : "+a[1]+" 手表");RectF r=area();panel(c,r);String[] labels={"石头","剪刀","布"};for(int i=0;i<3;i++){RectF q=buttonCell(r,i,3);p.setColor(SURFACE_HIGH);c.drawRoundRect(q,dp(12),dp(12),p);text(c,labels[i],q.centerX(),q.centerY()+s()*.012f,s()*.03f,TEXT,true,Paint.Align.CENTER);} if(round>0)text(c,"上一局："+rpsName(target)+" / "+rpsName(target2),r.centerX(),r.bottom-s()*.045f,s()*.023f,MUTED,false,Paint.Align.CENTER);
    }

    private void drawMemory(Canvas c){
        drawHeader(c,"记忆配对","配对 "+score+" / 8 · 翻牌 "+moves);RectF r=squareArea(.78f);float gap=dp(4),cell=(r.width()-gap*3)/4f;
        for(int i=0;i<16;i++){float x=r.left+(i%4)*(cell+gap),y=r.top+(i/4)*(cell+gap);scratchRect.set(x,y,x+cell,y+cell);RectF q=scratchRect;boolean show=b[i]==1||i==selected||i==second;p.setColor(show?SURFACE_HIGH:Color.rgb(52,65,82));c.drawRoundRect(q,dp(8),dp(8),p);if(show)text(c,String.valueOf(a[i]),q.centerX(),q.centerY()+cell*.12f,cell*.34f,b[i]==1?GOOD:PRIMARY,true,Paint.Align.CENTER);}
    }

    private void drawMath(Canvas c){
        long left=Math.max(0,deadline-now());drawHeader(c,"极速心算",String.format(Locale.US,"%.1fs · %d 分",left/1000f,score));RectF r=area();panel(c,r);text(c,target+" + "+target2+" = ?",r.centerX(),r.top+r.height()*.27f,s()*.07f,TEXT,true,Paint.Align.CENTER);int[] vals={choiceA,choiceB,choiceC};for(int i=0;i<3;i++){RectF q=buttonCell(r,i,3);p.setColor(SURFACE_HIGH);c.drawRoundRect(q,dp(12),dp(12),p);text(c,String.valueOf(vals[i]),q.centerX(),q.centerY()+s()*.015f,s()*.045f,PRIMARY,true,Paint.Align.CENTER);}if(left<=0){finishRound(1,"时间到",score+" 题正确",score);return;}animateNext();
    }

    private void drawNim(Canvas c){
        drawHeader(c,"Nim 取石","剩余 "+target+" · 每次取 1–3 个");RectF r=area();panel(c,r);for(int i=0;i<target;i++){float x=r.left+r.width()*(.12f+(i%5)*.19f),y=r.top+r.height()*(.18f+(i/5)*.25f);p.setColor(PRIMARY);c.drawCircle(x,y,s()*.026f,p);}for(int i=0;i<3;i++){RectF q=buttonCell(r,i,3);p.setColor(SURFACE_HIGH);c.drawRoundRect(q,dp(12),dp(12),p);text(c,"取 "+(i+1),q.centerX(),q.centerY()+s()*.012f,s()*.03f,TEXT,true,Paint.Align.CENTER);}
    }

    private void drawDice(Canvas c){
        drawHeader(c,"骰子对决",(isSinglePlayer()?"你":"玩家 1")+" "+a[0]+" : "+a[1]+" "+(isSinglePlayer()?"手表":"玩家 2"));RectF r=area();panel(c,r);text(c,round==0?"点击掷骰":("⚄  "+round),r.centerX(),r.centerY(),s()*.07f,PRIMARY,true,Paint.Align.CENTER);text(c,"先到 20 分",r.centerX(),r.bottom-s()*.05f,s()*.026f,MUTED,false,Paint.Align.CENTER);
    }

    private void drawWhack(Canvas c){
        long left=Math.max(0,deadline-now());drawHeader(c,"打地鼠",String.format(Locale.US,"%.1fs · %d 分",left/1000f,score));RectF r=squareArea(.76f);float gap=dp(5),cell=(r.width()-gap*2)/3f;for(int i=0;i<9;i++){float x=r.left+(i%3)*(cell+gap),y=r.top+(i/3)*(cell+gap);p.setColor(i==target?GOOD:SURFACE_HIGH);c.drawCircle(x+cell/2,y+cell/2,cell*.36f,p);if(i==target)text(c,"●",x+cell/2,y+cell*.62f,cell*.36f,BG,true,Paint.Align.CENTER);}if(left<=0){finishRound(1,"时间到",score+" 分",score);return;}if(now()-phaseAt>700){target=random.nextInt(9);phaseAt=now();}animateNext();
    }

    private void drawBlackjack(Canvas c){
        int ps=handValue(a,target), ds=handValue(b,target2);drawHeader(c,"21 点","你 "+ps+" · 庄家 "+(pausedPhase?ds:"?"));RectF r=area();panel(c,r);text(c,"你的手牌",r.centerX(),r.top+r.height()*.20f,s()*.028f,MUTED,false,Paint.Align.CENTER);drawCards(c,a,target,r.centerX(),r.top+r.height()*.36f);if(!pausedPhase){scratchRect.set(r.left+dp(8),r.bottom-r.height()*.25f,r.centerX()-dp(4),r.bottom-dp(8));RectF l=scratchRect;scratchRect2.set(r.centerX()+dp(4),l.top,r.right-dp(8),l.bottom);RectF rr=scratchRect2;p.setColor(SURFACE_HIGH);c.drawRoundRect(l,dp(12),dp(12),p);c.drawRoundRect(rr,dp(12),dp(12),p);text(c,"要牌",l.centerX(),l.centerY()+s()*.012f,s()*.03f,TEXT,true,Paint.Align.CENTER);text(c,"停牌",rr.centerX(),rr.centerY()+s()*.012f,s()*.03f,TEXT,true,Paint.Align.CENTER);}
    }

    private void drawQuickDraw(Canvas c){
        if(waiting&&now()>=deadline){waiting=false;ready=true;phaseAt=now();haptic(HapticFeedbackConstants.CLOCK_TICK);}drawHeader(c,"极速反应",ready?"现在点！":"等屏幕变绿");RectF r=area();p.setColor(ready?GOOD:SURFACE_HIGH);c.drawRoundRect(r,dp(20),dp(20),p);text(c,ready?"GO":"WAIT",r.centerX(),r.centerY()+s()*.025f,s()*.075f,ready?BG:MUTED,true,Paint.Align.CENTER);if(waiting)animateNext();
    }

    private void drawHighLow(Canvas c){
        drawHeader(c,"猜大小","第 "+(round+1)+" / 10 题 · "+score+" 分");RectF r=area();panel(c,r);text(c,cardName(target),r.centerX(),r.top+r.height()*.40f,s()*.10f,PRIMARY,true,Paint.Align.CENTER);scratchRect.set(r.left+dp(8),r.bottom-r.height()*.28f,r.centerX()-dp(4),r.bottom-dp(8));RectF l=scratchRect;scratchRect2.set(r.centerX()+dp(4),l.top,r.right-dp(8),l.bottom);RectF rr=scratchRect2;p.setColor(SURFACE_HIGH);c.drawRoundRect(l,dp(12),dp(12),p);c.drawRoundRect(rr,dp(12),dp(12),p);text(c,"更小",l.centerX(),l.centerY()+s()*.012f,s()*.03f,TEXT,true,Paint.Align.CENTER);text(c,"更大",rr.centerX(),rr.centerY()+s()*.012f,s()*.03f,TEXT,true,Paint.Align.CENTER);
    }

    private void drawOrbit(Canvas c){
        long left=Math.max(0,deadline-now());drawHeader(c,"轨道点击",String.format(Locale.US,"%.1fs · %d 分",left/1000f,score));RectF r=area();float radius=Math.min(r.width(),r.height())*.30f;angle=(angle+(powerSaver()?2.2f:1.3f))%360f;double rad=Math.toRadians(angle),tr=Math.toRadians(target);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));p.setColor(SURFACE_HIGH);c.drawCircle(r.centerX(),r.centerY(),radius,p);p.setStyle(Paint.Style.FILL);p.setColor(PRIMARY);c.drawCircle(r.centerX()+(float)Math.cos(rad)*radius,r.centerY()+(float)Math.sin(rad)*radius,s()*.028f,p);p.setColor(GOOD);c.drawCircle(r.centerX()+(float)Math.cos(tr)*radius,r.centerY()+(float)Math.sin(tr)*radius,s()*.038f,p);if(left<=0){finishRound(1,"时间到",score+" 次命中",score);return;}animateNext();
    }

    private void drawRing(Canvas c){
        drawHeader(c,"圆环时机","第 "+(round+1)+" / 8 次 · "+score+" 分");RectF r=area();float max=Math.min(r.width(),r.height())*.38f;ringRadius += powerSaver()?.018f:.010f;if(ringRadius>.95f)ringRadius=.10f;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(5));p.setColor(GOOD);c.drawCircle(r.centerX(),r.centerY(),max*.58f,p);p.setColor(PRIMARY);c.drawCircle(r.centerX(),r.centerY(),max*ringRadius,p);p.setStyle(Paint.Style.FILL);text(c,"目标是绿环",r.centerX(),r.bottom-s()*.035f,s()*.024f,MUTED,false,Paint.Align.CENTER);animateNext();
    }

    private RectF squareArea(float fraction){RectF g=area();float side=Math.min(g.width(),g.height())*fraction;squareRect.set(g.centerX()-side/2,g.centerY()-side/2,g.centerX()+side/2,g.centerY()+side/2);return squareRect;}
    private RectF buttonCell(RectF r,int index,int count){float gap=dp(5),w=(r.width()-dp(16)-gap*(count-1))/count;float x=r.left+dp(8)+index*(w+gap);float top=r.bottom-r.height()*.30f;buttonRect.set(x,top,x+w,r.bottom-dp(8));return buttonRect;}
    private int indexAt(RectF r,int n,float x,float y){if(!r.contains(x,y))return-1;int col=(int)((x-r.left)/(r.width()/n)),row=(int)((y-r.top)/(r.height()/n));return Math.max(0,Math.min(n*n-1,row*n+col));}

    @Override protected void onGameTap(float x,float y){
        int m=activeGameMode();
        if(m==GameModes.TAP_RUSH){if(now()<deadline){score++;haptic(HapticFeedbackConstants.CLOCK_TICK);}}
        else if(m==GameModes.TICTACTOE) tapTtt(x,y);
        else if(m==GameModes.COLOR_HUNT){int i=indexAt(squareArea(.76f),3,x,y);if(i==target){round++;score++;haptic(HapticFeedbackConstants.CONFIRM);if(round>=12)finishRound(1,"完成",score+" / 12",score);else target=random.nextInt(9);}else haptic(HapticFeedbackConstants.REJECT);}
        else if(m==GameModes.SLIDE_PUZZLE) tapSlide(x,y);
        else if(m==GameModes.LIGHTS_OUT){int i=indexAt(squareArea(.76f),4,x,y);if(i>=0){toggleLight(i);moves++;if(allLightsOff())finishRound(1,"解开了",moves+" 步",moves);}}
        else if(m==GameModes.ROCK_PAPER_SCISSORS) tapRps(x,y);
        else if(m==GameModes.MEMORY_MATCH) tapMemory(x,y);
        else if(m==GameModes.NUMBER_TAP) tapNumber(x,y);
        else if(m==GameModes.QUICK_MATH) tapMath(x,y);
        else if(m==GameModes.NIM) tapNim(x,y);
        else if(m==GameModes.DICE_DUEL) rollDice();
        else if(m==GameModes.WHACK_MOLE){int i=indexAt(squareArea(.76f),3,x,y);if(i==target){score++;target=random.nextInt(9);phaseAt=now();haptic(HapticFeedbackConstants.CLOCK_TICK);}}
        else if(m==GameModes.BLACKJACK) tapBlackjack(x,y);
        else if(m==GameModes.QUICK_DRAW) tapQuickDraw();
        else if(m==GameModes.HIGH_LOW) tapHighLow(x,y);
        else if(m==GameModes.ORBIT_TAP) tapOrbit();
        else if(m==GameModes.RING_TIMING) tapRing();
        invalidate();
    }

    private void tapTtt(float x,float y){int i=indexAt(squareArea(.78f),3,x,y);if(i<0||a[i]!=0)return;a[i]=turn==0?1:2;if(checkLine(a[i])){finishRound(1,(a[i]==1?"X":"O")+" 获胜","完成三连",1);return;}if(boardFull()){finishRound(0,"平局","棋盘已满",0);return;}turn=1-turn;if(isSinglePlayer()&&turn==1){int move=bestTttMove();a[move]=2;if(checkLine(2)){finishRound(-1,"手表获胜","再试一次",0);return;}if(boardFull()){finishRound(0,"平局","棋盘已满",0);return;}turn=0;}}
    private boolean checkLine(int v){int[][] l={{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};for(int[] q:l)if(a[q[0]]==v&&a[q[1]]==v&&a[q[2]]==v)return true;return false;}
    private boolean boardFull(){for(int i=0;i<9;i++)if(a[i]==0)return false;return true;}
    private int bestTttMove(){for(int p:new int[]{2,1})for(int i=0;i<9;i++)if(a[i]==0){a[i]=p;boolean w=checkLine(p);a[i]=0;if(w)return i;}if(a[4]==0)return 4;int[] order={0,2,6,8,1,3,5,7};for(int i:order)if(a[i]==0)return i;return 0;}

    private void tapSlide(float x,float y){RectF r=squareArea(.78f);int i=indexAt(r,3,x,y);if(i<0)return;int zero=-1;for(int k=0;k<9;k++)if(a[k]==0)zero=k;if(Math.abs(i/3-zero/3)+Math.abs(i%3-zero%3)==1){a[zero]=a[i];a[i]=0;moves++;if(slideSolved())finishRound(1,"拼好了",moves+" 步",moves);}}
    private void slideMove(int dir,boolean count){int z=0;for(int i=0;i<9;i++)if(a[i]==0)z=i;int row=z/3,col=z%3,n=z;if(dir==0&&row>0)n=z-3;if(dir==1&&row<2)n=z+3;if(dir==2&&col>0)n=z-1;if(dir==3&&col<2)n=z+1;if(n!=z){a[z]=a[n];a[n]=0;if(count)moves++;}}
    private boolean slideSolved(){for(int i=0;i<8;i++)if(a[i]!=i+1)return false;return a[8]==0;}

    private void toggleLight(int idx){int row=idx/4,col=idx%4;int[][] d={{0,0},{1,0},{-1,0},{0,1},{0,-1}};for(int[] q:d){int rr=row+q[0],cc=col+q[1];if(rr>=0&&rr<4&&cc>=0&&cc<4)a[rr*4+cc]^=1;}}
    private boolean allLightsOff(){for(int i=0;i<16;i++)if(a[i]!=0)return false;return true;}

    private void tapRps(float x,float y){RectF r=area();int pick=-1;for(int i=0;i<3;i++)if(buttonCell(r,i,3).contains(x,y))pick=i;if(pick<0)return;int cpu=random.nextInt(3);target=pick;target2=cpu;round++;boolean playerWin=(pick==0&&cpu==1)||(pick==1&&cpu==2)||(pick==2&&cpu==0);if(pick!=cpu){if(playerWin)a[0]++;else a[1]++;}if(a[0]>=2||a[1]>=2||round>=3)finishRound(a[0]>a[1]?1:a[0]<a[1]?-1:0,a[0]>a[1]?"你赢了":a[0]<a[1]?"手表赢了":"平局",a[0]+" : "+a[1],a[0]);}
    private String rpsName(int v){return v==0?"石头":v==1?"剪刀":"布";}

    private void tapMemory(float x,float y){int i=indexAt(squareArea(.78f),4,x,y);if(i<0||b[i]==1)return;if(second>=0){selected=second=-1;}if(selected<0){selected=i;return;}if(i==selected)return;second=i;moves++;if(a[selected]==a[second]){b[selected]=b[second]=1;score++;selected=second=-1;haptic(HapticFeedbackConstants.CONFIRM);if(score==8)finishRound(1,"全部配对",moves+" 次翻牌",moves);}else{haptic(HapticFeedbackConstants.REJECT);}}
    private void tapNumber(float x,float y){int i=indexAt(squareArea(.78f),4,x,y);if(i>=0&&a[i]==next){if(next==1)startedAt=now();next++;if(next>16){int ms=(int)Math.max(1,now()-startedAt);finishRound(1,"完成",String.format(Locale.US,"%.2f 秒",ms/1000f),ms);}else haptic(HapticFeedbackConstants.CLOCK_TICK);}}

    private void newMath(){target=1+random.nextInt(10);target2=1+random.nextInt(10);int ans=target+target2;int pos=random.nextInt(3);int[] vals={ans,Math.max(1,ans-1-random.nextInt(3)),ans+1+random.nextInt(3)};choiceA=vals[pos];choiceB=vals[(pos+1)%3];choiceC=vals[(pos+2)%3];}
    private void tapMath(float x,float y){RectF r=area();int pick=-1;for(int i=0;i<3;i++)if(buttonCell(r,i,3).contains(x,y))pick=i;if(pick<0)return;int[] vals={choiceA,choiceB,choiceC};if(vals[pick]==target+target2){score++;haptic(HapticFeedbackConstants.CONFIRM);}else haptic(HapticFeedbackConstants.REJECT);newMath();}

    private void tapNim(float x,float y){RectF r=area();int take=-1;for(int i=0;i<3;i++)if(buttonCell(r,i,3).contains(x,y))take=i+1;if(take<1||take>target)return;target-=take;moves++;if(target<=0){finishRound(1,turn==0?"你赢了":"玩家 2 赢了","最后一颗被取走",moves);return;}turn=1-turn;if(isSinglePlayer()&&turn==1){int ai=target%4; if(ai==0)ai=1+random.nextInt(Math.min(3,target));target-=Math.min(ai,target);moves++;if(target<=0){finishRound(-1,"手表赢了","最佳策略命中",moves);return;}turn=0;}}

    private void rollDice(){if(isSinglePlayer()&&turn==1)return;int d=1+random.nextInt(6);round=d;a[turn]+=d;if(a[turn]>=20){finishRound(turn==0?1:(isSinglePlayer()?-1:1),turn==0?"玩家 1 获胜":(isSinglePlayer()?"手表获胜":"玩家 2 获胜"),a[0]+" : "+a[1],Math.max(a[0],a[1]));return;}turn=1-turn;if(isSinglePlayer()&&turn==1){int ai=1+random.nextInt(6);a[1]+=ai;round=ai;if(a[1]>=20){finishRound(-1,"手表获胜",a[0]+" : "+a[1],a[0]);return;}turn=0;}}

    private int drawCard(){int v=1+random.nextInt(13);return Math.min(v,10);}
    private int handValue(int[] hand,int count){int sum=0,aces=0;for(int i=0;i<count;i++){sum+=hand[i]==1?11:hand[i];if(hand[i]==1)aces++;}while(sum>21&&aces-->0)sum-=10;return sum;}
    private void drawCards(Canvas c,int[] hand,int count,float cx,float y){float gap=s()*.07f;float start=cx-(count-1)*gap/2;for(int i=0;i<count;i++){scratchRect.set(start+i*gap-s()*.028f,y-s()*.045f,start+i*gap+s()*.028f,y+s()*.045f);RectF q=scratchRect;p.setColor(Color.rgb(235,238,242));c.drawRoundRect(q,dp(5),dp(5),p);text(c,cardName(hand[i]),q.centerX(),q.centerY()+s()*.012f,s()*.026f,BG,true,Paint.Align.CENTER);}}
    private String cardName(int v){return v==1?"A":v==11?"J":v==12?"Q":v==13?"K":String.valueOf(v);}
    private void tapBlackjack(float x,float y){if(pausedPhase)return;RectF r=area();boolean hit=x<r.centerX();if(hit){if(target<a.length)a[target++]=drawCard();int ps=handValue(a,target);if(ps>21){pausedPhase=true;finishRound(-1,"爆牌",ps+" 点",ps);}}else{while(handValue(b,target2)<17&&target2<b.length)b[target2++]=drawCard();pausedPhase=true;int ps=handValue(a,target),ds=handValue(b,target2);int kind=ds>21||ps>ds?1:ps<ds?-1:0;finishRound(kind,kind>0?"你赢了":kind<0?"庄家赢了":"平局",ps+" : "+ds,ps);}}

    private void tapQuickDraw(){if(waiting){finishRound(-1,"太早了","等到绿色再点",0);return;}if(ready){int reaction=(int)Math.max(1,now()-phaseAt);ready=false;finishRound(1,"反应完成",reaction+" ms",reaction);}}
    private void tapHighLow(float x,float y){RectF r=area();boolean higher=x>=r.centerX();int old=target,n=1+random.nextInt(13);while(n==old)n=1+random.nextInt(13);boolean correct=higher?n>old:n<old;if(correct){score++;haptic(HapticFeedbackConstants.CONFIRM);}else haptic(HapticFeedbackConstants.REJECT);target=n;round++;if(round>=10)finishRound(1,"完成",score+" / 10",score);}
    private void tapOrbit(){float diff=Math.abs(angle-target);diff=Math.min(diff,360-diff);if(diff<22){score++;target=random.nextInt(360);haptic(HapticFeedbackConstants.CONFIRM);}else haptic(HapticFeedbackConstants.REJECT);}
    private void tapRing(){float error=Math.abs(ringRadius-.58f);if(error<.10f){score+=error<.04f?2:1;haptic(HapticFeedbackConstants.CONFIRM);}else haptic(HapticFeedbackConstants.REJECT);round++;ringRadius=.10f;if(round>=8)finishRound(1,"完成",score+" 分",score);}

    @Override protected void saveGame(){
        int m=activeGameMode();
        if(!isPersistable(m))return;
        String payload;
        if(m==GameModes.TICTACTOE)payload="ttt|"+join(a,9)+"|"+turn;
        else if(m==GameModes.SLIDE_PUZZLE)payload="slide|"+join(a,9)+"|"+moves;
        else if(m==GameModes.LIGHTS_OUT)payload="lights|"+join(a,16)+"|"+moves;
        else if(m==GameModes.MEMORY_MATCH)payload="memory|"+join(a,16)+"|"+join(b,16)+"|"+score+"|"+moves;
        else if(m==GameModes.NIM)payload="nim|"+target+"|"+turn+"|"+moves;
        else if(m==GameModes.BLACKJACK)payload="bj|"+join(a,target)+"|"+join(b,target2);
        else return;
        GameSaveManager.save(prefs,m,2,isSinglePlayer(),payload);
    }

    @Override protected boolean restoreGame(){
        GameSaveManager.SaveRecord rec=GameSaveManager.load(prefs,activeGameMode());if(rec==null||rec.payload==null)return false;
        try{String[] p=rec.payload.split("\\|",-1);int m=activeGameMode();
            if(m==GameModes.TICTACTOE&&p.length>=3&&"ttt".equals(p[0])){parseInto(p[1],a,9);turn=Integer.parseInt(p[2]);return true;}
            if(m==GameModes.SLIDE_PUZZLE&&p.length>=3&&"slide".equals(p[0])){parseInto(p[1],a,9);moves=Integer.parseInt(p[2]);return true;}
            if(m==GameModes.LIGHTS_OUT&&p.length>=3&&"lights".equals(p[0])){parseInto(p[1],a,16);moves=Integer.parseInt(p[2]);return true;}
            if(m==GameModes.MEMORY_MATCH&&p.length>=5&&"memory".equals(p[0])){parseInto(p[1],a,16);parseInto(p[2],b,16);score=Integer.parseInt(p[3]);moves=Integer.parseInt(p[4]);selected=second=-1;return true;}
            if(m==GameModes.NIM&&p.length>=4&&"nim".equals(p[0])){target=Integer.parseInt(p[1]);turn=Integer.parseInt(p[2]);moves=Integer.parseInt(p[3]);return target>0;}
            if(m==GameModes.BLACKJACK&&p.length>=3&&"bj".equals(p[0])){target=parseInto(p[1],a,a.length);target2=parseInto(p[2],b,b.length);return target>=2&&target2>=2;}
            if(LegacySaveMigrator.isLegacy(prefs)&&restoreLegacy(rec.payload)){saveGame();return true;}
        }catch(RuntimeException ignored){} return false;
    }


    private boolean restoreLegacy(String data){
        int m=activeGameMode();
        try{
            if(m==GameModes.TICTACTOE){parseInto(data,a,9);turn=prefs.getInt("save_turn",1)==2?1:0;return true;}
            if(m==GameModes.SLIDE_PUZZLE){parseInto(data,a,9);moves=prefs.getInt("save_aux",0);return true;}
            if(m==GameModes.LIGHTS_OUT){if(data.length()!=16)return false;for(int i=0;i<16;i++)a[i]=data.charAt(i)=='1'?1:0;moves=prefs.getInt("save_aux",0);return true;}
            if(m==GameModes.MEMORY_MATCH){String[] q=data.split("\\|",-1);if(q.length!=2)return false;parseInto(q[0],a,16);if(q[1].length()!=16)return false;score=0;for(int i=0;i<16;i++){b[i]=q[1].charAt(i)=='1'?1:0;if(b[i]==1)score++;}score/=2;moves=prefs.getInt("save_aux",0);selected=second=-1;return true;}
            if(m==GameModes.NIM){target=prefs.getInt("save_aux",15);turn=prefs.getInt("save_turn",1)==2?1:0;moves=0;return target>0&&target<=21;}
            if(m==GameModes.BLACKJACK){String[] q=data.split(",");if(q.length!=5&&q.length!=7)return false;int ps=Integer.parseInt(q[0]),ds=Integer.parseInt(q[1]),pc=Integer.parseInt(q[2]),dc=Integer.parseInt(q[3]);int pa=q.length==7?Integer.parseInt(q[5]):0,da=q.length==7?Integer.parseInt(q[6]):0;target=fillLegacyHand(a,ps,pc,pa);target2=fillLegacyHand(b,ds,dc,da);return target>=2&&target2>=2;}
        }catch(RuntimeException ignored){}
        return false;
    }

    private int fillLegacyHand(int[] out,int total,int count,int softAces){
        Arrays.fill(out,0);if(count<=0||count>out.length||total<count||total>21)return 0;
        int pos=0,remain=total;
        for(int i=0;i<softAces&&pos<count;i++){out[pos++]=1;remain-=11;}
        int left=count-pos;
        for(int i=0;i<left;i++){int slots=left-i-1;int value=Math.min(10,remain-slots);if(value<1)return 0;out[pos++]=value;remain-=value;}
        return remain==0?count:0;
    }

        private boolean isPersistable(int m){return m==GameModes.TICTACTOE||m==GameModes.SLIDE_PUZZLE||m==GameModes.LIGHTS_OUT||m==GameModes.MEMORY_MATCH||m==GameModes.NIM||m==GameModes.BLACKJACK;}
    private String join(int[] values,int count){StringBuilder sb=new StringBuilder();for(int i=0;i<count;i++){if(i>0)sb.append(',');sb.append(values[i]);}return sb.toString();}
    private int parseInto(String value,int[] out,int max){if(value.isEmpty())return 0;String[] q=value.split(",");int n=Math.min(Math.min(q.length,max),out.length);for(int i=0;i<n;i++)out[i]=Integer.parseInt(q[i]);return n;}
}
