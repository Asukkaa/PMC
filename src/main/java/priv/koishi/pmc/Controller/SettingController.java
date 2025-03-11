package priv.koishi.pmc.Controller;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Utils.FileUtils.*;
import static priv.koishi.pmc.Utils.UiUtils.*;
import static priv.koishi.pmc.Utils.UiUtils.setControlLastConfig;

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
    int floatingX;

    /**
     * 浮窗Y坐标
     */
    int floatingY;

    /**
     * 浮窗距离屏幕的边距
     */
    int margin;

    /**
     * 浮窗宽度
     */
    int floatingWidth;

    /**
     * 浮窗高度
     */
    int floatingHeight;

    /**
     * 浮窗Stage
     */
    private Stage floatingStage;

    /**
     * 顶部浮窗信息输出栏
     */
    private Label floatingLabel;

    /**
     * 全局键盘监听器
     */
    private NativeKeyListener nativeKeyListener;

    @FXML
    private AnchorPane anchorPane_Set;

    @FXML
    private VBox vBox_Set;

    @FXML
    private ColorPicker colorPicker_Set;

    @FXML
    private TextField floatingDistance_Set;

    @FXML
    private Button setFloatingCoordinate_Set;

    @FXML
    private CheckBox lastTab_Set, fullWindow_Set, loadAutoClick_Set, hideWindowRun_Set, showWindowRun_Set,
            hideWindowRecord_Set, showWindowRecord_Set, firstClick_Set, floatingRun_Set, floatingRecord_Set;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void settingAdaption(Stage stage) {
        Scene scene = stage.getScene();
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        Node settingVBox = scene.lookup("#vBox_Set");
        settingVBox.setLayoutX(stageWidth * 0.03);
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
        Rectangle rectangle = new Rectangle(floatingWidth, floatingHeight);
        // 设置透明度
        rectangle.setOpacity(0.5);
        StackPane root = new StackPane();
        root.setBackground(Background.fill(Color.TRANSPARENT));
        root.setStyle("-fx-cursor: hand");
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
            Screen currentScreen = getCurrentScreen();
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
     * 获取当前所在屏幕
     *
     * @return 当前所在屏幕
     */
    private Screen getCurrentScreen() {
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getBounds();
            if (bounds.contains(floatingStage.getX(), floatingStage.getY())) {
                return screen;
            }
        }
        // 默认返回主屏幕
        return Screen.getPrimary();
    }

    /**
     * 显示浮窗
     */
    private void showFloatingWindow() {
        Scene scene = anchorPane_Set.getScene();
        // 获取浮窗的文本颜色设置
        ColorPicker colorPicker = (ColorPicker) scene.lookup("#colorPicker_Set");
        floatingLabel.setTextFill(colorPicker.getValue());
        floatingStage.setX(floatingX);
        floatingStage.setY(floatingY);
        Platform.runLater(() -> {
            if (floatingStage != null && !floatingStage.isShowing()) {
                floatingStage.show();
            }
        });
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
     * 开启全局键盘监听
     */
    private void startNativeKeyListener() {
        stopNativeKeyListener();
        // 键盘监听器
        nativeKeyListener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                Platform.runLater(() -> {
                    // 检测快捷键 esc
                    if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
                        System.out.println("esc");
                        hideFloatingWindow();
                        try {
                            saveFloatingCoordinate();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
            }
        };
        GlobalScreen.addNativeKeyListener(nativeKeyListener);
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
        setControlLastConfig(fullWindow_Set, prop, key_loadLastFullWindow);
        setColorPickerConfig(colorPicker_Set, prop, key_lastFloatingTextColor, key_lastColorCustom);
        configFileInput.close();
        InputStream clickFileInput = checkRunningInputStream(configFile_Click);
        prop.load(clickFileInput);
        String marginStr = prop.getProperty(key_margin);
        if (StringUtils.isNotBlank(marginStr)) {
            margin = Integer.parseInt(marginStr);
        }
        floatingX = Integer.parseInt(prop.getProperty(key_floatingX));
        floatingY = Integer.parseInt(prop.getProperty(key_floatingY));
        floatingWidth = Integer.parseInt(prop.getProperty(key_floatingWidth));
        floatingHeight = Integer.parseInt(prop.getProperty(key_floatingHeight));
        setControlLastConfig(floatingDistance_Set, prop, key_margin);
        setControlLastConfig(firstClick_Set, prop, key_lastFirstClick);
        setControlLastConfig(floatingRun_Set, prop, key_loadFloatingRun);
        setControlLastConfig(loadAutoClick_Set, prop, key_loadLastConfig);
        setControlLastConfig(hideWindowRun_Set, prop, key_lastHideWindowRun);
        setControlLastConfig(showWindowRun_Set, prop, key_lastShowWindowRun);
        setControlLastConfig(floatingRecord_Set, prop, key_loadFloatingRecord);
        setControlLastConfig(hideWindowRecord_Set, prop, key_lastHideWindowRecord);
        setControlLastConfig(showWindowRecord_Set, prop, key_lastShowWindowRecord);
        clickFileInput.close();
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(lastTab_Set.getText(), lastTab_Set);
        addToolTip(fullWindow_Set.getText(), fullWindow_Set);
        addToolTip(tip_firstClick, firstClick_Set);
        addToolTip(tip_floatingRun, floatingRun_Set);
        addToolTip(tip_hideWindowRun, hideWindowRun_Set);
        addToolTip(tip_showWindowRun, showWindowRun_Set);
        addToolTip(tip_floatingRecord, floatingRecord_Set);
        addToolTip(tip_hideWindowRecord, hideWindowRecord_Set);
        addToolTip(tip_showWindowRecord, showWindowRecord_Set);
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
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        integerRangeTextField(floatingDistance_Set, 0, null, tip_floatingDistance);
    }

    /**
     * 界面初始化
     *
     * @throws IOException io异常
     */
    @FXML
    private void initialize() throws IOException {
        // 文本框输入监听
        textFieldChangeListener();
        // 读取配置文件
        getConfig();
        // 初始化浮窗
        initFloatingWindow();
        // 监听并保存颜色选择器自定义颜色
        setCustomColorsListener();
        // 设置鼠标悬停提示
        setToolTip();
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
     * 记住关闭前打开的页面设置
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadLastTabAction() throws IOException {
        setLoadLastConfigCheckBox(lastTab_Set, configFile, key_loadLastConfig);
    }

    /**
     * 记住窗口是否最大化设置
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFullWindowAction() throws IOException {
        setLoadLastConfigCheckBox(fullWindow_Set, configFile, key_loadLastFullWindow);
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
     * 重启程序按钮
     *
     * @throws IOException io异常
     */
    @FXML
    private void reLaunch() throws IOException {
        Platform.exit();
        if (!isRunningFromJar()) {
            ProcessBuilder processBuilder = null;
            if (systemName.contains(win)) {
                String path = userDir.substring(0, userDir.lastIndexOf(Tools) + Tools.length());
                String appPath = path + File.separator + "Tools.exe";
                processBuilder = new ProcessBuilder(appPath);
            } else if (systemName.contains(macos)) {
                String appName = File.separator + "Tools.app";
                String appPath = userDir.substring(0, userDir.lastIndexOf(appName)) + appName;
                processBuilder = new ProcessBuilder("open", "-n", appPath);
            }
            if (processBuilder != null) {
                processBuilder.start();
            }
        }
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
     * 开始编辑浮窗位置
     */
    @FXML
    private void setFloatingCoordinate() {
        // 显示浮窗
        showFloatingWindow();
        // 监听键盘事件
        startNativeKeyListener();
    }

}
