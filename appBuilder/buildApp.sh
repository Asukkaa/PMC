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

# 自动打开Finder并选中生成的APP文件 (macOS only)
if [ -d "$appFullPath" ]; then
    echo "正在打开构建目录：$appFullPath"
    open -R "$appFullPath"
else
    echo "错误：生成的APP文件不存在" >&2
    exit 1
fi
