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
import priv.koishi.tools.Properties.CommonProperties;
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
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningOutputStream;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;
import static priv.koishi.tools.Utils.UiUtils.nodeRightAlignment;

/**
 * 将图片与excel匹配并插入页面控制器
 *
 * @author KOISHI
 * Date:2024-10-16
 * Time:下午1:24
 */
public class ImgToExcelController extends CommonProperties {

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
    private static final String tabId = "_Img";

    /**
     * 默认图片宽度
     */
    private static int defaultImgWidth;

    /**
     * 默认图片高度
     */
    private static int defaultImgHeight;

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
    private static final List<Control> disableControls = new ArrayList<>();

    /**
     * 线程池
     */
    private final CommonThreadPoolExecutor commonThreadPoolExecutor = new CommonThreadPoolExecutor();

    /**
     * 线程池实例
     */
    private final ExecutorService executorService = commonThreadPoolExecutor.createNewThreadPool();

    /**
     * 构建excel线程
     */
    private Task<SXSSFWorkbook> buildExcelTask;

    /**
     * 保存excel线程
     */
    private Task<String> saveExcelTask;

    /**
     * 读取excel分组信息线程
     */
    private Task<List<FileNumBean>> readExcelTask;

    @FXML
    private AnchorPane anchorPane_Img;

    @FXML
    private VBox vbox_Img;

    @FXML
    private ProgressBar progressBar_Img;

    @FXML
    private ChoiceBox<String> hideFileType_Img;

    @FXML
    private HBox fileNumberHBox_Img, tipHBox_Img;

    @FXML
    private TableView<FileNumBean> tableView_Img;

    @FXML
    private TableColumn<FileNumBean, String> groupId_Img, groupName_Img, groupNumber_Img, fileName_Img, fileUnitSize_Img;

    @FXML
    private Label inPath_Img, outPath_Img, excelPath_Img, fileNumber_Img, log_Img, tip_Img, excelType_Img, excelTypeLabel_Img;

    @FXML
    private Button fileButton_Img, reselectButton_Img, clearButton_Img, exportButton_Img, cancel_Img, outPathButton_Img, excelPathButton_Img;

    @FXML
    private TextField imgWidth_Img, imgHeight_Img, excelName_Img, sheetName_Img, subCode_Img, startRow_Img, startCell_Img, readRow_Img, readCell_Img, maxRow_Img, maxImgNum_Img;

    @FXML
    private CheckBox jpg_Img, png_Img, jpeg_Img, recursion_Img, showFileType_Img, openDirectory_Img, openFile_Img, noImg_Img, exportTitle_Img, exportFileNum_Img, exportFileSize_Img;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void imgToExcelAdaption(Stage stage) {
        Scene scene = stage.getScene();
        // 设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Img");
        table.setPrefHeight(stageHeight * 0.45);
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        table.setMaxWidth(tableWidth);
        Node fileNumVbox = scene.lookup("#vbox_Img");
        fileNumVbox.setLayoutX(stageWidth * 0.03);
        Node groupId = scene.lookup("#groupId_Img");
        groupId.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node groupNameNum = scene.lookup("#groupName_Img");
        groupNameNum.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node groupNumberNum = scene.lookup("#groupNumber_Img");
        groupNumberNum.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node fileNameNum = scene.lookup("#fileName_Img");
        fileNameNum.setStyle("-fx-pref-width: " + tableWidth * 0.6 + "px;");
        Node fileUnitSize = scene.lookup("#fileUnitSize_Img");
        fileUnitSize.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Label fileNum = (Label) scene.lookup("#fileNumber_Img");
        Label tip_Img = (Label) scene.lookup("#tip_Img");
        HBox fileNumberHBox = (HBox) scene.lookup("#fileNumberHBox_Img");
        nodeRightAlignment(fileNumberHBox, tableWidth, fileNum);
        HBox tipHBox = (HBox) scene.lookup("#tipHBox_Img");
        nodeRightAlignment(tipHBox, tableWidth, tip_Img);
    }

    /**
     * 保存最后一次配置的值
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    public static void imgToExcelSaveLastConfig(Scene scene) throws IOException {
        AnchorPane anchorPane = (AnchorPane) scene.lookup("#anchorPane_Img");
        if (anchorPane != null) {
            InputStream input = checkRunningInputStream(configFile_Img);
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
            CheckBox exportFileNum = (CheckBox) scene.lookup("#exportFileNum_Img");
            String exportFileNumValue = exportFileNum.isSelected() ? activation : unActivation;
            prop.put(key_lastExportFileNum, exportFileNumValue);
            CheckBox exportFileSize = (CheckBox) scene.lookup("#exportFileSize_Img");
            String exportFileSizeValue = exportFileSize.isSelected() ? activation : unActivation;
            prop.put(key_lastExportFileSize, exportFileSizeValue);
            CheckBox exportTitle = (CheckBox) scene.lookup("#exportTitle_Img");
            String exportTitleValue = exportTitle.isSelected() ? activation : unActivation;
            prop.put(key_lastExportTitle, exportTitleValue);
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
            OutputStream output = checkRunningOutputStream(configFile_Img);
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
        ObservableList<FileNumBean> fileNumList = tableView_Img.getItems();
        if (CollectionUtils.isNotEmpty(fileNumList)) {
            machGroup(fileConfig, fileNumList, inFileList, tableView_Img, tabId, fileNumber_Img, fileUnitSize_Img);
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
     *
     * @throws Exception 未选择需要识别的图片格式
     */
    private void updateInFileList() throws Exception {
        String selectedFilePath = inPath_Img.getText();
        if (StringUtils.isNotBlank(selectedFilePath)) {
            getInFileList(new File(selectedFilePath), getFilterExtension());
        }
    }

    /**
     * 添加数据渲染列表
     *
     * @return 读取excel任务线程
     * @throws Exception 未选择需要识别的图片格式
     */
    private Task<List<FileNumBean>> addInData() throws Exception {
        if (readExcelTask == null) {
            removeAll();
            // 渲染表格前需要更新一下读取的文件
            updateInFileList();
            String excelPath = excelPath_Img.getText();
            excelType_Img.setText(getFileType(new File(excelPath)));
            // 组装数据
            String maxImgValue = maxImgNum_Img.getText();
            int maxImgNum = 0;
            if (StringUtils.isNotBlank(maxImgValue)) {
                maxImgNum = Integer.parseInt(maxImgValue);
            }
            ExcelConfig excelConfig = new ExcelConfig();
            excelConfig.setReadCellNum(setDefaultIntValue(readCell_Img, defaultReadCell, 0, null))
                    .setReadRowNum(setDefaultIntValue(readRow_Img, defaultReadRow, 0, null))
                    .setMaxRowNum(setDefaultIntValue(maxRow_Img, -1, 1, null))
                    .setSheetName(sheetName_Img.getText())
                    .setInPath(excelPath_Img.getText());
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setShowFileType(showFileType_Img.isSelected())
                    .setComparatorTableColumn(fileUnitSize_Img)
                    .setDisableControls(disableControls)
                    .setSubCode(subCode_Img.getText())
                    .setMassageLabel(fileNumber_Img)
                    .setProgressBar(progressBar_Img)
                    .setTableView(tableView_Img)
                    .setInFileList(inFileList)
                    .setMaxImgNum(maxImgNum)
                    .setTabId(tabId);
            // 获取Task任务
            readExcelTask = readExcel(excelConfig, taskBean);
            // 绑定带进度条的线程
            bindingProgressBarTask(readExcelTask, taskBean);
            readExcelTask.setOnSucceeded(event -> {
                taskUnbind(taskBean);
                readExcelTask = null;
            });
            if (!readExcelTask.isRunning()) {
                executorService.execute(readExcelTask);
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
        InputStream input = checkRunningInputStream(configFile_Img);
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
     *
     * @throws IOException io异常
     */
    private void setLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Img);
        prop.load(input);
        if (activation.equals(prop.getProperty(key_loadLastConfig))) {
            setControlLastConfig(noImg_Img, prop, key_lastNoImg, false, null);
            setControlLastConfig(inPath_Img, prop, key_lastInPath, false, anchorPane_Img);
            setControlLastConfig(maxRow_Img, prop, key_lastMaxRow, false, null);
            setControlLastConfig(subCode_Img, prop, key_lastSubCode, true, null);
            setControlLastConfig(outPath_Img, prop, key_lastOutPath, false, anchorPane_Img);
            setControlLastConfig(readRow_Img, prop, key_lastReadRow, false, null);
            setControlLastConfig(readCell_Img, prop, key_lastReadCell, false, null);
            setControlLastConfig(openFile_Img, prop, key_lastOpenFile, false, null);
            setControlLastConfig(imgWidth_Img, prop, key_lastImgWidth, false, null);
            setControlLastConfig(startRow_Img, prop, key_lastStartRow, false, null);
            setControlLastConfig(excelPath_Img, prop, key_lastExcelPath, false, anchorPane_Img);
            setControlLastConfig(startCell_Img, prop, key_lastStartCell, false, null);
            setControlLastConfig(imgHeight_Img, prop, key_lastImgHeight, false, null);
            setControlLastConfig(maxImgNum_Img, prop, key_lastMaxImgNum, false, null);
            setControlLastConfig(recursion_Img, prop, key_lastRecursion, false, null);
            setControlLastConfig(excelName_Img, prop, key_lastExcelName, false, null);
            setControlLastConfig(sheetName_Img, prop, key_lastSheetName, false, null);
            setControlLastConfig(exportTitle_Img, prop, key_lastExportTitle, false, null);
            setControlLastConfig(hideFileType_Img, prop, key_lastHideFileType, false, null);
            setControlLastConfig(showFileType_Img, prop, key_lastShowFileType, false, null);
            setControlLastConfig(openDirectory_Img, prop, key_lastOpenDirectory, false, null);
            setControlLastConfig(exportFileNum_Img, prop, key_lastExportFileNum, false, null);
            setControlLastConfig(exportFileSize_Img, prop, key_lastExportFileSize, false, null);
            String lastFilterFileTypes = prop.getProperty(key_lastFilterFileType);
            if (StringUtils.isNotBlank(lastFilterFileTypes)) {
                jpg_Img.setSelected(lastFilterFileTypes.contains(jpg));
                png_Img.setSelected(lastFilterFileTypes.contains(png));
                jpeg_Img.setSelected(lastFilterFileTypes.contains(jpeg));
            }
            String excelPath = prop.getProperty(key_lastExcelPath);
            if (StringUtils.isNotBlank(excelPath)) {
                excelType_Img.setText(getFileType(new File(excelPath)));
            }
        }
        input.close();
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_noImg, noImg_Img);
        addToolTip(tip_maxRow, maxRow_Img);
        addToolTip(tip_subCode, subCode_Img);
        addToolTip(tip_Img.getText(), tip_Img);
        addToolTip(tip_openFile, openFile_Img);
        addToolTip(tip_recursion, recursion_Img);
        addToolTip(tip_sheetName, sheetName_Img);
        addToolTip(tip_maxImgNum, maxImgNum_Img);
        addToolTip(tip_startReadRow, startRow_Img);
        addToolTip(tip_fileButton, fileButton_Img);
        addToolTip(tip_learButton, clearButton_Img);
        addToolTip(tip_exportTitle, exportTitle_Img);
        addToolTip(tip_showFileType, showFileType_Img);
        addToolTip(tip_hideFileType, hideFileType_Img);
        addToolTip(tip_exportButton, exportButton_Img);
        addToolTip(tip_outPathButton, outPathButton_Img);
        addToolTip(tip_openDirectory, openDirectory_Img);
        addToolTip(tip_exportFileNum, exportFileNum_Img);
        addToolTip(tip_exportFileSize, exportFileSize_Img);
        addToolTip(tip_reselectButton, reselectButton_Img);
        addToolTip(tip_excelPathButton, excelPathButton_Img);
        addToolTip(tip_filterImgType, jpg_Img, png_Img, jpeg_Img);
        addToolTip(tip_excelType, excelType_Img, excelTypeLabel_Img);
        addToolTip(tip_excelName + defaultOutFileName, excelName_Img);
        addToolTip(text_onlyNaturalNumber + defaultStartCell, startCell_Img);
        addToolTip(tip_imgHeightWidth + defaultImgWidth + tip_imgWidth, imgWidth_Img);
        addToolTip(tip_imgHeightWidth + defaultImgHeight + tip_imgHeight, imgHeight_Img);
        addToolTip(text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row, readRow_Img);
        addToolTip(text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell, readCell_Img);
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableControls() {
        disableControls.add(fileButton_Img);
        disableControls.add(clearButton_Img);
        disableControls.add(exportButton_Img);
        disableControls.add(showFileType_Img);
        disableControls.add(outPathButton_Img);
        disableControls.add(reselectButton_Img);
        disableControls.add(excelPathButton_Img);
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 限制读取最大行数只能输入正整数
        integerRangeTextField(maxRow_Img, 1, null, tip_maxRow);
        // 最大匹配数量设置监听
        integerRangeTextField(maxImgNum_Img, 1, null, tip_maxImgNum);
        // 限制导出预留行只能输入自然数
        integerRangeTextField(startRow_Img, 0, null, tip_startReadRow);
        // 限制导出预留列只能输入自然数
        integerRangeTextField(startCell_Img, 0, null, text_onlyNaturalNumber + defaultStartCell);
        // 图片宽度设置监听
        integerRangeTextField(imgWidth_Img, 0, null, text_onlyNaturalNumber + defaultImgWidth + tip_imgWidth);
        // 图片高度设置监听
        integerRangeTextField(imgHeight_Img, 0, null, text_onlyNaturalNumber + defaultImgHeight + tip_imgHeight);
        // 限制读取起始列只能输入自然数
        integerRangeTextField(readCell_Img, 0, null, text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell);
        // 限制读取起始行只能输入自然数
        integerRangeTextField(readRow_Img, 0, null, text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row);
        // 鼠标悬留提示输入的文件名称分割符
        textFieldValueListener(subCode_Img, tip_subCode);
        // 鼠标悬留提示输入的导出excel表名称
        textFieldValueListener(sheetName_Img, tip_sheetName);
        // 鼠标悬留提示输入的导出excel文件名称
        textFieldValueListener(excelName_Img, tip_excelName + defaultOutFileName);
    }

    /**
     * 校验匹配文件总大小是否能够正常导出
     *
     * @return 文件大小可以正常导出或无法正常导出时选择继续导出返回true，终止任务返回false
     */
    private boolean checkFileSize() {
        String fileNumberText = fileNumber_Img.getText();
        String totalFileSize = fileNumberText.substring(fileNumberText.lastIndexOf(text_totalFileSize) + text_totalFileSize.length());
        double totalFileSizeValue = fileSizeCompareValue(totalFileSize) * 2;
        Scene scene = anchorPane_Img.getScene();
        Label appMemory = (Label) scene.lookup("#runningMemory_Set");
        String appMemoryText = appMemory.getText();
        double appMemoryValue = fileSizeCompareValue(appMemoryText);
        if (totalFileSizeValue >= appMemoryValue) {
            ButtonType result = creatConfirmDialog("要导出的文件需要占用内存过多，当前内存设置可能无法导出，是否继续导出？", "继续导出", "取消导出");
            return !result.getButtonData().isCancelButton();
        }
        String maxExcelSize = "4 GB";
        double maxExcelSizeValue = fileSizeCompareValue(maxExcelSize);
        if (totalFileSizeValue >= maxExcelSizeValue) {
            ButtonType result = creatConfirmDialog("当前匹配的文件导出后 excel 总大小可能超过 4 GB ，可能无法正常打开，是否继续导出？", "继续导出", "取消导出");
            return !result.getButtonData().isCancelButton();
        }
        return true;
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
        // 设置要防重复点击的组件
        setDisableControls();
        // 设置鼠标悬停提示
        setToolTip();
        // 设置javafx单元格宽度
        tableViewNumImgAdaption(groupId_Img, tableView_Img, groupName_Img.prefWidthProperty(), groupNumber_Img.prefWidthProperty(), fileName_Img, fileUnitSize_Img);
        // 给输入框添加内容变化监听
        textFieldChangeListener();
        // 设置初始配置值为上次配置值
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
            // 更新所选文件路径显示
            inFilePath = updatePathLabel(selectedFile.getPath(), inFilePath, key_inFilePath, inPath_Img, configFile_Img, anchorPane_Img);
            // 读取文件数据
            addInFile(selectedFile, getFilterExtension());
        }
    }

    /**
     * 获取要识别的图片格式
     *
     * @return 需要识别的图片格式
     * @throws Exception 未选择需要识别的图片格式
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
     *
     * @param dragEvent 拖拽释放事件
     * @throws Exception 未选择需要识别的图片格式
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
     *
     * @param dragEvent 拖拽中事件
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
        inFileList = null;
        removeTableViewData(tableView_Img, fileNumber_Img, log_Img);
    }

    /**
     * 导出excel按钮
     *
     * @throws Exception 导出文件夹位置为空、要查询的文件夹位置为空、excel模板文件位置为空
     */
    @FXML
    private void exportAll() throws Exception {
        if (buildExcelTask == null && saveExcelTask == null) {
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
                    .setSheetName(setDefaultStrValue(sheetName_Img, defaultSheetName))
                    .setExportFileSize(exportFileSize_Img.isSelected())
                    .setExportFileNum(exportFileNum_Img.isSelected())
                    .setOutExcelType(excelType_Img.getText())
                    .setExportTitle(exportTitle_Img.isSelected())
                    .setInPath(excelPath_Img.getText())
                    .setNoImg(noImg_Img.isSelected())
                    .setOutPath(outFilePath);
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setDisableControls(disableControls)
                    .setProgressBar(progressBar_Img)
                    .setMassageLabel(log_Img);
            // 重新查询任务
            readExcelTask = reselect();
            readExcelTask.setOnSucceeded(event -> {
                taskBean.setShowFileType(showFileType_Img.isSelected())
                        .setBeanList(readExcelTask.getValue())
                        .setCancelButton(cancel_Img)
                        .setTableView(tableView_Img)
                        .setTabId(tabId);
                taskUnbind(taskBean);
                readExcelTask = null;
                // 校验匹配文件总大小是否能够正常导出
                if (checkFileSize()) {
                    // 组装excel任务
                    buildExcelTask = buildImgGroupExcel(taskBean, excelConfig);
                    bindingProgressBarTask(buildExcelTask, taskBean);
                    buildExcelTask.setOnSucceeded(e -> {
                        // 保存excel任务
                        saveExcelTask = saveExcelTask(excelConfig, buildExcelTask.getValue());
                        bindingProgressBarTask(saveExcelTask, taskBean);
                        saveExcelTask.setOnSucceeded(s -> {
                            String excelPath = saveExcelTask.getValue();
                            try {
                                if (openDirectory_Img.isSelected()) {
                                    openDirectory(excelPath);
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
                            taskBean.getMassageLabel().setText(text_saveSuccess + excelPath);
                            taskBean.getMassageLabel().setTextFill(Color.GREEN);
                        });
                        saveExcelTask.setOnFailed(s -> {
                            taskNotSuccess(taskBean, text_taskFailed);
                            // 获取抛出的异常
                            Throwable ex = saveExcelTask.getException();
                            saveExcelTask = null;
                            buildExcelTask = null;
                            inFileList = null;
                            throw new RuntimeException(ex);
                        });
                        if (!saveExcelTask.isRunning()) {
                            executorService.execute(saveExcelTask);
                        }
                    });
                    buildExcelTask.setOnFailed(s -> {
                        taskNotSuccess(taskBean, text_taskFailed);
                        // 获取抛出的异常
                        Throwable ex = buildExcelTask.getException();
                        saveExcelTask = null;
                        buildExcelTask = null;
                        inFileList = null;
                        throw new RuntimeException(ex);
                    });
                    if (!buildExcelTask.isRunning()) {
                        executorService.execute(buildExcelTask);
                    }
                }
            });
            readExcelTask.setOnFailed(event -> {
                taskNotSuccess(taskBean, text_taskFailed);
                // 获取抛出的异常
                Throwable ex = readExcelTask.getException();
                readExcelTask = null;
                inFileList = null;
                throw new RuntimeException(ex);
            });
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
        File selectedFile = creatDirectoryChooser(actionEvent, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Img, configFile_Img, anchorPane_Img);
            if (StringUtils.isNotEmpty(excelPath_Img.getText())) {
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
        List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(new FileChooser.ExtensionFilter("Excel", "*.xlsx")));
        File selectedFile = creatFileChooser(actionEvent, excelInPath, extensionFilters, text_selectExcel);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            excelInPath = updatePathLabel(selectedFile.getPath(), excelInPath, key_excelInPath, excelPath_Img, configFile_Img, anchorPane_Img);
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
        String inFilePath = excelPath_Img.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception(text_excelPathNull);
        }
        if (!new File(inFilePath).exists()) {
            throw new Exception(text_directoryNotExists);
        }
        updateLabel(log_Img, "");
        return addInData();
    }

    /**
     * 是否展示文件拓展名选项监听
     *
     * @throws Exception excel模板文件位置为空、要读取的文件夹不存在
     */
    @FXML
    private void handleCheckBoxAction() throws Exception {
        ObservableList<FileNumBean> fileBeans = tableView_Img.getItems();
        if (CollectionUtils.isNotEmpty(fileBeans)) {
            reselect();
        }
    }

    /**
     * 取消导出按钮
     *
     * @throws IOException io异常
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
        taskNotSuccess(taskBean, text_taskCancelled);
    }

}
