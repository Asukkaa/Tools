package priv.koishi.tools;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import static priv.koishi.tools.Controller.MainController.mainAdaption;
import static priv.koishi.tools.Controller.MainController.saveLastConfig;
import static priv.koishi.tools.Utils.UiUtils.showExceptionDialog;

public class MainApplication extends Application {

    /**
     * 程序启动窗口宽度
     */
    static double appWidth;

    /**
     * 程序启动窗口高度
     */
    static double appHeight;

    /**
     * 程序窗口名称
     */
    static String appTitle;

    /**
     * 加载fxml页面
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/Main-view.fxml"));
        getConfig();
        Scene scene = new Scene(fxmlLoader.load(), appWidth, appHeight);
        stage.setTitle(appTitle);
        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("icon/wrench.png")).toExternalForm()));
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/Styles.css")).toExternalForm());
        //监听窗口面板宽度变化
        stage.widthProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> mainAdaption(stage, scene)));
        //监听窗口面板高度变化
        stage.heightProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> mainAdaption(stage, scene)));
        stage.setOnCloseRequest(event -> {
            try {
                saveLastConfig(scene);
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
        Platform.runLater(() -> Thread.setDefaultUncaughtExceptionHandler((e, exception) -> showExceptionDialog(exception)));
    }

    /**
     * 读取配置文件
     */
    public static void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = Objects.requireNonNull(MainApplication.class.getResource("config/config.properties")).openStream();
        prop.load(input);
        appWidth = Double.parseDouble(prop.getProperty("appWidth"));
        appHeight = Double.parseDouble(prop.getProperty("appHeight"));
        appTitle = prop.getProperty("appTitle");
        input.close();
    }

    /**
     * 启动程序
     */
    public static void main(String[] args) {
        launch();
    }

}