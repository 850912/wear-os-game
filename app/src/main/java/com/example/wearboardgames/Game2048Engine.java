package com.example.wearboardgames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Native 2048 rules. Direction: 0 up, 1 right, 2 down, 3 left. */
public final class Game2048Engine {
    public static final int SIZE = 4;

    private final int[][] board = new int[SIZE][SIZE];
    private final Random random;
    private int score;

    public Game2048Engine() {
        this(new Random());
    }

    Game2048Engine(Random random) {
        this.random = random;
        reset();
    }

    public void reset() {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) board[y][x] = 0;
        }
        score = 0;
        addRandomTile();
        addRandomTile();
    }

    public int valueAt(int x, int y) {
        return board[y][x];
    }

    public int getScore() {
        return score;
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(score);
        for (int y=0;y<SIZE;y++) for (int x=0;x<SIZE;x++) sb.append(',').append(board[y][x]);
        return sb.toString();
    }

    public boolean restore(String state) {
        if (state == null || state.length() == 0) return false;
        String[] parts = state.split(",");
        if (parts.length != 17) return false;
        try {
            score = Integer.parseInt(parts[0]);
            for (int i=0;i<16;i++) board[i/4][i%4] = Integer.parseInt(parts[i+1]);
            return true;
        } catch (NumberFormatException e) { return false; }
    }

    public MoveFrame move(int direction) {
        if (direction < 0 || direction > 3) return MoveFrame.unchanged();

        int[][] before = copyBoard();
        int[][] next = new int[SIZE][SIZE];
        List<TileMotion> motions = new ArrayList<>();
        boolean[][] merged = new boolean[SIZE][SIZE];
        int scoreGain = 0;

        for (int line = 0; line < SIZE; line++) {
            List<Entry> entries = new ArrayList<>(SIZE);
            for (int pos = 0; pos < SIZE; pos++) {
                int[] xy = map(line, pos, direction);
                int value = before[xy[1]][xy[0]];
                if (value != 0) entries.add(new Entry(xy[0], xy[1], value));
            }

            int outPos = 0;
            for (int i = 0; i < entries.size(); i++) {
                Entry a = entries.get(i);
                int[] target = map(line, outPos, direction);
                if (i + 1 < entries.size() && a.value == entries.get(i + 1).value) {
                    Entry b = entries.get(++i);
                    int result = a.value * 2;
                    next[target[1]][target[0]] = result;
                    merged[target[1]][target[0]] = true;
                    motions.add(new TileMotion(a.value, a.x, a.y, target[0], target[1]));
                    motions.add(new TileMotion(b.value, b.x, b.y, target[0], target[1]));
                    scoreGain += result;
                } else {
                    next[target[1]][target[0]] = a.value;
                    motions.add(new TileMotion(a.value, a.x, a.y, target[0], target[1]));
                }
                outPos++;
            }
        }

        boolean changed = !same(before, next);
        if (!changed) return MoveFrame.unchanged();

        for (int y = 0; y < SIZE; y++) {
            System.arraycopy(next[y], 0, board[y], 0, SIZE);
        }
        score += scoreGain;
        Spawn spawn = addRandomTile();
        return new MoveFrame(true, scoreGain, motions, merged, spawn, !canMove());
    }

    public boolean canMove() {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int v = board[y][x];
                if (v == 0) return true;
                if (x + 1 < SIZE && board[y][x + 1] == v) return true;
                if (y + 1 < SIZE && board[y + 1][x] == v) return true;
            }
        }
        return false;
    }

    public boolean hasReached2048() {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) if (board[y][x] >= 2048) return true;
        }
        return false;
    }

    int[][] copyBoard() {
        int[][] out = new int[SIZE][SIZE];
        for (int y = 0; y < SIZE; y++) System.arraycopy(board[y], 0, out[y], 0, SIZE);
        return out;
    }

    private Spawn addRandomTile() {
        List<int[]> empty = new ArrayList<>();
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) if (board[y][x] == 0) empty.add(new int[]{x, y});
        }
        if (empty.isEmpty()) return null;
        int[] spot = empty.get(random.nextInt(empty.size()));
        int value = random.nextFloat() < 0.9f ? 2 : 4;
        board[spot[1]][spot[0]] = value;
        return new Spawn(spot[0], spot[1], value);
    }

    private int[] map(int line, int pos, int direction) {
        switch (direction) {
            case 0: return new int[]{line, pos};          // up
            case 1: return new int[]{SIZE - 1 - pos, line}; // right
            case 2: return new int[]{line, SIZE - 1 - pos}; // down
            default:return new int[]{pos, line};          // left
        }
    }

    private static boolean same(int[][] a, int[][] b) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) if (a[y][x] != b[y][x]) return false;
        }
        return true;
    }

    private static final class Entry {
        final int x, y, value;
        Entry(int x, int y, int value) { this.x = x; this.y = y; this.value = value; }
    }

    public static final class TileMotion {
        public final int value;
        public final int fromX, fromY, toX, toY;
        TileMotion(int value, int fromX, int fromY, int toX, int toY) {
            this.value = value;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }

    public static final class Spawn {
        public final int x, y, value;
        Spawn(int x, int y, int value) { this.x = x; this.y = y; this.value = value; }
    }

    public static final class MoveFrame {
        public final boolean changed;
        public final int scoreGain;
        public final List<TileMotion> motions;
        public final boolean[][] merged;
        public final Spawn spawn;
        public final boolean gameOver;

        MoveFrame(boolean changed, int scoreGain, List<TileMotion> motions,
                  boolean[][] merged, Spawn spawn, boolean gameOver) {
            this.changed = changed;
            this.scoreGain = scoreGain;
            this.motions = Collections.unmodifiableList(motions);
            this.merged = merged;
            this.spawn = spawn;
            this.gameOver = gameOver;
        }

        static MoveFrame unchanged() {
            return new MoveFrame(false, 0, Collections.emptyList(), new boolean[SIZE][SIZE], null, false);
        }
    }
}
