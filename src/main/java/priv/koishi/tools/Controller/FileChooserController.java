package priv.koishi.tools.Controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Configuration.FileConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static priv.koishi.tools.Controller.MainController.moveFileController;
import static priv.koishi.tools.Controller.MoveFileController.addFile;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.FileUtils.readAllFiles;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * @author KOISHI
 * Date:2025-08-04
 * Time:22:12
 */
public class FileChooserController extends RootController {

    /**
     * 页面标识符
     */
    private static final String tabId = "_FC";

    @Setter
    private Runnable refreshCallback;

    /**
     * 文件选择页面舞台
     */
    private Stage stage;

    @FXML
    public AnchorPane anchorPane_FC;

    @FXML
    public HBox fileNumberHBox_FC;

    @FXML
    public Label outPath_FC, fileNumber_FC;

    @FXML
    public Button outPathButton_FC, addFileButton_FC;

    @FXML
    public TableView<FileBean> tableView_FC;

    @FXML
    public TableColumn<FileBean, ImageView> thumb_FC;

    @FXML
    public TableColumn<FileBean, Integer> index_FC;

    @FXML
    public TableColumn<FileBean, String> name_FC, path_FC, size_FC, fileType_FC,
            creatDate_FC, updateDate_FC, showStatus_FC;

    /**
     * 初始化数据
     *
     * @param outFilePath 列表选中的数据
     */
    public void initData(String outFilePath) throws IOException {
        setPathLabel(outPath_FC, outFilePath);
        // 设置鼠标悬停提示
        setToolTip();
        selectFile(new File(outFilePath));
    }

    private void selectFile(File outFilePath) throws IOException {
        removeAll();
        FileConfig fileConfig = new FileConfig()
                .setShowDirectoryName(text_onlyDirectory)
                .setInFile(outFilePath);
        addInData(readAllFiles(fileConfig));
    }

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = stage.getHeight();
        tableView_FC.setPrefHeight(stageHeight * 0.6);
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        tableView_FC.setMaxWidth(tableWidth);
        tableView_FC.setPrefWidth(tableWidth);
        regionRightAlignment(fileNumberHBox_FC, tableWidth, fileNumber_FC);
        bindPrefWidthProperty();
    }

    /**
     * 设置列表各列宽度
     */
    private void bindPrefWidthProperty() {
        index_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.04));
        thumb_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.1));
        name_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.14));
        fileType_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.08));
        path_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.2));
        size_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.08));
        showStatus_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.08));
        creatDate_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.14));
        updateDate_FC.prefWidthProperty().bind(tableView_FC.widthProperty().multiply(0.14));
    }

    /**
     * 清空列表
     */
    private void removeAll() {
        removeTableViewData(tableView_FC, fileNumber_FC, null);
    }

    /**
     * 添加数据渲染列表
     *
     * @param files 查询到的文件list
     * @throws IOException 未查询到符合条件的数据
     */
    private void addInData(List<File> files) throws IOException {
        addFile(files, false, tableView_FC);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_outPathButton, outPathButton_FC);
        addToolTip(tip_reselectButton, addFileButton_FC);
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            stage = (Stage) anchorPane_FC.getScene().getWindow();
            // 组件自适应宽高
            adaption();
            // 绑定表格数据
            autoBuildTableViewData(tableView_FC, FileBean.class, tabId, index_FC);
            // 设置文件大小排序
            fileSizeColum(size_FC);
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_FC);
            // 构建右键菜单
            tableViewContextMenu(tableView_FC, fileNumber_FC);
        });
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) throws IOException {
        removeAll();
        List<File> files = dragEvent.getDragboard().getFiles();
        File file = files.getFirst();
        selectFile(file);
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void acceptDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        files.forEach(file -> {
            if (file.isDirectory()) {
                // 接受拖放
                dragEvent.acceptTransferModes(TransferMode.COPY);
                dragEvent.consume();
            }
        });
    }

    /**
     * 选择要查询的文件夹
     *
     * @param actionEvent 点击事件
     */
    @FXML
    public void selectFilePath(ActionEvent actionEvent) throws IOException {
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatDirectoryChooser(window, outPath_FC.getText(), text_selectDirectory);
        if (selectedFile != null) {
            // 获取所选文件路径
            setPathLabel(outPath_FC, selectedFile.getPath());
            selectFile(selectedFile);
        }
    }

    /**
     * 前往上级文件夹
     */
    @FXML
    public void gotoParent() throws IOException {
        File file = new File(outPath_FC.getText());
        File parentFile = file.getParentFile();
        selectFile(parentFile);
        setPathLabel(outPath_FC, parentFile.getPath());
    }

    @FXML
    public void selectFile() {
        ObservableList<FileBean> selectedItems = tableView_FC.getSelectionModel().getSelectedItems();
        moveFileController.tableView_MV.getItems().addAll(selectedItems);
        MoveFileController.outFilePath = outPath_FC.getText();
        stage.close();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    @FXML
    public void cancelSelect() {
        stage.close();
    }

}
