package com.example.wearboardgames;

/** Pure serializer for the Tetris state so save/restore can be unit-tested without Android. */
public final class TetrisStateCodec {
    public static final int W=10,H=16,VERSION=4;
    private TetrisStateCodec() {}

    public static final class State {
        public final int[][] board = new int[H][W];
        public int score, lines, type, rotation, x, y, nextType, holdType, combo;
        public boolean holdUsed, paused;
    }

    public static String encode(State s) {
        if (!valid(s)) throw new IllegalArgumentException("Invalid Tetris state");
        StringBuilder out = new StringBuilder(220);
        out.append(VERSION).append('|').append(s.score).append('|').append(s.lines).append('|')
                .append(s.type).append('|').append(s.rotation).append('|').append(s.x).append('|').append(s.y).append('|')
                .append(s.nextType).append('|').append(s.holdType).append('|').append(s.combo).append('|')
                .append(s.holdUsed?'1':'0').append('|').append(s.paused?'1':'0').append('|');
        for(int row=0;row<H;row++) for(int col=0;col<W;col++) out.append((char)('0'+s.board[row][col]));
        return out.toString();
    }

    public static State decode(String raw) {
        if(raw==null) return null;
        String[] p=raw.split("\\|",13);
        if(p.length!=13 || p[12].length()!=W*H) return null;
        try {
            if(Integer.parseInt(p[0])!=VERSION) return null;
            State s=new State();
            s.score=Integer.parseInt(p[1]); s.lines=Integer.parseInt(p[2]); s.type=Integer.parseInt(p[3]);
            s.rotation=Integer.parseInt(p[4]); s.x=Integer.parseInt(p[5]); s.y=Integer.parseInt(p[6]);
            s.nextType=Integer.parseInt(p[7]); s.holdType=Integer.parseInt(p[8]); s.combo=Integer.parseInt(p[9]);
            if(!isBool(p[10])||!isBool(p[11])) return null;
            s.holdUsed=p[10].charAt(0)=='1'; s.paused=p[11].charAt(0)=='1';
            int k=0; for(int row=0;row<H;row++) for(int col=0;col<W;col++) {
                char ch=p[12].charAt(k++); if(ch<'0'||ch>'7') return null; s.board[row][col]=ch-'0';
            }
            return valid(s)?s:null;
        } catch(RuntimeException ex) { return null; }
    }

    private static boolean isBool(String s){return s.length()==1&&(s.charAt(0)=='0'||s.charAt(0)=='1');}
    private static boolean valid(State s){
        if(s==null||s.score<0||s.lines<0||s.type<0||s.type>6||s.rotation<0||s.rotation>3||
                s.x<-3||s.x>=W||s.y<-4||s.y>=H||s.nextType<0||s.nextType>6||s.holdType<-1||s.holdType>6||s.combo<-1) return false;
        if(s.board.length!=H) return false;
        for(int[] row:s.board){if(row==null||row.length!=W)return false;for(int v:row)if(v<0||v>7)return false;}
        return true;
    }
}
