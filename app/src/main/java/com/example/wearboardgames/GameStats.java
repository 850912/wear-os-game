package com.example.wearboardgames;

import android.content.SharedPreferences;

/** Unified statistics store with backward-compatible stat_play/stat_win/stat_best keys. */
public final class GameStats {
    private GameStats() {}

    public static void recordLaunch(SharedPreferences prefs, int gameId) {
        prefs.edit()
                .putInt("stat_play_" + gameId, prefs.getInt("stat_play_" + gameId, 0) + 1)
                .putLong("stat_last_" + gameId, System.currentTimeMillis())
                .apply();
    }

    public static void addPlayTime(SharedPreferences prefs, int gameId, long millis) {
        if (millis <= 0) return;
        long old = prefs.getLong("stat_time_" + gameId, 0L);
        prefs.edit().putLong("stat_time_" + gameId, old + millis).apply();
    }

    public static void recordResult(SharedPreferences prefs, int gameId, int kind, int metric, boolean lowerIsBetter) {
        SharedPreferences.Editor e = prefs.edit();
        if (kind > 0) {
            e.putInt("stat_win_" + gameId, prefs.getInt("stat_win_" + gameId, 0) + 1);
            e.putInt("stat_streak_" + gameId, prefs.getInt("stat_streak_" + gameId, 0) + 1);
        } else if (kind < 0) {
            e.putInt("stat_loss_" + gameId, prefs.getInt("stat_loss_" + gameId, 0) + 1);
            e.putInt("stat_streak_" + gameId, 0);
        }
        if (metric > 0) {
            String key = lowerIsBetter ? "stat_low_" + gameId : "stat_best_" + gameId;
            int old = prefs.getInt(key, 0);
            if (old == 0 || (lowerIsBetter ? metric < old : metric > old)) e.putInt(key, metric);
        }
        e.putInt("stat_recent_" + gameId, metric);
        e.apply();
    }
}
