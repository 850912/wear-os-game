package com.example.wearboardgames;

import android.content.SharedPreferences;
import android.view.View;

/** Single gate for haptic feedback so every game honors the global toggle. */
public final class HapticsManager {
    private HapticsManager() {}
    public static boolean perform(View view, SharedPreferences prefs, int constant) {
        return AppSettings.haptics(prefs) && view.performHapticFeedback(constant);
    }
}
