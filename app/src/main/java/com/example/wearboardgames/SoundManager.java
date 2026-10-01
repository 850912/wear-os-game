package com.example.wearboardgames;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;

/** Tiny optional sound layer. Sound is disabled by default on watches. */
public final class SoundManager {
    public static final int CLICK = 1, SCORE = 2, CLEAR = 3, GAME_OVER = 4;
    private static ToneGenerator tone;
    private SoundManager() {}

    public static synchronized void play(SharedPreferences prefs, int event) {
        if (!AppSettings.sound(prefs)) return;
        if (tone == null) tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 22);
        int type;
        int duration;
        switch (event) {
            case SCORE: type = ToneGenerator.TONE_PROP_ACK; duration = 45; break;
            case CLEAR: type = ToneGenerator.TONE_PROP_BEEP2; duration = 80; break;
            case GAME_OVER: type = ToneGenerator.TONE_PROP_NACK; duration = 100; break;
            default: type = ToneGenerator.TONE_PROP_BEEP; duration = 30; break;
        }
        tone.startTone(type, duration);
    }
}
