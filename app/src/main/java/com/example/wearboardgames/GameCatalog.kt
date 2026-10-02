package com.example.wearboardgames

internal data class GameDef(
    val mode: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val icon: String,
    val modeChoice: Boolean = false,
    val persistable: Boolean = false,
)

internal val games = listOf(
    GameDef(GameModes.XIANGQI, "中国象棋", "AI / 双人对弈", "棋盘对战", "象", modeChoice = true, persistable = true),
    GameDef(GameModes.CHESS, "国际象棋", "完整规则 · AI / 双人", "棋盘对战", "♞", modeChoice = true, persistable = true),
    GameDef(GameModes.GOMOKU, "五子棋", "强化 AI / 双人 · 五连获胜", "棋盘对战", "●", modeChoice = true, persistable = true),
    GameDef(GameModes.TICTACTOE, "井字棋", "不可输 AI / 双人", "棋盘对战", "×○", modeChoice = true, persistable = true),
    GameDef(GameModes.CONNECT4, "四子棋", "强化 AI / 双人 · 四连珠", "棋盘对战", "④", modeChoice = true, persistable = true),
    GameDef(GameModes.REVERSI, "黑白棋", "6×6 · AI / 双人", "棋盘对战", "◐", modeChoice = true, persistable = true),
    GameDef(GameModes.NIM, "Nim 取石", "最佳策略 AI / 双人", "棋盘对战", "◆", modeChoice = true, persistable = true),

    GameDef(GameModes.GAME_2048, "2048", "经典滑动 · 自动存档", "益智解谜", "2048", persistable = true),
    GameDef(GameModes.SLIDE_PUZZLE, "数字华容道", "3×3 拼图 · 自动存档", "益智解谜", "▦", persistable = true),
    GameDef(GameModes.LIGHTS_OUT, "熄灯解谜", "4×4 灯阵 · 自动存档", "益智解谜", "✦", persistable = true),
    GameDef(GameModes.MINI_MINES, "迷你扫雷", "5×5 · 首击安全", "益智解谜", "✹", persistable = true),
    GameDef(GameModes.MEMORY_MATCH, "记忆配对", "翻牌配对 · 8 对图案", "益智解谜", "▣", persistable = true),
    GameDef(GameModes.MAZE, "迷宫逃脱", "滑动移动 · 找到出口", "益智解谜", "⌁", persistable = true),
    GameDef(GameModes.SUDOKU, "数独", "4×4 唯一解 · 可修改", "益智解谜", "#", persistable = true),
    GameDef(GameModes.SOKOBAN, "推箱子", "多关卡 · 自动存档", "益智解谜", "□", persistable = true),

    GameDef(GameModes.BLOCK_DROP, "俄罗斯方块", "独立渲染 · 平滑下落 · 柔和提速", "街机经典", "▤", persistable = true),
    GameDef(GameModes.SNAKE, "贪吃蛇", "平滑滑行 · 滑动转向", "街机经典", "〰"),
    GameDef(GameModes.FLAPPY, "跳跃小鸟", "缓和重力 · 更大间隙", "街机经典", "▲", persistable = true),
    GameDef(GameModes.PONG, "腕上乒乓", "AI / 双人同屏", "街机经典", "↔", modeChoice = true),
    GameDef(GameModes.BRICK_BREAKER, "砖块破坏", "拖动挡板 · 清空砖块", "街机经典", "▰"),
    GameDef(GameModes.BOUNCE, "弹球挑战", "拖动挡板 · 连续反弹", "街机经典", "●"),
    GameDef(PinballView.MODE, "迷你弹珠台", "双挡板 · 碰撞柱 · 三球挑战", "街机经典", "◉"),
    GameDef(GameModes.RUNNER, "像素跑酷", "落地跳跃 · 空中二次修正", "街机经典", "▰"),
    GameDef(GameModes.DODGER, "三道闪避", "左右切道 · 躲开障碍", "街机经典", "↔"),
    GameDef(GameModes.STACK_TOWER, "叠塔", "移动后点击 · 登上 10 层", "街机经典", "▥"),

    GameDef(GameModes.SIMON, "记忆闪烁", "记住颜色 · 连续挑战", "轻松挑战", "✣"),
    GameDef(GameModes.WHACK_MOLE, "打地鼠", "30 秒短局 · 连击加分", "轻松挑战", "●"),
    GameDef(GameModes.BLACKJACK, "21 点", "简化规则 · 对战庄家", "轻松挑战", "21", persistable = true),
    GameDef(GameModes.QUICK_DRAW, "极速反应", "等提示变绿再点", "轻松挑战", "⚡"),

)

internal val categories = listOf(
    "棋盘对战" to "策略、AI 与双人对弈",
    "益智解谜" to "短时思考、自动存档与脑力题",
    "街机经典" to "动作、触控与 VSYNC 动画",
    "轻松挑战" to "十几秒到两分钟的短局",
)
