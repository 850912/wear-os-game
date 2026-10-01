# WearBoardGames v7.4 → v8.0 路线图完成对照

本文件按用户提供的《后续计划》和《WearBoardGames 开发路线图（v7.4 → v8.0）》逐项核对当前 v8.0.1 工程。

| 阶段 | 结果 | v8.0.1 落地内容 |
|---|---|---|
| 1. 拆实时动画游戏 | 完成 | Pong / Breakout / Pinball / LaneDodge / Stack / Simon / Bounce 已独立，连同 Tetris / Snake / Flappy / Runner 共 11 个独立实时 View。 |
| 2. 拆棋盘与益智 | 完成 | 2048 / 象棋 / 五子棋 / 四子棋 / 黑白棋 / 数独 / 扫雷 / 迷宫 / 推箱子采用 View + Engine；其余短局进入轻量分组 View。 |
| 3. 游戏内视觉统一 | 完成 | BaseGameView 统一 HUD、结果卡、圆角 surface、主次色、低文字密度和底部系统控制区；Micro/Puzzle 同步公共视觉规范。 |
| 4. 动画系统 | 完成（基础规范） | Compose 页面 fade/scale；按钮按压 scale；结果卡淡入/缩放；Tetris 消行/出生、Snake/Flappy/Runner 等专属动画保留。 |
| 5. Tetris 专项 | 完成 | NEXT / HOLD / 软降 / 硬降 / Combo / Level / Pause / ghost / 出生与消行动画 / 触觉 / v4 完整存档。T-Spin 按原计划为可选项，未作为发布阻塞项。 |
| 6. Flappy / Runner / Snake 手感 | 完成 | Flappy 重力/间隙/节奏调整；Runner 更宽松跳跃窗口；Snake 开局较缓、逐步提速并保留食物/死亡反馈。 |
| 7. 减少误触 | 完成 | 游戏热区与系统控制区分离；重开/菜单二次确认；系统 Back 二次确认。 |
| 8. 启动速度 | 完成（工程侧） | 按需创建非首屏能力、R8/shrink、静态游戏停止循环刷新、Baseline Profile producer + Startup journey。真实冷启动数值需真机/模拟器 Macrobenchmark。 |
| 9. Compose 首页 | 完成 | Wear Material 3 首页、分类、卡片、EdgeButton、TransformingLazyColumn、动态颜色。 |
| 10. 收藏和最近 | 完成 | 首页最近入口、收藏入口，分类/列表长按收藏。 |
| 11. 查找 | 完成 | 按路线图的小屏策略使用最近 / 收藏 / 分类 / A-Z 浏览，不强制小屏键盘；语音为低优先级未加入。 |
| 12. 扩游戏 | 完成 | 从 56 扩至 81 款 / 6 分类，达到长版路线图 v8 80+ 目标；新增玩法使用独立规则/交互核心。 |
| 13. 触觉 | 完成 | HapticsManager 统一管理，设置中可关闭，实时与短局调用统一收口。 |
| 14. 音效 | 完成 | SoundManager 统一短提示音，默认关闭。 |
| 15. 设置页 | 完成 | 动态色、触觉、声音、完整/简化动画、左右手、流畅/平衡/省电。 |
| 16. 游戏状态统一 | 完成 | GameSaveManager 使用版本、game id、timestamp、payload、AI 模式；LegacySaveMigrator 兼容 v7.x。 |
| 17. 战绩升级 | 完成 | GameStats 记录次数、时长、胜负、连续纪录、best/lower-best；UI 按玩法显示。 |
| 18. 测试体系 | 完成（本地） | 28 Java + 2 Kotlin 测试；Engine round-trip、规则、存档迁移、mode ID、Tetris codec、Catalog/路由。 |
| 19. CI | 完成（配置） | GitHub Actions：lintRelease → testReleaseUnitTest → assembleRelease/R8；上传 Lint 与 APK。CI 实际运行取决于用户仓库。 |
| 20. 发布前清理 | 完成 | GameHubView 从源码删除；运行路由无 fallback；README/CHANGELOG/验证说明更新；资源/Manifest/UTF-8/ZIP 完整性检查。 |

## 最终工具链验收边界

当前交付环境没有 Android SDK、Gradle 安装或依赖缓存，因此本地无法真实执行 AAPT2、Android Lint、D8/R8 和 APK 打包，也无法运行 Wear OS 模拟器/真机。工程已经把这部分固定到 `.github/workflows/android.yml`；在 Android Studio 或 GitHub Actions 中执行 `gradle lintRelease testReleaseUnitTest assembleRelease` 后即可完成最后的 Android 工具链验收。
