# 腕上小游戏 Wear OS · v3.0

这是在上一版 `wearboardgames_optimized` 基础上继续优化的 Wear OS 原生 Java 工程，包名保持：

`com.example.wearboardgames`

## v3.0 本次更新

### 1. 2048 UI / 动画重做
- 改为经典 2048 风格的米白背景、棕色棋盘、经典数字块配色。
- 增加“分数 / 最佳”双计分框，最佳分数使用 `SharedPreferences` 持久保存。
- 增加数字块滑动位移动画。
- 合并时增加弹跳 / 回弹动画。
- 新生成数字块增加缩放出现动画。
- 达到 2048 后可继续挑战更高数字。
- 逻辑拆分为独立 `Game2048Engine.java`，便于测试和维护。

2048 的视觉方向参考 Gabriele Cirulli 的开源 2048 项目（MIT）；本工程使用 Android Canvas 重新实现，详见 `THIRD_PARTY_NOTICES.md`。

### 2. 游戏动画改进
- 中国象棋：确认移动后棋子从起点平滑移动到终点。
- 五子棋：落子增加缩放回弹动画。
- 反应点击：目标增加呼吸脉冲效果。
- 新增贪吃蛇：连续移动、加速、食物脉冲与震动反馈。
- 新增记忆闪烁：四颜色按序闪烁，玩家复现序列。

### 3. 游戏列表
当前共 6 款游戏：
- 中国象棋
- 五子棋
- 2048
- 反应点击
- 贪吃蛇
- 记忆闪烁

主菜单改为纵向可滚动大卡片，圆形屏幕会缩窄卡片和底部按钮，避免边缘被圆屏裁切。

### 4. 关于 / 捐赠
- 新增“关于”页。
- 作者显示为：**黑白君**。
- 新增“支持作者”入口。
- 捐赠页使用用户提供的微信收款码，并针对手表屏幕裁切为更大的可扫描区域。
- 收款码只做裁切，没有重绘二维码内容；已在本地用二维码识别器校验裁切后的二维码仍可识别。
- 所有标题、说明和按钮均按屏幕尺寸动态适配，尽量避免圆形小屏文字或按钮显示不全。

### 5. 禁用右划返回
按照需求，在主题中加入：

```xml
<item name="android:windowSwipeToDismiss">false</item>
```

这样 Wear OS Activity 不再被系统的左到右滑动手势直接关闭，2048 和贪吃蛇可以正常使用水平滑动。

物理返回键 / 系统返回事件仍然可用：游戏页先返回主菜单，捐赠页先返回关于页，主菜单再退出应用。

### 6. 原有中国象棋规则
继续保留上一版完整规则判断：
- 车直线及阻挡
- 马走日与蹩马腿
- 炮架
- 象眼与不过河
- 士九宫
- 将帅九宫 / 将帅照面
- 兵卒过河
- 禁止送将
- 将军与无合法着法判负
- 撤销一步

## 工程参数
- `minSdk 30`
- `targetSdk 35`
- `compileSdk 35`
- Java + Android Canvas
- 无第三方运行时依赖
- 版本：`3.0` / `versionCode 3`

## 编译
用 Android Studio 打开工程根目录，完成 Gradle Sync 后选择 **Build > Build APK(s)**。如果你本机已有 Gradle，也可以在工程根目录执行 `gradle assembleDebug`。

生成位置通常为：

`app/build/outputs/apk/debug/app-debug.apk`

当前交付环境没有完整 Android SDK / Build Tools，因此这里提供的是已经做过 Java 语法级检查、2048 逻辑测试和 XML 解析检查的完整工程源码；建议最终在 Android Studio / 真机 Wear OS 上再做一轮触控和二维码扫描测试。

## CI / GitHub Actions 构建兼容性

本工程已固定为以下兼容组合：

- Android Gradle Plugin: **8.6.1**
- Gradle: **8.7**
- JDK: **17**
- compileSdk / targetSdk: **35**

这样可以直接兼容 GitHub Actions 中的 Gradle 8.7，避免 `Minimum supported Gradle version is 8.9` 错误。
工程同时附带 `.github/workflows/android.yml`，推送到 `main` 后可自动构建并上传 `app-debug.apk`。

## GitHub Actions 修复（2026-09-27）

如果日志出现 `Warning: Failed to find package 'tools'`，说明旧 workflow 请求了已经废弃的 Android SDK `tools` 包。本项目当前的 `.github/workflows/android.yml` 已移除该包，并改为安装 `platform-tools`、`platforms;android-35` 和 `build-tools;35.0.0`。

Termux 推荐直接运行项目根目录的 `termux_push_build.sh`。这个脚本会同步 `.github`，可避免旧版推送脚本因为排除 `.github/` 而导致 GitHub 上的 workflow 一直没有更新。
