package com.example.wearboardgames;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** chesslib owns legality, check, castling, en passant, promotion and draw rules. */
public final class ChessEngine {
    private Board board = new Board();
    private String initialFen = board.getFen();
    private final List<String> history = new ArrayList<>();
    private List<Move> legal = board.legalMoves();

    public void reset() {
        board = new Board(); initialFen = board.getFen(); history.clear(); refresh();
    }
    public Piece piece(int x, int y) { return board.getPiece(square(x, y)); }
    public boolean whiteTurn() { return board.getSideToMove() == Side.WHITE; }
    public List<Move> legalMoves() { return new ArrayList<>(legal); }
    public boolean mate() { return board.isMated(); }
    public boolean draw() { return board.isDraw(); }
    public boolean check() { return board.isKingAttacked(); }
    public boolean belongsToTurn(int x, int y) { return piece(x,y).getPieceSide() == board.getSideToMove(); }
    public static Square square(int x, int y) { return Square.squareAt((7-y)*8+x); }
    public static int x(Square square) { return square.ordinal()%8; }
    public static int y(Square square) { return 7-square.ordinal()/8; }

    public List<Move> candidates(int sx, int sy, int tx, int ty) {
        List<Move> result = new ArrayList<>();
        for (Move m : legal) if (m.getFrom() == square(sx,sy) && m.getTo() == square(tx,ty)) result.add(m);
        return result;
    }
    public boolean move(Move move) {
        if (mate() || draw() || !legal.contains(move) || !board.doMove(move, true)) return false;
        history.add(move.toString()); refresh(); return true;
    }
    private void refresh() { legal = board.legalMoves(); }
    public String serialize() { return initialFen + "|" + String.join(",", history); }
    public boolean restore(String payload) {
        if (payload == null) return false;
        try {
            String[] parts = payload.split("\\|", -1);
            if (parts.length != 2) return false;
            Board parsed = new Board(); parsed.loadFromFen(parts[0]);
            if (parsed.getPieceLocation(Piece.WHITE_KING).size()!=1 || parsed.getPieceLocation(Piece.BLACK_KING).size()!=1) return false;
            List<String> moves = new ArrayList<>();
            if (!parts[1].isEmpty()) for (String token : parts[1].split(",")) {
                Move m = new Move(token, parsed.getSideToMove());
                if (!parsed.legalMoves().contains(m) || !parsed.doMove(m,true)) return false;
                moves.add(token);
            }
            List<Move> parsedLegal = parsed.legalMoves();
            board=parsed; initialFen=parts[0]; history.clear(); history.addAll(moves); legal=parsedLegal;
            return true;
        } catch (RuntimeException error) { return false; }
    }

    /** Two-ply material search on a private snapshot, with a bounded watch CPU budget. */
    public Move chooseAiMove() {
        List<Move> moves = legalMoves();
        moves.sort(Comparator.comparingInt((Move m) -> value(board.getPiece(m.getTo()))).reversed());
        if (moves.isEmpty()) return null;
        Move best = moves.get(0);
        int bestScore = Integer.MIN_VALUE;
        boolean white = whiteTurn();
        long until = System.nanoTime()+250_000_000L;
        for (Move m : moves) {
            if (Thread.currentThread().isInterrupted()) break;
            board.doMove(m);
            int score;
            if (board.isMated()) score=100000;
            else {
                score=Integer.MAX_VALUE;
                List<Move> replies=board.legalMoves();
                if (replies.isEmpty()) score=0;
                for (Move reply : replies) {
                    board.doMove(reply);
                    int evaluated = evaluate(white);
                    board.undoMove();
                    score=Math.min(score,evaluated);
                }
            }
            board.undoMove();
            if (score>bestScore) { bestScore=score; best=m; }
            if (System.nanoTime()>=until) break;
        }
        return best;
    }
    private int evaluate(boolean white) {
        int score=0;
        for (int i=0;i<64;i++) {
            Piece p=board.getPiece(Square.squareAt(i));
            if (p==Piece.NONE) continue;
            score += (p.getPieceSide()==(white?Side.WHITE:Side.BLACK)?1:-1)*value(p);
        }
        return score;
    }
    private static int value(Piece piece) {
        if (piece==Piece.NONE) return 0;
        switch (piece.getPieceType()) {
            case PAWN: return 100; case KNIGHT: return 320; case BISHOP: return 330;
            case ROOK: return 500; case QUEEN: return 900; case KING: return 20000; default: return 0;
        }
    }
}
