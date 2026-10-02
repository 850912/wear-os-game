package com.example.wearboardgames;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** Locks v7.4 public game ids so existing save/stat/favorite keys never drift. */
public class GameModesCompatibilityTest {
    @Test public void v74CoreIdsRemainStable() {
        int[][] expected = {
                {GameModes.XIANGQI,1},{GameModes.GOMOKU,2},{GameModes.GAME_2048,3},{GameModes.TAP_RUSH,4},
                {GameModes.SNAKE,5},{GameModes.SIMON,6},{GameModes.TICTACTOE,7},{GameModes.COLOR_HUNT,8},
                {GameModes.BOUNCE,9},{GameModes.SLIDE_PUZZLE,10},{GameModes.LIGHTS_OUT,11},{GameModes.MINI_MINES,12},
                {GameModes.ROCK_PAPER_SCISSORS,13},{GameModes.CONNECT4,17},{GameModes.REVERSI,18},{GameModes.MEMORY_MATCH,19},
                {GameModes.MAZE,20},{GameModes.NUMBER_TAP,21},{GameModes.QUICK_MATH,22},{GameModes.NIM,23},
                {GameModes.PONG,24},{GameModes.BRICK_BREAKER,25},{GameModes.DICE_DUEL,26},{GameModes.SUDOKU,27},
                {GameModes.BLOCK_DROP,29},{GameModes.FLAPPY,30},{GameModes.WHACK_MOLE,31},{GameModes.BLACKJACK,32},
                {GameModes.SOKOBAN,33},{GameModes.RUNNER,34},{GameModes.DODGER,35},{GameModes.STACK_TOWER,36},
                {GameModes.QUICK_DRAW,37},{GameModes.HIGH_LOW,38},{GameModes.ORBIT_TAP,39},{GameModes.RING_TIMING,40},
        };
        for (int[] pair : expected) assertEquals(pair[1], pair[0]);
    }

    @Test public void newChessIdDoesNotReuseRemovedGameIds() {
        assertEquals(1001, GameModes.CHESS);
    }
}
