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
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import priv.koishi.pmc.Bean.ClickLogBean;
import priv.koishi.pmc.Bean.ClickPositionBean;
import priv.koishi.pmc.Bean.ImgFileBean;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;
import priv.koishi.pmc.EditingCell.EditingCell;
import priv.koishi.pmc.Listener.MousePositionListener;
import priv.koishi.pmc.Listener.MousePositionUpdater;
import priv.koishi.pmc.MainApplication;
import priv.koishi.pmc.Properties.CommonProperties;
import priv.koishi.pmc.ThreadPool.ThreadPoolManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.MacScreenPermissionChecker.MacScreenPermissionChecker.hasScreenCapturePermission;
import static priv.koishi.pmc.Service.AutoClickService.*;
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
public class AutoClickController extends CommonProperties implements MousePositionUpdater {

    /**
     * 导入文件路径
     */
    private static String inFilePath;

    /**
     * 上次所选要点击的图片地址
     */
    private static String clickImgSelectPath;

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
    private List<ClickLogBean> clickLogs;

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
     * 浮窗X坐标
     */
    private int floatingX;

    /**
     * 浮窗Y坐标
     */
    private int floatingY;

    /**
     * 浮窗宽度
     */
    private int floatingWidth;

    /**
     * 浮窗高度
     */
    private int floatingHeight;

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
     * 要防重复点击的组件
     */
    private static final List<Node> disableNodes = new ArrayList<>();

    /**
     * 线程池实例
     */
    private static final ExecutorService executorService = ThreadPoolManager.getPool(AutoClickController.class);

    /**
     * 自动点击任务
     */
    private Task<List<ClickLogBean>> autoClickTask;

    /**
     * 页面标识符
     */
    private static final String tabId = "_Click";

    /**
     * 无辅助功能权限
     */
    private boolean isNativeHookException = false;

    /**
     * 无录屏与录音权限
     */
    private boolean noScreenCapturePermission = false;

    /**
     * 正在录制标识
     */
    private boolean recordClicking;

    /**
     * 正在录制标识（准备时间结束）
     */
    private boolean isRecordClicking;

    /**
     * 正在运行自动操作标识
     */
    private boolean runClicking;

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
            exportAutoClick_Click, addOutPath_Click, recordClick_Click, clickLog_Click;

    @FXML
    private TextField loopTime_Click, outFileName_Click, preparationRecordTime_Click, preparationRunTime_Click;

    @FXML
    private TableView<ClickPositionVO> tableView_Click;

    @FXML
    private TableColumn<ClickPositionVO, Integer> index_Click;

    @FXML
    private TableColumn<ClickPositionVO, ImageView> thumb_Click;

    @FXML
    private TableColumn<ClickPositionVO, String> name_Click, clickTime_Click, clickNum_Click, clickKey_Click,
            waitTime_Click, clickType_Click, matchedType_Click, retryType_Click;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void adaption(Stage stage) {
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
        name.setStyle("-fx-pref-width: " + tableWidth * 0.13 + "px;");
        Node clickTime = scene.lookup("#clickTime_Click");
        clickTime.setStyle("-fx-pref-width: " + tableWidth * 0.07 + "px;");
        Node clickNum = scene.lookup("#clickNum_Click");
        clickNum.setStyle("-fx-pref-width: " + tableWidth * 0.07 + "px;");
        Node clickKey = scene.lookup("#clickKey_Click");
        clickKey.setStyle("-fx-pref-width: " + tableWidth * 0.08 + "px;");
        Node waitTime = scene.lookup("#waitTime_Click");
        waitTime.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node clickType = scene.lookup("#clickType_Click");
        clickType.setStyle("-fx-pref-width: " + tableWidth * 0.08 + "px;");
        Node matchedType = scene.lookup("#matchedType_Click");
        matchedType.setStyle("-fx-pref-width: " + tableWidth * 0.16 + "px;");
        Node retryType = scene.lookup("#retryType_Click");
        retryType.setStyle("-fx-pref-width: " + tableWidth * 0.16 + "px;");
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
        if (err != null) {
            HBox logHBox = (HBox) scene.lookup("#logHBox_Click");
            nodeRightAlignment(logHBox, tableWidth, err);
        }
    }

    /**
     * 保存最后一次配置的值
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    public static void saveLastConfig(Scene scene) throws IOException {
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
            String outPathValue = outPath.getText();
            prop.put(key_outFilePath, outPathValue);
            OutputStream output = checkRunningOutputStream(configFile_Click);
            prop.store(output, null);
            input.close();
            output.close();
            CheckBox autoSave = (CheckBox) scene.lookup("#autoSave_Set");
            TableView<?> tableView = (TableView<?>) scene.lookup("#tableView_Click");
            // 自动保存
            autoSave(autoSave, tableView, outPathValue);
        }
    }

    /**
     * 自动保存操作流程
     *
     * @param autoSave  自动保存开关
     * @param tableView 操作流程列表
     * @throws IOException io异常
     */
    private static void autoSave(CheckBox autoSave, TableView<?> tableView, String outPath) throws IOException {
        if (autoSave.isSelected()) {
            List<?> tableViewItems = new ArrayList<>(tableView.getItems());
            if (CollectionUtils.isNotEmpty(tableViewItems)) {
                ObjectMapper objectMapper = new ObjectMapper();
                String path = notOverwritePath(outPath + File.separator + autoSaveFileName + PMC);
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
        if (activation.equals(prop.getProperty(key_loadLastConfig, activation))) {
            setControlLastConfig(outPath_Click, prop, key_outFilePath);
            setControlLastConfig(loopTime_Click, prop, key_lastLoopTime);
            setControlLastConfig(outFileName_Click, prop, key_lastOutFileName);
            setControlLastConfig(openDirectory_Click, prop, key_lastOpenDirectory);
            setControlLastConfig(preparationRunTime_Click, prop, key_lastPreparationRunTime);
            setControlLastConfig(preparationRecordTime_Click, prop, key_lastPreparationRecordTime);
        }
        if (StringUtils.isBlank(outPath_Click.getText())) {
            setPathLabel(outPath_Click, defaultFileChooserPath, false);
        }
        input.close();
    }

    /**
     * 加载默认设置
     *
     * @throws IOException io异常
     */
    private void getConfig() throws IOException {
        // 读取配置文件
        getProperties();
        // 读取设置页面设置的值
        getSetting();
    }

    /**
     * 读取配置文件
     *
     * @throws IOException io异常
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
        floatingX = Integer.parseInt(prop.getProperty(key_floatingX, defaultFloatingX));
        floatingY = Integer.parseInt(prop.getProperty(key_floatingY, defaultFloatingY));
        detailWidth = Integer.parseInt(prop.getProperty(key_detailWidth, defaultDetailWidth));
        detailHeight = Integer.parseInt(prop.getProperty(key_detailHeight, defaultDetailHeight));
        floatingWidth = Integer.parseInt(prop.getProperty(key_floatingWidth, defaultFloatingWidth));
        floatingHeight = Integer.parseInt(prop.getProperty(key_floatingHeight, defaultFloatingHeight));
        input.close();
    }

    /**
     * 读取设置页面设置的值
     */
    private void getSetting() {
        Slider clickOpacity = (Slider) mainScene.lookup("#clickOpacity_Set");
        defaultClickOpacity = String.valueOf(clickOpacity.getValue());
        Slider stopOpacity = (Slider) mainScene.lookup("#stopOpacity_Set");
        defaultStopOpacity = String.valueOf(stopOpacity.getValue());
        TextField clickRetryNumTextField = (TextField) mainScene.lookup("#clickRetryNum_Set");
        clickRetryNum = StringUtils.isBlank(clickRetryNumTextField.getText()) ? defaultClickRetryNum : clickRetryNumTextField.getText();
        TextField stopRetryNumTextField = (TextField) mainScene.lookup("#stopRetryNum_Set");
        stopRetryNum = StringUtils.isBlank(stopRetryNumTextField.getText()) ? defaultStopRetryNum : stopRetryNumTextField.getText();
        TextField sampleIntervalTextField = (TextField) mainScene.lookup("#sampleInterval_Set");
        sampleInterval = StringUtils.isBlank(sampleIntervalTextField.getText()) ? defaultSampleInterval : sampleIntervalTextField.getText();
        TableView<?> tableView = (TableView<?>) mainScene.lookup("#tableView_Set");
        defaultStopImgFiles = tableView.getItems().stream().map(o -> (ImgFileBean) o).toList();
        CheckBox recordMoveCheckBox = (CheckBox) mainScene.lookup("#recordMove_Set");
        recordMove = recordMoveCheckBox.isSelected();
        CheckBox recordDragCheckBox = (CheckBox) mainScene.lookup("#recordDrag_Set");
        recordDrag = recordDragCheckBox.isSelected();
        CheckBox randomClickCheckBox = (CheckBox) mainScene.lookup("#randomClick_Set");
        randomClick = randomClickCheckBox.isSelected() ? activation : unActivation;
        CheckBox randomTrajectoryCheckBox = (CheckBox) mainScene.lookup("#randomTrajectory_Set");
        randomTrajectory = randomTrajectoryCheckBox.isSelected() ? activation : unActivation;
        CheckBox randomClickTimeCheckBox = (CheckBox) mainScene.lookup("#randomClickTime_Set");
        randomClickTime = randomClickTimeCheckBox.isSelected() ? activation : unActivation;
        TextField randomTimeTextField = (TextField) mainScene.lookup("#randomTimeOffset_Set");
        randomTime = StringUtils.isBlank(randomTimeTextField.getText()) ? defaultRandomTime : randomTimeTextField.getText();
        TextField clickTimeOffsetTextField = (TextField) mainScene.lookup("#clickTimeOffset_Set");
        clickTimeOffset = StringUtils.isBlank(clickTimeOffsetTextField.getText()) ? defaultClickTimeOffset : clickTimeOffsetTextField.getText();
        TextField randomClickXTextField = (TextField) mainScene.lookup("#randomClickX_Set");
        randomClickX = StringUtils.isBlank(randomClickXTextField.getText()) ? defaultRandomClickX : randomClickXTextField.getText();
        TextField randomClickYTextField = (TextField) mainScene.lookup("#randomClickY_Set");
        randomClickY = StringUtils.isBlank(randomClickYTextField.getText()) ? defaultRandomClickY : randomClickYTextField.getText();
        CheckBox randomWaitTimeCheckBox = (CheckBox) mainScene.lookup("#randomWaitTime_Set");
        randomWaitTime = randomWaitTimeCheckBox.isSelected() ? activation : unActivation;
        CheckBox randomClickIntervalCheckBox = (CheckBox) mainScene.lookup("#randomClickInterval_Set");
        randomClickInterval = randomClickIntervalCheckBox.isSelected() ? activation : unActivation;
    }

    /**
     * 设置javafx单元格宽度
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
     * 设置单元格可编辑
     */
    private void makeCellCanEdit() {
        tableView_Click.setEditable(true);
        name_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionVO::setName));
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
            // 更新列表数量
            updateTableViewSizeText(tableView_Click, dataNumber_Click, text_process);
        });
        Stage detailStage = new Stage();
        Scene scene = new Scene(root, detailWidth, detailHeight);
        detailStage.setScene(scene);
        String title = item.getName() == null ? "" : item.getName();
        detailStage.setTitle(title + " 步骤详情");
        detailStage.initModality(Modality.APPLICATION_MODAL);
        setWindLogo(detailStage, logoPath);
        // 监听窗口面板宽度变化
        detailStage.widthProperty().addListener((v1, v2, v3) -> Platform.runLater(controller::adaption));
        // 监听窗口面板高度变化
        detailStage.heightProperty().addListener((v1, v2, v3) -> Platform.runLater(controller::adaption));
        scene.getStylesheets().add(Objects.requireNonNull(MainApplication.class.getResource("css/Styles.css")).toExternalForm());
        detailStage.show();
    }

    /**
     * 初始化浮窗
     */
    private void initFloatingWindow() {
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
        // 读取配置文件
        getProperties();
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
            // 检查跳转逻辑参数与操作类型设置是否合理
            checkSetting(clickPositionVOS);
            runClicking = true;
            if (CollectionUtils.isNotEmpty(clickLogs)) {
                clickLogs.clear();
            }
            CheckBox firstClick = (CheckBox) mainScene.lookup("#firstClick_Set");
            TextField retrySecond = (TextField) tableView_Click.getScene().lookup("#retrySecond_Set");
            TextField overTime = (TextField) tableView_Click.getScene().lookup("#overtime_Set");
            TextField maxLogNum = (TextField) tableView_Click.getScene().lookup("#maxLogNum_Set");
            CheckBox clickLog = (CheckBox) mainScene.lookup("#clickLog_Set");
            CheckBox moveLog = (CheckBox) mainScene.lookup("#moveLog_Set");
            CheckBox dragLog = (CheckBox) mainScene.lookup("#dragLog_Set");
            CheckBox clickImgLog = (CheckBox) mainScene.lookup("#clickImgLog_Set");
            CheckBox stopImgLog = (CheckBox) mainScene.lookup("#stopImgLog_Set");
            CheckBox waitLog = (CheckBox) mainScene.lookup("#waitLog_Set");
            AutoClickTaskBean taskBean = new AutoClickTaskBean();
            taskBean.setRetrySecondValue(setDefaultIntValue(retrySecond, 1, 0, null))
                    .setLoopTime(setDefaultIntValue(loopTime_Click, 1, 0, null))
                    .setOverTimeValue(setDefaultIntValue(overTime, 0, 1, null))
                    .setMaxLogNum(setDefaultIntValue(maxLogNum, 0, 1, null))
                    .setClickImgLog(clickImgLog.isSelected())
                    .setStopImgLog(stopImgLog.isSelected())
                    .setFirstClick(firstClick.isSelected())
                    .setClickLog(clickLog.isSelected())
                    .setMoveLog(moveLog.isSelected())
                    .setDragLog(dragLog.isSelected())
                    .setWaitLog(waitLog.isSelected())
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
                clickLogs = autoClickTask.getValue();
                clearReferences();
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
                clickLogs = getNowLogs();
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
                clearReferences();
                throw new RuntimeException(ex);
            });
            autoClickTask.setOnCancelled(event -> {
                clickLogs = getNowLogs();
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
                clearReferences();
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
     * 检查跳转逻辑参数与操作类型设置是否合理
     */
    private static void checkSetting(List<ClickPositionVO> clickPositionVOS) {
        int maxIndex = clickPositionVOS.size();
        clickPositionVOS.forEach(clickPositionVO -> {
            int index = clickPositionVO.getIndex();
            String err = "序号为：" + index + " 名称为：" + clickPositionVO.getName() + " 的操作步骤设置有误\n";
            if (clickMatched_clickStep.equals(clickPositionVO.getMatchedType())) {
                int matchStep = Integer.parseInt(clickPositionVO.getMatchedStep());
                if (matchStep > maxIndex) {
                    throw new RuntimeException(err + text_matchedStepGreaterMax);
                }
                if (matchStep == index) {
                    throw new RuntimeException(err + text_matchedStepEqualIndex);
                }
            }
            if (retryType_Step.equals(clickPositionVO.getRetryStep())) {
                int retryStep = Integer.parseInt(clickPositionVO.getMatchedStep());
                if (retryStep > maxIndex) {
                    throw new RuntimeException(err + text_retryStepGreaterMax);
                }
                if (retryStep == index) {
                    throw new RuntimeException(err + text_retryStepEqualIndex);
                }
            }
        });
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
        // 测试点击选项
        MenuItem menuItem = buildClickTestMenuItem(tableView_Click, contextMenu);
        // 没有运行必要权限则无法点击
        if (isNativeHookException || noScreenCapturePermission) {
            menuItem.setDisable(true);
        }
        // 移动所选行选项
        buildMoveDataMenu(tableView_Click, contextMenu);
        // 修改点击按键
        buildEditClickKeyMenu(tableView_Click, contextMenu);
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
    private MenuItem buildClickTestMenuItem(TableView<ClickPositionVO> tableView, ContextMenu contextMenu) {
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
        return menuItem;
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
        // 读取设置页面设置的值
        getSetting();
        ClickPositionVO clickPositionVO = createClickPositionVO();
        clickPositionVO.setName(text_step + (tableViewItemSize + 1) + text_isAdd);
        if (tableViewItemSize == -1) {
            clickPositionVO.setName("测试步骤");
        }
        return clickPositionVO;
    }

    /**
     * 创建一个具有默认值的自动操作步骤类
     *
     * @return clickPositionVO 具有默认值的自动操作步骤类
     */
    private ClickPositionVO createClickPositionVO() {
        ClickPositionVO clickPositionVO = new ClickPositionVO();
        clickPositionVO.setTableView(tableView_Click)
                .setSampleInterval(Integer.parseInt(sampleInterval))
                .setRandomClickInterval(randomClickInterval)
                .setClickMatchThreshold(defaultClickOpacity)
                .setStopMatchThreshold(defaultStopOpacity)
                .setRandomTrajectory(randomTrajectory)
                .setStopImgFiles(defaultStopImgFiles)
                .setRandomClickTime(randomClickTime)
                .setMatchedType(clickMatched_click)
                .setRandomWaitTime(randomWaitTime)
                .setClickRetryTimes(clickRetryNum)
                .setClickKey(mouseButton_primary)
                .setStopRetryTimes(stopRetryNum)
                .setClickTime(clickTimeOffset)
                .setClickType(clickType_click)
                .setRetryType(retryType_stop)
                .setRandomClick(randomClick)
                .setRandomTime(randomTime)
                .setRandomX(randomClickX)
                .setRandomY(randomClickY)
                .setClickInterval("0")
                .setClickNum("1")
                .setWaitTime("0")
                .setStartX("0")
                .setStartY("0");
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
            clickPositionBeans = objectMapper.readValue(jsonFile,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ClickPositionBean.class));
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
            String clickType = clickPositionVO.getClickType();
            if (!isInIntegerRange(clickPositionVO.getStartX(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getStartY(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getClickTime(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getClickNum(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getClickInterval(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getWaitTime(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getClickRetryTimes(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getStopRetryTimes(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getClickMatchThreshold(), 0, 100)
                    || !isInIntegerRange(clickPositionVO.getStopMatchThreshold(), 0, 100)
                    || !isInIntegerRange(clickPositionVO.getRandomX(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getRandomY(), 0, null)
                    || !isInIntegerRange(clickPositionVO.getRandomClickTime(), 0, null)
                    || !clickMatchedList.contains(clickPositionVO.getMatchedType())
                    || !runClickTypeMap.containsKey(clickPositionVO.getClickKey())
                    || !retryTypeList.contains(clickPositionVO.getRetryType())
                    || !clickTypeList.contains(clickType)
                    || !activationList.contains(clickPositionVO.getRandomClick())
                    || !activationList.contains(clickPositionVO.getRandomTrajectory())
                    || !activationList.contains(clickPositionVO.getRandomClickInterval())
                    || !activationList.contains(clickPositionVO.getRandomWaitTime())
                    || !activationList.contains(clickPositionVO.getRandomClickTime())) {
                throw new IOException(text_LackKeyData);
            }
            clickPositionVO.setUuid(UUID.randomUUID().toString());
        }
        // 向列表添加数据
        addData(clickPositionVOS, append, tableView_Click, dataNumber_Click, text_process);
        updateLabel(log_Click, text_loadSuccess + inFilePath);
        log_Click.setTextFill(Color.GREEN);
    }

    /**
     * 根据鼠标位置调整ui
     *
     * @param mousePoint 鼠标位置
     */
    @Override
    public void onMousePositionUpdate(Point mousePoint) {
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
                            if (nativeMouseListener instanceof CustomMouseListener cmListener) {
                                // 停止轨迹记录
                                cmListener.stopRecording();
                            }
                            // 移除鼠标监听器
                            removeNativeListener(nativeMouseListener);
                            // 停止自动操作
                            if (autoClickTask != null && autoClickTask.isRunning()) {
                                autoClickTask.cancel();
                                autoClickTask = null;
                            }
                            // 停止录制计时
                            if (recordTimeline != null) {
                                recordTimeline.stop();
                                recordTimeline = null;
                                Platform.runLater(() -> {
                                    log_Click.setTextFill(Color.BLUE);
                                    log_Click.setText("录制已结束");
                                });
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
                            isRecordClicking = false;
                        }
                    }
                });
            }
        };
        addNativeListener(nativeKeyListener);
    }

    /**
     * 鼠标监听器
     */
    private class CustomMouseListener implements NativeMouseListener {

        /**
         * 记录点击时刻
         */
        private long pressTime;

        /**
         * 记录松开时刻
         */
        private long releasedTime;

        /**
         * 首次点击标记
         */
        private boolean isFirstClick = true;

        /**
         * 添加类型
         */
        private final int addType;

        /**
         * 鼠标移动记录器
         */
        private ClickPositionVO movePoint = createClickPositionVO();

        /**
         * 带点击的步骤
         */
        private ClickPositionVO clickBean;

        /**
         * 当前按下的按键
         */
        private final List<Integer> pressButtonList = new CopyOnWriteArrayList<>();

        /**
         * 构造器
         */
        private CustomMouseListener(int addType) {
            this.addType = addType;
        }

        /**
         * 停止鼠标轨迹记录
         */
        private void stopRecording() {
            // 停止拖拽轨迹记录
            removeNativeListener(dragMotionListener);
            // 停止移动轨迹记录
            if (isRecordClicking) {
                Point mousePoint = MousePositionListener.getMousePoint();
                int startX = (int) mousePoint.getX();
                int startY = (int) mousePoint.getY();
                int dataSize = tableView_Click.getItems().size() + 1;
                // 添加移动轨迹到表格
                addMoveTrajectory(dataSize, startX, startY);
            }
            removeNativeListener(moveMotionListener);
            pressButtonList.clear();
            Platform.runLater(() -> {
                log_Click.setTextFill(Color.BLUE);
                log_Click.setText("录制已结束");
                tableView_Click.refresh();
            });
        }

        /**
         * 鼠标拖拽监听器
         */
        private final NativeMouseMotionListener dragMotionListener = new NativeMouseMotionListener() {
            @Override
            public void nativeMouseDragged(NativeMouseEvent e) {
                if (recordClicking && recordDrag) {
                    Point mousePoint = MousePositionListener.getMousePoint();
                    int x = (int) mousePoint.getX();
                    int y = (int) mousePoint.getY();
                    List<Integer> pressButtons = new CopyOnWriteArrayList<>(pressButtonList);
                    clickBean.addMovePoint(x, y, pressButtons, true);
                    clickBean.setClickType(clickType_drag);
                }
            }
        };

        /**
         * 鼠标移动监听器
         */
        private final NativeMouseMotionListener moveMotionListener = new NativeMouseMotionListener() {
            @Override
            public void nativeMouseMoved(NativeMouseEvent e) {
                if (recordClicking && recordMove) {
                    Point mousePoint = MousePositionListener.getMousePoint();
                    int x = (int) mousePoint.getX();
                    int y = (int) mousePoint.getY();
                    movePoint.addMovePoint(x, y, null, false);
                }
            }
        };

        /**
         * 添加移动轨迹到表格
         *
         * @param index  数据编号
         * @param startX 起始横坐标
         * @param startY 起始纵坐标
         */
        private void addMoveTrajectory(int index, int startX, int startY) {
            // 所有按键都松开时才能记录
            if (recordMove && pressButtonList.isEmpty()) {
                Platform.runLater(() -> {
                    log_Click.setTextFill(Color.BLUE);
                    // 添加至表格
                    List<ClickPositionVO> clickPositionVOS = new ArrayList<>();
                    movePoint.setName(text_step + index + text_isRecord)
                            .setStartX(String.valueOf(startX))
                            .setStartY(String.valueOf(startY))
                            .setClickType(clickType_move);
                    clickPositionVOS.add(movePoint);
                    addData(clickPositionVOS, addType, tableView_Click, dataNumber_Click, text_process);
                    String log = text_cancelTask + text_recordClicking + "\n" +
                            text_recorded + "鼠标移动轨迹";
                    log_Click.setText(log);
                    floatingLabel.setText(log);
                });
            }
        }

        /**
         * 监听鼠标按下
         *
         * @param e 鼠标按下事件
         */
        @Override
        public void nativeMousePressed(NativeMouseEvent e) {
            if (isRecordClicking) {
                // 停止移动轨迹记录
                if (recordMove) {
                    removeNativeListener(moveMotionListener);
                }
                // 记录按下时刻的时间戳
                pressTime = System.currentTimeMillis();
                long waitTime;
                // 记录移动轨迹时因为点击和移动是分开的两个步骤，所以点击不用再等待一次移动的时间
                if (recordMove) {
                    waitTime = 0;
                } else {
                    waitTime = isFirstClick ?
                            pressTime - recordingStartTime :
                            pressTime - releasedTime;
                }
                int pressButton = e.getButton();
                // 记录按下的坐标
                Point mousePoint = MousePositionListener.getMousePoint();
                int startX = (int) mousePoint.getX();
                int startY = (int) mousePoint.getY();
                int dataSize = tableView_Click.getItems().size() + 1;
                // 添加移动轨迹到表格
                addMoveTrajectory(dataSize, startX, startY);
                // 创建点击位置对象
                if (pressButtonList.isEmpty()) {
                    clickBean = createClickPositionVO();
                    clickBean.setClickKey(recordClickTypeMap.get(pressButton))
                            .setName(text_step + dataSize + text_isRecord)
                            .setWaitTime(String.valueOf(waitTime))
                            .setStartX(String.valueOf(startX))
                            .setStartY(String.valueOf(startY));
                }
                // 记录按下的按键
                pressButtonList.add(pressButton);
                // 开始拖拽轨迹记录
                if (recordDrag) {
                    addNativeListener(dragMotionListener);
                }
            }
        }

        /**
         * 监听鼠标松开
         *
         * @param e 鼠标抬起事件
         */
        @Override
        public void nativeMouseReleased(NativeMouseEvent e) {
            if (isRecordClicking) {
                // 记录抬起的按键
                pressButtonList.remove(Integer.valueOf(e.getButton()));
                Point mousePoint = MousePositionListener.getMousePoint();
                int endX = (int) mousePoint.getX();
                int endY = (int) mousePoint.getY();
                // 所有按键都抬起后停止拖拽轨迹记录
                if (recordDrag && pressButtonList.isEmpty()) {
                    removeNativeListener(dragMotionListener);
                    // 拖拽结束是添加释放鼠标的坐标
                    clickBean.addMovePoint(endX, endY, null, true);
                }
                isFirstClick = false;
                // 记录移动轨迹
                if (recordMove) {
                    movePoint = createClickPositionVO();
                }
                // 所有按键都抬起后开始移动轨迹记录
                if (recordMove && pressButtonList.isEmpty()) {
                    addNativeListener(moveMotionListener);
                }
                // 只有在所有按键都抬起时才算一个完整的操作步骤
                if (pressButtonList.isEmpty()) {
                    releasedTime = System.currentTimeMillis();
                    // 计算点击持续时间（毫秒）
                    long duration = releasedTime - pressTime;
                    // 设置点击持续时间
                    clickBean.setClickTime(String.valueOf(duration));
                    Platform.runLater(() -> {
                        // 添加至表格
                        List<ClickPositionVO> clickPositionVOS = new ArrayList<>();
                        clickPositionVOS.add(clickBean);
                        addData(clickPositionVOS, addType, tableView_Click, dataNumber_Click, text_process);
                        String log = text_cancelTask + text_recordClicking + "\n" +
                                text_recorded + clickBean.getClickKey() +
                                "松开 X：" + endX + " Y：" + endY;
                        log_Click.setText(log);
                        floatingLabel.setText(log);
                    });
                }
            }
        }
    }

    /**
     * 开启全局鼠标监听
     *
     * @param addType 添加类型
     */
    private void startNativeMouseListener(int addType) {
        removeNativeListener(nativeMouseListener);
        // 读取设置页面设置的值
        getSetting();
        CustomMouseListener listener = new CustomMouseListener(addType);
        nativeMouseListener = listener;
        // 注册监听器
        addNativeListener(listener);
        addNativeListener(listener.moveMotionListener);
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
            recordTimeline = new Timeline();
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
                AtomicInteger preparationTime = new AtomicInteger(preparationTimeValue);
                // 创建 Timeline 来实现倒计时
                Timeline finalTimeline = recordTimeline;
                recordTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                    preparationTime.getAndDecrement();
                    if (preparationTime.get() > 0) {
                        text.set(text_cancelTask + preparationTime + text_preparation);
                    } else {
                        isRecordClicking = true;
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
        isNativeHookException = true;
        runClick_Click.setDisable(true);
        clickTest_Click.setDisable(true);
        recordClick_Click.setDisable(true);
        Button saveButton = (Button) mainScene.lookup("#setFloatingCoordinate_Set");
        saveButton.setDisable(true);
        String errorMessage = appName + " 缺少必要系统权限";
        if (systemName.contains(mac)) {
            errorMessage = tip_NativeHookException;
        }
        err_Click.setText(errorMessage);
        err_Click.setTooltip(creatTooltip(tip_NativeHookException));
    }

    /**
     * 禁用需要截屏相关权限的组件
     */
    private void setNoScreenCapturePermissionLog() {
        noScreenCapturePermission = true;
        runClick_Click.setDisable(true);
        clickTest_Click.setDisable(true);
        err_Click.setText(tip_noScreenCapturePermission);
        err_Click.setTooltip(creatTooltip(tip_noScreenCapturePermission));
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
                // 加载默认设置
                getConfig();
                // 设置鼠标悬停提示
                setToolTip();
                // 给输入框添加内容变化监听
                textFieldChangeListener();
                // 设置初始配置值为上次配置值
                setLastConfig();
                // 检查macOS屏幕录制权限
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
            // 设置javafx单元格宽度
            bindPrefWidthProperty();
            // 初始化浮窗
            initFloatingWindow();
            // 获取鼠标坐标监听器
            MousePositionListener.getInstance().addListener(this);
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
            // 读取配置文件
            getProperties();
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(appName, allPMC);
            List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(filter));
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
            String outFilePath = outPath_Click.getText();
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
        // 读取配置文件
        getProperties();
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        String outFilePath = outPath_Click.getText();
        File selectedFile = creatDirectoryChooser(window, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Click, configFile_Click);
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

    /**
     * 查看运行记录
     */
    @FXML
    private void clickLog() throws IOException {
        URL fxmlLocation = getClass().getResource(resourcePath + "fxml/ClickLog-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
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
        detailStage.setTitle("运行记录");
        detailStage.initModality(Modality.APPLICATION_MODAL);
        setWindLogo(detailStage, logoPath);
        // 监听窗口面板宽度变化
        detailStage.widthProperty().addListener((v1, v2, v3) -> Platform.runLater(controller::adaption));
        // 监听窗口面板高度变化
        detailStage.heightProperty().addListener((v1, v2, v3) -> Platform.runLater(controller::adaption));
        scene.getStylesheets().add(Objects.requireNonNull(MainApplication.class.getResource("css/Styles.css")).toExternalForm());
        detailStage.show();
    }

}
