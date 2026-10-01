# WearBoardGames v8.0.1 最终验收记录

## 当前交付环境实际执行并通过

- [x] 审查全部 **56 个 Java/Kotlin 源文件**：44 Java main + 2 Kotlin main + 8 Java test + 1 Kotlin test + 1 Baseline Profile Kotlin。
- [x] 44 个 Java 主源码：JDK 17 + 最小 Android API 桩完整编译，**0 source errors**。
- [x] Java 项目单测：**31 passed / 0 failed**。
- [x] Kotlin Catalog/路由单测：**2 passed / 0 failed**。
- [x] 合计项目 JVM 单测：**33 passed / 0 failed**。
- [x] Engine round-trip：2048、象棋、五子棋、四子棋、黑白棋、数独、扫雷、迷宫、推箱子。
- [x] 规则检查：五子连线、四子连线、数独固定格、扫雷首击安全、推箱子 v7 几何。
- [x] TetrisStateCodec v4：完整状态 round-trip / corruption rejection。
- [x] GameSaveManager 与 v7.4 → v8 棋盘存档迁移测试通过。
- [x] v7.4 mode ID 兼容测试通过。
- [x] GameCatalog：**81 games / 6 categories**，mode/title 唯一；每款游戏恰好命中一个运行 View 路由。
- [x] Reversi 和棋结果改为 `kind=0`，不再误计为胜利；GameStats 新增 draw 统计。
- [x] 战绩专属指标：Tetris level/lines、Snake length、Flappy pipes 已落地并在战绩页显示。
- [x] 动画规范：3→2→1→GO、score/combo popup、结果遮罩→卡片进场、Snake squash/death ripple、Runner 空中二次修正。
- [x] 公共底部控制区与 Tetris 侧控触控目标最低 48dp。
- [x] `termux_push_build.sh` 通过 `bash -n`，且其写入 workflow 与 `.github/workflows/android.yml` 同步。
- [x] MainActivity.kt 无 parser/syntax 类诊断；缺少 Compose/Android classpath 时只有预期 unresolved/inference 诊断。

## CI / Android 工具链验收配置

GitHub Actions 已配置两阶段验收：

1. `assembleDebug + lintRelease + testReleaseUnitTest`。
2. 启动 Wear OS 5.1 / API 35 emulator，执行 `:app:generateReleaseBaselineProfile`，随后 `:app:assembleRelease`，上传优化 APK 与生成的 Baseline Profile。

Release 支持通过 GitHub Secrets 注入正式签名；没有签名 secrets 时回退到 debug key 以便侧载验证。

## 当前沙箱未冒充执行的项目

- [ ] AAPT2 / Android Lint / D8 / R8 / shrinkResources 的真实 Android SDK 执行
- [ ] Wear OS Emulator/真机触控、旋钮、生命周期、帧率与冷启动实测
- [ ] GitHub Actions 远端 run 的结果（需要推送到用户仓库后触发）

这些不是源码缺项；对应执行链路已经写入 CI。
