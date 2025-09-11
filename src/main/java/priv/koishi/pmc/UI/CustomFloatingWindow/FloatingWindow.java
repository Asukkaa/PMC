package priv.koishi.pmc.UI.CustomFloatingWindow;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.pmc.Finals.CommonFinals.logoPath;
import static priv.koishi.pmc.Finals.i18nFinal.tip_massageRegion;
import static priv.koishi.pmc.Utils.FileUtils.updateProperties;
import static priv.koishi.pmc.Utils.ListenerUtils.removeNativeListener;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 浮窗组件
 *
 * @author KOISHI
 * Date:2025-09-03
 * Time:17:38
 */
public class FloatingWindow {

    /**
     * 全局键盘监听器
     */
    private static NativeKeyListener nativeKeyListener;

    /**
     * 当前开启的浮窗
     */
    public static final List<FloatingWindowDescriptor> floatingWindows = new ArrayList<>();

    /**
     * 创建多个可配置的浮窗
     *
     * @param floatingConfigs 浮窗配置数组
     */
    public static void createFloatingWindows(FloatingWindowDescriptor... floatingConfigs) {
        for (FloatingWindowDescriptor config : floatingConfigs) {
            createFloatingWindow(config);
        }
    }

    /**
     * 创建一个可配置的浮窗
     *
     * @param config 浮窗配置
     */
    public static void createFloatingWindow(FloatingWindowDescriptor config) {
        FloatingWindowConfig windowConfig = config.getConfig();
        // 创建主容器
        Rectangle rectangle = new Rectangle(windowConfig.getWidth(), windowConfig.getHeight());
        double opacity = config.getOpacity();
        if (!config.isTransparent() && opacity == 0) {
            rectangle.setFill(new Color(0, 0, 0, 0.01));
        } else {
            rectangle.setFill(new Color(0, 0, 0, opacity));
        }
        config.setRectangle(rectangle);
        StackPane root = new StackPane();
        root.setBackground(Background.EMPTY);
        // 浮窗坐标信息栏
        Label floatingPosition = new Label();
        Color color = config.getTextFill();
        String fontSize = "-fx-font-size: " + config.getFontSize() + "px;";
        floatingPosition.setTextFill(color);
        floatingPosition.setStyle(fontSize);
        config.setFloatingPosition(floatingPosition);
        Label massageLabel = new Label(config.getMassage());
        massageLabel.setTextFill(color);
        massageLabel.setStyle(fontSize);
        String name = config.getName();
        config.setMassageLabel(massageLabel);
        Label nameLabel = new Label();
        if (config.isShowName()) {
            nameLabel.setText(name);
        }
        nameLabel.setStyle(fontSize);
        nameLabel.setTextFill(color);
        VBox vBox = new VBox();
        vBox.getChildren().addAll(floatingPosition, massageLabel, nameLabel);
        root.getChildren().addAll(rectangle, vBox);
        StackPane.setMargin(vBox, new Insets(5, 0, 0, 5));
        // 创建浮窗舞台
        Stage stage = new Stage();
        Scene scene = new Scene(root, Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.setTitle(name);
        setWindowLogo(stage, logoPath);
        stage.setOnCloseRequest(event -> floatingWindows.remove(config));
        config.setStage(stage);
        // 根据配置决定是否启用拖拽
        if (config.isEnableDrag()) {
            addDragHandler(root, config);
        }
        // 根据配置决定是否启用调整大小
        if (config.isEnableResize()) {
            addResizeHandler(root, config);
        }
    }

    /**
     * 添加拖拽移动逻辑
     *
     * @param root   根节点
     * @param config 浮窗配置
     */
    private static void addDragHandler(StackPane root, FloatingWindowDescriptor config) {
        Stage stage = config.getStage();
        double[] xOffset = new double[1];
        double[] yOffset = new double[1];
        int margin = config.getMargin();
        root.setCursor(Cursor.MOVE);
        root.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            config.setModified(true);
            // 获取当前所在屏幕
            Screen currentScreen = getCurrentScreen(stage);
            Rectangle2D screenBounds = currentScreen.getBounds();
            // 计算新坐标
            double newX = event.getScreenX() - xOffset[0];
            double newY = event.getScreenY() - yOffset[0];
            int h = (int) stage.getHeight();
            int w = (int) stage.getWidth();
            stage.setWidth(w);
            stage.setHeight(h);
            // 边界约束
            newX = Math.max(screenBounds.getMinX() + margin, Math.min(newX, screenBounds.getMaxX() - w - margin));
            newY = Math.max(screenBounds.getMinY() + margin, Math.min(newY, screenBounds.getMaxY() - h - margin));
            int x = (int) newX;
            int y = (int) newY;
            // 应用限制后的坐标
            stage.setX(x);
            stage.setY(y);
            setPositionText(config, "");
        });
    }

    /**
     * 添加拖拽边缘调整大小逻辑
     *
     * @param root   根节点
     * @param config 浮窗配置
     */
    private static void addResizeHandler(StackPane root, FloatingWindowDescriptor config) {
        Rectangle rectangle = config.getRectangle();
        double width = rectangle.getWidth();
        double height = rectangle.getHeight();
        // 创建调整大小的边框区域
        double resizeBorder = 3;
        // 上边框 - 覆盖整个宽度 - 调整高度，同时调整 Y
        Rectangle resizeTop = createResizeBorder(width, resizeBorder, Cursor.N_RESIZE);
        StackPane.setAlignment(resizeTop, Pos.TOP_CENTER);
        setupBorderResizeHandler(resizeTop, false, true, false, true, config);
        // 右边框 - 覆盖整个高度- 调整宽度，不调整 X
        Rectangle resizeRight = createResizeBorder(resizeBorder, height, Cursor.E_RESIZE);
        StackPane.setAlignment(resizeRight, Pos.CENTER_RIGHT);
        setupBorderResizeHandler(resizeRight, true, false, false, false, config);
        // 下边框 - 覆盖整个宽度 - 调整高度，不调整 Y
        Rectangle resizeBottom = createResizeBorder(width, resizeBorder, Cursor.S_RESIZE);
        StackPane.setAlignment(resizeBottom, Pos.BOTTOM_CENTER);
        setupBorderResizeHandler(resizeBottom, false, true, false, false, config);
        // 左边框 - 覆盖整个高度 - 调整宽度，同时调整 X
        Rectangle resizeLeft = createResizeBorder(resizeBorder, height, Cursor.W_RESIZE);
        StackPane.setAlignment(resizeLeft, Pos.CENTER_LEFT);
        setupBorderResizeHandler(resizeLeft, true, false, true, false, config);
        // 左上角
        Rectangle resizeTopLeft = createResizeBorder(resizeBorder, resizeBorder, Cursor.NW_RESIZE);
        StackPane.setAlignment(resizeTopLeft, Pos.TOP_LEFT);
        setupCornerResizeHandler(resizeTopLeft, config);
        // 右上角
        Rectangle resizeTopRight = createResizeBorder(resizeBorder, resizeBorder, Cursor.NE_RESIZE);
        StackPane.setAlignment(resizeTopRight, Pos.TOP_RIGHT);
        setupCornerResizeHandler(resizeTopRight, config);
        // 左下角
        Rectangle resizeBottomLeft = createResizeBorder(resizeBorder, resizeBorder, Cursor.SW_RESIZE);
        StackPane.setAlignment(resizeBottomLeft, Pos.BOTTOM_LEFT);
        setupCornerResizeHandler(resizeBottomLeft, config);
        // 右下角
        Rectangle resizeBottomRight = createResizeBorder(resizeBorder, resizeBorder, Cursor.SE_RESIZE);
        StackPane.setAlignment(resizeBottomRight, Pos.BOTTOM_RIGHT);
        setupCornerResizeHandler(resizeBottomRight, config);
        // 添加所有调整大小控件到根面板
        root.getChildren().addAll(resizeTop, resizeRight, resizeBottom, resizeLeft,
                resizeTopLeft, resizeTopRight, resizeBottomLeft, resizeBottomRight);
        // 绑定边框大小到浮窗大小
        rectangle.widthProperty().addListener((obs, oldVal, newVal) -> {
            double w = newVal.doubleValue();
            resizeTop.setWidth(w);
            resizeBottom.setWidth(w);
        });
        rectangle.heightProperty().addListener((obs, oldVal, newVal) -> {
            double h = newVal.doubleValue();
            resizeRight.setHeight(h);
            resizeLeft.setHeight(h);
        });
    }

    /**
     * 创建调整大小的矩形区域
     *
     * @param width  矩形宽度
     * @param height 矩形高度
     * @param cursor 鼠标光标
     */
    private static Rectangle createResizeBorder(double width, double height, Cursor cursor) {
        Rectangle rect = new Rectangle(width, height);
        rect.setFill(new Color(0, 0, 0, 1));
        rect.setCursor(cursor);
        return rect;
    }

    /**
     * 设置调整大小的事件处理器
     *
     * @param resizeRect   用来拖拽调整大小的矩形
     * @param resizeWidth  是否调整宽度（true 调整）
     * @param resizeHeight 是否调整高度（true 调整）
     * @param adjustX      是否调整 X 坐标（true 调整）
     * @param adjustY      是否调整 Y 坐标（true 调整）
     */
    private static void setupBorderResizeHandler(Rectangle resizeRect, boolean resizeWidth, boolean resizeHeight,
                                                 boolean adjustX, boolean adjustY, FloatingWindowDescriptor config) {
        Stage stage = config.getStage();
        double[] initialX = new double[1];
        double[] initialY = new double[1];
        double[] initialWidth = new double[1];
        double[] initialHeight = new double[1];
        double[] initialStageX = new double[1];
        double[] initialStageY = new double[1];
        int margin = config.getMargin();
        resizeRect.setOnMousePressed(event -> {
            initialX[0] = event.getScreenX();
            initialY[0] = event.getScreenY();
            initialWidth[0] = stage.getWidth();
            initialHeight[0] = stage.getHeight();
            initialStageX[0] = stage.getX();
            initialStageY[0] = stage.getY();
            event.consume();
        });
        resizeRect.setOnMouseDragged(event -> {
            config.setModified(true);
            // 获取当前所在屏幕
            Screen currentScreen = getCurrentScreen(stage);
            Rectangle2D screenBounds = currentScreen.getBounds();
            double newWidth = initialWidth[0];
            double newHeight = initialHeight[0];
            double newX = initialStageX[0];
            double newY = initialStageY[0];
            if (resizeWidth) {
                double widthDelta = event.getScreenX() - initialX[0];
                if (adjustX) {
                    // 左侧调整：同时改变X坐标和宽度
                    newX = initialStageX[0] + widthDelta;
                    newWidth = initialWidth[0] - widthDelta;
                } else {
                    // 右侧调整：只改变宽度
                    newWidth = initialWidth[0] + widthDelta;
                }
                // 宽度边界约束
                newWidth = Math.max(config.getMinWidth(), Math.min(newWidth, screenBounds.getWidth() - newX));
            }
            if (resizeHeight) {
                double heightDelta = event.getScreenY() - initialY[0];
                if (adjustY) {
                    // 上侧调整：同时改变Y坐标和高度
                    newY = initialStageY[0] + heightDelta;
                    newHeight = initialHeight[0] - heightDelta;
                } else {
                    // 下侧调整：只改变高度
                    newHeight = initialHeight[0] + heightDelta;
                }
                // 高度边界约束
                newHeight = Math.max(config.getMinHeight(), Math.min(newHeight, screenBounds.getHeight() - newY));
            }
            // 位置边界约束
            newX = Math.max(screenBounds.getMinX() + margin, Math.min(newX, screenBounds.getMaxX() - newWidth - margin));
            newY = Math.max(screenBounds.getMinY() + margin, Math.min(newY, screenBounds.getMaxY() - newHeight - margin));
            int x = (int) newX;
            int y = (int) newY;
            int w = (int) newWidth;
            int h = (int) newHeight;
            stage.setX(x);
            stage.setY(y);
            stage.setWidth(w);
            stage.setHeight(h);
            // 更新主矩形大小
            StackPane root = (StackPane) stage.getScene().getRoot();
            for (Node node : root.getChildren()) {
                if (node instanceof Rectangle mainRect) {
                    mainRect.setWidth(newWidth);
                    mainRect.setHeight(newHeight);
                    break;
                }
            }
            setPositionText(config, "");
            event.consume();
        });
    }

    /**
     * 创建调整大小的矩形区域
     *
     * @param resizeRect 用来拖拽调整大小的矩形
     * @param config     浮窗配置
     */
    private static void setupCornerResizeHandler(Rectangle resizeRect, FloatingWindowDescriptor config) {
        Stage stage = config.getStage();
        double[] initialX = new double[1];
        double[] initialY = new double[1];
        double[] initialWidth = new double[1];
        double[] initialHeight = new double[1];
        double[] initialStageX = new double[1];
        double[] initialStageY = new double[1];
        int margin = config.getMargin();
        resizeRect.setOnMousePressed(event -> {
            initialX[0] = event.getScreenX();
            initialY[0] = event.getScreenY();
            initialWidth[0] = stage.getWidth();
            initialHeight[0] = stage.getHeight();
            initialStageX[0] = stage.getX();
            initialStageY[0] = stage.getY();
            event.consume();
        });
        resizeRect.setOnMouseDragged(event -> {
            config.setModified(true);
            // 获取当前所在屏幕
            Screen currentScreen = getCurrentScreen(stage);
            Rectangle2D screenBounds = currentScreen.getBounds();
            double newWidth;
            double newHeight;
            double newX = initialStageX[0];
            double newY = initialStageY[0];
            double widthDelta = event.getScreenX() - initialX[0];
            // 根据角落位置决定调整方向
            if (StackPane.getAlignment(resizeRect) == Pos.TOP_LEFT ||
                    StackPane.getAlignment(resizeRect) == Pos.BOTTOM_LEFT) {
                // 左侧角落：同时改变X坐标和宽度
                newX = initialStageX[0] + widthDelta;
                newWidth = initialWidth[0] - widthDelta;
            } else {
                // 右侧角落：只改变宽度
                newWidth = initialWidth[0] + widthDelta;
            }
            double heightDelta = event.getScreenY() - initialY[0];
            // 根据角落位置决定调整方向
            if (StackPane.getAlignment(resizeRect) == Pos.TOP_LEFT ||
                    StackPane.getAlignment(resizeRect) == Pos.TOP_RIGHT) {
                // 上侧角落：同时改变Y坐标和高度
                newY = initialStageY[0] + heightDelta;
                newHeight = initialHeight[0] - heightDelta;
            } else {
                // 下侧角落：只改变高度
                newHeight = initialHeight[0] + heightDelta;
            }
            // 边界约束
            newWidth = Math.max(config.getMinWidth(), Math.min(newWidth, screenBounds.getWidth() - newX));
            newHeight = Math.max(config.getMinHeight(), Math.min(newHeight, screenBounds.getHeight() - newY));
            newX = Math.max(screenBounds.getMinX(), Math.min(newX + margin, screenBounds.getMaxX() - newWidth - margin));
            newY = Math.max(screenBounds.getMinY(), Math.min(newY + margin, screenBounds.getMaxY() - newHeight - margin));
            int x = (int) newX;
            int y = (int) newY;
            int w = (int) newWidth;
            int h = (int) newHeight;
            stage.setX(x);
            stage.setY(y);
            stage.setWidth(w);
            stage.setHeight(h);
            // 更新主矩形大小
            StackPane root = (StackPane) stage.getScene().getRoot();
            for (Node node : root.getChildren()) {
                if (node instanceof Rectangle mainRect) {
                    mainRect.setWidth(newWidth);
                    mainRect.setHeight(newHeight);
                    break;
                }
            }
            setPositionText(config, "");
            event.consume();
        });
    }

    /**
     * 显示浮窗
     *
     * @param config 浮窗配置
     */
    public static void showFloatingWindow(FloatingWindowDescriptor config) {
        Platform.runLater(() -> {
            Stage floatingStage = config.getStage();
            Rectangle rectangle = config.getRectangle();
            FloatingWindowConfig windowConfig = config.getConfig();
            int x = windowConfig.getX();
            int y = windowConfig.getY();
            int w = windowConfig.getWidth();
            int h = windowConfig.getHeight();
            // 改变要防重复点击的组件状态
            changeDisableNodes(config.getDisableNodes(), true);
            rectangle.setX(x);
            rectangle.setY(y);
            rectangle.setWidth(w);
            rectangle.setHeight(h);
            setPositionText(config, "");
            Button button = config.getButton();
            if (button != null) {
                button.setText(config.getHideButtonText());
                addToolTip(config.getHideButtonToolTip(), button);
            }
            floatingStage.show();
            // 监听键盘事件
            startNativeKeyListener();
            floatingWindows.add(config);
        });
    }

    /**
     * 填写浮窗位置显示栏
     *
     * @param config 浮窗配置
     * @param text   要更新的文本
     */
    public static void setPositionText(FloatingWindowDescriptor config, String text) {
        Stage floatingStage = config.getStage();
        int x = (int) floatingStage.getX();
        int y = (int) floatingStage.getY();
        int w = (int) floatingStage.getWidth();
        int h = (int) floatingStage.getHeight();
        String point = text;
        if (config.isEnableDrag()) {
            point += "X: " + x + " Y: " + y;
        }
        if (config.isEnableResize()) {
            point += "\nWidth:" + w + " Height:" + h;
        }
        Label floatingPosition = config.getFloatingPosition();
        floatingPosition.setText(point);
    }

    /**
     * 隐藏浮窗
     *
     * @param floatingConfigs 浮窗配置
     */
    public static void hideFloatingWindow(FloatingWindowDescriptor... floatingConfigs) {
        Platform.runLater(() -> {
            for (FloatingWindowDescriptor floatingConfig : floatingConfigs) {
                Stage floatingStage = floatingConfig.getStage();
                if (floatingStage != null && floatingStage.isShowing()) {
                    int floatingX = (int) floatingStage.getX();
                    int floatingY = (int) floatingStage.getY();
                    int floatingWidth = (int) floatingStage.getWidth();
                    int floatingHeight = (int) floatingStage.getHeight();
                    FloatingWindowConfig windowConfig = floatingConfig.getConfig();
                    windowConfig.setWidth(floatingWidth)
                            .setHeight(floatingHeight)
                            .setX(floatingX)
                            .setY(floatingY);
                    floatingConfig.setConfig(windowConfig);
                    if (StringUtils.isNotBlank(floatingConfig.getConfigFile())) {
                        try {
                            saveFloatingCoordinate(floatingConfig);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    floatingStage.hide();
                    Button button = floatingConfig.getButton();
                    if (button != null) {
                        button.setText(floatingConfig.getShowButtonText());
                        addToolTip(tip_massageRegion(), button);
                    }
                    // 改变要防重复点击的组件状态
                    changeDisableNodes(floatingConfig.getDisableNodes(), false);
                    removeNativeListener(nativeKeyListener);
                    floatingConfig.getStage().close();
                    floatingWindows.remove(floatingConfig);
                }
            }
        });
    }

    /**
     * 保存浮窗设置
     *
     * @param floatingWindow 浮窗设置
     * @throws IOException 配置文件保存异常
     */
    private static void saveFloatingCoordinate(FloatingWindowDescriptor floatingWindow) throws IOException {
        FloatingWindowConfig config = floatingWindow.getConfig();
        int floatingX = config.getX();
        int floatingY = config.getY();
        int floatingWidth = config.getWidth();
        int floatingHeight = config.getHeight();
        String configFile = floatingWindow.getConfigFile();
        String xKey = floatingWindow.getXKey();
        String yKey = floatingWindow.getYKey();
        String widthKey = floatingWindow.getWidthKey();
        String heightKey = floatingWindow.getHeightKey();
        updateProperties(configFile, xKey, String.valueOf(floatingX));
        updateProperties(configFile, yKey, String.valueOf(floatingY));
        updateProperties(configFile, widthKey, String.valueOf(floatingWidth));
        updateProperties(configFile, heightKey, String.valueOf(floatingHeight));
    }

    /**
     * 更新浮窗信息
     *
     * @param config 要更新的浮窗配置
     * @param text   要更新的信息
     */
    public static void updateMassageLabel(FloatingWindowDescriptor config, String text) {
        Platform.runLater(() -> {
            Label massageLabel = config.getMassageLabel();
            massageLabel.setText(text);
        });
    }

    /**
     * 更新浮窗设置
     *
     * @param config 浮窗配置
     */
    public static void updateFloatingWindow(FloatingWindowDescriptor config) {
        Platform.runLater(() -> {
            Label massageLabel = config.getMassageLabel();
            Label floatingPosition = config.getFloatingPosition();
            Color color = config.getTextFill();
            massageLabel.setTextFill(color);
            floatingPosition.setTextFill(color);
            Rectangle rectangle = config.getRectangle();
            double opacity = config.getOpacity();
            if (!config.isTransparent() && opacity == 0) {
                rectangle.setFill(new Color(0, 0, 0, 0.01));
            } else {
                rectangle.setFill(new Color(0, 0, 0, opacity));
            }
        });
    }

    /**
     * 开启全局键盘监听
     */
    private static void startNativeKeyListener() {
        removeNativeListener(nativeKeyListener);
        // 键盘监听器
        nativeKeyListener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                Platform.runLater(() -> {
                    // 检测快捷键 esc
                    if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
                        floatingWindows.forEach(floatingConfig -> {
                            Stage stage = floatingConfig.getStage();
                            if (stage != null && stage.isShowing()) {
                                hideFloatingWindow(floatingConfig);
                            }
                        });
                    }
                });
            }
        };
        GlobalScreen.addNativeKeyListener(nativeKeyListener);
    }

}
