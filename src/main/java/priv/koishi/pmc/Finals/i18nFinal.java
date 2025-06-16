package priv.koishi.pmc.Finals;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import javafx.scene.input.MouseButton;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import priv.koishi.pmc.Finals.Enum.ClickTypeEnum;
import priv.koishi.pmc.Finals.Enum.MatchedTypeEnum;
import priv.koishi.pmc.Finals.Enum.RepeatTypeEnum;
import priv.koishi.pmc.Finals.Enum.RetryTypeEnum;

import java.util.*;
import java.util.stream.Collectors;

import static priv.koishi.pmc.Finals.CommonFinals.app;
import static priv.koishi.pmc.Finals.CommonFinals.appName;
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
     */
    public static String autoSaveFileName() {
        return bundle.getString("autoSaveFileName");
    }

    /**
     * 默认导出文件名称
     */
    public static String defaultOutFileName() {
        return bundle.getString("defaultOutFileName");
    }

    /**
     * 定时任务默认任务名称
     */
    public static String defaultTaskName() {
        return bundle.getString("defaultTaskName");
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

    public static String tip_logsNum() {
        return bundle.getString("tip.logsNum");
    }

    public static String tip_learButton() {
        return bundle.getString("tip.learButton");
    }

    public static String tip_openDirectory() {
        return bundle.getString("tip.openDirectory");
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

    public static String text_selectDirectory() {
        return bundle.getString("selectDirectory");
    }

    public static String text_selectTemplateImg() {
        return bundle.getString("selectTemplateImg");
    }

    public static String text_dataListNull() {
        return bundle.getString("listText.null");
    }

    public static String text_outPathNull() {
        return bundle.getString("outPathNull");
    }

    public static String text_selectAutoFile() {
        return bundle.getString("selectAutoFile");
    }

    public static String text_fileNotExists() {
        return bundle.getString("fileNotExists");
    }

    public static String text_nullPath() {
        return bundle.getString("pathNull");
    }

    public static String text_errPathFormat() {
        return bundle.getString("errPathFormat");
    }

    public static String text_allHave() {
        return bundle.getString("allHave");
    }

    public static String text_data() {
        return bundle.getString("unit.data");
    }

    public static String text_task() {
        return bundle.getString("unit.task");
    }

    public static String text_img() {
        return bundle.getString("unit.img");
    }

    public static String text_log() {
        return bundle.getString("unit.log");
    }

    public static String text_process() {
        return bundle.getString("unit.process");
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

    public static String text_imgExist() {
        return bundle.getString("imgExist");
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

    public static String clickMatched_click() {
        return bundle.getString("clickMatched.click");
    }

    public static String clickMatched_break() {
        return bundle.getString("clickMatched.break");
    }

    public static String clickMatched_step() {
        return bundle.getString("clickMatched.step");
    }

    public static String clickMatched_clickStep() {
        return bundle.getString("clickMatched.clickStep");
    }

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
                clickMatched_clickStep());
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

    public static String mouseButton_primary() {
        return bundle.getString("primary");
    }

    public static String mouseButton_secondary() {
        return bundle.getString("secondary");
    }

    public static String mouseButton_middle() {
        return bundle.getString("middle");
    }

    public static String mouseButton_forward() {
        return bundle.getString("forward");
    }

    public static String mouseButton_back() {
        return bundle.getString("back");
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

    public static String clickType_moveTrajectory() {
        return bundle.getString("clickType.moveTrajectory");
    }

    public static String clickType_move() {
        return bundle.getString("clickType.move");
    }

    public static String clickType_click() {
        return bundle.getString("clickType.click");
    }

    public static String clickType_drag() {
        return bundle.getString("clickType.drag");
    }

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

    public static String repeatType_daily() {
        return bundle.getString("repeatType.daily");
    }

    public static String repeatType_weekly() {
        return bundle.getString("repeatType.weekly");
    }

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

    public static String monday() {
        return bundle.getString("monday");
    }

    public static String tuesday() {
        return bundle.getString("tuesday");
    }

    public static String wednesday() {
        return bundle.getString("wednesday");
    }

    public static String thursday() {
        return bundle.getString("thursday");
    }

    public static String friday() {
        return bundle.getString("friday");
    }

    public static String saturday() {
        return bundle.getString("saturday");
    }

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

    public static final String zh_CN = "简体中文";

    public static final String zh_TW = "繁體中文";

    public static final String en = "English";

    /**
     * 切换语言下拉框选项
     */
    public static final BidiMap<Locale, String> languageMap = new DualHashBidiMap<>();

    static {
        languageMap.put(Locale.SIMPLIFIED_CHINESE, zh_CN);
        languageMap.put(Locale.TRADITIONAL_CHINESE, zh_TW);
        languageMap.put(Locale.ENGLISH, en);
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
    }

}
