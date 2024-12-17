package priv.koishi.tools;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import static priv.koishi.tools.Controller.MainController.mainAdaption;
import static priv.koishi.tools.Controller.MainController.saveLastConfig;
import static priv.koishi.tools.Text.CommonTexts.activation;
import static priv.koishi.tools.Text.CommonTexts.configFile;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.isRunningFromJar;
import static priv.koishi.tools.Utils.UiUtils.showExceptionAlert;

public class MainApplication extends Application {


    /**
     * 加载fxml页面
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
        if (activation.equals(prop.getProperty("loadLastConfig"))) {
            //设置默认选中的Tab
            tabPane.getSelectionModel().select(Integer.parseInt(prop.getProperty("lastTab")));
        }
        input.close();
        //监听窗口面板宽度变化
        stage.widthProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> mainAdaption(stage, scene)));
        //监听窗口面板高度变化
        stage.heightProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> mainAdaption(stage, scene)));
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
     * 全局异常处理器
     */
    @Override
    public void init() throws Exception {
        super.init();
        // 在init()方法中设置全局异常处理器
        Platform.runLater(() -> Thread.setDefaultUncaughtExceptionHandler((e, exception) -> showExceptionAlert(exception)));
    }

    /**
     * 启动程序
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