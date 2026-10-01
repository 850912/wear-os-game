# WearBoardGames v8.0.1 路线图完成版交付说明

本包以用户上传的 v8.0.1 修复版为基线，按《后续计划》和《WearBoardGames 开发路线图（v7.4 → v8.0）》逐项补齐并重新验收。

## 本次完成的关键内容

1. 统一动画：3→2→1→GO、得分/Combo 上浮渐隐、结束背景压暗→结果卡弹入。
2. Runner：落地跳跃 + 空中第二次修正；Snake：食物 pulse + 吃食物 squash + 死亡波纹；Flappy/Pong/Dodge 等接入统一倒计时/得分反馈。
3. Tetris：NEXT/HOLD/软降/硬降/Combo/Level/Pause/ghost/存档保持；侧控最小逻辑触控目标 48dp；记录最高等级/最多消行。
4. 战绩：新增胜/负/和，修复 Reversi 和棋误记胜利；新增 Tetris、Snake、Flappy 专属统计字段。
5. 架构：旧 GameHubView 已删除；源码按 `games/arcade`、`games/board`、`games/puzzle`、`games/micro` 物理目录整理；旧根目录 `tests/` 删除。
6. Baseline Profile：producer journey 修正为兼容 Canvas 游戏；CI 使用 Wear OS 5.1 emulator 执行 profile 生成再构建 Release。
7. CI/Termux：`termux_push_build.sh` 不再覆盖成缩水 workflow；两处 workflow 同步包含 Debug、Lint、Unit Test、Baseline Profile、Release/R8。
8. 发布签名：支持正式 keystore 环境变量 / GitHub Secrets；未配置时仅用于侧载的 Release 可回退 debug key；keystore 文件已 gitignore。
9. Wear Compose Material 3 / Foundation 更新到 1.7.0 stable。

## 当前包实际验证

- 44 Java main：最小 Android API 桩 + JDK 17 完整 javac，0 errors。
- 31 Java tests：31 passed / 0 failed。
- 2 Kotlin Catalog/route tests：2 passed / 0 failed。
- 合计：33/33 JVM tests passed。
- 81 games / 6 categories；mode/title 唯一；每款游戏仅一个运行路由。
- `termux_push_build.sh`：`bash -n` PASS。

## Android SDK 最终执行

当前交付沙箱不含完整 Android SDK/AGP Maven 环境和 Wear OS emulator，因此不冒充本地执行 Lint/AAPT2/D8/R8/Macrobenchmark。仓库 workflow 已把完整执行链固定下来，推送 GitHub 后可得到 Lint、Release APK 与 Baseline Profile artifact。
