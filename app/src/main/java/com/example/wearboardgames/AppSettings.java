package com.example.wearboardgames;

import android.content.Context;
import android.content.SharedPreferences;

/** Centralized user-facing settings shared by Compose and Canvas games. */
public final class AppSettings {
    public static final String PREFS = "wear_games";
    public static final String KEY_DYNAMIC_COLOR = "setting_dynamic_color";
    public static final String KEY_HAPTICS = "setting_haptics";
    public static final String KEY_SOUND = "setting_sound";
    public static final String KEY_ANIMATIONS = "setting_animations";
    public static final String KEY_LEFT_HANDED = "setting_left_handed";
    public static final String KEY_MOVE_CONFIRM = "setting_move_confirm";
    public static final String KEY_PERFORMANCE = "setting_performance"; // smooth | balanced | saver

    private AppSettings() {}

    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean dynamicColor(SharedPreferences prefs) { return prefs.getBoolean(KEY_DYNAMIC_COLOR, true); }
    public static boolean haptics(SharedPreferences prefs) { return prefs.getBoolean(KEY_HAPTICS, true); }
    public static boolean sound(SharedPreferences prefs) { return prefs.getBoolean(KEY_SOUND, false); }
    public static boolean animations(SharedPreferences prefs) { return prefs.getBoolean(KEY_ANIMATIONS, true); }
    public static boolean leftHanded(SharedPreferences prefs) { return prefs.getBoolean(KEY_LEFT_HANDED, false); }
    public static boolean moveConfirm(SharedPreferences prefs) { return prefs.getBoolean(KEY_MOVE_CONFIRM, true); }
    public static String performance(SharedPreferences prefs) { return prefs.getString(KEY_PERFORMANCE, "balanced"); }
    public static boolean saver(SharedPreferences prefs) { return "saver".equals(performance(prefs)); }
    public static boolean smooth(SharedPreferences prefs) { return "smooth".equals(performance(prefs)); }

    /**
     * Delay used by continuously animated games. Smooth follows the display VSYNC; balanced
     * intentionally reduces GPU/CPU pressure; saver is capped further for small watch batteries.
     */
    public static long frameDelayMs(SharedPreferences prefs) {
        String mode = performance(prefs);
        if ("smooth".equals(mode)) return 0L;
        if ("saver".equals(mode)) return 40L;      // ~25 fps target
        return 16L;                                // ~60 fps target, coalesced by the View scheduler
    }

    /** Expensive decorative effects are intentionally omitted in saver mode. */
    public static boolean richEffects(SharedPreferences prefs) {
        return animations(prefs) && !saver(prefs);
    }
}
