package com.example.wearboardgames;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Small, dependency-free Chinese chess rules engine.
 * Upper-case pieces are Red, lower-case pieces are Black.
 * Board coordinates: x=0..8 left to right, y=0..9 black side to red side.
 */
public final class XiangqiEngine {
    public static final int COLS = 9;
    public static final int ROWS = 10;

    private final char[][] board = new char[ROWS][COLS];
    private boolean redTurn = true;
    private final Deque<Move> history = new ArrayDeque<>();

    public XiangqiEngine() {
        reset();
    }

    public void reset() {
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) board[y][x] = 0;
        }
        char[] blackBack = {'r','h','e','a','k','a','e','h','r'};
        char[] redBack   = {'R','H','E','A','K','A','E','H','R'};
        System.arraycopy(blackBack, 0, board[0], 0, COLS);
        board[2][1] = 'c'; board[2][7] = 'c';
        for (int x = 0; x < COLS; x += 2) board[3][x] = 'p';
        for (int x = 0; x < COLS; x += 2) board[6][x] = 'P';
        board[7][1] = 'C'; board[7][7] = 'C';
        System.arraycopy(redBack, 0, board[9], 0, COLS);
        redTurn = true;
        history.clear();
    }

    public char pieceAt(int x, int y) {
        return inside(x, y) ? board[y][x] : 0;
    }

    public boolean isRedTurn() { return redTurn; }

    public boolean isRedPiece(char p) { return p != 0 && Character.isUpperCase(p); }

    public boolean belongsToTurn(int x, int y) {
        char p = pieceAt(x, y);
        return p != 0 && isRedPiece(p) == redTurn;
    }

    public boolean isLegalMove(int sx, int sy, int tx, int ty) {
        if (!inside(sx, sy) || !inside(tx, ty) || (sx == tx && sy == ty)) return false;
        char p = board[sy][sx];
        if (p == 0 || isRedPiece(p) != redTurn) return false;
        char target = board[ty][tx];
        if (target != 0 && isRedPiece(target) == redTurn) return false;
        if (!pieceCanMoveRaw(p, sx, sy, tx, ty)) return false;

        board[sy][sx] = 0;
        board[ty][tx] = p;
        boolean selfCheck = isInCheck(redTurn);
        board[sy][sx] = p;
        board[ty][tx] = target;
        return !selfCheck;
    }

    public MoveResult move(int sx, int sy, int tx, int ty) {
        if (!isLegalMove(sx, sy, tx, ty)) return MoveResult.ILLEGAL;
        char p = board[sy][sx];
        char captured = board[ty][tx];
        history.push(new Move(sx, sy, tx, ty, p, captured, redTurn));
        board[sy][sx] = 0;
        board[ty][tx] = p;

        boolean movedRed = redTurn;
        redTurn = !redTurn;

        // Capturing the general ends immediately.
        if (Character.toUpperCase(captured) == 'K') {
            return movedRed ? MoveResult.RED_WINS : MoveResult.BLACK_WINS;
        }

        boolean opponentRed = redTurn;
        if (!hasKing(opponentRed) || !hasAnyLegalMove(opponentRed)) {
            return movedRed ? MoveResult.RED_WINS : MoveResult.BLACK_WINS;
        }
        if (isInCheck(opponentRed)) return MoveResult.CHECK;
        return MoveResult.OK;
    }

    public boolean undo() {
        Move m = history.pollFirst();
        if (m == null) return false;
        board[m.sy][m.sx] = m.piece;
        board[m.ty][m.tx] = m.captured;
        redTurn = m.redTurnBefore;
        return true;
    }

    public boolean isInCheck(boolean red) {
        int[] king = findKing(red);
        if (king == null) return true;
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                char p = board[y][x];
                if (p == 0 || isRedPiece(p) == red) continue;
                if (pieceCanMoveRaw(p, x, y, king[0], king[1])) return true;
            }
        }
        return false;
    }

    public boolean hasAnyLegalMove(boolean red) {
        boolean oldTurn = redTurn;
        redTurn = red;
        try {
            for (int sy = 0; sy < ROWS; sy++) {
                for (int sx = 0; sx < COLS; sx++) {
                    char p = board[sy][sx];
                    if (p == 0 || isRedPiece(p) != red) continue;
                    for (int ty = 0; ty < ROWS; ty++) {
                        for (int tx = 0; tx < COLS; tx++) {
                            if (isLegalMove(sx, sy, tx, ty)) return true;
                        }
                    }
                }
            }
            return false;
        } finally {
            redTurn = oldTurn;
        }
    }

    private boolean pieceCanMoveRaw(char p, int sx, int sy, int tx, int ty) {
        if (!inside(tx, ty) || (sx == tx && sy == ty)) return false;
        char target = board[ty][tx];
        boolean red = isRedPiece(p);
        if (target != 0 && isRedPiece(target) == red) return false;

        int dx = tx - sx;
        int dy = ty - sy;
        int adx = Math.abs(dx);
        int ady = Math.abs(dy);
        switch (Character.toUpperCase(p)) {
            case 'R': // rook / chariot
                return straightClear(sx, sy, tx, ty, 0);
            case 'C': { // cannon
                if (sx != tx && sy != ty) return false;
                int blockers = countBetween(sx, sy, tx, ty);
                return target == 0 ? blockers == 0 : blockers == 1;
            }
            case 'H': { // horse; block the horse leg
                if (!((adx == 2 && ady == 1) || (adx == 1 && ady == 2))) return false;
                int legX = sx, legY = sy;
                if (adx == 2) legX += dx > 0 ? 1 : -1;
                else legY += dy > 0 ? 1 : -1;
                return board[legY][legX] == 0;
            }
            case 'E': { // elephant; block the elephant eye and do not cross river
                if (adx != 2 || ady != 2) return false;
                int eyeX = sx + dx / 2, eyeY = sy + dy / 2;
                if (board[eyeY][eyeX] != 0) return false;
                return red ? ty >= 5 : ty <= 4;
            }
            case 'A': // advisor, palace diagonal
                return adx == 1 && ady == 1 && inPalace(red, tx, ty);
            case 'K': { // general: palace step, or flying-general capture
                if (sx == tx && Character.toUpperCase(target) == 'K' && countBetween(sx, sy, tx, ty) == 0) {
                    return true;
                }
                return adx + ady == 1 && inPalace(red, tx, ty);
            }
            case 'P': { // soldier
                int forward = red ? -1 : 1;
                if (dx == 0 && dy == forward) return true;
                boolean crossed = red ? sy <= 4 : sy >= 5;
                return crossed && dy == 0 && adx == 1;
            }
            default:
                return false;
        }
    }

    private boolean straightClear(int sx, int sy, int tx, int ty, int requiredBetween) {
        if (sx != tx && sy != ty) return false;
        return countBetween(sx, sy, tx, ty) == requiredBetween;
    }

    private int countBetween(int sx, int sy, int tx, int ty) {
        if (sx != tx && sy != ty) return Integer.MAX_VALUE;
        int stepX = Integer.compare(tx, sx);
        int stepY = Integer.compare(ty, sy);
        int x = sx + stepX, y = sy + stepY, n = 0;
        while (x != tx || y != ty) {
            if (board[y][x] != 0) n++;
            x += stepX; y += stepY;
        }
        return n;
    }

    private boolean inPalace(boolean red, int x, int y) {
        if (x < 3 || x > 5) return false;
        return red ? y >= 7 && y <= 9 : y >= 0 && y <= 2;
    }

    private boolean hasKing(boolean red) { return findKing(red) != null; }

    private int[] findKing(boolean red) {
        char k = red ? 'K' : 'k';
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) if (board[y][x] == k) return new int[]{x, y};
        }
        return null;
    }

    private static boolean inside(int x, int y) {
        return x >= 0 && x < COLS && y >= 0 && y < ROWS;
    }

    public enum MoveResult { ILLEGAL, OK, CHECK, RED_WINS, BLACK_WINS }

    private static final class Move {
        final int sx, sy, tx, ty;
        final char piece, captured;
        final boolean redTurnBefore;
        Move(int sx, int sy, int tx, int ty, char piece, char captured, boolean redTurnBefore) {
            this.sx=sx; this.sy=sy; this.tx=tx; this.ty=ty;
            this.piece=piece; this.captured=captured; this.redTurnBefore=redTurnBefore;
        }
    }
}
