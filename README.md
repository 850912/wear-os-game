# 腕上小游戏 Wear OS · v5.0

原生 Java + Android Canvas 的 Wear OS 小游戏合集，包名保持：

`com.example.wearboardgames`

## v5.0 更新内容

### 游戏从 9 款扩展到 13 款

保留原有 9 款：
- 中国象棋
- 五子棋
- 2048
- 反应点击
- 贪吃蛇
- 记忆闪烁
- 井字棋
- 色块猎手
- 弹球挑战

新增 4 款：
- **数字华容道**：3×3 数字拼图，点击空格旁数字移动，恢复 1–8 顺序即通关。
- **熄灯解谜**：4×4 灯阵，点击一格会联动自身与上下左右，全部熄灭即通关。
- **迷你扫雷**：5×5 雷区、5 颗地雷，点击安全格并支持空白区域自动展开，打开全部 20 个安全格获胜。
- **猜拳挑战**：石头剪刀布对战手表，三局两胜。

所有新增游戏都使用 Android Canvas 原生绘制，不需要联网下载素材，也没有新增图片版权依赖。

### 禁用 App 内右划返回

- 删除 `GameHubView` 原先的“左边缘右滑返回 + 跟手动画”触发逻辑。
- `AppTheme` 继续设置 `android:windowSwipeToDismiss=false`，让水平滑动优先属于游戏操作。
- `MainActivity` 消费系统 Back 事件，避免系统返回手势在 App 内切页或退出。
- 页面导航统一使用游戏底部的“菜单 / 返回”按钮；退出 App 可使用 Wear OS Home / 表冠。

### 圆屏适配

- 主菜单继续使用圆屏安全宽度与边缘卡片缩放。
- 4 款新增游戏主交互区集中在圆屏中央约 50%–52% 的安全区域。
- 底部“菜单 / 重开”按钮保持在下弧线安全区内。

### 其他功能

- 保留统一胜利 / 失败结果层与震动反馈。
- 保留“意见反馈”在配对手机打开酷安主页。
- 保留作者支持二维码页。

## 工程参数

- `minSdk 30`
- `targetSdk 35`
- `compileSdk 35`
- Android Gradle Plugin `8.6.1`
- Gradle `8.7`
- JDK `17`
- Java + Android Canvas
- AndroidX Wear Remote Interactions `1.2.0`
- 版本：`5.0` / `versionCode 5`

## 编译

用 Android Studio 打开工程根目录，完成 Gradle Sync 后选择 **Build > Build APK(s)**。

如果本机已有 Gradle 8.7，可在工程根目录运行：

```bash
gradle assembleDebug --no-daemon
```

APK 通常生成在：

`app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions 配置仍位于 `.github/workflows/android.yml`，推送到 `main` 后可自动构建 debug APK。

## 说明

由于 2048 / 贪吃蛇等游戏需要水平滑动，Activity 关闭系统级 `windowSwipeToDismiss`；v5.0 同时移除了应用内部的边缘右滑返回，避免游戏过程中误触返回。
