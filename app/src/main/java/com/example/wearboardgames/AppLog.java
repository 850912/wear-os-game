package com.example.wearboardgames;

import android.util.Log;

/** Small, stable Logcat surface so device reports can be filtered to this app. */
public final class AppLog {
    public static final String TAG = "WearBoardGames";

    private AppLog() {}

    public static void i(String message) {
        Log.i(TAG, message);
    }

    public static void w(String message, Throwable error) {
        Log.w(TAG, message, error);
    }

    public static void e(String message, Throwable error) {
        Log.e(TAG, message, error);
    }
}
