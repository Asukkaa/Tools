package priv.koishi.tools.Controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
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
import javafx.scene.paint.Color;
import javafx.stage.Window;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.FileChooserConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static priv.koishi.tools.Controller.MainController.settingController;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.MainApplication.mainScene;
import static priv.koishi.tools.MainApplication.mainStage;
import static priv.koishi.tools.Service.MoveFileService.*;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.TaskUtils.bindingTaskNode;
import static priv.koishi.tools.Utils.TaskUtils.taskUnbind;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 移动文件工具页控制器
 *
 * @author KOISHI
 * Date:2025-08-04
 * Time:12:22
 */
public class MoveFileController extends RootController {

    /**
     * 目标文件路径
     */
    public static String outFilePath;

    /**
     * 上次选择的文件路径
     */
    public static String inFilePath;

    /**
     * 页面标识符
     */
    private static final String tabId = "_MV";

    /**
     * 要防重复点击的组件
     */
    private static final List<Node> disableNodes = new ArrayList<>();

    @FXML
    public AnchorPane anchorPane_MV;

    @FXML
    public ProgressBar progressBar_MV;

    @FXML
    public HBox fileNumberHBox_MV, filterHBox_MV;

    @FXML
    public TableView<FileBean> tableView_MV;

    @FXML
    public TableColumn<FileBean, ImageView> thumb_MV;

    @FXML
    public TableColumn<FileBean, Integer> index_MV;

    @FXML
    public TableColumn<FileBean, String> name_MV, path_MV, size_MV, fileType_MV,
            creatDate_MV, updateDate_MV, showStatus_MV;

    @FXML
    public TextField filterFileType_MV;

    @FXML
    public Label outPath_MV, fileNumber_MV, log_MV;

    @FXML
    public CheckBox openDirectory_MV;

    @FXML
    public Button clearButton_MV, moveButton_MV, addFileButton_MV, outPathButton_MV;

    @FXML
    public ChoiceBox<String> addFileType_MV, sourceAction_MV, moveType_MV, hideFileType_MV;

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tableView_MV.setPrefHeight(stageHeight * 0.55);
        // 设置组件宽度
        double stageWidth = mainStage.getWidth();
        double tableWidth = stageWidth * 0.94;
        tableView_MV.setMaxWidth(tableWidth);
        tableView_MV.setPrefWidth(tableWidth);
        regionRightAlignment(fileNumberHBox_MV, tableWidth, fileNumber_MV);
        // 设置javafx单元格宽度
        bindPrefWidthProperty();
    }

    /**
     * 设置列表各列宽度
     */
    private void bindPrefWidthProperty() {
        index_MV.prefWidthProperty().bind(tableView_MV.widthProperty().multiply(0.04));
        thumb_MV.prefWidthProperty().bind(tableView_MV.widthProperty().multiply(0.1));
        name_MV.prefWidthProperty().bind(tableView_MV.widthProperty().multiply(0.14));
        fileType_MV.prefWidthProperty().bind(tableView_MV.widthProperty().multiply(0.08));
        path_MV.prefWidthProperty().bind(tableView_MV.widthProperty().multiply(0.2));
        size_MV.prefWidthProperty().bind(tableView_MV.widthProperty().multiply(0.08));
        showStatus_MV.prefWidthProperty().bind(tableView_MV.widthProperty().multiply(0.08));
        creatDate_MV.prefWidthProperty().bind(tableView_MV.widthProperty().multiply(0.14));
        updateDate_MV.prefWidthProperty().bind(tableView_MV.widthProperty().multiply(0.14));
    }

    /**
     * 保存最后一次配置的值
     *
     * @throws IOException io异常
     */
    public void saveLastConfig() throws IOException {
        if (anchorPane_MV != null) {
            InputStream input = checkRunningInputStream(configFile_MV);
            Properties prop = new Properties();
            prop.load(input);
            prop.put(key_moveType, moveType_MV.getValue());
            prop.put(key_lastOutPath, outPath_MV.getText());
            prop.put(key_addFileType, addFileType_MV.getValue());
            prop.put(key_sourceAction, sourceAction_MV.getValue());
            prop.put(key_lastHideFileType, hideFileType_MV.getValue());
            prop.put(key_lastFilterFileType, filterFileType_MV.getText());
            String openDirectoryValue = openDirectory_MV.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, openDirectoryValue);
            OutputStream output = checkRunningOutputStream(configFile_MV);
            prop.store(output, null);
            input.close();
            output.close();
        }
    }

    /**
     * 读取配置文件
     *
     * @throws IOException io异常
     */
    private void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_MV);
        prop.load(input);
        outFilePath = prop.getProperty(key_outFilePath);
        inFilePath = prop.getProperty(key_inFilePath);
        input.close();
    }

    /**
     * 设置初始配置值为上次配置值
     *
     * @throws IOException io异常
     */
    private void setLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_MV);
        prop.load(input);
        if (activation.equals(prop.getProperty(key_loadLastConfig))) {
            setControlLastConfig(moveType_MV, prop, key_moveType);
            setControlLastConfig(outPath_MV, prop, key_lastOutPath);
            setControlLastConfig(addFileType_MV, prop, key_addFileType);
            setControlLastConfig(sourceAction_MV, prop, key_sourceAction);
            setControlLastConfig(hideFileType_MV, prop, key_lastHideFileType);
            setControlLastConfig(openDirectory_MV, prop, key_lastOpenDirectory);
            setControlLastConfig(filterFileType_MV, prop, key_lastFilterFileType);
        }
        input.close();
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_moveType, moveType_MV);
        addToolTip(tip_moveButton, moveButton_MV);
        addToolTip(tip_learButton, clearButton_MV);
        addToolTip(tip_movePath, outPathButton_MV);
        addToolTip(tip_addFileType, addFileType_MV);
        addToolTip(tip_sourceAction, sourceAction_MV);
        addToolTip(tip_hideFileType, hideFileType_MV);
        addToolTip(tip_openDirectory, openDirectory_MV);
        addToolTip(tip_addFileButton, addFileButton_MV);
        addToolTip(tip_filterFileType, filterFileType_MV);
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(moveButton_MV);
        disableNodes.add(filterHBox_MV);
        disableNodes.add(sourceAction_MV);
        disableNodes.add(clearButton_MV);
        disableNodes.add(outPathButton_MV);
        disableNodes.add(addFileButton_MV);
        Node autoClickTab = mainScene.lookup("#autoClickTab");
        disableNodes.add(autoClickTab);
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 鼠标悬留提示输入的需要识别的文件后缀名
        textFieldValueListener(filterFileType_MV, tip_filterFileType);
    }

    /**
     * 创建任务参数
     *
     * @param files 要处理的文件列表
     */
    private TaskBean<FileBean> creatTaskBean(List<File> files) {
        ChoiceBox<String> sort = settingController.sort_Set;
        String sortValue = sort.getValue();
        CheckBox reverseSort = settingController.reverseSort_Set;
        TaskBean<FileBean> taskBean = new TaskBean<>();
        taskBean.setReverseSort(reverseSort.isSelected())
                .setComparatorTableColumn(size_MV)
                .setProgressBar(progressBar_MV)
                .setMassageLabel(fileNumber_MV)
                .setDisableNodes(disableNodes)
                .setTableView(tableView_MV)
                .setSortType(sortValue)
                .setInFileList(files)
                .setTabId(tabId);
        return taskBean;
    }

    /**
     * 构建右键菜单
     *
     * @param tableView 要添加右键菜单的列表
     * @param label     列表对应的统计信息展示栏
     */
    public void tableViewContextMenu(TableView<FileBean> tableView, Label label) {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 查看文件选项
        buildFilePathItem(tableView, contextMenu);
        // 取消选中选项
        buildClearSelectedData(tableView, contextMenu);
        // 删除所选数据选项
        buildDeleteDataMenuItem(tableView, label, contextMenu, text_file);
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(contextMenu, tableView);
    }

    /**
     * 界面初始化
     *
     * @throws IOException io异常
     */
    @FXML
    private void initialize() throws IOException {
        // 读取全局变量配置
        getConfig();
        // 设置鼠标悬停提示
        setToolTip();
        // 给输入框添加内容变化监听
        textFieldChangeListener();
        // 设置初始配置值为上次配置值
        setLastConfig();
        Platform.runLater(() -> {
            // 组件自适应宽高
            adaption();
            ddFileTypeAction();
            // 设置要防重复点击的组件
            setDisableNodes();
            // 绑定表格数据
            autoBuildTableViewData(tableView_MV, FileBean.class, tabId, index_MV);
            // 设置文件大小排序
            fileSizeColum(size_MV);
            // 构建右键菜单
            tableViewContextMenu(tableView_MV, fileNumber_MV);
            // 监听列表数据变化
            tableView_MV.getItems().addListener((ListChangeListener<FileBean>) change ->
                    addFileType_MV.setDisable(!tableView_MV.getItems().isEmpty()));
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
        TaskBean<FileBean> taskBean = creatTaskBean(files);
        Task<Void> readFileTask = readMoveFile(taskBean);
        bindingTaskNode(readFileTask, taskBean);
        readFileTask.setOnSucceeded(event -> {
            taskUnbind(taskBean);
            // 更新文件数量
            updateTableViewSizeText(tableView_MV, fileNumber_MV, text_file);
        });
        addFileType_MV.setDisable(true);
        if (!readFileTask.isRunning()) {
            Thread.ofVirtual()
                    .name("readFileTask-vThread" + tabId)
                    .start(readFileTask);
        }
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void acceptDrop(DragEvent dragEvent) {
        dragEvent.acceptTransferModes(TransferMode.COPY);
        dragEvent.consume();
    }

    /**
     * 清空列表按钮功能
     */
    @FXML
    private void removeAll() {
        tableView_MV.getItems().stream().parallel().forEach(FileBean::clearResources);
        removeTableViewData(tableView_MV, fileNumber_MV, log_MV);
    }

    /**
     * 移动文件按钮
     *
     * @throws IOException 获取文件属性异常、创建文件夹失败
     */
    @FXML
    private void moveAll() throws Exception {
        String path = outPath_MV.getText();
        if (StringUtils.isBlank(path)) {
            throw new Exception("请选择目标文件夹");
        }
        File targetDirectory = new File(path);
        if (!targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                throw new Exception("创建文件夹 " + targetDirectory + " 失败");
            }
        }
        ObservableList<FileBean> items = tableView_MV.getItems();
        if (CollectionUtils.isEmpty(items)) {
            throw new Exception("请选择要移动的文件或文件夹");
        }
        TaskBean<FileBean> taskBean = new TaskBean<>();
        taskBean.setProgressBar(progressBar_MV)
                .setDisableNodes(disableNodes)
                .setTableView(tableView_MV)
                .setMassageLabel(log_MV)
                .setBeanList(items);
        Task<Void> moveFileTask = moveFile(taskBean);
        bindingTaskNode(moveFileTask, taskBean);
        moveFileTask.setOnSucceeded(event -> {
            taskUnbind(taskBean);
            if (openDirectory_MV.isSelected()) {
                openDirectory(path);
            }
            log_MV.setTextFill(Color.GREEN);
        });
        if (!moveFileTask.isRunning()) {
            Thread.ofVirtual()
                    .name("moveFileTask-vThread" + tabId)
                    .start(moveFileTask);
        }
    }

    /**
     * 设置导出文件按钮
     *
     * @param actionEvent 点击事件
     * @throws IOException io异常
     */
    @FXML
    private void targetPath(ActionEvent actionEvent) throws IOException {
        getConfig();
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatDirectoryChooser(window, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_MV, configFile_MV);
        }
    }

    /**
     * 添加文件按钮
     *
     * @param actionEvent 点击事件
     * @throws IOException io异常
     */
    @FXML
    private void addFiles(ActionEvent actionEvent) throws IOException {
        getConfig();
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        String addFileType = addFileType_MV.getValue();
        if (text_addFile.equals(addFileType)) {
            List<File> files = creatFilesChooser(window, inFilePath, null, text_selectFile);
            TaskBean<FileBean> taskBean = creatTaskBean(files);
            Task<Void> addFileTask = addMoveFile(taskBean, false);
            bindingTaskNode(addFileTask, taskBean);
            addFileTask.setOnSucceeded(event -> {
                taskUnbind(taskBean);
                // 更新文件数量
                updateTableViewSizeText(tableView_MV, fileNumber_MV, text_file);
            });
            addFileType_MV.setDisable(true);
            if (!addFileTask.isRunning()) {
                Thread.ofVirtual()
                        .name("addFileTask-vThread" + tabId)
                        .start(addFileTask);
            }
        } else if (text_addDirectory.equals(addFileType)) {
            FileChooserConfig fileConfig = new FileChooserConfig();
            fileConfig.setPathKey(key_inFilePath)
                    .setConfigPath(configFile_MV)
                    .setTitle(text_selectDirectory)
                    .setInFile(new File(inFilePath))
                    .setShowHideFile(text_noHideFile)
                    .setShowDirectoryName(text_onlyDirectory);
            FileChooserController controller = chooserFiles(fileConfig);
            // 设置回调
            controller.setFileChooserCallback(fileBeanList -> {
                TaskBean<FileBean> taskBean = creatTaskBean(null);
                taskBean.setBeanList(fileBeanList);
                Task<Void> removeSameFileTask = removeSameMoveFile(taskBean);
                bindingTaskNode(removeSameFileTask, taskBean);
                removeSameFileTask.setOnSucceeded(event -> {
                    taskUnbind(taskBean);
                    // 更新文件数量
                    updateTableViewSizeText(tableView_MV, fileNumber_MV, text_file);
                });
                addFileType_MV.setDisable(true);
                if (!removeSameFileTask.isRunning()) {
                    Thread.ofVirtual()
                            .name("removeSameFileTask-vThread" + tabId)
                            .start(removeSameFileTask);
                }
            });
        }
    }

    /**
     * 添加数据设置单选框监听
     */
    @FXML
    private void ddFileTypeAction() {
        String addFileType = addFileType_MV.getValue();
        String sourceAction = sourceAction_MV.getValue();
        if (text_addDirectory.equals(addFileType)) {
            addFileButton_MV.setText(text_selectMoveFolder);
            filterHBox_MV.setVisible(true);
            ObservableList<String> items = sourceAction_MV.getItems();
            if (!items.contains(sourceAction_deleteFolder) || !items.contains(sourceAction_trashFolder)) {
                items.addAll(sourceAction_trashFolder, sourceAction_deleteFolder);
            }
        } else if (text_addFile.equals(addFileType)) {
            addFileButton_MV.setText(text_selectMoveFile);
            filterHBox_MV.setVisible(false);
            ObservableList<String> items = sourceAction_MV.getItems();
            items.removeAll(sourceAction_deleteFolder, sourceAction_trashFolder);
            if (!items.contains(sourceAction)) {
                sourceAction_MV.setValue(sourceAction_saveFile);
            }
        }
    }

}
