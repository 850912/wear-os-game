package com.example.wearboardgames;

import java.lang.reflect.Field;
import java.util.Random;

public class Game2048EngineTest {
    private static void set(Game2048Engine g, int[][] rows) throws Exception {
        Field f = Game2048Engine.class.getDeclaredField("board");
        f.setAccessible(true);
        int[][] b=(int[][])f.get(g);
        for(int y=0;y<4;y++) for(int x=0;x<4;x++) b[y][x]=rows[y][x];
        Field sc=Game2048Engine.class.getDeclaredField("score"); sc.setAccessible(true); sc.setInt(g,0);
    }
    private static void eq(int a,int b,String m){ if(a!=b)throw new AssertionError(m+": "+a+" != "+b); }
    private static void ok(boolean v,String m){ if(!v)throw new AssertionError(m); }
    public static void main(String[] args) throws Exception {
        Game2048Engine g=new Game2048Engine(new Random(1));
        set(g,new int[][]{{2,2,2,2},{0,0,0,0},{0,0,0,0},{0,0,0,0}});
        Game2048Engine.MoveFrame f=g.move(3);
        ok(f.changed,"left move changed"); eq(g.valueAt(0,0),4,"pair1"); eq(g.valueAt(1,0),4,"pair2"); eq(f.scoreGain,8,"score gain 2+2 pairs");

        set(g,new int[][]{{2,2,4,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}});
        f=g.move(3); eq(g.valueAt(0,0),4,"no chain merge first"); eq(g.valueAt(1,0),4,"no chain merge second"); eq(f.scoreGain,4,"score gain chain rule");

        set(g,new int[][]{{2,0,0,0},{2,0,0,0},{4,0,0,0},{4,0,0,0}});
        f=g.move(0); eq(g.valueAt(0,0),4,"up pair1"); eq(g.valueAt(0,1),8,"up pair2"); eq(f.scoreGain,12,"up score");

        set(g,new int[][]{{2,4,8,16},{32,64,128,256},{512,1024,2,4},{8,16,32,64}});
        ok(!g.canMove(),"full locked board gameover");
        f=g.move(3); ok(!f.changed,"locked board unchanged");

        set(g,new int[][]{{1024,1024,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}});
        f=g.move(3); ok(g.hasReached2048(),"reaches 2048"); eq(g.valueAt(0,0),2048,"2048 value");
        System.out.println("Game2048Engine tests passed");
    }
}
