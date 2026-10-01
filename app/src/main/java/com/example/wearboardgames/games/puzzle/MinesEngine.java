package com.example.wearboardgames;

import java.util.ArrayDeque;
import java.util.Random;

/** Pure 5x5 mines engine with first-tap safety. */
public final class MinesEngine {
    public static final int SIZE=5,MINES=5;private final boolean[]mine=new boolean[25],open=new boolean[25];private boolean first=true,over,won;private int opened;private final Random random=new Random();
    public MinesEngine(){reset();}public void reset(){for(int i=0;i<25;i++){mine[i]=open[i]=false;}for(int n=0;n<MINES;){int i=random.nextInt(25);if(!mine[i]){mine[i]=true;n++;}}first=true;over=won=false;opened=0;}
    public boolean isOpen(int x,int y){return open[y*5+x];}public boolean isMine(int x,int y){return mine[y*5+x];}public boolean over(){return over;}public boolean won(){return won;}public int opened(){return opened;}
    public int neighbors(int x,int y){int n=0;for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++){int xx=x+dx,yy=y+dy;if(xx>=0&&xx<5&&yy>=0&&yy<5&&mine[yy*5+xx])n++;}return n;}
    public boolean open(int x,int y){if(over||x<0||x>=5||y<0||y>=5)return false;int idx=y*5+x;if(open[idx])return false;if(first){first=false;if(mine[idx])for(int i=0;i<25;i++)if(i!=idx&&!mine[i]){mine[i]=true;mine[idx]=false;break;}}if(mine[idx]){open[idx]=true;over=true;for(int i=0;i<25;i++)if(mine[i])open[i]=true;return true;}flood(idx);if(opened>=20){won=over=true;}return true;}
    private void flood(int start){ArrayDeque<Integer>q=new ArrayDeque<>();q.add(start);while(!q.isEmpty()){int i=q.removeFirst();if(open[i]||mine[i])continue;open[i]=true;opened++;int x=i%5,y=i/5;if(neighbors(x,y)==0)for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++){int xx=x+dx,yy=y+dy;if(xx>=0&&xx<5&&yy>=0&&yy<5&&!open[yy*5+xx])q.add(yy*5+xx);}}}
    public String serialize(){StringBuilder s=new StringBuilder();s.append(first?'1':'0').append(over?'1':'0').append(won?'1':'0').append('|');for(boolean v:mine)s.append(v?'1':'0');s.append('|');for(boolean v:open)s.append(v?'1':'0');return s.toString();}
    public boolean restore(String s){if(s==null)return false;String[]p=s.split("\\|",3);if(p.length!=3||p[0].length()!=3||p[1].length()!=25||p[2].length()!=25)return false;for(int i=0;i<3;i++)if(p[0].charAt(i)!='0'&&p[0].charAt(i)!='1')return false;for(int i=0;i<25;i++)if((p[1].charAt(i)!='0'&&p[1].charAt(i)!='1')||(p[2].charAt(i)!='0'&&p[2].charAt(i)!='1'))return false;first=p[0].charAt(0)=='1';over=p[0].charAt(1)=='1';won=p[0].charAt(2)=='1';opened=0;for(int i=0;i<25;i++){mine[i]=p[1].charAt(i)=='1';open[i]=p[2].charAt(i)=='1';if(open[i]&&!mine[i])opened++;}return true;}
}
