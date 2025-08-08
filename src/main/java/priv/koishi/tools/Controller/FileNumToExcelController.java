package priv.koishi.tools.Controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;
import priv.koishi.tools.Configuration.FileConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.MainApplication.mainScene;
import static priv.koishi.tools.MainApplication.mainStage;
import static priv.koishi.tools.Service.FileNumToExcelService.buildNameGroupNumExcel;
import static priv.koishi.tools.Service.ReadDataService.readExcel;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 分组统计文件夹下文件数量页面控制器
 *
 * @author KOISHI
 * Date:2024-10-08
 * Time:下午3:29
 */
public class FileNumToExcelController extends RootController {

    /**
     * 要处理的文件夹路径
     */
    private static String inFilePath;

    /**
     * 要处理的文件夹文件
     */
    private static List<File> inFileList;

    /**
     * 导出文件路径
     */
    private static String outFilePath;

    /**
     * 默认导出文件名称
     */
    private static String defaultOutFileName;

    /**
     * 默认读取表名称
     */
    private static String defaultSheetName;

    /**
     * excel模板路径
     */
    private static String excelInPath;

    /**
     * 页面标识符
     */
    private static final String tabId = "_Num";

    /**
     * 默认起始输出列
     */
    private static int defaultStartCell;

    /**
     * 默认起始读取行
     */
    private static int defaultReadRow;

    /**
     * 默认起始读取列
     */
    private static int defaultReadCell;

    /**
     * 要防重复点击的组件
     */
    private static final List<Node> disableNodes = new ArrayList<>();

    /**
     * 读取文件线程
     */
    private Task<List<FileNumBean>> readExcelTask;

    /**
     * 构建excel线程
     */
    private Task<Workbook> buildExcelTask;

    @FXML
    public AnchorPane anchorPane_Num;

    @FXML
    public HBox fileNumberHBox_Num;

    @FXML
    public ProgressBar progressBar_Num;

    @FXML
    public TableView<FileNumBean> tableView_Num;

    @FXML
    public ChoiceBox<String> hideFileType_Num, directoryNameType_Num;

    @FXML
    public TableColumn<FileNumBean, Integer> groupName_Num, groupNumber_Num, index_Num;

    @FXML
    public TableColumn<FileNumBean, String> fileName_Num, groupId_Num, fileUnitSize_Num;

    @FXML
    public Label outPath_Num, excelPath_Num, fileNumber_Num, inPath_Num, log_Num, excelType_Num, excelTypeLabel_Num;

    @FXML
    public Button fileButton_Num, clearButton_Num, exportButton_Num, reselectButton_Num,
            excelPathButton_Num, outPathButton_Num;

    @FXML
    public CheckBox recursion_Num, openDirectory_Num, openFile_Num, showFileType_Num,
            exportTitle_Num, exportFileNum_Num, exportFileSize_Num;

    @FXML
    public TextField excelName_Num, sheetName_Num, startRow_Num, startCell_Num, filterFileType_Num,
            subCode_Num, readRow_Num, readCell_Num, maxRow_Num;

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tableView_Num.setPrefHeight(stageHeight * 0.45);
        // 设置组件宽度
        double stageWidth = mainStage.getWidth();
        double tableWidth = stageWidth * 0.94;
        tableView_Num.setMaxWidth(tableWidth);
        tableView_Num.setPrefWidth(tableWidth);
        regionRightAlignment(fileNumberHBox_Num, tableWidth, fileNumber_Num);
        bindPrefWidthProperty();
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_Num.prefWidthProperty().bind(tableView_Num.widthProperty().multiply(0.1));
        groupId_Num.prefWidthProperty().bind(tableView_Num.widthProperty().multiply(0.1));
        groupName_Num.prefWidthProperty().bind(tableView_Num.widthProperty().multiply(0.1));
        groupNumber_Num.prefWidthProperty().bind(tableView_Num.widthProperty().multiply(0.1));
        fileName_Num.prefWidthProperty().bind(tableView_Num.widthProperty().multiply(0.5));
        fileUnitSize_Num.prefWidthProperty().bind(tableView_Num.widthProperty().multiply(0.1));
    }

    /**
     * 保存最后一次配置的值
     *
     * @throws IOException io异常
     */
    public void saveLastConfig() throws IOException {
        if (anchorPane_Num != null) {
            InputStream input = checkRunningInputStream(configFile_Num);
            Properties prop = new Properties();
            prop.load(input);
            prop.put(key_lastDirectoryNameType, directoryNameType_Num.getValue());
            prop.put(key_lastHideFileType, hideFileType_Num.getValue());
            String recursionValue = recursion_Num.isSelected() ? activation : unActivation;
            prop.put(key_lastRecursion, recursionValue);
            String showFileTypeValue = showFileType_Num.isSelected() ? activation : unActivation;
            prop.put(key_lastShowFileType, showFileTypeValue);
            String openDirectoryValue = openDirectory_Num.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, openDirectoryValue);
            String openFileValue = openFile_Num.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenFile, openFileValue);
            prop.put(key_lastExcelName, excelName_Num.getText());
            prop.put(key_lastSheetName, sheetName_Num.getText());
            prop.put(key_lastSubCode, subCode_Num.getText());
            prop.put(key_lastStartRow, startRow_Num.getText());
            prop.put(key_lastStartCell, startCell_Num.getText());
            prop.put(key_lastReadRow, readRow_Num.getText());
            prop.put(key_lastReadCell, readCell_Num.getText());
            prop.put(key_lastMaxRow, maxRow_Num.getText());
            prop.put(key_lastFilterFileType, filterFileType_Num.getText());
            prop.put(key_lastInPath, inPath_Num.getText());
            prop.put(key_lastOutPath, outPath_Num.getText());
            prop.put(key_lastExcelPath, excelPath_Num.getText());
            String exportFileNumValue = exportFileNum_Num.isSelected() ? activation : unActivation;
            prop.put(key_lastExportFileNum, exportFileNumValue);
            String exportFileSizeValue = exportFileSize_Num.isSelected() ? activation : unActivation;
            prop.put(key_lastExportFileSize, exportFileSizeValue);
            String exportTitleValue = exportTitle_Num.isSelected() ? activation : unActivation;
            prop.put(key_lastExportTitle, exportTitleValue);
            OutputStream output = checkRunningOutputStream(configFile_Num);
            prop.store(output, null);
            input.close();
            output.close();
        }
    }

    /**
     * 读取文件数据
     *
     * @param selectedFile        要读取的文件
     * @param filterExtensionList 要过滤的文件格式
     * @throws Exception 未查询到符合条件的数据
     */
    private void addInFile(File selectedFile, List<String> filterExtensionList) throws Exception {
        FileConfig fileConfig = getInFileList(selectedFile, filterExtensionList);
        // 列表中有excel分组后再匹配数据
        ObservableList<FileNumBean> fileNumList = tableView_Num.getItems();
        if (CollectionUtils.isNotEmpty(fileNumList)) {
            machGroup(fileConfig, fileNumList, inFileList, tableView_Num, tabId, fileNumber_Num, fileUnitSize_Num);
        }
    }

    /**
     * 查询要处理的文件
     *
     * @param selectedFile        要读取的文件
     * @param filterExtensionList 要过滤的文件格式
     * @return 文件读取设置
     */
    private FileConfig getInFileList(File selectedFile, List<String> filterExtensionList) throws IOException {
        FileConfig fileConfig = new FileConfig();
        fileConfig.setShowDirectoryName(directoryNameType_Num.getValue())
                .setShowFileType(showFileType_Num.isSelected())
                .setShowHideFile(hideFileType_Num.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setRecursion(recursion_Num.isSelected())
                .setSubCode(subCode_Num.getText())
                .setInFile(selectedFile);
        inFileList = readAllFiles(fileConfig);
        return fileConfig;
    }

    /**
     * 更新要处理的文件
     *
     * @throws IOException 读取文件失败、文件不存在
     */
    private void updateInFileList() throws IOException {
        String selectedFilePath = inPath_Num.getText();
        if (StringUtils.isNotBlank(selectedFilePath)) {
            getInFileList(new File(selectedFilePath), getFilterExtensionList(filterFileType_Num));
        }
    }

    /**
     * 添加数据渲染列表
     *
     * @return 读取excel任务线程
     * @throws IOException 读取文件失败、文件不存在
     */
    private Task<List<FileNumBean>> addInData() throws IOException {
        if (readExcelTask == null) {
            removeAll();
            // 渲染表格前需要更新一下读取的文件
            updateInFileList();
            String excelPath = excelPath_Num.getText();
            excelType_Num.setText(getFileType(new File(excelPath)));
            // 组装数据
            ExcelConfig excelConfig = new ExcelConfig();
            excelConfig.setReadCellNum(setDefaultIntValue(readCell_Num, defaultReadCell, 0, null))
                    .setReadRowNum(setDefaultIntValue(readRow_Num, defaultReadRow, 0, null))
                    .setMaxRowNum(setDefaultIntValue(maxRow_Num, -1, 1, null))
                    .setInPath(excelPath_Num.getText())
                    .setSheetName(sheetName_Num.getText());
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setShowFileType(showFileType_Num.isSelected())
                    .setComparatorTableColumn(fileUnitSize_Num)
                    .setDisableNodes(disableNodes)
                    .setSubCode(subCode_Num.getText())
                    .setProgressBar(progressBar_Num)
                    .setMassageLabel(fileNumber_Num)
                    .setTableView(tableView_Num)
                    .setInFileList(inFileList)
                    .setTabId(tabId);
            // 获取Task任务
            readExcelTask = readExcel(excelConfig, taskBean);
            readExcelTask.setOnSucceeded(event -> {
                taskUnbind(taskBean);
                readExcelTask = null;
            });
            // 绑定带进度条的线程
            bindingTaskNode(readExcelTask, taskBean);
            if (!readExcelTask.isRunning()) {
                Thread.ofVirtual()
                        .name("readExcelTask-vThread" + tabId)
                        .start(readExcelTask);
            }
        }
        return readExcelTask;
    }

    /**
     * 读取配置文件
     *
     * @throws IOException io异常
     */
    private void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Num);
        prop.load(input);
        inFilePath = prop.getProperty(key_inFilePath);
        excelInPath = prop.getProperty(key_excelInPath);
        outFilePath = prop.getProperty(key_outFilePath);
        defaultSheetName = prop.getProperty(key_defaultSheetName);
        defaultOutFileName = prop.getProperty(key_defaultOutFileName);
        defaultReadRow = Integer.parseInt(prop.getProperty(key_defaultReadRow));
        defaultReadCell = Integer.parseInt(prop.getProperty(key_defaultReadCell));
        defaultStartCell = Integer.parseInt(prop.getProperty(key_defaultStartCell));
        input.close();
    }

    /**
     * 设置初始配置值为上次配置值
     *
     * @throws IOException io异常
     */
    private void setLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Num);
        prop.load(input);
        if (activation.equals(prop.getProperty(key_loadLastConfig))) {
            setControlLastConfig(maxRow_Num, prop, key_lastMaxRow);
            setControlLastConfig(readRow_Num, prop, key_lastReadRow);
            setControlLastConfig(readCell_Num, prop, key_lastReadCell);
            setControlLastConfig(startRow_Num, prop, key_lastStartRow);
            setControlLastConfig(openFile_Num, prop, key_lastOpenFile);
            setControlLastConfig(excelName_Num, prop, key_lastExcelName);
            setControlLastConfig(sheetName_Num, prop, key_lastSheetName);
            setControlLastConfig(startCell_Num, prop, key_lastStartCell);
            setControlLastConfig(recursion_Num, prop, key_lastRecursion);
            setControlLastConfig(exportTitle_Num, prop, key_lastExportTitle);
            setControlLastConfig(showFileType_Num, prop, key_lastShowFileType);
            setControlLastConfig(hideFileType_Num, prop, key_lastHideFileType);
            setControlLastConfig(openDirectory_Num, prop, key_lastOpenDirectory);
            setControlLastConfig(exportFileNum_Num, prop, key_lastExportFileNum);
            setControlLastConfig(inPath_Num, prop, key_lastInPath);
            setControlLastConfig(exportFileSize_Num, prop, key_lastExportFileSize);
            setControlLastConfig(filterFileType_Num, prop, key_lastFilterFileType);
            setControlLastConfig(subCode_Num, prop, key_lastSubCode, true);
            setControlLastConfig(outPath_Num, prop, key_lastOutPath);
            setControlLastConfig(excelPath_Num, prop, key_lastExcelPath);
            setControlLastConfig(directoryNameType_Num, prop, key_lastDirectoryNameType);
            String excelPath = prop.getProperty(key_lastExcelPath);
            if (StringUtils.isNotBlank(excelPath)) {
                excelType_Num.setText(getFileType(new File(excelPath)));
            }
        }
        input.close();
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(fileButton_Num);
        disableNodes.add(clearButton_Num);
        disableNodes.add(exportButton_Num);
        disableNodes.add(showFileType_Num);
        disableNodes.add(outPathButton_Num);
        disableNodes.add(reselectButton_Num);
        disableNodes.add(excelPathButton_Num);
        Node autoClickTab = mainScene.lookup("#autoClickTab");
        disableNodes.add(autoClickTab);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_maxRow, maxRow_Num);
        addToolTip(tip_subCode, subCode_Num);
        addToolTip(tip_openFile, openFile_Num);
        addToolTip(tip_recursion, recursion_Num);
        addToolTip(tip_sheetName, sheetName_Num);
        addToolTip(tip_excelName + defaultOutFileName, excelName_Num);
        addToolTip(tip_startReadRow, startRow_Num);
        addToolTip(tip_fileButton, fileButton_Num);
        addToolTip(tip_learButton, clearButton_Num);
        addToolTip(tip_exportTitle, exportTitle_Num);
        addToolTip(tip_showFileType, showFileType_Num);
        addToolTip(tip_hideFileType, hideFileType_Num);
        addToolTip(tip_exportButton, exportButton_Num);
        addToolTip(tip_openDirectory, openDirectory_Num);
        addToolTip(tip_outPathButton, outPathButton_Num);
        addToolTip(tip_exportFileNum, exportFileNum_Num);
        addToolTip(tip_exportFileSize, exportFileSize_Num);
        addToolTip(tip_reselectButton, reselectButton_Num);
        addToolTip(tip_filterFileType, filterFileType_Num);
        addToolTip(tip_excelPathButton, excelPathButton_Num);
        addToolTip(tip_directoryNameType, directoryNameType_Num);
        addToolTip(tip_excelType, excelType_Num, excelTypeLabel_Num);
        addToolTip(text_onlyNaturalNumber + defaultStartCell, startCell_Num);
        addToolTip(text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row, readRow_Num);
        addToolTip(text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell, readCell_Num);
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 鼠标悬留提示输入的文件名称分割符
        textFieldValueListener(subCode_Num, tip_subCode);
        // 鼠标悬留提示输入的导出excel表名称
        textFieldValueListener(sheetName_Num, tip_sheetName);
        // 鼠标悬留提示输入的需要识别的文件后缀名
        textFieldValueListener(filterFileType_Num, tip_filterFileType);
        // 限制读取最大行数只能输入正整数
        integerRangeTextField(maxRow_Num, 1, null, tip_maxRow);
        // 限制导出预留行只能输入自然数
        integerRangeTextField(startRow_Num, 0, null, tip_startReadRow);
        // 鼠标悬留提示输入的导出excel文件名称
        textFieldValueListener(excelName_Num, tip_excelName + defaultOutFileName);
        // 限制导出预留列只能输入自然数
        integerRangeTextField(startCell_Num, 0, null, text_onlyNaturalNumber + defaultStartCell);
        // 限制读取起始行只能输入自然数
        integerRangeTextField(readRow_Num, 0, null, text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row);
        // 限制读取起始列只能输入自然数
        integerRangeTextField(readCell_Num, 0, null, text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell);
    }

    /**
     * 设置文本输入框提示
     */
    private void setPromptText() {
        excelName_Num.setPromptText(defaultOutFileName);
        readRow_Num.setPromptText(String.valueOf(defaultReadRow));
        readCell_Num.setPromptText(String.valueOf(defaultReadCell));
        startCell_Num.setPromptText(String.valueOf(defaultStartCell));
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
        // 设置文本输入框提示
        setPromptText();
        // 设置javafx单元格宽度
        bindPrefWidthProperty();
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
            autoBuildTableViewData(tableView_Num, FileNumBean.class, tabId, index_Num);
        });
    }

    /**
     * 选择文件夹按钮功能
     *
     * @param actionEvent 交互事件
     * @throws Exception 未查询到符合条件的数据、io异常
     */
    @FXML
    private void inDirectoryButton(ActionEvent actionEvent) throws Exception {
        getConfig();
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        // 显示文件选择器
        File selectedFile = creatDirectoryChooser(window, inFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            inFilePath = updatePathLabel(selectedFile.getPath(), inFilePath, key_inFilePath, inPath_Num, configFile_Num);
            // 读取文件数据
            addInFile(selectedFile, getFilterExtensionList(filterFileType_Num));
        }
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽释放事件
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        File file = files.getFirst();
        excelPath_Num.setText(file.getPath());
        try {
            addInData();
        } catch (IOException e) {
            showExceptionAlert(e);
        }
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽中事件
     */
    @FXML
    private void acceptDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        files.forEach(file -> {
            if (file.isFile() && (xlsx.equals(getFileType(file)) || xls.equals(getFileType(file)))) {
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
        inFileList = null;
        removeTableViewData(tableView_Num, fileNumber_Num, log_Num);
    }

    /**
     * 导出excel按钮
     *
     * @throws Exception 导出文件夹位置为空、要查询的文件夹位置为空、excel模板文件位置为空
     */
    @FXML
    private void exportAll() throws Exception {
        if (buildExcelTask == null) {
            updateLabel(log_Num, "");
            String outFilePath = outPath_Num.getText();
            if (StringUtils.isEmpty(outFilePath)) {
                throw new Exception(text_outPathNull);
            }
            if (StringUtils.isEmpty(inPath_Num.getText())) {
                throw new Exception(text_filePathNull);
            }
            if (StringUtils.isEmpty(excelPath_Num.getText())) {
                throw new Exception(text_excelPathNull);
            }
            int readRowValue = setDefaultIntValue(readRow_Num, defaultReadRow, 0, null);
            ExcelConfig excelConfig = new ExcelConfig();
            excelConfig.setStartCellNum(setDefaultIntValue(startCell_Num, defaultStartCell, 0, null))
                    .setStartRowNum(setDefaultIntValue(startRow_Num, readRowValue, 0, null))
                    .setOutName(setDefaultFileName(excelName_Num, defaultOutFileName))
                    .setSheetName(setDefaultStrValue(sheetName_Num, defaultSheetName))
                    .setExportFileSize(exportFileSize_Num.isSelected())
                    .setExportFileNum(exportFileNum_Num.isSelected())
                    .setOutExcelType(excelType_Num.getText())
                    .setExportTitle(exportTitle_Num.isSelected())
                    .setInPath(excelPath_Num.getText())
                    .setOutPath(outFilePath);
            readExcelTask = reselect();
            readExcelTask.setOnSucceeded(event -> {
                TaskBean<FileNumBean> taskBean = new TaskBean<>();
                taskBean.setShowFileType(showFileType_Num.isSelected())
                        .setBeanList(readExcelTask.getValue())
                        .setDisableNodes(disableNodes)
                        .setSubCode(subCode_Num.getText())
                        .setProgressBar(progressBar_Num)
                        .setTableView(tableView_Num)
                        .setInFileList(inFileList)
                        .setMassageLabel(log_Num)
                        .setTabId(tabId);
                // 获取Task任务
                buildExcelTask = buildNameGroupNumExcel(taskBean, excelConfig);
                // 线程成功后保存excel
                buildExcelTask = saveExcelOnSucceeded(excelConfig, taskBean, buildExcelTask, openDirectory_Num, openFile_Num, tabId);
                readExcelTask = null;
            });
            if (!readExcelTask.isRunning()) {
                Thread.ofVirtual()
                        .name("readExcelTask-vThread" + tabId)
                        .start(readExcelTask);
            }
        }
    }

    /**
     * 设置导出文件按钮
     *
     * @param actionEvent 交互事件
     * @throws Exception io异常
     */
    @FXML
    private void exportPath(ActionEvent actionEvent) throws Exception {
        getConfig();
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatDirectoryChooser(window, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Num, configFile_Num);
            if (StringUtils.isNotEmpty(excelPath_Num.getText())) {
                reselect();
            }
        }
    }

    /**
     * 选择excel模板按钮
     *
     * @param actionEvent 交互事件
     * @throws Exception io异常
     */
    @FXML
    private void getExcelPath(ActionEvent actionEvent) throws Exception {
        getConfig();
        List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
        extensionFilters.add(new FileChooser.ExtensionFilter("Excel", "*.xlsx", "*.xls"));
        extensionFilters.add(new FileChooser.ExtensionFilter("Excel(2007)", "*.xlsx"));
        extensionFilters.add(new FileChooser.ExtensionFilter("Excel(2003)", "*.xls"));
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatFileChooser(window, excelInPath, extensionFilters, text_selectExcel);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            excelInPath = updatePathLabel(selectedFile.getPath(), excelInPath, key_excelInPath, excelPath_Num, configFile_Num);
            addInData();
        }
    }

    /**
     * 重新查询按钮
     *
     * @return 读取excel任务线程
     * @throws Exception excel模板文件位置为空、要读取的文件夹不存在
     */
    @FXML
    private Task<List<FileNumBean>> reselect() throws Exception {
        String inFilePath = excelPath_Num.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception(text_excelPathNull);
        }
        if (!new File(inFilePath).exists()) {
            throw new Exception(text_directoryNotExists);
        }
        updateLabel(log_Num, "");
        return addInData();
    }

    /**
     * 是否展示文件拓展名选项监听
     *
     * @throws Exception excel模板文件位置为空、要读取的文件夹不存在
     */
    @FXML
    private void handleCheckBoxAction() throws Exception {
        ObservableList<FileNumBean> fileBeans = tableView_Num.getItems();
        if (CollectionUtils.isNotEmpty(fileBeans)) {
            reselect();
        }
    }

}
