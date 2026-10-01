package com.example.wearboardgames;

import static org.junit.Assert.*;

import org.junit.Test;

public class XiangqiEngineTest {
    @Test public void restoredGameKeepsUndoHistory() {
        XiangqiEngine original = new XiangqiEngine();
        assertEquals(XiangqiEngine.MoveResult.OK, original.move(0, 6, 0, 5));
        assertEquals(XiangqiEngine.MoveResult.OK, original.move(0, 3, 0, 4));

        XiangqiEngine restored = new XiangqiEngine();
        assertTrue(restored.restore(original.serialize()));
        assertTrue(restored.undo());
        assertEquals('p', restored.pieceAt(0, 3));
        assertEquals((char) 0, restored.pieceAt(0, 4));
        assertTrue(restored.undo());
        assertEquals('P', restored.pieceAt(0, 6));
        assertEquals((char) 0, restored.pieceAt(0, 5));
        assertTrue(restored.isRedTurn());
    }

    @Test public void malformedStateDoesNotReplaceBoard() {
        XiangqiEngine engine = new XiangqiEngine();
        String before = engine.serialize();
        assertFalse(engine.restore("1|not-a-board"));
        assertEquals(before, engine.serialize());
    }

    @Test public void horseLegBlockingIsEnforced() {
        XiangqiEngine engine = new XiangqiEngine();
        assertFalse(engine.isLegalMove(1, 9, 3, 8)); // own elephant blocks the horse leg
        assertTrue(engine.isLegalMove(1, 9, 2, 7));
    }
}
