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

if [ ! -d "$WORK/.git" ]; then
  rm -rf "$WORK"
  git clone "$REPO_URL" "$WORK"
else
  cd "$WORK"
  git fetch origin "$BRANCH"
  git checkout "$BRANCH"
  git reset --hard "origin/$BRANCH"
  git clean -fd
fi

# 关键：这里不再排除 .github，因此修复后的 Actions workflow 会一并推送。
rsync -av --delete \
  --exclude='.git/' \
  --exclude='.gradle/' \
  --exclude='.idea/' \
  --exclude='local.properties' \
  --exclude='build/' \
  --exclude='*/build/' \
  "$SRC/" "$WORK/"

cd "$WORK"
git config user.name "黑白君"
git config user.email "850912@users.noreply.github.com"
git add -A
NOW="$(date '+%Y-%m-%d %H:%M:%S')"
if git diff --cached --quiet; then
  git commit --allow-empty -m "Rebuild Wear OS Game - $NOW"
else
  git commit -m "Update Wear OS Game - $NOW"
fi

git push origin "$BRANCH"
COMMIT="$(git rev-parse HEAD)"
echo "已推送：$COMMIT"

RUN_ID=""
for _ in $(seq 1 40); do
  RUN_ID="$(gh run list --repo "$GH_REPO" --commit "$COMMIT" --limit 1 --json databaseId --jq '.[0].databaseId // empty' 2>/dev/null || true)"
  [ -n "$RUN_ID" ] && break
  sleep 3
done

if [ -z "$RUN_ID" ]; then
  echo "代码已推送，但暂未检测到 Actions。请查看：https://github.com/$GH_REPO/actions"
  exit 0
fi

mkdir -p "$OUT"
rm -rf "$OUT"/*
gh run watch "$RUN_ID" --repo "$GH_REPO" --exit-status
gh run download "$RUN_ID" --repo "$GH_REPO" --dir "$OUT"

APK="$(find "$OUT" -type f -name '*.apk' | head -n 1 || true)"
if [ -n "$APK" ]; then
  echo "构建成功：$APK"
  termux-open "$APK" 2>/dev/null || true
else
  echo "构建成功，但未找到 APK；请检查：$OUT"
  find "$OUT" -type f -maxdepth 4 -print
fi
