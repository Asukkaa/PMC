package priv.koishi.pmc.Finals;

import javafx.scene.paint.Color;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static priv.koishi.pmc.Utils.CommonUtils.getCurrentGCType;
import static priv.koishi.pmc.Utils.FileUtils.*;

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
     * 程序版本
     */
    public static final String version = "4.0.0";

    /**
     * jdk 版本
     */
    public static final String jdkVersion = System.getProperty("java.vendor") + " " + System.getProperty("java.vm.version");

    /**
     * 程序构建日期
     */
    public static final String buildDate = "2025.12.11";

    /**
     * 文件后缀名：bat
     */
    public static final String bat = ".bat";

    /**
     * 文件后缀名：sh
     */
    public static final String sh = ".sh";

    /**
     * 文件后缀名：py
     */
    public static final String py = ".py";

    /**
     * 文件后缀名：ps1
     */
    public static final String ps1 = ".ps1";

    /**
     * 文件后缀名：java
     */
    public static final String java = ".java";

    /**
     * 文件后缀名：jar
     */
    public static final String jar = ".jar";

    /**
     * 文件后缀名：class
     */
    public static final String clazz = ".class";

    /**
     * 文件后缀名：zip
     */
    public static final String zip = ".zip";

    /**
     * 文件后缀名：log
     */
    public static final String log = ".log";

    /**
     * 文件后缀名：cfg
     */
    public static final String cfg = ".cfg";

    /**
     * 文件后缀名匹配符：pmc
     */
    public static final String allPMC = "*.pmc";

    /**
     * 文件后缀名：pmc
     */
    public static final String PMC = ".pmc";

    /**
     * 文件后缀名：exe
     */
    public static final String exe = ".exe";

    /**
     * 文件后缀名：app
     */
    public static final String app = ".app";

    /**
     * 文件后缀名：plist
     */
    public static final String plist = ".plist";

    /**
     * 文件后缀名：png
     */
    public static final String png = ".png";

    /**
     * 文件后缀名：jpg
     */
    public static final String jpg = ".jpg";

    /**
     * 文件后缀名：jpeg
     */
    public static final String jpeg = ".jpeg";

    /**
     * 图片格式后缀名列表
     */
    public static final List<String> imageType = Arrays.asList(png, jpg, jpeg);

    /**
     * 文件后缀名匹配符：png
     */
    public static final String allPng = "*.png";

    /**
     * 文件后缀名匹配符：jpg
     */
    public static final String allJpg = "*.jpg";

    /**
     * 文件后缀名匹配符：jpeg
     */
    public static final String allJpeg = "*.jpeg";

    /**
     * 图片格式后缀名匹配符列表
     */
    public static final List<String> allImageType = Arrays.asList(allPng, allJpg, allJpeg);

    /**
     * 文件后缀名匹配符：bat
     */
    public static final String allBat = "*.bat";

    /**
     * 文件后缀名匹配符：cmd
     */
    public static final String allCmd = "*.cmd";

    /**
     * 文件后缀名匹配符：py
     */
    public static final String allPy = "*.py";

    /**
     * 文件后缀名匹配符：sh
     */
    public static final String allSh = "*.sh";

    /**
     * 文件后缀名匹配符：bash
     */
    public static final String allBash = "*.bash";

    /**
     * 文件后缀名匹配符：ps1
     */
    public static final String allPs1 = "*.ps1";

    /**
     * 文件后缀名匹配符：java
     */
    public static final String allJava = "*.java";

    /**
     * 文件后缀名匹配符：jar
     */
    public static final String allJar = "*.jar";

    /**
     * 文件后缀名匹配符：class
     */
    public static final String allClass = "*.class";

    /**
     * 文件大小单位：Byte
     */
    public static final String Byte = "Byte";

    /**
     * 文件大小单位：KB
     */
    public static final String KB = "KB";

    /**
     * 文件大小单位：MB
     */
    public static final String MB = "MB";

    /**
     * 文件大小单位：GB
     */
    public static final String GB = "GB";

    /**
     * 文件大小单位：TB
     */
    public static final String TB = "TB";

    /**
     * 文件大小单位：G
     */
    public static final String G = "G";

    /**
     * 单位：百分号
     */
    public static final String percentage = " %";

    /**
     * 程序 logo
     */
    public static String logoPath = "icon/PMC.png";

    /**
     * css 样式文件路径
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
     * jpackage 打包后的资源文件路径
     */
    public static String packagePath = "/bin/";

    /**
     * log 文件夹名称
     */
    public static final String logs = "logs";

    /**
     * log 文件夹目录
     */
    public static final String logsDir = packagePath + logs;

    /**
     * log4j 配置文件名称
     */
    public static String log4j2 = "log4j2.xml";

    /**
     * app 配置文件路径
     */
    public static final String configFile = "config/config.properties";

    /**
     * 读取自动操作工具功能配置文件路径
     */
    public static final String configFile_Click = "config/autoClickConfig.properties";

    /**
     * 更新脚本名称
     */
    public static final String updateScript = "pmc_update";

    /**
     * 当前程序运行操作系统名称
     */
    public static final String systemName = System.getProperty("os.name").toLowerCase();

    /**
     * win 操作系统简称
     */
    public static final String win = "win";

    /**
     * mac 操作系统简称
     */
    public static final String mac = "mac";

    /**
     * 是否为 win 操作系统（true-win 系统，false-非 win 系统）
     */
    public static final boolean isWin = systemName.contains(win);

    /**
     * 是否为 mac 操作系统（true-mac 系统，false-非 mac 系统）
     */
    public static final boolean isMac = systemName.contains(mac);

    /**
     * 用户主目录
     */
    public static final String userHome = System.getProperty("user.home");

    /**
     * idea 中程序运行目录
     */
    public static final String userDir = System.getProperty("user.dir");

    /**
     * java home 目录（win 为 runtime 目录，mac 为 ../runtime/Contents/Home）
     */
    public static final String javaHome = System.getProperty("java.home");

    /**
     * 程序核心目录（win 为根目录，mac 为 ../runtime/Contents）
     */
    public static final String rootDir = new File(javaHome).getParent();

    /**
     * 程序启动路径(win-exe 文件路径，mac-app 文件路径)
     */
    public static final String appLaunchPath = getAppLaunchPath();

    /**
     * 获取应用根目录(win 为应用名目录，mac 为应用程序目录)
     */
    public static final String appRootPath = new File(appLaunchPath).getParent();

    /**
     * 判断程序是否在 IDEA 中运行(在 IDEA 环境运为 true，其他环境为 false)
     */
    public static final boolean isRunningFromIDEA = isRunningFromIDEA();

    /**
     * app 目录
     */
    public static final String appDirectory = "/app";

    /**
     * Contents 目录
     */
    public static final String contentsDirectory = "/Contents";

    /**
     * cfg 文件路径
     */
    public static final String cfgFilePath = getCFGPath();

    /**
     * 当前 GC类 型
     */
    public static final String currentGCType = getCurrentGCType();

    /**
     * jvm 最大内存设置参数
     */
    public static final String Xmx = "-Xmx";

    /**
     * gc 类型设置参数
     */
    public static final String XX = "-XX:+Use";

    /**
     * jvm 参数
     */
    public static final List<String> jvmArgs = Arrays.asList(Xmx, XX);

    /**
     * cfg 文件 jvm 参数头
     */
    public static final String javaOptions = "java-options=";

    /**
     * 桌面名称
     */
    public static final String desktop = "Desktop";

    /**
     * 桌面目录
     */
    public static final String desktopPath = getDesktopPath();

    /**
     * 文件选择器默认路径
     */
    public static final String defaultFileChooserPath = desktopPath;

    /**
     * 操作系统当前用户名称
     */
    public static final String sysUerName = System.getProperty("user.name");

    /**
     * 操作系统临时目录
     */
    public static final String tmpdir = System.getProperty("java.io.tmpdir");

    /**
     * 更新临时文件目录
     */
    public static final String PMCTemp = File.separator + ".PMCTemp";

    /**
     * 更新临时文件目录完整地址
     */
    public static final String PMCTempPath = tmpdir + PMCTemp;

    /**
     * 更新临时文件解压目录
     */
    public static final String PMCUpdateUnzipped = File.separator + "PMCUpdateUnzipped";

    /**
     * macOS 要排除窗口识别的进程地址
     */
    public static final List<String> macSysNoWindowPath = Arrays.asList(
            "/System/Library/CoreServices/Spotlight.app",
            "/System/Library/CoreServices/ControlCenter.app",
            "/System/Library/CoreServices/SystemUIServer.app",
            "/System/Library/PrivateFrameworks/SkyLight.framework/Resources/WindowServer"
    );

    /**
     * 更新服务阿里云 uniCloud 地址
     */
    public static final String uniCloudCheckUpdateURL_aliyun = "https://fc-mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.next.bspapp.com/PMCUpdate";

    /**
     * 更新服务支付宝云 uniCloud 地址
     */
    public static final String uniCloudCheckUpdateURL_alipay = "https://env-00jxtp3qdq80.dev-hz.cloudbasefunction.cn/PMCUpdate";

    /**
     * 检测更新 URL 数组
     */
    public static final String[] urls = {
            uniCloudCheckUpdateURL_aliyun,
            uniCloudCheckUpdateURL_alipay
    };

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
     * github 地址
     */
    public static final String githubLink = "https://github.com/Asukkaa/PMC";

    /**
     * gitee 地址
     */
    public static final String giteeLink = "https://gitee.com/wowxqt/pmc";

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
     * 更新窗口信息消息气泡显示事件（单位：秒）
     */
    public static final int updateListMassageTime = 2;

    /**
     * 无键盘按键
     */
    public static final int noKeyboard = -1;

    /**
     * 绝对横坐标标识
     */
    public static String AbsoluteX = "absoluteX";

    /**
     * 绝对纵坐标标识
     */
    public static String AbsoluteY = "absoluteY";

    /**
     * 相对横坐标标识
     */
    public static String RelativeX = "relativeX";

    /**
     * 相对纵坐标标识
     */
    public static String RelativeY = "relativeY";

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
     * 默认设置目标窗口准备时间
     */
    public static final String defaultFindWindowWait = "3";

    /**
     * 默认录制准备时间
     */
    public static final String defaultPreparationRecord = "3";

    /**
     * 默认运行准备时间
     */
    public static final String defaultPreparationRun = "3";

    /**
     * 默认悬信息浮窗位置横坐标
     */
    public static final String defaultFloatingX = "0";

    /**
     * 默认悬信息浮窗位置纵坐标
     */
    public static final String defaultFloatingY = "0";

    /**
     * 默认要点击的图像识别类型枚举
     */
    public static final String defaultClickFindImgType = "0";

    /**
     * 默认终止操作图像识别类型枚举
     */
    public static final String defaultStopFindImgType = "0";

    /**
     * 默认日志窗口宽度
     */
    public static final String defaultLogWidth = "1300";

    /**
     * 默认日志窗口高度
     */
    public static final String defaultLogHeight = "600";

    /**
     * 默认操作步骤详情窗口宽度
     */
    public static final String defaultClickDetailWidth = "1300";

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
    public static final String defaultFloatingHeight = "220";

    /**
     * 默认信息浮窗高度
     */
    public static final int defaultFloatingHeightInt = Integer.parseInt(defaultFloatingHeight);

    /**
     * 默认信息浮窗宽度
     */
    public static final int defaultFloatingWidthInt = Integer.parseInt(defaultFloatingWidth);

    /**
     * 默认应用窗口宽度
     */
    public static final String defaultAppWidth = "1300";

    /**
     * 默认应用窗口高度
     */
    public static final String defaultAppHeight = "760";

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
     * macOS .DS_Store 文件名称
     */
    public static final String DS_Store = ".DS_Store";

}
