# v8.0.1

## Build / CI 修复

- 修复 Kotlin K2 编译失败：删除错误的 `androidx.compose.foundation.layout.weight` 顶层 import；`Modifier.weight()` 由 `RowScope` / `ColumnScope` 直接解析。
- 修复 Wear Compose 1.6.2 编译失败：删除错误的 `androidx.wear.compose.foundation.lazy.minimumVerticalContentPadding` 顶层 import；该 API 由 `TransformingLazyColumnItemScope` 直接解析。
- CI 增加 `assembleDebug` 编译闸门，再执行 Release Lint、单测与 R8 Release 构建。
- versionCode 20 → 21，versionName 8.0.0 → 8.0.1。

# v8.0.0

## 架构
- 删除旧 `GameHubView.java`；复杂游戏独立 View，棋盘游戏 View + Engine，短局按类型分组。
- 新增 `GameSaveManager`、`GameStats`、`HapticsManager`、`SoundManager`、`AppSettings`、`LegacySaveMigrator`。
- 旧 mode ID 保持稳定；新扩展使用独立 ID 区间，避免 v7 存档/战绩冲突。

## 游戏与体验
- 游戏总数由 56 扩展至 81，达到长版路线图的 v8 80+ 目标。
- 新增独立 Pinball，以及记忆、反应、街机、益智、体育等多种独立短局。
- Tetris：NEXT / HOLD / 软降 / 硬降 / Combo / Level / 暂停 / 触觉 / 完整状态存档。
- Flappy / Runner / Snake 继续调整小屏操作与难度曲线。
- 统一 HUD、结果卡、动画、双确认重开/返回（含系统 Back）、触觉与性能模式。

## 首页与设置
- Wear Compose Material 3 首页、分类、收藏、最近、A-Z 查找、战绩、设置。
- 动态颜色、触觉、默认静音、动画、左右手、流畅/均衡/省电。

## 存档 / 测试 / CI
- v7.x 旧存档迁移到 v8 versioned save。
- 新增棋盘 Engine round-trip、规则测试、Tetris codec、v7 mode ID 兼容、旧存档迁移、81 款目录/路由测试。
- CI：Lint → Unit Test → Release/R8 APK，并上传 Lint 与 APK artifact。
- versionCode 20 / versionName 8.0.0。
