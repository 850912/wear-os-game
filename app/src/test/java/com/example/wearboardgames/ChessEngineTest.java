package com.example.wearboardgames;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChessEngineTest {
    private void play(ChessEngine e,String uci) { assertTrue(uci,e.move(new Move(uci,e.whiteTurn()?Side.WHITE:Side.BLACK))); }
    @Test public void startingPositionAndFoolsMate() {
        ChessEngine e=new ChessEngine();assertEquals(20,e.legalMoves().size());
        play(e,"f2f3");play(e,"e7e5");play(e,"g2g4");play(e,"d8h4");
        assertTrue(e.mate());assertFalse(e.draw());
        ChessEngine copy=new ChessEngine();assertTrue(copy.restore(e.serialize()));assertTrue(copy.mate());
    }
    @Test public void castlingEnPassantPromotionAndStalemate() {
        ChessEngine e=new ChessEngine();
        assertTrue(e.restore("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1|"));
        play(e,"e1g1");assertEquals(Piece.WHITE_ROOK,e.piece(5,7));assertEquals(Piece.WHITE_KING,e.piece(6,7));
        e.reset();play(e,"e2e4");play(e,"a7a6");play(e,"e4e5");play(e,"d7d5");play(e,"e5d6");
        assertEquals(Piece.NONE,e.piece(3,3));assertEquals(Piece.WHITE_PAWN,e.piece(3,2));
        assertTrue(e.restore("7k/P7/8/8/8/8/8/7K w - - 0 1|"));
        assertEquals(4,e.candidates(0,1,0,0).size());play(e,"a7a8n");assertEquals(Piece.WHITE_KNIGHT,e.piece(0,0));
        assertTrue(e.restore("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1|"));assertTrue(e.draw());assertFalse(e.mate());
    }
    @Test public void repetitionSurvivesSaveAndAiPreservesState() {
        ChessEngine e=new ChessEngine();
        for(int i=0;i<2;i++){play(e,"g1f3");play(e,"g8f6");play(e,"f3g1");play(e,"f6g8");}
        assertTrue(e.draw());ChessEngine copy=new ChessEngine();assertTrue(copy.restore(e.serialize()));assertTrue(copy.draw());
        e.reset();play(e,"e2e4");String before=e.serialize();Move ai=e.chooseAiMove();
        assertNotNull(ai);assertEquals(before,e.serialize());assertTrue(e.legalMoves().contains(ai));
    }
    @Test public void malformedRestoreIsAtomic() {
        ChessEngine e=new ChessEngine();String before=e.serialize();assertFalse(e.restore("bad"));assertEquals(before,e.serialize());
    }
}
