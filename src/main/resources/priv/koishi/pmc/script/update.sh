#!/bin/bash

# 终止应用
pkill -9 -f '$APP_NAME' || true

# 替换指定目录
rm -rf "$LIB_DIR"
cp -Rf "$SOURCE_DIR" "$TARGET_DIR"

# 设置权限
chown -R $SYS_USER_NAME:staff "$TARGET_DIR"
xattr -d com.apple.quarantine "$TARGET_DIR"
chmod -R 755 "$TARGET_DIR"

# 重新签名
codesign --force --deep --sign - "$TARGET_DIR"

# 清理
rm -rf "$TEMP_DIR"
rm -f "$0"

# 启动新应用
open -a "$APP_PATH"
exit 0