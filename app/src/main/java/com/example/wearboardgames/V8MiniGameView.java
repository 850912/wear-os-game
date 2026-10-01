package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

import java.util.Arrays;
import java.util.Random;

/** Ten distinct v8 short-session games grouped by shared UI, as prescribed for simple games. */
public final class V8MiniGameView extends BaseGameView {
    public static final int AVOIDER=200, LANE_SWITCH=201, FALLING_GATE=202, MIRROR_TAP=203,
            SIZE_SORT=204, BINARY_SWITCH=205, PENALTY_KICK=206, DARTS=207,
            BOWLING=208, NUMBER_MEMORY=209;
    private final Random random = new Random();
    private final float[] xs = new float[8], ys = new float[8], rs = new float[8];
    private final int[] order = new int[8];
    private int score, round, target, index, lane, bits, length;
    private long started, lastTick, revealUntil;
    private float playerX, marker, goalie, aimX, aimY;
    private boolean over;
    private String digits = "", entered = "";

    public V8MiniGameView(Context context) { super(context); }
    public static boolean supportsMode(int m) { return m >= AVOIDER && m <= NUMBER_MEMORY; }
    @Override protected boolean supportsGameMode(int candidate) { return supportsMode(candidate); }
    @Override protected int gameMode() { return activeGameMode(); }
    private RectF board() { return gamePanel(roundScreen ? .105f : .07f); }

    @Override protected void resetGame() {
        score=round=index=bits=0; target=0; lane=1; length=4; playerX=.5f; marker=.5f; goalie=.5f;
        aimX=aimY=.5f; over=false; digits=entered=""; started=lastTick=now(); revealUntil=0;
        Arrays.fill(xs,0); Arrays.fill(ys,0); Arrays.fill(rs,0); Arrays.fill(order,0);
        switch(gameMode()) {
            case AVOIDER: initHazards(); break;
            case LANE_SWITCH: initLanes(); break;
            case FALLING_GATE: initGates(); break;
            case MIRROR_TAP: newMirror(); break;
            case SIZE_SORT: newSizes(); break;
            case BINARY_SWITCH: newBinary(); break;
            case PENALTY_KICK: newPenalty(); break;
            case DARTS: newDart(); break;
            case NUMBER_MEMORY: newNumberRound(); break;
        }
    }

    private String title(){switch(gameMode()){
        case AVOIDER:return"自由闪避";case LANE_SWITCH:return"换道冲刺";case FALLING_GATE:return"落块穿隙";
        case MIRROR_TAP:return"镜像点击";case SIZE_SORT:return"大小排序";case BINARY_SWITCH:return"二进制开关";
        case PENALTY_KICK:return"点球大战";case DARTS:return"迷你飞镖";case BOWLING:return"迷你保龄";default:return"数字记忆";}}
    private String subtitle(){switch(gameMode()){
        case AVOIDER:return"拖动躲避 · "+score+" 秒";case LANE_SWITCH:return"点赛道换道 · "+score+" 分";case FALLING_GATE:return"拖动穿过缺口 · "+score+" 分";
        case MIRROR_TAP:return"点击目标的水平镜像 · "+round+"/8";case SIZE_SORT:return"从小到大点击 · "+index+"/6";case BINARY_SWITCH:return"拨出数字 "+target+" · "+round+"/8";
        case PENALTY_KICK:return"避开门将 · "+score+"/"+round;case DARTS:return"5 镖总分 · "+score;case BOWLING:return"向上滑出球 · "+score+" 分";default:return revealUntil>now()?"记住数字":"输入 · "+entered.length()+"/"+length;}}

    @Override protected void drawGame(Canvas c){RectF r=board();update(r);drawHeader(c,title(),over?"本局结束":subtitle());panel(c,r);switch(gameMode()){
        case AVOIDER:drawAvoider(c,r);break;case LANE_SWITCH:drawLane(c,r);break;case FALLING_GATE:drawGates(c,r);break;
        case MIRROR_TAP:drawMirror(c,r);break;case SIZE_SORT:drawSizes(c,r);break;case BINARY_SWITCH:drawBinary(c,r);break;
        case PENALTY_KICK:drawPenalty(c,r);break;case DARTS:drawDarts(c,r);break;case BOWLING:drawBowling(c,r);break;default:drawNumberMemory(c,r);}
        if(needsFrames())animateNext();}
    private boolean needsFrames(){int m=gameMode();return !over&&(m==AVOIDER||m==LANE_SWITCH||m==FALLING_GATE||m==PENALTY_KICK||m==DARTS||m==NUMBER_MEMORY);}

    private void update(RectF r){if(over)return;long t=now();float dt=Math.min(.04f,Math.max(0,(t-lastTick)/1000f));lastTick=t;switch(gameMode()){
        case AVOIDER:updateAvoider(dt);break;case LANE_SWITCH:updateLanes(dt);break;case FALLING_GATE:updateGates(dt);break;
        case PENALTY_KICK:goalie=.5f+(float)Math.sin(t/430.0)*.32f;break;
        case DARTS:aimX=.5f+(float)Math.sin(t/510.0)*.31f;aimY=.5f+(float)Math.cos(t/690.0)*.29f;break;
        case NUMBER_MEMORY:if(revealUntil>0&&t>=revealUntil){revealUntil=0;invalidate();}break;}}

    private void initHazards(){for(int i=0;i<6;i++){xs[i]=.12f+random.nextFloat()*.76f;ys[i]=-random.nextFloat()*1.2f;rs[i]=.035f+random.nextFloat()*.025f;}started=now();}
    private void updateAvoider(float dt){for(int i=0;i<6;i++){ys[i]+=dt*(.24f+.03f*i);if(ys[i]>1.08f){ys[i]=-.10f;xs[i]=.10f+random.nextFloat()*.80f;}if(Math.abs(xs[i]-playerX)<rs[i]+.045f&&ys[i]>.79f&&ys[i]<.95f){over=true;finishRound(score>=12?1:-1,"撞到了","坚持 "+score+" 秒",score);return;}}score=(int)((now()-started)/1000);if(score>=20){over=true;finishRound(1,"闪避成功","坚持 20 秒",20);}}
    private void drawAvoider(Canvas c,RectF r){for(int i=0;i<6;i++){p.setColor(BAD);c.drawCircle(r.left+xs[i]*r.width(),r.top+ys[i]*r.height(),r.width()*rs[i],p);}p.setColor(PRIMARY);c.drawCircle(r.left+playerX*r.width(),r.top+r.height()*.88f,r.width()*.045f,p);}

    private void initLanes(){for(int i=0;i<5;i++){order[i]=random.nextInt(3);ys[i]=-i*.28f;}started=now();}
    private void updateLanes(float dt){for(int i=0;i<5;i++){ys[i]+=dt*.34f;if(ys[i]>.96f){if(order[i]==lane){over=true;finishRound(score>=12?1:-1,"撞上路障","通过 "+score+" 个",score);return;}score++;ys[i]=-.35f;order[i]=random.nextInt(3);}}if(score>=20){over=true;finishRound(1,"冲刺完成","通过 20 个",20);}}
    private void drawLane(Canvas c,RectF r){p.setColor(MUTED);p.setStrokeWidth(dp(2));for(int i=1;i<3;i++)c.drawLine(r.left+r.width()*i/3f,r.top,r.left+r.width()*i/3f,r.bottom,p);for(int i=0;i<5;i++){float cx=r.left+r.width()*(order[i]+.5f)/3f,cy=r.top+ys[i]*r.height();p.setColor(BAD);c.drawRoundRect(new RectF(cx-r.width()*.09f,cy-r.height()*.035f,cx+r.width()*.09f,cy+r.height()*.035f),dp(6),dp(6),p);}p.setColor(GOOD);c.drawCircle(r.left+r.width()*(lane+.5f)/3f,r.top+r.height()*.87f,r.width()*.045f,p);}

    private void initGates(){for(int i=0;i<4;i++){ys[i]=-i*.32f;xs[i]=.20f+random.nextFloat()*.60f;}started=now();}
    private void updateGates(float dt){for(int i=0;i<4;i++){ys[i]+=dt*.27f;if(ys[i]>.92f){if(Math.abs(playerX-xs[i])>.16f){over=true;finishRound(score>=8?1:-1,"没穿过缺口","通过 "+score+" 道",score);return;}score++;ys[i]=-.35f;xs[i]=.20f+random.nextFloat()*.60f;}}if(score>=15){over=true;finishRound(1,"穿隙完成","通过 15 道",15);}}
    private void drawGates(Canvas c,RectF r){for(int i=0;i<4;i++){float cy=r.top+ys[i]*r.height(),gap=r.left+xs[i]*r.width(),half=r.width()*.16f;p.setColor(SECONDARY);c.drawRect(r.left,cy-r.height()*.018f,gap-half,cy+r.height()*.018f,p);c.drawRect(gap+half,cy-r.height()*.018f,r.right,cy+r.height()*.018f,p);}p.setColor(PRIMARY);c.drawCircle(r.left+playerX*r.width(),r.top+r.height()*.87f,r.width()*.035f,p);}

    private void newMirror(){aimX=.15f+random.nextFloat()*.70f;aimY=.20f+random.nextFloat()*.55f;}
    private void drawMirror(Canvas c,RectF r){p.setColor(SURFACE_HIGH);c.drawRect(r.centerX()-dp(1),r.top,r.centerX()+dp(1),r.bottom,p);p.setColor(PRIMARY);c.drawCircle(r.left+aimX*r.width(),r.top+aimY*r.height(),r.width()*.045f,p);}

    private void newSizes(){for(int i=0;i<6;i++){xs[i]=.20f+.30f*(i%3);ys[i]=.30f+.38f*(i/3);rs[i]=.026f+i*.009f;order[i]=i;}for(int i=5;i>0;i--){int j=random.nextInt(i+1);float tr=rs[i];rs[i]=rs[j];rs[j]=tr;}index=0;}
    private void drawSizes(Canvas c,RectF r){for(int i=0;i<6;i++){if(order[i]<0)continue;p.setColor(i==target?PRIMARY:TEXT);c.drawCircle(r.left+xs[i]*r.width(),r.top+ys[i]*r.height(),r.width()*rs[i],p);}}
    private int smallestRemaining(){int best=-1;float br=99;for(int i=0;i<6;i++)if(order[i]>=0&&rs[i]<br){br=rs[i];best=i;}return best;}

    private void newBinary(){target=1+random.nextInt(31);bits=0;}
    private void drawBinary(Canvas c,RectF r){text(c,String.valueOf(bits),r.centerX(),r.top+r.height()*.27f,s()*.070f,TEXT,true,Paint.Align.CENTER);for(int i=0;i<5;i++){RectF q=bitCell(r,i);boolean on=(bits&(1<<i))!=0;p.setColor(on?GOOD:SURFACE_HIGH);c.drawRoundRect(q,dp(9),dp(9),p);text(c,String.valueOf(1<<i),q.centerX(),q.centerY()+q.height()*.10f,q.height()*.26f,on?BG:TEXT,true,Paint.Align.CENTER);}}
    private RectF bitCell(RectF r,int i){float gap=dp(4),w=(r.width()-gap*6)/5f,y=r.top+r.height()*.57f,h=r.height()*.22f;float l=r.left+gap+i*(w+gap);return new RectF(l,y,l+w,y+h);}

    private void newPenalty(){goalie=.5f;aimX=.18f+random.nextFloat()*.64f;aimY=.20f+random.nextFloat()*.45f;}
    private void drawPenalty(Canvas c,RectF r){RectF goal=new RectF(r.left+r.width()*.12f,r.top+r.height()*.17f,r.right-r.width()*.12f,r.top+r.height()*.72f);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));p.setColor(TEXT);c.drawRect(goal,p);p.setStyle(Paint.Style.FILL);float gy=goal.bottom-goal.height()*.10f;p.setColor(SECONDARY);c.drawRoundRect(new RectF(goal.left+goalie*goal.width()-r.width()*.08f,gy-r.height()*.035f,goal.left+goalie*goal.width()+r.width()*.08f,gy+r.height()*.035f),dp(8),dp(8),p);p.setColor(GOOD);c.drawCircle(goal.left+aimX*goal.width(),goal.top+aimY*goal.height(),r.width()*.025f,p);}

    private void newDart(){aimX=aimY=.5f;}
    private void drawDarts(Canvas c,RectF r){float cx=r.centerX(),cy=r.centerY();for(int i=4;i>=1;i--){p.setColor(i%2==0?SURFACE_HIGH:SECONDARY);c.drawCircle(cx,cy,r.width()*.075f*i,p);}p.setColor(BG);c.drawCircle(cx,cy,r.width()*.055f,p);p.setColor(PRIMARY);c.drawCircle(r.left+aimX*r.width(),r.top+aimY*r.height(),r.width()*.020f,p);}

    private void drawBowling(Canvas c,RectF r){for(int row=0;row<4;row++)for(int col=0;col<=row;col++){float cx=r.centerX()+(col-row/2f)*r.width()*.12f,cy=r.top+r.height()*(.20f+row*.09f);p.setColor(TEXT);c.drawCircle(cx,cy,r.width()*.018f,p);}p.setColor(PRIMARY);c.drawCircle(r.centerX(),r.bottom-r.height()*.10f,r.width()*.045f,p);text(c,"向上滑，越直越接近全倒",r.centerX(),r.bottom-r.height()*.22f,s()*.020f,MUTED,false,Paint.Align.CENTER);}

    private void newNumberRound(){StringBuilder sb=new StringBuilder();for(int i=0;i<length;i++)sb.append(random.nextInt(10));digits=sb.toString();entered="";revealUntil=now()+Math.max(900,1700-length*80L);}
    private void drawNumberMemory(Canvas c,RectF r){if(revealUntil>now()){text(c,digits,r.centerX(),r.centerY(),s()*.070f,TEXT,true,Paint.Align.CENTER);return;}for(int n=0;n<10;n++){int row=n/5,col=n%5;float gap=dp(4),w=(r.width()-gap*6)/5f,h=r.height()*.25f;float l=r.left+gap+col*(w+gap),top=r.top+r.height()*(.18f+row*.36f);RectF q=new RectF(l,top,l+w,top+h);p.setColor(SURFACE_HIGH);c.drawRoundRect(q,dp(8),dp(8),p);text(c,String.valueOf(n),q.centerX(),q.centerY()+q.height()*.12f,q.height()*.35f,TEXT,true,Paint.Align.CENTER);}}

    @Override protected void onGameTouchMove(float x,float y){if(gameMode()==AVOIDER||gameMode()==FALLING_GATE)movePlayer(x);}
    @Override protected void onGameTouchDown(float x,float y){if(gameMode()==AVOIDER||gameMode()==FALLING_GATE)movePlayer(x);}
    private void movePlayer(float x){RectF r=board();playerX=clamp((x-r.left)/r.width(),.05f,.95f);invalidate();}

    @Override protected void onGameTap(float x,float y){if(over)return;RectF r=board();switch(gameMode()){
        case LANE_SWITCH:{float px=clamp((x-r.left)/r.width(),0, .999f);lane=Math.min(2,(int)(px*3));haptic(HapticFeedbackConstants.CLOCK_TICK);break;}
        case MIRROR_TAP:{float ex=r.left+(1f-aimX)*r.width(),ey=r.top+aimY*r.height();float err=(float)Math.hypot(x-ex,y-ey);round++;if(err<r.width()*.10f){score++;haptic(HapticFeedbackConstants.CONFIRM);}if(round>=8){over=true;finishRound(score>=6?1:0,"镜像完成","命中 "+score+" / 8",score);}else newMirror();break;}
        case SIZE_SORT:{int hit=hitSize(r,x,y);if(hit<0)break;int expected=smallestRemaining();if(hit==expected){order[hit]=-1;index++;haptic(HapticFeedbackConstants.CONFIRM);if(index>=6){over=true;finishRound(1,"排序完成","全部正确",6);}}else{over=true;finishRound(-1,"顺序错误","应先点更小的圆",index);}break;}
        case BINARY_SWITCH:{for(int i=0;i<5;i++)if(bitCell(r,i).contains(x,y)){bits^=1<<i;haptic(HapticFeedbackConstants.CLOCK_TICK);if(bits==target){score++;round++;if(round>=8){over=true;finishRound(1,"解码完成","8 / 8",8);}else newBinary();}break;}break;}
        case PENALTY_KICK:tapPenalty(r,x,y);break;
        case DARTS:throwDart(r);break;
        case NUMBER_MEMORY:tapDigit(r,x,y);break;
    }invalidate();}

    private int hitSize(RectF r,float x,float y){for(int i=0;i<6;i++)if(order[i]>=0){float cx=r.left+xs[i]*r.width(),cy=r.top+ys[i]*r.height();if(Math.hypot(x-cx,y-cy)<=r.width()*Math.max(rs[i],.045f))return i;}return -1;}
    private void tapPenalty(RectF r,float x,float y){RectF goal=new RectF(r.left+r.width()*.12f,r.top+r.height()*.17f,r.right-r.width()*.12f,r.top+r.height()*.72f);if(!goal.contains(x,y))return;float nx=(x-goal.left)/goal.width(),ny=(y-goal.top)/goal.height();boolean blocked=Math.abs(nx-goalie)<.16f&&ny>.62f;round++;if(!blocked){score++;haptic(HapticFeedbackConstants.CONFIRM);}else haptic(HapticFeedbackConstants.REJECT);if(round>=8){over=true;finishRound(score>=5?1:0,"点球结束","进球 "+score+" / 8",score);}else newPenalty();}
    private void throwDart(RectF r){float dx=aimX-.5f,dy=aimY-.5f,d=(float)Math.hypot(dx,dy);int pts=d<.065f?10:d<.14f?5:d<.23f?3:d<.33f?1:0;score+=pts;round++;haptic(pts>=5?HapticFeedbackConstants.CONFIRM:HapticFeedbackConstants.CLOCK_TICK);if(round>=5){over=true;finishRound(score>=25?1:0,"飞镖结束","总分 "+score,score);}}
    private void tapDigit(RectF r,float x,float y){if(revealUntil>now())return;int found=-1;for(int n=0;n<10;n++){int row=n/5,col=n%5;float gap=dp(4),w=(r.width()-gap*6)/5f,h=r.height()*.25f;float l=r.left+gap+col*(w+gap),top=r.top+r.height()*(.18f+row*.36f);if(new RectF(l,top,l+w,top+h).contains(x,y)){found=n;break;}}if(found<0)return;entered+=found;if(!digits.startsWith(entered)){over=true;finishRound(-1,"记错了","正确数字 "+digits,score);return;}if(entered.length()==digits.length()){score++;round++;haptic(HapticFeedbackConstants.CONFIRM);if(round>=5){over=true;finishRound(1,"记忆完成","最长 "+length+" 位",length);}else{length++;newNumberRound();}}}

    @Override protected void onGameSwipe(float dx,float dy){if(gameMode()!=BOWLING||over||dy>=-dp(18))return;float straight=Math.abs(dx)/Math.max(1f,Math.abs(dy));float force=Math.min(1f,(float)Math.hypot(dx,dy)/(s()*.55f));int pins=Math.max(0,Math.min(10,Math.round(1f+9f*force-straight*8f)));score+=pins;round++;haptic(pins==10?HapticFeedbackConstants.CONFIRM:HapticFeedbackConstants.CLOCK_TICK);if(round>=5){over=true;finishRound(score>=35?1:0,"保龄结束","总分 "+score,score);}invalidate();}
}
