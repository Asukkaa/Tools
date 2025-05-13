package priv.koishi.tools.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.TabBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.*;

import static priv.koishi.tools.Controller.MainController.saveAllLastConfig;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.CommonUtils.getCurrentGCType;
import static priv.koishi.tools.Utils.FileUtils.*;
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
     * 程序主舞台
     */
    private Stage mainStage;

    @FXML
    private AnchorPane anchorPane_Set;

    @FXML
    private Button reLaunch_Set;

    @FXML
    private ChoiceBox<String> sort_Set;

    @FXML
    private TextField nextRunMemory_Set;

    @FXML
    private ChoiceBox<String> nextGcType_Set;

    @FXML
    private TableView<TabBean> tableView_Set;

    @FXML
    private TableColumn<TabBean, String> tabName_Set;

    @FXML
    private TableColumn<TabBean, CheckBox> activationCheckBox_Set;

    @FXML
    private Label runningMemory_Set, thisPath_Set, systemMemory_Set, gcType_Set;

    @FXML
    private CheckBox loadRename_Set, loadFileNum_Set, loadFileName_Set, loadImgToExcel_Set, lastTab_Set,
            fullWindow_Set, reverseSort_Set, loadAutoClick_Set, maxWindow_Set;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void adaption(Stage stage) {
        Scene scene = stage.getScene();
        // 设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Set");
        table.setPrefHeight(stageHeight * 0.2);
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.5;
        table.setMaxWidth(tableWidth);
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
    public static void saveLastConfig(Scene scene) throws IOException {
        // 保存jvm设置
        saveJVMConfig(scene);
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
        prop.setProperty(key_tabIds, String.join(" ", tabIds));
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
    }

    /**
     * 保存JVM参数设置
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    private static void saveJVMConfig(Scene scene) throws IOException {
        TextField nextRunMemory = (TextField) scene.lookup("#nextRunMemory_Set");
        String nextRunMemoryValue = nextRunMemory.getText();
        String XmxValue = StringUtils.isBlank(nextRunMemoryValue) ? "" : nextRunMemoryValue + G;
        ChoiceBox<?> nextGcType = (ChoiceBox<?>) scene.lookup("#nextGcType_Set");
        String nextGcTypeValue = (String) nextGcType.getValue();
        Map<String, String> options = new HashMap<>();
        options.put(Xmx, XmxValue);
        options.put(XX, nextGcTypeValue);
        // 更新cfg文件中jvm参数设置
        setJavaOptionValue(options);
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
        setControlLastConfig(checkBox, prop, key_loadLastConfig);
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
        setControlLastConfig(sort_Set, prop, key_sort);
        setControlLastConfig(lastTab_Set, prop, key_loadLastConfig);
        setControlLastConfig(reverseSort_Set, prop, key_reverseSort);
        setControlLastConfig(maxWindow_Set, prop, key_loadLastMaxWindow);
        setControlLastConfig(fullWindow_Set, prop, key_loadLastFullWindow);
        input.close();
    }

    /**
     * 设置是否加载最后一次功能配置信息初始值
     *
     * @throws IOException io异常
     */
    private void setLoadLastConfigs() throws IOException {
        Properties prop = new Properties();
        setLoadLastConfig(prop, loadFileNum_Set, configFile_Num);
        setLoadLastConfig(prop, loadFileName_Set, configFile_Name);
        setLoadLastConfig(prop, loadRename_Set, configFile_Rename);
        setLoadLastConfig(prop, loadImgToExcel_Set, configFile_Img);
        setLoadLastConfig(prop, loadAutoClick_Set, configFile_Click);
        getConfig(prop);
    }

    /**
     * 获取JVM设置并展示
     *
     * @throws IOException io异常
     */
    private void getJVMConfig() throws IOException {
        // 限制下次运行内存文本输入框内容
        integerRangeTextField(nextRunMemory_Set, 1, null, tip_nextRunMemory);
        // 获取当前运行路径
        setPathLabel(thisPath_Set, getAppPath());
        long maxMemory = Runtime.getRuntime().maxMemory();
        runningMemory_Set.setText(getUnitSize(maxMemory, false));
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = ((com.sun.management.OperatingSystemMXBean) osBean).getTotalMemorySize();
        String systemUnitSizeMemory = getUnitSize(totalMemory, false);
        systemMemory_Set.setText(systemUnitSizeMemory);
        Map<String, String> jvm = getJavaOptionValue(jvmArgs);
        String xmxValue = jvm.get(Xmx);
        if (StringUtils.isNotBlank(xmxValue)) {
            nextRunMemory_Set.setText(xmxValue.substring(0, xmxValue.indexOf(G)));
        }
        gcType_Set.setText(getCurrentGCType());
        String gcType = jvm.get(XX);
        if (StringUtils.isNotBlank(gcType)) {
            nextGcType_Set.setValue(gcType);
        }
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
        addValueToolTip(nextRunMemory_Set, tip_nextRunMemory);
        addToolTip(loadFileNum_Set.getText(), loadFileNum_Set);
        addToolTip(loadFileName_Set.getText(), loadFileName_Set);
        addToolTip(loadImgToExcel_Set.getText(), loadImgToExcel_Set);
        addValueToolTip(nextGcType_Set, tip_nextGcType, nextGcType_Set.getValue());
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
        getJVMConfig();
        // 设置鼠标悬停提示
        setToolTip();
        Platform.runLater(() -> {
            Scene mainScene = anchorPane_Set.getScene();
            mainStage = (Stage) mainScene.getWindow();
        });
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
     * 记住窗口是否全屏设置
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadFullWindowAction() throws IOException {
        setLoadLastConfigCheckBox(fullWindow_Set, configFile, key_loadLastFullWindow);
    }

    /**
     * 记住窗口是否最大化设置
     *
     * @throws IOException io异常
     */
    @FXML
    private void loadMaxWindowAction() throws IOException {
        setLoadLastConfigCheckBox(maxWindow_Set, configFile, key_loadLastMaxWindow);
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
        // 重启前需要保存设置，如果只使用关闭方法中的保存功能可能无法及时更新jvm配置参数
        saveAllLastConfig(mainStage);
        Platform.exit();
        if (!isRunningFromJar()) {
            ProcessBuilder processBuilder = null;
            if (systemName.contains(win)) {
                processBuilder = new ProcessBuilder(getAppPath());
            } else if (systemName.contains(mac)) {
                processBuilder = new ProcessBuilder("open", "-n", getAppPath());
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
