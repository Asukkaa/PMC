package priv.koishi.pmc.service;

import javafx.concurrent.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.bean.TimedTaskBean;
import priv.koishi.pmc.finals.enums.RepeatTypeEnum;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static priv.koishi.pmc.finals.CommonFinals.*;
import static priv.koishi.pmc.finals.i18nFinal.*;
import static priv.koishi.pmc.utils.FileUtils.getFileName;

/**
 * 定时任务服务类
 *
 * @author KOISHI
 * Date:2025-05-16
 * Time:16:36
 */
public class ScheduledService {

    /**
     * 定时任务名称
     */
    public static final String TASK_NAME = "PMC-";

    /**
     * 读取定时任务时间格式
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 读取定时任务日期格式
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 创建定时任务时间格式
     */
    private static final DateTimeFormatter FULL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Windows 日期格式设置
     */
    private static String windowsDatePattern;

    /**
     * Windows 时间格式设置
     */
    private static String windowsTimePattern;

    /**
     * 创建定时任务
     *
     * @param timedTaskBean 定时任务信息
     * @return 无返回值的 Task
     */
    public static Task<Void> createTask(TimedTaskBean timedTaskBean) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage(text_saving());
                if (isWin) {
                    // 创建 win 定时任务
                    createWinLaunchdTask(timedTaskBean);
                } else if (isMac) {
                    // 创建 mac 定时任务
                    createMacLaunchdTask(timedTaskBean);
                }
                updateMessage("");
                return null;
            }
        };
    }

    /**
     * 删除定时任务
     *
     * @param taskName 定时任务名称自定义部分
     * @throws Exception 删除任务失败
     */
    public static void deleteTask(String taskName) throws Exception {
        taskName = TASK_NAME + taskName;
        if (isWin) {
            CommandLine cmdLine = new CommandLine("schtasks");
            cmdLine.addArgument("/delete");
            cmdLine.addArgument("/tn");
            cmdLine.addArgument(taskName);
            cmdLine.addArgument("/f");
            execIgnoreOutput(cmdLine);
        } else if (isMac) {
            Path plistFile = Paths.get(userHome, "Library", "LaunchAgents", taskName + plist);
            // 卸载定时任务
            CommandLine bootoutCmd = new CommandLine("launchctl");
            bootoutCmd.addArgument("bootout");
            bootoutCmd.addArgument("gui/" + getCurrentUserId());
            bootoutCmd.addArgument(plistFile.toString());
            execIgnoreOutput(bootoutCmd);
            Files.deleteIfExists(plistFile);
        }
    }

    /**
     * 获取当前用户的 ID
     *
     * @return 前用户的 ID
     * @throws IOException 获取失败
     */
    private static String getCurrentUserId() throws IOException {
        CommandLine cmdLine = new CommandLine("id");
        cmdLine.addArgument("-u");
        return execToString(cmdLine).trim();
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
                updateMessage(text_searching());
                List<TimedTaskBean> taskDetails = new ArrayList<>();
                if (isWin) {
                    getWinTaskDetails(taskDetails);
                } else if (isMac) {
                    getMacTaskDetails(taskDetails);
                }
                return taskDetails;
            }

            /**
             * 查询 mac 定时任务详情
             *
             * @param taskDetails 任务详情
             */
            private void getMacTaskDetails(List<? super TimedTaskBean> taskDetails) {
                Path launchAgentsPath = Paths.get(userHome, "Library", "LaunchAgents");
                if (Files.exists(launchAgentsPath)) {
                    // 遍历所有以 TASK_NAME 开头的 .plist 文件
                    try (Stream<Path> stream = Files.list(launchAgentsPath)) {
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
                        throw new RuntimeException(text_searchLaunchAgentsErr() + launchAgentsPath, e);
                    }
                }
            }

            /**
             * 查询 win 定时任务详情
             *
             * @param taskDetails 任务详情
             * @throws IOException 获取任务详情失败
             */
            private void getWinTaskDetails(List<? super TimedTaskBean> taskDetails) throws IOException {
                if (StringUtils.isBlank(windowsDatePattern)) {
                    windowsDatePattern = getWindowsDatePattern();
                }
                if (StringUtils.isBlank(windowsTimePattern)) {
                    windowsTimePattern = getWindowsTimePattern();
                }
                CommandLine cmdLine = new CommandLine("cmd");
                cmdLine.addArgument("/c");
                cmdLine.addArgument("chcp 65001 >nul && schtasks /query /fo LIST /v", false);
                String output = execToString(cmdLine);
                // 新增程序路径过滤
                Pattern exePattern = Pattern.compile(Pattern.quote(appName + exe), Pattern.CASE_INSENSITIVE);
                String[] taskBlocks = output.split("\n\n");
                int dataSize = taskBlocks.length;
                updateProgress(0, dataSize);
                for (int i = 0; i < dataSize; i++) {
                    updateProgress(i + 1, dataSize);
                    parseWinTaskContent(taskDetails, exePattern, taskBlocks[i]);
                }
            }
        };
    }

    /**
     * 解析 win 定时任务内容
     *
     * @param taskDetails 任务详情
     * @param exePattern  程序路径正则表达式
     * @param taskBlocks  任务文本块
     */
    private static void parseWinTaskContent(List<? super TimedTaskBean> taskDetails, Pattern exePattern, String taskBlocks) {
        if (exePattern.matcher(taskBlocks).find()) {
            TimedTaskBean timedTaskBean = new TimedTaskBean();
            Pattern startDatePattern = Pattern.compile("Start Date:\\s+(.*?)\\n");
            Pattern startTimePattern = Pattern.compile("Start Time:\\s+(.*?)\\n");
            Pattern scheduleTypePattern = Pattern.compile("Schedule Type:\\s+(.*?)\\n");
            Pattern taskToRunPattern = Pattern.compile("Task To Run:\\s+(.*?)\\n");
            Pattern DaysPattern = Pattern.compile("Days:\\s+(.*?)\\n");
            Pattern taskNamePattern = Pattern.compile("TaskName:\\s+(.*?)\\n");
            String startDate = extractValue(startDatePattern, taskBlocks);
            startDate = formatDate(startDate);
            String startTime = extractValue(startTimePattern, taskBlocks);
            String scheduleType = extractValue(scheduleTypePattern, taskBlocks).toUpperCase().trim();
            String taskToRun = extractValue(taskToRunPattern, taskBlocks);
            String days = extractValue(DaysPattern, taskBlocks);
            String taskName = extractValue(taskNamePattern, taskBlocks);
            if ("ONE TIME ONLY".equals(scheduleType)) {
                scheduleType = RepeatTypeEnum.ONCE.getRepeatType();
            }
            String repeatType = repeatTypeMap.get(scheduleType);
            String daysCN = Arrays.stream(days.split(",\\s*"))
                    .map(day -> dayOfWeekName.getOrDefault(day.trim().toUpperCase(), ""))
                    .filter(day -> !day.isEmpty())
                    .sorted(Comparator.comparingInt(day ->
                            dayOfWeekReverseMap().getOrDefault(day, 8)))
                    .collect(Collectors.joining(dayOfWeekRegex));
            LocalTime time = parseTime(startTime);
            LocalDate date = parseDate(startDate);
            if (date != null && time != null) {
                timedTaskBean.setDateTime(LocalDateTime.of(date, time))
                        .setTime(time.format(TIME_FORMATTER))
                        .setDate(date.format(DATE_FORMATTER));
            }
            timedTaskBean.setTaskName(taskName.substring(taskName.indexOf(TASK_NAME) + TASK_NAME.length()))
                    .setPath(text_onlyLaunch())
                    .setName(text_onlyLaunch())
                    .setRepeat(repeatType)
                    .setDays(daysCN);
            if (taskToRun.contains(PMC)) {
                String path = taskToRun.substring(taskToRun.lastIndexOf(r) + r.length())
                        .replaceAll("\\*", " ");
                String name = getFileName(path);
                timedTaskBean.setPath(path)
                        .setName(name);
            }
            if ("Every day of the week".equals(days) || repeatType_daily().equals(repeatType)) {
                timedTaskBean.setDays(repeatType_daily());
            } else if (repeatType_once().equals(repeatType)) {
                timedTaskBean.setDays(repeatType_once());
            }
            taskDetails.add(timedTaskBean);
        }
    }

    /**
     * 解析 mac 定时任务详情
     *
     * @param taskDetails 任务详情
     * @param plistPath   plist 文件路径
     * @throws IOException 获取任务详情失败
     */
    private static void parseMacTaskContent(List<? super TimedTaskBean> taskDetails, Path plistPath) throws IOException {
        if (Files.exists(plistPath)) {
            TimedTaskBean timedTaskBean = new TimedTaskBean();
            String content = new String(Files.readAllBytes(plistPath));
            Pattern labelPattern = Pattern.compile("<key>Label</key>\\s*<string>(.*?)</string>");
            Matcher labelMatcher = labelPattern.matcher(content);
            if (labelMatcher.find()) {
                String label = labelMatcher.group(1);
                timedTaskBean.setTaskName(label.substring(label.indexOf(TASK_NAME) + TASK_NAME.length()));
            }
            Pattern pathPattern = Pattern.compile("<string>--r\\s*(.+)</string>");
            Matcher pathMatcher = pathPattern.matcher(content);
            if (pathMatcher.find()) {
                String path = pathMatcher.group(1);
                timedTaskBean.setName(text_onlyLaunch())
                        .setPath(text_onlyLaunch());
                if (path.contains(PMC)) {
                    timedTaskBean.setName(getFileName(path))
                            .setPath(path);
                }
            }
            if (content.contains("Daily")) {
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
                            .setRepeat(repeatType_daily())
                            .setDays(repeatType_daily());
                }
                findStartDate(content, timedTaskBean);
            } else if (content.contains("Weekday")) {
                Pattern arrayPattern = Pattern.compile(
                        "<key>StartCalendarInterval</key>\\s*<array>(.*?)</array>",
                        Pattern.DOTALL);
                Matcher arrayMatcher = arrayPattern.matcher(content);
                if (arrayMatcher.find()) {
                    String arrayContent = arrayMatcher.group(1);
                    Pattern dictPattern = Pattern.compile(
                            "<dict>\\s*" +
                                    "<key>Hour</key><integer>(\\d+)</integer>\\s*" +
                                    "<key>Minute</key><integer>(\\d+)</integer>\\s*" +
                                    "<key>Weekday</key><integer>(\\d+)</integer>\\s*" +
                                    "</dict>",
                            Pattern.DOTALL);
                    Matcher dictMatcher = dictPattern.matcher(arrayContent);
                    List<String> weekdays = new ArrayList<>();
                    while (dictMatcher.find()) {
                        int hour = Integer.parseInt(dictMatcher.group(1));
                        int minute = Integer.parseInt(dictMatcher.group(2));
                        int weekday = Integer.parseInt(dictMatcher.group(3));
                        timedTaskBean.setTime(String.format("%02d:%02d", hour, minute))
                                .setRepeat(repeatType_weekly());
                        weekdays.add(dayOfWeekMap.get(weekday));
                    }
                    if (CollectionUtils.isNotEmpty(weekdays)) {
                        weekdays.sort(Comparator.comparingInt(day ->
                                dayOfWeekReverseMap().getOrDefault(day, 8)
                        ));
                        timedTaskBean.setDays(StringUtils.join(weekdays, dayOfWeekRegex));
                    }
                }
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
                            .setRepeat(repeatType_once())
                            .setDays(repeatType_once())
                            .setDateTime(triggerTime);
                }
            }
            taskDetails.add(timedTaskBean);
        }
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
                LocalDate parsedDate = LocalDate.parse(startDate, DATE_FORMATTER);
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime time = LocalTime.parse(startTime, inputFormatter);
                timedTaskBean.setDateTime(LocalDateTime.of(parsedDate, time));
            }
        }
    }

    /**
     * 创建 win 定时任务
     *
     * @param timedTaskBean 定时任务信息
     * @throws IOException 创建失败
     */
    private static void createWinLaunchdTask(TimedTaskBean timedTaskBean) throws IOException {
        String workingDir = Paths.get(appLaunchPath).getParent().toString();
        String PMCFilePath = timedTaskBean.getPath().replace(" ", "*");
        LocalDateTime triggerTime = timedTaskBean.getDateTime();
        String repeatType = timedTaskBean.getRepeat();
        List<Integer> days = timedTaskBean.getDayList();
        String taskName = TASK_NAME + timedTaskBean.getTaskName();
        StringBuilder psCommand = new StringBuilder();
        psCommand.append("$action = New-ScheduledTaskAction -Execute '\"").append(appLaunchPath).append("'\" ")
                .append("-WorkingDirectory '").append(workingDir).append("' ")
                .append("-Argument '--r ").append(PMCFilePath).append("'; ");
        psCommand.append("$triggers = @(); ");
        if (repeatType_daily().equals(repeatType)) {
            psCommand.append("$trigger = New-ScheduledTaskTrigger -Daily -At '")
                    .append(triggerTime.format(FULL_TIME_FORMATTER)).append("'; ");
            psCommand.append("$triggers += $trigger; ");
        } else if (repeatType_weekly().equals(repeatType)) {
            String daysOfWeek = days.stream().map(day -> DayOfWeek.of(day).toString())
                    .collect(Collectors.joining(","));
            psCommand.append(String.format(
                    "$trigger = New-ScheduledTaskTrigger -Weekly -At '%s' -DaysOfWeek %s; $triggers += $trigger; ",
                    triggerTime.format(FULL_TIME_FORMATTER), daysOfWeek));
        } else if (repeatType_once().equals(repeatType)) {
            psCommand.append(String.format("$trigger = New-ScheduledTaskTrigger -Once -At '%s'; $triggers += $trigger; ",
                    triggerTime.format(FULL_TIME_FORMATTER)));
        }
        psCommand.append(String.format("Register-ScheduledTask -TaskName '%s' -Action $action -Trigger $triggers" +
                " -Settings (New-ScheduledTaskSettingsSet -Compatibility Win8) -Force", taskName));
        CommandLine cmdLine = new CommandLine("powershell.exe");
        cmdLine.addArgument("-Command");
        cmdLine.addArgument(psCommand.toString(), false);
        String output = execToStringMerged(cmdLine);
        if (output.contains("Exception") || output.contains("错误")) {
            throw new RuntimeException(text_creatTaskErr() + output);
        }
    }

    /**
     * 创建 mac 定时任务
     *
     * @param timedTaskBean 定时任务信息
     * @throws Exception 创建失败
     */
    private static void createMacLaunchdTask(TimedTaskBean timedTaskBean) throws Exception {
        String taskName = TASK_NAME + timedTaskBean.getTaskName();
        Path plistPath = getTaskFilePath(taskName);
        LocalDateTime triggerTime = timedTaskBean.getDateTime();
        List<Integer> days = timedTaskBean.getDayList();
        String repeatType = timedTaskBean.getRepeat();
        String PMCFilePath = timedTaskBean.getPath();
        String interval;
        if (repeatType_daily().equals(repeatType)) {
            interval = String.format("""
                            <key>StartCalendarInterval</key>
                            <dict>
                                <key>Hour</key><integer>%d</integer>
                                <key>Minute</key><integer>%d</integer>
                            </dict>
                            <key>StartDate</key>
                            <string>%s</string>
                            <key>RepeatType</key>
                            <string>Daily</string>""",
                    triggerTime.getHour(),
                    triggerTime.getMinute(),
                    triggerTime.toLocalDate());
        } else if (repeatType_weekly().equals(repeatType)) {
            String intervals = days.stream().map(day -> String.format("""
                            <dict>
                                <key>Hour</key><integer>%d</integer>
                                <key>Minute</key><integer>%d</integer>
                                <key>Weekday</key><integer>%d</integer>
                            </dict>""",
                    triggerTime.getHour(),
                    triggerTime.getMinute(),
                    day)).collect(Collectors.joining("\n"));
            interval = String.format("""
                            <key>StartCalendarInterval</key>
                            <array>
                                %s
                            </array>
                            <key>StartDate</key>
                            <string>%s</string>""",
                    intervals,
                    triggerTime.toLocalDate());
        } else {
            interval = "<key>RunAtLoad</key><false/>";
        }
        String calendarInterval = repeatType_once().equals(repeatType) ?
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
                "        <string>" + appLaunchPath + "</string>\n" +
                "        <string>--args</string>\n" +
                "        <string>--r " + PMCFilePath + "</string>\n" +
                "    </array>\n" +
                interval + "\n" +
                calendarInterval + "\n" +
                "</dict>\n" +
                "</plist>";
        if (plistPath != null) {
            Files.write(plistPath, plistContent.getBytes());
            CommandLine unloadCmd = new CommandLine("launchctl");
            unloadCmd.addArgument("unload");
            unloadCmd.addArgument(plistPath.toString());
            execIgnoreOutput(unloadCmd);
            CommandLine loadCmd = new CommandLine("launchctl");
            loadCmd.addArgument("load");
            loadCmd.addArgument(plistPath.toString());
            execIgnoreOutput(loadCmd);
        }
    }

    /**
     * 获取定时任务文件路径
     *
     * @param taskName 任务名（可不带 PMC 前缀，会自动判断并添加）
     * @return 任务文件路径
     */
    public static Path getTaskFilePath(String taskName) {
        if (!taskName.startsWith(TASK_NAME)) {
            taskName = TASK_NAME + taskName;
        }
        if (isMac) {
            return Paths.get(userHome, "Library", "LaunchAgents", taskName + plist);
        } else if (isWin) {
            return Paths.get(System.getenv("SystemRoot"), "System32", "Tasks", taskName);
        }
        return null;
    }

    /**
     * 尝试用多个格式解析日期
     *
     * @param dateStr 日期字符串
     * @return 日期对象，解析失败则返回 null
     */
    private static LocalDate parseDate(String dateStr) {
        if (StringUtils.isNotBlank(dateStr)) {
            String cleaned = dateStr.replaceAll("[，, ]+$", "").trim();
            if (windowsDatePattern != null) {
                return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern(windowsDatePattern));
            }
        }
        return null;
    }

    /**
     * 尝试用多个格式解析时间
     *
     * @param timeStr 时间字符串
     * @return 时间对象，解析失败则返回 null
     */
    private static LocalTime parseTime(String timeStr) {
        if (StringUtils.isNotBlank(timeStr)) {
            String cleaned = timeStr.trim();
            if (windowsTimePattern != null) {
                return LocalTime.parse(cleaned, DateTimeFormatter.ofPattern(windowsTimePattern));
            }
        }
        return null;
    }

    /**
     * 通过 PowerShell 获取 Windows 短日期格式
     *
     * @return 获取到的日期格式
     */
    private static String getWindowsDatePattern() {
        CommandLine cmdLine = new CommandLine("powershell.exe");
        cmdLine.addArgument("-NoProfile");
        cmdLine.addArgument("-Command");
        cmdLine.addArgument("[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; " +
                "(Get-Culture).DateTimeFormat.ShortDatePattern", false);
        try {
            String output = execToString(cmdLine);
            if (!output.isEmpty()) {
                String pattern = output.trim();
                pattern = formatDate(pattern);
                return pattern;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 通过 PowerShell 获取 Windows 短时间格式
     *
     * @return 获取到的时间格式
     */
    private static String getWindowsTimePattern() {
        CommandLine cmdLine = new CommandLine("powershell.exe");
        cmdLine.addArgument("-NoProfile");
        cmdLine.addArgument("-Command");
        cmdLine.addArgument("[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; " +
                "(Get-Culture).DateTimeFormat.ShortTimePattern", false);
        try {
            String output = execToString(cmdLine);
            return output.trim().replace("\uFEFF", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 只保留 Windows 的主要日期格式
     *
     * @param date 原始日期字符串
     * @return 处理后的日期字符串
     */
    private static String formatDate(String date) {
        if (date.contains(",")) {
            date = date.substring(0, date.lastIndexOf(","));
        }
        if (date.contains(" ")) {
            date = date.substring(0, date.lastIndexOf(" "));
        }
        return date;
    }


    /**
     * 执行命令并返回标准输出（UTF-8），忽略退出码
     *
     * @param cmdLine 要执行的命令
     * @return 命令执行返回值
     * @throws IOException 命令执行异常
     */
    private static String execToString(CommandLine cmdLine) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DefaultExecutor executor = DefaultExecutor.builder().get();
        executor.setExitValues(null);
        executor.setStreamHandler(new PumpStreamHandler(outputStream));
        executor.execute(cmdLine);
        return outputStream.toString(StandardCharsets.UTF_8)
                .replace("\r\n", "\n")
                .replace('\r', '\n');
    }

    /**
     * 执行命令并合并标准输出和错误输出（UTF-8），忽略退出码
     *
     * @param cmdLine 要执行的命令
     * @return 命令执行返回值与错误信息
     * @throws IOException 命令执行异常
     */
    private static String execToStringMerged(CommandLine cmdLine) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DefaultExecutor executor = DefaultExecutor.builder().get();
        executor.setExitValues(null);
        executor.setStreamHandler(new PumpStreamHandler(outputStream, outputStream));
        executor.execute(cmdLine);
        return outputStream.toString(StandardCharsets.UTF_8)
                .replace("\r\n", "\n")
                .replace('\r', '\n');
    }

    /**
     * 执行命令并忽略输出，仅等待完成
     *
     * @param cmdLine 要执行的命令
     * @throws IOException 命令执行异常
     */
    private static void execIgnoreOutput(CommandLine cmdLine) throws IOException {
        DefaultExecutor executor = DefaultExecutor.builder().get();
        executor.setExitValues(null);
        executor.setStreamHandler(new PumpStreamHandler(null, null));
        executor.execute(cmdLine);
    }

}