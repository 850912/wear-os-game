# 腕上小游戏 Wear OS · v4.0

原生 Java + Android Canvas 的 Wear OS 小游戏合集，包名保持：

`com.example.wearboardgames`

## v4.0 更新内容

### 游戏从 6 款扩展到 9 款

保留：
- 中国象棋
- 五子棋
- 2048
- 反应点击
- 贪吃蛇
- 记忆闪烁

新增：
- **井字棋**：玩家执 X，对战本地轻量 AI；AI 会优先取胜、阻挡、抢中心与角位。
- **色块猎手**：在逐步增大的色块阵列中找出不同色，12 关通关；点错会扣时间。
- **弹球挑战**：拖动底部挡板接球，连续反弹 12 次通关；球速和反弹角度会动态变化。

### 圆屏适配继续加强

- 主菜单在圆屏中使用更窄的安全宽度。
- 菜单卡片靠近圆形屏幕上下边缘时会缩小，减少边缘裁切，并让中心项目更突出。
- 游戏棋盘、按钮、结果提示卡、关于页和二维码页均按 `min(width, height)` 百分比布局。
- 新增 3 款游戏的主要交互区域全部位于圆屏中央安全区域。
- 底部操作区保持在圆屏下弧线内，不把关键按钮贴到四角。

此方向参考 Android 官方 Wear OS 文档关于圆屏预览、不同屏幕尺寸和圆屏列表变形的建议：
- https://developer.android.com/training/wearables/compose/previews
- https://developer.android.com/training/wearables/compose/screen-size
- https://developer.android.com/training/wearables/compose/lists

本项目仍使用 Canvas 自绘界面，没有整体迁移到 Compose，但采用了同样的“安全区 + 百分比尺寸 + 边缘缩放”思路。

### App / 手势 / 游戏动画

- 页面进入使用轻微平移 + 缩放动画。
- 返回使用反向页面动画。
- 新增**左边缘右滑返回**：从屏幕左侧边缘向右滑动，可以返回上一层；拖动过程中页面跟手移动。
- 2048、贪吃蛇等游戏的中央水平滑动仍保留为游戏操作，不与返回冲突。
- 主菜单按压卡片增加缩放反馈。
- 井字棋落子、色块选择、弹球、象棋移动、五子棋落子、2048 合并等均带动画。

### 胜利 / 失败结果反馈

增加统一的大尺寸结果层：
- 胜利：绿色成功卡 + 勾号 + 彩色粒子动画 + 震动。
- 失败：红色失败卡 + 叉号 + 脉冲圆环 + 震动。
- 平局 / 中性结果：黄色提示卡。

并给原有游戏补充明确目标：
- 反应点击：30 秒达到 20 分视为通关。
- 贪吃蛇：吃到 10 个食物通关。
- 记忆闪烁：完成 8 轮通关。
- 2048：首次合成 2048 会显示短暂庆祝提示，仍可继续挑战更高数字。

### 作者二维码已替换

`app/src/main/res/drawable-nodpi/donate_qr.png` 已替换为本次提供的二维码图片，并只裁掉外围大面积绿色空白，以便在手表上放大二维码和昵称区域；二维码内部图案未重绘。

### 意见反馈：在配对手机打开酷安

主菜单和“关于”页都提供“意见反馈”入口，目标地址：

`https://www.coolapk.com/u/22532694`

实现使用 Android 官方 `RemoteActivityHelper`：
- `Intent.ACTION_VIEW`
- `Intent.CATEGORY_BROWSABLE`
- `targetNodeId = null`，从手表请求在配对手机打开公开链接
- 入口必须由用户主动点击触发

参考：
- https://developer.android.com/reference/androidx/wear/remote/interactions/RemoteActivityHelper
- https://developer.android.com/training/wearables/apps/standalone-apps

工程增加稳定版依赖：

```gradle
implementation "androidx.wear:wear-remote-interactions:1.2.0"
```

如果手表暂时无法把请求发送到手机，界面会显示“未能连接手机，请确认蓝牙连接”。

## 工程参数

- `minSdk 30`
- `targetSdk 35`
- `compileSdk 35`
- Android Gradle Plugin `8.6.1`
- Gradle `8.7`
- JDK `17`
- Java + Android Canvas
- AndroidX Wear Remote Interactions `1.2.0`
- 版本：`4.0` / `versionCode 4`

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

由于 2048 / 贪吃蛇等游戏需要水平滑动，Activity 继续关闭系统级 `windowSwipeToDismiss`，避免整页被系统直接划走；应用内部用“左边缘右滑返回 + 跟手动画”替代，普通游戏区域的滑动不会误触返回。
