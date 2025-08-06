package priv.koishi.tools.Controller;

import javafx.application.Platform;
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
import priv.koishi.tools.Configuration.FileConfig;

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
import static priv.koishi.tools.Service.MoveFileService.moveFile;
import static priv.koishi.tools.Service.ReadDataService.readFile;
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

    /**
     * 读取文件线程
     */
    private Task<Void> readFileTask;

    @FXML
    public AnchorPane anchorPane_MV;

    @FXML
    public ProgressBar progressBar_MV;

    @FXML
    public HBox fileNumberHBox_MV, tipHBox_MV, filterHBox_MV;

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
            prop.put(key_lastDirectoryNameType, addFileType_MV.getValue());
            String openDirectoryValue = openDirectory_MV.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, openDirectoryValue);
            prop.put(key_lastFilterFileType, filterFileType_MV.getText());
            prop.put(key_lastOutPath, outPath_MV.getText());
            prop.put(key_moveType, moveType_MV.getValue());
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
            setControlLastConfig(openDirectory_MV, prop, key_lastOpenDirectory);
            setControlLastConfig(filterFileType_MV, prop, key_lastFilterFileType);
            setControlLastConfig(addFileType_MV, prop, key_lastDirectoryNameType);
        }
        input.close();
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_learButton, clearButton_MV);
        addToolTip(tip_exportButton, moveButton_MV);
        addToolTip(tip_outPathButton, outPathButton_MV);
        addToolTip(tip_openDirectory, openDirectory_MV);
        addToolTip(tip_reselectButton, addFileButton_MV);
        addToolTip(tip_filterFileType, filterFileType_MV);
        addToolTip(tip_directoryNameType, addFileType_MV);
    }

    /**
     * 设置javafx单元格宽度
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
     * 添加数据渲染列表
     *
     * @param inFileList 查询到的文件list
     * @throws Exception 未查询到符合条件的数据
     */
    private void addInData(List<File> inFileList) throws Exception {
        if (readFileTask == null) {
            removeAll();
            if (inFileList.isEmpty()) {
                throw new Exception(text_selectNull);
            }
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
                    .setInFileList(inFileList)
                    .setSortType(sortValue)
                    .setTabId(tabId);
            // 获取Task任务
            readFileTask = readFile(taskBean);
            // 绑定带进度条的线程
            bindingTaskNode(readFileTask, taskBean);
            readFileTask.setOnSucceeded(event -> {
                taskUnbind(taskBean);
                readFileTask = null;
            });
            if (!readFileTask.isRunning()) {
                Thread.ofVirtual()
                        .name("readFileTask-vThread" + tabId)
                        .start(readFileTask);
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
            // 设置要防重复点击的组件
            setDisableNodes();
            // 绑定表格数据
            autoBuildTableViewData(tableView_MV, FileBean.class, tabId, index_MV);
            // 设置文件大小排序
            fileSizeColum(size_MV);
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_MV);
            // 构建右键菜单
            tableViewContextMenu(tableView_MV, fileNumber_MV);
        });
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) throws Exception {
        removeAll();
        List<File> files = dragEvent.getDragboard().getFiles();
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_MV);
        File file = files.getFirst();
        FileConfig fileConfig = new FileConfig();
        fileConfig.setShowDirectoryName(addFileType_MV.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setInFile(file);
        addInData(readAllFiles(fileConfig));
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
     * 清空列表按钮功能
     */
    @FXML
    private void removeAll() {
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
        Thread.ofVirtual()
                .name("task-moveFile-vThread")
                .start(moveFileTask);
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
            addFile(files, false, tableView_MV);
            updateTableViewSizeText(tableView_MV, fileNumber_MV, text_file);
        } else if (text_addDirectory.equals(addFileType)) {
            FileChooserConfig fileConfig = new FileChooserConfig();
            fileConfig.setPathKey(key_inFilePath)
                    .setConfigFile(configFile_MV)
                    .setTitle(text_selectDirectory)
                    .setInFile(new File(inFilePath))
                    .setShowHideFile(text_noHideFile)
                    .setShowDirectoryName(text_onlyDirectory);
            FileChooserController controller = chooserFiles(fileConfig);
            // 设置回调
            controller.setFileChooserCallback(fileBeanList -> {
                // 根据文件路径去重
                removeSameFilePath(tableView_MV, fileBeanList);
                updateTableViewSizeText(tableView_MV, fileNumber_MV, text_file);
            });
        }
    }

    /**
     * 添加数据设置单选框监听
     */
    @FXML
    private void ddFileTypeAction() {
        String addFileType = addFileType_MV.getValue();
        filterHBox_MV.setVisible(text_addDirectory.equals(addFileType));
    }

}
