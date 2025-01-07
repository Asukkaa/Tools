package priv.koishi.tools.Controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningOutputStream;
import static priv.koishi.tools.Utils.FileUtils.getFileType;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * @author KOISHI
 * Date:2025-01-07
 * Time:16:45
 */
public class AboutController {

    @FXML
    private AnchorPane anchorPane_Abt;

    @FXML
    private VBox vBox_Abt;

    @FXML
    private TextField logsNum_Abt;

    @FXML
    private Label logsPath_Abt, mail_Abt;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void aboutAdaption(Stage stage) {
        Scene scene = stage.getScene();
        //设置组件宽度
        double stageWidth = stage.getWidth();
        Node settingVBox = scene.lookup("#vBox_Abt");
        settingVBox.setLayoutX(stageWidth * 0.03);
    }

    /**
     * 读取配置文件
     *
     * @throws IOException io异常
     */
    private void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        setControlLastConfig(logsNum_Abt, prop, key_logsNum, false, null);
        input.close();
    }

    /**
     * 获取logs文件夹路径并展示
     */
    private void setLogsPath() {
        String logsPath = currentDir + File.separator + "logs";
        setPathLabel(logsPath_Abt, logsPath, false, anchorPane_Abt);
    }

    /**
     * 保存日志问文件数量设置
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    public static void saveLogsNumSetting(Scene scene) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        TextField logsNumTextField = (TextField) scene.lookup("#logsNum_Abt");
        String logsNumValue = logsNumTextField.getText();
        prop.setProperty(key_logsNum, logsNumValue);
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
    }

    /**
     * 清理多余log文件
     *
     * @throws RuntimeException 删除日志文件失败
     */
    private void deleteLogs() {
        File[] files = new File(logsPath_Abt.getText()).listFiles();
        if (files != null) {
            List<File> logList = new ArrayList<>();
            for (File file : files) {
                if (log.equals(getFileType(file))) {
                    logList.add(file);
                }
            }
            int logsNum = Integer.parseInt(logsNum_Abt.getText());
            if (logList.size() > logsNum) {
                logList.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                List<File> removeList = logList.stream().skip(logsNum).toList();
                removeList.forEach(r -> {
                    String path = r.getAbsolutePath();
                    File file = new File(path);
                    if (!file.delete()) {
                        throw new RuntimeException("日志文件 " + path + " 删除失败");
                    }
                });
            }
        }
    }

    /**
     * 界面初始化
     *
     * @throws IOException io异常
     */
    @FXML
    private void initialize() throws IOException {
        //添加右键菜单
        setCopyValueContextMenu(mail_Abt, "复制反馈邮件", anchorPane_Abt);
        //log 文件保留数量输入监听
        integerRangeTextField(logsNum_Abt, 0, null, tip_logsNum);
        //读取配置文件
        getConfig();
        //获取logs文件夹路径并展示
        setLogsPath();
        //清理多余log文件
        deleteLogs();
    }

}
