package priv.koishi.tools.Controller;

import javafx.application.Platform;
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
import priv.koishi.tools.Bean.FileBean;
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

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Service.FileNameToExcelService.buildFileNameExcel;
import static priv.koishi.tools.Service.ReadDataService.readFile;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 获取文件夹下的文件信息页面控制器
 *
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午1:48
 */
public class FileNameToExcelController extends CommonProperties {

    /**
     * 要处理的文件夹路径
     */
    private static String inFilePath;

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
    private static final String tabId = "_Name";

    /**
     * 默认起始输出列
     */
    private static int defaultStartCell;

    /**
     * 要防重复点击的组件
     */
    private static final List<Node> disableNodes = new ArrayList<>();

    /**
     * 线程池
     */
    private final CommonThreadPoolExecutor commonThreadPoolExecutor = new CommonThreadPoolExecutor();

    /**
     * 线程池实例
     */
    private final ExecutorService executorService = commonThreadPoolExecutor.createNewThreadPool();

    /**
     * 读取文件线程
     */
    private Task<Void> readFileTask;

    /**
     * 构建excel线程
     */
    private Task<Workbook> buildExcelTask;

    /**
     * 程序主场景
     */
    private Scene mainScene;

    @FXML
    private AnchorPane anchorPane_Name;

    @FXML
    private VBox vbox_Name;

    @FXML
    private ProgressBar progressBar_Name;

    @FXML
    private HBox fileNumberHBox_Name, tipHBox_Name;

    @FXML
    private TableView<FileBean> tableView_Name;

    @FXML
    private TableColumn<FileBean, Integer> id_Name;

    @FXML
    private TableColumn<FileBean, String> name_Name, path_Name, size_Name, fileType_Name,
            creatDate_Name, updateDate_Name, showStatus_Name;

    @FXML
    private ChoiceBox<String> excelType_Name, hideFileType_Name, directoryNameType_Name;

    @FXML
    private TextField excelName_Name, sheetName_Name, startRow_Name, startCell_Name, filterFileType_Name;

    @FXML
    private Label outPath_Name, excelPath_Name, fileNumber_Name, inPath_Name, log_Name, tip_Name, excelTypeLabel_Name;

    @FXML
    private CheckBox recursion_Name, openDirectory_Name, openFile_Name, showFileType_Name,
            exportTitle_Name, exportFullList_Name;

    @FXML
    private Button fileButton_Name, clearButton_Name, exportButton_Name, reselectButton_Name,
            removeExcelButton_Name, excelPathButton_Name, outPathButton_Name;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void fileNameToExcelAdaption(Stage stage) {
        Scene scene = stage.getScene();
        // 设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Name");
        table.setPrefHeight(stageHeight * 0.45);
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        table.setMaxWidth(tableWidth);
        Node vbox = scene.lookup("#vbox_Name");
        vbox.setLayoutX(stageWidth * 0.03);
        Node id = scene.lookup("#id_Name");
        id.setStyle("-fx-pref-width: " + tableWidth * 0.04 + "px;");
        Node name = scene.lookup("#name_Name");
        name.setStyle("-fx-pref-width: " + tableWidth * 0.14 + "px;");
        Node fileType = scene.lookup("#fileType_Name");
        fileType.setStyle("-fx-pref-width: " + tableWidth * 0.06 + "px;");
        Node path = scene.lookup("#path_Name");
        path.setStyle("-fx-pref-width: " + tableWidth * 0.3 + "px;");
        Node size = scene.lookup("#size_Name");
        size.setStyle("-fx-pref-width: " + tableWidth * 0.08 + "px;");
        Node showStatus = scene.lookup("#showStatus_Name");
        showStatus.setStyle("-fx-pref-width: " + tableWidth * 0.06 + "px;");
        Node creatDate = scene.lookup("#creatDate_Name");
        creatDate.setStyle("-fx-pref-width: " + tableWidth * 0.16 + "px;");
        Node updateDate = scene.lookup("#updateDate_Name");
        updateDate.setStyle("-fx-pref-width: " + tableWidth * 0.16 + "px;");
        Label fileNum = (Label) scene.lookup("#fileNumber_Name");
        HBox fileNumberHBox = (HBox) scene.lookup("#fileNumberHBox_Name");
        nodeRightAlignment(fileNumberHBox, tableWidth, fileNum);
        Label tip = (Label) scene.lookup("#tip_Name");
        HBox tipHBox = (HBox) scene.lookup("#tipHBox_Name");
        nodeRightAlignment(tipHBox, tableWidth, tip);
    }

    /**
     * 保存最后一次配置的值
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    public static void fileNameToExcelSaveLastConfig(Scene scene) throws IOException {
        AnchorPane anchorPane = (AnchorPane) scene.lookup("#anchorPane_Name");
        if (anchorPane != null) {
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
            CheckBox exportTitle = (CheckBox) scene.lookup("#exportTitle_Name");
            String lastExportTitleValue = exportTitle.isSelected() ? activation : unActivation;
            prop.put(key_lastExportTitle, lastExportTitleValue);
            CheckBox exportFullList = (CheckBox) scene.lookup("#exportFullList_Name");
            String lastExportFullListValue = exportFullList.isSelected() ? activation : unActivation;
            prop.put(key_lastExportFullList, lastExportFullListValue);
            OutputStream output = checkRunningOutputStream(configFile_Name);
            prop.store(output, null);
            input.close();
            output.close();
        }
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
            ChoiceBox<?> sort = (ChoiceBox<?>) mainScene.lookup("#sort_Set");
            String sortValue = (String) sort.getValue();
            CheckBox reverseSort = (CheckBox) mainScene.lookup("#reverseSort_Set");
            TaskBean<FileBean> taskBean = new TaskBean<>();
            taskBean.setShowFileType(showFileType_Name.isSelected())
                    .setReverseSort(reverseSort.isSelected())
                    .setComparatorTableColumn(size_Name)
                    .setDisableNodes(disableNodes)
                    .setProgressBar(progressBar_Name)
                    .setMassageLabel(fileNumber_Name)
                    .setTableView(tableView_Name)
                    .setInFileList(inFileList)
                    .setSortType(sortValue)
                    .setTabId(tabId);
            // 获取Task任务
            readFileTask = readFile(taskBean);
            // 绑定带进度条的线程
            bindingProgressBarTask(readFileTask, taskBean);
            readFileTask.setOnSucceeded(event -> {
                taskUnbind(taskBean);
                // 设置列表通过拖拽排序行
                tableViewDragRow(tableView_Name);
                // 构建右键菜单
                tableViewContextMenu(tableView_Name, fileNumber_Name, anchorPane_Name);
                readFileTask = null;
            });
            if (!readFileTask.isRunning()) {
                executorService.execute(readFileTask);
            }
        }
    }

    /**
     * 读取配置文件
     *
     * @throws IOException io异常
     */
    private void getConfig() throws IOException {
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
     *
     * @throws IOException io异常
     */
    private void setLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Name);
        prop.load(input);
        if (activation.equals(prop.getProperty(key_loadLastConfig))) {
            setControlLastConfig(openFile_Name, prop, key_lastOpenFile);
            setControlLastConfig(startRow_Name, prop, key_lastStartRow);
            setControlLastConfig(excelName_Name, prop, key_lastExcelName);
            setControlLastConfig(sheetName_Name, prop, key_lastSheetName);
            setControlLastConfig(excelType_Name, prop, key_lastExcelType);
            setControlLastConfig(startCell_Name, prop, key_lastStartCell);
            setControlLastConfig(recursion_Name, prop, key_lastRecursion);
            setControlLastConfig(exportTitle_Name, prop, key_lastExportTitle);
            setControlLastConfig(showFileType_Name, prop, key_lastShowFileType);
            setControlLastConfig(hideFileType_Name, prop, key_lastHideFileType);
            setControlLastConfig(openDirectory_Name, prop, key_lastOpenDirectory);
            setControlLastConfig(exportFullList_Name, prop, key_lastExportFullList);
            setControlLastConfig(filterFileType_Name, prop, key_lastFilterFileType);
            setControlLastConfig(inPath_Name, prop, key_lastInPath, anchorPane_Name);
            setControlLastConfig(outPath_Name, prop, key_lastOutPath, anchorPane_Name);
            setControlLastConfig(directoryNameType_Name, prop, key_lastDirectoryNameType);
            setControlLastConfig(excelPath_Name, prop, key_lastExcelPath, anchorPane_Name);
            String excelPath = prop.getProperty(key_lastExcelPath);
            if (StringUtils.isNotBlank(excelPath)) {
                removeExcelButton_Name.setVisible(true);
                excelType_Name.setValue(getFileType(new File(excelPath)));
                excelType_Name.setDisable(true);
            }
        }
        input.close();
    }

    /**
     * 设置列表各列宽度
     */
    private void bindPrefWidthProperty() {
        id_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.04));
        name_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.14));
        fileType_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.06));
        path_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.3));
        size_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.08));
        showStatus_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.06));
        creatDate_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.16));
        updateDate_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.16));
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_sheet, sheetName_Name);
        addToolTip(tip_openFile, openFile_Name);
        addToolTip(tip_startRow, startRow_Name);
        addToolTip(tip_Name.getText(), tip_Name);
        addToolTip(tip_recursion, recursion_Name);
        addToolTip(tip_excelName + defaultOutFileName, excelName_Name);
        addToolTip(tip_fileButton, fileButton_Name);
        addToolTip(tip_learButton, clearButton_Name);
        addToolTip(tip_exportTitle, exportTitle_Name);
        addToolTip(tip_showFileType, showFileType_Name);
        addToolTip(tip_exportButton, exportButton_Name);
        addToolTip(tip_hideFileType, hideFileType_Name);
        addToolTip(tip_outPathButton, outPathButton_Name);
        addToolTip(tip_openDirectory, openDirectory_Name);
        addToolTip(tip_reselectButton, reselectButton_Name);
        addToolTip(tip_exportFullList, exportFullList_Name);
        addToolTip(tip_filterFileType, filterFileType_Name);
        addToolTip(tip_excelPathButton, excelPathButton_Name);
        addToolTip(tip_directoryNameType, directoryNameType_Name);
        addToolTip(tip_removeExcelButton, removeExcelButton_Name);
        addToolTip(tip_excelType, excelType_Name, excelTypeLabel_Name);
        addToolTip(text_onlyNaturalNumber + defaultStartCell, startCell_Name);
    }

    /**
     * 设置javafx单元格宽度
     */
    private void setDisableNodes() {
        disableNodes.add(fileButton_Name);
        disableNodes.add(clearButton_Name);
        disableNodes.add(exportButton_Name);
        disableNodes.add(showFileType_Name);
        disableNodes.add(outPathButton_Name);
        disableNodes.add(reselectButton_Name);
        disableNodes.add(excelPathButton_Name);
        Node autoClickTab = mainScene.lookup("#autoClickTab");
        disableNodes.add(autoClickTab);
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 限制导出预留行只能输入自然数
        integerRangeTextField(startRow_Name, 0, null, tip_startRow);
        // 限制导出预留列只能输入自然数
        integerRangeTextField(startCell_Name, 0, null, text_onlyNaturalNumber + defaultStartCell);
        // 鼠标悬留提示输入的需要识别的文件后缀名
        textFieldValueListener(sheetName_Name, tip_sheet);
        // 鼠标悬留提示输入的需要识别的文件后缀名
        textFieldValueListener(excelName_Name, tip_excelName + defaultOutFileName);
        // 鼠标悬留提示输入的需要识别的文件后缀名
        textFieldValueListener(filterFileType_Name, tip_filterFileType);
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
        // 设置javafx单元格宽度
        bindPrefWidthProperty();
        // 给输入框添加内容变化监听
        textFieldChangeListener();
        // 设置初始配置值为上次配置值
        setLastConfig();
        Platform.runLater(() -> {
            mainScene = anchorPane_Name.getScene();
            // 设置要防重复点击的组件
            setDisableNodes();
        });
    }

    /**
     * 选择文件夹按钮功能
     *
     * @throws Exception io异常
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
            // 更新所选文件路径显示
            inFilePath = updatePathLabel(selectedFile.getPath(), inFilePath, key_inFilePath, inPath_Name, configFile_Name, anchorPane_Name);
            // 读取数据
            addInData(readAllFiles(fileConfig));
        }
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     * @throws Exception io异常
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
        addToolTip(filePath, inPath_Name);
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
        removeTableViewData(tableView_Name, fileNumber_Name, log_Name);
    }

    /**
     * 导出excel按钮
     *
     * @throws Exception 导出文件夹位置为空、要读取的文件列表为空
     */
    @FXML
    private void exportAll() throws Exception {
        if (buildExcelTask == null) {
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
                    .setSheetName(setDefaultStrValue(sheetName_Name, defaultSheetName))
                    .setExportFullList(exportFullList_Name.isSelected())
                    .setOutExcelType(excelType_Name.getValue())
                    .setExportTitle(exportTitle_Name.isSelected())
                    .setInPath(excelPath_Name.getText())
                    .setOutPath(outFilePath);
            TaskBean<FileBean> taskBean = new TaskBean<>();
            taskBean.setShowFileType(showFileType_Name.isSelected())
                    .setDisableNodes(disableNodes)
                    .setProgressBar(progressBar_Name)
                    .setTableView(tableView_Name)
                    .setMassageLabel(log_Name)
                    .setBeanList(fileBeans)
                    .setTabId(tabId);
            // 获取Task任务
            buildExcelTask = buildFileNameExcel(excelConfig, taskBean);
            // 绑定带进度条的线程
            bindingProgressBarTask(buildExcelTask, taskBean);
            if (!buildExcelTask.isRunning()) {
                executorService.execute(buildExcelTask);
            }
            // 线程成功后保存excel
            buildExcelTask = saveExcelOnSucceeded(excelConfig, taskBean, buildExcelTask, openDirectory_Name, openFile_Name, executorService);
        }
    }

    /**
     * 设置导出文件按钮
     *
     * @throws IOException io异常
     */
    @FXML
    private void exportPath(ActionEvent actionEvent) throws IOException {
        getConfig();
        File selectedFile = creatDirectoryChooser(actionEvent, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Name, configFile_Name, anchorPane_Name);
        }
    }

    /**
     * 选择excel模板按钮
     *
     * @throws IOException io异常
     */
    @FXML
    private void getExcelPath(ActionEvent actionEvent) throws IOException {
        getConfig();
        List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
        extensionFilters.add(new FileChooser.ExtensionFilter("Excel", "*.xlsx", "*.xls"));
        extensionFilters.add(new FileChooser.ExtensionFilter("Excel(2007)", "*.xlsx"));
        extensionFilters.add(new FileChooser.ExtensionFilter("Excel(2003)", "*.xls"));
        File selectedFile = creatFileChooser(actionEvent, excelInPath, extensionFilters, text_selectExcel);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            excelInPath = updatePathLabel(selectedFile.getPath(), excelInPath, key_excelInPath, excelPath_Name, configFile_Name, anchorPane_Name);
            removeExcelButton_Name.setVisible(true);
            String excelType = getFileType(selectedFile);
            excelType_Name.setValue(excelType);
            excelType_Name.setDisable(true);
        }
    }

    /**
     * 重新查询按钮
     *
     * @throws Exception 要查询的文件夹位置为空、要读取的文件夹不存在
     */
    @FXML
    private void reselect() throws Exception {
        String inFilePath = inPath_Name.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception(text_filePathNull);
        }
        if (!new File(inFilePath).exists()) {
            throw new Exception(text_directoryNotExists);
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
     *
     * @throws Exception io异常
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
        excelType_Name.setDisable(false);
    }

}
