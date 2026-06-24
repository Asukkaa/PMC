package priv.koishi.pmc.Finals;

import com.sun.jna.Platform;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import priv.koishi.pmc.MainApplication;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    public static final String version = "4.4.1";

    /**
     * PMC 文件版本
     */
    public static final String PMCFileVersion = "2.4";

    /**
     * PMCS 文件版本
     */
    public static final String PMCSFileVersion = "1.0";

    /**
     * jdk 版本
     */
    public static final String jdkVersion = System.getProperty("java.vendor") + " " + System.getProperty("java.vm.version");

    /**
     * 程序构建日期
     */
    public static final String buildDate = "2026.06.24";

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
     * 文件后缀名匹配符：pmcs
     */
    public static final String allPMCS = "*.pmcs";

    /**
     * 文件后缀名：pmc
     */
    public static final String PMC = ".pmc";

    /**
     * 文件后缀名：pmcs
     */
    public static final String PMCS = ".pmcs";

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
     * 文件后缀名：traineddata
     */
    public static final String traineddata = ".traineddata";

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
     * 文件后缀名匹配符：traineddata
     */
    public static final String allTraineddata = "*.traineddata";

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
    public static final String logoPath = "icon/PMC.png";

    /**
     * css 样式文件路径
     */
    public static final String stylesCss = "css/Styles.css";

    /**
     * 资源文件夹地址前缀
     */
    public static final String resourcePath = "/priv/koishi/pmc/";

    /**
     * 打包前资源文件夹地址前缀
     */
    public static final String resourcesPath = "src/main/resources" + resourcePath;

    /**
     * Jpackage 打包后的资源文件路径
     */
    public static final String packagePath = "/bin/";

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
    public static final String log4j2 = "log4j2.xml";

    /**
     * tessdata 设置配置文件路径
     */
    public static final String configFile_Tessdata = "config/TessdataConfig.json";

    /**
     * 更新脚本名称
     */
    public static final String updateScript = "pmc_update";

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
    public static final boolean isWin = Platform.isWindows();

    /**
     * 是否为 mac 操作系统（true-mac 系统，false-非 mac 系统）
     */
    public static final boolean isMac = Platform.isMac();

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
     * 项目 appBuilder 路径
     */
    public static final String appBuilder = userDir + "/appBuilder";

    /**
     * tessdata 目录名称
     */
    public static final String tessdata = "tessdata";

    /**
     * app 目录
     */
    public static final String appDirectory = "/app";

    /**
     * Contents 目录
     */
    public static final String contentsDirectory = "/Contents";

    /**
     * mac app 目录(.app/Contents/app)
     */
    public static final String macAppDirectory = appLaunchPath + contentsDirectory + appDirectory;

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
     * 爱发电地址
     */
    public static final String afdianLink = "https://afdian.com/a/project_pmc";

    /**
     * tessdata 模型下载地址
     */
    public static final String tessdataLink = "https://github.com/tesseract-ocr/tessdata_best";

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
    public static final String enable = "1";

    /**
     * 禁用
     */
    public static final String disable = "0";

    /**
     * 端口被占用时激活窗口信号
     */
    public static final String activatePMC = "activatePMC";

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
     * 不添加新数据，用于组合键监听器获取组合键
     */
    public static final int noAdd = -99;

    /**
     * 无键盘按键
     */
    public static final int noKeyboard = -1;

    /**
     * 绝对横坐标标识
     */
    public static final String AbsoluteX = "absoluteX";

    /**
     * 绝对纵坐标标识
     */
    public static final String AbsoluteY = "absoluteY";

    /**
     * 相对横坐标标识
     */
    public static final String RelativeX = "relativeX";

    /**
     * 相对纵坐标标识
     */
    public static final String RelativeY = "relativeY";

    /**
     * macOS .DS_Store 文件名称
     */
    public static final String DS_Store = ".DS_Store";

    /**
     * 鼠标禁用图标
     */
    public static final Cursor disableCursor = Cursor.cursor(Objects.requireNonNull(
            MainApplication.class.getResource("icon/Disable.png")).toString());

}
