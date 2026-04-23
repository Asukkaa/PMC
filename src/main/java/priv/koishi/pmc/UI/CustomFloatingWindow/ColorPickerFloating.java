package priv.koishi.pmc.UI.CustomFloatingWindow;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import priv.koishi.pmc.Bean.Config.FloatingWindowConfig;
import priv.koishi.pmc.Listener.MousePositionListener;
import priv.koishi.pmc.Listener.MousePositionUpdater;

import java.awt.*;

import static priv.koishi.pmc.Controller.MainController.settingController;
import static priv.koishi.pmc.Finals.CommonFinals.defaultOffsetX;
import static priv.koishi.pmc.Finals.CommonFinals.defaultOffsetY;
import static priv.koishi.pmc.Finals.i18nFinal.*;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.setPositionText;
import static priv.koishi.pmc.UI.CustomFloatingWindow.FloatingWindow.updateFloatingWindow;
import static priv.koishi.pmc.Utils.ButtonMappingUtils.R_SHIFT;
import static priv.koishi.pmc.Utils.ButtonMappingUtils.cancelKey;
import static priv.koishi.pmc.Utils.ListenerUtils.addNativeListener;
import static priv.koishi.pmc.Utils.ListenerUtils.removeNativeListener;
import static priv.koishi.pmc.Utils.UiUtils.*;

/**
 * 自定义取色器浮窗
 *
 * @author applesaucepenguin
 * Date 2026-03-24
 * time 18:07
 */
public class ColorPickerFloating implements MousePositionUpdater {

    /**
     * 取色器浮窗描述
     */
    private final FloatingWindowDescriptor messageFloating;

    /**
     * 取色器颜色描述
     */
    private Label nowColorLabel;

    /**
     * 颜色选择器描述
     */
    private Label settingLabel;

    /**
     * 取色器颜色预览区域
     */
    private Rectangle nowColorPreview;

    /**
     * 颜色选择器预览区域
     */
    private Rectangle settingPreview;

    /**
     * 鼠标点击监听器
     */
    private NativeMouseListener clickListener;

    /**
     * 键盘监听器
     */
    private NativeKeyListener keyListener;

    /**
     * 需要绑定的颜色选择器
     */
    private ColorPicker colorPicker;

    /**
     * 当前颜色
     */
    private Color color;

    /**
     * 机器人实例
     */
    private Robot robot;

    /**
     * 构造函数
     */
    public ColorPickerFloating() {
        messageFloating = new FloatingWindowDescriptor();
        // 配置描述符
        configureDescriptor();
        // 创建浮窗
        FloatingWindow.createFloatingWindow(messageFloating);
    }

    /**
     * 构建浮窗内的自定义内容
     *
     * @return 自定义内容容器
     */
    private VBox buildCustomContent() {
        VBox box = new VBox(5);
        box.setEffect(new DropShadow(5, Color.BLACK));
        // 颜色预览区域
        nowColorPreview = new Rectangle(40, 40);
        nowColorPreview.setArcWidth(8);
        nowColorPreview.setArcHeight(8);
        nowColorPreview.setStroke(Color.WHITE);
        nowColorPreview.setStrokeWidth(1);
        nowColorLabel = new Label();
        nowColorLabel.setTextFill(Color.WHITE);
        VBox nowBox = new VBox(5, nowColorLabel, nowColorPreview);
        settingPreview = new Rectangle(40, 40);
        settingPreview.setArcWidth(8);
        settingPreview.setArcHeight(8);
        settingPreview.setStroke(Color.WHITE);
        settingPreview.setStrokeWidth(1);
        settingLabel = new Label();
        settingLabel.setTextFill(Color.WHITE);
        VBox settingBox = new VBox(5, settingLabel, settingPreview);
        settingBox.setVisible(false);
        HBox previewBox = new HBox(15, nowBox, settingBox);
        previewBox.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().setAll(previewBox);
        return box;
    }

    /**
     * 配置浮窗描述符
     */
    private void configureDescriptor() {
        FloatingWindowConfig config = new FloatingWindowConfig()
                .setWidth(200)
                .setHeight(210);
        messageFloating.setAdditionalContent(buildCustomContent())
                .setName(text_colorPickerFloating())
                .setTextFill(Color.WHITE)
                .setEnableResize(false)
                .setAddCloseKey(false)
                .setTransparent(true)
                .setEnableDrag(false)
                .setShowName(true)
                .setConfig(config)
                .setOpacity(0.8)
                .setFontSize(14);
    }

    /**
     * 启动取色器
     *
     * @param colorPicker 需要绑定的颜色选择器
     */
    public void start(ColorPicker colorPicker) {
        if (colorPicker != null) {
            this.colorPicker = colorPicker;
            color = colorPicker.getValue();
            settingPreview.setFill(colorPicker.getValue());
            settingLabel.setText(text_settingColor() + "\n" + color);
            settingPreview.getParent().setVisible(true);
            colorPicker.getScene().getRoot().setDisable(true);
        }
        updateTextFill();
        robot = new Robot();
        // 注册监听器
        registerListeners();
        // 显示浮窗
        FloatingWindow.showFloatingWindow(messageFloating);
        // 添加鼠标位置监听器
        MousePositionListener.getInstance().addListener(this);
    }

    /**
     * 更新浮窗文本颜色
     */
    private void updateTextFill() {
        if (settingController != null) {
            // 获取浮窗的文本颜色设置
            Color color = settingController.colorPicker_Set.getValue();
            messageFloating.setTextFill(color);
            nowColorLabel.setTextFill(color);
            settingLabel.setTextFill(color);
        }
    }

    /**
     * 注册全局鼠标/键盘监听器
     */
    private void registerListeners() {
        // 鼠标点击监听
        clickListener = new NativeMouseListener() {
            @Override
            public void nativeMouseClicked(NativeMouseEvent e) {
                if (color != null) {
                    Platform.runLater(() -> {
                        if (colorPicker != null) {
                            colorPicker.setValue(color);
                            settingPreview.setFill(color);
                            settingLabel.setText(text_settingColor() + "\n" + color);
                            // 设置页浮窗颜色取色器修改颜色后立刻更新浮窗颜色
                            if (settingController.colorPicker_Set == colorPicker) {
                                updateTextFill();
                                updateFloatingWindow(messageFloating);
                            }
                        }
                    });
                }
            }
        };
        addNativeListener(clickListener);
        // 键盘监听
        keyListener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                int keyCode = e.getKeyCode();
                keyCode = (keyCode == R_SHIFT) ? NativeKeyEvent.VC_SHIFT : keyCode;
                if (keyCode == cancelKey) {
                    Platform.runLater(ColorPickerFloating.this::close);
                }
            }
        };
        addNativeListener(keyListener);
    }

    /**
     * 根据鼠标位置调整 ui
     *
     * @param mousePoint 鼠标位置
     */
    @Override
    public void onMousePositionUpdate(Point mousePoint) {
        Platform.runLater(() -> {
            if (messageFloating != null) {
                Stage floatingStage = messageFloating.getStage();
                if (floatingStage != null && floatingStage.isShowing()) {
                    int x = (int) mousePoint.getX();
                    int y = (int) mousePoint.getY();
                    TextField offsetXTextField = settingController.offsetX_Set;
                    int offsetX = setDefaultIntValue(offsetXTextField, defaultOffsetX, 0, null);
                    TextField offsetYTextField = settingController.offsetY_Set;
                    int offsetY = setDefaultIntValue(offsetYTextField, defaultOffsetY, 0, null);
                    if (robot != null) {
                        color = robot.getPixelColor(x, y);
                        if (color != null) {
                            nowColorPreview.setFill(color);
                            nowColorLabel.setText(text_nowColor() + "\n" + color);
                        }
                    }
                    String text = autoClick_nowMousePos() +
                            "\nX: " + x + "      Y: " + y +
                            "\n" + text_saveColor();
                    setPositionText(messageFloating, text);
                    floatingMove(floatingStage, mousePoint, offsetX, offsetY);
                }
            }
        });
    }

    /**
     * 关闭取色器，清理监听器
     */
    public void close() {
        if (messageFloating != null) {
            FloatingWindow.hideFloatingWindow(messageFloating);
        }
        if (clickListener != null) {
            removeNativeListener(clickListener);
            clickListener = null;
        }
        if (keyListener != null) {
            removeNativeListener(keyListener);
            keyListener = null;
        }
        robot = null;
        color = null;
        // 移除鼠标位置监听器
        MousePositionListener.getInstance().removeListener(this);
        if (colorPicker != null) {
            Scene scene = colorPicker.getScene();
            scene.getRoot().setDisable(false);
            showStage((Stage) scene.getWindow());
            colorPicker = null;
        }
    }

    /**
     * 判断是否显示
     *
     * @return true 已显示 false 未显示
     */
    public boolean isShowing() {
        return messageFloating != null && messageFloating.getStage() != null && messageFloating.getStage().isShowing();
    }

}
