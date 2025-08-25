package priv.koishi.tools.Controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.collections4.CollectionUtils;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.CopyConfig;
import priv.koishi.tools.Configuration.FileChooserConfig;
import priv.koishi.tools.MainApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static priv.koishi.tools.Controller.MainController.settingController;
import static priv.koishi.tools.Enum.SelectItemsEnums.*;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.MainApplication.mainScene;
import static priv.koishi.tools.MainApplication.mainStage;
import static priv.koishi.tools.Service.CopyFileService.copyFile;
import static priv.koishi.tools.Service.ReadDataService.readFile;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;
import static priv.koishi.tools.Utils.UiUtils.addToolTip;

/**
 * 复制文件工具控制器
 *
 * @author KOISHI
 * Date:2025-08-19
 * Time:14:35
 */
public class CopyFileController extends RootController {

    /**
     * 目标文件路径
     */
    public String outFilePath;

    /**
     * 上次选择的文件路径
     */
    public String inFilePath;

    /**
     * 详情页高度
     */
    private int detailHeight;

    /**
     * 详情页宽度
     */
    private int detailWidth;

    /**
     * 页面标识符
     */
    private final String tabId = "_CP";

    /**
     * 复制文件线程任务
     */
    private Task<List<String>> copyFileTask;

    /**
     * 要防重复点击的组件
     */
    private final List<Node> disableNodes = new ArrayList<>();

    @FXML
    public AnchorPane anchorPane_CP;

    @FXML
    public ProgressBar progressBar_CP;

    @FXML
    public HBox fileNumberHBox_CP, filterHBox_CP;

    @FXML
    public TableView<FileBean> tableView_CP;

    @FXML
    public TableColumn<FileBean, ImageView> thumb_CP;

    @FXML
    public TableColumn<FileBean, Integer> index_CP;

    @FXML
    public TableColumn<FileBean, String> name_CP, path_CP, size_CP, fileType_CP, creatDate_CP, updateDate_CP,
            showStatus_CP;

    @FXML
    public Label outPath_CP, fileNumber_CP, log_CP;

    @FXML
    public TextField filterFileType_CP, prefix_CP, tag_CP, copyNum_CP;

    @FXML
    public CheckBox openDirectory_CP, addSpace_CP, reverseFileType_CP, firstRename_CP;

    @FXML
    public ChoiceBox<String> addFileType_CP, copyType_CP, hideFileType_CP, differenceCode_CP, subCode_CP;

    @FXML
    public Button clearButton_CP, copyButton_CP, addFileButton_CP, outPathButton_CP, updateCopy_CP, cancelButton_CP,
            removePathButton_CP;

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tableView_CP.setPrefHeight(stageHeight * 0.55);
        // 设置组件宽度
        double stageWidth = mainStage.getWidth();
        double tableWidth = stageWidth * 0.94;
        tableView_CP.setMaxWidth(tableWidth);
        tableView_CP.setPrefWidth(tableWidth);
        regionRightAlignment(fileNumberHBox_CP, tableWidth, fileNumber_CP);
        // 设置javafx单元格宽度
        bindPrefWidthProperty();
    }

    /**
     * 设置列表各列宽度
     */
    private void bindPrefWidthProperty() {
        index_CP.prefWidthProperty().bind(tableView_CP.widthProperty().multiply(0.04));
        thumb_CP.prefWidthProperty().bind(tableView_CP.widthProperty().multiply(0.1));
        name_CP.prefWidthProperty().bind(tableView_CP.widthProperty().multiply(0.14));
        fileType_CP.prefWidthProperty().bind(tableView_CP.widthProperty().multiply(0.08));
        path_CP.prefWidthProperty().bind(tableView_CP.widthProperty().multiply(0.2));
        size_CP.prefWidthProperty().bind(tableView_CP.widthProperty().multiply(0.08));
        showStatus_CP.prefWidthProperty().bind(tableView_CP.widthProperty().multiply(0.08));
        creatDate_CP.prefWidthProperty().bind(tableView_CP.widthProperty().multiply(0.14));
        updateDate_CP.prefWidthProperty().bind(tableView_CP.widthProperty().multiply(0.14));
    }

    /**
     * 保存最后一次配置的值
     *
     * @throws IOException io异常
     */
    public void saveLastConfig() throws IOException {
        if (anchorPane_CP != null) {
            InputStream input = checkRunningInputStream(configFile_CP);
            Properties prop = new Properties();
            prop.load(input);
            prop.put(key_lastTag, tag_CP.getText());
            prop.put(key_lastPrefix, prefix_CP.getText());
            prop.put(key_moveType, copyType_CP.getValue());
            prop.put(key_lastOutPath, outPath_CP.getText());
            prop.put(key_lastSubCode, subCode_CP.getValue());
            prop.put(key_addFileType, addFileType_CP.getValue());
            prop.put(key_lastHideFileType, hideFileType_CP.getValue());
            prop.put(key_lastFilterFileType, filterFileType_CP.getText());
            prop.put(key_lastDifferenceCode, differenceCode_CP.getValue());
            String addSpaceValue = addSpace_CP.isSelected() ? activation : unActivation;
            prop.put(key_lastAddSpace, addSpaceValue);
            String firstRenameValue = firstRename_CP.isSelected() ? activation : unActivation;
            prop.put(key_firstRename, firstRenameValue);
            String openDirectoryValue = openDirectory_CP.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, openDirectoryValue);
            String reverseFileTypeValue = reverseFileType_CP.isSelected() ? activation : unActivation;
            prop.put(key_reverseFileType, reverseFileTypeValue);
            OutputStream output = checkRunningOutputStream(configFile_CP);
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
        InputStream input = checkRunningInputStream(configFile_CP);
        prop.load(input);
        inFilePath = prop.getProperty(key_inFilePath);
        outFilePath = prop.getProperty(key_outFilePath);
        detailWidth = Integer.parseInt(prop.getProperty(key_detailWidth));
        detailHeight = Integer.parseInt(prop.getProperty(key_detailHeight));
        input.close();
    }

    /**
     * 设置初始配置值为上次配置值
     *
     * @throws IOException io异常
     */
    private void setLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_CP);
        prop.load(input);
        if (activation.equals(prop.getProperty(key_loadLastConfig))) {
            setControlLastConfig(tag_CP, prop, key_lastTag);
            setControlLastConfig(copyType_CP, prop, key_moveType);
            setControlLastConfig(prefix_CP, prop, key_lastPrefix);
            setControlLastConfig(outPath_CP, prop, key_lastOutPath);
            setControlLastConfig(subCode_CP, prop, key_lastSubCode);
            setControlLastConfig(addSpace_CP, prop, key_lastAddSpace);
            setControlLastConfig(addFileType_CP, prop, key_addFileType);
            setControlLastConfig(firstRename_CP, prop, key_firstRename);
            setControlLastConfig(hideFileType_CP, prop, key_lastHideFileType);
            setControlLastConfig(openDirectory_CP, prop, key_lastOpenDirectory);
            setControlLastConfig(reverseFileType_CP, prop, key_reverseFileType);
            setControlLastConfig(filterFileType_CP, prop, key_lastFilterFileType);
            setControlLastConfig(differenceCode_CP, prop, key_lastDifferenceCode);
        }
        input.close();
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_tag, tag_CP);
        addToolTip(tip_prefix, prefix_CP);
        addToolTip(tip_copyNum, copyNum_CP);
        addToolTip(tip_addSpace, addSpace_CP);
        addToolTip(tip_updateCopy, updateCopy_CP);
        addToolTip(tip_copyButton, copyButton_CP);
        addToolTip(tip_learButton, clearButton_CP);
        addToolTip(tip_copyPath, outPathButton_CP);
        addToolTip(tip_firstRename, firstRename_CP);
        addToolTip(tip_openDirectory, openDirectory_CP);
        addToolTip(tip_filterFileType, filterFileType_CP);
        addToolTip(tip_addCopyFileButton, addFileButton_CP);
        addToolTip(tip_removePathButton, removePathButton_CP);
        addToolTip(reverseFileType_CP.getText(), reverseFileType_CP);
        addValueToolTip(subCode_CP, tip_subCode, subCode_CP.getValue());
        addValueToolTip(copyType_CP, tip_moveType, copyType_CP.getValue());
        addValueToolTip(addFileType_CP, tip_addFileType, addFileType_CP.getValue());
        addValueToolTip(hideFileType_CP, tip_hideFileType, hideFileType_CP.getValue());
        addValueToolTip(differenceCode_CP, tip_differenceCode, differenceCode_CP.getValue());
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(copyButton_CP);
        disableNodes.add(filterHBox_CP);
        disableNodes.add(clearButton_CP);
        disableNodes.add(outPathButton_CP);
        disableNodes.add(addFileButton_CP);
        Node autoClickTab = mainScene.lookup("#autoClickTab");
        disableNodes.add(autoClickTab);
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 文件名前缀输入框添加鼠标悬停提示
        textFieldValueListener(prefix_CP, tip_prefix);
        // 限制重名文件尾缀输入框内容
        integerRangeTextField(tag_CP, 0, null, tip_tag);
        // 鼠标悬留提示输入的需要识别的文件后缀名
        textFieldValueListener(filterFileType_CP, tip_filterFileType);
        // 限制复制数量输入框内容
        integerRangeTextField(copyNum_CP, 1, null, tip_copyNum);
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
                .setInFileList(new ArrayList<>(files))
                .setComparatorTableColumn(size_CP)
                .setProgressBar(progressBar_CP)
                .setMassageLabel(fileNumber_CP)
                .setDisableNodes(disableNodes)
                .setTableView(tableView_CP)
                .setSortType(sortValue)
                .setTabId(tabId);
        return taskBean;
    }

    /**
     * 获取复制配置
     *
     * @return 复制配置
     */
    public CopyConfig creatCopyConfig() {
        int copyNum = setDefaultIntValue(copyNum_CP, 1, 1, null);
        int tag = setDefaultIntValue(tag_CP, 1, 0, null);
        return new CopyConfig()
                .setReverseFileType(reverseFileType_CP.isSelected())
                .setOpenDirectory(openDirectory_CP.isSelected())
                .setDifferenceCode(differenceCode_CP.getValue())
                .setFilterFileType(filterFileType_CP.getText())
                .setFirstRename(firstRename_CP.isSelected())
                .setHideFileType(hideFileType_CP.getValue())
                .setAddSpace(addSpace_CP.isSelected())
                .setCopyType(copyType_CP.getValue())
                .setSubCode(subCode_CP.getValue())
                .setOutPath(outPath_CP.getText())
                .setPrefix(prefix_CP.getText())
                .setCopyNum(copyNum)
                .setTag(tag);
    }

    /**
     * 显示详情页
     *
     * @param item 要显示详情的文件复制设置
     */
    private void showDetail(FileBean item) {
        URL fxmlLocation = getClass().getResource(resourcePath + "fxml/CopyFileDetail-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CopyFileDetailController controller = loader.getController();
        controller.initData(item, outFilePath);
        // 设置保存后的回调
        controller.setRefreshCallback(() -> {
            if (item.isRemove()) {
                tableView_CP.getItems().remove(item);
            }
            // 刷新列表
            tableView_CP.refresh();
            updateTableViewSizeText(tableView_CP, fileNumber_CP, text_process);
        });
        Stage detailStage = new Stage();
        Scene scene = new Scene(root, detailWidth, detailHeight);
        detailStage.setScene(scene);
        detailStage.setTitle(item.getName() + " 复制配置");
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResource("icon/Tools.png")).toExternalForm()));
        scene.getStylesheets().add(Objects.requireNonNull(MainApplication.class.getResource("css/Styles.css")).toExternalForm());
        detailStage.show();
    }

    /**
     * 查看文件复制设置详情
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单
     */
    private void showCopyConfigMenuItem(TableView<FileBean> tableView, ContextMenu contextMenu) {
        MenuItem detailItem = new MenuItem("查看所选项第一行详情");
        detailItem.setOnAction(e -> {
            FileBean selected = tableView.getSelectionModel().getSelectedItems().getFirst();
            if (selected != null) {
                showDetail(selected);
            }
        });
        contextMenu.getItems().add(detailItem);
    }

    /**
     * 构建右键菜单
     *
     * @param tableView 要添加右键菜单的列表
     * @param label     列表对应的统计信息展示栏
     */
    private void tableViewContextMenu(TableView<FileBean> tableView, Label label) {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 查看文件复制设置详情
        showCopyConfigMenuItem(tableView, contextMenu);
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
            autoBuildTableViewData(tableView_CP, FileBean.class, tabId, index_CP);
            // 设置文件大小排序
            fileSizeColum(size_CP);
            // 构建右键菜单
            tableViewContextMenu(tableView_CP, fileNumber_CP);
            // 监听列表数据变化
            tableView_CP.getItems().addListener((ListChangeListener<FileBean>) change ->
                    addFileType_CP.setDisable(!tableView_CP.getItems().isEmpty()));
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
        Task<List<FileBean>> addFileTask = readFile(taskBean);
        bindingTaskNode(addFileTask, taskBean);
        addFileTask.setOnSucceeded(event -> {
            addFileTask.getValue().forEach(fileBean -> fileBean.setCopyConfig(creatCopyConfig()));
            taskUnbind(taskBean);
        });
        addFileType_CP.setDisable(true);
        if (!addFileTask.isRunning()) {
            Thread.ofVirtual()
                    .name("addFileTask-vThread" + tabId)
                    .start(addFileTask);
        }
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void acceptDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        String addType = addFileType_CP.getValue();
        files.forEach(file -> {
            if (text_addFile.equals(addType) && file.isFile()) {
                dragEvent.acceptTransferModes(TransferMode.COPY);
                dragEvent.consume();
            } else if (text_addDirectory.equals(addType) && file.isDirectory()) {
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
        tableView_CP.getItems().stream().parallel().forEach(FileBean::clearResources);
        removeTableViewData(tableView_CP, fileNumber_CP, log_CP);
    }

    /**
     * 移动文件按钮
     */
    @FXML
    private void copyAll() {
        ObservableList<FileBean> items = tableView_CP.getItems();
        if (CollectionUtils.isEmpty(items)) {
            throw new RuntimeException("请选择要复制的文件或文件夹");
        }
        TaskBean<FileBean> taskBean = new TaskBean<>();
        taskBean.setCancelButton(cancelButton_CP)
                .setProgressBar(progressBar_CP)
                .setDisableNodes(disableNodes)
                .setTableView(tableView_CP)
                .setMassageLabel(log_CP)
                .setBeanList(items);
        copyFileTask = copyFile(taskBean, text_addDirectory.equals(addFileType_CP.getValue()));
        taskBean.setWorkTask(copyFileTask);
        bindingTaskNode(copyFileTask, taskBean);
        copyFileTask.setOnSucceeded(event -> {
            taskBean.getCancelButton().setVisible(false);
            taskUnbind(taskBean);
            List<String> result = copyFileTask.getValue();
            if (CollectionUtils.isEmpty(result)) {
                taskNotSuccess(taskBean, text_taskFailed);
            } else {
                for (String s : result) {
                    openDirectory(s);
                }
                taskBean.getMassageLabel().setTextFill(Color.GREEN);
            }
            copyFileTask = null;
        });
        copyFileTask.setOnFailed(event -> {
            taskNotSuccess(taskBean, text_taskFailed);
            copyFileTask = null;
            throw new RuntimeException(event.getSource().getException());
        });
        copyFileTask.setOnCancelled(event -> {
            taskNotSuccess(taskBean, text_taskCancelled);
            copyFileTask = null;
        });
        if (!copyFileTask.isRunning()) {
            Thread.ofVirtual()
                    .name("copyFileTask-vThread" + tabId)
                    .start(copyFileTask);
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
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_CP, configFile_CP);
            removePathButton_CP.setVisible(true);
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
        String addFileType = addFileType_CP.getValue();
        if (text_addFile.equals(addFileType)) {
            List<File> files = creatFilesChooser(window, inFilePath, null, text_selectFile);
            TaskBean<FileBean> taskBean = creatTaskBean(files);
            Task<List<FileBean>> addFileTask = readFile(taskBean);
            bindingTaskNode(addFileTask, taskBean);
            addFileTask.setOnSucceeded(event -> {
                addFileTask.getValue().forEach(fileBean -> fileBean.setCopyConfig(creatCopyConfig()));
                taskUnbind(taskBean);
            });
            addFileType_CP.setDisable(true);
            if (!addFileTask.isRunning()) {
                Thread.ofVirtual()
                        .name("addFileTask-vThread" + tabId)
                        .start(addFileTask);
            }
        } else if (text_addDirectory.equals(addFileType)) {
            FileChooserConfig fileConfig = new FileChooserConfig();
            fileConfig.setPathKey(key_inFilePath)
                    .setConfigPath(configFile_CP)
                    .setTitle(text_selectDirectory)
                    .setInFile(new File(inFilePath))
                    .setShowHideFile(text_noHideFile)
                    .setShowDirectory(text_onlyDirectory);
            FileChooserController controller = chooserFiles(fileConfig);
            // 设置回调
            controller.setFileChooserCallback(fileBeanList -> {
                fileBeanList.forEach(fileBean -> fileBean.setCopyConfig(creatCopyConfig()));
                tableView_CP.getItems().addAll(fileBeanList);
                tableView_CP.refresh();
                addFileType_CP.setDisable(true);

            });
        }
    }

    /**
     * 添加数据设置单选框监听
     */
    @FXML
    private void ddFileTypeAction() {
        String addFileType = addFileType_CP.getValue();
        addValueToolTip(addFileType_CP, tip_addFileType, addFileType);
        if (text_addDirectory.equals(addFileType)) {
            addFileButton_CP.setText(text_selectMoveFolder);
            filterHBox_CP.setVisible(true);
        } else if (text_addFile.equals(addFileType)) {
            addFileButton_CP.setText(text_selectMoveFile);
            filterHBox_CP.setVisible(false);
        }
    }

    /**
     * 隐藏文件查询设置单选框监听
     */
    @FXML
    private void hideFileTypeAction() {
        addValueToolTip(hideFileType_CP, tip_hideFileType, hideFileType_CP.getValue());
    }

    /**
     * 目录结构设置单选框监听
     */
    @FXML
    private void copyTypeAction() {
        addValueToolTip(copyType_CP, tip_moveType, copyType_CP.getValue());
    }

    /**
     * 根据文件名尾缀类型更新重命名分隔符设置下拉框选项
     */
    @FXML
    private void differenceCodeAction() {
        String differenceCode = differenceCode_CP.getValue();
        addValueToolTip(differenceCode_CP, tip_differenceCode, differenceCode);
        switch (differenceCode) {
            case text_arabicNumerals: {
                updateSelectItems(addSpace_CP, subCode_CP, subCodeArabicNumItems);
                break;
            }
            case text_chineseNumerals: {
                updateSelectItems(addSpace_CP, subCode_CP, subCodeChineseNumItems);
                break;
            }
            case text_abc: {
                updateSelectItems(addSpace_CP, subCode_CP, subCodeLowercaseItems);
                break;
            }
            case text_ABC: {
                updateSelectItems(addSpace_CP, subCode_CP, subCodeUppercaseNumItems);
                break;
            }
        }
    }

    /**
     * 分隔符选项监听
     */
    @FXML
    private void subCodeAction() {
        addValueToolTip(subCode_CP, tip_subCode, subCode_CP.getValue());
    }

    /**
     * 是否向分隔符左侧添加一个空格选项监听
     */
    @FXML
    private void handleCheckBoxAction() {
        differenceCodeAction();
    }

    /**
     * 更新复制设置按钮
     */
    @FXML
    private void updateCopyConfig() {
        Platform.runLater(() -> {
            ObservableList<FileBean> items = tableView_CP.getItems();
            if (CollectionUtils.isEmpty(items)) {
                throw new RuntimeException("请选择要复制的文件或文件夹");
            }
            ObservableList<FileBean> selectedItems = tableView_CP.getSelectionModel().getSelectedItems();
            if (CollectionUtils.isEmpty(selectedItems)) {
                ButtonType result = creatConfirmDialog(
                        "是否更新整个列表？",
                        "未选中任何数据，将会更新整个列表",
                        "更新列表全部数据设置",
                        "取消");
                if (!result.getButtonData().isCancelButton()) {
                    items.forEach(fileBean -> fileBean.setCopyConfig(creatCopyConfig()));
                }
            } else {
                selectedItems.forEach(fileBean -> fileBean.setCopyConfig(creatCopyConfig()));
            }
        });
    }

    /**
     * 取消复制按钮
     */
    @FXML
    private void cancelCopy() {
        if (copyFileTask != null && copyFileTask.isRunning()) {
            copyFileTask.cancel();
        }
    }

    /**
     * 删除目标路径按钮
     */
    @FXML
    private void removePath() {
        setPathLabel(outPath_CP, "");
        removePathButton_CP.setVisible(false);
    }

}
