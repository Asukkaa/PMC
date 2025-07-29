#!/bin/bash

# 终止应用
if [ -n "$APP_PID" ]; then
    kill -9 "$APP_PID" 2>/dev/null || true
else
    pkill -9 -f '$APP_NAME' || true
fi

# 替换指定目录
cp -Rf "$SOURCE_DIR" "$TARGET_DIR"

# 设置权限
chown -R $SYS_USER_NAME:staff "$APP_PATH"
xattr -d com.apple.quarantine "$APP_PATH"
chmod -R 755 "$APP_PATH"

# 重新签名
codesign --force --deep --sign - "$APP_PATH"

# 清理
rm -rf "$TEMP_DIR"
rm -f "$0"

# 启动新应用
open -a "$APP_PATH"
exit 0