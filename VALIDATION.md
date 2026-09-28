# v4.0 Validation Notes

已在交付环境执行：

- Java 源码括号 / 圆括号平衡检查通过。
- 使用 `javac` 做语法阶段检查：仅出现缺少 Android SDK 类型的预期错误，未发现 `';' expected`、`illegal start`、`reached end of file` 等 Java 语法错误。
- `Game2048EngineTest` 重新编译并运行通过。
- `AndroidManifest.xml` / values XML 保持可解析结构。
- 新二维码资源已从用户提供图片裁去外围大面积绿色空白并保存为 PNG，未重绘二维码内容。
- 圆屏布局使用统一安全宽度：菜单约 70%、底部操作区约 66%，新增游戏主棋盘 / 主交互区约 49%–50%（弹球区约 68%），避免落入圆屏四角不可见区域。

建议在 Android Studio / Wear OS Emulator / 真机进一步验证：

1. 320×320、384×384、454×454 圆屏上的所有菜单项与底部按钮。
2. 方屏设备上的菜单卡、结果层和 3 款新增游戏。
3. 左边缘右滑返回与 2048 / 贪吃蛇中央滑动手势是否符合目标机型习惯。
4. 配对 Android 手机点击“意见反馈”后是否能打开 `https://www.coolapk.com/u/22532694`。
5. 不同 Wear OS / 手机配对状态下 `RemoteActivityHelper` 的行为。
6. 用微信在实际手表屏幕亮度下扫描支持作者二维码。
