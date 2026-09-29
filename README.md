# 腕上小游戏 Wear OS · v7.0

Wear OS 离线小游戏合集。v7 将应用外壳从单体 Java Canvas 菜单重构为 **Wear Compose Material 3 Expressive**，游戏核心仍采用低延迟 Canvas；`Game2048Engine.java` 保持原始版本不变。

## v7.0 主要变化

- 首页、分类页、模式选择、工具页和战绩页迁移到 `androidx.wear.compose:compose-material3:1.7.0`。
- 使用 `AppScaffold`、`ScreenScaffold`、`TransformingLazyColumn`、Wear M3 `Card`、动态配色和列表形变，兼顾圆屏与方屏。
- 首页由超长列表改为 **4 个分类大卡片**，游戏进入分类子页面，卡片尽量占满可用宽度。
- 主游戏库从 24 款扩展到 **30 款**；猜拳、骰子对决从主入口移除。
- 新增：俄罗斯方块、跳跃小鸟、打地鼠、21 点、推箱子、像素跑酷、三道闪避、叠塔。
- 数独改为真正可编辑玩法：可输入、修改、清除，行/列/宫冲突标红，不再要求每一步必须直接命中答案。
- “重开”改为 2.2 秒内二次确认，且收紧底部按钮命中区，减少误触。
- 井字棋使用完整 Minimax；五子棋、四子棋、黑白棋加入更强的局面评分；Nim 保留最佳策略。
- 自动存档覆盖 17 款可持续游戏；游戏容器销毁、切后台和游戏内返回都会尝试保存。
- 2048 核心引擎未修改，保留原逻辑。

## 30 款主入口游戏

### 棋盘对战（6）

中国象棋、五子棋、井字棋、四子棋、黑白棋、Nim 取石。

### 益智解谜（8）

2048、数字华容道、熄灯解谜、迷你扫雷、记忆配对、迷宫逃脱、4×4 数独、推箱子。

### 街机经典（9）

俄罗斯方块、贪吃蛇、跳跃小鸟、腕上乒乓、砖块破坏、弹球挑战、像素跑酷、三道闪避、叠塔。

### 轻松挑战（7）

反应点击、记忆闪烁、色块猎手、数字连点、极速心算、打地鼠、21 点。

## 自动存档

可恢复：中国象棋、五子棋、井字棋、四子棋、黑白棋、Nim、2048、数字华容道、熄灯解谜、迷你扫雷、记忆配对、迷宫、数独、推箱子、俄罗斯方块、跳跃小鸟、21 点。

主页会在存在有效存档时显示“继续”卡片。已经结束的对局不会继续保留无效存档。

## UI / Wear OS 适配

- Wear Material 3 Expressive 外壳使用动态表盘配色；不支持动态配色时回退到默认 Wear M3 主题。
- `TransformingLazyColumn` 负责圆屏边缘缩放/形变，方屏保持完整卡片宽度。
- 游戏保留 Canvas 以保证滑动、拖拽、实时动画延迟；共享标题、圆角控制区、反馈和底部操作统一放大。
- 保留 `windowSwipeToDismiss=false`，避免 2048、贪吃蛇、迷宫、俄罗斯方块、推箱子等横向手势被系统返回抢走。
- 支持表冠/旋钮滚动备用 Canvas 列表；Compose 列表由 Wear Foundation 处理滚动。

## 工程参数

- `minSdk 30`
- `targetSdk 36`
- `compileSdk 36`
- Android Gradle Plugin `8.13.2`
- Gradle `8.13`
- Kotlin `2.3.21`
- JDK `17`
- Wear Compose Material 3 / Foundation `1.7.0`
- Activity Compose `1.13.0`
- Compose UI Tooling `1.12.1`（debug）
- Wear Remote Interactions `1.2.0`
- 版本：`7.0` / `versionCode 7`

## 编译

Android Studio 打开工程根目录，Gradle Sync 后执行 **Build > Build APK(s)**。

有 Gradle 8.13 和 Android SDK 36 的命令行环境可运行：

```bash
gradle assembleDebug --no-daemon --stacktrace
```

Debug APK 默认位于：`app/build/outputs/apk/debug/app-debug.apk`。

`.github/workflows/android.yml` 已同步为 JDK 17 + Gradle 8.13 + Android SDK 36，可直接构建并上传 debug APK artifact。
