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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static priv.koishi.pmc.Finals.CommonFinals.*;
import static priv.koishi.pmc.Finals.CommonKeys.*;
import static priv.koishi.pmc.Finals.i18nFinal.text_noCancelKey;
import static priv.koishi.pmc.Finals.i18nFinal.tip_messageRegion;
import static priv.koishi.pmc.JnaNative.GlobalWindowMonitor.WindowMonitor.updateRelativeInfo;
import static priv.koishi.pmc.Utils.ButtonMappingUtils.R_SHIFT;
import static priv.koishi.pmc.Utils.ButtonMappingUtils.cancelKey;
import static priv.koishi.pmc.Utils.FileUtils.checkRunningInputStream;
import static priv.koishi.pmc.Utils.FileUtils.updateProperties;
import static priv.koishi.pmc.Utils.ListenerUtils.removeNativeListener;
import static priv.koishi.pmc.Utils.NodeDisableUtils.changeDisableNodes;
import static priv.koishi.pmc.Utils.ToolTipUtils.addToolTip;
import static priv.koishi.pmc.Utils.UiUtils.getCurrentScreen;
import static priv.koishi.pmc.Utils.UiUtils.setWindowLogo;

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
     * 浮窗调整大小标识符
     */
    public static final String RESIZE = "RESIZE";

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
        Rectangle rectangle = creatRectangle(config);
        config.setRectangle(rectangle);
        StackPane root = new StackPane();
        root.setBackground(Background.EMPTY);
        // 浮窗坐标信息栏
        TextAlignment textAlignment = config.getTextAlignment();
        Label floatingPosition = new Label();
        Color color = config.getTextFill();
        String fontSize = "-fx-font-size: " + config.getFontSize() + "px;";
        floatingPosition.setTextFill(color);
        floatingPosition.setStyle(fontSize);
        floatingPosition.setTextAlignment(textAlignment);
        floatingPosition.setMaxWidth(270);
        config.setFloatingPosition(floatingPosition);
        Label messageLabel = new Label(config.getMessage());
        messageLabel.setTextFill(color);
        messageLabel.setStyle(fontSize);
        messageLabel.setTextAlignment(textAlignment);
        messageLabel.setMaxWidth(270);
        String name = config.getName();
        config.setMessageLabel(messageLabel);
        Label nameLabel = new Label();
        nameLabel.setText(name);
        nameLabel.setStyle(fontSize);
        nameLabel.setTextFill(color);
        nameLabel.setTextAlignment(textAlignment);
        nameLabel.setMaxWidth(270);
        config.setNameeLabel(nameLabel);
        VBox vBox = new VBox();
        vBox.setAlignment(config.getPos());
        vBox.getChildren().addAll(floatingPosition, messageLabel, nameLabel);
        root.getChildren().addAll(rectangle, vBox);
        StackPane.setMargin(vBox, new Insets(15, 0, 0, 15));
        // 创建浮窗舞台
        Stage stage = new Stage();
        Scene scene = new Scene(root, Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.setTitle(name);
        setWindowLogo(stage, logoPath);
        stage.setOnCloseRequest(_ -> floatingWindows.remove(config));
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
     * 创建浮窗矩形
     *
     * @param config 浮窗配置
     * @return 浮窗矩形
     */
    private static Rectangle creatRectangle(FloatingWindowDescriptor config) {
        FloatingWindowConfig windowConfig = config.getConfig();
        // 创建主容器
        Rectangle rectangle = new Rectangle(windowConfig.getWidth(), windowConfig.getHeight());
        Color backgroundColor = config.getBackgroundColor();
        if (backgroundColor != null) {
            rectangle.setFill(backgroundColor);
        } else {
            double opacity = config.getOpacity();
            if (!config.isTransparent() && opacity == 0) {
                rectangle.setFill(new Color(0, 0, 0, 0.01));
            } else {
                rectangle.setFill(new Color(0, 0, 0, opacity));
            }
        }
        return rectangle;
    }

    /**
     * 更新浮窗的大小限制条件
     *
     * @param config 浮窗配置
     */
    public static void updateSizeConstraints(FloatingWindowDescriptor config) {
        if (config != null && config.getStage() != null) {
            Platform.runLater(() -> {
                Stage stage = config.getStage();
                StackPane root = (StackPane) stage.getScene().getRoot();
                if (config.isEnableDrag()) {
                    addDragHandler(root, config);
                }
                if (config.isEnableResize()) {
                    addResizeHandler(root, config);
                }
            });
        }
    }

    /**
     * 添加拖拽移动逻辑
     *
     * @param root   根节点
     * @param config 浮窗配置
     */
    private static void addDragHandler(StackPane root, FloatingWindowDescriptor config) {
        double[] xOffset = new double[1];
        double[] yOffset = new double[1];
        root.setCursor(Cursor.MOVE);
        root.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            config.setModified(true);
            mouseDragged(config, event, xOffset, yOffset);
        });
    }

    /**
     * 添加拖拽移动逻辑
     *
     * @param config  浮窗配置
     * @param event   鼠标事件
     * @param xOffset X 轴偏移量
     * @param yOffset Y 轴偏移量
     */
    private static void mouseDragged(FloatingWindowDescriptor config, MouseEvent event, double[] xOffset,
                                     double[] yOffset) {
        Stage stage = config.getStage();
        int margin = config.getMargin();
        int minX = config.getMinX();
        int minY = config.getMinY();
        int maxWidth = config.getMaxWidth();
        int maxHeight = config.getMaxHeight();
        // 获取当前所在屏幕
        Screen currentScreen = getCurrentScreen(stage);
        Rectangle2D screenBounds = currentScreen.getBounds();
        // 计算自定义矩形边界（考虑最小横纵坐标和最大宽高）
        double customMinX = minX > 0 ? minX : screenBounds.getMinX() + margin;
        double customMinY = minY > 0 ? minY : screenBounds.getMinY() + margin;
        double customMaxX = minX + maxWidth;
        double customMaxY = minY + maxHeight;
        double newX = stage.getX();
        double newY = stage.getY();
        // 计算新坐标
        if (event != null) {
            newX = event.getScreenX() - xOffset[0];
            newY = event.getScreenY() - yOffset[0];
        }
        int w = (int) stage.getWidth();
        int h = (int) stage.getHeight();
        stage.setWidth(w);
        stage.setHeight(h);
        // 边界约束（同时考虑屏幕边界和自定义矩形边界）
        double minXAllowed = Math.max(screenBounds.getMinX() + margin, customMinX);
        double minYAllowed = Math.max(screenBounds.getMinY() + margin, customMinY);
        double maxXAllowed = Math.min(screenBounds.getMaxX() - w - margin,
                minX > 0 ? customMaxX - w : screenBounds.getMaxX() - w - margin);
        double maxYAllowed = Math.min(screenBounds.getMaxY() - h - margin,
                minY > 0 ? customMaxY - h : screenBounds.getMaxY() - h - margin);
        // 检查最大横纵坐标约束
        int maxX = config.getMaxX();
        int maxY = config.getMaxY();
        if (maxX > 0) {
            maxXAllowed = Math.min(maxXAllowed, maxX - w);
        }
        if (maxY > 0) {
            maxYAllowed = Math.min(maxYAllowed, maxY - h);
        }
        newX = Math.max(minXAllowed, Math.min(newX, maxXAllowed));
        newY = Math.max(minYAllowed, Math.min(newY, maxYAllowed));
        int x = (int) newX;
        int y = (int) newY;
        // 应用限制后的坐标
        stage.setX(x);
        stage.setY(y);
        setPositionText(config, "");
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
        // 如果已经存在调整大小边框，先移除
        root.getChildren().removeIf(node ->
                node instanceof Rectangle &&
                        node.getCursor() != null &&
                        node.getCursor().toString().contains(RESIZE));
        // 创建调整大小的边框区域
        double resizeBorder = 10;
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
        rectangle.widthProperty().addListener((_, _, newVal) -> {
            double w = newVal.doubleValue();
            resizeTop.setWidth(w);
            resizeBottom.setWidth(w);
        });
        rectangle.heightProperty().addListener((_, _, newVal) -> {
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
        rect.setFill(new Color(1, 1, 1, 1));
        rect.setCursor(cursor);
        return rect;
    }

    /**
     * 设置所有 Label 为相同宽度（取最宽的 Label 的宽度）
     *
     * @param config 浮窗配置
     */
    private static void setSameLabelWidth(FloatingWindowDescriptor config) {
        if (config.isFormattingText()) {
            double maxWidth = 0;
            Label messageLabel = config.getMessageLabel();
            Label nameLabel = config.getNameeLabel();
            Label floatingPosition = config.getFloatingPosition();
            Label[] labels = {messageLabel, nameLabel, floatingPosition};
            // 找到最宽的 Label
            for (Label label : labels) {
                if (label != null) {
                    // 获取 Label 的实际宽度
                    double width = label.getWidth();
                    if (width > maxWidth) {
                        maxWidth = width;
                    }
                }
            }
            if (maxWidth > 0) {
                for (Label label : labels) {
                    if (label != null) {
                        label.setMinWidth(maxWidth);
                        label.setPrefWidth(maxWidth);
                    }
                }
            }
        }
    }

    /**
     * 设置边界调整大小的事件处理器
     *
     * @param resizeRect   用来拖拽调整大小的矩形
     * @param resizeWidth  是否调整宽度（true 调整）
     * @param resizeHeight 是否调整高度（true 调整）
     * @param adjustX      是否调整 X 坐标（true 调整）
     * @param adjustY      是否调整 Y 坐标（true 调整）
     * @param config       浮窗配置
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
        int minX = config.getMinX();
        int minY = config.getMinY();
        int margin = config.getMargin();
        int maxWidth = config.getMaxWidth();
        int minWidth = config.getMinWidth();
        int maxHeight = config.getMaxHeight();
        int minHeight = config.getMinHeight();
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
            // 计算自定义矩形边界（考虑最小横纵坐标和最大宽高）
            int maxX = config.getMaxX();
            int maxY = config.getMaxY();
            double customMinX = minX > 0 ? minX : screenBounds.getMinX() + margin;
            double customMinY = minY > 0 ? minY : screenBounds.getMinY() + margin;
            double customMaxX = minX + maxWidth;
            double customMaxY = minY + maxHeight;
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
                // 计算最大允许宽度（考虑屏幕边界、自定义矩形边界和配置的最大宽度）
                double maxAllowedWidth = screenBounds.getWidth() - newX;
                if (maxWidth >= minWidth) {
                    maxAllowedWidth = Math.min(maxAllowedWidth, maxWidth);
                }
                // 确保宽度不超过自定义矩形的右边界
                if (minX > 0 && newX >= minX) {
                    maxAllowedWidth = Math.min(maxAllowedWidth, customMaxX - newX);
                }
                // 确保右边界不超过最大横坐标
                if (maxX > 0) {
                    maxAllowedWidth = Math.min(maxAllowedWidth, maxX - newX);
                }
                // 宽度边界约束
                newWidth = Math.max(config.getMinWidth(), Math.min(newWidth, maxAllowedWidth));
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
                // 计算最大允许高度（考虑屏幕边界、自定义矩形边界和配置的最大高度）
                double maxAllowedHeight = screenBounds.getHeight() - newY;
                if (maxHeight >= minHeight) {
                    maxAllowedHeight = Math.min(maxAllowedHeight, maxHeight);
                }
                // 确保高度不超过自定义矩形的下边界
                if (minY > 0 && newY >= minY) {
                    maxAllowedHeight = Math.min(maxAllowedHeight, customMaxY - newY);
                }
                // 确保下边界不超过最大纵坐标
                if (maxY > 0) {
                    maxAllowedHeight = Math.min(maxAllowedHeight, maxY - newY);
                }
                // 高度边界约束
                newHeight = Math.max(config.getMinHeight(), Math.min(newHeight, maxAllowedHeight));
            }
            // 位置边界约束（同时考虑屏幕边界和自定义矩形边界）
            double minXAllowed = Math.max(screenBounds.getMinX() + margin, customMinX);
            double minYAllowed = Math.max(screenBounds.getMinY() + margin, customMinY);
            double maxXAllowed = Math.min(screenBounds.getMaxX() - newWidth - margin,
                    minX > 0 ? customMaxX - newWidth : screenBounds.getMaxX() - newWidth - margin);
            double maxYAllowed = Math.min(screenBounds.getMaxY() - newHeight - margin,
                    minY > 0 ? customMaxY - newHeight : screenBounds.getMaxY() - newHeight - margin);
            // 检查最大横纵坐标约束
            if (maxX > 0) {
                maxXAllowed = Math.min(maxXAllowed, maxX - newWidth);
            }
            if (maxY > 0) {
                maxYAllowed = Math.min(maxYAllowed, maxY - newHeight);
            }
            newX = Math.max(minXAllowed, Math.min(newX, maxXAllowed));
            newY = Math.max(minYAllowed, Math.min(newY, maxYAllowed));
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
     * 设置角落调整大小的事件处理器
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
        int minX = config.getMinX();
        int minY = config.getMinY();
        int margin = config.getMargin();
        int maxWidth = config.getMaxWidth();
        int minWidth = config.getMinWidth();
        int maxHeight = config.getMaxHeight();
        int minHeight = config.getMinHeight();
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
            // 计算自定义矩形边界（考虑最小横纵坐标和最大宽高）
            double customMinX = minX > 0 ? minX : screenBounds.getMinX() + margin;
            double customMinY = minY > 0 ? minY : screenBounds.getMinY() + margin;
            double customMaxX = minX + maxWidth;
            double customMaxY = minY + maxHeight;
            double newWidth, newHeight;
            double newX = initialStageX[0], newY = initialStageY[0];
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
            // 计算最大允许宽度（考虑屏幕边界、自定义矩形边界和配置的最大宽度）
            double maxAllowedWidth = screenBounds.getWidth() - newX;
            if (maxWidth >= minWidth) {
                maxAllowedWidth = Math.min(maxAllowedWidth, maxWidth);
            }
            // 确保宽度不超过自定义矩形的右边界
            if (minX > 0 && newX >= minX) {
                maxAllowedWidth = Math.min(maxAllowedWidth, customMaxX - newX);
            }
            // 计算最大允许高度（考虑屏幕边界、自定义矩形边界和配置的最大高度）
            double maxAllowedHeight = screenBounds.getHeight() - newY;
            if (maxHeight >= minHeight) {
                maxAllowedHeight = Math.min(maxAllowedHeight, maxHeight);
            }
            // 确保高度不超过自定义矩形的下边界
            if (minY > 0 && newY >= minY) {
                maxAllowedHeight = Math.min(maxAllowedHeight, customMaxY - newY);
            }
            // 大小边界约束
            newWidth = Math.max(config.getMinWidth(), Math.min(newWidth, maxAllowedWidth));
            newHeight = Math.max(config.getMinHeight(), Math.min(newHeight, maxAllowedHeight));
            // 位置边界约束（同时考虑屏幕边界和自定义矩形边界）
            double minXAllowed = Math.max(screenBounds.getMinX() + margin, customMinX);
            double minYAllowed = Math.max(screenBounds.getMinY() + margin, customMinY);
            double maxXAllowed = Math.min(screenBounds.getMaxX() - newWidth - margin,
                    minX > 0 ? customMaxX - newWidth : screenBounds.getMaxX() - newWidth - margin);
            double maxYAllowed = Math.min(screenBounds.getMaxY() - newHeight - margin,
                    minY > 0 ? customMaxY - newHeight : screenBounds.getMaxY() - newHeight - margin);
            newX = Math.max(minXAllowed, Math.min(newX, maxXAllowed));
            newY = Math.max(minYAllowed, Math.min(newY, maxYAllowed));
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
            if (cancelKey == noKeyboard) {
                throw new RuntimeException(text_noCancelKey());
            }
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
            floatingStage.setX(x);
            floatingStage.setY(y);
            floatingStage.setWidth(w);
            floatingStage.setHeight(h);
            config.getMessageLabel().setText(config.getMessage());
            setPositionText(config, "");
            Button button = config.getButton();
            if (button != null) {
                button.setText(config.getHideButtonText());
                addToolTip(config.getHideButtonToolTip(), button);
            }
            config.getNameeLabel().setVisible(config.isShowName());
            floatingStage.show();
            setSameLabelWidth(config);
            // 模拟一次窗口拖拽来校验位置
            if (config.isShowRelativeInfo()) {
                mouseDragged(config, null, new double[1], new double[1]);
            }
            // 监听键盘事件
            if (config.isAddCloseKey()) {
                startNativeKeyListener();
                floatingWindows.add(config);
            }
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
        if (floatingStage.isShowing()) {
            int x = (int) floatingStage.getX();
            int y = (int) floatingStage.getY();
            int w = (int) floatingStage.getWidth();
            int h = (int) floatingStage.getHeight();
            updateText(config, text, x, y, w, h);
        } else {
            int x = config.getConfig().getX();
            int y = config.getConfig().getY();
            int w = config.getConfig().getWidth();
            int h = config.getConfig().getHeight();
            updateText(config, text, x, y, w, h);
        }
    }

    /**
     * 更新浮窗属性信息
     *
     * @param config 浮窗配置
     * @param text   要更新的文本
     * @param x      浮窗 X 坐标
     * @param y      浮窗 Y 坐标
     * @param w      浮窗宽度
     * @param h      浮窗高度
     */
    private static void updateText(FloatingWindowDescriptor config, String text, int x, int y, int w, int h) {
        String point = text;
        if (config.isEnableDrag()) {
            point += "X: " + x + " Y: " + y;
        }
        if (config.isEnableResize()) {
            point += "\nWidth: " + w + " Height: " + h;
        }
        if (config.isShowRelativeInfo()) {
            point += "\n" + updateRelativeInfo(config.getConfig().getWindowInfo());
        }
        Label floatingPosition = config.getFloatingPosition();
        floatingPosition.setText(point);
        setSameLabelWidth(config);
    }

    /**
     * 隐藏浮窗
     *
     * @param floatingConfigs 浮窗配置
     */
    public static void hideFloatingWindow(FloatingWindowDescriptor... floatingConfigs) {
        hideFloatingWindow(true, floatingConfigs);
    }

    /**
     * 隐藏浮窗
     *
     * @param isChangeDisableNodes 是否改变要防重复点击的组件为可交换状态(true-改变为可交互状态)
     * @param floatingConfigs      浮窗配置
     */
    public static void hideFloatingWindow(boolean isChangeDisableNodes, FloatingWindowDescriptor... floatingConfigs) {
        Platform.runLater(() -> {
            for (FloatingWindowDescriptor floatingConfig : floatingConfigs) {
                if (floatingConfig != null) {
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
                        if (floatingConfig.isCloseSave() && StringUtils.isNotBlank(floatingConfig.getConfigFile())) {
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
                            addToolTip(tip_messageRegion(), button);
                        }
                        // 改变要防重复点击的组件状态
                        if (isChangeDisableNodes) {
                            changeDisableNodes(floatingConfig.getDisableNodes(), false);
                        }
                        removeNativeListener(nativeKeyListener);
                        if (floatingConfig.isOnlyHide()) {
                            floatingStage.hide();
                        } else {
                            floatingStage.close();
                        }
                        floatingWindows.remove(floatingConfig);
                    }
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
    public static void updateMessageLabel(FloatingWindowDescriptor config, String text) {
        Platform.runLater(() -> {
            Label messageLabel = config.getMessageLabel();
            messageLabel.setText(text);
            config.setMessage(text);
            setSameLabelWidth(config);
        });
    }

    /**
     * 更新浮窗设置
     *
     * @param config 浮窗配置
     */
    public static void updateFloatingWindow(FloatingWindowDescriptor config) {
        Platform.runLater(() -> {
            Color color = config.getTextFill();
            Label nameeLabel = config.getNameeLabel();
            if (nameeLabel != null) {
                nameeLabel.setTextFill(color);
            }
            Label messageLabel = config.getMessageLabel();
            if (messageLabel != null) {
                messageLabel.setTextFill(color);
            }
            Label floatingPosition = config.getFloatingPosition();
            if (floatingPosition != null) {
                floatingPosition.setTextFill(color);
            }
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
     * 初始化浮窗坐标与宽高
     *
     * @param messageFloating 浮窗属性类
     * @param configPath      配置文件路径
     * @throws IOException 配置文件读取异常
     */
    public static void getFloatingSetting(FloatingWindowDescriptor messageFloating, String configPath) throws IOException {
        if (messageFloating != null) {
            FloatingWindowConfig config = messageFloating.getConfig();
            if (config != null) {
                Properties prop = new Properties();
                InputStream input = checkRunningInputStream(configPath);
                prop.load(input);
                config.setHeight(Integer.parseInt(prop.getProperty(key_messageHeight, defaultFloatingHeight)))
                        .setWidth(Integer.parseInt(prop.getProperty(key_messageWidth, defaultFloatingWidth)))
                        .setX(Integer.parseInt(prop.getProperty(key_messageX, defaultFloatingX)))
                        .setY(Integer.parseInt(prop.getProperty(key_messageY, defaultFloatingY)));
                input.close();
                messageFloating.setConfig(config);
            }
        }
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
                    int keyCode = e.getKeyCode();
                    // 处理右 shift
                    keyCode = (keyCode == R_SHIFT) ? NativeKeyEvent.VC_SHIFT : keyCode;
                    if (keyCode == cancelKey) {
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
