#!/bin/bash
export PATH=/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin

# 终止应用
/usr/bin/pkill -9 -f '$APP_NAME' || true

# 替换指定目录
/usr/bin/sudo /bin/rm -rf "$TARGET_DIR"
/usr/bin/sudo /bin/cp -Rf "$SOURCE_DIR" "$TARGET_DIR"

# 设置权限
/usr/bin/sudo /usr/sbin/chown -R $SYS_USER_NAME:staff "$TARGET_DIR"
/usr/bin/sudo /usr/bin/xattr -d com.apple.quarantine "$TARGET_DIR"
/usr/bin/sudo /bin/chmod -R 755 "$TARGET_DIR"

# 重新签名
/usr/bin/sudo /usr/bin/codesign --force --deep --sign - "$TARGET_DIR"

# 清理
/bin/rm -rf "$TEMP_DIR"
/bin/rm -f "$0"

# 启动新应用
/usr/bin/open -a "$TARGET_DIR"
exit 0