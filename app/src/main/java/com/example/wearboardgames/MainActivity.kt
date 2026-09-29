package com.example.wearboardgames

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.dynamicColorScheme
import java.util.concurrent.atomic.AtomicReference

class MainActivity : ComponentActivity() {
    private var activeGameView: GameHubView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = Color.BLACK
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        setContent {
            val context = LocalContext.current
            val dynamicScheme = remember(context) { dynamicColorScheme(context) }
            MaterialTheme(colorScheme = dynamicScheme ?: ColorScheme()) {
                WearGamesApp(
                    onGameViewChanged = { activeGameView = it },
                )
            }
        }
    }

    override fun onPause() {
        activeGameView?.persistCurrentState()
        super.onPause()
    }
}

private enum class HubPage { HOME, CATEGORY, MODE, GAME, TOOLS, RECORDS, SUPPORT, ABOUT }

private data class GameDef(
    val mode: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val icon: String,
    val modeChoice: Boolean = false,
    val persistable: Boolean = false,
)

private val games = listOf(
    GameDef(GameHubView.XIANGQI, "中国象棋", "完整规则 · 双人对弈", "棋盘对战", "象", persistable = true),
    GameDef(GameHubView.GOMOKU, "五子棋", "强化 AI / 双人 · 五连获胜", "棋盘对战", "●", modeChoice = true, persistable = true),
    GameDef(GameHubView.TICTACTOE, "井字棋", "不可输 AI / 双人", "棋盘对战", "×○", modeChoice = true, persistable = true),
    GameDef(GameHubView.CONNECT4, "四子棋", "强化 AI / 双人 · 四连珠", "棋盘对战", "④", modeChoice = true, persistable = true),
    GameDef(GameHubView.REVERSI, "黑白棋", "6×6 · AI / 双人", "棋盘对战", "◐", modeChoice = true, persistable = true),
    GameDef(GameHubView.NIM, "Nim 取石", "最佳策略 AI / 双人", "棋盘对战", "◆", modeChoice = true, persistable = true),

    GameDef(GameHubView.GAME_2048, "2048", "经典滑动 · 保留原逻辑", "益智解谜", "2048", persistable = true),
    GameDef(GameHubView.SLIDE_PUZZLE, "数字华容道", "3×3 拼图 · 自动存档", "益智解谜", "▦", persistable = true),
    GameDef(GameHubView.LIGHTS_OUT, "熄灯解谜", "4×4 灯阵 · 自动存档", "益智解谜", "✦", persistable = true),
    GameDef(GameHubView.MINI_MINES, "迷你扫雷", "5×5 雷区 · 自动存档", "益智解谜", "✹", persistable = true),
    GameDef(GameHubView.MEMORY_MATCH, "记忆配对", "翻牌配对 · 8 对图案", "益智解谜", "▣", persistable = true),
    GameDef(GameHubView.MAZE, "迷宫逃脱", "滑动移动 · 找到出口", "益智解谜", "⌁", persistable = true),
    GameDef(GameHubView.SUDOKU, "数独", "4×4 快速局 · 可修改/清除", "益智解谜", "#", persistable = true),
    GameDef(GameHubView.SOKOBAN, "推箱子", "滑动移动 · 多关卡 · 自动存档", "益智解谜", "□", persistable = true),

    GameDef(GameHubView.BLOCK_DROP, "俄罗斯方块", "旋转 / 横移 / 快速下落", "街机经典", "▤", persistable = true),
    GameDef(GameHubView.SNAKE, "贪吃蛇", "滑动转向 · 越吃越快", "街机经典", "〰"),
    GameDef(GameHubView.FLAPPY, "跳跃小鸟", "轻点起飞 · 穿过障碍", "街机经典", "▲", persistable = true),
    GameDef(GameHubView.PONG, "腕上乒乓", "AI / 双人同屏", "街机经典", "↔", modeChoice = true),
    GameDef(GameHubView.BRICK_BREAKER, "砖块破坏", "拖动挡板 · 清空砖块", "街机经典", "▰"),
    GameDef(GameHubView.BOUNCE, "弹球挑战", "拖动挡板 · 连续反弹", "街机经典", "●"),
    GameDef(GameHubView.RUNNER, "像素跑酷", "轻点跳跃 · 越跑越快", "街机经典", "▰"),
    GameDef(GameHubView.DODGER, "三道闪避", "左右切道 · 躲开障碍", "街机经典", "↔"),
    GameDef(GameHubView.STACK_TOWER, "叠塔", "把移动方块叠到 10 层", "街机经典", "▥"),

    GameDef(GameHubView.TAP_RUSH, "反应点击", "30 秒手速挑战", "轻松挑战", "◎"),
    GameDef(GameHubView.SIMON, "记忆闪烁", "记住颜色 · 连续挑战", "轻松挑战", "✣"),
    GameDef(GameHubView.COLOR_HUNT, "色块猎手", "找出不同色 · 12 关", "轻松挑战", "▩"),
    GameDef(GameHubView.NUMBER_TAP, "数字连点", "按 1 → 16 顺序点击", "轻松挑战", "16"),
    GameDef(GameHubView.QUICK_MATH, "极速心算", "20 秒快速计算", "轻松挑战", "+"),
    GameDef(GameHubView.WHACK_MOLE, "打地鼠", "30 秒短局 · 连击加分", "轻松挑战", "●"),
    GameDef(GameHubView.BLACKJACK, "21 点", "要牌 / 停牌 · 对战庄家", "轻松挑战", "21", persistable = true),
)

private val categories = listOf(
    "棋盘对战" to "策略与双人对弈",
    "益智解谜" to "适合碎片时间的思考局",
    "街机经典" to "更适合表冠与触控的经典玩法",
    "轻松挑战" to "30 秒到 2 分钟即可完成",
)

@Composable
private fun WearGamesApp(onGameViewChanged: (GameHubView?) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("wear_games", android.content.Context.MODE_PRIVATE) }
    var page by remember { mutableStateOf(HubPage.HOME) }
    var category by remember { mutableStateOf(categories.first().first) }
    var selected by remember { mutableStateOf(games.first()) }
    var singlePlayer by remember { mutableStateOf(true) }
    var resume by remember { mutableStateOf(false) }

    val goHome: () -> Unit = {
        resume = false
        page = HubPage.HOME
    }

    BackHandler(enabled = page != HubPage.HOME) {
        when (page) {
            HubPage.GAME -> {
                onGameViewChanged(null)
                page = if (selected.modeChoice) HubPage.MODE else HubPage.CATEGORY
            }
            HubPage.MODE -> page = HubPage.CATEGORY
            HubPage.CATEGORY, HubPage.TOOLS -> page = HubPage.HOME
            HubPage.RECORDS, HubPage.SUPPORT, HubPage.ABOUT -> page = HubPage.TOOLS
            else -> goHome()
        }
    }

    AppScaffold {
        when (page) {
            HubPage.HOME -> HubListScreen("腕上小游戏", "30 款 · 4 分类 · Material 3 Expressive") {
                if (prefs.getBoolean("has_save", false)) {
                    val last = prefs.getInt("last_mode", -1)
                    val saved = games.firstOrNull { it.mode == last }
                    if (saved != null) {
                        item {
                            ExpressiveCard(
                                title = "继续 · ${saved.title}",
                                subtitle = "恢复上次自动存档",
                                icon = "▶",
                                onClick = {
                                    selected = saved
                                    resume = true
                                    page = HubPage.GAME
                                },
                            )
                        }
                    }
                }
                items(categories) { (name, desc) ->
                    ExpressiveCard(name, desc, categoryIcon(name)) {
                        category = name
                        page = HubPage.CATEGORY
                    }
                }
                item {
                    ExpressiveCard("工具与关于", "战绩、支持作者、反馈、版本说明", "⋯") { page = HubPage.TOOLS }
                }
            }

            HubPage.CATEGORY -> {
                val list = games.filter { it.category == category }
                HubListScreen(category, categories.first { it.first == category }.second) {
                    item { BackCard("返回分类") { page = HubPage.HOME } }
                    items(list) { game ->
                        ExpressiveCard(game.title, game.subtitle, game.icon) {
                            selected = game
                            resume = false
                            if (game.modeChoice) page = HubPage.MODE else {
                                singlePlayer = true
                                page = HubPage.GAME
                            }
                        }
                    }
                }
            }

            HubPage.MODE -> HubListScreen(selected.title, "选择本局模式") {
                item { BackCard("返回游戏列表") { page = HubPage.CATEGORY } }
                item {
                    ExpressiveCard("单人模式", "对战手表 · 使用强化 AI", "AI") {
                        singlePlayer = true
                        page = HubPage.GAME
                    }
                }
                item {
                    ExpressiveCard("双人模式", "同一块手表轮流 / 分区操作", "2P") {
                        singlePlayer = false
                        page = HubPage.GAME
                    }
                }
            }

            HubPage.GAME -> key("${selected.mode}-$singlePlayer-$resume") {
                GameHost(
                    game = selected,
                    singlePlayer = singlePlayer,
                    resume = resume,
                    onExit = {
                        onGameViewChanged(null)
                        resume = false
                        page = if (selected.modeChoice) HubPage.MODE else HubPage.CATEGORY
                    },
                    onGameViewChanged = onGameViewChanged,
                )
            }

            HubPage.TOOLS -> HubListScreen("工具与关于", "不占用游戏主列表空间") {
                item { BackCard("返回首页") { page = HubPage.HOME } }
                item { ExpressiveCard("战绩记录", "查看每款游戏的游玩与最佳成绩", "★") { page = HubPage.RECORDS } }
                item { ExpressiveCard("支持作者", "查看黑白君的微信赞赏码", "♥") { page = HubPage.SUPPORT } }
                item {
                    ExpressiveCard("手机反馈", "在配对手机打开反馈主页", "↗") {
                        val sent = PhoneLinkOpener(context).openOnPhone("https://www.coolapk.com/u/22532694")
                        Toast.makeText(context, if (sent) "已请求在手机打开" else "未能连接配对手机", Toast.LENGTH_SHORT).show()
                    }
                }
                item { ExpressiveCard("关于", "版本与设计说明", "i") { page = HubPage.ABOUT } }
            }

            HubPage.RECORDS -> HubListScreen("战绩记录", "游玩次数 · 胜利/完成 · 最佳") {
                item { BackCard("返回工具") { page = HubPage.TOOLS } }
                items(games) { game ->
                    val plays = prefs.getInt("stat_play_${game.mode}", 0)
                    val wins = prefs.getInt("stat_win_${game.mode}", 0)
                    val best = prefs.getInt("stat_best_${game.mode}", 0)
                    InfoCard("${game.icon}  ${game.title}", "游玩 $plays · 完成/胜 $wins · 最佳 ${if (best == 0) "—" else best}")
                }
            }

            HubPage.SUPPORT -> HubListScreen("支持作者", "感谢支持 · 黑白君") {
                item { BackCard("返回工具") { page = HubPage.TOOLS } }
                item { SupportAuthorCard() }
            }

            HubPage.ABOUT -> HubListScreen("关于", "腕上小游戏 · v7.0.2") {
                item { BackCard("返回工具") { page = HubPage.TOOLS } }
                item { InfoCard("Material 3 Expressive", "首页、分类和模式选择使用 Wear Compose Material 3；关闭高开销卡片形变，游戏画布保留低延迟 Canvas。") }
                item { InfoCard("适配策略", "卡片铺满可用宽度；圆屏使用更大的安全边距，方屏保留完整内容，减少实时形变计算。") }
                item { InfoCard("游戏库", "主入口扩展至 30 款；移除猜拳与骰子对决，新增俄罗斯方块、跳跃小鸟、打地鼠、21 点、推箱子、像素跑酷、三道闪避和叠塔。") }
            }
        }
    }
}

@Composable
private fun HubListScreen(
    title: String,
    subtitle: String,
    content: androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope.() -> Unit,
) {
    val state = rememberTransformingLazyColumnState()
    val isRound = LocalConfiguration.current.isScreenRound
    ScreenScaffold(
        scrollState = state,
        contentPadding = PaddingValues(
            horizontal = if (isRound) 24.dp else 10.dp,
            vertical = 8.dp,
        ),
    ) { padding ->
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = padding,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item {
                ListHeader(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope.ExpressiveCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("$icon  $title", style = MaterialTheme.typography.titleSmall)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope.BackCard(
    label: String,
    onClick: () -> Unit,
) = ExpressiveCard(label, "", "‹", onClick)

@Composable
private fun androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope.InfoCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(body, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope.SupportAuthorCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("请黑白君喝杯饮料", style = MaterialTheme.typography.titleSmall)
            Image(
                painter = painterResource(R.drawable.support_author_qr),
                contentDescription = "黑白君微信赞赏码",
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Fit,
            )
            Text(
                "如果这个小工具让你开心了一下，谢谢你的支持 ☺",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GameHost(
    game: GameDef,
    singlePlayer: Boolean,
    resume: Boolean,
    onExit: () -> Unit,
    onGameViewChanged: (GameHubView?) -> Unit,
) {
    val viewRef = remember { AtomicReference<GameHubView?>(null) }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                GameHubView(context).also { view ->
                    view.keepScreenOn = true
                    viewRef.set(view)
                    view.setHostListener(object : GameHubView.HostListener {
                        override fun onExitToHub() = onExit()
                    })
                    onGameViewChanged(view)
                    view.post {
                        if (resume) view.resumeSavedGameExternal()
                        else view.openGame(game.mode, singlePlayer)
                    }
                }
            },
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            viewRef.get()?.persistCurrentState()
            viewRef.set(null)
            onGameViewChanged(null)
        }
    }
}

private fun categoryIcon(name: String): String = when (name) {
    "棋盘对战" -> "♟"
    "益智解谜" -> "◇"
    "街机经典" -> "◉"
    else -> "⚡"
}
