package priv.koishi.pmc.Controller;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.*;
import priv.koishi.pmc.Bean.Config.FileChooserConfig;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;
import priv.koishi.pmc.Bean.Result.PMCLoadResult;
import priv.koishi.pmc.Bean.Result.PMCLogResult;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Bean.VO.ImgFileVO;
import priv.koishi.pmc.Callback.InputRecordCallback;
import priv.koishi.pmc.Event.EventBus;
import priv.koishi.pmc.Event.SettingsLoadedEvent;
import priv.koishi.pmc.Finals.Enum.ClickTypeEnum;
import priv.koishi.pmc.Finals.Enum.FindImgTypeEnum;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;
import priv.koishi.pmc.Listener.MousePositionListener;
import priv.koishi.pmc.Listener.MousePositionUpdater;
import priv.koishi.pmc.Listener.UnifiedInputRecordListener;
import priv.koishi.pmc.Queue.DynamicQueue;
import priv.koishi.pmc.UI.CustomEditingCell.EditingCell;
import priv.koishi.pmc.UI.CustomEditingCell.ItemConsumer;
import priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow;
import priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindowDescriptor;
import priv.koishi.pmc.UI.CustomMessageBubble.MessageBubble;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static priv.koishi.pmc.Controller.FileChooserController.chooserFiles;
import static priv.koishi.pmc.Controller.MainController.listPMCController;
import static priv.koishi.pmc.Controller.MainController.settingController;
import static priv.koishi.pmc.Controller.SettingController.*;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.CommonKeys.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.JnaNative.PermissionChecker.MacChecker.hasScreenCapturePermission;
import static priv.koishi.pmc.MainApplication.*;
import static priv.koishi.pmc.Service.AutoClickService.*;
import static priv.koishi.pmc.Service.ImageRecognitionService.refreshScreenParameters;
import static priv.koishi.pmc.Service.PMCFileService.*;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.*;
import static priv.koishi.pmc.Utils.ButtonMappingUtils.*;
import static priv.koishi.pmc.Utils.CommonUtils.copyAllProperties;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.ListenerUtils.*;
import static priv.koishi.pmc.Utils.NodeDisableUtils.*;
import static priv.koishi.pmc.Utils.TableViewUtils.*;
import static priv.koishi.pmc.Utils.TaskUtils.*;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;
import static priv.koishi.pmc.Utils.ToolTipUtils.creatTooltip;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 自动操作工具页面控制器
 *
 * @author KOISHI
 * Date:2025-02-17
 * Time:17:21
 */
public class AutoClickController extends RootController implements MousePositionUpdater {

    /**
     * 导入文件路径
     */
    private String inFilePath;

    /**
     * 上次所选要点击的图片地址
     */
    private String clickImgSelectPath;

    /**
     * 上次所选终止操作的图片地址
     */
    public static String stopImgSelectPath;

    /**
     * 默认要点击的图片识别重试次数
     */
    private String clickRetryNum;

    /**
     * 默认终止操作图片识别重试次数
     */
    private String stopRetryNum;

    /**
     * 默认要点击的图片识别匹配度
     */
    private String defaultClickOpacity;

    /**
     * 默认终止操作图片识别匹配度
     */
    private String defaultStopOpacity;

    /**
     * 默认终止操作图片
     */
    private List<ImgFileBean> defaultStopImgFiles;

    /**
     * 操作记录
     */
    private List<ClickLogBean> clickLogs = new CopyOnWriteArrayList<>();

    /**
     * 详情页高度
     */
    private int detailHeight;

    /**
     * 详情页宽度
     */
    private int detailWidth;

    /**
     * 记录页高度
     */
    private int logHeight;

    /**
     * 记录页宽度
     */
    private int logWidth;

    /**
     * 鼠标轨迹采样间隔
     */
    private String sampleInterval;

    /**
     * 是否启用随机点击坐标 0-不启用，1-启用
     */
    private String randomClick;

    /**
     * 是否启用随机轨迹 0-不启用，1-启用
     */
    private String randomTrajectory;

    /**
     * 横轴随机偏移量
     */
    private String randomClickX;

    /**
     * 纵轴随机偏移量
     */
    private String randomClickY;

    /**
     * 是否启用随机点击时长 0-不启用，1-启用
     */
    private String randomClickTime;

    /**
     * 默认点击时长（单位：毫秒）
     */
    private String clickTimeOffset;

    /**
     * 随机点击时长（单位：毫秒）
     */
    private String randomTime;

    /**
     * 是否启用随机点击间隔 0-不启用，1-启用
     */
    private String randomClickInterval;

    /**
     * 是否启用随机等待时长 0-不启用，1-启用
     */
    private String randomWaitTime;

    /**
     * 记录鼠标移动轨迹
     */
    private boolean recordMove;

    /**
     * 记录鼠标拖拽轨迹
     */
    private boolean recordDrag;

    /**
     * 录制时记录鼠标滑轮事件
     */
    private boolean recordMouseWheel;

    /**
     * 录制时记录键盘事件
     */
    private boolean recordKeyboard;

    /**
     * 录制时记录鼠标点击事件
     */
    private boolean recordMouseClick;

    /**
     * 要防重复点击的组件
     */
    public final List<Node> disableNodes = new ArrayList<>();

    /**
     * 自动点击任务
     */
    public Task<PMCLogResult> autoClickTask;

    /**
     * 批量导入 PMC 文件任务
     */
    public Task<PMCLoadResult> loadPMCFilsTask;

    /**
     * 导入 PMC 文件任务
     */
    public Task<List<ClickPositionVO>> loadedPMCTask;

    /**
     * 导出 PMC 文件任务
     */
    public Task<String> exportPMCTask;

    /**
     * 页面标识符
     */
    private final String tabId = "_Click";

    /**
     * 无辅助功能权限
     */
    public static boolean isNativeHookException;

    /**
     * 无录屏与录音权限（true-无权限 false-有权限）
     */
    public static boolean noScreenCapturePermission;

    /**
     * 正在录制标识
     */
    public static boolean recordClicking;

    /**
     * 正在运行自动操作标识
     */
    public static boolean runClicking;

    /**
     * 正在获取当前窗口信息标志（true 正在获取窗口信息）
     */
    public static boolean findingWindow;

    /**
     * 窗口子页面已打开标识（true 子窗口已打开）
     */
    public static boolean isSonOpening;

    /**
     * 录制时间线
     */
    private Timeline recordTimeline;

    /**
     * 运行时间线
     */
    private Timeline runTimeline;

    /**
     * 全局键盘监听器
     */
    private NativeKeyListener nativeKeyListener;

    /**
     * 全局输入监听器
     */
    private UnifiedInputRecordListener listener;

    /**
     * 全局录制与运行快捷键监听器
     */
    public static UnifiedInputRecordListener shortcutsListener;

    /**
     * 信息浮窗设置
     */
    public static FloatingWindowDescriptor messageFloating;

    /**
     * 录制信息字体颜色绑定
     */
    public static final ObjectProperty<Color> recordTextColorProperty = new SimpleObjectProperty<>(Color.BLUE);

    /**
     * 详情页舞台
     */
    public Stage detailStage;

    /**
     * 详情页窗口标题
     */
    private String detailTitle;

    @FXML
    public AnchorPane anchorPane_Click;

    @FXML
    public HBox logHBox_Click;

    @FXML
    public ProgressBar progressBar_Click;

    @FXML
    public Label mousePosition_Click, dataNumber_Click, log_Click, tip_Click, cancelTip_Click, outPath_Click, err_Click;

    @FXML
    public CheckBox openDirectory_Click, notOverwrite_Click, loadFolder_Click;

    @FXML
    public Button clearButton_Click, runClick_Click, addPosition_Click, loadAutoClick_Click, exportAutoClick_Click,
            addOutPath_Click, recordClick_Click, clickLog_Click;

    @FXML
    public TextField loopTime_Click, outFileName_Click, preparationRecordTime_Click, preparationRunTime_Click;

    @FXML
    public TableView<ClickPositionVO> tableView_Click;

    @FXML
    public TableColumn<ClickPositionVO, Integer> index_Click;

    @FXML
    public TableColumn<ClickPositionVO, ImageView> thumb_Click;

    @FXML
    public TableColumn<ClickPositionVO, String> name_Click, clickTime_Click, clickNum_Click, clickKey_Click,
            waitTime_Click, clickType_Click, matchedType_Click, retryType_Click;

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tableView_Click.setPrefHeight(stageHeight * 0.5);
        // 设置组件宽度
        double tableWidth = mainStage.getWidth() * 0.95;
        tableView_Click.setMaxWidth(tableWidth);
        tableView_Click.setPrefWidth(tableWidth);
        bindPrefWidthProperty();
    }

    /**
     * 设置 JavaFX 单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.05));
        thumb_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        name_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.13));
        clickTime_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.07));
        clickNum_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.07));
        clickKey_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.08));
        waitTime_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        clickType_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.08));
        matchedType_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.16));
        retryType_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.16));
    }

    /**
     * 保存最后一次配置的值
     *
     * @throws IOException 配置文件保存异常
     */
    public void saveLastConfig() throws IOException {
        if (anchorPane_Click != null) {
            InputStream input = checkRunningInputStream(configFile_Click);
            Properties prop = new Properties();
            prop.load(input);
            prop.put(key_lastLoopTime, loopTime_Click.getText());
            prop.put(key_lastOutFileName, outFileName_Click.getText());
            String lastOpenDirectoryValue = openDirectory_Click.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, lastOpenDirectoryValue);
            String lastNotOverwriteValue = notOverwrite_Click.isSelected() ? activation : unActivation;
            prop.put(key_lastNotOverwrite, lastNotOverwriteValue);
            String loadFolderValue = loadFolder_Click.isSelected() ? activation : unActivation;
            prop.put(key_loadFolder, loadFolderValue);
            prop.put(key_lastPreparationRecordTime, preparationRecordTime_Click.getText());
            prop.put(key_lastPreparationRunTime, preparationRunTime_Click.getText());
            String outPathValue = outPath_Click.getText();
            prop.put(key_outFilePath, outPathValue);
            OutputStream output = checkRunningOutputStream(configFile_Click);
            prop.store(output, null);
            input.close();
            output.close();
            CheckBox autoSave = settingController.autoSavePMC_Set;
            // 自动保存
            autoSave(autoSave, outPathValue);
        }
    }

    /**
     * 自动保存操作流程
     *
     * @param autoSave 自动保存开关
     */
    private void autoSave(CheckBox autoSave, String outPath) {
        if (autoSave.isSelected()) {
            List<ClickPositionVO> tableViewItems = new ArrayList<>(tableView_Click.getItems());
            if (CollectionUtils.isNotEmpty(tableViewItems)) {
                TaskBean<ClickPositionVO> taskBean = creatTaskBean();
                taskBean.setMessageLabel(log_Click)
                        .setBeanList(tableViewItems);
                exportPMCTask = exportPMC(taskBean, autoSavePMCFileName(), outPath, notOverwrite_Click.isSelected());
                bindingTaskNode(exportPMCTask, taskBean);
                exportPMCTask.setOnSucceeded(_ -> {
                    taskUnbind(taskBean);
                    log_Click.setTextFill(Color.GREEN);
                    exportPMCTask = null;
                });
                exportPMCTask.setOnFailed(event -> {
                    exportPMCTask = null;
                    taskNotSuccess(taskBean, text_taskFailed());
                    throw new RuntimeException(event.getSource().getException());
                });
                Thread.ofVirtual()
                        .name("exportPMCTask-vThread" + tabId)
                        .start(exportPMCTask);
            }
        }
    }

    /**
     * 设置初始配置值为上次配置值
     *
     * @throws IOException 配置文件保存异常
     */
    private void setLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Click);
        prop.load(input);
        if (activation.equals(prop.getProperty(key_loadLastConfig, activation))) {
            setControlLastConfig(outPath_Click, prop, key_outFilePath);
            setControlLastConfig(loadFolder_Click, prop, key_loadFolder, unActivation);
            setControlLastConfig(loopTime_Click, prop, key_lastLoopTime, defaultLoopTime);
            setControlLastConfig(notOverwrite_Click, prop, key_lastNotOverwrite, activation);
            setControlLastConfig(openDirectory_Click, prop, key_lastOpenDirectory, activation);
            setControlLastConfig(outFileName_Click, prop, key_lastOutFileName, defaultPMCFileName());
            setControlLastConfig(preparationRunTime_Click, prop, key_lastPreparationRunTime, defaultPreparationRun);
            setControlLastConfig(preparationRecordTime_Click, prop, key_lastPreparationRecordTime, defaultPreparationRecord);
        }
        if (StringUtils.isBlank(outPath_Click.getText())) {
            setPathLabel(outPath_Click, defaultFileChooserPath);
        }
        input.close();
    }

    /**
     * 读取配置文件
     *
     * @throws IOException 配置文件读取异常
     */
    private void getProperties() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Click);
        prop.load(input);
        inFilePath = prop.getProperty(key_inFilePath);
        stopImgSelectPath = prop.getProperty(key_stopImgSelectPath, desktopPath);
        clickImgSelectPath = prop.getProperty(key_clickImgSelectPath, desktopPath);
        logWidth = Integer.parseInt(prop.getProperty(key_logWidth, defaultLogWidth));
        logHeight = Integer.parseInt(prop.getProperty(key_logHeight, defaultLogHeight));
        detailWidth = Integer.parseInt(prop.getProperty(key_clickDetailWidth, defaultClickDetailWidth));
        detailHeight = Integer.parseInt(prop.getProperty(key_clickDetailHeight, defaultClickDetailHeight));
        input.close();
    }

    /**
     * 读取设置页面设置的值
     */
    private void getSetting() {
        Slider clickOpacity = settingController.clickOpacity_Set;
        defaultClickOpacity = String.valueOf(clickOpacity.getValue());
        Slider stopOpacity = settingController.stopOpacity_Set;
        defaultStopOpacity = String.valueOf(stopOpacity.getValue());
        TextField clickRetryNumTextField = settingController.clickRetryNum_Set;
        clickRetryNum = StringUtils.isBlank(clickRetryNumTextField.getText()) ?
                defaultClickRetryNum : clickRetryNumTextField.getText();
        TextField stopRetryNumTextField = settingController.stopRetryNum_Set;
        stopRetryNum = StringUtils.isBlank(stopRetryNumTextField.getText()) ?
                defaultStopRetryNum : stopRetryNumTextField.getText();
        TextField sampleIntervalTextField = settingController.sampleInterval_Set;
        sampleInterval = StringUtils.isBlank(sampleIntervalTextField.getText()) ?
                defaultSampleInterval : sampleIntervalTextField.getText();
        TableView<ImgFileVO> tableView = settingController.tableView_Set;
        defaultStopImgFiles = tableView.getItems().stream().map(o -> (ImgFileBean) o).toList();
        CheckBox recordMoveCheckBox = settingController.recordMove_Set;
        recordMove = recordMoveCheckBox.isSelected();
        CheckBox recordDragCheckBox = settingController.recordDrag_Set;
        recordDrag = recordDragCheckBox.isSelected();
        CheckBox randomClickCheckBox = settingController.randomClick_Set;
        randomClick = randomClickCheckBox.isSelected() ? activation : unActivation;
        CheckBox randomTrajectoryCheckBox = settingController.randomTrajectory_Set;
        randomTrajectory = randomTrajectoryCheckBox.isSelected() ? activation : unActivation;
        CheckBox randomClickTimeCheckBox = settingController.randomClickTime_Set;
        randomClickTime = randomClickTimeCheckBox.isSelected() ? activation : unActivation;
        TextField randomTimeTextField = settingController.randomTimeOffset_Set;
        randomTime = StringUtils.isBlank(randomTimeTextField.getText()) ?
                defaultRandomTime : randomTimeTextField.getText();
        TextField clickTimeOffsetTextField = settingController.clickTimeOffset_Set;
        clickTimeOffset = StringUtils.isBlank(clickTimeOffsetTextField.getText()) ?
                defaultClickTimeOffset : clickTimeOffsetTextField.getText();
        TextField randomClickXTextField = settingController.randomClickX_Set;
        randomClickX = StringUtils.isBlank(randomClickXTextField.getText()) ?
                defaultRandomClickX : randomClickXTextField.getText();
        TextField randomClickYTextField = settingController.randomClickY_Set;
        randomClickY = StringUtils.isBlank(randomClickYTextField.getText()) ?
                defaultRandomClickY : randomClickYTextField.getText();
        CheckBox randomWaitTimeCheckBox = settingController.randomWaitTime_Set;
        randomWaitTime = randomWaitTimeCheckBox.isSelected() ? activation : unActivation;
        CheckBox randomClickIntervalCheckBox = settingController.randomClickInterval_Set;
        randomClickInterval = randomClickIntervalCheckBox.isSelected() ? activation : unActivation;
        CheckBox recordMouseWheelCheckBox = settingController.recordMouseWheel_Set;
        recordMouseWheel = recordMouseWheelCheckBox.isSelected();
        CheckBox recordKeyboardCheckBox = settingController.recordKeyboard_Set;
        recordKeyboard = recordKeyboardCheckBox.isSelected();
        CheckBox recordMouseClickCheckBox = settingController.recordMouseClick_Set;
        recordMouseClick = recordMouseClickCheckBox.isSelected();
    }

    /**
     * 判断单元格是否可编辑
     *
     * @param clickType 操作类型枚举
     * @return true 可编辑 false 不可编辑
     */
    private static boolean canEdit(int clickType) {
        return clickType != ClickTypeEnum.MOVE_TRAJECTORY.ordinal()
                && clickType != ClickTypeEnum.MOVE.ordinal()
                && clickType != ClickTypeEnum.MOVETO.ordinal()
                && clickType > ClickTypeEnum.OPEN_URL.ordinal();
    }

    /**
     * 判断单元格所在操作步骤是否有按键
     *
     * @param clickType 操作类型枚举
     * @return true 无按键 false 有按键
     */
    private static boolean noKeyCell(int clickType) {
        return !canEdit(clickType)
                || clickType == ClickTypeEnum.WHEEL_UP.ordinal()
                || clickType == ClickTypeEnum.WHEEL_DOWN.ordinal();
    }

    /**
     * 设置单元格可编辑
     */
    private void makeCellCanEdit() {
        // 设置表格可编辑
        tableView_Click.setEditable(true);
        // 设置步骤名称可编辑
        name_Click.setCellFactory((_) ->
                new EditingCell<>(ClickPositionVO::setName));
        // 设置等待时间可编辑
        waitTime_Click.setCellFactory((_) ->
                new EditingCell<>(ClickPositionVO::setWaitTime, true, 0, null));
        // 设置点击时长可编辑
        clickTime_Click.setCellFactory((_) ->
                new EditingCell<>(new ItemConsumer<>() {

                    /**
                     * 将编辑后的对象属性进行保存.
                     * 如果不将属性保存到 cell 所在表格的 ObservableList 集合中对象的相应属性中,
                     * 则只是改变了表格显示的值,一旦表格刷新,则仍会表示旧值.
                     *
                     * @param item     当前行数据
                     * @param value 新值
                     */
                    @Override
                    public void setTProperties(ClickPositionVO item, String value) {
                        item.setClickTime(value);
                    }

                    /**
                     * 检查单元格是否可编辑
                     *
                     * @param item 当前行数据
                     * @return true-可编辑，false-不可编辑
                     */
                    @Override
                    public boolean isEditable(ClickPositionVO item) {
                        int clickType = item.getClickTypeEnum();
                        return clickType != ClickTypeEnum.COMBINATIONS.ordinal();
                    }

                    /**
                     * 获取单元格禁用时的显示值
                     *
                     * @param item 当前行数据
                     * @return 禁用时显示的值
                     */
                    @Override
                    public String getDisabledValue(ClickPositionVO item) {
                        int clickType = item.getClickTypeEnum();
                        if (clickType == ClickTypeEnum.COMBINATIONS.ordinal()) {
                            return item.getClickTime();
                        }
                        return null;
                    }

                }, true, 0, null));
        // 设置点击次数可编辑
        clickNum_Click.setCellFactory(_ -> new EditingCell<>(
                new ItemConsumer<>() {

                    /**
                     * 将编辑后的对象属性进行保存.
                     * 如果不将属性保存到 cell 所在表格的 ObservableList 集合中对象的相应属性中,
                     * 则只是改变了表格显示的值,一旦表格刷新,则仍会表示旧值.
                     *
                     * @param item     当前行数据
                     * @param value 新值
                     */
                    @Override
                    public void setTProperties(ClickPositionVO item, String value) {
                        item.setClickNum(value);
                    }

                    /**
                     * 检查单元格是否可编辑
                     *
                     * @param item 当前行数据
                     * @return true-可编辑，false-不可编辑
                     */
                    @Override
                    public boolean isEditable(ClickPositionVO item) {
                        int clickType = item.getClickTypeEnum();
                        return canEdit(clickType);
                    }

                    /**
                     * 获取单元格禁用时的显示值
                     *
                     * @param item 当前行数据
                     * @return 禁用时显示的值
                     */
                    @Override
                    public String getDisabledValue(ClickPositionVO item) {
                        int clickType = item.getClickTypeEnum();
                        if (!canEdit(clickType)) {
                            return "1";
                        }
                        return null;
                    }

                }, true, 1, null));
    }

    /**
     * 显示详情页
     *
     * @param item 要显示详情的操作流程设置
     */
    private void showDetail(ClickPositionVO item) {
        URL fxmlLocation = getClass().getResource(resourcePath + "fxml/ClickDetail-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation, bundle);
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ClickDetailController controller = loader.getController();
        item.setClickImgSelectPath(clickImgSelectPath);
        item.setStopImgSelectPath(stopImgSelectPath);
        try {
            controller.initData(item, inFilePath);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        // 设置保存后的回调
        controller.setRefreshCallback(() -> {
            String clickSelectPath = item.getClickImgSelectPath();
            if (StringUtils.isNotBlank(clickSelectPath)) {
                clickImgSelectPath = clickSelectPath;
            }
            String stopSelectPath = item.getStopImgSelectPath();
            if (StringUtils.isNotBlank(stopSelectPath)) {
                stopImgSelectPath = stopSelectPath;
            }
            if (item.isRemove()) {
                tableView_Click.getItems().remove(item);
            }
            // 更新缩略图
            item.updateThumb();
            // 刷新列表
            tableView_Click.refresh();
            // 更新列表数量
            updateTableViewSizeText(tableView_Click, dataNumber_Click, unit_process());
        });
        detailStage = new Stage();
        Scene scene = new Scene(root, detailWidth, detailHeight);
        detailStage.setScene(scene);
        String title = item.getName() == null ? "" : item.getName();
        detailTitle = title + clickDetail_title();
        detailStage.setTitle(detailTitle);
        detailStage.initModality(Modality.APPLICATION_MODAL);
        setWindowLogo(detailStage, logoPath);
        // 监听窗口面板宽度变化
        detailStage.widthProperty().addListener((_, _, _) ->
                Platform.runLater(controller::adaption));
        // 监听窗口面板高度变化
        detailStage.heightProperty().addListener((_, _, _) ->
                Platform.runLater(controller::adaption));
        // 设置 css 样式
        setWindowCss(scene, stylesCss);
        detailStage.show();
        isSonOpening = true;
    }

    /**
     * 初始化浮窗
     */
    private void initFloatingWindow() {
        messageFloating = new FloatingWindowDescriptor()
                .setConfig(SettingController.messageFloating.getConfig())
                .setName(floatingName_message())
                .setEnableResize(false)
                .setAddCloseKey(false)
                .setTransparent(true)
                .setEnableDrag(false)
                .setCloseSave(false)
                .setShowName(false)
                .setFontSize(14);
        // 创建浮窗
        createFloatingWindows(messageFloating);
    }

    /**
     * 显示浮窗
     *
     * @param isRun 是否为运行自动操作
     * @throws IOException 配置文件读取异常
     */
    public static void showFloatingWindow(boolean isRun) throws IOException {
        // 获取浮窗的文本颜色设置
        Color color = settingController.colorPicker_Set.getValue();
        // 获取浮窗的显示设置
        CheckBox floatingRun = settingController.floatingRun_Set;
        CheckBox floatingRecord = settingController.floatingRecord_Set;
        boolean isShow = isRun ? floatingRun.isSelected() : floatingRecord.isSelected();
        Slider slider = settingController.opacity_Set;
        if (isShow) {
            getFloatingSetting(messageFloating, configFile_Click);
            Platform.runLater(() -> {
                if (messageFloating != null) {
                    messageFloating.setConfig(SettingController.messageFloating.getConfig())
                            .setOpacity(slider.getValue())
                            .setTextFill(color);
                    updateFloatingWindow(messageFloating);
                    FloatingWindow.showFloatingWindow(messageFloating);
                }
            });
        }
    }

    /**
     * 判断程序是否为空闲状态
     *
     * @return true 表示为空闲状态，false 表示非空闲状态
     */
    public boolean isFree() {
        return !runClicking && !recordClicking && loadPMCFilsTask == null && loadedPMCTask == null && exportPMCTask == null;
    }

    /**
     * 启动自动操作流程
     *
     * @param clickPositionVOS 自动操作流程
     * @param loopTimes        循环次数
     * @param isBatch          是否为批量执行 PMC 文件（true 批量执行）
     * @throws IOException 配置文件读取异常
     */
    public void launchClickTask(List<ClickPositionVO> clickPositionVOS, int loopTimes, boolean isBatch) throws IOException {
        if (isFree()) {
            if (cancelKey == noKeyboard) {
                throw new RuntimeException(text_noCancelKey());
            }
            // 标记为正在运行自动操作
            runClicking = true;
            if (clickLogs != null) {
                clickLogs.clear();
            }
            Label logLabel = isBatch ? listPMCController.log_List : log_Click;
            updateLabel(logLabel, "");
            AutoClickTaskBean taskBean = buildAutoClickTaskBean(clickPositionVOS, loopTimes, isBatch);
            CheckBox hideWindowRun = settingController.hideWindowRun_Set;
            if (hideWindowRun.isSelected()) {
                mainStage.setIconified(true);
            }
            // 刷新屏幕参数
            refreshScreenParameters();
            // 开启键盘监听
            startNativeKeyListener(taskBean);
            if (isBatch) {
                ObservableList<PMCListBean> pmcListBeans = listPMCController.tableView_List.getItems();
                autoClickTask = autoClicks(new Robot(), pmcListBeans, taskBean);
            } else {
                autoClickTask = autoClick(taskBean, new Robot(), new DynamicQueue<>());
            }
            // 绑定带进度条的线程
            bindingTaskNode(autoClickTask, taskBean, true);
            setTaskEvent(taskBean);
            if (runTimeline == null) {
                TextField preparationRunTime = isBatch ?
                        listPMCController.preparationRunTime_List : preparationRunTime_Click;
                // 获取准备时间值
                int preparation = setDefaultIntValue(preparationRunTime,
                        Integer.parseInt(defaultPreparationRun), 0, null);
                // 设置浮窗文本显示准备时间
                String text = text_cancelTask() + preparation + text_run();
                updateMessageLabel(messageFloating, text);
                logLabel.setText(text);
                showFloatingWindow(true);
                // 延时执行任务
                runTimeline = executeRunTimeLine(preparation, isBatch);
            }
        }
    }

    /**
     * 设置任务事件
     *
     * @param taskBean 线程任务参数
     */
    private void setTaskEvent(AutoClickTaskBean taskBean) {
        autoClickTask.setOnSucceeded(_ -> {
            PMCLogResult value = autoClickTask.getValue();
            clickLogs = value.clickLogBeans();
            listPMCController.clickLogs = value.pmcLogBeans();
            if (clickLogs == null) {
                taskNotSuccess(taskBean, text_taskFailed());
            } else {
                taskUnbind(taskBean);
                Label logLabel = taskBean.getMessageLabel();
                logLabel.setTextFill(Color.GREEN);
                logLabel.setText(text_taskFinished());
                CheckBox showWindowRun = settingController.showWindowRun_Set;
                if (showWindowRun.isSelected()) {
                    if (mainStage.isIconified()) {
                        showStage(mainStage);
                    }
                }
            }
            clearReferences();
            hideFloatingWindow(messageFloating);
            // 移除键盘监听器
            removeNativeListener(nativeKeyListener);
            autoClickTask = null;
            runTimeline = null;
            runClicking = false;
        });
        autoClickTask.setOnFailed(_ -> {
            clickLogs = getNowLogs();
            taskNotSuccess(taskBean, text_taskFailed());
            hideFloatingWindow(messageFloating);
            CheckBox showWindowRun = settingController.showWindowRun_Set;
            if (showWindowRun.isSelected()) {
                showStage(mainStage);
            }
            // 移除键盘监听器
            removeNativeListener(nativeKeyListener);
            // 移除开始前的倒计时
            if (runTimeline != null) {
                runTimeline.stop();
                runTimeline = null;
            }
            Throwable ex = autoClickTask.getException();
            autoClickTask = null;
            runClicking = false;
            clearReferences();
            throw new RuntimeException(ex);
        });
        autoClickTask.setOnCancelled(_ -> {
            clickLogs = getNowLogs();
            taskNotSuccess(taskBean, text_taskCancelled());
            hideFloatingWindow(messageFloating);
            CheckBox showWindowRun = settingController.showWindowRun_Set;
            if (showWindowRun.isSelected()) {
                showStage(mainStage);
            }
            // 移除键盘监听器
            removeNativeListener(nativeKeyListener);
            autoClickTask = null;
            runTimeline = null;
            runClicking = false;
            clearReferences();
        });
    }

    /**
     * 构建自动操作线程任务设置参数
     *
     * @param clickPositionVOS 自动操作流程
     * @param loopTimes        循环次数
     * @param isBatch          是否为批量执行 PMC 文件（true 批量执行）
     */
    private AutoClickTaskBean buildAutoClickTaskBean(List<ClickPositionVO> clickPositionVOS, int loopTimes, boolean isBatch) {
        TextField retrySecond = settingController.retrySecond_Set;
        TextField maxLogNum = settingController.maxLogNum_Set;
        TextField overTime = settingController.overtime_Set;
        CheckBox mouseWheelLog = settingController.mouseWheelLog_Set;
        CheckBox runScriptLog = settingController.runScriptLog_Set;
        CheckBox keyboardLog = settingController.keyboardLog_Set;
        CheckBox clickImgLog = settingController.clickImgLog_Set;
        CheckBox openFileLog = settingController.openFileLog_Set;
        CheckBox stopImgLog = settingController.stopImgLog_Set;
        CheckBox firstClick = settingController.firstClick_Set;
        CheckBox openUrlLog = settingController.openUrlLog_Set;
        CheckBox clickLog = settingController.clickLog_Set;
        CheckBox moveLog = settingController.moveLog_Set;
        CheckBox dragLog = settingController.dragLog_Set;
        CheckBox waitLog = settingController.waitLog_Set;
        AutoClickTaskBean taskBean = new AutoClickTaskBean();
        taskBean.setRetrySecondValue(setDefaultIntValue(retrySecond, 1, 0, null))
                .setOverTimeValue(setDefaultIntValue(overTime, 0, 1, null))
                .setMaxLogNum(setDefaultIntValue(maxLogNum, 0, 1, null))
                .setMouseWheelLog(mouseWheelLog.isSelected())
                .setRunScriptLog(runScriptLog.isSelected())
                .setOpenFileLog(openFileLog.isSelected())
                .setClickImgLog(clickImgLog.isSelected())
                .setKeyboardLog(keyboardLog.isSelected())
                .setOpenUrlLog(openUrlLog.isSelected())
                .setStopImgLog(stopImgLog.isSelected())
                .setFirstClick(firstClick.isSelected())
                .setMessageFloating(messageFloating)
                .setClickLog(clickLog.isSelected())
                .setMoveLog(moveLog.isSelected())
                .setDragLog(dragLog.isSelected())
                .setWaitLog(waitLog.isSelected())
                .setRunTimeline(runTimeline)
                .setLoopTimes(loopTimes)
                .setProgressBar(isBatch ? listPMCController.progressBar_List : progressBar_Click)
                .setDisableNodes(isBatch ? listPMCController.disableNodes : disableNodes)
                .setMessageLabel(isBatch ? listPMCController.log_List : log_Click)
                .setBeanList(clickPositionVOS)
                .setBindingMessageLabel(false);
        return taskBean;
    }

    /**
     * 延时执行任务
     *
     * @param preparation 准备时间
     * @param isBatch     是否为批量执行 PMC 文件（true 批量执行）
     * @return runTimeline 运行时间线
     */
    private Timeline executeRunTimeLine(int preparation, boolean isBatch) {
        if (preparation == 0) {
            if (!autoClickTask.isRunning()) {
                // 使用新线程启动
                new Thread(autoClickTask).start();
            }
            return runTimeline;
        }
        runTimeline = new Timeline();
        AtomicInteger preparationTime = new AtomicInteger(preparation);
        // 创建 Timeline 来实现倒计时
        Timeline finalTimeline = runTimeline;
        runTimeline = new Timeline(new KeyFrame(Duration.seconds(1), _ -> {
            preparationTime.getAndDecrement();
            if (preparationTime.get() > 0) {
                String text = text_cancelTask() + preparationTime + text_run();
                updateMessageLabel(messageFloating, text);
                Label logLabel = isBatch ? listPMCController.log_List : log_Click;
                logLabel.setText(text);
            } else {
                // 停止 Timeline
                finalTimeline.stop();
                if (!autoClickTask.isRunning()) {
                    // 使用新线程启动
                    new Thread(autoClickTask).start();
                }
            }
        }));
        // 设置 Timeline 的循环次数
        runTimeline.setCycleCount(preparation);
        runTimeline.play();
        return runTimeline;
    }

    /**
     * 构建右键菜单
     */
    private void buildContextMenu() {
        // 添加列表右键菜单
        ContextMenu tableMenu = new ContextMenu();
        // 查看详情选项
        buildDetailMenuItem(tableMenu);
        // 修改所选项要点击的图片地址
        buildEditClickImgPathMenu(tableView_Click, tableMenu);
        // 测试点击选项
        MenuItem menuItem = buildClickTestMenuItem(tableMenu);
        // 没有运行必要权限则无法点击
        if (isNativeHookException || noScreenCapturePermission) {
            menuItem.setDisable(true);
        }
        // 移动所选行选项
        buildMoveDataMenu(tableView_Click, tableMenu);
        // 修改点击按键
        buildEditClickKeyMenu(tableView_Click, tableMenu);
        // 修改重试类型
        buildEditRetryTypeMenu(tableView_Click, tableMenu);
        // 插入数据选项
        insertDataMenu(tableMenu);
        // 复制数据选项
        buildCopyDataMenu(tableView_Click, tableMenu, dataNumber_Click, unit_process());
        // 取消选中选项
        buildClearSelectedData(tableView_Click, tableMenu);
        // 删除所选数据选项
        buildDeleteDataMenuItem(tableView_Click, dataNumber_Click, tableMenu, unit_data());
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(tableMenu, tableView_Click);
    }

    /**
     * 查看所选项第一行详情选项
     *
     * @param contextMenu 右键菜单集合
     */
    private void buildDetailMenuItem(ContextMenu contextMenu) {
        MenuItem detailItem = new MenuItem(menu_detailMenu());
        detailItem.setOnAction(_ -> {
            ClickPositionVO selected = tableView_Click.getSelectionModel().getSelectedItems().getFirst();
            if (selected != null) {
                showDetail(selected);
            }
        });
        contextMenu.getItems().add(detailItem);
    }

    /**
     * 执行选中的步骤选项
     *
     * @param contextMenu 右键菜单集合
     */
    private MenuItem buildClickTestMenuItem(ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem(menu_runSelectMenu());
        menuItem.setOnAction(_ -> {
            List<ClickPositionVO> selectedItem = tableView_Click.getSelectionModel().getSelectedItems();
            if (CollectionUtils.isNotEmpty(selectedItem)) {
                try {
                    launchClickTask(selectedItem, 1, false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        contextMenu.getItems().add(menuItem);
        return menuItem;
    }

    /**
     * 插入数据选项
     *
     * @param contextMenu 右键菜单集合
     */
    private void insertDataMenu(ContextMenu contextMenu) {
        Menu menu = new Menu(menu_addDateMenu());
        // 创建二级菜单项
        MenuItem insertUp = new MenuItem(menuItem_insertUp());
        MenuItem insertDown = new MenuItem(menuItem_insertDown());
        MenuItem recordUp = new MenuItem(menuItem_recordUp());
        MenuItem recordDown = new MenuItem(menuItem_recordDown());
        MenuItem insertTop = new MenuItem(menuItem_insertTop());
        MenuItem recordTop = new MenuItem(menuItem_recordTop());
        // 为每个菜单项添加事件处理
        insertUp.setOnAction(_ -> insertDataMenuItem(menuItem_insertUp()));
        insertDown.setOnAction(_ -> insertDataMenuItem(menuItem_insertDown()));
        recordUp.setOnAction(_ -> insertDataMenuItem(menuItem_recordUp()));
        recordDown.setOnAction(_ -> insertDataMenuItem(menuItem_recordDown()));
        insertTop.setOnAction(_ -> insertDataMenuItem(menuItem_insertTop()));
        recordTop.setOnAction(_ -> insertDataMenuItem(menuItem_recordTop()));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(insertUp, insertDown, recordUp, recordDown, insertTop, recordTop);
        contextMenu.getItems().add(menu);
    }

    /**
     * 插入数据选项二级菜单选项
     *
     * @param insertType 数据插入类型
     */
    private void insertDataMenuItem(String insertType) {
        List<ClickPositionVO> selectedItem = tableView_Click.getSelectionModel().getSelectedItems();
        if (CollectionUtils.isNotEmpty(selectedItem)) {
            if (menuItem_insertUp().equals(insertType)) {
                addClick(upAdd);
            } else if (menuItem_insertDown().equals(insertType)) {
                addClick(downAdd);
            } else if (menuItem_recordUp().equals(insertType)) {
                startRecord(upAdd);
            } else if (menuItem_recordDown().equals(insertType)) {
                startRecord(downAdd);
            } else if (menuItem_insertTop().equals(insertType)) {
                addClick(topAdd);
            } else if (menuItem_recordTop().equals(insertType)) {
                startRecord(topAdd);
            }
        }
    }

    /**
     * 更改点击按键
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    private static void buildEditClickKeyMenu(TableView<ClickPositionVO> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu(menu_changeKey());
        // 创建二级菜单项
        MenuItem primary = new MenuItem(mouseButton_primary());
        MenuItem secondary = new MenuItem(mouseButton_secondary());
        MenuItem middle = new MenuItem(mouseButton_middle());
        MenuItem forward = new MenuItem(mouseButton_forward());
        MenuItem back = new MenuItem(mouseButton_back());
        // 为每个菜单项添加事件处理
        primary.setOnAction(_ -> updateClickKeyMenuItem(tableView, mouseButton_primary()));
        secondary.setOnAction(_ -> updateClickKeyMenuItem(tableView, mouseButton_secondary()));
        middle.setOnAction(_ -> updateClickKeyMenuItem(tableView, mouseButton_middle()));
        forward.setOnAction(_ -> updateClickKeyMenuItem(tableView, mouseButton_forward()));
        back.setOnAction(_ -> updateClickKeyMenuItem(tableView, mouseButton_back()));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(primary, secondary, middle, forward, back);
        contextMenu.getItems().add(menu);
    }

    /**
     * 修改点击按键二级菜单选项
     *
     * @param tableView 要添加右键菜单的列表
     * @param clickKey  点击按键
     */
    private static void updateClickKeyMenuItem(TableView<ClickPositionVO> tableView, String clickKey) {
        List<ClickPositionVO> selectedItem = tableView.getSelectionModel().getSelectedItems();
        if (CollectionUtils.isNotEmpty(selectedItem)) {
            selectedItem.forEach(bean -> {
                if (!noKeyCell(bean.getClickTypeEnum())) {
                    bean.setMouseKeyEnum(recordClickTypeMap.getKey(clickKey));
                }
            });
            tableView.refresh();
        }
    }

    /**
     * 更改重试类型
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    private static void buildEditRetryTypeMenu(TableView<ClickPositionVO> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu(menu_changeRetryType());
        // 创建二级菜单项
        MenuItem primary = new MenuItem(retryType_continuously());
        MenuItem secondary = new MenuItem(retryType_click());
        MenuItem middle = new MenuItem(retryType_stop());
        MenuItem forward = new MenuItem(retryType_break());
        // 为每个菜单项添加事件处理
        primary.setOnAction(_ -> updateRetryTypeMenuItem(tableView, retryType_continuously()));
        secondary.setOnAction(_ -> updateRetryTypeMenuItem(tableView, retryType_click()));
        middle.setOnAction(_ -> updateRetryTypeMenuItem(tableView, retryType_stop()));
        forward.setOnAction(_ -> updateRetryTypeMenuItem(tableView, retryType_break()));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(primary, secondary, middle, forward);
        contextMenu.getItems().add(menu);
    }

    /**
     * 更改重试类型二级菜单选项
     *
     * @param tableView 要添加右键菜单的列表
     * @param retryType 操作类型
     */
    private static void updateRetryTypeMenuItem(TableView<ClickPositionVO> tableView, String retryType) {
        List<ClickPositionVO> selectedItem = tableView.getSelectionModel().getSelectedItems();
        if (CollectionUtils.isNotEmpty(selectedItem)) {
            selectedItem.forEach(bean -> bean.setRetryTypeEnum(retryTypeMap.getKey(retryType)));
            tableView.refresh();
        }
    }

    /**
     * 获取点击步骤设置
     *
     * @param tableViewItemSize 列表数据量（生成默认操作名称用）
     * @return clickPositionBean 自动操作步骤类
     */
    private ClickPositionVO getClickSetting(int tableViewItemSize) {
        ClickPositionVO clickPositionVO = createClickPositionVO();
        clickPositionVO.setName(text_step() + (tableViewItemSize + 1) + text_isAdd());
        return clickPositionVO;
    }

    /**
     * 创建一个具有默认值的自动操作步骤类
     *
     * @return clickPositionVO 具有默认值的自动操作步骤类
     */
    private ClickPositionVO createClickPositionVO() {
        // 读取设置页面设置的值
        getSetting();
        FloatingWindowConfig clickConfig = new FloatingWindowConfig();
        FloatingWindowConfig stopConfig = new FloatingWindowConfig();
        try {
            copyAllProperties(clickFloating.getConfig(), clickConfig);
            if (clickConfig.getFindImgTypeEnum() == FindImgTypeEnum.WINDOW.ordinal()) {
                WindowInfo windowInfo = new WindowInfo();
                WindowInfo clickWindowInfo = clickWindowMonitor.getWindowInfo();
                if (clickWindowInfo != null) {
                    copyAllProperties(clickWindowInfo, windowInfo);
                }
                String refresh = settingController.updateClickWindow_Set.isSelected() ? activation : unActivation;
                clickConfig.setAlwaysRefresh(refresh)
                        .setWindowInfo(windowInfo);
            }
            copyAllProperties(stopFloating.getConfig(), stopConfig);
            if (stopConfig.getFindImgTypeEnum() == FindImgTypeEnum.WINDOW.ordinal()) {
                WindowInfo windowInfo = new WindowInfo();
                WindowInfo stopWindowInfo = stopWindowMonitor.getWindowInfo();
                if (stopWindowInfo != null) {
                    copyAllProperties(stopWindowInfo, windowInfo);
                }
                String refresh = settingController.updateStopWindow_Set.isSelected() ? activation : unActivation;
                stopConfig.setAlwaysRefresh(refresh)
                        .setWindowInfo(windowInfo);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        String useRelatively = settingController.useRelatively_Set.isSelected() ? activation : unActivation;
        String noMove = settingController.noMove_Set.isSelected() ? activation : unActivation;
        ClickPositionVO clickPositionVO = new ClickPositionVO();
        clickPositionVO.setTableView(tableView_Click)
                .setSampleInterval(Integer.parseInt(sampleInterval))
                .setRandomClickInterval(randomClickInterval)
                .setClickMatchThreshold(defaultClickOpacity)
                .setStopMatchThreshold(defaultStopOpacity)
                .setRandomTrajectory(randomTrajectory)
                .setStopImgFiles(defaultStopImgFiles)
                .setRandomClickTime(randomClickTime)
                .setRandomWaitTime(randomWaitTime)
                .setClickRetryTimes(clickRetryNum)
                .setClickWindowConfig(clickConfig)
                .setStopWindowConfig(stopConfig)
                .setStopRetryTimes(stopRetryNum)
                .setClickTime(clickTimeOffset)
                .setUseRelative(useRelatively)
                .setRandomClick(randomClick)
                .setRandomTime(randomTime)
                .setRandomX(randomClickX)
                .setRandomY(randomClickY)
                .setNoMove(noMove);
        return clickPositionVO;
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 导出自动流程文件名称文本输入框鼠标悬停提示
        textFieldValueListener(outFileName_Click, tip_autoClickFileName() + defaultPMCFileName());
        // 限制循环次数文本输入框内容
        integerRangeTextField(loopTime_Click, 0, null, tip_loopTime());
        // 限制运行准备时间文本输入框内容
        integerRangeTextField(preparationRunTime_Click, 0, null, tip_preparationRunTime() + defaultPreparationRun);
        // 限制录制准备时间文本输入框内容
        integerRangeTextField(preparationRecordTime_Click, 0, null, tip_preparationRecordTime() + defaultPreparationRecord);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_clickLog(), clickLog_Click);
        addToolTip(tip_Click.getText(), tip_Click);
        addToolTip(tip_runClick(), runClick_Click);
        addToolTip(tip_loopTime(), loopTime_Click);
        addToolTip(tip_learButton(), clearButton_Click);
        addToolTip(tip_addPosition(), addPosition_Click);
        addToolTip(tip_recordClick(), recordClick_Click);
        addToolTip(tip_notOverwrite(), notOverwrite_Click);
        addToolTip(tip_loadFolder_Click(), loadFolder_Click);
        addToolTip(tip_loadAutoClick(), loadAutoClick_Click);
        addToolTip(tip_outAutoClickPath(), addOutPath_Click);
        addToolTip(tip_openDirectory(), openDirectory_Click);
        addToolTip(tip_exportAutoClick(), exportAutoClick_Click);
        addToolTip(tip_autoClickFileName() + defaultPMCFileName(), outFileName_Click);
        addToolTip(tip_preparationRunTime() + defaultPreparationRun, preparationRunTime_Click);
        addToolTip(tip_preparationRecordTime() + defaultPreparationRecord, preparationRecordTime_Click);
    }

    /**
     * 设置文本输入框提示
     */
    private void setPromptText() {
        loopTime_Click.setPromptText(defaultLoopTime);
        outFileName_Click.setPromptText(defaultPMCFileName());
        preparationRunTime_Click.setPromptText(defaultPreparationRun);
        preparationRecordTime_Click.setPromptText(defaultPreparationRecord);
    }

    /**
     * 创建任务参数对象
     *
     * @return 任务参数对象
     */
    private TaskBean<ClickPositionVO> creatTaskBean() {
        TaskBean<ClickPositionVO> taskBean = new TaskBean<>();
        taskBean.setProgressBar(progressBar_Click)
                .setMessageLabel(dataNumber_Click)
                .setTableView(tableView_Click)
                .setDisableNodes(disableNodes);
        return taskBean;
    }

    /**
     * 将自动流程添加到列表中
     *
     * @param clickPositionVOS 自动流程集合
     * @param filePath         要导入的文件路径
     */
    public void addAutoClickPositions(List<? extends ClickPositionVO> clickPositionVOS, String filePath) {
        // 向列表添加数据
        addData(clickPositionVOS, append, tableView_Click, dataNumber_Click, unit_process());
        if (CollectionUtils.isNotEmpty(clickPositionVOS)) {
            updateLabel(log_Click, text_loadSuccess() + filePath);
            Platform.runLater(() -> log_Click.setTextFill(Color.GREEN));
        }
    }

    /**
     * 根据鼠标位置调整 ui
     *
     * @param mousePoint 鼠标位置
     */
    @Override
    public void onMousePositionUpdate(Point mousePoint) {
        Platform.runLater(() -> {
            if (messageFloating != null) {
                Stage floatingStage = messageFloating.getStage();
                int x = (int) mousePoint.getX();
                int y = (int) mousePoint.getY();
                String text = autoClick_nowMousePos() + "X: " + x + " Y: " + y;
                // 运行自动流程时信息浮窗跟随鼠标
                CheckBox mouseFloatingRun = settingController.mouseFloatingRun_Set;
                // 录制自动流程时信息浮窗跟随鼠标
                CheckBox mouseFloatingRecord = settingController.mouseFloatingRecord_Set;
                TextField offsetXTextField = settingController.offsetX_Set;
                int offsetX = setDefaultIntValue(offsetXTextField, defaultOffsetX, 0, null);
                TextField offsetYTextField = settingController.offsetY_Set;
                int offsetY = setDefaultIntValue(offsetYTextField, defaultOffsetY, 0, null);
                CheckBox titleCoordinateSet = settingController.titleCoordinate_Set;
                mousePosition_Click.setText(text);
                if (titleCoordinateSet != null && titleCoordinateSet.isSelected()) {
                    mainStage.setTitle(appName + " - " + text);
                    if (detailStage != null && detailStage.isShowing()) {
                        detailStage.setTitle(detailTitle + " - " + text);
                    }
                } else {
                    mainStage.setTitle(appName);
                }
                if (floatingStage != null && floatingStage.isShowing()) {
                    setPositionText(messageFloating, autoClick_nowMousePos() + "\nX: " + x + " Y: " + y);
                    if ((mouseFloatingRun.isSelected() && runClicking) || findingWindow
                            || (mouseFloatingRecord.isSelected() && recordClicking)) {
                        floatingMove(floatingStage, mousePoint, offsetX, offsetY);
                    }
                }
            }
        });
    }

    /**
     * 开启全局键盘监听
     */
    private void startNativeKeyListener(AutoClickTaskBean taskBean) {
        removeNativeListener(nativeKeyListener);
        // 键盘监听器
        nativeKeyListener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                Platform.runLater(() -> {
                    // 仅在自动操作与录制情况下才监听键盘
                    if (recordClicking || runClicking) {
                        int keyCode = e.getKeyCode();
                        // 处理右 shift
                        keyCode = (keyCode == R_SHIFT) ? NativeKeyEvent.VC_SHIFT : keyCode;
                        // 检测快捷键 esc
                        if (keyCode == cancelKey && !isRobotInput) {
                            stopAllWork(taskBean);
                        }
                    }
                });
            }
        };
        addNativeListener(nativeKeyListener);
    }

    /**
     * 停止所有任务
     */
    private void stopAllWork(AutoClickTaskBean taskBean) {
        if (listener != null) {
            listener.stopRecording();
        }
        // 停止自动操作
        if (autoClickTask != null && autoClickTask.isRunning()) {
            autoClickTask.cancel();
            autoClickTask = null;
        }
        // 停止录制计时
        if (recordTimeline != null) {
            recordTimeline.stop();
            recordTimeline = null;
            Platform.runLater(() -> updateLog(autoClick_recordEnd()));
        }
        // 停止运行计时
        if (runTimeline != null) {
            runTimeline.stop();
            runTimeline = null;
            autoClickTask = null;
            if (taskBean == null) {
                taskBean = new AutoClickTaskBean();
                taskBean.setProgressBar(progressBar_Click)
                        .setMessageLabel(log_Click);
            }
            taskNotSuccess(taskBean, text_taskCancelled());
        }
        // 改变要防重复点击的组件状态
        changeDisableNodes(disableNodes, false);
        hideFloatingWindow(messageFloating);
        // 弹出程序主窗口
        CheckBox showWindowRecord = settingController.showWindowRecord_Set;
        if (showWindowRecord.isSelected()) {
            showStage(mainStage);
        }
        // 移除键盘监听器
        removeNativeListener(nativeKeyListener);
        recordClicking = false;
        runClicking = false;
    }

    /**
     * 更新记录信息
     *
     * @param log 记录信息
     */
    private void updateLog(String log) {
        log_Click.textFillProperty().unbind();
        log_Click.textFillProperty().bind(recordTextColorProperty);
        log_Click.setText(log);
        updateMessageLabel(messageFloating, log);
    }

    /**
     * 初始化统一输入录制监听器
     *
     * @param addType 添加方式
     * @return 统一输入录制监听器
     */
    private UnifiedInputRecordListener initUnifiedInputRecordListener(int addType) {
        // 初始化统一输入录制监听器
        return new UnifiedInputRecordListener(addType, new InputRecordCallback() {

            /**
             * 添加操作步骤到操作列表
             *
             * @param events  操作步骤
             * @param addType 添加方式
             */
            @Override
            public void saveAddEvents(List<? extends ClickPositionVO> events, int addType) {
                addData(events, addType, tableView_Click, dataNumber_Click, unit_process());
            }

            /**
             * 更新记录信息
             *
             * @param log 记录信息
             */
            @Override
            public void updateRecordLog(String log) {
                updateLog(log);
            }

            /**
             * 更新滑轮记录信息
             *
             * @param wheelRotation 滑轮滚动方向
             * @param wheelNum      滑轮滚动次数
             */
            @Override
            public void onWheelRecorded(int wheelRotation, int wheelNum) {
                String wheelDirection = wheelRotation > 0 ? clickType_wheelDown() : clickType_wheelUp();
                String log = text_cancelTask() + text_recordClicking() + "\n" +
                        text_recorded() + wheelDirection + " " + wheelNum + unit_times();
                updateRecordLog(log);
            }

            /**
             * 显示错误信息
             */
            @Override
            public void showError() {
                Alert alert = creatErrorAlert(text_mouseWheelError());
                alert.setTitle(text_mouseWheelErr());
                alert.setHeaderText(text_mouseWheelError());
                showErrLabelText(log_Click, text_taskFailed());
                alert.show();
            }

            /**
             * 创建一个具有默认值的自动操作步骤类
             *
             * @return clickPositionVO 具有默认值的自动操作步骤类
             */
            @Override
            public ClickPositionVO createDefaultClickPosition() {
                return createClickPositionVO();
            }

            /**
             * 获取当前步骤数
             *
             * @return 当前步骤数
             */
            @Override
            public int getCurrentStepCount() {
                return tableView_Click.getItems().size();
            }

            /**
             * 获取是否录制鼠标移动事件
             *
             * @return true-记录
             */
            @Override
            public boolean isRecordMove() {
                return recordMove;
            }

            /**
             * 获取是否录制鼠标拖拽事件
             *
             * @return true-记录
             */
            @Override
            public boolean isRecordDrag() {
                return recordDrag;
            }

            /**
             * 停止所有任务
             */
            @Override
            public void stopWorkAll() {
                stopAllWork(null);
            }

            /**
             * 获取是否录制鼠标滚轮事件
             *
             * @return true-记录
             */
            @Override
            public boolean isRecordMouseWheel() {
                return recordMouseWheel;
            }

            /**
             * 获取是否录制键盘事件
             *
             * @return true-记录
             */
            @Override
            public boolean isRecordKeyboard() {
                return recordKeyboard;
            }

            /**
             * 获取是否录制鼠标点击事件
             *
             * @return true-记录
             */
            @Override
            public boolean isRecordMouseClick() {
                return recordMouseClick;
            }

        });
    }

    /**
     * 初始化全局录制与运行快捷键监听器
     *
     * @return 全局录制与运行快捷键监听器
     */
    private UnifiedInputRecordListener initShortcutListener() {
        // 创建组合键监听器
        return new UnifiedInputRecordListener(noAdd, new InputRecordCallback() {

            /**
             * 用来临时保存组合键的临时操作步骤
             */
            private ClickPositionVO combinationBean;

            /**
             * 组合键轨迹
             */
            private final List<TrajectoryPointBean> trajectory = new CopyOnWriteArrayList<>();

            /**
             * 创建一个具有默认值的自动操作步骤类
             *
             * @return clickPositionVO 具有默认值的自动操作步骤类
             */
            @Override
            public ClickPositionVO createDefaultClickPosition() {
                combinationBean = new ClickPositionVO();
                return combinationBean;
            }

            /**
             * 保存操作步骤
             *
             * @param events  操作步骤
             * @param addType 添加方式
             */
            @Override
            public void saveAddEvents(List<? extends ClickPositionVO> events, int addType) {
                // 这里不添加到表格，只用于组合键监听
                if (combinationBean != null && isFree()) {
                    // 所有按键松开，组合键录制完成
                    handleCombinationComplete();
                }
            }

            /**
             * 获取当前步骤数
             *
             * @return 临时步骤数
             */
            @Override
            public int getCurrentStepCount() {
                return -1;
            }

            /**
             * 获取是否录制鼠标移动事件
             *
             * @return 不记录
             */
            @Override
            public boolean isRecordMove() {
                return false;
            }

            /**
             * 获取是否录制鼠标拖拽事件
             *
             * @return 不记录
             */
            @Override
            public boolean isRecordDrag() {
                return false;
            }

            /**
             * 更新滑轮记录信息
             *
             * @param wheelRotation 滑轮滚动方向
             * @param wheelNum      滑轮滚动次数
             */
            @Override
            public void onWheelRecorded(int wheelRotation, int wheelNum) {
            }

            /**
             * 更新记录信息
             *
             * @param log 记录信息
             */
            @Override
            public void updateRecordLog(String log) {
            }

            /**
             * 停止所有任务
             */
            @Override
            public void stopWorkAll() {
                removeNativeListener(listener);
            }

            /**
             * 显示错误信息
             */
            @Override
            public void showError() {
            }

            /**
             * 获取是否录制鼠标滚轮事件
             *
             * @return 不记录
             */
            @Override
            public boolean isRecordMouseWheel() {
                return false;
            }

            /**
             * 获取是否录制键盘事件
             *
             * @return 记录
             */
            @Override
            public boolean isRecordKeyboard() {
                return true;
            }

            /**
             * 获取是否录制鼠标点击事件
             *
             * @return 不记录
             */
            @Override
            public boolean isRecordMouseClick() {
                return false;
            }

            /**
             * 处理组合键录制完成
             */
            private void handleCombinationComplete() {
                combinationBean.setClickTypeEnum(ClickTypeEnum.COMBINATIONS.ordinal());
                trajectory.clear();
                trajectory.addAll(combinationBean.getMoveTrajectory());
                Platform.runLater(() -> {
                    if (combinationBean != null) {
                        Set<Integer> keySet = new LinkedHashSet<>();
                        if (CollectionUtils.isNotEmpty(trajectory)) {
                            for (TrajectoryPointBean t : trajectory) {
                                List<Integer> pressKeyboardKeys = t.getPressKeyboardKeys();
                                if (CollectionUtils.isNotEmpty(pressKeyboardKeys)) {
                                    keySet.addAll(pressKeyboardKeys);
                                }
                            }
                        }
                        // 检测录制快捷键
                        if (CollectionUtils.isNotEmpty(recordKeys)) {
                            if (recordKeys.toString().equals(keySet.toString())) {
                                if (isSonOpening) {
                                    new MessageBubble(text_sonOpenRecordErr(), 1);
                                } else if (settingController.isFloatingOpen()) {
                                    new MessageBubble(text_floatingOpenRecordErr(), 1);
                                } else {
                                    recordClick();
                                    mainController.tabPane.getSelectionModel().select(mainController.autoClickTab);
                                }
                            }
                        }
                        // 检测运行快捷键
                        if (CollectionUtils.isNotEmpty(runKeys)) {
                            if (runKeys.toString().equals(keySet.toString())) {
                                if (isSonOpening) {
                                    new MessageBubble(text_sonOpenRunErr(), 1);
                                } else if (settingController.isFloatingOpen()) {
                                    new MessageBubble(text_floatingOpenRunErr(), 1);
                                } else {
                                    ObservableList<ClickPositionVO> items = tableView_Click.getItems();
                                    if (CollectionUtils.isNotEmpty(items)) {
                                        try {
                                            runClick();
                                            mainController.tabPane.getSelectionModel().select(mainController.autoClickTab);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        new MessageBubble(text_noDateNum(), 1);
                                    }
                                }
                            }
                        }
                    }
                });
            }

        });
    }


    /**
     * 开始统一录制
     *
     * @param addType 添加方式
     */
    private void startUnifiedRecording(int addType) {
        removeNativeListener(listener);
        listener = initUnifiedInputRecordListener(addType);
        // 读取设置页面设置的值
        getSetting();
        // 开始录制
        listener.startRecording();
    }

    /**
     * 开始录制
     *
     * @param addType 添加类型
     */
    private void startRecord(int addType) {
        if (isFree()) {
            if (cancelKey == noKeyboard) {
                throw new RuntimeException(text_noCancelKey());
            }
            // 标记为正在录制
            recordClicking = true;
            // 改变要防重复点击的组件状态
            changeNodesDisable(disableNodes, true);
            if (clickFloating.getConfig().getFindImgTypeEnum() == FindImgTypeEnum.WINDOW.ordinal()) {
                clickWindowMonitor.updateWindowInfo();
            }
            // 获取准备时间值
            int preparationTimeValue = setDefaultIntValue(preparationRecordTime_Click,
                    Integer.parseInt(defaultPreparationRecord), 0, null);
            // 隐藏主窗口
            CheckBox hideWindowRecord = settingController.hideWindowRecord_Set;
            if (hideWindowRecord.isSelected()) {
                mainStage.setIconified(true);
            }
            // 开启键盘监听
            startNativeKeyListener(null);
            // 设置浮窗文本显示准备时间
            AtomicReference<String> text = new AtomicReference<>(text_cancelTask()
                    + preparationTimeValue + text_preparation());
            updateMessageLabel(messageFloating, text.get());
            log_Click.textFillProperty().unbind();
            log_Click.textFillProperty().bind(recordTextColorProperty);
            log_Click.setText(text.get());
            // 显示浮窗
            try {
                showFloatingWindow(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            recordTimeline = new Timeline();
            if (preparationTimeValue == 0) {
                // 开启鼠标监听
                startUnifiedRecording(addType);
                // 更新浮窗文本
                text.set(text_cancelTask() + text_recordClicking());
                updateMessageLabel(messageFloating, text.get());
                log_Click.setText(text.get());
            } else {
                AtomicInteger preparationTime = new AtomicInteger(preparationTimeValue);
                // 创建 Timeline 来实现倒计时
                Timeline finalTimeline = recordTimeline;
                recordTimeline = new Timeline(new KeyFrame(Duration.seconds(1), _ -> {
                    preparationTime.getAndDecrement();
                    if (preparationTime.get() > 0) {
                        text.set(text_cancelTask() + preparationTime + text_preparation());
                    } else {
                        // 开启鼠标监听
                        startUnifiedRecording(addType);
                        // 停止 Timeline
                        finalTimeline.stop();
                        // 更新浮窗文本
                        text.set(text_cancelTask() + text_recordClicking());
                    }
                    updateMessageLabel(messageFloating, text.get());
                    log_Click.setText(text.get());
                }));
                // 设置 Timeline 的循环次数
                recordTimeline.setCycleCount(preparationTimeValue);
                // 启动 Timeline
                recordTimeline.play();
            }
        }
    }

    /**
     * 获取操作设置并添加到列表中
     *
     * @param addType 添加类型
     */
    private void addClick(int addType) {
        if (autoClickTask == null && !recordClicking) {
            ObservableList<ClickPositionVO> tableViewItems = tableView_Click.getItems();
            // 获取点击步骤设置
            ClickPositionVO clickPositionVO = getClickSetting(tableViewItems.size());
            List<ClickPositionVO> clickPositionVOS = new ArrayList<>();
            clickPositionVOS.add(clickPositionVO);
            // 向列表添加数据
            addData(clickPositionVOS, addType, tableView_Click, dataNumber_Click, unit_process());
            // 初始化信息栏
            updateLabel(log_Click, "");
            // 显示详情
            showDetail(clickPositionVO);
        }
    }

    /**
     * 启动加载自动操作文件任务
     *
     * @param files 要加载的文件
     */
    public void startLoadPMCTask(List<? extends File> files) {
        TaskBean<ClickPositionVO> taskBean = creatTaskBean();
        loadPMCFilsTask = loadPMCFils(taskBean, files);
        bindingTaskNode(loadPMCFilsTask, taskBean);
        loadPMCFilsTask.setOnSucceeded(_ -> {
            taskUnbind(taskBean);
            PMCLoadResult value = loadPMCFilsTask.getValue();
            String lastPMCPath = value.lastPMCPath();
            List<ClickPositionVO> clickPositionVOS = value.clickPositionList();
            addAutoClickPositions(clickPositionVOS, lastPMCPath);
            mainController.tabPane.getSelectionModel().select(mainController.autoClickTab);
            loadPMCFilsTask = null;
        });
        loadPMCFilsTask.setOnFailed(event -> {
            taskUnbind(taskBean);
            taskNotSuccess(taskBean, text_taskFailed());
            loadPMCFilsTask = null;
            throw new RuntimeException(event.getSource().getException());
        });
        Thread.ofVirtual()
                .name("loadPMCFilsTask-vThread" + tabId)
                .start(loadPMCFilsTask);
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(clickLog_Click);
        disableNodes.add(runClick_Click);
        disableNodes.add(tableView_Click);
        disableNodes.add(recordClick_Click);
        disableNodes.add(addPosition_Click);
        disableNodes.add(clearButton_Click);
        disableNodes.add(loadAutoClick_Click);
        disableNodes.add(exportAutoClick_Click);
        Node aboutTab = mainScene.lookup("#aboutTab");
        disableNodes.add(aboutTab);
        Node listPMCTab = mainScene.lookup("#listPMCTab");
        disableNodes.add(listPMCTab);
        Node settingTab = mainScene.lookup("#settingTab");
        disableNodes.add(settingTab);
        Node autoClickTab = mainScene.lookup("#autoClickTab");
        disableNodes.add(autoClickTab);
        Node timedStartTab = mainScene.lookup("#timedStartTab");
        disableNodes.add(timedStartTab);
    }

    /**
     * 禁用需要辅助控制权限的组件
     */
    private void setNativeHookExceptionLog() {
        isNativeHookException = true;
        setNodeDisable(runClick_Click, true);
        setNodeDisable(recordClick_Click, true);
        adaption();
        String errorMessage = appName + autoClick_noPermissions();
        if (isMac) {
            errorMessage = tip_NativeHookException();
        }
        err_Click.setText(errorMessage);
        err_Click.setTooltip(creatTooltip(tip_NativeHookException()));
    }

    /**
     * 禁用需要截屏相关权限的组件
     */
    private void setNoScreenCapturePermissionLog() {
        noScreenCapturePermission = true;
        setNodeDisable(runClick_Click, true);
        adaption();
        err_Click.setText(tip_noScreenCapturePermission());
        err_Click.setTooltip(creatTooltip(tip_noScreenCapturePermission()));
    }

    /**
     * 页面加载完毕后的执行逻辑
     *
     * @param event 设置页加载完成事件
     */
    private void settingsLoaded(SettingsLoadedEvent event) {
        // 设置快捷键提示
        setShortcutText();
        // 设置要防重复点击的组件
        setDisableNodes();
        shortcutsListener = initShortcutListener();
        shortcutsListener.startRecording();
        // 运行定时任务
        if (StringUtils.isNotBlank(loadPMCPath)) {
            TaskBean<ClickPositionVO> taskBean = creatTaskBean();
            loadedPMCTask = buildPMC(taskBean, new File(loadPMCPath));
            loadedPMCTask.setOnSucceeded(_ -> {
                taskUnbind(taskBean);
                List<ClickPositionVO> clickPositionVOS = loadedPMCTask.getValue();
                addAutoClickPositions(clickPositionVOS, loadPMCPath);
                loadedPMCTask = null;
                try {
                    // 运行自动操作
                    if (runPMCFile) {
                        runClick();
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                // 清空启动参数
                clearArgs();
            });
            loadedPMCTask.setOnFailed(e -> {
                taskNotSuccess(taskBean, text_taskFailed());
                loadedPMCTask = null;
                throw new RuntimeException(e.getSource().getException());
            });
            Thread.ofVirtual()
                    .name("loadedPMCTask-vThread" + tabId)
                    .start(loadedPMCTask);
        }
        // 禁用需要辅助控制权限的组件
        if (isNativeHookException) {
            Button saveButton = settingController.messageRegion_Set;
            setNodeDisable(saveButton, true);
        }
    }

    /**
     * 设置快捷键提示
     */
    private void setShortcutText() {
        if (cancelKey == noKeyboard) {
            cancelTip_Click.setText(text_noCancelKey());
        } else {
            cancelTip_Click.setText(text_cancelTip_Click());
        }
        String recordToolTip = tip_recordClick() + "\n" + text_shortcut() + getKeysText(recordKeys);
        addToolTip(recordToolTip, recordClick_Click);
        String runToolTip = tip_runClick() + "\n" + text_shortcut() + getKeysText(runKeys);
        addToolTip(runToolTip, runClick_Click);
    }

    /**
     * 获取选择的文件
     *
     * @throws IOException 配置文件保存异常
     */
    private void getSelectFile(List<? extends File> selectedFile) throws IOException {
        if (CollectionUtils.isNotEmpty(selectedFile)) {
            inFilePath = selectedFile.getFirst().getPath();
            updateProperties(configFile_Click, key_inFilePath, new File(inFilePath).getParent());
            startLoadPMCTask(selectedFile);
        }
    }

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() throws IOException {
        // 读取配置文件
        getProperties();
        Platform.runLater(() -> {
            try {
                // 组件自适应宽高
                adaption();
                // 初始化浮窗
                initFloatingWindow();
                // 设置鼠标悬停提示
                setToolTip();
                // 设置文本输入框提示
                setPromptText();
                // 给输入框添加内容变化监听
                textFieldChangeListener();
                // 设置初始配置值为上次配置值
                setLastConfig();
                // 检查 macOS 屏幕录制权限
                if (!hasScreenCapturePermission()) {
                    // 禁用需要截屏相关权限的组件
                    setNoScreenCapturePermissionLog();
                }
                // 注册全局输入监听器
                GlobalScreen.registerNativeHook();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NativeHookException e) {
                setNativeHookExceptionLog();
            }
            if (StringUtils.isBlank(err_Click.getText())) {
                logHBox_Click.getChildren().remove(err_Click);
            }
            // 获取鼠标坐标监听器
            MousePositionListener.getInstance().addListener(this);
            // 自动填充 JavaFX 表格
            autoBuildTableViewData(tableView_Click, ClickPositionVO.class, tabId, index_Click);
            // 监听列表数据变化
            tableView_Click.getItems().addListener((ListChangeListener<ClickPositionVO>) _ ->
                    updateLabel(log_Click, ""));
            // 表格设置为可编辑
            makeCellCanEdit();
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Click);
            // 构建右键菜单
            buildContextMenu();
            // 等待设置加载完毕
            EventBus.subscribe(SettingsLoadedEvent.class, this::settingsLoaded);
        });
    }

    /**
     * 运行自动点击按钮
     *
     * @throws Exception 列表中没有要执行的操作
     */
    @FXML
    public void runClick() throws Exception {
        ObservableList<ClickPositionVO> tableViewItems = tableView_Click.getItems();
        if (CollectionUtils.isEmpty(tableViewItems)) {
            throw new RuntimeException(text_noAutoClickToRun());
        }
        int loopTimes = setDefaultIntValue(loopTime_Click, 1, 0, null);
        // 启动自动操作流程
        launchClickTask(tableViewItems, loopTimes, false);
    }

    /**
     * 清空操作列表按钮
     */
    @FXML
    public void removeAll() {
        if (isFree()) {
            tableView_Click.getItems().stream().parallel().forEach(ClickPositionVO::clearResources);
            removeTableViewData(tableView_Click, dataNumber_Click);
        }
    }

    /**
     * 添加点击步骤
     */
    @FXML
    private void addPosition() {
        tableView_Click.getSelectionModel().clearSelection();
        addClick(append);
    }

    /**
     * 导入操作流程按钮
     *
     * @param actionEvent 点击事件
     * @throws IOException 配置文件读取异常、配置文件保存异常、页面加载失败
     */
    @FXML
    public void loadAutoClick(ActionEvent actionEvent) throws IOException {
        if (isFree()) {
            // 读取配置文件
            getProperties();
            if (loadFolder_Click.isSelected()) {
                List<String> extensionFilter = new ArrayList<>();
                extensionFilter.add(PMC);
                extensionFilter.add(extension_folder());
                FileChooserConfig fileConfig = new FileChooserConfig();
                fileConfig.setExtensionFilter(extensionFilter)
                        .setTitle(text_selectDirectory())
                        .setConfigPath(configFile_Click)
                        .setPathKey(key_inFilePath)
                        .setShowDirectory(search_onlyDirectory())
                        .setShowHideFile(hide_noHideFile())
                        .setPath(inFilePath);
                FileChooserController controller = chooserFiles(fileConfig);
                // 设置回调
                controller.setFileChooserCallback(this::getSelectFile);
            } else {
                isSonOpening = true;
                Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
                FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(appName, allPMC);
                List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(filter));
                List<File> selectedFile = creatFilesChooser(window, inFilePath, extensionFilters, text_selectAutoFile());
                isSonOpening = false;
                getSelectFile(selectedFile);
            }
        }
    }

    /**
     * 导出操作流程按钮
     */
    @FXML
    public void exportAutoClick() {
        if (isFree()) {
            List<ClickPositionVO> tableViewItems = new ArrayList<>(tableView_Click.getItems());
            if (CollectionUtils.isEmpty(tableViewItems)) {
                throw new RuntimeException(text_noAutoClickList());
            }
            String outFilePath = outPath_Click.getText();
            if (StringUtils.isBlank(outFilePath)) {
                throw new RuntimeException(text_outPathNull());
            }
            TaskBean<ClickPositionVO> taskBean = creatTaskBean();
            taskBean.setMessageLabel(log_Click)
                    .setBeanList(tableViewItems);
            String fileName = setDefaultFileName(outFileName_Click, defaultPMCFileName());
            exportPMCTask = exportPMC(taskBean, fileName, outFilePath, notOverwrite_Click.isSelected());
            bindingTaskNode(exportPMCTask, taskBean);
            exportPMCTask.setOnSucceeded(_ -> {
                taskUnbind(taskBean);
                String path = exportPMCTask.getValue();
                log_Click.setTextFill(Color.GREEN);
                if (openDirectory_Click.isSelected()) {
                    openDirectory(path);
                }
                exportPMCTask = null;
            });
            exportPMCTask.setOnFailed(event -> {
                exportPMCTask = null;
                taskNotSuccess(taskBean, text_taskFailed());
                throw new RuntimeException(event.getSource().getException());
            });
            Thread.ofVirtual()
                    .name("exportPMCTask-vThread" + tabId)
                    .start(exportPMCTask);
        }
    }

    /**
     * 设置操作流程导出文件夹地址按钮
     *
     * @param actionEvent 点击事件
     * @throws IOException 配置文件读取异常、配置文件保存异常
     */
    @FXML
    private void addOutPath(ActionEvent actionEvent) throws IOException {
        // 读取配置文件
        getProperties();
        isSonOpening = true;
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        String outFilePath = outPath_Click.getText();
        File selectedFile = creatDirectoryChooser(window, outFilePath, text_selectDirectory());
        isSonOpening = false;
        if (selectedFile != null) {
            // 更新所选文件路径显示
            updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Click, configFile_Click);
        }
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        startLoadPMCTask(files);
        dragEvent.setDropCompleted(true);
        dragEvent.consume();
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void acceptDrop(DragEvent dragEvent) {
        if (isFree()) {
            // 接受拖放
            dragEvent.acceptTransferModes(TransferMode.COPY);
            dragEvent.consume();
        }
    }

    /**
     * 录制自动操作按钮
     */
    @FXML
    private void recordClick() {
        startRecord(append);
    }

    /**
     * 查看运行记录
     *
     * @throws IOException 页面加载失败
     */
    @FXML
    private void clickLog() throws IOException {
        URL fxmlLocation = getClass().getResource(resourcePath + "fxml/ClickLog-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation, bundle);
        Parent root = loader.load();
        ClickLogController controller = loader.getController();
        controller.initData(clickLogs);
        controller.setRefreshCallback(() -> {
            List<ClickLogBean> logs = controller.getClickLogs();
            if (CollectionUtils.isEmpty(logs)) {
                clickLogs.clear();
            }
        });
        Stage detailStage = new Stage();
        Scene scene = new Scene(root, logWidth, logHeight);
        detailStage.setScene(scene);
        detailStage.setTitle(clickLog_title());
        detailStage.initModality(Modality.APPLICATION_MODAL);
        setWindowLogo(detailStage, logoPath);
        // 监听窗口面板宽度变化
        detailStage.widthProperty().addListener((_, _, _) ->
                Platform.runLater(controller::adaption));
        // 监听窗口面板高度变化
        detailStage.heightProperty().addListener((_, _, _) ->
                Platform.runLater(controller::adaption));
        // 设置 css 样式
        setWindowCss(scene, stylesCss);
        detailStage.show();
        isSonOpening = true;
    }

}
