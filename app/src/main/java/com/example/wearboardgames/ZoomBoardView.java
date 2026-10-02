package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/** Shared board camera and snapshot-based AI worker. Engines stay on the UI thread. */
public abstract class ZoomBoardView extends BaseGameView {
    protected final BoardViewport viewport = new BoardViewport();
    private static final ExecutorService AI = Executors.newSingleThreadExecutor();
    private Future<?> aiTask;
    private int generation;
    protected boolean aiThinking;

    protected ZoomBoardView(Context context) { super(context); }
    protected abstract RectF board();

    protected final void beginBoard(Canvas canvas, RectF bounds) {
        viewport.constrain(bounds.width(), bounds.height());
        canvas.save();
        canvas.clipRect(bounds);
        canvas.translate(viewport.panX(), viewport.panY());
        canvas.scale(viewport.zoom(), viewport.zoom(), bounds.centerX(), bounds.centerY());
    }
    protected final void endBoard(Canvas canvas) { canvas.restore(); }
    @Override protected void onGamePinchZoom(float factor) {
        RectF b = board(); viewport.scale(factor, b.width(), b.height());
    }
    @Override protected boolean onGamePan(float dx, float dy) {
        RectF b = board();
        boolean consumed = viewport.pan(dx, dy, b.width(), b.height());
        if (consumed) postInvalidateOnAnimation();
        return consumed;
    }

    protected final void cancelAi() {
        generation++;
        if (aiTask != null) aiTask.cancel(true);
        aiTask = null;
        aiThinking = false;
    }
    protected final <T> void requestAi(Callable<T> compute, Consumer<T> apply) {
        if (aiThinking || !isAttachedToWindow()) return;
        final int token = generation;
        aiThinking = true;
        aiTask = AI.submit(() -> {
            try {
                T value = compute.call();
                post(() -> {
                    if (token != generation || !isAttachedToWindow()) return;
                    aiThinking = false;
                    apply.accept(value);
                    invalidate();
                });
            } catch (Exception error) {
                AppLog.e("Board AI failed", error);
                post(() -> { if (token == generation) { aiThinking = false; invalidate(); } });
            }
        });
    }
    @Override protected void onDetachedFromWindow() {
        cancelAi();
        super.onDetachedFromWindow();
    }
}
