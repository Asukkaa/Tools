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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import priv.koishi.tools.Bean.ExcelConfigBean;
import priv.koishi.tools.Bean.FileConfigBean;
import priv.koishi.tools.Bean.FileNumBean;
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

import static priv.koishi.tools.Service.ImgToExcelService.buildImgGroupExcel;
import static priv.koishi.tools.Service.ReadDataService.readExcel;
import static priv.koishi.tools.Service.ReadDataService.showReadExcelData;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.isInIntegerRange;
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
    static String tabId = "_Img";

    /**
     * 默认图片宽度
     */
    static int defaultImgWidth = 6000;

    /**
     * 默认图片高度
     */
    static int defaultImgHeight = 100;

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
    private final ToolsThreadPoolExecutor toolsThreadPoolExecutor = new ToolsThreadPoolExecutor();

    /**
     * 线程池实例
     */
    ExecutorService executorService = toolsThreadPoolExecutor.createNewThreadPool();

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
    private Label inPath_Img, outPath_Img, excelPath_Img, fileNumber_Img, log_Img;

    @FXML
    private Button fileButton_Img, reselectButton_Img, clearButton_Img, exportButton_Img;

    @FXML
    private CheckBox jpg_Img, png_Img, jpeg_Img, recursion_Img, showFileType_Img, openDirectory_Img, openFile_Img, noImg_Img;

    @FXML
    private TextField imgWidth_Img, imgHeight_Img, excelName_Img, sheetOutName_Img, subCode_Img, startRow_Img, startCell_Img, readRow_Img, readCell_Img, maxRow_Img;

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
        fileNum.setPrefWidth(tableWidth - removeAll.getWidth() - exportAll.getWidth() - reselect.getWidth() - noImg.getWidth() - 50);
    }

    /**
     * 读取文件数据
     */
    private void addInFile(File selectedFile, List<String> filterExtensionList) throws Exception {
        FileConfigBean fileConfigBean = new FileConfigBean();
        fileConfigBean.setShowFileType(showFileType_Img.isSelected())
                .setShowHideFile(hideFileType_Img.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setRecursion(recursion_Img.isSelected())
                .setInFile(selectedFile);
        inFileList = readAllFiles(fileConfigBean);
        //列表中有excel分组后再匹配数据
        ObservableList<FileNumBean> fileNumList = tableView_Img.getItems();
        if (CollectionUtils.isNotEmpty(fileNumList)) {
            matchGroupData(fileNumList, inFileList, subCode_Img.getText(), showFileType_Img.isSelected());
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setTableView(tableView_Img)
                    .setTabId(tabId);
            showReadExcelData(fileNumList, taskBean);
        }
    }

    /**
     * 添加数据渲染列表
     */
    private Task<List<FileNumBean>> addInData() {
        removeAll();
        //组装数据
        int readRowValue = setDefaultIntValue(readRow_Img, defaultReadRow, 0, null);
        int readCellValue = setDefaultIntValue(readCell_Img, defaultReadCell, 0, null);
        int maxRowValue = setDefaultIntValue(maxRow_Img, -1, 1, null);
        ExcelConfigBean excelConfigBean = new ExcelConfigBean();
        excelConfigBean.setSheet(sheetOutName_Img.getText())
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
                .setTabId(tabId);
        //获取Task任务
        Task<List<FileNumBean>> readExcelTask = readExcel(excelConfigBean, taskBean);
        //绑定带进度条的线程
        bindingProgressBarTask(readExcelTask, taskBean);
        throwTaskException(readExcelTask);
        readExcelTask.setOnSucceeded(t -> progressBar_Img.setVisible(false));
        executorService.execute(readExcelTask);
        //设置javafx单元格宽度
        return tableViewNumImgAdaption(readExcelTask, groupId_Img, tableView_Img, groupName_Img.prefWidthProperty(), groupNumber_Img.prefWidthProperty(), fileName_Img);
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
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        addToolTip(imgHeight_Img, "只能填数字，不填默认为 " + defaultImgHeight);
        addToolTip(imgWidth_Img, "只能填数字，不填默认为 " + defaultImgWidth);
        addToolTip(noImg_Img, "勾选后没有图片的数据将会在单元格中标记为 无图片");
        addToolTip(startRow_Img, "只能填数字，不填默认与读取预留行相同");
        addToolTip(startCell_Img, "只能填数字，不填默认为 " + defaultStartCell);
        addToolTip(readRow_Img, "只能填数字，不填默认为 " + defaultReadRow + " 从第 " + (defaultReadRow + 1) + " 行读取");
        addToolTip(readCell_Img, "只能填数字，不填默认为 " + defaultReadCell + " ，从第 " + (defaultReadCell + 1) + " 列读取");
        addNumImgToolTip(recursion_Img, subCode_Img, excelName_Img, sheetOutName_Img, maxRow_Img);
    }

    /**
     * 选择文件夹按钮功能
     */
    @FXML
    private void inDirectoryButton(ActionEvent actionEvent) throws Exception {
        getConfig();
        List<String> filterExtensionList = new ArrayList<>();
        if (jpg_Img.isSelected()) {
            filterExtensionList.add(".jpg");
        }
        if (png_Img.isSelected()) {
            filterExtensionList.add(".png");
        }
        if (jpeg_Img.isSelected()) {
            filterExtensionList.add(".jpeg");
        }
        if (CollectionUtils.isEmpty(filterExtensionList)) {
            throw new Exception("未选择需要识别的图片格式");
        }
        // 显示文件选择器
        File selectedFile = creatDirectoryChooser(actionEvent, inFilePath, "选择文件夹");
        if (selectedFile != null) {
            String selectedFilePath = selectedFile.getPath();
            updatePath(configFile, "inFilePath", selectedFilePath);
            inPath_Img.setText(selectedFilePath);
            addToolTip(inPath_Img, selectedFilePath);
            //读取文件数据
            addInFile(selectedFile, filterExtensionList);
        }
    }

    /**
     * 拖拽释放行为
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) {
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
            if (file.isFile() && ".xlsx".equals(getFileType(file))) {
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
    }

    /**
     * 导出excel按钮
     */
    @FXML
    private void exportAll() throws Exception {
        String subCode = subCode_Img.getText();
        String inDirectory = inPath_Img.getText();
        String outFilePath = outPath_Img.getText();
        String inFilePath = excelPath_Img.getText();
        if (StringUtils.isEmpty(outFilePath)) {
            throw new Exception("导出文件夹位置为空，需要先设置导出文件夹位置再继续");
        }
        if (StringUtils.isEmpty(inDirectory)) {
            throw new Exception("选择需要统计的文件夹位置再继续");
        }
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception("excel模板文件位置为空，需要先设置excel模板文件位置再继续");
        }
        if (StringUtils.isEmpty(subCode)) {
            throw new Exception("文件名称分割符位置为空，需要先设置文件名称分割符再继续");
        }
        String sheetName = setDefaultStrValue(sheetOutName_Img, "Sheet1");
        String excelNameValue = setDefaultFileName(excelName_Img, "NameList");
        int readRowValue = setDefaultIntValue(readRow_Img, defaultReadRow, 0, null);
        int imgWidth = setDefaultIntValue(imgWidth_Img, defaultImgWidth, 0, null);
        int imgHeight = setDefaultIntValue(imgHeight_Img, defaultImgHeight, 0, null);
        int startRowValue = setDefaultIntValue(startRow_Img, readRowValue, 0, null);
        int startCellValue = setDefaultIntValue(startCell_Img, defaultStartCell, 0, null);
        ExcelConfigBean excelConfigBean = new ExcelConfigBean();
        excelConfigBean.setOutExcelExtension(excelType_Img.getValue())
                .setInPath(excelPath_Img.getText())
                .setNoImg(noImg_Img.isSelected())
                .setStartCellNum(startCellValue)
                .setStartRowNum(startRowValue)
                .setOutName(excelNameValue)
                .setOutPath(outFilePath)
                .setImgHeight(imgHeight)
                .setImgWidth(imgWidth)
                .setSubCode(subCode)
                .setSheet(sheetName);
        Task<List<FileNumBean>> reselectTask = reselect();
        throwTaskException(reselectTask);
        reselectTask.setOnSucceeded(t -> {
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setShowFileType(showFileType_Img.isSelected())
                    .setBeanList(reselectTask.getValue())
                    .setSubCode(subCode_Img.getText())
                    .setProgressBar(progressBar_Img)
                    .setTableView(tableView_Img)
                    .setMassageLabel(log_Img)
                    .setTabId(tabId);
            //获取Task任务
            Task<SXSSFWorkbook> buildExcelTask = buildImgGroupExcel(taskBean, excelConfigBean);
            //线程成功后保存excel
            saveExcelOnSucceeded(excelConfigBean, taskBean, buildExcelTask, openDirectory_Img, openFile_Img, executorService);
        });
    }

    /**
     * 设置导出文件按钮
     */
    @FXML
    private void exportPath(ActionEvent actionEvent) throws Exception {
        getConfig();
        File selectedFile = creatDirectoryChooser(actionEvent, outFilePath, "选择文件夹");
        if (selectedFile != null) {
            updatePath(configFile, "outFilePath", selectedFile.getPath());
            //显示选择的路径
            outFilePath = selectedFile.getPath();
            outPath_Img.setText(outFilePath);
            addToolTip(outPath_Img, outFilePath);
            String inFilePath = excelPath_Img.getText();
            if (StringUtils.isNotEmpty(inFilePath)) {
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
        File selectedFile = creatFileChooser(actionEvent, excelInPath, extensionFilters, "选择excel模板文件");
        if (selectedFile != null) {
            updatePath(configFile, "excelInPath", selectedFile.getPath());
            //显示选择的路径
            excelInPath = selectedFile.getPath();
            excelPath_Img.setText(excelInPath);
            addToolTip(excelPath_Img, excelInPath);
            addInData();
        }
    }

    /**
     * 限制导出预留行只能输入自然数
     */
    @FXML
    private void rowHandleKeyTyped() {
        if (!isInIntegerRange(startRow_Img.getText(), 0, null)) {
            startRow_Img.setText("");
        }
        aadValueToolTip(startRow_Img, "只能填数字，不填默认与读取预留行相同");
    }

    /**
     * 限制导出预留列只能输入自然数
     */
    @FXML
    private void cellHandleKeyTyped() {
        if (!isInIntegerRange(startCell_Img.getText(), 0, null)) {
            startCell_Img.setText("");
        }
        aadValueToolTip(startCell_Img, "只能填数字，不填默认为 " + defaultStartCell);
    }

    /**
     * 鼠标悬留提示输入的导出excel文件名称
     */
    @FXML
    private void nameHandleKeyTyped() {
        aadValueToolTip(excelName_Img, "如果导出地址和名称与模板一样则会覆盖模板excel文件");
    }

    /**
     * 鼠标悬留提示输入的导出excel表名称
     */
    @FXML
    private void sheetHandleKeyTyped() {
        aadValueToolTip(sheetOutName_Img, "须填与excel模板相同的表名才能正常统计");
    }

    /**
     * 鼠标悬留提示输入的文件名称分割符
     */
    @FXML
    private void subHandleKeyTyped() {
        aadValueToolTip(subCode_Img, "填写后会按所填写的字符串来分割文件名称，按照分割后的文件名称左侧字符串进行分组");
    }

    /**
     * 限制读取起始行只能输入自然数
     */
    @FXML
    private void readRowHandleKeyTyped() {
        if (!isInIntegerRange(readRow_Img.getText(), 0, null)) {
            readRow_Img.setText("");
        }
        aadValueToolTip(readRow_Img, "只能填数字，不填默认为 " + defaultReadRow + " 从第 " + (defaultReadRow + 1) + " 行读取");
    }

    /**
     * 限制读取起始列只能输入自然数
     */
    @FXML
    private void readCellHandleKeyTyped() {
        if (!isInIntegerRange(readCell_Img.getText(), 0, null)) {
            readCell_Img.setText("");
        }
        aadValueToolTip(readCell_Img, "只能填数字，不填默认为 " + defaultReadCell + " ，从第 " + (defaultReadCell + 1) + " 列读取");
    }

    /**
     * 限制读取最大行数只能输入正整数
     */
    @FXML
    private void maxRowHandleKeyTyped() {
        if (!isInIntegerRange(maxRow_Img.getText(), 1, null)) {
            maxRow_Img.setText("");
        }
        aadValueToolTip(maxRow_Img, "只能填数字，不填默认不限制，会读取到有数据的最后一行，最小值为1");
    }

    /**
     * 重新查询按钮
     */
    @FXML
    private Task<List<FileNumBean>> reselect() throws Exception {
        String inFilePath = excelPath_Img.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception("excel模板文件位置为空，需要先设置excel模板文件位置再继续");
        }
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

    @FXML
    private void imgWidthHandleKeyTyped() {
        if (!isInIntegerRange(imgWidth_Img.getText(), 0, null)) {
            imgWidth_Img.setText("");
        }
        aadValueToolTip(imgWidth_Img, "只能填数字，不填默认为 " + defaultImgWidth);
    }

    @FXML
    private void imgHeightHandleKeyTyped() {
        if (!isInIntegerRange(imgHeight_Img.getText(), 0, null)) {
            imgHeight_Img.setText("");
        }
        aadValueToolTip(imgHeight_Img, "只能填数字，不填默认为 " + defaultImgHeight);
    }

}
