package priv.koishi.tools.Controller;

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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;
import priv.koishi.tools.Configuration.FileConfig;
import priv.koishi.tools.Properties.ToolsProperties;
import priv.koishi.tools.ThreadPool.CommonThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static priv.koishi.tools.Service.FileNumToExcelService.buildNameGroupNumExcel;
import static priv.koishi.tools.Service.ReadDataService.readExcel;
import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.FileUtils.getFileType;
import static priv.koishi.tools.Utils.FileUtils.readAllFiles;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * @author KOISHI
 * Date:2024-10-08
 * Time:下午3:29
 */
public class FileNumToExcelController extends ToolsProperties {

    /**
     * 要处理的文件夹路径
     */
    static String inFilePath;

    /**
     * 要处理的文件夹文件
     */
    static List<File> inFileList;

    /**
     * 导出文件路径
     */
    static String outFilePath;

    /**
     * 默认导出文件名称
     */
    static String defaultOutFileName;

    /**
     * 默认读取表名称
     */
    static String defaultSheetName;

    /**
     * excel模板路径
     */
    static String excelInPath;

    /**
     * 页面标识符
     */
    static String tabId = "_Num";

    /**
     * 默认起始输出列
     */
    static int defaultStartCell = 1;

    /**
     * 默认起始读取行
     */
    static int defaultReadRow = 1;

    /**
     * 默认起始读取列
     */
    static int defaultReadCell = 0;

    /**
     * 配置文件路径
     */
    static String configFile = "config/fileNumToExcelConfig.properties";

    /**
     * 线程池
     */
    private final CommonThreadPoolExecutor commonThreadPoolExecutor = new CommonThreadPoolExecutor();

    /**
     * 线程池实例
     */
    ExecutorService executorService = commonThreadPoolExecutor.createNewThreadPool();

    @FXML
    private VBox vbox_Num;

    @FXML
    private ProgressBar progressBar_Num;

    @FXML
    private TableView<FileNumBean> tableView_Num;

    @FXML
    private TableColumn<FileNumBean, String> fileName_Num, groupId_Num;

    @FXML
    private TableColumn<FileNumBean, Integer> groupName_Num, groupNumber_Num;

    @FXML
    private CheckBox recursion_Num, openDirectory_Num, openFile_Num, showFileType_Num;

    @FXML
    private Button fileButton_Num, clearButton_Num, exportButton_Num, reselectButton_Num;

    @FXML
    private ChoiceBox<String> excelType_Num, hideFileType_Num, directoryNameType_Num, exportType_Num;

    @FXML
    private Label outPath_Num, excelPath_Num, fileNumber_Num, inPath_Num, log_Num, exportTypeLabel_Num;

    @FXML
    private TextField excelName_Num, sheetOutName_Num, startRow_Num, startCell_Num, filterFileType_Num, subCode_Num, readRow_Num, readCell_Num, maxRow_Num;

    /**
     * 组件自适应宽高
     */
    public static void fileNumToExcelAdaption(Stage stage, Scene scene) {
        //设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Num");
        table.setPrefHeight(stageHeight * 0.5);
        //设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        Node fileNumVbox = scene.lookup("#vbox_Num");
        fileNumVbox.setLayoutX(stageWidth * 0.03);
        Node tableView = scene.lookup("#tableView_Num");
        tableView.setStyle("-fx-pref-width: " + tableWidth + "px;");
        Node groupId = scene.lookup("#groupId_Num");
        groupId.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node groupNameNum = scene.lookup("#groupName_Num");
        groupNameNum.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node groupNumberNum = scene.lookup("#groupNumber_Num");
        groupNumberNum.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node fileNameNum = scene.lookup("#fileName_Num");
        fileNameNum.setStyle("-fx-pref-width: " + tableWidth * 0.7 + "px;");
        Label fileNum = (Label) scene.lookup("#fileNumber_Num");
        Button removeAll = (Button) scene.lookup("#clearButton_Num");
        Button exportAll = (Button) scene.lookup("#exportButton_Num");
        Label exportTypeLabel = (Label) scene.lookup("#exportTypeLabel_Num");
        ChoiceBox<?> exportType = (ChoiceBox<?>) scene.lookup("#exportType_Num");
        Button reselect = (Button) scene.lookup("#reselectButton_Num");
        fileNum.setPrefWidth(tableWidth - removeAll.getWidth() - exportAll.getWidth() - exportTypeLabel.getWidth()
                - exportType.getWidth() - reselect.getWidth() - 60);
    }

    /**
     * 读取文件数据
     */
    private void addInFile(File selectedFile, List<String> filterExtensionList) throws Exception {
        FileConfig fileConfig = getInFileList(selectedFile, filterExtensionList);
        //列表中有excel分组后再匹配数据
        ObservableList<FileNumBean> fileNumList = tableView_Num.getItems();
        if (CollectionUtils.isNotEmpty(fileNumList)) {
            machGroup(fileConfig, fileNumList, inFileList, tableView_Num, tabId, fileNumber_Num);
        }
    }

    /**
     * 查询要处理的文件
     */
    private FileConfig getInFileList(File selectedFile, List<String> filterExtensionList) {
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
     */
    private void updateInFileList() {
        String selectedFilePath = inPath_Num.getText();
        if (StringUtils.isNotBlank(selectedFilePath)) {
            getInFileList(new File(selectedFilePath), getFilterExtensionList(filterFileType_Num));
        }
    }

    /**
     * 添加数据渲染列表
     */
    private Task<List<FileNumBean>> addInData() {
        removeAll();
        //渲染表格前需要更新一下读取的文件
        updateInFileList();
        //组装数据
        int readRowValue = setDefaultIntValue(readRow_Num, defaultReadRow, 0, null);
        int readCellValue = setDefaultIntValue(readCell_Num, defaultReadCell, 0, null);
        int maxRowValue = setDefaultIntValue(maxRow_Num, -1, 1, null);
        ExcelConfig excelConfig = new ExcelConfig();
        excelConfig.setSheet(sheetOutName_Num.getText())
                .setInPath(excelPath_Num.getText())
                .setReadCellNum(readCellValue)
                .setReadRowNum(readRowValue)
                .setMaxRowNum(maxRowValue);
        TaskBean<FileNumBean> taskBean = new TaskBean<>();
        taskBean.setShowFileType(showFileType_Num.isSelected())
                .setSubCode(subCode_Num.getText())
                .setProgressBar(progressBar_Num)
                .setMassageLabel(fileNumber_Num)
                .setTableView(tableView_Num)
                .setInFileList(inFileList)
                .setTabId(tabId);
        //获取Task任务
        Task<List<FileNumBean>> readExcelTask = readExcel(excelConfig, taskBean);
        readExcelTask.setOnSucceeded(event -> taskUnbind(taskBean));
        //绑定带进度条的线程
        bindingProgressBarTask(readExcelTask, taskBean);
        //使用新线程启动
        executorService.execute(readExcelTask);
        return readExcelTask;
    }

    /**
     * 读取配置文件
     */
    private static void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        // 加载properties文件
        prop.load(input);
        // 根据key读取value
        inFilePath = prop.getProperty(key_inFilePath);
        outFilePath = prop.getProperty(key_outFilePath);
        defaultOutFileName = prop.getProperty(key_defaultOutFileName);
        defaultSheetName = prop.getProperty(key_defaultSheetName);
        excelInPath = prop.getProperty(key_excelInPath);
        defaultStartCell = Integer.parseInt(prop.getProperty(key_defaultStartCell));
        defaultReadRow = Integer.parseInt(prop.getProperty(key_defaultReadRow));
        defaultReadCell = Integer.parseInt(prop.getProperty(key_defaultReadCell));
        input.close();
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() throws IOException {
        getConfig();
        addToolTip(filterFileType_Num, tip_filterFileType);
        addToolTip(startRow_Num, tip_startReadRow);
        addToolTip(startCell_Num, text_onlyNaturalNumber + defaultStartCell);
        addToolTip(readRow_Num, text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row);
        addToolTip(readCell_Num, text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell);
        addNumImgToolTip(recursion_Num, subCode_Num, excelName_Num, sheetOutName_Num, maxRow_Num);
        //设置javafx单元格宽度
        tableViewNumImgAdaption(groupId_Num, tableView_Num, groupName_Num.prefWidthProperty(), groupNumber_Num.prefWidthProperty(), fileName_Num);
    }

    /**
     * 选择文件夹按钮功能
     */
    @FXML
    private void inDirectoryButton(ActionEvent actionEvent) throws Exception {
        getConfig();
        // 显示文件选择器
        File selectedFile = creatDirectoryChooser(actionEvent, inFilePath, text_selectDirectory);
        if (selectedFile != null) {
            //更新所选文件路径显示
            inFilePath = updatePathLabel(selectedFile.getPath(), inFilePath, key_inFilePath, inPath_Num, configFile);
            //读取文件数据
            addInFile(selectedFile, getFilterExtensionList(filterFileType_Num));
        }
    }

    /**
     * 拖拽释放行为
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        File file = files.getFirst();
        excelPath_Num.setText(file.getPath());
        addInData();
    }

    /**
     * 拖拽中行为
     */
    @FXML
    private void acceptDrop(DragEvent dragEvent) {
        List<File> files = dragEvent.getDragboard().getFiles();
        files.forEach(file -> {
            if (file.isFile() && xlsx.equals(getFileType(file))) {
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
        removeNumImgAll(tableView_Num, fileNumber_Num, log_Num);
        System.gc();
    }

    /**
     * 导出excel按钮
     */
    @FXML
    private void exportAll() throws Exception {
        updateLabel(log_Num, "");
        String inDirectory = inPath_Num.getText();
        String outFilePath = outPath_Num.getText();
        String inFilePath = excelPath_Num.getText();
        if (StringUtils.isEmpty(outFilePath)) {
            throw new Exception(text_outPathNull);
        }
        if (StringUtils.isEmpty(inDirectory)) {
            throw new Exception(text_filePathNull);
        }
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception(text_excelPathNull);
        }
        String sheetName = setDefaultStrValue(sheetOutName_Num, defaultSheetName);
        String excelNameValue = setDefaultFileName(excelName_Num, defaultOutFileName);
        int readRowValue = setDefaultIntValue(readRow_Num, defaultReadRow, 0, null);
        int startRowValue = setDefaultIntValue(startRow_Num, readRowValue, 0, null);
        int startCellValue = setDefaultIntValue(startCell_Num, defaultStartCell, 0, null);
        ExcelConfig excelConfig = new ExcelConfig();
        excelConfig.setOutExcelExtension(excelType_Num.getValue())
                .setExportType(exportType_Num.getValue())
                .setInPath(excelPath_Num.getText())
                .setStartCellNum(startCellValue)
                .setStartRowNum(startRowValue)
                .setOutName(excelNameValue)
                .setOutPath(outFilePath)
                .setSheet(sheetName);
        Task<List<FileNumBean>> reselectTask = reselect();
        reselectTask.setOnSucceeded(event -> {
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setShowFileType(showFileType_Num.isSelected())
                    .setReselectButton(reselectButton_Num)
                    .setBeanList(reselectTask.getValue())
                    .setSubCode(subCode_Num.getText())
                    .setProgressBar(progressBar_Num)
                    .setTableView(tableView_Num)
                    .setInFileList(inFileList)
                    .setMassageLabel(log_Num)
                    .setTabId(tabId);
            //获取Task任务
            Task<SXSSFWorkbook> buildExcelTask = buildNameGroupNumExcel(taskBean, excelConfig);
            //线程成功后保存excel
            saveExcelOnSucceeded(excelConfig, taskBean, buildExcelTask, openDirectory_Num, openFile_Num, executorService);
        });
        //使用新线程启动
        executorService.execute(reselectTask);
    }

    /**
     * 设置导出文件按钮
     */
    @FXML
    private void exportPath(ActionEvent actionEvent) throws Exception {
        getConfig();
        File selectedFile = creatDirectoryChooser(actionEvent, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            //更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Num, configFile);
            if (StringUtils.isNotEmpty(excelPath_Num.getText())) {
                reselect();
            }
        }
    }

    /**
     * 选择excel模板按钮
     */
    @FXML
    private void getExcelPath(ActionEvent actionEvent) throws Exception {
        getConfig();
        List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(new FileChooser.ExtensionFilter("Excel", "*.xlsx")));
        File selectedFile = creatFileChooser(actionEvent, excelInPath, extensionFilters, text_selectExcel);
        if (selectedFile != null) {
            //更新所选文件路径显示
            excelInPath = updatePathLabel(selectedFile.getPath(), excelInPath, key_excelInPath, excelPath_Num, configFile);
            addInData();
        }
    }

    /**
     * 限制导出预留行只能输入自然数
     */
    @FXML
    private void rowHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(startRow_Num, 0, null, event);
        addValueToolTip(startRow_Num, text_onlyNaturalNumber + defaultStartCell);
    }

    /**
     * 限制导出预留列只能输入自然数
     */
    @FXML
    private void cellHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(startCell_Num, 0, null, event);
        addValueToolTip(startCell_Num, text_onlyNaturalNumber + defaultStartCell);
    }

    /**
     * 鼠标悬留提示输入的导出excel文件名称
     */
    @FXML
    private void nameHandleKeyTyped() {
        addValueToolTip(excelName_Num, tip_excelName);
    }

    /**
     * 鼠标悬留提示输入的导出excel表名称
     */
    @FXML
    private void sheetHandleKeyTyped() {
        addValueToolTip(sheetOutName_Num, tip_sheetOutName);
    }

    /**
     * 鼠标悬留提示输入的需要识别的文件后缀名
     */
    @FXML
    private void filterHandleKeyTyped() {
        addValueToolTip(filterFileType_Num, tip_filterFileType);
    }

    /**
     * 鼠标悬留提示输入的文件名称分割符
     */
    @FXML
    private void subHandleKeyTyped() {
        addValueToolTip(subCode_Num, tip_subCode);
    }

    /**
     * 限制读取起始行只能输入自然数
     */
    @FXML
    private void readRowHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(readRow_Num, 0, null, event);
        addValueToolTip(readRow_Num, text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row);
    }

    /**
     * 限制读取起始列只能输入自然数
     */
    @FXML
    private void readCellHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(readCell_Num, 0, null, event);
        addValueToolTip(readCell_Num, text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell);
    }

    /**
     * 限制读取最大行数只能输入正整数
     */
    @FXML
    private void maxRowHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(maxRow_Num, 1, null, event);
        addValueToolTip(maxRow_Num, tip_maxRow);
    }

    /**
     * 重新查询按钮
     */
    @FXML
    private Task<List<FileNumBean>> reselect() throws Exception {
        String inFilePath = excelPath_Num.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception(text_excelPathNull);
        }
        updateLabel(log_Num, "");
        return addInData();
    }

    /**
     * 是否展示文件拓展名选项监听
     */
    @FXML
    private void handleCheckBoxAction() throws Exception {
        ObservableList<FileNumBean> fileBeans = tableView_Num.getItems();
        if (CollectionUtils.isNotEmpty(fileBeans)) {
            reselect();
        }
    }

}
