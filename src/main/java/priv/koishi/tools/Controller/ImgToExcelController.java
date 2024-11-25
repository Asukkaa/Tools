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
import javafx.scene.paint.Color;
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
import priv.koishi.tools.Service.ImgToExcelService;
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

import static priv.koishi.tools.Service.ImgToExcelService.buildImgGroupExcel;
import static priv.koishi.tools.Service.ReadDataService.readExcel;
import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningOutputStream;
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
    static int defaultImgWidth = 12;

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
     * 要防重复点击的组件
     */
    static List<Control> disableControls = new ArrayList<>();

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

    /**
     * 构建excel线程
     */
    private Task<SXSSFWorkbook> buildExcelTask;

    /**
     * 保存excel线程
     */
    private Task<String> saveExcelTask;

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
    private Label inPath_Img, outPath_Img, excelPath_Img, fileNumber_Img, log_Img, maxImg_Img, tip_Img;

    @FXML
    private CheckBox jpg_Img, png_Img, jpeg_Img, recursion_Img, showFileType_Img, openDirectory_Img, openFile_Img, noImg_Img;

    @FXML
    private Button fileButton_Img, reselectButton_Img, clearButton_Img, exportButton_Img, cancel_Img, outButton_Img, excelPathButton_Img;

    @FXML
    private TextField imgWidth_Img, imgHeight_Img, excelName_Img, sheetName_Img, subCode_Img, startRow_Img, startCell_Img, readRow_Img, readCell_Img, maxRow_Img, maxImgNum_Img;

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
        Label tip_Img = (Label) scene.lookup("#tip_Img");
        ProgressBar progressBar = (ProgressBar) scene.lookup("#progressBar_Img");
        Button cancel = (Button) scene.lookup("#cancel_Img");
        fileNum.setPrefWidth(tableWidth - removeAll.getWidth() - exportAll.getWidth() - reselect.getWidth() - noImg.getWidth() - maxImg.getWidth() - maxImgNum.getWidth() - 70);
        tip_Img.setPrefWidth(tableWidth - progressBar.getWidth() - cancel.getWidth() - 20);
    }

    /**
     * 保存最后一次配置的值
     */
    public static void imgToExcelSaveLastConfig(Scene scene) throws IOException {
        InputStream input = checkRunningInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        ChoiceBox<?> hideFileType = (ChoiceBox<?>) scene.lookup("#hideFileType_Img");
        prop.put(key_lastHideFileType, hideFileType.getValue());
        CheckBox recursion = (CheckBox) scene.lookup("#recursion_Img");
        String recursionValue = recursion.isSelected() ? activation : unActivation;
        prop.put(key_lastRecursion, recursionValue);
        CheckBox showFileType = (CheckBox) scene.lookup("#showFileType_Img");
        String showFileTypeValue = showFileType.isSelected() ? activation : unActivation;
        prop.put(key_lastShowFileType, showFileTypeValue);
        CheckBox openDirectory = (CheckBox) scene.lookup("#openDirectory_Img");
        String openDirectoryValue = openDirectory.isSelected() ? activation : unActivation;
        prop.put(key_lastOpenDirectory, openDirectoryValue);
        CheckBox openFile = (CheckBox) scene.lookup("#openFile_Img");
        String openFileValue = openFile.isSelected() ? activation : unActivation;
        prop.put(key_lastOpenFile, openFileValue);
        TextField excelName = (TextField) scene.lookup("#excelName_Img");
        prop.put(key_lastExcelName, excelName.getText());
        TextField sheetName = (TextField) scene.lookup("#sheetName_Img");
        prop.put(key_lastSheetName, sheetName.getText());
        TextField subCode = (TextField) scene.lookup("#subCode_Img");
        prop.put(key_lastSubCode, subCode.getText());
        ChoiceBox<?> excelType = (ChoiceBox<?>) scene.lookup("#excelType_Img");
        prop.put(key_lastExcelType, excelType.getValue());
        TextField startRow = (TextField) scene.lookup("#startRow_Img");
        prop.put(key_lastStartRow, startRow.getText());
        TextField startCell = (TextField) scene.lookup("#startCell_Img");
        prop.put(key_lastStartCell, startCell.getText());
        TextField readRow = (TextField) scene.lookup("#readRow_Img");
        prop.put(key_lastReadRow, readRow.getText());
        TextField readCell = (TextField) scene.lookup("#readCell_Img");
        prop.put(key_lastReadCell, readCell.getText());
        TextField maxRow = (TextField) scene.lookup("#maxRow_Img");
        prop.put(key_lastMaxRow, maxRow.getText());
        TextField imgWidth = (TextField) scene.lookup("#imgWidth_Img");
        prop.put(key_lastImgWidth, imgWidth.getText());
        TextField imgHeight = (TextField) scene.lookup("#imgHeight_Img");
        prop.put(key_lastImgHeight, imgHeight.getText());
        Label inPath = (Label) scene.lookup("#inPath_Img");
        prop.put(key_lastInPath, inPath.getText());
        Label outPath = (Label) scene.lookup("#outPath_Img");
        prop.put(key_lastOutPath, outPath.getText());
        Label excelPath = (Label) scene.lookup("#excelPath_Img");
        prop.put(key_lastExcelPath, excelPath.getText());
        TextField maxImgNum = (TextField) scene.lookup("#maxImgNum_Img");
        prop.put(key_lastMaxImgNum, maxImgNum.getText());
        CheckBox noImg = (CheckBox) scene.lookup("#noImg_Img");
        String noImgValue = noImg.isSelected() ? activation : unActivation;
        prop.put(key_lastNoImg, noImgValue);
        List<String> lastFilterFileTypes = new ArrayList<>();
        CheckBox jpgCheckBox = (CheckBox) scene.lookup("#jpg_Img");
        if (jpgCheckBox.isSelected()) {
            lastFilterFileTypes.add(jpg);
        }
        CheckBox pngCheckBox = (CheckBox) scene.lookup("#png_Img");
        if (pngCheckBox.isSelected()) {
            lastFilterFileTypes.add(png);
        }
        CheckBox jpegCheckBox = (CheckBox) scene.lookup("#jpeg_Img");
        if (jpegCheckBox.isSelected()) {
            lastFilterFileTypes.add(jpeg);
        }
        prop.put(key_lastFilterFileType, String.join(" ", lastFilterFileTypes));
        OutputStream output = checkRunningOutputStream(configFile);
        prop.store(output, null);
        input.close();
        output.close();
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
        ExcelConfig excelConfig = new ExcelConfig();
        excelConfig.setReadCellNum(setDefaultIntValue(readCell_Img, defaultReadCell, 0, null))
                .setReadRowNum(setDefaultIntValue(readRow_Img, defaultReadRow, 0, null))
                .setMaxRowNum(setDefaultIntValue(maxRow_Img, -1, 1, null))
                .setSheet(sheetName_Img.getText())
                .setInPath(excelPath_Img.getText());
        TaskBean<FileNumBean> taskBean = new TaskBean<>();
        taskBean.setShowFileType(showFileType_Img.isSelected())
                .setDisableControls(disableControls)
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
        prop.load(input);
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
     * 设置初始配置值为上次配置值
     */
    private void setLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile);
        prop.load(input);
        if (activation.equals(prop.getProperty(key_loadLastConfig))) {
            setControlLastConfig(hideFileType_Img, prop, key_lastHideFileType, false);
            setControlLastConfig(recursion_Img, prop, key_lastRecursion, false);
            setControlLastConfig(showFileType_Img, prop, key_lastShowFileType, false);
            setControlLastConfig(openDirectory_Img, prop, key_lastOpenDirectory, false);
            setControlLastConfig(openFile_Img, prop, key_lastOpenFile, false);
            setControlLastConfig(excelName_Img, prop, key_lastExcelName, false);
            setControlLastConfig(sheetName_Img, prop, key_lastSheetName, false);
            setControlLastConfig(subCode_Img, prop, key_lastSubCode, true);
            setControlLastConfig(excelType_Img, prop, key_lastExcelType, false);
            setControlLastConfig(startRow_Img, prop, key_lastStartRow, false);
            setControlLastConfig(startCell_Img, prop, key_lastStartCell, false);
            setControlLastConfig(readRow_Img, prop, key_lastReadRow, false);
            setControlLastConfig(readCell_Img, prop, key_lastReadCell, false);
            setControlLastConfig(maxRow_Img, prop, key_lastMaxRow, false);
            setControlLastConfig(imgWidth_Img, prop, key_lastImgWidth, false);
            setControlLastConfig(imgHeight_Img, prop, key_lastImgHeight, false);
            setControlLastConfig(inPath_Img, prop, key_lastInPath, false);
            setControlLastConfig(outPath_Img, prop, key_lastOutPath, false);
            setControlLastConfig(excelPath_Img, prop, key_lastExcelPath, false);
            setControlLastConfig(maxImgNum_Img, prop, key_lastMaxImgNum, false);
            setControlLastConfig(noImg_Img, prop, key_lastNoImg, false);
            String lastFilterFileTypes = prop.getProperty(key_lastFilterFileType);
            if (StringUtils.isNotBlank(lastFilterFileTypes)) {
                jpg_Img.setSelected(lastFilterFileTypes.contains(jpg));
                png_Img.setSelected(lastFilterFileTypes.contains(png));
                jpeg_Img.setSelected(lastFilterFileTypes.contains(jpeg));
            }
        }
        input.close();
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(noImg_Img, tip_noImg);
        addToolTip(tip_Img, tip_Img.getText());
        addToolTip(jpg_Img, tip_filterImgType);
        addToolTip(png_Img, tip_filterImgType);
        addToolTip(jpeg_Img, tip_filterImgType);
        addToolTip(maxImgNum_Img, tip_maxImgNum);
        addToolTip(startRow_Img, tip_startReadRow);
        addToolTip(startCell_Img, text_onlyNaturalNumber + defaultStartCell);
        addToolTip(imgWidth_Img, tip_imgHeightWidth + defaultImgWidth + tip_imgWidth);
        addToolTip(imgHeight_Img, tip_imgHeightWidth + defaultImgHeight + tip_imgHeight);
        addNumImgToolTip(recursion_Img, subCode_Img, excelName_Img, sheetName_Img, maxRow_Img);
        addToolTip(readRow_Img, text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row);
        addToolTip(readCell_Img, text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell);
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableControls() {
        disableControls.add(outButton_Img);
        disableControls.add(fileButton_Img);
        disableControls.add(clearButton_Img);
        disableControls.add(exportButton_Img);
        disableControls.add(showFileType_Img);
        disableControls.add(reselectButton_Img);
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
        inFileList = null;
        System.gc();
    }

    /**
     * 导出excel按钮
     */
    @FXML
    private void exportAll() throws Exception {
        updateLabel(log_Img, "");
        String outFilePath = outPath_Img.getText();
        if (StringUtils.isEmpty(outFilePath)) {
            throw new Exception(text_outPathNull);
        }
        if (StringUtils.isEmpty(inPath_Img.getText())) {
            throw new Exception(text_filePathNull);
        }
        if (StringUtils.isEmpty(excelPath_Img.getText())) {
            throw new Exception(text_excelPathNull);
        }
        int readRowValue = setDefaultIntValue(readRow_Img, defaultReadRow, 0, null);
        ExcelConfig excelConfig = new ExcelConfig();
        excelConfig.setStartCellNum(setDefaultIntValue(startCell_Img, defaultStartCell, 0, null))
                .setImgHeight(setDefaultIntValue(imgHeight_Img, defaultImgHeight, 0, null))
                .setStartRowNum(setDefaultIntValue(startRow_Img, readRowValue, 0, null))
                .setImgWidth(setDefaultIntValue(imgWidth_Img, defaultImgWidth, 0, null))
                .setOutName(setDefaultFileName(excelName_Img, defaultOutFileName))
                .setSheet(setDefaultStrValue(sheetName_Img, defaultSheetName))
                .setOutExcelExtension(excelType_Img.getValue())
                .setInPath(excelPath_Img.getText())
                .setNoImg(noImg_Img.isSelected())
                .setOutPath(outFilePath);
        Task<List<FileNumBean>> reselectTask = reselect();
        reselectTask.setOnSucceeded(event -> {
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setShowFileType(showFileType_Img.isSelected())
                    .setBeanList(reselectTask.getValue())
                    .setDisableControls(disableControls)
                    .setProgressBar(progressBar_Img)
                    .setCancelButton(cancel_Img)
                    .setTableView(tableView_Img)
                    .setMassageLabel(log_Img)
                    .setTabId(tabId);
            //获取Task任务
            buildExcelTask = buildImgGroupExcel(taskBean, excelConfig);
            bindingProgressBarTask(buildExcelTask, taskBean);
            buildExcelTask.setOnSucceeded(e -> {
                saveExcelTask = saveExceltask(excelConfig, buildExcelTask.getValue());
                bindingProgressBarTask(saveExcelTask, taskBean);
                saveExcelTask.setOnSucceeded(s -> {
                    String excelPath = saveExcelTask.getValue();
                    try {
                        if (openDirectory_Img.isSelected()) {
                            openFile(new File(excelPath).getParent());
                        }
                        if (openFile_Img.isSelected()) {
                            openFile(excelPath);
                        }
                        ImgToExcelService.closeStream();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        saveExcelTask = null;
                        buildExcelTask = null;
                        inFileList = null;
                        taskUnbind(taskBean);
                    }
                    taskBean.getMassageLabel().setText("所有数据已保存到： " + excelPath);
                    taskBean.getMassageLabel().setTextFill(Color.GREEN);
                });
                executorService.execute(saveExcelTask);
            });
            executorService.execute(buildExcelTask);
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
        addValueToolTip(sheetName_Img, tip_sheetName);
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
        if (!new File(inFilePath).exists()) {
            throw new Exception(text_fileNotExists);
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
        addValueToolTip(imgWidth_Img, text_onlyNaturalNumber + defaultImgWidth + tip_imgWidth);
    }

    /**
     * 图片高度设置监听
     */
    @FXML
    private void imgHeightHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(imgHeight_Img, 0, null, event);
        addValueToolTip(imgHeight_Img, text_onlyNaturalNumber + defaultImgHeight + tip_imgHeight);
    }

    /**
     * 最大匹配数量设置监听
     */
    @FXML
    private void maxImgNumKeyTyped(KeyEvent event) {
        integerRangeTextField(maxImgNum_Img, 1, null, event);
        addValueToolTip(maxImgNum_Img, tip_maxImgNum);
    }

    /**
     * 取消导出按钮
     */
    @FXML
    private void cancelTask() throws IOException {
        if (buildExcelTask != null && buildExcelTask.isRunning()) {
            buildExcelTask.cancel();
        }
        buildExcelTask = null;
        if (saveExcelTask != null && saveExcelTask.isRunning()) {
            saveExcelTask.cancel();
        }
        saveExcelTask = null;
        inFileList = null;
        ImgToExcelService.closeStream();
        TaskBean<FileNumBean> taskBean = new TaskBean<>();
        taskBean.setDisableControls(disableControls)
                .setProgressBar(progressBar_Img)
                .setCancelButton(cancel_Img)
                .setMassageLabel(log_Img);
        taskUnbind(taskBean);
        log_Img.setText("任务已取消");
        log_Img.setTextFill(Color.RED);
    }

}
