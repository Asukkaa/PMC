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
appContents="$target/$appFile/Contents"
app="$appContents/app"
appBin="$appContents/runtime/Contents/Home/bin"
InfoPlist="$appContents/Info.plist"
appVersion="2.1.0"
appMainClass="priv.koishi.pmc/priv.koishi.pmc.MainApplication"
runtimeImage="app"
language="zh_CN"

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
(cd "$target" && jpackage --name "$appName" --type app-image -m "$appMainClass" --runtime-image "$runtimeImage" --icon "$appIcon" --app-version "$appVersion")
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