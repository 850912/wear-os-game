package com.example.wearboardgames;
import org.junit.Test;
import static org.junit.Assert.*;

public class TetrisTimingTest {
    @Test public void speedStaysReadableForLongGames() {
        assertEquals(1000,TetrisTiming.dropDelay(0));
        assertEquals(965,TetrisTiming.dropDelay(10));
        assertEquals(650,TetrisTiming.dropDelay(10000));
        assertTrue(TetrisTiming.LOCK_DELAY>=500);
    }
}
