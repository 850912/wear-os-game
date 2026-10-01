package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

import java.util.Arrays;
import java.util.Random;

/**
 * v8 expansion pack. These are intentionally compact, distinct short-session games that share
 * one renderer instead of creating another monolithic all-game hub.
 */
public final class ExpansionGameView extends BaseGameView {
    public static final int MEMORY_SEQUENCE=100, COLOR_RUSH=101, AIM_TRAINER=102, MINI_GOLF=103,
            AIR_HOCKEY=104, MINI_BASKETBALL=105, REACTION_GRID=106, DOT_CONNECT=107,
            TAP_TEMPO=108, BALANCE_BAR=109, COMPASS_MATCH=110, PATTERN_LOCK=111,
            TARGET_SUM=112, HOLD_RELEASE=113;
    private final Random random=new Random();
    private int score,round,index,target,first=-1; private long deadline,phaseStart,lastTick,holdStart;
    private boolean over,showing,running,holding; private float a,b,c,d,vx,vy;
    private final int[] values=new int[12]; private final float[] xs=new float[12],ys=new float[12];
    private final RectF cellRect=new RectF(), scratchRect=new RectF();

    public ExpansionGameView(Context context){super(context);}
    public static boolean supportsMode(int m){return m>=MEMORY_SEQUENCE&&m<=HOLD_RELEASE;}
    @Override protected boolean supportsGameMode(int candidate){return supportsMode(candidate);}
    @Override protected int gameMode(){return activeGameMode();}
    private RectF board(){return gamePanel(roundScreen?.105f:.07f);}

    @Override protected void resetGame(){score=round=index=0;target=0;first=-1;deadline=phaseStart=lastTick=now();holdStart=0;over=false;showing=false;running=false;holding=false;a=b=c=d=vx=vy=0;Arrays.fill(values,0);newRound();}

    private String title(){switch(gameMode()){case MEMORY_SEQUENCE:return"记忆序列";case COLOR_RUSH:return"色彩冲刺";case AIM_TRAINER:return"瞄准训练";case MINI_GOLF:return"迷你高尔夫";case AIR_HOCKEY:return"空气曲棍球";case MINI_BASKETBALL:return"迷你投篮";case REACTION_GRID:return"反应九宫格";case DOT_CONNECT:return"连点轨迹";case TAP_TEMPO:return"节拍挑战";case BALANCE_BAR:return"平衡杆";case COMPASS_MATCH:return"方位校准";case PATTERN_LOCK:return"图案锁";case TARGET_SUM:return"目标和";default:return"蓄力释放";}}
    private String subtitle(){switch(gameMode()){case MEMORY_SEQUENCE:return showing?"记住亮起顺序":"按顺序复现 · "+index+"/"+values[0];case COLOR_RUSH:return"20 秒 · 得分 "+score;case AIM_TRAINER:return"20 秒 · 命中 "+score;case MINI_GOLF:return"杆数 "+Math.max(1,round);case AIR_HOCKEY:return score+" : "+round;case MINI_BASKETBALL:return"命中 "+score+" / "+round;case REACTION_GRID:return"20 秒 · "+score+" 分";case DOT_CONNECT:return"第 "+(index+1)+" / 6 点";case TAP_TEMPO:return index==0?"轻点开始 · 目标 0.65 秒":"节奏误差越小越好";case BALANCE_BAR:return"保持 20 秒 · "+score+" 秒";case COMPASS_MATCH:return"对准亮线 · "+round+" / 8";case PATTERN_LOCK:return showing?"记住 4 个格子":"复现 · "+index+" / 4";case TARGET_SUM:return"凑出 "+target+" · "+round+"/10";default:return"松手落在绿色区 · "+round+"/8";}}

    @Override protected void drawGame(Canvas canvas){RectF r=board();update(r);drawHeader(canvas,title(),over?"本局结束":subtitle());panel(canvas,r);switch(gameMode()){case MEMORY_SEQUENCE:drawMemory(canvas,r);break;case COLOR_RUSH:drawColor(canvas,r);break;case AIM_TRAINER:drawAim(canvas,r);break;case MINI_GOLF:drawGolf(canvas,r);break;case AIR_HOCKEY:drawHockey(canvas,r);break;case MINI_BASKETBALL:drawBasket(canvas,r);break;case REACTION_GRID:drawGrid(canvas,r);break;case DOT_CONNECT:drawDots(canvas,r);break;case TAP_TEMPO:drawTempo(canvas,r);break;case BALANCE_BAR:drawBalance(canvas,r);break;case COMPASS_MATCH:drawCompass(canvas,r);break;case PATTERN_LOCK:drawPattern(canvas,r);break;case TARGET_SUM:drawSum(canvas,r);break;default:drawHold(canvas,r);}if(needsFrames())animateNext();}
    private boolean needsFrames(){int m=gameMode();return !over&&(m==AIM_TRAINER||m==MINI_GOLF||m==AIR_HOCKEY||m==MINI_BASKETBALL||m==BALANCE_BAR||m==COMPASS_MATCH||m==HOLD_RELEASE||m==COLOR_RUSH||m==REACTION_GRID||m==MEMORY_SEQUENCE||m==PATTERN_LOCK);}

    private void newRound(){long t=now();switch(gameMode()){
        case MEMORY_SEQUENCE:{int len=Math.min(7,4+round);values[0]=len;for(int i=0;i<len;i++)values[i+1]=random.nextInt(9);showing=true;phaseStart=t;index=0;break;}
        case COLOR_RUSH:target=random.nextInt(4);deadline=t+20000;break;
        case AIM_TRAINER:deadline=t+20000;a=.5f;b=.5f;c=random.nextFloat()*6.28f;break;
        case MINI_GOLF:a=.5f;b=.86f;c=.25f+random.nextFloat()*.5f;d=.15f+random.nextFloat()*.20f;vx=vy=0;round=1;break;
        case AIR_HOCKEY:a=.5f;b=.5f;c=.5f;d=.80f;vx=(random.nextBoolean()?1:-1)*.35f;vy=.48f;lastTick=t;break;
        case MINI_BASKETBALL:a=0;b=.62f;c=.14f+random.nextFloat()*.72f;round=0;score=0;lastTick=t;break;
        case REACTION_GRID:target=random.nextInt(9);deadline=t+20000;break;
        case DOT_CONNECT:for(int i=0;i<6;i++){xs[i]=.14f+random.nextFloat()*.72f;ys[i]=.12f+random.nextFloat()*.72f;}index=0;break;
        case TAP_TEMPO:index=0;score=0;phaseStart=0;break;
        case BALANCE_BAR:a=.5f;vx=.06f;score=0;deadline=t+20000;lastTick=t;break;
        case COMPASS_MATCH:a=0;target=random.nextInt(360);round=0;lastTick=t;break;
        case PATTERN_LOCK:for(int i=0;i<4;i++){int v;do v=random.nextInt(9);while(contains(values,i,v));values[i]=v;}showing=true;phaseStart=t;index=0;round=0;break;
        case TARGET_SUM:round=0;score=0;makeSum();break;
        case HOLD_RELEASE:round=0;score=0;target=25+random.nextInt(51);break;
    }}
    private boolean contains(int[] arr,int n,int v){for(int i=0;i<n;i++)if(arr[i]==v)return true;return false;}

    private void update(RectF r){if(over)return;long t=now();float dt=Math.min(.04f,Math.max(0,(t-lastTick)/1000f));switch(gameMode()){
        case MEMORY_SEQUENCE:if(showing&&t-phaseStart>values[0]*430L+500){showing=false;index=0;}break;
        case PATTERN_LOCK:if(showing&&t-phaseStart>1900){showing=false;index=0;}break;
        case COLOR_RUSH:case AIM_TRAINER:case REACTION_GRID:if(t>=deadline){finishScore("时间到");}break;
        case MINI_GOLF:if(Math.abs(vx)+Math.abs(vy)>.003f){a+=vx*dt;b+=vy*dt;vx*=(float)Math.pow(.14,dt);vy*=(float)Math.pow(.14,dt);if(a<.04f||a>.96f){vx=-vx;a=clamp(a,.04f,.96f);}if(b<.04f||b>.96f){vy=-vy;b=clamp(b,.04f,.96f);}if(dist(a,b,c,d)<.055f&&Math.abs(vx)+Math.abs(vy)<.20f){over=true;finishRound(1,"进洞！","用了 "+round+" 杆",round);}}lastTick=t;break;
        case AIR_HOCKEY:updateHockey(dt);lastTick=t;break;
        case MINI_BASKETBALL:a+=dt*.88f;if(a>1)a-=1;lastTick=t;break;
        case BALANCE_BAR:vx+=(random.nextFloat()-.5f)*dt*.16f;a+=vx*dt;if(a<0||a>1){over=true;finishRound(-1,"失去平衡","坚持 "+score+" 秒",score);}else score=(int)((20000-(deadline-t))/1000);if(t>=deadline&&!over){over=true;finishRound(1,"稳住了！","坚持 20 秒",20);}lastTick=t;break;
        case COMPASS_MATCH:a=(a+dt*92f)%360f;lastTick=t;break;
        case HOLD_RELEASE:lastTick=t;break;
    }}
    private void finishScore(String title){over=true;finishRound(score>=8?1:0,title,"得分 "+score,score);}
    private float dist(float x1,float y1,float x2,float y2){return(float)Math.hypot(x1-x2,y1-y2);}

    private RectF cell(RectF r,int idx,int n){int row=idx/n,col=idx%n;float gap=dp(4),w=(r.width()-gap*(n+1))/n,h=(r.height()-gap*(n+1))/n;cellRect.set(r.left+gap+col*(w+gap),r.top+gap+row*(h+gap),r.left+gap+col*(w+gap)+w,r.top+gap+row*(h+gap)+h);return cellRect;}
    private void drawMemory(Canvas c0,RectF r){int active=-1;if(showing){long e=now()-phaseStart;if(e>=300){int slot=(int)((e-300)/430);if(slot<values[0]&&((e-300)%430)<270)active=values[slot+1];}}for(int i=0;i<9;i++){RectF q=cell(r,i,3);p.setColor(i==active?PRIMARY:SURFACE_HIGH);c0.drawRoundRect(q,dp(10),dp(10),p);}}
    private void drawColor(Canvas c0,RectF r){int[] cs={Color.rgb(235,90,96),Color.rgb(79,184,114),Color.rgb(82,139,232),Color.rgb(236,191,71)};text(c0,"目标",r.centerX(),r.top+r.height()*.16f,s()*.026f,MUTED,false,Paint.Align.CENTER);p.setColor(cs[target]);c0.drawCircle(r.centerX(),r.top+r.height()*.28f,s()*.045f,p);for(int i=0;i<4;i++){float x=r.left+r.width()*(.20f+.20f*i),y=r.top+r.height()*.67f;p.setColor(cs[i]);c0.drawCircle(x,y,s()*.055f,p);}}
    private void drawAim(Canvas c0,RectF r){float t=(now()%5000)/5000f*6.283f;a=.5f+(float)Math.cos(t)*.25f;b=.5f+(float)Math.sin(t*1.37f)*.24f;float x=r.left+a*r.width(),y=r.top+b*r.height();p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(4));p.setColor(PRIMARY);c0.drawCircle(x,y,s()*.045f,p);p.setStyle(Paint.Style.FILL);p.setColor(BAD);c0.drawCircle(x,y,s()*.013f,p);}
    private void drawGolf(Canvas c0,RectF r){float bx=r.left+a*r.width(),by=r.top+b*r.height(),hx=r.left+c*r.width(),hy=r.top+d*r.height();p.setColor(Color.rgb(43,111,65));c0.drawCircle(hx,hy,s()*.035f,p);p.setColor(Color.BLACK);c0.drawCircle(hx,hy,s()*.015f,p);p.setColor(TEXT);c0.drawCircle(bx,by,s()*.022f,p);}
    private void drawHockey(Canvas c0,RectF r){float px=r.left+a*r.width(),py=r.top+b*r.height();p.setColor(TEXT);c0.drawCircle(px,py,s()*.020f,p);p.setColor(PRIMARY);c0.drawCircle(r.left+c*r.width(),r.top+d*r.height(),s()*.055f,p);p.setColor(SECONDARY);float aiX=clamp(a,.12f,.88f);c0.drawCircle(r.left+aiX*r.width(),r.top+r.height()*.16f,s()*.052f,p);}
    private void drawBasket(Canvas c0,RectF r){float y=r.top+r.height()*.65f;scratchRect.set(r.left+r.width()*.12f,y,r.right-r.width()*.12f,y+dp(12));RectF bar=scratchRect;p.setColor(SURFACE_HIGH);c0.drawRoundRect(bar,dp(6),dp(6),p);float cx=bar.left+c*bar.width();p.setColor(GOOD);c0.drawRect(cx-bar.width()*.065f,bar.top,cx+bar.width()*.065f,bar.bottom,p);p.setColor(PRIMARY);float mx=bar.left+a*bar.width();c0.drawCircle(mx,bar.centerY(),dp(9),p);text(c0,"篮筐",r.centerX(),r.top+r.height()*.26f,s()*.042f,TEXT,true,Paint.Align.CENTER);}
    private void drawGrid(Canvas c0,RectF r){for(int i=0;i<9;i++){RectF q=cell(r,i,3);p.setColor(i==target?GOOD:SURFACE_HIGH);c0.drawRoundRect(q,dp(10),dp(10),p);}}
    private void drawDots(Canvas c0,RectF r){for(int i=0;i<6;i++){float x=r.left+xs[i]*r.width(),y=r.top+ys[i]*r.height();p.setColor(i<index?GOOD:(i==index?PRIMARY:SURFACE_HIGH));c0.drawCircle(x,y,s()*.032f,p);text(c0,String.valueOf(i+1),x,y+s()*.011f,s()*.026f,TEXT,true,Paint.Align.CENTER);}}
    private void drawTempo(Canvas c0,RectF r){float pulse=(float)((now()%650)/650.0);float rad=s()*(.035f+.09f*pulse);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(4));p.setColor(PRIMARY);c0.drawCircle(r.centerX(),r.centerY(),rad,p);p.setStyle(Paint.Style.FILL);text(c0,index==0?"START":index+" / 10",r.centerX(),r.centerY()+s()*.014f,s()*.05f,TEXT,true,Paint.Align.CENTER);}
    private void drawBalance(Canvas c0,RectF r){float y=r.centerY(),l=r.left+r.width()*.12f,rr=r.right-r.width()*.12f;p.setColor(SURFACE_HIGH);scratchRect.set(l,y-dp(5),rr,y+dp(5));c0.drawRoundRect(scratchRect,dp(5),dp(5),p);p.setColor(GOOD);c0.drawRect((l+rr)/2-r.width()*.08f,y-dp(5),(l+rr)/2+r.width()*.08f,y+dp(5),p);p.setColor(PRIMARY);c0.drawCircle(l+a*(rr-l),y,s()*.032f,p);}
    private void drawCompass(Canvas c0,RectF r){float rad=Math.min(r.width(),r.height())*.30f,cx=r.centerX(),cy=r.centerY();p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(4));p.setColor(SURFACE_HIGH);c0.drawCircle(cx,cy,rad,p);double ta=Math.toRadians(target-90),aa=Math.toRadians(a-90);p.setColor(GOOD);c0.drawLine(cx,cy,cx+(float)Math.cos(ta)*rad,cy+(float)Math.sin(ta)*rad,p);p.setColor(PRIMARY);c0.drawLine(cx,cy,cx+(float)Math.cos(aa)*rad*.9f,cy+(float)Math.sin(aa)*rad*.9f,p);p.setStyle(Paint.Style.FILL);}
    private void drawPattern(Canvas c0,RectF r){for(int i=0;i<9;i++){RectF q=cell(r,i,3);boolean on=false;if(showing){for(int j=0;j<4;j++)if(values[j]==i)on=true;}else{for(int j=0;j<index;j++)if(values[j]==i)on=true;}p.setColor(on?SECONDARY:SURFACE_HIGH);c0.drawRoundRect(q,dp(10),dp(10),p);}}
    private void drawSum(Canvas c0,RectF r){for(int i=0;i<6;i++){float x=r.left+r.width()*(.22f+.28f*(i%3)),y=r.top+r.height()*(.37f+.32f*(i/3));p.setColor(i==first?PRIMARY:SURFACE_HIGH);c0.drawCircle(x,y,s()*.060f,p);text(c0,String.valueOf(values[i]),x,y+s()*.013f,s()*.035f,TEXT,true,Paint.Align.CENTER);}}
    private void drawHold(Canvas c0,RectF r){float val=holding?((now()-holdStart)%1800)/1800f:0;float y=r.centerY(),left=r.left+r.width()*.12f,right=r.right-r.width()*.12f;scratchRect.set(left,y-dp(8),right,y+dp(8));RectF bar=scratchRect;p.setColor(SURFACE_HIGH);c0.drawRoundRect(bar,dp(8),dp(8),p);float tc=left+target/100f*(right-left);p.setColor(GOOD);c0.drawRect(tc-(right-left)*.07f,bar.top,tc+(right-left)*.07f,bar.bottom,p);p.setColor(PRIMARY);c0.drawCircle(left+val*(right-left),y,dp(10),p);text(c0,holding?"松手！":"按住蓄力",r.centerX(),r.top+r.height()*.28f,s()*.036f,TEXT,true,Paint.Align.CENTER);}

    private int gridHit(RectF r,float x,float y,int n){if(!r.contains(x,y))return-1;int col=(int)((x-r.left)/(r.width()/n)),row=(int)((y-r.top)/(r.height()/n));return Math.max(0,Math.min(n*n-1,row*n+col));}
    @Override protected void onGameTap(float x,float y){if(over)return;RectF r=board();switch(gameMode()){
        case MEMORY_SEQUENCE:if(showing)return;int g=gridHit(r,x,y,3);if(g==values[index+1]){index++;haptic(HapticFeedbackConstants.CLOCK_TICK);if(index>=values[0]){round++;score=round;if(round>=5){over=true;finishRound(1,"记住了！","完成 5 轮",5);}else newRound();}}else{over=true;finishRound(-1,"顺序错了","完成 "+round+" 轮",round);}break;
        case COLOR_RUSH:{int hit=colorHit(r,x,y);if(hit==target){score++;target=random.nextInt(4);haptic(HapticFeedbackConstants.CLOCK_TICK);}else score=Math.max(0,score-1);break;}
        case AIM_TRAINER:{float tx=r.left+a*r.width(),ty=r.top+b*r.height();if(Math.hypot(x-tx,y-ty)<s()*.07f){score++;haptic(HapticFeedbackConstants.CLOCK_TICK);}break;}
        case MINI_GOLF:if(Math.abs(vx)+Math.abs(vy)<.02f){float bx=r.left+a*r.width(),by=r.top+b*r.height(),dx=(x-bx)/r.width(),dy=(y-by)/r.height(),len=(float)Math.hypot(dx,dy);if(len>.01){round++;vx=dx/len*.62f;vy=dy/len*.62f;lastTick=now();}}break;
        case MINI_BASKETBALL:{float err=Math.abs(a-c);err=Math.min(err,1-err);round++;if(err<.09f){score++;haptic(HapticFeedbackConstants.CONFIRM);}c=.14f+random.nextFloat()*.72f;if(round>=10){over=true;finishRound(score>=6?1:0,"投篮结束","命中 "+score+" / 10",score);}break;}
        case REACTION_GRID:{int q=gridHit(r,x,y,3);if(q==target){score++;target=random.nextInt(9);haptic(HapticFeedbackConstants.CLOCK_TICK);}break;}
        case DOT_CONNECT:{int hit=nearestDot(r,x,y);if(hit==index){index++;haptic(HapticFeedbackConstants.CLOCK_TICK);if(index>=6){score++;if(score>=3){over=true;finishRound(1,"连点完成！","连续 3 组",3);}else newRound();}}else if(hit>=0){index=0;haptic(HapticFeedbackConstants.REJECT);}break;}
        case TAP_TEMPO:tempoTap();break;
        case BALANCE_BAR:balanceTap(x<r.centerX()?-1:1);break;
        case COMPASS_MATCH:compassTap();break;
        case PATTERN_LOCK:if(showing)return;int pg=gridHit(r,x,y,3);if(pg==values[index]){index++;if(index>=4){round++;if(round>=5){over=true;finishRound(1,"图案记住了","完成 5 轮",5);}else{for(int i=0;i<4;i++){int v;do v=random.nextInt(9);while(contains(values,i,v));values[i]=v;}showing=true;phaseStart=now();index=0;}}}else{over=true;finishRound(-1,"图案不对","完成 "+round+" 轮",round);}break;
        case TARGET_SUM:sumTap(r,x,y);break;
    }invalidate();}

    private int colorHit(RectF r,float x,float y){for(int i=0;i<4;i++){float cx=r.left+r.width()*(.20f+.20f*i),cy=r.top+r.height()*.67f;if(Math.hypot(x-cx,y-cy)<s()*.075f)return i;}return-1;}
    private int nearestDot(RectF r,float x,float y){for(int i=0;i<6;i++){float cx=r.left+xs[i]*r.width(),cy=r.top+ys[i]*r.height();if(Math.hypot(x-cx,y-cy)<s()*.065f)return i;}return-1;}
    private void tempoTap(){long t=now();if(index==0){phaseStart=t;index=1;return;}long expected=phaseStart+index*650L;int error=(int)Math.min(650,Math.abs(t-expected));score+=Math.max(0,650-error);index++;if(index>=10){int avg=score/9;over=true;finishRound(avg>500?1:0,"节拍完成","稳定度 "+avg,avg);}}
    private void balanceTap(int dir){vx+=dir*.08f;haptic(HapticFeedbackConstants.CLOCK_TICK);}
    private void compassTap(){float diff=Math.abs(a-target);diff=Math.min(diff,360-diff);if(diff<18){score++;haptic(HapticFeedbackConstants.CONFIRM);}round++;if(round>=8){over=true;finishRound(score>=5?1:0,"校准完成","命中 "+score+" / 8",score);}else target=random.nextInt(360);}
    private void makeSum(){first=-1;int x=1+random.nextInt(9),y=1+random.nextInt(9);target=x+y;values[0]=x;values[1]=y;for(int i=2;i<6;i++)values[i]=1+random.nextInt(9);for(int i=5;i>0;i--){int j=random.nextInt(i+1),tmp=values[i];values[i]=values[j];values[j]=tmp;}}
    private void sumTap(RectF r,float x,float y){int hit=-1;for(int i=0;i<6;i++){float cx=r.left+r.width()*(.22f+.28f*(i%3)),cy=r.top+r.height()*(.37f+.32f*(i/3));if(Math.hypot(x-cx,y-cy)<s()*.075f){hit=i;break;}}if(hit<0)return;if(first<0){first=hit;return;}if(hit!=first&&values[hit]+values[first]==target){score++;round++;haptic(HapticFeedbackConstants.CONFIRM);if(round>=10){over=true;finishRound(1,"心算完成","答对 "+score+" / 10",score);}else makeSum();}else{first=-1;}}
    private void updateHockey(float dt){if(dt<=0)return;float ai=.16f;float aiX=clamp(a,.12f,.88f);a+=vx*dt;b+=vy*dt;if(a<.04f||a>.96f){vx=-vx;a=clamp(a,.04f,.96f);}float pr=.09f;if(vy>0&&dist(a,b,c,d)<pr){float dx=a-c,dy=b-d,len=Math.max(.01f,(float)Math.hypot(dx,dy));vx=dx/len*.55f;vy=dy/len*.55f;}if(vy<0&&Math.hypot(a-aiX,b-ai)<pr){float dx=a-aiX,dy=b-ai,len=Math.max(.01f,(float)Math.hypot(dx,dy));vx=dx/len*.52f;vy=Math.abs(dy/len*.52f);}if(b<-.03f){score++;resetPuck(false);}else if(b>1.03f){round++;resetPuck(true);}if(score>=5||round>=5){over=true;finishRound(score>round?1:-1,score>round?"你赢了！":"AI 获胜",score+" : "+round,score);}}
    private void resetPuck(boolean down){a=.5f;b=.5f;vx=(random.nextBoolean()?1:-1)*.32f;vy=(down?1:-1)*.48f;}
    @Override protected void onGameTouchDown(float x,float y){if(gameMode()==AIR_HOCKEY)movePaddle(x,y);else if(gameMode()==HOLD_RELEASE){holding=true;holdStart=now();}}
    @Override protected void onGameTouchMove(float x,float y){if(gameMode()==AIR_HOCKEY)movePaddle(x,y);}
    @Override protected void onGameTouchUp(float x,float y){if(gameMode()==HOLD_RELEASE&&holding){float val=((now()-holdStart)%1800)/1800f*100f;holding=false;float err=Math.abs(val-target);round++;if(err<=8){score++;haptic(HapticFeedbackConstants.CONFIRM);}if(round>=8){over=true;finishRound(score>=5?1:0,"蓄力结束","命中 "+score+" / 8",score);}else target=25+random.nextInt(51);invalidate();}}
    private void movePaddle(float x,float y){RectF r=board();c=clamp((x-r.left)/r.width(),.08f,.92f);d=clamp((y-r.top)/r.height(),.58f,.92f);}
}
