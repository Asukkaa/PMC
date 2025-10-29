import java.io.File;
import java.util.concurrent.TimeUnit;

public class test {

    void main(String[] args) {
        IO.println("=== Java 文件测试 ===");

        // 输出工作路径
        String workingDir = System.getProperty("user.dir");
        IO.println("工作路径: " + workingDir);

        // 输出运行参数
        IO.println("接收到的参数:");
        if (args.length == 0) {
            IO.println("  无参数");
        } else {
            for (int i = 0; i < args.length; i++) {
                IO.println("  参数" + (i + 1) + ": " + args[i]);
            }
        }

        // 显示当前目录的文件列表
        IO.println("\n当前目录文件列表:");
        File currentDir = new File(workingDir);
        File[] files = currentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                String type = file.isDirectory() ? "[目录]" : "[文件]";
                IO.println("  " + type + " " + file.getName());
            }
        }

        // 3秒倒计时
        IO.println("\n开始 3 秒倒计时...");
        try {
            for (int i = 3; i > 0; i--) {
                IO.println("倒计时: " + i + " 秒");
                TimeUnit.SECONDS.sleep(1);
            }
            IO.println("倒计时结束!");
        } catch (InterruptedException e) {
            IO.println("倒计时被中断");
            Thread.currentThread().interrupt();
        }
        IO.println("\n文件执行完成!");
    }

}
