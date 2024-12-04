package priv.koishi.tools.Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.*;
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

    /**
     * 启动脚本名称
     */
    static final String batName = "app.bat";

    /**
     * 启动脚本bin路径
     */
    static final String bin = File.separator + "bin";

    /**
     * 启动脚本runtime路径
     */
    static final String runtime = File.separator + "runtime";

    /**
     * logs路径
     */
    static final String logs = File.separator + "logs";

    /**
     * 当前程序运行位置
     */
    static final String currentDir = System.getProperty("user.dir");

    public static final String nowSetting = "当前配置值为 ";

    public static final String memorySetting = " GB ，关闭程序即可保存修改，之后使用 ";

    public static final String nowValue = " 启动程序即可生效\n当前所填值为 ";

    @FXML
    private AnchorPane anchorPane_Set;

    @FXML
    private VBox vBox_Set;

    @FXML
    private TextField batMemory_Set, logsNum_Set;

    @FXML
    private Label mail_set, memory_Set, thisPath_Set, batPath_Set, logsPath_Set;

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
     * 保存设置
     */
    public static void saveSetting(Scene scene) throws IOException {
        //保存最大运行内存设置
        saveMemorySetting(scene);
        //保存日志问文件数量设置
        saveLogsNumSetting(scene);
    }

    /**
     * 保存日志问文件数量设置
     */
    private static void saveLogsNumSetting(Scene scene) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        TextField logsNumTextField = (TextField) scene.lookup("#logsNum_Set");
        String logsNumValue = logsNumTextField.getText();
        prop.setProperty(key_logsNum, logsNumValue);
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
    }

    /**
     * 保存最大运行内存设置
     */
    private static void saveMemorySetting(Scene scene) throws IOException {
        TextField batMemoryTextField = (TextField) scene.lookup("#batMemory_Set");
        String batMemoryValue = batMemoryTextField.getText();
        if (StringUtils.isNotBlank(batMemoryValue) && !batMemoryValue.equals(batMemory)) {
            Label batPath = (Label) scene.lookup("#batPath_Set");
            Path batFilePath = Paths.get(batPath.getText());
            String originalLineContent = Xmx + batMemory;
            String newLineContent = Xmx + batMemoryValue;
            List<String> lines = Files.readAllLines(batFilePath);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains(originalLineContent)) {
                    lines.set(i, line.replace(originalLineContent, newLineContent));
                    break;
                }
            }
            Files.write(batFilePath, lines);
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
    private void setLoadLastConfig(Properties prop, CheckBox checkBox, String configFile) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        checkBox.setSelected(activation.equals(prop.getProperty(key_loadLastConfig)));
        input.close();
    }

    /**
     * 读取配置文件
     */
    private void getConfig(Properties prop) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        lastTab_Set.setSelected(activation.equals(prop.getProperty(key_loadLastConfig)));
        fullWindow_Set.setSelected(activation.equals(prop.getProperty(key_loadLastFullWindow)));
        setControlLastConfig(logsNum_Set, prop, key_logsNum, false, null);
        input.close();
    }

    /**
     * 设置是否加载最后一次功能配置信息初始值
     */
    private void setLoadLastConfigs() throws IOException {
        Properties prop = new Properties();
        setLoadLastConfig(prop, loadRename_Set, configFile_Rename);
        setLoadLastConfig(prop, loadFileNum_Set, configFile_Num);
        setLoadLastConfig(prop, loadFileName_Set, configFile_Name);
        setLoadLastConfig(prop, loadImgToExcel_Set, configFile_Img);
        getConfig(prop);
    }

    /**
     * 获取最大运行内存并展示
     */
    private void getMaxMemory() throws IOException {
        long maxMemory = Runtime.getRuntime().maxMemory();
        memory_Set.setText(getUnitSize(maxMemory));
        addToolTip(memory_Set, "最大运行内存默认为系统内存四分之一，无法直接修改，如需修改则需编辑 app.bat 脚本后用该脚本运行");
        setPathLabel(thisPath_Set, currentDir, false, anchorPane_Set);
        String batPath;
        if ("bin".equals(getFileName(new File(currentDir))) || isRunningFromJar()) {
            batPath = currentDir + File.separator + batName;
        } else {
            batPath = currentDir + runtime + bin + File.separator + batName;
        }
        setPathLabel(batPath_Set, batPath, false, anchorPane_Set);
        BufferedReader reader = new BufferedReader(new FileReader(batPath));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(Xmx)) {
                batMemory = line.substring(line.lastIndexOf(Xmx) + Xmx.length(), line.lastIndexOf("g"));
                batMemory_Set.setText(batMemory);
                addToolTip(batMemory_Set, nowSetting + batMemory + memorySetting + batName + nowValue + batMemory);
                break;
            }
        }
        reader.close();
    }

    /**
     * 获取logs文件夹路径并展示
     */
    private void setLogsPath() {
        String logsPath = currentDir + logs;
        setPathLabel(logsPath_Set, logsPath, false, anchorPane_Set);
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        //app.bat 分配的最大内存输入监听
        integerRangeTextField(batMemory_Set, 1, null, nowSetting + batMemory + memorySetting + batName + nowValue + batMemory_Set.getText());
        //log 文件保留数量输入监听
        integerRangeTextField(logsNum_Set, 0, null, tip_logsNum);
    }

    /**
     * 清理多余log文件
     */
    private void deleteLogs() {
        File[] files = new File(logsPath_Set.getText()).listFiles();
        if (files != null) {
            List<File> logList = new ArrayList<>();
            for (File file : files) {
                if (log.equals(getFileType(file))) {
                    logList.add(file);
                }
            }
            int logsNum = Integer.parseInt(logsNum_Set.getText());
            if (logList.size() > logsNum) {
                logList.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                List<File> removeList = logList.stream().skip(logsNum).toList();
                removeList.forEach(r -> {
                    String path = r.getAbsolutePath();
                    File file = new File(path);
                    if (!file.delete()) {
                        throw new RuntimeException("日志文件 " + path + " 删除失败");
                    }
                });
            }
        }
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() throws IOException {
        //添加右键菜单
        setCopyValueContextMenu(mail_set, "复制反馈邮件", anchorPane_Set);
        //给输入框添加内容变化监听
        textFieldChangeListener();
        //设置是否加载最后一次功能配置信息初始值
        setLoadLastConfigs();
        //获取logs文件夹路径并展示
        setLogsPath();
        //获取最大运行内存并展示
        getMaxMemory();
        //清理多余log文件
        deleteLogs();
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

}
