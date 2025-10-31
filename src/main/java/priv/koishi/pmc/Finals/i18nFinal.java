package priv.koishi.pmc.Finals;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import javafx.scene.input.MouseButton;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import priv.koishi.pmc.Finals.Enum.*;

import java.util.*;
import java.util.stream.Collectors;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.MainApplication.bundle;

/**
 * 国际化相关文本常量
 *
 * @author KOISHI
 * Date:2025-06-10
 * Time:13:02
 */
public class i18nFinal {

    /**
     * 自动保存文件名
     *
     * @return PMC自动导出流程
     */
    public static String autoSaveFileName() {
        return bundle.getString("autoSaveFileName");
    }

    /**
     * 默认导出文件名称
     *
     * @return PMC自动操作流程
     */
    public static String defaultOutFileName() {
        return bundle.getString("defaultOutFileName");
    }

    /**
     * 时任务默认任务名称
     *
     * @return 自动启动任务
     */
    public static String defaultTaskName() {
        return bundle.getString("defaultTaskName");
    }

    /**
     * @return 运行自动操作后可点击查看操作记录
     */
    public static String tip_clickLog() {
        return bundle.getString("tip.clickLog");
    }

    /**
     * @return 点击将会清除所有操作记录
     */
    public static String tip_removeAll_Log() {
        return bundle.getString("tip.removeAll_Log");
    }

    /**
     * @return logs 文件夹下只会保留该配置所填写数量的 log 日志，不填将会保留全部日志
     */
    public static String tip_logsNum() {
        return bundle.getString("tip.logsNum");
    }

    /**
     * @return 点击将会清空列表中的数据
     */
    public static String tip_learButton() {
        return bundle.getString("tip.learButton");
    }

    /**
     * @return 勾选后任务结束将会打开对应文件夹
     */
    public static String tip_openDirectory() {
        return bundle.getString("tip.openDirectory");
    }

    /**
     * @return 勾选后导出的同名文件名将会自动加上数字后缀
     */
    public static String tip_notOverwrite() {
        return bundle.getString("tip.notOverwrite");
    }

    /**
     * @return 点击即可跳转对应网盘分享页
     */
    public static String tip_openLink() {
        return bundle.getString("tip.openLink");
    }

    /**
     * @return 点击即可跳转对应 git 仓库，如果喜欢本项目可以给作者一个 star
     */
    public static String tip_openGitLink() {
        return bundle.getString("tip.openGitLink");
    }

    /**
     * @return 每步操作执行前等待时间，单位为毫秒，只能填自然数，不填默认为 0
     */
    public static String tip_wait() {
        return bundle.getString("tip.wait");
    }

    /**
     * @return 鼠标点击位置起始纵坐标，只能填自然数，不填默认为 0
     */
    public static String tip_mouseStartX() {
        return bundle.getString("tip.mouseStartX");
    }

    /**
     * @return 鼠标点击位置起始纵坐标，只能填自然数，不填默认为 0
     */
    public static String tip_mouseStartY() {
        return bundle.getString("tip.mouseStartY");
    }

    /**
     * @return 点击后将会按照列表中的步骤执行自动操作，执行自动化任务时按下 esc 即可取消任务
     */
    public static String tip_runClick() {
        return bundle.getString("tip.runClick");
    }

    /**
     * @return 点击后将会根据设置在列表中添加一条操作步骤
     */
    public static String tip_addPosition() {
        return bundle.getString("tip.addPosition");
    }

    /**
     * @return 自动操作循环次数，只能填自然数，不填默认为 1，填 0 为无限循环
     */
    public static String tip_loopTime() {
        return bundle.getString("tip.loopTime");
    }

    /**
     * @return 每步操作执行次数，点击为多次点击，长按为多次长按，只能填自然数，不填默认为 1
     */
    public static String tip_clickNumBer() {
        return bundle.getString("tip.clickNumBer");
    }

    /**
     * @return 每步操作需要按下的键
     */
    public static String tip_clickKey() {
        return bundle.getString("tip.clickKey");
    }

    /**
     * @return 操作列表中点击加拖拽数量必须与松开操作数量一致 设置点击图像后只能选择点击后松开或移动到设置坐标
     */
    public static String tip_clickType() {
        return bundle.getString("tip.clickType");
    }

    /**
     * @return 每步操作中，单次操作的点击时长，单位为毫秒，只能填自然数，不填默认为 0
     */
    public static String tip_clickTime() {
        return bundle.getString("tip.clickTime");
    }

    /**
     * @return 每步操作中，单次操作的时间间隔，单位为毫秒，只能填自然数，不填默认为 0
     */
    public static String tip_clickInterval() {
        return bundle.getString("tip.clickInterval");
    }

    /**
     * @return 每步操作的名称，不填将给一个默认名称
     */
    public static String tip_clickName() {
        return bundle.getString("tip.clickName");
    }

    /**
     * @return 点击可设置操作流程导出文件夹地址
     */
    public static String tip_outAutoClickPath() {
        return bundle.getString("tip.outAutoClickPath");
    }

    /**
     * @return 点击后选择要导入的操作流程即可在列表中追加
     */
    public static String tip_loadAutoClick() {
        return bundle.getString("tip.loadAutoClick");
    }

    /**
     * @return 点击即可按照设置导出文件夹与文件名导出列表中的操作流程
     */
    public static String tip_exportAutoClick() {
        return bundle.getString("tip.exportAutoClick");
    }

    /**
     * @return 勾选后运行或测试自动操作结束后将会弹出本应用的窗口
     */
    public static String tip_showWindowRun() {
        return bundle.getString("tip.showWindowRun");
    }

    /**
     * @return 勾选后录制自动操作开始前将会隐藏本应用的窗口
     */
    public static String tip_hideWindowRecord() {
        return bundle.getString("tip.hideWindowRecord");
    }

    /**
     * @return 勾选后录制自动操作结束后将会弹出本应用的窗口
     */
    public static String tip_showWindowRecord() {
        return bundle.getString("tip.showWindowRecord");
    }

    /**
     * @return 勾选后在运行自动操作时信息浮窗会跟随鼠标移动
     */
    public static String tip_mouseFloatingRun() {
        return bundle.getString("tip.mouseFloatingRun");
    }

    /**
     * @return 勾选后在录制自动操作时信息浮窗会跟随鼠标移动
     */
    public static String tip_mouseFloatingRecord() {
        return bundle.getString("tip.mouseFloatingRecord");
    }

    /**
     * @return 勾选后点击 显示浮窗位置 按钮所显示的浮窗会跟随鼠标移动
     */
    public static String tip_mouseFloating() {
        return bundle.getString("tip.mouseFloating");
    }

    /**
     * @return 用来控制浮窗距离屏幕边界的最小距离，只能填自然数，单位为像素，不填默认为 0
     */
    public static String tip_margin() {
        return bundle.getString("tip.margin");
    }

    /**
     * @return 在录制自动操作前将会等待的时间，只能填自然数，单位秒，不填默认为
     */
    public static String tip_preparationRecordTime() {
        return bundle.getString("tip.preparationRecordTime");
    }

    /**
     * @return 在运行自动操作前将会等待的时间，只能填自然数，单位秒，不填默认为
     */
    public static String tip_preparationRunTime() {
        return bundle.getString("tip.preparationRunTime");
    }

    /**
     * @return 勾选后在运行自动操作时将会显示一个运行信息浮窗
     */
    public static String tip_floatingRun() {
        return bundle.getString("tip.floatingRun");
    }

    /**
     * @return 勾选后在录制自动操作时将会显示一个录制信息浮窗
     */
    public static String tip_floatingRecord() {
        return bundle.getString("tip.floatingRecord");
    }

    /**
     * @return 点击后将会展示浮窗位置，之后按下 esc 可关闭浮窗
     */
    public static String tip_massageRegion() {
        return bundle.getString("tip.setFloatingCoordinate");
    }

    /**
     * @return 点击后将会展识别区域浮窗，之后按下 esc 可关闭浮窗
     */
    public static String tip_showRegion() {
        return bundle.getString("tip.showRegion");
    }

    /**
     * @return 点击后将会关闭浮窗
     */
    public static String tip_closeFloating() {
        return bundle.getString("tip.closeFloating");
    }

    /**
     * @return 点击后将会保存并关闭浮窗设置
     */
    public static String tip_saveFloating() {
        return bundle.getString("tip.saveFloating");
    }

    /**
     * @return <p>勾选后将根据相对坐标与目标窗口宽高进行换算</p>
     * 只有设置图像识别目标窗口后才可使用
     */
    public static String tip_useRelatively() {
        return bundle.getString("tip.useRelatively");
    }

    /**
     * @return <p>使用相对坐标时将更新绝对坐标</p>
     * 使用绝对坐标时将更新相对坐标
     */
    public static String tip_updateCoordinate() {
        return bundle.getString("tip.updateCoordinate");
    }

    /**
     * @return 浮窗将会根据填写值向左右偏移，只能填整数，正数向右偏移，负数向左偏移，不填默认为
     */
    public static String tip_offsetX() {
        return bundle.getString("tip.offsetX");
    }

    /**
     * @return 浮窗将会根据填写值向上下偏移，只能填整数，正数向下偏移，负数向上偏移，不填默认为
     */
    public static String tip_offsetY() {
        return bundle.getString("tip.offsetY");
    }

    /**
     * @return 将会修改自动操作运行与录制时的信息浮窗字体颜色
     */
    public static String tip_colorPicker() {
        return bundle.getString("tip.colorPicker");
    }

    /**
     * @return 感谢贴吧吧友 @拒绝神绮99次 设计的 logo
     */
    public static String tip_thanks() {
        return bundle.getString("tip.thanks");
    }

    /**
     * @return 感谢你的赞赏！
     */
    public static String tip_appreciate() {
        return bundle.getString("tip.appreciate");
    }

    /**
     * @return 勾选后将会在应用启动时加载自动操作工具上次关闭时的设置
     */
    public static String tip_lastAutoClickSetting() {
        return bundle.getString("tip.lastAutoClickSetting");
    }

    /**
     * @return 滑块将会改变当前步骤要点及的图像识别匹配度，建议设置 80% 以上
     */
    public static String tip_clickOpacity() {
        return bundle.getString("tip.clickOpacity");
    }

    /**
     * @return 滑块将会改变当前步骤所有终止操作图像识别匹配度，建议设置 80% 以上
     */
    public static String tip_stopOpacity() {
        return bundle.getString("tip.stopOpacity");
    }

    /**
     * @return 点击后可添加用来终止当前步骤的图片，建议不要添加太多影响图像识别效率
     */
    public static String tip_stopImgBtn() {
        return bundle.getString("tip.stopImgBtn");
    }

    /**
     * @return 创建新的操作步骤时会自动将列表中的图片加入对应步骤中，建议不要添加太多影响图像识别效率
     */
    public static String tip_defaultStopImgBtn() {
        return bundle.getString("tip.defaultStopImgBtn");
    }

    /**
     * @return 点击后可添加需要点击的图片，建议选择特征明显范围较小的图片
     */
    public static String tip_clickImgBtn() {
        return bundle.getString("tip.clickImgBtn");
    }

    /**
     * @return 删除需要点击的图片
     */
    public static String tip_removeClickImgBtn() {
        return bundle.getString("tip.removeClickImgBtn");
    }

    /**
     * @return 删除列表所有终止操作图片
     */
    public static String tip_removeStopImgBtn() {
        return bundle.getString("tip.removeStopImgBtn");
    }

    /**
     * @return 将当前步骤的步骤名称更改为要点击的图片名称
     */
    public static String tip_updateClickNameBtn() {
        return bundle.getString("tip.updateClickNameBtn");
    }

    /**
     * @return 将当前步骤的步骤名称更改为要点击的图片名称
     */
    public static String tip_overtime() {
        return bundle.getString("tip.overtime");
    }

    /**
     * @return 只能填自然整数，单位秒，不填默认为 1，每张图片识别失败后将会等着当前设置值后再重试
     */
    public static String tip_retrySecond() {
        return bundle.getString("tip.retrySecond");
    }

    /**
     * @return 点击将会重启本应用并保存设置项
     */
    public static String tip_reLaunch() {
        return bundle.getString("tip.reLaunch");
    }

    /**
     * @return 勾选后如果修改过操作步骤详情页的设置后直接点窗口上的关闭按钮将会弹出是否保存的确认框
     */
    public static String tip_remindSave() {
        return bundle.getString("tip.remindSave");
    }

    /**
     * @return 勾选后如果修改过操作定时任务详情页的设置后直接点窗口上的关闭按钮将会弹出是否保存的确认框
     */
    public static String tip_remindTaskSave() {
        return bundle.getString("tip.remindTaskSave");
    }

    /**
     * @return 前步骤序号为：
     */
    public static String tip_clickIndex() {
        return bundle.getString("tip.clickIndex");
    }

    /**
     * @return 当前操作步骤列表共有操作步骤数量为：
     */
    public static String tip_tableViewSize() {
        return bundle.getString("tip.tableViewSize");
    }

    /**
     * @return 只能填正整数，不可为空，不可大于操作步骤列表中步骤总数，不可填当前步骤序号，目标序号变更后需重新设置
     */
    public static String tip_step() {
        return bundle.getString("tip.step");
    }

    /**
     * @return 要识别的图像匹配成功后将会根据选项进行不同的操作
     */
    public static String tip_matchedType() {
        return bundle.getString("tip.matchedType");
    }

    /**
     * @return 要识别的图像匹配失败后将会根据选项进行不同的操作
     */
    public static String tip_retryType() {
        return bundle.getString("tip.retryType");
    }

    /**
     * @return 勾选后将会在录制自动操作时记录鼠标没有拖拽时的移动轨迹
     */
    public static String tip_recordMove() {
        return bundle.getString("tip.recordMove");
    }

    /**
     * @return 勾选后将会在录制自动操作时记录鼠标拖拽状态时的移动轨迹
     */
    public static String tip_recordDrag() {
        return bundle.getString("tip.recordDrag");
    }

    /**
     * @return 勾选后运行自动操作点击时将按设置进行点击位置的坐标偏移
     */
    public static String tip_randomClick() {
        return bundle.getString("tip.randomClick");
    }

    /**
     * @return 勾选后运行自动操作移动鼠标时将按设置进行轨迹坐标的偏移
     */
    public static String tip_randomTrajectory() {
        return bundle.getString("tip.randomTrajectory");
    }

    /**
     * @return 勾选后运行自动操作点击时长将按设置的时间偏移量进行偏移
     */
    public static String tip_randomClickTime() {
        return bundle.getString("tip.randomClickTime");
    }

    /**
     * @return 勾选后运行自动操作时多次点击的操作步骤的点击间隔将按设置的时间偏移量进行偏移
     */
    public static String tip_randomClickInterval() {
        return bundle.getString("tip.randomClickInterval");
    }

    /**
     * @return 勾选后运行自动操作时每步操前的等待时间将按照设置的时间偏移量进行偏移
     */
    public static String tip_randomWaitTime() {
        return bundle.getString("tip.randomWaitTime");
    }

    /**
     * @return 用来限制运行记录数量的设置，只能填正整数，不填则不限制最大记录数量
     */
    public static String tip_maxLogNum() {
        return bundle.getString("tip.maxLogNum");
    }

    /**
     * @return 下次启动应用将会按照此设置项分配应用最大运行内存，单位为GB，只能填自然数，为空将设置为默认值操作系统最大内存的1/4
     */
    public static String tip_nextRunMemory() {
        return bundle.getString("tip.nextRunMemory");
    }

    /**
     * @return 只能填 0 到 23 的自然数，不填默认为 0
     */
    public static String tip_hour() {
        return bundle.getString("tip.hour");
    }

    /**
     * @return 只能填 0 到 59 的自然数，不填默认为 0
     */
    public static String tip_minute() {
        return bundle.getString("tip.minute");
    }

    /**
     * @return 定时任务的名称，保存后无法修改，同名任务将会被覆盖，不填默认为
     */
    public static String tip_taskName() {
        return bundle.getString("tip.taskName");
    }

    /**
     * @return 重复类型选择 仅一次 时才可修改，其他重复类型只能设置当天生效
     */
    public static String tip_datePicker() {
        return bundle.getString("tip.datePicker");
    }

    /**
     * @return 定时任务将按照此选项设置类型间隔进行运作
     */
    public static String tip_repeatType() {
        return bundle.getString("tip.repeatType");
    }

    /**
     * @return 点击后将会打开定时任务设置页，设置定时任务并保存成功后将会在下面列表中显示
     */
    public static String tip_addTimedTask() {
        return bundle.getString("tip.addTimedTask");
    }

    /**
     * @return 点击后将会刷新定时任务列表，查询最新设置
     */
    public static String tip_getScheduleTask() {
        return bundle.getString("tip.getScheduleTask");
    }

    /**
     * @return 删除所选文件
     */
    public static String tip_deletePath() {
        return bundle.getString("tip.deletePath");
    }

    /**
     * @return 用来设置定时启动本应用后自动执行的自动操作
     */
    public static String tip_loadAutoClickBtn() {
        return bundle.getString("tip.loadAutoClickBtn");
    }

    /**
     * @return <p>下次启动应用将会按照此设置项设置垃圾回收（GC）方式</p>
     * <p>G1GC：分区回收，低延迟与吞吐量平衡</p>
     * <p>ZGC：亚毫秒级停顿</p>
     * <p>ParallelGC：多线程并行回收，吞吐量优先</p>
     * <p>ShenandoahGC：全并发回收，停顿时间与堆大小无关</p>
     * SerialGC：单线程回收，简单高效
     */
    public static String tip_nextGcType() {
        return bundle.getString("tip.nextGcType");
    }

    /**
     * @return 修改应用显示的语言，修改后重启应用才会生效
     */
    public static String tip_language() {
        return bundle.getString("tip.language");
    }

    /**
     * @return <p>在开启相应设置后，录制自动操作时会按照设置值的时间间隔记录拖拽和移动时的鼠标轨迹</p>
     * 只能填自然数，单位为毫秒，数字越小越接近录制轨迹，不填默认为
     */
    public static String tip_sampleInterval() {
        return bundle.getString("tip.sampleInterval");
    }

    /**
     * @return <p>要点击的图片识别没有匹配项后将会按照设置次数再次识别</p>
     * 只能填自然数，不填默认为
     */
    public static String tip_clickRetryNum() {
        return bundle.getString("tip.clickRetryNum");
    }

    /**
     * @return <p>终止操作图片识别没有匹配项后将会按照设置次数再次识别</p>
     * 只能填自然数，不填默认为
     */
    public static String tip_stopRetryNum() {
        return bundle.getString("tip.stopRetryNum");
    }

    /**
     * @return <p>滑动将会改变录制或运行自动操作时浮窗透明度</p>
     * <p>透明的为 0 时 Windows 下鼠标将会点击透过浮窗</p>
     * macOS 暂时无法实现鼠标点击透过
     */
    public static String tip_opacity() {
        return bundle.getString("tip.opacity");
    }

    /**
     * @return <p>点击录制自动操作按钮将会等待设置的准备时间后开始录制自动操作</p>
     * 每次鼠标点击并松开为一个步骤，每次点击间隔为操作前等待时间
     */
    public static String tip_recordClick() {
        return bundle.getString("tip.recordClick");
    }

    /**
     * @return <p>不用填写文件拓展名，导出文件为 .pmc 格式，如果导出文件夹已经存在同名文件不会覆盖</p>
     * <p>文件名不能包含  <>:"/\|?*</p>
     * 设置为空或者不合法将会以默认名称命名，默认名称为：
     */
    public static String tip_autoClickFileName() {
        return bundle.getString("tip.autoClickFileName");
    }

    /**
     * @return <p>勾选后：</p>
     * <p>如果是运行 测试操作流程 则会 鼠标左键 点击一次设置栏设置的起始坐标后再执行测试操作</p>
     * <p>如果是运行 自动化操作 则会 鼠标左键 点击一次第一步操作的起始坐标后再执行自动化操作</p>
     * <p>Windows 一般会直接点击对应窗口的对应坐标，macOS 可能需要先点击对应窗口将焦点切换过去才能点中对应窗口的对应坐标</p>
     * 建议 Windows 用户不要勾选， macOS 用户视情况勾选
     */
    public static String tip_firstClick() {
        return bundle.getString("tip.firstClick");
    }

    /**
     * @return <p>勾选后在应用关闭时如果列表不为空将会保存列表的所有操作步骤</p>
     * 自动保存路径为导出文件夹路径，文件名为：
     */
    public static String tip_autoSave() {
        return bundle.getString("tip.autoSave");
    }

    /**
     * @return <p>勾选后运行或测试自动操作开始前将会隐藏本应用的窗口</p>
     * 如果有图像识别设置最好勾选，操作列表缩略图可能会干扰识别准确度
     */
    public static String tip_hideWindowRun() {
        return bundle.getString("tip.hideWindowRun");
    }

    /**
     * @return <p>勾选后将会在图像识别前自动更新窗口信息</p>
     * <p>建议在窗口位置和大小不会改变的情况下不要勾选</p>
     * 启动自动操作时将会自动更新一次窗口信息
     */
    public static String tip_alwaysRefresh() {
        return bundle.getString("tip.alwaysRefresh");
    }

    /**
     * @return <p>设置目标窗口后才可填写，用来计算相对坐标</p>
     * 有效范围 0 - 100，最多保留两位小数
     */
    public static String tip_relatively() {
        return bundle.getString("tip.relatively");
    }

    /**
     * @return <p>需要在 macOS 系统设置中启用辅助功能权限：</p>
     * <p>1. 打开 [系统偏好设置 → 安全性与隐私 → 辅助功能]</p>
     * <p>2. 点击🔒解锁设置</p>
     * <p>3. 删除列表中的 {@value priv.koishi.pmc.Finals.CommonFinals#appName}{@value priv.koishi.pmc.Finals.CommonFinals#app} (如果有的话)</p>
     * <p>4. 将 {@value priv.koishi.pmc.Finals.CommonFinals#appName}{@value priv.koishi.pmc.Finals.CommonFinals#app} 添加到允许列表中</p>
     * 5. 重启 {@value priv.koishi.pmc.Finals.CommonFinals#appName}{@value priv.koishi.pmc.Finals.CommonFinals#app}
     */
    public static String tip_NativeHookException() {
        return bundle.getString("tip.NativeHookException") + appName + app +
                bundle.getString("tip.ifHave") + appName + app +
                bundle.getString("tip.addList") + appName + app;
    }

    /**
     * @return <p>需要在 macOS 系统设置中启用录屏与系统录音权限：</p>
     * <p>1. 打开 [系统偏好设置 → 安全性与隐私 → 录屏与系统录音]</p>
     * <p>2. 点击🔒解锁设置</p>
     * <p>3. 删除列表中的 {@value priv.koishi.pmc.Finals.CommonFinals#appName}{@value priv.koishi.pmc.Finals.CommonFinals#app} (如果有的话)</p>
     * <p>4. 将 {@value priv.koishi.pmc.Finals.CommonFinals#appName}{@value priv.koishi.pmc.Finals.CommonFinals#app} 添加到允许列表中</p>
     * 5. 重启 {@value priv.koishi.pmc.Finals.CommonFinals#appName}{@value priv.koishi.pmc.Finals.CommonFinals#app}
     */
    public static String tip_noScreenCapturePermission() {
        return bundle.getString("tip.noScreenCapturePermission") + appName + app +
                bundle.getString("tip.ifHave") + appName + app +
                bundle.getString("tip.addList") + appName + app;
    }

    /**
     * @return <p>需要在 macOS 系统设置中启用自动化权限：</p>
     * <p>1. 打开 [系统偏好设置 → 安全性与隐私 → 自动化]</p>
     * <p>2. 点击🔒解锁设置</p>
     * <p>3. 选择列表中的 {@value priv.koishi.pmc.Finals.CommonFinals#appName}{@value priv.koishi.pmc.Finals.CommonFinals#app} (如果有的话)</p>
     * <p>4. 将 {@value priv.koishi.pmc.Finals.CommonFinals#appName}{@value priv.koishi.pmc.Finals.CommonFinals#app} 的 System Events 选项开启</p>
     * <p>5. 重启 {@value priv.koishi.pmc.Finals.CommonFinals#appName}{@value priv.koishi.pmc.Finals.CommonFinals#app}</p>
     * 如果列表中没有 {@value priv.koishi.pmc.Finals.CommonFinals#appName}{@value priv.koishi.pmc.Finals.CommonFinals#app} 尝试重新安装应用后开启权限
     */
    public static String tip_noAutomationPermission() {
        return bundle.getString("tip.noAutomationPermission") + appName + app +
                bundle.getString("tip.ifHave") + appName + app +
                bundle.getString("tip.activeSystemEvents") + appName + app +
                bundle.getString("tip.ifNotHave") + appName + app + bundle.getString("tip.reinstall");
    }

    /**
     * @return <p>运行自动操作时横坐标将随机在设置值范围内随机发生左右偏移，可用来模仿手动移动和点击鼠标</p>
     * 单位为像素，只能填自然数，不填默认为
     */
    public static String tip_randomClickX() {
        return bundle.getString("tip.randomClickX");
    }

    /**
     * @return <p>运行自动操作时纵坐标将随机在设置值范围内随机发生上下偏移，可用来模仿手动移动和点击鼠标</p>
     * 单位为像素，只能填自然数，不填默认为
     */
    public static String tip_randomClickY() {
        return bundle.getString("tip.randomClickY");
    }

    /**
     * @return <p>运行自动操作时执行前等待时间、每个步骤内的操作间隔、点击时长都会以此项设置进行随机偏移</p>
     * 偏移后时间最小为0，单位为毫秒，只能填自然数，不填默认为
     */
    public static String tip_randomTime() {
        return bundle.getString("tip.randomTime");
    }

    /**
     * @return <p>手动创建新的操作步骤时将会按照此设置先设置默认点击时长</p>
     * 单位为毫秒，只能填自然数，不填默认为
     */
    public static String tip_clickTimeOffset() {
        return bundle.getString("tip.clickTimeOffset");
    }

    /**
     * @return <p>目标图像识别成功后横坐标将按此设置发生偏移</p>
     * <p>单位为像素，只能填整数，不填默认为 0</p>
     * 向左为负数，向右为正数
     */
    public static String tip_imgX() {
        return bundle.getString("tip.imgX");
    }

    /**
     * @return <p>目标图像识别成功后横纵标将按此设置发生偏移</p>
     * <p>单位为像素，只能填整数，不填默认为 0</p>
     * 向下为正数，向上为负数
     */
    public static String tip_imgY() {
        return bundle.getString("tip.imgY");
    }

    /**
     * @return 所填序号不存在
     */
    public static String tip_notExistsIndex() {
        return bundle.getString("tip.notExistsIndex");
    }

    /**
     * @return 点击后将会关闭当前页面，不会将任何数据添加到要移动的文件列表中
     */
    public static String tip_close() {
        return bundle.getString("tip.close");
    }

    /**
     * @return 点击后将会把列表中选中的文件夹添加到要移动的文件列表中，没有选中项则添加当前查询的目录
     */
    public static String tip_confirm() {
        return bundle.getString("tip.confirm");
    }

    /**
     * @return 点击后可选择要查询的目录
     */
    public static String tip_selectPath() {
        return bundle.getString("tip.selectPath");
    }

    /**
     * @return 点击可返回上级目录
     */
    public static String tip_gotoParent() {
        return bundle.getString("tip.gotoParent");
    }

    /**
     * @return 点击将会按配置项重新查信息到列表中
     */
    public static String tip_reselectButton() {
        return bundle.getString("tip.reselectButton");
    }

    /**
     * @return 将会按照这里填写的字符进行筛选
     */
    public static String tip_fileNameFilter() {
        return bundle.getString("tip.fileNameFilter");
    }

    /**
     * @return 填写后只会识别所填写的后缀名文件，多个文件后缀名用空格隔开，后缀名需带 '.'
     */
    public static String tip_filterFileType() {
        return bundle.getString("tip.filterFileType");
    }

    /**
     * @return 用来选择文件名匹配的方式
     */
    public static String tip_fileNameType() {
        return bundle.getString("tip.fileNameType");
    }

    /**
     * @return 点击可选择隐藏文件查询逻辑
     */
    public static String tip_hideFileType() {
        return bundle.getString("tip.hideFileType");
    }

    /**
     * @return 点击可选择文件与文件夹查询逻辑
     */
    public static String tip_directoryNameType() {
        return bundle.getString("tip.directoryNameType");
    }

    /**
     * @return <p>勾选后只能选择 PMC 文件所在文件夹</p>
     * <p>选中文件夹后将会导入文件夹下所有的 PMC 文件，如果文件夹中有图片，</p>
     * 将会按照图片名称自动匹配未匹配到图片的要点击的图像设置
     */
    public static String tip_loadFolder_Click() {
        return bundle.getString("tip.loadFolder_Click");
    }

    /**
     * @return 点击即可查询最新版本，查询失败可点击上方对应的网盘按钮查询
     */
    public static String tip_checkUpdate_Abt() {
        return bundle.getString("tip.checkUpdate_Abt");
    }

    /**
     * @return 点击可选择图像识别范围
     *
     */
    public static String tip_findImgType() {
        return bundle.getString("tip.findImgType");
    }

    /**
     * @return 勾选后第一次识别失败后，后续重试时识别区域将会改为整个屏幕
     */
    public static String tip_allRegion() {
        return bundle.getString("tip.allRegion");
    }

    /**
     * @return 用来设置窗口识别的准备时间，只能填自然数，单位秒，不填默认为
     */
    public static String tip_findWindowWait() {
        return bundle.getString("tip.findWindowWait");
    }

    /**
     * @return 点击后可开始记录需要识别的窗口
     */
    public static String tip_findWindow() {
        return bundle.getString("tip.findWindow");
    }

    /**
     * @return 用来设置应用的外观
     */
    public static String tip_them() {
        return bundle.getString("tip.them");
    }

    /**
     * @return 点击将会根据当前设置打开文件、网址或运行脚本
     */
    public static String tip_testLink() {
        return bundle.getString("tip.testLink");
    }

    /**
     * @return 点击将即可选择要打开的文件或要运行的脚本
     */
    public static String tip_pathLink() {
        return bundle.getString("tip.pathLink");
    }

    /**
     * @return 击将即可选择脚本执行时的工作目录
     */
    public static String tip_workDir() {
        return bundle.getString("tip.workDir");
    }

    /**
     * @return 需要填写完整的网址
     */
    public static String tip_url() {
        return bundle.getString("tip.url");
    }

    /**
     * @return 填写脚本需要的参数，多个参数用空格隔开
     */
    public static String tip_parameter() {
        return bundle.getString("tip.parameter");
    }

    /**
     * @return 勾选后将会最小化终端窗口运行脚本
     */
    public static String tip_minWindow() {
        return bundle.getString("tip.minWindow");
    }

    /**
     * @return 删除工作目录
     */
    public static String tip_removeWorkDir() {
        return bundle.getString("tip.removeWorkDir");
    }

    /**
     * @return <p>版本：{@value priv.koishi.pmc.Finals.CommonFinals#version}</p>
     * <p>构建日期：{@value priv.koishi.pmc.Finals.CommonFinals#buildDate}</p>
     * JDK版本：Oracle Corporation 25+36-3489
     */
    public static String tip_version() {
        return """
                %s：%s
                %s：%s
                JDK %s：%s""".formatted(
                bundle.getString("tip.version"), version,
                bundle.getString("tip.buildDate"), buildDate,
                bundle.getString("tip.version"), jdkVersion);
    }

    /**
     * @return 选择文件夹
     */
    public static String text_selectDirectory() {
        return bundle.getString("selectDirectory");
    }

    /**
     * @return 选择文件夹或文件
     */
    public static String text_selectFileFolder() {
        return bundle.getString("selectFileFolder");
    }

    /**
     * @return 选择要识别的图片
     */
    public static String text_selectTemplateImg() {
        return bundle.getString("selectTemplateImg");
    }

    /**
     * @return 导出文件夹位置为空，需要先设置导出文件夹位置再继续
     */
    public static String text_outPathNull() {
        return bundle.getString("outPathNull");
    }

    /**
     * @return 选择自动操作流程文件
     */
    public static String text_selectAutoFile() {
        return bundle.getString("selectAutoFile");
    }

    /**
     * @return 文件不存在
     */
    public static String text_fileNotExists() {
        return bundle.getString("fileNotExists");
    }

    /**
     * @return 测试完成
     */
    public static String text_testSuccess() {
        return bundle.getString("testSuccess");
    }

    /**
     * @return 脚本文件不可执行
     */
    public static String text_scriptNotExecutable() {
        return bundle.getString("scriptNotExecutable");
    }

    /**
     * @return 路径不能为空
     */
    public static String text_pathNull() {
        return bundle.getString("pathNull");
    }

    /**
     * @return 路径格式不正确
     */
    public static String text_errPathFormat() {
        return bundle.getString("errPathFormat");
    }

    /**
     * @return 共有
     */
    public static String text_allHave() {
        return bundle.getString("allHave");
    }

    /**
     * @return 复制成功
     */
    public static String text_copySuccess() {
        return bundle.getString("copySuccess");
    }

    /**
     * @return 保存成功
     */
    public static String text_successSave() {
        return bundle.getString("successSave");
    }

    /**
     * @return 当前设置值为：
     */
    public static String text_nowValue() {
        return bundle.getString("nowValue");
    }

    /**
     * @return 所有数据已导出到：
     */
    public static String text_saveSuccess() {
        return bundle.getString("saveSuccess");
    }

    /**
     * @return 已导入自动操作流程：
     */
    public static String text_loadSuccess() {
        return bundle.getString("loadSuccess");
    }

    /**
     * @return 轮操作
     */
    public static String text_executionTime() {
        return bundle.getString("executionTime");
    }

    /**
     * @return 正在执行第
     */
    public static String text_execution() {
        return bundle.getString("execution");
    }

    /**
     * @return 按下 esc 即可取消任务
     */
    public static String text_cancelTask() {
        return bundle.getString("cancelTask");
    }

    /**
     * @return <p>鼠标拖拽浮窗即可移动浮窗</p>
     * 按下esc 即可保存浮窗位置
     */
    public static String text_saveFloatingCoordinate() {
        return bundle.getString("saveFloatingCoordinate");
    }

    /**
     * @return <p>拖拽浮窗边缘即可调整浮窗大小</p>
     * <p>鼠标拖拽浮窗即可移动浮窗</p>
     * 按下 esc 即可保存浮窗位置
     */
    public static String text_saveFindImgConfig() {
        return bundle.getString("saveFindImgConfig") + bundle.getString("saveFloatingCoordinate");
    }

    /**
     * @return 按下 esc 即可关闭浮窗
     */
    public static String text_escCloseFloating() {
        return bundle.getString("escCloseFloating");
    }

    /**
     * @return 关闭浮窗
     */
    public static String text_closeFloating() {
        return bundle.getString("closeFloating");
    }

    /**
     * @return 保存并关闭浮窗设置
     */
    public static String text_saveCloseFloating() {
        return bundle.getString("saveCloseFloating");
    }

    /**
     * @return 显示浮窗位置
     */
    public static String text_showFloating() {
        return bundle.getString("showFloating");
    }

    /**
     * @return 导入自动化流程文件：
     */
    public static String text_loadAutoClick() {
        return bundle.getString("loadAutoClick");
    }

    /**
     * @return 内容格式不正确
     */
    public static String text_formatError() {
        return bundle.getString("formatError");
    }

    /**
     * @return 列表中没有要导出的自动操作流程
     */
    public static String text_noAutoClickList() {
        return bundle.getString("noAutoClickList");
    }

    /**
     * @return 列表中没有要执行的操作
     */
    public static String text_noAutoClickToRun() {
        return bundle.getString("noAutoClickToRun");
    }

    /**
     * @return 导入文件缺少关键数据
     */
    public static String text_missingKeyData() {
        return bundle.getString("missingKeyData");
    }

    /**
     * @return 步骤
     */
    public static String text_step() {
        return bundle.getString("step");
    }

    /**
     * @return 正在录制操作
     */
    public static String text_recordClicking() {
        return bundle.getString("recordClicking");
    }

    /**
     * @return 秒后开始录制操作
     */
    public static String text_preparation() {
        return bundle.getString("preparation");
    }

    /**
     * @return 秒后开始自动操作
     */
    public static String text_run() {
        return bundle.getString("run");
    }

    /**
     * @return 已记录
     */
    public static String text_recorded() {
        return bundle.getString("recorded");
    }

    /**
     * @return 出现错误，任务终止
     */
    public static String text_taskFailed() {
        return bundle.getString("taskFailed");
    }

    /**
     * @return 任务已取消
     */
    public static String text_taskCancelled() {
        return bundle.getString("taskCancelled");
    }

    /**
     * @return 所有操作都已执行完毕
     */
    public static String text_taskFinished() {
        return bundle.getString("taskFinished");
    }

    /**
     * @return (添加)
     */
    public static String text_isAdd() {
        return bundle.getString("isAdd");
    }

    /**
     * @return (录制)
     */
    public static String text_isRecord() {
        return bundle.getString("isRecord");
    }

    /**
     * @return 无图片
     */
    public static String text_noImg() {
        return bundle.getString("noImg");
    }

    /**
     * @return 图片文件缺失或损坏
     */
    public static String text_badImg() {
        return bundle.getString("badImg");
    }

    /**
     * @return 重试后要跳转的步骤序号不能大于列表步骤数量
     */
    public static String text_retryStepGreaterMax() {
        return bundle.getString("retryStepGreaterMax");
    }

    /**
     * @return 重试后要跳转的步骤序号不能等于当前步骤序号
     */
    public static String text_retryStepEqualIndex() {
        return bundle.getString("retryStepEqualIndex");
    }

    /**
     * @return 重试后要跳转的步骤序号不能为空
     */
    public static String text_retryStepIsNull() {
        return bundle.getString("retryStepIsNull");
    }

    /**
     * @return 匹配后要跳转的步骤序号不能大于列表步骤数量
     */
    public static String text_matchedStepGreaterMax() {
        return bundle.getString("matchedStepGreaterMax");
    }

    /**
     * @return 匹配后要跳转的步骤序号不能为空
     */
    public static String text_matchedStepIsNull() {
        return bundle.getString("matchedStepIsNull");
    }

    /**
     * @return 仅启动应用
     */
    public static String text_onlyLaunch() {
        return bundle.getString("onlyLaunch");
    }

    /**
     * @return 图片
     */
    public static String text_image() {
        return bundle.getString("img");
    }

    /**
     * @return 本轮进度：
     */
    public static String text_progress() {
        return bundle.getString("progress");
    }

    /**
     * @return 将在
     */
    public static String text_willBe() {
        return bundle.getString("willBe");
    }

    /**
     * @return 毫秒后将执行：
     */
    public static String text_msWillBe() {
        return bundle.getString("msWillBe");
    }

    /**
     * @return 执行到序号为：
     */
    public static String text_index() {
        return bundle.getString("taskIndex");
    }

    /**
     * @return 检查到序号为：
     */
    public static String text_checkIndex() {
        return bundle.getString("checkIndex");
    }

    /**
     * @return 无法获取到要点击的窗口信息
     */
    public static String text_noClickWindowInfo() {
        return bundle.getString("noClickWindowInfo");
    }

    /**
     * @return 无法获取到终止操作窗口信息
     */
    public static String text_noStopWindowInfo() {
        return bundle.getString("noStopWindowInfo");
    }

    /**
     * @return 正在检查图像识别范围设置
     */
    public static String text_checkingWindowInfo() {
        return bundle.getString("checkingWindowInfo");
    }

    /**
     * @return 正在检查跳转逻辑参数设置
     */
    public static String text_checkJumpSetting() {
        return bundle.getString("checkJumpSetting");
    }

    /**
     * @return 正在获取最新的目标窗口信息
     */
    public static String text_gettingWindowInfo() {
        return bundle.getString("gettingWindowInfo");
    }

    /**
     * @return 正在更新目标窗口信息
     */
    public static String text_updatingWindowInfo() {
        return bundle.getString("updatingWindowInfo");
    }

    /**
     * @return 图像识别范围设置异常
     */
    public static String text_windowInfoErr() {
        return bundle.getString("windowInfoErr");
    }

    /**
     * @return 跳转逻辑设置异常
     */
    public static String text_jumpSettingErr() {
        return bundle.getString("jumpSettingErr");
    }

    /**
     * @return 未记录窗口信息
     */
    public static String text_windowInfoNull() {
        return bundle.getString("windowInfoNull");
    }

    /**
     * @return 坐标：
     */
    public static String text_point() {
        return bundle.getString("point");
    }

    /**
     * @return 未知监听类型
     */
    public static String text_unknownListener() {
        return bundle.getString("unknownListener");
    }

    /**
     * @return 打开文件夹
     */
    public static String text_openDirectory() {
        return bundle.getString("openDirectory");
    }

    /**
     * @return 打开上级文件夹
     */
    public static String text_openParentDirectory() {
        return bundle.getString("openParentDirectory");
    }

    /**
     * @return 打开文件
     */
    public static String text_openFile() {
        return bundle.getString("openFile");
    }

    /**
     * @return 复制路径
     */
    public static String text_copyPath() {
        return bundle.getString("copyPath");
    }

    /**
     * @return 鼠标左键点击打开
     */
    public static String text_mouseClickOpen() {
        return bundle.getString("mouseClickOpen");
    }

    /**
     * @return 文件不存在，鼠标左键点击打开
     */
    public static String text_mouseClickOpenNull() {
        return bundle.getString("mouseClickOpenNull");
    }

    /**
     * @return 文件不存在
     */
    public static String text_mouseClickNull() {
        return bundle.getString("mouseClickNull");
    }

    /**
     * @return 图片地址：
     */
    public static String text_imgPath() {
        return bundle.getString("imgPath");
    }

    /**
     * @return 异常信息
     */
    public static String text_abnormal() {
        return bundle.getString("abnormal");
    }

    /**
     * @return 正在读取数据
     */
    public static String text_readData() {
        return bundle.getString("readData");
    }

    /**
     * @return 正在匹配图片
     */
    public static String text_matchImg() {
        return bundle.getString("matchImg");
    }

    /**
     * @return 正在导出PMC自动操作流程
     */
    public static String text_exportData() {
        return bundle.getString("exportData");
    }

    /**
     * @return 个文件
     */
    public static String text_file() {
        return bundle.getString("file");
    }

    /**
     * @return 隐藏
     */
    public static String text_hidden() {
        return bundle.getString("hidden");
    }

    /**
     * @return 非隐藏
     */
    public static String text_unhidden() {
        return bundle.getString("unhidden");
    }

    /**
     * @return 查询所选第一行文件
     */
    public static String text_checkFirstFile() {
        return bundle.getString("checkFirstFile");
    }

    /**
     * @return 查询中...
     */
    public static String text_searching() {
        return bundle.getString("searching");
    }

    /**
     * @return 读取 LaunchAgents 目录失败:
     */
    public static String text_searchLaunchAgentsErr() {
        return bundle.getString("searchLaunchAgentsErr");
    }

    /**
     * @return 任务创建失败:
     */
    public static String text_creatTaskErr() {
        return bundle.getString("creatTaskErr");
    }

    /**
     * @return 保存中...
     */
    public static String text_saving() {
        return bundle.getString("saving");
    }

    /**
     * @return 路径为空
     */
    public static String text_nullPath() {
        return bundle.getString("nullPath");
    }

    /**
     * @return 不存在
     */
    public static String text_noExists() {
        return bundle.getString("noExists");
    }

    /**
     * @return 匹配超时
     */
    public static String text_timeOut() {
        return bundle.getString("timeOut");
    }

    /**
     * @return 屏幕图像获取失败:
     */
    public static String text_screenErr() {
        return bundle.getString("screenErr");
    }

    /**
     * @return 操作被用户取消:
     */
    public static String text_cancel() {
        return bundle.getString("cancel");
    }

    /**
     * @return 操作内容：
     */
    public static String text_taskInfo() {
        return bundle.getString("taskInfo");
    }

    /**
     * @return 单次操作时间：
     */
    public static String text_clickTime() {
        return bundle.getString("clickTime");
    }

    /**
     * @return 重复：
     */
    public static String text_repeat() {
        return bundle.getString("repeat");
    }

    /**
     * @return 次，每次操作间隔：
     */
    public static String text_interval() {
        return bundle.getString("interval");
    }

    /**
     * @return 操作内容：识别目标图像：
     */
    public static String text_picTarget() {
        return bundle.getString("picTarget");
    }

    /**
     * @return 图像匹配后：
     */
    public static String text_afterMatch() {
        return bundle.getString("afterMatch");
    }

    /**
     * @return 正在识别终止操作图像：
     */
    public static String text_searchingStop() {
        return bundle.getString("searchingStop");
    }

    /**
     * @return 的步骤时终止操作
     */
    public static String text_taskStop() {
        return bundle.getString("taskStop");
    }

    /**
     * @return 匹配到终止操作图像：
     */
    public static String text_findStopImg() {
        return bundle.getString("findStopImg");
    }

    /**
     * @return 匹配度为：
     */
    public static String text_matchThreshold() {
        return bundle.getString("matchThreshold");
    }

    /**
     * @return 正在识别目标图像：
     */
    public static String text_searchingClick() {
        return bundle.getString("searchingClick");
    }

    /**
     * @return 的步骤时发生异常
     */
    public static String text_taskErr() {
        return bundle.getString("taskErr");
    }

    /**
     * @return 已重试最大重试次数：
     */
    public static String text_maxRetry() {
        return bundle.getString("maxRetry");
    }

    /**
     * @return 未找到匹配图像：
     */
    public static String text_notFound() {
        return bundle.getString("notFound");
    }

    /**
     * @return 未知GC类型:
     */
    public static String text_unknowGC() {
        return bundle.getString("unknowGC");
    }

    /**
     * @return 最接近匹配度为：
     */
    public static String text_closestMatchThreshold() {
        return bundle.getString("closestMatchThreshold");
    }

    /**
     * @return 容量不能小于 0
     */
    public static String text_minSize() {
        return bundle.getString("minSize");
    }

    /**
     * @return 双击单元格可进行编辑
     */
    public static String text_editingCellTip() {
        return bundle.getString("editingCellTip");
    }

    /**
     * @return 点击
     */
    public static String text_click() {
        return bundle.getString("click");
    }

    /**
     * @return 无法获取当前焦点应用 PID:
     */
    public static String text_getPidErr() {
        return bundle.getString("getPidErr");
    }

    /**
     * @return 获取 Mac 焦点窗口信息失败
     */
    public static String text_getMacFocusErr() {
        return bundle.getString("getMacFocusErr");
    }

    /**
     * @return 无法获取焦点应用路径
     */
    public static String text_geFocusPathErr() {
        return bundle.getString("geFocusPathErr");
    }

    /**
     * @return 无法获取窗口信息，窗口可能已经关闭
     */
    public static String text_noWindowInfo() {
        return bundle.getString("noWindowInfo");
    }

    /**
     * @return 不符合输入范围
     */
    public static String text_errRange() {
        return bundle.getString("errRange");
    }

    /**
     * @return 更新成功，受影响的操作共有：
     */
    public static String text_updateNum() {
        return bundle.getString("updateNum");
    }

    /**
     * @return 操作列表为空
     */
    public static String text_noUpdateNum() {
        return bundle.getString("noUpdateNum");
    }

    /**
     * @return 窗口信息已更新
     */
    public static String text_updateSuccess() {
        return bundle.getString("updateSuccess");
    }

    /**
     * @return 无法展示窗口位置，窗口可能已隐藏
     */
    public static String text_windowHidden() {
        return bundle.getString("windowHidden");
    }

    /**
     * @return 脚本文件
     */
    public static String text_script() {
        return bundle.getString("script");
    }

    /**
     * @return 测试中
     */
    public static String text_testing() {
        return bundle.getString("testing");
    }

    /**
     * @return 网址格式不正确
     */
    public static String text_urlErr() {
        return bundle.getString("urlErr");
    }

    /**
     * @return 发送邮件
     */
    public static String text_mailTo() {
        return bundle.getString("mailTo");
    }

    /**
     * @return 当前单元格不可编辑
     */
    public static String text_cantEdit() {
        return bundle.getString("cantEdit");
    }

    /**
     * @return 步骤详情
     */
    public static String clickDetail_title() {
        return bundle.getString("clickDetail.title");
    }

    /**
     * @return 保存识别区域
     */
    public static String findImgSet_saveRegion() {
        return bundle.getString("findImgSet.saveRegion");
    }

    /**
     * @return 自动操作目标窗口
     */
    public static String findImgSet_tagetWindow() {
        return bundle.getString("findImgSet.tagetWindow");
    }

    /**
     * @return 更新窗口数据
     */
    public static String findImgSet_updateWindow() {
        return bundle.getString("findImgSet.updateWindow");
    }

    /**
     * @return 更新到操作列表
     */
    public static String findImgSet_updateList() {
        return bundle.getString("findImgSet.updateList");
    }

    /**
     * @return 展示窗口位置
     */
    public static String findImgSet_showWindow() {
        return bundle.getString("findImgSet.showWindow");
    }

    /**
     * @return 删除窗口信息
     */
    public static String findImgSet_deleteWindow() {
        return bundle.getString("findImgSet.deleteWindow");
    }

    /**
     * @return 进程名称：
     */
    public static String findImgSet_PName() {
        return bundle.getString("findImgSet.PName");
    }

    /**
     * @return 窗口标题：
     */
    public static String findImgSet_windowTitle() {
        return bundle.getString("findImgSet.windowTitle");
    }

    /**
     * @return 窗口位置：
     */
    public static String findImgSet_windowLocation() {
        return bundle.getString("findImgSet.windowLocation");
    }

    /**
     * @return 窗口大小：
     */
    public static String findImgSet_windowSize() {
        return bundle.getString("findImgSet.windowSize");
    }

    /**
     * @return 进程 ID：
     */
    public static String findImgSet_PID() {
        return bundle.getString("findImgSet.PID");
    }

    /**
     * @return 进程路径：
     */
    public static String findImgSet_windowPath() {
        return bundle.getString("findImgSet.windowPath");
    }

    /**
     * @return 正在记录窗口信息
     */
    public static String findImgSet_finding() {
        return bundle.getString("findImgSet.finding");
    }

    /**
     * @return 已记录窗口信息
     */
    public static String findImgSet_getInfo() {
        return bundle.getString("findImgSet.getInfo");
    }

    /**
     * @return 未找到窗口信息请重试
     */
    public static String findImgSet_notFind() {
        return bundle.getString("findImgSet.notFind");
    }

    /**
     * @return 松开鼠标即可记录窗口信息
     */
    public static String findImgSet_released() {
        return bundle.getString("findImgSet.released");
    }

    /**
     * @return 正在记录窗口信息
     */
    public static String findImgSet_recording() {
        return bundle.getString("findImgSet.recording");
    }

    /**
     * @return 秒后开始记录窗口信息
     */
    public static String findImgSet_wait() {
        return bundle.getString("findImgSet.wait");
    }

    /**
     * @return 未设置目标窗口
     */
    public static String findImgSet_noWindow() {
        return bundle.getString("findImgSet.noWindow");
    }

    /**
     * @return 显示识别区域
     */
    public static String findImgSet_showRegion() {
        return bundle.getString("findImgSet.showRegion");
    }

    /**
     * @return 要点击的图像识别区域
     */
    public static String floatingName_click() {
        return bundle.getString("floatingName.click");
    }

    /**
     * @return 终止操作图像识别区域
     */
    public static String floatingName_stop() {
        return bundle.getString("floatingName.stop");
    }

    /**
     * @return 信息展示栏区域
     */
    public static String floatingName_massage() {
        return bundle.getString("floatingName.massage");
    }

    /**
     * @return 任务详情
     */
    public static String taskDetail_title() {
        return bundle.getString("taskDetail.title");
    }

    /**
     * @return 未选择任何星期
     */
    public static String taskDetail_noWeekDay() {
        return bundle.getString("taskDetail.noWeekDay");
    }

    /**
     * @return 缺少必要系统权限
     */
    public static String autoClick_noPermissions() {
        return bundle.getString("autoClick.noPermissions");
    }

    /**
     * @return 当前鼠标位置为：
     */
    public static String autoClick_nowMousePos() {
        return bundle.getString("autoClick.nowMousePos");
    }

    /**
     * @return 操作步骤设置有误
     */
    public static String autoClick_settingErr() {
        return bundle.getString("autoClick.settingErr");
    }

    /**
     * @return 名称：
     */
    public static String autoClick_name() {
        return bundle.getString("autoClick.name");
    }

    /**
     * @return 序号：
     */
    public static String autoClick_index() {
        return bundle.getString("autoClick.index");
    }

    /**
     * @return 鼠标移动轨迹
     */
    public static String autoClick_mouseTrajectory() {
        return bundle.getString("autoClick.mouseTrajectory");
    }

    /**
     * @return 录制已结束
     */
    public static String autoClick_recordEnd() {
        return bundle.getString("autoClick.recordEnd");
    }

    /**
     * @return 日志文件删除失败
     */
    public static String about_deleteFailed() {
        return bundle.getString("about.deleteFailed");
    }

    /**
     * @return 复制反馈邮件
     */
    public static String about_copyEmail() {
        return bundle.getString("about.copyEmail");
    }

    /**
     * @return 运行记录
     */
    public static String clickLog_title() {
        return bundle.getString("clickLog.title");
    }

    /**
     * @return 列表为空
     */
    public static String listText_dataListNull() {
        return bundle.getString("listText.null");
    }

    /**
     * @return 修改未保存
     */
    public static String confirm_unSaved() {
        return bundle.getString("confirm.unSaved");
    }

    /**
     * @return 当前有未保存的修改，是否保存？
     */
    public static String confirm_unSavedConfirm() {
        return bundle.getString("confirm.unSavedConfirm");
    }

    /**
     * @return 保存并关闭
     */
    public static String confirm_ok() {
        return bundle.getString("confirm.ok");
    }

    /**
     * @return 直接关闭
     */
    public static String confirm_cancelSave() {
        return bundle.getString("confirm.cancelSave");
    }

    /**
     * @return 需要重启应用
     */
    public static String confirm_reLaunch() {
        return bundle.getString("confirm.reLaunch");
    }

    /**
     * @return 该设置需要重启应用才能修改，是否立刻重启？
     */
    public static String confirm_reLaunchConfirm() {
        return bundle.getString("confirm.reLaunchConfirm");
    }

    /**
     * @return 立即重启
     */
    public static String confirm_reLaunchOk() {
        return bundle.getString("confirm.reLaunchOk");
    }

    /**
     * @return 取消
     */
    public static String confirm_cancel() {
        return bundle.getString("confirm.cancel");
    }

    /**
     * @return 图片已存在
     */
    public static String confirm_imageExist() {
        return bundle.getString("confirm.imageExist");
    }

    /**
     * @return 图片已存在，是否删除这张选中图片？
     */
    public static String confirm_imageExistConfirm() {
        return bundle.getString("confirm.imageExistConfirm");
    }

    /**
     * @return 删除
     */
    public static String confirm_delete() {
        return bundle.getString("confirm.delete");
    }

    /**
     * @return 插入设置步骤到所选行第一行上一行
     */
    public static String menuItem_insertUp() {
        return bundle.getString("menuItem.insertUp");
    }

    /**
     * @return 插入设置步骤到所选行下一行
     */
    public static String menuItem_insertDown() {
        return bundle.getString("menuItem.insertDown");
    }

    /**
     * @return 插入录制步骤到所选行第一行上一行
     */
    public static String menuItem_recordUp() {
        return bundle.getString("menuItem.recordUp");
    }

    /**
     * @return 插入录制步骤到所选行最后一行下一行
     */
    public static String menuItem_recordDown() {
        return bundle.getString("menuItem.recordDown");
    }

    /**
     * @return 插入设置步骤到列表顶部
     */
    public static String menuItem_insertTop() {
        return bundle.getString("menuItem.insertTop");
    }

    /**
     * @return 插入录制步骤到列表顶部
     */
    public static String menuItem_recordTop() {
        return bundle.getString("menuItem.recordTop");
    }

    /**
     * @return 复制所选数据到所选行第一行上方
     */
    public static String menuItem_upCopy() {
        return bundle.getString("menuItem.upCopy");
    }

    /**
     * @return 复制所选数据到所选行最后一行下方
     */
    public static String menuItem_downCopy() {
        return bundle.getString("menuItem.downCopy");
    }

    /**
     * @return 复制所选数据到列表最后一行
     */
    public static String menuItem_appendCopy() {
        return bundle.getString("menuItem.appendCopy");
    }

    /**
     * @return 复制所选数据到列表顶部
     */
    public static String menuItem_topCopy() {
        return bundle.getString("menuItem.topCopy");
    }

    /**
     * @return 打开所选文件
     */
    public static String menuItem_openSelected() {
        return bundle.getString("menuItem.openSelected");
    }

    /**
     * @return 打开所选文件所在文件夹
     */
    public static String menuItem_openDirectory() {
        return bundle.getString("menuItem.openDirectory");
    }

    /**
     * @return 复制文件路径
     */
    public static String menuItem_copyFilePath() {
        return bundle.getString("menuItem.copyFilePath");
    }

    /**
     * @return 所选行上移一行
     */
    public static String menuItem_moveUp() {
        return bundle.getString("menuItem.moveUp");
    }

    /**
     * @return 所选行下移一行
     */
    public static String menuItem_moveDown() {
        return bundle.getString("menuItem.moveDown");
    }

    /**
     * @return 所选行置顶
     */
    public static String menuItem_moveTop() {
        return bundle.getString("menuItem.moveTop");
    }

    /**
     * @return 所选行置底
     */
    public static String menuItem_moveBottom() {
        return bundle.getString("menuItem.moveBottom");
    }

    /**
     * @return 取消选中
     */
    public static String menu_cancelSelected() {
        return bundle.getString("menu.cancelSelected");
    }

    /**
     * @return 更改所选项第一行的图片
     */
    public static String menu_changeFirstImg() {
        return bundle.getString("menu.changeFirstImg");
    }

    /**
     * @return 更改重试类型
     */
    public static String menu_changeRetryType() {
        return bundle.getString("menu.changeRetryType");
    }

    /**
     * @return 更改点击按键
     */
    public static String menu_changeKey() {
        return bundle.getString("menu.changeKey");
    }

    /**
     * @return 移动所选数据
     */
    public static String menu_moveSelected() {
        return bundle.getString("menu.moveSelected");
    }

    /**
     * @return 查看文件
     */
    public static String menu_viewFile() {
        return bundle.getString("menu.viewFile");
    }

    /**
     * @return 复制所选数据
     */
    public static String menu_copy() {
        return bundle.getString("menu.copy");
    }

    /**
     * @return 查看所选项第一行详情
     */
    public static String menu_detailMenu() {
        return bundle.getString("menu.detailMenu");
    }

    /**
     * @return 删除所选数据
     */
    public static String menu_deleteMenu() {
        return bundle.getString("menu.deleteMenu");
    }

    /**
     * @return 执行选中的步骤
     */
    public static String menu_runSelectMenu() {
        return bundle.getString("menu.runSelectMenu");
    }

    /**
     * @return 插入数据
     */
    public static String menu_addDateMenu() {
        return bundle.getString("menu.addDateMenu");
    }

    /**
     * @return 按下
     */
    public static String log_press() {
        return bundle.getString("log.press");
    }

    /**
     * @return 松开
     */
    public static String log_release() {
        return bundle.getString("log.release");
    }

    /**
     * @return 移动鼠标
     */
    public static String log_move() {
        return bundle.getString("log.move");
    }

    /**
     * @return 长按
     */
    public static String log_hold() {
        return bundle.getString("log.hold");
    }

    /**
     * @return 拖拽
     */
    public static String log_drag() {
        return bundle.getString("log.drag");
    }

    /**
     * @return 等待
     */
    public static String log_wait() {
        return bundle.getString("log.wait");
    }

    /**
     * @return 识别目标图像
     */
    public static String log_clickImg() {
        return bundle.getString("log.clickImg");
    }

    /**
     * @return 识别终止操作图像
     */
    public static String log_stopImg() {
        return bundle.getString("log.stopImg");
    }

    /**
     * @return 图像识别
     */
    public static String log_findImage() {
        return bundle.getString("log.findImage");
    }

    /**
     * @return 重试直到图像出现
     */
    public static String retryType_continuously() {
        return bundle.getString("retryType.continuously");
    }

    /**
     * @return 按设置次数重试后点击设置位置
     */
    public static String retryType_click() {
        return bundle.getString("retryType.click");
    }

    /**
     * @return 按设置次数重试后终止操作
     */
    public static String retryType_stop() {
        return bundle.getString("retryType.stop");
    }

    /**
     * @return 按设置次数重试后跳过本次操作
     */
    public static String retryType_break() {
        return bundle.getString("retryType.break");
    }

    /**
     * @return 按设置次数重试后跳转指定步骤
     */
    public static String retryType_Step() {
        return bundle.getString("retryType.step");
    }

    /**
     * @return 文件
     */
    public static String extension_file() {
        return bundle.getString("extension.file");
    }

    /**
     * @return 文件夹
     */
    public static String extension_folder() {
        return bundle.getString("extension.folder");
    }

    /**
     * @return 文件或文件夹
     */
    public static String extension_fileOrFolder() {
        return bundle.getString("extension.fileOrFolder");
    }

    /**
     * @return 毫秒
     */
    public static String unit_ms() {
        return bundle.getString("unit.ms");
    }

    /**
     * @return 组数据
     */
    public static String unit_data() {
        return bundle.getString("unit.data");
    }

    /**
     * @return 个任务
     */
    public static String unit_task() {
        return bundle.getString("unit.task");
    }

    /**
     * @return 张图片
     */
    public static String unit_img() {
        return bundle.getString("unit.img");
    }

    /**
     * @return 条记录
     */
    public static String unit_log() {
        return bundle.getString("unit.log");
    }

    /**
     * @return 步操作
     */
    public static String unit_process() {
        return bundle.getString("unit.process");
    }

    /**
     * @return 次
     */
    public static String unit_times() {
        return bundle.getString("unit.times");
    }

    /**
     * @return 检查并下载更新
     */
    public static String update_checkUpdate_Abt() {
        return bundle.getString("update.checkUpdate_Abt");
    }

    /**
     * @return 下载更新失败
     */
    public static String update_downloadFailed() {
        return bundle.getString("update.downloadFailed");
    }

    /**
     * @return 发现新版本：
     */
    public static String update_findNewVersion() {
        return bundle.getString("update.findNewVersion");
    }

    /**
     * @return 找不到更新脚本
     */
    public static String update_scriptNotFind() {
        return bundle.getString("update.scriptNotFind");
    }

    /**
     * @return 检查更新失败
     */
    public static String update_checkFailed() {
        return bundle.getString("update.checkFailed");
    }

    /**
     * @return 无法创建目录：
     */
    public static String update_creatDirErr() {
        return bundle.getString("update.creatDirErr");
    }

    /**
     * @return 无法创建父目录：
     */
    public static String update_creatFatherDirErr() {
        return bundle.getString("update.creatFatherDirErr");
    }

    /**
     * @return 无法删除文件或目录：
     */
    public static String update_deleteErr() {
        return bundle.getString("update.deleteErr");
    }

    /**
     * @return 发布日期：
     */
    public static String update_releaseDate() {
        return bundle.getString("update.releaseDate");
    }

    /**
     * @return 现在更新
     */
    public static String update_updateButton() {
        return bundle.getString("update.updateButton");
    }

    /**
     * @return 稍后更新
     */
    public static String update_laterButton() {
        return bundle.getString("update.laterButton");
    }

    /**
     * @return 检查更新中...
     */
    public static String update_checking() {
        return bundle.getString("update.checking");
    }

    /**
     * @return 检查失败，响应体为空
     */
    public static String update_nullResponse() {
        return bundle.getString("update.nullResponse");
    }

    /**
     * @return 下载中...
     */
    public static String update_downloading() {
        return bundle.getString("update.downloading");
    }

    /**
     * @return 下下载更新失败，HTTP代码:
     */
    public static String update_downloadError() {
        return bundle.getString("update.downloadError");
    }

    /**
     * @return 下载完成，等待安装更新
     */
    public static String update_waitInstall() {
        return bundle.getString("update.waitInstall");
    }

    /**
     * @return 重启并安装更新
     */
    public static String update_installing() {
        return bundle.getString("update.installing");
    }

    /**
     * @return 无法设置脚本可执行权限
     */
    public static String update_scriptNoPermission() {
        return bundle.getString("update.scriptNoPermission");
    }

    /**
     * @return 脚本不可执行:
     */
    public static String update_scriptCantRun() {
        return bundle.getString("update.scriptCantRun");
    }

    /**
     * @return 更新并重启应用:
     */
    public static String update_password() {
        return bundle.getString("update.password");
    }

    /**
     * @return 创建临时文件失败
     */
    public static String update_tempFileErr() {
        return bundle.getString("update.tempFileErr");
    }

    /**
     * @return 脚本执行失败，退出码:
     */
    public static String update_scriptExit() {
        return bundle.getString("update.scriptExit");
    }

    /**
     * @return 脚本输出内容:
     */
    public static String update_scriptOut() {
        return bundle.getString("update.scriptOut");
    }

    /**
     * @return 下载更新
     */
    public static String update_downloadingUpdate() {
        return bundle.getString("update.downloadingUpdate");
    }

    /**
     * @return 最后检查时间：
     */
    public static String update_lastCheck() {
        return bundle.getString("update.lastCheck");
    }

    /**
     * @return 当前版本为最新版本
     */
    public static String update_nowIsLast() {
        return bundle.getString("update.nowIsLast");
    }

    /**
     * @return 导入pmc文件
     */
    public static String import_title() {
        return bundle.getString("import.title");
    }

    /**
     * @return 是否清空操作列表？
     */
    public static String import_header() {
        return bundle.getString("import.header");
    }

    /**
     * @return 在操作列表下方追加流程
     */
    public static String import_append() {
        return bundle.getString("import.append");
    }

    /**
     * @return 清空操作列表后导入流程
     */
    public static String import_clear() {
        return bundle.getString("import.clear");
    }

    /**
     * @return 取消导入
     */
    public static String import_cancel() {
        return bundle.getString("import.cancel");
    }

    /**
     * @return 关于
     */
    public static String macMenu_about() {
        return bundle.getString("macMenu.about");
    }

    /**
     * @return 设置...
     */
    public static String macMenu_settings() {
        return bundle.getString("macMenu.settings");
    }

    /**
     * @return 隐藏
     */
    public static String macMenu_hide() {
        return bundle.getString("macMenu.hide");
    }

    /**
     * @return 隐藏其他
     */
    public static String macMenu_hideOther() {
        return bundle.getString("macMenu.hideOther");
    }

    /**
     * @return 退出
     */
    public static String macMenu_quit() {
        return bundle.getString("macMenu.quit");
    }

    /**
     * @return <p>将会使用操作系统默认打开方式打开文件</p>
     * <p>无法确定文件是否正常打开，后续操作需要自行计算文件打开的时间</p>
     * 脚本文件如果默认打开方式为终端则会直接运行脚本
     */
    public static String pathTip_openFile() {
        return bundle.getString("pathTip.openFile");
    }

    /**
     * @return <p>将会使用操作系统默认浏览器打开网址</p>
     * <p>无法确定网址是否正常打开，后续操作需要自行计算网址打开的时间</p>
     * 需要输入完整网址，可不输入协议（http:// https://）前缀
     */
    public static String pathTip_openUrl() {
        return bundle.getString("pathTip.openUrl");
    }

    /**
     * @return <p>将会打开终端运行脚本，脚本执行结束后自动关闭终端窗口进行后续操作</p>
     * <p>执行脚本需要安装对应的脚本运行环境</p>
     * <p>运行 .py 文件需要 python 3.0 以上的环境</p>
     * <p>运行 .ps1 文件需要 powershell 环境</p>
     * <p>运行 .java 文件需要 java 11 以上的环境</p>
     * 运行 .jar 和 .class 文件需要 java 环境
     */
    public static String pathTip_runScript() {
        return bundle.getString("pathTip.runScript");
    }

    /**
     * @return 只查询文件
     */
    public static String search_onlyFile() {
        return bundle.getString("search.onlyFile");
    }

    /**
     * @return 只查询文件夹
     */
    public static String search_onlyDirectory() {
        return bundle.getString("search.onlyDirectory");
    }

    /**
     * @return 文件和文件夹都查询
     */
    public static String search_fileDirectory() {
        return bundle.getString("search.fileDirectory");
    }

    /**
     * 文件与文件夹查询条件选项
     */
    public static final List<String> searchTypeList = new ArrayList<>();

    /**
     * 更新文件与文件夹查询条件选项
     */
    public static void updateSearchTypeList() {
        List<String> newList = Arrays.asList(
                search_onlyFile(),
                search_fileDirectory(),
                search_onlyDirectory());
        searchTypeList.clear();
        searchTypeList.addAll(newList);
    }

    /**
     * @return 不查询隐藏文件
     */
    public static String hide_noHideFile() {
        return bundle.getString("hide.noHideFile");
    }

    /**
     * @return 只查询隐藏文件
     */
    public static String hide_onlyHideFile() {
        return bundle.getString("hide.onlyHideFile");
    }

    /**
     * @return 是否隐藏都查询
     */
    public static String hide_allFile() {
        return bundle.getString("hide.allFile");
    }

    /**
     * 隐藏文件查询设置选项
     */
    public static final List<String> hideSearchTypeList = new ArrayList<>();

    /**
     * 更新隐藏文件查询条件选项
     */
    public static void updateHideSearchTypeList() {
        List<String> newList = Arrays.asList(
                hide_noHideFile(),
                hide_allFile(),
                hide_onlyHideFile());
        hideSearchTypeList.clear();
        hideSearchTypeList.addAll(newList);
    }

    /**
     * @return 文件名包含
     */
    public static String name_contain() {
        return bundle.getString("name.contain");
    }

    /**
     * @return 文件名为
     */
    public static String name_is() {
        return bundle.getString("name.is");
    }

    /**
     * @return 文件名起始于
     */
    public static String name_start() {
        return bundle.getString("name.start");
    }

    /**
     * @return 文件名结束于
     */
    public static String name_end() {
        return bundle.getString("name.end");
    }

    /**
     * 文件名查询设置选项
     */
    public static final List<String> nameSearchTypeList = new ArrayList<>();

    /**
     * 更新文件名查询条件选项
     */
    public static void updateNameSearchTypeList() {
        List<String> newList = Arrays.asList(
                name_contain(),
                name_is(),
                name_start(),
                name_end());
        nameSearchTypeList.clear();
        nameSearchTypeList.addAll(newList);
    }

    /**
     * @return 识别全屏
     */
    public static String findImgType_all() {
        return bundle.getString("findImgType.all");
    }

    /**
     * @return 识别指定窗口
     */
    public static String findImgType_window() {
        return bundle.getString("findImgType.window");
    }

    /**
     * @return 识别指定区域
     */
    public static String findImgType_region() {
        return bundle.getString("findImgType.region");
    }

    /**
     * 图像识别区域类型选项
     */
    public static final List<String> findImgTypeList = new ArrayList<>();

    /**
     * 更新图像识别区域类型选项
     */
    public static void updateFindImgTypeList() {
        List<String> newList = Arrays.asList(
                findImgType_all(),
                findImgType_region(),
                findImgType_window());
        findImgTypeList.clear();
        findImgTypeList.addAll(newList);
    }

    /**
     * 图像识别区域类型选项映射
     */
    public static final BidiMap<Integer, String> findImgTypeMap = new DualHashBidiMap<>();

    /**
     * 更新图像识别区域类型选项映射
     */
    public static void updateFindImgTypeMap() {
        findImgTypeMap.clear();
        findImgTypeMap.put(FindImgTypeEnum.ALL.ordinal(), findImgType_all());
        findImgTypeMap.put(FindImgTypeEnum.REGION.ordinal(), findImgType_region());
        findImgTypeMap.put(FindImgTypeEnum.WINDOW.ordinal(), findImgType_window());
    }

    /**
     * @return 按文件名称排序
     */
    public static String sort_Name() {
        return bundle.getString("sort.Name");
    }

    /**
     * @return 按文件创建时间排序
     */
    public static String sort_creatTime() {
        return bundle.getString("sort.creatTime");
    }

    /**
     * @return 按文件修改时间排序
     */
    public static String sort_updateTime() {
        return bundle.getString("sort.updateTime");
    }

    /**
     * @return 按文件大小排序
     */
    public static String sort_size() {
        return bundle.getString("sort.size");
    }

    /**
     * @return 按文件类型排序
     */
    public static String sort_type() {
        return bundle.getString("sort.type");
    }

    /**
     * 重试逻辑下拉框选项
     */
    public static final List<String> retryTypeList = new ArrayList<>();

    /**
     * 更新重试逻辑下拉框选项
     */
    public static void updateRetryTypeList() {
        List<String> newList = Arrays.asList(
                retryType_continuously(),
                retryType_click(),
                retryType_stop(),
                retryType_break(),
                retryType_Step());
        retryTypeList.clear();
        retryTypeList.addAll(newList);
    }

    /**
     * 重试逻辑下拉框选项与枚举映射
     */
    public static final BidiMap<Integer, String> retryTypeMap = new DualHashBidiMap<>();

    /**
     * 更新重试逻辑下拉框选项与枚举映射
     */
    public static void updateRetryTypeMap() {
        retryTypeMap.clear();
        retryTypeMap.put(RetryTypeEnum.CONTINUOUSLY.ordinal(), retryType_continuously());
        retryTypeMap.put(RetryTypeEnum.CLICK.ordinal(), retryType_click());
        retryTypeMap.put(RetryTypeEnum.STOP.ordinal(), retryType_stop());
        retryTypeMap.put(RetryTypeEnum.BREAK.ordinal(), retryType_break());
        retryTypeMap.put(RetryTypeEnum.STEP.ordinal(), retryType_Step());
    }

    /**
     * @return 按操作类型设置处理匹配的图像
     */
    public static String clickMatched_click() {
        return bundle.getString("clickMatched.click");
    }

    /**
     * @return 直接执行下一个操作步骤
     */
    public static String clickMatched_break() {
        return bundle.getString("clickMatched.break");
    }

    /**
     * @return 跳转到指定操作步骤
     */
    public static String clickMatched_step() {
        return bundle.getString("clickMatched.step");
    }

    /**
     * @return 点击匹配图像后跳转指定步骤
     */
    public static String clickMatched_clickStep() {
        return bundle.getString("clickMatched.clickStep");
    }

    /**
     * @return 匹配图像存在则重复点击
     */
    public static String clickMatched_clickWhile() {
        return bundle.getString("clickMatched.clickWhile");
    }

    /**
     * 要识别的图像识别匹配后逻辑下拉框选项
     */
    public static final List<String> clickMatchedList = new ArrayList<>();

    /**
     * 更新要识别的图像识别匹配后逻辑下拉框选项
     */
    public static void updateClickMatchedList() {
        List<String> newList = Arrays.asList(
                clickMatched_click(),
                clickMatched_break(),
                clickMatched_step(),
                clickMatched_clickStep(),
                clickMatched_clickWhile());
        clickMatchedList.clear();
        clickMatchedList.addAll(newList);
    }

    /**
     * 要识别的图像识别匹配后逻辑下拉框选项与枚举映射
     */
    public static final BidiMap<Integer, String> matchedTypeMap = new DualHashBidiMap<>();

    /**
     * 更新要识别的图像识别匹配后逻辑下拉框选项与枚举映射
     */
    public static void updateClickMatchedMap() {
        matchedTypeMap.clear();
        matchedTypeMap.put(MatchedTypeEnum.CLICK.ordinal(), clickMatched_click());
        matchedTypeMap.put(MatchedTypeEnum.BREAK.ordinal(), clickMatched_break());
        matchedTypeMap.put(MatchedTypeEnum.STEP.ordinal(), clickMatched_step());
        matchedTypeMap.put(MatchedTypeEnum.CLICK_STEP.ordinal(), clickMatched_clickStep());
        matchedTypeMap.put(MatchedTypeEnum.CLICK_WHILE.ordinal(), clickMatched_clickWhile());
    }

    /**
     * @return 鼠标左键
     */
    public static String mouseButton_primary() {
        return bundle.getString("mouseButton.primary");
    }

    /**
     * @return 鼠标右键
     */
    public static String mouseButton_secondary() {
        return bundle.getString("mouseButton.secondary");
    }

    /**
     * @return 鼠标中键
     */
    public static String mouseButton_middle() {
        return bundle.getString("mouseButton.middle");
    }

    /**
     * @return 鼠标前侧键
     */
    public static String mouseButton_forward() {
        return bundle.getString("mouseButton.forward");
    }

    /**
     * @return 鼠标后侧键
     */
    public static String mouseButton_back() {
        return bundle.getString("mouseButton.back");
    }

    /**
     * 点击按键下拉框选项
     */
    public static final List<String> mouseButtonList = new ArrayList<>();

    /**
     * 更新点击按键下拉框选项
     */
    public static void updateMouseButtonList() {
        List<String> newList = Arrays.asList(
                mouseButton_primary(),
                mouseButton_secondary(),
                mouseButton_middle(),
                mouseButton_forward(),
                mouseButton_back());
        mouseButtonList.clear();
        mouseButtonList.addAll(newList);
    }

    /**
     * 自动操作的操作类型选项对应的鼠标行为（操作用）
     */
    public static final BidiMap<String, MouseButton> runClickTypeMap = new DualHashBidiMap<>();

    /**
     * 更新自动操作的操作类型选项对应的鼠标行为（操作用）
     */
    public static void updateRunClickTypeMap() {
        runClickTypeMap.clear();
        runClickTypeMap.put(mouseButton_primary(), MouseButton.PRIMARY);
        runClickTypeMap.put(mouseButton_secondary(), MouseButton.SECONDARY);
        runClickTypeMap.put(mouseButton_middle(), MouseButton.MIDDLE);
        runClickTypeMap.put(mouseButton_forward(), MouseButton.FORWARD);
        runClickTypeMap.put(mouseButton_back(), MouseButton.BACK);
    }

    /**
     * 自动操作的操作类型选项对应的鼠标行为（录制用）
     */
    public static final BidiMap<Integer, String> recordClickTypeMap = new DualHashBidiMap<>();

    /**
     * 更新自动操作的操作类型选项对应的鼠标行为（录制用）
     */
    public static void updateRecordClickTypeMap() {
        recordClickTypeMap.clear();
        recordClickTypeMap.put(NativeMouseEvent.BUTTON1, mouseButton_primary());
        recordClickTypeMap.put(NativeMouseEvent.BUTTON2, mouseButton_secondary());
        recordClickTypeMap.put(NativeMouseEvent.BUTTON3, mouseButton_middle());
        recordClickTypeMap.put(NativeMouseEvent.BUTTON4, mouseButton_back());
        recordClickTypeMap.put(NativeMouseEvent.BUTTON5, mouseButton_forward());
    }

    /**
     * 录制与点击按键类映射
     */
    public static final BidiMap<Integer, MouseButton> NativeMouseToMouseButton = new DualHashBidiMap<>();

    /**
     * 更新录制与点击按键类映射
     */
    public static void updateMouseButton() {
        NativeMouseToMouseButton.clear();
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON1, MouseButton.PRIMARY);
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON2, MouseButton.SECONDARY);
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON3, MouseButton.MIDDLE);
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON4, MouseButton.BACK);
        NativeMouseToMouseButton.put(NativeMouseEvent.BUTTON5, MouseButton.FORWARD);
        NativeMouseToMouseButton.put(NativeMouseEvent.NOBUTTON, MouseButton.NONE);
    }

    /**
     * @return 打开文件
     */
    public static String clickType_openFile() {
        return bundle.getString("clickType.openFile");
    }

    /**
     * @return 打开网址
     */
    public static String clickType_openUrl() {
        return bundle.getString("clickType.openUrl");
    }

    /**
     * @return 运行脚本
     */
    public static String clickType_runScript() {
        return bundle.getString("clickType.runScript");
    }

    /**
     * @return 带轨迹的移动
     */
    public static String clickType_moveTrajectory() {
        return bundle.getString("clickType.moveTrajectory");
    }

    /**
     * @return 仅移动
     */
    public static String clickType_move() {
        return bundle.getString("clickType.move");
    }

    /**
     * @return 点击后松开
     */
    public static String clickType_click() {
        return bundle.getString("clickType.click");
    }

    /**
     * @return 拖拽
     */
    public static String clickType_drag() {
        return bundle.getString("clickType.drag");
    }

    /**
     * @return 移动到设置坐标
     */
    public static String clickType_moveTo() {
        return bundle.getString("clickType.moveTo");
    }

    /**
     * @return 滚轮上滑
     */
    public static String clickType_wheelUp() {
        return bundle.getString("clickType.wheelUp");
    }

    /**
     * @return 滚轮下滑
     */
    public static String clickType_wheelDown() {
        return bundle.getString("clickType.wheelDown");
    }

    /**
     * 打开链接相关选项
     */
    public static final List<String> linkList = new ArrayList<>();

    /**
     * 更新打开链接相关选项
     */
    public static void updateLinkList() {
        List<String> newList = Arrays.asList(
                clickType_openFile(),
                clickType_runScript(),
                clickType_openUrl());
        linkList.clear();
        linkList.addAll(newList);
    }

    /**
     * 自动操作的操作类型选项
     */
    public static final List<String> clickTypeList = new ArrayList<>();

    /**
     * 更新自动操作的操作类型选项
     */
    public static void updateClickTypeList() {
        // 更新打开链接相关选项
        updateLinkList();
        List<String> newList = Arrays.asList(
                clickType_moveTrajectory(),
                clickType_move(),
                clickType_click(),
                clickType_drag(),
                clickType_moveTo(),
                clickType_wheelUp(),
                clickType_wheelDown());
        clickTypeList.clear();
        clickTypeList.addAll(linkList);
        clickTypeList.addAll(newList);
    }

    /**
     * 自动操作的操作类型选项与枚举映射
     */
    public static final BidiMap<Integer, String> clickTypeMap = new DualHashBidiMap<>();

    /**
     * 更新自动操作的操作类型选项与枚举映射
     */
    public static void updateClickTypeMap() {
        clickTypeMap.clear();
        clickTypeMap.put(ClickTypeEnum.OPEN_FILE.ordinal(), clickType_openFile());
        clickTypeMap.put(ClickTypeEnum.RUN_SCRIPT.ordinal(), clickType_runScript());
        clickTypeMap.put(ClickTypeEnum.OPEN_URL.ordinal(), clickType_openUrl());
        clickTypeMap.put(ClickTypeEnum.MOVE_TRAJECTORY.ordinal(), clickType_moveTrajectory());
        clickTypeMap.put(ClickTypeEnum.MOVE.ordinal(), clickType_move());
        clickTypeMap.put(ClickTypeEnum.CLICK.ordinal(), clickType_click());
        clickTypeMap.put(ClickTypeEnum.DRAG.ordinal(), clickType_drag());
        clickTypeMap.put(ClickTypeEnum.MOVETO.ordinal(), clickType_moveTo());
        clickTypeMap.put(ClickTypeEnum.WHEEL_UP.ordinal(), clickType_wheelUp());
        clickTypeMap.put(ClickTypeEnum.WHEEL_DOWN.ordinal(), clickType_wheelDown());
    }

    /**
     * @return 每天
     */
    public static String repeatType_daily() {
        return bundle.getString("repeatType.daily");
    }

    /**
     * @return 每周
     */
    public static String repeatType_weekly() {
        return bundle.getString("repeatType.weekly");
    }

    /**
     * @return 仅一次
     */
    public static String repeatType_once() {
        return bundle.getString("repeatType.once");
    }

    /**
     * 重复类型下拉框选项
     */
    public static final List<String> repeatTypeList = new ArrayList<>();

    /**
     * 更新重复类型下拉框选项
     */
    public static void updateRepeatTypeList() {
        List<String> newList = Arrays.asList(
                repeatType_daily(),
                repeatType_weekly(),
                repeatType_once());
        repeatTypeList.clear();
        repeatTypeList.addAll(newList);
    }

    /**
     * 定时任务重复类型映射
     */
    public static final BidiMap<String, String> repeatTypeMap = new DualHashBidiMap<>();

    /**
     * 更新定时任务重复类型映射
     */
    public static void updateRepeatTypeMap() {
        repeatTypeMap.clear();
        repeatTypeMap.put(RepeatTypeEnum.DAILY.getRepeatType(), repeatType_daily());
        repeatTypeMap.put(RepeatTypeEnum.WEEKLY.getRepeatType(), repeatType_weekly());
        repeatTypeMap.put(RepeatTypeEnum.ONCE.getRepeatType(), repeatType_once());
    }

    /**
     * @return 星期一
     */
    public static String monday() {
        return bundle.getString("monday");
    }

    /**
     * @return 星期二
     */
    public static String tuesday() {
        return bundle.getString("tuesday");
    }

    /**
     * @return 星期三
     */
    public static String wednesday() {
        return bundle.getString("wednesday");
    }

    /**
     * @return 星期四
     */
    public static String thursday() {
        return bundle.getString("thursday");
    }

    /**
     * @return 星期五
     */
    public static String friday() {
        return bundle.getString("friday");
    }

    /**
     * @return 星期六
     */
    public static String saturday() {
        return bundle.getString("saturday");
    }

    /**
     * @return 星期日
     */
    public static String sunday() {
        return bundle.getString("sunday");
    }

    /**
     * 定时任务星期名称中英文映射
     */
    public static final BidiMap<String, String> dayOfWeekName = new DualHashBidiMap<>();

    /**
     * 更新定时任务星期名称中英文映射
     */
    public static void updateDayOfWeekName() {
        dayOfWeekName.clear();
        dayOfWeekName.put("MON", monday());
        dayOfWeekName.put("TUE", tuesday());
        dayOfWeekName.put("WED", wednesday());
        dayOfWeekName.put("THU", thursday());
        dayOfWeekName.put("FRI", friday());
        dayOfWeekName.put("SAT", saturday());
        dayOfWeekName.put("SUN", sunday());
    }

    /**
     * 定时任务星期名称与数字映射
     */
    public static final BidiMap<Integer, String> dayOfWeekMap = new DualHashBidiMap<>();

    /**
     * 更新定时任务星期名称与数字映射
     */
    public static void updateDayOfWeekMap() {
        dayOfWeekMap.clear();
        dayOfWeekMap.put(1, monday());
        dayOfWeekMap.put(2, tuesday());
        dayOfWeekMap.put(3, wednesday());
        dayOfWeekMap.put(4, thursday());
        dayOfWeekMap.put(5, friday());
        dayOfWeekMap.put(6, saturday());
        dayOfWeekMap.put(7, sunday());
    }

    /**
     * 定时任务星期名称与数字映射的逆向映射
     *
     * @return 定时任务星期名称与数字映射的逆向映射
     */
    public static Map<String, Integer> dayOfWeekReverseMap() {
        return dayOfWeekMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    /**
     * javaFX 原生外观
     */
    public static String theme_javafx() {
        return bundle.getString("theme.javafx");
    }

    /**
     * 白天模式
     */
    public static String theme_light() {
        return bundle.getString("theme.light");
    }

    /**
     * 夜晚模式
     */
    public static String theme_dark() {
        return bundle.getString("theme.dark");
    }

    /**
     * 跟随系统
     */
    public static String theme_auto() {
        return bundle.getString("theme.auto");
    }

    /**
     * 外观选项
     */
    public static final List<String> themeList = new ArrayList<>();

    /**
     * 更新外观选项
     */
    public static void updateThemeList() {
        themeList.clear();
        themeList.add(theme_javafx());
        themeList.add(theme_light());
        themeList.add(theme_dark());
        themeList.add(theme_auto());
    }

    /**
     * 外观选项映射
     */
    public static final BidiMap<Integer, String> themeMap = new DualHashBidiMap<>();

    /**
     * 更新外观选项映射
     */
    public static void updateThemeMap() {
        themeMap.clear();
        themeMap.put(ThemeEnum.JavaFx.ordinal(), theme_javafx());
        themeMap.put(ThemeEnum.Light.ordinal(), theme_light());
        themeMap.put(ThemeEnum.Dark.ordinal(), theme_dark());
        themeMap.put(ThemeEnum.Auto.ordinal(), theme_auto());
    }

    /**
     * 切换语言下拉框选项
     */
    public static final BidiMap<Locale, String> languageMap = new DualHashBidiMap<>();

    static {
        languageMap.put(Locale.SIMPLIFIED_CHINESE, LanguageEnum.zh_CN.getString());
        languageMap.put(Locale.TRADITIONAL_CHINESE, LanguageEnum.zh_TW.getString());
        languageMap.put(Locale.ENGLISH, LanguageEnum.en.getString());
    }

    /**
     * 更新映射文本
     */
    public static void updateAllDynamicTexts() {
        // 更新重试逻辑下拉框选项与枚举映射
        updateRetryTypeMap();
        // 更新要识别的图像识别匹配后逻辑下拉框选项与枚举映射
        updateClickMatchedMap();
        // 更新自动操作的操作类型选项与枚举映射
        updateClickTypeMap();
        // 更新要识别的图像识别匹配后逻辑下拉框选项
        updateClickMatchedList();
        // 更新重试逻辑下拉框选项
        updateRetryTypeList();
        // 更新点击按键下拉框选项
        updateMouseButtonList();
        // 更新自动操作的操作类型选项
        updateClickTypeList();
        // 更新重复类型下拉框选项
        updateRepeatTypeList();
        // 更新自动操作的操作类型选项对应的鼠标行为（操作用）
        updateRunClickTypeMap();
        // 更新自动操作的操作类型选项对应的鼠标行为（录制用）
        updateRecordClickTypeMap();
        // 更新录制与点击按键类映射
        updateMouseButton();
        // 更新定时任务重复类型映射
        updateRepeatTypeMap();
        // 更新定时任务星期名称与数字映射
        updateDayOfWeekName();
        // 更新定时任务星期名称与数字映射
        updateDayOfWeekMap();
        // 更新文件名查询条件选项
        updateNameSearchTypeList();
        // 更新隐藏文件查询条件选项
        updateHideSearchTypeList();
        // 更新文件与文件夹查询条件选项
        updateSearchTypeList();
        // 更新图像识别区域类型选项
        updateFindImgTypeList();
        // 更新图像识别区域类型选项映射
        updateFindImgTypeMap();
        // 更新外观选项
        updateThemeList();
        // 更新外观选项映射
        updateThemeMap();
    }

}
