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

import static priv.koishi.tools.Service.ImgToExcelService.buildImgGroupExcel;
import static priv.koishi.tools.Service.ReadDataService.readExcel;
import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * @author KOISHI
 * Date:2024-10-16
 * Time:下午1:24
 */
public class ImgToExcelController extends ToolsProperties {

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
    static String tabId = "_Img";

    /**
     * 默认图片宽度
     */
    static int defaultImgWidth = 3000;

    /**
     * 默认图片高度
     */
    static int defaultImgHeight = 50;

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
    static String configFile = "config/imgToExcelConfig.properties";

    /**
     * 线程池
     */
    private final CommonThreadPoolExecutor commonThreadPoolExecutor = new CommonThreadPoolExecutor();

    /**
     * 线程池实例
     */
    ExecutorService executorService = commonThreadPoolExecutor.createNewThreadPool();

    @FXML
    private VBox vbox_Img;

    @FXML
    private ProgressBar progressBar_Img;

    @FXML
    private TableView<FileNumBean> tableView_Img;

    @FXML
    private TableColumn<FileNumBean, String> groupId_Img, groupName_Img, groupNumber_Img, fileName_Img;

    @FXML
    private ChoiceBox<String> hideFileType_Img, excelType_Img;

    @FXML
    private Label inPath_Img, outPath_Img, excelPath_Img, fileNumber_Img, log_Img, maxImg_Img;

    @FXML
    private Button fileButton_Img, reselectButton_Img, clearButton_Img, exportButton_Img;

    @FXML
    private CheckBox jpg_Img, png_Img, jpeg_Img, recursion_Img, showFileType_Img, openDirectory_Img, openFile_Img, noImg_Img;

    @FXML
    private TextField imgWidth_Img, imgHeight_Img, excelName_Img, sheetOutName_Img, subCode_Img, startRow_Img, startCell_Img, readRow_Img, readCell_Img, maxRow_Img, maxImgNum_Img;

    /**
     * 组件自适应宽高
     */
    public static void imgToExcelAdaption(Stage stage, Scene scene) {
        //设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Img");
        table.setPrefHeight(stageHeight * 0.5);
        //设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        Node fileNumVbox = scene.lookup("#vbox_Img");
        fileNumVbox.setLayoutX(stageWidth * 0.03);
        Node tableView = scene.lookup("#tableView_Img");
        tableView.setStyle("-fx-pref-width: " + tableWidth + "px;");
        Node groupId = scene.lookup("#groupId_Img");
        groupId.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node groupNameNum = scene.lookup("#groupName_Img");
        groupNameNum.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node groupNumberNum = scene.lookup("#groupNumber_Img");
        groupNumberNum.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node fileNameNum = scene.lookup("#fileName_Img");
        fileNameNum.setStyle("-fx-pref-width: " + tableWidth * 0.7 + "px;");
        Label fileNum = (Label) scene.lookup("#fileNumber_Img");
        Button removeAll = (Button) scene.lookup("#clearButton_Img");
        Button exportAll = (Button) scene.lookup("#exportButton_Img");
        Button reselect = (Button) scene.lookup("#reselectButton_Img");
        CheckBox noImg = (CheckBox) scene.lookup("#noImg_Img");
        Label maxImg = (Label) scene.lookup("#maxImg_Img");
        TextField maxImgNum = (TextField) scene.lookup("#maxImgNum_Img");
        fileNum.setPrefWidth(tableWidth - removeAll.getWidth() - exportAll.getWidth() - reselect.getWidth() - noImg.getWidth() - maxImg.getWidth() - maxImgNum.getWidth() - 70);
    }

    /**
     * 读取文件数据
     */
    private void addInFile(File selectedFile, List<String> filterExtensionList) throws Exception {
        FileConfig fileConfig = getInFileList(selectedFile, filterExtensionList);
        //列表中有excel分组后再匹配数据
        ObservableList<FileNumBean> fileNumList = tableView_Img.getItems();
        if (CollectionUtils.isNotEmpty(fileNumList)) {
            machGroup(fileConfig, fileNumList, inFileList, tableView_Img, tabId, fileNumber_Img);
        }
    }

    /**
     * 查询要处理的文件
     */
    private FileConfig getInFileList(File selectedFile, List<String> filterExtensionList) {
        FileConfig fileConfig = new FileConfig();
        String maxImgValue = maxImgNum_Img.getText();
        int maxImgNum = 0;
        if (StringUtils.isNotBlank(maxImgValue)) {
            maxImgNum = Integer.parseInt(maxImgValue);
        }
        fileConfig.setShowFileType(showFileType_Img.isSelected())
                .setShowHideFile(hideFileType_Img.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setRecursion(recursion_Img.isSelected())
                .setSubCode(subCode_Img.getText())
                .setMaxImgNum(maxImgNum)
                .setInFile(selectedFile);
        inFileList = readAllFiles(fileConfig);
        return fileConfig;
    }

    /**
     * 更新要处理的文件
     */
    private void updateInFileList() throws Exception {
        String selectedFilePath = inPath_Img.getText();
        if (StringUtils.isNotBlank(selectedFilePath)) {
            getInFileList(new File(selectedFilePath), getFilterExtension());
        }
    }

    /**
     * 添加数据渲染列表
     */
    private Task<List<FileNumBean>> addInData() throws Exception {
        removeAll();
        //渲染表格前需要更新一下读取的文件
        updateInFileList();
        //组装数据
        String maxImgValue = maxImgNum_Img.getText();
        int maxImgNum = 0;
        if (StringUtils.isNotBlank(maxImgValue)) {
            maxImgNum = Integer.parseInt(maxImgValue);
        }
        int readRowValue = setDefaultIntValue(readRow_Img, defaultReadRow, 0, null);
        int readCellValue = setDefaultIntValue(readCell_Img, defaultReadCell, 0, null);
        int maxRowValue = setDefaultIntValue(maxRow_Img, -1, 1, null);
        ExcelConfig excelConfig = new ExcelConfig();
        excelConfig.setSheet(sheetOutName_Img.getText())
                .setInPath(excelPath_Img.getText())
                .setReadCellNum(readCellValue)
                .setReadRowNum(readRowValue)
                .setMaxRowNum(maxRowValue);
        TaskBean<FileNumBean> taskBean = new TaskBean<>();
        taskBean.setShowFileType(showFileType_Img.isSelected())
                .setSubCode(subCode_Img.getText())
                .setMassageLabel(fileNumber_Img)
                .setProgressBar(progressBar_Img)
                .setTableView(tableView_Img)
                .setInFileList(inFileList)
                .setMaxImgNum(maxImgNum)
                .setTabId(tabId);
        //获取Task任务
        Task<List<FileNumBean>> readExcelTask = readExcel(excelConfig, taskBean);
        //绑定带进度条的线程
        bindingProgressBarTask(readExcelTask, taskBean);
        readExcelTask.setOnSucceeded(event -> taskUnbind(taskBean));
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
        excelInPath = prop.getProperty(key_excelInPath);
        outFilePath = prop.getProperty(key_outFilePath);
        defaultSheetName = prop.getProperty(key_defaultSheetName);
        defaultOutFileName = prop.getProperty(key_defaultOutFileName);
        defaultReadRow = Integer.parseInt(prop.getProperty(key_defaultReadRow));
        defaultReadCell = Integer.parseInt(prop.getProperty(key_defaultReadCell));
        defaultImgWidth = Integer.parseInt(prop.getProperty(key_defaultImgWidth));
        defaultImgHeight = Integer.parseInt(prop.getProperty(key_defaultImgHeight));
        defaultStartCell = Integer.parseInt(prop.getProperty(key_defaultStartCell));
        input.close();
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() throws IOException {
        getConfig();
        addToolTip(noImg_Img, tip_noImg);
        addToolTip(maxImgNum_Img, tip_maxImgNum);
        addToolTip(startRow_Img, tip_startReadRow);
        addToolTip(imgWidth_Img, tip_imgHeight + defaultImgWidth);
        addToolTip(imgHeight_Img, tip_imgHeight + defaultImgHeight);
        addToolTip(startCell_Img, text_onlyNaturalNumber + defaultStartCell);
        addNumImgToolTip(recursion_Img, subCode_Img, excelName_Img, sheetOutName_Img, maxRow_Img);
        addToolTip(readRow_Img, text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row);
        addToolTip(readCell_Img, text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell);
        //设置javafx单元格宽度
        tableViewNumImgAdaption(groupId_Img, tableView_Img, groupName_Img.prefWidthProperty(), groupNumber_Img.prefWidthProperty(), fileName_Img);
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
            inFilePath = updatePathLabel(selectedFile.getPath(), inFilePath, key_inFilePath, inPath_Img, configFile);
            //读取文件数据
            addInFile(selectedFile, getFilterExtension());
        }
    }

    /**
     * 获取要识别的图片格式
     */
    private List<String> getFilterExtension() throws Exception {
        List<String> filterExtensionList = new ArrayList<>();
        if (jpg_Img.isSelected()) {
            filterExtensionList.add(jpg);
        }
        if (png_Img.isSelected()) {
            filterExtensionList.add(png);
        }
        if (jpeg_Img.isSelected()) {
            filterExtensionList.add(jpeg);
        }
        if (CollectionUtils.isEmpty(filterExtensionList)) {
            throw new Exception("未选择需要识别的图片格式");
        }
        return filterExtensionList;
    }

    /**
     * 拖拽释放行为
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) throws Exception {
        List<File> files = dragEvent.getDragboard().getFiles();
        File file = files.getFirst();
        excelPath_Img.setText(file.getPath());
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
        removeNumImgAll(tableView_Img, fileNumber_Img, log_Img);
        System.gc();
    }

    /**
     * 导出excel按钮
     */
    @FXML
    private void exportAll() throws Exception {
        updateLabel(log_Img, "");
        String inDirectory = inPath_Img.getText();
        String outFilePath = outPath_Img.getText();
        String inFilePath = excelPath_Img.getText();
        if (StringUtils.isEmpty(outFilePath)) {
            throw new Exception(text_outPathNull);
        }
        if (StringUtils.isEmpty(inDirectory)) {
            throw new Exception(text_filePathNull);
        }
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception(text_excelPathNull);
        }
        String sheetName = setDefaultStrValue(sheetOutName_Img, defaultSheetName);
        String excelNameValue = setDefaultFileName(excelName_Img, defaultOutFileName);
        int readRowValue = setDefaultIntValue(readRow_Img, defaultReadRow, 0, null);
        int imgWidth = setDefaultIntValue(imgWidth_Img, defaultImgWidth, 0, null);
        int imgHeight = setDefaultIntValue(imgHeight_Img, defaultImgHeight, 0, null);
        int startRowValue = setDefaultIntValue(startRow_Img, readRowValue, 0, null);
        int startCellValue = setDefaultIntValue(startCell_Img, defaultStartCell, 0, null);
        ExcelConfig excelConfig = new ExcelConfig();
        excelConfig.setOutExcelExtension(excelType_Img.getValue())
                .setInPath(excelPath_Img.getText())
                .setNoImg(noImg_Img.isSelected())
                .setStartCellNum(startCellValue)
                .setStartRowNum(startRowValue)
                .setOutName(excelNameValue)
                .setOutPath(outFilePath)
                .setImgHeight(imgHeight)
                .setImgWidth(imgWidth)
                .setSheet(sheetName);
        Task<List<FileNumBean>> reselectTask = reselect();
        reselectTask.setOnSucceeded(event -> {
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setShowFileType(showFileType_Img.isSelected())
                    .setReselectButton(reselectButton_Img)
                    .setBeanList(reselectTask.getValue())
                    .setProgressBar(progressBar_Img)
                    .setTableView(tableView_Img)
                    .setMassageLabel(log_Img)
                    .setTabId(tabId);
            //获取Task任务
            Task<SXSSFWorkbook> buildExcelTask = buildImgGroupExcel(taskBean, excelConfig);
            //线程成功后保存excel
            saveExcelOnSucceeded(excelConfig, taskBean, buildExcelTask, openDirectory_Img, openFile_Img, executorService);
        });
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
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Img, configFile);
            if (StringUtils.isNotEmpty(excelPath_Img.getText())) {
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
            excelInPath = updatePathLabel(selectedFile.getPath(), excelInPath, key_excelInPath, excelPath_Img, configFile);
            addInData();
        }
    }

    /**
     * 限制导出预留行只能输入自然数
     */
    @FXML
    private void rowHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(startRow_Img, 0, null, event);
        addValueToolTip(startRow_Img, tip_startReadRow);
    }

    /**
     * 限制导出预留列只能输入自然数
     */
    @FXML
    private void cellHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(startCell_Img, 0, null, event);
        addValueToolTip(startCell_Img, text_onlyNaturalNumber + defaultStartCell);
    }

    /**
     * 鼠标悬留提示输入的导出excel文件名称
     */
    @FXML
    private void nameHandleKeyTyped() {
        addValueToolTip(excelName_Img, tip_excelName);
    }

    /**
     * 鼠标悬留提示输入的导出excel表名称
     */
    @FXML
    private void sheetHandleKeyTyped() {
        addValueToolTip(sheetOutName_Img, tip_sheetOutName);
    }

    /**
     * 鼠标悬留提示输入的文件名称分割符
     */
    @FXML
    private void subHandleKeyTyped() {
        addValueToolTip(subCode_Img, tip_subCode);
    }

    /**
     * 限制读取起始行只能输入自然数
     */
    @FXML
    private void readRowHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(readRow_Img, 0, null, event);
        addValueToolTip(readRow_Img, text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row);
    }

    /**
     * 限制读取起始列只能输入自然数
     */
    @FXML
    private void readCellHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(readCell_Img, 0, null, event);
        addValueToolTip(readCell_Img, text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell);
    }

    /**
     * 限制读取最大行数只能输入正整数
     */
    @FXML
    private void maxRowHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(maxRow_Img, 1, null, event);
        addValueToolTip(maxRow_Img, tip_maxRow);
    }

    /**
     * 重新查询按钮
     */
    @FXML
    private Task<List<FileNumBean>> reselect() throws Exception {
        String inFilePath = excelPath_Img.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception(text_excelPathNull);
        }
        updateLabel(log_Img, "");
        return addInData();
    }

    /**
     * 是否展示文件拓展名选项监听
     */
    @FXML
    private void handleCheckBoxAction() throws Exception {
        ObservableList<FileNumBean> fileBeans = tableView_Img.getItems();
        if (CollectionUtils.isNotEmpty(fileBeans)) {
            reselect();
        }
    }

    /**
     * 图片宽度设置监听
     */
    @FXML
    private void imgWidthHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(imgWidth_Img, 0, null, event);
        addValueToolTip(imgWidth_Img, text_onlyNaturalNumber + defaultImgWidth);
    }

    /**
     * 图片高度设置监听
     */
    @FXML
    private void imgHeightHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(imgHeight_Img, 0, null, event);
        addValueToolTip(imgHeight_Img, text_onlyNaturalNumber + defaultImgHeight);
    }

    /**
     * 最大匹配数量设置监听
     */
    @FXML
    public void maxImgNumKeyTyped(KeyEvent event) {
        integerRangeTextField(maxImgNum_Img, 1, null, event);
        addValueToolTip(maxImgNum_Img, tip_maxImgNum);
    }

}
