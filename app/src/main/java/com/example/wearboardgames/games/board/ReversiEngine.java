package com.example.wearboardgames;

import java.util.ArrayList;
import java.util.List;

/** Pure 6x6 Reversi engine. */
public final class ReversiEngine {
    public static final int SIZE=6;private final int[][]b=new int[SIZE][SIZE];private int turn=1,winner;
    public ReversiEngine(){reset();}public void reset(){for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++)b[y][x]=0;b[2][2]=b[3][3]=2;b[2][3]=b[3][2]=1;turn=1;winner=0;}
    public int at(int x,int y){return x>=0&&x<SIZE&&y>=0&&y<SIZE?b[y][x]:-1;}public int turn(){return turn;}public int winner(){return winner;}
    public boolean legal(int x,int y){return x>=0&&x<SIZE&&y>=0&&y<SIZE&&flips(x,y,turn,null)>0;}public boolean move(int x,int y){if(winner!=0||x<0||x>=SIZE||y<0||y>=SIZE||b[y][x]!=0)return false;List<int[]>f=new ArrayList<>();if(flips(x,y,turn,f)==0)return false;b[y][x]=turn;for(int[]q:f)b[q[1]][q[0]]=turn;turn=3-turn;if(!hasMove(turn)){turn=3-turn;if(!hasMove(turn))finish();}return true;}
    public int[] chooseAiMove(){int best=-1,bx=-1,by=-1;for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++){int f=flips(x,y,turn,null);if(f<=0)continue;int bonus=(x==0||x==SIZE-1)&&(y==0||y==SIZE-1)?20:0;if(f+bonus>best){best=f+bonus;bx=x;by=y;}}return bx<0?null:new int[]{bx,by};}
    private int flips(int x,int y,int who,List<int[]>out){if(x<0||x>=SIZE||y<0||y>=SIZE||b[y][x]!=0)return 0;int other=3-who,total=0;for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++){if(dx==0&&dy==0)continue;int xx=x+dx,yy=y+dy,n=0;while(at(xx,yy)==other){n++;xx+=dx;yy+=dy;}if(n>0&&at(xx,yy)==who){total+=n;if(out!=null)for(int i=1;i<=n;i++)out.add(new int[]{x+dx*i,y+dy*i});}}return total;}
    private boolean hasMove(int who){int old=turn;turn=who;try{for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++)if(flips(x,y,who,null)>0)return true;return false;}finally{turn=old;}}
    private void finish(){int a=0,c=0;for(int[]r:b)for(int v:r){if(v==1)a++;else if(v==2)c++;}winner=a==c?3:(a>c?1:2);}
    public int count(int who){int n=0;for(int[]r:b)for(int v:r)if(v==who)n++;return n;}
    public String serialize(){StringBuilder s=new StringBuilder();s.append(turn).append('|').append(winner).append('|');for(int[]r:b)for(int v:r)s.append((char)('0'+v));return s.toString();}
    public boolean restore(String s){if(s==null)return false;String[]p=s.split("\\|",3);if(p.length!=3||p[2].length()!=SIZE*SIZE)return false;try{int t=Integer.parseInt(p[0]),w=Integer.parseInt(p[1]);if(t<1||t>2||w<0||w>3)return false;for(int i=0;i<SIZE*SIZE;i++){int v=p[2].charAt(i)-'0';if(v<0||v>2)return false;b[i/SIZE][i%SIZE]=v;}turn=t;winner=w;return true;}catch(RuntimeException e){return false;}}
}
