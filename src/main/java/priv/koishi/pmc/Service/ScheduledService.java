package priv.koishi.pmc.Service;

import javafx.concurrent.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.TimedTaskBean;
import priv.koishi.pmc.Finals.Enum.RepeatTypeEnum;

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
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.Utils.FileUtils.getFileName;

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
     */
    public static Task<Void> createTask(TimedTaskBean timedTaskBean) {
        return new Task<>() {
            @Override
            protected Void call() throws IOException {
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
     * @throws IOException 删除任务失败
     */
    public static void deleteTask(String taskName) throws IOException {
        taskName = TASK_NAME + taskName;
        if (isWin) {
            new ProcessBuilder("schtasks", "/delete", "/tn", taskName, "/f").start();
        } else if (isMac) {
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
                updateMessage(text_searching());
                List<TimedTaskBean> taskDetails = new ArrayList<>();
                if (isWin) {
                    getWinTaskDetails(taskDetails);
                } else if (isMac) {
                    getMacTaskDetails(taskDetails);
                }
                updateMessage("");
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
                        // 先收集符合条件的文件列表
                        List<Path> filteredFiles = stream.filter(path -> path.toString().endsWith(plist)
                                && path.getFileName().toString().startsWith(TASK_NAME)).toList();
                        int dataSize = filteredFiles.size();
                        updateProgress(0, dataSize);
                        for (int i = 0; i < dataSize; i++) {
                            updateProgress(i + 1, dataSize);
                            Path path = filteredFiles.get(i);
                            try {
                                // 解析 mac 定时任务详情
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
                // 改为查询全部任务
                Process process = new ProcessBuilder("cmd", "/c", "chcp 65001 >nul && schtasks /query /fo LIST /v").start();
                String output = readProcessOutput(process);
                // 新增程序路径过滤（使用正则表达式忽略大小写）
                Pattern exePattern = Pattern.compile(Pattern.quote(appName + exe), Pattern.CASE_INSENSITIVE);
                String[] taskBlocks = output.split("\n\n");
                int dataSize = taskBlocks.length;
                updateProgress(0, dataSize);
                for (int i = 0; i < dataSize; i++) {
                    updateProgress(i + 1, dataSize);
                    // 解析 win 定时任务内容
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
        // 只处理包含程序路径的任务块
        if (exePattern.matcher(taskBlocks).find()) {
            TimedTaskBean timedTaskBean = new TimedTaskBean();
            // 解析 Windows 任务信息
            Pattern startDatePattern = Pattern.compile("Start Date:\\s+(.*?)\\n");
            Pattern startTimePattern = Pattern.compile("Start Time:\\s+(.*?)\\n");
            Pattern scheduleTypePattern = Pattern.compile("Schedule Type:\\s+(.*?)\\n");
            Pattern taskToRunPattern = Pattern.compile("Task To Run:\\s+(.*?)\\n");
            Pattern DaysPattern = Pattern.compile("Days:\\s+(.*?)\\n");
            Pattern taskNamePattern = Pattern.compile("TaskName:\\s+(.*?)\\n");
            // 提取各字段值
            String startDate = extractValue(startDatePattern, taskBlocks);
            startDate = startDate.substring(0, startDate.lastIndexOf(" "));
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
                    // 根据 dayOfWeekMap 的键（数字星期）排序
                    .sorted(Comparator.comparingInt(day ->
                            dayOfWeekReverseMap().getOrDefault(day, 8)))
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
                    .setPath(text_onlyLaunch())
                    .setName(text_onlyLaunch())
                    .setRepeat(repeatType)
                    .setDate(startDate)
                    .setDays(daysCN);
            if (taskToRun.contains(PMC)) {
                // 处理文件路径中的空格
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
            // 解析任务名称
            Pattern labelPattern = Pattern.compile("<key>Label</key>\\s*<string>(.*?)</string>");
            Matcher labelMatcher = labelPattern.matcher(content);
            if (labelMatcher.find()) {
                String label = labelMatcher.group(1);
                timedTaskBean.setTaskName(label.substring(label.indexOf(TASK_NAME) + TASK_NAME.length()));
            }
            // 解析 macOS 任务信息
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
            if (content.contains(repeatType_daily())) {
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
                // 获取起始日期
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
                            .setRepeat(repeatType_once())
                            .setDays(repeatType_once());
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
     * 创建 win 定时任务
     *
     * @param timedTaskBean 定时任务信息
     * @throws IOException 创建失败
     */
    private static void createWinLaunchdTask(TimedTaskBean timedTaskBean) throws IOException {
        String workingDir = Paths.get(appLaunchPath).getParent().toString();
        // 处理文件路径中的空格
        String PMCFilePath = timedTaskBean.getPath().replaceAll(" ", "*");
        LocalDateTime triggerTime = timedTaskBean.getDateTime();
        String repeatType = timedTaskBean.getRepeat();
        List<Integer> days = timedTaskBean.getDayList();
        String taskName = TASK_NAME + timedTaskBean.getTaskName();
        // 构建基础命令
        StringBuilder psCommand = new StringBuilder();
        psCommand.append("$action = New-ScheduledTaskAction -Execute '\"").append(appLaunchPath).append("'\" ")
                .append("-WorkingDirectory '").append(workingDir).append("' ")
                .append("-Argument '--r ").append(PMCFilePath).append("'; ");
        // 构建触发器
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
            // 添加单次触发器
            psCommand.append(String.format("$trigger = New-ScheduledTaskTrigger -Once -At '%s'; $triggers += $trigger; ",
                    triggerTime.format(FULL_TIME_FORMATTER)));
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
            throw new RuntimeException(text_creatTaskErr() + output);
        }
    }

    /**
     * 创建 mac 定时任务
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
                            <string>%s</string>""",
                    triggerTime.getHour(),
                    triggerTime.getMinute(),
                    triggerTime.toLocalDate(),
                    repeatType);
        } else if (repeatType_weekly().equals(repeatType)) {
            // 支持多天执行
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
        Files.write(plistPath, plistContent.getBytes());
        // 先卸载旧配置
        new ProcessBuilder("launchctl", "unload", plistPath.toString()).start();
        // 加载任务
        new ProcessBuilder("launchctl", "load", plistPath.toString()).start();
    }

}
