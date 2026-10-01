package com.example.wearboardgames;

public final class XiangqiEngineTest {
    private static void ok(boolean v, String m) { if (!v) throw new AssertionError(m); }
    private static void eq(char a, char b, String m) { if (a != b) throw new AssertionError(m + ": " + a + " != " + b); }

    public static void main(String[] args) {
        XiangqiEngine e = new XiangqiEngine();
        ok(e.move(0, 6, 0, 5) != XiangqiEngine.MoveResult.ILLEGAL, "red opening pawn");
        ok(e.move(0, 3, 0, 4) != XiangqiEngine.MoveResult.ILLEGAL, "black opening pawn");

        String save = e.serialize();
        XiangqiEngine restored = new XiangqiEngine();
        ok(restored.restore(save), "restore current save");
        eq(restored.pieceAt(0, 5), 'P', "red pawn restored");
        eq(restored.pieceAt(0, 4), 'p', "black pawn restored");
        ok(restored.undo(), "undo black after restore");
        eq(restored.pieceAt(0, 3), 'p', "black pawn undo destination");
        ok(restored.undo(), "undo red after restore");
        eq(restored.pieceAt(0, 6), 'P', "red pawn undo destination");

        String legacy = save.substring(0, save.indexOf('|', 2));
        ok(new XiangqiEngine().restore(legacy), "legacy save remains readable");
        ok(!new XiangqiEngine().restore("2|" + legacy.substring(2)), "reject invalid turn");
        System.out.println("XiangqiEngine tests passed");
    }
}
