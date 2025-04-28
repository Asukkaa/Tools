package priv.koishi.tools.Controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import priv.koishi.tools.Bean.TabBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.FileUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.FileUtils.checkRunningOutputStream;

/**
 * 全局页面控制器
 *
 * @author KOISHI Date:2024-10-02
 * Time:下午1:08
 */
public class MainController {

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab fileNumToExcelTab, fileNameToExcelTab, imgToExcelTab, fileRenameTab, settingTab, aboutTab, autoClickTab;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void mainAdaption(Stage stage, List<TabBean> tabBeanList) {
        Scene scene = stage.getScene();
        // 设置组件高度
        double stageHeight = stage.getHeight();
        TabPane tabPane = (TabPane) scene.lookup("#tabPane");
        tabPane.setStyle("-fx-pref-height: " + stageHeight + "px;");
        tabBeanList.forEach(tabBean -> {
            boolean isActivation = tabBean.getActivationCheckBox().isSelected();
            switch (tabBean.getTabId()) {
                case id_fileNameToExcelTab:
                    if (isActivation) {
                        FileNameToExcelController.adaption(stage);
                    }
                    break;
                case id_fileNumToExcelTab:
                    if (isActivation) {
                        FileNumToExcelController.adaption(stage);
                    }
                    break;
                case id_imgToExcelTab:
                    if (isActivation) {
                        ImgToExcelController.adaption(stage);
                    }
                    break;
                case id_fileRenameTab:
                    if (isActivation) {
                        FileRenameController.adaption(stage);
                    }
                    break;
                case id_settingTab:
                    if (isActivation) {
                        SettingController.adaption(stage);
                    }
                    break;
                case id_aboutTab:
                    break;
                case id_autoClickTab:
                    if (isActivation) {
                        AutoClickController.adaption(stage);
                    }
                    break;
            }
        });
    }

    /**
     * 保存各个功能最后一次设置值
     *
     * @throws IOException io异常
     */
    public static void saveAllLastConfig(Stage stage) throws IOException {
        Scene scene = stage.getScene();
        // 保存批量向excel功能插入图片最后设置
        ImgToExcelController.saveLastConfig(scene);
        // 保存分组统计文件信息导出到excel最后设置
        FileNumToExcelController.saveLastConfig(scene);
        // 保存导出文件详细信息到excel最后设置
        FileNameToExcelController.saveLastConfig(scene);
        // 保存文件批量重命名功能最后设置
        FileRenameController.saveLastConfig(scene);
        // 保存自动操作工具功能最后设置
        AutoClickController.saveLastConfig(scene);
        // 保存关程序闭前页面状态设置
        MainController.saveLastConfig(stage);
        // 保存设置页面最后设置
        SettingController.saveLastConfig(scene);
        // 保存日志文件数量设置
        AboutController.saveLastConfig(scene);
    }

    /**
     * 保存关程序闭前页面状态
     *
     * @throws IOException io异常
     */
    private static void saveLastConfig(Stage stage) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        Scene scene = stage.getScene();
        TabPane tabPane = (TabPane) scene.lookup("#tabPane");
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        prop.put(key_lastTab, selectedTab.getId());
        String fullWindow = stage.isFullScreen() ? activation : unActivation;
        prop.put(key_lastFullWindow, fullWindow);
        String maximize = stage.isMaximized() ? activation : unActivation;
        prop.put(key_lastMaxWindow, maximize);
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
    }

}
