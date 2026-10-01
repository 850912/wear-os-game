# v8.0.1 最终验收记录

## 当前交付环境已实际执行并通过

- [x] 全部 Java 主源码：`javac --release 17 -Xlint:all -Werror`，**0 errors / 0 warnings**。
- [x] Java 测试源码：`javac --release 17 -Xlint:all -Werror`，**0 errors / 0 warnings**。
- [x] Java 项目单测：**28 passed / 0 failed**。
- [x] Kotlin Catalog/路由单测：**2 passed / 0 failed**。
- [x] Engine round-trip：2048、象棋、五子棋、四子棋、黑白棋、数独、扫雷、迷宫、推箱子。
- [x] 规则检查：五子连线、四子连线、数独固定格、扫雷首击安全、推箱子 v7 几何。
- [x] `TetrisStateCodec` v4：完整状态 round-trip / corruption rejection。
- [x] `GameSaveManager`：版本号、payload、时间戳、按 game id 清理行为。
- [x] v7.4 → v8 存档迁移：五子棋、四子棋、黑白棋、数独、扫雷、迷宫、推箱子均可转成新 Engine payload 并恢复。
- [x] v7.4 mode ID 兼容：核心 1–40（保留历史空洞）以及 Micro 41–52、Puzzle 53–60 由 `GameModesCompatibilityTest` 锁定。
- [x] `GameCatalog.kt`：**81 games / 6 categories**，mode/title 唯一、分类有效且非空。
- [x] 81 款游戏每款恰好命中一个运行 View 路由；无旧 Hub fallback。
- [x] `GameHubView.java` 已从运行源码删除。
- [x] Manifest + **6 个资源 XML** 全部可解析；**11 个 PNG** 均通过图像完整性检查。
- [x] **92 个**文本/配置文件通过 UTF-8 与 NUL 字节检查。
- [x] `termux_push_build.sh` 通过 `bash -n` 语法检查。
- [x] GitHub Actions YAML 可解析，并包含 `lintRelease / testReleaseUnitTest / assembleRelease`。
- [x] 配置一致性：v8.0.1 / versionCode 21、compile/target SDK 35、R8 + shrinkResources、baselineprofile module。

## v8.0.1 针对 GitHub Actions 失败的修复检查

- [x] 删除 `import androidx.compose.foundation.layout.weight`；`Modifier.weight()` 仅在 `RowScope` / `ColumnScope` 中使用。
- [x] 删除 `import androidx.wear.compose.foundation.lazy.minimumVerticalContentPadding`；所有调用均位于 `TransformingLazyColumnItemScope`。
- [x] GitHub Actions 新增 `gradle assembleDebug --no-daemon --stacktrace` 编译闸门。
- [x] 版本更新为 `8.0.1 / versionCode 21`。

## 联网核对的官方工具链 / API

- Wear Compose **1.6.2** 是当前稳定 1.6 版本，官方建议 Wear UI 使用 Compose Material 3。
- Android Gradle Plugin **8.6.x** 支持 API 35；对应 Gradle **8.7**、JDK **17**。
- Baseline Profile 使用 `com.android.test` producer module、`targetProjectPath ':app'`、应用侧 `baselineProfile project(':baselineprofile')`，与 Android 官方当前做法一致。
- 当前使用的 `TransformingLazyColumn`、触控/旋钮 snap、`SurfaceTransformation`、`Card` long-click、`ScreenScaffold + EdgeButton` 等均属于 Wear Compose Material 3 / Foundation 的正式 API。

## 当前环境无法诚实替代的最终 Android 工具链验收

当前沙箱没有 Android SDK、Gradle 安装或 Android Gradle Plugin 依赖缓存，shell 网络也无法解析外部主机，因此无法在本机真正执行：

- [ ] `gradle lintRelease`
- [ ] `gradle testReleaseUnitTest`
- [ ] `gradle assembleRelease`（AAPT2 / D8 / R8 / shrinkResources）
- [ ] Wear OS 模拟器/真机圆屏、旋钮、触摸、生命周期回归
- [ ] Baseline Profile 采集与冷启动 Macrobenchmark

项目已在 `.github/workflows/android.yml` 固定 JDK 17 / Gradle 8.7 / SDK 35，并配置上述 Lint、单测和 Release APK 构建。真实 Android 工具链成功后应作为最终 APK 验收，不用静态检查冒充 APK 构建。
