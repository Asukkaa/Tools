package priv.koishi.tools.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import priv.koishi.tools.Bean.TabBean;
import priv.koishi.tools.EventBus.CommonEventBus;
import priv.koishi.tools.EventBus.MainLoadedEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.MainApplication.mainStage;
import static priv.koishi.tools.Utils.FileUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.FileUtils.checkRunningOutputStream;
import static priv.koishi.tools.Utils.UiUtils.creatTooltip;

/**
 * 全局页面控制器
 *
 * @author KOISHI Date:2024-10-02
 * Time:下午1:08
 */
public class MainController extends RootController {

    /**
     * 关于页面控制器
     */
    public static AboutController aboutController;

    /**
     * 设置页面控制器
     */
    public static SettingController settingController;

    /**
     * 自动操作工具页面控制器
     */
    public static AutoClickController autoClickController;

    /**
     * 获取文件夹下的文件信息页面控制器
     */
    public static FileNameToExcelController fileNameToExcelController;

    /**
     * 将图片与excel匹配并插入页面控制器
     */
    public static ImgToExcelController imgToExcelController;

    /**
     * 按指定规则批量重命名文件页面控制器
     */
    public static FileRenameController fileRenameController;

    /**
     * 移动文件工具页面控制器
     */
    public static MoveFileController moveFileController;

    public static CopyFileController copyFileController;

    @FXML
    public TabPane tabPane;

    @FXML
    public Tab fileNameToExcelTab, moveFileTab, imgToExcelTab, fileRenameTab, settingTab, aboutTab, copyFileTab,
            autoClickTab;

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() {
        // 设置tab页的鼠标悬停提示
        tabPane.getTabs().forEach(tab -> tab.setTooltip(creatTooltip(tab.getText())));
        Platform.runLater(() -> {
            aboutController = getController(AboutController.class);
            settingController = getController(SettingController.class);
            moveFileController = getController(MoveFileController.class);
            copyFileController = getController(CopyFileController.class);
            autoClickController = getController(AutoClickController.class);
            imgToExcelController = getController(ImgToExcelController.class);
            fileRenameController = getController(FileRenameController.class);
            fileNameToExcelController = getController(FileNameToExcelController.class);
            // 主页面加载完毕
            CommonEventBus.publish(new MainLoadedEvent());
        });
    }

    /**
     * 组件自适应宽高
     */
    public void mainAdaption(List<? extends TabBean> tabBeanList) {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tabPane.setStyle("-fx-pref-height: " + stageHeight + "px;");
        tabBeanList.forEach(tabBean -> {
            boolean isActivation = tabBean.getActivationCheckBox().isSelected();
            switch (tabBean.getTabId()) {
                case id_fileNameToExcelTab:
                    if (isActivation) {
                        fileNameToExcelController.adaption();
                    }
                    break;
                case id_imgToExcelTab:
                    if (isActivation) {
                        imgToExcelController.adaption();
                    }
                    break;
                case id_fileRenameTab:
                    if (isActivation) {
                        fileRenameController.adaption();
                    }
                    break;
                case id_settingTab:
                    if (isActivation) {
                        settingController.adaption();
                    }
                    break;
                case id_aboutTab:
                    break;
                case id_autoClickTab:
                    if (isActivation) {
                        autoClickController.adaption();
                    }
                    break;
                case id_moveFileTab:
                    if (isActivation) {
                        moveFileController.adaption();
                    }
                    break;
                case id_copyFileTab:
                    if (isActivation) {
                        copyFileController.adaption();
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
    public void saveAllLastConfig() throws IOException {
        // 保存批量向excel功能插入图片最后设置
        if (imgToExcelController != null) {
            imgToExcelController.saveLastConfig();
        }
        // 保存导出文件详细信息到excel最后设置
        if (fileNameToExcelController != null) {
            fileNameToExcelController.saveLastConfig();
        }
        // 保存文件批量重命名功能最后设置
        if (fileRenameController != null) {
            fileRenameController.saveLastConfig();
        }
        // 保存自动操作工具功能最后设置
        if (autoClickController != null) {
            autoClickController.saveLastConfig();
        }
        // 保存设置页面最后设置
        if (settingController != null) {
            settingController.saveLastConfig();
        }
        // 保存日志文件数量设置
        if (aboutController != null) {
            aboutController.saveLastConfig();
        }
        // 移动文件功能保存最后设置
        if (moveFileController != null) {
            moveFileController.saveLastConfig();
        }
        // 拷贝文件功能保存最后设置
        if (copyFileController != null) {
            copyFileController.saveLastConfig();
        }
        // 保存关程序闭前页面状态设置
        saveLastConfig();
    }

    /**
     * 保存关程序闭前页面状态
     *
     * @throws IOException io异常
     */
    private void saveLastConfig() throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        prop.put(key_lastTab, selectedTab.getId());
        String fullWindow = mainStage.isFullScreen() ? activation : unActivation;
        prop.put(key_lastFullWindow, fullWindow);
        String maximize = mainStage.isMaximized() ? activation : unActivation;
        prop.put(key_lastMaxWindow, maximize);
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
    }

}
