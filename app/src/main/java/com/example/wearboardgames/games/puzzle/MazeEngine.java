package com.example.wearboardgames;

import java.util.ArrayDeque;
import java.util.Random;

/** Pure perfect-maze generator. Wall bits: 1 up, 2 right, 4 down, 8 left. */
public final class MazeEngine {
    public static final int SIZE=9;private final int[]walls=new int[81];private int player,moves;private final Random random=new Random();
    public MazeEngine(){reset();}public void reset(){for(int i=0;i<81;i++)walls[i]=15;boolean[]seen=new boolean[81];ArrayDeque<Integer>st=new ArrayDeque<>();int cur=0,count=1;seen[0]=true;while(count<81){int r=cur/9,c=cur%9;int[]cand=new int[4];int n=0;if(r>0&&!seen[cur-9])cand[n++]=0;if(c<8&&!seen[cur+1])cand[n++]=1;if(r<8&&!seen[cur+9])cand[n++]=2;if(c>0&&!seen[cur-1])cand[n++]=3;if(n>0){int dir=cand[random.nextInt(n)],next=dir==0?cur-9:dir==1?cur+1:dir==2?cur+9:cur-1;st.push(cur);walls[cur]&=~(1<<dir);int opp=(dir+2)%4;walls[next]&=~(1<<opp);cur=next;seen[cur]=true;count++;}else cur=st.pop();}player=0;moves=0;}
    public int walls(int idx){return walls[idx];}public int player(){return player;}public int moves(){return moves;}public boolean won(){return player==80;}
    public boolean move(int dir){if(dir<0||dir>3||(walls[player]&(1<<dir))!=0)return false;player+=dir==0?-9:dir==1?1:dir==2?9:-1;moves++;return true;}
    public String serialize(){StringBuilder s=new StringBuilder();s.append(player).append('|').append(moves).append('|');for(int i=0;i<81;i++){if(i>0)s.append(',');s.append(walls[i]);}return s.toString();}
    public boolean restore(String s){if(s==null)return false;String[]p=s.split("\\|",3);if(p.length!=3)return false;String[]w=p[2].split(",");if(w.length!=81)return false;try{int pl=Integer.parseInt(p[0]),m=Integer.parseInt(p[1]);if(pl<0||pl>=81||m<0)return false;for(int i=0;i<81;i++){int v=Integer.parseInt(w[i]);if(v<0||v>15)return false;walls[i]=v;}player=pl;moves=m;return true;}catch(RuntimeException e){return false;}}
}
