package priv.koishi.pmc.Controller;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Bean.VO.ImgFileVO;
import priv.koishi.pmc.Event.EventBus;
import priv.koishi.pmc.Event.SettingsLoadedEvent;
import priv.koishi.pmc.Finals.Enum.FindImgTypeEnum;
import priv.koishi.pmc.Finals.Enum.LanguageEnum;
import priv.koishi.pmc.Finals.Enum.ThemeEnum;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor;
import priv.koishi.pmc.Listener.MousePositionListener;
import priv.koishi.pmc.Listener.MousePositionUpdater;
import priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindowDescriptor;
import priv.koishi.pmc.UI.CustomMessageBubble.MessageBubble;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.*;
import java.util.List;

import static priv.koishi.pmc.Controller.AutoClickController.stopImgSelectPath;
import static priv.koishi.pmc.Controller.MainController.autoClickController;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.CommonFinals.isRunningFromIDEA;
import static priv.koishi.pmc.Finals.CommonKeys.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor.creatDefaultWindowInfoHandler;
import static priv.koishi.pmc.JnaNative.PermissionChecker.MacChecker.hasAutomationPermission;
import static priv.koishi.pmc.MainApplication.*;
import static priv.koishi.pmc.Service.ImageRecognitionService.screenHeight;
import static priv.koishi.pmc.Service.ImageRecognitionService.screenWidth;
import static priv.koishi.pmc.Service.PMCFileService.loadImg;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.*;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.ListenerUtils.integerRangeTextField;
import static priv.koishi.pmc.Utils.ListenerUtils.integerSliderValueListener;
import static priv.koishi.pmc.Utils.NodeDisableUtils.changeDisableNodes;
import static priv.koishi.pmc.Utils.NodeDisableUtils.setNodeDisable;
import static priv.koishi.pmc.Utils.TableViewUtils.*;
import static priv.koishi.pmc.Utils.TaskUtils.bindingTaskNode;
import static priv.koishi.pmc.Utils.TaskUtils.taskUnbind;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;
import static priv.koishi.pmc.Utils.ToolTipUtils.addValueToolTip;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 设置页面控制器
 *
 * @author KOISHI
 * Date:2024-11-12
 * Time:下午4:51
 */
public class SettingController extends RootController implements MousePositionUpdater {

    /**
     * 页面标识符
     */
    private final String tabId = "_Set";

    /**
     * 页面加载完毕标志（true 加载完毕，false 未加载完毕）
     */
    private boolean initializedFinished;

    /**
     * 无自动化权限（true-无权限 false-有权限）
     */
    public static boolean noAutomationPermission;

    /**
     * 基础要防重复点击的组件
     */
    private final List<Node> baseDisableNodes = new ArrayList<>();

    /**
     * 终止操作识别区域设置要防重复点击的组件
     */
    private final List<Node> stopDisableNodes = new ArrayList<>();

    /**
     * 目标图像识别区域设置要防重复点击的组件
     */
    private final List<Node> clickDisableNodes = new ArrayList<>();

    /**
     * 窗口信息设置要防重复点击的组件
     */
    private final List<Node> windowInfoDisableNodes = new ArrayList<>();

    /**
     * 窗口进程地址
     */
    private String clickWindowPath, stopWindowPath;

    /**
     * 浮窗设置
     */
    public static FloatingWindowDescriptor clickFloating, stopFloating, massageFloating, windowInfoFloating;

    /**
     * 窗口信息获取器
     */
    public static WindowMonitor clickWindowMonitor, stopWindowMonitor;

    @FXML
    public AnchorPane anchorPane_Set;

    @FXML
    public HBox clickRegionHBox_Set, stopRegionHBox_Set, stopWindowInfoHBox_Set, clickWindowInfoHBox_Set,
            stopRegionInfoHBox_Set, clickRegionInfoHBox_Set, noPermissionHBox_Set;

    @FXML
    public ProgressBar progressBar_Set;

    @FXML
    public ColorPicker colorPicker_Set;

    @FXML
    public Slider opacity_Set, clickOpacity_Set, stopOpacity_Set;

    @FXML
    public ChoiceBox<String> nextGcType_Set, language_Set, clickFindImgType_Set, stopFindImgType_Set, theme_Set;

    @FXML
    public Button massageRegion_Set, stopImgBtn_Set, removeAll_Set, reLaunch_Set, clickRegion_Set, stopRegion_Set,
            clickWindow_Set, stopWindow_Set;

    @FXML
    public Label dataNumber_Set, tip_Set, runningMemory_Set, systemMemory_Set, clickWindowInfo_Set, stopWindowInfo_Set,
            gcType_Set, thisPath_Set, noPermission_Set;

    @FXML
    public TextField floatingDistance_Set, offsetX_Set, offsetY_Set, clickRetryNum_Set, stopRetryNum_Set, overtime_Set,
            retrySecond_Set, sampleInterval_Set, randomClickX_Set, randomClickY_Set, clickTimeOffset_Set, maxLogNum_Set,
            randomTimeOffset_Set, nextRunMemory_Set, findWindowWait_Set;

    @FXML
    public CheckBox lastTab_Set, fullWindow_Set, loadAutoClick_Set, hideWindowRun_Set, showWindowRun_Set, dragLog_Set,
            autoSave_Set, recordDrag_Set, recordMove_Set, randomClick_Set, randomTrajectory_Set, clickAllRegion_Set,
            randomClickInterval_Set, randomWaitTime_Set, clickLog_Set, moveLog_Set, firstClick_Set, clickImgLog_Set,
            hideWindowRecord_Set, showWindowRecord_Set, floatingRun_Set, floatingRecord_Set, randomClickTime_Set,
            mouseFloatingRun_Set, mouseFloatingRecord_Set, mouseFloating_Set, maxWindow_Set, remindClickSave_Set,
            stopImgLog_Set, imgLog_Set, waitLog_Set, remindTaskSave_Set, stopAllRegion_Set, titleCoordinate_Set,
            updateStopWindow_Set, updateClickWindow_Set, useRelatively_Set, openFileLog_Set, runScriptLog_Set,
            openUrlLog_Set, mouseWheelLog_Set, keyboardLog_Set, recordMouseWheel_Set, recordKeyboard_Set, noMove_Set,
            recordMouseClick_Set;

    @FXML
    public TableView<ImgFileVO> tableView_Set;

    @FXML
    public TableColumn<ImgFileVO, Integer> index_Set;

    @FXML
    public TableColumn<ImgFileVO, ImageView> thumb_Set;

    @FXML
    public TableColumn<ImgFileVO, String> name_Set, type_Set, path_Set;

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tableView_Set.setPrefHeight(stageHeight * 0.3);
        // 设置组件宽度
        double tableWidth = mainStage.getWidth() * 0.9;
        tableView_Set.setMaxWidth(tableWidth);
        tableView_Set.setPrefWidth(tableWidth);
        bindPrefWidthProperty();
    }

    /**
     * 设置 JavaFX 单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_Set.prefWidthProperty().bind(tableView_Set.widthProperty().multiply(0.1));
        thumb_Set.prefWidthProperty().bind(tableView_Set.widthProperty().multiply(0.2));
        name_Set.prefWidthProperty().bind(tableView_Set.widthProperty().multiply(0.2));
        path_Set.prefWidthProperty().bind(tableView_Set.widthProperty().multiply(0.4));
        type_Set.prefWidthProperty().bind(tableView_Set.widthProperty().multiply(0.1));
    }

    /**
     * 保存设置功能最后设置
     *
     * @throws IOException 配置文件保存异常
     */
    public void saveLastConfig() throws IOException {
        if (anchorPane_Set != null) {
            InputStream input = checkRunningInputStream(configFile_Click);
            Properties prop = new Properties();
            prop.load(input);
            prop.put(key_offsetX, offsetX_Set.getText());
            prop.put(key_offsetY, offsetY_Set.getText());
            prop.put(key_overtime, overtime_Set.getText());
            prop.put(key_maxLogNum, maxLogNum_Set.getText());
            prop.put(key_margin, floatingDistance_Set.getText());
            prop.put(key_retrySecond, retrySecond_Set.getText());
            prop.put(key_randomClickX, randomClickX_Set.getText());
            prop.put(key_randomClickY, randomClickY_Set.getText());
            prop.put(key_findWindowWait, findWindowWait_Set.getText());
            prop.put(key_sampleInterval, sampleInterval_Set.getText());
            prop.put(key_clickTimeOffset, clickTimeOffset_Set.getText());
            prop.put(key_defaultStopRetryNum, stopRetryNum_Set.getText());
            prop.put(key_opacity, String.valueOf(opacity_Set.getValue()));
            prop.put(key_randomTimeOffset, randomTimeOffset_Set.getText());
            prop.put(key_defaultClickRetryNum, clickRetryNum_Set.getText());
            prop.put(key_stopOpacity, String.valueOf(stopOpacity_Set.getValue()));
            prop.put(key_clickOpacity, String.valueOf(clickOpacity_Set.getValue()));
            prop.put(key_stopFindImgType, findImgTypeMap.getKey(stopFindImgType_Set.getValue()).toString());
            prop.put(key_clickFindImgType, findImgTypeMap.getKey(clickFindImgType_Set.getValue()).toString());
            List<ImgFileVO> list = tableView_Set.getItems();
            int index = 0;
            while (index < 10) {
                prop.remove(key_defaultStopImg + index);
                index++;
            }
            for (int i = 0; i < list.size(); i++) {
                ImgFileVO bean = list.get(i);
                prop.put(key_defaultStopImg + i, bean.getPath());
            }
            if (clickWindowMonitor != null) {
                WindowInfo windowInfo = clickWindowMonitor.getWindowInfo();
                String processPath = "";
                if (windowInfo != null) {
                    processPath = windowInfo.getProcessPath();
                }
                prop.put(key_clickWindowPath, processPath);
            }
            if (stopWindowMonitor != null) {
                WindowInfo windowInfo = stopWindowMonitor.getWindowInfo();
                String processPath = "";
                if (windowInfo != null) {
                    processPath = windowInfo.getProcessPath();
                }
                prop.put(key_stopWindowPath, processPath);
            }
            OutputStream output = checkRunningOutputStream(configFile_Click);
            prop.store(output, null);
            input.close();
            output.close();
            // 保存 JVM 参数设置
            saveJVMConfig();
            // 保存语言设置
            updateProperties(configFile, key_language, language_Set.getValue());
            // 更新外观设置
            int them = themeMap.getKey(theme_Set.getValue());
            updateProperties(configFile, key_theme, String.valueOf(them));
        }
    }

    /**
     * 保存 JVM 参数设置
     *
     * @throws IOException cfg 配置文件读取或写入异常
     */
    private void saveJVMConfig() throws IOException {
        String nextRunMemoryValue = nextRunMemory_Set.getText();
        String XmxValue = StringUtils.isBlank(nextRunMemoryValue) ? "" : nextRunMemoryValue + G;
        String nextGcTypeValue = nextGcType_Set.getValue();
        Map<String, String> options = new HashMap<>();
        options.put(Xmx, XmxValue);
        options.put(XX, nextGcTypeValue);
        // 更新 cfg 文件中 jvm 参数设置
        setJavaOptionValue(options);
    }

    /**
     * 创建浮窗描初始配置
     *
     * @return 浮窗描初始配置
     */
    private FloatingWindowDescriptor createFloatingWindowDescriptor() {
        return new FloatingWindowDescriptor()
                .setDisableNodes(new ArrayList<>(baseDisableNodes))
                .setShowButtonText(findImgSet_showRegion())
                .setHideButtonText(findImgSet_saveRegion())
                .setHideButtonToolTip(tip_saveFloating())
                .setTextFill(colorPicker_Set.getValue())
                .setShowButtonToolTip(tip_showRegion())
                .setMassage(text_saveFindImgConfig())
                .setConfigFile(configFile_Click);
    }

    /**
     * 初始化浮窗
     *
     * @throws IOException 配置文件读取异常
     */
    private void initFloatingWindow() throws IOException {
        clickFloating = createFloatingWindowDescriptor()
                .setName(floatingName_click())
                .setHeightKey(key_clickHeight)
                .setWidthKey(key_clickWidth)
                .setButton(clickRegion_Set)
                .setXKey(key_clickX)
                .setYKey(key_clickY);
        clickFloating.getDisableNodes().add(clickFindImgType_Set);
        stopFloating = createFloatingWindowDescriptor()
                .setName(floatingName_stop())
                .setHeightKey(key_stopHeight)
                .setWidthKey(key_stopWidth)
                .setButton(stopRegion_Set)
                .setXKey(key_stopX)
                .setYKey(key_stopY);
        stopFloating.getDisableNodes().add(stopFindImgType_Set);
        int margin = setDefaultIntValue(floatingDistance_Set, 0, 0, null);
        String hideButtonText = mouseFloating_Set.isSelected() ? text_closeFloating() : text_saveCloseFloating();
        String hideButtonToolTip = mouseFloating_Set.isSelected() ? tip_closeFloating() : tip_saveFloating();
        massageFloating = createFloatingWindowDescriptor()
                .setShowButtonToolTip(tip_massageRegion())
                .setHideButtonToolTip(hideButtonToolTip)
                .setShowButtonText(text_showFloating())
                .setOpacity(opacity_Set.getValue())
                .setHideButtonText(hideButtonText)
                .setName(floatingName_massage())
                .setHeightKey(key_massageHeight)
                .setWidthKey(key_massageWidth)
                .setButton(massageRegion_Set)
                .setXKey(key_massageX)
                .setYKey(key_massageY)
                .setMargin(margin);
        windowInfoFloating = new FloatingWindowDescriptor()
                .setConfig(new FloatingWindowConfig())
                .setName(findImgSet_tagetWindow())
                .setEnableResize(false)
                .setAddCloseKey(false)
                .setEnableDrag(false);
        // 读取配置文件
        loadFloatingWindowConfig();
        // 创建浮窗
        createFloatingWindows(clickFloating, stopFloating, massageFloating, windowInfoFloating);
    }

    /**
     * 为组件加载上次设置信息
     *
     * @throws IOException 配置文件读取异常
     */
    private void loadControlLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream configFileInput = checkRunningInputStream(configFile);
        prop.load(configFileInput);
        setControlLastConfig(lastTab_Set, prop, key_loadLastConfig, activation);
        setControlLastConfig(maxWindow_Set, prop, key_loadLastMaxWindow, unActivation);
        setControlLastConfig(fullWindow_Set, prop, key_loadLastFullWindow, unActivation);
        int them = Integer.parseInt(prop.getProperty(key_theme, String.valueOf(ThemeEnum.Auto.ordinal())));
        theme_Set.setValue(themeMap.get(them));
        configFileInput.close();
        InputStream clickFileInput = checkRunningInputStream(configFile_Click);
        prop.load(clickFileInput);
        setControlLastConfig(overtime_Set, prop, key_overtime);
        setControlLastConfig(imgLog_Set, prop, key_imgLog, activation);
        setControlLastConfig(moveLog_Set, prop, key_moveLog, activation);
        setControlLastConfig(dragLog_Set, prop, key_dragLog, activation);
        setControlLastConfig(waitLog_Set, prop, key_waitLog, activation);
        setControlLastConfig(noMove_Set, prop, key_noMove, unActivation);
        setControlLastConfig(autoSave_Set, prop, key_autoSave, activation);
        setControlLastConfig(clickLog_Set, prop, key_clickLog, activation);
        setControlLastConfig(opacity_Set, prop, key_opacity, defaultOpacity);
        setControlLastConfig(clickImgLog_Set, prop, key_clickLog, activation);
        setControlLastConfig(stopImgLog_Set, prop, key_stopImgLog, activation);
        setControlLastConfig(recordDrag_Set, prop, key_recordDrag, activation);
        setControlLastConfig(recordMove_Set, prop, key_recordMove, activation);
        setControlLastConfig(openUrlLog_Set, prop, key_openUrlLog, activation);
        setControlLastConfig(maxLogNum_Set, prop, key_maxLogNum, defaultMaxLog);
        setControlLastConfig(openFileLog_Set, prop, key_openFileLog, activation);
        setControlLastConfig(keyboardLog_Set, prop, key_keyboardLog, activation);
        setControlLastConfig(runScriptLog_Set, prop, key_runScriptLog, activation);
        setControlLastConfig(randomClick_Set, prop, key_randomClick, unActivation);
        setControlLastConfig(floatingDistance_Set, prop, key_margin, defaultMargin);
        setControlLastConfig(firstClick_Set, prop, key_lastFirstClick, unActivation);
        setControlLastConfig(floatingRun_Set, prop, key_loadFloatingRun, activation);
        setControlLastConfig(mouseFloating_Set, prop, key_mouseFloating, activation);
        setControlLastConfig(mouseWheelLog_Set, prop, key_mouseWheelLog, activation);
        setControlLastConfig(loadAutoClick_Set, prop, key_loadLastConfig, activation);
        setControlLastConfig(tableView_Set, prop, key_defaultStopImg, dataNumber_Set);
        setControlLastConfig(stopAllRegion_Set, prop, key_stopAllRegion, unActivation);
        setControlLastConfig(useRelatively_Set, prop, key_useRelatively, unActivation);
        setControlLastConfig(recordKeyboard_Set, prop, key_recordKeyboard, activation);
        setControlLastConfig(clickAllRegion_Set, prop, key_clickAllRegion, unActivation);
        setControlLastConfig(remindClickSave_Set, prop, key_remindClickSave, activation);
        setControlLastConfig(retrySecond_Set, prop, key_retrySecond, defaultRetrySecond);
        setControlLastConfig(stopOpacity_Set, prop, key_stopOpacity, defaultStopOpacity);
        setControlLastConfig(randomWaitTime_Set, prop, key_randomWaitTime, unActivation);
        setControlLastConfig(hideWindowRun_Set, prop, key_lastHideWindowRun, activation);
        setControlLastConfig(showWindowRun_Set, prop, key_lastShowWindowRun, activation);
        setControlLastConfig(titleCoordinate_Set, prop, key_titleCoordinate, activation);
        setControlLastConfig(recordMouseWheel_Set, prop, key_recordMouseWheel, activation);
        setControlLastConfig(recordMouseClick_Set, prop, key_recordMouseClick, activation);
        setControlLastConfig(randomClickTime_Set, prop, key_randomClickTime, unActivation);
        setControlLastConfig(floatingRecord_Set, prop, key_loadFloatingRecord, activation);
        setControlLastConfig(mouseFloatingRun_Set, prop, key_mouseFloatingRun, activation);
        setControlLastConfig(clickOpacity_Set, prop, key_clickOpacity, defaultClickOpacity);
        setControlLastConfig(randomClickX_Set, prop, key_randomClickX, defaultRandomClickX);
        setControlLastConfig(randomClickY_Set, prop, key_randomClickY, defaultRandomClickY);
        setControlLastConfig(randomTrajectory_Set, prop, key_randomTrajectory, unActivation);
        setControlLastConfig(offsetX_Set, prop, key_offsetX, String.valueOf(defaultOffsetX));
        setControlLastConfig(offsetY_Set, prop, key_offsetY, String.valueOf(defaultOffsetY));
        setControlLastConfig(updateStopWindow_Set, prop, key_updateStopWindow, unActivation);
        setControlLastConfig(updateClickWindow_Set, prop, key_updateClickWindow, unActivation);
        setControlLastConfig(hideWindowRecord_Set, prop, key_lastHideWindowRecord, activation);
        setControlLastConfig(showWindowRecord_Set, prop, key_lastShowWindowRecord, activation);
        setControlLastConfig(mouseFloatingRecord_Set, prop, key_mouseFloatingRecord, activation);
        setControlLastConfig(sampleInterval_Set, prop, key_sampleInterval, defaultSampleInterval);
        setControlLastConfig(findWindowWait_Set, prop, key_findWindowWait, defaultFindWindowWait);
        setControlLastConfig(randomClickInterval_Set, prop, key_randomClickInterval, unActivation);
        setControlLastConfig(stopRetryNum_Set, prop, key_defaultStopRetryNum, defaultStopRetryNum);
        setColorPickerConfig(colorPicker_Set, prop, key_lastFloatingTextColor, key_lastColorCustom);
        setControlLastConfig(clickTimeOffset_Set, prop, key_clickTimeOffset, defaultClickTimeOffset);
        setControlLastConfig(clickRetryNum_Set, prop, key_defaultClickRetryNum, defaultClickRetryNum);
        setControlLastConfig(randomTimeOffset_Set, prop, key_randomTimeOffset, defaultRandomTimeOffset);
        int stopFindImgType = Integer.parseInt(prop.getProperty(key_stopFindImgType, defaultStopFindImgType));
        stopFindImgType_Set.setValue(findImgTypeMap.get(stopFindImgType));
        int clickFindImgType = Integer.parseInt(prop.getProperty(key_clickFindImgType, defaultClickFindImgType));
        clickFindImgType_Set.setValue(findImgTypeMap.get(clickFindImgType));
        clickWindowPath = prop.getProperty(key_clickWindowPath);
        stopWindowPath = prop.getProperty(key_stopWindowPath);
        clickFileInput.close();
        String language = languageMap.get(bundle.getLocale());
        if (language != null) {
            language_Set.setValue(language);
        }
    }

    /**
     * 读取浮窗配置文件
     *
     * @throws IOException 配置文件读取异常
     */
    public static void loadFloatingWindowConfig() throws IOException {
        Properties prop = new Properties();
        InputStream clickFileInput = checkRunningInputStream(configFile_Click);
        prop.load(clickFileInput);
        int massageX = Integer.parseInt(prop.getProperty(key_massageX, defaultFloatingX));
        int massageY = Integer.parseInt(prop.getProperty(key_massageY, defaultFloatingY));
        int massageWidth = Integer.parseInt(prop.getProperty(key_massageWidth, defaultFloatingWidth));
        int massageHeight = Integer.parseInt(prop.getProperty(key_massageHeight, defaultFloatingHeight));
        FloatingWindowConfig massageConfig = new FloatingWindowConfig();
        massageConfig.setHeight(Math.max(1, Math.min(massageHeight, screenHeight)))
                .setWidth(Math.max(1, Math.min(massageWidth, screenHeight)))
                .setX(Math.max(0, Math.min(massageX, screenWidth)))
                .setY(Math.max(0, Math.min(massageY, screenHeight)));
        massageFloating.setConfig(massageConfig);
        int clickHeight = Integer.parseInt(prop.getProperty(key_clickHeight, defaultFloatingHeight));
        int clickWidth = Integer.parseInt(prop.getProperty(key_clickWidth, defaultFloatingWidth));
        int clickX = Integer.parseInt(prop.getProperty(key_clickX, defaultFloatingX));
        int clickY = Integer.parseInt(prop.getProperty(key_clickY, defaultFloatingY));
        FloatingWindowConfig clickConfig = new FloatingWindowConfig();
        clickConfig.setFindImgTypeEnum(Integer.parseInt(prop.getProperty(key_clickFindImgType, defaultClickFindImgType)))
                .setAllRegion(prop.getProperty(key_clickAllRegion, unActivation))
                .setHeight(Math.max(1, Math.min(clickHeight, screenHeight)))
                .setWidth(Math.max(1, Math.min(clickWidth, screenHeight)))
                .setX(Math.max(0, Math.min(clickX, screenWidth)))
                .setY(Math.max(0, Math.min(clickY, screenHeight)));
        clickFloating.setConfig(clickConfig);
        int stopHeight = Integer.parseInt(prop.getProperty(key_stopHeight, defaultFloatingHeight));
        int stopWidth = Integer.parseInt(prop.getProperty(key_stopWidth, defaultFloatingWidth));
        int stopX = Integer.parseInt(prop.getProperty(key_stopX, defaultFloatingX));
        int stopY = Integer.parseInt(prop.getProperty(key_stopY, defaultFloatingY));
        FloatingWindowConfig stopConfig = new FloatingWindowConfig();
        stopConfig.setFindImgTypeEnum(Integer.parseInt(prop.getProperty(key_stopFindImgType, defaultStopFindImgType)))
                .setAllRegion(prop.getProperty(key_stopAllRegion, unActivation))
                .setHeight(Math.max(1, Math.min(stopHeight, screenHeight)))
                .setWidth(Math.max(1, Math.min(stopWidth, screenHeight)))
                .setX(Math.max(0, Math.min(stopX, screenWidth)))
                .setY(Math.max(0, Math.min(stopY, screenHeight)));
        stopFloating.setConfig(stopConfig);
        clickFileInput.close();
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_noMove(), noMove_Set);
        addToolTip(tip_reLaunch(), reLaunch_Set);
        addToolTip(tip_overtime(), overtime_Set);
        addToolTip(tip_firstClick(), firstClick_Set);
        addToolTip(tip_recordDrag(), recordDrag_Set);
        addToolTip(tip_recordMove(), recordMove_Set);
        addToolTip(tip_randomClick(), randomClick_Set);
        addToolTip(tip_floatingRun(), floatingRun_Set);
        addToolTip(tip_margin(), floatingDistance_Set);
        addToolTip(tip_retrySecond(), retrySecond_Set);
        addToolTip(lastTab_Set.getText(), lastTab_Set);
        addValueToolTip(maxLogNum_Set, tip_maxLogNum());
        addToolTip(tip_remindSave(), remindClickSave_Set);
        addToolTip(tip_removeStopImgBtn(), removeAll_Set);
        addToolTip(tip_mouseFloating(), mouseFloating_Set);
        addToolTip(tip_hideWindowRun(), hideWindowRun_Set);
        addToolTip(tip_showWindowRun(), showWindowRun_Set);
        addToolTip(maxWindow_Set.getText(), maxWindow_Set);
        addToolTip(tip_massageRegion(), massageRegion_Set);
        addToolTip(tip_useRelatively(), useRelatively_Set);
        addToolTip(tip_defaultStopImgBtn(), stopImgBtn_Set);
        addToolTip(tip_remindTaskSave(), remindTaskSave_Set);
        addToolTip(tip_randomWaitTime(), randomWaitTime_Set);
        addToolTip(fullWindow_Set.getText(), fullWindow_Set);
        addToolTip(tip_floatingRecord(), floatingRecord_Set);
        addToolTip(tip_randomClickTime(), randomClickTime_Set);
        addValueToolTip(nextRunMemory_Set, tip_nextRunMemory());
        addToolTip(tip_randomTrajectory(), randomTrajectory_Set);
        addToolTip(tip_hideWindowRecord(), hideWindowRecord_Set);
        addToolTip(tip_showWindowRecord(), showWindowRecord_Set);
        addToolTip(tip_mouseFloatingRun(), mouseFloatingRun_Set);
        addToolTip(tip_lastAutoClickSetting(), loadAutoClick_Set);
        addToolTip(tip_offsetX() + defaultOffsetX, offsetX_Set);
        addToolTip(tip_offsetY() + defaultOffsetY, offsetY_Set);
        addToolTip(tip_showRegion(), clickRegion_Set, stopRegion_Set);
        addToolTip(tip_findWindow(), clickWindow_Set, stopWindow_Set);
        addToolTip(tip_randomClickInterval(), randomClickInterval_Set);
        addToolTip(tip_mouseFloatingRecord(), mouseFloatingRecord_Set);
        addToolTip(titleCoordinate_Set.getText(), titleCoordinate_Set);
        addToolTip(tip_autoSave() + autoSaveFileName(), autoSave_Set);
        addToolTip(tip_allRegion(), clickAllRegion_Set, stopAllRegion_Set);
        addValueToolTip(language_Set, tip_language(), language_Set.getValue());
        addToolTip(tip_stopRetryNum() + defaultStopRetryNum, stopRetryNum_Set);
        addValueToolTip(nextGcType_Set, tip_nextGcType(), nextGcType_Set.getValue());
        addToolTip(tip_alwaysRefresh(), updateStopWindow_Set, updateClickWindow_Set);
        addToolTip(tip_clickRetryNum() + defaultClickRetryNum, clickRetryNum_Set);
        addToolTip(tip_sampleInterval() + defaultSampleInterval, sampleInterval_Set);
        addValueToolTip(randomClickX_Set, tip_randomClickX() + defaultRandomClickX);
        addValueToolTip(randomClickY_Set, tip_randomClickY() + defaultRandomClickY);
        addValueToolTip(randomTimeOffset_Set, tip_randomTime() + defaultRandomTime);
        addToolTip(tip_findWindowWait() + defaultFindWindowWait, findWindowWait_Set);
        addValueToolTip(opacity_Set, tip_opacity(), String.valueOf(opacity_Set.getValue()));
        addValueToolTip(clickTimeOffset_Set, tip_clickTimeOffset() + defaultClickTimeOffset);
        addValueToolTip(colorPicker_Set, tip_colorPicker(), String.valueOf(colorPicker_Set.getValue()));
        addValueToolTip(stopOpacity_Set, tip_stopOpacity(), String.valueOf((int) stopOpacity_Set.getValue()));
        addValueToolTip(clickOpacity_Set, tip_clickOpacity(), String.valueOf((int) clickOpacity_Set.getValue()));
        addToolTip(clickLog_Set, moveLog_Set, dragLog_Set, waitLog_Set, clickImgLog_Set, stopImgLog_Set, imgLog_Set,
                openFileLog_Set, runScriptLog_Set, openUrlLog_Set, mouseWheelLog_Set, keyboardLog_Set,
                recordMouseWheel_Set, recordKeyboard_Set, recordMouseClick_Set);
    }

    /**
     * 设置文本输入框提示
     */
    private void setPromptText() {
        retrySecond_Set.setPromptText("1");
        floatingDistance_Set.setPromptText("0");
        stopRetryNum_Set.setPromptText(defaultStopRetryNum);
        randomClickX_Set.setPromptText(defaultRandomClickX);
        randomClickY_Set.setPromptText(defaultRandomClickY);
        randomTimeOffset_Set.setPromptText(defaultRandomTime);
        clickRetryNum_Set.setPromptText(defaultClickRetryNum);
        sampleInterval_Set.setPromptText(defaultSampleInterval);
        findWindowWait_Set.setPromptText(defaultFindWindowWait);
        offsetX_Set.setPromptText(String.valueOf(defaultOffsetX));
        offsetY_Set.setPromptText(String.valueOf(defaultOffsetY));
        clickTimeOffset_Set.setPromptText(defaultClickTimeOffset);
    }

    /**
     * 监听并保存颜色选择器自定义颜色
     */
    private void setCustomColorsListener() {
        // 监听 customColors 的变化
        colorPicker_Set.getCustomColors().addListener((ListChangeListener<Color>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved() || change.wasUpdated()) {
                    // 保存自定义颜色
                    ObservableList<Color> customColors = colorPicker_Set.getCustomColors();
                    StringBuilder colorsString = new StringBuilder();
                    customColors.forEach(color -> colorsString.append(color.toString()).append(" "));
                    String result = colorsString.toString().trim();
                    try {
                        updateProperties(configFile_Click, key_lastColorCustom, result);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    /**
     * 监听颜色选择器设置变化
     */
    private void setColorsListener() {
        colorPicker_Set.valueProperty().addListener((_, _, newValue) -> {
            if (massageFloating != null) {
                massageFloating.setTextFill(newValue);
                updateFloatingWindow(massageFloating);
            }
            addValueToolTip(colorPicker_Set, tip_colorPicker(), String.valueOf(newValue));
        });
    }

    /**
     * 监听数值滑动条内容变化
     */
    private void sliderValueListener() {
        opacity_Set.valueProperty().addListener((_, _, newValue) -> {
            double rounded = Math.round(newValue.doubleValue() * 10) / 10.0;
            if (newValue.doubleValue() != rounded) {
                opacity_Set.setValue(rounded);
            }
            if (massageFloating != null) {
                Rectangle rectangle = massageFloating.getRectangle();
                if (rectangle != null) {
                    if (rounded == 0) {
                        rectangle.setFill(new Color(0, 0, 0, 0.01));
                    } else {
                        rectangle.setFill(new Color(0, 0, 0, rounded));
                    }
                }
            }
            addValueToolTip(opacity_Set, tip_opacity(), String.valueOf(rounded));
        });
    }

    /**
     * 给组件添加内容变化监听
     */
    private void nodeValueChangeListener() {
        // 监听颜色选择器设置变化
        setColorsListener();
        // 透明度滑块监听
        sliderValueListener();
        // 停止操作图像识别准确度设置监听
        integerSliderValueListener(stopOpacity_Set, tip_stopOpacity());
        // 要点击的图像识别准确度设置监听
        integerSliderValueListener(clickOpacity_Set, tip_clickOpacity());
        // 每张图片最大匹配时间输入框监听
        integerRangeTextField(overtime_Set, 1, null, tip_overtime());
        // 最大记录数量文本输入框内容
        integerRangeTextField(maxLogNum_Set, 1, null, tip_maxLogNum());
        // 匹配失败重试间隔时间输入框监听
        integerRangeTextField(retrySecond_Set, 0, null, tip_retrySecond());
        // 浮窗离屏幕边界距离输入框监听
        integerRangeTextField(floatingDistance_Set, 0, null, tip_margin());
        // 限制下次运行内存文本输入框内容
        integerRangeTextField(nextRunMemory_Set, 1, null, tip_nextRunMemory());
        // 浮窗跟随鼠标时横轴偏移量输入框监听
        integerRangeTextField(offsetX_Set, -screenWidth, screenWidth, tip_offsetX() + defaultOffsetX);
        // 浮窗跟随鼠标时纵轴偏移量输入框监听
        integerRangeTextField(offsetY_Set, -screenHeight, screenHeight, tip_offsetY() + defaultOffsetY);
        // 随机点击时间偏移量文本输入框内容
        integerRangeTextField(randomTimeOffset_Set, 0, null, tip_randomTime() + defaultRandomTime);
        // 限制终止操作识别失败重试次数文本输入框内容
        integerRangeTextField(stopRetryNum_Set, 0, null, tip_stopRetryNum() + defaultStopRetryNum);
        // 随机横坐标偏移量文本输入框内容
        integerRangeTextField(randomClickX_Set, 0, screenWidth, tip_randomClickX() + defaultRandomClickX);
        // 随机纵坐标偏移量文本输入框内容
        integerRangeTextField(randomClickY_Set, 0, screenHeight, tip_randomClickY() + defaultRandomClickY);
        // 限制要点击的图片识别失败重试次数文本输入框内容
        integerRangeTextField(clickRetryNum_Set, 0, null, tip_clickRetryNum() + defaultClickRetryNum);
        // 限制鼠标轨迹采样间隔文本输入框内容
        integerRangeTextField(sampleInterval_Set, 0, null, tip_sampleInterval() + defaultSampleInterval);
        // 限制默认窗口识别的准备时间文本输入框内容
        integerRangeTextField(findWindowWait_Set, 0, null, tip_findWindowWait() + defaultFindWindowWait);
        // 限制默认单次点击时长文本输入框内容
        integerRangeTextField(clickTimeOffset_Set, 0, null, tip_clickTimeOffset() + defaultClickTimeOffset);
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        baseDisableNodes.add(removeAll_Set);
        baseDisableNodes.add(stopImgBtn_Set);
        baseDisableNodes.add(stopWindow_Set);
        baseDisableNodes.add(clickWindow_Set);
        Node aboutTab = mainScene.lookup("#aboutTab");
        baseDisableNodes.add(aboutTab);
        Node settingTab = mainScene.lookup("#settingTab");
        baseDisableNodes.add(settingTab);
        Node autoClickTab = mainScene.lookup("#autoClickTab");
        baseDisableNodes.add(autoClickTab);
        Node timedStartTab = mainScene.lookup("#timedStartTab");
        baseDisableNodes.add(timedStartTab);
        Node tabPane = mainScene.lookup("#tabPane");
        windowInfoDisableNodes.add(tabPane);
        stopDisableNodes.addAll(baseDisableNodes);
        stopDisableNodes.add(stopFindImgType_Set);
        clickDisableNodes.addAll(baseDisableNodes);
        clickDisableNodes.add(clickFindImgType_Set);
    }

    /**
     * 获取 JVM 设置并展示
     *
     * @throws IOException cfg 配置文件读取异常
     */
    private void getJVMConfig() throws IOException {
        // 获取当前运行路径
        setPathLabel(thisPath_Set, appLaunchPath);
        long maxMemory = Runtime.getRuntime().maxMemory();
        runningMemory_Set.setText(getUnitSize(maxMemory, false));
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = ((com.sun.management.OperatingSystemMXBean) osBean).getTotalMemorySize();
        String systemUnitSizeMemory = getUnitSize(totalMemory, false);
        systemMemory_Set.setText(systemUnitSizeMemory);
        Map<String, String> jvm = getJavaOptionValue(jvmArgs);
        String xmxValue = jvm.get(Xmx);
        if (StringUtils.isNotBlank(xmxValue)) {
            nextRunMemory_Set.setText(xmxValue.substring(0, xmxValue.indexOf(G)));
        }
        if (currentGCType.contains(text_unknowGC())) {
            gcType_Set.setText(text_unknowGC());
        } else {
            gcType_Set.setText(currentGCType);
        }
        addToolTip(currentGCType, gcType_Set);
        String gcType = jvm.get(XX);
        if (StringUtils.isNotBlank(gcType)) {
            nextGcType_Set.setValue(gcType);
        }
    }

    /**
     * 创建重启应用确认弹窗
     */
    private void creatReLaunchConfirm() {
        if (initializedFinished) {
            ButtonType result = creatConfirmDialog(
                    confirm_reLaunch(),
                    confirm_reLaunchConfirm(),
                    confirm_reLaunchOk(),
                    confirm_cancel());
            ButtonBar.ButtonData buttonData = result.getButtonData();
            if (!buttonData.isCancelButton()) {
                // 重启应用
                try {
                    reLaunch();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * 创建任务参数对象
     *
     * @return 任务参数对象
     */
    private TaskBean<ImgFileVO> creatTaskBean() {
        TaskBean<ImgFileVO> taskBean = new TaskBean<>();
        taskBean.setProgressBar(progressBar_Set)
                .setMassageLabel(dataNumber_Set)
                .setTableView(tableView_Set)
                .setDisableNodes(windowInfoDisableNodes);
        return taskBean;
    }

    /**
     * 启动加载图片任务
     *
     * @param files 要加载的文件
     */
    private void startLoadImgTask(List<? extends File> files) {
        TaskBean<ImgFileVO> taskBean = creatTaskBean();
        Task<Void> loadImgTask = loadImg(taskBean, files);
        bindingTaskNode(loadImgTask, taskBean);
        loadImgTask.setOnSucceeded(_ -> {
            taskUnbind(taskBean);
            updateTableViewSizeText(tableView_Set, dataNumber_Set, unit_img());
            tableView_Set.refresh();
        });
        Thread.ofVirtual()
                .name("loadImgTask-vThread" + tabId)
                .start(loadImgTask);
    }

    /**
     * 初始化下拉框
     */
    private void setChoiceBoxItems() {
        // 初始化语言设置下拉框
        initializeChoiceBoxItems(language_Set, LanguageEnum.zh_CN.getString(), languageMap);
        // 停止操作图像识别区域设置
        initializeChoiceBoxItems(stopFindImgType_Set, findImgType_all(), findImgTypeList);
        // 要点击的图像识别区域设置
        initializeChoiceBoxItems(clickFindImgType_Set, findImgType_all(), findImgTypeList);
        // 外观下拉框
        initializeChoiceBoxItems(theme_Set, theme_light(), themeList);
    }

    /**
     * 构建右键菜单
     */
    private void buildContextMenu() {
        // 构建表格右键菜单
        buildTableViewContextMenu(tableView_Set, dataNumber_Set);
        List<Stage> stages = List.of(mainStage);
        // 构建窗口信息栏右键菜单
        ContextMenu stopContextMenu = buildWindowInfoMenu(stopWindowInfo_Set, stopWindowMonitor, windowInfoDisableNodes, stages);
        buildStopUpdateListMenu(stopContextMenu, stopWindowMonitor);
        ContextMenu clickContextMenu = buildWindowInfoMenu(clickWindowInfo_Set, clickWindowMonitor, windowInfoDisableNodes, stages);
        buildClickUpdateListMenu(clickContextMenu, clickWindowMonitor);
    }

    /**
     * 更新操作列表所有终止操作窗口信息
     *
     * @param contextMenu   右键菜单集合
     * @param windowMonitor 窗口监视器
     */
    private static void buildStopUpdateListMenu(ContextMenu contextMenu, WindowMonitor windowMonitor) {
        MenuItem menuItem = new MenuItem(findImgSet_updateList());
        menuItem.setOnAction(_ -> {
            windowMonitor.updateWindowInfo();
            ObservableList<ClickPositionVO> items = autoClickController.tableView_Click.getItems();
            if (CollectionUtils.isNotEmpty(items)) {
                int update = 0;
                for (ClickPositionVO item : items) {
                    FloatingWindowConfig windowConfig = item.getStopWindowConfig();
                    if (windowConfig != null) {
                        if (FindImgTypeEnum.WINDOW.ordinal() == windowConfig.getFindImgTypeEnum()) {
                            windowConfig.setWindowInfo(windowMonitor.getWindowInfo());
                            item.setStopWindowConfig(windowConfig);
                            update++;
                        }
                    }
                }
                new MessageBubble(text_updateNum() + update, updateListMassageTime);
            } else {
                new MessageBubble(text_noUpdateNum(), updateListMassageTime);
            }
        });
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 更新操作列表所有要识别的窗口信息
     *
     * @param contextMenu   右键菜单集合
     * @param windowMonitor 窗口监视器
     */
    private static void buildClickUpdateListMenu(ContextMenu contextMenu, WindowMonitor windowMonitor) {
        MenuItem menuItem = new MenuItem(findImgSet_updateList());
        menuItem.setOnAction(_ -> {
            windowMonitor.updateWindowInfo();
            ObservableList<ClickPositionVO> items = autoClickController.tableView_Click.getItems();
            if (CollectionUtils.isNotEmpty(items)) {
                int update = 0;
                for (ClickPositionVO item : items) {
                    FloatingWindowConfig windowConfig = item.getClickWindowConfig();
                    if (windowConfig != null) {
                        if (FindImgTypeEnum.WINDOW.ordinal() == windowConfig.getFindImgTypeEnum()) {
                            windowConfig.setWindowInfo(windowMonitor.getWindowInfo());
                            item.setClickWindowConfig(windowConfig);
                            update++;
                        }
                    }
                }
                new MessageBubble(text_updateNum() + update, updateListMassageTime);
            } else {
                new MessageBubble(text_noUpdateNum(), updateListMassageTime);
            }
        });
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 初始化窗口信息获取器
     */
    private void initWindowMonitor() {
        stopWindowMonitor = new WindowMonitor(windowInfoDisableNodes, mainStage);
        stopWindowMonitor.setWindowInfoHandler(creatDefaultWindowInfoHandler(stopWindowInfo_Set));
        if (StringUtils.isNotBlank(stopWindowPath)) {
            stopWindowMonitor.updateWindowInfo(stopWindowPath);
        }
        clickWindowMonitor = new WindowMonitor(windowInfoDisableNodes, mainStage);
        clickWindowMonitor.setWindowInfoHandler(creatDefaultWindowInfoHandler(clickWindowInfo_Set));
        if (StringUtils.isNotBlank(clickWindowPath)) {
            clickWindowMonitor.updateWindowInfo(clickWindowPath);
        }
    }

    /**
     * 禁用需要自动化权限的组件
     */
    private void setNoPermissionLog() {
        noAutomationPermission = true;
        setNodeDisable(stopWindow_Set, true);
        setNodeDisable(clickWindow_Set, true);
        setNodeDisable(noPermissionHBox_Set, true);
        addToolTip(tip_noAutomationPermission(), noPermission_Set);
    }

    /**
     * 添加系统主题变化监听器
     */
    private void setRegisterListener() {
        Platform.getPreferences().colorSchemeProperty().addListener((_, _, _) -> {
            String value = theme_Set.getValue();
            if (theme_auto().equals(value)) {
                ColorScheme scheme = Platform.getPreferences().getColorScheme();
                if (ColorScheme.DARK == scheme) {
                    setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                    isDarkTheme = true;
                } else {
                    setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                    isDarkTheme = false;
                }
                // 处理无法自动切换主题的页面
                manuallyChangeThemeList.forEach(controllerClass -> {
                    @SuppressWarnings("unchecked")
                    Class<ManuallyChangeThemeController> typedClass = (Class<ManuallyChangeThemeController>) controllerClass;
                    ManuallyChangeThemeController controller = getController(typedClass);
                    if (controller != null) {
                        controller.manuallyChangeTheme();
                    }
                });
            }
        });
    }

    /**
     * 根据鼠标位置调整 ui
     *
     * @param mousePoint 鼠标位置
     */
    @Override
    public void onMousePositionUpdate(Point mousePoint) {
        Platform.runLater(() -> {
            if (massageFloating != null) {
                Stage floatingStage = massageFloating.getStage();
                if (floatingStage != null) {
                    if (floatingStage.isShowing() && mouseFloating_Set.isSelected()) {
                        int offsetX = setDefaultIntValue(offsetX_Set, defaultOffsetX, -screenWidth, screenWidth);
                        int offsetY = setDefaultIntValue(offsetY_Set, defaultOffsetY, -screenHeight, screenHeight);
                        floatingMove(floatingStage, mousePoint, offsetX, offsetY);
                        setPositionText(massageFloating, "");
                    }
                }
            }
        });
    }

    /**
     * 界面初始化
     *
     * @throws IOException 配置文件读取异常、配置文件读取异常
     */
    @FXML
    private void initialize() throws IOException {
        // 初始化下拉框
        setChoiceBoxItems();
        // 获取最大运行内存并展示
        getJVMConfig();
        // 设置鼠标悬停提示
        setToolTip();
        // 设置文本输入框提示
        setPromptText();
        // 给组件添加内容变化监听
        nodeValueChangeListener();
        // 加载上次设置信息
        loadControlLastConfig();
        // 初始化浮窗
        initFloatingWindow();
        // 监听并保存颜色选择器自定义颜色
        setCustomColorsListener();
        Platform.runLater(() -> {
            setRegisterListener();
            // 建议自动化权限
            if (!hasAutomationPermission()) {
                // 禁用需要自动化权限的组件
                setNoPermissionLog();
            }
            // 组件自适应宽高
            adaption();
            // 初始化窗口监控器
            initWindowMonitor();
            // 获取鼠标坐标监听器
            MousePositionListener.getInstance().addListener(this);
            // 自动填充 JavaFX 表格
            autoBuildTableViewData(tableView_Set, ImgFileVO.class, tabId, index_Set);
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Set);
            // 构建右键菜单
            buildContextMenu();
            // 加载完成后发布事件
            EventBus.publish(new SettingsLoadedEvent());
            // 标记页面加载完毕
            initializedFinished = true;
            // 设置要防重复点击的组件
            setDisableNodes();
        });
    }

    /**
     * 自动操作工具功能加载上次设置信息
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadAutoClickAction() throws IOException {
        setLoadLastConfigCheckBox(loadAutoClick_Set, configFile_Click, key_loadLastConfig);
    }

    /**
     * 操作步骤详情页修改后未保存提示
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadClickRemindSaveAction() throws IOException {
        setLoadLastConfigCheckBox(remindClickSave_Set, configFile_Click, key_remindClickSave);
    }

    /**
     * 定时任务详情页修改后未保存提示
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadTaskRemindSaveAction() throws IOException {
        setLoadLastConfigCheckBox(remindTaskSave_Set, configFile_Click, key_remindTaskSave);
    }

    /**
     * 关闭程序时自动保存列表操作步骤
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadAutoSaveAction() throws IOException {
        setLoadLastConfigCheckBox(autoSave_Set, configFile_Click, key_autoSave);
    }

    /**
     * 记住关闭前打开的页面设置
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadLastTabAction() throws IOException {
        setLoadLastConfigCheckBox(lastTab_Set, configFile, key_loadLastConfig);
    }

    /**
     * 记住窗口是否全屏设置
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadFullWindowAction() throws IOException {
        setLoadLastConfigCheckBox(fullWindow_Set, configFile, key_loadLastFullWindow);
    }

    /**
     * 记住窗口是否最大化设置
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadMaxWindowAction() throws IOException {
        setLoadLastConfigCheckBox(maxWindow_Set, configFile, key_loadLastMaxWindow);
    }

    /**
     * 执行自动流程前最小化本程序
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadHideWindowRunAction() throws IOException {
        setLoadLastConfigCheckBox(hideWindowRun_Set, configFile_Click, key_lastHideWindowRun);
    }

    /**
     * 执行自动流程结束后弹出本程序
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadShowWindowRunAction() throws IOException {
        setLoadLastConfigCheckBox(showWindowRun_Set, configFile_Click, key_lastShowWindowRun);
    }

    /**
     * 录制自动流程前最小化本程序
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadHideWindowRecordAction() throws IOException {
        setLoadLastConfigCheckBox(hideWindowRecord_Set, configFile_Click, key_lastHideWindowRecord);
    }

    /**
     * 录制自动流程结束后弹出本程序
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadShowWindowRecordAction() throws IOException {
        setLoadLastConfigCheckBox(showWindowRecord_Set, configFile_Click, key_lastShowWindowRecord);
    }

    /**
     * 执行自动流程前点击第一个起始坐标
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadFirstClickAction() throws IOException {
        setLoadLastConfigCheckBox(firstClick_Set, configFile_Click, key_lastFirstClick);
    }

    /**
     * 执行自动流程时显示信息浮窗
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadFloatingRunAction() throws IOException {
        setLoadLastConfigCheckBox(floatingRun_Set, configFile_Click, key_loadFloatingRun);
    }

    /**
     * 录制自动流程时显示信息浮窗
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadFloatingRecordAction() throws IOException {
        setLoadLastConfigCheckBox(floatingRecord_Set, configFile_Click, key_loadFloatingRecord);
    }

    /**
     * 监听并保存浮窗字体颜色
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void colorAction() throws IOException {
        updateProperties(configFile_Click, key_lastFloatingTextColor, String.valueOf(colorPicker_Set.getValue()));
    }

    /**
     * 运行自动流程时信息浮窗跟随鼠标
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadMouseFloatingRunAction() throws IOException {
        setLoadLastConfigCheckBox(mouseFloatingRun_Set, configFile_Click, key_mouseFloatingRun);
    }

    /**
     * 录制自动流程时信息浮窗跟随鼠标
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void loadMouseFloatingRecordAction() throws IOException {
        setLoadLastConfigCheckBox(mouseFloatingRecord_Set, configFile_Click, key_mouseFloatingRecord);
    }

    /**
     * 应用主界面标题栏展示鼠标位置
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void titleCoordinate() throws IOException {
        setLoadLastConfigCheckBox(titleCoordinate_Set, configFile_Click, key_titleCoordinate);
    }

    /**
     * 录制时记录拖鼠标拽轨迹
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void recordDrag() throws IOException {
        setLoadLastConfigCheckBox(recordDrag_Set, configFile_Click, key_recordDrag);
    }

    /**
     * 录制时记录鼠标移动轨迹
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void recordMove() throws IOException {
        setLoadLastConfigCheckBox(recordMove_Set, configFile_Click, key_recordMove);
    }

    /**
     * 运行时启用随机点击坐标偏移
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void randomClick() throws IOException {
        setLoadLastConfigCheckBox(randomClick_Set, configFile_Click, key_randomClick);
    }

    /**
     * 运行时启用随机轨迹坐标偏移
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void randomTrajectory() throws IOException {
        setLoadLastConfigCheckBox(randomTrajectory_Set, configFile_Click, key_randomTrajectory);
    }

    /**
     * 运行时启用随机点击时长偏移
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void randomClickTime() throws IOException {
        setLoadLastConfigCheckBox(randomClickTime_Set, configFile_Click, key_randomClickTime);
    }

    /**
     * 运行时启用随机操作间隔时间偏移
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void randomClickInterval() throws IOException {
        setLoadLastConfigCheckBox(randomClickInterval_Set, configFile_Click, key_randomClickInterval);
    }

    /**
     * 运行时启用随机操作执行前等待时间偏移
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void randomWaitTime() throws IOException {
        setLoadLastConfigCheckBox(randomWaitTime_Set, configFile_Click, key_randomWaitTime);
    }

    /**
     * 运行自动流程时记录点击信息
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void clickLog() throws IOException {
        setLoadLastConfigCheckBox(clickLog_Set, configFile_Click, key_clickLog);
    }

    /**
     * 运行自动流程时记录移动轨迹
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void moveLog() throws IOException {
        setLoadLastConfigCheckBox(moveLog_Set, configFile_Click, key_moveLog);
    }

    /**
     * 运行自动流程时记录拖拽轨迹
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void dragLog() throws IOException {
        setLoadLastConfigCheckBox(dragLog_Set, configFile_Click, key_dragLog);
    }

    /**
     * 运行自动流程时记录目标图像识别信息
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void clickImgLog() throws IOException {
        setLoadLastConfigCheckBox(clickImgLog_Set, configFile_Click, key_clickImgLog);
    }

    /**
     * 运行自动流程时记录终止图像识别信息
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void stopImgLog() throws IOException {
        setLoadLastConfigCheckBox(stopImgLog_Set, configFile_Click, key_stopImgLog);
    }

    /**
     * 运行自动流程时记录详细的图像识别信息
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void imgLog() throws IOException {
        setLoadLastConfigCheckBox(imgLog_Set, configFile_Click, key_imgLog);
    }

    /**
     * 运行自动流程时记录等待信息
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void waitLog() throws IOException {
        setLoadLastConfigCheckBox(waitLog_Set, configFile_Click, key_waitLog);
    }

    /**
     * 运行自动流程时记录打开文件事件
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void openFileLog() throws IOException {
        setLoadLastConfigCheckBox(openFileLog_Set, configFile_Click, key_openFileLog);
    }

    /**
     * 运行自动流程时记录运行脚本事件
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void runScriptLog() throws IOException {
        setLoadLastConfigCheckBox(runScriptLog_Set, configFile_Click, key_runScriptLog);
    }

    /**
     * 运行自动流程时记录打开网址事件
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void openUrlLog() throws IOException {
        setLoadLastConfigCheckBox(openUrlLog_Set, configFile_Click, key_openUrlLog);
    }

    /**
     * 运行自动流程时记录鼠标滚轮滚动
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void mouseWheelLog() throws IOException {
        setLoadLastConfigCheckBox(mouseWheelLog_Set, configFile_Click, key_mouseWheelLog);
    }


    /**
     * 终止操作图像第一次识别失败后改为识别整个屏幕
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void stopAllRegionAction() throws IOException {
        String allRegion = stopAllRegion_Set.isSelected() ? activation : unActivation;
        stopFloating.getConfig().setAllRegion(allRegion);
        setLoadLastConfigCheckBox(stopAllRegion_Set, configFile_Click, key_stopAllRegion);
    }

    /**
     * 要点击的图像第一次识别失败后改为识别整个屏幕
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void clickAllRegionAction() throws IOException {
        String allRegion = clickAllRegion_Set.isSelected() ? activation : unActivation;
        clickFloating.getConfig().setAllRegion(allRegion);
        setLoadLastConfigCheckBox(clickAllRegion_Set, configFile_Click, key_clickAllRegion);
    }

    /**
     * 要点击的图像窗口信息实时更新开关
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void updateClickWindowAction() throws IOException {
        setLoadLastConfigCheckBox(updateClickWindow_Set, configFile_Click, key_updateClickWindow);
    }

    /**
     * 终止操作图像窗口信息实时更新开关
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void updateStopWindowAction() throws IOException {
        setLoadLastConfigCheckBox(updateStopWindow_Set, configFile_Click, key_updateStopWindow);
    }

    /**
     * 使用相对坐标开关
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void useRelatively() throws IOException {
        setLoadLastConfigCheckBox(useRelatively_Set, configFile_Click, key_useRelatively);
    }

    /**
     * 运行自动流程时记录键盘事件开关
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void keyboardLog() throws IOException {
        setLoadLastConfigCheckBox(keyboardLog_Set, configFile_Click, key_keyboardLog);
    }

    /**
     * 录制时记录键盘事件开关
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void recordKeyboard() throws IOException {
        setLoadLastConfigCheckBox(recordKeyboard_Set, configFile_Click, key_recordKeyboard);
    }

    /**
     * 录制时记录鼠标滑轮事件开关
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void recordMouseWheel() throws IOException {
        setLoadLastConfigCheckBox(recordMouseWheel_Set, configFile_Click, key_recordMouseWheel);
    }

    /**
     * 录制时记录鼠标点击事件开关
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void recordMouseClick() throws IOException {
        setLoadLastConfigCheckBox(recordMouseClick_Set, configFile_Click, key_recordMouseClick);
    }

    /**
     * 输入相关操作不移动鼠标开关
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void noMouseMove() throws IOException {
        setLoadLastConfigCheckBox(noMove_Set, configFile_Click, key_noMove);
    }

    /**
     * 显示浮窗位置时信息浮窗跟随鼠标
     *
     * @throws IOException 配置文件保存异常
     */
    @FXML
    private void mouseFloatingAction() throws IOException {
        setLoadLastConfigCheckBox(mouseFloating_Set, configFile_Click, key_mouseFloating);
        if (massageFloating != null) {
            Stage floatingStage = massageFloating.getStage();
            if (floatingStage != null && floatingStage.isShowing()) {
                if (mouseFloating_Set.isSelected()) {
                    massageFloating.setCloseSave(false);
                    updateMassageLabel(massageFloating, text_escCloseFloating());
                    massageRegion_Set.setText(text_closeFloating());
                    addToolTip(tip_closeFloating(), massageRegion_Set);
                } else {
                    massageFloating.setCloseSave(true);
                    updateMassageLabel(massageFloating, text_saveFloatingCoordinate());
                    massageRegion_Set.setText(text_saveCloseFloating());
                    addToolTip(tip_saveFloating(), massageRegion_Set);
                }
            }
        }
    }

    /**
     * 显示或隐藏浮窗位置
     *
     * @throws IOException 配置文件读取异常
     */
    @FXML
    private void massageRegionAction() throws IOException {
        Stage floatingStage = massageFloating.getStage();
        if (floatingStage != null) {
            getFloatingSetting(massageFloating, configFile_Click);
            massageFloating.setDisableNodes(baseDisableNodes);
            if (!floatingStage.isShowing()) {
                // 显示浮窗
                showFloatingWindow(massageFloating);
            } else if (floatingStage.isShowing()) {
                // 还原鼠标跟随前的坐标
                if (mouseFloating_Set.isSelected()) {
                    massageFloating.setHideButtonText(text_closeFloating())
                            .setHideButtonToolTip(tip_closeFloating())
                            .setCloseSave(false);
                } else {
                    massageFloating.setHideButtonText(text_saveCloseFloating())
                            .setHideButtonToolTip(tip_saveFloating())
                            .setCloseSave(true);
                }
                // 隐藏浮窗
                hideFloatingWindow(false, massageFloating);
                // 只有所有浮窗都关闭时才恢复其他节点可交互状态
                Stage clickStage = clickFloating.getStage();
                Stage stopStage = stopFloating.getStage();
                if ((clickStage == null || !clickStage.isShowing()) &&
                        (stopStage == null || !stopStage.isShowing())) {
                    changeDisableNodes(baseDisableNodes, false);
                }
            }
        }
    }

    /**
     * 重启程序按钮
     *
     * @throws IOException 配置文件保存异常、重启执行失败
     */
    @FXML
    private void reLaunch() throws IOException {
        // 重启前需要保存设置，如果只使用关闭方法中的保存功能可能无法及时更新 jvm 配置参数
        mainController.saveAllLastConfig();
        Platform.exit();
        if (!isRunningFromIDEA) {
            ProcessBuilder processBuilder = null;
            if (isWin) {
                processBuilder = new ProcessBuilder(appLaunchPath);
            } else if (isMac) {
                processBuilder = new ProcessBuilder("open", "-n", appLaunchPath);
            }
            if (processBuilder != null) {
                processBuilder.start();
            }
        }
    }

    /**
     * 选择终止操作的图片
     *
     * @param actionEvent 点击事件
     */
    @FXML
    private void addStopImgPath(ActionEvent actionEvent) throws IOException {
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        List<File> imgFiles = creatImgFilesChooser(window, stopImgSelectPath);
        if (CollectionUtils.isNotEmpty(imgFiles)) {
            File selectedFile = imgFiles.getFirst();
            stopImgSelectPath = updatePathLabel(selectedFile.getPath(), stopImgSelectPath, key_stopImgSelectPath, null, configFile_Click);
            startLoadImgTask(imgFiles);
        }
    }

    /**
     * 清空图片列表
     */
    @FXML
    private void removeAll() {
        tableView_Set.getItems().stream().parallel().forEach(ImgFileVO::clearResources);
        removeTableViewData(tableView_Set, dataNumber_Set);
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        startLoadImgTask(files);
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
        acceptDropImg(dragEvent);
    }

    /**
     * 更改语言下拉框
     */
    @FXML
    private void languageAction() {
        addValueToolTip(language_Set, tip_language(), language_Set.getValue());
        // 创建重启确认框
        creatReLaunchConfirm();
    }

    /**
     * gc 类型变更下拉框监听
     */
    @FXML
    private void nextGcTypeAction() {
        addValueToolTip(nextGcType_Set, tip_nextGcType(), nextGcType_Set.getValue());
        // 创建重启确认框
        creatReLaunchConfirm();
    }

    /**
     * 要点击的图像识别区域下拉框
     */
    @FXML
    private void clickFindImgTypeAction() {
        String value = clickFindImgType_Set.getValue();
        addValueToolTip(clickFindImgType_Set, tip_findImgType(), value);
        if (findImgType_region().equals(value)) {
            clickRegionInfoHBox_Set.setVisible(true);
            clickRegionInfoHBox_Set.getChildren().removeAll(clickRegionHBox_Set, clickWindowInfoHBox_Set);
            clickRegionInfoHBox_Set.getChildren().add(clickRegionHBox_Set);
            if (clickFloating != null) {
                FloatingWindowConfig config = clickFloating.getConfig();
                config.setFindImgTypeEnum(FindImgTypeEnum.REGION.ordinal());
                clickFloating.setConfig(config);
            }
        } else if (findImgType_window().equals(value)) {
            clickRegionInfoHBox_Set.setVisible(true);
            clickRegionInfoHBox_Set.getChildren().removeAll(clickRegionHBox_Set, clickWindowInfoHBox_Set);
            clickRegionInfoHBox_Set.getChildren().add(clickWindowInfoHBox_Set);
            if (clickFloating != null) {
                FloatingWindowConfig config = clickFloating.getConfig();
                config.setFindImgTypeEnum(FindImgTypeEnum.WINDOW.ordinal());
                clickFloating.setConfig(config);
            }
        } else if (findImgType_all().equals(value)) {
            clickRegionInfoHBox_Set.setVisible(false);
            clickRegionInfoHBox_Set.getChildren().removeAll(clickRegionHBox_Set, clickWindowInfoHBox_Set);
            if (clickFloating != null) {
                FloatingWindowConfig config = clickFloating.getConfig();
                config.setFindImgTypeEnum(FindImgTypeEnum.ALL.ordinal());
                clickFloating.setConfig(config);
            }
        }
    }

    /**
     * 终止操作图像识别区域下拉框
     */
    @FXML
    private void stopFindImgTypeAction() {
        String value = stopFindImgType_Set.getValue();
        addValueToolTip(stopFindImgType_Set, tip_findImgType(), value);
        if (findImgType_region().equals(value)) {
            stopRegionInfoHBox_Set.setVisible(true);
            stopRegionInfoHBox_Set.getChildren().removeAll(stopRegionHBox_Set, stopWindowInfoHBox_Set);
            stopRegionInfoHBox_Set.getChildren().add(stopRegionHBox_Set);
            if (stopFloating != null) {
                FloatingWindowConfig config = stopFloating.getConfig();
                config.setFindImgTypeEnum(FindImgTypeEnum.REGION.ordinal());
                stopFloating.setConfig(config);
            }
        } else if (findImgType_window().equals(value)) {
            stopRegionInfoHBox_Set.setVisible(true);
            stopRegionInfoHBox_Set.getChildren().removeAll(stopRegionHBox_Set, stopWindowInfoHBox_Set);
            stopRegionInfoHBox_Set.getChildren().add(stopWindowInfoHBox_Set);
            if (stopFloating != null) {
                FloatingWindowConfig config = stopFloating.getConfig();
                config.setFindImgTypeEnum(FindImgTypeEnum.WINDOW.ordinal());
                stopFloating.setConfig(config);
            }
        } else if (findImgType_all().equals(value)) {
            stopRegionInfoHBox_Set.setVisible(false);
            stopRegionInfoHBox_Set.getChildren().removeAll(stopRegionHBox_Set, stopWindowInfoHBox_Set);
            if (stopFloating != null) {
                FloatingWindowConfig config = stopFloating.getConfig();
                config.setFindImgTypeEnum(FindImgTypeEnum.ALL.ordinal());
                stopFloating.setConfig(config);
            }
        }
    }

    /**
     * 显示或隐藏要点击的图像识别区域
     */
    @FXML
    private void clickRegionAction() {
        Stage clickStage = clickFloating.getStage();
        if (clickStage != null) {
            clickFloating.setDisableNodes(clickDisableNodes);
            if (!clickStage.isShowing()) {
                // 显示浮窗
                showFloatingWindow(clickFloating);
            } else if (clickStage.isShowing()) {
                // 隐藏浮窗
                hideFloatingWindow(false, clickFloating);
                setNodeDisable(clickFindImgType_Set, false);
                // 只有所有浮窗都关闭时才恢复其他节点可交互状态
                Stage stopStage = stopFloating.getStage();
                Stage massageStage = massageFloating.getStage();
                if ((stopStage == null || !stopStage.isShowing()) &&
                        (massageStage == null || !massageStage.isShowing())) {
                    changeDisableNodes(clickDisableNodes, false);
                }
            }
        }
    }

    /**
     * 显示或隐藏终止操作图像识别区域
     */
    @FXML
    private void stopRegionAction() {
        Stage stopStage = stopFloating.getStage();
        if (stopStage != null) {
            stopFloating.setDisableNodes(stopDisableNodes);
            if (!stopStage.isShowing()) {
                // 显示浮窗
                showFloatingWindow(stopFloating);
            } else if (stopStage.isShowing()) {
                // 隐藏浮窗
                hideFloatingWindow(false, stopFloating);
                setNodeDisable(stopFindImgType_Set, false);
                // 只有所有浮窗都关闭时才恢复其他节点可交互状态
                Stage clickStage = clickFloating.getStage();
                Stage massageStage = massageFloating.getStage();
                if ((clickStage == null || !clickStage.isShowing()) &&
                        (massageStage == null || !massageStage.isShowing())) {
                    changeDisableNodes(stopDisableNodes, false);
                }
            }
        }
    }

    /**
     * 获取要点击的窗口信息
     *
     * @throws IOException 配置文件读取异常
     */
    @FXML
    private void findClickWindowAction() throws IOException {
        // 改变要防重复点击的组件状态
        changeDisableNodes(windowInfoDisableNodes, true);
        // 隐藏主窗口
        mainStage.setIconified(true);
        // 获取准备时间值
        int preparation = setDefaultIntValue(findWindowWait_Set,
                Integer.parseInt(defaultFindWindowWait), 0, null);
        if (clickWindowMonitor != null && !clickWindowMonitor.isFindingWindow()) {
            clickWindowMonitor.startClickWindowMouseListener(preparation);
        }
    }

    /**
     * 获取终止操作窗口信息
     *
     * @throws IOException 配置文件读取异常
     */
    @FXML
    private void findStopWindowAction() throws IOException {
        // 改变要防重复点击的组件状态
        changeDisableNodes(windowInfoDisableNodes, true);
        // 隐藏主窗口
        mainStage.setIconified(true);
        // 获取准备时间值
        int preparation = setDefaultIntValue(findWindowWait_Set,
                Integer.parseInt(defaultFindWindowWait), 0, null);
        if (stopWindowMonitor != null && !stopWindowMonitor.isFindingWindow()) {
            stopWindowMonitor.startClickWindowMouseListener(preparation);
        }
    }

    /**
     * 切换主题下拉框
     */
    @FXML
    private void themeAction() {
        String value = theme_Set.getValue();
        addValueToolTip(theme_Set, tip_theme(), value);
        int them = themeMap.getKey(value);
        changeTheme(them);
        if (mainController != null) {
            Platform.runLater(mainController::mainAdaption);
        }
    }

}
