package priv.koishi.pmc.UI.CustomFloatingWindow;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
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
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;

import java.util.ArrayList;
import java.util.List;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.i18nFinal.clickDetail_showRegion;
import static priv.koishi.pmc.Finals.i18nFinal.tip_setFloatingCoordinate;
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
        rectangle.setFill(new Color(0, 0, 0, 0.5));
        StackPane root = new StackPane();
        root.setBackground(Background.EMPTY);
        // 浮窗坐标信息栏
        Label floatingPosition = new Label();
        Color color = config.getTextFill();
        String fontSize = "-fx-font-size: 18px;";
        floatingPosition.setTextFill(color);
        floatingPosition.setStyle(fontSize);
        Label floatingLabel = new Label(config.getFloatingLabelText());
        floatingLabel.setTextFill(color);
        floatingLabel.setStyle(fontSize);
        String name = config.getName();
        Label nameLabel = new Label();
        if (config.isShowName()) {
            nameLabel.setText(name);
        }
        nameLabel.setStyle(fontSize);
        nameLabel.setTextFill(color);
        VBox vBox = new VBox();
        vBox.getChildren().addAll(floatingPosition, floatingLabel, nameLabel);
        root.getChildren().addAll(rectangle, vBox);
        // 创建浮窗舞台
        Stage stage = new Stage();
        Scene scene = new Scene(root, Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.setTitle(name);
        setWindowLogo(stage, logoPath);
        stage.setOnCloseRequest(event -> floatingWindows.remove(config));
        // 根据配置决定是否启用拖拽
        if (config.isEnableDrag()) {
            addDragHandler(root, stage, floatingPosition, config);
        }
        // 根据配置决定是否启用调整大小
        if (config.isEnableResize()) {
            addResizeHandler(root, rectangle, stage, floatingPosition, config);
        }
        // 保存引用到配置中
        config.setFloatingPosition(floatingPosition)
                .setStage(stage)
                .setRectangle(rectangle);
    }

    /**
     * 添加拖拽移动逻辑
     *
     * @param root             根节点
     * @param stage            浮窗舞台
     * @param floatingPosition 浮窗坐标信息栏
     * @param config           浮窗配置
     */
    private static void addDragHandler(StackPane root, Stage stage, Label floatingPosition, FloatingWindowDescriptor config) {
        double[] xOffset = new double[1];
        double[] yOffset = new double[1];
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
            newX = Math.max(screenBounds.getMinX(), Math.min(newX, screenBounds.getMaxX() - w));
            newY = Math.max(screenBounds.getMinY(), Math.min(newY, screenBounds.getMaxY() - h));
            int x = (int) newX;
            int y = (int) newY;
            // 应用限制后的坐标
            stage.setX(x);
            stage.setY(y);
            setPositionText(config, x, y, w, h, floatingPosition);
        });
    }

    /**
     * 添加拖拽边缘调整大小逻辑
     *
     * @param root             根节点
     * @param stage            浮窗舞台
     * @param floatingPosition 浮窗坐标信息栏
     * @param config           浮窗配置
     */
    private static void addResizeHandler(StackPane root, Rectangle rectangle, Stage stage, Label floatingPosition, FloatingWindowDescriptor config) {
        double width = rectangle.getWidth();
        double height = rectangle.getHeight();
        // 创建调整大小的边框区域
        double resizeBorder = 3;
        // 上边框 - 覆盖整个宽度
        Rectangle resizeTop = createResizeBorder(width, resizeBorder, Cursor.N_RESIZE);
        StackPane.setAlignment(resizeTop, Pos.TOP_CENTER);
        // 右边框 - 覆盖整个高度
        Rectangle resizeRight = createResizeBorder(resizeBorder, height, Cursor.E_RESIZE);
        StackPane.setAlignment(resizeRight, Pos.CENTER_RIGHT);
        // 下边框 - 覆盖整个宽度
        Rectangle resizeBottom = createResizeBorder(width, resizeBorder, Cursor.S_RESIZE);
        StackPane.setAlignment(resizeBottom, Pos.BOTTOM_CENTER);
        // 左边框 - 覆盖整个高度
        Rectangle resizeLeft = createResizeBorder(resizeBorder, height, Cursor.W_RESIZE);
        StackPane.setAlignment(resizeLeft, Pos.CENTER_LEFT);
        // 四个角落
        Rectangle resizeTopLeft = createResizeBorder(resizeBorder, resizeBorder, Cursor.NW_RESIZE);
        StackPane.setAlignment(resizeTopLeft, Pos.TOP_LEFT);
        Rectangle resizeTopRight = createResizeBorder(resizeBorder, resizeBorder, Cursor.NE_RESIZE);
        StackPane.setAlignment(resizeTopRight, Pos.TOP_RIGHT);
        Rectangle resizeBottomLeft = createResizeBorder(resizeBorder, resizeBorder, Cursor.SW_RESIZE);
        StackPane.setAlignment(resizeBottomLeft, Pos.BOTTOM_LEFT);
        Rectangle resizeBottomRight = createResizeBorder(resizeBorder, resizeBorder, Cursor.SE_RESIZE);
        StackPane.setAlignment(resizeBottomRight, Pos.BOTTOM_RIGHT);
        // 上边框 - 调整高度，同时调整 Y
        setupBorderResizeHandler(resizeTop, stage, false, true, false, true, floatingPosition, config);
        // 右边框 - 调整宽度，不调整 X
        setupBorderResizeHandler(resizeRight, stage, true, false, false, false, floatingPosition, config);
        // 下边框 - 调整高度，不调整 Y
        setupBorderResizeHandler(resizeBottom, stage, false, true, false, false, floatingPosition, config);
        // 左边框 - 调整宽度，同时调整 X
        setupBorderResizeHandler(resizeLeft, stage, true, false, true, false, floatingPosition, config);
        setupCornerResizeHandler(resizeTopLeft, stage, floatingPosition, config);
        setupCornerResizeHandler(resizeTopRight, stage, floatingPosition, config);
        setupCornerResizeHandler(resizeBottomLeft, stage, floatingPosition, config);
        setupCornerResizeHandler(resizeBottomRight, stage, floatingPosition, config);
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
     * @param resizeRect       用来拖拽调整大小的矩形
     * @param stage            浮窗舞台
     * @param resizeWidth      是否调整宽度（true 调整）
     * @param resizeHeight     是否调整高度（true 调整）
     * @param adjustX          是否调整 X 坐标（true 调整）
     * @param adjustY          是否调整 Y 坐标（true 调整）
     * @param floatingPosition 浮窗位置显示栏
     */
    private static void setupBorderResizeHandler(Rectangle resizeRect, Stage stage, boolean resizeWidth,
                                                 boolean resizeHeight, boolean adjustX, boolean adjustY,
                                                 Label floatingPosition, FloatingWindowDescriptor config) {
        double[] initialX = new double[1];
        double[] initialY = new double[1];
        double[] initialWidth = new double[1];
        double[] initialHeight = new double[1];
        double[] initialStageX = new double[1];
        double[] initialStageY = new double[1];
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
                newWidth = Math.max(defaultFloatingWidthInt, Math.min(newWidth, screenBounds.getWidth() - newX));
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
                newHeight = Math.max(defaultFloatingHeightInt, Math.min(newHeight, screenBounds.getHeight() - newY));
            }
            // 位置边界约束
            newX = Math.max(screenBounds.getMinX(), Math.min(newX, screenBounds.getMaxX() - 50));
            newY = Math.max(screenBounds.getMinY(), Math.min(newY, screenBounds.getMaxY() - 50));
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
            setPositionText(config, x, y, w, h, floatingPosition);
            event.consume();
        });
    }

    /**
     * 创建调整大小的矩形区域
     *
     * @param resizeRect       用来拖拽调整大小的矩形
     * @param stage            待调整的舞台
     * @param floatingPosition 浮窗位置显示栏
     */
    private static void setupCornerResizeHandler(Rectangle resizeRect, Stage stage, Label floatingPosition,
                                                 FloatingWindowDescriptor config) {
        double[] initialX = new double[1];
        double[] initialY = new double[1];
        double[] initialWidth = new double[1];
        double[] initialHeight = new double[1];
        double[] initialStageX = new double[1];
        double[] initialStageY = new double[1];
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
            newWidth = Math.max(defaultFloatingWidthInt, Math.min(newWidth, screenBounds.getWidth() - newX));
            newHeight = Math.max(defaultFloatingHeightInt, Math.min(newHeight, screenBounds.getHeight() - newY));
            newX = Math.max(screenBounds.getMinX(), Math.min(newX, screenBounds.getMaxX() - 50));
            newY = Math.max(screenBounds.getMinY(), Math.min(newY, screenBounds.getMaxY() - 50));
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
            setPositionText(config, x, y, w, h, floatingPosition);
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
            Label floatingPosition = config.getFloatingPosition();
            FloatingWindowConfig windowConfig = config.getConfig();
            int x = windowConfig.getX();
            int y = windowConfig.getY();
            int w = windowConfig.getWidth();
            int h = windowConfig.getHeight();
            // 改变要防重复点击的组件状态
            changeDisableNodes(config.getDisableNodes(), true);
            floatingStage.setX(x);
            floatingStage.setY(y);
            floatingStage.setWidth(w);
            floatingStage.setHeight(h);
            setPositionText(config, x, y, w, h, floatingPosition);
            Button button = config.getButton();
            button.setText(config.getHideButtonText());
            addToolTip(config.getHideButtonToolTip(), button);
            floatingStage.show();
            // 监听键盘事件
            startNativeKeyListener();
            floatingWindows.add(config);
        });
    }

    /**
     * 填写浮窗位置显示栏
     *
     * @param floatingPosition 浮窗位置显示栏
     */
    private static void setPositionText(FloatingWindowDescriptor config, int x, int y, int w, int h, Label floatingPosition) {
        String point = "";
        if (config.isEnableDrag()) {
            point += " X: " + x + " Y: " + y;
        }
        if (config.isEnableResize()) {
            point += "\n Width:" + w + " Height:" + h;
        }
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
                    floatingStage.hide();
                    Button button = floatingConfig.getButton();
                    button.setText(clickDetail_showRegion());
                    addToolTip(tip_setFloatingCoordinate(), button);
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
     * 更新浮窗信息
     *
     * @param config 要更新的浮窗配置
     * @param text   要更新的信息
     */
    public static void updateFloatingLabel(FloatingWindowDescriptor config, String text) {
        Platform.runLater(() -> {
            Label floatingPosition = config.getFloatingLabel();
            floatingPosition.setText(text);
            config.setFloatingLabel(floatingPosition);
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
