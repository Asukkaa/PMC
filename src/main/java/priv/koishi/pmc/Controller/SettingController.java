package priv.koishi.pmc.Controller;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import priv.koishi.pmc.Bean.VO.ImgFileVO;
import priv.koishi.pmc.Listener.MousePositionListener;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static priv.koishi.pmc.Controller.AutoClickController.stopImgSelectPath;
import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Utils.CommonUtils.removeNativeListener;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 设置页面控制器
 *
 * @author KOISHI
 * Date:2024-11-12
 * Time:下午4:51
 */
public class SettingController {

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
     * 自动保存文件名
     */
    private static String autoSaveFileName;

    /**
     * 页面标识符
     */
    private final String tabId = "_Set";

    /**
     * 全局键盘监听器
     */
    private NativeKeyListener nativeKeyListener;

    /**
     * 要防重复点击的组件
     */
    private static final List<Node> disableNodes = new ArrayList<>();

    /**
     * 浮窗所在矩形
     */
    private Rectangle rectangle;

    /**
     * 浮窗Stage
     */
    private Stage floatingStage;

    /**
     * 顶部浮窗信息输出栏
     */
    private Label floatingLabel;

    /**
     * 程序主场景
     */
    private Scene mainScene;

    @FXML
    private AnchorPane anchorPane_Set;

    @FXML
    private HBox fileNumberHBox_Set, findImgSetting_Set;

    @FXML
    private ColorPicker colorPicker_Set;

    @FXML
    private Label dataNumber_Set, tip_Set;

    @FXML
    private Slider opacity_Set, clickOpacity_Set, stopOpacity_Set;

    @FXML
    private Button setFloatingCoordinate_Set, stopImgBtn_Set, removeAll_Set, reLaunch_Set;

    @FXML
    private TextField floatingDistance_Set, offsetX_Set, offsetY_Set, clickRetryNum_Set, stopRetryNum_Set,
            retrySecond_Set, overtime_Set;

    @FXML
    private CheckBox lastTab_Set, fullWindow_Set, loadAutoClick_Set, hideWindowRun_Set, showWindowRun_Set,
            hideWindowRecord_Set, showWindowRecord_Set, firstClick_Set, floatingRun_Set, floatingRecord_Set,
            mouseFloatingRun_Set, mouseFloatingRecord_Set, mouseFloating_Set, maxWindow_Set, remindSave_Set,
            autoSave_Set;

    @FXML
    private TableView<ImgFileVO> tableView_Set;

    @FXML
    private TableColumn<ImgFileVO, Integer> index_Set;

    @FXML
    private TableColumn<ImgFileVO, ImageView> thumb_Set;

    @FXML
    private TableColumn<ImgFileVO, String> name_Set, type_Set, path_Set;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void settingAdaption(Stage stage) {
        Scene scene = stage.getScene();
        // 设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Set");
        table.setPrefHeight(stageHeight * 0.3);
        // 设置组件宽度
        double tableWidth = stage.getWidth() * 0.9;
        table.setMaxWidth(tableWidth);
        Node index = scene.lookup("#index_Set");
        index.setStyle("-fx-pref-width: " + tableWidth * 0.05 + "px;");
        Node thumb = scene.lookup("#thumb_Set");
        thumb.setStyle("-fx-pref-width: " + tableWidth * 0.2 + "px;");
        Node name = scene.lookup("#name_Set");
        name.setStyle("-fx-pref-width: " + tableWidth * 0.25 + "px;");
        Node path = scene.lookup("#path_Set");
        path.setStyle("-fx-pref-width: " + tableWidth * 0.4 + "px;");
        Node type = scene.lookup("#type_Set");
        type.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Label dataNum = (Label) scene.lookup("#dataNumber_Set");
        HBox fileNumberHBox = (HBox) scene.lookup("#fileNumberHBox_Set");
        nodeRightAlignment(fileNumberHBox, tableWidth, dataNum);
        Label tip = (Label) scene.lookup("#tip_Set");
        HBox findImgSetting = (HBox) scene.lookup("#findImgSetting_Set");
        nodeRightAlignment(findImgSetting, tableWidth, tip);
    }

    /**
     * 保存设置功能最后设置
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    public static void settingSaveLastConfig(Scene scene) throws IOException {
        AnchorPane anchorPane = (AnchorPane) scene.lookup("#anchorPane_Set");
        if (anchorPane != null) {
            InputStream input = checkRunningInputStream(configFile_Click);
            Properties prop = new Properties();
            prop.load(input);
            TextField floatingDistance = (TextField) scene.lookup("#floatingDistance_Set");
            prop.put(key_margin, floatingDistance.getText());
            Slider opacity = (Slider) scene.lookup("#opacity_Set");
            prop.put(key_opacity, String.valueOf(opacity.getValue()));
            Slider clickOpacity = (Slider) scene.lookup("#clickOpacity_Set");
            prop.put(key_clickOpacity, String.valueOf(clickOpacity.getValue()));
            Slider stopOpacity = (Slider) scene.lookup("#stopOpacity_Set");
            prop.put(key_stopOpacity, String.valueOf(stopOpacity.getValue()));
            TextField clickRetryNum = (TextField) scene.lookup("#clickRetryNum_Set");
            prop.put(key_defaultClickRetryNum, clickRetryNum.getText());
            TextField stopRetryNum = (TextField) scene.lookup("#stopRetryNum_Set");
            prop.put(key_defaultStopRetryNum, stopRetryNum.getText());
            TextField retrySecond = (TextField) scene.lookup("#retrySecond_Set");
            prop.put(key_retrySecond, retrySecond.getText());
            TextField overtime = (TextField) scene.lookup("#overtime_Set");
            prop.put(key_overtime, overtime.getText());
            TableView<?> tableView = (TableView<?>) scene.lookup("#tableView_Set");
            List<ImgFileVO> list = tableView.getItems().stream().map(o -> (ImgFileVO) o).toList();
            int index = 0;
            while (index < 10) {
                prop.remove(key_defaultStopImg + index);
                index++;
            }
            for (int i = 0; i < list.size(); i++) {
                ImgFileVO bean = list.get(i);
                prop.put(key_defaultStopImg + i, bean.getPath());
            }
            OutputStream output = checkRunningOutputStream(configFile_Click);
            prop.store(output, null);
            input.close();
            output.close();
        }
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
        // 描边设置
        rectangle.setStroke(new Color(0, 0, 0, 1.0));
        // 设置透明度
        rectangle.setFill(new Color(0, 0, 0, 0.5));
        StackPane root = new StackPane();
        root.setBackground(Background.fill(Color.TRANSPARENT));
        int margin = setDefaultIntValue(floatingDistance_Set, 0, 0, null);
        // 添加拖拽事件处理器
        final double[] xOffset = new double[1];
        final double[] yOffset = new double[1];
        root.setOnMousePressed(event -> {
            // 记录鼠标按下时的初始偏移量
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            // 获取当前所在屏幕
            Screen currentScreen = getCurrentScreen(floatingStage);
            Rectangle2D screenBounds = currentScreen.getBounds();
            // 计算新坐标
            double newX = event.getScreenX() - xOffset[0];
            double newY = event.getScreenY() - yOffset[0];
            // 边界约束
            newX = Math.max(screenBounds.getMinX() + margin, Math.min(newX, screenBounds.getMaxX() - floatingWidth - margin));
            newY = Math.max(screenBounds.getMinY() + margin, Math.min(newY, screenBounds.getMaxY() - floatingHeight - margin));
            int x = (int) newX;
            int y = (int) newY;
            // 应用限制后的坐标
            floatingStage.setX(x);
            floatingStage.setY(y);
        });
        Color labelTextFill = Color.WHITE;
        floatingLabel = new Label(text_saveFloatingCoordinate);
        floatingLabel.setTextFill(labelTextFill);
        floatingLabel.setStyle("-fx-font-size: 18px;");
        root.getChildren().addAll(rectangle, floatingLabel);
        Scene scene = new Scene(root, Color.TRANSPARENT);
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
     */
    private void showFloatingWindow() {
        Platform.runLater(() -> {
            floatingLabel.setTextFill(colorPicker_Set.getValue());
            floatingStage.setX(floatingX);
            floatingStage.setY(floatingY);
            double opacity = opacity_Set.getValue();
            if (opacity == 0) {
                rectangle.setFill(new Color(0, 0, 0, 0.01));
            } else {
                rectangle.setFill(new Color(0, 0, 0, opacity));
            }
            // 改变要防重复点击的组件状态
            changeDisableNodes(disableNodes, true);
            if (mouseFloating_Set.isSelected()) {
                floatingLabel.setText(text_escCloseFloating);
                setFloatingCoordinate_Set.setText(text_closeFloating);
                addToolTip(tip_closeFloating, setFloatingCoordinate_Set);
            } else {
                floatingLabel.setText(text_saveFloatingCoordinate);
                setFloatingCoordinate_Set.setText(text_saveCloseFloating);
                addToolTip(tip_saveFloating, setFloatingCoordinate_Set);
            }
            floatingStage.show();
            // 监听键盘事件
            startNativeKeyListener();
        });
    }

    /**
     * 隐藏浮窗
     */
    private void hideFloatingWindow() {
        Platform.runLater(() -> {
            if (!mouseFloating_Set.isSelected()) {
                try {
                    saveFloatingCoordinate();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            floatingStage.hide();
            setFloatingCoordinate_Set.setText(text_showFloating);
            addToolTip(tip_setFloatingCoordinate, setFloatingCoordinate_Set);
            // 改变要防重复点击的组件状态
            changeDisableNodes(disableNodes, false);
            removeNativeListener(nativeKeyListener);
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
                    if (floatingStage != null && floatingStage.isShowing()) {
                        // 检测快捷键 esc
                        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
                            hideFloatingWindow();
                        }
                    }
                });
            }
        };
        GlobalScreen.addNativeKeyListener(nativeKeyListener);
    }

    /**
     * 保存浮窗位置
     */
    private void saveFloatingCoordinate() throws IOException {
        floatingX = (int) floatingStage.getX();
        floatingY = (int) floatingStage.getY();
        updateProperties(configFile_Click, key_floatingX, String.valueOf(floatingX));
        updateProperties(configFile_Click, key_floatingY, String.valueOf(floatingY));
    }

    /**
     * 读取配置文件
     *
     * @throws IOException io异常
     */
    private void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream configFileInput = checkRunningInputStream(configFile);
        prop.load(configFileInput);
        setControlLastConfig(lastTab_Set, prop, key_loadLastConfig);
        setControlLastConfig(maxWindow_Set, prop, key_loadLastMaxWindow);
        setControlLastConfig(fullWindow_Set, prop, key_loadLastFullWindow);
        configFileInput.close();
        InputStream clickFileInput = checkRunningInputStream(configFile_Click);
        prop.load(clickFileInput);
        autoSaveFileName = prop.getProperty(key_autoSaveFileName);
        floatingX = Integer.parseInt(prop.getProperty(key_floatingX));
        floatingY = Integer.parseInt(prop.getProperty(key_floatingY));
        floatingWidth = Integer.parseInt(prop.getProperty(key_floatingWidth));
        floatingHeight = Integer.parseInt(prop.getProperty(key_floatingHeight));
        setControlLastConfig(offsetX_Set, prop, key_offsetX);
        setControlLastConfig(offsetY_Set, prop, key_offsetY);
        setControlLastConfig(opacity_Set, prop, key_opacity);
        setControlLastConfig(overtime_Set, prop, key_overtime);
        setControlLastConfig(autoSave_Set, prop, key_autoSave);
        setControlLastConfig(remindSave_Set, prop, key_remindSave);
        setControlLastConfig(retrySecond_Set, prop, key_retrySecond);
        setControlLastConfig(stopOpacity_Set, prop, key_stopOpacity);
        setControlLastConfig(floatingDistance_Set, prop, key_margin);
        setControlLastConfig(firstClick_Set, prop, key_lastFirstClick);
        setControlLastConfig(clickOpacity_Set, prop, key_clickOpacity);
        setControlLastConfig(floatingRun_Set, prop, key_loadFloatingRun);
        setControlLastConfig(mouseFloating_Set, prop, key_mouseFloating);
        setControlLastConfig(loadAutoClick_Set, prop, key_loadLastConfig);
        setControlLastConfig(hideWindowRun_Set, prop, key_lastHideWindowRun);
        setControlLastConfig(showWindowRun_Set, prop, key_lastShowWindowRun);
        setControlLastConfig(stopRetryNum_Set, prop, key_defaultStopRetryNum);
        setControlLastConfig(floatingRecord_Set, prop, key_loadFloatingRecord);
        setControlLastConfig(mouseFloatingRun_Set, prop, key_mouseFloatingRun);
        setControlLastConfig(clickRetryNum_Set, prop, key_defaultClickRetryNum);
        setControlLastConfig(hideWindowRecord_Set, prop, key_lastHideWindowRecord);
        setControlLastConfig(showWindowRecord_Set, prop, key_lastShowWindowRecord);
        setControlLastConfig(mouseFloatingRecord_Set, prop, key_mouseFloatingRecord);
        setControlLastConfig(tableView_Set, prop, key_defaultStopImg, dataNumber_Set);
        setColorPickerConfig(colorPicker_Set, prop, key_lastFloatingTextColor, key_lastColorCustom);
        clickFileInput.close();
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_offsetX, offsetX_Set);
        addToolTip(tip_offsetY, offsetY_Set);
        addToolTip(tip_reLaunch, reLaunch_Set);
        addToolTip(tip_remindSave, remindSave_Set);
        addToolTip(tip_firstClick, firstClick_Set);
        addToolTip(tip_floatingRun, floatingRun_Set);
        addToolTip(tip_margin, floatingDistance_Set);
        addToolTip(lastTab_Set.getText(), lastTab_Set);
        addToolTip(tip_removeStopImgBtn, removeAll_Set);
        addToolTip(tip_mouseFloating, mouseFloating_Set);
        addToolTip(tip_hideWindowRun, hideWindowRun_Set);
        addToolTip(tip_showWindowRun, showWindowRun_Set);
        addToolTip(tip_defaultStopImgBtn, stopImgBtn_Set);
        addToolTip(tip_floatingRecord, floatingRecord_Set);
        addToolTip(maxWindow_Set.getText(), maxWindow_Set);
        addToolTip(fullWindow_Set.getText(), fullWindow_Set);
        addToolTip(tip_hideWindowRecord, hideWindowRecord_Set);
        addToolTip(tip_showWindowRecord, showWindowRecord_Set);
        addToolTip(tip_mouseFloatingRun, mouseFloatingRun_Set);
        addToolTip(tip_lastAutoClickSetting, loadAutoClick_Set);
        addToolTip(tip_autoSave + autoSaveFileName, autoSave_Set);
        addToolTip(tip_mouseFloatingRecord, mouseFloatingRecord_Set);
        addToolTip(tip_setFloatingCoordinate, setFloatingCoordinate_Set);
        addValueToolTip(overtime_Set, tip_overtime, overtime_Set.getText());
        addToolTip(tip_stopRetryNum + defaultStopRetryNum, stopRetryNum_Set);
        addToolTip(tip_clickRetryNum + defaultClickRetryNum, clickRetryNum_Set);
        addValueToolTip(retrySecond_Set, tip_retrySecond, retrySecond_Set.getText());
        addValueToolTip(opacity_Set, tip_opacity, text_nowValue, String.valueOf(opacity_Set.getValue()));
        addValueToolTip(colorPicker_Set, tip_colorPicker, text_nowValue, String.valueOf(colorPicker_Set.getValue()));
        addValueToolTip(stopOpacity_Set, tip_stopOpacity, text_nowValue, String.valueOf((int) stopOpacity_Set.getValue()));
        addValueToolTip(clickOpacity_Set, tip_clickOpacity, text_nowValue, String.valueOf((int) clickOpacity_Set.getValue()));
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
        colorPicker_Set.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (floatingLabel != null) {
                floatingLabel.setTextFill(newValue);
            }
            addValueToolTip(colorPicker_Set, tip_colorPicker, text_nowValue, String.valueOf(newValue));
        });
    }

    /**
     * 监听数值滑动条内容变化
     */
    private void sliderValueListener() {
        opacity_Set.valueProperty().addListener((observable, oldValue, newValue) -> {
            double rounded = Math.round(newValue.doubleValue() * 10) / 10.0;
            if (newValue.doubleValue() != rounded) {
                opacity_Set.setValue(rounded);
            }
            if (rectangle != null) {
                if (rounded == 0) {
                    rectangle.setFill(new Color(0, 0, 0, 0.01));
                } else {
                    rectangle.setFill(new Color(0, 0, 0, rounded));
                }
            }
            addValueToolTip(opacity_Set, tip_opacity, text_nowValue, String.valueOf(rounded));
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
        integerSliderValueListener(stopOpacity_Set, tip_stopOpacity);
        // 要点击的图像识别准确度设置监听
        integerSliderValueListener(clickOpacity_Set, tip_clickOpacity);
        // 每张图片最大匹配时间输入框监听
        integerRangeTextField(overtime_Set, 1, null, tip_overtime);
        // 浮窗跟随鼠标时横轴偏移量输入框监听
        integerRangeTextField(offsetX_Set, null, null, tip_offsetX);
        // 浮窗跟随鼠标时纵轴偏移量输入框监听
        integerRangeTextField(offsetY_Set, null, null, tip_offsetY);
        // 匹配失败重试间隔时间输入框监听
        integerRangeTextField(retrySecond_Set, 0, null, tip_retrySecond);
        // 浮窗离屏幕边界距离输入框监听
        integerRangeTextField(floatingDistance_Set, 0, null, tip_margin);
        // 限制终止操作识别失败重试次数文本输入框内容
        integerRangeTextField(stopRetryNum_Set, 0, null, tip_stopRetryNum + defaultStopRetryNum);
        // 限制要点击的图片识别失败重试次数文本输入框内容
        integerRangeTextField(clickRetryNum_Set, 0, null, tip_clickRetryNum + defaultClickRetryNum);
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        Node autoClickTab = mainScene.lookup("#autoClickTab");
        disableNodes.add(autoClickTab);
        Node settingTab = mainScene.lookup("#settingTab");
        disableNodes.add(settingTab);
        Node aboutTab = mainScene.lookup("#aboutTab");
        disableNodes.add(aboutTab);
    }

    /**
     * 根据鼠标位置调整ui
     */
    private void onMousePositionUpdate() {
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        Platform.runLater(() -> {
            if (mouseFloating_Set.isSelected() && floatingStage != null && floatingStage.isShowing()) {
                int offsetX = setDefaultIntValue(offsetX_Set, defaultOffsetX, null, null);
                int offsetY = setDefaultIntValue(offsetY_Set, defaultOffsetY, null, null);
                floatingMove(floatingStage, mousePoint, offsetX, offsetY);
            }
        });
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_Set.prefWidthProperty().bind(tableView_Set.widthProperty().multiply(0.05));
        thumb_Set.prefWidthProperty().bind(tableView_Set.widthProperty().multiply(0.2));
        name_Set.prefWidthProperty().bind(tableView_Set.widthProperty().multiply(0.25));
        path_Set.prefWidthProperty().bind(tableView_Set.widthProperty().multiply(0.4));
        type_Set.prefWidthProperty().bind(tableView_Set.widthProperty().multiply(0.1));
    }

    /**
     * 界面初始化
     *
     * @throws IOException io异常
     */
    @FXML
    private void initialize() throws IOException {
        // 读取配置文件
        getConfig();
        // 设置javafx单元格宽度
        bindPrefWidthProperty();
        // 设置鼠标悬停提示
        setToolTip();
        // 给组件添加内容变化监听
        nodeValueChangeListener();
        // 初始化浮窗
        initFloatingWindow();
        // 监听并保存颜色选择器自定义颜色
        setCustomColorsListener();
        Platform.runLater(() -> {
            mainScene = anchorPane_Set.getScene();
            // 获取鼠标坐标监听器
            new MousePositionListener(this::onMousePositionUpdate);
            // 设置要防重复点击的组件
            setDisableNodes();
            // 自动填充javafx表格
            autoBuildTableViewData(tableView_Set, ImgFileVO.class, tabId, index_Set);
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Set);
            // 构建右键菜单
            buildContextMenu(tableView_Set, dataNumber_Set);
        });
    }

    /**
     * 自动操作工具功能加载上次设置信息
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadAutoClickAction() throws IOException {
        setLoadLastConfigCheckBox(loadAutoClick_Set, configFile_Click, key_loadLastConfig);
    }

    /**
     * 操作步骤详情页修改后未保存提示
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadRemindSaveAction() throws IOException {
        setLoadLastConfigCheckBox(remindSave_Set, configFile_Click, key_remindSave);
    }

    /**
     * 关闭程序时自动保存列表操作步骤
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadAutoSaveAction() throws IOException {
        setLoadLastConfigCheckBox(autoSave_Set, configFile_Click, key_autoSave);
    }

    /**
     * 记住关闭前打开的页面设置
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadLastTabAction() throws IOException {
        setLoadLastConfigCheckBox(lastTab_Set, configFile, key_loadLastConfig);
    }

    /**
     * 记住窗口是否全屏设置
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFullWindowAction() throws IOException {
        setLoadLastConfigCheckBox(fullWindow_Set, configFile, key_loadLastFullWindow);
    }

    /**
     * 记住窗口是否最大化设置
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadMaxWindowAction() throws IOException {
        setLoadLastConfigCheckBox(maxWindow_Set, configFile, key_loadLastMaxWindow);
    }

    /**
     * 执行自动流程前最小化本程序
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadHideWindowRunAction() throws IOException {
        setLoadLastConfigCheckBox(hideWindowRun_Set, configFile_Click, key_lastHideWindowRun);
    }

    /**
     * 执行自动流程结束后弹出本程序
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadShowWindowRunAction() throws IOException {
        setLoadLastConfigCheckBox(showWindowRun_Set, configFile_Click, key_lastShowWindowRun);
    }

    /**
     * 录制自动流程前最小化本程序
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadHideWindowRecordAction() throws IOException {
        setLoadLastConfigCheckBox(hideWindowRecord_Set, configFile_Click, key_lastHideWindowRecord);
    }

    /**
     * 录制自动流程结束后弹出本程序
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadShowWindowRecordAction() throws IOException {
        setLoadLastConfigCheckBox(showWindowRecord_Set, configFile_Click, key_lastShowWindowRecord);
    }

    /**
     * 执行自动流程前点击第一个起始坐标
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFirstClickAction() throws IOException {
        setLoadLastConfigCheckBox(firstClick_Set, configFile_Click, key_lastFirstClick);
    }

    /**
     * 执行自动流程时显示信息浮窗
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFloatingRunAction() throws IOException {
        setLoadLastConfigCheckBox(floatingRun_Set, configFile_Click, key_loadFloatingRun);
    }

    /**
     * 录制自动流程时显示信息浮窗
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFloatingRecordAction() throws IOException {
        setLoadLastConfigCheckBox(floatingRecord_Set, configFile_Click, key_loadFloatingRecord);
    }

    /**
     * 监听并保存浮窗字体颜色
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadColorAction() throws IOException {
        updateProperties(configFile_Click, key_lastFloatingTextColor, String.valueOf(colorPicker_Set.getValue()));
    }

    /**
     * 运行自动流程时信息浮窗跟随鼠标
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadMouseFloatingRunAction() throws IOException {
        setLoadLastConfigCheckBox(mouseFloatingRun_Set, configFile_Click, key_mouseFloatingRun);
    }

    /**
     * 录制自动流程时信息浮窗跟随鼠标
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadMouseFloatingRecordAction() throws IOException {
        setLoadLastConfigCheckBox(mouseFloatingRecord_Set, configFile_Click, key_mouseFloatingRecord);
    }

    /**
     * 显示浮窗位置时信息浮窗跟随鼠标
     *
     * @throws IOException io异常
     */
    @FXML
    private void mouseFloatingAction() throws IOException {
        setLoadLastConfigCheckBox(mouseFloating_Set, configFile_Click, key_mouseFloating);
        if (floatingStage != null && floatingStage.isShowing() && mouseFloating_Set.isSelected()) {
            floatingLabel.setText(text_escCloseFloating);
            setFloatingCoordinate_Set.setText(text_closeFloating);
            addToolTip(tip_closeFloating, setFloatingCoordinate_Set);
        }
        if (floatingStage != null && floatingStage.isShowing() && !mouseFloating_Set.isSelected()) {
            floatingLabel.setText(text_saveFloatingCoordinate);
            setFloatingCoordinate_Set.setText(text_saveCloseFloating);
            addToolTip(tip_saveFloating, setFloatingCoordinate_Set);
        }
    }

    /**
     * 重启程序按钮
     *
     * @throws IOException io异常
     */
    @FXML
    private void reLaunch() throws IOException {
        Platform.exit();
        if (!isRunningFromJar()) {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (systemName.contains(win)) {
                processBuilder.command(getAppPath());
            } else if (systemName.contains(macos)) {
                processBuilder.command("open", "-n", getAppPath());
            }
            processBuilder.start();
        }
    }

    /**
     * 显示或隐藏浮窗位置
     */
    @FXML
    private void setFloatingCoordinate() {
        // 显示浮窗
        if (floatingStage != null && !floatingStage.isShowing()) {
            showFloatingWindow();
        }
        // 隐藏浮窗
        if (floatingStage != null && floatingStage.isShowing()) {
            hideFloatingWindow();
        }
    }

    /**
     * 选择终止操作的图片
     *
     * @param actionEvent 点击事件
     */
    @FXML
    private void addStopImgPath(ActionEvent actionEvent) throws IOException {
        stopImgSelectPath = addStopImgPaths(actionEvent, tableView_Set, dataNumber_Set, stopImgSelectPath);
    }

    /**
     * 清空图片列表
     */
    @FXML
    private void removeAll() {
        removeTableViewData(tableView_Set, dataNumber_Set, null);
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    public void handleDrop(DragEvent dragEvent) {
        handleDropImg(dragEvent, tableView_Set);
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

}
