package com.example.wearboardgames;

import org.junit.Test;
import static org.junit.Assert.*;

public class TetrisStateCodecTest {
    @Test public void roundTripPreservesFullState(){
        TetrisStateCodec.State a=new TetrisStateCodec.State();
        for(int y=0;y<TetrisStateCodec.H;y++)for(int x=0;x<TetrisStateCodec.W;x++)a.board[y][x]=(x+y)%8;
        a.score=12345;a.lines=37;a.type=6;a.rotation=3;a.x=4;a.y=8;a.nextType=2;a.holdType=5;a.combo=3;a.holdUsed=true;a.paused=true;
        String encoded=TetrisStateCodec.encode(a);TetrisStateCodec.State b=TetrisStateCodec.decode(encoded);
        assertNotNull(b);assertEquals(a.score,b.score);assertEquals(a.lines,b.lines);assertEquals(a.type,b.type);assertEquals(a.rotation,b.rotation);
        assertEquals(a.x,b.x);assertEquals(a.y,b.y);assertEquals(a.nextType,b.nextType);assertEquals(a.holdType,b.holdType);assertEquals(a.combo,b.combo);
        assertEquals(a.holdUsed,b.holdUsed);assertEquals(a.paused,b.paused);
        for(int y=0;y<TetrisStateCodec.H;y++)assertArrayEquals(a.board[y],b.board[y]);
    }
    @Test public void rejectsCorruption(){assertNull(TetrisStateCodec.decode("bad"));assertNull(TetrisStateCodec.decode("4|0|0|0|0|3|-1|0|-1|-1|0|0|x"));}
}
