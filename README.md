# 腕上小游戏 Wear OS · v8.0.2

面向 Wear OS 小屏/圆屏的离线小游戏合集。v8.0.2 已完成从旧单体 `GameHubView` 到模块化架构的迁移，当前 **81 款游戏 / 6 个分类**。首页、分类、收藏、最近、查找、设置与战绩使用 Wear Compose Material 3；复杂实时游戏使用独立 Canvas View，棋盘游戏采用 View + Engine，短局按类型共享轻量 View。

## v8.0.2 重点

- 新增稳定 Logcat 标签 `WearBoardGames` 和 `scripts/capture_wear_logcat.sh`，可直接从连接的手表抓取应用 PID 日志与 crash buffer。
- `PhoneLinkOpener` 现在会记录 RemoteActivityHelper 的异步成功/失败，避免“请求已提交”掩盖真正的远端失败。
- GitHub Actions 升级到 Node 24 代 action（checkout v7 / setup-java v6 / setup-gradle v6 / upload-artifact v6）。
- `GameHubView.java` 已从运行代码删除，不再存在大一统游戏 Hub。
- 独立实时 View：Tetris、Snake、Flappy、Runner、Pong、Breakout、Bounce、Pinball、Lane Dodge、Stack、Simon。
- 棋盘/益智 View + Engine：2048、象棋、五子棋、四子棋、黑白棋、数独、扫雷、迷宫、推箱子。
- 统一基础设施：`BaseGameView`、`GameSaveManager`、`GameStats`、`HapticsManager`、`SoundManager`、`AppSettings`。
- 首页支持继续游戏、收藏、最近游戏、A-Z 查找、分类、战绩、设置。
- 设置支持动态配色、触觉、声音、动画、左右手、棋类落子二次确认，以及流畅/平衡/省电模式。
- 统一双确认“重开 / 返回”，系统 Back 也需二次确认；系统控制区和游戏热区分离。
- Tetris 加入 NEXT、HOLD、软降、硬降、Combo、等级、暂停、触觉与完整状态存档；暂停按钮已与真实触控命中区统一，暂停时不会再误触成右移。
- 2048 按 Gabriele Cirulli 官方 2048 的 MIT 视觉规范原生适配 Wear：官方配色、SCORE/BEST、滑动/出现/合并动效；许可见 `THIRD_PARTY_NOTICES.md`。
- v7.x 单槽存档可迁移到 v8，Tetris 使用可单测的 v4 状态编解码。
- CI 分两阶段执行：`assembleDebug → lintRelease → testReleaseUnitTest`，随后在 Wear OS 5.1 模拟器采集 Baseline Profile 并执行 `assembleRelease`；Release 开启 R8 与资源压缩。

## 81 款游戏

分类：

- 棋盘对战：象棋、五子棋、井字棋、四子棋、黑白棋、Nim 等。
- 益智解谜：2048、数字华容道、熄灯、扫雷、配对、迷宫、数独、推箱子、目标和、二进制开关等。
- 街机经典：俄罗斯方块、贪吃蛇、Flappy、Pong、Breakout、Bounce、Pinball、Runner、三道闪避、叠塔、自由闪避、换道冲刺、落块穿隙等。
- 轻松挑战：Simon、色块猎手、快速心算、21 点、石头剪刀布、节拍、平衡等。
- 反应训练：精准计时、路径记忆、Stroop、移动靶心、镜像点击、数字记忆等。
- 运动竞技：迷你高尔夫、空气曲棍球、投篮、点球、飞镖、保龄。

完整清单以 `GameCatalog.kt` 为准；所有 mode ID 在目录检查中唯一，并且每个游戏恰好命中一个运行路由。

## 架构

```text
MainActivity.kt          Wear Compose Material 3 外壳 / 导航 / 收藏 / 最近 / 设置 / 战绩
GameCatalog.kt           81 款游戏目录
BaseGameView.java        独立 Canvas 游戏公共 HUD / 输入 / 结果 / 控制 / 统计
├── TetrisView.java
├── SnakeView.java
├── FlappyView.java
├── RunnerView.java
├── PongView.java
├── BreakoutView.java
├── BounceView.java
├── PinballView.java
├── LaneDodgeView.java
├── StackView.java
└── SimonView.java

View + Engine
├── Game2048View / Game2048Engine
├── XiangqiView / XiangqiEngine
├── GomokuView / GomokuEngine
├── Connect4View / Connect4Engine
├── ReversiView / ReversiEngine
├── SudokuView / SudokuEngine
├── MinesView / MinesEngine
├── MazeView / MazeEngine
└── SokobanView / SokobanEngine

物理源码目录
├── games/arcade
├── games/board
├── games/puzzle
└── games/micro

Grouped short games
├── MicroGameView.java
├── PuzzleMiniView.java
├── ClassicMiniGameView.java
├── ExpansionGameView.java
└── V8MiniGameView.java
```

## 存档与战绩

`GameSaveManager` 使用版本化单槽结构：`save_version / game_id(last_mode) / timestamp / payload / save_ai`。v7.x 旧格式由 `LegacySaveMigrator` 兼容。Tetris v4 存档通过 `TetrisStateCodec` 保存完整状态；棋盘 Engine 均有序列化/恢复测试。

`GameStats` 统一记录游玩次数、胜/负/和、连续纪录、总游戏时间、最近成绩与 best/lower-best 指标；Tetris 额外记录最高等级/最多消行，Snake 记录最长长度，Flappy 记录最多通过管道。不同游戏在 UI 中只显示有意义的指标。

## 性能策略

- “流畅”跟随显示 VSYNC；“平衡”使用约 24ms 帧预算；“省电”使用约 50ms 帧预算。
- 静态棋盘不持续刷新，只在输入/计时事件发生时重绘。
- Tetris、Snake、Breakout、Runner、Flappy、Pong、Bounce、Lane Dodge、Stack、Pinball、Simon 等高频路径复用绘制对象或使用标量碰撞，减少每帧临时对象与 GC 抖动。
- 动画关闭或省电模式会削减公共装饰效果，避免为视觉效果持续消耗 GPU/CPU。
- Release 开启 R8 + `shrinkResources`；保留 `baseline-prof.txt` 和 `profileinstaller 1.4.1`。

## 构建环境

推荐并由 CI 固定：

- JDK 17
- Gradle 8.7
- Android Gradle Plugin 8.6.1
- compileSdk / targetSdk 35，minSdk 30
- Kotlin 2.1.21
- Wear Compose Material 3 / Foundation 1.7.0
- Android Build Tools 35.0.0

本项目使用 GitHub Actions 的 `gradle/actions/setup-gradle` 固定 Gradle 8.7。Baseline Profile 由 CI 的 Wear OS 5.1 模拟器采集。

```bash
gradle lintRelease testReleaseUnitTest assembleRelease --no-daemon
```

Release APK：`app/build/outputs/apk/release/app-release.apk`。

## 自动检查

见 `VALIDATION.md`。项目内包含 JUnit/Kotlin 单测：规则/Engine round-trip、非法 payload、首击安全、v7 推箱子几何兼容、Tetris 完整状态 codec、v7 mode ID 锁定、旧存档迁移、81 款 Catalog/路由唯一性。当前交付环境实跑 31 个 Java + 2 个 Kotlin 测试全部通过（33/33）；Java 主源码另做 JDK 17 + 最小 Android API 桩编译检查。当前沙箱没有完整 Android SDK，因此不把静态检查冒充 APK 构建；CI 会在标准 Android 环境执行 Debug 编译、Lint、Unit Test，并在 Wear OS 5.1 模拟器生成 Baseline Profile 后执行 Release/R8 构建并上传 APK/Profile。
