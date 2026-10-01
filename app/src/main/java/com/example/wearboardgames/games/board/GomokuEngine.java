package com.example.wearboardgames;

import java.util.Random;

/** Pure 15x15 Gomoku rules/AI engine. 1=black, 2=white. */
public final class GomokuEngine {
    public static final int SIZE=15;
    private final int[][] board=new int[SIZE][SIZE];
    private int turn=1,lastX=-1,lastY=-1,winner;
    public void reset(){for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++)board[y][x]=0;turn=1;lastX=lastY=-1;winner=0;}
    public GomokuEngine(){reset();}
    public int at(int x,int y){return x>=0&&x<SIZE&&y>=0&&y<SIZE?board[y][x]:-1;}
    public int turn(){return turn;} public int winner(){return winner;} public int lastX(){return lastX;} public int lastY(){return lastY;}
    public boolean place(int x,int y){if(winner!=0||x<0||x>=SIZE||y<0||y>=SIZE||board[y][x]!=0)return false;board[y][x]=turn;lastX=x;lastY=y;if(hasFive(x,y,turn))winner=turn;else if(full())winner=3;else turn=3-turn;return true;}
    public int[] chooseAiMove(){if(winner!=0)return null;int ai=turn,other=3-ai;for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++)if(board[y][x]==0&&wouldWin(x,y,ai))return new int[]{x,y};for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++)if(board[y][x]==0&&wouldWin(x,y,other))return new int[]{x,y};int best=-1,bx=-1,by=-1;Random r=new Random();for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++)if(board[y][x]==0){int sc=score(x,y,ai)*3+score(x,y,other)*2-r.nextInt(3);if(sc>best){best=sc;bx=x;by=y;}}return bx<0?null:new int[]{bx,by};}
    private int score(int x,int y,int who){int s=0;int[][]d={{1,0},{0,1},{1,1},{1,-1}};for(int[]q:d){int n=1;for(int sign:new int[]{-1,1}){int xx=x+q[0]*sign,yy=y+q[1]*sign;while(at(xx,yy)==who){n++;xx+=q[0]*sign;yy+=q[1]*sign;}}s+=n*n;}int center=SIZE/2;s+=SIZE-(Math.abs(x-center)+Math.abs(y-center));return s;}
    private boolean wouldWin(int x,int y,int who){board[y][x]=who;boolean win=hasFive(x,y,who);board[y][x]=0;return win;}
    private boolean hasFive(int x,int y,int who){int[][]d={{1,0},{0,1},{1,1},{1,-1}};for(int[]q:d){int n=1;for(int sign:new int[]{-1,1}){int xx=x+q[0]*sign,yy=y+q[1]*sign;while(at(xx,yy)==who){n++;xx+=q[0]*sign;yy+=q[1]*sign;}}if(n>=5)return true;}return false;}
    private boolean full(){for(int[]r:board)for(int v:r)if(v==0)return false;return true;}
    public String serialize(){StringBuilder b=new StringBuilder();b.append(turn).append('|').append(winner).append('|').append(lastX).append('|').append(lastY).append('|');for(int[]r:board)for(int v:r)b.append((char)('0'+v));return b.toString();}
    public boolean restore(String s){if(s==null)return false;String[]p=s.split("\\|",5);if(p.length!=5||p[4].length()!=SIZE*SIZE)return false;try{int t=Integer.parseInt(p[0]),w=Integer.parseInt(p[1]),lx=Integer.parseInt(p[2]),ly=Integer.parseInt(p[3]);if(t<1||t>2||w<0||w>3)return false;int[][]tmp=new int[SIZE][SIZE];for(int i=0;i<SIZE*SIZE;i++){int v=p[4].charAt(i)-'0';if(v<0||v>2)return false;tmp[i/SIZE][i%SIZE]=v;}for(int y=0;y<SIZE;y++)System.arraycopy(tmp[y],0,board[y],0,SIZE);turn=t;winner=w;lastX=lx;lastY=ly;return true;}catch(RuntimeException e){return false;}}
}
