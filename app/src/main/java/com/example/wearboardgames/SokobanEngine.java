package com.example.wearboardgames;

/** Pure watch-friendly Sokoban engine. Level geometry stays compatible with v7.x saves. */
public final class SokobanEngine {
    public static final int W=6,H=6;
    private static final String[][] LEVELS={
            {"######","# @  #","# $  #","# .  #","#    #","######"},
            {"######","#    #","# $ .#","# @  #","#    #","######"},
            {"######","# .  #","# #$ #","#  @ #","#    #","######"},
            {"######","#  . #","#  $ #","# #@ #","#    #","######"}
    };
    private final char[] base=new char[W*H];
    private final boolean[] boxes=new boolean[W*H];
    private int player,level,moves;

    public SokobanEngine(){load(0);}
    public static int levelCount(){return LEVELS.length;}
    public void load(int l){
        level=Math.floorMod(l,LEVELS.length);moves=0;
        for(int i=0;i<W*H;i++){base[i]=' ';boxes[i]=false;}
        for(int y=0;y<H;y++)for(int x=0;x<W;x++){
            char ch=LEVELS[level][y].charAt(x);int i=y*W+x;
            if(ch=='#'||ch=='.'||ch=='*'||ch=='+')base[i]=(ch=='#')?'#':'.';
            if(ch=='$'||ch=='*')boxes[i]=true;
            if(ch=='@'||ch=='+')player=i;
        }
    }
    public int level(){return level;} public int moves(){return moves;} public int player(){return player;}
    public boolean wall(int i){return i<0||i>=W*H||base[i]=='#';}
    public boolean goal(int i){return i>=0&&i<W*H&&base[i]=='.';}
    public boolean box(int i){return i>=0&&i<W*H&&boxes[i];}
    public boolean move(int dir){
        int dx=dir==1?1:dir==3?-1:0,dy=dir==2?1:dir==0?-1:0;
        int to=player+dy*W+dx;if(wall(to))return false;
        if(boxes[to]){int beyond=to+dy*W+dx;if(wall(beyond)||boxes[beyond])return false;boxes[to]=false;boxes[beyond]=true;}
        player=to;moves++;return true;
    }
    public boolean won(){for(int i=0;i<W*H;i++)if(boxes[i]&&!goal(i))return false;return true;}
    public String serialize(){StringBuilder s=new StringBuilder();s.append(level).append('|').append(moves).append('|').append(player).append('|');for(boolean v:boxes)s.append(v?'1':'0');return s.toString();}
    public boolean restore(String s){
        if(s==null)return false;String[]p=s.split("\\|",4);if(p.length!=4||p[3].length()!=W*H)return false;
        try{int l=Integer.parseInt(p[0]),m=Integer.parseInt(p[1]),pl=Integer.parseInt(p[2]);if(l<0||l>=LEVELS.length||m<0||pl<0||pl>=W*H)return false;load(l);if(wall(pl))return false;moves=m;player=pl;for(int i=0;i<W*H;i++){char ch=p[3].charAt(i);if(ch!='0'&&ch!='1')return false;if(ch=='1'&&wall(i))return false;boxes[i]=ch=='1';}if(boxes[player])return false;return true;}catch(RuntimeException e){return false;}
    }
    public void nextLevel(){load(level+1);}
}
