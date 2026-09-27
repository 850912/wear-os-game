# Validation notes

本交付包已完成以下离线检查：

- `GameHubView.java`、`Game2048Engine.java`、`XiangqiEngine.java`、`MainActivity.java`：使用轻量 Android API stub 进行 `javac` 语法 / 类型检查，检查通过。
- `Game2048EngineTest.java`：验证左右/上下合并、连续相同数字不重复合并、得分、2048 达成及无可移动状态，测试通过。
- 所有 XML 资源：通过 XML parser 检查。
- 微信收款码：原图只做裁切；裁切资源使用 OpenCV QRCodeDetector 验证可解码。
- 收款码按典型圆形手表显示尺寸缩小时采用不插值绘制，保留二维码硬边，减少缩放模糊。
- 圆屏布局采用相对屏幕尺寸和圆屏安全宽度：菜单卡片、底部按钮、2048/贪吃蛇/记忆闪烁主体均避开圆屏边缘高风险区。

由于当前执行环境没有 Android SDK / AAPT2 / D8，因此未在此环境产出新的 APK。建议最终在至少一个 384–396 px 圆屏和一个 450–466 px 圆屏 Wear OS 模拟器或真机上做最后一轮视觉/触控验收。
