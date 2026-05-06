package priv.koishi.pmc.Finals.DefaultConfig;

import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.disable;
import static priv.koishi.pmc.Finals.CommonFinals.enable;
import static priv.koishi.pmc.Finals.CommonKeys.*;

/**
 * 自动操作相关默认配置类
 *
 * @author Koishi
 * Date:2026-05-06
 * Time:11:40
 */
public class AutoClickDefault {

    /**
     * 读取自动操作工具页面配置文件路径
     */
    public static final String configFile_Click = "config/AutoClickConfig.properties";

    /**
     * 自动操作相关默认配置
     */
    public static final Properties clickProperties = new Properties();

    static {
        // 终止操作窗口信息浮窗横坐标
        clickProperties.put(key_stopX, "0");
        // 终止操作窗口信息浮窗纵坐标
        clickProperties.put(key_stopY, "0");
        // 浮窗离屏幕边界距离
        clickProperties.put(key_margin, "0");
        // 浮窗横坐标
        clickProperties.put(key_clickX, "0");
        // 浮窗纵坐标
        clickProperties.put(key_clickY, "0");
        // 每张图片最大匹配时间
        clickProperties.put(key_overtime, "");
        // 循环次数
        clickProperties.put(key_loopTime, "");
        // 目标窗口信息浮窗位置横坐标
        clickProperties.put(key_messageX, "0");
        // 目标窗口信息浮窗位置纵坐标
        clickProperties.put(key_messageY, "0");
        // 浮窗跟随鼠标时横轴偏移量
        clickProperties.put(key_offsetX, "30");
        // 浮窗跟随鼠标时纵轴偏移量
        clickProperties.put(key_offsetY, "30");
        // 浮窗背景透明度
        clickProperties.put(key_opacity, "0.5");
        // 运行自动流程时记录详细的图像识别信息
        clickProperties.put(key_imgLog, enable);
        // 导入文件的选择器默认选中路径
        clickProperties.put(key_inFilePath, "");
        // 选择导出文件夹时选择器的默认路径
        clickProperties.put(key_outFilePath, "");
        // 运行自动流程时记录拖拽轨迹
        clickProperties.put(key_dragLog, enable);
        // 颜色选择器自定义颜色
        clickProperties.put(key_colorCustom, "");
        // 导出自动流程文件名称
        clickProperties.put(key_outFileName, "");
        // 运行自动流程时记录等待信息
        clickProperties.put(key_waitLog, enable);
        // 输入相关操作不移动鼠标开关
        clickProperties.put(key_noMove, disable);
        // 运行自动流程时记录移动轨迹
        clickProperties.put(key_moveLog, enable);
        // 自动保存操作列表
        clickProperties.put(key_autoSave, enable);
        // 运行自动流程时记录点击信息
        clickProperties.put(key_clickLog, enable);
        // 匹配失败重试间隔时间
        clickProperties.put(key_retrySecond, "1");
        // 终止操作窗口信息浮窗宽度
        clickProperties.put(key_stopWidth, "250");
        // 自动操作日志最大记录数量
        clickProperties.put(key_maxLogNum, "1000");
        // 默认横坐标随机偏移量
        clickProperties.put(key_randomClickX, "5");
        // 默认纵坐标随机偏移量
        clickProperties.put(key_randomClickY, "5");
        // 终止操作窗口信息浮窗高度
        clickProperties.put(key_stopHeight, "160");
        // 浮窗宽度
        clickProperties.put(key_clickWidth, "260");
        // 浮窗高度
        clickProperties.put(key_clickHeight, "180");
        // 终止操作窗口进程路径
        clickProperties.put(key_stopWindowPath, "");
        // 运行自动流程时记录终止图像识别信息
        clickProperties.put(key_stopImgLog, enable);
        // 运行自动流程时记录打开网址事件
        clickProperties.put(key_openUrlLog, enable);
        // 录制时记录拖鼠标拽轨迹
        clickProperties.put(key_recordDrag, enable);
        // 加载配置文件
        clickProperties.put(key_loadConfig, enable);
        // 录制时记录鼠标移动轨迹
        clickProperties.put(key_recordMove, enable);
        // 运行自动流程时记录打开文件事件
        clickProperties.put(key_openFileLog, enable);
        // 终止操作图片默认识别匹配度
        clickProperties.put(key_stopOpacity, "80.0");
        // 记录窗口准备时间
        clickProperties.put(key_findWindowWait, "3");
        // 目标窗口进程路径
        clickProperties.put(key_clickWindowPath, "");
        // 运行自动流程时记录键盘事件
        clickProperties.put(key_keyboardLog, enable);
        // 运行自动流程时记录目标图像识别信息
        clickProperties.put(key_clickImgLog, enable);
        // 执行自动流程前点击第一个起始坐标
        clickProperties.put(key_firstClick, disable);
        // 以文件夹的形式导入操作流程
        clickProperties.put(key_loadFolder, disable);
        // 目标窗口信息浮窗宽度
        clickProperties.put(key_messageWidth, "250");
        // 目标窗口信息浮窗高度
        clickProperties.put(key_messageHeight, "240");
        // 终止操作图像识别类型
        clickProperties.put(key_stopFindImgType, "0");
        // 要点击的图片默认识别匹配度
        clickProperties.put(key_clickOpacity, "80.0");
        // 鼠标轨迹采样间隔
        clickProperties.put(key_sampleInterval, "10");
        // 文件重名不覆盖
        clickProperties.put(key_notOverwrite, enable);
        // 运行时启用随机点击坐标偏移
        clickProperties.put(key_randomClick, disable);
        // 运行自动流程时记录运行脚本事件
        clickProperties.put(key_runScriptLog, enable);
        // 导出后打开文件夹
        clickProperties.put(key_openDirectory, enable);
        // 执行自动流程结束后弹出本程序
        clickProperties.put(key_showWindowRun, enable);
        // 运行自动流程时记录鼠标滚轮滚动
        clickProperties.put(key_mouseWheelLog, enable);
        // 运行自动流程时记录窗口移动事件开关
        clickProperties.put(key_moveWindowLog, enable);
        // 执行自动流程前最小化本程序
        clickProperties.put(key_hideWindowRun, enable);
        // 使用相对坐标开关
        clickProperties.put(key_useRelatively, enable);
        // 要点击的图像识别区域设置
        clickProperties.put(key_clickFindImgType, "0");
        // 选择终止操作的图片的选择器默认路径
        clickProperties.put(key_stopImgSelectPath, "");
        // 默认时长随机偏移量
        clickProperties.put(key_randomTimeOffset, "50");
        // 默认单次点击时长
        clickProperties.put(key_clickTimeOffset, "120");
        // 目标图片文件选择器默认路径
        clickProperties.put(key_clickImgSelectPath, "");
        // 运行自动操作前准备时间
        clickProperties.put(key_preparationRunTime, "");
        // 终止操作图像第一次识别失败后改为识别整个屏幕
        clickProperties.put(key_stopAllRegion, disable);
        // 定时任务详情页修改后未保存提示
        clickProperties.put(key_remindTaskSave, enable);
        // 录制时记录键盘事件开关
        clickProperties.put(key_recordKeyboard, enable);
        // 显示浮窗位置按钮显示的浮窗跟随鼠标
        clickProperties.put(key_mouseFloating, disable);
        // 执行自动流程时显示信息浮窗
        clickProperties.put(key_loadFloatingRun, enable);
        // 要点击的图像第一次识别失败后改为识别整个屏幕
        clickProperties.put(key_clickAllRegion, disable);
        // 运行时启用随机操作执行前等待时间偏移
        clickProperties.put(key_randomWaitTime, disable);
        // 应用标题栏展示鼠标位置
        clickProperties.put(key_titleCoordinate, enable);
        // 操作步骤详情页修改后未保存提示
        clickProperties.put(key_remindClickSave, enable);
        // 录制自动流程前最小化本程序
        clickProperties.put(key_hideWindowRecord, enable);
        // 终止操作图片默认识别失败重试次数
        clickProperties.put(key_defaultStopRetryNum, "0");
        // 录制自动流程结束后弹出本程序
        clickProperties.put(key_showWindowRecord, enable);
        // 运行时启用随机点击时长偏移
        clickProperties.put(key_randomClickTime, disable);
        // 录制时记录鼠标点击事件开关
        clickProperties.put(key_recordMouseClick, enable);
        // 录制时记录鼠标滑轮事件开关
        clickProperties.put(key_recordMouseWheel, enable);
        // 运行自动流程时信息浮窗跟随鼠标
        clickProperties.put(key_mouseFloatingRun, disable);
        // 录制自动操作前准备时间
        clickProperties.put(key_preparationRecordTime, "");
        // 目标图像默认识别失败重试次数
        clickProperties.put(key_defaultClickRetryNum, "3");
        // 终止操作图像窗口信息实时更新开关
        clickProperties.put(key_updateStopWindow, disable);
        // 要点击的图像窗口信息实时更新开关
        clickProperties.put(key_updateClickWindow, disable);
        // 录制自动流程时显示信息浮窗
        clickProperties.put(key_loadFloatingRecord, enable);
        // 录制自动流程时信息浮窗跟随鼠标
        clickProperties.put(key_mouseFloatingRecord, enable);
        // 运行时启用随机操作间隔时间偏移
        clickProperties.put(key_randomClickInterval, disable);
        // 浮窗字体颜色
        clickProperties.put(key_floatingTextColor, "0xffffffff");
    }

    /**
     * 默认浮窗横轴偏移量（30）
     */
    public static final int defaultOffsetX = Integer.parseInt(clickProperties.getProperty(key_offsetX));

    /**
     * 默认浮纵轴窗偏移量（30）
     */
    public static final int defaultOffsetY = Integer.parseInt(clickProperties.getProperty(key_offsetY));

    /**
     * 默认终止操作图片默认识别匹配匹配度（80.0）
     */
    public static final String defaultStopOpacity = clickProperties.getProperty(key_stopOpacity);

    /**
     * 默认要点击的图片默认识别匹配匹配度（80.0）
     */
    public static final String defaultClickOpacity = clickProperties.getProperty(key_clickOpacity);

    /**
     * 默认随机偏移时长（50）
     */
    public static final String defaultRandomTime = clickProperties.getProperty(key_randomTimeOffset);

    /**
     * 默认点击时长（120）
     */
    public static final String defaultClickTimeOffset = clickProperties.getProperty(key_clickTimeOffset);

    /**
     * 默认横轴随机偏移量（5）
     */
    public static final String defaultRandomClickX = clickProperties.getProperty(key_randomClickX);

    /**
     * 默认纵轴随机偏移量（5）
     */
    public static final String defaultRandomClickY = clickProperties.getProperty(key_randomClickY);

    /**
     * 默认鼠标轨迹采样间隔（10）
     */
    public static final String defaultSampleInterval = clickProperties.getProperty(key_sampleInterval);

    /**
     * 默认要点击的图片识别重试次数（3）
     */
    public static final String defaultClickRetryNum = clickProperties.getProperty(key_defaultClickRetryNum);

    /**
     * 默认终止操作图片识别重试次数（0）
     */
    public static final String defaultStopRetryNum = clickProperties.getProperty(key_defaultStopRetryNum);

    /**
     * 默认设置目标窗口准备时间（3）
     */
    public static final String defaultFindWindowWait = clickProperties.getProperty(key_findWindowWait);

    /**
     * 默认循环次数
     */
    public static final String defaultLoopTime = "1";

    /**
     * 默认录制准备时间
     */
    public static final String defaultPreparationRecord = "3";

    /**
     * 默认运行准备时间
     */
    public static final String defaultPreparationRun = "3";

    /**
     * 默认信息浮窗宽度
     */
    public static final String defaultFloatingWidth = "250";

    /**
     * 默认信息浮窗高度
     */
    public static final String defaultFloatingHeight = "240";

    /**
     * 默认信息浮窗宽度（ {@value priv.koishi.pmc.Finals.DefaultConfig.AutoClickDefault#defaultFloatingWidth}）
     */
    public static final int defaultFloatingWidthInt = Integer.parseInt(defaultFloatingWidth);

    /**
     * 默认信息浮窗高度（ {@value priv.koishi.pmc.Finals.DefaultConfig.AutoClickDefault#defaultFloatingHeight}）
     */
    public static final int defaultFloatingHeightInt = Integer.parseInt(defaultFloatingHeight);

    /**
     * 默认颜色容差
     */
    public static final int defaultColorTolerance = 10;

    /**
     * 默认图像识别范围窗口宽度
     */
    public static final int minFindImgWidth = 50;

    /**
     * 默认图像识别范围窗口高度
     */
    public static final int minFindImgHeight = 50;

}
