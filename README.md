# 腕上小游戏 Wear OS · v8.0.0

面向 Wear OS 小屏/圆屏的离线小游戏合集。v8.0.0 已完成从旧单体 `GameHubView` 到模块化架构的迁移，当前 **81 款游戏 / 6 个分类**。首页、分类、收藏、最近、查找、设置与战绩使用 Wear Compose Material 3；复杂实时游戏使用独立 Canvas View，棋盘游戏采用 View + Engine，短局按类型共享轻量 View。

## v8.0.0 重点

- `GameHubView.java` 已从运行代码删除，不再存在大一统游戏 Hub。
- 独立实时 View：Tetris、Snake、Flappy、Runner、Pong、Breakout、Bounce、Pinball、Lane Dodge、Stack、Simon。
- 棋盘/益智 View + Engine：2048、象棋、五子棋、四子棋、黑白棋、数独、扫雷、迷宫、推箱子。
- 统一基础设施：`BaseGameView`、`GameSaveManager`、`GameStats`、`HapticsManager`、`SoundManager`、`AppSettings`。
- 首页支持继续游戏、收藏、最近游戏、A-Z 查找、分类、战绩、设置。
- 设置支持动态配色、触觉、声音、动画、左右手、流畅/均衡/省电模式。
- 统一双确认“重开 / 返回”，系统 Back 也需二次确认；系统控制区和游戏热区分离。
- Tetris 加入 NEXT、HOLD、软降、硬降、Combo、等级、暂停、触觉与完整状态存档。
- v7.x 单槽存档可迁移到 v8，Tetris 使用可单测的 v4 状态编解码。
- CI 执行 `lintRelease → testReleaseUnitTest → assembleRelease`，Release 开启 R8 与资源压缩。

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

Grouped short games
├── MicroGameView.java
├── PuzzleMiniView.java
├── ClassicMiniGameView.java
├── ExpansionGameView.java
└── V8MiniGameView.java
```

## 存档与战绩

`GameSaveManager` 使用版本化单槽结构：`save_version / game_id(last_mode) / timestamp / payload / save_ai`。v7.x 旧格式由 `LegacySaveMigrator` 兼容。Tetris v4 存档通过 `TetrisStateCodec` 保存完整状态；棋盘 Engine 均有序列化/恢复测试。

`GameStats` 统一记录游玩次数、胜/负、连续纪录、总游戏时间、最近成绩与 best/lower-best 指标。不同游戏在 UI 中只显示有意义的最佳指标。

## 性能策略

- 实时游戏使用 VSYNC 或受控帧调度；省电模式将高频刷新降至约 30Hz。
- 静态棋盘不持续刷新。
- 手机反馈等非首屏能力按需创建。
- Release 开启 R8 + `shrinkResources`。
- 保留 `baseline-prof.txt`，覆盖启动关键 App 类；`profileinstaller 1.4.1` 用于 release 安装。

## 构建环境

推荐并由 CI 固定：

- JDK 17
- Gradle 8.7
- Android Gradle Plugin 8.6.1
- compileSdk / targetSdk 35，minSdk 30
- Kotlin 2.1.21
- Wear Compose Material 3 / Foundation 1.6.2
- Android Build Tools 35.0.0

本项目原始压缩包不包含 Gradle Wrapper，因此 GitHub Actions 使用 `gradle/actions/setup-gradle` 固定 Gradle 8.7。Android Studio 导入后也可使用本机兼容 Gradle 8.7 构建。

```bash
gradle lintRelease testReleaseUnitTest assembleRelease --no-daemon
```

Release APK：`app/build/outputs/apk/release/app-release.apk`。

## 自动检查

见 `VALIDATION.md`。项目内包含 JUnit/Kotlin 单测：规则/Engine round-trip、非法 payload、首击安全、v7 推箱子几何兼容、Tetris 完整状态 codec、v7 mode ID 锁定、旧存档迁移、81 款 Catalog/路由唯一性。当前交付环境实跑 28 个 Java + 2 个 Kotlin 测试全部通过；CI 在有 Android SDK 的标准环境中执行 Lint、Unit Test、Release/R8 构建并上传 APK。
