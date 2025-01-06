package priv.koishi.tools;

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
import static priv.koishi.tools.Text.CommonTexts.*;
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
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/Main-view.fxml"));
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        double appWidth = Double.parseDouble(prop.getProperty("appWidth"));
        double appHeight = Double.parseDouble(prop.getProperty("appHeight"));
        if (activation.equals(prop.getProperty("lastFullWindow")) && activation.equals(prop.getProperty("loadLastFullWindow"))) {
            stage.setMaximized(true);
        }
        Scene scene = new Scene(fxmlLoader.load(), appWidth, appHeight);
        stage.setTitle(prop.getProperty("appTitle"));
        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("icon/Tools.png")).toExternalForm()));
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/Styles.css")).toExternalForm());
        TabPane tabPane = (TabPane) scene.lookup("#tabPane");
        List<String> tabIds = Arrays.asList(prop.getProperty("tabIds").split(" "));
        List<TabBean> tabBeanList = buildTabsData(scene, tabPane, tabIds);
        ObservableList<Tab> tabs = tabPane.getTabs();
        //设置默认选中的Tab
        tabs.forEach(tab -> {
            if (activation.equals(prop.getProperty(key_loadLastConfig))) {
                if (tab.getId().equals(prop.getProperty(key_lastTab))) {
                    tabPane.getSelectionModel().select(tab);
                }
            }
        });
        input.close();
        //监听窗口面板宽度变化
        stage.widthProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> mainAdaption(stage, tabBeanList)));
        //监听窗口面板高度变化
        stage.heightProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> mainAdaption(stage, tabBeanList)));
        stage.setOnCloseRequest(event -> {
            try {
                saveLastConfig(stage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        });
        stage.show();
    }

    /**
     * 初始化各功能页面入口
     *
     * @param scene   程序主场景
     * @param tabPane 各功能页面入口所在tab布局
     * @param tabIds  各功能页面入口对应的id
     * @return 含有各功能页面属性的列表
     */
    @SuppressWarnings("unchecked")
    private static List<TabBean> buildTabsData(Scene scene, TabPane tabPane, List<String> tabIds) {
        TableView<TabBean> tableView = (TableView<TabBean>) scene.lookup("#tableView_Set");
        List<TabBean> tabBeanList = new ArrayList<>();
        ObservableList<Tab> tabs = tabPane.getTabs();
        tabIds.forEach(tabId -> {
            String tab = tabId.substring(0, tabId.indexOf("."));
            String state = tabId.substring(tabId.indexOf(".") + 1);
            TabBean tabBean = new TabBean();
            Optional<Tab> tabOptional = tabs.stream()
                    .filter(t -> t.getId().equals(tab))
                    .findFirst();
            if (tabOptional.isPresent()) {
                Tab tabNode = tabOptional.get();
                CheckBox checkBox = new CheckBox("启用");
                boolean isActivation = activation.equals(state);
                checkBox.setSelected(isActivation);
                tabBean.setActivationCheckBox(checkBox)
                        .setTabName(tabNode.getText())
                        .setTabId(tab);
                tabBeanList.add(tabBean);
                if (!isActivation) {
                    tabs.remove(tabNode);
                }
            }
        });
        //构建tab信息列表
        for (TableColumn<TabBean, ?> column : tableView.getColumns()) {
            if ("tabName_Set".equals(column.getId())) {
                column.setCellValueFactory(new PropertyValueFactory<>("tabName"));
            } else if ("activationCheckBox_Set".equals(column.getId())) {
                column.setCellValueFactory(new PropertyValueFactory<>("activationCheckBox"));
            }
        }
        tableView.setItems(FXCollections.observableArrayList(tabBeanList));
        //设置列表通过拖拽排序行
        tableViewDragRow(tableView);
        return tabBeanList;
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
        //打包后需要手动指定日志配置文件位置
        if (!isRunningFromJar()) {
            ConfigurationSource source = new ConfigurationSource(new FileInputStream("log4j2.xml"));
            Configurator.initialize(null, source);
        }
        launch();
    }

}