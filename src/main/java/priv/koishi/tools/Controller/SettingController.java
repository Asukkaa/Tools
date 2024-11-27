package priv.koishi.tools.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.isRunningFromJar;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * @author KOISHI
 * Date:2024-11-12
 * Time:下午4:51
 */
public class SettingController {

    /**
     * app.bat设置的最大内存值
     */
    static String batMemory;

    /**
     * app.bat最大内存设置参数
     */
    static final String Xmx = "-Xmx";

    static final String batName = "app.bat";

    static final String bin = File.separator + "bin";

    static final String runtime = File.separator + "runtime";

    static final String logs = File.separator + "logs";

    /**
     * 当前程序运行位置
     */
    static final String currentDir = System.getProperty("user.dir");

    @FXML
    private VBox vBox_Set;

    @FXML
    private TextField batMemory_Set;

    @FXML
    private Label mail_set, massage_set, memory_Set, thisPath_Set, batPath_Set, logsPath_Set;

    @FXML
    private CheckBox loadRename_Set, loadFileNum_Set, loadFileName_Set, loadImgToExcel_Set, lastTab_Set, fullWindow_Set;

    /**
     * 组件自适应宽高
     */
    public static void settingAdaption(Stage stage, Scene scene) {
        //设置组件宽度
        double stageWidth = stage.getWidth();
        Node settingVBox = scene.lookup("#vBox_Set");
        settingVBox.setLayoutX(stageWidth * 0.03);
    }

    /**
     * 保存最大运行内存设置
     */
    public static void saveMemorySetting(Scene scene) {
        TextField batMemoryTextField = (TextField) scene.lookup("#batMemory_Set");
        String batMemoryValue = batMemoryTextField.getText();
        if (StringUtils.isNotBlank(batMemoryValue) && !batMemoryValue.equals(batMemory)) {
            Label batPath = (Label) scene.lookup("#batPath_Set");
            Path batFilePath = Paths.get(batPath.getText());
            String originalLineContent = Xmx + batMemory;
            String newLineContent = Xmx + batMemoryValue;
            try {
                List<String> lines = Files.readAllLines(batFilePath);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (line.contains(originalLineContent)) {
                        lines.set(i, line.replace(originalLineContent, newLineContent));
                        break;
                    }
                }
                Files.write(batFilePath, lines);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据是否加载最后一次功能选项框选择值更新相关配置文件
     */
    private void setLoadLastConfigCheckBox(CheckBox checkBox, String configFile, String key) throws IOException {
        if (checkBox.isSelected()) {
            updateProperties(configFile, key, activation);
        } else {
            updateProperties(configFile, key, unActivation);
        }
    }

    /**
     * 设置是否加载最后一次功能配置信息初始值
     */
    private void setLoadLastConfig(Properties prop, CheckBox checkBox, String configFile, String key) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        checkBox.setSelected(activation.equals(prop.getProperty(key)));
        input.close();
    }

    /**
     * 添加复制邮件右键菜单
     */
    private void setCopyMailContextMenu() {
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

    /**
     * 设置是否加载最后一次功能配置信息初始值
     */
    private void setLoadLastConfigs() throws IOException {
        Properties prop = new Properties();
        setLoadLastConfig(prop, loadRename_Set, configFile_Rename, key_loadLastConfig);
        setLoadLastConfig(prop, loadFileNum_Set, configFile_Num, key_loadLastConfig);
        setLoadLastConfig(prop, loadFileName_Set, configFile_Name, key_loadLastConfig);
        setLoadLastConfig(prop, loadImgToExcel_Set, configFile_Img, key_loadLastConfig);
        setLoadLastConfig(prop, lastTab_Set, configFile, key_loadLastConfig);
        setLoadLastConfig(prop, fullWindow_Set, configFile, key_loadLastFullWindow);
    }

    /**
     * 获取最大运行内存并展示
     */
    private void getMaxMemory() throws IOException {
        long maxMemory = Runtime.getRuntime().maxMemory();
        memory_Set.setText(getUnitSize(maxMemory));
        addToolTip(memory_Set, "最大运行内存默认为系统内存四分之一，无法直接修改，如需修改则需编辑 app.bat 脚本后用该脚本运行");
        setPathLabel(thisPath_Set, currentDir, currentDir);
        String batPath;
        if ("bin".equals(getFileName(new File(currentDir))) || isRunningFromJar()) {
            batPath = currentDir + File.separator + batName;
        } else {
            batPath = currentDir + runtime + bin + File.separator + batName;
        }
        setPathLabel(batPath_Set, batPath, new File(batPath).getParent());
        try (BufferedReader reader = new BufferedReader(new FileReader(batPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(Xmx)) {
                    batMemory = line.substring(line.lastIndexOf(Xmx) + Xmx.length(), line.lastIndexOf("g"));
                    batMemory_Set.setText(batMemory);
                    addToolTip(batMemory_Set, "当前配置值为 " + batMemory +
                            " GB ，关闭程序即可保存修改，之后使用 " + batName + " 启动程序即可生效\n当前所填值为 " + batMemory);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取logs文件夹路径并展示
     */
    private void setLogsPath() {
        String logsPath = currentDir + logs;
        setPathLabel(logsPath_Set, logsPath, logsPath);
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() throws IOException {
        //添加右键菜单
        setCopyMailContextMenu();
        //设置是否加载最后一次功能配置信息初始值
        setLoadLastConfigs();
        //获取最大运行内存并展示
        getMaxMemory();
        //获取logs文件夹路径并展示
        setLogsPath();
    }

    /**
     * 按指定规则批量重命名文件功能加载上次设置信息
     */
    @FXML
    private void loadRenameAction() throws IOException {
        setLoadLastConfigCheckBox(loadRename_Set, configFile_Rename, key_loadLastConfig);
    }

    /**
     * 分组统计文件夹下文件数量功能加载上次设置信息
     */
    @FXML
    private void loadFileNumAction() throws IOException {
        setLoadLastConfigCheckBox(loadFileNum_Set, configFile_Num, key_loadLastConfig);
    }

    /**
     * 获取文件夹下的文件名称功能加载上次设置信息
     */
    @FXML
    private void loadFileNameAction() throws IOException {
        setLoadLastConfigCheckBox(loadFileName_Set, configFile_Name, key_loadLastConfig);
    }

    /**
     * 将图片与excel匹配并插入功能加载上次设置信息
     */
    @FXML
    private void loadImgToExcelAction() throws IOException {
        setLoadLastConfigCheckBox(loadImgToExcel_Set, configFile_Img, key_loadLastConfig);
    }

    /**
     * 记住关闭前打开的页面设置
     */
    @FXML
    private void loadLastTabAction() throws IOException {
        setLoadLastConfigCheckBox(lastTab_Set, configFile, key_loadLastConfig);
    }

    /**
     * 记住窗口是否最大化设置
     */
    @FXML
    private void loadFullWindowAction() throws IOException {
        setLoadLastConfigCheckBox(fullWindow_Set, configFile, key_loadLastFullWindow);
    }

    /**
     * app.bat 分配的最大内存输入监听
     */
    @FXML
    private void batMemoryKeyTyped(KeyEvent event) {
        integerRangeTextField(batMemory_Set, 1, null, event);
        addToolTip(batMemory_Set, "当前配置值为 " + batMemory + " GB ，关闭程序即可保存修改，之后使用 app.bat 启动程序即可生效\n当前所填值为 " + batMemory_Set.getText());
    }

}
