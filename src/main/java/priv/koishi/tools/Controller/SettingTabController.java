package priv.koishi.tools.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * @author KOISHI
 * Date:2024-11-12
 * Time:下午4:51
 */
public class SettingTabController {

    @FXML
    private VBox vBox_set;

    @FXML
    private Label mail_set, massage_set;

    /**
     * 组件自适应宽高
     */
    public static void settingToExcelAdaption(Stage stage, Scene scene) {
        //设置组件宽度
        double stageWidth = stage.getWidth();
        Node settingVBox = scene.lookup("#vBox_set");
        settingVBox.setLayoutX(stageWidth * 0.03);
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        //添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyMailMenuItem = new MenuItem("复制反馈邮件");
        contextMenu.getItems().add(copyMailMenuItem);
        mail_set.setContextMenu(contextMenu);
        mail_set.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(mail_set, event.getScreenX(), event.getScreenY());
            }
        });
        //设置右键菜单行为
        copyMailMenuItem.setOnAction(event -> {
            // 获取当前系统剪贴板
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            // 将文本转换为Transferable对象
            StringSelection stringSelection = new StringSelection(mail_set.getText());
            // 将Transferable对象设置到剪贴板中
            clipboard.setContents(stringSelection, null);
            massage_set.setVisible(true);
            // 设置几秒后隐藏按钮
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.6), ae -> massage_set.setVisible(false));
            Timeline timeline = new Timeline(keyFrame);
            timeline.play();
        });
    }

}
