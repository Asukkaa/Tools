package priv.koishi.tools;

import com.github.kwhat.jnativehook.GlobalScreen;
import de.jangassen.MenuToolkit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import priv.koishi.tools.Bean.TabBean;
import priv.koishi.tools.Controller.MainController;
import priv.koishi.tools.EventBus.EventBus;
import priv.koishi.tools.EventBus.SettingsLoadedEvent;
import priv.koishi.tools.ThreadPool.ThreadPoolManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Finals.CommonFinals.isRunningFromJar;
import static priv.koishi.tools.Utils.FileUtils.*;
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
     * 日志记录器
     */
    private static Logger logger;

    /**
     * 程序主舞台
     */
    public static Stage mainStage;

    /**
     * 程序主场景
     */
    public static Scene mainScene;

    /**
     * 主控制器
     */
    public static MainController mainController;

    /**
     * 加载fxml页面
     *
     * @param stage 程序主舞台
     * @throws RuntimeException io异常
     * @throws Exception        io异常
     */
    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        // 读取fxml页面
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/Main-view.fxml"));
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        double appWidth = Double.parseDouble(prop.getProperty(key_appWidth));
        double appHeight = Double.parseDouble(prop.getProperty(key_appHeight));
        if (activation.equals(prop.getProperty(key_lastMaxWindow))
                && activation.equals(prop.getProperty(key_loadLastMaxWindow))) {
            stage.setMaximized(true);
        } else if (activation.equals(prop.getProperty(key_lastFullWindow))
                && activation.equals(prop.getProperty(key_loadLastFullWindow))) {
            stage.setFullScreen(true);
        }
        mainScene = new Scene(fxmlLoader.load(), appWidth, appHeight);
        stage.setTitle(appName);
        stage.setScene(mainScene);
        setWindLogo(stage, logoPath);
        // 设置css样式
        setWindowCss(mainScene, stylesCss);
        mainController = fxmlLoader.getController();
        TabPane tabPane = mainController.tabPane;
        String loadLastConfig = prop.getProperty(key_loadLastConfig);
        String lastTab = prop.getProperty(key_lastTab);
        input.close();
        // 初始化macOS系统应用菜单
        initMenu(tabPane);
        // 页面入口展示和自适应宽高
        EventBus.subscribe(SettingsLoadedEvent.class, event ->
                mainApplicationAdaption(event, tabPane, loadLastConfig, lastTab));
        stage.setOnCloseRequest(event -> {
            try {
                stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        stage.show();
        logger.info("--------------程序启动成功-------------------");
    }

    /**
     * 页面入口展示和自适应宽高
     *
     * @param event          配置加载完成事件
     * @param tabPane        主页面布局
     * @param loadLastConfig 是否加载上次配置
     * @param lastTab        默认选中的Tab
     */
    private void mainApplicationAdaption(SettingsLoadedEvent event, TabPane tabPane, String loadLastConfig, String lastTab) {
        List<TabBean> tabBeanList = event.getTabBeanList();
        // 初始化组件宽高
        mainController.mainAdaption(tabBeanList);
        // 监听窗口面板宽度变化
        mainStage.widthProperty().addListener((v1, v2, v3) ->
                Platform.runLater(() -> mainController.mainAdaption(tabBeanList)));
        // 监听窗口面板高度变化
        mainStage.heightProperty().addListener((v1, v2, v3) ->
                Platform.runLater(() -> mainController.mainAdaption(tabBeanList)));
        // 设置默认选中的Tab
        if (activation.equals(loadLastConfig)) {
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getId().equals(lastTab)) {
                    tabPane.getSelectionModel().select(tab);
                    break;
                }
            }
        }
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
        Platform.runLater(() -> Thread.setDefaultUncaughtExceptionHandler((e, exception) ->
                showExceptionAlert(exception)));
    }

    /**
     * 程序停止时保存设置并关闭资源
     *
     * @throws IOException io异常、钩子异常、线程池关闭异常
     */
    @Override
    public void stop() throws Exception {
        // 关闭线程池
        ThreadPoolManager.shutdownAll();
        // 卸载全局输入监听钩子
        GlobalScreen.unregisterNativeHook();
        // 保存设置
        if (mainController != null) {
            mainController.saveAllLastConfig();
        }
        // 停止 javafx ui 线程
        Platform.exit();
        logger.info("==============程序退出中====================");
        System.exit(0);
    }

    /**
     * 初始化macOS系统应用菜单
     *
     * @param tabPane 程序页面基础布局
     */
    private void initMenu(TabPane tabPane) {
        MenuItem about = new MenuItem("关于 " + appName);
        about.setOnAction(e -> {
            tabPane.getSelectionModel().select(mainController.aboutTab);
            showStage(mainStage);
        });
        MenuItem setting = new MenuItem("设置...");
        setting.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN));
        setting.setOnAction(e -> {
            tabPane.getSelectionModel().select(mainController.settingTab);
            showStage(mainStage);
        });
        MenuToolkit.toolkit(Locale.getDefault()).createAboutMenuItem(appName);
        MenuItem hide = MenuToolkit.toolkit(Locale.getDefault()).createHideMenuItem(appName);
        hide.setText("隐藏 " + appName);
        MenuItem hideOthers = MenuToolkit.toolkit(Locale.getDefault()).createHideOthersMenuItem();
        hideOthers.setText("隐藏其他");
        MenuItem quit = MenuToolkit.toolkit(Locale.getDefault()).createQuitMenuItem(appName);
        quit.setText("退出 " + appName);
        Menu menu = new Menu();
        menu.getItems().addAll(about, new SeparatorMenuItem(), setting, new SeparatorMenuItem(), hide, hideOthers, new SeparatorMenuItem(), quit);
        MenuToolkit.toolkit(Locale.getDefault()).setApplicationMenu(menu);
    }

    /**
     * 启动程序
     *
     * @param args 启动参数
     * @throws IOException io异常
     */
    public static void main(String[] args) throws IOException {
        System.setProperty("log.dir", getLogsPath());
        // 打包后需要手动指定日志配置文件位置
        if (!isRunningFromJar) {
            ConfigurationSource source = new ConfigurationSource(new FileInputStream(getAppResourcePath(log4j2)));
            Configurator.initialize(null, source);
        }
        logger = LogManager.getLogger(MainApplication.class);
        logger.info("==============程序启动中====================");
        launch();
    }

}
