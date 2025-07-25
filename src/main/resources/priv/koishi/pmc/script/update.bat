@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

:: 接收Java传递的参数
set "source_dir=%~1"
set "target_dir=%~2"
set "exe_name=%~3"
set "temp_dir=%~4"
set "app_root_Path=%~5"
set "app_pid=%~6"

:: 检查管理员权限
net session >nul 2>&1
if %errorlevel% == 0 (
    goto :AdminSuccess
) else (
    goto :UACPrompt
)

:UACPrompt
:: 通过VBS提权
%1 mshta vbscript:CreateObject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 %*","","runas",1)(window.close)&&exit

:AdminSuccess

:: 结束JavaFX应用程序
if defined app_pid (
    taskkill /f /pid %app_pid% >nul 2>&1
) else (
    taskkill /f /im "%exe_name%" >nul 2>&1
)

:: 复制更新文件
robocopy "%source_dir%" "%target_dir%" /E /IS /IT /R:3 /W:1 /NP

:: 启动新版本应用
start "" "%app_root_Path%\%exe_name%"

:: 清理临时文件
start /B cmd /c "rd /s /q "%temp_dir%" & del /f /q "%self%""

:: 自删除逻辑
set "self=%~f0"
call :DelayedSelfDelete
exit /b

:DelayedSelfDelete
del /f /q "%self%"
exit /b