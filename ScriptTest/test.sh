#!/bin/bash

echo "=== Shell 脚本测试开始 ==="
echo "脚本路径: $0"
echo "工作目录: $(pwd)"
echo "启动时间: $(date)"

# 输出所有传入的参数
echo "=== 启动参数 ==="
if [ $# -eq 0 ]; then
    echo "没有传入任何参数"
else
    echo "参数个数: $#"
    for i in $(seq 1 $#); do
        echo "参数 $i: ${!i}"
    done
fi

echo ""
echo "=== 工作目录下的文件 ==="
# 列出当前工作目录下的所有文件和文件夹
if command -v ls >/dev/null 2>&1; then
    ls -la
else
    # 如果 ls 命令不可用，使用 find 命令
    find . -maxdepth 1 -type f -o -type d
fi

echo ""
echo "=== 3秒倒计时开始 ==="

# 3秒倒计时
for i in {3..1}; do
    echo "倒计时: $i 秒"
    sleep 1
done

echo "倒计时结束!"
echo ""
echo "=== 环境信息 ==="
echo "当前用户: $(whoami)"
echo "Shell: $SHELL"
echo "Python3 路径: $(which python3 2>/dev/null || echo '未找到')"
echo "Java 版本: $(java -version 2>&1 | head -n 1)"

echo ""
echo "=== 测试完成 ==="
echo "完成时间: $(date)"
echo "脚本执行完毕，即将退出..."