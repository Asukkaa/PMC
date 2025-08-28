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

    public static String tip_logsNum() {
        return bundle.getString("tip.logsNum");
    }

    public static String tip_learButton() {
        return bundle.getString("tip.learButton");
    }

    public static String tip_openDirectory() {
        return bundle.getString("tip.openDirectory");
    }

    public static String tip_notOverwrite() {
        return bundle.getString("tip.notOverwrite");
    }

    public static String tip_openLink() {
        return bundle.getString("tip.openLink");
    }

    public static String tip_openGitLink() {
        return bundle.getString("tip.openGitLink");
    }

    public static String tip_wait() {
        return bundle.getString("tip.wait");
    }

    public static String tip_mouseStartX() {
        return bundle.getString("tip.mouseStartX");
    }

    public static String tip_mouseStartY() {
        return bundle.getString("tip.mouseStartY");
    }

    public static String tip_runClick() {
        return bundle.getString("tip.runClick");
    }

    public static String tip_addPosition() {
        return bundle.getString("tip.addPosition");
    }

    public static String tip_loopTime() {
        return bundle.getString("tip.loopTime");
    }

    public static String tip_clickNumBer() {
        return bundle.getString("tip.clickNumBer");
    }

    public static String tip_clickKey() {
        return bundle.getString("tip.clickKey");
    }

    public static String tip_clickType() {
        return bundle.getString("tip.clickType");
    }

    public static String tip_clickTime() {
        return bundle.getString("tip.clickTime");
    }

    public static String tip_clickInterval() {
        return bundle.getString("tip.clickInterval");
    }

    public static String tip_clickName() {
        return bundle.getString("tip.clickName");
    }

    public static String tip_outAutoClickPath() {
        return bundle.getString("tip.outAutoClickPath");
    }

    public static String tip_loadAutoClick() {
        return bundle.getString("tip.loadAutoClick");
    }

    public static String tip_exportAutoClick() {
        return bundle.getString("tip.exportAutoClick");
    }

    public static String tip_showWindowRun() {
        return bundle.getString("tip.showWindowRun");
    }

    public static String tip_hideWindowRecord() {
        return bundle.getString("tip.hideWindowRecord");
    }

    public static String tip_showWindowRecord() {
        return bundle.getString("tip.showWindowRecord");
    }

    public static String tip_mouseFloatingRun() {
        return bundle.getString("tip.mouseFloatingRun");
    }

    public static String tip_mouseFloatingRecord() {
        return bundle.getString("tip.mouseFloatingRecord");
    }

    public static String tip_mouseFloating() {
        return bundle.getString("tip.mouseFloating");
    }

    public static String tip_margin() {
        return bundle.getString("tip.margin");
    }

    public static String tip_preparationRecordTime() {
        return bundle.getString("tip.preparationRecordTime");
    }

    public static String tip_preparationRunTime() {
        return bundle.getString("tip.preparationRunTime");
    }

    public static String tip_floatingRun() {
        return bundle.getString("tip.floatingRun");
    }

    public static String tip_floatingRecord() {
        return bundle.getString("tip.floatingRecord");
    }

    public static String tip_setFloatingCoordinate() {
        return bundle.getString("tip.setFloatingCoordinate");
    }

    public static String tip_closeFloating() {
        return bundle.getString("tip.closeFloating");
    }

    public static String tip_saveFloating() {
        return bundle.getString("tip.saveFloating");
    }

    public static String tip_offsetX() {
        return bundle.getString("tip.offsetX");
    }

    public static String tip_offsetY() {
        return bundle.getString("tip.offsetY");
    }

    public static String tip_colorPicker() {
        return bundle.getString("tip.colorPicker");
    }

    public static String tip_thanks() {
        return bundle.getString("tip.thanks");
    }

    public static String tip_appreciate() {
        return bundle.getString("tip.appreciate");
    }

    public static String tip_lastAutoClickSetting() {
        return bundle.getString("tip.lastAutoClickSetting");
    }

    public static String tip_clickOpacity() {
        return bundle.getString("tip.clickOpacity");
    }

    public static String tip_stopOpacity() {
        return bundle.getString("tip.stopOpacity");
    }

    public static String tip_stopImgBtn() {
        return bundle.getString("tip.stopImgBtn");
    }

    public static String tip_defaultStopImgBtn() {
        return bundle.getString("tip.defaultStopImgBtn");
    }

    public static String tip_clickImgBtn() {
        return bundle.getString("tip.clickImgBtn");
    }

    public static String tip_removeClickImgBtn() {
        return bundle.getString("tip.removeClickImgBtn");
    }

    public static String tip_removeStopImgBtn() {
        return bundle.getString("tip.removeStopImgBtn");
    }

    public static String tip_updateClickNameBtn() {
        return bundle.getString("tip.updateClickNameBtn");
    }

    public static String tip_overtime() {
        return bundle.getString("tip.overtime");
    }

    public static String tip_retrySecond() {
        return bundle.getString("tip.retrySecond");
    }

    public static String tip_reLaunch() {
        return bundle.getString("tip.reLaunch");
    }

    public static String tip_remindSave() {
        return bundle.getString("tip.remindSave");
    }

    public static String tip_remindTaskSave() {
        return bundle.getString("tip.remindTaskSave");
    }

    public static String tip_clickIndex() {
        return bundle.getString("tip.clickIndex");
    }

    public static String tip_tableViewSize() {
        return bundle.getString("tip.tableViewSize");
    }

    public static String tip_step() {
        return bundle.getString("tip.step");
    }

    public static String tip_matchedType() {
        return bundle.getString("tip.matchedType");
    }

    public static String tip_retryType() {
        return bundle.getString("tip.retryType");
    }

    public static String tip_recordMove() {
        return bundle.getString("tip.recordMove");
    }

    public static String tip_recordDrag() {
        return bundle.getString("tip.recordDrag");
    }

    public static String tip_randomClick() {
        return bundle.getString("tip.randomClick");
    }

    public static String tip_randomTrajectory() {
        return bundle.getString("tip.randomTrajectory");
    }

    public static String tip_randomClickTime() {
        return bundle.getString("tip.randomClickTime");
    }

    public static String tip_randomClickInterval() {
        return bundle.getString("tip.randomClickInterval");
    }

    public static String tip_randomWaitTime() {
        return bundle.getString("tip.randomWaitTime");
    }

    public static String tip_maxLogNum() {
        return bundle.getString("tip.maxLogNum");
    }

    public static String tip_nextRunMemory() {
        return bundle.getString("tip.nextRunMemory");
    }

    public static String tip_hour() {
        return bundle.getString("tip.hour");
    }

    public static String tip_minute() {
        return bundle.getString("tip.minute");
    }

    public static String tip_taskName() {
        return bundle.getString("tip.taskName");
    }

    public static String tip_datePicker() {
        return bundle.getString("tip.datePicker");
    }

    public static String tip_repeatType() {
        return bundle.getString("tip.repeatType");
    }

    public static String tip_addTimedTask() {
        return bundle.getString("tip.addTimedTask");
    }

    public static String tip_getScheduleTask() {
        return bundle.getString("tip.getScheduleTask");
    }

    public static String tip_deletePath() {
        return bundle.getString("tip.deletePath");
    }

    public static String tip_loadAutoClickBtn() {
        return bundle.getString("tip.loadAutoClickBtn");
    }

    public static String tip_nextGcType() {
        return bundle.getString("tip.nextGcType");
    }

    public static String tip_language() {
        return bundle.getString("tip.language");
    }

    public static String tip_sampleInterval() {
        return bundle.getString("tip.sampleInterval");
    }

    public static String tip_clickRetryNum() {
        return bundle.getString("tip.clickRetryNum");
    }

    public static String tip_stopRetryNum() {
        return bundle.getString("tip.stopRetryNum");
    }

    public static String tip_opacity() {
        return bundle.getString("tip.opacity");
    }

    public static String tip_recordClick() {
        return bundle.getString("tip.recordClick");
    }

    public static String tip_autoClickFileName() {
        return bundle.getString("tip.autoClickFileName");
    }

    public static String tip_firstClick() {
        return bundle.getString("tip.firstClick");
    }

    public static String tip_autoSave() {
        return bundle.getString("tip.autoSave");
    }

    public static String tip_hideWindowRun() {
        return bundle.getString("tip.hideWindowRun");
    }

    public static String tip_NativeHookException() {
        return bundle.getString("tip.NativeHookException") + appName + app +
                bundle.getString("tip.ifHave") + appName + app +
                bundle.getString("tip.addList") + appName + app;
    }

    public static String tip_noScreenCapturePermission() {
        return bundle.getString("tip.noScreenCapturePermission") + appName + app +
                bundle.getString("tip.ifHave") + appName + app +
                bundle.getString("tip.addList") + appName + app;
    }

    public static String tip_randomClickX() {
        return bundle.getString("tip.randomClickX");
    }

    public static String tip_randomClickY() {
        return bundle.getString("tip.randomClickY");
    }

    public static String tip_randomTime() {
        return bundle.getString("tip.randomTime");
    }

    public static String tip_clickTimeOffset() {
        return bundle.getString("tip.clickTimeOffset");
    }

    public static String tip_imgX() {
        return bundle.getString("tip.imgX");
    }

    public static String tip_imgY() {
        return bundle.getString("tip.imgY");
    }

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
     * @return 勾选后只能选择PMC文件所在文件夹
     * 选中文件夹后将会导入文件夹下所有的PMC文件，如果文件夹中有图片，
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
     * @return 版本：{@value priv.koishi.pmc.Finals.CommonFinals#version}
     * 构建日期：{@value priv.koishi.pmc.Finals.CommonFinals#buildDate}
     */
    public static String tip_version() {
        return """
                %s：%s
                %s：%s""".formatted(
                bundle.getString("tip.version"), version,
                bundle.getString("tip.buildDate"), buildDate);
    }

    /**
     * @return 选择文件夹
     */
    public static String text_selectDirectory() {
        return bundle.getString("selectDirectory");
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

    public static String text_copySuccess() {
        return bundle.getString("copySuccess");
    }

    public static String text_successSave() {
        return bundle.getString("successSave");
    }

    public static String text_nowValue() {
        return bundle.getString("nowValue");
    }

    public static String text_saveSuccess() {
        return bundle.getString("saveSuccess");
    }

    public static String text_loadSuccess() {
        return bundle.getString("loadSuccess");
    }

    public static String text_executionTime() {
        return bundle.getString("executionTime");
    }

    public static String text_execution() {
        return bundle.getString("execution");
    }

    public static String text_cancelTask() {
        return bundle.getString("cancelTask");
    }

    public static String text_saveFloatingCoordinate() {
        return bundle.getString("saveFloatingCoordinate");
    }

    public static String text_escCloseFloating() {
        return bundle.getString("escCloseFloating");
    }

    public static String text_closeFloating() {
        return bundle.getString("closeFloating");
    }

    public static String text_saveCloseFloating() {
        return bundle.getString("saveCloseFloating");
    }

    public static String text_showFloating() {
        return bundle.getString("showFloating");
    }

    public static String text_loadAutoClick() {
        return bundle.getString("loadAutoClick");
    }

    public static String text_formatError() {
        return bundle.getString("formatError");
    }

    public static String text_noAutoClickList() {
        return bundle.getString("noAutoClickList");
    }

    public static String text_noAutoClickToRun() {
        return bundle.getString("noAutoClickToRun");
    }

    public static String text_missingKeyData() {
        return bundle.getString("missingKeyData");
    }

    public static String text_step() {
        return bundle.getString("step");
    }

    public static String text_recordClicking() {
        return bundle.getString("recordClicking");
    }

    public static String text_preparation() {
        return bundle.getString("preparation");
    }

    public static String text_run() {
        return bundle.getString("run");
    }

    public static String text_recorded() {
        return bundle.getString("recorded");
    }

    public static String text_mouseTrajectory() {
        return bundle.getString("autoClick.mouseTrajectory");
    }

    public static String text_taskFailed() {
        return bundle.getString("taskFailed");
    }

    public static String text_taskCancelled() {
        return bundle.getString("taskCancelled");
    }

    public static String text_taskFinished() {
        return bundle.getString("taskFinished");
    }

    public static String text_isAdd() {
        return bundle.getString("isAdd");
    }

    public static String text_isRecord() {
        return bundle.getString("isRecord");
    }

    public static String text_noImg() {
        return bundle.getString("noImg");
    }

    public static String text_badImg() {
        return bundle.getString("badImg");
    }

    public static String text_retryStepGreaterMax() {
        return bundle.getString("retryStepGreaterMax");
    }

    public static String text_retryStepEqualIndex() {
        return bundle.getString("retryStepEqualIndex");
    }

    public static String text_retryStepIsNull() {
        return bundle.getString("retryStepIsNull");
    }

    public static String text_matchedStepGreaterMax() {
        return bundle.getString("matchedStepGreaterMax");
    }

    public static String text_matchedStepIsNull() {
        return bundle.getString("matchedStepIsNull");
    }

    public static String text_onlyLaunch() {
        return bundle.getString("onlyLaunch");
    }

    public static String text_recordEnd() {
        return bundle.getString("autoClick.recordEnd");
    }

    public static String text_image() {
        return bundle.getString("img");
    }

    public static String text_progress() {
        return bundle.getString("progress");
    }

    public static String text_willBe() {
        return bundle.getString("willBe");
    }

    public static String text_ms() {
        return bundle.getString("unit.ms");
    }

    public static String text_msWillBe() {
        return bundle.getString("msWillBe");
    }

    public static String text_index() {
        return bundle.getString("taskIndex");
    }

    public static String text_point() {
        return bundle.getString("point");
    }

    public static String text_unknownListener() {
        return bundle.getString("unknownListener");
    }

    public static String text_openDirectory() {
        return bundle.getString("openDirectory");
    }

    public static String text_openParentDirectory() {
        return bundle.getString("openParentDirectory");
    }

    public static String text_openFile() {
        return bundle.getString("openFile");
    }

    public static String text_copyPath() {
        return bundle.getString("copyPath");
    }

    public static String text_mouseClickOpen() {
        return bundle.getString("mouseClickOpen");
    }

    public static String text_mouseClickOpenNull() {
        return bundle.getString("mouseClickOpenNull");
    }

    public static String text_imgPath() {
        return bundle.getString("imgPath");
    }

    public static String text_abnormal() {
        return bundle.getString("abnormal");
    }

    public static String text_readData() {
        return bundle.getString("readData");
    }

    public static String text_matchImg() {
        return bundle.getString("matchImg");
    }

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
     * @return 最接近匹配度为：
     */
    public static String text_closestMatchThreshold() {
        return bundle.getString("closestMatchThreshold");
    }

    /**
     * @return 容量不能小于0
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
     * @return 步骤详情
     */
    public static String clickDetail_title() {
        return bundle.getString("clickDetail.title");
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

    public static String confirm_unSaved() {
        return bundle.getString("confirm.unSaved");
    }

    public static String confirm_unSavedConfirm() {
        return bundle.getString("confirm.unSavedConfirm");
    }

    public static String confirm_ok() {
        return bundle.getString("confirm.ok");
    }

    public static String confirm_cancelSave() {
        return bundle.getString("confirm.cancelSave");
    }

    public static String confirm_reLaunch() {
        return bundle.getString("confirm.reLaunch");
    }

    public static String confirm_reLaunchConfirm() {
        return bundle.getString("confirm.reLaunchConfirm");
    }

    public static String confirm_reLaunchOk() {
        return bundle.getString("confirm.reLaunchOk");
    }

    public static String confirm_cancel() {
        return bundle.getString("confirm.cancel");
    }

    public static String confirm_imageExist() {
        return bundle.getString("confirm.imageExist");
    }

    public static String confirm_imageExistConfirm() {
        return bundle.getString("confirm.imageExistConfirm");
    }

    public static String confirm_delete() {
        return bundle.getString("confirm.delete");
    }

    public static String menuItem_insertUp() {
        return bundle.getString("menuItem.insertUp");
    }

    public static String menuItem_insertDown() {
        return bundle.getString("menuItem.insertDown");
    }

    public static String menuItem_recordUp() {
        return bundle.getString("menuItem.recordUp");
    }

    public static String menuItem_recordDown() {
        return bundle.getString("menuItem.recordDown");
    }

    public static String menuItem_insertTop() {
        return bundle.getString("menuItem.insertTop");
    }

    public static String menuItem_recordTop() {
        return bundle.getString("menuItem.recordTop");
    }

    public static String menuItem_upCopy() {
        return bundle.getString("menuItem.upCopy");
    }

    public static String menuItem_downCopy() {
        return bundle.getString("menuItem.downCopy");
    }

    public static String menuItem_appendCopy() {
        return bundle.getString("menuItem.appendCopy");
    }

    public static String menuItem_topCopy() {
        return bundle.getString("menuItem.topCopy");
    }

    public static String menuItem_openSelected() {
        return bundle.getString("menuItem.openSelected");
    }

    public static String menuItem_openDirectory() {
        return bundle.getString("menuItem.openDirectory");
    }

    public static String menuItem_copyFilePath() {
        return bundle.getString("menuItem.copyFilePath");
    }

    public static String menuItem_moveUp() {
        return bundle.getString("menuItem.moveUp");
    }

    public static String menuItem_moveDown() {
        return bundle.getString("menuItem.moveDown");
    }

    public static String menuItem_moveTop() {
        return bundle.getString("menuItem.moveTop");
    }

    public static String menuItem_moveBottom() {
        return bundle.getString("menuItem.moveBottom");
    }

    public static String menu_cancelSelected() {
        return bundle.getString("menu.cancelSelected");
    }

    public static String menu_changeFirstImg() {
        return bundle.getString("menu.changeFirstImg");
    }

    public static String menu_changeRetryType() {
        return bundle.getString("menu.changeRetryType");
    }

    public static String menu_changeKey() {
        return bundle.getString("menu.changeKey");
    }

    public static String menu_moveSelected() {
        return bundle.getString("menu.moveSelected");
    }

    public static String menu_viewFile() {
        return bundle.getString("menu.viewFile");
    }

    public static String menu_copy() {
        return bundle.getString("menu.copy");
    }

    public static String menu_detailMenu() {
        return bundle.getString("menu.detailMenu");
    }

    public static String menu_deleteMenu() {
        return bundle.getString("menu.deleteMenu");
    }

    public static String menu_runSelectMenu() {
        return bundle.getString("menu.runSelectMenu");
    }

    public static String menu_addDateMenu() {
        return bundle.getString("menu.addDateMenu");
    }

    public static String log_press() {
        return bundle.getString("log.press");
    }

    public static String log_release() {
        return bundle.getString("log.release");
    }

    public static String log_move() {
        return bundle.getString("log.move");
    }

    public static String log_hold() {
        return bundle.getString("log.hold");
    }

    public static String log_drag() {
        return bundle.getString("log.drag");
    }

    public static String log_wait() {
        return bundle.getString("log.wait");
    }

    public static String log_clickImg() {
        return bundle.getString("log.clickImg");
    }

    public static String log_stopImg() {
        return bundle.getString("log.stopImg");
    }

    public static String log_findImage() {
        return bundle.getString("log.findImage");
    }

    public static String retryType_continuously() {
        return bundle.getString("retryType.continuously");
    }

    public static String retryType_click() {
        return bundle.getString("retryType.click");
    }

    public static String retryType_stop() {
        return bundle.getString("retryType.stop");
    }

    public static String retryType_break() {
        return bundle.getString("retryType.break");
    }

    public static String retryType_Step() {
        return bundle.getString("retryType.step");
    }

    public static String extension_file() {
        return bundle.getString("extension.file");
    }

    public static String extension_folder() {
        return bundle.getString("extension.folder");
    }

    public static String extension_fileOrFolder() {
        return bundle.getString("extension.fileOrFolder");
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
     * 自动操作的操作类型选项
     */
    public static final List<String> clickTypeList = new ArrayList<>();

    /**
     * 更新自动操作的操作类型选项
     */
    public static void updateClickTypeList() {
        List<String> newList = Arrays.asList(
                clickType_moveTrajectory(),
                clickType_move(),
                clickType_click(),
                clickType_drag(),
                clickType_moveTo());
        clickTypeList.clear();
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
        clickTypeMap.put(ClickTypeEnum.MOVE_TRAJECTORY.ordinal(), clickType_moveTrajectory());
        clickTypeMap.put(ClickTypeEnum.MOVE.ordinal(), clickType_move());
        clickTypeMap.put(ClickTypeEnum.CLICK.ordinal(), clickType_click());
        clickTypeMap.put(ClickTypeEnum.DRAG.ordinal(), clickType_drag());
        clickTypeMap.put(ClickTypeEnum.MOVETO.ordinal(), clickType_moveTo());
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
    }

}
