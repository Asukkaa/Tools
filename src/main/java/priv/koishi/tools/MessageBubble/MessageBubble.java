package priv.koishi.tools.MessageBubble;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * @author KOISHI
 * Date:2024-11-28
 * Time:下午5:49
 */
public class MessageBubble extends Label {

    /**
     * 消息气泡
     */
    public MessageBubble(String text) {
        setText(text);
        setTextFill(Color.WHITE);
        setPadding(new Insets(10));
        setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-background-radius: 5; -fx-background-color: #201f1f;-fx-opacity: 0.8;");
    }

}
