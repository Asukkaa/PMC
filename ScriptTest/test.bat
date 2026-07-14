@echo off
chcp 65001 > nul
echo 脚本开始执行，参数: %*
echo 当前工作目录: %cd%
echo 当前目录下的文件和文件夹:
dir /b
echo 等待 3 秒...
timeout /t 1 /nobreak > nul
echo 等待 2 秒...
timeout /t 1 /nobreak > nul
echo 等待 1 秒...
timeout /t 1 /nobreak > nul
echo 脚本执行完成
