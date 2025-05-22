package priv.koishi.pmc.Service;

import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.TimedTaskBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static final String TASK_NAME = "PMC-";

    /**
     * 读取定时任务时间格式
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 创建定时任务时间格式
     */
    private static final DateTimeFormatter FULL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 创建定时任务
     *
     * @param timedTaskBean 定时任务信息
     * @throws IOException 任务创建失败
     */
    public static void createTask(TimedTaskBean timedTaskBean) throws IOException {
        if (systemName.contains(win)) {
            createWinLaunchdTask(timedTaskBean);
        } else if (systemName.contains(mac)) {
            createMacLaunchdTask(timedTaskBean);
        }
    }

    /**
     * 删除定时任务
     *
     * @param taskName 定时任务名称自定义部分
     * @throws IOException 删除任务失败
     */
    public static void deleteTask(String taskName) throws IOException {
        taskName = TASK_NAME + taskName;
        if (systemName.contains(win)) {
            new ProcessBuilder("schtasks", "/delete", "/tn", taskName, "/f").start();
        } else if (systemName.contains(mac)) {
            Path plistFile = Paths.get(userHome, "Library", "LaunchAgents", taskName + plist);
            Files.deleteIfExists(plistFile);
        }
    }

    /**
     * 查询定时任务详情
     *
     * @return 定时任务详情
     */
    public static Task<List<TimedTaskBean>> getTaskDetailsTask() {
        return new Task<>() {
            @Override
            protected List<TimedTaskBean> call() throws Exception {
                updateMessage("查询中");
                List<TimedTaskBean> taskDetails = new ArrayList<>();
                if (systemName.contains(win)) {
                    getWinTaskDetails(taskDetails);
                } else if (systemName.contains(mac)) {
                    getMacTaskDetails(taskDetails);
                }
                updateMessage("");
                return taskDetails;
            }

            /**
             * 查询mac定时任务详情
             *
             * @param taskDetails 任务详情
             * @throws IOException 获取任务详情失败
             */
            private void getMacTaskDetails(List<? super TimedTaskBean> taskDetails) throws IOException {
                Path launchAgentsPath = Paths.get(userHome, "Library", "LaunchAgents");
                if (Files.exists(launchAgentsPath)) {
                    // 遍历所有以 TASK_NAME 开头的 .plist 文件
                    try (Stream<Path> stream = Files.list(launchAgentsPath)) {
                        // 先收集符合条件的文件列表
                        List<Path> filteredFiles = stream.filter(path -> path.toString().endsWith(plist)
                                && path.getFileName().toString().startsWith(TASK_NAME)).toList();
                        int dataSize = filteredFiles.size();
                        updateProgress(0, dataSize);
                        for (int i = 0; i < dataSize; i++) {
                            updateProgress(i + 1, dataSize);
                            Path path = filteredFiles.get(i);
                            try {
                                parseMacTaskContent(taskDetails, path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } catch (IOException e) {
                        throw new IOException("读取 LaunchAgents 目录失败: " + launchAgentsPath, e);
                    }
                }
            }

            /**
             * 解析mac定时任务详情
             *
             * @param taskDetails 任务详情
             * @param plistPath plist文件路径
             * @throws IOException 获取任务详情失败
             */
            private void parseMacTaskContent(List<? super TimedTaskBean> taskDetails, Path plistPath) throws IOException {
                if (Files.exists(plistPath)) {
                    TimedTaskBean timedTaskBean = new TimedTaskBean();
                    String content = new String(Files.readAllBytes(plistPath));
                    // 解析任务名称
                    Pattern labelPattern = Pattern.compile("<key>Label</key>\\s*<string>(.*?)</string>");
                    Matcher labelMatcher = labelPattern.matcher(content);
                    if (labelMatcher.find()) {
                        String label = labelMatcher.group(1);
                        timedTaskBean.setTaskName(label.substring(label.indexOf(TASK_NAME) + TASK_NAME.length()));
                    }
                    // 解析macOS任务信息
                    Pattern pathPattern = Pattern.compile("<string>--r\\s*(.+)</string>");
                    Matcher pathMatcher = pathPattern.matcher(content);
                    if (pathMatcher.find()) {
                        String path = pathMatcher.group(1);
                        timedTaskBean.setName(text_onlyLaunch)
                                .setPath(text_onlyLaunch);
                        if (path.contains(PMC)) {
                            timedTaskBean.setName(getFileName(path))
                                    .setPath(path);
                        }
                    }
                    if (content.contains(DAILY_CN)) {
                        Pattern dailyPattern = Pattern.compile(
                                "<key>StartCalendarInterval</key>\\s*<dict>"
                                        + "\\s*<key>Hour</key><integer>(\\d+)</integer>"
                                        + "\\s*<key>Minute</key><integer>(\\d+)</integer>"
                                        + "\\s*</dict>");
                        Matcher dailyMatcher = dailyPattern.matcher(content);
                        if (dailyMatcher.find()) {
                            int hour = Integer.parseInt(dailyMatcher.group(1));
                            int minute = Integer.parseInt(dailyMatcher.group(2));
                            timedTaskBean.setTime(String.format("%02d:%02d", hour, minute))
                                    .setRepeat(DAILY_CN)
                                    .setDays(DAILY_CN);
                        }
                        // 获取起始日期
                        findStartDate(content, timedTaskBean);
                    } else if (content.contains("Weekday")) {
                        Pattern weeklyPattern = Pattern.compile(
                                "<key>StartCalendarInterval</key>\\s*<dict>"
                                        + "\\s*<key>Hour</key><integer>(\\d+)</integer>"
                                        + "\\s*<key>Minute</key><integer>(\\d+)</integer>"
                                        + "\\s*<key>Weekday</key><integer>(\\d+)</integer>"
                                        + "\\s*</dict>", Pattern.DOTALL);
                        List<String> weekdays = new ArrayList<>();
                        Matcher weeklyMatcher = weeklyPattern.matcher(content);
                        while (weeklyMatcher.find()) {
                            int hour = Integer.parseInt(weeklyMatcher.group(1));
                            int minute = Integer.parseInt(weeklyMatcher.group(2));
                            int weekday = Integer.parseInt(weeklyMatcher.group(3));
                            timedTaskBean.setTime(String.format("%02d:%02d", hour, minute))
                                    .setRepeat(WEEKLY_CN);
                            weekdays.add(dayOfWeekMap.get(weekday));
                        }
                        if (!weekdays.isEmpty()) {
                            timedTaskBean.setDays(StringUtils.join(weekdays, dayOfWeekRegex));
                        }
                        // 获取起始日期
                        findStartDate(content, timedTaskBean);
                    } else {
                        Pattern datePattern = Pattern.compile(
                                "<key>StartCalendarInterval</key>\\s*<dict>"
                                        + "\\s*<key>Hour</key><integer>(\\d+)</integer>"
                                        + "\\s*<key>Minute</key><integer>(\\d+)</integer>"
                                        + "\\s*<key>Day</key><integer>(\\d+)</integer>"
                                        + "\\s*<key>Month</key><integer>(\\d+)</integer>"
                                        + "\\s*<key>Year</key><integer>(\\d+)</integer>"
                                        + "\\s*</dict>");
                        Matcher dateMatcher = datePattern.matcher(content);
                        if (dateMatcher.find()) {
                            int hour = Integer.parseInt(dateMatcher.group(1));
                            int minute = Integer.parseInt(dateMatcher.group(2));
                            int day = Integer.parseInt(dateMatcher.group(3));
                            int month = Integer.parseInt(dateMatcher.group(4));
                            int year = Integer.parseInt(dateMatcher.group(5));
                            LocalDateTime triggerTime = LocalDateTime.of(year, month, day, hour, minute);
                            timedTaskBean.setDate(triggerTime.toLocalDate().toString())
                                    .setTime(triggerTime.format(TIME_FORMATTER))
                                    .setDateTime(triggerTime)
                                    .setRepeat(ONCE_CN)
                                    .setDays(ONCE_CN);
                        }
                    }
                    taskDetails.add(timedTaskBean);
                }
            }

            /**
             * 查询win定时任务详情
             *
             * @param taskDetails 任务详情
             * @throws IOException 获取任务详情失败
             */
            private void getWinTaskDetails(List<? super TimedTaskBean> taskDetails) throws IOException {
                // 改为查询全部任务
                Process process = new ProcessBuilder("schtasks", "/query", "/fo", "LIST", "/v").start();
                String output = readProcessOutput(process);
                // 新增程序路径过滤（使用正则表达式忽略大小写）
                Pattern exePattern = Pattern.compile(Pattern.quote(appName + exe), Pattern.CASE_INSENSITIVE);
                String[] taskBlocks = output.split("\n\n");
                int dataSize = taskBlocks.length;
                updateProgress(0, dataSize);
                for (int i = 0; i < dataSize; i++) {
                    updateProgress(i + 1, dataSize);
                    String block = taskBlocks[i];
                    // 只处理包含程序路径的任务块
                    if (exePattern.matcher(block).find()) {
                        TimedTaskBean timedTaskBean = new TimedTaskBean();
                        // 解析Windows任务信息
                        Pattern startDatePattern = Pattern.compile("Start Date:\\s+(.*?)\\n");
                        Pattern startTimePattern = Pattern.compile("Start Time:\\s+(.*?)\\n");
                        Pattern scheduleTypePattern = Pattern.compile("Schedule Type:\\s+(.*?)\\n");
                        Pattern taskToRunPattern = Pattern.compile("Task To Run:\\s+(.*?)\\n");
                        Pattern DaysPattern = Pattern.compile("Days:\\s+(.*?)\\n");
                        Pattern taskNamePattern = Pattern.compile("TaskName:\\s+(.*?)\\n");
                        // 提取各字段值
                        String startDate = extractValue(startDatePattern, block);
                        startDate = startDate.substring(0, startDate.lastIndexOf(" "));
                        String startTime = extractValue(startTimePattern, block);
                        String scheduleType = extractValue(scheduleTypePattern, block).toUpperCase().trim();
                        String taskToRun = extractValue(taskToRunPattern, block);
                        String days = extractValue(DaysPattern, block);
                        String taskName = extractValue(taskNamePattern, block);
                        if ("ONE TIME ONLY".equals(scheduleType)) {
                            scheduleType = ONCE;
                        }
                        String repeatType = repeatTypeMap.getKey(scheduleType);
                        String daysCN = Arrays.stream(days.split(",\\s*"))
                                .map(day -> dayOfWeekName.getOrDefault(day.trim().toUpperCase(), ""))
                                .collect(Collectors.joining(dayOfWeekRegex));
                        DateTimeFormatter inputFormatter = new DateTimeFormatterBuilder()
                                .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NEVER)
                                .appendPattern(":mm:ss")
                                .toFormatter();
                        LocalTime time = LocalTime.parse(startTime, inputFormatter);
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");
                        LocalDate parsedDate = LocalDate.parse(startDate, dateFormatter);
                        timedTaskBean.setTaskName(taskName.substring(taskName.indexOf(TASK_NAME) + TASK_NAME.length()))
                                .setDateTime(LocalDateTime.of(parsedDate, time))
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
                        taskDetails.add(timedTaskBean);
                    }
                }
            }
        };
    }

    /**
     * 从字符串中提取指定值
     *
     * @param pattern 正则表达式模式
     * @param input   输入字符串
     * @return 提取的值，如果未找到则返回空字符串
     */
    private static String extractValue(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * 获取起始日期
     *
     * @param content       定时任务内容
     * @param timedTaskBean 定时任务对象
     */
    private static void findStartDate(String content, TimedTaskBean timedTaskBean) {
        Pattern startDatePattern = Pattern.compile("<key>StartDate</key>\\s*<string>(\\d{4}-\\d{2}-\\d{2})</string>");
        Matcher startDateMatcher = startDatePattern.matcher(content);
        if (startDateMatcher.find()) {
            String startDate = startDateMatcher.group(1);
            timedTaskBean.setDate(startDate);
            String startTime = timedTaskBean.getTime();
            if (StringUtils.isNotBlank(startTime)) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate parsedDate = LocalDate.parse(startDate, dateFormatter);
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime time = LocalTime.parse(startTime, inputFormatter);
                timedTaskBean.setDateTime(LocalDateTime.of(parsedDate, time));
            }
        }
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
     * @param timedTaskBean 定时任务信息
     * @throws IOException 创建失败
     */
    private static void createWinLaunchdTask(TimedTaskBean timedTaskBean) throws IOException {
        String exePath = getAppPath();
        String workingDir = Paths.get(exePath).getParent().toString();
        String PMCFilePath = timedTaskBean.getPath();
        LocalDateTime triggerTime = timedTaskBean.getDateTime();
        String repeatType = timedTaskBean.getRepeat();
        List<Integer> days = timedTaskBean.getDayList();
        String taskName = TASK_NAME + timedTaskBean.getTaskName();
        // 构建基础命令
        StringBuilder psCommand = new StringBuilder();
        psCommand.append("$action = New-ScheduledTaskAction -Execute '\"").append(exePath).append("'\" ")
                .append("-WorkingDirectory '").append(workingDir).append("' ")
                .append("-Argument '--r ").append(PMCFilePath).append("'; ");
        // 构建触发器
        psCommand.append("$triggers = @(); ");
        switch (repeatType) {
            case DAILY_CN: {
                psCommand.append("$trigger = New-ScheduledTaskTrigger -Daily -At '")
                        .append(triggerTime.format(FULL_TIME_FORMATTER)).append("'; ");
                psCommand.append("$triggers += $trigger; ");
                break;
            }
            case WEEKLY_CN: {
                String daysOfWeek = days.stream().map(day -> DayOfWeek.of(day).toString())
                        .collect(Collectors.joining(","));
                psCommand.append(String.format(
                        "$trigger = New-ScheduledTaskTrigger -Weekly -At '%s' -DaysOfWeek %s; $triggers += $trigger; ",
                        triggerTime.format(FULL_TIME_FORMATTER), daysOfWeek));
                break;
            }
            case ONCE_CN: {
                // 添加单次触发器
                psCommand.append(String.format("$trigger = New-ScheduledTaskTrigger -Once -At '%s'; $triggers += $trigger; ",
                        triggerTime.format(FULL_TIME_FORMATTER)));
                break;
            }
        }
        // 注册任务（支持多个触发器）
        psCommand.append(String.format("Register-ScheduledTask -TaskName '%s' -Action $action -Trigger $triggers" +
                        " -Settings (New-ScheduledTaskSettingsSet -Compatibility Win8) -Force", taskName));
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
     * @param timedTaskBean 定时任务信息
     * @throws IOException 创建失败
     */
    private static void createMacLaunchdTask(TimedTaskBean timedTaskBean) throws IOException {
        String taskName = TASK_NAME + timedTaskBean.getTaskName();
        Path plistPath = Paths.get(userHome, "Library", "LaunchAgents", taskName + plist);
        LocalDateTime triggerTime = timedTaskBean.getDateTime();
        List<Integer> days = timedTaskBean.getDayList();
        String repeatType = timedTaskBean.getRepeat();
        String PMCFilePath = timedTaskBean.getPath();
        String interval;
        switch (repeatType) {
            case DAILY_CN: {
                interval = String.format("""
                                <key>StartCalendarInterval</key>
                                <dict>
                                    <key>Hour</key><integer>%d</integer>
                                    <key>Minute</key><integer>%d</integer>
                                </dict>
                                <key>StartDate</key>
                                <string>%s</string>
                                <key>RepeatType</key>
                                <string>%s</string>""",
                        triggerTime.getHour(),
                        triggerTime.getMinute(),
                        triggerTime.toLocalDate(),
                        repeatType);
                break;
            }
            case WEEKLY_CN: {
                // 支持多天执行（如每周一、三）
                interval = days.stream().map(day -> String.format("""
                                <key>StartCalendarInterval</key>
                                <dict>
                                    <key>Hour</key><integer>%d</integer>
                                    <key>Minute</key><integer>%d</integer>
                                    <key>Weekday</key><integer>%d</integer>
                                </dict>
                                <key>StartDate</key>
                                <string>%s</string>""",
                        triggerTime.getHour(),
                        triggerTime.getMinute(),
                        day,
                        triggerTime.toLocalDate())).collect(Collectors.joining());
                break;
            }
            default: {
                interval = "<key>RunAtLoad</key><false/>";
                break;
            }
        }
        String calendarInterval = ONCE_CN.equals(repeatType) ?
                String.format("""
                                <key>StartCalendarInterval</key>
                                <dict>
                                    <key>Hour</key><integer>%d</integer>
                                    <key>Minute</key><integer>%d</integer>
                                    <key>Day</key><integer>%d</integer>
                                    <key>Month</key><integer>%d</integer>
                                    <key>Year</key><integer>%d</integer>
                                </dict>""",
                        triggerTime.getHour(),
                        triggerTime.getMinute(),
                        triggerTime.getDayOfMonth(),
                        triggerTime.getMonthValue(),
                        triggerTime.getYear()) : "";
        String plistContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">\n" +
                "<dict>\n" +
                "    <key>Label</key>\n" +
                "    <string>" + taskName + "</string>\n" +
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
