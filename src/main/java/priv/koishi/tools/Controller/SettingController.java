package priv.koishi.tools.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.TabBean;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.CommonUtils.*;
import static priv.koishi.tools.Utils.FileUtils.getUnitSize;
import static priv.koishi.tools.Utils.FileUtils.updateProperties;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 设置页面控制器
 *
 * @author KOISHI
 * Date:2024-11-12
 * Time:下午4:51
 */
public class SettingController {

    /**
     * 启动脚本设置的最大内存值
     */
    private static String scriptMemory;

    /**
     * app.bat最大内存设置参数
     */
    private static final String Xmx = "-Xmx";

    /**
     * 启动脚本名称
     */
    private static final String scriptName = getScriptName();

    @FXML
    private TableView<TabBean> tableView_Set;

    @FXML
    private TableColumn<TabBean, String> tabName_Set;

    @FXML
    private TableColumn<TabBean, CheckBox> activationCheckBox_Set;

    @FXML
    private AnchorPane anchorPane_Set;

    @FXML
    private VBox vBox_Set;

    @FXML
    private Button reLaunch_Set;

    @FXML
    private ChoiceBox<String> sort_Set;

    @FXML
    private TextField nextRunMemory_Set;

    @FXML
    private Label runningMemory_Set, thisPath_Set, systemMemory_Set;

    @FXML
    private CheckBox loadRename_Set, loadFileNum_Set, loadFileName_Set, loadImgToExcel_Set, lastTab_Set, fullWindow_Set, reverseSort_Set, loadAutoClick_Set;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void settingAdaption(Stage stage) {
        Scene scene = stage.getScene();
        // 设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Set");
        table.setPrefHeight(stageHeight * 0.3);
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.5;
        table.setMaxWidth(tableWidth);
        Node settingVBox = scene.lookup("#vBox_Set");
        settingVBox.setLayoutX(stageWidth * 0.03);
        Node tabName = scene.lookup("#tabName_Set");
        tabName.setStyle("-fx-pref-width: " + tableWidth * 0.7 + "px;");
        Node tabState = scene.lookup("#activationCheckBox_Set");
        tabState.setStyle("-fx-pref-width: " + tableWidth * 0.3 + "px;");
    }

    /**
     * 保存设置
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    public static void saveSetting(Scene scene) throws IOException {
        // 保存最大运行内存设置
        saveMemorySetting(scene);
        // 保存页面开启状态与展示顺序设置
        saveTabIds(scene);
    }

    /**
     * 保存页面开启状态与展示顺序设置
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    @SuppressWarnings("unchecked")
    private static void saveTabIds(Scene scene) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        TableView<TabBean> tableView = (TableView<TabBean>) scene.lookup("#tableView_Set");
        List<String> tabIds = new ArrayList<>();
        for (TabBean tabBean : tableView.getItems()) {
            String tabId = tabBean.getTabId();
            String activationState = tabBean.getActivationCheckBox().isSelected() ? activation : unActivation;
            String tabStateId = tabId + "." + activationState;
            tabIds.add(tabStateId);
        }
        prop.setProperty("tabIds", String.join(" ", tabIds));
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
    }

    /**
     * 获取运行脚本名称
     *
     * @return 根据不同操作系统返回不同脚本名称
     */
    private static String getScriptName() {
        if (systemName.contains(macos)) {
            return "Tools";
        }
        return "app.bat";
    }

    /**
     * 保存最大运行内存设置
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    private static void saveMemorySetting(Scene scene) throws IOException {
        TextField nextRunMemoryTextField = (TextField) scene.lookup("#nextRunMemory_Set");
        String nextRunMemoryValue = nextRunMemoryTextField.getText();
        Label thisPath = (Label) scene.lookup("#thisPath_Set");
        Path scriptFilePath = Paths.get(thisPath.getText() + File.separator + scriptName);
        List<String> lines = Files.readAllLines(scriptFilePath);
        String newLineContent = Xmx + nextRunMemoryValue;
        if (StringUtils.isNotBlank(nextRunMemoryValue)) {
            if (scriptMemory == null) {
                newLineContent = text_VMOptions + newLineContent + g;
                writeMemorySetting(scriptFilePath, lines, newLineContent, text_VMOptions);
            } else if (!nextRunMemoryValue.equals(scriptMemory)) {
                String originalLineContent = Xmx + scriptMemory;
                writeMemorySetting(scriptFilePath, lines, newLineContent, originalLineContent);
            }
        }
    }

    /**
     * 写入最大运行内存设置
     *
     * @param scriptFilePath      启动脚本路径
     * @param lines               启动脚本内容
     * @param newLineContent      修改后的值
     * @param originalLineContent 修改前的值
     * @throws IOException io异常
     */
    private static void writeMemorySetting(Path scriptFilePath, List<String> lines, String newLineContent, String originalLineContent) throws IOException {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains(originalLineContent)) {
                lines.set(i, line.replace(originalLineContent, newLineContent));
                break;
            }
        }
        Files.write(scriptFilePath, lines);
    }

    /**
     * 根据是否加载最后一次功能选项框选择值更新相关配置文件
     *
     * @param checkBox   更改配置的选项框
     * @param configFile 要更新的配置文件相对路径
     * @param key        要更新的配置
     * @throws IOException io异常
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
     *
     * @param prop       要读取的配置文件对象
     * @param checkBox   更改配置的选项框
     * @param configFile 要更新的配置文件相对路径
     * @throws IOException io异常
     */
    private void setLoadLastConfig(Properties prop, CheckBox checkBox, String configFile) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        setControlLastConfig(checkBox, prop, key_loadLastConfig, false, null);
        input.close();
    }

    /**
     * 读取配置文件
     *
     * @param prop 要读取的配置文件对象
     * @throws IOException io异常
     */
    private void getConfig(Properties prop) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        setControlLastConfig(fullWindow_Set, prop, key_loadLastFullWindow, false, null);
        setControlLastConfig(lastTab_Set, prop, key_loadLastConfig, false, null);
        setControlLastConfig(reverseSort_Set, prop, key_reverseSort, false, null);
        setControlLastConfig(sort_Set, prop, key_sort, false, null);
        input.close();
    }

    /**
     * 设置是否加载最后一次功能配置信息初始值
     *
     * @throws IOException io异常
     */
    private void setLoadLastConfigs() throws IOException {
        Properties prop = new Properties();
        setLoadLastConfig(prop, loadRename_Set, configFile_Rename);
        setLoadLastConfig(prop, loadFileNum_Set, configFile_Num);
        setLoadLastConfig(prop, loadFileName_Set, configFile_Name);
        setLoadLastConfig(prop, loadImgToExcel_Set, configFile_Img);
        setLoadLastConfig(prop, loadAutoClick_Set, configFile_Click);
        getConfig(prop);
    }

    /**
     * 获取最大运行内存并展示
     *
     * @throws IOException io异常
     */
    private void getMaxMemory() throws IOException {
        long maxMemory = Runtime.getRuntime().maxMemory();
        runningMemory_Set.setText(getUnitSize(maxMemory, false));
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = ((com.sun.management.OperatingSystemMXBean) osBean).getTotalMemorySize();
        String systemUnitSizeMemory = getUnitSize(totalMemory, false);
        String memoryUnit = systemUnitSizeMemory.substring(systemUnitSizeMemory.lastIndexOf(" ") + 1);
        // 获取操作系统内存整数部分作为下次启动时程序分配最大内存的限制
        int systemMemoryValue = (int) Double.parseDouble(systemUnitSizeMemory.substring(0, systemUnitSizeMemory.lastIndexOf(" ")));
        if (TB.equals(memoryUnit)) {
            systemMemoryValue = systemMemoryValue * 1024;
        } else if (!GB.equals(memoryUnit)) {
            systemMemoryValue = 1;
        }
        systemMemory_Set.setText(systemUnitSizeMemory);
        setPathLabel(thisPath_Set, currentDir, false, anchorPane_Set);
        String scriptPath = currentDir + File.separator + scriptName;
        addValueToolTip(nextRunMemory_Set, tip_defaultNextRunMemory, text_nowValue);
        // 下次运行的最大内存输入监听
        integerRangeTextField(nextRunMemory_Set, 1, systemMemoryValue, tip_defaultNextRunMemory);
        BufferedReader reader = new BufferedReader(new FileReader(scriptPath));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(Xmx)) {
                scriptMemory = line.substring(line.lastIndexOf(Xmx) + Xmx.length(), line.lastIndexOf(g));
                nextRunMemory_Set.setText(scriptMemory);
                addValueToolTip(nextRunMemory_Set, text_nowSetting + scriptMemory + text_memorySetting, text_nowValue);
                // 下次运行的最大内存输入监听
                integerRangeTextField(nextRunMemory_Set, 1, systemMemoryValue, text_nowSetting + scriptMemory + text_memorySetting);
                break;
            }
        }
        reader.close();
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_sort, sort_Set);
        addToolTip(tip_reLaunch, reLaunch_Set);
        addToolTip(tip_reverseSort, reverseSort_Set);
        addToolTip(lastTab_Set.getText(), lastTab_Set);
        addToolTip(fullWindow_Set.getText(), fullWindow_Set);
        addToolTip(loadRename_Set.getText(), loadRename_Set);
        addToolTip(loadFileNum_Set.getText(), loadFileNum_Set);
        addToolTip(loadFileName_Set.getText(), loadFileName_Set);
        addToolTip(loadImgToExcel_Set.getText(), loadImgToExcel_Set);
    }

    /**
     * 设置列表各列宽度
     */
    private void bindPrefWidthProperty() {
        tabName_Set.prefWidthProperty().bind(tableView_Set.widthProperty().multiply(0.7));
        activationCheckBox_Set.prefWidthProperty().bind(tableView_Set.widthProperty().multiply(0.3));
    }

    /**
     * 界面初始化
     *
     * @throws IOException io异常
     */
    @FXML
    private void initialize() throws IOException {
        // 设置列表各列宽度
        bindPrefWidthProperty();
        // 设置是否加载最后一次功能配置信息初始值
        setLoadLastConfigs();
        // 获取最大运行内存并展示
        getMaxMemory();
        // 设置鼠标悬停提示
        setToolTip();
    }

    /**
     * 按指定规则批量重命名文件功能加载上次设置信息
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadRenameAction() throws IOException {
        setLoadLastConfigCheckBox(loadRename_Set, configFile_Rename, key_loadLastConfig);
    }

    /**
     * 分组统计文件夹下文件数量功能加载上次设置信息
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFileNumAction() throws IOException {
        setLoadLastConfigCheckBox(loadFileNum_Set, configFile_Num, key_loadLastConfig);
    }

    /**
     * 获取文件夹下的文件名称功能加载上次设置信息
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFileNameAction() throws IOException {
        setLoadLastConfigCheckBox(loadFileName_Set, configFile_Name, key_loadLastConfig);
    }

    /**
     * 将图片与excel匹配并插入功能加载上次设置信息
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadImgToExcelAction() throws IOException {
        setLoadLastConfigCheckBox(loadImgToExcel_Set, configFile_Img, key_loadLastConfig);
    }

    /**
     * 自动操作工具功能加载上次设置信息
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadAutoClickAction() throws IOException {
        setLoadLastConfigCheckBox(loadAutoClick_Set, configFile_Click, key_loadLastConfig);
    }

    /**
     * 记住关闭前打开的页面设置
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadLastTabAction() throws IOException {
        setLoadLastConfigCheckBox(lastTab_Set, configFile, key_loadLastConfig);
    }

    /**
     * 记住窗口是否最大化设置
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFullWindowAction() throws IOException {
        setLoadLastConfigCheckBox(fullWindow_Set, configFile, key_loadLastFullWindow);
    }

    /**
     * 倒序排序设置
     *
     * @throws IOException io异常
     */
    @FXML
    private void reverseSorAction() throws IOException {
        setLoadLastConfigCheckBox(reverseSort_Set, configFile, key_reverseSort);
    }

    /**
     * 重启程序按钮
     *
     * @throws IOException io异常
     */
    @FXML
    private void reLaunch() throws IOException {
        Platform.exit();
        if (!isRunningFromJar()) {
            ProcessBuilder processBuilder = null;
            if (systemName.contains(win)) {
                String path = currentDir.substring(0, currentDir.lastIndexOf(Tools) + Tools.length());
                String appPath = path + File.separator + "Tools.exe";
                processBuilder = new ProcessBuilder(appPath);
            } else if (systemName.contains(macos)) {
                String appName = File.separator + "Tools.app";
                String appPath = currentDir.substring(0, currentDir.lastIndexOf(appName)) + appName;
                processBuilder = new ProcessBuilder("open", "-n", appPath);
            }
            if (processBuilder != null) {
                processBuilder.start();
            }
        }
    }

    /**
     * 文件查询默认排序设置监听
     *
     * @throws IOException io异常
     */
    @FXML
    private void sortAction() throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        prop.setProperty(key_sort, sort_Set.getValue());
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
    }

}
