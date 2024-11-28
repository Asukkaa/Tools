package priv.koishi.tools.Controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import static priv.koishi.tools.Controller.FileNameToExcelController.fileNameToExcelAdaption;
import static priv.koishi.tools.Controller.FileNameToExcelController.fileNameToExcelSaveLastConfig;
import static priv.koishi.tools.Controller.FileNumToExcelController.fileNumToExcelAdaption;
import static priv.koishi.tools.Controller.FileNumToExcelController.fileNumToExcelSaveLastConfig;
import static priv.koishi.tools.Controller.FileRenameController.fileRenameAdaption;
import static priv.koishi.tools.Controller.FileRenameController.fileRenameSaveLastConfig;
import static priv.koishi.tools.Controller.ImgToExcelController.imgToExcelAdaption;
import static priv.koishi.tools.Controller.ImgToExcelController.imgToExcelSaveLastConfig;
import static priv.koishi.tools.Controller.SettingController.saveMemorySetting;
import static priv.koishi.tools.Controller.SettingController.settingAdaption;
import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningOutputStream;

/**
 * @author KOISHI Date:2024-10-02
 * Time:下午1:08
 */
public class MainController {

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab fileNumToExcelTab, fileNameToExcelTab, imgToExcelTab, fileRenameTab, settingTab;

    /**
     * 组件自适应宽高
     */
    public static void mainAdaption(Stage stage, Scene scene) {
        //设置组件高度
        double stageHeight = stage.getHeight();
        TabPane tabPane = (TabPane) scene.lookup("#tabPane");
        tabPane.setStyle("-fx-pref-height: " + stageHeight + "px;");
        fileNameToExcelAdaption(stage, scene);
        fileNumToExcelAdaption(stage, scene);
        imgToExcelAdaption(stage, scene);
        fileRenameAdaption(stage, scene);
        settingAdaption(stage, scene);
    }

    /**
     * 保存各个功能最后一次设置值
     */
    public static void saveLastConfig(Scene scene, Stage stage) throws IOException {
        imgToExcelSaveLastConfig(scene);
        fileNumToExcelSaveLastConfig(scene);
        fileNameToExcelSaveLastConfig(scene);
        fileRenameSaveLastConfig(scene);
        mainSavaLastConfig(scene, stage);
        saveMemorySetting(scene);
    }

    /**
     * 保存关程序闭前页面状态
     */
    private static void mainSavaLastConfig(Scene scene, Stage stage) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        TabPane tabPane = (TabPane) scene.lookup("#tabPane");
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        String position = String.valueOf(tabPane.getTabs().indexOf(selectedTab));
        prop.put(key_lastTab, position);
        String fullWindow = stage.isMaximized() ? activation : unActivation;
        prop.put(key_lastFullWindow, fullWindow);
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
    }

}
