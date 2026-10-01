package com.example.wearboardgames;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;

import java.util.Arrays;

/** Standalone Tetris-style renderer/engine extracted from the legacy GameModes. */
public final class TetrisView extends BaseGameView {
    private static final int MODE = GameModes.BLOCK_DROP;
    private static final int W = 10, H = 16;
    private static final int[][] MASKS = {
            {0x0F00,0x2222,0x00F0,0x4444}, {0x0660,0x0660,0x0660,0x0660},
            {0x0E40,0x4C40,0x4E00,0x4640}, {0x06C0,0x8C40,0x06C0,0x8C40},
            {0x0C60,0x4C80,0x0C60,0x4C80}, {0x08E0,0x6440,0x0E20,0x44C0},
            {0x02E0,0x4460,0x0E80,0xC440}
    };
    private static final int[] COLORS = {
            Color.TRANSPARENT, Color.rgb(88, 210, 239), Color.rgb(255, 218, 91),
            Color.rgb(193, 126, 255), Color.rgb(91, 220, 149), Color.rgb(255, 112, 128),
            Color.rgb(105, 151, 255), Color.rgb(255, 166, 91)
    };

    private final int[][] board = new int[H][W];
    private int type, nextType = -1, holdType = -1, rotation, pieceX, pieceY, score, lines, combo = -1;
    private long nextTick, spawnAnimStart, lineFlashUntil, pieceMoveAnimStart;
    private float pieceMoveFromY;
    private boolean over, paused, holdUsed;
    private final RectF holdRect = new RectF();
    private final RectF nextRect = new RectF();
    private final RectF pauseRect = new RectF();
    private final RectF cellRect = new RectF();
    private final RectF frameRect = new RectF();
    private boolean sideControlGesture;

    public TetrisView(Context context) { super(context); }
    public static boolean supportsMode(int mode) { return mode == MODE; }
    @Override protected int gameMode() { return MODE; }

    @Override protected void resetGame() {
        for (int[] row : board) Arrays.fill(row, 0);
        score = lines = 0;
        combo = -1;
        nextType = -1;
        holdType = -1;
        over = paused = holdUsed = false;
        sideControlGesture = false;
        lineFlashUntil = 0;
        spawnPiece();
    }

    @Override protected void drawGame(Canvas c) {
        update();
        int level = lines / 10 + 1;
        String state = over ? "结束 · " + score + " 分" : paused ? "已暂停" : "得分 " + score + " · Lv." + level + " · " + lines + " 行";
        drawHeader(c, "俄罗斯方块", state);
        RectF area = gamePanel(roundScreen ? .255f : .285f);
        // Tetris is height-bound on a watch. Use every safe vertical pixel and keep the board centered.
        float cell = Math.min(area.width() / W, area.height() / H);
        float bw = cell * W, bh = cell * H;
        float left = getWidth() / 2f - bw / 2f;
        float top = area.top + Math.max(0, (area.height() - bh) * .30f);
        frameRect.set(left - cell * .30f, top - cell * .22f, left + bw + cell * .30f, top + bh + cell * .22f); RectF frame = frameRect;
        p.setColor(Color.rgb(20, 24, 31));
        c.drawRoundRect(frame, cell * .65f, cell * .65f, p);
        drawSidePanel(c, frame, cell);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1f, cell * .035f));
        p.setColor(Color.rgb(40, 46, 56));
        for (int i = 1; i < W; i++) c.drawLine(left + i * cell, top, left + i * cell, top + bh, p);
        for (int i = 1; i < H; i++) c.drawLine(left, top + i * cell, left + bw, top + i * cell, p);
        p.setStyle(Paint.Style.FILL);

        for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) {
            int value = board[y][x];
            if (value != 0) drawCell(c, left + x * cell, top + y * cell, cell, COLORS[value], 1f);
        }

        if (!over) {
            int ghostY = pieceY;
            while (fits(pieceX, ghostY + 1, rotation)) ghostY++;
            drawPiece(c, left, top, cell, pieceX, ghostY, rotation, Color.argb(74, 220, 230, 240), 1f);
            float anim = clamp((now() - spawnAnimStart) / 150f, 0f, 1f);
            float scale = .78f + .22f * (1f - (1f - anim) * (1f - anim));
            float moveT = clamp((now() - pieceMoveAnimStart) / 125f, 0f, 1f);
            float visualY = pieceMoveAnimStart == 0 ? pieceY : pieceMoveFromY + (pieceY - pieceMoveFromY) * (1f - (1f - moveT) * (1f - moveT));
            drawPiece(c, left, top, cell, pieceX, visualY, rotation, COLORS[type + 1], scale);
            if (moveT < 1f) animateNext();
        }

        long time = now();
        if (time < lineFlashUntil) {
            float alpha = (lineFlashUntil - time) / 180f;
            p.setColor(Color.argb((int)(85 * clamp(alpha, 0, 1)), 255, 255, 255));
            c.drawRoundRect(frame, cell * .65f, cell * .65f, p);
            animateNext();
        }
        if (time - spawnAnimStart < 150) animateNext();
        if (paused && !over) {
            p.setColor(Color.argb(165, 0, 0, 0));
            c.drawRoundRect(frame, cell * .65f, cell * .65f, p);
            text(c, "暂停", frame.centerX(), frame.centerY() + s()*.018f, s()*.055f, TEXT, true, Paint.Align.CENTER);
        }
        text(c, "左右移动 · 中下软降 · 上滑旋转 · 下滑直落", getWidth()/2f,
                Math.min(gameBottom() - dp(2), top + bh + cell * .78f), s()*.0168f, MUTED, false, Paint.Align.CENTER);
    }

    private void drawSidePanel(Canvas c, RectF frame, float cell) {
        float chipW = Math.max(dp(34), Math.min(s()*.18f, frame.left - dp(8)));
        float chipH = Math.max(dp(42), s()*.16f);
        holdRect.set(dp(3), frame.top + frame.height()*.18f, dp(3)+chipW, frame.top + frame.height()*.18f+chipH);
        nextRect.set(getWidth()-dp(3)-chipW, frame.top + frame.height()*.18f, getWidth()-dp(3), frame.top + frame.height()*.18f+chipH);
        pauseRect.set(getWidth()-dp(3)-chipW, nextRect.bottom+dp(6), getWidth()-dp(3), nextRect.bottom+dp(6)+dp(48));
        p.setColor(SURFACE_HIGH); c.drawRoundRect(holdRect, dp(10), dp(10), p); c.drawRoundRect(nextRect, dp(10), dp(10), p);
        p.setColor(paused ? Color.rgb(74, 112, 91) : SURFACE_HIGH); c.drawRoundRect(pauseRect, dp(10), dp(10), p);
        text(c, "HOLD", holdRect.centerX(), holdRect.top+dp(12), s()*.016f, MUTED, true, Paint.Align.CENTER);
        text(c, "NEXT", nextRect.centerX(), nextRect.top+dp(12), s()*.016f, MUTED, true, Paint.Align.CENTER);
        text(c, paused ? "继续" : "暂停", pauseRect.centerX(), pauseRect.centerY()+s()*.008f, s()*.019f, TEXT, true, Paint.Align.CENTER);
        if (holdType >= 0) drawMini(c, holdType, holdRect.centerX(), holdRect.centerY()+dp(6), Math.min(cell*.55f, dp(7)));
        if (nextType >= 0) drawMini(c, nextType, nextRect.centerX(), nextRect.centerY()+dp(6), Math.min(cell*.55f, dp(7)));
        if (combo > 0) text(c, "COMBO ×"+(combo+1), frame.centerX(), frame.top-dp(5), s()*.017f, SECONDARY, true, Paint.Align.CENTER);
    }

    private void drawMini(Canvas c, int pieceType, float cx, float cy, float mini) {
        int mask = MASKS[pieceType][0];
        float left = cx-mini*2, top = cy-mini*2;
        for(int y=0;y<4;y++) for(int x=0;x<4;x++) if(maskCell(mask,x,y)) {
            p.setColor(COLORS[pieceType+1]);
            cellRect.set(left+x*mini+1, top+y*mini+1, left+(x+1)*mini-1, top+(y+1)*mini-1);
            c.drawRoundRect(cellRect, mini*.18f, mini*.18f, p);
        }
    }

    private void drawPiece(Canvas c, float left, float top, float cell, int px0, float py0, int rot, int color, float scale) {
        int mask = MASKS[type][rot & 3];
        for (int py = 0; py < 4; py++) for (int px = 0; px < 4; px++) if (maskCell(mask, px, py)) {
            int x = px0 + px; float y = py0 + py;
            if (y >= 0 && x >= 0 && x < W && y < H) drawCell(c, left + x*cell, top + y*cell, cell, color, scale);
        }
    }

    private void drawCell(Canvas c, float l, float t, float cell, int color, float scale) {
        float inset = Math.max(1f, cell * (.075f + (1f - scale) * .30f));
        float cx = l + cell/2f, cy = t + cell/2f, half = (cell/2f - inset) * scale;
        cellRect.set(cx-half, cy-half, cx+half, cy+half);
        p.setColor(color);
        c.drawRoundRect(cellRect, cell*.16f, cell*.16f, p);
        if (richEffectsEnabled() && Color.alpha(color) > 100) {
            p.setColor(Color.argb(58, 255, 255, 255));
            cellRect.set(cx-half+cell*.09f, cy-half+cell*.08f, cx+half-cell*.09f, cy-half+cell*.18f);
            c.drawRoundRect(cellRect, cell*.06f, cell*.06f, p);
        }
    }

    private boolean maskCell(int mask, int x, int y) {
        int bit = 15 - (y * 4 + x);
        return bit >= 0 && ((mask >> bit) & 1) == 1;
    }

    private boolean fits(int x, int y, int rot) {
        int mask = MASKS[type][rot & 3];
        for (int py=0; py<4; py++) for (int px=0; px<4; px++) if (maskCell(mask,px,py)) {
            int bx=x+px, by=y+py;
            if (bx<0 || bx>=W || by>=H) return false;
            if (by>=0 && board[by][bx]!=0) return false;
        }
        return true;
    }

    private int randomType() { return (int)(Math.random() * MASKS.length); }

    private void spawnPiece() {
        if (nextType < 0) nextType = randomType();
        type = nextType;
        nextType = randomType();
        rotation = 0;
        pieceX = 3;
        pieceY = -1;
        holdUsed = false;
        pieceMoveFromY = pieceY; pieceMoveAnimStart = 0;
        nextTick = now() + dropDelay();
        spawnAnimStart = now();
        if (!fits(pieceX, pieceY, rotation)) {
            over = true;
            GameStats.recordMaxMetric(prefs,gameMode(),"level",lines / 10 + 1); GameStats.recordMaxMetric(prefs,gameMode(),"lines",lines);
            finishRound(-1, "堆到顶部了", "得分 " + score + " · 消除 " + lines + " 行", score);
        }
    }

    private long dropDelay() {
        // Gentle curve for a watch: meaningful acceleration without becoming unreadably fast.
        return Math.max(300, 690 - (lines / 10) * 70L - Math.min(180, lines * 4L));
    }

    private void update() {
        if (over || paused) return;
        long time = now();
        if (time >= nextTick) {
            step();
            nextTick = time + dropDelay();
        }
        // Only the active falling piece needs continuous frame scheduling.
        invalidateSoon(Math.max(16, Math.min(80, nextTick - time)));
    }

    private void step() {
        if (fits(pieceX, pieceY+1, rotation)) { pieceMoveFromY = pieceY; pieceY++; pieceMoveAnimStart = now(); }
        else lockPiece();
    }

    private void lockPiece() {
        int mask = MASKS[type][rotation & 3];
        for (int py=0; py<4; py++) for (int px=0; px<4; px++) if (maskCell(mask,px,py)) {
            int bx=pieceX+px, by=pieceY+py;
            if (by>=0 && by<H && bx>=0 && bx<W) board[by][bx]=type+1;
        }
        int cleared=0;
        for (int y=H-1; y>=0; y--) {
            boolean full=true;
            for (int x=0; x<W; x++) if (board[y][x]==0) { full=false; break; }
            if (full) {
                cleared++;
                for (int yy=y; yy>0; yy--) System.arraycopy(board[yy-1],0,board[yy],0,W);
                Arrays.fill(board[0],0);
                y++;
            }
        }
        if (cleared>0) {
            combo++;
            int level = lines / 10 + 1;
            lines += cleared;
            int gained = new int[]{0,100,300,500,800}[cleared] * level + Math.max(0, combo) * 50;
            score += gained;
            showScorePopup(combo > 0 ? "+" + gained + " · COMBO ×" + (combo + 1) : "+" + gained);
            GameStats.recordMaxMetric(prefs,gameMode(),"level",lines / 10 + 1);
            GameStats.recordMaxMetric(prefs,gameMode(),"lines",lines);
            lineFlashUntil = now()+180;
            haptic(cleared == 4 ? HapticFeedbackConstants.LONG_PRESS : HapticFeedbackConstants.CONFIRM);
            sound(cleared >= 2 ? SoundManager.CLEAR : SoundManager.SCORE);
        } else combo = -1;
        spawnPiece();
    }

    private boolean move(int dx) {
        if (!over && fits(pieceX+dx,pieceY,rotation)) { pieceX+=dx; return true; }
        return false;
    }

    private boolean rotate() {
        int next=(rotation+1)&3;
        if (fits(pieceX,pieceY,next)) { rotation=next; return true; }
        if (fits(pieceX-1,pieceY,next)) { pieceX--; rotation=next; return true; }
        if (fits(pieceX+1,pieceY,next)) { pieceX++; rotation=next; return true; }
        return false;
    }

    private void hardDrop() {
        if (over || paused) return;
        int dropped=0;
        while (fits(pieceX,pieceY+1,rotation)) { pieceY++; dropped++; }
        score += dropped * 2;
        lockPiece();
    }

    private void softDrop() {
        if (over || paused) return;
        if (fits(pieceX,pieceY+1,rotation)) { pieceY++; score++; pieceMoveFromY=pieceY-1; pieceMoveAnimStart=now(); }
        else lockPiece();
    }

    private void holdPiece() {
        if (over || paused || holdUsed) return;
        int current = type;
        if (holdType < 0) {
            holdType = current;
            spawnPiece();
        } else {
            type = holdType; holdType = current; rotation=0; pieceX=3; pieceY=-1;
            spawnAnimStart=now(); nextTick=now()+dropDelay();
            if(!fits(pieceX,pieceY,rotation)){over=true;GameStats.recordMaxMetric(prefs,gameMode(),"level",lines / 10 + 1); GameStats.recordMaxMetric(prefs,gameMode(),"lines",lines); finishRound(-1,"无法换入方块","得分 "+score,score);}
        }
        holdUsed = true;
        haptic(HapticFeedbackConstants.CLOCK_TICK);
    }

    private boolean containsWithMinTouch(RectF r,float x,float y){
        float min=dp(48), ex=Math.max(0f,(min-r.width())/2f), ey=Math.max(0f,(min-r.height())/2f);
        return x>=r.left-ex&&x<=r.right+ex&&y>=r.top-ey&&y<=r.bottom+ey;
    }
    private boolean inHoldZone(float x,float y){return containsWithMinTouch(holdRect,x,y);}
    private boolean inPauseZone(float x,float y){return containsWithMinTouch(pauseRect,x,y);}

    @Override protected void onGameTouchDown(float x,float y){sideControlGesture=inPauseZone(x,y)||inHoldZone(x,y);}

    @Override protected void onGameTap(float x, float y) {
        if (over) { sideControlGesture=false; return; }
        if (inPauseZone(x,y)) { paused=!paused; nextTick=now()+dropDelay(); haptic(HapticFeedbackConstants.CLOCK_TICK); sideControlGesture=false; invalidate(); return; }
        if (paused) { sideControlGesture=false; return; }
        if (inHoldZone(x,y)) { holdPiece(); sideControlGesture=false; invalidate(); return; }
        boolean changed;
        if (x < getWidth()*.35f) changed = move(-1);
        else if (x > getWidth()*.65f) changed = move(1);
        else if (y > gameTop() + (gameBottom()-gameTop())*.62f) { softDrop(); changed=true; }
        else changed = rotate();
        if (changed) haptic(HapticFeedbackConstants.CLOCK_TICK);
        sideControlGesture=false;
        invalidate();
    }

    @Override protected void onGameSwipe(float dx, float dy) {
        if (sideControlGesture) { sideControlGesture=false; return; }
        if (over || paused) return;
        boolean changed=false;
        if (Math.abs(dy) > Math.abs(dx)) {
            if (dy > 0) { hardDrop(); changed=true; }
            else changed=rotate();
        } else changed=move(dx>0?1:-1);
        if (changed) haptic(HapticFeedbackConstants.CLOCK_TICK);
        invalidate();
    }

    @Override protected void saveGame() {
        if (over) { clearSavedGameIfMine(); return; }
        TetrisStateCodec.State state = new TetrisStateCodec.State();
        for (int yy=0;yy<H;yy++) System.arraycopy(board[yy],0,state.board[yy],0,W);
        state.score=score; state.lines=lines; state.type=type; state.rotation=rotation; state.x=pieceX; state.y=pieceY;
        state.nextType=nextType; state.holdType=holdType; state.combo=combo; state.holdUsed=holdUsed; state.paused=paused;
        GameSaveManager.save(prefs, MODE, TetrisStateCodec.VERSION, false, TetrisStateCodec.encode(state));
    }

    @Override protected boolean restoreGame() {
        GameSaveManager.SaveRecord record = GameSaveManager.load(prefs, MODE);
        if (record == null) return false;
        if (record.version >= TetrisStateCodec.VERSION) {
            TetrisStateCodec.State state = TetrisStateCodec.decode(record.payload);
            if (state == null) return false;
            for (int yy=0;yy<H;yy++) System.arraycopy(state.board[yy],0,board[yy],0,W);
            score=state.score; lines=state.lines; type=state.type; rotation=state.rotation; pieceX=state.x; pieceY=state.y;
            nextType=state.nextType; holdType=state.holdType; combo=state.combo; holdUsed=state.holdUsed; paused=state.paused; over=false;
            if (!fits(pieceX,pieceY,rotation)) return false;
            finishRestoreTiming();
            return true;
        }
        return restoreLegacyV3(record.payload);
    }

    private boolean restoreLegacyV3(String payload) {
        String[] values = (payload == null ? "" : payload).split(",");
        if (values.length != W*H) return false;
        int[][] restored = new int[H][W];
        try {
            int k=0;
            for (int yy=0;yy<H;yy++) for (int xx=0;xx<W;xx++) {
                int v=Integer.parseInt(values[k++]); if(v<0||v>MASKS.length)return false; restored[yy][xx]=v;
            }
            int restoredType=prefs.getInt("block_type",0), restoredRot=prefs.getInt("block_rot",0);
            int restoredX=prefs.getInt("block_x",3), restoredY=prefs.getInt("block_y",-1);
            if(restoredType<0||restoredType>=MASKS.length||restoredRot<0||restoredRot>3||restoredX<-3||restoredX>=W||restoredY<-4||restoredY>=H)return false;
            for(int yy=0;yy<H;yy++)System.arraycopy(restored[yy],0,board[yy],0,W);
            type=restoredType; rotation=restoredRot; pieceX=restoredX; pieceY=restoredY;
            score=Math.max(0,prefs.getInt("save_aux",0)); lines=Math.max(0,prefs.getInt("save_aux2",0)); over=false;
            nextType=prefs.getInt("block_next",randomType()); if(nextType<0||nextType>=MASKS.length)nextType=randomType();
            holdType=prefs.getInt("block_hold",-1); if(holdType<-1||holdType>=MASKS.length)holdType=-1;
            combo=Math.max(-1,prefs.getInt("block_combo",-1)); holdUsed=prefs.getBoolean("block_hold_used",false); paused=prefs.getBoolean("block_paused",false);
            if(!fits(pieceX,pieceY,rotation))return false;
            finishRestoreTiming();
            // Migrate the next lifecycle checkpoint to the v4 codec automatically.
            return true;
        } catch(RuntimeException ex) { return false; }
    }

    private void finishRestoreTiming(){
        nextTick=now()+dropDelay(); spawnAnimStart=now(); pieceMoveFromY=pieceY; pieceMoveAnimStart=0;
    }
}
