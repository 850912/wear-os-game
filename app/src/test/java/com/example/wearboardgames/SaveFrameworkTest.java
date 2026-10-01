package com.example.wearboardgames;

import android.content.SharedPreferences;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class SaveFrameworkTest {
    @Test public void gameSaveManagerKeepsLegacyKeysAndClearsOnlyMatchingGame() {
        FakePrefs p = new FakePrefs();
        GameSaveManager.save(p, GameModes.GOMOKU, 8, false, "payload");
        GameSaveManager.SaveRecord r = GameSaveManager.load(p, GameModes.GOMOKU);
        assertNotNull(r);
        assertEquals(GameModes.GOMOKU, r.gameId);
        assertEquals(8, r.version);
        assertFalse(r.ai);
        assertEquals("payload", r.payload);
        assertTrue(r.timestamp > 0L);
        assertEquals(1, p.getInt("save_format", 0));

        GameSaveManager.clearIfGame(p, GameModes.CONNECT4);
        assertTrue(p.getBoolean("has_save", false));
        GameSaveManager.clearIfGame(p, GameModes.GOMOKU);
        assertFalse(p.getBoolean("has_save", true));
    }

    @Test public void v74BoardPayloadsMigrateIntoV8EngineFormats() {
        FakePrefs p = legacyPrefs(GameModes.GOMOKU);
        p.edit().putInt("save_turn", 2).apply();
        String gomoku = LegacySaveMigrator.enginePayload(p, GameModes.GOMOKU, csvZeros(GomokuEngine.SIZE * GomokuEngine.SIZE));
        assertNotNull(gomoku); assertTrue(new GomokuEngine().restore(gomoku));

        p = legacyPrefs(GameModes.CONNECT4);
        String c4 = LegacySaveMigrator.enginePayload(p, GameModes.CONNECT4, csvZeros(Connect4Engine.ROWS * Connect4Engine.COLS));
        assertNotNull(c4); assertTrue(new Connect4Engine().restore(c4));

        p = legacyPrefs(GameModes.REVERSI);
        String rev = LegacySaveMigrator.enginePayload(p, GameModes.REVERSI, csvZeros(ReversiEngine.SIZE * ReversiEngine.SIZE));
        assertNotNull(rev); assertTrue(new ReversiEngine().restore(rev));

        SudokuEngine sudokuEngine = new SudokuEngine(); sudokuEngine.reset(0);
        String[] sudokuParts = sudokuEngine.serialize().split("\\|", -1);
        p = legacyPrefs(GameModes.SUDOKU);
        String sudokuOld = charsToCsv(sudokuParts[0]) + "|unused|" + sudokuParts[1];
        String sudoku = LegacySaveMigrator.enginePayload(p, GameModes.SUDOKU, sudokuOld);
        assertNotNull(sudoku); assertTrue(new SudokuEngine().restore(sudoku));

        p = legacyPrefs(GameModes.MINI_MINES);
        String minesOld = zeros(25) + "|" + zeros(25);
        String mines = LegacySaveMigrator.enginePayload(p, GameModes.MINI_MINES, minesOld);
        assertNotNull(mines); assertTrue(new MinesEngine().restore(mines));

        MazeEngine mazeEngine = new MazeEngine();
        String[] mazeParts = mazeEngine.serialize().split("\\|", 3);
        p = legacyPrefs(GameModes.MAZE);
        p.edit().putInt("save_aux", Integer.parseInt(mazeParts[0])).putInt("save_aux2", Integer.parseInt(mazeParts[1])).apply();
        String maze = LegacySaveMigrator.enginePayload(p, GameModes.MAZE, mazeParts[2]);
        assertNotNull(maze); assertTrue(new MazeEngine().restore(maze));

        SokobanEngine sokobanEngine = new SokobanEngine();
        String[] sokobanParts = sokobanEngine.serialize().split("\\|", 4);
        StringBuilder oldMap = new StringBuilder();
        for (int i=0;i<sokobanParts[3].length();i++) {
            if (i>0) oldMap.append(',');
            oldMap.append(sokobanParts[3].charAt(i)=='1' ? '2' : '0');
        }
        p = legacyPrefs(GameModes.SOKOBAN);
        p.edit().putInt("save_turn", Integer.parseInt(sokobanParts[0]))
                .putInt("save_aux", Integer.parseInt(sokobanParts[2]))
                .putInt("save_aux2", Integer.parseInt(sokobanParts[1])).apply();
        String sokoban = LegacySaveMigrator.enginePayload(p, GameModes.SOKOBAN, oldMap + "|unused");
        assertNotNull(sokoban); assertTrue(new SokobanEngine().restore(sokoban));
    }

    @Test public void malformedLegacyPayloadIsRejectedWithoutMarkingMigrated() {
        FakePrefs p = legacyPrefs(GameModes.GOMOKU);
        assertNull(LegacySaveMigrator.enginePayload(p, GameModes.GOMOKU, "bad"));
        assertTrue(LegacySaveMigrator.isLegacy(p));
        LegacySaveMigrator.markMigrated(p);
        assertFalse(LegacySaveMigrator.isLegacy(p));
    }

    private static FakePrefs legacyPrefs(int mode) {
        FakePrefs p = new FakePrefs();
        p.edit().putBoolean("has_save", true).putInt("last_mode", mode).putInt("save_format", 0).apply();
        return p;
    }
    private static String zeros(int n) { StringBuilder b=new StringBuilder(n); for(int i=0;i<n;i++)b.append('0'); return b.toString(); }
    private static String csvZeros(int n) { StringBuilder b=new StringBuilder(); for(int i=0;i<n;i++){if(i>0)b.append(',');b.append('0');}return b.toString(); }
    private static String charsToCsv(String s) { StringBuilder b=new StringBuilder(); for(int i=0;i<s.length();i++){if(i>0)b.append(',');b.append(s.charAt(i));}return b.toString(); }

    /** JVM-only SharedPreferences implementation used to keep save/migration tests deterministic. */
    private static final class FakePrefs implements SharedPreferences {
        private final Map<String,Object> values = new HashMap<>();
        @Override public Map<String, ?> getAll() { return Collections.unmodifiableMap(values); }
        @Override public String getString(String key, String defValue) { Object v=values.get(key); return v instanceof String?(String)v:defValue; }
        @SuppressWarnings("unchecked") @Override public Set<String> getStringSet(String key, Set<String> defValue) { Object v=values.get(key); return v instanceof Set?(Set<String>)v:defValue; }
        @Override public int getInt(String key, int defValue) { Object v=values.get(key); return v instanceof Integer?(Integer)v:defValue; }
        @Override public long getLong(String key, long defValue) { Object v=values.get(key); return v instanceof Long?(Long)v:defValue; }
        @Override public float getFloat(String key, float defValue) { Object v=values.get(key); return v instanceof Float?(Float)v:defValue; }
        @Override public boolean getBoolean(String key, boolean defValue) { Object v=values.get(key); return v instanceof Boolean?(Boolean)v:defValue; }
        @Override public boolean contains(String key) { return values.containsKey(key); }
        @Override public Editor edit() { return new FakeEditor(); }
        @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) { }
        @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) { }

        private final class FakeEditor implements Editor {
            private final Map<String,Object> puts=new HashMap<>(); private final Set<String> removes=new HashSet<>(); private boolean clear;
            @Override public Editor putString(String key,String value){puts.put(key,value);return this;}
            @Override public Editor putStringSet(String key,Set<String> value){puts.put(key,value==null?null:new HashSet<>(value));return this;}
            @Override public Editor putInt(String key,int value){puts.put(key,value);return this;}
            @Override public Editor putLong(String key,long value){puts.put(key,value);return this;}
            @Override public Editor putFloat(String key,float value){puts.put(key,value);return this;}
            @Override public Editor putBoolean(String key,boolean value){puts.put(key,value);return this;}
            @Override public Editor remove(String key){removes.add(key);return this;}
            @Override public Editor clear(){clear=true;return this;}
            @Override public boolean commit(){apply();return true;}
            @Override public void apply(){if(clear)values.clear();for(String k:removes)values.remove(k);values.putAll(puts);}
        }
    }
}
