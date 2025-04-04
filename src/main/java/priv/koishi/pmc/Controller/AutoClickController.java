package priv.koishi.pmc.Controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import javafx.stage.*;
import javafx.util.Duration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.AutoClickTaskBean;
import priv.koishi.pmc.Bean.ClickPositionBean;
import priv.koishi.pmc.Bean.ImgFileBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.EditingCell.EditingCell;
import priv.koishi.pmc.Listener.MousePositionListener;
import priv.koishi.pmc.MainApplication;
import priv.koishi.pmc.Properties.CommonProperties;
import priv.koishi.pmc.ThreadPool.CommonThreadPoolExecutor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Service.AutoClickService.autoClick;
import static priv.koishi.pmc.Service.ImageRecognitionService.refreshScreenParameters;
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
     * 自动保存文件名
     */
    private static String autoSaveFileName;

    /**
     * 上次所选要点击的图片地址
     */
    private static String clickImgSelectPath;

    /**
     * 上次所选终止操作的图片地址
     */
    public static String stopImgSelectPath;

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
     * 详情页高度
     */
    private int detailHeight;

    /**
     * 详情页宽度
     */
    private int detailWidth;

    /**
     * 浮窗X坐标
     */
    private int floatingX;

    /**
     * 浮窗Y坐标
     */
    private int floatingY;

    /**
     * 浮窗距离屏幕的边距
     */
    private int margin;

    /**
     * 浮窗宽度
     */
    private int floatingWidth;

    /**
     * 浮窗高度
     */
    private int floatingHeight;

    /**
     * 要防重复点击的组件
     */
    private static final List<Node> disableNodes = new ArrayList<>();

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
     * 正在录制标识
     */
    boolean recordClicking;

    /**
     * 正在运行自动操作标识
     */
    boolean runClicking;

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
     * 浮窗所在矩形
     */
    private Rectangle rectangle;

    /**
     * 程序主场景
     */
    private Scene mainScene;

    /**
     * 程序主舞台
     */
    private Stage mainStage;

    @FXML
    private AnchorPane anchorPane_Click;

    @FXML
    private HBox fileNumberHBox_Click, tipHBox_Click, cancelTipHBox_Click, logHBox_Click;

    @FXML
    private ProgressBar progressBar_Click;

    @FXML
    private Label mousePosition_Click, dataNumber_Click, log_Click, tip_Click, cancelTip_Click, outPath_Click,
            err_Click;

    @FXML
    private CheckBox openDirectory_Click;

    @FXML
    private Button clearButton_Click, runClick_Click, clickTest_Click, addPosition_Click, loadAutoClick_Click,
            exportAutoClick_Click, addOutPath_Click, recordClick_Click;

    @FXML
    private TextField loopTime_Click, outFileName_Click, preparationRecordTime_Click, preparationRunTime_Click;

    @FXML
    private TableView<ClickPositionVO> tableView_Click;

    @FXML
    private TableColumn<ClickPositionVO, Integer> index_Click;

    @FXML
    private TableColumn<ClickPositionVO, ImageView> thumb_Click;

    @FXML
    private TableColumn<ClickPositionVO, String> name_Click, clickTime_Click, clickNum_Click,
            clickInterval_Click, waitTime_Click, clickType_Click, retryType_Click;

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
        table.setPrefHeight(stageHeight * 0.5);
        // 设置组件宽度
        double tableWidth = stage.getWidth() * 0.95;
        table.setMaxWidth(tableWidth);
        Node index = scene.lookup("#index_Click");
        index.setStyle("-fx-pref-width: " + tableWidth * 0.05 + "px;");
        Node thumb = scene.lookup("#thumb_Click");
        thumb.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node name = scene.lookup("#name_Click");
        name.setStyle("-fx-pref-width: " + tableWidth * 0.15 + "px;");
        Node clickTime = scene.lookup("#clickTime_Click");
        clickTime.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node clickNum = scene.lookup("#clickNum_Click");
        clickNum.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node clickInterval = scene.lookup("#clickInterval_Click");
        clickInterval.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node waitTime = scene.lookup("#waitTime_Click");
        waitTime.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node clickType = scene.lookup("#clickType_Click");
        clickType.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node retryType = scene.lookup("#retryType_Click");
        retryType.setStyle("-fx-pref-width: " + tableWidth * 0.2 + "px;");
        Label dataNum = (Label) scene.lookup("#dataNumber_Click");
        HBox fileNumberHBox = (HBox) scene.lookup("#fileNumberHBox_Click");
        nodeRightAlignment(fileNumberHBox, tableWidth, dataNum);
        Label tip = (Label) scene.lookup("#tip_Click");
        HBox tipHBox = (HBox) scene.lookup("#tipHBox_Click");
        nodeRightAlignment(tipHBox, tableWidth, tip);
        Label cancelTip = (Label) scene.lookup("#cancelTip_Click");
        HBox cancelTipHBox = (HBox) scene.lookup("#cancelTipHBox_Click");
        nodeRightAlignment(cancelTipHBox, tableWidth, cancelTip);
        Label err = (Label) scene.lookup("#err_Click");
        HBox logHBox = (HBox) scene.lookup("#logHBox_Click");
        nodeRightAlignment(logHBox, tableWidth, err);
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
            TextField loopTime = (TextField) scene.lookup("#loopTime_Click");
            prop.put(key_lastLoopTime, loopTime.getText());
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
            CheckBox autoSave = (CheckBox) scene.lookup("#autoSave_Set");
            TableView<?> tableView = (TableView<?>) scene.lookup("#tableView_Click");
            // 自动保存
            autoSave(autoSave, tableView);
        }
    }

    /**
     * 自动保存操作流程
     *
     * @param autoSave  自动保存开关
     * @param tableView 操作流程列表
     * @throws IOException io异常
     */
    private static void autoSave(CheckBox autoSave, TableView<?> tableView) throws IOException {
        if (autoSave.isSelected()) {
            List<?> tableViewItems = new ArrayList<>(tableView.getItems());
            if (CollectionUtils.isNotEmpty(tableViewItems)) {
                ObjectMapper objectMapper = new ObjectMapper();
                String path = notOverwritePath(outFilePath + File.separator + autoSaveFileName + PMC);
                // 构建基类类型信息
                JavaType baseType = objectMapper.getTypeFactory().constructParametricType(List.class, ClickPositionBean.class);
                // 使用基类类型进行序列化
                objectMapper.writerFor(baseType).writeValue(new File(path), tableViewItems);
            }
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
            setControlLastConfig(outPath_Click, prop, key_outFilePath);
            setControlLastConfig(loopTime_Click, prop, key_lastLoopTime);
            setControlLastConfig(outFileName_Click, prop, key_lastOutFileName);
            setControlLastConfig(openDirectory_Click, prop, key_lastOpenDirectory);
            setControlLastConfig(preparationRunTime_Click, prop, key_lastPreparationRunTime);
            setControlLastConfig(preparationRecordTime_Click, prop, key_lastPreparationRecordTime);
        }
        if (StringUtils.isBlank(outPath_Click.getText())) {
            setPathLabel(outPath_Click, defaultFileChooserPath, false);
            outFilePath = defaultFileChooserPath;
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
        String marginStr = prop.getProperty(key_margin);
        if (StringUtils.isNotBlank(marginStr)) {
            margin = Integer.parseInt(marginStr);
        }
        Slider clickOpacity = (Slider) mainScene.lookup("#clickOpacity_Set");
        defaultClickOpacity = String.valueOf(clickOpacity.getValue());
        Slider stopOpacity = (Slider) mainScene.lookup("#stopOpacity_Set");
        defaultStopOpacity = String.valueOf(stopOpacity.getValue());
        TextField clickRetryNumTextField = (TextField) mainScene.lookup("#clickRetryNum_Set");
        clickRetryNum = clickRetryNumTextField.getText() == null ? defaultStopRetryNum : clickRetryNumTextField.getText();
        TextField stopRetryNumTextField = (TextField) mainScene.lookup("#stopRetryNum_Set");
        stopRetryNum = stopRetryNumTextField.getText() == null ? defaultStopRetryNum : stopRetryNumTextField.getText();
        TableView<?> tableView = (TableView<?>) mainScene.lookup("#tableView_Set");
        defaultStopImgFiles = tableView.getItems().stream().map(o -> (ImgFileBean) o).toList();
        inFilePath = prop.getProperty(key_inFilePath);
        outFilePath = prop.getProperty(key_outFilePath);
        autoSaveFileName = prop.getProperty(key_autoSaveFileName);
        stopImgSelectPath = prop.getProperty(key_stopImgSelectPath);
        clickImgSelectPath = prop.getProperty(key_clickImgSelectPath);
        defaultOutFileName = prop.getProperty(key_defaultOutFileName);
        floatingX = Integer.parseInt(prop.getProperty(key_floatingX));
        floatingY = Integer.parseInt(prop.getProperty(key_floatingY));
        detailWidth = Integer.parseInt(prop.getProperty(key_detailWidth));
        detailHeight = Integer.parseInt(prop.getProperty(key_detailHeight));
        floatingWidth = Integer.parseInt(prop.getProperty(key_floatingWidth));
        floatingHeight = Integer.parseInt(prop.getProperty(key_floatingHeight));
        defaultPreparationRunTime = prop.getProperty(key_defaultPreparationRunTime);
        defaultPreparationRecordTime = prop.getProperty(key_defaultPreparationRecordTime);
        input.close();
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.05));
        thumb_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        name_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.15));
        clickTime_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        clickNum_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        clickInterval_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        waitTime_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        clickType_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        retryType_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.2));
    }

    /**
     * 设置单元格可编辑
     */
    private void makeCellCanEdit() {
        tableView_Click.setEditable(true);
        name_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionVO::setName));
        clickInterval_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionVO::setClickInterval, true, 0, null));
        waitTime_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionVO::setWaitTime, true, 0, null));
        clickTime_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionVO::setClickTime, true, 0, null));
        clickNum_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionVO::setClickNum, true, 0, null));
    }

    /**
     * 显示详情页
     *
     * @param item 要显示详情的操作流程设置
     */
    private void showDetail(ClickPositionVO item) {
        URL fxmlLocation = getClass().getResource(resourcePath + "fxml/Detail-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DetailController controller = loader.getController();
        item.setClickImgSelectPath(clickImgSelectPath);
        item.setStopImgSelectPath(stopImgSelectPath);
        try {
            controller.initData(item, mainStage);
        } catch (IOException e) {
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
            dataNumber_Click.setText(text_allHave + tableView_Click.getItems().size() + text_process);
        });
        Stage detailStage = new Stage();
        Scene scene = new Scene(root, detailWidth, detailHeight);
        detailStage.setScene(scene);
        detailStage.setTitle(item.getName() + " 步骤详情");
        detailStage.initModality(Modality.APPLICATION_MODAL);
        setWindLogo(detailStage, logoPath);
        // 监听窗口面板宽度变化
        detailStage.widthProperty().addListener((v1, v2, v3) -> Platform.runLater(controller::detailAdaption));
        // 监听窗口面板高度变化
        detailStage.heightProperty().addListener((v1, v2, v3) -> Platform.runLater(controller::detailAdaption));
        scene.getStylesheets().add(Objects.requireNonNull(MainApplication.class.getResource("css/Styles.css")).toExternalForm());
        detailStage.show();
    }

    /**
     * 初始化浮窗
     */
    private void initFloatingWindow() {
        // 获取主屏幕信息（初始位置用）
        Screen primaryScreen = Screen.getPrimary();
        Rectangle2D primaryBounds = primaryScreen.getBounds();
        // 创建一个矩形作为浮窗的内容
        rectangle = new Rectangle(floatingWidth, floatingHeight);
        // 设置透明度
        rectangle.setOpacity(0);
        StackPane root = new StackPane();
        root.setBackground(Background.fill(Color.TRANSPARENT));
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
        // 初始位置设置在主屏幕顶部居中
        floatingStage.setX(primaryBounds.getMinX() + (primaryBounds.getWidth() - floatingWidth) / 2);
        floatingStage.setY(primaryBounds.getMinY() - margin);
    }

    /**
     * 显示浮窗
     *
     * @param isRun 是否为运行自动操作
     * @throws IOException io异常
     */
    private void showFloatingWindow(boolean isRun) throws IOException {
        // 获取浮窗的文本颜色设置
        ColorPicker colorPicker = (ColorPicker) mainScene.lookup("#colorPicker_Set");
        Color color = colorPicker.getValue();
        floatingLabel.setTextFill(color);
        floatingMousePosition.setTextFill(color);
        getConfig();
        floatingStage.setX(floatingX);
        floatingStage.setY(floatingY);
        // 获取浮窗的显示设置
        CheckBox floatingRun = (CheckBox) mainScene.lookup("#floatingRun_Set");
        CheckBox floatingRecord = (CheckBox) mainScene.lookup("#floatingRecord_Set");
        boolean isShow = isRun ? floatingRun.isSelected() : floatingRecord.isSelected();
        Slider slider = (Slider) mainScene.lookup("#opacity_Set");
        rectangle.setOpacity(slider.getValue());
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
     * @param clickPositionVOS 自动操作流程
     * @throws IOException io异常
     */
    private void launchClickTask(List<ClickPositionVO> clickPositionVOS) throws IOException {
        if (!runClicking && !recordClicking) {
            runClicking = true;
            CheckBox firstClick = (CheckBox) mainScene.lookup("#firstClick_Set");
            AutoClickTaskBean taskBean = new AutoClickTaskBean();
            taskBean.setLoopTime(setDefaultIntValue(loopTime_Click, 1, 0, null))
                    .setFirstClick(firstClick.isSelected())
                    .setFloatingLabel(floatingLabel)
                    .setRunTimeline(runTimeline)
                    .setProgressBar(progressBar_Click)
                    .setBindingMassageLabel(false)
                    .setDisableNodes(disableNodes)
                    .setBeanList(clickPositionVOS)
                    .setMassageLabel(log_Click);
            updateLabel(log_Click, "");
            CheckBox hideWindowRun = (CheckBox) mainScene.lookup("#hideWindowRun_Set");
            if (hideWindowRun.isSelected()) {
                mainStage.setIconified(true);
            }
            // 刷新屏幕参数
            refreshScreenParameters();
            // 开启键盘监听
            startNativeKeyListener();
            // 创建一个Robot实例
            Robot robot = new Robot();
            autoClickTask = autoClick(taskBean, robot);
            // 绑定带进度条的线程
            bindingTaskNode(autoClickTask, taskBean);
            autoClickTask.setOnSucceeded(event -> {
                taskUnbind(taskBean);
                log_Click.setTextFill(Color.GREEN);
                log_Click.setText(text_taskFinished);
                hideFloatingWindow();
                CheckBox showWindowRun = (CheckBox) mainScene.lookup("#showWindowRun_Set");
                if (showWindowRun.isSelected()) {
                    showStage(mainStage);
                }
                // 移除键盘监听器
                removeNativeListener(nativeKeyListener);
                autoClickTask = null;
                runTimeline = null;
                runClicking = false;
            });
            autoClickTask.setOnFailed(event -> {
                taskNotSuccess(taskBean, text_taskFailed);
                hideFloatingWindow();
                CheckBox showWindowRun = (CheckBox) mainScene.lookup("#showWindowRun_Set");
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
                // 获取抛出的异常
                Throwable ex = autoClickTask.getException();
                autoClickTask = null;
                runClicking = false;
                throw new RuntimeException(ex);
            });
            autoClickTask.setOnCancelled(event -> {
                taskNotSuccess(taskBean, text_taskCancelled);
                hideFloatingWindow();
                CheckBox showWindowRun = (CheckBox) mainScene.lookup("#showWindowRun_Set");
                if (showWindowRun.isSelected()) {
                    showStage(mainStage);
                }
                // 移除键盘监听器
                removeNativeListener(nativeKeyListener);
                autoClickTask = null;
                runTimeline = null;
                runClicking = false;
            });
            if (runTimeline == null) {
                // 改变要防重复点击的组件状态
                changeDisableNodes(taskBean, true);
                // 获取准备时间值
                int preparationTimeValue = setDefaultIntValue(preparationRunTime_Click, Integer.parseInt(defaultPreparationRunTime), 0, null);
                // 设置浮窗文本显示准备时间
                String text = text_cancelTask + preparationTimeValue + text_run;
                floatingLabel.setText(text);
                log_Click.setText(text);
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
            if (!autoClickTask.isRunning()) {
                // 使用新线程启动
                executorService.execute(autoClickTask);
            }
            return runTimeline;
        }
        runTimeline = new Timeline();
        AtomicInteger preparationTime = new AtomicInteger(preparationTimeValue);
        // 创建 Timeline 来实现倒计时
        Timeline finalTimeline = runTimeline;
        runTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            preparationTime.getAndDecrement();
            if (preparationTime.get() > 0) {
                String text = text_cancelTask + preparationTime + text_run;
                floatingLabel.setText(text);
                log_Click.setText(text);
            } else {
                // 停止 Timeline
                finalTimeline.stop();
                if (!autoClickTask.isRunning()) {
                    // 使用新线程启动
                    executorService.execute(autoClickTask);
                }
            }
        }));
        // 设置 Timeline 的循环次数
        runTimeline.setCycleCount(preparationTimeValue);
        runTimeline.play();
        return runTimeline;
    }

    /**
     * 构建右键菜单
     */
    private void buildContextMenu() {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 查看详情选项
        buildDetailMenuItem(tableView_Click, contextMenu);
        // 修改所选项要点击的图片地址
        buildEditClickImgPathMenu(tableView_Click, contextMenu);
        // 添加测试点击选项
        buildClickTestMenuItem(tableView_Click, contextMenu);
        // 移动所选行选项
        buildMoveDataMenu(tableView_Click, contextMenu);
        // 修改操作类型
        buildEditClickTypeMenu(tableView_Click, contextMenu);
        // 修改重试类型
        buildEditRetryTypeMenu(tableView_Click, contextMenu);
        // 插入数据选项
        insertDataMenu(tableView_Click, contextMenu);
        // 复制数据选项
        buildCopyDataMenu(tableView_Click, contextMenu, dataNumber_Click);
        // 取消选中选项
        buildClearSelectedData(tableView_Click, contextMenu);
        // 删除所选数据选项
        buildDeleteDataMenuItem(tableView_Click, dataNumber_Click, contextMenu, text_data);
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(contextMenu, tableView_Click);
    }

    /**
     * 查看所选项第一行详情选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    private void buildDetailMenuItem(TableView<ClickPositionVO> tableView, ContextMenu contextMenu) {
        MenuItem detailItem = new MenuItem("查看所选项第一行详情");
        detailItem.setOnAction(e -> {
            ClickPositionVO selected = tableView.getSelectionModel().getSelectedItems().getFirst();
            if (selected != null) {
                showDetail(selected);
            }
        });
        contextMenu.getItems().add(detailItem);
    }

    /**
     * 执行选中的步骤选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    private void buildClickTestMenuItem(TableView<ClickPositionVO> tableView, ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem("执行选中的步骤");
        menuItem.setOnAction(event -> {
            List<ClickPositionVO> selectedItem = tableView.getSelectionModel().getSelectedItems();
            if (CollectionUtils.isNotEmpty(selectedItem)) {
                try {
                    launchClickTask(selectedItem);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 插入数据选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    private void insertDataMenu(TableView<ClickPositionVO> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu("插入数据");
        // 创建二级菜单项
        MenuItem insertUp = new MenuItem(menuItem_insertUp);
        MenuItem insertDown = new MenuItem(menuItem_insertDown);
        MenuItem recordUp = new MenuItem(menuItem_recordUp);
        MenuItem recordDown = new MenuItem(menuItem_recordDown);
        MenuItem insertTop = new MenuItem(menuItem_insertTop);
        MenuItem recordTop = new MenuItem(menuItem_recordTop);
        // 为每个菜单项添加事件处理
        insertUp.setOnAction(event -> insertDataMenuItem(tableView, menuItem_insertUp));
        insertDown.setOnAction(event -> insertDataMenuItem(tableView, menuItem_insertDown));
        recordUp.setOnAction(event -> insertDataMenuItem(tableView, menuItem_recordUp));
        recordDown.setOnAction(event -> insertDataMenuItem(tableView, menuItem_recordDown));
        insertTop.setOnAction(event -> insertDataMenuItem(tableView, menuItem_insertTop));
        recordTop.setOnAction(event -> insertDataMenuItem(tableView, menuItem_recordTop));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(insertUp, insertDown, recordUp, recordDown, insertTop, recordTop);
        contextMenu.getItems().add(menu);
    }

    /**
     * 插入数据选项二级菜单选项
     *
     * @param tableView  要处理的数据列表
     * @param insertType 数据插入类型
     */
    private void insertDataMenuItem(TableView<ClickPositionVO> tableView, String insertType) {
        List<ClickPositionVO> selectedItem = tableView.getSelectionModel().getSelectedItems();
        if (CollectionUtils.isNotEmpty(selectedItem)) {
            switch (insertType) {
                case menuItem_insertUp: {
                    addClick(upAdd);
                    break;
                }
                case menuItem_insertDown: {
                    addClick(downAdd);
                    break;
                }
                case menuItem_recordUp: {
                    startRecord(upAdd);
                    break;
                }
                case menuItem_recordDown: {
                    startRecord(downAdd);
                    break;
                }
                case menuItem_insertTop: {
                    addClick(topAdd);
                    break;
                }
                case menuItem_recordTop: {
                    startRecord(topAdd);
                    break;
                }
            }
        }
    }

    /**
     * 获取点击步骤设置
     *
     * @param tableViewItemSize 列表数据量（生成默认操作名称用）
     * @return clickPositionBean 自动操作步骤类
     */
    private ClickPositionVO getClickSetting(int tableViewItemSize) {
        try {
            getConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ClickPositionVO clickPositionVO = new ClickPositionVO();
        clickPositionVO.setTableView(tableView_Click)
                .setName(text_step + (tableViewItemSize + 1) + text_isAdd)
                .setClickMatchThreshold(defaultClickOpacity)
                .setStopMatchThreshold(defaultStopOpacity)
                .setStopImgFiles(defaultStopImgFiles)
                .setClickType(mouseButton_primary)
                .setClickRetryTimes(clickRetryNum)
                .setStopRetryTimes(stopRetryNum)
                .setRetryType(retryType_stop)
                .setClickInterval("0")
                .setClickTime("0")
                .setClickNum("1")
                .setWaitTime("0")
                .setStartX("0")
                .setStartY("0")
                .setEndX("0")
                .setEndY("0");
        if (tableViewItemSize == -1) {
            clickPositionVO.setName("测试步骤");
        }
        return clickPositionVO;
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 导出自动流程文件名称文本输入框鼠标悬停提示
        textFieldValueListener(outFileName_Click, tip_autoClickFileName + defaultOutFileName);
        // 限制循环次数文本输入框内容
        integerRangeTextField(loopTime_Click, 0, null, tip_loopTime);
        // 限制运行准备时间文本输入框内容
        integerRangeTextField(preparationRunTime_Click, 0, null, tip_preparationRunTime + defaultPreparationRunTime);
        // 限制录制准备时间文本输入框内容
        integerRangeTextField(preparationRecordTime_Click, 0, null, tip_preparationRecordTime + defaultPreparationRecordTime);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_runClick, runClick_Click);
        addToolTip(tip_loopTime, loopTime_Click);
        addToolTip(tip_clickTest, clickTest_Click);
        addToolTip(tip_learButton, clearButton_Click);
        addToolTip(tip_addPosition, addPosition_Click);
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
     * 导入自动操作流程文件
     *
     * @param filePath 文件路径
     * @throws IOException 导入自动化流程文件内容格式不正确
     */
    private void loadPMCFile(String filePath) throws IOException {
        // 读取 JSON 文件并转换为 List<ClickPositionBean>
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File(filePath);
        List<ClickPositionBean> clickPositionBeans;
        try {
            clickPositionBeans = objectMapper.readValue(jsonFile, objectMapper.getTypeFactory().constructCollectionType(List.class, ClickPositionBean.class));
        } catch (MismatchedInputException | JsonParseException e) {
            throw new IOException(text_loadAutoClick + filePath + text_formatError);
        }
        if (CollectionUtils.isNotEmpty(clickPositionBeans)) {
            List<ClickPositionVO> clickPositionVOS = new ArrayList<>();
            clickPositionBeans.forEach(bean -> {
                ClickPositionVO vo = new ClickPositionVO();
                try {
                    // 自动拷贝父类中的属性
                    copyProperties(bean, vo);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                // 初始化子类特有属性
                vo.setTableView(tableView_Click)
                        .setRemove(false)
                        .setUuid(UUID.randomUUID().toString());

                clickPositionVOS.add(vo);
            });
            // 将自动流程添加到列表中
            addAutoClickPositions(clickPositionVOS);
        }
    }

    /**
     * 将自动流程添加到列表中
     *
     * @param clickPositionVOS 自动流程集合
     */
    private void addAutoClickPositions(List<ClickPositionVO> clickPositionVOS) throws IOException {
        for (ClickPositionVO clickPositionVO : clickPositionVOS) {
            clickPositionVO.setUuid(UUID.randomUUID().toString());
            if (!isInIntegerRange(clickPositionVO.getStartX(), 0, null) || !isInIntegerRange(clickPositionVO.getStartY(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getEndX(), 0, null) || !isInIntegerRange(clickPositionVO.getEndY(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getClickTime(), 0, null) || !isInIntegerRange(clickPositionVO.getClickNum(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getClickInterval(), 0, null) || !isInIntegerRange(clickPositionVO.getWaitTime(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getClickRetryTimes(), 0, null) || !isInIntegerRange(clickPositionVO.getStopRetryTimes(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getClickMatchThreshold(), 0, 100) || !isInIntegerRange(clickPositionVO.getStopMatchThreshold(), 0, 100)
                    || !runClickTypeMap.containsKey(clickPositionVO.getClickType()) || !retryTypeList.contains(clickPositionVO.getRetryType())) {
                throw new IOException(text_LackKeyData);
            }
        }
        // 向列表添加数据
        addData(clickPositionVOS, append, tableView_Click, dataNumber_Click, text_process);
        updateLabel(log_Click, text_loadSuccess + inFilePath);
        log_Click.setTextFill(Color.GREEN);
    }

    /**
     * 根据鼠标位置调整ui
     */
    private void onMousePositionUpdate() {
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        int x = (int) mousePoint.getX();
        int y = (int) mousePoint.getY();
        String text = "当前鼠标位置为： X: " + x + " Y: " + y;
        CheckBox mouseFloatingRun = (CheckBox) mainScene.lookup("#mouseFloatingRun_Set");
        CheckBox mouseFloatingRecord = (CheckBox) mainScene.lookup("#mouseFloatingRecord_Set");
        TextField offsetXTextField = (TextField) mainScene.lookup("#offsetX_Set");
        int offsetX = setDefaultIntValue(offsetXTextField, defaultOffsetX, 0, null);
        TextField offsetYTextField = (TextField) mainScene.lookup("#offsetY_Set");
        int offsetY = setDefaultIntValue(offsetYTextField, defaultOffsetY, 0, null);
        Platform.runLater(() -> {
            floatingMousePosition.setText(text);
            mousePosition_Click.setText(text);
            if (floatingStage != null && floatingStage.isShowing()) {
                if ((mouseFloatingRun.isSelected() && runClicking) || (mouseFloatingRecord.isSelected() && recordClicking)) {
                    floatingMove(floatingStage, mousePoint, offsetX, offsetY);
                }
            }
        });
    }

    /**
     * 开启全局键盘监听
     */
    private void startNativeKeyListener() {
        removeNativeListener(nativeKeyListener);
        // 键盘监听器
        nativeKeyListener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                Platform.runLater(() -> {
                    // 仅在自动操作与录制情况下才监听键盘
                    if (recordClicking || runClicking) {
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
                                taskBean.setProgressBar(progressBar_Click)
                                        .setMassageLabel(log_Click);
                                taskNotSuccess(taskBean, text_taskCancelled);
                            }
                            // 改变要防重复点击的组件状态
                            changeDisableNodes(disableNodes, false);
                            // 移除鼠标监听器
                            removeNativeListener(nativeMouseListener);
                            hideFloatingWindow();
                            // 弹出程序主窗口
                            CheckBox showWindowRecord = (CheckBox) mainScene.lookup("#showWindowRecord_Set");
                            if (showWindowRecord.isSelected()) {
                                showStage(mainStage);
                            }
                            // 移除键盘监听器
                            removeNativeListener(nativeKeyListener);
                            recordClicking = false;
                            runClicking = false;
                        }
                    }
                });
            }
        };
        GlobalScreen.addNativeKeyListener(nativeKeyListener);
    }

    /**
     * 开启全局鼠标监听
     *
     * @param addType 添加类型
     */
    private void startNativeMouseListener(int addType) {
        removeNativeListener(nativeMouseListener);
        try {
            getConfig();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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
            ClickPositionVO clickBean;

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
                    clickBean = new ClickPositionVO();
                    clickBean.setTableView(tableView_Click)
                            .setClickType(recordClickTypeMap.get(pressButton))
                            .setName(text_step + dataSize + text_isRecord)
                            .setClickMatchThreshold(defaultClickOpacity)
                            .setStopMatchThreshold(defaultStopOpacity)
                            .setWaitTime(String.valueOf(waitTime))
                            .setStopImgFiles(defaultStopImgFiles)
                            .setStartX(String.valueOf(startX))
                            .setStartY(String.valueOf(startY))
                            .setClickRetryTimes(clickRetryNum)
                            .setStopRetryTimes(stopRetryNum)
                            .setRetryType(retryType_stop);
                    Platform.runLater(() -> {
                        log_Click.setTextFill(Color.BLUE);
                        String log = text_cancelTask + text_recordClicking + "\n" +
                                text_recorded + recordClickTypeMap.get(pressButton) +
                                " 点击 X：" + clickBean.getStartX() + " Y：" + clickBean.getStartY();
                        log_Click.setText(log);
                        floatingLabel.setText(log);
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
                        List<ClickPositionVO> clickPositionVOS = new ArrayList<>();
                        clickPositionVOS.add(clickBean);
                        addData(clickPositionVOS, addType, tableView_Click, dataNumber_Click, text_process);
                        String log = text_cancelTask + text_recordClicking + "\n" +
                                text_recorded + recordClickTypeMap.get(pressButton) +
                                "松开 X：" + clickBean.getEndX() + " Y：" + clickBean.getEndY();
                        log_Click.setText(log);
                        floatingLabel.setText(log);
                    });
                }
            }
        };
        GlobalScreen.addNativeMouseListener(nativeMouseListener);
    }

    /**
     * 开始录制
     *
     * @param addType 添加类型
     */
    private void startRecord(int addType) {
        if (!runClicking && !recordClicking) {
            recordClicking = true;
            // 改变要防重复点击的组件状态
            changeDisableNodes(disableNodes, true);
            // 获取准备时间值
            int preparationTimeValue = setDefaultIntValue(preparationRecordTime_Click, Integer.parseInt(defaultPreparationRecordTime), 0, null);
            // 开始录制
            CheckBox hideWindowRecord = (CheckBox) mainScene.lookup("#hideWindowRecord_Set");
            if (hideWindowRecord.isSelected()) {
                mainStage.setIconified(true);
            }
            // 开启键盘监听
            startNativeKeyListener();
            // 设置浮窗文本显示准备时间
            AtomicReference<String> text = new AtomicReference<>(text_cancelTask + preparationTimeValue + text_preparation);
            floatingLabel.setText(text.get());
            updateLabel(log_Click, text.get());
            // 显示浮窗
            try {
                showFloatingWindow(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (preparationTimeValue == 0) {
                // 开启鼠标监听
                startNativeMouseListener(addType);
                // 录制开始时间
                recordingStartTime = System.currentTimeMillis();
                // 更新浮窗文本
                text.set(text_cancelTask + text_recordClicking);
                floatingLabel.setText(text.get());
                log_Click.setText(text.get());
            } else {
                recordTimeline = new Timeline();
                AtomicInteger preparationTime = new AtomicInteger(preparationTimeValue);
                // 创建 Timeline 来实现倒计时
                Timeline finalTimeline = recordTimeline;
                recordTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                    preparationTime.getAndDecrement();
                    if (preparationTime.get() > 0) {
                        text.set(text_cancelTask + preparationTime + text_preparation);
                    } else {
                        // 开启鼠标监听
                        startNativeMouseListener(addType);
                        // 录制开始时间
                        recordingStartTime = System.currentTimeMillis();
                        // 停止 Timeline
                        finalTimeline.stop();
                        // 更新浮窗文本
                        text.set(text_cancelTask + text_recordClicking);
                    }
                    floatingLabel.setText(text.get());
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
            addData(clickPositionVOS, addType, tableView_Click, dataNumber_Click, text_process);
            // 初始化信息栏
            updateLabel(log_Click, "");
            // 显示详情
            showDetail(clickPositionVO);
        }
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(runClick_Click);
        disableNodes.add(clickTest_Click);
        disableNodes.add(tableView_Click);
        disableNodes.add(recordClick_Click);
        disableNodes.add(addPosition_Click);
        disableNodes.add(clearButton_Click);
        disableNodes.add(loadAutoClick_Click);
        disableNodes.add(exportAutoClick_Click);
        Node autoClickTab = mainScene.lookup("#autoClickTab");
        disableNodes.add(autoClickTab);
        Node settingTab = mainScene.lookup("#settingTab");
        disableNodes.add(settingTab);
        Node aboutTab = mainScene.lookup("#aboutTab");
        disableNodes.add(aboutTab);
    }

    /**
     * 禁用需要辅助控制权限的组件
     */
    private void setNativeHookExceptionLog() {
        runClick_Click.setDisable(true);
        recordClick_Click.setDisable(true);
        clickTest_Click.setDisable(true);
        Button saveButton = (Button) mainScene.lookup("#setFloatingCoordinate_Set");
        saveButton.setDisable(true);
        String errorMessage = appName + " 缺少必要系统权限";
        if (systemName.contains(macos)) {
            errorMessage = tip_NativeHookException;
        }
        err_Click.setText(errorMessage);
        err_Click.setTooltip(creatTooltip(tip_NativeHookException));
    }

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            mainScene = anchorPane_Click.getScene();
            mainStage = (Stage) mainScene.getWindow();
            try {
                // 读取配置文件
                getConfig();
                // 设置鼠标悬停提示
                setToolTip();
                // 给输入框添加内容变化监听
                textFieldChangeListener();
                // 设置初始配置值为上次配置值
                setLastConfig();
                // 注册全局输入监听器
                GlobalScreen.registerNativeHook();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NativeHookException e) {
                setNativeHookExceptionLog();
            }
            // 设置javafx单元格宽度
            bindPrefWidthProperty();
            // 初始化浮窗
            initFloatingWindow();
            // 获取鼠标坐标监听器
            new MousePositionListener(this::onMousePositionUpdate);
            // 设置要防重复点击的组件
            setDisableNodes();
            // 自动填充javafx表格
            autoBuildTableViewData(tableView_Click, ClickPositionVO.class, tabId, index_Click);
            // 表格设置为可编辑
            makeCellCanEdit();
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Click);
            // 构建右键菜单
            buildContextMenu();
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
     *
     * @throws IOException io异常
     */
    @FXML
    private void clickTest() throws IOException {
        // 获取步骤设置
        List<ClickPositionVO> clickPositionVOS = new ArrayList<>();
        ClickPositionVO clickPositionVO = getClickSetting(-1);
        clickPositionVOS.add(clickPositionVO);
        // 启动自动操作流程
        launchClickTask(clickPositionVOS);
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
     * @throws IOException io异常
     */
    @FXML
    public void loadAutoClick(ActionEvent actionEvent) throws IOException {
        if (autoClickTask == null && !recordClicking) {
            getConfig();
            List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(new FileChooser.ExtensionFilter("Perfect Mouse Control", "*.pmc")));
            Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
            File selectedFile = creatFileChooser(window, inFilePath, extensionFilters, text_selectAutoFile);
            if (selectedFile != null) {
                inFilePath = selectedFile.getPath();
                updateProperties(configFile_Click, key_inFilePath, new File(inFilePath).getParent());
                loadPMCFile(inFilePath);
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
            List<ClickPositionBean> tableViewItems = new ArrayList<>(tableView_Click.getItems());
            if (CollectionUtils.isEmpty(tableViewItems)) {
                throw new Exception(text_noAutoClickList);
            }
            if (StringUtils.isBlank(outFilePath)) {
                throw new Exception(text_outPathNull);
            }
            String fileName = setDefaultFileName(outFileName_Click, defaultOutFileName);
            ObjectMapper objectMapper = new ObjectMapper();
            String path = notOverwritePath(outFilePath + File.separator + fileName + PMC);
            // 构建基类类型信息
            JavaType baseType = objectMapper.getTypeFactory().constructParametricType(List.class, ClickPositionBean.class);
            // 使用基类类型进行序列化
            objectMapper.writerFor(baseType).writeValue(new File(path), tableViewItems);
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
     * @param actionEvent 点击事件
     * @throws IOException io异常
     */
    @FXML
    private void addOutPath(ActionEvent actionEvent) throws IOException {
        getConfig();
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatDirectoryChooser(window, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Click, configFile_Click);
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
            for (File file : files) {
                loadPMCFile(file.getPath());
            }
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
                try {
                    if (PMC.equals(getExistsFileType(file))) {
                        // 接受拖放
                        dragEvent.acceptTransferModes(TransferMode.COPY);
                        dragEvent.consume();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * 录制自动操作按钮
     */
    @FXML
    private void recordClick() {
        startRecord(append);
    }

}
