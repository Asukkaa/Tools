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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;
import priv.koishi.tools.Configuration.FileConfig;
import priv.koishi.tools.Properties.ToolsProperties;
import priv.koishi.tools.ThreadPool.CommonThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static priv.koishi.tools.Service.FileNameToExcelService.buildFileNameExcel;
import static priv.koishi.tools.Service.ReadDataService.readFile;
import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningOutputStream;
import static priv.koishi.tools.Utils.FileUtils.readAllFiles;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

public class FileNameToExcelController extends ToolsProperties {

    /**
     * 要处理的文件夹路径
     */
    static String inFilePath;

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
    static String tabId = "_Name";

    /**
     * 默认起始输出列
     */
    static int defaultStartCell = 1;

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
    private VBox vbox_Name;

    @FXML
    private ProgressBar progressBar_Name;

    @FXML
    private TableView<FileBean> tableView_Name;

    @FXML
    private TableColumn<FileBean, Integer> id_Name;

    @FXML
    private TableColumn<FileBean, String> name_Name, path_Name, size_Name, fileType_Name, creatDate_Name, updateDate_Name;

    @FXML
    private ChoiceBox<String> excelType_Name, hideFileType_Name, directoryNameType_Name;

    @FXML
    private CheckBox recursion_Name, openDirectory_Name, openFile_Name, showFileType_Name;

    @FXML
    private Label outPath_Name, excelPath_Name, fileNumber_Name, inPath_Name, log_Name, tip_Name;

    @FXML
    private TextField excelName_Name, sheetName_Name, startRow_Name, startCell_Name, filterFileType_Name;

    @FXML
    private Button fileButton_Name, clearButton_Name, exportButton_Name, reselectButton_Name, removeExcelButton_Name, excelPathButton_Img;

    /**
     * 组件自适应宽高
     */
    public static void fileNameToExcelAdaption(Stage stage, Scene scene) {
        //设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Name");
        table.setPrefHeight(stageHeight * 0.5);
        //设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        Node vbox = scene.lookup("#vbox_Name");
        vbox.setLayoutX(stageWidth * 0.03);
        Node tableView = scene.lookup("#tableView_Name");
        tableView.setStyle("-fx-pref-width: " + tableWidth + "px;");
        Node id = scene.lookup("#id_Name");
        id.setStyle("-fx-pref-width: " + tableWidth * 0.04 + "px;");
        Node name = scene.lookup("#name_Name");
        name.setStyle("-fx-pref-width: " + tableWidth * 0.14 + "px;");
        Node fileType = scene.lookup("#fileType_Name");
        fileType.setStyle("-fx-pref-width: " + tableWidth * 0.06 + "px;");
        Node path = scene.lookup("#path_Name");
        path.setStyle("-fx-pref-width: " + tableWidth * 0.36 + "px;");
        Node size = scene.lookup("#size_Name");
        size.setStyle("-fx-pref-width: " + tableWidth * 0.08 + "px;");
        Node creatDate = scene.lookup("#creatDate_Name");
        creatDate.setStyle("-fx-pref-width: " + tableWidth * 0.16 + "px;");
        Node updateDate = scene.lookup("#updateDate_Name");
        updateDate.setStyle("-fx-pref-width: " + tableWidth * 0.16 + "px;");
        Label fileNum = (Label) scene.lookup("#fileNumber_Name");
        Button removeAll = (Button) scene.lookup("#clearButton_Name");
        Button exportAll = (Button) scene.lookup("#exportButton_Name");
        Button reselect = (Button) scene.lookup("#reselectButton_Name");
        ProgressBar progressBar = (ProgressBar) scene.lookup("#progressBar_Name");
        Label tip = (Label) scene.lookup("#tip_Name");
        fileNum.setPrefWidth(tableWidth - removeAll.getWidth() - exportAll.getWidth() - reselect.getWidth() - 40);
        tip.setPrefWidth(tableWidth - progressBar.getWidth() - 10);
    }

    /**
     * 保存最后一次配置的值
     */
    public static void fileNameToExcelSaveLastConfig(Scene scene) throws IOException {
        InputStream input = checkRunningInputStream(configFile_Name);
        Properties prop = new Properties();
        prop.load(input);
        ChoiceBox<?> directoryNameType = (ChoiceBox<?>) scene.lookup("#directoryNameType_Name");
        prop.put(key_lastDirectoryNameType, directoryNameType.getValue());
        ChoiceBox<?> hideFileType = (ChoiceBox<?>) scene.lookup("#hideFileType_Name");
        prop.put(key_lastHideFileType, hideFileType.getValue());
        CheckBox recursion = (CheckBox) scene.lookup("#recursion_Name");
        String recursionValue = recursion.isSelected() ? activation : unActivation;
        prop.put(key_lastRecursion, recursionValue);
        CheckBox showFileType = (CheckBox) scene.lookup("#showFileType_Name");
        String showFileTypeValue = showFileType.isSelected() ? activation : unActivation;
        prop.put(key_lastShowFileType, showFileTypeValue);
        CheckBox openDirectory = (CheckBox) scene.lookup("#openDirectory_Name");
        String openDirectoryValue = openDirectory.isSelected() ? activation : unActivation;
        prop.put(key_lastOpenDirectory, openDirectoryValue);
        CheckBox openFile = (CheckBox) scene.lookup("#openFile_Name");
        String openFileValue = openFile.isSelected() ? activation : unActivation;
        prop.put(key_lastOpenFile, openFileValue);
        TextField excelName = (TextField) scene.lookup("#excelName_Name");
        prop.put(key_lastExcelName, excelName.getText());
        TextField sheetName = (TextField) scene.lookup("#sheetName_Name");
        prop.put(key_lastSheetName, sheetName.getText());
        ChoiceBox<?> excelType = (ChoiceBox<?>) scene.lookup("#excelType_Name");
        prop.put(key_lastExcelType, excelType.getValue());
        TextField startRow = (TextField) scene.lookup("#startRow_Name");
        prop.put(key_lastStartRow, startRow.getText());
        TextField startCell = (TextField) scene.lookup("#startCell_Name");
        prop.put(key_lastStartCell, startCell.getText());
        TextField filterFileType = (TextField) scene.lookup("#filterFileType_Name");
        prop.put(key_lastFilterFileType, filterFileType.getText());
        Label inPath = (Label) scene.lookup("#inPath_Name");
        prop.put(key_lastInPath, inPath.getText());
        Label outPath = (Label) scene.lookup("#outPath_Name");
        prop.put(key_lastOutPath, outPath.getText());
        Label excelPath = (Label) scene.lookup("#excelPath_Name");
        prop.put(key_lastExcelPath, excelPath.getText());
        OutputStream output = checkRunningOutputStream(configFile_Name);
        prop.store(output, null);
        input.close();
        output.close();
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
        taskBean.setShowFileType(showFileType_Name.isSelected())
                .setDisableControls(disableControls)
                .setProgressBar(progressBar_Name)
                .setMassageLabel(fileNumber_Name)
                .setTableView(tableView_Name)
                .setTableColumn(size_Name)
                .setInFileList(inFileList)
                .setTabId(tabId);
        //获取Task任务
        Task<Void> readFileTask = readFile(taskBean);
        //绑定带进度条的线程
        bindingProgressBarTask(readFileTask, taskBean);
        readFileTask.setOnSucceeded(event -> {
            taskUnbind(taskBean);
            //设置列表通过拖拽排序行
            tableViewDragRow(tableView_Name);
            //构建右键菜单
            tableViewContextMenu(tableView_Name, fileNumber_Name);
        });
        executorService.execute(readFileTask);
    }

    /**
     * 读取配置文件
     */
    private static void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Name);
        prop.load(input);
        inFilePath = prop.getProperty(key_inFilePath);
        outFilePath = prop.getProperty(key_outFilePath);
        defaultOutFileName = prop.getProperty(key_defaultOutFileName);
        defaultSheetName = prop.getProperty(key_defaultSheetName);
        excelInPath = prop.getProperty(key_excelInPath);
        defaultStartCell = Integer.parseInt(prop.getProperty(key_defaultStartCell));
        input.close();
    }

    /**
     * 设置初始配置值为上次配置值
     */
    private void setLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Name);
        prop.load(input);
        if (activation.equals(prop.getProperty(key_loadLastConfig))) {
            setControlLastConfig(directoryNameType_Name, prop, key_lastDirectoryNameType, false);
            setControlLastConfig(hideFileType_Name, prop, key_lastHideFileType, false);
            setControlLastConfig(recursion_Name, prop, key_lastRecursion, false);
            setControlLastConfig(showFileType_Name, prop, key_lastShowFileType, false);
            setControlLastConfig(openDirectory_Name, prop, key_lastOpenDirectory, false);
            setControlLastConfig(openFile_Name, prop, key_lastOpenFile, false);
            setControlLastConfig(excelName_Name, prop, key_lastExcelName, false);
            setControlLastConfig(sheetName_Name, prop, key_lastSheetName, false);
            setControlLastConfig(excelType_Name, prop, key_lastExcelType, false);
            setControlLastConfig(startRow_Name, prop, key_lastStartRow, false);
            setControlLastConfig(startCell_Name, prop, key_lastStartCell, false);
            setControlLastConfig(filterFileType_Name, prop, key_lastFilterFileType, false);
            setControlLastConfig(inPath_Name, prop, key_lastInPath, false);
            setControlLastConfig(outPath_Name, prop, key_lastOutPath, false);
            setControlLastConfig(excelPath_Name, prop, key_lastExcelPath, false);
        }
        input.close();
    }

    /**
     * 设置要防重复点击的组件
     */
    private void bindPrefWidthProperty() {
        id_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.04));
        name_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.14));
        fileType_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.06));
        path_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.36));
        size_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.08));
        creatDate_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.16));
        updateDate_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.16));
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(startRow_Name, tip_startRow);
        addToolTip(tip_Name, tip_Name.getText());
        addToolTip(recursion_Name, tip_recursion);
        addToolTip(excelName_Name, tip_excelName);
        addToolTip(sheetName_Name, tip_sheet);
        addToolTip(filterFileType_Name, tip_filterFileType);
        addToolTip(removeExcelButton_Name, tip_removeExcelButton);
        addToolTip(startCell_Name, text_onlyNaturalNumber + defaultStartCell);
    }

    /**
     * 设置javafx单元格宽度
     */
    private void setDisableControls() {
        disableControls.add(fileButton_Name);
        disableControls.add(clearButton_Name);
        disableControls.add(exportButton_Name);
        disableControls.add(showFileType_Name);
        disableControls.add(reselectButton_Name);
        disableControls.add(excelPathButton_Img);
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() throws IOException {
        //读取全局变量配置
        getConfig();
        //设置初始配置值为上次配置值
        setLastConfig();
        //设置要防重复点击的组件
        setDisableControls();
        //设置鼠标悬停提示
        setToolTip();
        //设置javafx单元格宽度
        bindPrefWidthProperty();
    }

    /**
     * 选择文件夹按钮功能
     */
    @FXML
    private void inDirectoryButton(ActionEvent actionEvent) throws Exception {
        getConfig();
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Name);
        // 显示文件选择器
        File selectedFile = creatDirectoryChooser(actionEvent, inFilePath, text_selectDirectory);
        FileConfig fileConfig = new FileConfig();
        fileConfig.setShowDirectoryName(directoryNameType_Name.getValue())
                .setShowHideFile(hideFileType_Name.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setRecursion(recursion_Name.isSelected())
                .setInFile(selectedFile);
        if (selectedFile != null) {
            //更新所选文件路径显示
            inFilePath = updatePathLabel(selectedFile.getPath(), inFilePath, key_inFilePath, inPath_Name, configFile_Name);
            //读取数据
            addInData(readAllFiles(fileConfig));
        }
    }

    /**
     * 拖拽释放行为
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) throws Exception {
        removeAll();
        List<File> files = dragEvent.getDragboard().getFiles();
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Name);
        File file = files.getFirst();
        FileConfig fileConfig = new FileConfig();
        fileConfig.setShowDirectoryName(directoryNameType_Name.getValue())
                .setShowHideFile(hideFileType_Name.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setRecursion(recursion_Name.isSelected())
                .setInFile(file);
        String filePath = file.getPath();
        inPath_Name.setText(filePath);
        addToolTip(inPath_Name, filePath);
        addInData(readAllFiles(fileConfig));
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
        tableView_Name.setItems(nullData);
        updateLabel(fileNumber_Name, text_dataListNull);
        updateLabel(log_Name, "");
        System.gc();
    }

    /**
     * 导出excel按钮
     */
    @FXML
    private void exportAll() throws Exception {
        updateLabel(log_Name, "");
        String outFilePath = outPath_Name.getText();
        ObservableList<FileBean> fileBeans = tableView_Name.getItems();
        if (StringUtils.isEmpty(outFilePath)) {
            throw new Exception(text_outPathNull);
        }
        if (CollectionUtils.isEmpty(fileBeans)) {
            throw new Exception(text_fileListNull);
        }
        updateLabel(log_Name, "");
        ExcelConfig excelConfig = new ExcelConfig();
        excelConfig.setStartCellNum(setDefaultIntValue(startCell_Name, defaultStartCell, 0, null))
                .setStartRowNum(setDefaultIntValue(startRow_Name, 0, 0, null))
                .setOutName(setDefaultFileName(excelName_Name, defaultOutFileName))
                .setSheet(setDefaultStrValue(sheetName_Name, defaultSheetName))
                .setOutExcelExtension(excelType_Name.getValue())
                .setInPath(excelPath_Name.getText())
                .setOutPath(outFilePath);
        TaskBean<FileBean> taskBean = new TaskBean<>();
        taskBean.setShowFileType(showFileType_Name.isSelected())
                .setDisableControls(disableControls)
                .setProgressBar(progressBar_Name)
                .setTableView(tableView_Name)
                .setMassageLabel(log_Name)
                .setBeanList(fileBeans)
                .setTabId(tabId);
        //获取Task任务
        Task<SXSSFWorkbook> buildExcelTask = buildFileNameExcel(excelConfig, taskBean);
        //绑定带进度条的线程
        bindingProgressBarTask(buildExcelTask, taskBean);
        executorService.execute(buildExcelTask);
        //线程成功后保存excel
        saveExcelOnSucceeded(excelConfig, taskBean, buildExcelTask, openDirectory_Name, openFile_Name, executorService);
    }

    /**
     * 设置导出文件按钮
     */
    @FXML
    private void exportPath(ActionEvent actionEvent) throws IOException {
        getConfig();
        File selectedFile = creatDirectoryChooser(actionEvent, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            //更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Name, configFile_Name);
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
            //更新所选文件路径显示
            excelInPath = updatePathLabel(selectedFile.getPath(), excelInPath, key_excelInPath, excelPath_Name, configFile_Name);
            removeExcelButton_Name.setVisible(true);
        }
    }

    /**
     * 限制导出预留行只能输入自然数
     */
    @FXML
    private void rowHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(startRow_Name, 0, null, event);
        addValueToolTip(startRow_Name, tip_startRow);
    }

    /**
     * 限制导出预留列只能输入自然数
     */
    @FXML
    private void cellHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(startCell_Name, 0, null, event);
        addValueToolTip(startCell_Name, text_onlyNaturalNumber + defaultStartCell);
    }

    /**
     * 鼠标悬留提示输入的导出excel文件名称
     */
    @FXML
    private void nameHandleKeyTyped() {
        addValueToolTip(excelName_Name, tip_excelName);
    }

    /**
     * 鼠标悬留提示输入的导出excel表名称
     */
    @FXML
    private void sheetHandleKeyTyped() {
        addValueToolTip(sheetName_Name, tip_sheet);
    }

    /**
     * 鼠标悬留提示输入的需要识别的文件后缀名
     */
    @FXML
    private void filterHandleKeyTyped() {
        addValueToolTip(filterFileType_Name, tip_filterFileType);
    }

    /**
     * 重新查询按钮
     */
    @FXML
    private void reselect() throws Exception {
        String inFilePath = inPath_Name.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception(text_filePathNull);
        }
        if (!new File(inFilePath).exists()){
            throw new Exception(text_fileNotExists);
        }
        updateLabel(log_Name, "");
        FileConfig fileConfig = new FileConfig();
        fileConfig.setFilterExtensionList(getFilterExtensionList(filterFileType_Name))
                .setShowDirectoryName(directoryNameType_Name.getValue())
                .setShowHideFile(hideFileType_Name.getValue())
                .setRecursion(recursion_Name.isSelected())
                .setInFile(new File(inFilePath));
        addInData(readAllFiles(fileConfig));
    }

    /**
     * 是否展示文件拓展名选项监听
     */
    @FXML
    private void handleCheckBoxAction() throws Exception {
        ObservableList<FileBean> fileBeans = tableView_Name.getItems();
        if (CollectionUtils.isNotEmpty(fileBeans)) {
            reselect();
        }
    }

    /**
     * 清空excel模板路径按钮
     */
    @FXML
    private void removeExcelPath() {
        excelPath_Name.setText("");
        excelPath_Name.setTooltip(null);
        removeExcelButton_Name.setVisible(false);
    }

}
