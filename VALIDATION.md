# v6.0 Validation Notes

已在当前交付环境完成以下检查：

- `GameHubView.java` 花括号 / 圆括号 / 方括号数量平衡，无异常替换字符。
- 使用轻量 Android API stub 对 `GameHubView.java`、`MainActivity.java`、`PhoneLinkOpener.java`、`Game2048Engine.java`、`XiangqiEngine.java` 做完整 Java 静态编译，`javac` 状态为 0。
- 两个纯 Java 游戏引擎在无 Android 依赖下直接 `javac` 编译通过。
- 菜单配置为 24 款游戏 + 战绩记录 + 意见反馈 + 关于；四个游戏分类均已接入绘制与点击入口。
- 11 款新增游戏均使用 Android Canvas 原生绘制，无新增网络图片或第三方视觉素材。
- 已检查自动存档、恢复入口、AI 回合恢复、已结束对局不保留续玩存档的代码路径。
- 保留 `android:windowSwipeToDismiss=false`；`MainActivity.onBackPressed()` 消费返回事件。
- 主菜单 / 战绩页已接入 `OverScroller`、`VelocityTracker`、越界阻尼、回弹与 `AXIS_SCROLL`。

当前容器没有完整 Android SDK / Gradle 运行环境，因此这里没有直接产出 APK，也没有宣称完成真机 UI 验证。工程保留 GitHub Actions 构建配置，可在 Android Studio、CI 或 Wear OS SDK 环境进行正式 `assembleDebug`。

建议真机/模拟器重点验证：

1. 320×320、384×384、454×454 圆屏上的菜单尺寸、底部按钮与弧边安全区。
2. 快速甩动列表后顶部/底部回弹手感，以及表冠滚动。
3. 2048、贪吃蛇、迷宫横向滑动不会触发返回。
4. 单人 AI 游戏在轮到 AI 时切后台再回来，AI 能继续执行。
5. 13 类可恢复游戏退出/重进后的“继续上次”。
6. 24 款游戏分别进行至少一轮完成/失败/重开，确认战绩数据符合预期。
