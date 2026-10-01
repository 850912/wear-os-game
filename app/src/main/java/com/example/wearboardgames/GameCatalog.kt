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
    // 棋盘对战
    GameDef(GameModes.XIANGQI, "中国象棋", "完整规则 · 双人对弈", "棋盘对战", "象", persistable = true),
    GameDef(GameModes.GOMOKU, "五子棋", "强化 AI / 双人 · 五连获胜", "棋盘对战", "●", modeChoice = true, persistable = true),
    GameDef(GameModes.TICTACTOE, "井字棋", "不可输 AI / 双人", "棋盘对战", "×○", modeChoice = true, persistable = true),
    GameDef(GameModes.CONNECT4, "四子棋", "强化 AI / 双人 · 四连珠", "棋盘对战", "④", modeChoice = true, persistable = true),
    GameDef(GameModes.REVERSI, "黑白棋", "6×6 · AI / 双人", "棋盘对战", "◐", modeChoice = true, persistable = true),
    GameDef(GameModes.NIM, "Nim 取石", "最佳策略 AI / 双人", "棋盘对战", "◆", modeChoice = true, persistable = true),

    // 益智解谜
    GameDef(GameModes.GAME_2048, "2048", "经典滑动 · 自动存档", "益智解谜", "2048", persistable = true),
    GameDef(GameModes.SLIDE_PUZZLE, "数字华容道", "3×3 拼图 · 自动存档", "益智解谜", "▦", persistable = true),
    GameDef(GameModes.LIGHTS_OUT, "熄灯解谜", "4×4 灯阵 · 自动存档", "益智解谜", "✦", persistable = true),
    GameDef(GameModes.MINI_MINES, "迷你扫雷", "5×5 · 首击安全", "益智解谜", "✹", persistable = true),
    GameDef(GameModes.MEMORY_MATCH, "记忆配对", "翻牌配对 · 8 对图案", "益智解谜", "▣", persistable = true),
    GameDef(GameModes.MAZE, "迷宫逃脱", "滑动移动 · 找到出口", "益智解谜", "⌁", persistable = true),
    GameDef(GameModes.SUDOKU, "数独", "4×4 唯一解 · 可修改", "益智解谜", "#", persistable = true),
    GameDef(GameModes.SOKOBAN, "推箱子", "多关卡 · 自动存档", "益智解谜", "□", persistable = true),
    GameDef(PuzzleMiniView.PAIR_SUM, "两数凑和", "选两个数 · 相加等于目标", "益智解谜", "+"),
    GameDef(PuzzleMiniView.SEQUENCE_NEXT, "数列下一项", "找规律 · 10 轮短局", "益智解谜", "…"),
    GameDef(PuzzleMiniView.QUICK_COUNT, "闪速数点", "快速数清屏幕里的点", "益智解谜", "••"),

    // 街机经典
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
    GameDef(GameModes.ORBIT_TAP, "轨道点击", "追击旋转目标 · 20 秒", "街机经典", "◎"),
    GameDef(GameModes.RING_TIMING, "圆环时机", "看准白环 · 8 次挑战", "街机经典", "◌"),
    GameDef(MicroGameView.STAR_CATCH, "接星星", "拖动篮子 · 20 秒", "街机经典", "★"),
    GameDef(MicroGameView.METEOR_DODGE, "陨石闪避", "拖动飞船 · 活得更久", "街机经典", "△"),
    GameDef(MicroGameView.LUNAR_LANDER, "月球着陆", "按住点火 · 轻柔着陆", "街机经典", "☾"),
    GameDef(MicroGameView.BUBBLE_POP, "泡泡连击", "泡泡消失前点中", "街机经典", "○"),

    // 轻松挑战
    GameDef(GameModes.TAP_RUSH, "反应点击", "30 秒手速挑战", "轻松挑战", "◎"),
    GameDef(GameModes.SIMON, "记忆闪烁", "记住颜色 · 连续挑战", "轻松挑战", "✣"),
    GameDef(GameModes.COLOR_HUNT, "色块猎手", "找出不同色 · 12 关", "轻松挑战", "▩"),
    GameDef(GameModes.NUMBER_TAP, "数字连点", "按 1 → 16 顺序点击", "轻松挑战", "16"),
    GameDef(GameModes.QUICK_MATH, "极速心算", "20 秒快速计算", "轻松挑战", "+"),
    GameDef(GameModes.WHACK_MOLE, "打地鼠", "30 秒短局 · 连击加分", "轻松挑战", "●"),
    GameDef(GameModes.BLACKJACK, "21 点", "简化规则 · 对战庄家", "轻松挑战", "21", persistable = true),
    GameDef(GameModes.ROCK_PAPER_SCISSORS, "石头剪刀布", "三局两胜 · 对战手表", "轻松挑战", "✊"),
    GameDef(GameModes.DICE_DUEL, "骰子对决", "先到 20 分 · AI / 双人", "轻松挑战", "⚄", modeChoice = true),
    GameDef(GameModes.QUICK_DRAW, "极速反应", "等提示变绿再点", "轻松挑战", "⚡"),
    GameDef(GameModes.HIGH_LOW, "猜大小", "连续判断下一张牌", "轻松挑战", "↕"),

    // 反应训练
    GameDef(MicroGameView.PRECISION_TIMER, "精准计时", "目标 5.00 秒 · 看感觉停表", "反应训练", "5.00"),
    GameDef(MicroGameView.LEFT_RIGHT, "左右反应", "看箭头 · 点对应半屏", "反应训练", "↔"),
    GameDef(MicroGameView.RHYTHM_TAP, "节奏点击", "圆环重合时点击", "反应训练", "◉"),
    GameDef(MicroGameView.SPIN_LOCK, "旋转锁", "指针进入亮区时点击", "反应训练", "⌾"),
    GameDef(MicroGameView.MEMORY_PATH, "路径记忆", "记住 3×3 闪烁路径", "反应训练", "▦"),
    GameDef(MicroGameView.NUMBER_SORT, "数字排序", "从小到大依次点击", "反应训练", "123"),
    GameDef(MicroGameView.COLOR_STROOP, "颜色冲突", "忽略文字 · 选择真实颜色", "反应训练", "彩"),
    GameDef(MicroGameView.ODD_EVEN, "奇偶快答", "20 秒判断奇数 / 偶数", "反应训练", "±"),
    GameDef(PuzzleMiniView.TARGET_TAP, "移动靶心", "20 秒 · 点中随机靶心", "反应训练", "⊙"),
    GameDef(PuzzleMiniView.SWIPE_ARROW, "方向滑动", "看箭头 · 向对应方向滑", "反应训练", "⇅"),
    GameDef(PuzzleMiniView.STOP_BAR, "中心急停", "移动线靠近中心时点击", "反应训练", "┃"),
    GameDef(PuzzleMiniView.SHAPE_MATCH, "形色判断", "判断颜色和形状是否相同", "反应训练", "◆"),
    GameDef(PuzzleMiniView.SAFE_ZONE, "安全区急停", "移动线进入绿色区域时点击", "反应训练", "▰"),

    // v8 扩展：独立玩法，不以换皮凑数量
    GameDef(ExpansionGameView.MEMORY_SEQUENCE, "记忆序列", "记住格子顺序 · 5 轮", "反应训练", "▦"),
    GameDef(ExpansionGameView.COLOR_RUSH, "色彩冲刺", "20 秒快速找色", "反应训练", "●"),
    GameDef(ExpansionGameView.AIM_TRAINER, "瞄准训练", "追踪移动靶心 · 20 秒", "反应训练", "⊕"),
    GameDef(ExpansionGameView.REACTION_GRID, "反应九宫格", "亮起就点 · 20 秒", "反应训练", "▦"),
    GameDef(ExpansionGameView.DOT_CONNECT, "连点轨迹", "按编号连接 6 个点", "益智解谜", "⌁"),
    GameDef(ExpansionGameView.TAP_TEMPO, "节拍挑战", "保持 0.65 秒节奏", "轻松挑战", "♪"),
    GameDef(ExpansionGameView.BALANCE_BAR, "平衡杆", "左右修正 · 坚持 20 秒", "轻松挑战", "━"),
    GameDef(ExpansionGameView.COMPASS_MATCH, "方位校准", "旋转指针对准亮线", "轻松挑战", "↑"),
    GameDef(ExpansionGameView.PATTERN_LOCK, "图案锁", "记住 4 格图案 · 5 轮", "益智解谜", "⌗"),
    GameDef(ExpansionGameView.TARGET_SUM, "目标和", "两数相加凑目标 · 10 轮", "益智解谜", "Σ"),
    GameDef(ExpansionGameView.HOLD_RELEASE, "蓄力释放", "按住蓄力 · 松手命中绿区", "反应训练", "▰"),
    GameDef(ExpansionGameView.MINI_GOLF, "迷你高尔夫", "点方向击球 · 尽快进洞", "运动竞技", "●"),
    GameDef(ExpansionGameView.AIR_HOCKEY, "空气曲棍球", "拖动球拍 · 先到 5 分", "运动竞技", "◉"),
    GameDef(ExpansionGameView.MINI_BASKETBALL, "迷你投篮", "抓准力度 · 10 次投篮", "运动竞技", "○"),

    // v8.0 高目标扩展：补足 80+，每款保持独立交互核心
    GameDef(V8MiniGameView.AVOIDER, "自由闪避", "拖动角色 · 躲避下落障碍", "街机经典", "◇"),
    GameDef(V8MiniGameView.LANE_SWITCH, "换道冲刺", "三道选择 · 躲开路障", "街机经典", "⇆"),
    GameDef(V8MiniGameView.FALLING_GATE, "落块穿隙", "拖动穿过连续缺口", "街机经典", "▭"),
    GameDef(V8MiniGameView.MIRROR_TAP, "镜像点击", "点击目标的水平镜像位置", "反应训练", "↔"),
    GameDef(V8MiniGameView.SIZE_SORT, "大小排序", "从小到大点完 6 个圆", "益智解谜", "●"),
    GameDef(V8MiniGameView.BINARY_SWITCH, "二进制开关", "拨动 5 位开关凑目标数", "益智解谜", "01"),
    GameDef(V8MiniGameView.PENALTY_KICK, "点球大战", "避开移动门将 · 8 次射门", "运动竞技", "◎"),
    GameDef(V8MiniGameView.DARTS, "迷你飞镖", "移动准星 · 5 镖计分", "运动竞技", "⊙"),
    GameDef(V8MiniGameView.BOWLING, "迷你保龄", "向上滑球 · 5 轮计分", "运动竞技", "●"),
    GameDef(V8MiniGameView.NUMBER_MEMORY, "数字记忆", "记住数字 · 每轮加一位", "反应训练", "123"),

)

internal val categories = listOf(
    "棋盘对战" to "策略、AI 与双人对弈",
    "益智解谜" to "短时思考、自动存档与脑力题",
    "街机经典" to "动作、触控与 VSYNC 动画",
    "轻松挑战" to "十几秒到两分钟的短局",
    "反应训练" to "节奏、记忆、滑动与快速判断",
    "运动竞技" to "高尔夫、曲棍球与投篮短局",
)
