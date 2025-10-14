package priv.koishi.pmc.Controller;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;
import priv.koishi.pmc.Bean.ImgFileBean;
import priv.koishi.pmc.Bean.TaskBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.Bean.VO.ImgFileVO;
import priv.koishi.pmc.Finals.Enum.FindImgTypeEnum;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowInfo;
import priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor;
import priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindowDescriptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.IntStream;

import static priv.koishi.pmc.Controller.MainController.settingController;
import static priv.koishi.pmc.Controller.SettingController.noAutomationPermission;
import static priv.koishi.pmc.Controller.SettingController.windowInfoFloating;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.MainApplication.mainStage;
import static priv.koishi.pmc.Service.AutoClickService.loadImg;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.*;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.ListenerUtils.*;
import static priv.koishi.pmc.Utils.TaskUtils.*;
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
     * 修改内容变化标志监听器
     */
    private final Map<Object, WeakReference<ChangeListener<?>>> weakChangeListeners = new WeakHashMap<>();

    /**
     * 修改内容变化标志监听器（滑块组件专用）
     */
    private final Map<Object, WeakReference<InvalidationListener>> weakInvalidationListeners = new WeakHashMap<>();

    /**
     * 带鼠标悬停提示的内容变化监听器
     */
    private final Map<Object, ChangeListener<?>> changeListeners = new WeakHashMap<>();

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
    public AnchorPane anchorPane_Det;

    @FXML
    public VBox clickImgVBox_Det, progressBarVBox_Det;

    @FXML
    public HBox fileNumberHBox_Det, retryStepHBox_Det, matchedStepHBox_Det, clickTypeHBox_Det, clickRegionHBox_Det,
            stopRegionHBox_Det, clickRegionInfoHBox_Det, clickWindowInfoHBox_Det, stopRegionInfoHBox_Det,
            stopWindowInfoHBox_Det, noPermissionHBox_Det;

    @FXML
    public ProgressBar progressBar_Det;

    @FXML
    public Slider clickOpacity_Det, stopOpacity_Det;

    @FXML
    public ImageView clickImg_Det, matchedStepWarning_Det, retryStepWarning_Det;

    @FXML
    public ChoiceBox<String> clickType_Det, retryType_Det, matchedType_Det, clickKey_Det, clickFindImgType_Det,
            stopFindImgType_Det, coordinateType_Det;

    @FXML
    public Button removeClickImg_Det, stopImgBtn_Det, clickImgBtn_Det, removeAll_Det, clickRegion_Det, stopRegion_Det,
            updateClickName_Det, clickWindow_Det, stopWindow_Det, updateCoordinate_Det;

    @FXML
    public CheckBox randomClick_Det, randomTrajectory_Det, randomClickTime_Det, randomWaitTime_Det, clickAllRegion_Det,
            stopAllRegion_Det, randomClickInterval_Det, updateClickWindow_Det, updateStopWindow_Det;

    @FXML
    public Label clickImgPath_Det, dataNumber_Det, clickImgName_Det, clickImgType_Det, clickIndex_Det, clickTypeText_Det,
            tableViewSize_Det, clickWindowInfo_Det, stopWindowInfo_Det, noPermission_Det, coordinateTypeText_Det;

    @FXML
    public TextField clickName_Det, mouseStartX_Det, mouseStartY_Det, wait_Det, clickNumBer_Det, timeClick_Det,
            interval_Det, clickRetryNum_Det, stopRetryNum_Det, retryStep_Det, matchedStep_Det, randomClickX_Det,
            randomClickY_Det, randomTimeOffset_Det, imgX_Det, imgY_Det, relativelyX_Det, relativelyY_Det;

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
        tableView_Det.setPrefHeight(stage.getHeight() * 0.4);
        regionRightAlignment(fileNumberHBox_Det, tableWidth, dataNumber_Det);
        progressBarVBox_Det.setPrefWidth(stage.getWidth() * 0.4);
        bindPrefWidthProperty();
    }

    /**
     * 设置 javaFX 单元格宽度
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
    public void initData(ClickPositionVO item) throws IllegalAccessException {
        selectedItem = item;
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
        if (CollectionUtils.isEmpty(item.getMoveTrajectory())) {
            clickTypeItems.remove(clickType_moveTrajectory());
            clickTypeItems.remove(clickType_drag());
        } else if (clickType_drag().equals(clickType) || clickType_moveTrajectory().equals(clickType)) {
            clickTypeItems.add(clickType);
            clickType_Det.setDisable(true);
            mouseStartX_Det.setDisable(true);
            mouseStartY_Det.setDisable(true);
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
        stopWindowMonitor = new WindowMonitor(stopWindowInfo_Det, disableNodes, stage);
        FloatingWindowConfig stopWindowConfig = selectedItem.getStopWindowConfig();
        WindowInfo stopWindowInfo = stopWindowConfig.getWindowInfo();
        updateStopWindow_Det.setSelected(activation.equals(stopWindowConfig.getAlwaysRefresh()));
        stopWindowMonitor.setWindowInfo(stopWindowInfo);
        stopWindowMonitor.updateWindowInfo();
        windowMonitorClick = new WindowMonitor(clickWindowInfo_Det, disableNodes, stage);
        FloatingWindowConfig clickWindowConfig = selectedItem.getClickWindowConfig();
        WindowInfo clickWindowInfo = clickWindowConfig.getWindowInfo();
        updateClickWindow_Det.setSelected(activation.equals(clickWindowConfig.getAlwaysRefresh()));
        windowMonitorClick.setWindowInfo(clickWindowInfo);
        windowMonitorClick.updateWindowInfo();
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
        // 移除修改内容变化标志监听器（滑块组件专用）
        removeInvalidationListeners(weakInvalidationListeners);
        weakInvalidationListeners.clear();
        // 移除修改内容变化标志监听器
        removeWeakReferenceChangeListener(weakChangeListeners);
        weakChangeListeners.clear();
        // 移除带鼠标悬停提示的内容变化监听器
        removeChangeListener(changeListeners);
        changeListeners.clear();
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
        registerWeakInvalidationListener(wait_Det, wait_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(interval_Det, interval_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(timeClick_Det, timeClick_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickName_Det, clickName_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(retryStep_Det, retryStep_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(mouseStartX_Det, mouseStartX_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(mouseStartY_Det, mouseStartY_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickNumBer_Det, clickNumBer_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(matchedStep_Det, matchedStep_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(stopRetryNum_Det, stopRetryNum_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickImgPath_Det, clickImgPath_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(randomClickX_Det, randomClickX_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(randomClickY_Det, randomClickY_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickRetryNum_Det, clickRetryNum_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(stopAllRegion_Det, stopAllRegion_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(clickAllRegion_Det, clickAllRegion_Det.textProperty(), invalidationListener, weakInvalidationListeners);
        registerWeakInvalidationListener(randomClickTime_Det, randomClickTime_Det.textProperty(), invalidationListener, weakInvalidationListeners);
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
        // 操作名称文本输入框鼠标悬停提示
        ChangeListener<String> clickNameListener = textFieldValueListener(clickName_Det, tip_clickName());
        changeListeners.put(clickName_Det, clickNameListener);
        // 限制每步操作执行前等待时间文本输入框内容
        ChangeListener<Boolean> waitListener = integerRangeTextField(wait_Det, 0, null, tip_wait());
        changeListeners.put(wait_Det, waitListener);
        // 匹配图像坐标横(X)轴偏移量文本输入框内容
        ChangeListener<Boolean> imgXListener = integerRangeTextField(imgX_Det, null, null, tip_imgX());
        changeListeners.put(imgX_Det, imgXListener);
        // 匹配图像坐标纵(Y)轴偏移量文本输入框内容
        ChangeListener<Boolean> imgYListener = integerRangeTextField(imgY_Det, null, null, tip_imgY());
        changeListeners.put(imgY_Det, imgYListener);
        // 停止操作图像识别准确度设置监听
        ChangeListener<Number> stopOpacityListener = integerSliderValueListener(stopOpacity_Det, tip_stopOpacity());
        changeListeners.put(stopOpacity_Det, stopOpacityListener);
        // 要点击的图像识别准确度设置监听
        ChangeListener<Number> clickOpacityListener = integerSliderValueListener(clickOpacity_Det, tip_clickOpacity());
        changeListeners.put(clickOpacity_Det, clickOpacityListener);
        // 限制操作时长文本输入内容
        ChangeListener<Boolean> timeClickListener = integerRangeTextField(timeClick_Det, 0, null, tip_clickTime());
        changeListeners.put(timeClick_Det, timeClickListener);
        // 限制操作间隔文本输入框内容
        ChangeListener<Boolean> intervalListener = integerRangeTextField(interval_Det, 0, null, tip_clickInterval());
        changeListeners.put(interval_Det, intervalListener);
        // 限制鼠标起始位置横(X)坐标文本输入框内容
        ChangeListener<Boolean> mouseStartXListener = integerRangeTextField(mouseStartX_Det, 0, null, tip_mouseStartX());
        changeListeners.put(mouseStartX_Det, mouseStartXListener);
        // 限制鼠标起始位置纵(Y)坐标文本输入框内容
        ChangeListener<Boolean> mouseStartYListener = integerRangeTextField(mouseStartY_Det, 0, null, tip_mouseStartY());
        changeListeners.put(mouseStartY_Det, mouseStartYListener);
        // 限制点击次数文本输入框内容
        ChangeListener<Boolean> clickNumBerListener = integerRangeTextField(clickNumBer_Det, 0, null, tip_clickNumBer());
        changeListeners.put(clickNumBer_Det, clickNumBerListener);
        // 限制重试后要跳转的步骤序号文本输入框内容
        ChangeListener<String> retryStepListener = warnIntegerRangeTextField(retryStep_Det, 1, maxIndex, tip_step(), retryStepWarning_Det);
        changeListeners.put(retryStep_Det, retryStepListener);
        // 限制点击起始相对横(X)坐标文本输入框内容
        ChangeListener<Boolean> relativelyXListener = DoubleRangeTextField(relativelyX_Det, 0.0, 100.0, 2, tip_relatively());
        changeListeners.put(relativelyX_Det, relativelyXListener);
        // 限制点击起始相对纵(Y)坐标文本输入框内容
        ChangeListener<Boolean> relativelyYListener = DoubleRangeTextField(relativelyY_Det, 0.0, 100.0, 2, tip_relatively());
        changeListeners.put(relativelyY_Det, relativelyYListener);
        // 限制识别匹配后要跳转的步骤序号文本输入框内容
        ChangeListener<String> matchedStepListener = warnIntegerRangeTextField(matchedStep_Det, 1, maxIndex, tip_step(), matchedStepWarning_Det);
        changeListeners.put(matchedStep_Det, matchedStepListener);
        // 随机点击时间偏移量文本输入框内容
        ChangeListener<Boolean> randomTimeListener = integerRangeTextField(randomTimeOffset_Det, 0, null, tip_randomTime() + defaultRandomTime);
        changeListeners.put(randomTimeOffset_Det, randomTimeListener);
        // 随机横坐标偏移量文本输入框内容
        ChangeListener<Boolean> randomClickXListener = integerRangeTextField(randomClickX_Det, 0, null, tip_randomClickX() + defaultRandomClickX);
        changeListeners.put(randomClickX_Det, randomClickXListener);
        // 随机纵坐标偏移量文本输入框内容
        ChangeListener<Boolean> randomClickYListener = integerRangeTextField(randomClickY_Det, 0, null, tip_randomClickY() + defaultRandomClickY);
        changeListeners.put(randomClickY_Det, randomClickYListener);
        // 限制终止操作识别失败重试次数文本输入框内容
        ChangeListener<Boolean> stopRetryNumListener = integerRangeTextField(stopRetryNum_Det, 0, null, tip_stopRetryNum() + stopRetryNumDefault);
        changeListeners.put(stopRetryNum_Det, stopRetryNumListener);
        // 限制要点击的图片识别失败重试次数文本输入框内容
        ChangeListener<Boolean> clickRetryNumListener = integerRangeTextField(clickRetryNum_Det, 0, null, tip_clickRetryNum() + clickRetryNumDefault);
        changeListeners.put(clickRetryNum_Det, clickRetryNumListener);
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
        addToolTip(tip_wait(), wait_Det);
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
        addToolTip(tip_randomWaitTime(), randomWaitTime_Det);
        addToolTip(tip_randomClickTime(), randomClickTime_Det);
        addToolTip(tip_step(), matchedStep_Det, retryStep_Det);
        addToolTip(tip_removeClickImgBtn(), removeClickImg_Det);
        addToolTip(tip_randomTrajectory(), randomTrajectory_Det);
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
        // 要点击的图像识别区域设置
        initializeChoiceBoxItems(coordinateType_Det, absoluteCoordinates(), coordinateTypeList);
    }

    /**
     * 关闭页面
     */
    private void closeStage() {
        hideFloatingWindow(clickFloating, stopFloating);
        removeAll();
        removeAllListeners();
        windowMonitorClick = null;
        stopWindowMonitor = null;
        if (loadImgTask != null && loadImgTask.isRunning()) {
            loadImgTask.cancel();
        }
        stage.close();
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
                .setTableView(tableView_Det)
                .setDisableNodes(disableNodes);
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
        stopWindow_Det.setDisable(true);
        clickWindow_Det.setDisable(true);
        noPermissionHBox_Det.setVisible(true);
        addToolTip(tip_noAutomationPermission(), noPermission_Det);
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
            stage = (Stage) anchorPane_Det.getScene().getWindow();
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
            // 自动填充 javaFX 表格
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
        updateFloatingWindowConfig(clickFindImgType_Det, clickRegionInfoHBox_Det, clickRegionHBox_Det,
                clickWindowInfoHBox_Det, clickFloating, windowMonitorClick, true);
        updateFloatingWindowConfig(stopFindImgType_Det, stopRegionInfoHBox_Det, stopRegionHBox_Det,
                stopWindowInfoHBox_Det, stopFloating, stopWindowMonitor, true);
        saveFloatingWindow(clickFloating, stopFloating);
        String randomClick = randomClick_Det.isSelected() ? activation : unActivation;
        String stopAllRegion = stopAllRegion_Det.isSelected() ? activation : unActivation;
        String clickAllRegion = clickAllRegion_Det.isSelected() ? activation : unActivation;
        String randomWaitTime = randomWaitTime_Det.isSelected() ? activation : unActivation;
        String randomClickTime = randomClickTime_Det.isSelected() ? activation : unActivation;
        String randomTrajectory = randomTrajectory_Det.isSelected() ? activation : unActivation;
        String stopWindowUpdate = updateStopWindow_Det.isSelected() ? activation : unActivation;
        String clickWindowUpdate = updateClickWindow_Det.isSelected() ? activation : unActivation;
        String randomClickInterval = randomClickInterval_Det.isSelected() ? activation : unActivation;
        FloatingWindowConfig stopFloatingConfig = stopFloating.getConfig();
        FloatingWindowConfig clickFloatingConfig = clickFloating.getConfig();
        stopFloatingConfig.setAllRegion(stopAllRegion)
                .setAlwaysRefresh(stopWindowUpdate);
        clickFloatingConfig.setAllRegion(clickAllRegion)
                .setAlwaysRefresh(clickWindowUpdate);
        int selectIndex = selectedItem.getIndex();
        String matchedType = matchedType_Det.getValue();
        selectedItem.setStopImgSelectPath(stopImgSelectPath)
                .setClickImgSelectPath(clickImgSelectPath)
                .setRandomClick(randomClick)
                .setName(clickName_Det.getText())
                .setRandomWaitTime(randomWaitTime)
                .setRandomClickTime(randomClickTime)
                .setRandomTrajectory(randomTrajectory)
                .setStopWindowConfig(stopFloatingConfig)
                .setClickWindowConfig(clickFloatingConfig)
                .setClickImgPath(clickImgPath_Det.getText())
                .setRandomClickInterval(randomClickInterval)
                .setMatchedTypeEnum(matchedTypeMap.getKey(matchedType))
                .setStopImgFiles(new ArrayList<>(tableView_Det.getItems()))
                .setClickTypeEnum(clickTypeMap.getKey(clickType_Det.getValue()))
                .setStopMatchThreshold(String.valueOf(stopOpacity_Det.getValue()))
                .setClickMatchThreshold(String.valueOf(clickOpacity_Det.getValue()))
                .setClickKeyEnum(recordClickTypeMap.getKey(clickKey_Det.getValue()))
                .setImgX(String.valueOf(setDefaultIntValue(imgX_Det, 0, null, null)))
                .setImgY(String.valueOf(setDefaultIntValue(imgY_Det, 0, null, null)))
                .setWaitTime(String.valueOf(setDefaultIntValue(wait_Det, 0, 0, null)))
                .setStartX(String.valueOf(setDefaultIntValue(mouseStartX_Det, 0, 0, null)))
                .setStartY(String.valueOf(setDefaultIntValue(mouseStartY_Det, 0, 0, null)))
                .setClickNum(String.valueOf(setDefaultIntValue(clickNumBer_Det, 1, 1, null)))
                .setClickInterval(String.valueOf(setDefaultIntValue(interval_Det, 0, 0, null)))
                .setRandomX(String.valueOf(setDefaultIntValue(randomClickX_Det, Integer.parseInt(defaultRandomClickX), 0, null)))
                .setRandomY(String.valueOf(setDefaultIntValue(randomClickY_Det, Integer.parseInt(defaultRandomClickY), 0, null)))
                .setClickTime(String.valueOf(setDefaultIntValue(timeClick_Det, Integer.parseInt(defaultClickTimeOffset), 0, null)))
                .setRandomTime(String.valueOf(setDefaultIntValue(randomTimeOffset_Det, Integer.parseInt(defaultRandomTime), 0, null)))
                .setStopRetryTimes(String.valueOf(setDefaultIntValue(stopRetryNum_Det, Integer.parseInt(stopRetryNumDefault), 0, null)))
                .setClickRetryTimes(String.valueOf(setDefaultIntValue(clickRetryNum_Det, Integer.parseInt(clickRetryNumDefault), 0, null)));
        if (clickMatched_step().equals(matchedType) || clickMatched_clickStep().equals(matchedType)) {
            String matchedStep = matchedStep_Det.getText();
            if (StringUtils.isBlank(matchedStep)) {
                throw new RuntimeException(text_matchedStepIsNull());
            }
            selectedItem.setMatchedStep(matchedStep);
        }
        String retryType = retryType_Det.getValue();
        selectedItem.setRetryTypeEnum(retryTypeMap.getKey(retryType));
        if (retryType_Step().equals(retryType)) {
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
        clickType_Det.setDisable(false);
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
        retryStep_Det.setText("");
        retryStepHBox_Det.setVisible(retryType_Step().equals(retryType_Det.getValue()));
        addValueToolTip(retryType_Det, tip_retryType(), retryType_Det.getValue());
    }

    /**
     * 识别匹配后逻辑下拉框变化
     */
    @FXML
    private void matchedTypeChange() {
        matchedStep_Det.setText("");
        matchedStepHBox_Det.setVisible(clickMatched_step().equals(matchedType_Det.getValue()) ||
                clickMatched_clickStep().equals(matchedType_Det.getValue()));
        addValueToolTip(matchedType_Det, tip_matchedType(), matchedType_Det.getValue());
    }

    /**
     * 操作类型下拉框变化
     */
    @FXML
    private void clickTypeChange() {
        clickTypeHBox_Det.setVisible(clickType_click().equals(clickType_Det.getValue()));
        addValueToolTip(clickType_Det, tip_clickType(), clickType_Det.getValue());
        addValueToolTip(clickTypeText_Det, tip_clickType(), clickType_Det.getValue());
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
        if (stopWindowMonitor != null && !stopWindowMonitor.findingWindow) {
            stopWindowMonitor.startClickWindowMouseListener(preparation);
        }
    }

    /**
     * 坐标类型下拉框
     */
    @FXML
    private void coordinateTypeChange() {

    }

    /**
     * 更新坐标信息按钮
     */
    @FXML
    private void updateCoordinate() {

    }

}
