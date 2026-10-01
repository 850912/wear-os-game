# WearBoardGames v8.0.1 交付说明

本交付以用户提供的《后续计划》《WearBoardGames 开发路线图（v7.4 → v8.0）》和 v7.4.0 原始工程为基线，完成架构、UI/UX、游戏扩展、统一存档/战绩、性能工程、测试与 CI 收尾。

## 交付内容

- 版本：`8.0.1` / `versionCode 21`。
- **81 款游戏 / 6 分类**；旧 v7.4 mode ID 保持兼容。
- 运行期删除 `GameHubView`；复杂实时游戏独立 View，核心棋盘/益智采用 View + Engine，短局按类型共享轻量 View。
- Wear Compose Material 3：首页、分类、收藏、最近、A-Z 查找、设置、战绩。
- 统一 HUD、结果卡、40dp+ 系统控制区、重开/返回二次确认、系统 Back 二次确认。
- Tetris：NEXT / HOLD / 软降 / 硬降 / Combo / Level / Pause / ghost / 动画 / v4 完整状态存档。
- Flappy / Runner / Snake 小屏手感继续优化；Pong / Breakout / Bounce / Pinball / Dodge / Stack / Simon 独立。
- 统一公共能力：`GameSaveManager`、`LegacySaveMigrator`、`GameStats`、`HapticsManager`、`SoundManager`、`AppSettings`。
- 设置：动态色、触觉、默认静音、动画、左右手、流畅/平衡/省电。
- 性能：受控刷新、静态游戏停止循环刷新、R8、资源压缩、Baseline Profile producer + Startup journey。
- 自动测试与 GitHub Actions CI 已加入。

## 最终本地检查

- Java 主源码严格编译/Lint：`-Xlint:all -Werror`，0 error / 0 warning。
- Java 测试源码严格编译/Lint：0 error / 0 warning。
- Java tests：28 passed / 0 failed。
- Kotlin Catalog/route tests：2 passed / 0 failed。
- 81 游戏 / 6 分类，mode/title 唯一，每款恰好命中一个运行路由。
- 资源：Manifest + 6 XML 可解析；11 PNG 完整。
- 92 个文本/配置文件 UTF-8/NUL 检查通过；Termux 脚本语法与 CI YAML 检查通过。

详细结果见 `VALIDATION.md`；路线图逐项对照见 `ROADMAP_COMPLETION.md`。

## Android 工具链边界

原始工程没有 Gradle Wrapper；当前沙箱也没有 Android SDK/Gradle 依赖缓存，shell 无法联网取得 distribution，因此不能声称本机已执行 AAPT2、Android Lint、D8/R8 或真实 APK 构建。项目 CI 固定 JDK 17 / Gradle 8.7 / SDK 35，并执行：

`gradle assembleDebug --no-daemon --stacktrace`

随后：

`gradle lintRelease testReleaseUnitTest assembleRelease --no-daemon --stacktrace`

CI/Android Studio 首次完整成功后，即完成最终 Android 工具链与 APK 验收。
