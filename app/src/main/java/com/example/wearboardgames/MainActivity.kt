package com.example.wearboardgames

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.dynamicColorScheme
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import java.util.concurrent.atomic.AtomicReference

class MainActivity : ComponentActivity() {
    private var activeGameView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLog.i("MainActivity onCreate; version=8.1.0")
        window.navigationBarColor = Color.BLACK
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        setContent {
            val context = LocalContext.current
            val prefs = remember { AppSettings.prefs(context) }
            val configuration = LocalConfiguration.current
            var themeRevision by remember { mutableStateOf(0) }
            val useDynamic = prefs.getBoolean(AppSettings.KEY_DYNAMIC_COLOR, true)
            val scheme = remember(context, configuration, useDynamic, themeRevision) {
                if (useDynamic) dynamicColorScheme(context) else null
            }
            MaterialTheme(colorScheme = scheme ?: ColorScheme()) {
                WearGamesApp(
                    onGameViewChanged = { activeGameView = it },
                    onThemeSettingChanged = { themeRevision++ },
                )
            }
        }
    }

    override fun onPause() {
        AppLog.i("MainActivity onPause; activeView=${activeGameView?.javaClass?.simpleName ?: "none"}")
        when (val view = activeGameView) {
            is BaseGameView -> view.persistCurrentState()
        }
        super.onPause()
    }
}

private enum class HubPage {
    HOME, CATEGORY, MODE, GAME, TOOLS, RECORDS, FAVORITES, RECENT, INDEX, SETTINGS, SUPPORT, ABOUT
}

@Composable
private fun WearGamesApp(
    onGameViewChanged: (View?) -> Unit,
    onThemeSettingChanged: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { AppSettings.prefs(context) }
    var page by remember { mutableStateOf(HubPage.HOME) }
    var category by remember { mutableStateOf(categories.first().first) }
    var selected by remember { mutableStateOf(games.first()) }
    var singlePlayer by remember { mutableStateOf(true) }
    var resume by remember { mutableStateOf(false) }
    var prefsRevision by remember { mutableStateOf(0) }
    var gameBackConfirmUntil by remember { mutableLongStateOf(0L) }
    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> prefsRevision++ }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun refreshPrefs() { prefsRevision++ }
    fun enterGame(game: GameDef, asResume: Boolean = false, forceSingle: Boolean? = null) {
        selected = game
        resume = asResume
        AppLog.i("enterGame mode=${game.mode} title=${game.title} resume=$asResume")
        if (asResume) singlePlayer = prefs.getBoolean("save_ai", true)
        forceSingle?.let { singlePlayer = it }
        gameBackConfirmUntil = 0L
        pushRecent(prefs, game.mode)
        refreshPrefs()
        page = HubPage.GAME
    }
    fun openGame(game: GameDef) {
        selected = game
        resume = false
        if (game.modeChoice) page = HubPage.MODE else enterGame(game, forceSingle = true)
    }
    fun returnFromGame() {
        AppLog.i("returnFromGame mode=${selected.mode} title=${selected.title}")
        onGameViewChanged(null)
        resume = false
        gameBackConfirmUntil = 0L
        page = if (selected.modeChoice) HubPage.MODE else HubPage.CATEGORY
    }

    BackHandler(enabled = page != HubPage.HOME) {
        if (page == HubPage.GAME) {
            val now = SystemClock.elapsedRealtime()
            if (now <= gameBackConfirmUntil) {
                returnFromGame()
            } else {
                gameBackConfirmUntil = now + 1_400L
                Toast.makeText(context, "再按一次返回退出游戏", Toast.LENGTH_SHORT).show()
            }
            return@BackHandler
        }
        page = when (page) {
            HubPage.MODE -> HubPage.CATEGORY
            HubPage.CATEGORY, HubPage.TOOLS, HubPage.FAVORITES, HubPage.RECENT, HubPage.INDEX -> HubPage.HOME
            HubPage.RECORDS, HubPage.SETTINGS, HubPage.SUPPORT, HubPage.ABOUT -> HubPage.TOOLS
            else -> HubPage.HOME
        }
    }

    AppScaffold(timeText = { if (page != HubPage.GAME) TimeText() }) {
        key(page) {
            val motionEnabled = AppSettings.animations(prefs) && !AppSettings.saver(prefs)
            val opacity = remember { Animatable(if (motionEnabled && page != HubPage.GAME) .75f else 1f) }
            LaunchedEffect(Unit) { opacity.animateTo(1f, tween(120)) }
            Box(Modifier.fillMaxSize().graphicsLayer { alpha = opacity.value }) {
            val targetPage = page
            @Suppress("UNUSED_EXPRESSION")
            prefsRevision
            when (targetPage) {
                HubPage.HOME -> HubListScreen(
                    title = "腕上小游戏",
                    subtitle = "${games.size} 款 · Wear Material 3 · 离线可玩",
                ) {
                    if (prefs.getBoolean("has_save", false)) {
                        val last = prefs.getInt("last_mode", -1)
                        games.firstOrNull { it.mode == last }?.let { saved ->
                            item(key = "resume-${saved.mode}") {
                                ExpressiveCard("继续 · ${saved.title}", "恢复上次自动存档", "▶") {
                                    enterGame(saved, asResume = true)
                                }
                            }
                        }
                    }
                    val recent = readRecent(prefs).mapNotNull { id -> games.firstOrNull { it.mode == id } }
                    if (recent.isNotEmpty()) {
                        item(key = "recent") {
                            ExpressiveCard("最近游戏", recent.take(3).joinToString(" · ") { it.title }, "↺") {
                                page = HubPage.RECENT
                            }
                        }
                    }
                    val favoriteCount = readFavorites(prefs).size
                    item(key = "favorites") {
                        ExpressiveCard("收藏", if (favoriteCount == 0) "长按游戏卡片即可收藏" else "$favoriteCount 款常玩游戏", "♥") {
                            page = HubPage.FAVORITES
                        }
                    }
                    item(key = "index") {
                        ExpressiveCard("查找游戏", "按名称浏览全部 ${games.size} 款", "A-Z") { page = HubPage.INDEX }
                    }
                    items(items = categories, key = { it.first }, contentType = { "category" }) { (name, desc) ->
                        val count = games.count { it.category == name }
                        ExpressiveCard(name, "$count 款 · $desc", categoryIcon(name)) {
                            category = name
                            page = HubPage.CATEGORY
                        }
                    }
                    item(key = "tools") {
                        ExpressiveCard("设置与关于", "设置、战绩、支持与版本信息", "⋯") { page = HubPage.TOOLS }
                    }
                }

                HubPage.CATEGORY -> {
                    val list = remember(category) { games.filter { it.category == category } }
                    HubListScreen(
                        title = category,
                        subtitle = "${list.size} 款 · ${categories.first { it.first == category }.second}",
                        edgeLabel = "返回",
                        onEdgeClick = { page = HubPage.HOME },
                    ) {
                        items(items = list, key = { it.mode }, contentType = { "game" }) { game ->
                            val favorite = readFavorites(prefs).contains(game.mode)
                            GameCard(game, favorite, onClick = { openGame(game) }) {
                                toggleFavorite(prefs, game.mode)
                                refreshPrefs()
                            }
                        }
                    }
                }

                HubPage.MODE -> HubListScreen(
                    title = selected.title,
                    subtitle = if (readFavorites(prefs).contains(selected.mode)) "已收藏 · 选择本局模式" else "选择本局模式",
                    edgeLabel = "返回",
                    onEdgeClick = { page = HubPage.CATEGORY },
                ) {
                    item(key = "favorite-mode") {
                        val fav = readFavorites(prefs).contains(selected.mode)
                        ExpressiveCard(if (fav) "★ 已收藏" else "☆ 加入收藏", "常玩游戏会出现在首页收藏入口", "♥") {
                            toggleFavorite(prefs, selected.mode); refreshPrefs()
                        }
                    }
                    item(key = "single") {
                        ExpressiveCard("单人模式", "与手表 AI 对战", "AI") {
                            singlePlayer = true; enterGame(selected)
                        }
                    }
                    item(key = "double") {
                        ExpressiveCard("双人模式", "同一块手表轮流 / 分区操作", "2P") {
                            singlePlayer = false; enterGame(selected)
                        }
                    }
                }

                HubPage.GAME -> key("${selected.mode}-$singlePlayer-$resume") {
                    GameHost(
                        game = selected,
                        singlePlayer = singlePlayer,
                        resume = resume,
                        onExit = ::returnFromGame,
                        onGameViewChanged = onGameViewChanged,
                    )
                }

                HubPage.TOOLS -> HubListScreen(
                    title = "设置与关于",
                    subtitle = "统一偏好、战绩和应用信息",
                    edgeLabel = "返回",
                    onEdgeClick = { page = HubPage.HOME },
                ) {
                    item(key = "settings") { ExpressiveCard("设置", "触觉、声音、动画、性能和左右手", "⚙") { page = HubPage.SETTINGS } }
                    item(key = "records") { ExpressiveCard("战绩记录", "游玩时长、胜负、连胜与最佳成绩", "★") { page = HubPage.RECORDS } }
                    item(key = "support") { ExpressiveCard("支持作者", "查看黑白君的微信赞赏码", "♥") { page = HubPage.SUPPORT } }
                    item(key = "feedback") {
                        ExpressiveCard("手机反馈", "在配对手机打开反馈主页", "↗") {
                            val sent = PhoneLinkOpener(context).openOnPhone("https://www.coolapk.com/u/22532694")
                            Toast.makeText(context, if (sent) "已请求在手机打开" else "未能连接配对手机", Toast.LENGTH_SHORT).show()
                        }
                    }
                    item(key = "about") { ExpressiveCard("关于", "v8.1.0 · ${games.size} 款游戏", "i") { page = HubPage.ABOUT } }
                }

                HubPage.RECORDS -> HubListScreen(
                    title = "战绩记录",
                    subtitle = "按玩法显示有意义的统计",
                    edgeLabel = "返回",
                    onEdgeClick = { page = HubPage.TOOLS },
                ) {
                    items(items = games, key = { it.mode }, contentType = { "record" }) { game ->
                        val plays = prefs.getInt("stat_play_${game.mode}", 0)
                        val wins = prefs.getInt("stat_win_${game.mode}", 0)
                        val losses = prefs.getInt("stat_loss_${game.mode}", 0)
                        val draws = prefs.getInt("stat_draw_${game.mode}", 0)
                        val streak = prefs.getInt("stat_streak_${game.mode}", 0)
                        val totalTime = prefs.getLong("stat_time_${game.mode}", 0L)
                        val best = readBestMetric(prefs, game.mode)
                        InfoCard(
                            "${game.icon}  ${game.title}",
                            formatRecordSummary(prefs, game.mode, plays, wins, losses, draws, streak, totalTime, best),
                        )
                    }
                }

                HubPage.FAVORITES -> {
                    val ids = readFavorites(prefs)
                    val list = games.filter { ids.contains(it.mode) }
                    HubListScreen("收藏", if (list.isEmpty()) "在分类页长按游戏卡片收藏" else "${list.size} 款 · 长按可取消", "返回", { page = HubPage.HOME }) {
                        if (list.isEmpty()) item(key = "empty") { InfoCard("还没有收藏", "打开任意分类，长按游戏卡片即可加入收藏。") }
                        items(items = list, key = { it.mode }) { game ->
                            GameCard(game, true, onClick = { openGame(game) }) { toggleFavorite(prefs, game.mode); refreshPrefs() }
                        }
                    }
                }

                HubPage.RECENT -> {
                    val list = readRecent(prefs).mapNotNull { id -> games.firstOrNull { it.mode == id } }
                    HubListScreen("最近游戏", if (list.isEmpty()) "还没有游玩记录" else "最近 ${list.size} 款", "返回", { page = HubPage.HOME }) {
                        items(items = list, key = { it.mode }) { game ->
                            GameCard(game, readFavorites(prefs).contains(game.mode), onClick = { openGame(game) }) {
                                toggleFavorite(prefs, game.mode); refreshPrefs()
                            }
                        }
                    }
                }

                HubPage.INDEX -> {
                    val list = remember { games.sortedBy { it.title } }
                    HubListScreen("查找游戏", "按名称浏览 · 无需小屏键盘", "返回", { page = HubPage.HOME }) {
                        items(items = list, key = { it.mode }) { game ->
                            GameCard(game, readFavorites(prefs).contains(game.mode), onClick = { openGame(game) }) {
                                toggleFavorite(prefs, game.mode); refreshPrefs()
                            }
                        }
                    }
                }

                HubPage.SETTINGS -> HubListScreen(
                    title = "设置",
                    subtitle = "所有实时游戏统一读取",
                    edgeLabel = "返回",
                    onEdgeClick = { page = HubPage.TOOLS },
                ) {
                    item(key = "dynamic-$prefsRevision") {
                        val on = prefs.getBoolean(AppSettings.KEY_DYNAMIC_COLOR, true)
                        ExpressiveCard("动态颜色 · ${if (on) "开" else "关"}", "跟随手表系统配色", "◐") {
                            prefs.edit().putBoolean(AppSettings.KEY_DYNAMIC_COLOR, !on).apply(); refreshPrefs(); onThemeSettingChanged()
                        }
                    }
                    item(key = "haptics-$prefsRevision") {
                        val on = prefs.getBoolean(AppSettings.KEY_HAPTICS, true)
                        ExpressiveCard("触觉反馈 · ${if (on) "开" else "关"}", "点击、得分、碰撞统一管理", "〰") {
                            prefs.edit().putBoolean(AppSettings.KEY_HAPTICS, !on).apply(); refreshPrefs()
                        }
                    }
                    item(key = "sound-$prefsRevision") {
                        val on = prefs.getBoolean(AppSettings.KEY_SOUND, false)
                        ExpressiveCard("声音 · ${if (on) "开" else "关"}", "默认静音，开启后只播放短提示音", "♪") {
                            prefs.edit().putBoolean(AppSettings.KEY_SOUND, !on).apply(); refreshPrefs()
                        }
                    }
                    item(key = "animations-$prefsRevision") {
                        val on = prefs.getBoolean(AppSettings.KEY_ANIMATIONS, true)
                        ExpressiveCard("动画效果 · ${if (on) "开" else "关"}", "落子、按钮和游戏结果", "✦") {
                            prefs.edit().putBoolean(AppSettings.KEY_ANIMATIONS, !on).apply(); refreshPrefs()
                        }
                    }
                    item(key = "hand-$prefsRevision") {
                        val left = prefs.getBoolean(AppSettings.KEY_LEFT_HANDED, false)
                        ExpressiveCard("操作手 · ${if (left) "左手" else "右手"}", "所有公共重开 / 菜单控制会交换到顺手一侧", "↔") {
                            prefs.edit().putBoolean(AppSettings.KEY_LEFT_HANDED, !left).apply(); refreshPrefs()
                        }
                    }
                    item(key = "confirm-move-$prefsRevision") {
                        val on = prefs.getBoolean(AppSettings.KEY_MOVE_CONFIRM, true)
                        ExpressiveCard("棋类落子确认 · ${if (on) "开" else "关"}", "象棋、五子棋、黑白棋、四子棋先预览，再点一次确认", "✓") {
                            prefs.edit().putBoolean(AppSettings.KEY_MOVE_CONFIRM, !on).apply(); refreshPrefs()
                        }
                    }
                    item(key = "perf-$prefsRevision") {
                        val current = prefs.getString(AppSettings.KEY_PERFORMANCE, "balanced") ?: "balanced"
                        val label = when (current) { "smooth" -> "流畅"; "saver" -> "省电"; else -> "平衡" }
                        ExpressiveCard("性能模式 · $label", "流畅跟随屏幕刷新；平衡降负载；省电进一步限帧", "⚡") {
                            val next = when (current) { "balanced" -> "smooth"; "smooth" -> "saver"; else -> "balanced" }
                            prefs.edit().putString(AppSettings.KEY_PERFORMANCE, next).apply(); refreshPrefs()
                        }
                    }
                }

                HubPage.SUPPORT -> HubListScreen(
                    title = "支持作者",
                    subtitle = "感谢支持 · 黑白君",
                    edgeLabel = "返回",
                    onEdgeClick = { page = HubPage.TOOLS },
                ) { item(key = "qr") { SupportAuthorCard() } }

                HubPage.ABOUT -> HubListScreen(
                    title = "关于",
                    subtitle = "腕上小游戏 · v8.1.0",
                    edgeLabel = "返回",
                    onEdgeClick = { page = HubPage.TOOLS },
                ) {
                    item(key = "m3") { InfoCard("Wear Material 3", "首页、分类、设置和战绩继续使用 TransformingLazyColumn、动态颜色与 EdgeButton。") }
                    item(key = "arch") { InfoCard("模块化游戏", "Tetris、Snake、Flappy、Runner、Pong、Breakout、Bounce、Pinball、Dodge、Stack、Simon 已使用独立实时 View；核心棋盘游戏采用 View + Engine。") }
                    item(key = "perf-$prefsRevision") { InfoCard("性能策略", "流畅 / 平衡 / 省电采用不同实时刷新预算，静态状态停止连续重绘；Release 启用 R8、资源压缩和 Baseline Profile。") }
                    item(key = "games") { InfoCard("${games.size} 款游戏", "棋类、经典街机和益智游戏。") }
                    item(key = "input") { InfoCard("防误触", "游戏区与系统控制区分离；重开、返回以及易误触棋类落子均支持二次确认。") }
                }
            }
            }
        }
    }
}

@Composable
private fun HubListScreen(
    title: String,
    subtitle: String,
    edgeLabel: String? = null,
    onEdgeClick: (() -> Unit)? = null,
    content: androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope.() -> Unit,
) {
    val state = rememberTransformingLazyColumnState()
    val isRound = LocalConfiguration.current.isScreenRound

    @Composable
    fun ListBody(scaffoldPadding: PaddingValues) {
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            flingBehavior = TransformingLazyColumnDefaults.snapFlingBehavior(state),
            rotaryScrollableBehavior = RotaryScrollableDefaults.snapBehavior(state),
            contentPadding = PaddingValues(
                start = if (isRound) 18.dp else 10.dp,
                end = if (isRound) 18.dp else 10.dp,
                top = maxOf(scaffoldPadding.calculateTopPadding(), 6.dp),
                bottom = maxOf(scaffoldPadding.calculateBottomPadding(), 8.dp),
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            item(key = "header-$title") {
                val transformationSpec = rememberTransformationSpec()
                ListHeader(
                    modifier = Modifier
                        .transformedHeight(this, transformationSpec)
                        .minimumVerticalContentPadding(
                            ListHeaderDefaults.minimumTopListContentPadding,
                            ListHeaderDefaults.minimumBottomListContentPadding,
                        )
                        .fillMaxWidth(),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            content()
        }
    }

    if (edgeLabel != null && onEdgeClick != null) {
        ScreenScaffold(scrollState = state, edgeButton = { EdgeButton(onClick = onEdgeClick) { Text(edgeLabel) } }) { padding -> ListBody(padding) }
    } else {
        ScreenScaffold(scrollState = state) { padding -> ListBody(padding) }
    }
}

@Composable
private fun TransformingLazyColumnItemScope.ExpressiveCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
) {
    val transformationSpec = rememberTransformationSpec()
    var lastClickAt by remember { mutableLongStateOf(0L) }
    Card(
        onClick = {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickAt > 280L) { lastClickAt = now; onClick() }
        },
        modifier = Modifier
            .transformedHeight(this, transformationSpec)
            .minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding)
            .fillMaxWidth(),
        transformation = SurfaceTransformation(transformationSpec),
    ) { CardContents(title, subtitle, icon) }
}

@Composable
private fun TransformingLazyColumnItemScope.GameCard(
    game: GameDef,
    favorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val transformationSpec = rememberTransformationSpec()
    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        onLongClickLabel = "收藏或取消收藏",
        modifier = Modifier
            .transformedHeight(this, transformationSpec)
            .minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding)
            .fillMaxWidth(),
        transformation = SurfaceTransformation(transformationSpec),
    ) {
        CardContents(
            if (favorite) "★ ${game.title}" else game.title,
            game.subtitle,
            game.icon,
        )
    }
}

@Composable
private fun CardContents(title: String, subtitle: String, icon: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(icon, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TransformingLazyColumnItemScope.InfoCard(title: String, body: String) {
    val transformationSpec = rememberTransformationSpec()
    Card(
        modifier = Modifier
            .transformedHeight(this, transformationSpec)
            .minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding)
            .fillMaxWidth(),
        transformation = SurfaceTransformation(transformationSpec),
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(body, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TransformingLazyColumnItemScope.SupportAuthorCard() {
    val transformationSpec = rememberTransformationSpec()
    Card(
        modifier = Modifier
            .transformedHeight(this, transformationSpec)
            .minimumVerticalContentPadding(CardDefaults.minimumVerticalListContentPadding)
            .fillMaxWidth(),
        transformation = SurfaceTransformation(transformationSpec),
    ) {
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
            Text("如果这个小工具让你开心了一下，谢谢你的支持 ☺", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun GameHost(
    game: GameDef,
    singlePlayer: Boolean,
    resume: Boolean,
    onExit: () -> Unit,
    onGameViewChanged: (View?) -> Unit,
) {
    val viewRef = remember { AtomicReference<View?>(null) }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                fun attach(view: BaseGameView): View {
                    view.keepScreenOn = true
                    viewRef.set(view)
                    view.setHostListener(object : BaseGameView.HostListener { override fun onExitToHub() = onExit() })
                    onGameViewChanged(view)
                    view.post { if (resume) view.resumeSavedGameExternal(game.mode, singlePlayer) else view.openGame(game.mode, singlePlayer) }
                    return view
                }
                when {
                    TetrisView.supportsMode(game.mode) -> attach(TetrisView(context))
                    SnakeView.supportsMode(game.mode) -> attach(SnakeView(context))
                    FlappyView.supportsMode(game.mode) -> attach(FlappyView(context))
                    RunnerView.supportsMode(game.mode) -> attach(RunnerView(context))
                    PongView.supportsMode(game.mode) -> attach(PongView(context))
                    BreakoutView.supportsMode(game.mode) -> attach(BreakoutView(context))
                    BounceView.supportsMode(game.mode) -> attach(BounceView(context))
                    PinballView.supportsMode(game.mode) -> attach(PinballView(context))
                    LaneDodgeView.supportsMode(game.mode) -> attach(LaneDodgeView(context))
                    StackView.supportsMode(game.mode) -> attach(StackView(context))
                    SimonView.supportsMode(game.mode) -> attach(SimonView(context))
                    XiangqiView.supportsMode(game.mode) -> attach(XiangqiView(context))
                    ChessView.supportsMode(game.mode) -> attach(ChessView(context))
                    Game2048View.supportsMode(game.mode) -> attach(Game2048View(context))
                    GomokuView.supportsMode(game.mode) -> attach(GomokuView(context))
                    Connect4View.supportsMode(game.mode) -> attach(Connect4View(context))
                    ReversiView.supportsMode(game.mode) -> attach(ReversiView(context))
                    SudokuView.supportsMode(game.mode) -> attach(SudokuView(context))
                    MinesView.supportsMode(game.mode) -> attach(MinesView(context))
                    MazeView.supportsMode(game.mode) -> attach(MazeView(context))
                    SokobanView.supportsMode(game.mode) -> attach(SokobanView(context))
                    ClassicMiniGameView.supportsMode(game.mode) -> attach(ClassicMiniGameView(context))
                    else -> error("Unsupported game mode: ${game.mode}")
                }
            },
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            when (val view = viewRef.get()) {
                is BaseGameView -> view.persistCurrentState()
            }
            viewRef.get()?.keepScreenOn = false
            viewRef.set(null)
            onGameViewChanged(null)
        }
    }
}

private fun categoryIcon(name: String): String = when (name) {
    "棋盘对战" -> "♟"
    "益智解谜" -> "◇"
    "街机经典" -> "◉"
    "反应训练" -> "⌁"
    "运动竞技" -> "●"
    else -> "⚡"
}

private fun usesLowerMetric(mode: Int): Boolean = mode == GameModes.SLIDE_PUZZLE ||
    mode == GameModes.LIGHTS_OUT || mode == GameModes.MEMORY_MATCH ||
    mode == GameModes.MAZE || mode == GameModes.NUMBER_TAP || mode == GameModes.SOKOBAN

private fun readBestMetric(prefs: android.content.SharedPreferences, mode: Int): Int =
    prefs.getInt(if (usesLowerMetric(mode)) "stat_low_$mode" else "stat_best_$mode", 0)

private fun formatBestMetric(mode: Int, best: Int): String {
    if (best <= 0) return "—"
    return when (mode) {
        GameModes.NUMBER_TAP -> String.format(java.util.Locale.US, "%.1f 秒", best / 1000f)
        GameModes.SLIDE_PUZZLE, GameModes.LIGHTS_OUT, GameModes.MAZE, GameModes.SOKOBAN -> "$best 步"
        GameModes.MEMORY_MATCH -> "$best 次"
        GameModes.SIMON -> "$best 轮"
        else -> best.toString()
    }
}


private fun formatRecordSummary(
    prefs: android.content.SharedPreferences, mode: Int, plays: Int, wins: Int, losses: Int, draws: Int,
    streak: Int, totalTime: Long, best: Int,
): String {
    val outcome = if (mode in setOf(GameModes.XIANGQI, GameModes.CHESS, GameModes.GOMOKU, GameModes.TICTACTOE, GameModes.CONNECT4, GameModes.REVERSI, GameModes.NIM, GameModes.ROCK_PAPER_SCISSORS, GameModes.BLACKJACK))
        "胜 $wins · 负 $losses · 和 $draws" else "完成/胜 $wins · 失败 $losses"
    val specific = when (mode) {
        GameModes.BLOCK_DROP -> "最高等级 ${GameStats.readMetric(prefs, mode, "level")} · 最多消行 ${GameStats.readMetric(prefs, mode, "lines")}"
        GameModes.SNAKE -> "最长长度 ${GameStats.readMetric(prefs, mode, "length")}"
        GameModes.FLAPPY -> "最多通过 ${GameStats.readMetric(prefs, mode, "pipes")} 根管道"
        else -> "最佳 ${formatBestMetric(mode, best)}"
    }
    return "游玩 $plays · $outcome · 连续 $streak\n时长 ${formatDuration(totalTime)} · $specific"
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "—"
    val totalSeconds = ms / 1000
    return if (totalSeconds < 60) "${totalSeconds}秒" else "${totalSeconds / 60}分${totalSeconds % 60}秒"
}

private fun readFavorites(prefs: android.content.SharedPreferences): Set<Int> =
    prefs.getString("favorite_modes", "").orEmpty().split(',').mapNotNull { it.toIntOrNull() }.toSet()

private fun toggleFavorite(prefs: android.content.SharedPreferences, mode: Int) {
    val values = readFavorites(prefs).toMutableSet()
    if (!values.add(mode)) values.remove(mode)
    prefs.edit().putString("favorite_modes", values.sorted().joinToString(",")).apply()
}

private fun readRecent(prefs: android.content.SharedPreferences): List<Int> =
    prefs.getString("recent_modes", "").orEmpty().split(',').mapNotNull { it.toIntOrNull() }.distinct().take(8)

private fun pushRecent(prefs: android.content.SharedPreferences, mode: Int) {
    val values = readRecent(prefs).toMutableList()
    values.remove(mode)
    values.add(0, mode)
    prefs.edit().putString("recent_modes", values.take(8).joinToString(",")).apply()
}
