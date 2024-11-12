package priv.koishi.tools.Controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import static priv.koishi.tools.Controller.FileNameToExcelController.fileNameToExcelAdaption;
import static priv.koishi.tools.Controller.FileNumToExcelController.fileNumToExcelAdaption;
import static priv.koishi.tools.Controller.FileRenameController.fileRenameAdaption;
import static priv.koishi.tools.Controller.ImgToExcelController.imgToExcelAdaption;
import static priv.koishi.tools.Controller.SettingTabController.settingToExcelAdaption;

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
        settingToExcelAdaption(stage, scene);
    }

}
