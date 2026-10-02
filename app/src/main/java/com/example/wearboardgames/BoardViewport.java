package com.example.wearboardgames;

/** Pure camera transform; the inverse is shared by drawing and hit testing. */
public final class BoardViewport {
    private float zoom = 1f, panX, panY;
    public float zoom() { return zoom; }
    public float panX() { return panX; }
    public float panY() { return panY; }
    public void reset() { zoom = 1f; panX = panY = 0; }
    public void scale(float factor, float width, float height) {
        if (!Float.isFinite(factor) || factor <= 0) return;
        float old = zoom;
        zoom = Math.max(1f, Math.min(3f, zoom*factor));
        panX *= zoom/old; panY *= zoom/old;
        constrain(width, height);
    }
    public boolean pan(float dx, float dy, float width, float height) {
        if (zoom <= 1f) return false;
        panX += dx; panY += dy;
        constrain(width, height);
        return true;
    }
    public void constrain(float width, float height) {
        float mx = Math.max(0, width)*(zoom-1)/2, my = Math.max(0, height)*(zoom-1)/2;
        panX = Math.max(-mx, Math.min(mx, panX));
        panY = Math.max(-my, Math.min(my, panY));
    }
    public float boardX(float x, float center) { return center + (x-center-panX)/zoom; }
    public float boardY(float y, float center) { return center + (y-center-panY)/zoom; }
}
