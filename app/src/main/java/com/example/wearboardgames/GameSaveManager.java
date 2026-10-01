package com.example.wearboardgames;

import android.content.SharedPreferences;

/**
 * Versioned single-slot save store. It mirrors legacy keys so v7.x saves and the v8 hub remain compatible.
 */
public final class GameSaveManager {
    private static final int FORMAT = 1;
    private GameSaveManager() {}

    public static void save(SharedPreferences prefs, int gameId, int saveVersion, boolean ai, String payload) {
        prefs.edit()
                .putBoolean("has_save", true)
                .putInt("last_mode", gameId)
                .putInt("save_version", saveVersion)
                .putBoolean("save_ai", ai)
                .putString("save_data", payload == null ? "" : payload)
                .putInt("save_format", FORMAT)
                .putLong("save_timestamp", System.currentTimeMillis())
                .apply();
    }

    public static SaveRecord load(SharedPreferences prefs, int gameId) {
        if (!prefs.getBoolean("has_save", false) || prefs.getInt("last_mode", -1) != gameId) return null;
        return new SaveRecord(
                gameId,
                prefs.getInt("save_version", 1),
                prefs.getBoolean("save_ai", true),
                prefs.getString("save_data", ""),
                prefs.getLong("save_timestamp", 0L));
    }

    public static void clearIfGame(SharedPreferences prefs, int gameId) {
        if (prefs.getBoolean("has_save", false) && prefs.getInt("last_mode", -1) == gameId) {
            prefs.edit().putBoolean("has_save", false).remove("save_timestamp").apply();
        }
    }

    public static final class SaveRecord {
        public final int gameId, version;
        public final boolean ai;
        public final String payload;
        public final long timestamp;
        SaveRecord(int gameId, int version, boolean ai, String payload, long timestamp) {
            this.gameId = gameId; this.version = version; this.ai = ai; this.payload = payload; this.timestamp = timestamp;
        }
    }
}
