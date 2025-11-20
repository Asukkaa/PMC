package priv.koishi.pmc.Controller;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.Config.FileChooserConfig;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;
import priv.koishi.pmc.Bean.ImgFileBean;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Bean.VO.ImgFileVO;
import priv.koishi.pmc.Finals.Enum.ClickTypeEnum;
import priv.koishi.pmc.Finals.Enum.FindImgTypeEnum;
import priv.koishi.pmc.Finals.Enum.MatchedTypeEnum;
import priv.koishi.pmc.Finals.Enum.RetryTypeEnum;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfoHandler;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor;
import priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindowDescriptor;
import priv.koishi.pmc.UI.CustomMessageBubble.MessageBubble;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

import static priv.koishi.pmc.Controller.FileChooserController.chooserFiles;
import static priv.koishi.pmc.Controller.MainController.settingController;
import static priv.koishi.pmc.Controller.SettingController.noAutomationPermission;
import static priv.koishi.pmc.Controller.SettingController.windowInfoFloating;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.CommonKeys.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor.creatDefaultWindowInfoHandler;
import static priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor.windowInfoShow;
import static priv.koishi.pmc.MainApplication.mainStage;
import static priv.koishi.pmc.Service.AutoClickService.scriptRun;
import static priv.koishi.pmc.Service.ImageRecognitionService.screenHeight;
import static priv.koishi.pmc.Service.ImageRecognitionService.screenWidth;
import static priv.koishi.pmc.Service.PMCFileService.loadImg;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.*;
import static priv.koishi.pmc.Utils.CommonUtils.isValidUrl;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.ListenerUtils.*;
import static priv.koishi.pmc.Utils.NodeDisableUtils.changeDisableNodes;
import static priv.koishi.pmc.Utils.NodeDisableUtils.setNodeDisable;
import static priv.koishi.pmc.Utils.TableViewUtils.*;
import static priv.koishi.pmc.Utils.TaskUtils.*;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;
import static priv.koishi.pmc.Utils.ToolTipUtils.addValueToolTip;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 操作步骤详情页控制器
 *
 * @author koishi
 * Date 2022/3/11
 * Time 15:09
 */
public class ClickDetailController extends RootController {

    /**
     * 页面数据对象
     */
    private ClickPositionVO selectedItem;

    /**
     * 上次所选要点击的图片地址
     */
    private String clickImgSelectPath;

    /**
     * 上次所选终止操作的图片地址
     */
    public String stopImgSelectPath;

    /**
     * 默认要点击的图片识别重试次数
     */
    private String clickRetryNumDefault;

    /**
     * 默认终止操作图片识别重试次数
     */
    private String stopRetryNumDefault;

    /**
     * 导入文件路径
     */
    private String inFilePath;

    /**
     * 操作列表步骤数量（操作步骤最大跳转序号）
     */
    private int maxIndex;

    /**
     * 页面是否修改标志
     */
    private boolean isModified;

    /**
     * 要防重复点击的组件
     */
    private final List<Node> disableNodes = new ArrayList<>();

    /**
     * 加载图片任务
     */
    private Task<Void> loadImgTask;

    /**
     * 修改内容变化标志监听器（滑块组件专用）
     */
    private final Map<Object, WeakReference<ChangeListener<?>>> weakChangeListeners = new WeakHashMap<>();

    /**
     * 修改内容变化标志监听器
     */
    private final Map<Object, WeakReference<InvalidationListener>> weakInvalidationListeners = new WeakHashMap<>();

    /**
     * 带鼠标悬停提示的内容变化监听器删除器
     */
    private final List<Runnable> listenerRemovers = new ArrayList<>();

    /**
     * 表格结构变化监听器
     */
    private ListChangeListener<ImgFileVO> tableListener;

    /**
     * 页面标识符
     */
    private final String tabId = "_Det";

    /**
     * 详情页页面舞台
     */
    private Stage stage;

    /**
     * 浮窗配置
     */
    private FloatingWindowDescriptor clickFloating, stopFloating;

    /**
     * 窗口信息获取器
     */
    public static WindowMonitor windowMonitorClick, stopWindowMonitor;

    /**
     * 更新数据用的回调函数
     */
    @Setter
    private Runnable refreshCallback;

    @FXML
    public BorderPane borderPane_Det;

    @FXML
    public VBox clickImgVBox_Det, progressBarVBox_Det, clickVBox_Det, vBox_Det, pathLinkVBox_Det, commonVBox_Det;

    @FXML
    public HBox retryStepHBox_Det, matchedStepHBox_Det, clickTypeHBox_Det, clickRegionHBox_Det, stopRegionHBox_Det,
            clickRegionInfoHBox_Det, clickWindowInfoHBox_Det, stopRegionInfoHBox_Det, pathHBox_Det, workDirHBox_Det,
            stopWindowInfoHBox_Det, noPermissionHBox_Det, relativelyHBox_Det, urlHBox_Det, pathLinkHBox_Det,
            parameterHBox_Det, clickKeyHBox_Det;

    @FXML
    public ProgressBar progressBar_Det;

    @FXML
    public Slider clickOpacity_Det, stopOpacity_Det;

    @FXML
    public ImageView clickImg_Det, matchedStepWarning_Det, retryStepWarning_Det;

    @FXML
    public ChoiceBox<String> clickType_Det, retryType_Det, matchedType_Det, clickKey_Det, clickFindImgType_Det,
            stopFindImgType_Det;

    @FXML
    public Button removeClickImg_Det, stopImgBtn_Det, clickImgBtn_Det, removeAll_Det, clickRegion_Det, stopRegion_Det,
            updateClickName_Det, clickWindow_Det, stopWindow_Det, updateCoordinate_Det, pathLink_Det, testLink_Det,
            workDir_Det, removeWorkPath_Det;

    @FXML
    public CheckBox randomClick_Det, randomTrajectory_Det, randomClickTime_Det, randomWaitTime_Det, clickAllRegion_Det,
            stopAllRegion_Det, randomClickInterval_Det, updateClickWindow_Det, updateStopWindow_Det, useRelatively_Det,
            minWindow_Det;

    @FXML
    public Label clickImgPath_Det, dataNumber_Det, clickImgName_Det, clickImgType_Det, clickIndex_Det, link_Det,
            tableViewSize_Det, clickWindowInfo_Det, stopWindowInfo_Det, noPermission_Det, coordinateTypeText_Det,
            clickTypeText_Det, openUrl_Det, workPath_Det, log_Det, pathTip_Det, resolution_Det;

    @FXML
    public TextField clickName_Det, mouseStartX_Det, mouseStartY_Det, wait_Det, clickNumBer_Det, timeClick_Det,
            interval_Det, clickRetryNum_Det, stopRetryNum_Det, retryStep_Det, matchedStep_Det, randomClickX_Det,
            randomClickY_Det, randomTimeOffset_Det, imgX_Det, imgY_Det, relativelyX_Det, relativelyY_Det,
            parameter_Det, url_Det;

    @FXML
    public TableView<ImgFileVO> tableView_Det;

    @FXML
    public TableColumn<ImgFileVO, Integer> index_Det;

    @FXML
    public TableColumn<ImgFileVO, ImageView> thumb_Det;

    @FXML
    public TableColumn<ImgFileVO, String> name_Det, path_Det, type_Det;

    /**
     * 组件宽高自适应
     */
    public void adaption() {
        double tableWidth = stage.getWidth() * 0.5;
        tableView_Det.setMaxWidth(tableWidth);
        tableView_Det.setPrefWidth(tableWidth);
        tableView_Det.setPrefHeight(stage.getHeight() * 0.5);
        progressBarVBox_Det.setPrefWidth(stage.getWidth() * 0.4);
        bindPrefWidthProperty();
    }

    /**
     * 设置 JavaFX 单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_Det.prefWidthProperty().bind(tableView_Det.widthProperty().multiply(0.1));
        thumb_Det.prefWidthProperty().bind(tableView_Det.widthProperty().multiply(0.2));
        name_Det.prefWidthProperty().bind(tableView_Det.widthProperty().multiply(0.2));
        path_Det.prefWidthProperty().bind(tableView_Det.widthProperty().multiply(0.3));
        type_Det.prefWidthProperty().bind(tableView_Det.widthProperty().multiply(0.2));
    }

    /**
     * 初始化数据
     *
     * @param item 列表选中的数据
     * @throws IllegalAccessException 访问属性异常
     */
    public void initData(ClickPositionVO item, String inFilePath) throws IllegalAccessException {
        selectedItem = item;
        this.inFilePath = inFilePath;
        isModified = false;
        maxIndex = selectedItem.getTableView().getItems().size();
        clickIndex_Det.setText(String.valueOf(item.getIndex()));
        String tableViewSize = String.valueOf(maxIndex);
        tableViewSize_Det.setText(tableViewSize);
        // 设置鼠标悬停提示
        setToolTip();
        // 设置文本输入框提示
        setPromptText();
        // 给输入框添加内容变化监听
        nodeValueChangeListener();
        // 初始化操作类型相关组件
        initClickType(item);
        // 初始识别区域设置相关组件
        initFloatingWindowConfig(item);
        // 初始终止操作图片列表
        initStopImg(item.getStopImgFiles());
        // 展示要点击的图片
        showClickImg(item.getClickImgPath());
        imgX_Det.setText(item.getImgX());
        imgY_Det.setText(item.getImgY());
        wait_Det.setText(item.getWaitTime());
        clickName_Det.setText(item.getName());
        clickKey_Det.setValue(item.getClickKey());
        mouseStartX_Det.setText(item.getStartX());
        mouseStartY_Det.setText(item.getStartY());
        timeClick_Det.setText(item.getClickTime());
        clickNumBer_Det.setText(item.getClickNum());
        retryType_Det.setValue(item.getRetryType());
        randomClickX_Det.setText(item.getRandomX());
        randomClickY_Det.setText(item.getRandomY());
        relativelyX_Det.setText(item.getRelativeX());
        relativelyY_Det.setText(item.getRelativeY());
        interval_Det.setText(item.getClickInterval());
        matchedType_Det.setValue(item.getMatchedType());
        stopImgSelectPath = item.getStopImgSelectPath();
        clickImgSelectPath = item.getClickImgSelectPath();
        stopRetryNum_Det.setText(item.getStopRetryTimes());
        randomTimeOffset_Det.setText(item.getRandomTime());
        clickRetryNum_Det.setText(item.getClickRetryTimes());
        randomClick_Det.setSelected(activation.equals(item.getRandomClick()));
        stopOpacity_Det.setValue(Double.parseDouble(item.getStopMatchThreshold()));
        randomWaitTime_Det.setSelected(activation.equals(item.getRandomWaitTime()));
        clickOpacity_Det.setValue(Double.parseDouble(item.getClickMatchThreshold()));
        randomClickTime_Det.setSelected(activation.equals(item.getRandomClickTime()));
        randomTrajectory_Det.setSelected(activation.equals(item.getRandomTrajectory()));
        randomClickInterval_Det.setSelected(activation.equals(item.getRandomClickInterval()));
        String retryStep = "0".equals(item.getRetryStep()) ? "" : item.getRetryStep();
        retryStep_Det.setText(retryStep);
        String matchedStep = "0".equals(item.getMatchedStep()) ? "" : item.getMatchedStep();
        matchedStep_Det.setText(matchedStep);
    }

    /**
     * 初始化操作类型相关组件
     *
     * @param item 列表选中的数据
     */
    private void initClickType(ClickPositionVO item) {
        ObservableList<String> clickTypeItems = clickType_Det.getItems();
        String clickType = item.getClickType();
        clickType_Det.setValue(clickType);
        String targetPath = item.getTargetPath();
        if (CollectionUtils.isEmpty(item.getMoveTrajectory())) {
            clickTypeItems.remove(clickType_moveTrajectory());
            clickTypeItems.remove(clickType_drag());
        } else if (clickType_drag().equals(clickType) || clickType_moveTrajectory().equals(clickType)) {
            clickTypeItems.add(clickType);
            setNodeDisable(clickType_Det, true);
            setNodeDisable(mouseStartX_Det, true);
            setNodeDisable(mouseStartY_Det, true);
        }
        if (clickType_openFile().equals(clickType)) {
            setPathLabel(link_Det, targetPath);
        } else if (clickType_runScript().equals(clickType)) {
            setPathLabel(link_Det, targetPath);
            setPathLabel(workPath_Det, item.getWorkPath());
            parameter_Det.setText(item.getParameter());
            minWindow_Det.setSelected(item.getMinScriptWindow().equals(activation));
        } else if (clickType_openUrl().equals(clickType)) {
            url_Det.setText(targetPath);
        }
    }

    /**
     * 初始终止操作图片列表
     *
     * @param imgFileBeans 要展示的终止操作图片
     */
    private void initStopImg(List<? extends ImgFileBean> imgFileBeans) {
        ObservableList<ImgFileVO> items = tableView_Det.getItems();
        if (CollectionUtils.isNotEmpty(imgFileBeans)) {
            imgFileBeans.forEach(b -> {
                // 必须重新创建对象才能正确刷新列表图片
                ImgFileVO imgFileVO = new ImgFileVO();
                imgFileVO.setTableView(tableView_Det)
                        .setType(b.getType())
                        .setName(b.getName())
                        .setPath(b.getPath());
                items.add(imgFileVO);
            });
            updateTableViewSizeText(tableView_Det, dataNumber_Det, unit_img());
        }
        tableView_Det.refresh();
    }

    /**
     * 初始化窗口信息获取器
     */
    private void initWindowMonitor() {
        stopWindowMonitor = new WindowMonitor(disableNodes, stage);
        stopWindowMonitor.setWindowInfoHandler(creatDefaultWindowInfoHandler(stopWindowInfo_Det));
        FloatingWindowConfig stopWindowConfig = selectedItem.getStopWindowConfig();
        WindowInfo stopWindowInfo = stopWindowConfig.getWindowInfo();
        updateStopWindow_Det.setSelected(activation.equals(stopWindowConfig.getAlwaysRefresh()));
        stopWindowMonitor.setWindowInfo(stopWindowInfo);
        stopWindowMonitor.updateWindowInfo();
        windowMonitorClick = new WindowMonitor(disableNodes, stage);
        windowMonitorClick.setWindowInfoHandler(creatWindowInfoHandler());
        FloatingWindowConfig clickWindowConfig = selectedItem.getClickWindowConfig();
        WindowInfo clickWindowInfo = clickWindowConfig.getWindowInfo();
        updateClickWindow_Det.setSelected(activation.equals(clickWindowConfig.getAlwaysRefresh()));
        windowMonitorClick.setWindowInfo(clickWindowInfo);
        windowMonitorClick.updateWindowInfo();
        WindowInfo windowInfo = windowMonitorClick.getWindowInfo();
        if (windowInfo == null || StringUtils.isBlank(windowInfo.getProcessPath())) {
            setNodeDisable(relativelyHBox_Det, true);
            useRelatively_Det.setSelected(false);
        } else {
            setNodeDisable(relativelyHBox_Det, false);
            useRelatively_Det.setSelected(activation.equals(selectedItem.getUseRelative()));
        }
        useRelatively();
    }

    /**
     * 创建窗口信息处理器
     *
     * @return 窗口信息处理器
     */
    private WindowInfoHandler creatWindowInfoHandler() {
        return new WindowInfoHandler() {

            /**
             * 显示窗口信息接口
             *
             * @param windowInfo 窗口信息
             */
            @Override
            public void showInfo(WindowInfo windowInfo) {
                Platform.runLater(() -> {
                    windowInfoShow(windowInfo, clickWindowInfo_Det);
                    if (windowInfo != null) {
                        setNodeDisable(relativelyHBox_Det, false);
                        if (useRelatively_Det.isSelected()) {
                            calculateAbsolutePosition(windowInfo);
                        } else {
                            calculateRelativePosition(windowInfo);
                        }
                    } else {
                        setNodeDisable(relativelyHBox_Det, true);
                    }
                });
            }

            /**
             * 删除窗口信息接口
             */
            @Override
            public void removeInfo() {
                setNodeDisable(relativelyHBox_Det, true);
                clickWindowInfo_Det.setText("");
                relativelyX_Det.setText("");
                relativelyY_Det.setText("");
            }
        };
    }

    /**
     * 计算相对坐标
     *
     * @param windowInfo 窗口信息
     */
    private void calculateRelativePosition(WindowInfo windowInfo) {
        int x = setDefaultIntValue(mouseStartX_Det, 0, 0, screenWidth);
        int y = setDefaultIntValue(mouseStartY_Det, 0, 0, screenHeight);
        Map<String, String> relativePosition = WindowMonitor.calculateRelativePosition(windowInfo, x, y);
        relativelyX_Det.setText(relativePosition.get(RelativeX));
        relativelyY_Det.setText(relativePosition.get(RelativeY));
    }

    /**
     * 计算绝对坐标
     *
     * @param windowInfo 窗口信息
     */
    private void calculateAbsolutePosition(WindowInfo windowInfo) {
        double relativeX = setDefaultDoubleValue(relativelyX_Det, 0, 0.0, 100.0);
        double relativeY = setDefaultDoubleValue(relativelyY_Det, 0, 0.0, 100.0);
        Map<String, Integer> absolutePosition = WindowMonitor.calculateAbsolutePosition(windowInfo, relativeX, relativeY);
        int x = absolutePosition.get(AbsoluteX);
        int y = absolutePosition.get(AbsoluteY);
        mouseStartX_Det.setText(String.valueOf(x));
        mouseStartY_Det.setText(String.valueOf(y));
    }

    /**
     * 初始识别区域设置相关组件
     *
     * @param item 列表选中的数据
     */
    private void initFloatingWindowConfig(ClickPositionVO item) {
        FloatingWindowConfig clickWindowConfig = item.getClickWindowConfig();
        clickFloating = createFloatingWindowDescriptor();
        if (clickWindowConfig != null) {
            clickFloating.setConfig(clickWindowConfig);
            clickFindImgType_Det.setValue(findImgTypeMap.get(clickWindowConfig.getFindImgTypeEnum()));
            clickAllRegion_Det.setSelected(activation.equals(clickWindowConfig.getAllRegion()));
        } else {
            clickFloating.setConfig(new FloatingWindowConfig());
        }
        clickFloating.setDisableNodes(Collections.singletonList(clickFindImgType_Det))
                .setName(floatingName_click())
                .setButton(clickRegion_Det);
        FloatingWindowConfig stopWindowConfig = item.getStopWindowConfig();
        stopFloating = createFloatingWindowDescriptor();
        if (stopWindowConfig != null) {
            stopFloating.setConfig(stopWindowConfig);
            stopFindImgType_Det.setValue(findImgTypeMap.get(stopWindowConfig.getFindImgTypeEnum()));
            stopAllRegion_Det.setSelected(activation.equals(stopWindowConfig.getAllRegion()));
        } else {
            stopFloating.setConfig(new FloatingWindowConfig());
        }
        stopFloating.setDisableNodes(Collections.singletonList(stopFindImgType_Det))
                .setName(floatingName_stop())
                .setButton(stopRegion_Det);
        // 初始化浮窗
        createFloatingWindows(clickFloating, stopFloating, windowInfoFloating);
    }

    /**
     * 创建浮窗描初始配置
     *
     * @return 浮窗描初始配置
     */
    private FloatingWindowDescriptor createFloatingWindowDescriptor() {
        Color textFill = settingController.colorPicker_Set.getValue();
        return new FloatingWindowDescriptor()
                .setHideButtonToolTip(text_saveCloseFloating())
                .setShowButtonText(findImgSet_showRegion())
                .setHideButtonText(findImgSet_saveRegion())
                .setShowButtonToolTip(tip_showRegion())
                .setMassage(text_saveFindImgConfig())
                .setTextFill(textFill);
    }

    /**
     * 展示要点击的图片
     *
     * @param clickImgPath 要点击的图片路径
     */
    private void showClickImg(String clickImgPath) {
        File clickImgFile = setPathLabel(clickImgPath_Det, clickImgPath);
        if (clickImgFile != null) {
            removeClickImg_Det.setVisible(true);
            if (clickImgFile.exists()) {
                clickImg_Det.setImage(new Image((clickImgFile).toURI().toString()));
                clickImgName_Det.setTextFill(existsFileColor);
                clickImgType_Det.setTextFill(existsFileColor);
            } else {
                clickImg_Det.setImage(null);
                clickImgName_Det.setTextFill(notExistsFileColor);
                clickImgType_Det.setTextFill(notExistsFileColor);
            }
            String imgName = getFileName(clickImgFile.getPath());
            clickImgName_Det.setText(imgName);
            addToolTip(imgName, clickImgName_Det);
            String imgType = getFileType(clickImgFile.getPath());
            clickImgType_Det.setText(imgType);
            addToolTip(imgType, clickImgType_Det);
            clickImgVBox_Det.setVisible(true);
        } else {
            removeClickImg_Det.setVisible(false);
            clickImg_Det.setImage(null);
            clickImgName_Det.setText("");
            clickImgType_Det.setText("");
            clickImgVBox_Det.setVisible(false);
        }
    }

    /**
     * 移除所有监听器
     */
    private void removeAllListeners() {
        clickFloating.dispose();
        stopFloating.dispose();
        if (tableListener != null) {
            tableView_Det.getItems().removeListener(tableListener);
        }
        // 移除修改内容变化标志监听器
        removeInvalidationListeners(weakInvalidationListeners);
        weakInvalidationListeners.clear();
        // 移除修改内容变化标志监听器（滑块组件专用）
        removeWeakReferenceChangeListener(weakChangeListeners);
        weakChangeListeners.clear();
        // 移除带鼠标悬停提示的内容变化监听器
        listenerRemovers.forEach(Runnable::run);
        listenerRemovers.clear();
        // 处理表格监听器引用
        tableListener = null;
    }

    /**
     * 监听页面组件内容是否改动
     */
    private void addModificationListeners() {
        // 通用内容变化监听
        InvalidationListener invalidationListener = _ -> isModified = true;
        // 绑定所有输入控件
        registerWeakInvalidationListener(url_Det, url_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(wait_Det, wait_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(link_Det, link_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(workPath_Det, workPath_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(interval_Det, interval_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(parameter_Det, parameter_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(timeClick_Det, timeClick_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickName_Det, clickName_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(retryStep_Det, retryStep_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(mouseStartX_Det, mouseStartX_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(mouseStartY_Det, mouseStartY_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickNumBer_Det, clickNumBer_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(matchedStep_Det, matchedStep_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(relativelyX_Det, relativelyX_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(relativelyY_Det, relativelyY_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(minWindow_Det, minWindow_Det.selectedProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(stopRetryNum_Det, stopRetryNum_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickImgPath_Det, clickImgPath_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(randomClickX_Det, randomClickX_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(randomClickY_Det, randomClickY_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickRetryNum_Det, clickRetryNum_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(stopWindowInfo_Det, stopWindowInfo_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickWindowInfo_Det, clickWindowInfo_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(useRelatively_Det, useRelatively_Det.selectedProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(stopAllRegion_Det, stopAllRegion_Det.selectedProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickAllRegion_Det, clickAllRegion_Det.selectedProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(randomClickTime_Det, randomClickTime_Det.selectedProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickType_Det, clickType_Det.getSelectionModel().selectedItemProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(retryType_Det, retryType_Det.getSelectionModel().selectedItemProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(matchedType_Det, matchedType_Det.getSelectionModel().selectedItemProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(stopFindImgType_Det, stopFindImgType_Det.getSelectionModel().selectedItemProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickFindImgType_Det, clickFindImgType_Det.getSelectionModel().selectedItemProperty(), invalidationListener, weakInvalidationListeners);
        // 监听滑块改变
        ChangeListener<Number> clickOpacityListener = (_, _, newVal) ->
                isModified = newVal.doubleValue() != Double.parseDouble(selectedItem.getClickMatchThreshold());
        registerWeakListener(stopOpacity_Det, stopOpacity_Det.valueProperty(), clickOpacityListener, weakChangeListeners);
        registerWeakListener(clickOpacity_Det, clickOpacity_Det.valueProperty(), clickOpacityListener, weakChangeListeners);
        // 监听表格内容变化
        tableView_Det.getItems().forEach(item ->
                registerWeakInvalidationListener(item, item.pathProperty(), invalidationListener, weakInvalidationListeners));
        // 监听表格结构变化
        tableListener = buildTableChangeListener(invalidationListener);
        tableView_Det.getItems().addListener(new WeakListChangeListener<>(tableListener));
    }

    /**
     * 构建表格结构变化监听器
     *
     * @param invalidationListener 内容变化监听器
     * @return 表格结构变化监听器
     */
    private ListChangeListener<ImgFileVO> buildTableChangeListener(InvalidationListener invalidationListener) {
        return c -> {
            while (c.next()) {
                // 处理添加与替换事件中的新增元素
                if (c.wasAdded() || c.wasReplaced()) {
                    c.getAddedSubList().forEach(item ->
                            registerWeakInvalidationListener(item, item.pathProperty(),
                                    invalidationListener, weakInvalidationListeners));
                }
                // 处理删除与替换事件中的移除元素
                if (c.wasRemoved() || c.wasReplaced()) {
                    c.getRemoved().forEach(item -> {
                        WeakReference<InvalidationListener> ref = weakInvalidationListeners.get(item);
                        if (ref != null) {
                            InvalidationListener listener = ref.get();
                            if (listener != null) {
                                item.pathProperty().removeListener(listener);
                            }
                            weakInvalidationListeners.remove(item);
                        }
                    });
                }
                // 处理纯更新事件（元素属性变化）
                if (c.wasUpdated()) {
                    IntStream.range(c.getFrom(), c.getTo())
                            .mapToObj(i -> c.getList().get(i))
                            .forEach(item -> {
                                // 先清理旧监听器
                                WeakReference<InvalidationListener> ref = weakInvalidationListeners.get(item);
                                if (ref != null) {
                                    InvalidationListener oldListener = ref.get();
                                    if (oldListener != null) {
                                        item.pathProperty().removeListener(oldListener);
                                    }
                                    weakInvalidationListeners.remove(item);
                                }
                                // 注册新监听器
                                registerWeakInvalidationListener(item, item.pathProperty(),
                                        invalidationListener, weakInvalidationListeners);
                            });
                }
                isModified = true;
            }
        };
    }

    /**
     * 给组件添加内容变化监听
     */
    private void nodeValueChangeListener() {
        // 目标网址文本输入框鼠标悬停提示
        Runnable urlRemover = textFieldValueListener(url_Det, tip_url());
        listenerRemovers.add(urlRemover);
        // 操作名称文本输入框鼠标悬停提示
        Runnable clickNameRemover = textFieldValueListener(clickName_Det, tip_clickName());
        listenerRemovers.add(clickNameRemover);
        // 限制每步操作执行前等待时间文本输入框内容
        Runnable waitRemover = integerRangeTextField(wait_Det, 0, null, tip_wait());
        listenerRemovers.add(waitRemover);
        // 匹配图像坐标横(X)轴偏移量文本输入框内容
        Runnable imgXRemover = integerRangeTextField(imgX_Det, null, null, tip_imgX());
        listenerRemovers.add(imgXRemover);
        // 匹配图像坐标纵(Y)轴偏移量文本输入框内容
        Runnable imgYRemover = integerRangeTextField(imgY_Det, null, null, tip_imgY());
        listenerRemovers.add(imgYRemover);
        // 停止操作图像识别准确度设置监听
        Runnable stopOpacityRemover = integerSliderValueListener(stopOpacity_Det, tip_stopOpacity());
        listenerRemovers.add(stopOpacityRemover);
        // 要点击的图像识别准确度设置监听
        Runnable clickOpacityRemover = integerSliderValueListener(clickOpacity_Det, tip_clickOpacity());
        listenerRemovers.add(clickOpacityRemover);
        // 限制操作时长文本输入内容
        Runnable timeClickRemover = integerRangeTextField(timeClick_Det, 0, null, tip_clickTime());
        listenerRemovers.add(timeClickRemover);
        // 限制操作间隔文本输入框内容
        Runnable intervalRemover = integerRangeTextField(interval_Det, 0, null, tip_clickInterval());
        listenerRemovers.add(intervalRemover);
        // 限制鼠标起始位置横(X)坐标文本输入框内容
        Runnable mouseStartXRemover = integerRangeTextField(mouseStartX_Det, 0, null, tip_mouseStartX());
        listenerRemovers.add(mouseStartXRemover);
        // 限制鼠标起始位置纵(Y)坐标文本输入框内容
        Runnable mouseStartYRemover = integerRangeTextField(mouseStartY_Det, 0, null, tip_mouseStartY());
        listenerRemovers.add(mouseStartYRemover);
        // 限制点击次数文本输入框内容
        Runnable clickNumBerRemover = integerRangeTextField(clickNumBer_Det, 0, null, tip_clickNumBer());
        listenerRemovers.add(clickNumBerRemover);
        // 限制重试后要跳转的步骤序号文本输入框内容
        Runnable retryStepRemover = warnIntegerRangeTextField(retryStep_Det, 1, maxIndex, tip_step(), retryStepWarning_Det);
        listenerRemovers.add(retryStepRemover);
        // 限制点击起始相对横(X)坐标文本输入框内容
        Runnable relativelyXRemover = DoubleRangeTextField(relativelyX_Det, 0.0, 100.0, 2, tip_relatively());
        listenerRemovers.add(relativelyXRemover);
        // 限制点击起始相对纵(Y)坐标文本输入框内容
        Runnable relativelyYRemover = DoubleRangeTextField(relativelyY_Det, 0.0, 100.0, 2, tip_relatively());
        listenerRemovers.add(relativelyYRemover);
        // 限制识别匹配后要跳转的步骤序号文本输入框内容
        Runnable matchedStepRemover = warnIntegerRangeTextField(matchedStep_Det, 1, maxIndex, tip_step(), matchedStepWarning_Det);
        listenerRemovers.add(matchedStepRemover);
        // 随机点击时间偏移量文本输入框内容
        Runnable randomTimeRemover = integerRangeTextField(randomTimeOffset_Det, 0, null, tip_randomTime() + defaultRandomTime);
        listenerRemovers.add(randomTimeRemover);
        // 随机横坐标偏移量文本输入框内容
        Runnable randomClickXRemover = integerRangeTextField(randomClickX_Det, 0, null, tip_randomClickX() + defaultRandomClickX);
        listenerRemovers.add(randomClickXRemover);
        // 随机纵坐标偏移量文本输入框内容
        Runnable randomClickYRemover = integerRangeTextField(randomClickY_Det, 0, null, tip_randomClickY() + defaultRandomClickY);
        listenerRemovers.add(randomClickYRemover);
        // 限制终止操作识别失败重试次数文本输入框内容
        Runnable stopRetryNumRemover = integerRangeTextField(stopRetryNum_Det, 0, null, tip_stopRetryNum() + stopRetryNumDefault);
        listenerRemovers.add(stopRetryNumRemover);
        // 限制要点击的图片识别失败重试次数文本输入框内容
        Runnable clickRetryNumRemover = integerRangeTextField(clickRetryNum_Det, 0, null, tip_clickRetryNum() + clickRetryNumDefault);
        listenerRemovers.add(clickRetryNumRemover);
    }

    /**
     * 设置文本输入框提示
     */
    private void setPromptText() {
        wait_Det.setPromptText(selectedItem.getWaitTime());
        clickName_Det.setPromptText(selectedItem.getName());
        randomClickX_Det.setPromptText(defaultRandomClickX);
        randomClickY_Det.setPromptText(defaultRandomClickY);
        stopRetryNum_Det.setPromptText(stopRetryNumDefault);
        randomTimeOffset_Det.setPromptText(defaultRandomTime);
        clickRetryNum_Det.setPromptText(clickRetryNumDefault);
        mouseStartX_Det.setPromptText(selectedItem.getStartX());
        mouseStartY_Det.setPromptText(selectedItem.getStartY());
        timeClick_Det.setPromptText(selectedItem.getClickTime());
        clickNumBer_Det.setPromptText(selectedItem.getClickNum());
        interval_Det.setPromptText(selectedItem.getClickInterval());
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_url(), url_Det);
        addToolTip(tip_wait(), wait_Det);
        addToolTip(tip_workDir(), workDir_Det);
        addToolTip(tip_testLink(), testLink_Det);
        addToolTip(tip_pathLink(), pathLink_Det);
        addToolTip(tip_minWindow(), minWindow_Det);
        addToolTip(tip_parameter(), parameter_Det);
        addToolTip(tip_clickTime(), timeClick_Det);
        addToolTip(tip_clickName(), clickName_Det);
        addToolTip(tip_stopImgBtn(), stopImgBtn_Det);
        addToolTip(tip_clickInterval(), interval_Det);
        addToolTip(tip_clickImgBtn(), clickImgBtn_Det);
        addToolTip(tip_clickNumBer(), clickNumBer_Det);
        addToolTip(tip_mouseStartX(), mouseStartX_Det);
        addToolTip(tip_mouseStartY(), mouseStartY_Det);
        addToolTip(tip_randomClick(), randomClick_Det);
        addToolTip(tip_removeStopImgBtn(), removeAll_Det);
        addToolTip(tip_useRelatively(), useRelatively_Det);
        addToolTip(tip_removeWorkDir(), removeWorkPath_Det);
        addToolTip(tip_randomWaitTime(), randomWaitTime_Det);
        addToolTip(tip_randomClickTime(), randomClickTime_Det);
        addToolTip(tip_step(), matchedStep_Det, retryStep_Det);
        addToolTip(tip_removeClickImgBtn(), removeClickImg_Det);
        addToolTip(tip_randomTrajectory(), randomTrajectory_Det);
        addToolTip(tip_updateCoordinate(), updateCoordinate_Det);
        addToolTip(tip_updateClickNameBtn(), updateClickName_Det);
        addValueToolTip(imgX_Det, tip_imgX(), imgX_Det.getText());
        addValueToolTip(imgY_Det, tip_imgY(), imgY_Det.getText());
        addToolTip(tip_findWindow(), clickWindow_Det, stopWindow_Det);
        addToolTip(tip_showRegion(), clickRegion_Det, stopRegion_Det);
        addToolTip(tip_randomClickInterval(), randomClickInterval_Det);
        addToolTip(tip_relatively(), relativelyX_Det, relativelyY_Det);
        addToolTip(tip_allRegion(), clickAllRegion_Det, stopAllRegion_Det);
        addValueToolTip(clickKey_Det, tip_clickKey(), clickKey_Det.getValue());
        addValueToolTip(clickType_Det, tip_clickType(), clickType_Det.getValue());
        addValueToolTip(retryType_Det, tip_retryType(), retryType_Det.getValue());
        addToolTip(tip_stopRetryNum() + stopRetryNumDefault, stopRetryNum_Det);
        addToolTip(tip_clickIndex() + clickIndex_Det.getText(), clickIndex_Det);
        addToolTip(tip_alwaysRefresh(), updateStopWindow_Det, updateClickWindow_Det);
        addValueToolTip(clickTypeText_Det, tip_clickType(), clickType_Det.getValue());
        addToolTip(tip_clickRetryNum() + clickRetryNumDefault, clickRetryNum_Det);
        addToolTip(tip_notExistsIndex(), matchedStepWarning_Det, retryStepWarning_Det);
        addValueToolTip(matchedType_Det, tip_matchedType(), matchedType_Det.getValue());
        addValueToolTip(randomClickX_Det, tip_randomClickX() + defaultRandomClickX);
        addValueToolTip(randomClickY_Det, tip_randomClickY() + defaultRandomClickY);
        addValueToolTip(randomTimeOffset_Det, tip_randomTime() + defaultRandomTime);
        addToolTip(tip_tableViewSize() + tableViewSize_Det.getText(), tableViewSize_Det);
        addValueToolTip(stopFindImgType_Det, tip_findImgType(), stopFindImgType_Det.getValue());
        addValueToolTip(clickFindImgType_Det, tip_findImgType(), clickFindImgType_Det.getValue());
        addValueToolTip(stopOpacity_Det, tip_stopOpacity(), String.valueOf((int) stopOpacity_Det.getValue()));
        addValueToolTip(clickOpacity_Det, tip_clickOpacity(), String.valueOf((int) clickOpacity_Det.getValue()));
    }

    /**
     * 读取配置文件
     *
     * @throws IOException 配置文件读取异常
     */
    private void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Click);
        prop.load(input);
        stopRetryNumDefault = prop.getProperty(key_defaultStopRetryNum, defaultStopRetryNum);
        stopRetryNumDefault = StringUtils.isEmpty(stopRetryNumDefault) ? defaultStopRetryNum : stopRetryNumDefault;
        clickRetryNumDefault = prop.getProperty(key_defaultClickRetryNum, defaultClickRetryNum);
        clickRetryNumDefault = StringUtils.isEmpty(clickRetryNumDefault) ? defaultClickRetryNum : clickRetryNumDefault;
        input.close();
    }

    /**
     * 添加确认关闭确认框
     */
    private void addCloseConfirm() {
        // 添加关闭请求监听
        stage.setOnCloseRequest(_ -> {
            CheckBox remindSave = settingController.remindClickSave_Set;
            if (remindSave != null && remindSave.isSelected()) {
                if (isModified || stopFloating.isModified() || clickFloating.isModified()) {
                    ButtonType result = creatConfirmDialog(
                            confirm_unSaved(),
                            confirm_unSavedConfirm(),
                            confirm_ok(),
                            confirm_cancelSave());
                    ButtonBar.ButtonData buttonData = result.getButtonData();
                    if (!buttonData.isCancelButton()) {
                        // 保存并关闭
                        try {
                            saveDetail();
                        } catch (IllegalAccessException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else {
                        // 直接关闭
                        closeStage();
                    }
                }
            }
            closeStage();
        });
    }

    /**
     * 初始化下拉框
     */
    private void setChoiceBoxItems() {
        // 要匹配的图像重试逻辑
        initializeChoiceBoxItems(retryType_Det, retryType_stop(), retryTypeList);
        // 操作类型
        initializeChoiceBoxItems(clickType_Det, clickType_click(), clickTypeList);
        // 点击按键
        initializeChoiceBoxItems(clickKey_Det, mouseButton_primary(), mouseButtonList);
        // 图像识别匹配逻辑
        initializeChoiceBoxItems(matchedType_Det, clickMatched_click(), clickMatchedList);
        // 停止操作图像识别区域设置
        initializeChoiceBoxItems(stopFindImgType_Det, findImgType_all(), findImgTypeList);
        // 要点击的图像识别区域设置
        initializeChoiceBoxItems(clickFindImgType_Det, findImgType_all(), findImgTypeList);
    }

    /**
     * 关闭页面
     */
    private void closeStage() {
        tableView_Det.getItems().stream().parallel().forEach(ImgFileVO::unbindTableView);
        hideFloatingWindow(clickFloating, stopFloating);
        removeAll();
        removeAllListeners();
        windowMonitorClick = null;
        stopWindowMonitor = null;
        if (loadImgTask != null && loadImgTask.isRunning()) {
            loadImgTask.cancel();
        }
        stage.close();
        removeController();
        if (mainStage.isIconified()) {
            showStage(mainStage);
        }
    }

    /**
     * 创建任务参数对象
     *
     * @return 任务参数对象
     */
    private TaskBean<ImgFileVO> creatTaskBean() {
        TaskBean<ImgFileVO> taskBean = new TaskBean<>();
        taskBean.setProgressBar(progressBar_Det)
                .setMassageLabel(dataNumber_Det)
                .setDisableNodes(disableNodes)
                .setTableView(tableView_Det);
        return taskBean;
    }

    /**
     * 启动加载图片任务
     *
     * @param files 要加载的文件
     */
    private void startLoadImgTask(List<? extends File> files) {
        TaskBean<ImgFileVO> taskBean = creatTaskBean();
        loadImgTask = loadImg(taskBean, files);
        bindingTaskNode(loadImgTask, taskBean);
        loadImgTask.setOnSucceeded(_ -> {
            taskUnbind(taskBean);
            updateTableViewSizeText(tableView_Det, dataNumber_Det, unit_img());
            tableView_Det.refresh();
            loadImgTask = null;
        });
        loadImgTask.setOnFailed(event -> {
            taskNotSuccess(taskBean, text_taskFailed());
            loadImgTask = null;
            throw new RuntimeException(event.getSource().getException());
        });
        loadImgTask.setOnCancelled(_ -> {
            taskNotSuccess(taskBean, text_taskCancelled());
            loadImgTask = null;
        });
        Thread.ofVirtual()
                .name("loadImgTask-vThread" + tabId)
                .start(loadImgTask);
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(testLink_Det);
        disableNodes.add(removeAll_Det);
        disableNodes.add(stopImgBtn_Det);
    }

    /**
     * 构建右键菜单
     */
    private void buildContextMenu() {
        // 构建表格右键菜单
        buildTableViewContextMenu(tableView_Det, dataNumber_Det);
        List<Stage> stages = List.of(stage, mainStage);
        // 构建窗口信息栏右键菜单
        buildWindowInfoMenu(stopWindowInfo_Det, stopWindowMonitor, disableNodes, stages);
        buildWindowInfoMenu(clickWindowInfo_Det, windowMonitorClick, disableNodes, stages);
    }

    /**
     * 保存浮窗设置
     *
     * @param floatingVOs 浮窗设置
     */
    private void saveFloatingWindow(FloatingWindowDescriptor... floatingVOs) {
        for (FloatingWindowDescriptor floatingVO : floatingVOs) {
            Stage stage = floatingVO.getStage();
            if (stage != null && stage.isShowing()) {
                FloatingWindowConfig config = floatingVO.getConfig()
                        .setHeight((int) stage.getHeight())
                        .setWidth((int) stage.getWidth())
                        .setX((int) stage.getX())
                        .setY((int) stage.getY());
                floatingVO.setConfig(config);
            }
        }
    }

    /**
     * 更新浮窗配置
     *
     * @param findImgTypeDet    识别区域设置下拉框
     * @param regionInfoHBoxDet 设置指定识别区域的组件所在容器
     * @param regionHBoxDet     选项识别指定范围相关设置的组件所在容器
     * @param windowInfoHBoxDet 选项识别指定窗口相关设置的组件所在容器
     * @param floating          识别范围展示浮窗
     * @param windowMonitor     窗口监控
     * @param checkWindowInfo   是否检查窗口信息为空（true 校验空值）
     */
    private void updateFloatingWindowConfig(ChoiceBox<String> findImgTypeDet, HBox regionInfoHBoxDet, HBox regionHBoxDet,
                                            HBox windowInfoHBoxDet, FloatingWindowDescriptor floating,
                                            WindowMonitor windowMonitor, boolean checkWindowInfo) {
        String value = findImgTypeDet.getValue();
        addValueToolTip(findImgTypeDet, tip_findImgType(), value);
        if (findImgType_region().equals(value)) {
            regionInfoHBoxDet.setVisible(true);
            regionInfoHBoxDet.getChildren().removeAll(regionHBoxDet, windowInfoHBoxDet);
            regionInfoHBoxDet.getChildren().add(regionHBoxDet);
            if (floating != null) {
                FloatingWindowConfig config = floating.getConfig();
                config.setFindImgTypeEnum(FindImgTypeEnum.REGION.ordinal());
                floating.setConfig(config);
            }
        } else if (findImgType_window().equals(value)) {
            regionInfoHBoxDet.setVisible(true);
            regionInfoHBoxDet.getChildren().removeAll(regionHBoxDet, windowInfoHBoxDet);
            regionInfoHBoxDet.getChildren().add(windowInfoHBoxDet);
            if (floating != null) {
                FloatingWindowConfig config = floating.getConfig();
                config.setFindImgTypeEnum(FindImgTypeEnum.WINDOW.ordinal());
                floating.setConfig(config);
                if (windowMonitor != null) {
                    WindowInfo windowInfo = windowMonitor.getWindowInfo();
                    if (checkWindowInfo && windowInfo == null) {
                        throw new RuntimeException(text_windowInfoNull());
                    }
                    config.setWindowInfo(windowInfo);
                }
            }
        } else if (findImgType_all().equals(value)) {
            regionInfoHBoxDet.setVisible(false);
            regionInfoHBoxDet.getChildren().removeAll(regionHBoxDet, windowInfoHBoxDet);
            if (floating != null) {
                FloatingWindowConfig config = floating.getConfig();
                config.setFindImgTypeEnum(FindImgTypeEnum.ALL.ordinal());
                floating.setConfig(config);
            }
        }
    }

    /**
     * 禁用需要自动化权限的组件
     */
    private void setNoPermissionLog() {
        setNodeDisable(stopWindow_Det, true);
        setNodeDisable(clickWindow_Det, true);
        noPermissionHBox_Det.setVisible(true);
        addToolTip(tip_noAutomationPermission(), noPermission_Det);
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
            setPathLabel(link_Det, inFilePath);
        }
    }

    /**
     * 页面初始化
     *
     * @throws IOException 配置文件读取异常
     */
    @FXML
    private void initialize() throws IOException {
        // 初始化下拉框
        setChoiceBoxItems();
        // 读取配置文件
        getConfig();
        Platform.runLater(() -> {
            stage = (Stage) borderPane_Det.getScene().getWindow();
            // 初始化窗口监控器
            initWindowMonitor();
            // 组件宽高自适应
            adaption();
            // 禁用需要自动化权限的组件
            if (noAutomationPermission) {
                setNoPermissionLog();
            }
            // 设置要防重复点击的组件
            setDisableNodes();
            // 添加确认关闭确认框
            addCloseConfirm();
            // 添加控件监听
            addModificationListeners();
            // 自动填充 JavaFX 表格
            autoBuildTableViewData(tableView_Det, ImgFileVO.class, tabId, index_Det);
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Det);
            // 构建右键菜单
            buildContextMenu();
        });
    }

    /**
     * 保存更改并关闭详情页按钮
     *
     * @throws IllegalAccessException 浮窗设置类复制异常
     */
    @FXML
    private void saveDetail() throws IllegalAccessException {
        String url = url_Det.getText();
        String link = link_Det.getText();
        int selectIndex = selectedItem.getIndex();
        int clickNum = setDefaultIntValue(clickNumBer_Det, 1, 1, null);
        Integer clickType = clickTypeMap.getKey(clickType_Det.getValue());
        Integer retryType = retryTypeMap.getKey(retryType_Det.getValue());
        Integer clickKey = recordClickTypeMap.getKey(clickKey_Det.getValue());
        Integer matchedType = matchedTypeMap.getKey(matchedType_Det.getValue());
        String minWindow = minWindow_Det.isSelected() ? activation : unActivation;
        String randomClick = randomClick_Det.isSelected() ? activation : unActivation;
        String useRelatively = useRelatively_Det.isSelected() ? activation : unActivation;
        String stopAllRegion = stopAllRegion_Det.isSelected() ? activation : unActivation;
        String clickAllRegion = clickAllRegion_Det.isSelected() ? activation : unActivation;
        String randomWaitTime = randomWaitTime_Det.isSelected() ? activation : unActivation;
        String randomClickTime = randomClickTime_Det.isSelected() ? activation : unActivation;
        String randomTrajectory = randomTrajectory_Det.isSelected() ? activation : unActivation;
        String stopWindowUpdate = updateStopWindow_Det.isSelected() ? activation : unActivation;
        String clickWindowUpdate = updateClickWindow_Det.isSelected() ? activation : unActivation;
        String randomClickInterval = randomClickInterval_Det.isSelected() ? activation : unActivation;
        if (clickType <= ClickTypeEnum.OPEN_URL.ordinal()) {
            matchedType = MatchedTypeEnum.CLICK.ordinal();
            retryType = RetryTypeEnum.STOP.ordinal();
            clickKey = NativeMouseEvent.NOBUTTON;
            clickNum = 1;
            useRelatively = unActivation;
            if ((clickType == ClickTypeEnum.OPEN_FILE.ordinal() || clickType == ClickTypeEnum.RUN_SCRIPT.ordinal())
                    && StringUtils.isBlank(link)) {
                throw new RuntimeException(text_pathNull());
            } else if (clickType == ClickTypeEnum.OPEN_URL.ordinal()) {
                if (StringUtils.isBlank(url)) {
                    throw new RuntimeException(text_pathNull());
                } else if (isValidUrl(url)) {
                    link = url;
                } else {
                    throw new RuntimeException(text_urlErr());
                }
            }
        } else {
            if (clickType == ClickTypeEnum.MOVE_TRAJECTORY.ordinal()
                    || clickType == ClickTypeEnum.MOVE.ordinal()
                    || clickType == ClickTypeEnum.MOVETO.ordinal()) {
                clickKey = NativeMouseEvent.NOBUTTON;
                clickNum = 1;
            } else if (clickType == ClickTypeEnum.WHEEL_UP.ordinal()
                    || clickType == ClickTypeEnum.WHEEL_DOWN.ordinal()) {
                clickKey = NativeMouseEvent.NOBUTTON;
            }
            updateFloatingWindowConfig(clickFindImgType_Det, clickRegionInfoHBox_Det, clickRegionHBox_Det,
                    clickWindowInfoHBox_Det, clickFloating, windowMonitorClick, true);
            updateFloatingWindowConfig(stopFindImgType_Det, stopRegionInfoHBox_Det, stopRegionHBox_Det,
                    stopWindowInfoHBox_Det, stopFloating, stopWindowMonitor, true);
            saveFloatingWindow(clickFloating, stopFloating);
            FloatingWindowConfig stopFloatingConfig = stopFloating.getConfig();
            FloatingWindowConfig clickFloatingConfig = clickFloating.getConfig();
            stopFloatingConfig.setAllRegion(stopAllRegion)
                    .setAlwaysRefresh(stopWindowUpdate);
            clickFloatingConfig.setAllRegion(clickAllRegion)
                    .setAlwaysRefresh(clickWindowUpdate);
            selectedItem.setStopWindowConfig(stopFloatingConfig)
                    .setClickWindowConfig(clickFloatingConfig);
            if (windowMonitorClick.getWindowInfo() != null) {
                if (FindImgTypeEnum.WINDOW.ordinal() == clickFloatingConfig.getFindImgTypeEnum()) {
                    updateCoordinate();
                    selectedItem.setRelativeX(relativelyX_Det.getText())
                            .setRelativeY(relativelyY_Det.getText());
                }
            }
        }
        selectedItem.setStopImgSelectPath(stopImgSelectPath)
                .setClickImgSelectPath(clickImgSelectPath)
                .setTargetPath(link)
                .setClickKeyEnum(clickKey)
                .setRandomClick(randomClick)
                .setClickTypeEnum(clickType)
                .setRetryTypeEnum(retryType)
                .setMinScriptWindow(minWindow)
                .setUseRelative(useRelatively)
                .setMatchedTypeEnum(matchedType)
                .setName(clickName_Det.getText())
                .setRandomWaitTime(randomWaitTime)
                .setWorkPath(workPath_Det.getText())
                .setRandomClickTime(randomClickTime)
                .setParameter(parameter_Det.getText())
                .setRandomTrajectory(randomTrajectory)
                .setClickNum(String.valueOf(clickNum))
                .setClickImgPath(clickImgPath_Det.getText())
                .setRandomClickInterval(randomClickInterval)
                .setStopImgFiles(new ArrayList<>(tableView_Det.getItems()))
                .setStopMatchThreshold(String.valueOf(stopOpacity_Det.getValue()))
                .setClickMatchThreshold(String.valueOf(clickOpacity_Det.getValue()))
                .setWaitTime(String.valueOf(setDefaultIntValue(wait_Det, 0, 0, null)))
                .setImgX(String.valueOf(setDefaultIntValue(imgX_Det, 0, -screenWidth, screenWidth)))
                .setImgY(String.valueOf(setDefaultIntValue(imgY_Det, 0, -screenHeight, screenHeight)))
                .setStartX(String.valueOf(setDefaultIntValue(mouseStartX_Det, 0, 0, screenWidth)))
                .setStartY(String.valueOf(setDefaultIntValue(mouseStartY_Det, 0, 0, screenHeight)))
                .setClickInterval(String.valueOf(setDefaultIntValue(interval_Det, 0, 0, null)))
                .setClickTime(String.valueOf(setDefaultIntValue(timeClick_Det, Integer.parseInt(defaultClickTimeOffset), 0, null)))
                .setRandomX(String.valueOf(setDefaultIntValue(randomClickX_Det, Integer.parseInt(defaultRandomClickX), 0, screenWidth)))
                .setRandomY(String.valueOf(setDefaultIntValue(randomClickY_Det, Integer.parseInt(defaultRandomClickY), 0, screenHeight)))
                .setRandomTime(String.valueOf(setDefaultIntValue(randomTimeOffset_Det, Integer.parseInt(defaultRandomTime), 0, null)))
                .setStopRetryTimes(String.valueOf(setDefaultIntValue(stopRetryNum_Det, Integer.parseInt(stopRetryNumDefault), 0, null)))
                .setClickRetryTimes(String.valueOf(setDefaultIntValue(clickRetryNum_Det, Integer.parseInt(clickRetryNumDefault), 0, null)));
        if (MatchedTypeEnum.STEP.ordinal() == matchedType || MatchedTypeEnum.CLICK_STEP.ordinal() == matchedType) {
            String matchedStep = matchedStep_Det.getText();
            if (StringUtils.isBlank(matchedStep)) {
                throw new RuntimeException(text_matchedStepIsNull());
            }
            selectedItem.setMatchedStep(matchedStep);
        }
        if (RetryTypeEnum.STEP.ordinal() == retryType) {
            String retryStep = retryStep_Det.getText();
            if (StringUtils.isBlank(retryStep)) {
                throw new RuntimeException(text_retryStepIsNull());
            }
            if (Integer.parseInt(retryStep) == selectIndex) {
                throw new RuntimeException(text_retryStepEqualIndex());
            }
            selectedItem.setRetryStep(retryStep);
        }
        closeStage();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    /**
     * 删除当前步骤按钮
     */
    @FXML
    private void removeDetail() {
        selectedItem.setRemove(true);
        closeStage();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    /**
     * 选择要点击的图片
     *
     * @param actionEvent 点击事件
     */
    @FXML
    private void addClickImgPath(ActionEvent actionEvent) throws IOException {
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatImgFileChooser(window, clickImgSelectPath);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            clickImgSelectPath = updatePathLabel(selectedFile.getPath(), clickImgSelectPath,
                    key_clickImgSelectPath, clickImgPath_Det, configFile_Click);
            showClickImg(clickImgSelectPath);
            clickType_Det.setValue(clickType_click());
            clickTypeHBox_Det.setVisible(true);
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
            // 更新所选文件路径显示
            stopImgSelectPath = updatePathLabel(selectedFile.getPath(), stopImgSelectPath, key_stopImgSelectPath, null, configFile_Click);
            startLoadImgTask(imgFiles);
        }
    }

    /**
     * 删除要点击的图片
     */
    @FXML
    public void removeClickImg() {
        showClickImg(null);
        setNodeDisable(clickType_Det, false);
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    public void handleDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        startLoadImgTask(files);
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    public void acceptDrop(DragEvent dragEvent) {
        acceptDropImg(dragEvent);
    }

    /**
     * 清空图片列表
     */
    @FXML
    private void removeAll() {
        tableView_Det.getItems().stream().parallel().forEach(ImgFileVO::clearResources);
        removeTableViewData(tableView_Det, dataNumber_Det);
    }

    /**
     * 将当前步骤名称更新为图片名称
     */
    @FXML
    private void updateClickName() {
        clickName_Det.setText(clickImgName_Det.getText());
    }

    /**
     * 要匹配的图像重试逻辑下拉框变化
     */
    @FXML
    private void retryTypeChange() {
        String value = retryType_Det.getValue();
        retryStep_Det.setText("");
        retryStepHBox_Det.setVisible(retryType_Step().equals(value));
        addValueToolTip(retryType_Det, tip_retryType(), value);
    }

    /**
     * 识别匹配后逻辑下拉框变化
     */
    @FXML
    private void matchedTypeChange() {
        String value = matchedType_Det.getValue();
        matchedStep_Det.setText("");
        matchedStepHBox_Det.setVisible(clickMatched_step().equals(value) ||
                clickMatched_clickStep().equals(value));
        addValueToolTip(matchedType_Det, tip_matchedType(), value);
    }

    /**
     * 操作类型下拉框变化
     */
    @FXML
    private void clickTypeChange() {
        String value = clickType_Det.getValue();
        addValueToolTip(clickType_Det, tip_clickType(), value);
        addValueToolTip(clickTypeText_Det, tip_clickType(), value);
        setPathLabel(link_Det, null);
        vBox_Det.getChildren().clear();
        vBox_Det.getChildren().add(commonVBox_Det);
        if (linkList.contains(value)) {
            vBox_Det.getChildren().add(pathLinkVBox_Det);
            testLink_Det.setVisible(true);
            resolution_Det.setVisible(false);
            randomClickInterval_Det.setVisible(false);
            pathLinkVBox_Det.getChildren().removeAll(parameterHBox_Det, pathTip_Det);
            pathLinkHBox_Det.getChildren().clear();
            if (clickType_openFile().equals(value)) {
                pathLinkVBox_Det.getChildren().add(pathTip_Det);
                pathLinkHBox_Det.getChildren().add(pathHBox_Det);
                workDirHBox_Det.setVisible(false);
                pathTip_Det.setText(pathTip_openFile());
            } else if (clickType_runScript().equals(value)) {
                pathLinkVBox_Det.getChildren().addAll(parameterHBox_Det, pathTip_Det);
                pathLinkHBox_Det.getChildren().add(pathHBox_Det);
                workDirHBox_Det.setVisible(true);
                pathTip_Det.setText(pathTip_runScript());
            } else if (clickType_openUrl().equals(value)) {
                pathLinkVBox_Det.getChildren().add(pathTip_Det);
                pathLinkHBox_Det.getChildren().add(urlHBox_Det);
                pathTip_Det.setText(pathTip_openUrl());
            }
        } else {
            clickTypeHBox_Det.setVisible(!clickType_move().equals(value) && !clickType_moveTo().equals(value));
            clickKeyHBox_Det.setVisible(!clickType_move().equals(value)
                    && !clickType_moveTo().equals(value)
                    && !clickType_moveTrajectory().equals(value)
                    && !clickType_wheelUp().equals(value)
                    && !clickType_wheelDown().equals(value));
            vBox_Det.getChildren().add(clickVBox_Det);
            testLink_Det.setVisible(false);
            randomClickInterval_Det.setVisible(true);
            pathTip_Det.setText("");
            resolution_Det.setVisible(true);
        }
        ObservableList<String> clickItems = clickKey_Det.getItems();
        if (CollectionUtils.isNotEmpty(clickItems) && !clickItems.contains(clickKey_Det.getValue())) {
            clickKey_Det.setValue(clickKey_Det.getItems().getFirst());
        }
    }

    /**
     * 点击按键下拉框变化
     */
    @FXML
    private void clickKeyChange() {
        addValueToolTip(clickKey_Det, tip_clickKey(), clickKey_Det.getValue());
    }

    /**
     * 要点击的图像识别区域下拉框
     */
    @FXML
    private void clickFindImgTypeAction() {
        updateFloatingWindowConfig(clickFindImgType_Det, clickRegionInfoHBox_Det, clickRegionHBox_Det,
                clickWindowInfoHBox_Det, clickFloating, windowMonitorClick, false);
        if (findImgType_window().equals(clickFindImgType_Det.getValue())) {
            setNodeDisable(relativelyHBox_Det, false);
            useRelatively();
        } else {
            setNodeDisable(relativelyHBox_Det, true);
            setNodeDisable(mouseStartX_Det, false);
            setNodeDisable(mouseStartY_Det, false);
        }
    }

    /**
     * 终止操作图像识别区域下拉框
     */
    @FXML
    private void stopFindImgTypeAction() {
        updateFloatingWindowConfig(stopFindImgType_Det, stopRegionInfoHBox_Det, stopRegionHBox_Det,
                stopWindowInfoHBox_Det, stopFloating, stopWindowMonitor, false);
    }

    /**
     * 显示或隐藏要点击的图像识别区域
     */
    @FXML
    private void clickRegionAction() {
        Stage floatingStage = clickFloating.getStage();
        if (floatingStage != null) {
            if (!floatingStage.isShowing()) {
                // 显示浮窗
                showFloatingWindow(clickFloating);
            } else if (floatingStage.isShowing()) {
                // 隐藏浮窗
                hideFloatingWindow(clickFloating);
            }
        }
    }

    /**
     * 显示或隐藏终止操作图像识别区域
     */
    @FXML
    private void stopRegionAction() {
        Stage floatingStage = stopFloating.getStage();
        if (floatingStage != null) {
            if (!floatingStage.isShowing()) {
                // 显示浮窗
                showFloatingWindow(stopFloating);
            } else if (floatingStage.isShowing()) {
                // 隐藏浮窗
                hideFloatingWindow(stopFloating);
            }
        }
    }

    /**
     * 获取要点击的窗口信息
     */
    @FXML
    private void findClickWindowAction() {
        // 改变要防重复点击的组件状态
        changeDisableNodes(disableNodes, true);
        // 隐藏窗口
        mainStage.setIconified(true);
        stage.setIconified(true);
        // 获取准备时间值
        int preparation = setDefaultIntValue(settingController.findWindowWait_Set,
                Integer.parseInt(defaultPreparationRecord), 0, null);
        if (windowMonitorClick != null) {
            windowMonitorClick.startClickWindowMouseListener(preparation);
        }
    }

    /**
     * 获取终止操作窗口信息
     */
    @FXML
    private void findStopWindowAction() {
        // 改变要防重复点击的组件状态
        changeDisableNodes(disableNodes, true);
        // 隐藏窗口
        mainStage.setIconified(true);
        stage.setIconified(true);
        // 获取准备时间值
        int preparation = setDefaultIntValue(settingController.findWindowWait_Set,
                Integer.parseInt(defaultPreparationRecord), 0, null);
        if (stopWindowMonitor != null && !stopWindowMonitor.isFindingWindow()) {
            stopWindowMonitor.startClickWindowMouseListener(preparation);
        }
    }

    /**
     * 更新坐标信息按钮
     */
    @FXML
    private void updateCoordinate() {
        windowMonitorClick.updateWindowInfo();
        WindowInfo windowInfo = windowMonitorClick.getWindowInfo();
        if (windowInfo != null) {
            if (useRelatively_Det.isSelected()) {
                calculateAbsolutePosition(windowInfo);
            } else {
                calculateRelativePosition(windowInfo);
            }
        }
    }

    /**
     * 使用相对坐标开关
     */
    @FXML
    private void useRelatively() {
        if (useRelatively_Det.isSelected()) {
            setNodeDisable(mouseStartX_Det, true);
            setNodeDisable(mouseStartY_Det, true);
            setNodeDisable(relativelyX_Det, false);
            setNodeDisable(relativelyY_Det, false);
        } else {
            setNodeDisable(mouseStartX_Det, false);
            setNodeDisable(mouseStartY_Det, false);
            setNodeDisable(relativelyX_Det, true);
            setNodeDisable(relativelyY_Det, true);
        }
    }

    /**
     * 选择目标路径按钮
     *
     * @param actionEvent 点击事件
     * @throws IOException 配置文件读取异常、配置文件保存异常、页面加载失败
     */
    @FXML
    private void pathLinkAction(ActionEvent actionEvent) throws IOException {
        String value = clickType_Det.getValue();
        if (clickType_openFile().equals(value)) {
            FileChooserConfig fileConfig = new FileChooserConfig();
            fileConfig.setTitle(text_selectFileFolder())
                    .setConfigPath(configFile_Click)
                    .setPathKey(key_inFilePath)
                    .setShowDirectory(search_fileDirectory())
                    .setShowHideFile(hide_noHideFile())
                    .setPath(inFilePath);
            FileChooserController controller = chooserFiles(fileConfig);
            // 设置回调
            controller.setFileChooserCallback(this::getSelectFile);
        } else if (clickType_runScript().equals(value)) {
            Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
            List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
            if (isWin) {
                extensionFilters.add(new FileChooser.ExtensionFilter(text_script(), allBat, allCmd, allPy, allPs1, allJava, allJar, allClass));
                extensionFilters.add(new FileChooser.ExtensionFilter(text_script(), allBat));
                extensionFilters.add(new FileChooser.ExtensionFilter(text_script(), allCmd));
            } else {
                extensionFilters.add(new FileChooser.ExtensionFilter(text_script(), allSh, allBash, allPy, allPs1, allJava, allJar, allClass));
                extensionFilters.add(new FileChooser.ExtensionFilter(text_script(), allSh));
                extensionFilters.add(new FileChooser.ExtensionFilter(text_script(), allBash));
            }
            extensionFilters.add(new FileChooser.ExtensionFilter(text_script(), allPy));
            extensionFilters.add(new FileChooser.ExtensionFilter(text_script(), allPs1));
            extensionFilters.add(new FileChooser.ExtensionFilter(text_script(), allJava));
            extensionFilters.add(new FileChooser.ExtensionFilter(text_script(), allJar));
            extensionFilters.add(new FileChooser.ExtensionFilter(text_script(), allClass));
            List<File> selectedFile = creatFilesChooser(window, inFilePath, extensionFilters, text_selectAutoFile());
            getSelectFile(selectedFile);
        }
    }

    /**
     * 测试目标路径按钮
     *
     * @throws Exception 网址打开失败
     */
    @FXML
    private void testLinkAction() throws Exception {
        String value = clickType_Det.getValue();
        int time = 2;
        if (clickType_openFile().equals(value)) {
            String path = link_Det.getText();
            if (StringUtils.isNotBlank(path)) {
                File file = new File(path);
                if (!file.exists()) {
                    new MessageBubble(text_fileNotExists(), time);
                } else {
                    openFile(path);
                    new MessageBubble(text_testSuccess(), time);
                }
            } else {
                new MessageBubble(text_pathNull(), time);
            }
        } else if (clickType_runScript().equals(value)) {
            String path = link_Det.getText();
            if (StringUtils.isNotBlank(path)) {
                File file = new File(path);
                if (!file.exists()) {
                    new MessageBubble(text_fileNotExists(), time);
                } else {
                    String workDir = workPath_Det.getText();
                    String parameter = parameter_Det.getText();
                    TaskBean<?> taskBean = creatTaskBean()
                            .setMassageLabel(log_Det);
                    Task<Void> scriptTask = scriptRun(taskBean, file, workDir, parameter, minWindow_Det.isSelected());
                    bindingTaskNode(scriptTask, taskBean);
                    scriptTask.setOnSucceeded(_ -> {
                        taskUnbind(taskBean);
                        new MessageBubble(text_testSuccess(), time);
                    });
                    Thread.ofVirtual()
                            .name("scriptTask-vThread" + tabId)
                            .start(scriptTask);
                }
            } else {
                new MessageBubble(text_pathNull(), time);
            }
        } else if (clickType_openUrl().equals(value)) {
            String url = url_Det.getText();
            if (StringUtils.isNotBlank(url)) {
                if (isValidUrl(url)) {
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                        new MessageBubble(text_testSuccess(), time);
                    } catch (IOException e) {
                        new MessageBubble(text_urlErr(), time);
                    }
                } else {
                    new MessageBubble(text_urlErr(), time);
                }
            } else {
                new MessageBubble(text_pathNull(), time);
            }
        }
    }

    /**
     * 选择工作路径按钮
     *
     * @param actionEvent 点击事件
     * @throws IOException 配置文件读取异常、配置文件保存异常、页面加载失败
     */
    @FXML
    private void workPathAction(ActionEvent actionEvent) throws IOException {
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        String outFilePath = workPath_Det.getText();
        File selectedFile = creatDirectoryChooser(window, outFilePath, text_selectDirectory());
        if (selectedFile != null) {
            inFilePath = selectedFile.getAbsolutePath();
            updateProperties(configFile_Click, key_inFilePath, inFilePath);
            setPathLabel(workPath_Det, inFilePath);
            removeWorkPath_Det.setVisible(true);
        }
    }

    /**
     * 删除工作路径
     */
    @FXML
    private void removeWorkPath() {
        setPathLabel(workPath_Det, "");
        removeWorkPath_Det.setVisible(false);
    }

}
