#!/bin/bash

# 强制定位到脚本目录
script_dir=$(cd "$(dirname "$0")" || exit 1; pwd)
cd "$script_dir" || exit 1
source="$script_dir/mac"
target="$script_dir/../target"
appIcon="$script_dir/PMC.icns"
bin="$target/app/bin"
appName="Perfect Mouse Control"
appFile="$appName.app"
appFullPath="$target/$appFile"
appContents="$appFullPath/Contents"
app="$appContents/app"
appBin="$appContents/runtime/Contents/Home/bin"
InfoPlist="$appContents/Info.plist"
appMainClass="priv.koishi.pmc/priv.koishi.pmc.MainApplication"
runtimeImage="app"
language="zh_CN"

# 关闭正在运行的程序
echo "强制关闭正在运行的 $appName..."
pkill -9 -f "$appName" && echo "已强制终止 $appName" || echo "未检测到 $appName 运行进程"

# 从 Java 文件提取版本号
src="$script_dir/../src"
javaFile="$src/main/java/priv/koishi/pmc/Finals/CommonFinals.java"

# 检查含有版本号信息的Java文件存在性
if [ ! -f "$javaFile" ]; then
    echo "错误：Java文件不存在于路径 [$javaFile]" >&2
    exit 1
fi

# 使用 sed 方案提取版本号（兼容 macOS BSD sed）
appVersion=$(sed -E -n 's/.*public[[:space:]]+static[[:space:]]+final[[:space:]]+String[[:space:]]+version[[:space:]]*=[[:space:]]*"([^"]*)".*/\1/p' "$javaFile")

# 错误检查
if [ -z "$appVersion" ]; then
    echo "错误：无法从 $javaFile 中提取版本号" >&2
    echo "请确认版本号声明格式为：public static final String version = \"x.x.x\";" >&2
    exit 1
fi
echo "已提取版本号：$appVersion"

# 复制文件并处理zip压缩包
mkdir -p "$bin"
find "$source" -type f -name "*.zip" -exec sh -c '
    zip_file="$0"
    rel_path=$(dirname "${zip_file#$1/}")
    target_dir="$2/$rel_path"
    mkdir -p "$target_dir"
    unzip -oq "$zip_file" -d "$target_dir"
    rm -rf "$target_dir"/__MACOSX 2>/dev/null
    echo "已解压 [$zip_file] 到 [$target_dir]"
' {} "$source" "$bin" \;

# 复制其他非zip文件（保留原目录结构）
rsync -av --exclude='*.zip' "$source"/ "$bin"/

# 清理旧构建
if [ -d "$target/$appFile" ]; then
    echo "发现已存在的 [$appFile] 目录，正在清理..."
    rm -rf "${target:?}/${appFile:?}"
fi

# 执行打包
(cd "$target" && jpackage --name "$appName" --type app-image -m "$appMainClass" --runtime-image "$runtimeImage" --icon "$appIcon" --app-version "$appVersion" --java-options "-XX:+UseZGC")
echo "已完成 jpackage 打包"

# 移动动态库文件
echo "正在迁移动态库文件..."
# 确保目标目录存在
mkdir -p "$app"
# 带错误检查的移动操作
mv -v "$appBin"/*.dylib "$app"/ || exit 1
echo "已完成 .dylib 文件迁移到 [$app]"

# 修改Info.plist配置
echo "正在本地化配置..."
if [ -f "$InfoPlist" ]; then
    /usr/libexec/PlistBuddy -c "Set :CFBundleDevelopmentRegion $language" "$InfoPlist" || exit 1
    echo "已更新 [$InfoPlist] 的 CFBundleDevelopmentRegion 为 $language"
else
    echo "错误：找不到 Info.plist 文件" >&2
    exit 1
fi

appZipFile="$target/${appName}-${appVersion}-mac.zip"

# 创建更新需要的app压缩包
create_app_zip() {
    echo "正在创建 app zip 文件：$appZipFile"
    # 清理旧文件
    rm -f "${target}/${appName}-"*.zip 2>/dev/null
    # 执行压缩（保留符号链接和权限）
    (cd "$target" && zip -r -y "$appZipFile" "$appFile") || {
        echo "错误：app zip 压缩失败" >&2
        exit 1
    }
    echo "成功生成 app zip 文件：$appZipFile"
}

libZipFile="$target/lib-${appVersion}-mac.zip"

# 创建更新需要的lib压缩包
create_lib_zip() {
    echo "正在创建 lib zip 文件：$libZipFile"
   # 清理旧文件
    rm -f "${target}/lib-"*.zip 2>/dev/null
    # 执行压缩（保留符号链接和权限）
    (cd "$target/app" && zip -r -y "$libZipFile" "lib") || {
        echo "错误：lib zip 压缩失败" >&2
        exit 1
    }
    echo "成功生成 lib zip 文件：$libZipFile"
}

dmgName="${appName} ${appVersion}"
volumeName="${appName}"
dmgTemp="${target}/${dmgName}-temp.dmg"
dmgFinal="${target}/${dmgName}.dmg"
backgroundImage="dmg背景.png"
dmgSize="1000m"

create_dmg() {
    echo "正在卸载残留挂载点..."
    # 卸载旧挂载点
    for _ in {1..3}; do
        hdiutil eject "/Volumes/$volumeName" -force 2>/dev/null
        sleep 1
    done

    echo "正在检查旧 DMG 文件..."
    # 使用 find 命令检测是否存在匹配文件
    if find "${target}" -maxdepth 1 -name "${appName}*.dmg" -o -name "${appName}-temp.dmg.sparseimage" | grep -q .; then
        echo "发现存在的旧 DMG 文件，正在清理..."
        rm -f "${target}/${appName}"*.dmg "${target}/${appName}-temp.dmg.sparseimage" >/dev/null 2>&1
        if rm -f "${target}/${appName}"*.dmg "${target}/${appName}-temp.dmg.sparseimage" >/dev/null 2>&1; then
            echo "已清理：以 [${appName}] 开头的 .dmg 和 .sparseimage 文件"
        else
            echo "警告：清理旧文件时遇到问题" >&2
        fi
    else
        echo "未发现需要清理的旧 DMG 文件"
    fi

    # 创建临时 DMG 文件
    echo "正在创建临时 DMG 文件..."
    rm -f "$dmgTemp" "$dmgFinal"

    #创建临时镜像
    hdiutil create -size ${dmgSize} -fs HFSJ -volname "$volumeName" -type SPARSE -layout NONE "$dmgTemp" >/dev/null || exit 1

    # 挂载镜像并捕获输出
    mountOutput=$(hdiutil mount -nobrowse -readwrite -owners on "$dmgTemp.sparseimage" 2>&1)
    mountPoint=$(echo "$mountOutput" | awk -F'\t' '/\/Volumes/{print $3}')

    # 载点验证
    if [[ "$mountPoint" != "/Volumes/$volumeName"* ]] || [ ! -w "$mountPoint" ]; then
        echo "错误：挂载失败，输出信息：$mountOutput" >&2
        exit 1
    fi

    # 复制应用程序到挂载点
    echo "复制 $appFullPath 到 DMG 卷中..."
    cp -R "$appFullPath" "$mountPoint/" || exit 1
    # 添加应用程序目录快捷方式
    ln -s "/Applications" "$mountPoint/Applications"

    # 设置背景图和窗口样式
    echo "配置 DMG 样式..."
    mkdir "$mountPoint/.background"
    cp "$backgroundImage" "$mountPoint/.background/"

    # 验证背景图片是否存在
    if [ ! -f "$mountPoint/.background/$backgroundImage" ]; then
        echo "错误：背景图片复制失败" >&2
        exit 1
    fi

    # 创建 .DS_Store 配置
    osascript <<EOD
    -- 前置检查：确保 Finder 运行
    do shell script "osascript -e 'delay 0.1' -e 'tell application \"Finder\" to activate'"

    -- 显式启动 Finder 并等待
    tell application "Finder"
        activate
        delay 2  -- 必须的启动等待时间

        -- 获取磁盘引用（增强兼容性）
        set targetDisk to a reference to disk "$volumeName"

        -- 双重验证机制
        repeat 10 times
            if exists targetDisk then exit repeat
            delay 1
        end repeat

        if not (exists targetDisk) then
            error "无法找到磁盘卷"
        end if

        -- 窗口初始化流程
        set maxRetry to 5
        set success to false
        repeat maxRetry times
            try
                -- 创建窗口引用
                set win to make new Finder window
                set target of win to targetDisk
                set current view of win to icon view
                set bounds of win to {200, 200, 1224, 700}

                -- 配置视图选项
                set opts to icon view options of win
                tell opts
                    set arrangement to not arranged
                    set background picture to (POSIX file "$mountPoint/.background/$backgroundImage") as alias
                    set icon size to 128  -- 设置图标尺寸
                end tell

                -- 定位应用程序图标
                set appItem to (first item of targetDisk whose name ends with ".app")
                set position of appItem to {180, 200}
                set applicationsAlias to (first item of targetDisk whose name is "Applications")
                set position of applicationsAlias to {630, 200}

                -- 成功标志
                set success to true
                exit repeat
            on error
                delay 2
                do shell script "killall Finder"
                activate
            end try
        end repeat

        if not success then
            error "窗口配置失败"
        end if

        -- 清理操作
        close win
    end tell
EOD

    # 清理 Finder 缓存，卸载 DMG
    echo "清理并打包最终 DMG..."
    hdiutil eject "$mountPoint" >/dev/null || exit 1

    # 将 sparseimage 转换为正式 dmg
    hdiutil convert "$dmgTemp.sparseimage" -format UDZO -o "$dmgFinal" >/dev/null || exit 1

    # 压缩 DMG 并清理临时文件
    rm -f "$dmgTemp.sparseimage"
    echo "DMG 已生成：$dmgFinal"
}

# 并行执行三个任务
echo "开始并行打包任务..."
create_app_zip &
create_lib_zip &
create_dmg &
# 等待所有后台任务完成
wait

# 自动打开Finder并选中生成的文件
echo "正在打开构建目录：${target}"
osascript <<EOL
tell application "Finder"
    activate
    set targetFolder to (POSIX file "$target") as alias
    open targetFolder -- 打开目录
    delay 1 -- 等待目录加载
    select {POSIX file "$appFullPath" as alias, POSIX file "$dmgFinal" as alias, POSIX file "$appZipFile" as alias, POSIX file "$libZipFile" as alias} -- 多选文件
end tell
EOL
