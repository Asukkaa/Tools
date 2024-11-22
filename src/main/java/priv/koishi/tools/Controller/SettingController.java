package priv.koishi.tools.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.FileUtils.updateProperties;

/**
 * @author KOISHI
 * Date:2024-11-12
 * Time:下午4:51
 */
public class SettingController {

    /**
     * excel插入图片功能配置文件路径
     */
    static final String configFile_Img = "config/imgToExcelConfig.properties";

    /**
     * 重命名功能配置文件路径
     */
    static final String configFile_Rename = "config/fileRenameConfig.properties";

    /**
     * 统计文件数量功能配置文件路径
     */
    static final String configFile_Num = "config/fileNumToExcelConfig.properties";

    /**
     * 读取文件名称功能配置文件路径
     */
    static final String configFile_Name = "config/fileNameToExcelConfig.properties";

    @FXML
    private VBox vBox_set;

    @FXML
    private Label mail_set, massage_set;

    @FXML
    private CheckBox loadRename_Set, loadFileNum_Set, loadFileName_Set, loadImgToExcel_Set;

    /**
     * 组件自适应宽高
     */
    public static void settingAdaption(Stage stage, Scene scene) {
        //设置组件宽度
        double stageWidth = stage.getWidth();
        Node settingVBox = scene.lookup("#vBox_set");
        settingVBox.setLayoutX(stageWidth * 0.03);
    }

    /**
     * 根据是否加载最后一次功能选项框选择值更新相关配置文件
     */
    private void setLoadLastConfigCheckBox(CheckBox checkBox, String configFile) throws IOException {
        if (checkBox.isSelected()) {
            updateProperties(configFile, key_loadLastConfig, activation);
        } else {
            updateProperties(configFile, key_loadLastConfig, unActivation);
        }
    }

    /**
     * 设置是否加载最后一次功能配置信息初始值
     */
    private void setLoadLastConfig(Properties prop, CheckBox checkBox, String configFile) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        checkBox.setSelected(activation.equals(prop.getProperty(key_loadLastConfig)));
        input.close();
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() throws IOException {
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
        //设置是否加载最后一次功能配置信息初始值
        Properties prop = new Properties();
        setLoadLastConfig(prop, loadRename_Set, configFile_Rename);
        setLoadLastConfig(prop, loadFileNum_Set, configFile_Num);
        setLoadLastConfig(prop, loadFileName_Set, configFile_Name);
        setLoadLastConfig(prop, loadImgToExcel_Set, configFile_Img);
    }

    /**
     * 按指定规则批量重命名文件功能加载上次设置信息
     */
    @FXML
    private void loadRenameAction() throws IOException {
        setLoadLastConfigCheckBox(loadRename_Set, configFile_Rename);
    }

    /**
     * 分组统计文件夹下文件数量功能加载上次设置信息
     */
    @FXML
    private void loadFileNumAction() throws IOException {
        setLoadLastConfigCheckBox(loadFileNum_Set, configFile_Num);
    }

    /**
     * 获取文件夹下的文件名称功能加载上次设置信息
     */
    @FXML
    private void loadFileNameAction() throws IOException {
        setLoadLastConfigCheckBox(loadFileName_Set, configFile_Name);
    }

    /**
     * 将图片与excel匹配并插入功能加载上次设置信息
     */
    @FXML
    private void loadImgToExcelAction() throws IOException {
        setLoadLastConfigCheckBox(loadImgToExcel_Set, configFile_Img);
    }

}
