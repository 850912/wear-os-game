package com.example.wearboardgames;

import static org.junit.Assert.*;

import java.util.Random;
import org.junit.Test;

public class Game2048EngineTest {
    private static final class FixedRandom extends Random {
        private static final long serialVersionUID = 1L;
        @Override public int nextInt(int bound) { return 0; }
        @Override public float nextFloat() { return 0f; }
    }

    @Test public void invalidRestoreIsAtomic() {
        Game2048Engine engine = new Game2048Engine(new FixedRandom());
        String before = engine.serialize();
        assertFalse(engine.restore("0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"));
        assertEquals(before, engine.serialize());
    }

    @Test public void mergeUpdatesScoreAndBoard() {
        Game2048Engine engine = new Game2048Engine(new FixedRandom());
        assertTrue(engine.restore("0,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0"));
        Game2048Engine.MoveFrame frame = engine.move(3);
        assertTrue(frame.changed);
        assertEquals(4, frame.scoreGain);
        assertEquals(4, engine.getScore());
        assertEquals(4, engine.valueAt(0, 0));
    }

    @Test public void restoreRejectsNegativeScoreAndNonPowerOfTwoTile() {
        Game2048Engine engine = new Game2048Engine(new FixedRandom());
        assertFalse(engine.restore("-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"));
        assertFalse(engine.restore("0,6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"));
    }
}
