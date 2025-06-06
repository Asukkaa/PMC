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
    public static final String autoSaveFileName = "PMC自动导出流程";

    /**
     * 默认导出文件名称
     */
    public static final String defaultOutFileName = "PMC自动操作流程";

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

    public static final String extension_file = "文件";

    public static final String extension_folder = "文件夹";

    public static final String extension_fileOrFolder = "文件或文件夹";

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
    public static final String defaultTaskName = "自动启动任务 ";

    /**
     * 操作记录列表时间格式
     */
    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static final String tip_logsNum = "logs 文件夹下只会保留该配置所填写数量的 log 日志，不填将会保留全部日志";

    public static final String tip_learButton = "点击将会清空列表中的数据";

    public static final String tip_openDirectory = "勾选后任务结束将会打开对应文件夹";

    public static final String tip_openLink = "点击即可跳转对应网盘分享页";

    public static final String tip_openGitLink = "点击即可跳转对应 git 仓库";

    public static final String tip_wait = "每步操作执行前等待时间，单位为毫秒，只能填自然数，不填默认为 0";

    public static final String tip_mouseStartX = "鼠标点击位置起始横坐标，只能填自然数，不填默认为 0";

    public static final String tip_mouseStartY = "鼠标点击位置起始纵坐标，只能填自然数，不填默认为 0";

    public static final String tip_runClick = "点击后将会按照列表中的步骤执行自动操作，执行自动化任务时按下 esc 即可取消任务";

    public static final String tip_addPosition = "点击后将会根据设置在列表中添加一条操作步骤";

    public static final String tip_loopTime = "自动操作循环次数，只能填自然数，不填默认为 1，填 0 为无限循环";

    public static final String tip_clickNumBer = "每步操作执行次数，点击为多次点击，长按为多次长按，只能填自然数，不填默认为 1";

    public static final String tip_clickKey = "每步操作需要按下的键";

    public static final String tip_clickType = "操作列表中点击加拖拽数量必须与松开操作数量一致\n设置点击图像后只能选择点击后松开或移动到设置坐标";

    public static final String tip_clickTime = "每步操作中，单次操作的点击时长，单位为毫秒，只能填自然数，不填默认为 0";

    public static final String tip_clickInterval = "每步操作中，单次操作的时间间隔，单位为毫秒，只能填自然数，不填默认为 0";

    public static final String tip_clickName = "每步操作的名称，不填将给一个默认名称";

    public static final String tip_outAutoClickPath = "点击可设置操作流程导出文件夹地址";

    public static final String tip_loadAutoClick = "点击后选择要导入的操作流程即可在列表中追加";

    public static final String tip_exportAutoClick = "点击即可按照设置导出文件夹与文件名导出列表中的操作流程";

    public static final String tip_showWindowRun = "勾选后运行或测试自动操作结束后将会弹出本程序的窗口";

    public static final String tip_hideWindowRecord = "勾选后录制自动操作开始前将会隐藏本程序的窗口";

    public static final String tip_showWindowRecord = "勾选后录制自动操作结束后将会弹出本程序的窗口";

    public static final String tip_mouseFloatingRun = "勾选后在运行自动操作时运行信息浮窗会跟随鼠标移动";

    public static final String tip_mouseFloatingRecord = "勾选后在录制自动操作时信息浮窗会跟随鼠标移动";

    public static final String tip_mouseFloating = "勾选后点击 显示浮窗位置 按钮所显示的浮窗会跟随鼠标移动";

    public static final String tip_margin = "用来控制浮窗距离屏幕边界的最小距离，只能填自然数，单位为像素，不填默认为 0";

    public static final String tip_preparationRecordTime = "在录制自动操作前将会等待的时间，只能填自然数，单位秒，不填默认为 ";

    public static final String tip_preparationRunTime = "在运行自动操作前将会等待的时间，只能填自然数，单位秒，不填默认为 ";

    public static final String tip_floatingRun = "勾选后在运行自动操作时将会显示一个运行信息浮窗";

    public static final String tip_floatingRecord = "勾选后在录制自动操作时将会显示一个录制信息浮窗";

    public static final String tip_setFloatingCoordinate = "点击后将会展示浮窗位置，之后按下 esc 可关闭浮窗";

    public static final String tip_closeFloating = "点击后将会关闭浮窗";

    public static final String tip_saveFloating = "点击后将会关闭浮窗并保存浮窗位置";

    public static final String tip_offsetX = "浮窗将会根据填写值向左右偏移，只能填整数，正数向右偏移，负数向左偏移，不填默认为 ";

    public static final String tip_offsetY = "浮窗将会根据填写值向上下偏移，只能填整数，正数向下偏移，负数向上偏移，不填默认为 ";

    public static final String tip_colorPicker = "将会修改自动操作运行与录制时的信息浮窗字体颜色";

    public static final String tip_thanks = "感谢吧友 @拒绝神绮99次 设计的 logo";

    public static final String tip_appreciate = "感谢你的赞赏！";

    public static final String tip_lastAutoClickSetting = "勾选后将会在程序启动时加载自动操作工具上次关闭时的设置";

    public static final String tip_clickOpacity = "滑块将会改变当前步骤要点及的图像识别匹配度，建议设置 80% 以上";

    public static final String tip_stopOpacity = "滑块将会改变当前步骤所有终止操作图像识别匹配度，建议设置 80% 以上";

    public static final String tip_stopImgBtn = "点击后可添加用来终止当前步骤的图片，建议不要添加太多影响图像识别效率";

    public static final String tip_defaultStopImgBtn = "创建新的操作步骤时会自动将列表中的图片加入对应步骤中，建议不要添加太多影响图像识别效率";

    public static final String tip_clickImgBtn = "点击后可添加需要点击的图片，建议选择特征明显范围较小的图片";

    public static final String tip_removeClickImgBtn = "删除需要点击的图片";

    public static final String tip_removeStopImgBtn = "删除列表所有终止操作图片";

    public static final String tip_updateClickNameBtn = "将当前步骤的步骤名称更改为要点击的图片名称";

    public static final String tip_overtime = "只能填正整数，单位秒，不填默认为不限制，每张图片识别如果使用时间超过设置值将会直接终止操作";

    public static final String tip_retrySecond = "只能填自然整数，单位秒，不填默认为 1，每张图片识别失败后将会等着当前设置值后再重试";

    public static final String tip_reLaunch = "点击将会重启本程序并保存设置项";

    public static final String tip_remindSave = "勾选后如果修改过操作步骤详情页的设置后直接点窗口上的关闭按钮将会弹出是否保存的确认框";

    public static final String tip_clickIndex = "当前步骤序号为：";

    public static final String tip_tableViewSize = "当前操作步骤列表共有操作步骤数量为：";

    public static final String tip_Step = "只能填正整数，不可为空，不可大于操作步骤列表中步骤总数，不可填当前步骤序号，目标序号变更后需重新设置";

    public static final String tip_matchedType = "要识别的图像匹配成功后将会根据选项进行不同的操作";

    public static final String tip_retryType = "要识别的图像匹配失败后将会根据选项进行不同的操作";

    public static final String tip_recordMove = "勾选后将会在录制自动操作时记录鼠标没有拖拽时的移动轨迹";

    public static final String tip_recordDrag = "勾选后将会在录制自动操作时记录鼠标拖拽状态时的移动轨迹";

    public static final String tip_randomClick = "勾选后运行自动操作点击时将按设置进行点击位置的坐标偏移";

    public static final String tip_randomTrajectory = "勾选后运行自动操作移动鼠标时将按设置进行轨迹坐标的偏移";

    public static final String tip_randomClickTime = "勾选后运行自动操作点击时长将按设置的时间偏移量进行偏移";

    public static final String tip_randomClickInterval = "勾选后运行自动操作时多次点击的操作步骤的点击间隔将按设置的时间偏移量进行偏移";

    public static final String tip_randomWaitTime = "勾选后运行自动操作时每步操前的等待时间将按照设置的时间偏移量进行偏移";

    public static final String tip_maxLogNum = "用来限制运行记录数量的设置，只能填正整数，不填则不限制最大记录数量";

    public static final String tip_nextRunMemory = "下次启动程序将会按照此设置项分配程序最大运行内存，单位为GB，只能填自然数，为空将设置为默认值操作系统最大内存的1/4";

    public static final String tip_hour = "只能填 0 到 23 的自然数，不填默认为 0";

    public static final String tip_minute = "只能填 0 到 59 的自然数，不填默认为 0";

    public static final String tip_taskName = "定时任务的名称，保存后无法修改，同名任务将会被覆盖，不填默认为 ";

    public static final String tip_datePicker = "重复类型选择 仅一次 时才可修改，其他重复类型只能设置当天生效";

    public static final String tip_repeatType = "定时任务将按照此选项设置类型间隔进行运作";

    public static final String tip_addTimedTask = "点击后将会打开定时任务设置页，设置定时任务并保存成功后将会在下面列表中显示";

    public static final String tip_getScheduleTask = "点击后将会刷新定时任务列表，查询最新设置";

    public static final String tip_deletePath = "删除所选文件";

    public static final String tip_nextGcType = """
            下次启动程序将会按照此设置项设置垃圾回收（GC）方式
            G1GC：分区回收，低延迟与吞吐量平衡
            ZGC：亚毫秒级停顿
            ParallelGC：多线程并行回收，吞吐量优先
            ShenandoahGC：全并发回收，停顿时间与堆大小无关
            SerialGC：单线程回收，简单高效""";

    public static final String tip_sampleInterval = """
            在开启相应设置后，录制自动操作时会按照设置值的时间间隔记录拖拽和移动时的鼠标轨迹
            只能填自然数，单位为毫秒，数字越小越接近录制轨迹，不填默认为\s""";

    public static final String tip_clickRetryNum = """
            要点击的图片识别没有匹配项后将会按照设置次数再次识别
            只能填自然数，不填默认为\s""";

    public static final String tip_stopRetryNum = """
            终止操作图片识别没有匹配项后将会按照设置次数再次识别
            只能填自然数，不填默认为\s""";

    public static final String tip_opacity = """
            滑动将会改变录制或运行自动操作时浮窗透明度
            透明的为 0 时 Windows 下鼠标将会点击透过浮窗
            macOS 暂时无法实现鼠标点击透过""";

    public static final String tip_recordClick = """
            点击录制自动操作按钮将会等待设置的准备时间后开始录制自动操作
            每次鼠标点击并松开为一个步骤，每次点击间隔为操作前等待时间""";

    public static final String tip_autoClickFileName = """
            不用填写文件拓展名，导出文件为 .pmc 格式，如果导出文件夹已经存在同名文件不会覆盖
            文件名不能包含  <>:"/\\|?*
            设置为空或者不合法将会以默认名称命名，默认名称为：""";

    public static final String tip_firstClick = """
            勾选后：
            如果是运行 测试操作流程 则会 鼠标左键 点击一次设置栏设置的起始坐标后再执行测试操作
            如果是运行 自动化操作 则会 鼠标左键 点击一次第一步操作的起始坐标后再执行自动化操作
            Windows 会直接点击对应窗口的对应坐标，macOS 需要先点击对应窗口将焦点切换过去才能点中对应窗口的对应坐标
            建议 Windows 用户不要勾选， macOS 用户需要勾选""";

    public static final String tip_autoSave = """
            勾选后在程序关闭时如果列表不为空将会保存列表的所有操作步骤
            自动保存路径为导出文件夹路径，文件名为：""";

    public static final String tip_hideWindowRun = """
            勾选后运行或测试自动操作开始前将会隐藏本程序的窗口
            如果有图像识别设置最好勾选，操作列表缩略图可能会干扰识别准确度""";

    public static final String tip_NativeHookException = """
            需要在macOS系统设置中启用辅助设备权限：
            1. 打开 [系统偏好设置 → 安全性与隐私 → 隐私]
            2. 在左侧列表选择「辅助功能」
            3. 点击🔒解锁设置
            4. 删除列表中的\s""" + appName + app + """
             （如果有的话）
            5. 将\s""" + appName + app + """
             添加到允许列表中
            6. 重启\s""" + appName + app;

    public static final String tip_noScreenCapturePermission = """
            需要在macOS系统设置中启用录屏与系统录音权限：
            1. 打开 [系统偏好设置 → 安全性与隐私 → 隐私]
            2. 在左侧列表选择「录屏与系统录音」
            3. 点击🔒解锁设置
            4. 删除列表中的\s""" + appName + app + """
             （如果有的话）
            5. 将\s""" + appName + app + """
             添加到允许列表中
            6. 重启\s""" + appName + app;

    public static final String tip_randomClickX = """
            运行自动操作时横坐标将随机在设置值范围内随机发生左右偏移，可用来模仿手动移动和点击鼠标
            单位为像素，只能填自然数，不填默认为\s""";

    public static final String tip_randomClickY = """
            运行自动操作时纵坐标将随机在设置值范围内随机发生上下偏移，可用来模仿手动移动和点击鼠标
            单位为像素，只能填自然数，不填默认为\s""";

    public static final String tip_randomTime = """
            运行自动操作时执行前等待时间、每个步骤内的操作间隔、点击时长都会以此项设置进行随机偏移
            偏移后时间最小为0，单位为毫秒，只能填自然数，不填默认为\s""";

    public static final String tip_clickTimeOffset = """
            手动创建新的操作步骤时将会按照此设置先设置默认点击时长
            单位为毫秒，只能填自然数，不填默认为\s""";

    public static final String version = "3.0.0";

    public static final String buildDate = "2025年6月";

    public static final String tip_version = """
            version：%s
            %s构建""".formatted(version, buildDate);

    public static final String text_selectDirectory = "选择文件夹";

    public static final String text_selectTemplateImg = "选择要识别的图片";

    public static final String text_dataListNull = "列表为空";

    public static final String text_outPathNull = "导出文件夹位置为空，需要先设置导出文件夹位置再继续";

    public static final String text_selectAutoFile = "选择自动化操作流程文件";

    public static final String text_fileNotExists = "文件不存在";

    public static final String text_nullPath = "路径不能为空";

    public static final String text_errPathFormat = "路径格式不正确";

    public static final String text_allHave = "共有 ";

    public static final String text_data = " 组数据";

    public static final String text_task = " 个任务";

    public static final String text_img = " 张图片";

    public static final String text_log = " 条记录";

    public static final String text_process = " 步操作";

    public static final String text_copySuccess = "复制成功";

    public static final String text_successSave = "保存成功";

    public static final String text_nowValue = "当前设置值为： ";

    public static final String text_saveSuccess = "所有数据已导出到： ";

    public static final String text_loadSuccess = "已导入自动操作流程：";

    public static final String text_executionTime = " 轮操作";

    public static final String text_execution = "正在执行第 ";

    public static final String text_cancelTask = "按下 esc 即可取消任务\n";

    public static final String text_saveFloatingCoordinate = "鼠标拖拽浮窗即可移动浮窗\n按下 esc 即可保存浮窗位置";

    public static final String text_escCloseFloating = "按下 esc 即可关闭浮窗";

    public static final String text_closeFloating = "关闭浮窗";

    public static final String text_saveCloseFloating = "关闭并保存浮窗位置";

    public static final String text_showFloating = "显示浮窗位置";

    public static final String text_loadAutoClick = "导入自动化流程文件：";

    public static final String text_formatError = " 内容格式不正确";

    public static final String text_noAutoClickList = "列表中没有要导出的自动操作流程";

    public static final String text_noAutoClickToRun = "列表中没有要执行的操作";

    public static final String text_LackKeyData = "导入文件缺少关键数据";

    public static final String text_step = "步骤 ";

    public static final String text_recordClicking = " 正在录制操作";

    public static final String text_preparation = " 秒后开始录制操作";

    public static final String text_run = " 秒后开始自动操作";

    public static final String text_recorded = "已记录 ";

    public static final String text_mouseTrajectory = bundle.getString("autoClick.mouseTrajectory");

    public static final String text_taskFailed = "出现错误，任务终止";

    public static final String text_taskCancelled = "任务已取消";

    public static final String text_taskFinished = "所有操作都已执行完毕";

    public static final String text_isAdd = " (添加)";

    public static final String text_isRecord = " (录制)";

    public static final String text_imgExist = "图片已存在";

    public static final String text_noImg = "无图片";

    public static final String text_badImg = "图片文件缺失或损坏";

    public static final String text_retryStepGreaterMax = "重试后要跳转的步骤序号不能大于列表步骤数量";

    public static final String text_retryStepEqualIndex = "重试后要跳转的步骤序号不能等于当前步骤序号";

    public static final String text_retryStepIsNull = "重试后要跳转的步骤序号不能为空";

    public static final String text_matchedStepGreaterMax = "匹配后要跳转的步骤序号不能大于列表步骤数量";

    public static final String text_matchedStepEqualIndex = "匹配后要跳转的步骤序号不能等于当前步骤序号";

    public static final String text_matchedStepIsNull = "匹配后要跳转的步骤序号不能为空";

    public static final String text_onlyLaunch = "仅启动程序";

    public static final String text_recordEnd = bundle.getString("autoClick.recordEnd");

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

    public static final String menuItem_insertUp = "插入设置步骤到所选行第一行上一行";

    public static final String menuItem_insertDown = "插入设置步骤到所选行最后一行下一行";

    public static final String menuItem_recordUp = "插入录制步骤到所选行第一行上一行";

    public static final String menuItem_recordDown = "插入录制步骤到所选行最后一行下一行";

    public static final String menuItem_insertTop = "插入设置步骤到列表顶部";

    public static final String menuItem_recordTop = "插入录制步骤到列表顶部";

    public static final String menuItem_upCopy = "复制所选数据到所选行第一行上方";

    public static final String menuItem_downCopy = "复制所选数据到所选行最后一行下方";

    public static final String menuItem_appendCopy = "复制所选数据到列表最后一行";

    public static final String menuItem_topCopy = "复制所选数据到列表顶部";

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
