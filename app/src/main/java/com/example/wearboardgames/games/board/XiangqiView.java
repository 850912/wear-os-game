package com.example.wearboardgames;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;

/** View shell for the pure XiangqiEngine with preview + second-tap move confirmation. */
public final class XiangqiView extends BaseGameView {
    private static final int MODE = GameModes.XIANGQI;
    private final XiangqiEngine engine = new XiangqiEngine();
    private int sx = -1, sy = -1, pendingTx = -1, pendingTy = -1;
    private boolean over;

    public XiangqiView(Context context) { super(context); }
    public static boolean supportsMode(int mode) { return mode == MODE; }
    @Override protected int gameMode() { return MODE; }
    private RectF board() { return gamePanel(roundScreen ? .16f : .12f); }

    @Override protected void resetGame() { engine.reset(); clearSelection(); over = false; }

    @Override protected void drawGame(Canvas c) {
        RectF b = board();
        String state = over ? "本局结束" : pendingTx >= 0 ? "再次点击目标确认" : engine.isRedTurn() ? "红方走" : "黑方走";
        drawHeader(c, "中国象棋", state);
        panel(c, b);
        float cell = Math.min(b.width() / 8f, b.height() / 9f), w = cell * 8, h = cell * 9;
        float left = b.centerX() - w / 2f, top = b.centerY() - h / 2f;

        RectF wood = new RectF(left - cell * .34f, top - cell * .28f, left + w + cell * .34f, top + h + cell * .28f);
        p.setColor(Color.rgb(214, 177, 119)); c.drawRoundRect(wood, cell * .28f, cell * .28f, p);
        p.setColor(Color.rgb(113, 76, 43)); p.setStrokeWidth(Math.max(1f, dp(.8f)));
        for (int x = 0; x < 9; x++) c.drawLine(left + x * cell, top, left + x * cell, top + h, p);
        for (int y = 0; y < 10; y++) c.drawLine(left, top + y * cell, left + w, top + y * cell, p);
        text(c, "楚 河", left + cell * 2f, top + cell * 4.72f, cell * .28f, Color.rgb(119, 74, 40), true, Paint.Align.CENTER);
        text(c, "汉 界", left + cell * 6f, top + cell * 4.72f, cell * .28f, Color.rgb(119, 74, 40), true, Paint.Align.CENTER);

        for (int y = 0; y < 10; y++) for (int x = 0; x < 9; x++) {
            char pc = engine.pieceAt(x, y); if (pc == 0) continue;
            float cx = left + x * cell, cy = top + y * cell, r = cell * .39f;
            p.setColor(Color.argb(65, 69, 45, 27)); c.drawCircle(cx + dp(1), cy + dp(1.5f), r, p);
            p.setColor(Color.rgb(247, 224, 180)); c.drawCircle(cx, cy, r, p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.2f));
            int pieceColor = Character.isUpperCase(pc) ? Color.rgb(190, 57, 51) : Color.rgb(43, 43, 42);
            p.setColor(pieceColor); c.drawCircle(cx, cy, r, p); p.setStyle(Paint.Style.FILL);
            text(c, name(pc), cx, cy + cell * .12f, cell * .38f, pieceColor, true, Paint.Align.CENTER);
        }

        if (sx >= 0) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); p.setColor(PRIMARY);
            c.drawCircle(left + sx * cell, top + sy * cell, cell * .47f, p); p.setStyle(Paint.Style.FILL);
        }
        if (pendingTx >= 0) {
            float cx = left + pendingTx * cell, cy = top + pendingTy * cell;
            p.setColor(Color.argb(85, 154, 203, 255)); c.drawCircle(cx, cy, cell * .43f, p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); p.setColor(PRIMARY); c.drawCircle(cx, cy, cell * .50f, p); p.setStyle(Paint.Style.FILL);
        }
    }

    private String name(char p) {
        switch (Character.toUpperCase(p)) { case 'R': return "车"; case 'H': return "马"; case 'E': return "相"; case 'A': return "士"; case 'K': return Character.isUpperCase(p) ? "帅" : "将"; case 'C': return "炮"; default: return Character.isUpperCase(p) ? "兵" : "卒"; }
    }

    @Override protected void onGameTap(float x, float y) {
        if (over) return;
        RectF b = board(); float cell = Math.min(b.width() / 8f, b.height() / 9f);
        float left = b.centerX() - cell * 4, top = b.centerY() - cell * 4.5f;
        int tx = Math.round((x - left) / cell), ty = Math.round((y - top) / cell);
        if (tx < 0 || tx > 8 || ty < 0 || ty > 9) return;

        if (sx < 0) {
            if (engine.belongsToTurn(tx, ty)) { sx = tx; sy = ty; pendingTx = pendingTy = -1; haptic(HapticFeedbackConstants.CLOCK_TICK); }
            invalidate(); return;
        }

        if (engine.belongsToTurn(tx, ty)) {
            sx = tx; sy = ty; pendingTx = pendingTy = -1; haptic(HapticFeedbackConstants.CLOCK_TICK); invalidate(); return;
        }
        if (!engine.isLegalMove(sx, sy, tx, ty)) {
            pendingTx = pendingTy = -1; haptic(HapticFeedbackConstants.REJECT); invalidate(); return;
        }
        if (moveConfirmationEnabled() && (pendingTx != tx || pendingTy != ty)) {
            pendingTx = tx; pendingTy = ty; haptic(HapticFeedbackConstants.CLOCK_TICK); invalidate(); return;
        }

        XiangqiEngine.MoveResult result = engine.move(sx, sy, tx, ty);
        clearSelection();
        if (result == XiangqiEngine.MoveResult.ILLEGAL) return;
        haptic(HapticFeedbackConstants.CONFIRM);
        if (result == XiangqiEngine.MoveResult.RED_WINS || result == XiangqiEngine.MoveResult.BLACK_WINS) {
            over = true;
            finishRound(1, result == XiangqiEngine.MoveResult.RED_WINS ? "红方获胜" : "黑方获胜", "完整规则对局", 1);
        }
        invalidate();
    }

    private void clearSelection() { sx = sy = pendingTx = pendingTy = -1; }
    @Override protected void saveGame() { if (over) { clearSavedGameIfMine(); return; } GameSaveManager.save(prefs, MODE, 1, false, engine.serialize()); }
    @Override protected boolean restoreGame() { GameSaveManager.SaveRecord r = GameSaveManager.load(prefs, MODE); if (r == null) return false; over = false; clearSelection(); return engine.restore(r.payload); }
}
