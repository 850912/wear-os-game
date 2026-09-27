GitHub Actions 第三次修复说明
===========================

本次失败日志仍在运行旧 workflow：
  android-actions/setup-android@v3
  cmdline-tools-version: 12266719
  packages: tools platform-tools

随后 sdkmanager 尝试安装已经不存在的 legacy 包 "tools"，构建在真正编译 APP 之前就失败。

本包的修复：
1. workflow 不再使用 android-actions/setup-android@v3。
2. 直接使用 GitHub Ubuntu runner 已预装的 sdkmanager。
3. 只安装：platform-tools、platforms;android-35、build-tools;35.0.0。
4. Gradle 固定 8.7，AGP 固定 8.6.1，JDK 17。
5. termux_push_build.sh 在推送前会强制重写 workflow。
6. 推送后会通过 GitHub API 再读取远端 workflow；若仍是旧版，脚本会停止，不再浪费一次 Actions。
7. 本 ZIP 采用“项目文件直接位于 ZIP 根目录”的结构，请直接解压覆盖 /storage/emulated/0/黑白君/game/。

执行：
  cd "/storage/emulated/0/黑白君/game"
  chmod +x termux_push_build.sh
  ./termux_push_build.sh
