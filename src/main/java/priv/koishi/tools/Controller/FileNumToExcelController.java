package priv.koishi.tools.Controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;
import priv.koishi.tools.Configuration.FileConfig;
import priv.koishi.tools.Properties.CommonProperties;
import priv.koishi.tools.ThreadPool.CommonThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static priv.koishi.tools.Service.FileNumToExcelService.buildNameGroupNumExcel;
import static priv.koishi.tools.Service.ReadDataService.readExcel;
import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningOutputStream;
import static priv.koishi.tools.Utils.FileUtils.getFileType;
import static priv.koishi.tools.Utils.FileUtils.readAllFiles;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 分组统计文件夹下文件数量页面控制器
 *
 * @author KOISHI
 * Date:2024-10-08
 * Time:下午3:29
 */
public class FileNumToExcelController extends CommonProperties {

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
    static int defaultStartCell;

    /**
     * 默认起始读取行
     */
    static int defaultReadRow;

    /**
     * 默认起始读取列
     */
    static int defaultReadCell;

    /**
     * 要防重复点击的组件
     */
    static List<Control> disableControls = new ArrayList<>();

    /**
     * 线程池
     */
    private final CommonThreadPoolExecutor commonThreadPoolExecutor = new CommonThreadPoolExecutor();

    /**
     * 线程池实例
     */
    ExecutorService executorService = commonThreadPoolExecutor.createNewThreadPool();

    @FXML
    private AnchorPane anchorPane_Num;

    @FXML
    private VBox vbox_Num;

    @FXML
    private HBox fileNumberHBox_Num;

    @FXML
    private ProgressBar progressBar_Num;

    @FXML
    private TableView<FileNumBean> tableView_Num;

    @FXML
    private ChoiceBox<String> hideFileType_Num, directoryNameType_Num;

    @FXML
    private TableColumn<FileNumBean, Integer> groupName_Num, groupNumber_Num;

    @FXML
    private TableColumn<FileNumBean, String> fileName_Num, groupId_Num, fileUnitSize_Num;

    @FXML
    private Label outPath_Num, excelPath_Num, fileNumber_Num, inPath_Num, log_Num, excelType_Num, excelTypeLabel_Num;

    @FXML
    private Button fileButton_Num, clearButton_Num, exportButton_Num, reselectButton_Num, excelPathButton_Num, outPathButton_Num;

    @FXML
    private CheckBox recursion_Num, openDirectory_Num, openFile_Num, showFileType_Num, exportTitle_Num, exportFileNum_Num, exportFileSize_Num;

    @FXML
    private TextField excelName_Num, sheetName_Num, startRow_Num, startCell_Num, filterFileType_Num, subCode_Num, readRow_Num, readCell_Num, maxRow_Num;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void fileNumToExcelAdaption(Stage stage) {
        Scene scene = stage.getScene();
        //设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Num");
        table.setPrefHeight(stageHeight * 0.5);
        //设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        table.setMaxWidth(stageWidth);
        Node fileNumVbox = scene.lookup("#vbox_Num");
        fileNumVbox.setLayoutX(stageWidth * 0.03);
        Node groupId = scene.lookup("#groupId_Num");
        groupId.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node groupNameNum = scene.lookup("#groupName_Num");
        groupNameNum.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node groupNumberNum = scene.lookup("#groupNumber_Num");
        groupNumberNum.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node fileNameNum = scene.lookup("#fileName_Num");
        fileNameNum.setStyle("-fx-pref-width: " + tableWidth * 0.6 + "px;");
        Node fileUnitSize = scene.lookup("#fileUnitSize_Num");
        fileUnitSize.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Label fileNum = (Label) scene.lookup("#fileNumber_Num");
        HBox fileNumberHBox = (HBox) scene.lookup("#fileNumberHBox_Num");
        nodeRightAlignment(fileNumberHBox, tableWidth, fileNum);
    }

    /**
     * 保存最后一次配置的值
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    public static void fileNumToExcelSaveLastConfig(Scene scene) throws IOException {
        AnchorPane anchorPane = (AnchorPane) scene.lookup("#anchorPane_Num");
        if (anchorPane != null) {
            InputStream input = checkRunningInputStream(configFile_Num);
            Properties prop = new Properties();
            prop.load(input);
            ChoiceBox<?> directoryNameType = (ChoiceBox<?>) scene.lookup("#directoryNameType_Num");
            prop.put(key_lastDirectoryNameType, directoryNameType.getValue());
            ChoiceBox<?> hideFileType = (ChoiceBox<?>) scene.lookup("#hideFileType_Num");
            prop.put(key_lastHideFileType, hideFileType.getValue());
            CheckBox recursion = (CheckBox) scene.lookup("#recursion_Num");
            String recursionValue = recursion.isSelected() ? activation : unActivation;
            prop.put(key_lastRecursion, recursionValue);
            CheckBox showFileType = (CheckBox) scene.lookup("#showFileType_Num");
            String showFileTypeValue = showFileType.isSelected() ? activation : unActivation;
            prop.put(key_lastShowFileType, showFileTypeValue);
            CheckBox openDirectory = (CheckBox) scene.lookup("#openDirectory_Num");
            String openDirectoryValue = openDirectory.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, openDirectoryValue);
            CheckBox openFile = (CheckBox) scene.lookup("#openFile_Num");
            String openFileValue = openFile.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenFile, openFileValue);
            TextField excelName = (TextField) scene.lookup("#excelName_Num");
            prop.put(key_lastExcelName, excelName.getText());
            TextField sheetName = (TextField) scene.lookup("#sheetName_Num");
            prop.put(key_lastSheetName, sheetName.getText());
            TextField subCode = (TextField) scene.lookup("#subCode_Num");
            prop.put(key_lastSubCode, subCode.getText());
            TextField startRow = (TextField) scene.lookup("#startRow_Num");
            prop.put(key_lastStartRow, startRow.getText());
            TextField startCell = (TextField) scene.lookup("#startCell_Num");
            prop.put(key_lastStartCell, startCell.getText());
            TextField readRow = (TextField) scene.lookup("#readRow_Num");
            prop.put(key_lastReadRow, readRow.getText());
            TextField readCell = (TextField) scene.lookup("#readCell_Num");
            prop.put(key_lastReadCell, readCell.getText());
            TextField maxRow = (TextField) scene.lookup("#maxRow_Num");
            prop.put(key_lastMaxRow, maxRow.getText());
            TextField filterFileType = (TextField) scene.lookup("#filterFileType_Num");
            prop.put(key_lastFilterFileType, filterFileType.getText());
            Label inPath = (Label) scene.lookup("#inPath_Num");
            prop.put(key_lastInPath, inPath.getText());
            Label outPath = (Label) scene.lookup("#outPath_Num");
            prop.put(key_lastOutPath, outPath.getText());
            Label excelPath = (Label) scene.lookup("#excelPath_Num");
            prop.put(key_lastExcelPath, excelPath.getText());
            CheckBox exportFileNum = (CheckBox) scene.lookup("#exportFileNum_Num");
            String exportFileNumValue = exportFileNum.isSelected() ? activation : unActivation;
            prop.put(key_lastExportFileNum, exportFileNumValue);
            CheckBox exportFileSize = (CheckBox) scene.lookup("#exportFileSize_Num");
            String exportFileSizeValue = exportFileSize.isSelected() ? activation : unActivation;
            prop.put(key_lastExportFileSize, exportFileSizeValue);
            CheckBox exportTitle = (CheckBox) scene.lookup("#exportTitle_Num");
            String exportTitleValue = exportTitle.isSelected() ? activation : unActivation;
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
        //列表中有excel分组后再匹配数据
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
     *
     * @return 读取excel任务线程
     */
    private Task<List<FileNumBean>> addInData() {
        removeAll();
        //渲染表格前需要更新一下读取的文件
        updateInFileList();
        String excelPath = excelPath_Num.getText();
        excelType_Num.setText(getFileType(new File(excelPath)));
        //组装数据
        ExcelConfig excelConfig = new ExcelConfig();
        excelConfig.setReadCellNum(setDefaultIntValue(readCell_Num, defaultReadCell, 0, null))
                .setReadRowNum(setDefaultIntValue(readRow_Num, defaultReadRow, 0, null))
                .setMaxRowNum(setDefaultIntValue(maxRow_Num, -1, 1, null))
                .setInPath(excelPath_Num.getText())
                .setSheetName(sheetName_Num.getText());
        TaskBean<FileNumBean> taskBean = new TaskBean<>();
        taskBean.setShowFileType(showFileType_Num.isSelected())
                .setComparatorTableColumn(fileUnitSize_Num)
                .setDisableControls(disableControls)
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
            setControlLastConfig(inPath_Num, prop, key_lastInPath, false, anchorPane_Num);
            setControlLastConfig(maxRow_Num, prop, key_lastMaxRow, false, null);
            setControlLastConfig(subCode_Num, prop, key_lastSubCode, true, null);
            setControlLastConfig(outPath_Num, prop, key_lastOutPath, false, anchorPane_Num);
            setControlLastConfig(readRow_Num, prop, key_lastReadRow, false, null);
            setControlLastConfig(readCell_Num, prop, key_lastReadCell, false, null);
            setControlLastConfig(startRow_Num, prop, key_lastStartRow, false, null);
            setControlLastConfig(openFile_Num, prop, key_lastOpenFile, false, null);
            setControlLastConfig(excelPath_Num, prop, key_lastExcelPath, false, anchorPane_Num);
            setControlLastConfig(excelName_Num, prop, key_lastExcelName, false, null);
            setControlLastConfig(sheetName_Num, prop, key_lastSheetName, false, null);
            setControlLastConfig(startCell_Num, prop, key_lastStartCell, false, null);
            setControlLastConfig(recursion_Num, prop, key_lastRecursion, false, null);
            setControlLastConfig(exportTitle_Num, prop, key_lastExportTitle, false, null);
            setControlLastConfig(showFileType_Num, prop, key_lastShowFileType, false, null);
            setControlLastConfig(hideFileType_Num, prop, key_lastHideFileType, false, null);
            setControlLastConfig(openDirectory_Num, prop, key_lastOpenDirectory, false, null);
            setControlLastConfig(exportFileNum_Num, prop, key_lastExportFileNum, false, null);
            setControlLastConfig(exportFileSize_Num, prop, key_lastExportFileSize, false, null);
            setControlLastConfig(filterFileType_Num, prop, key_lastFilterFileType, false, null);
            setControlLastConfig(directoryNameType_Num, prop, key_lastDirectoryNameType, false, null);
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
    private void setDisableControls() {
        disableControls.add(fileButton_Num);
        disableControls.add(clearButton_Num);
        disableControls.add(exportButton_Num);
        disableControls.add(showFileType_Num);
        disableControls.add(outPathButton_Num);
        disableControls.add(reselectButton_Num);
        disableControls.add(excelPathButton_Num);
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
        //限制读取最大行数只能输入正整数
        integerRangeTextField(maxRow_Num, 1, null, tip_maxRow);
        //限制导出预留行只能输入自然数
        integerRangeTextField(startRow_Num, 0, null, text_onlyNaturalNumber + defaultStartCell);
        //限制导出预留列只能输入自然数
        integerRangeTextField(startCell_Num, 0, null, text_onlyNaturalNumber + defaultStartCell);
        //限制读取起始行只能输入自然数
        integerRangeTextField(readRow_Num, 0, null, text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row);
        //限制读取起始列只能输入自然数
        integerRangeTextField(readCell_Num, 0, null, text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell);
        //鼠标悬留提示输入的文件名称分割符
        textFieldValueListener(subCode_Num, tip_subCode);
        //鼠标悬留提示输入的导出excel表名称
        textFieldValueListener(sheetName_Num, tip_sheetName);
        //鼠标悬留提示输入的导出excel文件名称
        textFieldValueListener(excelName_Num, tip_excelName + defaultOutFileName);
        //鼠标悬留提示输入的需要识别的文件后缀名
        textFieldValueListener(filterFileType_Num, tip_filterFileType);
    }

    /**
     * 界面初始化
     *
     * @throws IOException io异常
     */
    @FXML
    private void initialize() throws IOException {
        //读取全局变量配置
        getConfig();
        //设置要防重复点击的组件
        setDisableControls();
        //设置鼠标悬停提示
        setToolTip();
        //设置javafx单元格宽度
        tableViewNumImgAdaption(groupId_Num, tableView_Num, groupName_Num.prefWidthProperty(), groupNumber_Num.prefWidthProperty(), fileName_Num, fileUnitSize_Num);
        //给输入框添加内容变化监听
        textFieldChangeListener();
        //设置初始配置值为上次配置值
        setLastConfig();
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
        // 显示文件选择器
        File selectedFile = creatDirectoryChooser(actionEvent, inFilePath, text_selectDirectory);
        if (selectedFile != null) {
            //更新所选文件路径显示
            inFilePath = updatePathLabel(selectedFile.getPath(), inFilePath, key_inFilePath, inPath_Num, configFile_Num, anchorPane_Num);
            //读取文件数据
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
        addInData();
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
        Task<List<FileNumBean>> reselectTask = reselect();
        reselectTask.setOnSucceeded(event -> {
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setShowFileType(showFileType_Num.isSelected())
                    .setBeanList(reselectTask.getValue())
                    .setDisableControls(disableControls)
                    .setSubCode(subCode_Num.getText())
                    .setProgressBar(progressBar_Num)
                    .setTableView(tableView_Num)
                    .setInFileList(inFileList)
                    .setMassageLabel(log_Num)
                    .setTabId(tabId);
            //获取Task任务
            Task<Workbook> buildExcelTask = buildNameGroupNumExcel(taskBean, excelConfig);
            //线程成功后保存excel
            saveExcelOnSucceeded(excelConfig, taskBean, buildExcelTask, openDirectory_Num, openFile_Num, executorService);
        });
        //使用新线程启动
        executorService.execute(reselectTask);
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
        File selectedFile = creatDirectoryChooser(actionEvent, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            //更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Num, configFile_Num, anchorPane_Num);
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
        File selectedFile = creatFileChooser(actionEvent, excelInPath, extensionFilters, text_selectExcel);
        if (selectedFile != null) {
            //更新所选文件路径显示
            excelInPath = updatePathLabel(selectedFile.getPath(), excelInPath, key_excelInPath, excelPath_Num, configFile_Num, anchorPane_Num);
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
