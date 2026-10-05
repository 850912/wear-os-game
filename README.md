# 腕上小游戏 Wear OS · v8.1.1

面向 Wear OS 小屏和圆屏的离线游戏合集，当前保留 **29 款游戏**。导航使用 Wear Compose Material 3，游戏使用原生 Canvas；棋盘规则与渲染分离。

## 本次更新

- 俄罗斯方块统一为轻点旋转、横滑移动、短下滑软降、长下滑硬降；暂停/继续放到底部，保留独立 HOLD。
- 下落间隔从 1000ms 随等级缓慢下降，最快 650ms；触底提供 550ms 锁定宽限，最多重置 8 次。
- 象棋增加单人 AI；象棋、五子棋、国际象棋支持双指缩放，放大后单指拖动棋盘。
- 国际象棋采用 chesslib 1.3.4 处理合法走法、王车易位、吃过路兵、升变和胜负判断，支持 AI 与双人模式。
- AI 在后台基于局面快照计算，重开或离开页面会取消旧任务。
- 选择页面采用官方 TransformingLazyColumn、SurfaceTransformation 与 ScreenScaffold，移除重复页面组合与逐项入场动画；页面只使用短淡入。
- 设置通过 SharedPreferences 监听更新显示，点击后保持列表位置。
- 帧调度合并到单个可取消的 VSYNC 回调；静态棋盘和离散计时游戏按事件刷新。
- 修复乒乓球拖动改变物理时钟、记忆闪烁旧延迟任务干扰重开，以及黑白棋跳过玩家回合后的 AI 连走问题。
- 删除 53 个重复或内容不足的目录条目以及四套相关渲染实现，新增国际象棋后共 29 款。保留游戏原有 ID，国际象棋使用独立 ID 1001。

## 保留游戏

- 棋盘：象棋、国际象棋、五子棋、井字棋、四子棋、黑白棋、Nim。
- 益智：2048、数字华容道、熄灯、扫雷、记忆配对、迷宫、数独、推箱子。
- 街机：俄罗斯方块、贪吃蛇、跳跃小鸟、乒乓球、打砖块、弹跳、弹珠、跑酷、三道闪避、叠塔。
- 短局：记忆闪烁、打地鼠、21 点、极速反应。

准确名称和路由以 GameCatalog.kt 为准。旧版删除游戏的收藏/最近记录不会显示，保留游戏仍沿用原存档 ID。

## 构建与验证

使用 JDK 17、Gradle 8.7、AGP 8.6.1、Kotlin 2.1.21、Android SDK 35、Wear Compose 1.6.2。

```bash
gradle :app:testDebugUnitTest :app:lintRelease :app:assembleDebug :app:assembleRelease --no-daemon
```

GitHub Actions 上传 wear-os-game-debug、wear-os-game-release 和 validation-reports。Release 开启 R8 与资源压缩；没有配置发布密钥时使用调试签名，适合侧载测试。真机性能应使用 Release APK 评估。

现有基线配置保留，普通构建不会启动模拟器重新采集 Baseline Profile。本地环境无 Android SDK，纯 Java 规则测试在本地执行，Android 编译、完整单测和 Lint 交由 CI 验证。本次未进行手表帧率、功耗或触屏可用性实测。

## 参考

- [Wear Compose 列表](https://developer.android.com/training/wearables/compose/lists)
- [Compose 性能最佳实践](https://developer.android.com/develop/ui/compose/performance/bestpractices)
- [Wear Compose](https://developer.android.com/training/wearables/compose)
- 第三方许可见 THIRD_PARTY_NOTICES.md。
