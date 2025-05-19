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

    /**
     * 定时任务名称
     */
    private static final String TASK_NAME = "PMCScheduled";

    /**
     * 定时任务时间格式
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 创建定时任务
     *
     * @param triggerTime 触发时间
     * @param repeatType  重复类型
     * @param PMCFilePath PMC文件路径
     * @throws IOException 任务创建失败
     */
    public static void createTask(LocalDateTime triggerTime, String repeatType, String PMCFilePath) throws IOException {
        if (systemName.contains(win)) {
            createWinLaunchdTask(triggerTime, repeatType, PMCFilePath);
        } else if (systemName.contains(mac)) {
            createMacLaunchdTask(triggerTime, repeatType, PMCFilePath);
        }
    }

    /**
     * 删除定时任务
     *
     * @throws IOException 删除任务失败
     */
    public static void deleteTask() throws IOException {
        if (systemName.contains(win)) {
            new ProcessBuilder("schtasks", "/delete", "/tn", TASK_NAME, "/f").start();
        } else if (systemName.contains(mac)) {
            Path plist = Paths.get(userHome, "Library", "LaunchAgents", TASK_NAME + ".plist");
            Files.deleteIfExists(plist);
        }
    }

    /**
     * 获取定时任务详情
     *
     * @return 定时任务详情
     * @throws IOException 获取任务详情失败
     */
    public static String getTaskDetails() throws IOException {
        if (systemName.contains(win)) {
            Process process = new ProcessBuilder("schtasks", "/query", "/tn", TASK_NAME, "/fo", "LIST", "/v").start();
            return readProcessOutput(process);
        } else if (systemName.contains(mac)) {
            // 通过launchctl获取状态
            Process statusProcess = new ProcessBuilder("launchctl", "list", TASK_NAME).start();
            String status = readProcessOutput(statusProcess);
            // 直接读取plist文件
            Path plistPath = Paths.get(userHome, "Library", "LaunchAgents", TASK_NAME + ".plist");
            if (Files.exists(plistPath)) {
                String content = new String(Files.readAllBytes(plistPath));
                return "任务状态:\n" + status + "\n配置文件内容:\n" + content;
            }
            return "";
        }
        return "";
    }

    /**
     * 读取定时任务结果
     *
     * @param process 读取进程
     * @return 执行结果
     * @throws IOException 读取失败
     */
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

    /**
     * 创建win定时任务
     *
     * @param triggerTime 触发时间
     * @param repeatType  重复类型
     * @param PMCFilePath PMC文件路径
     * @throws IOException 创建失败
     */
    private static void createWinLaunchdTask(LocalDateTime triggerTime, String repeatType, String PMCFilePath) throws IOException {
        String exePath = getAppPath();
        String workingDir = Paths.get(exePath).getParent().toString();
        // 构建 PowerShell 命令字符串
        String psCommand = String.format(
                "$action = New-ScheduledTaskAction -Execute '\"%s\"' -WorkingDirectory '%s' -Argument '--r " + PMCFilePath + "'; " +
                        "$trigger = New-ScheduledTaskTrigger -%s -At %s; " +
                        "Register-ScheduledTask -TaskName '%s' -Action $action -Trigger $trigger -Settings (New-ScheduledTaskSettingsSet -Compatibility Win8) -Force",
                exePath,
                workingDir,
                repeatTypeMap.get(repeatType),
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

    /**
     * 创建mac定时任务
     *
     * @param triggerTime 触发时间
     * @param repeatType  重复类型
     * @param PMCFilePath PMC文件路径
     * @throws IOException 创建失败
     */
    private static void createMacLaunchdTask(LocalDateTime triggerTime, String repeatType, String PMCFilePath) throws IOException {
        Path plistPath = Paths.get(userHome, "Library", "LaunchAgents", TASK_NAME + ".plist");
        repeatType = repeatTypeMap.get(repeatType);
        String interval = switch (repeatType) {
            case "DAILY" -> "<key>StartInterval</key><integer>86400</integer>";
            case "WEEKLY" -> "<key>StartInterval</key><integer>604800</integer>";
            case "MONTHLY" -> "<key>StartInterval</key><integer>2592000</integer>";
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
                "        <string>--r " + PMCFilePath + "</string>\n" + // 定时任务标识
                "    </array>\n" +
                interval + "\n" +
                calendarInterval + "\n" +
                "</dict>\n" +
                "</plist>";
        Files.write(plistPath, plistContent.getBytes());
        // 先卸载旧配置
        new ProcessBuilder("launchctl", "unload", plistPath.toString()).start();
        // 加载任务
        new ProcessBuilder("launchctl", "load", plistPath.toString()).start();
    }

}
