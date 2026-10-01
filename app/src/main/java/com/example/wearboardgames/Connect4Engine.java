package com.example.wearboardgames;

/** Pure 7x6 Connect Four engine. */
public final class Connect4Engine {
    public static final int COLS=7,ROWS=6; private final int[][]b=new int[ROWS][COLS];private int turn=1,winner;
    public void reset(){for(int y=0;y<ROWS;y++)for(int x=0;x<COLS;x++)b[y][x]=0;turn=1;winner=0;} public Connect4Engine(){reset();}
    public int at(int x,int y){return x>=0&&x<COLS&&y>=0&&y<ROWS?b[y][x]:-1;} public int turn(){return turn;}public int winner(){return winner;}
    public boolean drop(int col){if(winner!=0||col<0||col>=COLS)return false;for(int y=ROWS-1;y>=0;y--)if(b[y][col]==0){b[y][col]=turn;if(win(col,y,turn))winner=turn;else if(full())winner=3;else turn=3-turn;return true;}return false;}
    public int chooseAiMove(){int ai=turn,other=3-ai;for(int c=0;c<COLS;c++)if(simWin(c,ai))return c;for(int c=0;c<COLS;c++)if(simWin(c,other))return c;int[]pref={3,2,4,1,5,0,6};for(int c:pref)if(at(c,0)==0)return c;return -1;}
    private boolean simWin(int col,int who){for(int y=ROWS-1;y>=0;y--)if(b[y][col]==0){b[y][col]=who;boolean r=win(col,y,who);b[y][col]=0;return r;}return false;}
    private boolean win(int x,int y,int who){int[][]ds={{1,0},{0,1},{1,1},{1,-1}};for(int[]d:ds){int n=1;for(int s:new int[]{-1,1}){int xx=x+d[0]*s,yy=y+d[1]*s;while(at(xx,yy)==who){n++;xx+=d[0]*s;yy+=d[1]*s;}}if(n>=4)return true;}return false;}
    private boolean full(){for(int x=0;x<COLS;x++)if(b[0][x]==0)return false;return true;}
    public String serialize(){StringBuilder s=new StringBuilder();s.append(turn).append('|').append(winner).append('|');for(int[]r:b)for(int v:r)s.append((char)('0'+v));return s.toString();}
    public boolean restore(String s){if(s==null)return false;String[]p=s.split("\\|",3);if(p.length!=3||p[2].length()!=ROWS*COLS)return false;try{int t=Integer.parseInt(p[0]),w=Integer.parseInt(p[1]);if(t<1||t>2||w<0||w>3)return false;for(int i=0;i<ROWS*COLS;i++){int v=p[2].charAt(i)-'0';if(v<0||v>2)return false;b[i/COLS][i%COLS]=v;}turn=t;winner=w;return true;}catch(RuntimeException e){return false;}}
}
