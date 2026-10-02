package com.example.wearboardgames;
import org.junit.Test;
import static org.junit.Assert.*;

public class XiangqiAiTest {
    @Test public void aiChoosesLegalBlackMoveWithoutMutatingPosition() {
        XiangqiEngine e=new XiangqiEngine();
        assertNotEquals(XiangqiEngine.MoveResult.ILLEGAL,e.move(0,6,0,5));
        String before=e.serialize();int[] move=e.chooseAiMove();
        assertNotNull(move);assertEquals(before,e.serialize());
        assertTrue(e.isLegalMove(move[0],move[1],move[2],move[3]));
        assertNotEquals(XiangqiEngine.MoveResult.ILLEGAL,e.move(move[0],move[1],move[2],move[3]));
        assertTrue(e.isRedTurn());
    }
}
