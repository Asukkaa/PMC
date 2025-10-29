# test.py
import sys
import time

def show_progress(seconds):
    """显示进度条"""
    for i in range(seconds):
        progress = (i + 1) / seconds * 100
        bar_length = 30
        filled_length = int(bar_length * (i + 1) // seconds)
        bar = '█' * filled_length + '░' * (bar_length - filled_length)
        sys.stdout.write(f'\r等待中: [{bar}] {progress:.0f}% ({i+1}/{seconds}秒)')
        sys.stdout.flush()
        time.sleep(1)
    print()  # 换行

def main():
    print("Python 脚本开始执行")
    
    # 获取所有参数
    args = sys.argv[1:]
    print(f"接收到的参数: {args}")
    
    print("等待3秒...")
    show_progress(3)
    
    # 输出参数详情
    print("\n参数详情:")
    for i, arg in enumerate(args, 1):
        print(f"  参数{i}: '{arg}'")
    
    print(f"\n所有参数: {args}")
    print("脚本执行完成")
    
    sys.exit(0)

if __name__ == "__main__":
    main()