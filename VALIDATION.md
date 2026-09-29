# v7.0 Validation Notes

当前交付环境完成的检查：

- Compose 主入口统计为 30 款且 mode ID 无重复：棋盘 6、益智 8、街机 9、轻松 7。
- 30/30 主入口均能在 `GameHubView` 找到绘制分发与 reset 分发。
- 17 个标记为可恢复的游戏均接入 Java `canPersistMode` 存档层。
- `GameHubView.java` 与 `MainActivity.kt` 的花括号结构平衡。
- 原始压缩包与当前 `Game2048Engine.java` SHA-256 一致：`8d204750c65c12344ba27de9ec6a4d1c8fb31a2b97734c9b6208ec981c2cf990`。
- 新旧备用菜单数组均为 30 个游戏 + 3 个工具项，标题 / 副标题 / 分类 / mode 数量一致。
- 数独输入、清除、冲突检测与完成检测路径已接全。
- 新增 8 款主入口游戏均为代码绘制，无外部图片依赖。
- GitHub Actions 与 Termux 推送脚本均已同步 Android SDK 35 + Gradle 8.7。
- JDK 17 已独立编译通过 `Game2048Engine.java` 与 `XiangqiEngine.java`。
- Manifest 与全部 Android 资源 XML 均已通过 XML 解析检查。
- `termux_push_build.sh` 已通过 `bash -n` 语法检查。
- 除 2048 外的 Canvas 游戏共享标题/提示字号已放大；2048 保持原有标题字号与核心引擎。
- 参考 Google Wear OS API 文档核对了 `AppScaffold`、`ScreenScaffold`、`TransformingLazyColumn`、`SurfaceTransformation`、`rememberTransformationSpec`、`transformedHeight` 与动态配色的用法。

限制：当前容器没有 Android SDK / Gradle，本地无法执行真实 `assembleDebug`。`javac` 检查会因缺少 Android 类库而产生依赖解析错误，因此本交付不冒充“已本机编译成功”。仓库 CI 已配置真实 Android SDK 35 构建，可在 GitHub Actions 或 Android Studio 中完成最终 APK 编译。

建议设备回归矩阵：

1. 320×320、384×384、454×454 圆屏；典型方屏 / 矩形 Wear OS 模拟器。
2. 首页、四个分类、模式选择、工具、战绩的首尾滚动与表冠滚动。
3. 30 款游戏逐一进入、重开、退出；验证第一次“重开”只提示，第二次才执行。
4. 数独：填错冲突、修改、清除、完成；推箱子各关滑动边界。
5. 五子棋 / 井字棋 / 四子棋 / 黑白棋单人 AI 与双人模式。
6. 17 款可恢复游戏分别中途退到首页、切后台、重新启动后继续。
7. 2048 手势、得分、合并、存档与原版行为回归，重点确认核心引擎未被替换。

## 2026-09-29 build hotfix
The uploaded CI failure was caused by AGP 8.13.2 running under Gradle 8.7. This revision aligns the project to the observed runner instead of requiring a newer global Gradle installation:
- Gradle 8.7
- AGP 8.6.1
- Kotlin 2.1.21
- compileSdk / targetSdk 35
- Wear Compose Material 3 / Foundation 1.6.2
- Activity Compose 1.10.1

This keeps Material 3 Expressive while removing the incompatible 8.13.x / 1.7.x build-system floor.
