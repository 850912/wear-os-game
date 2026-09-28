# v5.0 Validation Notes

已在交付环境执行：

- Java 源码花括号平衡检查通过。
- `MENU_TITLES` / `MENU_SUBS` / `MENU_MODES` 数量一致，均为 15 个菜单项（13 款游戏 + 意见反馈 + 关于）。
- 使用 `javac` 做语法阶段检查：当前环境没有 Android SDK，因此出现缺少 `android.*` 类型的预期错误；未观察到 Java 结构性语法错误。
- `AndroidManifest.xml` 和资源 XML 保持可解析结构。
- App 内原先的左边缘右滑返回触发逻辑已禁用。
- `android:windowSwipeToDismiss=false` 保留；`MainActivity.onBackPressed()` 改为消费返回事件。
- 新增数字华容道、熄灯解谜、迷你扫雷、猜拳挑战，全部 Canvas 自绘，无新增网络素材依赖。

建议在 Android Studio / Wear OS Emulator / 真机进一步验证：

1. 320×320、384×384、454×454 圆屏上的 13 款游戏菜单、棋盘与底部按钮。
2. 2048 / 贪吃蛇水平滑动时不会触发返回。
3. 从屏幕左边缘右滑不会返回菜单，也不会退出 App。
4. 新增四款游戏的通关、失败、重开逻辑。
5. 配对 Android 手机点击“意见反馈”后的 RemoteActivityHelper 行为。
