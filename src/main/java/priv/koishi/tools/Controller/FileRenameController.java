package priv.koishi.tools.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.*;
import priv.koishi.tools.EditingCell.EditingCell;
import priv.koishi.tools.Properties.ToolsProperties;
import priv.koishi.tools.ThreadPool.CommonThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static priv.koishi.tools.Enum.SelectItemsEnums.*;
import static priv.koishi.tools.Service.ReadDataService.readExcel;
import static priv.koishi.tools.Service.ReadDataService.readFile;
import static priv.koishi.tools.Service.RenameService.buildRename;
import static priv.koishi.tools.Service.RenameService.fileRename;
import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.TaskUtils.bindingProgressBarTask;
import static priv.koishi.tools.Utils.TaskUtils.taskUnbind;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * @author KOISHI
 * Date:2024-10-18
 * Time:下午4:36
 */
public class FileRenameController extends ToolsProperties {

    /**
     * 要处理的文件夹路径
     */
    static String inFilePath;

    /**
     * excel模板路径
     */
    static String excelInPath;

    /**
     * 页面标识符
     */
    static String tabId = "_Re";

    /**
     * 文件名起始编号
     */
    static int defaultStartNameNum = 1;

    /**
     * 默认起始读取行
     */
    static int defaultReadRow = 0;

    /**
     * 默认起始读取列
     */
    static int defaultReadCell = 0;

    /**
     * 要防重复点击的组件
     */
    static List<Control> disableControls = new ArrayList<>();

    /**
     * 配置文件路径
     */
    static String configFile = "config/fileRenameConfig.properties";

    /**
     * 线程池
     */
    private final CommonThreadPoolExecutor commonThreadPoolExecutor = new CommonThreadPoolExecutor();

    /**
     * 线程池实例
     */
    ExecutorService executorService = commonThreadPoolExecutor.createNewThreadPool();

    @FXML
    private ProgressBar progressBar_Re;

    @FXML
    private TableView<FileBean> tableView_Re;

    @FXML
    private TableColumn<FileBean, Integer> id_Re;

    @FXML
    private CheckBox openDirectory_Re, addSpace_Re;

    @FXML
    private HBox renameTypeHBox_Re, behaviorHBox_Re, targetStrHBox_Re;

    @FXML
    private VBox vbox_Re, codeRenameVBox_Re, strRenameVBox_Re, excelRenameVBox_Re;

    @FXML
    private Label excelPath_Re, fileNumber_Re, inPath_Re, log_Re, typeLabel_Re, tip_Re;

    @FXML
    private TableColumn<FileBean, String> name_Re, rename_Re, path_Re, size_Re, fileType_Re, creatDate_Re, updateDate_Re;

    @FXML
    private Button fileButton_Re, clearButton_Re, renameButton_Re, reselectButton_Re, updateRenameButton_Re, excelPathButton_Img;

    @FXML
    private ChoiceBox<String> hideFileType_Re, directoryNameType_Re, renameType_Re, subCode_Re, differenceCode_Re, targetStr_Re, leftBehavior_Re, rightBehavior_Re, renameBehavior_Re;

    @FXML
    private TextField sheetOutName_Re, filterFileType_Re, readRow_Re, readCell_Re, maxRow_Re, startName_Re, nameNum_Re, startSize_Re, left_Re, right_Re, renameStr_Re, leftValue_Re, rightValue_Re, renameValue_Re, tag_Re;

    /**
     * 组件自适应宽高
     */
    public static void fileRenameAdaption(Stage stage, Scene scene) {
        //设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Re");
        table.setPrefHeight(stageHeight * 0.5);
        //设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        Node vbox = scene.lookup("#vbox_Re");
        vbox.setLayoutX(stageWidth * 0.03);
        Node tableView = scene.lookup("#tableView_Re");
        tableView.setStyle("-fx-pref-width: " + tableWidth + "px;");
        Node id = scene.lookup("#id_Re");
        id.setStyle("-fx-pref-width: " + tableWidth * 0.04 + "px;");
        Node name = scene.lookup("#name_Re");
        name.setStyle("-fx-pref-width: " + tableWidth * 0.14 + "px;");
        Node rename = scene.lookup("#rename_Re");
        rename.setStyle("-fx-pref-width: " + tableWidth * 0.14 + "px;");
        Node fileType = scene.lookup("#fileType_Re");
        fileType.setStyle("-fx-pref-width: " + tableWidth * 0.06 + "px;");
        Node path = scene.lookup("#path_Re");
        path.setStyle("-fx-pref-width: " + tableWidth * 0.22 + "px;");
        Node size = scene.lookup("#size_Re");
        size.setStyle("-fx-pref-width: " + tableWidth * 0.08 + "px;");
        Node creatDate = scene.lookup("#creatDate_Re");
        creatDate.setStyle("-fx-pref-width: " + tableWidth * 0.16 + "px;");
        Node updateDate = scene.lookup("#updateDate_Re");
        updateDate.setStyle("-fx-pref-width: " + tableWidth * 0.16 + "px;");
        Label fileNum = (Label) scene.lookup("#fileNumber_Re");
        Button removeAll = (Button) scene.lookup("#clearButton_Re");
        Button renameAll = (Button) scene.lookup("#renameButton_Re");
        Button reselect = (Button) scene.lookup("#reselectButton_Re");
        Button updateRenameButton = (Button) scene.lookup("#updateRenameButton_Re");
        ProgressBar progressBar = (ProgressBar) scene.lookup("#progressBar_Re");
        Label tip = (Label) scene.lookup("#tip_Re");
        fileNum.setPrefWidth(tableWidth - removeAll.getWidth() - renameAll.getWidth() - reselect.getWidth() - updateRenameButton.getWidth() - 50);
        tip.setPrefWidth(tableWidth - progressBar.getWidth() - 10);
    }

    /**
     * 添加数据渲染列表
     */
    private void addInData(List<File> inFileList) throws Exception {
        removeAll();
        if (inFileList.isEmpty()) {
            throw new Exception(text_selectNull);
        }
        TaskBean<FileBean> taskBean = new TaskBean<>();
        taskBean.setDisableControls(disableControls)
                .setProgressBar(progressBar_Re)
                .setMassageLabel(fileNumber_Re)
                .setTableView(tableView_Re)
                .setInFileList(inFileList)
                .setTableColumn(size_Re)
                .setShowFileType(false)
                .setTabId(tabId);
        //匹配重命名规则
        matchRenameConfig(taskBean);
        //获取Task任务
        Task<Void> readFileTask = readFile(taskBean);
        //绑定带进度条的线程
        bindingProgressBarTask(readFileTask, taskBean);
        readFileTask.setOnSucceeded(event -> {
            if (text_excelRename.equals(renameType_Re.getValue()) && StringUtils.isNotBlank(excelPath_Re.getText())) {
                readExcelRename();
            } else {
                //表格设置为可编辑
                tableView_Re.setEditable(true);
                rename_Re.setCellFactory((tableColumn) -> new EditingCell<>(FileBean::setRename));
                taskUnbind(taskBean);
            }
            //设置列表通过拖拽排序行
            tableViewDragRow(tableView_Re);
            //构建右键菜单
            tableViewContextMenu(tableView_Re, fileNumber_Re);
        });
        executorService.execute(readFileTask);
    }

    /**
     * 读取excel重命名模板
     */
    private void readExcelRename() {
        ExcelConfig excelConfig = new ExcelConfig();
        excelConfig.setReadCellNum(setDefaultIntValue(readCell_Re, defaultReadCell, 0, null))
                .setReadRowNum(setDefaultIntValue(readRow_Re, defaultReadRow, 0, null))
                .setMaxRowNum(setDefaultIntValue(maxRow_Re, -1, 1, null))
                .setSheet(sheetOutName_Re.getText())
                .setInPath(excelPath_Re.getText());
        TaskBean<FileNumBean> taskBean = new TaskBean<>();
        taskBean.setDisableControls(disableControls)
                .setProgressBar(progressBar_Re)
                .setReturnRenameList(true)
                .setMassageLabel(log_Re)
                .setShowFileType(false)
                .setTabId(tabId);
        //获取Task任务
        Task<List<FileNumBean>> readExcelTask = readExcel(excelConfig, taskBean);
        readExcelTask.setOnSucceeded(event -> {
            List<String> excelRenameList = readExcelTask.getValue().stream().map(FileNumBean::getGroupName).toList();
            ObservableList<FileBean> fileBeanList = tableView_Re.getItems();
            showMatchExcelData(taskBean, fileBeanList, excelRenameList);
        });
        //绑定带进度条的线程
        bindingProgressBarTask(readExcelTask, taskBean);
        //使用新线程启动
        executorService.execute(readExcelTask);
    }

    /**
     * 展示读取excel重命名数据
     */
    private void showMatchExcelData(TaskBean<?> taskBean, ObservableList<FileBean> fileBeanList, List<String> excelRenameList) {
        if (CollectionUtils.isNotEmpty(fileBeanList) && CollectionUtils.isNotEmpty(excelRenameList)) {
            matchExcelRename(fileBeanList, excelRenameList);
        }
        //表格设置为可编辑
        tableView_Re.setEditable(true);
        rename_Re.setCellFactory((tableColumn) -> new EditingCell<>(FileBean::setRename));
        taskUnbind(taskBean);
        if (taskBean.getMassageLabel() == log_Re) {
            log_Re.setText("");
        }
    }

    /**
     * 匹配excel重命名数据
     */
    private void matchExcelRename(ObservableList<FileBean> fileBeanList, List<String> excelRenameList) {
        int fileBeanListSize = fileBeanList.size();
        int excelRenameListSize = excelRenameList.size();
        for (int i = 0; i < fileBeanListSize; i++) {
            FileBean fileBean = fileBeanList.get(i);
            if (i < excelRenameListSize) {
                String fileRename = excelRenameList.get(i);
                if (StringUtils.isNotBlank(fileRename)) {
                    fileBean.setRename(fileRename);
                } else {
                    fileBean.setRename(fileBean.getName());
                }
            } else {
                fileBean.setRename(fileBean.getName());
            }
        }
        autoBuildTableViewData(tableView_Re, fileBeanList, tabId);
    }

    /**
     * 匹配重命名规则
     */
    private void matchRenameConfig(TaskBean<FileBean> taskBean) {
        String renameType = renameType_Re.getValue();
        switch (renameType) {
            case text_codeRename: {
                matchCodeRename(taskBean);
                break;
            }
            case text_strRename: {
                matchStringRename(taskBean);
                break;
            }
        }
    }

    /**
     * 按编号规则重命名
     */
    private void matchCodeRename(TaskBean<FileBean> taskBean) {
        CodeRenameConfig codeRenameConfig = new CodeRenameConfig();
        codeRenameConfig.setStartName(setDefaultIntValue(startName_Re, 1, 0, null))
                .setStartSize(setDefaultIntValue(startSize_Re, 0, 0, null))
                .setNameNum(setDefaultIntValue(nameNum_Re, 0, 0, null))
                .setTag(setDefaultIntValue(tag_Re, 1, 0, null))
                .setDifferenceCode(differenceCode_Re.getValue())
                .setAddSpace(addSpace_Re.isSelected())
                .setSubCode(subCode_Re.getValue());
        taskBean.setConfiguration(codeRenameConfig);
    }

    /**
     * 按指定字符重命名
     */
    private void matchStringRename(TaskBean<FileBean> taskBean) {
        String targetStr = targetStr_Re.getValue();
        StringRenameConfig stringRenameConfig = new StringRenameConfig();
        stringRenameConfig.setTargetStr(targetStr);
        if (text_specifyString.equals(targetStr) || text_specifyIndex.equals(targetStr)) {
            String renameBehavior = renameBehavior_Re.getValue();
            stringRenameConfig.setRenameValue(renameValue_Re.getText())
                    .setRenameBehavior(renameBehavior);
            if (text_replace.equals(renameBehavior)) {
                stringRenameConfig.setRenameStr(renameStr_Re.getText());
            } else if (text_bothSides.equals(renameBehavior)) {
                String leftBehavior = leftBehavior_Re.getValue();
                String rightBehavior = rightBehavior_Re.getValue();
                stringRenameConfig.setRight(setDefaultIntValue(right_Re, 0, 0, null))
                        .setLeft(setDefaultIntValue(left_Re, 0, 0, null))
                        .setRightBehavior(rightBehavior)
                        .setLeftBehavior(leftBehavior);
                if (text_insert.equals(leftBehavior) || text_replace.equals(leftBehavior)) {
                    stringRenameConfig.setLeftValue(leftValue_Re.getText());
                }
                if (text_insert.equals(rightBehavior) || text_replace.equals(rightBehavior)) {
                    stringRenameConfig.setRightValue(rightValue_Re.getText());
                }
            }
        }
        taskBean.setConfiguration(stringRenameConfig);
    }

    /**
     * 读取配置文件
     */
    private static void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        inFilePath = prop.getProperty(key_inFilePath);
        excelInPath = prop.getProperty(key_excelInPath);
        defaultReadRow = Integer.parseInt(prop.getProperty(key_defaultReadRow));
        defaultReadCell = Integer.parseInt(prop.getProperty(key_defaultReadCell));
        defaultStartNameNum = Integer.parseInt(prop.getProperty(key_defaultStartNameNum));
        input.close();
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() throws IOException {
        //读取全局变量配置
        getConfig();
        //设置要防重复点击的组件
        disableControls.add(fileButton_Re);
        disableControls.add(clearButton_Re);
        disableControls.add(renameButton_Re);
        disableControls.add(reselectButton_Re);
        disableControls.add(excelPathButton_Img);
        disableControls.add(updateRenameButton_Re);
        //设置鼠标悬停提示
        addToolTip(tag_Re, tip_tag);
        addToolTip(left_Re, tip_left);
        addToolTip(right_Re, tip_right);
        addToolTip(maxRow_Re, tip_maxRow);
        addToolTip(nameNum_Re, tip_nameNum);
        addToolTip(tip_Re, tip_Re.getText());
        addToolTip(addSpace_Re, tip_addSpace);
        addToolTip(renameButton_Re, tip_rename);
        addToolTip(leftValue_Re, tip_leftValue);
        addToolTip(leftBehavior_Re, tip_option);
        addToolTip(startSize_Re, tip_startSize);
        addToolTip(renameStr_Re, tip_renameStr);
        addToolTip(rightBehavior_Re, tip_option);
        addToolTip(rightValue_Re, tip_rightValue);
        addToolTip(renameValue_Re, tip_renameValue);
        addToolTip(sheetOutName_Re, tip_sheetOutName);
        addToolTip(filterFileType_Re, tip_filterFileType);
        addToolTip(startName_Re, text_onlyNaturalNumber + defaultStartNameNum);
        addToolTip(readRow_Re, text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row);
        addToolTip(readCell_Re, text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell);
        //设置要暂时移除的组件
        vbox_Re.getChildren().remove(strRenameVBox_Re);
        vbox_Re.getChildren().remove(excelRenameVBox_Re);
        //设置javafx单元格宽度
        id_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.04));
        name_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.14));
        rename_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.14));
        fileType_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.06));
        path_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.22));
        size_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.08));
        creatDate_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.16));
        updateDate_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.16));
    }

    /**
     * 选择文件夹按钮功能
     */
    @FXML
    private void inDirectoryButton(ActionEvent actionEvent) throws Exception {
        getConfig();
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Re);
        // 显示文件选择器
        File selectedFile = creatDirectoryChooser(actionEvent, inFilePath, text_selectDirectory);
        FileConfig fileConfig = new FileConfig();
        fileConfig.setShowDirectoryName(directoryNameType_Re.getValue())
                .setShowHideFile(hideFileType_Re.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setInFile(selectedFile);
        if (selectedFile != null) {
            //更新所选文件路径显示
            inFilePath = updatePathLabel(selectedFile.getAbsolutePath(), inFilePath, key_inFilePath, inPath_Re, configFile);
            //读取数据
            List<File> inFileList = readAllFiles(fileConfig);
            addInData(inFileList);
        }
    }

    /**
     * 拖拽释放行为
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) throws Exception {
        removeAll();
        List<File> files = dragEvent.getDragboard().getFiles();
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Re);
        File file = files.getFirst();
        FileConfig fileConfig = new FileConfig();
        fileConfig.setShowDirectoryName(directoryNameType_Re.getValue())
                .setShowHideFile(hideFileType_Re.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setInFile(file);
        List<File> inFileList = readAllFiles(fileConfig);
        String filePath = file.getPath();
        inPath_Re.setText(filePath);
        addToolTip(inPath_Re, filePath);
        addInData(inFileList);
    }

    /**
     * 拖拽中行为
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
        ObservableList<FileBean> nullData = FXCollections.observableArrayList(new ArrayList<>());
        tableView_Re.setItems(nullData);
        updateLabel(fileNumber_Re, text_dataListNull);
        updateLabel(log_Re, "");
        System.gc();
    }

    /**
     * 开始重命名按钮
     */
    @FXML
    private void renameAll() throws Exception {
        updateLabel(log_Re, "");
        ObservableList<FileBean> fileBeans = tableView_Re.getItems();
        if (CollectionUtils.isEmpty(fileBeans)) {
            throw new Exception(text_fileListNull);
        }
        Map<String, List<FileBean>> fileBeanMap = fileBeans.stream().collect(Collectors.groupingBy(FileBean::getFullRename));
        List<String> repeatNameList = new ArrayList<>();
        fileBeanMap.forEach((rename, fileBeanList) -> {
            if (fileBeanList.size() > 1) {
                List<String> ids = new ArrayList<>();
                fileBeanList.forEach(fileBean -> ids.add(String.valueOf(fileBean.getId())));
                String errString = "序号为: " + String.join("、", ids) + " 的文件重命名重复为 " + rename;
                repeatNameList.add(errString);
            }
        });
        String repeatNames = String.join("\n", repeatNameList) + "\n";
        List<String> errNameList = new ArrayList<>();
        fileBeans.forEach(fileBean -> {
            String rename = fileBean.getFullRename();
            if (!isValidFileName(rename)) {
                String errString = "序号为: " + fileBean.getId() + " 的文件 " + fileBean.getFullName() + " 非法重命名为 " + fileBean.getFullRename();
                errNameList.add(errString);
            }
        });
        String errNames = String.join("\n", errNameList) + "\n";
        String errString = "";
        if (StringUtils.isNotBlank(repeatNames)) {
            errString += "重复的名称：\n" + repeatNames;
        }
        if (StringUtils.isNotBlank(errNames)) {
            errString += "错误的名称：\n" + errNames;
        }
        if (StringUtils.isNotBlank(errString)) {
            Alert alert = creatErrorDialog(errString);
            alert.setHeaderText("文件重命名配置错误");
            // 展示弹窗
            alert.showAndWait();
        } else {
            TaskBean<FileBean> taskBean = new TaskBean<>();
            taskBean.setDisableControls(disableControls)
                    .setProgressBar(progressBar_Re)
                    .setMassageLabel(log_Re)
                    .setBeanList(fileBeans)
                    .setTabId(tabId);
            //匹配重命名规则
            matchRenameConfig(taskBean);
            //获取Task任务
            Task<String> renameTask = fileRename(taskBean);
            //绑定带进度条的线程
            bindingProgressBarTask(renameTask, taskBean);
            renameTask.setOnSucceeded(event -> {
                taskUnbind(taskBean);
                if (openDirectory_Re.isSelected()) {
                    try {
                        openFile(renameTask.getValue());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                taskBean.getMassageLabel().setTextFill(Color.GREEN);
            });
            executorService.execute(renameTask);
        }
    }

    /**
     * 选择excel模板按钮
     */
    @FXML
    private void getExcelPath(ActionEvent actionEvent) throws IOException {
        getConfig();
        List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(new FileChooser.ExtensionFilter("Excel", "*.xlsx")));
        File selectedFile = creatFileChooser(actionEvent, excelInPath, extensionFilters, text_selectExcel);
        if (selectedFile != null) {
            excelInPath = updatePathLabel(selectedFile.getAbsolutePath(), excelInPath, key_excelInPath, excelPath_Re, configFile);
            readExcelRename();
        }
    }

    /**
     * 鼠标悬留提示输入的导出excel表名称
     */
    @FXML
    private void sheetHandleKeyTyped() {
        addValueToolTip(sheetOutName_Re, tip_sheetOutName);
    }

    /**
     * 鼠标悬留提示输入的文件名起始编号
     */
    @FXML
    private void startNameHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(startName_Re, 0, null, event);
        addValueToolTip(startName_Re, text_onlyNaturalNumber + defaultStartNameNum);
    }

    /**
     * 鼠标悬留提示输入的文件名起始编号位数
     */
    @FXML
    private void startSizeHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(startSize_Re, 0, null, event);
        addValueToolTip(startSize_Re, tip_startSize);
    }

    /**
     * 鼠标悬留提示输入的相同编号文件数量
     */
    @FXML
    private void nameNumHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(nameNum_Re, 0, null, event);
        addValueToolTip(nameNum_Re, tip_nameNum);
    }

    /**
     * 鼠标悬留提示输入的需要识别的文件后缀名
     */
    @FXML
    private void filterHandleKeyTyped() {
        addValueToolTip(filterFileType_Re, tip_filterFileType);
    }

    /**
     * 重新查询按钮
     */
    @FXML
    private void reselect() throws Exception {
        String inFilePath = inPath_Re.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception(text_filePathNull);
        }
        updateLabel(log_Re, "");
        FileConfig fileConfig = new FileConfig();
        fileConfig.setFilterExtensionList(getFilterExtensionList(filterFileType_Re))
                .setShowDirectoryName(directoryNameType_Re.getValue())
                .setShowHideFile(hideFileType_Re.getValue())
                .setInFile(new File(inFilePath));
        addInData(readAllFiles(fileConfig));
    }

    /**
     * 是否向分隔符左侧添加一个空格选项监听
     */
    @FXML
    private void handleCheckBoxAction() {
        differenceCodeAction();
    }

    /**
     * 限制读取起始行只能输入自然数
     */
    @FXML
    private void readRowHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(readRow_Re, 0, null, event);
        addValueToolTip(readRow_Re, text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row);
    }

    /**
     * 限制读取起始列只能输入自然数
     */
    @FXML
    private void readCellHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(readCell_Re, 0, null, event);
        addValueToolTip(readCell_Re, text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell);
    }

    /**
     * 限制读取最大行数只能输入正整数
     */
    @FXML
    private void maxRowHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(maxRow_Re, 1, null, event);
        addValueToolTip(maxRow_Re, tip_maxRow);
    }

    /**
     * 根据区分编码类型更新重命名分隔符设置下拉框选项
     */
    @FXML
    private void differenceCodeAction() {
        String item = differenceCode_Re.getValue();
        switch (item) {
            case text_arabicNumerals: {
                updateSelectItems(addSpace_Re, subCode_Re, subCodeArabicNumItems);
                break;
            }
            case text_chineseNumerals: {
                updateSelectItems(addSpace_Re, subCode_Re, subCodeChineseNumItems);
                break;
            }
            case text_abc: {
                updateSelectItems(addSpace_Re, subCode_Re, subCodeLowercaseItems);
                break;
            }
            case text_ABC: {
                updateSelectItems(addSpace_Re, subCode_Re, subCodeUppercaseNumItems);
                break;
            }
        }
    }

    /**
     * 根据重命名方式展示设置组件
     */
    @FXML
    private void renameTypeAction() {
        String item = renameType_Re.getValue();
        int index = vbox_Re.getChildren().indexOf(renameTypeHBox_Re) + 1;
        switch (item) {
            case text_codeRename: {
                vbox_Re.getChildren().add(index, codeRenameVBox_Re);
                vbox_Re.getChildren().remove(strRenameVBox_Re);
                vbox_Re.getChildren().remove(excelRenameVBox_Re);
                break;
            }
            case text_strRename: {
                vbox_Re.getChildren().remove(codeRenameVBox_Re);
                vbox_Re.getChildren().add(index, strRenameVBox_Re);
                vbox_Re.getChildren().remove(excelRenameVBox_Re);
                targetStrAction();
                break;
            }
            case text_excelRename: {
                vbox_Re.getChildren().remove(codeRenameVBox_Re);
                vbox_Re.getChildren().remove(strRenameVBox_Re);
                vbox_Re.getChildren().add(index, excelRenameVBox_Re);
                break;
            }
        }
    }

    /**
     * 监控匹配字符规则选项
     */
    @FXML
    private void targetStrAction() {
        String item = targetStr_Re.getValue();
        switch (item) {
            case text_specifyString: {
                typeLabel_Re.setText(text_matchString);
                renameValue_Re.setText("");
                addValueToolTip(renameValue_Re, tip_renameValue);
                renameBehavior_Re.getItems().remove(text_bothSides);
                renameBehavior_Re.getItems().add(text_bothSides);
                behaviorAction();
                break;
            }
            case text_specifyIndex: {
                typeLabel_Re.setText(text_matchIndex);
                renameValue_Re.setText("");
                addValueToolTip(renameValue_Re, tip_renameValue);
                renameBehavior_Re.getItems().remove(text_bothSides);
                renameBehavior_Re.setValue(renameBehavior_Re.getItems().getFirst());
                behaviorAction();
                break;
            }
            default: {
                targetStrHBox_Re.setVisible(false);
                behaviorHBox_Re.setVisible(false);
                break;
            }
        }
    }

    /**
     * 监控重命名方法选项
     */
    @FXML
    private void behaviorAction() {
        String item = renameBehavior_Re.getValue();
        targetStrHBox_Re.setVisible(true);
        switch (item) {
            case text_replace: {
                renameStr_Re.setVisible(true);
                behaviorHBox_Re.setVisible(false);
                break;
            }
            case text_remove: {
                renameStr_Re.setVisible(false);
                behaviorHBox_Re.setVisible(false);
                break;
            }
            case text_bothSides: {
                renameStr_Re.setVisible(false);
                behaviorHBox_Re.setVisible(true);
                break;
            }
        }
    }

    /**
     * 监控指定位置之前处理方式下拉框
     */
    @FXML
    private void leftBehaviorAction() {
        String item = leftBehavior_Re.getValue();
        leftValue_Re.setVisible(text_insert.equals(item) || text_replace.equals(item));
    }

    /**
     * 监控指定位置之后处理方式下拉框
     */
    @FXML
    public void rightBehaviorAction() {
        String item = rightBehavior_Re.getValue();
        rightValue_Re.setVisible(text_insert.equals(item) || text_replace.equals(item));
    }

    /**
     * 要匹配的字符串鼠标悬停提示
     */
    @FXML
    private void renameValueHandleKeyTyped(KeyEvent event) {
        //这个输入框只有在输入指定字符位置时才限制输入范围
        if (text_specifyIndex.equals(targetStr_Re.getValue())) {
            integerRangeTextField(renameValue_Re, 0, null, event);
        }
        addValueToolTip(renameValue_Re, tip_renameValue);
    }

    /**
     * 指定字符串所替换的字符串鼠标悬停提示
     */
    @FXML
    private void renameStrHandleKeyTyped() {
        addValueToolTip(renameStr_Re, tip_renameStr);
    }

    /**
     * 限制向左匹配字符位置输入框内容
     */
    @FXML
    private void leftHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(left_Re, 0, null, event);
        addValueToolTip(left_Re, tip_left);
    }

    /**
     * 限制向右匹配字符位置输入框内容
     */
    @FXML
    private void rightHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(right_Re, 0, null, event);
        addValueToolTip(right_Re, tip_right);
    }

    /**
     * 给目标字符串左侧替换或插入输入框添加鼠标悬停提示
     */
    @FXML
    private void rightValueHandleKeyTyped() {
        addValueToolTip(rightValue_Re, tip_rightValue);
    }

    /**
     * 给目标字符串右侧替换或插入输入框添加鼠标悬停提示
     */
    @FXML
    private void leftValueHandleKeyTyped() {
        addValueToolTip(leftValue_Re, tip_leftValue);
    }

    /**
     * 给相同编号文件起始尾缀输入框添加鼠标悬停提示
     */
    @FXML
    private void tagHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(tag_Re, 0, null, event);
        addValueToolTip(tag_Re, tip_tag);
    }

    /**
     * 更新重命名按钮
     */
    @FXML
    private void updateRename() throws Exception {
        ObservableList<FileBean> fileBeans = tableView_Re.getItems();
        if (CollectionUtils.isEmpty(fileBeans)) {
            throw new Exception(text_fileListNull);
        }
        if (text_excelRename.equals(renameType_Re.getValue()) && StringUtils.isNotBlank(excelPath_Re.getText())) {
            readExcelRename();
        } else if (!text_excelRename.equals(renameType_Re.getValue())) {
            TaskBean<FileBean> taskBean = new TaskBean<>();
            //匹配重命名规则
            matchRenameConfig(taskBean);
            Configuration configuration = taskBean.getConfiguration();
            CodeRenameConfig codeRenameConfig = null;
            StringRenameConfig stringRenameConfig = null;
            int startName = -1;
            int tag = -1;
            int nameNum = 1;
            if (configuration != null) {
                if (configuration instanceof CodeRenameConfig) {
                    codeRenameConfig = (CodeRenameConfig) configuration;
                    startName = codeRenameConfig.getStartName();
                    tag = codeRenameConfig.getTag();
                } else if (configuration instanceof StringRenameConfig) {
                    stringRenameConfig = (StringRenameConfig) configuration;
                }
            }
            for (FileBean fileBean : fileBeans) {
                //组装文件重命名数据
                buildRename(codeRenameConfig, fileBean, stringRenameConfig, startName, tag);
                if (codeRenameConfig != null) {
                    if (nameNum < codeRenameConfig.getNameNum()) {
                        tag++;
                        nameNum++;
                    } else {
                        startName++;
                        tag = codeRenameConfig.getTag();
                        nameNum = 1;
                    }
                }
            }
            autoBuildTableViewData(tableView_Re, fileBeans, tabId);
            //表格设置为可编辑
            tableView_Re.setEditable(true);
            rename_Re.setCellFactory((tableColumn) -> new EditingCell<>(FileBean::setRename));
        }
        updateLabel(log_Re, "");
    }
}
