package com.example.wearboardgames;

public final class TetrisTiming {
    private TetrisTiming() { }
    public static long dropDelay(int lines) { return Math.max(650L, 1000L - Math.max(0, lines) / 10 * 35L); }
    public static final long LOCK_DELAY = 550L;
}
