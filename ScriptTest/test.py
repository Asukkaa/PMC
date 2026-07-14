import os
import sys
import time


def show_progress(seconds):
    """显示进度条（保持不变）"""
    for i in range(seconds):
        progress = (i + 1) / seconds * 100
        bar_length = 30
        filled_length = int(bar_length * (i + 1) // seconds)
        bar = '█' * filled_length + '░' * (bar_length - filled_length)
        sys.stdout.write(f'\r等待中: [{bar}] {progress:.0f}% ({i + 1}/{seconds}秒)')
        sys.stdout.flush()
        time.sleep(1)
    print()


def main():
    # ========== 版本信息 ==========
    print("=== Python 脚本测试 ===")
    python_version = sys.version
    platform = sys.platform
    print(f"Python 版本: {python_version} ({platform})")

    # ========== 工作目录 ==========
    current_dir = os.getcwd()
    print(f"工作目录: {current_dir}")

    # ========== 参数 ==========
    args = sys.argv[1:]
    print("接收到的参数:")
    if not args:
        print("  无参数")
    else:
        for i, arg in enumerate(args, 1):
            print(f"  参数{i}: '{arg}'")

    # ========== 当前目录文件列表 ==========
    print("\n当前目录文件列表:")
    try:
        files = os.listdir(current_dir)
        for file in sorted(files):
            file_path = os.path.join(current_dir, file)
            if os.path.isdir(file_path):
                print(f"  [目录] {file}")
            else:
                print(f"  [文件] {file}")
    except Exception as e:
        print(f"  无法列出目录内容: {e}")

    # ========== 3 秒倒计时 ==========
    print("\n开始 3 秒倒计时...")
    show_progress(3)
    print("倒计时结束!")

    print("\n脚本执行完成!")


if __name__ == "__main__":
    main()
