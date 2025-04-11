@echo off

set "source=win"
set "target=..\target"
set "bin=%target%\app\bin"
set "appIcon=..\appBuilder\PMC.ico"
set "appName=Perfect Mouse Control"
set "appVersion=2.1.1"
set "appMainClass=priv.koishi.pmc/priv.koishi.pmc.MainApplication"
set "runtimeImage=app"

:: 处理ZIP文件
for /r "%source%" %%F in (*.zip) do (
    set "zipPath=%%F"
    setlocal enabledelayedexpansion
    :: 使用绝对路径解压
    tar -xf "!zipPath!" -C "%bin%" --exclude="__MACOSX"
    endlocal
)

:: 复制其他非ZIP文件
robocopy "%source%" "%bin%" /E /XF *.zip
echo 已复制非ZIP文件到 [%bin%]

:: 清理旧构建
pushd "%target%"
if exist "%appName%" (
    echo 发现已存在的 [%appName%] 目录，正在清理...
    rmdir /s /q "%appName%"
)

:: 执行打包
jpackage --name "%appName%" --type app-image -m "%appMainClass%" --runtime-image "%runtimeImage%" --icon "%appIcon%" --app-version "%appVersion%"
echo 已完成 jpackage 打包

pause
