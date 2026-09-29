GitHub Actions 构建说明（v7 当前版）
==================================

此文件在 v6 中用于记录一次历史 CI 故障。v7 已完成构建链升级，请以当前
`.github/workflows/android.yml`、`README.md` 和 `BUILD_COMPATIBILITY.txt` 为准。

当前配置：
- Android SDK / target / compile：36
- build-tools：36.0.0
- Gradle：8.13
- Android Gradle Plugin：8.13.2
- Kotlin：2.3.21
- JDK：17
- Wear Compose Material 3 / Foundation：1.7.0

当前 workflow 不安装已废弃的 legacy `tools` 包，只使用 GitHub Ubuntu runner
现有的 sdkmanager 安装 platform-tools、platforms;android-36、build-tools;36.0.0。

Termux 推送：
  cd "/storage/emulated/0/黑白君/game"
  chmod +x termux_push_build.sh
  ./termux_push_build.sh
