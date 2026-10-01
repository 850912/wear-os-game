package com.example.wearboardgames;

import org.junit.Test;
import static org.junit.Assert.*;

public class BoardRulesTest {
    @Test public void gomokuDetectsFive() {
        GomokuEngine e=new GomokuEngine();
        for(int x=0;x<4;x++){ assertTrue(e.place(x,0)); assertTrue(e.place(x,1)); }
        assertTrue(e.place(4,0)); assertEquals(1,e.winner());
    }

    @Test public void connect4DetectsVerticalWin() {
        Connect4Engine e=new Connect4Engine();
        for(int i=0;i<3;i++){assertTrue(e.drop(0));assertTrue(e.drop(1));}
        assertTrue(e.drop(0)); assertEquals(1,e.winner());
    }

    @Test public void sudokuRejectsEditingFixedCell() {
        SudokuEngine e=new SudokuEngine(); e.reset(0);
        assertTrue(e.fixed(0,0)); assertFalse(e.set(0,0,2)); assertEquals(1,e.at(0,0));
    }

    @Test public void minesFirstTapNeverLoses() {
        for(int i=0;i<40;i++){MinesEngine e=new MinesEngine();assertTrue(e.open(2,2));assertFalse(e.isMine(2,2));}
    }

    @Test public void sokobanKeepsV7LevelGeometry() {
        SokobanEngine e=new SokobanEngine(); assertEquals(6,SokobanEngine.W); assertEquals(6,SokobanEngine.H); assertEquals(4,SokobanEngine.levelCount());
    }
}
