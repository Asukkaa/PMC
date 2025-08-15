@echo off
chcp 65001 > nul

set "source=win"
set "target=..\target"
set "src=..\src"
set "bin=%target%\app\bin"
set "lib=%target%\app\lib"
set "appIcon=..\appBuilder\PMC.ico"
set "appName=Perfect Mouse Control"
set "appMainClass=priv.koishi.pmc/priv.koishi.pmc.MainApplication"
set "runtimeImage=app"
set "appPath=%target%\%appName%"

::关闭正在运行的程序
echo 关闭正在运行的程序...
taskkill /f /im "%appName%.exe" >nul 2>&1

:: 解析版本号
set "javaFile=%src%\main\java\priv\koishi\pmc\Finals\CommonFinals.java"
for /f "delims=" %%i in ('powershell -Command "(Select-String -Path '%javaFile%' -Pattern 'public static final String version = \x22(.*?)\x22;').Matches.Groups[1].Value"') do (
    set "appVersion=%%i"
)
:: 验证解析的版本号
if "%appVersion%" == "" (
    echo 版本号解析失败
    exit /b 1
)
echo 版本号：%appVersion%

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
jpackage --name "%appName%" --type app-image -m "%appMainClass%" --runtime-image "%runtimeImage%" --icon "%appIcon%" --app-version "%appVersion%" --java-options "-XX:+UseZGC"
echo 已完成 jpackage 打包

:: 生成 lib zip
set "libZipName=lib-%appVersion%-win.zip"
if exist "%target%\lib-*.zip" (
    echo 正在清理旧的 lib zip 文件...
    del /q "%target%\lib-*.zip"
)
if exist "%lib%" (
    echo 正在生成 lib 压缩包: %libZipName%
    start "" /B powershell -Command "Compress-Archive -Path '%lib%' -DestinationPath '%target%\%libZipName%' -Force; Set-Content -Path '%target%\lib.done' -Value 1"
) else (
    echo 错误：生成的 lib 目录不存在
)

:: 生成 app zip
set "appZipName=%appName%-%appVersion%-win.zip"
if exist "%target%\%appName%-*.zip" (
    echo 正在清理旧的应用程序 zip 文件...
    del /q "%target%\%appName%-*.zip"
)
if exist "%appPath%" (
    echo 正在生成 app 压缩包: %appZipName%
    start "" /B powershell -Command "Compress-Archive -Path '%appPath%\*' -DestinationPath '%target%\%appZipName%' -Force; Set-Content -Path '%target%\app.done' -Value 1"
) else (
    echo 错误：生成的应用程序目录不存在
)

:: 等待压缩完成
echo 等待压缩任务完成...
:WaitLoop
if not exist "%target%\lib.done" goto :WaitLoop
if not exist "%target%\app.done" goto :WaitLoop

:: 清理标记文件并打开目录
del "%target%\lib.done" "%target%\app.done"
echo 压缩完成，正在打开目录: %appPath%
explorer /select,"%appPath%"

pause
