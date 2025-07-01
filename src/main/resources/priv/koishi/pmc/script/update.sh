 #!/bin/bash
 export PATH=/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin

 # 终止应用
 /usr/bin/pkill -9 -f '$APP_NAME' || true

 # 删除旧应用
 /usr/bin/sudo /bin/rm -rf "/Applications/$APP_NAME.app"

 # 复制新应用
 /usr/bin/sudo /bin/cp -Rf "$SOURCE_DIR" "/Applications/"

 # 设置权限
 /usr/bin/sudo /usr/sbin/chown -R $SYS_USER_NAME:staff "/Applications/$APP_NAME.app"
 /usr/bin/sudo /usr/bin/xattr -d com.apple.quarantine "/Applications/$APP_NAME.app"
 /usr/bin/sudo /bin/chmod -R 755 "/Applications/$APP_NAME.app"

 # 重新签名
 /usr/bin/sudo /usr/bin/codesign --force --deep --sign - "/Applications/$APP_NAME.app"

 # 清理
 /bin/rm -rf "$TEMP_DIR"
 /bin/rm -f "$0"

 # 启动新应用
 /usr/bin/open -a "/Applications/$APP_NAME.app"
 exit 0