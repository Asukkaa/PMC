package priv.koishi.pmc.CustomUI.CustomMessageBubble;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
     * 消息气泡所在舞台
     */
    private final Stage bubbleStage;

    /**
     * 消息气泡位置横轴偏移量（正数右移负数左移）
     */
    private final int offsetX = 30;

    /**
     * 消息气泡位置纵偏移量（正数下移负数上移）
     */
    private final int offsetY = 30;

    /**
     * 消息气泡
     *
     * @param text 消息气泡要展示的消息
     * @param time 消息气泡显示时间
     */
    public MessageBubble(String text, double time) {
        setText(text);
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
        // 自动关闭
        new Timeline(new KeyFrame(Duration.seconds(time), e -> {
            bubbleStage.close();
            MousePositionListener.getInstance().removeListener(this);
        })).play();
        bubbleStage.show();
    }

    /**
     * 根据鼠标位置调整ui
     *
     * @param mousePoint 鼠标位置
     */
    @Override
    public void onMousePositionUpdate(Point mousePoint) {
        bubbleStage.setX(mousePoint.getX() + offsetX);
        bubbleStage.setY(mousePoint.getY() + offsetY);
    }

}
