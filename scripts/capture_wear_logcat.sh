#!/usr/bin/env bash
set -euo pipefail
PKG="com.example.wearboardgames"
OUT="${1:-wearboardgames-logcat.txt}"

command -v adb >/dev/null 2>&1 || { echo "adb 未安装或不在 PATH" >&2; exit 1; }
adb get-state >/dev/null

PID="$(adb shell pidof "$PKG" 2>/dev/null | tr -d '\r' | awk '{print $1}')"
if [ -z "$PID" ]; then
  echo "未检测到 $PKG 进程。请先在手表上启动 WearBoardGames，再运行此脚本。" >&2
  echo "当前 adb 设备：" >&2
  adb devices -l >&2
  exit 2
fi

{
  echo "# WearBoardGames focused logcat"
  echo "# package=$PKG pid=$PID captured_at=$(date -Iseconds 2>/dev/null || date)"
  echo "# device=$(adb shell getprop ro.product.model | tr -d '\r')"
  echo "# build=$(adb shell getprop ro.build.fingerprint | tr -d '\r')"
  echo
  adb logcat -d -v threadtime --pid="$PID" WearBoardGames:V AndroidRuntime:E '*:S'
  echo
  echo "# crash buffer"
  adb logcat -d -b crash -v threadtime
} > "$OUT"

echo "已保存：$OUT"
