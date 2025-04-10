@echo off
set "source=win"
set "bin=..\target\app\bin"
set "target=..\target"
set "appName=Perfect Mouse Control"
set "appVersion=2.1.0"
set "appIcon=..\appBuilder\PMC.ico"
set "appMainClass=priv.koishi.pmc/priv.koishi.pmc.MainApplication"
set "runtimeImage=app"

xcopy /E /I /Y "%source%\*" "%bin%\"
echo 已复制 [%source%] 及其子目录内容到 [%bin%]

pushd "%target%"
if exist "%appName%" (
    echo 发现已存在的 [%appName%] 目录，正在清理...
    rmdir /s /q "%appName%"
)
jpackage --name "%appName%" --type app-image -m "%appMainClass%" --runtime-image "%runtimeImage%" --icon "%appIcon%" --app-version "%appVersion%"
echo 已完成 jpackage 打包

pause