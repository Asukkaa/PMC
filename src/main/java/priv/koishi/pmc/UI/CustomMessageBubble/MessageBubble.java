package priv.koishi.pmc.UI.CustomMessageBubble;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import priv.koishi.pmc.Listener.MousePositionListener;
import priv.koishi.pmc.Listener.MousePositionUpdater;

import java.awt.*;

/**
 * 消息气泡组件
 *
 * @author KOISHI
 * Date:2024-11-28
 * Time:下午5:49
 */
public class MessageBubble extends Label implements MousePositionUpdater {

    /**
     * 当前显示的消息气泡实例
     */
    private static MessageBubble currentBubble;

    /**
     * 消息气泡所在舞台
     */
    private Stage bubbleStage;

    /**
     * 消息气泡位置横轴偏移量（正数右移负数左移）
     */
    private final int offsetX = 30;

    /**
     * 消息气泡位置纵偏移量（正数下移负数上移）
     */
    private final int offsetY = 30;

    /**
     * 自动关闭的计时器
     */
    private Timeline autoCloseTimeline;

    /**
     * 消息气泡（默认显示 1.5 秒）
     *
     * @param text 消息气泡要展示的消息
     */
    public MessageBubble(String text) {
        new MessageBubble(text, 1.5);
    }

    /**
     * 消息气泡
     *
     * @param text 消息气泡要展示的消息
     * @param time 消息气泡显示时间（单位秒，time > 0 才会自动关闭）
     */
    public MessageBubble(String text, double time) {
        // 如果已有显示的气泡，先关闭它
        if (currentBubble != null) {
            currentBubble.closeBubble();
        }
        // 设置当前实例为新的气泡
        currentBubble = this;
        setText(text);
        setMaxWidth(500);
        setWrapText(true);
        setTextFill(Color.WHITE);
        setPadding(new Insets(10));
        setStyle("-fx-background-radius: 5; -fx-background-color: black;-fx-opacity: 0.8;");
        bubbleStage = new Stage();
        bubbleStage.initStyle(StageStyle.TRANSPARENT);
        bubbleStage.initModality(Modality.NONE);
        bubbleStage.setAlwaysOnTop(true);
        StackPane stackPane = new StackPane(this);
        stackPane.setBackground(null);
        Scene scene = new Scene(stackPane);
        scene.setFill(Color.TRANSPARENT);
        bubbleStage.setScene(scene);
        // 获取鼠标坐标监听器
        MousePositionListener.getInstance().addListener(this);
        // 设置初始位置
        Point mousePoint = MousePositionListener.getMousePoint();
        bubbleStage.setX(mousePoint.getX() + offsetX);
        bubbleStage.setY(mousePoint.getY() + offsetY);
        if (time > 0) {
            startAutoCloseTimer(time);
        }
        bubbleStage.show();
    }

    /**
     * 关闭消息气泡
     */
    public void closeBubble() {
        stopAutoCloseTimer();
        if (bubbleStage != null) {
            bubbleStage.close();
        }
        MousePositionListener.getInstance().removeListener(this);
        if (currentBubble == this) {
            currentBubble = null;
        }
    }

    /**
     * 启动自动关闭计时器
     *
     * @param time 延迟秒数
     */
    private void startAutoCloseTimer(double time) {
        if (autoCloseTimeline != null) {
            autoCloseTimeline.stop();
        }
        autoCloseTimeline = new Timeline(new KeyFrame(Duration.seconds(time), _ -> closeBubble()));
        autoCloseTimeline.play();
    }

    /**
     * 停止自动关闭计时器
     */
    private void stopAutoCloseTimer() {
        if (autoCloseTimeline != null) {
            autoCloseTimeline.stop();
            autoCloseTimeline = null;
        }
    }

    /**
     * 修改当前显示气泡的自动关闭时间
     * <p>
     * 如果气泡已关闭，则该方法无效。
     * 若传入时间 > 0，则重置计时器，在指定时间后自动关闭；
     * 若传入时间 <= 0，则取消自动关闭（气泡将保持显示直到手动关闭）。
     *
     * @param time 自动关闭延迟时间（秒），小于等于 0 表示取消自动关闭
     */
    public void setAutoCloseTime(double time) {
        if (bubbleStage == null || !bubbleStage.isShowing()) {
            return;
        }
        Platform.runLater(() -> {
            if (time > 0) {
                startAutoCloseTimer(time);
            } else {
                stopAutoCloseTimer();
            }
        });
    }

    /**
     * 更新气泡显示的文本
     *
     * @param newText 要更新的文本
     */
    public void updateText(String newText) {
        if (bubbleStage == null || !bubbleStage.isShowing()) {
            return;
        }
        Platform.runLater(() -> setText(newText));
    }

    /**
     * 更新气泡文本和显示时间
     *
     * @param newText 要更新的文本
     * @param time    自动关闭延迟时间（秒），小于等于 0 表示取消自动关闭
     */
    public void updateBubble(String newText, double time) {
        updateText(newText);
        setAutoCloseTime(time);
    }

    /**
     * 根据鼠标位置调整 ui
     *
     * @param mousePoint 鼠标位置
     */
    @Override
    public void onMousePositionUpdate(Point mousePoint) {
        if (bubbleStage != null && bubbleStage.isShowing()) {
            bubbleStage.setX(mousePoint.getX() + offsetX);
            bubbleStage.setY(mousePoint.getY() + offsetY);
        }
    }

}
