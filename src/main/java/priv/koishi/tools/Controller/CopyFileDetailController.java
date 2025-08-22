package priv.koishi.tools.Controller;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.CopyConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static priv.koishi.tools.Controller.MainController.settingController;
import static priv.koishi.tools.Enum.SelectItemsEnums.*;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.MainApplication.mainStage;
import static priv.koishi.tools.Service.CopyFileService.readCopyFile;
import static priv.koishi.tools.Utils.TaskUtils.bindingTaskNode;
import static priv.koishi.tools.Utils.TaskUtils.taskUnbind;
import static priv.koishi.tools.Utils.UiUtils.*;
import static priv.koishi.tools.Utils.UiUtils.addToolTip;

/**
 * 复制文件工具控制器
 *
 * @author KOISHI
 * Date:2025-08-19
 * Time:14:35
 */
public class CopyFileDetailController extends RootController {

    /**
     * 目标文件路径
     */
    private static String outFilePath;

    /**
     * 页面标识符
     */
    private static final String tabId = "_CD";

    /**
     * 读取要复制的文件线程任务
     */
    private Task<List<FileBean>> readCopyFileTask;

    /**
     * 带鼠标悬停提示的内容变化监听器
     */
    private final Map<Object, ChangeListener<?>> changeListeners = new WeakHashMap<>();

    /**
     * 详情页页面舞台
     */
    private Stage stage;

    /**
     * 页面数据对象
     */
    private FileBean selectedItem;

    /**
     * 要防重复点击的组件
     */
    private static final List<Node> disableNodes = new ArrayList<>();

    /**
     * 回调函数
     */
    @Setter
    private Runnable refreshCallback;

    @FXML
    public AnchorPane anchorPane_CD;

    @FXML
    public ProgressBar progressBar_CD;

    @FXML
    public VBox progressBarVBox_CD;

    @FXML
    public HBox fileNumberHBox_CD, filterHBox_CD;

    @FXML
    public TableView<FileBean> tableView_CD;

    @FXML
    public TableColumn<FileBean, ImageView> thumb_CD;

    @FXML
    public TableColumn<FileBean, Integer> index_CD;

    @FXML
    public TableColumn<FileBean, String> name_CD, path_CD, size_CD, fileType_CD, creatDate_CD, updateDate_CD,
            showStatus_CD, copyPath_CD;

    @FXML
    public Label outPath_CD, fileNumber_CD, log_CD, sourcePath_CD;

    @FXML
    public TextField filterFileType_CD, prefix_CD, tag_CD, copyNum_CD;

    @FXML
    public Button outPathButton_CD, updateCopy_CD, removePathButton_CD;

    @FXML
    public CheckBox openDirectory_CD, addSpace_CD, reverseFileType_CD, firstRename_CD;

    @FXML
    public ChoiceBox<String> copyType_CD, hideFileType_CD, differenceCode_CD, subCode_CD;

    /**
     * 初始化数据
     *
     * @param item 详情页数据
     */
    public void initData(FileBean item, String outFilePath) {
        selectedItem = item;
        CopyFileDetailController.outFilePath = outFilePath;
        CopyConfig copyConfig = item.getCopyConfig();
        String sourcePath = item.getPath();
        File sourceFile = new File(sourcePath);
        prefix_CD.setText(copyConfig.getPrefix());
        subCode_CD.setValue(copyConfig.getSubCode());
        copyType_CD.setValue(copyConfig.getCopyType());
        addSpace_CD.setSelected(copyConfig.isAddSpace());
        filterHBox_CD.setVisible(sourceFile.isDirectory());
        tag_CD.setText(String.valueOf(copyConfig.getTag()));
        firstRename_CD.setSelected(copyConfig.isFirstRename());
        hideFileType_CD.setValue(copyConfig.getHideFileType());
        filterFileType_CD.setText(copyConfig.getFilterFileType());
        openDirectory_CD.setSelected(copyConfig.isOpenDirectory());
        differenceCode_CD.setValue(copyConfig.getDifferenceCode());
        copyNum_CD.setText(String.valueOf(copyConfig.getCopyNum()));
        reverseFileType_CD.setSelected(copyConfig.isReverseFileType());
        setPathLabel(outPath_CD, copyConfig.getOutPath());
        setPathLabel(sourcePath_CD, sourcePath);
        // 初始化复制文件预览列表
        Platform.runLater(this::reselect);
    }

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tableView_CD.setPrefHeight(stageHeight * 0.5);
        // 设置组件宽度
        double stageWidth = mainStage.getWidth();
        double tableWidth = stageWidth * 0.94;
        tableView_CD.setMaxWidth(tableWidth);
        tableView_CD.setPrefWidth(tableWidth);
        progressBarVBox_CD.setMaxWidth(tableWidth * 0.4);
        progressBarVBox_CD.setPrefWidth(tableWidth * 0.4);
        regionRightAlignment(fileNumberHBox_CD, tableWidth, fileNumber_CD);
        // 设置javafx单元格宽度
        bindPrefWidthProperty();
    }

    /**
     * 设置列表各列宽度
     */
    private void bindPrefWidthProperty() {
        index_CD.prefWidthProperty().bind(tableView_CD.widthProperty().multiply(0.04));
        thumb_CD.prefWidthProperty().bind(tableView_CD.widthProperty().multiply(0.1));
        name_CD.prefWidthProperty().bind(tableView_CD.widthProperty().multiply(0.14));
        fileType_CD.prefWidthProperty().bind(tableView_CD.widthProperty().multiply(0.08));
        path_CD.prefWidthProperty().bind(tableView_CD.widthProperty().multiply(0.1));
        copyPath_CD.prefWidthProperty().bind(tableView_CD.widthProperty().multiply(0.1));
        size_CD.prefWidthProperty().bind(tableView_CD.widthProperty().multiply(0.08));
        showStatus_CD.prefWidthProperty().bind(tableView_CD.widthProperty().multiply(0.08));
        creatDate_CD.prefWidthProperty().bind(tableView_CD.widthProperty().multiply(0.14));
        updateDate_CD.prefWidthProperty().bind(tableView_CD.widthProperty().multiply(0.14));
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_tag, tag_CD);
        addToolTip(tip_prefix, prefix_CD);
        addToolTip(tip_copyNum, copyNum_CD);
        addToolTip(tip_addSpace, addSpace_CD);
        addToolTip(tip_copyPath, outPathButton_CD);
        addToolTip(tip_firstRename, firstRename_CD);
        addToolTip(tip_updateCopyList, updateCopy_CD);
        addToolTip(tip_openDirectory, openDirectory_CD);
        addToolTip(tip_filterFileType, filterFileType_CD);
        addToolTip(tip_removePathButton, removePathButton_CD);
        addToolTip(reverseFileType_CD.getText(), reverseFileType_CD);
        addValueToolTip(subCode_CD, tip_subCode, subCode_CD.getValue());
        addValueToolTip(copyType_CD, tip_moveType, copyType_CD.getValue());
        addValueToolTip(hideFileType_CD, tip_hideFileType, hideFileType_CD.getValue());
        addValueToolTip(differenceCode_CD, tip_differenceCode, differenceCode_CD.getValue());
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(updateCopy_CD);
        disableNodes.add(outPathButton_CD);
    }

    /**
     * 清空列表
     */
    private void removeAll() {
        if (readCopyFileTask != null && readCopyFileTask.isRunning()) {
            readCopyFileTask.cancel();
            readCopyFileTask = null;
        }
        tableView_CD.getItems().stream().parallel().forEach(FileBean::clearResources);
        tableView_CD.getItems().clear();
    }

    /**
     * 页面关闭事件处理逻辑
     */
    private void closeRequest() {
        removeAll();
        // 清理监听器引用
        tableView_CD.setRowFactory(tv -> null);
        removeAllListeners();
        ContextMenu contextMenu = tableView_CD.getContextMenu();
        if (contextMenu != null) {
            // 清除所有菜单项事件
            contextMenu.getItems().stream().parallel().forEach(item -> item.setOnAction(null));
            tableView_CD.setContextMenu(null);
        }
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 文件名前缀输入框添加鼠标悬停提示
        ChangeListener<String> prefixListener = textFieldValueListener(prefix_CD, tip_prefix);
        changeListeners.put(prefix_CD, prefixListener);
        // 限制重名文件尾缀输入框内容
        ChangeListener<String> tagListener = integerRangeTextField(tag_CD, 0, null, tip_tag);
        changeListeners.put(tag_CD, tagListener);
        // 限制复制数量输入框内容
        ChangeListener<String> copyNumListener = integerRangeTextField(copyNum_CD, 1, null, tip_copyNum);
        changeListeners.put(copyNum_CD, copyNumListener);
        // 鼠标悬留提示输入的需要识别的文件后缀名
        ChangeListener<String> filterFileTypeListener = textFieldValueListener(filterFileType_CD, tip_filterFileType);
        changeListeners.put(filterFileType_CD, filterFileTypeListener);
    }

    /**
     * 移除所有监听器
     */
    @SuppressWarnings("unchecked")
    private void removeAllListeners() {
        // 处理带鼠标悬停提示的变更监听器集合，遍历所有entry，根据不同类型移除对应的选择/数值监听器
        changeListeners.forEach((key, listener) -> {
            if (key instanceof ChoiceBox<?> choiceBox) {
                choiceBox.getSelectionModel().selectedItemProperty().removeListener((InvalidationListener) listener);
            } else if (key instanceof Slider slider) {
                slider.valueProperty().removeListener((ChangeListener<? super Number>) listener);
            } else if (key instanceof TextInputControl textInput) {
                textInput.textProperty().removeListener((ChangeListener<? super String>) listener);
            } else if (key instanceof CheckBox checkBox) {
                checkBox.selectedProperty().removeListener((ChangeListener<? super Boolean>) listener);
            }
        });
        changeListeners.clear();
    }

    /**
     * 创建任务参数
     */
    private TaskBean<FileBean> creatTaskBean() {
        ChoiceBox<String> sort = settingController.sort_Set;
        String sortValue = sort.getValue();
        CheckBox reverseSort = settingController.reverseSort_Set;
        TaskBean<FileBean> taskBean = new TaskBean<>();
        taskBean.setReverseSort(reverseSort.isSelected())
                .setComparatorTableColumn(size_CD)
                .setProgressBar(progressBar_CD)
                .setMassageLabel(fileNumber_CD)
                .setDisableNodes(disableNodes)
                .setTableView(tableView_CD)
                .setSortType(sortValue)
                .setInFileList(null)
                .setTabId(tabId);
        return taskBean;
    }

    /**
     * 获取复制配置
     *
     * @return 复制配置
     */
    public CopyConfig creatCopyConfig() {
        int copyNum = setDefaultIntValue(copyNum_CD, 1, 1, null);
        int tag = setDefaultIntValue(tag_CD, 1, 0, null);
        return new CopyConfig()
                .setReverseFileType(reverseFileType_CD.isSelected())
                .setOpenDirectory(openDirectory_CD.isSelected())
                .setDifferenceCode(differenceCode_CD.getValue())
                .setFilterFileType(filterFileType_CD.getText())
                .setFirstRename(firstRename_CD.isSelected())
                .setHideFileType(hideFileType_CD.getValue())
                .setAddSpace(addSpace_CD.isSelected())
                .setCopyType(copyType_CD.getValue())
                .setSubCode(subCode_CD.getValue())
                .setOutPath(outPath_CD.getText())
                .setPrefix(prefix_CD.getText())
                .setCopyNum(copyNum)
                .setTag(tag);
    }

    /**
     * 构建右键菜单
     *
     * @param tableView 要添加右键菜单的列表
     */
    private void tableViewContextMenu(TableView<FileBean> tableView) {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 查看文件选项
        buildFilePathItem(tableView, contextMenu);
        // 取消选中选项
        buildClearSelectedData(tableView, contextMenu);
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(contextMenu, tableView);
    }

    /**
     * 界面初始化
     *
     */
    @FXML
    private void initialize() {
        // 设置鼠标悬停提示
        setToolTip();
        // 给输入框添加内容变化监听
        textFieldChangeListener();
        Platform.runLater(() -> {
            stage = (Stage) anchorPane_CD.getScene().getWindow();
            stage.setOnCloseRequest(e -> closeRequest());
            // 组件自适应宽高
            adaption();
            // 设置要防重复点击的组件
            setDisableNodes();
            // 绑定表格数据
            autoBuildTableViewData(tableView_CD, FileBean.class, tabId, index_CD);
            // 设置文件大小排序
            fileSizeColum(size_CD);
            // 构建右键菜单
            tableViewContextMenu(tableView_CD);
        });
    }

    /**
     * 设置导出文件按钮
     *
     * @param actionEvent 点击事件
     * @throws IOException io异常
     */
    @FXML
    private void targetPath(ActionEvent actionEvent) throws IOException {
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        String path = outPath_CD.getText();
        String defaultFilePath = StringUtils.isBlank(path) ? outFilePath : path;
        File selectedFile = creatDirectoryChooser(window, defaultFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), defaultFilePath, key_outFilePath, outPath_CD, configFile_CP);
            removePathButton_CD.setVisible(true);
            reselect();
        }
    }

    /**
     * 隐藏文件查询设置单选框监听
     */
    @FXML
    private void hideFileTypeAction() {
        addValueToolTip(hideFileType_CD, tip_hideFileType, hideFileType_CD.getValue());
    }

    /**
     * 目录结构设置单选框监听
     */
    @FXML
    private void copyTypeAction() {
        addValueToolTip(copyType_CD, tip_moveType, copyType_CD.getValue());
    }

    /**
     * 根据文件名尾缀类型更新重命名分隔符设置下拉框选项
     */
    @FXML
    private void differenceCodeAction() {
        String differenceCode = differenceCode_CD.getValue();
        addValueToolTip(differenceCode_CD, tip_differenceCode, differenceCode);
        switch (differenceCode) {
            case text_arabicNumerals: {
                updateSelectItems(addSpace_CD, subCode_CD, subCodeArabicNumItems);
                break;
            }
            case text_chineseNumerals: {
                updateSelectItems(addSpace_CD, subCode_CD, subCodeChineseNumItems);
                break;
            }
            case text_abc: {
                updateSelectItems(addSpace_CD, subCode_CD, subCodeLowercaseItems);
                break;
            }
            case text_ABC: {
                updateSelectItems(addSpace_CD, subCode_CD, subCodeUppercaseNumItems);
                break;
            }
        }
    }

    /**
     * 分隔符选项监听
     */
    @FXML
    private void subCodeAction() {
        addValueToolTip(subCode_CD, tip_subCode, subCode_CD.getValue());
    }

    /**
     * 是否向分隔符左侧添加一个空格选项监听
     */
    @FXML
    private void addSpaceAction() {
        differenceCodeAction();
    }

    /**
     * 刷新预览列表按钮
     */
    @FXML
    private void reselect() {
        removeAll();
        TaskBean<FileBean> taskBean = creatTaskBean();
        selectedItem.setCopyConfig(creatCopyConfig());
        readCopyFileTask = readCopyFile(taskBean, selectedItem);
        bindingTaskNode(readCopyFileTask, taskBean);
        readCopyFileTask.setOnSucceeded(event -> {
            List<FileBean> fileBeans = readCopyFileTask.getValue();
            tableView_CD.getItems().addAll(fileBeans);
            tableView_CD.refresh();
            taskUnbind(taskBean);
            updateTableViewSizeText(tableView_CD, fileNumber_CD, text_file);
        });
        if (!readCopyFileTask.isRunning()) {
            Thread.ofVirtual()
                    .name("readCopyFileTask-vThread" + tabId)
                    .start(readCopyFileTask);
        }
    }

    /**
     * 保存更改并关闭详情页按钮
     */
    @FXML
    private void saveDetails() {
        selectedItem.setCopyConfig(creatCopyConfig());
        closeStage(stage, this::closeRequest);
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    /**
     * 删除当前配置按钮
     */
    @FXML
    private void removeDetail() {
        selectedItem.setRemove(true);
        closeStage(stage, this::closeRequest);
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    /**
     * 删除目标路径按钮
     */
    @FXML
    private void removePath() {
        setPathLabel(outPath_CD, "");
        removePathButton_CD.setVisible(false);
        tableView_CD.getItems().forEach(fileBean -> fileBean.setCopyPath(fileBean.getPath()));
        tableView_CD.refresh();
    }

}
