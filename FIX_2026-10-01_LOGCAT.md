# v8.0.2 修复说明（2026-10-01）

本轮输入的 `日志.txt` 来自 Android 手机系统日志，未出现应用包名 `com.example.wearboardgames`、`AndroidRuntime` 的 WearBoardGames 崩溃记录或 `FATAL EXCEPTION`，因此不能把其中 HoneyBoard、Google Assistant、SurfaceFlinger 等日志当作本应用崩溃原因。

已做的修复：

- 增加稳定 Logcat 标签 `WearBoardGames`，覆盖应用启动、暂停、游戏进入/退出和远程手机链接请求。
- `PhoneLinkOpener` 不再只记录同步提交结果，同时监听 `RemoteActivityHelper` 的异步完成结果并写入成功/失败日志。
- 新增 `scripts/capture_wear_logcat.sh`，按应用 PID + `WearBoardGames`/`AndroidRuntime` 标签抓取日志，并附加 crash buffer。
- 版本提升到 `8.0.2` / `versionCode 22`。
- GitHub Actions 更新到 Node 24 代 Action，消除旧 `setup-java@v4` 等弃用路径；Termux 内嵌 workflow 与仓库 workflow 同步。

抓取方法：连接 **手表本身** 的 adb，启动 WearBoardGames 后在项目根目录执行：

```bash
./scripts/capture_wear_logcat.sh
```

输出文件默认为 `wearboardgames-logcat.txt`。
