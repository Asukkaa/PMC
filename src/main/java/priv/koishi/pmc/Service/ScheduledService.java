package priv.koishi.pmc.Service;

import priv.koishi.pmc.Bean.TimedTaskBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Utils.FileUtils.getAppPath;
import static priv.koishi.pmc.Utils.FileUtils.getFileName;

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
     * @param days        指定的星期
     * @throws IOException 任务创建失败
     */
    public static void createTask(LocalDateTime triggerTime, String repeatType, String PMCFilePath, List<Integer> days) throws IOException {
        if (systemName.contains(win)) {
            createWinLaunchdTask(triggerTime, repeatType, PMCFilePath, days);
        } else if (systemName.contains(mac)) {
            createMacLaunchdTask(triggerTime, repeatType, PMCFilePath, days);
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
            Path plistFile = Paths.get(userHome, "Library", "LaunchAgents", TASK_NAME + plist);
            Files.deleteIfExists(plistFile);
        }
    }

    private static String extractValue(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * 获取定时任务详情
     *
     * @return 定时任务详情
     * @throws IOException 获取任务详情失败
     */
    public static List<TimedTaskBean> getTaskDetails() throws IOException {
        List<TimedTaskBean> taskDetails = new ArrayList<>();
        TimedTaskBean timedTaskBean = new TimedTaskBean();
        if (systemName.contains(win)) {
            Process process = new ProcessBuilder("schtasks", "/query", "/tn", TASK_NAME, "/fo", "LIST", "/v").start();
            String output = readProcessOutput(process);
            System.out.println(output);
            // 解析Windows任务信息
            Pattern startDatePattern = Pattern.compile("Start Date:\\s+(.*?)\\n");
            Pattern startTimePattern = Pattern.compile("Start Time:\\s+(.*?)\\n");
            Pattern scheduleTypePattern = Pattern.compile("Schedule Type:\\s+(.*?)\\n");
            Pattern taskToRunPattern = Pattern.compile("Task To Run:\\s+(.*?)\\n");
            Pattern DaysPattern = Pattern.compile("Days:\\s+(.*?)\\n");
            Pattern taskNamePattern = Pattern.compile("TaskName:\\s+(.*?)\\n");
            // 提取各字段值
            String startDate = extractValue(startDatePattern, output);
            String startTime = extractValue(startTimePattern, output);
            String scheduleType = extractValue(scheduleTypePattern, output).toUpperCase().trim();
            String taskToRun = extractValue(taskToRunPattern, output);
            String days = extractValue(DaysPattern, output);
            String taskName = extractValue(taskNamePattern, output);
            if ("ONE TIME ONLY".equals(scheduleType)) {
                scheduleType = ONCE;
            }
            String repeatType = repeatTypeMap.getKey(scheduleType);
            String daysCN = Arrays.stream(days.split(",\\s*"))
                    .map(day -> dayOfWeekName.getOrDefault(day.trim().toUpperCase(), ""))
                    .collect(Collectors.joining(", "));
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime time = LocalTime.parse(startTime, inputFormatter);
            timedTaskBean.setTaskName(taskName.substring(taskName.indexOf("\\") + 1))
                    .setTime(time.format(TIME_FORMATTER))
                    .setPath(text_onlyLaunch)
                    .setName(text_onlyLaunch)
                    .setRepeat(repeatType)
                    .setDate(startDate)
                    .setDays(daysCN);
            if (taskToRun.contains(PMC)) {
                String path = taskToRun.substring(taskToRun.lastIndexOf(r) + r.length());
                String name = getFileName(path);
                timedTaskBean.setPath(path)
                        .setName(name);
            }
            if ("Every day of the week".equals(days) || DAILY_CN.equals(repeatType)) {
                timedTaskBean.setDays(DAILY_CN);
            } else if (ONCE_CN.equals(repeatType)) {
                timedTaskBean.setDays(ONCE_CN);
            }
        } else if (systemName.contains(mac)) {
            Path plistPath = Paths.get(userHome, "Library", "LaunchAgents", TASK_NAME + ".plist");
            if (Files.exists(plistPath)) {
                String content = new String(Files.readAllBytes(plistPath));
                // 解析macOS任务信息
                Pattern pathPattern = Pattern.compile("<string>--r\\s*(.+)</string>");
                Matcher pathMatcher = pathPattern.matcher(content);
                if (pathMatcher.find()) {
                    timedTaskBean.setPath(pathMatcher.group(1));
                    timedTaskBean.setName(getFileName(timedTaskBean.getPath()));
                }
                if (content.contains("StartInterval")) {
                    if (content.contains("86400")) {
                        timedTaskBean.setRepeat("daily");
                    } else if (content.contains("604800")) {
                        timedTaskBean.setRepeat("weekly");
                    } else if (content.contains("2592000")) {
                        timedTaskBean.setRepeat("monthly");
                    }
                } else {
                    Pattern datePattern = Pattern.compile(
                            "<key>StartCalendarInterval</key>\\s*<dict>\\s*<key>Hour</key><integer>(\\d+)" +
                                    "</integer>\\s*<key>Minute</key><integer>(\\d+)</integer>\\s*<key>Day</key>" +
                                    "<integer>(\\d+)</integer>\\s*<key>Month</key><integer>(\\d+)</integer>");
                    Matcher dateMatcher = datePattern.matcher(content);
                    if (dateMatcher.find()) {
                        int hour = Integer.parseInt(dateMatcher.group(1));
                        int minute = Integer.parseInt(dateMatcher.group(2));
                        int day = Integer.parseInt(dateMatcher.group(3));
                        int month = Integer.parseInt(dateMatcher.group(4));
                        LocalDateTime triggerTime = LocalDateTime.of(2025, month, day, hour, minute);
                        timedTaskBean.setDate(TIME_FORMATTER.format(triggerTime));
                        timedTaskBean.setTime(TIME_FORMATTER.format(triggerTime));
                        timedTaskBean.setRepeat("once");
                    }
                }
            }
        }
        taskDetails.add(timedTaskBean);
        return taskDetails;
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
     * @param days        指定的星期
     * @throws IOException 创建失败
     */
    private static void createWinLaunchdTask(LocalDateTime triggerTime, String repeatType, String PMCFilePath, List<Integer> days) throws IOException {
        String exePath = getAppPath();
        String workingDir = Paths.get(exePath).getParent().toString();
        // 构建基础命令
        StringBuilder psCommand = new StringBuilder();
        psCommand.append("$action = New-ScheduledTaskAction -Execute '\"").append(exePath).append("'\" ")
                .append("-WorkingDirectory '").append(workingDir).append("' ")
                .append("-Argument '--r ").append(PMCFilePath).append("'; ");
        // 构建触发器
        psCommand.append("$triggers = @(); ");
        switch (repeatTypeMap.get(repeatType)) {
            case DAILY: {
                psCommand.append("$trigger = New-ScheduledTaskTrigger -Daily -At '").append(triggerTime.format(TIME_FORMATTER)).append("'; ");
                psCommand.append("$triggers += $trigger; ");
                break;
            }
            case WEEKLY: {
                for (int day : days) {
                    // 使用 -Weekly + -DaysOfMonth 实现每月指定日期
                    psCommand.append(String.format(
                            "$trigger = New-ScheduledTaskTrigger -Weekly -At '%s' -DaysOfWeek %d; $triggers += $trigger; ",
                            triggerTime.format(TIME_FORMATTER), day));
                }
                break;
            }
            case ONCE: {
                // 添加单次触发器
                psCommand.append(String.format("$trigger = New-ScheduledTaskTrigger -Once -At '%s'; $triggers += $trigger; ",
                        triggerTime.format(TIME_FORMATTER)));
                break;
            }
        }
        // 注册任务（支持多个触发器）
        psCommand.append(String.format("Register-ScheduledTask -TaskName '%s' -Action $action -Trigger $triggers -Settings (New-ScheduledTaskSettingsSet -Compatibility Win8) -Force",
                TASK_NAME));
        // 执行 PowerShell 命令
        ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", psCommand.toString());
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
     * @param days        指定的星期
     * @throws IOException 创建失败
     */
    private static void createMacLaunchdTask(LocalDateTime triggerTime, String repeatType, String PMCFilePath, List<Integer> days) throws IOException {
        Path plistPath = Paths.get(userHome, "Library", "LaunchAgents", TASK_NAME + plist);
        repeatType = repeatTypeMap.get(repeatType);
        String interval;
        switch (repeatType) {
            case DAILY: {
                interval = "<key>StartInterval</key><integer>86400</integer>";
                break;
            }
            case WEEKLY: {
                // 支持多天执行（如每周一、三）
                interval = days.stream().map(day -> String.format("""
                                        <key>StartCalendarInterval</key>
                                        <dict>
                                            <key>Hour</key><integer>%d</integer>
                                            <key>Minute</key><integer>%d</integer>
                                            <key>Weekday</key><integer>%d</integer>
                                        </dict>""",
                                triggerTime.getHour(),
                                triggerTime.getMinute(),
                                day))
                        .collect(Collectors.joining());
                break;
            }
            default: {
                interval = "<key>RunAtLoad</key><false/>";
                break;
            }
        }
        String calendarInterval = ONCE.equals(repeatType) ?
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
                "        <string>/usr/bin/open</string>\n" +
                "        <string>-n</string>\n" +
                "        <string>" + getAppPath() + "</string>\n" +
                "        <string>--args</string>\n" +
                "        <string>--r " + PMCFilePath + "</string>\n" +
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
