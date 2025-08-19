package priv.koishi.tools.Controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Callback.FileChooserCallback;
import priv.koishi.tools.Configuration.FileChooserConfig;
import priv.koishi.tools.Configuration.FileConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Service.ReadDataService.readAllFilesTask;
import static priv.koishi.tools.Utils.FileUtils.openDirectory;
import static priv.koishi.tools.Utils.FileUtils.updateProperties;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;
import static priv.koishi.tools.Utils.UiUtils.addToolTip;

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

    /**
     * 文件查询设置
     */
    private FileChooserConfig fileChooserConfig;

    /**
     * 文件名输入框监听器
     */
    private ChangeListener<String> textFieldChangeListener;

    /**
     * 文件查询任务
     */
    private static Task<List<File>> readAllFilesTask;

    /**
     * 要防重复点击的组件
     */
    private static final List<Node> disableNodes = new ArrayList<>();

    /**
     * 回调函数
     */
    @Setter
    private FileChooserCallback fileChooserCallback;

    /**
     * 文件选择页面舞台
     */
    private Stage stage;

    @FXML
    public AnchorPane anchorPane_FC;

    @FXML
    public HBox fileNumberHBox_FC;

    @FXML
    public VBox progressBarVBox_FC;

    @FXML
    public ProgressBar progressBar_FC;

    @FXML
    public TextField fileNameFilter_FC;

    @FXML
    public CheckBox reverse_FC, filterNameCase_FC;

    @FXML
    public Label filePath_FC, fileNumber_FC, log_FC, tip_FC;

    @FXML
    public ChoiceBox<String> fileFilter_FC, hideFileType_FC, fileNameType_FC;

    @FXML
    public Button selectPathButton_FC, gotoParentButton_FC, refreshButton_FC, confirm_FC, close_FC;

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
     * @param fileChooserConfig 文件查询设置
     */
    public void initData(FileChooserConfig fileChooserConfig) {
        String path = fileChooserConfig.getInFile().getPath();
        if (StringUtils.isBlank(path)) {
            path = defaultFileChooserPath;
        }
        this.fileChooserConfig = fileChooserConfig;
        fileFilter_FC.setValue(fileChooserConfig.getShowDirectoryName());
        hideFileType_FC.setValue(fileChooserConfig.getShowHideFile());
        // 设置鼠标悬停提示
        setToolTip();
        selectFile(new File(path));
    }

    /**
     * 选择文件
     *
     * @param file 列表选中的数据
     */
    private void selectFile(File file) {
        removeAll();
        FileConfig fileConfig = new FileConfig();
        fileConfig.setFilterNameCase(filterNameCase_FC.isSelected())
                .setShowDirectoryName(fileFilter_FC.getValue())
                .setFileNameFilter(fileNameFilter_FC.getText())
                .setShowHideFile(hideFileType_FC.getValue())
                .setFileNameType(fileNameType_FC.getValue())
                .setReverseFileName(reverse_FC.isSelected())
                .setInFile(file);
        TaskBean<FileBean> taskBean = new TaskBean<>();
        taskBean.setProgressBar(progressBar_FC)
                .setMassageLabel(fileNumber_FC)
                .setDisableNodes(disableNodes)
                .setTableView(tableView_FC)
                .setSortType(sort_type)
                .setReverseSort(true);
        readAllFilesTask = readAllFilesTask(taskBean, fileConfig);
        bindingTaskNode(readAllFilesTask, taskBean);
        readAllFilesTask.setOnSucceeded(event -> {
            taskUnbind(taskBean);
            try {
                addRemoveSameFile(readAllFilesTask.getValue(), false, tableView_FC);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 获取所选文件路径
            setPathLabel(filePath_FC, file.getPath());
            updateTableViewSizeText(tableView_FC, fileNumber_FC, text_file);
        });
        readAllFilesTask.setOnFailed(event -> {
            taskNotSuccess(taskBean, text_taskFailed);
            throw new RuntimeException(event.getSource().getException());
        });
        readAllFilesTask.setOnCancelled(event ->
                taskNotSuccess(taskBean, text_taskCancelled));
        if (!readAllFilesTask.isRunning()) {
            Thread.ofVirtual()
                    .name("readAllFilesTask-vThread" + tabId)
                    .start(readAllFilesTask);
        }
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
        progressBarVBox_FC.setMaxWidth(tableWidth * 0.4);
        progressBarVBox_FC.setPrefWidth(tableWidth * 0.4);
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
        if (readAllFilesTask != null && readAllFilesTask.isRunning()) {
            readAllFilesTask.cancel();
            readAllFilesTask = null;
        }
        tableView_FC.getItems().stream().parallel().forEach(FileBean::clearResources);
        removeTableViewData(tableView_FC, fileNumber_FC, null);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_close, close_FC);
        addToolTip(tip_confirm, confirm_FC);
        addToolTip(tip_FC.getText(), tip_FC);
        addToolTip(reverse_FC.getText(), reverse_FC);
        addToolTip(tip_selectPath, selectPathButton_FC);
        addToolTip(tip_gotoParent, gotoParentButton_FC);
        addToolTip(tip_reselectButton, refreshButton_FC);
        addToolTip(tip_fileNameFilter, fileNameFilter_FC);
        addToolTip(filterNameCase_FC.getText(), filterNameCase_FC);
        addValueToolTip(fileNameType_FC, tip_fileNameType, fileNameType_FC.getValue());
        addValueToolTip(hideFileType_FC, tip_hideFileType, hideFileType_FC.getValue());
        addValueToolTip(fileFilter_FC, tip_directoryNameType, fileFilter_FC.getValue());
    }

    /**
     * 双击列表数据进入点击的目录
     *
     * @param fileBean 列表数据
     * @throws IOException io异常
     */
    private void handleFileDoubleClick(FileBean fileBean) throws IOException {
        if (fileBean != null) {
            // 双击文件夹时进入下级目录
            if (new File(fileBean.getPath()).isDirectory()) {
                selectFile(new File(fileBean.getPath()));
            } else {
                openDirectory(fileBean.getPath());
            }
        }
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(close_FC);
        disableNodes.add(confirm_FC);
        disableNodes.add(fileFilter_FC);
        disableNodes.add(hideFileType_FC);
        disableNodes.add(fileNameType_FC);
        disableNodes.add(refreshButton_FC);
        disableNodes.add(fileNameFilter_FC);
        disableNodes.add(selectPathButton_FC);
        disableNodes.add(gotoParentButton_FC);
    }

    /**
     * 构建右键菜单
     *
     * @param tableView 要添加右键菜单的列表
     */
    public void tableViewContextMenu(TableView<FileBean> tableView) {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 查询所选文件选项
        buildSelectPathItem(tableView, contextMenu);
        // 查看文件选项
        buildFilePathItem(tableView, contextMenu);
        // 取消选中选项
        buildClearSelectedData(tableView, contextMenu);
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(contextMenu, tableView);
    }

    /**
     * 查询所选第一行文件
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单
     */
    private void buildSelectPathItem(TableView<FileBean> tableView, ContextMenu contextMenu) {
        MenuItem selectPathItem = new MenuItem("查询所选第一行文件");
        selectPathItem.setOnAction(event -> {
            FileBean selectedItem = tableView.getSelectionModel().getSelectedItems().getFirst();
            try {
                handleFileDoubleClick(selectedItem);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        contextMenu.getItems().add(selectPathItem);
    }

    /**
     * 页面关闭事件处理逻辑
     */
    private void closeRequest() {
        try {
            removeAll();
            updateProperties(fileChooserConfig.getConfigPath(), fileChooserConfig.getPathKey(), filePath_FC.getText());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        // 清理监听器引用
        tableView_FC.setRowFactory(tv -> null);
        fileNameFilter_FC.textProperty().removeListener(textFieldChangeListener);
        ContextMenu contextMenu = tableView_FC.getContextMenu();
        if (contextMenu != null) {
            // 清除所有菜单项事件
            contextMenu.getItems().stream().parallel().forEach(item -> item.setOnAction(null));
            tableView_FC.setContextMenu(null);
        }
    }

    /**
     * 设置列表双击事件
     */
    private void setRowDoubleClick() {
        tableView_FC.setRowFactory(tv -> {
            TableRow<FileBean> row = new TableRow<>();
            // 双击事件
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    FileBean selectedFileBean = row.getItem();
                    try {
                        handleFileDoubleClick(selectedFileBean);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            // 更新行样式方法
            row.itemProperty().addListener((obs, oldItem, newItem) ->
                    updateRowStyle(row));
            row.selectedProperty().addListener((obs, wasSelected, isSelected) ->
                    updateRowStyle(row));
            return row;
        });
    }

    /**
     * 更新行样式
     *
     * @param row 要更新的行
     */
    private void updateRowStyle(TableRow<FileBean> row) {
        FileBean item = row.getItem();
        if (item == null) {
            row.setStyle("");
            return;
        }
        if (row.isSelected()) {
            // 选中行的高亮样式
            row.setStyle("");
        } else {
            // 根据文件类型设置样式
            String fileType = item.getFileType();
            if (extension_folder.equals(fileType)) {
                row.setStyle("-fx-background-color: #e6f7ff;");
            } else {
                row.setStyle("");
            }
        }
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            stage = (Stage) anchorPane_FC.getScene().getWindow();
            // 设置页面关闭事件处理逻辑
            stage.setOnCloseRequest(e -> closeRequest());
            // 组件自适应宽高
            adaption();
            // 设置要防重复点击的组件
            setDisableNodes();
            // 绑定表格数据
            autoBuildTableViewData(tableView_FC, FileBean.class, tabId, index_FC);
            // 设置文件大小排序
            fileSizeColum(size_FC);
            // 设置列表双击事件
            setRowDoubleClick();
            // 构建右键菜单
            tableViewContextMenu(tableView_FC);
            // 给输入框添加内容变化监听
            textFieldChangeListener = textFieldValueListener(fileNameFilter_FC, tip_fileNameFilter);
        });
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        File file = files.getFirst();
        try {
            if (file.isFile()) {
                selectFile(file.getParentFile());
            } else if (file.isDirectory()) {
                selectFile(file);
            }
        } catch (Exception e) {
            showExceptionAlert(e);
        }
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void acceptDrop(DragEvent dragEvent) {
        // 接受拖放
        dragEvent.acceptTransferModes(TransferMode.COPY);
        dragEvent.consume();
    }

    /**
     * 选择要查询的文件夹
     *
     * @param actionEvent 点击事件
     */
    @FXML
    private void selectFilePath(ActionEvent actionEvent) {
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatDirectoryChooser(window, filePath_FC.getText(), text_selectDirectory);
        if (selectedFile != null) {
            selectFile(selectedFile);
        }
    }

    /**
     * 前往上级文件夹
     */
    @FXML
    private void gotoParent() {
        File file = new File(filePath_FC.getText());
        File parentFile = file.getParentFile();
        selectFile(parentFile);
    }

    /**
     * 确认选择的文件按钮
     *
     * @throws IOException io异常
     */
    @FXML
    private void confirmSelect() throws IOException {
        ObservableList<FileBean> selectedItems = tableView_FC.getSelectionModel().getSelectedItems();
        List<FileBean> fileBeanList = new ArrayList<>();
        String showDirectory = fileChooserConfig.getShowDirectoryName();
        if (CollectionUtils.isNotEmpty(selectedItems)) {
            for (FileBean fileBean : selectedItems) {
                String fileType = fileBean.getFileType();
                if (text_onlyDirectory.equals(showDirectory)) {
                    if (extension_folder.equals(fileType)) {
                        fileBeanList.add(fileBean);
                    }
                } else if (text_onlyFile.equals(showDirectory)) {
                    if (!extension_folder.equals(fileType)) {
                        List<String> filterExtensionList = fileChooserConfig.getFilterExtensionList();
                        if (CollectionUtils.isEmpty(filterExtensionList) || filterExtensionList.contains(fileType)) {
                            fileBeanList.add(fileBean);
                        }
                    }
                }
            }
        } else {
            // 列表为空时，选择当前目录
            File file = new File(filePath_FC.getText());
            FileBean fileBean = creatFileBean(tableView_FC, file);
            fileBeanList.add(fileBean);
        }
        closeStage(stage, this::closeRequest);
        // 触发列表刷新
        if (fileChooserCallback != null) {
            fileChooserCallback.onFileChooser(fileBeanList);
        }
    }

    /**
     * 取消按钮
     */
    @FXML
    private void closeWindow() {
        closeStage(stage, this::closeRequest);
    }

    /**
     * 刷新列表按钮
     */
    @FXML
    private void refreshTable() {
        selectFile(new File(filePath_FC.getText()));
    }

    /**
     * 过滤条件单选框监听
     */
    @FXML
    private void fileFilterAction() {
        addValueToolTip(fileFilter_FC, tip_directoryNameType, fileFilter_FC.getValue());
        refreshTable();
    }

    /**
     * 隐藏文件查询设置单选框监听
     */
    @FXML
    private void hideFileTypeAction() {
        addValueToolTip(hideFileType_FC, tip_hideFileType, hideFileType_FC.getValue());
        refreshTable();
    }

    /**
     * 文件名查询设置单选框监听
     */
    @FXML
    private void fileNameTypeAction() {
        addValueToolTip(fileNameType_FC, tip_fileNameType, fileNameType_FC.getValue());
        refreshTable();
    }

    /**
     * 反向查询文件名开关
     */
    @FXML
    private void reverseAction() {
        refreshTable();
    }

    /**
     * 区分大小写开关
     */
    @FXML
    private void filterNameCaseAction() {
        refreshTable();
    }

}
