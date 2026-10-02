package com.example.wearboardgames;

import org.junit.Test;
import static org.junit.Assert.*;

public class BoardViewportTest {
    @Test public void panningAndInverseReachBothEdges() {
        BoardViewport v=new BoardViewport();
        v.scale(2,200,160);v.pan(1000,1000,200,160);
        assertEquals(100,v.panX(),.001);assertEquals(80,v.panY(),.001);
        assertEquals(0,v.boardX(0,100),.001);
        v.pan(-2000,-2000,200,160);
        assertEquals(200,v.boardX(200,100),.001);
        v.scale(.5f,200,160);
        assertEquals(0,v.panX(),.001);assertEquals(0,v.panY(),.001);
        assertFalse(v.pan(20,20,200,160));
    }
    @Test public void invalidScaleCannotPoisonCamera() {
        BoardViewport v=new BoardViewport();v.scale(Float.NaN,200,200);
        assertEquals(1,v.zoom(),.001);
    }
}
