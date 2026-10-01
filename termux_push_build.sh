#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SRC="/storage/emulated/0/黑白君/game"
WORK="$HOME/.wear-os-game-push"
REPO_URL="https://github.com/850912/wear-os-game.git"
GH_REPO="850912/wear-os-game"
BRANCH="main"
OUT="/storage/emulated/0/Download/wear-os-game-build"

command -v git >/dev/null || { echo "缺少 git：pkg install git"; exit 1; }
command -v gh >/dev/null || { echo "缺少 gh：pkg install gh"; exit 1; }
command -v rsync >/dev/null || { echo "缺少 rsync：pkg install rsync"; exit 1; }
gh auth status >/dev/null 2>&1 || { echo "请先执行：gh auth login --hostname github.com --git-protocol https --web"; exit 1; }
[ -d "$SRC" ] || { echo "找不到源码目录：$SRC"; exit 1; }
[ -f "$SRC/app/build.gradle" ] || {
  echo "❌ $SRC 不是项目根目录：找不到 app/build.gradle"
  echo "请把压缩包内容直接解压到：$SRC"
  echo "不要形成 $SRC/wearboardgames_optimized/ 这样的二级目录。"
  exit 1
}

if [ ! -d "$WORK/.git" ]; then
  rm -rf "$WORK"
  git clone "$REPO_URL" "$WORK"
else
  cd "$WORK"
  git fetch origin "$BRANCH"
  git checkout "$BRANCH"
  git reset --hard "origin/$BRANCH"
  git clean -fdx
fi

# 同步项目根目录，包括 .github。排除本机构建缓存。
rsync -av --delete \
  --exclude='.git/' \
  --exclude='.gradle/' \
  --exclude='.idea/' \
  --exclude='local.properties' \
  --exclude='build/' \
  --exclude='*/build/' \
  "$SRC/" "$WORK/"

# 双保险：即使手机文件管理器漏掉隐藏的 .github 目录，也强制在 Git 工作区
# 写入正确 workflow，避免继续跑旧的 "packages: tools platform-tools" 配置。
mkdir -p "$WORK/.github/workflows"
cat > "$WORK/.github/workflows/android.yml" <<'YAML'
name: Build Wear OS APK

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

permissions:
  contents: read

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Install Android SDK 35
        shell: bash
        run: |
          set -euxo pipefail
          SDKMANAGER="${ANDROID_SDK_ROOT:-${ANDROID_HOME}}/cmdline-tools/latest/bin/sdkmanager"
          if [ ! -x "$SDKMANAGER" ]; then
            SDKMANAGER="$(command -v sdkmanager)"
          fi
          yes | "$SDKMANAGER" --licenses >/dev/null || true
          "$SDKMANAGER" \
            "platform-tools" \
            "platforms;android-35" \
            "build-tools;35.0.0"

      - name: Set up Gradle 8.7
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.7'

      - name: Show build environment
        shell: bash
        run: |
          java -version
          gradle --version
          echo "ANDROID_HOME=$ANDROID_HOME"
          echo "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"

      - name: Build Optimized APK
        run: gradle testReleaseUnitTest assembleRelease --no-daemon --stacktrace

      - name: Upload Optimized APK
        uses: actions/upload-artifact@v4
        with:
          name: wear-os-game-release
          path: app/build/outputs/apk/release/app-release.apk
          if-no-files-found: error
          retention-days: 14
YAML

cd "$WORK"

# 明确校验 workflow，发现旧配置就立即停止，避免再白跑一次 Actions。
if grep -Eq 'packages:[[:space:]]*tools|sdkmanager[[:space:]]+tools|cmdline-tools-version:' .github/workflows/android.yml; then
  echo "❌ workflow 仍包含旧 SDK tools 配置，已停止推送。"
  exit 1
fi

echo "=== 将要推送的 workflow 前 80 行 ==="
sed -n '1,80p' .github/workflows/android.yml

git config user.name "黑白君"
git config user.email "850912@users.noreply.github.com"
git add -A
NOW="$(date '+%Y-%m-%d %H:%M:%S')"
if git diff --cached --quiet; then
  git commit --allow-empty -m "Rebuild Wear OS Game - $NOW"
else
  git commit -m "Fix CI and update Wear OS Game - $NOW"
fi

git push origin "$BRANCH"
COMMIT="$(git rev-parse HEAD)"
echo "✅ 已推送 commit：$COMMIT"

# 推送后再从 GitHub 读取 workflow，确认远端确实不是旧版。
echo "=== GitHub 远端 workflow 校验 ==="
REMOTE_WF="$(gh api "repos/$GH_REPO/contents/.github/workflows/android.yml?ref=$BRANCH" --jq '.content' | tr -d '\n' | base64 -d)"
if printf '%s\n' "$REMOTE_WF" | grep -Eq 'packages:[[:space:]]*tools|sdkmanager[[:space:]]+tools|cmdline-tools-version:'; then
  echo "❌ GitHub 远端仍是旧 workflow，停止等待构建。"
  exit 1
fi
printf '%s\n' "$REMOTE_WF" | sed -n '1,60p'

echo "✅ GitHub 远端 workflow 已确认更新。"

RUN_ID=""
for _ in $(seq 1 60); do
  RUN_ID="$(gh run list --repo "$GH_REPO" --commit "$COMMIT" --limit 1 --json databaseId --jq '.[0].databaseId // empty' 2>/dev/null || true)"
  [ -n "$RUN_ID" ] && break
  sleep 3
done

if [ -z "$RUN_ID" ]; then
  echo "代码已推送，但暂未检测到 Actions。请查看：https://github.com/$GH_REPO/actions"
  exit 0
fi

echo "✅ Actions Run ID: $RUN_ID"
rm -rf "$OUT"
mkdir -p "$OUT"

if ! gh run watch "$RUN_ID" --repo "$GH_REPO" --exit-status; then
  echo "❌ GitHub Actions 构建失败。失败步骤如下："
  gh run view "$RUN_ID" --repo "$GH_REPO" --log-failed || true
  exit 1
fi

gh run download "$RUN_ID" --repo "$GH_REPO" --dir "$OUT"
APK="$(find "$OUT" -type f -name '*.apk' | head -n 1 || true)"
if [ -n "$APK" ]; then
  echo "✅ 构建成功：$APK"
  termux-open "$APK" 2>/dev/null || true
else
  echo "⚠️ 构建成功但未找到 APK，请检查：$OUT"
  find "$OUT" -maxdepth 4 -type f -print
fi
