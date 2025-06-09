package priv.koishi.pmc.Finals;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static priv.koishi.pmc.MainApplication.bundle;
import static priv.koishi.pmc.Utils.FileUtils.getCFGPath;
import static priv.koishi.pmc.Utils.FileUtils.getDesktopPath;

/**
 * 通用常量类
 *
 * @author KOISHI
 * Date:2024-11-13
 * Time:下午2:41
 */
public class CommonFinals {

    /**
     * 程序名称
     */
    public static final String appName = "Perfect Mouse Control";

    /**
     * 程序logo
     */
    public static String logoPath = "icon/PMC.png";

    /**
     * css样式文件路径
     */
    public static String stylesCss = "css/Styles.css";

    /**
     * 资源文件夹地址前缀
     */
    public static String resourcePath = "/priv/koishi/pmc/";

    /**
     * 打包前资源文件夹地址前缀
     */
    public static String resourcesPath = "src/main/resources" + resourcePath;

    /**
     * jpackage打包后的资源文件路径
     */
    public static String packagePath = "/bin/";

    /**
     * log4j配置文件名称
     */
    public static String log4j2 = "log4j2.xml";

    /**
     * app配置文件路径
     */
    public static final String configFile = "config/config.properties";

    /**
     * 读取自动操作工具功能配置文件路径
     */
    public static final String configFile_Click = "config/autoClickConfig.properties";

    /**
     * log文件夹目录
     */
    public static final String logsDir = packagePath + "logs";

    /**
     * log文件夹名称
     */
    public static final String logs = "logs";

    /**
     * 用户主目录
     */
    public static final String userHome = System.getProperty("user.home");

    /**
     * java home目录
     */
    public static final String javaHome = System.getProperty("java.home");

    /**
     * 程序根目录
     */
    public static final String pmcDir = new File(javaHome).getParent();

    /**
     * 桌面目录
     */
    public static final String desktopPath = getDesktopPath();

    /**
     * 文件选择器默认路径
     */
    public static final String defaultFileChooserPath = desktopPath;

    /**
     * 当前程序运行操作系统名称
     */
    public static final String systemName = System.getProperty("os.name").toLowerCase();

    /**
     * win操作系统
     */
    public static final boolean isWin = systemName.contains("win");

    /**
     * mac操作系统
     */
    public static final boolean isMac = systemName.contains("mac");

    /**
     * 百度网盘地址
     */
    public static final String baiduLink = "https://pan.baidu.com/s/1UbQx3XdUAtLBPJ6thd-H5A?pwd=3efe";

    /**
     * 夸克网盘地址
     */
    public static final String quarkLink = "https://pan.quark.cn/s/987130e1f360";

    /**
     * 迅雷网盘地址
     */
    public static final String xunleiLink = "https://pan.xunlei.com/s/VOKjQlqFxTDbJNN7yRA_DLVgA1?pwd=gx3q#";

    /**
     * github地址
     */
    public static final String githubLink = "https://github.com/Asukkaa/PMC";

    /**
     * gitee地址
     */
    public static final String giteeLink = "https://gitee.com/wowxqt/pmc";

    /**
     * 自动保存文件名
     */
    public static final String autoSaveFileName = bundle.getString("autoSaveFileName");

    /**
     * 默认导出文件名称
     */
    public static final String defaultOutFileName = bundle.getString("defaultOutFileName");

    /**
     * jvm最大内存设置参数
     */
    public static final String Xmx = "-Xmx";

    /**
     * gc类型设置参数
     */
    public static final String XX = "-XX:+Use";

    /**
     * jvm参数
     */
    public static final List<String> jvmArgs = Arrays.asList(Xmx, XX);

    /**
     * cfg文件jvm参数头
     */
    public static final String javaOptions = "java-options=";

    /**
     * cfg文件路径
     */
    public static final String cfgFilePath = getCFGPath();

    public static final String appDirectory = "/app";

    public static final String contentsDirectory = "/Contents";

    public static final String desktop = "Desktop";

    public static final String log = ".log";

    public static final String cfg = ".cfg";

    public static final String allPMC = "*.pmc";

    public static final String PMC = ".pmc";

    public static final String exe = ".exe";

    public static final String app = ".app";

    public static final String Byte = "Byte";

    public static final String KB = "KB";

    public static final String MB = "MB";

    public static final String GB = "GB";

    public static final String TB = "TB";

    public static final String G = "G";

    public static final String extension_file = bundle.getString("extension.file");

    public static final String extension_folder = bundle.getString("extension.folder");

    public static final String extension_fileOrFolder = bundle.getString("extension.fileOrFolder");

    public static final String allPng = "*.png";

    public static final String allJpg = "*.jpg";

    public static final String allJpeg = "*.jpeg";

    public static final String png = ".png";

    public static final String jpg = ".jpg";

    public static final String jpeg = ".jpeg";

    public static final String plist = ".plist";

    public static final List<String> imageType = Arrays.asList(png, jpg, jpeg);

    public static final List<String> allImageType = Arrays.asList(allPng, allJpg, allJpeg);

    /**
     * 存在的文件路径颜色
     */
    public static final Color existsFileColor = Color.rgb(0, 88, 128);

    /**
     * 不存在的文件路径颜色
     */
    public static final Color notExistsFileColor = Color.RED;

    /**
     * 星期分隔符
     */
    public static final String dayOfWeekRegex = ", ";

    /**
     * 定时任务自动执行参数前缀
     */
    public static final String r = "--r ";

    /**
     * 启用
     */
    public static final String activation = "1";

    /**
     * 禁用
     */
    public static final String unActivation = "0";

    /**
     * 端口被占用时激活窗口信号
     */
    public static final String activatePMC = "activatePMC";

    /**
     * 启用禁用标志
     */
    public static final List<String> activationList = Arrays.asList(activation, unActivation);

    /**
     * 默认浮窗横轴偏移量
     */
    public static final int defaultOffsetX = 30;

    /**
     * 默认浮纵轴窗偏移量
     */
    public static final int defaultOffsetY = 30;

    /**
     * 在列表所选行第一行上方插入
     */
    public static final int upAdd = 1;

    /**
     * 在列表所选行最后一行下方插入
     */
    public static final int downAdd = 2;

    /**
     * 向列表最后一行追加
     */
    public static final int append = -1;

    /**
     * 向列表第一行上方插入
     */
    public static final int topAdd = 0;

    /**
     * 默认循环次数
     */
    public static final String defaultLoopTime = "1";

    /**
     * 默认随机偏移时长
     */
    public static final String defaultRandomTime = "50";

    /**
     * 默认点击时长
     */
    public static final String defaultClickTimeOffset = "120";

    /**
     * 默认横轴随机偏移量
     */
    public static final String defaultRandomClickX = "5";

    /**
     * 默认纵轴随机偏移量
     */
    public static final String defaultRandomClickY = "5";

    /**
     * 默认鼠标轨迹采样间隔
     */
    public static final String defaultSampleInterval = "10";

    /**
     * 默认要点击的图片识别重试次数
     */
    public static final String defaultClickRetryNum = "3";

    /**
     * 默认终止操作图片识别重试次数
     */
    public static final String defaultStopRetryNum = "0";

    /**
     * 默认录制准备时间
     */
    public static final String defaultPreparationRecordTime = "3";

    /**
     * 默认运行准备时间
     */
    public static final String defaultPreparationRunTime = "3";

    /**
     * 默认悬信息浮窗位置横坐标
     */
    public static final String defaultFloatingX = "0";

    /**
     * 默认悬信息浮窗位置纵坐标
     */
    public static final String defaultFloatingY = "0";

    /**
     * 默认日志窗口宽度
     */
    public static final String defaultLogWidth = "1000";

    /**
     * 默认日志窗口高度
     */
    public static final String defaultLogHeight = "600";

    /**
     * 默认操作步骤详情窗口宽度
     */
    public static final String defaultClickDetailWidth = "1200";

    /**
     * 默认操作步骤详情窗口高度
     */
    public static final String defaultClickDetailHeight = "720";

    /**
     * 默认定时任务详情窗口宽度
     */
    public static final String defaultTaskDetailWidth = "850";

    /**
     * 默认定时任务详情窗口高度
     */
    public static final String defaultTaskDetailHeight = "400";

    /**
     * 默认信息浮窗宽度
     */
    public static final String defaultFloatingWidth = "250";

    /**
     * 默认信息浮窗高度
     */
    public static final String defaultFloatingHeight = "160";

    /**
     * 默认应用窗口宽度
     */
    public static final String defaultAppWidth = "1300";

    /**
     * 默认应用窗口高度
     */
    public static final String defaultAppHeight = "720";

    /**
     * 默认打开的页面
     */
    public static final String defaultLastTab = "autoClickTab";

    /**
     * 默认应用端口
     */
    public static final String defaultAppPort = "52514";

    /**
     * 默认浮窗背景透明度
     */
    public static final String defaultOpacity = "0.5";

    /**
     * 默认操作记录最大条数
     */
    public static final String defaultMaxLog = "1000";

    /**
     * 默认图像识别重试间隔时长
     */
    public static final String defaultRetrySecond = "1";

    /**
     * 默认终止操作图片默认识别匹配匹配度
     */
    public static final String defaultStopOpacity = "80.0";

    /**
     * 默认信息浮窗边距
     */
    public static final String defaultMargin = "0";

    /**
     * 默认要点击的图片默认识别匹配匹配度
     */
    public static final String defaultClickOpacity = "80.0";

    /**
     * 默认时长随机偏移量
     */
    public static final String defaultRandomTimeOffset = "50";

    /**
     * 颜色选择器默认选中颜色
     */
    public static final String defaultColor = "0xffffffff";

    /**
     * 定时任务默认任务名称
     */
    public static final String defaultTaskName = bundle.getString("defaultTaskName");

    /**
     * 操作记录列表时间格式
     */
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static final String tip_logsNum = bundle.getString("tip.logsNum");

    public static final String tip_learButton = bundle.getString("tip.learButton");

    public static final String tip_openDirectory = bundle.getString("tip.openDirectory");

    public static final String tip_openLink = bundle.getString("tip.openLink");

    public static final String tip_openGitLink = bundle.getString("tip.openGitLink");

    public static final String tip_wait = bundle.getString("tip.wait");

    public static final String tip_mouseStartX = bundle.getString("tip.mouseStartX");

    public static final String tip_mouseStartY = bundle.getString("tip.mouseStartY");

    public static final String tip_runClick = bundle.getString("tip.runClick");

    public static final String tip_addPosition = bundle.getString("tip.addPosition");

    public static final String tip_loopTime = bundle.getString("tip.loopTime");

    public static final String tip_clickNumBer = bundle.getString("tip.clickNumBer");

    public static final String tip_clickKey = bundle.getString("tip.clickKey");

    public static final String tip_clickType = bundle.getString("tip.clickType");

    public static final String tip_clickTime = bundle.getString("tip.clickTime");

    public static final String tip_clickInterval = bundle.getString("tip.clickInterval");

    public static final String tip_clickName = bundle.getString("tip.clickName");

    public static final String tip_outAutoClickPath = bundle.getString("tip.outAutoClickPath");

    public static final String tip_loadAutoClick = bundle.getString("tip.loadAutoClick");

    public static final String tip_exportAutoClick = bundle.getString("tip.exportAutoClick");

    public static final String tip_showWindowRun = bundle.getString("tip.showWindowRun");

    public static final String tip_hideWindowRecord = bundle.getString("tip.hideWindowRecord");

    public static final String tip_showWindowRecord = bundle.getString("tip.showWindowRecord");

    public static final String tip_mouseFloatingRun = bundle.getString("tip.mouseFloatingRun");

    public static final String tip_mouseFloatingRecord = bundle.getString("tip.mouseFloatingRecord");

    public static final String tip_mouseFloating = bundle.getString("tip.mouseFloating");

    public static final String tip_margin = bundle.getString("tip.margin");

    public static final String tip_preparationRecordTime = bundle.getString("tip.preparationRecordTime");

    public static final String tip_preparationRunTime = bundle.getString("tip.preparationRunTime");

    public static final String tip_floatingRun = bundle.getString("tip.floatingRun");

    public static final String tip_floatingRecord = bundle.getString("tip.floatingRecord");

    public static final String tip_setFloatingCoordinate = bundle.getString("tip.setFloatingCoordinate");

    public static final String tip_closeFloating = bundle.getString("tip.closeFloating");

    public static final String tip_saveFloating = bundle.getString("tip.saveFloating");

    public static final String tip_offsetX = bundle.getString("tip.offsetX");

    public static final String tip_offsetY = bundle.getString("tip.offsetY");

    public static final String tip_colorPicker = bundle.getString("tip.colorPicker");

    public static final String tip_thanks = bundle.getString("tip.thanks");

    public static final String tip_appreciate = bundle.getString("tip.appreciate");

    public static final String tip_lastAutoClickSetting = bundle.getString("tip.lastAutoClickSetting");

    public static final String tip_clickOpacity = bundle.getString("tip.clickOpacity");

    public static final String tip_stopOpacity = bundle.getString("tip.stopOpacity");

    public static final String tip_stopImgBtn = bundle.getString("tip.stopImgBtn");

    public static final String tip_defaultStopImgBtn = bundle.getString("tip.defaultStopImgBtn");

    public static final String tip_clickImgBtn = bundle.getString("tip.clickImgBtn");

    public static final String tip_removeClickImgBtn = bundle.getString("tip.removeClickImgBtn");

    public static final String tip_removeStopImgBtn = bundle.getString("tip.removeStopImgBtn");

    public static final String tip_updateClickNameBtn = bundle.getString("tip.updateClickNameBtn");

    public static final String tip_overtime = bundle.getString("tip.overtime");

    public static final String tip_retrySecond = bundle.getString("tip.retrySecond");

    public static final String tip_reLaunch = bundle.getString("tip.reLaunch");

    public static final String tip_remindSave = bundle.getString("tip.remindSave");

    public static final String tip_clickIndex = bundle.getString("tip.clickIndex");

    public static final String tip_tableViewSize = bundle.getString("tip.tableViewSize");

    public static final String tip_step = bundle.getString("tip.step");

    public static final String tip_matchedType = bundle.getString("tip.matchedType");

    public static final String tip_retryType = bundle.getString("tip.retryType");

    public static final String tip_recordMove = bundle.getString("tip.recordMove");

    public static final String tip_recordDrag = bundle.getString("tip.recordDrag");

    public static final String tip_randomClick = bundle.getString("tip.randomClick");

    public static final String tip_randomTrajectory = bundle.getString("tip.randomTrajectory");

    public static final String tip_randomClickTime = bundle.getString("tip.randomClickTime");

    public static final String tip_randomClickInterval = bundle.getString("tip.randomClickInterval");

    public static final String tip_randomWaitTime = bundle.getString("tip.randomWaitTime");

    public static final String tip_maxLogNum = bundle.getString("tip.maxLogNum");

    public static final String tip_nextRunMemory = bundle.getString("tip.nextRunMemory");

    public static final String tip_hour = bundle.getString("tip.hour");

    public static final String tip_minute = bundle.getString("tip.minute");

    public static final String tip_taskName = bundle.getString("tip.taskName");

    public static final String tip_datePicker = bundle.getString("tip.datePicker");

    public static final String tip_repeatType = bundle.getString("tip.repeatType");

    public static final String tip_addTimedTask = bundle.getString("tip.addTimedTask");

    public static final String tip_getScheduleTask = bundle.getString("tip.getScheduleTask");

    public static final String tip_deletePath = bundle.getString("tip.deletePath");

    public static final String tip_nextGcType = bundle.getString("tip.nextGcType");

    public static final String tip_sampleInterval = bundle.getString("tip.sampleInterval");

    public static final String tip_clickRetryNum = bundle.getString("tip.clickRetryNum");

    public static final String tip_stopRetryNum = bundle.getString("tip.stopRetryNum");

    public static final String tip_opacity = bundle.getString("tip.opacity");

    public static final String tip_recordClick = bundle.getString("tip.recordClick");

    public static final String tip_autoClickFileName = bundle.getString("tip.autoClickFileName");

    public static final String tip_firstClick = bundle.getString("tip.firstClick");

    public static final String tip_autoSave = bundle.getString("tip.autoSave");

    public static final String tip_hideWindowRun = bundle.getString("tip.hideWindowRun");

    public static final String tip_NativeHookException = bundle.getString("tip.NativeHookException") +
            "\n" + appName + app +
            "\n" + bundle.getString("tip.ifHave") +
            "\n" + bundle.getString("tip.addList") + appName + app;

    public static final String tip_noScreenCapturePermission = bundle.getString("tip.noScreenCapturePermission") +
            "\n" + appName + app +
            "\n" + bundle.getString("tip.ifHave") +
            "\n" + bundle.getString("tip.addList") + appName + app;

    public static final String tip_randomClickX = bundle.getString("tip.randomClickX");

    public static final String tip_randomClickY = bundle.getString("tip.randomClickY");

    public static final String tip_randomTime = bundle.getString("tip.randomTime");

    public static final String tip_clickTimeOffset = bundle.getString("tip.clickTimeOffset");

    public static final String version = "3.0.0";

    public static final String buildDate = "2025.06";

    public static final String tip_version = """
            version：%s
            build：%s""".formatted(version, buildDate);

    public static final String text_selectDirectory = bundle.getString("selectDirectory");

    public static final String text_selectTemplateImg = bundle.getString("selectTemplateImg");

    public static final String text_dataListNull = bundle.getString("listText.null");

    public static final String text_outPathNull = bundle.getString("outPathNull");

    public static final String text_selectAutoFile = bundle.getString("selectAutoFile");

    public static final String text_fileNotExists = bundle.getString("fileNotExists");

    public static final String text_nullPath = bundle.getString("pathNull");

    public static final String text_errPathFormat = bundle.getString("errPathFormat");

    public static final String text_allHave = bundle.getString("allHave");

    public static final String text_data = bundle.getString("unit.data");

    public static final String text_task = bundle.getString("unit.task");

    public static final String text_img = bundle.getString("unit.img");

    public static final String text_log = bundle.getString("unit.log");

    public static final String text_process = bundle.getString("unit.process");

    public static final String text_copySuccess = bundle.getString("copySuccess");

    public static final String text_successSave = bundle.getString("successSave");

    public static final String text_nowValue = bundle.getString("nowValue");

    public static final String text_saveSuccess = bundle.getString("saveSuccess");

    public static final String text_loadSuccess = bundle.getString("loadSuccess");

    public static final String text_executionTime = bundle.getString("executionTime");

    public static final String text_execution = bundle.getString("execution");

    public static final String text_cancelTask = bundle.getString("cancelTask");

    public static final String text_saveFloatingCoordinate = bundle.getString("saveFloatingCoordinate");

    public static final String text_escCloseFloating = bundle.getString("escCloseFloating");

    public static final String text_closeFloating = bundle.getString("closeFloating");

    public static final String text_saveCloseFloating = bundle.getString("saveCloseFloating");

    public static final String text_showFloating = bundle.getString("showFloating");

    public static final String text_loadAutoClick = bundle.getString("loadAutoClick");

    public static final String text_formatError = bundle.getString("formatError");

    public static final String text_noAutoClickList = bundle.getString("noAutoClickList");

    public static final String text_noAutoClickToRun = bundle.getString("noAutoClickToRun");

    public static final String text_missingKeyData = bundle.getString("missingKeyData");

    public static final String text_step = bundle.getString("step");

    public static final String text_recordClicking = bundle.getString("recordClicking");

    public static final String text_preparation = bundle.getString("preparation");

    public static final String text_run = bundle.getString("run");

    public static final String text_recorded = bundle.getString("recorded");

    public static final String text_mouseTrajectory = bundle.getString("autoClick.mouseTrajectory");

    public static final String text_taskFailed = bundle.getString("taskFailed");

    public static final String text_taskCancelled = bundle.getString("taskCancelled");

    public static final String text_taskFinished = bundle.getString("taskFinished");

    public static final String text_isAdd = bundle.getString("isAdd");

    public static final String text_isRecord = bundle.getString("isRecord");

    public static final String text_imgExist = bundle.getString("imgExist");

    public static final String text_noImg = bundle.getString("noImg");

    public static final String text_badImg = bundle.getString("badImg");

    public static final String text_retryStepGreaterMax = bundle.getString("retryStepGreaterMax");

    public static final String text_retryStepEqualIndex = bundle.getString("retryStepEqualIndex");

    public static final String text_retryStepIsNull = bundle.getString("retryStepIsNull");

    public static final String text_matchedStepGreaterMax = bundle.getString("matchedStepGreaterMax");

    public static final String text_matchedStepEqualIndex = bundle.getString("matchedStepEqualIndex");

    public static final String text_matchedStepIsNull = bundle.getString("matchedStepIsNull");

    public static final String text_onlyLaunch = bundle.getString("onlyLaunch");

    public static final String text_recordEnd = bundle.getString("autoClick.recordEnd");

    public static final String text_image = bundle.getString("img");

    public static final String text_progress = bundle.getString("progress");

    public static final String text_willBe = bundle.getString("willBe");

    public static final String text_ms = bundle.getString("unit.ms");

    public static final String text_msWillBe = bundle.getString("msWillBe");

    public static final String text_index = bundle.getString("taskIndex");

    public static final String text_point = bundle.getString("point");

    public static final String key_logsNum = "logsNum";

    public static final String key_inFilePath = "inFilePath";

    public static final String key_outFilePath = "outFilePath";

    public static final String key_clickImgSelectPath = "clickImgSelectPath";

    public static final String key_stopImgSelectPath = "stopImgSelectPath";

    public static final String key_appWidth = "appWidth";

    public static final String key_appHeight = "appHeight";

    public static final String key_appreciateWidth = "appreciateWidth";

    public static final String key_appreciateHeight = "appreciateHeight";

    public static final String key_appPort = "appPort";

    public static final String key_clickDetailWidth = "clickDetailWidth";

    public static final String key_clickDetailHeight = "clickDetailHeight";

    public static final String key_taskDetailWidth = "taskDetailWidth";

    public static final String key_taskDetailHeight = "taskDetailHeight";

    public static final String key_logWidth = "logWidth";

    public static final String key_logHeight = "logHeight";

    public static final String key_floatingX = "floatingX";

    public static final String key_floatingY = "floatingY";

    public static final String key_margin = "margin";

    public static final String key_opacity = "opacity";

    public static final String key_stopOpacity = "stopOpacity";

    public static final String key_clickOpacity = "clickOpacity";

    public static final String key_retrySecond = "retrySecond";

    public static final String key_overtime = "overtime";

    public static final String key_remindClickSave = "remindClickSave";

    public static final String key_remindTaskSave = "remindTaskSave";

    public static final String key_mouseFloatingRun = "mouseFloatingRun";

    public static final String key_mouseFloatingRecord = "mouseFloatingRecord";

    public static final String key_mouseFloating = "mouseFloating";

    public static final String key_offsetX = "offsetX";

    public static final String key_offsetY = "offsetY";

    public static final String key_floatingWidth = "floatingWidth";

    public static final String key_floatingHeight = "floatingHeight";

    public static final String key_autoSave = "autoSave";

    public static final String key_recordDrag = "recordDrag";

    public static final String key_recordMove = "recordMove";

    public static final String key_randomClick = "randomClick";

    public static final String key_randomTrajectory = "randomTrajectory";

    public static final String key_randomClickTime = "randomClickTime";

    public static final String key_randomTimeOffset = "randomTimeOffset";

    public static final String key_clickTimeOffset = "clickTimeOffset";

    public static final String key_randomClickX = "randomClickX";

    public static final String key_randomClickY = "randomClickY";

    public static final String key_randomClickInterval = "randomClickInterval";

    public static final String key_randomWaitTime = "randomWaitTime";

    public static final String key_sampleInterval = "sampleInterval";

    public static final String key_clickLog = "clickLog";

    public static final String key_moveLog = "moveLog";

    public static final String key_dragLog = "dragLog";

    public static final String key_waitLog = "waitLog";

    public static final String key_clickImgLog = "clickImgLog";

    public static final String key_stopImgLog = "stopImgLog";

    public static final String key_maxLogNum = "maxLogNum";

    public static final String key_loadLastConfig = "loadLastConfig";

    public static final String key_loadLastFullWindow = "loadLastFullWindow";

    public static final String key_loadLastMaxWindow = "loadLastMaxWindow";

    public static final String key_lastMaxWindow = "lastMaxWindow";

    public static final String key_lastOpenDirectory = "lastOpenDirectory";

    public static final String key_lastTab = "lastTab";

    public static final String key_lastFullWindow = "lastFullWindow";

    public static final String key_lastLoopTime = "lastLoopTime";

    public static final String key_lastFirstClick = "lastFirstClick";

    public static final String key_lastOutFileName = "lastOutFileName";

    public static final String key_lastHideWindowRun = "lastHideWindowRun";

    public static final String key_lastShowWindowRun = "lastShowWindowRun";

    public static final String key_lastHideWindowRecord = "lastHideWindowRecord";

    public static final String key_lastShowWindowRecord = "lastShowWindowRecord";

    public static final String key_loadFloatingRun = "lastFloatingRun";

    public static final String key_loadFloatingRecord = "lastFloatingRecord";

    public static final String key_lastPreparationRecordTime = "lastPreparationRecordTime";

    public static final String key_lastPreparationRunTime = "lastPreparationRunTime";

    public static final String key_defaultClickRetryNum = "defaultClickRetryNum";

    public static final String key_defaultStopRetryNum = "defaultStopRetryNum";

    public static final String key_defaultStopImg = "defaultStopImg";

    public static final String key_lastFloatingTextColor = "lastFloatingTextColor";

    public static final String key_lastColorCustom = "lastColorCustom";

    public static final String confirm_unSaved = bundle.getString("confirm.unSaved");

    public static final String confirm_unSavedConfirm = bundle.getString("confirm.unSavedConfirm");

    public static final String confirm_ok = bundle.getString("confirm.ok");

    public static final String confirm_cancel = bundle.getString("confirm.cancel");

    public static final String menuItem_insertUp = bundle.getString("menuItem.insertUp");

    public static final String menuItem_insertDown = bundle.getString("menuItem.insertDown");

    public static final String menuItem_recordUp = bundle.getString("menuItem.recordUp");

    public static final String menuItem_recordDown = bundle.getString("menuItem.recordDown");

    public static final String menuItem_insertTop = bundle.getString("menuItem.insertTop");

    public static final String menuItem_recordTop = bundle.getString("menuItem.recordTop");

    public static final String menuItem_upCopy = bundle.getString("menuItem.upCopy");

    public static final String menuItem_downCopy = bundle.getString("menuItem.downCopy");

    public static final String menuItem_appendCopy = bundle.getString("menuItem.appendCopy");

    public static final String menuItem_topCopy = bundle.getString("menuItem.topCopy");

    public static final String menu_detailMenu = bundle.getString("detailMenu");

    public static final String menu_deleteMenu = bundle.getString("deleteMenu");

    public static final String menu_runSelectMenu = bundle.getString("runSelectMenu");

    public static final String menu_addDateMenu = bundle.getString("addDateMenu");

    public static final String log_press = bundle.getString("log.press");

    public static final String log_release = bundle.getString("log.release");

    public static final String log_move = bundle.getString("log.move");

    public static final String log_hold = bundle.getString("log.hold");

    public static final String log_drag = bundle.getString("log.drag");

    public static final String log_wait = bundle.getString("log.wait");

    public static final String log_clickImg = bundle.getString("log.clickImg");

    public static final String log_stopImg = bundle.getString("log.stopImg");

    public static final String retryType_continuously = bundle.getString("retryType.continuously");

    public static final String retryType_click = bundle.getString("retryType.click");

    public static final String retryType_stop = bundle.getString("retryType.stop");

    public static final String retryType_break = bundle.getString("retryType.break");

    public static final String retryType_Step = bundle.getString("retryType.Step");

    /**
     * 重试逻辑下拉框选项
     */
    public static final List<String> retryTypeList = Arrays.asList(retryType_continuously, retryType_click,
            retryType_stop, retryType_break, retryType_Step);

    public static final String clickMatched_click = bundle.getString("clickMatched.click");

    public static final String clickMatched_break = bundle.getString("clickMatched.break");

    public static final String clickMatched_step = bundle.getString("clickMatched.step");

    public static final String clickMatched_clickStep = bundle.getString("clickMatched.clickStep");

    public static final String clickMatched_clickWhile = bundle.getString("clickMatched.clickWhile");

    /**
     * 要识别的图像识别匹配后逻辑下拉框选项
     */
    public static final List<String> clickMatchedList = Arrays.asList(clickMatched_click, clickMatched_break,
            clickMatched_step, clickMatched_clickStep);

    public static final String mouseButton_primary = bundle.getString("primary");

    public static final String mouseButton_secondary = bundle.getString("secondary");

    public static final String mouseButton_middle = bundle.getString("middle");

    public static final String mouseButton_forward = bundle.getString("forward");

    public static final String mouseButton_back = bundle.getString("back");

    /**
     * 点击按键下拉框选项
     */
    public static final List<String> mouseButtonList = Arrays.asList(mouseButton_primary, mouseButton_secondary,
            mouseButton_middle, mouseButton_forward, mouseButton_back);

    /**
     * 自动操作的操作类型选项对应的鼠标行为（操作用）
     */
    public static final BidiMap<String, MouseButton> runClickTypeMap = new DualHashBidiMap<>();

    static {
        runClickTypeMap.put(mouseButton_primary, MouseButton.PRIMARY);
        runClickTypeMap.put(mouseButton_secondary, MouseButton.SECONDARY);
        runClickTypeMap.put(mouseButton_middle, MouseButton.MIDDLE);
        runClickTypeMap.put(mouseButton_forward, MouseButton.FORWARD);
        runClickTypeMap.put(mouseButton_back, MouseButton.BACK);
    }

    /**
     * 自动操作的操作类型选项对应的鼠标行为（录制用）
     */
    public static final BidiMap<Integer, String> recordClickTypeMap = new DualHashBidiMap<>();

    static {
        recordClickTypeMap.put(NativeMouseEvent.BUTTON1, mouseButton_primary);
        recordClickTypeMap.put(NativeMouseEvent.BUTTON2, mouseButton_secondary);
        recordClickTypeMap.put(NativeMouseEvent.BUTTON3, mouseButton_middle);
        recordClickTypeMap.put(NativeMouseEvent.BUTTON4, mouseButton_back);
        recordClickTypeMap.put(NativeMouseEvent.BUTTON5, mouseButton_forward);
    }

    /**
     * 录制与点击按键类映射
     */
    public static final BidiMap<Integer, MouseButton> NativeMouseToMouseButton = new DualHashBidiMap<>();

    static {
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON1, MouseButton.PRIMARY);
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON2, MouseButton.SECONDARY);
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON3, MouseButton.MIDDLE);
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON4, MouseButton.BACK);
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON5, MouseButton.FORWARD);
        NativeMouseToMouseButton.put(NativeMouseEvent.NOBUTTON, MouseButton.NONE);
    }

    public static final String clickType_move = bundle.getString("clickType.move");

    public static final String clickType_click = bundle.getString("clickType.click");

    public static final String clickType_drag = bundle.getString("clickType.drag");

    public static final String clickType_moveTo = bundle.getString("clickType.moveTo");

    /**
     * 自动操作的操作类型选项
     */
    public static final List<String> clickTypeList = Arrays.asList(clickType_move, clickType_click,
            clickType_drag, clickType_moveTo);

    public static final String DAILY = "DAILY";

    public static final String WEEKLY = "WEEKLY";

    public static final String ONCE = "ONCE";

    public static final String repeatType_daily = bundle.getString("repeatType.daily");

    public static final String repeatType_weekly = bundle.getString("repeatType.weekly");

    public static final String repeatType_once = bundle.getString("repeatType.once");

    /**
     * 重复类型下拉框选项
     */
    public static final List<String> repeatTypeList = Arrays.asList(repeatType_daily, repeatType_weekly,
            repeatType_once);

    /**
     * 定时任务重复类型映射
     */
    public static final BidiMap<String, String> repeatTypeMap = new DualHashBidiMap<>();

    static {
        repeatTypeMap.put(repeatType_daily, DAILY);
        repeatTypeMap.put(repeatType_weekly, WEEKLY);
        repeatTypeMap.put(repeatType_once, ONCE);
    }

    public static final String monday = bundle.getString("monday");

    public static final String tuesday = bundle.getString("tuesday");

    public static final String wednesday = bundle.getString("wednesday");

    public static final String thursday = bundle.getString("thursday");

    public static final String friday = bundle.getString("friday");

    public static final String saturday = bundle.getString("saturday");

    public static final String sunday = bundle.getString("sunday");

    /**
     * 定时任务星期名称中英文映射
     */
    public static final BidiMap<String, String> dayOfWeekName = new DualHashBidiMap<>();

    static {
        dayOfWeekName.put("MON", monday);
        dayOfWeekName.put("TUE", tuesday);
        dayOfWeekName.put("WED", wednesday);
        dayOfWeekName.put("THU", thursday);
        dayOfWeekName.put("FRI", friday);
        dayOfWeekName.put("SAT", saturday);
        dayOfWeekName.put("SUN", sunday);
    }

    /**
     * 定时任务星期名称与数字映射
     */
    public static final BidiMap<Integer, String> dayOfWeekMap = new DualHashBidiMap<>();

    static {
        dayOfWeekMap.put(1, monday);
        dayOfWeekMap.put(2, tuesday);
        dayOfWeekMap.put(3, wednesday);
        dayOfWeekMap.put(4, thursday);
        dayOfWeekMap.put(5, friday);
        dayOfWeekMap.put(6, saturday);
        dayOfWeekMap.put(7, sunday);
    }

    /**
     * 定时任务星期名称与数字映射的逆向映射
     */
    public static final Map<String, Integer> dayOfWeekReverseMap = dayOfWeekMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

}
