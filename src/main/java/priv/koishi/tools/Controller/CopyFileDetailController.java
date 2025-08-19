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
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.CopyConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Controller.MainController.settingController;
import static priv.koishi.tools.Enum.SelectItemsEnums.*;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.MainApplication.mainStage;
import static priv.koishi.tools.Service.MoveFileService.readMoveFile;
import static priv.koishi.tools.Utils.TaskUtils.bindingTaskNode;
import static priv.koishi.tools.Utils.TaskUtils.taskUnbind;
import static priv.koishi.tools.Utils.UiUtils.*;

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
    public static String outFilePath;

    /**
     * 上次选择的文件路径
     */
    public static String inFilePath;

    /**
     * 页面标识符
     */
    private static final String tabId = "_CD";

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
    public HBox fileNumberHBox_CD, filterHBox_CD;

    @FXML
    public TableView<FileBean> tableView_CD;

    @FXML
    public TableColumn<FileBean, ImageView> thumb_CD;

    @FXML
    public TableColumn<FileBean, Integer> index_CD;

    @FXML
    public TableColumn<FileBean, String> name_CD, path_CD, size_CD, fileType_CD, creatDate_CD, updateDate_CD,
            showStatus_CD;

    @FXML
    public Label outPath_CD, fileNumber_CD, log_CD;

    @FXML
    public TextField filterFileType_CD, prefix_CD, tag_CD, copyNum_CD;

    @FXML
    public CheckBox openDirectory_CD, addSpace_CD, reverseFileType_CD;

    @FXML
    public Button outPathButton_CD, updateCopy_CD;

    @FXML
    public ChoiceBox<String> addFileType_CD, copyType_CD, hideFileType_CD, differenceCode_CD, subCode_CD;


    public void initData(FileBean item) {
        selectedItem = item;
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
        path_CD.prefWidthProperty().bind(tableView_CD.widthProperty().multiply(0.2));
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
        addToolTip(tip_addSpace, addSpace_CD);
        addToolTip(tip_movePath, outPathButton_CD);
        addToolTip(tip_openDirectory, openDirectory_CD);
        addToolTip(tip_filterFileType, filterFileType_CD);
        addToolTip(reverseFileType_CD.getText(), reverseFileType_CD);
        addValueToolTip(subCode_CD, tip_subCode, subCode_CD.getValue());
        addValueToolTip(copyType_CD, tip_moveType, copyType_CD.getValue());
        addValueToolTip(addFileType_CD, tip_addFileType, addFileType_CD.getValue());
        addValueToolTip(hideFileType_CD, tip_hideFileType, hideFileType_CD.getValue());
        addValueToolTip(differenceCode_CD, tip_differenceCode, differenceCode_CD.getValue());
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(filterHBox_CD);
        disableNodes.add(outPathButton_CD);
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 文件名前缀输入框添加鼠标悬停提示
        textFieldValueListener(prefix_CD, tip_prefix);
        // 限制重名文件尾缀输入框内容
        integerRangeTextField(tag_CD, 0, null, tip_tag);
        // 限制复制数量输入框内容
        integerRangeTextField(copyNum_CD, 1, null, tip_tag);
        // 鼠标悬留提示输入的需要识别的文件后缀名
        textFieldValueListener(filterFileType_CD, tip_filterFileType);
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
                .setComparatorTableColumn(size_CD)
                .setProgressBar(progressBar_CD)
                .setMassageLabel(fileNumber_CD)
                .setDisableNodes(disableNodes)
                .setTableView(tableView_CD)
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
    private void tableViewContextMenu(TableView<FileBean> tableView, Label label) {
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
     * 获取复制配置
     *
     * @return 复制配置
     */
    public CopyConfig creatCopyConfig() {
        int copyNum = setDefaultIntValue(copyNum_CD, 1, 1, null);
        int tag = setDefaultIntValue(copyNum_CD, 1, 0, null);
        return new CopyConfig()
                .setReverseFileType(reverseFileType_CD.isSelected())
                .setDifferenceCode(differenceCode_CD.getValue())
                .setFilterFileType(filterFileType_CD.getText())
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
            // 组件自适应宽高
            adaption();
            ddFileTypeAction();
            // 设置要防重复点击的组件
            setDisableNodes();
            // 绑定表格数据
            autoBuildTableViewData(tableView_CD, FileBean.class, tabId, index_CD);
            // 设置文件大小排序
            fileSizeColum(size_CD);
            // 构建右键菜单
            tableViewContextMenu(tableView_CD, fileNumber_CD);
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
            updateTableViewSizeText(tableView_CD, fileNumber_CD, text_file);
        });
        addFileType_CD.setDisable(true);
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
     * 设置导出文件按钮
     *
     * @param actionEvent 点击事件
     * @throws IOException io异常
     */
    @FXML
    private void targetPath(ActionEvent actionEvent) throws IOException {
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatDirectoryChooser(window, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_CD, configFile_CP);
        }
    }

    /**
     * 添加数据设置单选框监听
     */
    @FXML
    private void ddFileTypeAction() {
        String addFileType = addFileType_CD.getValue();
        addValueToolTip(addFileType_CD, tip_addFileType, addFileType);
        if (text_addDirectory.equals(addFileType)) {
            filterHBox_CD.setVisible(true);
        } else if (text_addFile.equals(addFileType)) {
            filterHBox_CD.setVisible(false);
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
    private void handleCheckBoxAction() {
        differenceCodeAction();
    }

    /**
     * 更新复制设置按钮
     */
    @FXML
    private void updateCopyConfig() {
        Platform.runLater(() -> {
            ObservableList<FileBean> items = tableView_CD.getItems();
            if (CollectionUtils.isEmpty(items)) {
                throw new RuntimeException("请选择要复制的文件或文件夹");
            }
            ObservableList<FileBean> selectedItems = tableView_CD.getSelectionModel().getSelectedItems();
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

}
