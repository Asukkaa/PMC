package priv.koishi.pmc.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Utils.FileUtils.getAppPath;

/**
 * @author KOISHI
 * Date:2025-05-16
 * Time:16:36
 */
public class ScheduledService {

    private static final String TASK_NAME = "PMCScheduled";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void createTask(LocalDateTime triggerTime, String repeatType, String PMCFilePath) throws IOException {
        if (systemName.contains(win)) {
            createWinLaunchdTask(triggerTime, repeatType, PMCFilePath);
        } else if (systemName.contains(mac)) {
            createMacLaunchdTask(triggerTime, repeatType);
        }
    }

    public static void deleteTask() throws IOException {
        if (systemName.contains(win)) {
            new ProcessBuilder("schtasks", "/delete", "/tn", TASK_NAME, "/f").start();
        } else if (systemName.contains(mac)) {
            Path plist = Paths.get(System.getProperty("user.home"),
                    "Library", "LaunchAgents", TASK_NAME + ".plist");
            Files.deleteIfExists(plist);
        }
    }

    public static String getTaskDetails() throws IOException {
        if (systemName.contains(win)) {
            Process process = new ProcessBuilder(
                    "schtasks", "/query", "/tn", TASK_NAME, "/fo", "LIST", "/v"
            ).start();
            return readProcessOutput(process);
        } else if (systemName.contains(mac)) {
            // 通过launchctl获取状态
            Process statusProcess = new ProcessBuilder(
                    "launchctl", "list", TASK_NAME
            ).start();
            String status = readProcessOutput(statusProcess);
            // 直接读取plist文件
            Path plistPath = Paths.get(System.getProperty("user.home"),
                    "Library", "LaunchAgents", TASK_NAME + ".plist");
            if (Files.exists(plistPath)) {
                String content = new String(Files.readAllBytes(plistPath));
                return "任务状态:\n" + status + "\n配置文件内容:\n" + content;
            }
            return "";
        }
        return "";
    }

    // 辅助方法：读取进程输出
    private static String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }

    private static void createWinLaunchdTask(LocalDateTime triggerTime, String repeatType, String PMCFilePath) throws IOException {
        String exePath = getAppPath();
        String workingDir = Paths.get(exePath).getParent().toString();
        // 构建 PowerShell 命令字符串
        String psCommand = String.format(
                "$action = New-ScheduledTaskAction -Execute '\"%s\"' -WorkingDirectory '%s' -Argument '-r " + PMCFilePath + "'; " +
                        "$trigger = New-ScheduledTaskTrigger -%s -At %s; " +
                        "Register-ScheduledTask -TaskName '%s' -Action $action -Trigger $trigger -Settings (New-ScheduledTaskSettingsSet -Compatibility Win8) -Force",
                exePath,
                workingDir,
                translateRepeatType(repeatType),
                triggerTime.format(TIME_FORMATTER),
                TASK_NAME
        );
        // 构建进程命令
        ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "-Command",
                psCommand
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = readProcessOutput(process);
        if (output.contains("Exception") || output.contains("错误")) {
            throw new IOException("任务创建失败: " + output);
        }
    }

    private static void createMacLaunchdTask(LocalDateTime triggerTime, String repeatType) throws IOException {
        Path plistPath = Paths.get(System.getProperty("user.home"),
                "Library", "LaunchAgents", TASK_NAME + ".plist");

        String interval = switch (repeatType) {
            case "DAILY" -> "<key>StartInterval</key><integer>86400</integer>";
            case "WEEKLY" -> "<key>StartInterval</key><integer>604800</integer>";
            default -> "<key>RunAtLoad</key><false/>";
        };

        String calendarInterval = "ONCE".equals(repeatType) ?
                String.format("""
                                <key>StartCalendarInterval</key>
                                <dict>
                                    <key>Minute</key><integer>%d</integer>
                                    <key>Hour</key><integer>%d</integer>
                                    <key>Day</key><integer>%d</integer>
                                    <key>Month</key><integer>%d</integer>
                                </dict>""",
                        triggerTime.getMinute(),
                        triggerTime.getHour(),
                        triggerTime.getDayOfMonth(),
                        triggerTime.getMonthValue()
                ) : "";

        String plistContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">\n" +
                "<dict>\n" +
                "    <key>Label</key>\n" +
                "    <string>" + TASK_NAME + "</string>\n" +
                "    <key>ProgramArguments</key>\n" +
                "    <array>\n" +
                "        <string>/usr/bin/open</string>\n" + // 使用open命令
                "        <string>-n</string>\n" + // 强制新实例
                "        <string>" + getAppPath() + "</string>\n" + // .app路径
                "        <string>--args</string>\n" + // 传递参数
                "        <string>--scheduled</string>\n" + // 定时任务标识
                "    </array>\n" +
                interval + "\n" +
                calendarInterval + "\n" +
                "</dict>\n" +
                "</plist>";

        Files.write(plistPath, plistContent.getBytes());

        // 加载任务
        new ProcessBuilder("launchctl", "load", plistPath.toString()).start();
    }

    private static String translateRepeatType(String type) {
        return switch (type) {
            case "每天" -> "DAILY";
            case "每周" -> "WEEKLY";
            default -> "ONCE";
        };
    }

}
