# Wear Board Games

一个面向 Wear OS 的双人棋盘游戏示例，包含五子棋和中国象棋两种模式。

## 功能

- 同一块手表双人轮流游玩
- 五子棋：11 x 11 棋盘、横竖斜线五子判定
- 中国象棋：红黑双方轮流选择棋子和移动
- 适配圆形小屏幕，棋盘使用 Canvas 绘制，无外部图片资源
- GitHub Actions 自动构建 Debug APK 并上传为构建产物

## 本地构建

需要 JDK 17、Android SDK 35 和 Gradle 8.7：

```bash
gradle assembleDebug
```

生成文件：`app/build/outputs/apk/debug/app-debug.apk`

## GitHub 构建

将代码推送到 GitHub 后，`.github/workflows/android.yml` 会在 push 或 pull request 时自动构建。构建完成后可从 Actions 的 Artifacts 下载 APK。
