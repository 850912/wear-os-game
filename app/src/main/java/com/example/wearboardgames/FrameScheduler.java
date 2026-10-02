package com.example.wearboardgames;

import android.os.SystemClock;
import android.view.View;

/** One cancellable, earliest-deadline frame request per View, aligned with VSYNC. */
final class FrameScheduler {
    private final View view;
    private long deadline = Long.MAX_VALUE;
    private final Runnable frame;

    FrameScheduler(View view) {
        this.view = view;
        frame = () -> {
            deadline = Long.MAX_VALUE;
            if (view.isAttachedToWindow() && view.getWindowVisibility() == View.VISIBLE) view.invalidate();
        };
    }

    void request(long delay) {
        if (!view.isAttachedToWindow() || view.getWindowVisibility() != View.VISIBLE) return;
        long due = SystemClock.uptimeMillis() + Math.max(0, delay);
        if (due >= deadline) return;
        view.removeCallbacks(frame);
        deadline = due;
        if (delay <= 16) view.postOnAnimation(frame);
        else view.postOnAnimationDelayed(frame, delay);
    }

    void cancel() {
        view.removeCallbacks(frame);
        deadline = Long.MAX_VALUE;
    }
}
