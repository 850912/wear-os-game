package com.example.wearboardgames;

import org.junit.Test;
import static org.junit.Assert.*;

public class EngineRoundTripTest {
    @Test public void game2048RoundTrip() {
        Game2048Engine a = new Game2048Engine(); a.move(3);
        Game2048Engine b = new Game2048Engine(); assertTrue(b.restore(a.serialize()));
        assertEquals(a.serialize(), b.serialize());
    }

    @Test public void xiangqiRoundTrip() {
        XiangqiEngine a = new XiangqiEngine();
        assertNotEquals(XiangqiEngine.MoveResult.ILLEGAL, a.move(0,6,0,5));
        XiangqiEngine b = new XiangqiEngine(); assertTrue(b.restore(a.serialize()));
        assertEquals(a.serialize(), b.serialize());
    }

    @Test public void gomokuRoundTrip() {
        GomokuEngine a = new GomokuEngine(); assertTrue(a.place(7,7)); assertTrue(a.place(7,8));
        GomokuEngine b = new GomokuEngine(); assertTrue(b.restore(a.serialize()));
        assertEquals(a.serialize(), b.serialize());
    }

    @Test public void connect4RoundTrip() {
        Connect4Engine a = new Connect4Engine(); assertTrue(a.drop(3)); assertTrue(a.drop(2));
        Connect4Engine b = new Connect4Engine(); assertTrue(b.restore(a.serialize()));
        assertEquals(a.serialize(), b.serialize());
    }

    @Test public void reversiRoundTrip() {
        ReversiEngine a = new ReversiEngine();
        int[] move = a.chooseAiMove(); assertNotNull(move); assertTrue(a.move(move[0], move[1]));
        ReversiEngine b = new ReversiEngine(); assertTrue(b.restore(a.serialize()));
        assertEquals(a.serialize(), b.serialize());
    }

    @Test public void sudokuRoundTrip() {
        SudokuEngine a = new SudokuEngine(); a.reset(1);
        outer: for(int y=0;y<4;y++) for(int x=0;x<4;x++) if(!a.fixed(x,y)){a.cycle(x,y);break outer;}
        SudokuEngine b = new SudokuEngine(); assertTrue(b.restore(a.serialize()));
        assertEquals(a.serialize(), b.serialize());
    }

    @Test public void minesRoundTrip() {
        MinesEngine a = new MinesEngine(); assertTrue(a.open(2,2));
        MinesEngine b = new MinesEngine(); assertTrue(b.restore(a.serialize()));
        assertEquals(a.serialize(), b.serialize());
    }

    @Test public void mazeRoundTrip() {
        MazeEngine a = new MazeEngine();
        for(int d=0;d<4;d++) if(a.move(d)) break;
        MazeEngine b = new MazeEngine(); assertTrue(b.restore(a.serialize()));
        assertEquals(a.serialize(), b.serialize());
    }

    @Test public void sokobanRoundTrip() {
        SokobanEngine a = new SokobanEngine(); a.move(2);
        SokobanEngine b = new SokobanEngine(); assertTrue(b.restore(a.serialize()));
        assertEquals(a.serialize(), b.serialize());
    }

    @Test public void invalidPayloadsAreRejected() {
        assertFalse(new Game2048Engine().restore("bad"));
        assertFalse(new XiangqiEngine().restore("bad"));
        assertFalse(new GomokuEngine().restore("bad"));
        assertFalse(new Connect4Engine().restore("bad"));
        assertFalse(new ReversiEngine().restore("bad"));
        assertFalse(new SudokuEngine().restore("bad"));
        assertFalse(new MinesEngine().restore("bad"));
        assertFalse(new MazeEngine().restore("bad"));
        assertFalse(new SokobanEngine().restore("bad"));
    }
}
