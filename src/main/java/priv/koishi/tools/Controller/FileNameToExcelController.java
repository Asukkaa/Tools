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
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import priv.koishi.tools.Bean.ExcelConfigBean;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.FileConfigBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Properties.ToolsProperties;
import priv.koishi.tools.ThreadPool.ToolsThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static priv.koishi.tools.Service.FileNameToExcelService.buildFileNameExcel;
import static priv.koishi.tools.Service.ReadDataService.readFile;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.isInIntegerRange;
import static priv.koishi.tools.Utils.FileUtils.readAllFiles;
import static priv.koishi.tools.Utils.FileUtils.updatePath;
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
     * 导出文件名称
     */
    static String outFileName;

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
     *
     * 配置文件路径
     */
    static String configFile = "config/fileNameToExcelConfig.properties";

    /**
     * 线程池
     */
    private final ToolsThreadPoolExecutor toolsThreadPoolExecutor = new ToolsThreadPoolExecutor();

    /**
     * 线程池实例
     */
    ExecutorService executorService = toolsThreadPoolExecutor.createNewThreadPool();

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
    private Label outPath_Name, excelPath_Name, fileNumber_Name, inPath_Name, log_Name;

    @FXML
    private ChoiceBox<String> excelType_Name, hideFileType_Name, directoryNameType_Name;

    @FXML
    private CheckBox recursion_Name, openDirectory_Name, openFile_Name, showFileType_Name;

    @FXML
    private TextField excelName_Name, sheetOutName_Name, startRow_Name, startCell_Name, filterFileType_Name;

    @FXML
    private Button fileButton_Name, clearButton_Name, exportButton_Name, reselectButton_Name, removeExcelButton_Name;

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
        fileNum.setPrefWidth(tableWidth - removeAll.getWidth() - exportAll.getWidth() - reselect.getWidth() - 40);
    }

    /**
     * 添加数据渲染列表
     */
    private void addInData(List<File> inFileList) throws Exception {
        removeAll();
        if (inFileList.isEmpty()) {
            throw new Exception("未查询到符合条件的数据，需修改查询条件后再继续");
        }
        TaskBean<FileBean> taskBean = new TaskBean<>();
        taskBean.setShowFileType(showFileType_Name.isSelected())
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
        readFileTask.setOnSucceeded(t -> taskUnbind(taskBean));
        executorService.execute(readFileTask);
        //设置javafx单元格宽度
        id_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.04));
        name_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.14));
        fileType_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.06));
        path_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.36));
        size_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.08));
        creatDate_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.16));
        updateDate_Name.prefWidthProperty().bind(tableView_Name.widthProperty().multiply(0.16));
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
        inFilePath = prop.getProperty("inFilePath");
        outFilePath = prop.getProperty("outFilePath");
        outFileName = prop.getProperty("outFileName");
        excelInPath = prop.getProperty("excelInPath");
        defaultStartCell = Integer.parseInt(prop.getProperty("defaultStartCell"));
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() throws IOException {
        getConfig();
        addToolTip(filterFileType_Name, "填写后只会识别所填写的后缀名文件，多个文件后缀名用空格隔开，后缀名需带 '.'");
        addToolTip(recursion_Name, "勾选后将会查询文件夹中的文件夹里的文件");
        addToolTip(excelName_Name, "如果导出地址和名称与模板一样则会覆盖模板excel文件");
        addToolTip(sheetOutName_Name, "须填与excel模板相同的表名才能正常读取模板，若填表名不存在或不需要读取模板则会创建一个所填表");
        addToolTip(startRow_Name, "只能填数字，不填默认为0，不预留行");
        addToolTip(startCell_Name, "只能填数字，不填默认为 " + defaultStartCell);
        addToolTip(removeExcelButton_Name, "删除excel模板路径");
    }

    /**
     * 选择文件夹按钮功能
     */
    @FXML
    private void inDirectoryButton(ActionEvent actionEvent) throws Exception {
        getConfig();
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Name);
        // 显示文件选择器
        File selectedFile = creatDirectoryChooser(actionEvent, inFilePath, "选择文件夹");
        FileConfigBean fileConfigBean = new FileConfigBean();
        fileConfigBean.setShowDirectoryName(directoryNameType_Name.getValue())
                .setShowHideFile(hideFileType_Name.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setRecursion(recursion_Name.isSelected())
                .setInFile(selectedFile);
        if (selectedFile != null) {
            String selectedFilePath = selectedFile.getPath();
            updatePath(configFile, "inFilePath", selectedFilePath);
            removeAll();
            inPath_Name.setText(selectedFilePath);
            addToolTip(inPath_Name, selectedFilePath);
            //读取数据
            List<File> inFileList = readAllFiles(fileConfigBean);
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
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Name);
        File file = files.getFirst();
        FileConfigBean fileConfigBean = new FileConfigBean();
        fileConfigBean.setShowDirectoryName(directoryNameType_Name.getValue())
                .setShowHideFile(hideFileType_Name.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setRecursion(recursion_Name.isSelected())
                .setInFile(file);
        List<File> inFileList = readAllFiles(fileConfigBean);
        String filePath = file.getPath();
        inPath_Name.setText(filePath);
        addToolTip(inPath_Name, filePath);
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
        List<FileBean> nullFileBeans = new ArrayList<>();
        ObservableList<FileBean> nullData = FXCollections.observableArrayList(nullFileBeans);
        tableView_Name.setItems(nullData);
        // 解除绑定，设置文本，然后重新绑定
        fileNumber_Name.textProperty().unbind();
        fileNumber_Name.setText("列表为空");
    }

    /**
     * 导出excel按钮
     */
    @FXML
    private void exportAll() throws Exception {
        String outFilePath = outPath_Name.getText();
        ObservableList<FileBean> fileBeans = tableView_Name.getItems();
        if (StringUtils.isEmpty(outFilePath)) {
            throw new Exception("导出文件夹位置为空，需要先设置导出文件夹位置再继续");
        }
        if (CollectionUtils.isEmpty(fileBeans)) {
            throw new Exception("要读取的文件列表为空，需要选择一个有文件的文件夹");
        }
        int startRowValue = setDefaultIntValue(startRow_Name, 0, 0, null);
        int startCellValue = setDefaultIntValue(startCell_Name, defaultStartCell, 0, null);
        String excelNameValue = setDefaultFileName(excelName_Name, "NameList");
        String sheetName = setDefaultStrValue(sheetOutName_Name, "Sheet1");
        log_Name.setTextFill(Color.BLACK);
        log_Name.setText("");
        ExcelConfigBean excelConfigBean = new ExcelConfigBean();
        excelConfigBean.setOutExcelExtension(excelType_Name.getValue())
                .setInPath(excelPath_Name.getText())
                .setStartCellNum(startCellValue)
                .setStartRowNum(startRowValue)
                .setOutName(excelNameValue)
                .setOutPath(outFilePath)
                .setSheet(sheetName);
        TaskBean<FileBean> taskBean = new TaskBean<>();
        taskBean.setShowFileType(showFileType_Name.isSelected())
                .setProgressBar(progressBar_Name)
                .setTableView(tableView_Name)
                .setMassageLabel(log_Name)
                .setBeanList(fileBeans)
                .setTabId(tabId);
        //获取Task任务
        Task<SXSSFWorkbook> buildExcelTask = buildFileNameExcel(excelConfigBean, taskBean);
        //绑定带进度条的线程
        bindingProgressBarTask(buildExcelTask, taskBean);
        executorService.execute(buildExcelTask);
        //线程成功后保存excel
        saveExcelOnSucceeded(excelConfigBean, taskBean, buildExcelTask, openDirectory_Name, openFile_Name, executorService);
    }

    /**
     * 设置导出文件按钮
     */
    @FXML
    private void exportPath(ActionEvent actionEvent) throws IOException {
        getConfig();
        File selectedFile = creatDirectoryChooser(actionEvent, outFilePath, "选择文件夹");
        if (selectedFile != null) {
            updatePath(configFile, "outFilePath", selectedFile.getPath());
            //显示选择的路径
            outFilePath = selectedFile.getPath();
            outPath_Name.setText(outFilePath);
            addToolTip(outPath_Name, outFilePath);
        }
    }

    /**
     * 选择excel模板按钮
     */
    @FXML
    private void getExcelPath(ActionEvent actionEvent) throws IOException {
        getConfig();
        List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(new FileChooser.ExtensionFilter("Excel", "*.xlsx")));
        File selectedFile = creatFileChooser(actionEvent, excelInPath, extensionFilters, "选择excel模板文件");
        if (selectedFile != null) {
            updatePath(configFile, "excelInPath", selectedFile.getPath());
            //显示选择的路径
            excelInPath = selectedFile.getPath();
            excelPath_Name.setText(excelInPath);
            addToolTip(excelPath_Name, excelInPath);
            removeExcelButton_Name.setVisible(true);
        }
    }

    /**
     * 限制导出预留行只能输入自然数
     */
    @FXML
    private void rowHandleKeyTyped() {
        if (!isInIntegerRange(startRow_Name.getText(), 0, null)) {
            startRow_Name.setText("");
        }
        aadValueToolTip(startRow_Name, "只能填数字，不填默认为0，不预留行");
    }

    /**
     * 限制导出预留列只能输入自然数
     */
    @FXML
    private void cellHandleKeyTyped() {
        if (!isInIntegerRange(startCell_Name.getText(), 0, null)) {
            startCell_Name.setText("");
        }
        aadValueToolTip(startCell_Name, "只能填数字，不填默认为0，不预留列");
    }

    /**
     * 鼠标悬留提示输入的导出excel文件名称
     */
    @FXML
    private void nameHandleKeyTyped() {
        aadValueToolTip(excelName_Name, "如果导出地址和名称与模板一样则会覆盖模板excel文件");
    }

    /**
     * 鼠标悬留提示输入的导出excel表名称
     */
    @FXML
    private void sheetHandleKeyTyped() {
        aadValueToolTip(sheetOutName_Name, "须填与excel模板相同的表名才能正常读取模板，若填表名不存在或不需要读取模板则会创建一个所填表");
    }

    /**
     * 鼠标悬留提示输入的需要识别的文件后缀名
     */
    @FXML
    private void filterHandleKeyTyped() {
        aadValueToolTip(filterFileType_Name, "填写后只会识别所填写的后缀名文件，多个文件后缀名用空格隔开，后缀名需带 '.'");
    }

    /**
     * 重新查询按钮
     */
    @FXML
    private void reselect() throws Exception {
        String inFilePath = inPath_Name.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception("要查询的文件夹位置为空，需要先设置要查询的文件夹位置再继续");
        }
        FileConfigBean fileConfigBean = new FileConfigBean();
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Name);
        fileConfigBean.setShowDirectoryName(directoryNameType_Name.getValue())
                .setShowHideFile(hideFileType_Name.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setRecursion(recursion_Name.isSelected())
                .setInFile(new File(inFilePath));
        List<File> inFileList = readAllFiles(fileConfigBean);
        addInData(inFileList);
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
