# WearBoardGames v8.0.1 最终源码审查

日期：2026-10-01

## 范围

最终包包含 56 个 Java/Kotlin 源文件：44 Java 主源码、2 Kotlin 主源码、8 Java 测试、1 Kotlin 测试、1 Baseline Profile generator。运行代码为 81 款游戏 / 6 分类；Catalog 的 mode/title 唯一，每个 mode 只命中一个运行路由。

源码已按路线图物理整理为 `games/arcade/`、`games/board/`、`games/puzzle/`、`games/micro/`，公共基础设施保留在 `com/example/wearboardgames/` 根目录。旧 `GameHubView` 和根目录遗留 `tests/` 已清理。

## 最终补齐的路线图项

- 公共 `BaseGameView` 增加 3→2→1→GO 倒计时、得分/Combo 上浮渐隐、先压暗后弹入结果卡的结束过渡。
- Runner 支持落地跳跃与空中第二次修正；Snake 增加吃食物 squash 与死亡波纹；Flappy/Runner/Snake/Pong/Lane Dodge 接入开始倒计时与得分反馈。
- Tetris HOLD/暂停逻辑触控目标扩为至少 48dp，并记录最高等级/最多消行。
- `GameStats` 增加 draw 与命名扩展指标；Reversi 和棋从错误的 win 修正为 draw；战绩页对 Tetris、Snake、Flappy、棋类显示专属字段。
- Wear Compose Material 3 / Foundation 更新为稳定版 1.6.2。
- CI 分成 quality 与 baseline-profile-release 两阶段；Release 阶段在 Wear OS 5.1 emulator 采集 Baseline Profile 后执行 R8 Release 构建。
- `termux_push_build.sh` 内置 workflow 与仓库 workflow 完全同步，不再删除 lint/debug/baseline 步骤。
- Release signing 支持环境变量 / GitHub Secrets；`.jks/.keystore` 被 gitignore。

## 实际本地验证

- 44 个 Java 主源码以 JDK 17 + Android API 最小桩完整 `javac`：0 errors。
- Java JUnit 测试：31 passed / 0 failed。
- Kotlin Catalog/route：2 passed / 0 failed。
- 合计 33/33 JVM 测试通过。
- `termux_push_build.sh`：`bash -n` 通过。
- `MainActivity.kt` 在无 Compose classpath 的 `kotlinc` parser 扫描中无语法类错误。

## Android 工具链边界

当前沙箱缺完整 Android SDK / AGP Maven 环境 / Wear Emulator，因此不声称本地已执行 AAPT2、Lint、D8/R8、Macrobenchmark。以上动作均已固定在 GitHub Actions workflow，推送仓库后由标准 Android/Wear 环境执行。
