import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author KOISHI
 * Date:2025-10-29
 * Time:18:04
 */
public class test {

    public static void main(String[] args) {
        System.out.println("=== Java 程序测试 ===");

        // 输出工作路径
        String workingDir = System.getProperty("user.dir");
        System.out.println("工作路径: " + workingDir);

        // 输出运行参数
        System.out.println("接收到的参数:");
        if (args.length == 0) {
            System.out.println("  无参数");
        } else {
            for (int i = 0; i < args.length; i++) {
                System.out.println("  参数" + (i + 1) + ": " + args[i]);
            }
        }

        // 显示当前目录的文件列表
        System.out.println("\n当前目录文件列表:");
        File currentDir = new File(workingDir);
        File[] files = currentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                String type = file.isDirectory() ? "[目录]" : "[文件]";
                System.out.println("  " + type + " " + file.getName());
            }
        }

        // 3秒倒计时
        System.out.println("\n开始 3 秒倒计时...");
        try {
            for (int i = 3; i > 0; i--) {
                System.out.println("倒计时: " + i + " 秒");
                TimeUnit.SECONDS.sleep(1);
            }
            System.out.println("倒计时结束!");
        } catch (InterruptedException e) {
            System.out.println("倒计时被中断");
            Thread.currentThread().interrupt();
        }

        System.out.println("\n程序执行完成!");
    }

}
