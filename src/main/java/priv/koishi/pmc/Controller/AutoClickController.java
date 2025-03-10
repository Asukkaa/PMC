package priv.koishi.pmc.Controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.AutoClickTaskBean;
import priv.koishi.pmc.Bean.ClickPositionBean;
import priv.koishi.pmc.EditingCell.EditingCell;
import priv.koishi.pmc.Properties.CommonProperties;
import priv.koishi.pmc.ThreadPool.CommonThreadPoolExecutor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Service.AutoClickService.autoClick;
import static priv.koishi.pmc.Utils.CommonUtils.*;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.TaskUtils.*;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 自动点击工具页面控制器
 *
 * @author KOISHI
 * Date:2025-02-17
 * Time:17:21
 */
public class AutoClickController extends CommonProperties {

    /**
     * 导出文件路径
     */
    private static String outFilePath;

    /**
     * 导入文件路径
     */
    private static String inFilePath;

    /**
     * 默认导出文件名称
     */
    private static String defaultOutFileName;

    /**
     * 默认录制准备时间
     */
    private static String defaultPreparationRecordTime;

    /**
     * 默认运行准备时间
     */
    private static String defaultPreparationRunTime;

    /**
     * 要防重复点击的组件
     */
    private static final List<Control> disableControls = new ArrayList<>();

    /**
     * 线程池
     */
    private final CommonThreadPoolExecutor commonThreadPoolExecutor = new CommonThreadPoolExecutor();

    /**
     * 线程池实例
     */
    private final ExecutorService executorService = commonThreadPoolExecutor.createNewThreadPool();

    /**
     * 自动点击任务
     */
    private Task<Void> autoClickTask;

    /**
     * 页面标识符
     */
    private static final String tabId = "_Click";

    /**
     * 浮窗Stage
     */
    private Stage floatingStage;

    /**
     * 顶部浮窗信息输出栏
     */
    private Label floatingLabel;

    /**
     * 顶部浮窗鼠标位置信息栏
     */
    private Label floatingMousePosition;

    /**
     * 正在录制标识
     */
    boolean recordClicking;

    /**
     * 录制时间线
     */
    private Timeline recordTimeline;

    /**
     * 运行时间线
     */
    private Timeline runTimeline;

    /**
     * 录制开始时间
     */
    private long recordingStartTime;

    /**
     * 全局鼠标监听器
     */
    private NativeMouseListener nativeMouseListener;

    /**
     * 全局键盘监听器
     */
    private NativeKeyListener nativeKeyListener;

    @FXML
    private AnchorPane anchorPane_Click;

    @FXML
    private VBox vbox_Click;

    @FXML
    private HBox fileNumberHBox_Click, tipHBox_Click, cancelTipHBox_Click;

    @FXML
    private ProgressBar progressBar_Click;

    @FXML
    private ChoiceBox<String> clickType_Click;

    @FXML
    private Label mousePosition_Click, dataNumber_Click, log_Click, tip_Click, cancelTip_Click, outPath_Click;

    @FXML
    private CheckBox openDirectory_Click;

    @FXML
    private Button clearButton_Click, runClick_Click, clickTest_Click, addPosition_Click, loadAutoClick_Click, exportAutoClick_Click, addOutPath_Click, recordClick_Click;

    @FXML
    private TextField mouseStartX_Click, mouseStartY_Click, mouseEndX_Click, mouseEndY_Click, wait_Click, loopTime_Click, clickNumBer_Click, timeClick_Click, clickName_Click, interval_Click, outFileName_Click, preparationRecordTime_Click, preparationRunTime_Click;

    @FXML
    private TableView<ClickPositionBean> tableView_Click;

    @FXML
    private TableColumn<ClickPositionBean, String> name_Click, startX_Click, startY_Click, endX_Click, endY_Click, clickTime_Click, clickNum_Click, clickInterval_Click, waitTime_Click, type_Click;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void autoClickAdaption(Stage stage) {
        Scene scene = stage.getScene();
        // 设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Click");
        table.setPrefHeight(stageHeight * 0.35);
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.95;
        table.setMaxWidth(tableWidth);
        Node settingVBox = scene.lookup("#vbox_Click");
        settingVBox.setLayoutX(stageWidth * 0.03);
        Node name = scene.lookup("#name_Click");
        name.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node startX = scene.lookup("#startX_Click");
        startX.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node startY = scene.lookup("#startY_Click");
        startY.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node endX = scene.lookup("#endX_Click");
        endX.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node endY = scene.lookup("#endY_Click");
        endY.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node clickTime = scene.lookup("#clickTime_Click");
        clickTime.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node clickNum = scene.lookup("#clickNum_Click");
        clickNum.setStyle("-fx-pref-width: " + tableWidth * 0.07 + "px;");
        Node clickInterval = scene.lookup("#clickInterval_Click");
        clickInterval.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node waitTime = scene.lookup("#waitTime_Click");
        waitTime.setStyle("-fx-pref-width: " + tableWidth * 0.13 + "px;");
        Node type = scene.lookup("#type_Click");
        type.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Label dataNum = (Label) scene.lookup("#dataNumber_Click");
        HBox fileNumberHBox = (HBox) scene.lookup("#fileNumberHBox_Click");
        nodeRightAlignment(fileNumberHBox, tableWidth, dataNum);
        Label tip = (Label) scene.lookup("#tip_Click");
        HBox tipHBox = (HBox) scene.lookup("#tipHBox_Click");
        nodeRightAlignment(tipHBox, tableWidth, tip);
        Label cancelTip = (Label) scene.lookup("#cancelTip_Click");
        HBox cancelTipHBox = (HBox) scene.lookup("#cancelTipHBox_Click");
        nodeRightAlignment(cancelTipHBox, tableWidth, cancelTip);
    }


    /**
     * 保存最后一次配置的值
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    public static void autoClickSaveLastConfig(Scene scene) throws IOException {
        AnchorPane anchorPane = (AnchorPane) scene.lookup("#anchorPane_Click");
        if (anchorPane != null) {
            InputStream input = checkRunningInputStream(configFile_Click);
            Properties prop = new Properties();
            prop.load(input);
            TextField mouseStartX = (TextField) scene.lookup("#mouseStartX_Click");
            prop.put(key_lastMouseStartX, mouseStartX.getText());
            TextField mouseStartY = (TextField) scene.lookup("#mouseStartY_Click");
            prop.put(key_lastMouseStartY, mouseStartY.getText());
            TextField mouseEndX = (TextField) scene.lookup("#mouseEndX_Click");
            prop.put(key_lastMouseEndX, mouseEndX.getText());
            TextField mouseEndY = (TextField) scene.lookup("#mouseEndY_Click");
            prop.put(key_lastMouseEndY, mouseEndY.getText());
            TextField wait = (TextField) scene.lookup("#wait_Click");
            prop.put(key_lastWait, wait.getText());
            TextField loopTime = (TextField) scene.lookup("#loopTime_Click");
            prop.put(key_lastLoopTime, loopTime.getText());
            TextField clickNumBer = (TextField) scene.lookup("#clickNumBer_Click");
            prop.put(key_lastClickNumBer, clickNumBer.getText());
            TextField timeClick = (TextField) scene.lookup("#timeClick_Click");
            prop.put(key_lastTimeClick, timeClick.getText());
            TextField interval = (TextField) scene.lookup("#interval_Click");
            prop.put(key_lastInterval, interval.getText());
            TextField clickName = (TextField) scene.lookup("#clickName_Click");
            prop.put(key_lastClickName, clickName.getText());
            ChoiceBox<?> clickType = (ChoiceBox<?>) scene.lookup("#clickType_Click");
            prop.put(key_lastClickType, clickType.getValue());
            TextField outFileName = (TextField) scene.lookup("#outFileName_Click");
            prop.put(key_lastOutFileName, outFileName.getText());
            CheckBox openDirectory = (CheckBox) scene.lookup("#openDirectory_Click");
            String lastOpenDirectoryValue = openDirectory.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, lastOpenDirectoryValue);
            TextField preparationRecordTime = (TextField) scene.lookup("#preparationRecordTime_Click");
            prop.put(key_lastPreparationRecordTime, preparationRecordTime.getText());
            TextField preparationRunTime = (TextField) scene.lookup("#preparationRunTime_Click");
            prop.put(key_lastPreparationRunTime, preparationRunTime.getText());
            Label outPath = (Label) scene.lookup("#outPath_Click");
            prop.put(key_outFilePath, outPath.getText());
            OutputStream output = checkRunningOutputStream(configFile_Click);
            prop.store(output, null);
            input.close();
            output.close();
        }
    }

    /**
     * 设置初始配置值为上次配置值
     *
     * @throws IOException io异常
     */
    private void setLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Click);
        prop.load(input);
        if (activation.equals(prop.getProperty(key_loadLastConfig))) {
            setControlLastConfig(wait_Click, prop, key_lastWait, false, null);
            setControlLastConfig(outPath_Click, prop, key_outFilePath, false, anchorPane_Click);
            setControlLastConfig(interval_Click, prop, key_lastInterval, false, null);
            setControlLastConfig(loopTime_Click, prop, key_lastLoopTime, false, null);
            setControlLastConfig(clickName_Click, prop, key_lastClickName, true, null);
            setControlLastConfig(mouseEndX_Click, prop, key_lastMouseEndX, false, null);
            setControlLastConfig(mouseEndY_Click, prop, key_lastMouseEndY, false, null);
            setControlLastConfig(timeClick_Click, prop, key_lastTimeClick, false, null);
            setControlLastConfig(clickType_Click, prop, key_lastClickType, false, null);
            setControlLastConfig(mouseStartX_Click, prop, key_lastMouseStartX, false, null);
            setControlLastConfig(mouseStartY_Click, prop, key_lastMouseStartY, false, null);
            setControlLastConfig(clickNumBer_Click, prop, key_lastClickNumBer, false, null);
            setControlLastConfig(outFileName_Click, prop, key_lastOutFileName, false, null);
            setControlLastConfig(openDirectory_Click, prop, key_lastOpenDirectory, false, null);
            setControlLastConfig(preparationRunTime_Click, prop, key_lastPreparationRunTime, false, null);
            setControlLastConfig(preparationRecordTime_Click, prop, key_lastPreparationRecordTime, false, null);
        }
        input.close();
    }

    /**
     * 读取配置文件
     *
     * @throws IOException io异常
     */
    private void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Click);
        prop.load(input);
        inFilePath = prop.getProperty(key_inFilePath);
        outFilePath = prop.getProperty(key_outFilePath);
        defaultOutFileName = prop.getProperty(key_defaultOutFileName);
        defaultPreparationRunTime = prop.getProperty(key_defaultPreparationRunTime);
        defaultPreparationRecordTime = prop.getProperty(key_defaultPreparationRecordTime);
        input.close();
    }

    /**
     * 获取鼠标坐标
     */
    private void getNowMousePosition() {
        // 使用java.awt.MouseInfo获取鼠标的全局位置
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        int x = (int) mousePoint.getX();
        int y = (int) mousePoint.getY();
        String text = "当前鼠标位置为： X: " + x + " Y: " + y;
        Platform.runLater(() -> {
            floatingMousePosition.setText(text);
            mousePosition_Click.setText(text);
        });
    }

    /**
     * 获取鼠标坐标监听器
     */
    private void moussePositionListener() {
        // 启动定时器，实时获取鼠标位置
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                getNowMousePosition();
            }
        };
        timer.start();
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        name_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        startX_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        startY_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        endX_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        endY_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        clickTime_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        clickNum_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.07));
        clickInterval_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        waitTime_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.13));
        type_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
    }

    /**
     * 设置单元格可编辑
     */
    private void makeCellCanEdit() {
        tableView_Click.setEditable(true);
        name_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setName));
        startX_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setStartX, true, 0, null));
        endX_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setEndX, true, 0, null));
        startY_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setStartY, true, 0, null));
        endY_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setEndY, true, 0, null));
        clickInterval_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setClickInterval, true, 0, null));
        waitTime_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setWaitTime, true, 0, null));
        clickTime_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setClickTime, true, 0, null));
        clickNum_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setClickNum, true, 0, null));
    }

    /**
     * 初始化浮窗
     */
    private void initFloatingWindow() {
        double width = 550;
        double height = 110;
        // 创建一个矩形作为浮窗的内容
        Rectangle rectangle = new Rectangle(width, height);
        // 设置透明度
        rectangle.setOpacity(0.0);
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent");
        Color labelTextFill = Color.WHITE;
        floatingLabel = new Label(text_cancelTask);
        floatingLabel.setTextFill(labelTextFill);
        floatingMousePosition = new Label();
        floatingMousePosition.setTextFill(labelTextFill);
        VBox vBox = new VBox();
        vBox.getChildren().addAll(floatingMousePosition, floatingLabel);
        root.getChildren().addAll(rectangle, vBox);
        Scene scene = new Scene(root, Color.TRANSPARENT);
        vBox.setMouseTransparent(true);
        floatingStage = new Stage();
        // 设置透明样式
        floatingStage.initStyle(StageStyle.TRANSPARENT);
        // 设置始终置顶
        floatingStage.setAlwaysOnTop(true);
        floatingStage.setScene(scene);
        // 设置浮窗的位置在屏幕最上方正中间
        java.awt.Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        double x = (screenBounds.getWidth() - width) / 2;
        double y = 0;
        floatingStage.setX(x);
        floatingStage.setY(y);
    }

    /**
     * 显示浮窗
     */
    private void showFloatingWindow(boolean isRun) {
        Scene scene = anchorPane_Click.getScene();
        CheckBox floatingRun = (CheckBox) scene.lookup("#floatingRun_Set");
        CheckBox floatingRecord = (CheckBox) scene.lookup("#floatingRecord_Set");
        boolean isShow = isRun ? floatingRun.isSelected() : floatingRecord.isSelected();
        if (isShow) {
            Platform.runLater(() -> {
                if (floatingStage != null && !floatingStage.isShowing()) {
                    floatingStage.show();
                }
            });
        }
    }

    /**
     * 隐藏浮窗
     */
    private void hideFloatingWindow() {
        Platform.runLater(() -> {
            if (floatingStage != null && floatingStage.isShowing()) {
                floatingStage.hide();
            }
        });
    }

    /**
     * 启动自动操作流程
     *
     * @param clickPositionBeans 自动操作流程
     */
    private void launchClickTask(List<ClickPositionBean> clickPositionBeans) {
        if (!recordClicking && autoClickTask == null && runTimeline == null) {
            Scene scene = anchorPane_Click.getScene();
            CheckBox firstClick = (CheckBox) scene.lookup("#firstClick_Set");
            AutoClickTaskBean taskBean = new AutoClickTaskBean();
            taskBean.setLoopTime(setDefaultIntValue(loopTime_Click, 1, 0, null))
                    .setFirstClick(firstClick.isSelected())
                    .setFloatingLabel(floatingLabel)
                    .setRunTimeline(runTimeline)
                    .setDisableControls(disableControls)
                    .setProgressBar(progressBar_Click)
                    .setBeanList(clickPositionBeans)
                    .setMassageLabel(log_Click);
            updateLabel(log_Click, "");
            Stage stage = (Stage) scene.getWindow();
            CheckBox hideWindowRun = (CheckBox) scene.lookup("#hideWindowRun_Set");
            if (hideWindowRun.isSelected()) {
                stage.setIconified(true);
            }
            // 开启键盘监听
            startNativeKeyListener();
            // 创建一个Robot实例
            Robot robot = new Robot();
            autoClickTask = autoClick(taskBean, robot);
            // 绑定带进度条的线程
            bindingProgressBarTask(autoClickTask, taskBean);
            Label massageLabel = taskBean.getMassageLabel();
            autoClickTask.setOnSucceeded(event -> {
                taskUnbind(taskBean);
                massageLabel.setTextFill(Color.GREEN);
                massageLabel.setText(text_taskFinished);
                hideFloatingWindow();
                CheckBox showWindowRun = (CheckBox) scene.lookup("#showWindowRun_Set");
                if (showWindowRun.isSelected()) {
                    showStage(stage);
                }
                // 移除键盘监听器
                stopNativeKeyListener();
                autoClickTask = null;
                runTimeline = null;
            });
            autoClickTask.setOnFailed(event -> {
                taskNotSuccess(taskBean, text_taskFailed);
                hideFloatingWindow();
                CheckBox showWindowRun = (CheckBox) scene.lookup("#showWindowRun_Set");
                if (showWindowRun.isSelected()) {
                    showStage(stage);
                }
                // 移除键盘监听器
                stopNativeKeyListener();
                // 移除开始前的倒计时
                if (runTimeline != null) {
                    runTimeline.stop();
                    runTimeline = null;
                }
                // 获取抛出的异常
                Throwable ex = autoClickTask.getException();
                autoClickTask = null;
                throw new RuntimeException(ex);
            });
            autoClickTask.setOnCancelled(event -> {
                taskNotSuccess(taskBean, text_taskCancelled);
                hideFloatingWindow();
                CheckBox showWindowRun = (CheckBox) scene.lookup("#showWindowRun_Set");
                if (showWindowRun.isSelected()) {
                    showStage(stage);
                }
                // 移除键盘监听器
                stopNativeKeyListener();
                autoClickTask = null;
                runTimeline = null;
            });
            if (!autoClickTask.isRunning()) {
                // 改变要防重复点击的组件状态
                changeDisableControls(taskBean, true);
                // 获取准备时间值
                int preparationTimeValue = setDefaultIntValue(preparationRunTime_Click, Integer.parseInt(defaultPreparationRunTime), 0, null);
                // 设置浮窗文本显示准备时间
                floatingLabel.setText(text_cancelTask + preparationTimeValue + text_run);
                showFloatingWindow(true);
                // 延时执行任务
                runTimeline = executeRunTimeLine(preparationTimeValue);
            }
        }
    }

    /**
     * 延时执行任务
     *
     * @param preparationTimeValue 准备时间
     * @return runTimeline 运行时间线
     */
    private Timeline executeRunTimeLine(int preparationTimeValue) {
        if (preparationTimeValue == 0) {
            floatingLabel.setText(text_cancelTask + text_recordClicking);
            // 使用新线程启动
            executorService.execute(autoClickTask);
            return runTimeline;
        }
        runTimeline = new Timeline();
        AtomicInteger preparationTime = new AtomicInteger(preparationTimeValue);
        // 创建 Timeline 来实现倒计时
        Timeline finalTimeline = runTimeline;
        runTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            preparationTime.getAndDecrement();
            if (preparationTime.get() > 0) {
                floatingLabel.setText(text_cancelTask + preparationTime + text_run);
            } else {
                // 停止 Timeline
                finalTimeline.stop();
                // 更新浮窗文本
                floatingLabel.setText(text_cancelTask + text_recordClicking);
                // 使用新线程启动
                executorService.execute(autoClickTask);
            }
        }));
        // 设置 Timeline 的循环次数
        runTimeline.setCycleCount(preparationTimeValue);
        runTimeline.play();
        return runTimeline;
    }

    /**
     * 执行选中的步骤选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    private void buildClickTestMenuItem(TableView<ClickPositionBean> tableView, ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem("执行选中的步骤");
        menuItem.setOnAction(event -> {
            List<ClickPositionBean> selectedItem = tableView.getSelectionModel().getSelectedItems();
            if (CollectionUtils.isNotEmpty(selectedItem)) {
                launchClickTask(selectedItem);
            }
        });
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 构建右键菜单
     */
    private void buildContextMenu() {
        // 设置可以选中多行
        tableView_Click.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 添加测试点击选项
        buildClickTestMenuItem(tableView_Click, contextMenu);
        // 所选行上移一行选项
        buildUpMoveDataMenuItem(tableView_Click, contextMenu);
        // 所选行下移一行选项
        buildDownMoveDataMenuItem(tableView_Click, contextMenu);
        // 修改操作类型
        buildEditClickType(tableView_Click, contextMenu);
        // 删除所选数据选项
        buildDeleteDataMenuItem(tableView_Click, dataNumber_Click, contextMenu, text_data);
        tableView_Click.setContextMenu(contextMenu);
        tableView_Click.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(tableView_Click, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * 获取点击步骤设置
     *
     * @return clickPositionBean 自动操作步骤类
     */
    private ClickPositionBean getClickSetting(int tableViewItemSize) {
        ClickPositionBean clickPositionBean = new ClickPositionBean();
        int startX = setDefaultIntValue(mouseStartX_Click, 0, 0, null);
        int startY = setDefaultIntValue(mouseStartY_Click, 0, 0, null);
        clickPositionBean.setName(setDefaultStrValue(clickName_Click, text_step + (tableViewItemSize + 1) + text_isAdd))
                .setClickInterval(String.valueOf(setDefaultIntValue(interval_Click, 0, 0, null)))
                .setClickNum(String.valueOf(setDefaultIntValue(clickNumBer_Click, 1, 0, null)))
                .setClickTime(String.valueOf(setDefaultIntValue(timeClick_Click, 0, 0, null)))
                .setWaitTime(String.valueOf(setDefaultIntValue(wait_Click, 0, 0, null)))
                .setEndX(String.valueOf(setDefaultIntValue(mouseEndX_Click, startX, 0, null)))
                .setEndY(String.valueOf(setDefaultIntValue(mouseEndY_Click, startY, 0, null)))
                .setType(clickType_Click.getValue())
                .setStartX(String.valueOf(startX))
                .setStartY(String.valueOf(startY));
        return clickPositionBean;
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 操作名称文本输入框鼠标悬停提示
        textFieldValueListener(clickName_Click, tip_clickName);
        // 导出自动流程文件名称文本输入框鼠标悬停提示
        textFieldValueListener(outFileName_Click, tip_autoClickFileName + defaultOutFileName);
        // 限制单次操作点击间隔文本输入框内容
        integerRangeTextField(wait_Click, 0, null, tip_wait);
        // 限制循环次数文本输入框内容
        integerRangeTextField(loopTime_Click, 0, null, tip_loopTime);
        // 限制操作时长文本输入内容
        integerRangeTextField(timeClick_Click, 0, null, tip_clickTime);
        // 限制鼠标结束位置横(X)坐标文本输入框内容
        integerRangeTextField(mouseEndX_Click, 0, null, tip_mouseEndX);
        // 限制鼠标结束位置纵(Y)坐标文本输入框内容
        integerRangeTextField(mouseEndY_Click, 0, null, tip_mouseEndY);
        // 限制鼠标起始位置横(X)坐标文本输入框内容
        integerRangeTextField(mouseStartX_Click, 0, null, tip_mouseStartX);
        // 限制鼠标起始位置纵(Y)坐标文本输入框内容
        integerRangeTextField(mouseStartY_Click, 0, null, tip_mouseStartY);
        // 限制点击次数文本输入框内容
        integerRangeTextField(clickNumBer_Click, 0, null, tip_clickNumBer);
        // 限制运行准备时间文本输入框内容
        integerRangeTextField(preparationRunTime_Click, 0, null, tip_preparationRunTime + defaultPreparationRunTime);
        // 限制录制准备时间文本输入框内容
        integerRangeTextField(preparationRecordTime_Click, 0, null, tip_preparationRecordTime + defaultPreparationRecordTime);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_wait, wait_Click);
        addToolTip(tip_runClick, runClick_Click);
        addToolTip(tip_loopTime, loopTime_Click);
        addToolTip(tip_mouseEndX, mouseEndX_Click);
        addToolTip(tip_mouseEndY, mouseEndY_Click);
        addToolTip(tip_clickType, clickType_Click);
        addToolTip(tip_clickTime, timeClick_Click);
        addToolTip(tip_clickName, clickName_Click);
        addToolTip(tip_clickTest, clickTest_Click);
        addToolTip(tip_clickInterval, interval_Click);
        addToolTip(tip_learButton, clearButton_Click);
        addToolTip(tip_addPosition, addPosition_Click);
        addToolTip(tip_clickNumBer, clickNumBer_Click);
        addToolTip(tip_mouseStartX, mouseStartX_Click);
        addToolTip(tip_mouseStartY, mouseStartY_Click);
        addToolTip(tip_recordClick, recordClick_Click);
        addToolTip(tip_outAutoClickPath, addOutPath_Click);
        addToolTip(tip_openDirectory, openDirectory_Click);
        addToolTip(tip_loadAutoClick, loadAutoClick_Click);
        addToolTip(tip_exportAutoClick, exportAutoClick_Click);
        addToolTip(tip_autoClickFileName + defaultOutFileName, outFileName_Click);
        addToolTip(tip_preparationRunTime + defaultPreparationRunTime, preparationRunTime_Click);
        addToolTip(tip_preparationRecordTime + defaultPreparationRecordTime, preparationRecordTime_Click);
    }

    /**
     * 向列表添加数据
     *
     * @param clickPositionBeans 自动流程集合
     */
    private void addData(List<ClickPositionBean> clickPositionBeans) {
        ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
        tableViewItems.addAll(clickPositionBeans);
        // 自动填充javafx表格
        autoBuildTableViewData(tableView_Click, tableViewItems, tabId);
        // 同步表格数据量
        dataNumber_Click.setText(text_allHave + tableViewItems.size() + text_process);
        // 表格设置为可编辑
        makeCellCanEdit();
        // 设置列表通过拖拽排序行
        tableViewDragRow(tableView_Click);
        // 构建右键菜单
        buildContextMenu();
    }

    /**
     * 将自动流程添加到列表中
     *
     * @param clickPositionBeans 自动流程集合
     */
    private void addAutoClickPositions(List<ClickPositionBean> clickPositionBeans) throws IOException {
        for (ClickPositionBean clickPositionBean : clickPositionBeans) {
            clickPositionBean.setUuid(UUID.randomUUID().toString());
            if (!isInIntegerRange(clickPositionBean.getStartX(), 0, null) || !isInIntegerRange(clickPositionBean.getStartY(), 0, null)
                    || !isInIntegerRange(clickPositionBean.getEndX(), 0, null) || !isInIntegerRange(clickPositionBean.getEndY(), 0, null)
                    || !isInIntegerRange(clickPositionBean.getClickTime(), 0, null) || !isInIntegerRange(clickPositionBean.getClickNum(), 0, null)
                    || !isInIntegerRange(clickPositionBean.getClickInterval(), 0, null) || !isInIntegerRange(clickPositionBean.getWaitTime(), 0, null)
                    || !clickTypeMap.containsKey(clickPositionBean.getType())) {
                throw new IOException(text_LackKeyData);
            }
        }
        // 向列表添加数据
        addData(clickPositionBeans);
        updateLabel(log_Click, text_loadSuccess + inFilePath);
        log_Click.setTextFill(Color.GREEN);
    }

    /**
     * 开启全局键盘监听
     */
    private void startNativeKeyListener() {
        stopNativeKeyListener();
        // 键盘监听器
        nativeKeyListener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                Platform.runLater(() -> {
                    // 仅在自动操作与录制情况下才监听键盘
                    if (autoClickTask != null && autoClickTask.isRunning() || recordTimeline != null || runTimeline != null || recordClicking) {
                        // 检测快捷键 esc
                        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
                            // 停止自动操作
                            if (autoClickTask != null && autoClickTask.isRunning()) {
                                autoClickTask.cancel();
                                autoClickTask = null;
                            }
                            // 停止录制计时
                            if (recordTimeline != null) {
                                recordTimeline.stop();
                                recordTimeline = null;
                                log_Click.setTextFill(Color.BLUE);
                                log_Click.setText("录制已结束");
                            }
                            // 停止运行计时
                            if (runTimeline != null) {
                                runTimeline.stop();
                                runTimeline = null;
                                autoClickTask = null;
                                AutoClickTaskBean taskBean = new AutoClickTaskBean();
                                taskBean.setDisableControls(disableControls)
                                        .setProgressBar(progressBar_Click)
                                        .setMassageLabel(log_Click);
                                taskNotSuccess(taskBean, text_taskCancelled);
                            }
                            // 改变要防重复点击的组件状态
                            changeDisableControls(disableControls, false);
                            // 移除鼠标监听器
                            stopNativeMouseListener();
                            hideFloatingWindow();
                            recordClicking = false;
                            Scene scene = anchorPane_Click.getScene();
                            Stage stage = (Stage) scene.getWindow();
                            CheckBox showWindowRecord = (CheckBox) scene.lookup("#showWindowRecord_Set");
                            if (showWindowRecord.isSelected()) {
                                showStage(stage);
                            }
                        }
                    }
                });
            }
        };
        GlobalScreen.addNativeKeyListener(nativeKeyListener);
    }

    /**
     * 将程序窗口弹出
     *
     * @param stage 程序主舞台
     */
    private static void showStage(Stage stage) {
        stage.setIconified(false);
        stage.setAlwaysOnTop(true);
        stage.setAlwaysOnTop(false);
        stage.requestFocus();
    }

    /**
     * 移除键盘监听器
     */
    private void stopNativeKeyListener() {
        if (nativeKeyListener != null) {
            GlobalScreen.removeNativeKeyListener(nativeKeyListener);
            nativeKeyListener = null;
        }
    }

    /**
     * 开启全局鼠标监听
     */
    private void startNativeMouseListener() {
        stopNativeMouseListener();
        // 鼠标监听器
        nativeMouseListener = new NativeMouseListener() {
            // 记录点击时刻
            private long pressTime;
            // 记录松开时刻
            private long releasedTime;
            // 记录鼠标按钮
            private int pressButton;
            // 首次点击标记
            private boolean isFirstClick = true;
            // 记录点击信息
            ClickPositionBean clickBean;

            // 监听鼠标按下
            @Override
            public void nativeMousePressed(NativeMouseEvent e) {
                if (recordClicking) {
                    // 记录按下时刻的时间戳和坐标
                    pressTime = System.currentTimeMillis();
                    long waitTime;
                    if (isFirstClick) {
                        waitTime = pressTime - recordingStartTime;
                    } else {
                        waitTime = pressTime - releasedTime;
                    }
                    int dataSize = tableView_Click.getItems().size() + 1;
                    pressButton = e.getButton();
                    Point mousePoint = MouseInfo.getPointerInfo().getLocation();
                    int startX = (int) mousePoint.getX();
                    int startY = (int) mousePoint.getY();
                    clickBean = new ClickPositionBean();
                    clickBean.setName(text_step + dataSize + text_isRecord)
                            .setUuid(UUID.randomUUID().toString())
                            .setType(typeClickMap.get(pressButton))
                            .setWaitTime(String.valueOf(waitTime))
                            .setStartX(String.valueOf(startX))
                            .setStartY(String.valueOf(startY));
                    Platform.runLater(() -> {
                        log_Click.setTextFill(Color.BLUE);
                        String log = text_recorded + typeClickMap.get(pressButton) + " 点击 (" + clickBean.getStartX() + "," + clickBean.getStartY() + ")";
                        log_Click.setText(log);
                        floatingLabel.setText(text_cancelTask + text_recordClicking + "\n" + log);
                    });
                }
            }

            // 监听鼠标松开
            @Override
            public void nativeMouseReleased(NativeMouseEvent e) {
                if (recordClicking && pressButton == e.getButton()) {
                    isFirstClick = false;
                    releasedTime = System.currentTimeMillis();
                    // 计算点击持续时间（毫秒）
                    long duration = releasedTime - pressTime;
                    Point mousePoint = MouseInfo.getPointerInfo().getLocation();
                    int endX = (int) mousePoint.getX();
                    int endY = (int) mousePoint.getY();
                    // 创建点击步骤对象
                    clickBean.setClickTime(String.valueOf(duration))
                            .setEndX(String.valueOf(endX))
                            .setEndY(String.valueOf(endY))
                            .setClickInterval("0")
                            .setClickNum("1");
                    Platform.runLater(() -> {
                        // 添加至表格
                        List<ClickPositionBean> clickPositionBeans = new ArrayList<>();
                        clickPositionBeans.add(clickBean);
                        addData(clickPositionBeans);
                        // 日志反馈
                        log_Click.setTextFill(Color.BLUE);
                        String log = text_recorded + typeClickMap.get(pressButton) + " 松开 (" + clickBean.getEndX() + "," + clickBean.getEndY() + ")";
                        log_Click.setText(log);
                        floatingLabel.setText(text_cancelTask + text_recordClicking + "\n" + log);
                    });
                }
            }
        };
        GlobalScreen.addNativeMouseListener(nativeMouseListener);
    }

    /**
     * 移除鼠标监听器
     */
    private void stopNativeMouseListener() {
        if (nativeMouseListener != null) {
            GlobalScreen.removeNativeMouseListener(nativeMouseListener);
            nativeMouseListener = null;
        }
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableControls() {
        disableControls.add(runClick_Click);
        disableControls.add(clickTest_Click);
        disableControls.add(recordClick_Click);
        disableControls.add(addPosition_Click);
        disableControls.add(clearButton_Click);
        disableControls.add(loadAutoClick_Click);
        disableControls.add(exportAutoClick_Click);
    }

    /**
     * 页面初始化
     *
     * @throws IOException         配置文件读取失败
     * @throws NativeHookException 注册全局输入监听器失败
     */
    @FXML
    private void initialize() throws IOException, NativeHookException {
        // 初始化浮窗
        initFloatingWindow();
        // 获取鼠标坐标监听器
        moussePositionListener();
        // 设置javafx单元格宽度
        bindPrefWidthProperty();
        // 读取配置文件
        getConfig();
        // 设置要防重复点击的组件
        setDisableControls();
        // 设置鼠标悬停提示
        setToolTip();
        // 给输入框添加内容变化监听
        textFieldChangeListener();
        // 设置初始配置值为上次配置值
        setLastConfig();
        // 注册全局输入监听器
        GlobalScreen.registerNativeHook();
    }

    /**
     * 运行自动点击按钮
     *
     * @throws Exception 列表中没有要执行的操作
     */
    @FXML
    public void runClick() throws Exception {
        ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
        if (CollectionUtils.isEmpty(tableViewItems)) {
            throw new Exception(text_noAutoClickToRun);
        }
        // 启动自动操作流程
        launchClickTask(tableViewItems);
    }

    /**
     * 清空javafx列表按钮
     */
    @FXML
    public void removeAll() {
        if (autoClickTask == null && !recordClicking) {
            removeTableViewData(tableView_Click, dataNumber_Click, log_Click);
        }
    }

    /**
     * 点击测试按钮
     */
    @FXML
    private void clickTest() {
        // 获取步骤设置
        List<ClickPositionBean> clickPositionBeans = new ArrayList<>();
        ClickPositionBean clickPositionBean = getClickSetting(-1);
        clickPositionBeans.add(clickPositionBean);
        // 启动自动操作流程
        launchClickTask(clickPositionBeans);
    }

    /**
     * 添加点击步骤
     */
    @FXML
    private void addPosition() {
        if (autoClickTask == null && !recordClicking) {
            ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
            int tableViewItemSize = tableViewItems.size();
            // 获取点击步骤设置
            ClickPositionBean clickPositionBean = getClickSetting(tableViewItemSize);
            List<ClickPositionBean> clickPositionBeans = new ArrayList<>();
            clickPositionBeans.add(clickPositionBean);
            // 向列表添加数据
            addData(clickPositionBeans);
            // 初始化信息栏
            updateLabel(log_Click, "");
        }
    }

    /**
     * 导入操作流程按钮
     *
     * @throws IOException io异常
     */
    @FXML
    public void loadAutoClick(ActionEvent actionEvent) throws IOException {
        if (autoClickTask == null && !recordClicking) {
            getConfig();
            List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(new FileChooser.ExtensionFilter("Perfect Mouse Control", "*.pmc")));
            File selectedFile = creatFileChooser(actionEvent, inFilePath, extensionFilters, text_selectAutoFile);
            if (selectedFile != null) {
                inFilePath = selectedFile.getPath();
                updateProperties(configFile_Click, key_inFilePath, new File(inFilePath).getParent());
                // 读取 JSON 文件并转换为 List<ClickPositionBean>
                ObjectMapper objectMapper = new ObjectMapper();
                File jsonFile = new File(inFilePath);
                List<ClickPositionBean> clickPositionBeans;
                try {
                    clickPositionBeans = objectMapper.readValue(jsonFile, objectMapper.getTypeFactory().constructCollectionType(List.class, ClickPositionBean.class));
                } catch (MismatchedInputException | JsonParseException e) {
                    throw new IOException(text_loadAutoClick + inFilePath + text_formatError);
                }
                // 将自动流程添加到列表中
                addAutoClickPositions(clickPositionBeans);
            }
        }
    }

    /**
     * 导出操作流程按钮
     *
     * @throws Exception 列表中无要导出的自动操作流程
     */
    @FXML
    public void exportAutoClick() throws Exception {
        if (autoClickTask == null && !recordClicking) {
            ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
            if (CollectionUtils.isEmpty(tableViewItems)) {
                throw new Exception(text_noAutoClickList);
            }
            if (StringUtils.isBlank(outFilePath)) {
                throw new Exception(text_outPathNull);
            }
            String fileName = setDefaultFileName(outFileName_Click, defaultOutFileName);
            ObjectMapper objectMapper = new ObjectMapper();
            String path = outFilePath + File.separator + fileName + PMC;
            objectMapper.writeValue(new File(path), tableViewItems);
            updateLabel(log_Click, text_saveSuccess + path);
            log_Click.setTextFill(Color.GREEN);
            if (openDirectory_Click.isSelected()) {
                openDirectory(path);
            }
        }
    }

    /**
     * 设置操作流程导出文件夹地址按钮
     *
     * @throws IOException io异常
     */
    @FXML
    private void addOutPath(ActionEvent actionEvent) throws IOException {
        getConfig();
        File selectedFile = creatDirectoryChooser(actionEvent, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Click, configFile_Click, anchorPane_Click);
        }
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     * @throws IOException 导入自动化流程文件内容格式不正确、导入文件缺少关键数据
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) throws IOException {
        if (autoClickTask == null && !recordClicking) {
            List<File> files = dragEvent.getDragboard().getFiles();
            List<ClickPositionBean> clickPositionBeans = new ArrayList<>();
            for (File file : files) {
                // 读取 JSON 文件并转换为 List<ClickPositionBean>
                ObjectMapper objectMapper = new ObjectMapper();
                File jsonFile = new File(file.getPath());
                try {
                    clickPositionBeans.addAll(objectMapper.readValue(jsonFile, objectMapper.getTypeFactory().constructCollectionType(List.class, ClickPositionBean.class)));
                } catch (IOException e) {
                    throw new IOException(text_loadAutoClick + inFilePath + text_formatError);
                }
            }
            // 将自动流程添加到列表中
            addAutoClickPositions(clickPositionBeans);
        }
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void acceptDrop(DragEvent dragEvent) {
        if (autoClickTask == null && !recordClicking) {
            List<File> files = dragEvent.getDragboard().getFiles();
            files.forEach(file -> {
                if (PMC.equals(getFileType(file))) {
                    // 接受拖放
                    dragEvent.acceptTransferModes(TransferMode.COPY);
                    dragEvent.consume();
                }
            });
        }
    }

    /**
     * 录制自动操作按钮
     */
    @FXML
    private void recordClick() {
        if (!recordClicking && autoClickTask == null) {
            // 改变要防重复点击的组件状态
            changeDisableControls(disableControls, true);
            // 获取准备时间值
            int preparationTimeValue = setDefaultIntValue(preparationRecordTime_Click, Integer.parseInt(defaultPreparationRecordTime), 0, null);
            // 开始录制
            recordClicking = true;
            Scene scene = anchorPane_Click.getScene();
            Stage stage = (Stage) scene.getWindow();
            CheckBox hideWindowRecord = (CheckBox) scene.lookup("#hideWindowRecord_Set");
            if (hideWindowRecord.isSelected()) {
                stage.setIconified(true);
            }
            // 开启键盘监听0
            startNativeKeyListener();
            // 设置浮窗文本显示准备时间
            floatingLabel.setText(text_cancelTask + preparationTimeValue + text_preparation);
            // 显示浮窗
            showFloatingWindow(false);
            if (preparationTimeValue == 0) {
                // 开启鼠标监听
                startNativeMouseListener();
                // 录制开始时间
                recordingStartTime = System.currentTimeMillis();
                // 更新浮窗文本
                floatingLabel.setText(text_cancelTask + text_recordClicking);
            } else {
                recordTimeline = new Timeline();
                AtomicInteger preparationTime = new AtomicInteger(preparationTimeValue);
                // 创建 Timeline 来实现倒计时
                Timeline finalTimeline = recordTimeline;
                recordTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                    preparationTime.getAndDecrement();
                    if (preparationTime.get() > 0) {
                        floatingLabel.setText(text_cancelTask + preparationTime + text_preparation);
                    } else {
                        // 开启鼠标监听
                        startNativeMouseListener();
                        // 录制开始时间
                        recordingStartTime = System.currentTimeMillis();
                        // 停止 Timeline
                        finalTimeline.stop();
                        // 更新浮窗文本
                        floatingLabel.setText(text_cancelTask + text_recordClicking);
                    }
                }));
                // 设置 Timeline 的循环次数
                recordTimeline.setCycleCount(preparationTimeValue);
                // 启动 Timeline
                recordTimeline.play();
            }
        }
    }

}
