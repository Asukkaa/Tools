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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;
import priv.koishi.tools.Configuration.FileConfig;
import priv.koishi.tools.Service.ImgToExcelService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static priv.koishi.tools.Controller.MainController.settingController;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.MainApplication.mainScene;
import static priv.koishi.tools.MainApplication.mainStage;
import static priv.koishi.tools.Service.ImgToExcelService.buildImgGroupExcel;
import static priv.koishi.tools.Service.ReadDataService.readExcel;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;
import static priv.koishi.tools.Utils.UiUtils.textFieldValueListener;

/**
 * 将图片与excel匹配并插入页面控制器
 *
 * @author KOISHI
 * Date:2024-10-16
 * Time:下午1:24
 */
public class ImgToExcelController extends RootController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LogManager.getLogger(ImgToExcelController.class);

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
    private static final List<Node> disableNodes = new ArrayList<>();

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
    public AnchorPane anchorPane_Img;

    @FXML
    public ProgressBar progressBar_Img;

    @FXML
    public HBox fileNumberHBox_Img, tipHBox_Img, linkHBox_Img, linkRightHBox_Img;

    @FXML
    public ChoiceBox<String> hideFileType_Img, insertImgType_Img, linkNameType_Img;

    @FXML
    public TableView<FileNumBean> tableView_Img;

    @FXML
    public TableColumn<FileNumBean, Integer> groupId_Img, groupNumber_Img, index_Img;

    @FXML
    public TableColumn<FileNumBean, String> groupName_Img, fileName_Img, fileUnitSize_Img;

    @FXML
    public Label inPath_Img, outPath_Img, excelPath_Img, fileNumber_Img, log_Img, tip_Img, excelType_Img,
            excelTypeLabel_Img, linkName_Img;

    @FXML
    public Button fileButton_Img, reselectButton_Img, clearButton_Img, exportButton_Img, cancel_Img,
            outPathButton_Img, excelPathButton_Img;

    @FXML
    public TextField imgWidth_Img, imgHeight_Img, excelName_Img, sheetName_Img, subCode_Img, startRow_Img,
            startCell_Img, readRow_Img, readCell_Img, maxRow_Img, maxImgNum_Img, linkLeftName_Img, linkRightName_Img;

    @FXML
    public CheckBox jpg_Img, png_Img, jpeg_Img, recursion_Img, showFileType_Img, openDirectory_Img, openFile_Img,
            noImg_Img, exportTitle_Img, exportFileNum_Img, exportFileSize_Img;

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tableView_Img.setPrefHeight(stageHeight * 0.4);
        // 设置组件宽度
        double stageWidth = mainStage.getWidth();
        double tableWidth = stageWidth * 0.94;
        tableView_Img.setMaxWidth(tableWidth);
        tableView_Img.setPrefWidth(tableWidth);
        regionRightAlignment(fileNumberHBox_Img, tableWidth, fileNumber_Img);
        regionRightAlignment(tipHBox_Img, tableWidth, tip_Img);
        bindPrefWidthProperty();
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_Img.prefWidthProperty().bind(tableView_Img.widthProperty().multiply(0.1));
        groupId_Img.prefWidthProperty().bind(tableView_Img.widthProperty().multiply(0.1));
        groupName_Img.prefWidthProperty().bind(tableView_Img.widthProperty().multiply(0.1));
        groupNumber_Img.prefWidthProperty().bind(tableView_Img.widthProperty().multiply(0.1));
        fileName_Img.prefWidthProperty().bind(tableView_Img.widthProperty().multiply(0.5));
        fileUnitSize_Img.prefWidthProperty().bind(tableView_Img.widthProperty().multiply(0.1));
    }

    /**
     * 保存最后一次配置的值
     *
     * @throws IOException io异常
     */
    public void saveLastConfig() throws IOException {
        if (anchorPane_Img != null) {
            InputStream input = checkRunningInputStream(configFile_Img);
            Properties prop = new Properties();
            prop.load(input);
            prop.put(key_lastHideFileType, hideFileType_Img.getValue());
            String recursionValue = recursion_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastRecursion, recursionValue);
            String showFileTypeValue = showFileType_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastShowFileType, showFileTypeValue);
            String openDirectoryValue = openDirectory_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, openDirectoryValue);
            String openFileValue = openFile_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenFile, openFileValue);
            prop.put(key_lastExcelName, excelName_Img.getText());
            prop.put(key_lastSheetName, sheetName_Img.getText());
            prop.put(key_lastSubCode, subCode_Img.getText());
            prop.put(key_lastStartRow, startRow_Img.getText());
            prop.put(key_lastStartCell, startCell_Img.getText());
            prop.put(key_lastReadRow, readRow_Img.getText());
            prop.put(key_lastReadCell, readCell_Img.getText());
            prop.put(key_lastMaxRow, maxRow_Img.getText());
            prop.put(key_lastImgWidth, imgWidth_Img.getText());
            prop.put(key_lastImgHeight, imgHeight_Img.getText());
            prop.put(key_lastInPath, inPath_Img.getText());
            prop.put(key_lastOutPath, outPath_Img.getText());
            prop.put(key_lastExcelPath, excelPath_Img.getText());
            prop.put(key_lastMaxImgNum, maxImgNum_Img.getText());
            String noImgValue = noImg_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastNoImg, noImgValue);
            String exportFileNumValue = exportFileNum_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastExportFileNum, exportFileNumValue);
            String exportFileSizeValue = exportFileSize_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastExportFileSize, exportFileSizeValue);
            String exportTitleValue = exportTitle_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastExportTitle, exportTitleValue);
            prop.put(key_insertImgType, insertImgType_Img.getValue());
            prop.put(key_linkNameType, linkNameType_Img.getValue());
            prop.put(key_linkLeftName, linkLeftName_Img.getText());
            prop.put(key_linkRightName, linkRightName_Img.getText());
            List<String> lastFilterFileTypes = new ArrayList<>();
            if (jpg_Img.isSelected()) {
                lastFilterFileTypes.add(jpg);
            }
            if (png_Img.isSelected()) {
                lastFilterFileTypes.add(png);
            }
            if (jpeg_Img.isSelected()) {
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
            setControlLastConfig(noImg_Img, prop, key_lastNoImg);
            setControlLastConfig(maxRow_Img, prop, key_lastMaxRow);
            setControlLastConfig(readRow_Img, prop, key_lastReadRow);
            setControlLastConfig(readCell_Img, prop, key_lastReadCell);
            setControlLastConfig(openFile_Img, prop, key_lastOpenFile);
            setControlLastConfig(imgWidth_Img, prop, key_lastImgWidth);
            setControlLastConfig(startRow_Img, prop, key_lastStartRow);
            setControlLastConfig(startCell_Img, prop, key_lastStartCell);
            setControlLastConfig(imgHeight_Img, prop, key_lastImgHeight);
            setControlLastConfig(maxImgNum_Img, prop, key_lastMaxImgNum);
            setControlLastConfig(recursion_Img, prop, key_lastRecursion);
            setControlLastConfig(excelName_Img, prop, key_lastExcelName);
            setControlLastConfig(sheetName_Img, prop, key_lastSheetName);
            setControlLastConfig(exportTitle_Img, prop, key_lastExportTitle);
            setControlLastConfig(hideFileType_Img, prop, key_lastHideFileType);
            setControlLastConfig(showFileType_Img, prop, key_lastShowFileType);
            setControlLastConfig(openDirectory_Img, prop, key_lastOpenDirectory);
            setControlLastConfig(exportFileNum_Img, prop, key_lastExportFileNum);
            setControlLastConfig(inPath_Img, prop, key_lastInPath);
            setControlLastConfig(exportFileSize_Img, prop, key_lastExportFileSize);
            setControlLastConfig(subCode_Img, prop, key_lastSubCode, true);
            setControlLastConfig(outPath_Img, prop, key_lastOutPath);
            setControlLastConfig(excelPath_Img, prop, key_lastExcelPath);
            setControlLastConfig(insertImgType_Img, prop, key_insertImgType);
            setControlLastConfig(linkNameType_Img, prop, key_linkNameType);
            setControlLastConfig(linkLeftName_Img, prop, key_linkLeftName);
            setControlLastConfig(linkRightName_Img, prop, key_linkRightName);
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
     * @throws IOException 文件不存在
     */
    private FileConfig getInFileList(File selectedFile, List<String> filterExtensionList) throws IOException {
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
                    .setSubCode(subCode_Img.getText())
                    .setMassageLabel(fileNumber_Img)
                    .setProgressBar(progressBar_Img)
                    .setDisableNodes(disableNodes)
                    .setTableView(tableView_Img)
                    .setInFileList(inFileList)
                    .setMaxImgNum(maxImgNum)
                    .setTabId(tabId);
            // 获取Task任务
            readExcelTask = readExcel(excelConfig, taskBean);
            // 绑定带进度条的线程
            bindingTaskNode(readExcelTask, taskBean);
            readExcelTask.setOnSucceeded(event -> {
                taskUnbind(taskBean);
                readExcelTask = null;
            });
            readExcelTask.setOnFailed(event -> {
                taskUnbind(taskBean);
                taskNotSuccess(taskBean, text_taskFailed);
                // 获取抛出的异常
                Throwable ex = readExcelTask.getException();
                readExcelTask = null;
                throw new RuntimeException(ex);
            });
            if (!readExcelTask.isRunning()) {
                Thread.ofVirtual()
                        .name("readExcelTask-vThread" + tabId)
                        .start(readExcelTask);
            }
        }
        return readExcelTask;
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
        addToolTip(tip_linkNameType, linkNameType_Img);
        addToolTip(tip_showFileType, showFileType_Img);
        addToolTip(tip_hideFileType, hideFileType_Img);
        addToolTip(tip_exportButton, exportButton_Img);
        addToolTip(tip_outPathButton, outPathButton_Img);
        addToolTip(tip_openDirectory, openDirectory_Img);
        addToolTip(tip_exportFileNum, exportFileNum_Img);
        addToolTip(tip_insertImgType, insertImgType_Img);
        addToolTip(tip_exportFileSize, exportFileSize_Img);
        addToolTip(tip_reselectButton, reselectButton_Img);
        addToolTip(tip_excelPathButton, excelPathButton_Img);
        addToolTip(tip_filterImgType, jpg_Img, png_Img, jpeg_Img);
        addToolTip(tip_excelType, excelType_Img, excelTypeLabel_Img);
        addToolTip(tip_linkName, linkLeftName_Img, linkRightName_Img);
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
    private void setDisableNodes() {
        disableNodes.add(fileButton_Img);
        disableNodes.add(clearButton_Img);
        disableNodes.add(exportButton_Img);
        disableNodes.add(showFileType_Img);
        disableNodes.add(outPathButton_Img);
        disableNodes.add(reselectButton_Img);
        disableNodes.add(excelPathButton_Img);
        Node autoClickTab = mainScene.lookup("#autoClickTab");
        disableNodes.add(autoClickTab);
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
        // 鼠标悬留提示输入的超链接左侧名称
        textFieldValueListener(linkLeftName_Img, tip_linkName);
        // 鼠标悬留提示输入的超链接右侧名称
        textFieldValueListener(linkRightName_Img, tip_linkName);
    }

    /**
     * 设置文本输入框提示
     */
    private void setPromptText() {
        excelName_Img.setPromptText(defaultOutFileName);
        readRow_Img.setPromptText(String.valueOf(defaultReadRow));
        readCell_Img.setPromptText(String.valueOf(defaultReadCell));
        imgWidth_Img.setPromptText(String.valueOf(defaultImgWidth));
        imgHeight_Img.setPromptText(String.valueOf(defaultImgHeight));
        startCell_Img.setPromptText(String.valueOf(defaultStartCell));
    }

    /**
     * 校验匹配文件总大小是否能够正常导出
     *
     * @return 文件大小可以正常导出或无法正常导出时选择继续导出返回true，终止任务返回false
     */
    private boolean checkFileSize() {
        if (!insertType_img.equals(insertImgType_Img.getValue())) {
            return true;
        }
        String fileNumberText = fileNumber_Img.getText();
        String totalFileSize = fileNumberText.substring(fileNumberText.lastIndexOf(text_totalFileSize) + text_totalFileSize.length());
        double totalFileSizeValue = fileSizeCompareValue(totalFileSize) * 2;
        Label appMemory = settingController.runningMemory_Set;
        String appMemoryText = appMemory.getText();
        double appMemoryValue = fileSizeCompareValue(appMemoryText);
        if (totalFileSizeValue >= appMemoryValue) {
            ButtonType result = creatConfirmDialog(
                    "占用内存过多",
                    "要导出的文件需要占用内存过多，当前内存设置可能无法导出，是否继续导出？",
                    "继续导出",
                    "取消导出");
            return !result.getButtonData().isCancelButton();
        }
        String maxExcelSize = "4 GB";
        double maxExcelSizeValue = fileSizeCompareValue(maxExcelSize);
        if (totalFileSizeValue >= maxExcelSizeValue) {
            ButtonType result = creatConfirmDialog(
                    "占用内存过多",
                    "当前匹配的文件导出后 excel 总大小可能超过 4 GB ，可能无法正常打开，是否继续导出？",
                    "继续导出",
                    "取消导出");
            return !result.getButtonData().isCancelButton();
        }
        return true;
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

    private void resetTasks() {
        saveExcelTask = null;
        buildExcelTask = null;
        inFileList = null;
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
            autoBuildTableViewData(tableView_Img, FileNumBean.class, tabId, index_Img);
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
            inFilePath = updatePathLabel(selectedFile.getPath(), inFilePath, key_inFilePath, inPath_Img, configFile_Img);
            // 读取文件数据
            addInFile(selectedFile, getFilterExtension());
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
        excelPath_Img.setText(file.getPath());
        try {
            addInData();
        } catch (Exception e) {
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
                    .setInsertImgType(insertImgType_Img.getValue())
                    .setLinkRightName(linkRightName_Img.getText())
                    .setLinkNameType(linkNameType_Img.getValue())
                    .setExportTitle(exportTitle_Img.isSelected())
                    .setLinkLeftName(linkLeftName_Img.getText())
                    .setOutExcelType(excelType_Img.getText())
                    .setInPath(excelPath_Img.getText())
                    .setNoImg(noImg_Img.isSelected())
                    .setOutPath(outFilePath);
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setDisableNodes(disableNodes)
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
                    bindingTaskNode(buildExcelTask, taskBean);
                    buildExcelTask.setOnSucceeded(e -> {
                        // 保存excel任务
                        saveExcelTask = saveExcelTask(excelConfig, buildExcelTask.getValue());
                        bindingTaskNode(saveExcelTask, taskBean);
                        saveExcelTask.setOnSucceeded(s -> {
                            String excelPath = saveExcelTask.getValue();
                            try {
                                if (openDirectory_Img.isSelected()) {
                                    openDirectory(excelPath);
                                }
                                if (openFile_Img.isSelected()) {
                                    openFile(excelPath);
                                }
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            } finally {
                                try {
                                    ImgToExcelService.closeStream();
                                } catch (IOException ex) {
                                    logger.error("关闭流时发生异常", ex);
                                }
                                resetTasks();
                                taskUnbind(taskBean);
                            }
                            taskBean.getMassageLabel().setText(text_saveSuccess + excelPath);
                            taskBean.getMassageLabel().setTextFill(Color.GREEN);
                        });
                        saveExcelTask.setOnFailed(s -> {
                            taskNotSuccess(taskBean, text_taskFailed);
                            // 获取抛出的异常
                            Throwable ex = saveExcelTask.getException();
                            resetTasks();
                            throw new RuntimeException(ex);
                        });
                        if (!saveExcelTask.isRunning()) {
                            Thread.ofVirtual()
                                    .name("saveExcelTask-vThread" + tabId)
                                    .start(saveExcelTask);
                        }
                    });
                    buildExcelTask.setOnFailed(s -> {
                        taskNotSuccess(taskBean, text_taskFailed);
                        // 获取抛出的异常
                        Throwable ex = buildExcelTask.getException();
                        resetTasks();
                        throw new RuntimeException(ex);
                    });
                    if (!buildExcelTask.isRunning()) {
                        Thread.ofVirtual()
                                .name("buildExcelTask-vThread" + tabId)
                                .start(buildExcelTask);
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
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatDirectoryChooser(window, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Img, configFile_Img);
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
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatFileChooser(window, excelInPath, extensionFilters, text_selectExcel);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            excelInPath = updatePathLabel(selectedFile.getPath(), excelInPath, key_excelInPath, excelPath_Img, configFile_Img);
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
        taskBean.setDisableNodes(disableNodes)
                .setProgressBar(progressBar_Img)
                .setCancelButton(cancel_Img)
                .setMassageLabel(log_Img);
        taskNotSuccess(taskBean, text_taskCancelled);
    }

    /**
     * 插入图片类型选项监听
     */
    @FXML
    private void insertImgTypAction() {
        linkHBox_Img.setVisible(!insertType_img.equals(insertImgType_Img.getValue()));
    }

    /**
     * 超链接名称类型选项监听
     */
    @FXML
    private void linkNameType() {
        String linkNameType = linkNameType_Img.getValue();
        if (linkName_unified.equals(linkNameType)) {
            linkName_Img.setText("超链接名称:");
            linkRightHBox_Img.setVisible(false);
        } else if (linkName_splice.equals(linkNameType)) {
            linkName_Img.setText("超链接左侧名称:");
            linkRightHBox_Img.setVisible(true);
        }
    }

}
