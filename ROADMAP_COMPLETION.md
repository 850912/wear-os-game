# WearBoardGames v7.4 → v8.0 路线图完成验收

完成基线：v8.0.2 / versionCode 22 / 81 款游戏 / 6 分类。本文按用户提供的《后续计划》和《WearBoardGames 开发路线图（v7.4 → v8.0）》逐项验收源码，而不是仅按版本号判断。

| 阶段 | 结果 | v8.0.2 最终落地 |
|---|---|---|
| 1. 拆实时动画游戏 | ✅ 完成 | Tetris / Snake / Flappy / Runner / Pong / Breakout / Bounce / Pinball / LaneDodge / Stack / Simon 为独立实时 View；GameHubView 已删除。 |
| 2. 拆棋盘与益智 | ✅ 完成 | 2048 / 象棋 / 五子棋 / 四子棋 / 黑白棋 / 数独 / 扫雷 / 迷宫 / 推箱子采用 View + Engine；简单短局进入 micro/puzzle 分组 View。 |
| 3. 游戏内视觉统一 | ✅ 完成 | BaseGameView 统一 HUD、圆角 surface、系统控制区、结果卡、字体层级；共享短局 View 同步视觉规范。交互触控目标按当前 Wear OS 规范扩至 48dp。 |
| 4. 动画系统 | ✅ 完成 | Compose 页面 fade/scale/spring；公共按压反馈；统一 3→2→1→GO；+1/+N/Combo 飘字；Game Over 先压暗再弹入结果卡；Tetris/Snake/Flappy/Runner 等保留专属动画。 |
| 5. Tetris 专项 | ✅ 完成 | NEXT / HOLD / 软降 / 硬降 / Combo / Level / Pause / ghost / 出生与消行动画 / 触觉 / 完整存档。T-Spin 在原计划中为可选项，不作为验收阻塞项。 |
| 6. Flappy / Runner / Snake 手感 | ✅ 完成 | Flappy 采用缓和重力/间隙/渐进难度；Runner 支持落地跳跃 + 空中第二次修正；Snake 开局较缓、逐渐提速、食物 pulse、吃食物 squash、死亡波纹。 |
| 7. 减少误触 | ✅ 完成 | 游戏区与系统区隔离；重开/菜单二次确认；系统 Back 二次确认；棋类可启用二次落子确认；Tetris 侧控使用 ≥48dp hit target。 |
| 8. 启动速度 | ✅ 完成（工程链路） | 延迟初始化、静态场景停止连续刷新、R8/shrinkResources、Baseline Profile producer 与真实首页→分类→Tetris journey；CI 在 Wear OS 5.1 模拟器执行 profile 采集后再构建 Release。 |
| 9. Compose 首页 | ✅ 完成 | Wear Material 3 首页、分类、卡片、EdgeButton、TransformingLazyColumn、动态颜色与圆屏布局。 |
| 10. 收藏和最近 | ✅ 完成 | 首页最近/收藏入口，长按收藏/取消，最近记录持久化。 |
| 11. 查找 | ✅ 完成 | 按小屏策略提供最近 / 收藏 / 分类 / A-Z 索引；文档中语音搜索属于低优先级可选扩展。 |
| 12. 扩游戏 | ✅ 完成 | 56 → 81 款 / 6 分类，超过长版路线图 v8 80+ 目标；Catalog mode/title 唯一且每款只命中一个运行路由。 |
| 13. 触觉 | ✅ 完成 | HapticsManager 统一管理，设置可开关，落地/得分/失败/按钮等事件统一调用。 |
| 14. 音效 | ✅ 完成 | SoundManager 统一短提示音，默认关闭，设置可开关。 |
| 15. 设置页 | ✅ 完成 | 动态色、触觉、声音、完整/简化动画、左右手、流畅/平衡/省电、棋类落子确认。 |
| 16. 游戏状态统一 | ✅ 完成 | GameSaveManager：save_version / game id / timestamp / payload / AI 模式；LegacySaveMigrator 兼容 v7.x；Tetris 使用 v4 codec。 |
| 17. 战绩升级 | ✅ 完成 | GameStats 记录次数、时长、胜/负/和、连续纪录、best/lower-best；Tetris 额外记录最高等级/最多消行，Snake 最长长度，Flappy 最多通过管道；UI 按玩法显示。 |
| 18. 测试体系 | ✅ 完成 | app/src/test 共 31 个 Java + 2 个 Kotlin 测试方法，覆盖 Engine round-trip、规则、存档迁移、mode ID、Tetris codec、Catalog/路由；本交付环境实跑 33/33 通过。 |
| 19. CI | ✅ 完成（配置） | Quality job：assembleDebug + lintRelease + testReleaseUnitTest；Release job：Wear OS 5.1 emulator → generateReleaseBaselineProfile → assembleRelease/R8；上传 Lint、APK、Baseline Profile。Termux 写入的 workflow 与仓库 workflow 保持一致。 |
| 20. 发布前清理 | ✅ 完成 | GameHubView 删除；旧根目录 tests/ 遗留测试删除；源码物理目录按 games/arcade、board、puzzle、micro 整理；README/VALIDATION/交付说明同步；keystore 默认忽略。 |

## 发布签名

Release 构建支持正式 upload/release keystore。提供以下环境变量时使用正式签名：`WBG_KEYSTORE_PATH`、`WBG_KEYSTORE_PASSWORD`、`WBG_KEY_ALIAS`、`WBG_KEY_PASSWORD`。GitHub Actions 可使用 `WBG_KEYSTORE_BASE64` 还原 keystore，并读取后三项 secret。未提供这些变量时，为方便本地/Actions 直接侧载测试，Release 回退使用 debug key；正式商店发布时必须配置正式 key。

## 当前工具链边界

本交付沙箱没有完整 Android SDK/AGP Maven 缓存和 Wear OS Emulator，因此这里不伪造 AAPT2、Lint、D8/R8 或 Macrobenchmark 的“本机已运行”结果。源码、JVM 测试、CI 配置和 Baseline Profile journey 已完成；真正的 Android 工具链执行由仓库 workflow 在标准 GitHub Actions Android/Wear 环境完成。
