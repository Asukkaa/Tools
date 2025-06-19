package priv.koishi.tools.Controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.TabBean;
import priv.koishi.tools.EventBus.EventBus;
import priv.koishi.tools.EventBus.MainLoadedEvent;
import priv.koishi.tools.EventBus.SettingsLoadedEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.*;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.MainApplication.mainController;
import static priv.koishi.tools.MainApplication.mainStage;
import static priv.koishi.tools.Utils.CommonUtils.getCurrentGCType;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.FileUtils.isRunningFromJar;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 设置页面控制器
 *
 * @author KOISHI
 * Date:2024-11-12
 * Time:下午4:51
 */
public class SettingController extends RootController {

    @FXML
    public AnchorPane anchorPane_Set;

    @FXML
    public Button reLaunch_Set;

    @FXML
    public ChoiceBox<String> sort_Set;

    @FXML
    public TextField nextRunMemory_Set;

    @FXML
    public ChoiceBox<String> nextGcType_Set;

    @FXML
    public TableView<TabBean> tableView_Set;

    @FXML
    public TableColumn<TabBean, String> tabName_Set;

    @FXML
    public TableColumn<TabBean, CheckBox> activationCheckBox_Set;

    @FXML
    public Label runningMemory_Set, thisPath_Set, systemMemory_Set, gcType_Set;

    @FXML
    public CheckBox loadRename_Set, loadFileNum_Set, loadFileName_Set, loadImgToExcel_Set, lastTab_Set,
            fullWindow_Set, reverseSort_Set, loadAutoClick_Set, maxWindow_Set;

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tableView_Set.setPrefHeight(stageHeight * 0.2);
        // 设置组件宽度
        double stageWidth = mainStage.getWidth();
        double tableWidth = stageWidth * 0.5;
        tableView_Set.setMaxWidth(tableWidth);
    }

    /**
     * 保存设置
     *
     * @throws IOException io异常
     */
    public void saveLastConfig() throws IOException {
        // 保存jvm设置
        saveJVMConfig();
        // 保存页面开启状态与展示顺序设置
        saveTabIds();
    }

    /**
     * 保存页面开启状态与展示顺序设置
     *
     * @throws IOException io异常
     */
    private void saveTabIds() throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        List<String> tabIds = new ArrayList<>();
        for (TabBean tabBean : tableView_Set.getItems()) {
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
     * @throws IOException io异常
     */
    private void saveJVMConfig() throws IOException {
        String nextRunMemoryValue = nextRunMemory_Set.getText();
        String XmxValue = StringUtils.isBlank(nextRunMemoryValue) ? "" : nextRunMemoryValue + G;
        String nextGcTypeValue = nextGcType_Set.getValue();
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
     * 初始化各功能页面入口
     */
    private void buildTabsData(MainLoadedEvent event) {
        Properties prop = new Properties();
        InputStream input;
        try {
            input = checkRunningInputStream(configFile);
            prop.load(input);
            List<String> tabStateIds = Arrays.asList(prop.getProperty(key_tabIds).split(" "));
            input.close();
            List<TabBean> tabBeanList = new ArrayList<>();
            ObservableList<Tab> tabs = mainController.tabPane.getTabs();
            List<String> tabIds = new ArrayList<>();
            tabStateIds.forEach(tabStateId -> {
                String tabId = tabStateId.substring(0, tabStateId.indexOf("."));
                tabIds.add(tabId);
                String state = tabStateId.substring(tabStateId.indexOf(".") + 1);
                TabBean tabBean = new TabBean();
                Optional<Tab> tabOptional = tabs.stream()
                        .filter(t -> t.getId().equals(tabId))
                        .findFirst();
                if (tabOptional.isPresent()) {
                    Tab tab = tabOptional.get();
                    CheckBox checkBox = new CheckBox(text_activation);
                    addToolTip(tip_tabSwitch, checkBox);
                    boolean isActivation = activation.equals(state);
                    // 设置页面和关于页面不允许禁用
                    if (id_settingTab.equals(tabId) || id_aboutTab.equals(tabId)) {
                        checkBox.setDisable(true);
                        checkBox.setSelected(true);
                        isActivation = true;
                    }
                    checkBox.setSelected(isActivation);
                    tabBean.setActivationCheckBox(checkBox)
                            .setTabName(tab.getText())
                            .setTabId(tabId);
                    tabBeanList.add(tabBean);
                    if (!isActivation) {
                        tabs.remove(tab);
                    }
                }
            });
            // 功能页按照设置排序
            List<Tab> sortTabs = new ArrayList<>(sortTabsByIds(tabs, tabIds));
            tabs.clear();
            tabs.addAll(sortTabs);
            Platform.runLater(() -> {
                // 构建tab信息列表
                buildTableView(tableView_Set, tabBeanList);
                // 为tab信息列表添加右键菜单
                tableViewContextMenu(tableView_Set);
                // 加载完成后发布事件
                EventBus.publish(new SettingsLoadedEvent(tabBeanList));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建右键菜单
     *
     * @param tableView 要添加右键菜单的列表
     */
    private static void tableViewContextMenu(TableView<TabBean> tableView) {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 移动所选行选项
        buildMoveDataMenu(tableView, contextMenu);
        // 取消选中选项
        buildClearSelectedData(tableView, contextMenu);
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(contextMenu, tableView);
    }

    /**
     * 构建tab信息列表
     *
     * @param tableView   tab信息列表
     * @param tabBeanList 要渲染的tab信息
     */
    private static void buildTableView(TableView<TabBean> tableView, List<? extends TabBean> tabBeanList) {
        for (TableColumn<TabBean, ?> column : tableView.getColumns()) {
            addTableColumnToolTip(column);
            if ("tabName_Set".equals(column.getId())) {
                column.setCellValueFactory(new PropertyValueFactory<>("tabName"));
                addTableCellToolTip(column);
            } else if ("activationCheckBox_Set".equals(column.getId())) {
                column.setCellValueFactory(new PropertyValueFactory<>("activationCheckBox"));
            }
        }
        tableView.setItems(FXCollections.observableArrayList(tabBeanList));
        // 设置列表通过拖拽排序行
        tableViewDragRow(tableView);
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
        // 初始化各功能页面入口
        EventBus.subscribe(MainLoadedEvent.class, this::buildTabsData);
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
        mainController.saveAllLastConfig();
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
