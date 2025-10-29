@echo off
chcp 65001 > nul
echo 脚本开始执行，参数: %*
echo 等待3秒...
timeout /t 1 /nobreak > nul
echo 等待2秒...
timeout /t 1 /nobreak > nul
echo 等待1秒...
timeout /t 1 /nobreak > nul
echo 输出结果: 参数1="%~1", 参数2="%~2", 参数3="%~3"
echo 所有参数: %*
echo 脚本执行完成
exit 0