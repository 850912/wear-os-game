package com.example.wearboardgames;

/** Pure 4x4 Sudoku state with deterministic unique puzzles. */
public final class SudokuEngine {
    public static final int SIZE=4;private final int[]cells=new int[16];private final boolean[]fixed=new boolean[16];
    private static final int[][]PUZZLES={{1,0,0,4,0,4,1,0,0,1,4,0,4,0,0,1},{0,2,0,4,3,0,1,0,0,1,0,3,4,0,2,0},{1,0,3,0,0,4,0,2,2,0,4,0,0,3,0,1}};
    public SudokuEngine(){reset(0);}public void reset(int variant){int[]p=PUZZLES[Math.floorMod(variant,PUZZLES.length)];for(int i=0;i<16;i++){cells[i]=p[i];fixed[i]=p[i]!=0;}}
    public int at(int x,int y){return cells[y*4+x];}public boolean fixed(int x,int y){return fixed[y*4+x];}
    public boolean set(int x,int y,int value){int i=y*4+x;if(fixed[i]||value<0||value>4)return false;cells[i]=value;return true;}public int cycle(int x,int y){int i=y*4+x;if(fixed[i])return cells[i];cells[i]=(cells[i]+1)%5;return cells[i];}
    public boolean conflict(int x,int y){int v=at(x,y);if(v==0)return false;for(int i=0;i<4;i++){if(i!=x&&at(i,y)==v)return true;if(i!=y&&at(x,i)==v)return true;}int bx=(x/2)*2,by=(y/2)*2;for(int yy=by;yy<by+2;yy++)for(int xx=bx;xx<bx+2;xx++)if((xx!=x||yy!=y)&&at(xx,yy)==v)return true;return false;}
    public boolean solved(){for(int y=0;y<4;y++)for(int x=0;x<4;x++)if(at(x,y)==0||conflict(x,y))return false;return true;}
    public String serialize(){StringBuilder s=new StringBuilder();for(int v:cells)s.append((char)('0'+v));s.append('|');for(boolean f:fixed)s.append(f?'1':'0');return s.toString();}
    public boolean restore(String s){if(s==null)return false;String[]p=s.split("\\|",2);if(p.length!=2||p[0].length()!=16||p[1].length()!=16)return false;for(int i=0;i<16;i++){int v=p[0].charAt(i)-'0';char f=p[1].charAt(i);if(v<0||v>4||(f!='0'&&f!='1'))return false;cells[i]=v;fixed[i]=f=='1';}return true;}
}
