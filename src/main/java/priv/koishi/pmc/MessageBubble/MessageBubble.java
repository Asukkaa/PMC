package priv.koishi.pmc.MessageBubble;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * 消息气泡组件
 *
 * @author KOISHI
 * Date:2024-11-28
 * Time:下午5:49
 */
public class MessageBubble extends Label {

    /**
     * 消息气泡
     *
     * @param text 消息气泡要展示的消息
     */
    public MessageBubble(String text) {
        setText(text);
        setTextFill(Color.WHITE);
        setPadding(new Insets(10));
        setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-background-radius: 5; -fx-background-color: #201f1f;-fx-opacity: 0.8;");
    }

}
