package priv.koishi.tools;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import priv.koishi.tools.Bean.TabBean;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static priv.koishi.tools.Controller.MainController.mainAdaption;
import static priv.koishi.tools.Controller.MainController.saveLastConfig;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.isRunningFromJar;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 程序启动类
 *
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午1:48
 */
public class MainApplication extends Application {

    /**
     * 加载fxml页面
     *
     * @param stage 程序主舞台
     * @throws RuntimeException io异常
     * @throws Exception        io异常
     */
    @Override
    public void start(Stage stage) throws Exception {
        // 读取fxml页面
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/Main-view.fxml"));
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        double appWidth = Double.parseDouble(prop.getProperty(key_appWidth));
        double appHeight = Double.parseDouble(prop.getProperty(key_appHeight));
        if (activation.equals(prop.getProperty(key_lastFullWindow)) && activation.equals(prop.getProperty(key_loadLastFullWindow))) {
            stage.setMaximized(true);
        }
        Scene scene = new Scene(fxmlLoader.load(), appWidth, appHeight);
        stage.setTitle(prop.getProperty(key_appTitle));
        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("icon/Tools.png")).toExternalForm()));
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/Styles.css")).toExternalForm());
        TabPane tabPane = (TabPane) scene.lookup("#tabPane");
        // 读取各功能页面入口设置
        List<String> tabStateIds = Arrays.asList(prop.getProperty(key_tabIds).split(" "));
        // 初始化各功能页面入口
        List<TabBean> tabBeanList = buildTabsData(scene, tabPane, tabStateIds);
        // 设置默认选中的Tab
        if (activation.equals(prop.getProperty(key_loadLastConfig))) {
            tabPane.getTabs().forEach(tab -> {
                if (tab.getId().equals(prop.getProperty(key_lastTab))) {
                    tabPane.getSelectionModel().select(tab);
                }
            });
        }
        input.close();
        // 监听窗口面板宽度变化
        stage.widthProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> mainAdaption(stage, tabBeanList)));
        // 监听窗口面板高度变化
        stage.heightProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> mainAdaption(stage, tabBeanList)));
        // 程序关闭时保存各个页面的设置
        stage.setOnCloseRequest(event -> {
            try {
                saveLastConfig(stage);
                // 程序退出时注销全局键盘监听器
                GlobalScreen.unregisterNativeHook();
            } catch (IOException | NativeHookException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        });
        stage.show();
    }

    /**
     * 初始化各功能页面入口
     *
     * @param scene       程序主场景
     * @param tabPane     各功能页面入口所在tab布局
     * @param tabStateIds 带启用状态的tab id
     * @return 含有各功能页面属性的列表
     */
    @SuppressWarnings("unchecked")
    private static List<TabBean> buildTabsData(Scene scene, TabPane tabPane, List<String> tabStateIds) {
        TableView<TabBean> tableView = (TableView<TabBean>) scene.lookup("#tableView_Set");
        List<TabBean> tabBeanList = new ArrayList<>();
        ObservableList<Tab> tabs = tabPane.getTabs();
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
        // 构建tab信息列表
        buildTableView(tableView, tabBeanList);
        // 为tab信息列表添加右键菜单
        tableViewContextMenu(tableView);
        return tabBeanList;
    }

    /**
     * 构建右键菜单
     *
     * @param tableView 要添加右键菜单的列表
     */
    private static void tableViewContextMenu(TableView<TabBean> tableView) {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 所选行上移一行选项
        buildUpMoveDataMenuItem(tableView, contextMenu);
        // 所选行下移一行选项
        buildDownMoveDataMenuItem(tableView, contextMenu);
        tableView.setContextMenu(contextMenu);
        tableView.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * 构建tab信息列表
     *
     * @param tableView   tab信息列表
     * @param tabBeanList 要渲染的tab信息
     */
    private static void buildTableView(TableView<TabBean> tableView, List<TabBean> tabBeanList) {
        for (TableColumn<TabBean, ?> column : tableView.getColumns()) {
            if ("tabName_Set".equals(column.getId())) {
                column.setCellValueFactory(new PropertyValueFactory<>("tabName"));
            } else if ("activationCheckBox_Set".equals(column.getId())) {
                column.setCellValueFactory(new PropertyValueFactory<>("activationCheckBox"));
            }
        }
        tableView.setItems(FXCollections.observableArrayList(tabBeanList));
        // 设置列表通过拖拽排序行
        tableViewDragRow(tableView);
    }

    /**
     * 全局异常处理器
     *
     * @throws Exception 全局异常
     */
    @Override
    public void init() throws Exception {
        super.init();
        // 在init()方法中设置全局异常处理器
        Platform.runLater(() -> Thread.setDefaultUncaughtExceptionHandler((e, exception) -> showExceptionAlert(exception)));
    }

    /**
     * 启动程序
     *
     * @param args 启动参数
     * @throws IOException io异常
     */
    public static void main(String[] args) throws IOException {
        // 打包后需要手动指定日志配置文件位置
        if (!isRunningFromJar()) {
            ConfigurationSource source = new ConfigurationSource(new FileInputStream("log4j2.xml"));
            Configurator.initialize(null, source);
        }
        launch();
    }

}
