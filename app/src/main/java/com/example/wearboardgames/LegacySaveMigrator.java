package com.example.wearboardgames;

import android.content.SharedPreferences;

/** Converts the v7.4 GameHub save slot into v8 engine payloads on first resume. */
public final class LegacySaveMigrator {
    private LegacySaveMigrator() {}

    public static boolean isLegacy(SharedPreferences prefs) {
        return prefs.getBoolean("has_save", false) && prefs.getInt("save_format", 0) == 0;
    }

    public static String enginePayload(SharedPreferences prefs, int mode, String old) {
        if (!isLegacy(prefs) || old == null) return null;
        try {
            if (mode == GameModes.GOMOKU) {
                String digits = csvDigits(old, GomokuEngine.SIZE * GomokuEngine.SIZE, 2);
                int turn = validTurn(prefs.getInt("save_turn", 1));
                return turn + "|0|-1|-1|" + digits;
            }
            if (mode == GameModes.CONNECT4) {
                String digits = csvDigits(old, Connect4Engine.ROWS * Connect4Engine.COLS, 2);
                return validTurn(prefs.getInt("save_turn", 1)) + "|0|" + digits;
            }
            if (mode == GameModes.REVERSI) {
                String digits = csvDigits(old, ReversiEngine.SIZE * ReversiEngine.SIZE, 2);
                return validTurn(prefs.getInt("save_turn", 1)) + "|0|" + digits;
            }
            if (mode == GameModes.SUDOKU) {
                String[] q = old.split("\\|", -1); if (q.length != 3) return null;
                String cells = csvDigits(q[0], 16, 4); String fixed = boolDigits(q[2], 16);
                return cells + "|" + fixed;
            }
            if (mode == GameModes.MINI_MINES) {
                String[] q = old.split("\\|", -1); if (q.length != 2) return null;
                String mines = boolDigits(q[0], 25), open = boolDigits(q[1], 25);
                boolean any = open.indexOf('1') >= 0;
                return (any ? "0" : "1") + "00|" + mines + "|" + open;
            }
            if (mode == GameModes.MAZE) {
                String[] values = old.split(","); if (values.length != 81) return null;
                for (String value : values) { int v=Integer.parseInt(value); if(v<0||v>15)return null; }
                int player=prefs.getInt("save_aux",0), moves=prefs.getInt("save_aux2",0);
                if(player<0||player>=81||moves<0)return null;
                return player+"|"+moves+"|"+old;
            }
            if (mode == GameModes.SOKOBAN) {
                String[] q=old.split("\\|",-1); if(q.length!=2)return null;
                String[] map=q[0].split(","); if(map.length!=SokobanEngine.W*SokobanEngine.H)return null;
                StringBuilder boxes=new StringBuilder(map.length);
                for(String value:map){int v=Integer.parseInt(value);if(v<0||v>2)return null;boxes.append(v==2?'1':'0');}
                int level=prefs.getInt("save_turn",0), player=prefs.getInt("save_aux",0), moves=prefs.getInt("save_aux2",0);
                if(level<0||level>=SokobanEngine.levelCount()||player<0||player>=map.length||moves<0)return null;
                return level+"|"+moves+"|"+player+"|"+boxes;
            }
        } catch (RuntimeException ignored) { }
        return null;
    }

    public static void markMigrated(SharedPreferences prefs) {
        prefs.edit().putInt("save_format", 1).apply();
    }

    private static int validTurn(int t){return t==2?2:1;}
    private static String csvDigits(String csv,int count,int max){String[]p=csv.split(",");if(p.length!=count)throw new IllegalArgumentException();StringBuilder b=new StringBuilder(count);for(String s:p){int v=Integer.parseInt(s);if(v<0||v>max)throw new IllegalArgumentException();b.append((char)('0'+v));}return b.toString();}
    private static String boolDigits(String s,int count){if(s.length()!=count)throw new IllegalArgumentException();for(int i=0;i<s.length();i++)if(s.charAt(i)!='0'&&s.charAt(i)!='1')throw new IllegalArgumentException();return s;}
}
