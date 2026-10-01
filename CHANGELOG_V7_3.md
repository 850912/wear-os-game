# v7.3.0

- 主入口扩充到 **56 款游戏 / 5 个分类**。
- 新增 `MicroGameView` 12 款与 `PuzzleMiniView` 8 款，停止继续把新玩法堆进单一 `GameHubView`。
- Wear Material 3 列表使用 transformation、`EdgeButton`、稳定 `contentType`，并让触摸与表冠/旋转侧键使用一致的中心吸附滚动。
- 实时动画改为 VSYNC 对齐；静态小游戏事件驱动刷新。
- `GameHubView` 的手机反馈 `RemoteActivityHelper` 改为按需初始化，普通游戏进入路径更轻。
- 加入 Baseline Profile / ProfileInstaller 1.4.1。
- 新增 JUnit 4 的 2048 / 象棋回归测试，CI 改为测试通过后再构建优化 Release APK。
- 延续 Tetris、Flappy、Runner、存档、战绩、防误触、数独唯一解、扫雷首击安全等修复。
- 版本号：`7.3.0` / `versionCode 12`。
