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
import org.apache.poi.ss.usermodel.Workbook;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Callback.ReadGroupFileCallback;
import priv.koishi.tools.Configuration.ExcelConfig;
import priv.koishi.tools.Configuration.FileConfig;
import priv.koishi.tools.Service.ImgToExcelService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static priv.koishi.tools.Controller.MainController.settingController;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.MainApplication.mainScene;
import static priv.koishi.tools.MainApplication.mainStage;
import static priv.koishi.tools.Service.FileNumToExcelService.buildNameGroupNumExcel;
import static priv.koishi.tools.Service.ImgToExcelService.buildImgGroupExcel;
import static priv.koishi.tools.Service.ReadDataService.*;
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
    private final Logger logger = LogManager.getLogger(ImgToExcelController.class);

    /**
     * 要处理的文件夹路径
     */
    private String inFilePath;

    /**
     * 要处理的文件夹文件
     */
    private List<File> inFileList;

    /**
     * 导出文件路径
     */
    private String outFilePath;

    /**
     * 默认导出文件名称
     */
    private String defaultOutFileName;

    /**
     * excel模板路径
     */
    private String excelInPath;

    /**
     * 页面标识符
     */
    private final String tabId = "_Img";

    /**
     * 默认图片宽度
     */
    private int defaultImgWidth;

    /**
     * 默认图片高度
     */
    private int defaultImgHeight;

    /**
     * 默认起始输出列
     */
    private int defaultStartCell;

    /**
     * 默认起始读取行
     */
    private int defaultReadRow;

    /**
     * 默认起始读取列
     */
    private int defaultReadCell;

    /**
     * 要防重复点击的组件
     */
    private final List<Node> disableNodes = new ArrayList<>();

    /**
     * 构建excel线程
     */
    private Task<Workbook> buildExcelTask;

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
    public HBox fileNumberHBox_Img, tipHBox_Img, linkHBox_Img, linkRightHBox_Img, imgHBox_Img, fileHBox_Img,
            fileTypeHBox_Img;

    @FXML
    public ChoiceBox<String> hideFileType_Img, insertImgType_Img, linkNameType_Img, sheetName_Img, matchFileType_Img;
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
    public TextField imgWidth_Img, imgHeight_Img, excelName_Img, subCode_Img, startRow_Img, startCell_Img, readRow_Img,
            readCell_Img, maxRow_Img, maxImgNum_Img, linkLeftName_Img, filterFileType_Img, linkRightName_Img;

    @FXML
    public CheckBox jpg_Img, png_Img, jpeg_Img, recursion_Img, showFileType_Img, openDirectory_Img, openFile_Img,
            noFile_Img, exportTitle_Img, exportFileNum_Img, exportFileSize_Img, reverseFileType_Img;

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
            prop.put(key_lastMaxRow, maxRow_Img.getText());
            prop.put(key_lastInPath, inPath_Img.getText());
            prop.put(key_lastOutPath, outPath_Img.getText());
            prop.put(key_lastReadRow, readRow_Img.getText());
            prop.put(key_lastSubCode, subCode_Img.getText());
            prop.put(key_lastStartRow, startRow_Img.getText());
            prop.put(key_lastImgWidth, imgWidth_Img.getText());
            prop.put(key_lastReadCell, readCell_Img.getText());
            prop.put(key_lastStartCell, startCell_Img.getText());
            prop.put(key_lastImgHeight, imgHeight_Img.getText());
            prop.put(key_lastExcelPath, excelPath_Img.getText());
            prop.put(key_lastMaxImgNum, maxImgNum_Img.getText());
            prop.put(key_lastExcelName, excelName_Img.getText());
            prop.put(key_linkLeftName, linkLeftName_Img.getText());
            prop.put(key_linkNameType, linkNameType_Img.getValue());
            prop.put(key_linkRightName, linkRightName_Img.getText());
            prop.put(key_insertImgType, insertImgType_Img.getValue());
            prop.put(key_matchFileType, matchFileType_Img.getValue());
            prop.put(key_lastHideFileType, hideFileType_Img.getValue());
            String noImgValue = noFile_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastNoImg, noImgValue);
            String openFileValue = openFile_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenFile, openFileValue);
            String recursionValue = recursion_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastRecursion, recursionValue);
            String exportTitleValue = exportTitle_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastExportTitle, exportTitleValue);
            String showFileTypeValue = showFileType_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastShowFileType, showFileTypeValue);
            String openDirectoryValue = openDirectory_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, openDirectoryValue);
            String exportFileNumValue = exportFileNum_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastExportFileNum, exportFileNumValue);
            String exportFileSizeValue = exportFileSize_Img.isSelected() ? activation : unActivation;
            prop.put(key_lastExportFileSize, exportFileSizeValue);
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
            setControlLastConfig(noFile_Img, prop, key_lastNoImg);
            setControlLastConfig(inPath_Img, prop, key_lastInPath);
            setControlLastConfig(maxRow_Img, prop, key_lastMaxRow);
            setControlLastConfig(readRow_Img, prop, key_lastReadRow);
            setControlLastConfig(outPath_Img, prop, key_lastOutPath);
            setControlLastConfig(readCell_Img, prop, key_lastReadCell);
            setControlLastConfig(openFile_Img, prop, key_lastOpenFile);
            setControlLastConfig(imgWidth_Img, prop, key_lastImgWidth);
            setControlLastConfig(startRow_Img, prop, key_lastStartRow);
            setControlLastConfig(excelPath_Img, prop, key_lastExcelPath);
            setControlLastConfig(startCell_Img, prop, key_lastStartCell);
            setControlLastConfig(imgHeight_Img, prop, key_lastImgHeight);
            setControlLastConfig(maxImgNum_Img, prop, key_lastMaxImgNum);
            setControlLastConfig(recursion_Img, prop, key_lastRecursion);
            setControlLastConfig(excelName_Img, prop, key_lastExcelName);
            setControlLastConfig(sheetName_Img, prop, key_lastSheetName);
            setControlLastConfig(linkNameType_Img, prop, key_linkNameType);
            setControlLastConfig(linkLeftName_Img, prop, key_linkLeftName);
            setControlLastConfig(matchFileType_Img, prop, key_matchFileType);
            setControlLastConfig(linkRightName_Img, prop, key_linkRightName);
            setControlLastConfig(insertImgType_Img, prop, key_insertImgType);
            setControlLastConfig(exportTitle_Img, prop, key_lastExportTitle);
            setControlLastConfig(hideFileType_Img, prop, key_lastHideFileType);
            setControlLastConfig(showFileType_Img, prop, key_lastShowFileType);
            setControlLastConfig(openDirectory_Img, prop, key_lastOpenDirectory);
            setControlLastConfig(exportFileNum_Img, prop, key_lastExportFileNum);
            setControlLastConfig(exportFileSize_Img, prop, key_lastExportFileSize);
            setControlLastConfig(subCode_Img, prop, key_lastSubCode, true);
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
     * 查询要处理的文件
     *
     * @param selectedFile        要读取的文件
     * @param filterExtensionList 要过滤的文件格式
     * @return 文件读取设置
     */
    private FileConfig creatFileConfig(File selectedFile, List<String> filterExtensionList) {
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
        return fileConfig;
    }

    /**
     * 添加数据渲染列表
     *
     * @param callback 读取文件回调
     */
    private void addInData(ReadGroupFileCallback callback) {
        if (readExcelTask == null) {
            removeAll();
            TaskBean<FileNumBean> taskBean = creatTaskBean();
            // 渲染表格前需要更新一下读取的文件
            Task<List<File>> readAllFilesTask;
            if (StringUtils.isNotBlank(inFilePath)) {
                FileConfig fileConfig = creatFileConfig(new File(inFilePath), getFilterExtension());
                if (text_matchFile.equals(matchFileType_Img.getValue())) {
                    fileConfig.setReverseFileType(reverseFileType_Img.isSelected());
                }
                readAllFilesTask = readAllFilesTask(taskBean, fileConfig);
            } else {
                readAllFilesTask = null;
            }
            if (readAllFilesTask != null) {
                bindingTaskNode(readAllFilesTask, taskBean);
                readAllFilesTask.setOnSucceeded(event -> {
                    inFileList = readAllFilesTask.getValue();
                    taskUnbind(taskBean);
                    startReadExcelTask(taskBean, callback);
                });
                if (!readAllFilesTask.isRunning()) {
                    Thread.ofVirtual()
                            .name("readAllFilesTask-vThread" + tabId)
                            .start(readAllFilesTask);
                }
            } else {
                startReadExcelTask(taskBean, callback);
            }
        }
    }

    /**
     * 执行读取excel任务
     *
     * @param taskBean 任务参数
     * @param callback 读取成功后的回调函数
     */
    private void startReadExcelTask(TaskBean<FileNumBean> taskBean, ReadGroupFileCallback callback) {
        excelType_Img.setText(getFileType(new File(excelInPath)));
        // 组装数据
        ExcelConfig excelConfig = new ExcelConfig();
        excelConfig.setReadCellNum(setDefaultIntValue(readCell_Img, defaultReadCell, 0, null))
                .setReadRowNum(setDefaultIntValue(readRow_Img, defaultReadRow, 0, null))
                .setMaxRowNum(setDefaultIntValue(maxRow_Img, -1, 1, null))
                .setSheetName(sheetName_Img.getValue())
                .setInPath(excelInPath);
        taskBean.setInFileList(inFileList);
        // 获取Task任务
        readExcelTask = readExcel(excelConfig, taskBean);
        // 绑定带进度条的线程
        bindingTaskNode(readExcelTask, taskBean);
        readExcelTask.setOnSucceeded(event -> {
            taskUnbind(taskBean);
            readExcelTask = null;
            if (callback != null) {
                callback.onComplete();
            }
        });
        readExcelTask.setOnFailed(event -> {
            taskUnbind(taskBean);
            taskNotSuccess(taskBean, text_taskFailed);
            readExcelTask = null;
            inFileList = null;
            throw new RuntimeException(event.getSource().getException());
        });
        if (!readExcelTask.isRunning()) {
            Thread.ofVirtual()
                    .name("readExcelTask-vThread" + tabId)
                    .start(readExcelTask);
        }
    }

    /**
     * 创建TaskBean
     *
     * @return TaskBean
     */
    private TaskBean<FileNumBean> creatTaskBean() {
        String maxImgValue = maxImgNum_Img.getText();
        int maxImgNum = 0;
        if (StringUtils.isNotBlank(maxImgValue)) {
            maxImgNum = Integer.parseInt(maxImgValue);
        }
        ChoiceBox<String> sort = settingController.sort_Set;
        String sortValue = sort.getValue();
        CheckBox reverseSort = settingController.reverseSort_Set;
        TaskBean<FileNumBean> taskBean = new TaskBean<>();
        taskBean.setShowFileType(showFileType_Img.isSelected())
                .setComparatorTableColumn(fileUnitSize_Img)
                .setReverseSort(reverseSort.isSelected())
                .setSubCode(subCode_Img.getText())
                .setMassageLabel(fileNumber_Img)
                .setProgressBar(progressBar_Img)
                .setDisableNodes(disableNodes)
                .setTableView(tableView_Img)
                .setInFileList(inFileList)
                .setMaxImgNum(maxImgNum)
                .setSheet(sheetName_Img)
                .setSortType(sortValue)
                .setTabId(tabId);
        return taskBean;
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_maxRow, maxRow_Img);
        addToolTip(tip_subCode, subCode_Img);
        addToolTip(tip_Img.getText(), tip_Img);
        addToolTip(tip_openFile, openFile_Img);
        addToolTip(tip_recursion, recursion_Img);
        addToolTip(tip_maxImgNum, maxImgNum_Img);
        addToolTip(tip_startReadRow, startRow_Img);
        addToolTip(tip_fileButton, fileButton_Img);
        addToolTip(tip_learButton, clearButton_Img);
        addToolTip(tip_exportTitle, exportTitle_Img);
        addToolTip(tip_showFileType, showFileType_Img);
        addToolTip(tip_exportButton, exportButton_Img);
        addToolTip(tip_outPathButton, outPathButton_Img);
        addToolTip(tip_openDirectory, openDirectory_Img);
        addToolTip(tip_exportFileNum, exportFileNum_Img);
        addToolTip(tip_filterFileType, filterFileType_Img);
        addToolTip(tip_exportFileSize, exportFileSize_Img);
        addToolTip(tip_reselectButton, reselectButton_Img);
        addToolTip(tip_noFile + text_noImg, noFile_Img);
        addToolTip(tip_excelPathButton, excelPathButton_Img);
        addToolTip(tip_filterImgType, jpg_Img, png_Img, jpeg_Img);
        addToolTip(tip_excelType, excelType_Img, excelTypeLabel_Img);
        addToolTip(tip_linkName, linkLeftName_Img, linkRightName_Img);
        addToolTip(reverseFileType_Img.getText(), reverseFileType_Img);
        addToolTip(tip_excelName + defaultOutFileName, excelName_Img);
        addValueToolTip(sheetName_Img, tip_sheetName, sheetName_Img.getValue());
        addToolTip(text_onlyNaturalNumber + defaultStartCell, startCell_Img);
        addValueToolTip(linkNameType_Img, tip_linkNameType, linkNameType_Img.getValue());
        addValueToolTip(hideFileType_Img, tip_hideFileType, hideFileType_Img.getValue());
        addToolTip(tip_imgHeightWidth + defaultImgWidth + tip_imgWidth, imgWidth_Img);
        addValueToolTip(insertImgType_Img, tip_insertImgType, insertImgType_Img.getValue());
        addValueToolTip(matchFileType_Img, tip_matchFileType, matchFileType_Img.getValue());
        addToolTip(tip_imgHeightWidth + defaultImgHeight + tip_imgHeight, imgHeight_Img);
        addToolTip(text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row, readRow_Img);
        addToolTip(text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell, readCell_Img);
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(sheetName_Img);
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
        // 鼠标悬留提示输入的文件名称分割符
        textFieldValueListener(subCode_Img, tip_subCode);
        // 鼠标悬留提示输入的超链接左侧名称
        textFieldValueListener(linkLeftName_Img, tip_linkName);
        // 鼠标悬留提示输入的超链接右侧名称
        textFieldValueListener(linkRightName_Img, tip_linkName);
        // 鼠标悬停提示输入的需要识别的文件格式
        textFieldValueListener(filterFileType_Img, tip_filterFileType);
        // 限制读取最大行数只能输入正整数
        integerRangeTextField(maxRow_Img, 1, null, tip_maxRow);
        // 最大匹配数量设置监听
        integerRangeTextField(maxImgNum_Img, 1, null, tip_maxImgNum);
        // 限制导出预留行只能输入自然数
        integerRangeTextField(startRow_Img, 0, null, tip_startReadRow);
        // 鼠标悬留提示输入的导出excel文件名称
        textFieldValueListener(excelName_Img, tip_excelName + defaultOutFileName);
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
        String totalFileSize = fileNumberText.substring(fileNumberText.lastIndexOf(text_totalFileSize)
                + text_totalFileSize.length());
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
     */
    private List<String> getFilterExtension() {
        List<String> filterExtensionList = new ArrayList<>();
        String value = matchFileType_Img.getValue();
        if (text_matchImg.equals(value)) {
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
                throw new RuntimeException("未选择需要识别的图片格式");
            }
        } else if (text_matchFile.equals(value)) {
            filterExtensionList = getFilterExtensionList(filterFileType_Img);
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
            insertImgTypAction();
            linkNameType();
            matchFileTypeAction();
        });
    }

    /**
     * 选择文件夹按钮功能
     *
     * @param actionEvent 交互事件
     * @throws IOException io异常
     */
    @FXML
    private void inDirectoryButton(ActionEvent actionEvent) throws IOException {
        getConfig();
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        // 显示文件选择器
        File selectedFile = creatDirectoryChooser(window, inFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            inFilePath = updatePathLabel(selectedFile.getPath(), inFilePath, key_inFilePath, inPath_Img, configFile_Img);
            // 读取文件数据
            FileConfig fileConfig = creatFileConfig(selectedFile, getFilterExtension());
            TaskBean<FileNumBean> taskBean = creatTaskBean();
            taskBean.setBeanList(tableView_Img.getItems())
                    .setBindingMassageLabel(false);
            Task<List<File>> readMachGroupTask = readMachGroup(taskBean, fileConfig);
            bindingTaskNode(readMachGroupTask, taskBean);
            readMachGroupTask.setOnSucceeded(event -> {
                taskUnbind(taskBean);
                inFileList = readMachGroupTask.getValue();
            });
            if (!readMachGroupTask.isRunning()) {
                Thread.ofVirtual()
                        .name("readMachGroupTask-vThread" + tabId)
                        .start(readMachGroupTask);
            }
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
        sheetName_Img.getItems().clear();
        try {
            addInData(null);
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
        String value = matchFileType_Img.getValue();
        files.forEach(file -> {
            String fileType = getFileType(file);
            if (text_matchImg.equals(value)) {
                if (xlsx.equals(fileType)) {
                    // 接受拖放
                    dragEvent.acceptTransferModes(TransferMode.COPY);
                    dragEvent.consume();
                }
            } else if (text_matchFile.equals(value)) {
                if (xlsx.equals(fileType) || xls.equals(fileType)) {
                    // 接受拖放
                    dragEvent.acceptTransferModes(TransferMode.COPY);
                    dragEvent.consume();
                }
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
     */
    @FXML
    private void exportAll() {
        if (buildExcelTask == null && saveExcelTask == null) {
            updateLabel(log_Img, "");
            String outFilePath = outPath_Img.getText();
            if (StringUtils.isEmpty(outFilePath)) {
                throw new RuntimeException(text_outPathNull);
            }
            if (StringUtils.isEmpty(inPath_Img.getText())) {
                throw new RuntimeException(text_filePathNull);
            }
            String inFilePath = excelPath_Img.getText();
            if (StringUtils.isEmpty(inFilePath)) {
                throw new RuntimeException(text_excelPathNull);
            }
            if (!new File(inFilePath).exists()) {
                throw new RuntimeException(text_directoryNotExists);
            }
            int readRowValue = setDefaultIntValue(readRow_Img, defaultReadRow, 0, null);
            String insertImgType = insertImgType_Img.getValue();
            ExcelConfig excelConfig = new ExcelConfig()
                    .setStartCellNum(setDefaultIntValue(startCell_Img, defaultStartCell, 0, null))
                    .setImgHeight(setDefaultIntValue(imgHeight_Img, defaultImgHeight, 0, null))
                    .setStartRowNum(setDefaultIntValue(startRow_Img, readRowValue, 0, null))
                    .setImgWidth(setDefaultIntValue(imgWidth_Img, defaultImgWidth, 0, null))
                    .setOutName(setDefaultFileName(excelName_Img, defaultOutFileName))
                    .setExportFileSize(exportFileSize_Img.isSelected())
                    .setExportFileNum(exportFileNum_Img.isSelected())
                    .setLinkRightName(linkRightName_Img.getText())
                    .setLinkNameType(linkNameType_Img.getValue())
                    .setExportTitle(exportTitle_Img.isSelected())
                    .setLinkLeftName(linkLeftName_Img.getText())
                    .setOutExcelType(excelType_Img.getText())
                    .setSheetName(sheetName_Img.getValue())
                    .setNoImg(noFile_Img.isSelected())
                    .setInsertType(insertImgType)
                    .setOutPath(outFilePath)
                    .setInPath(inFilePath);
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setDisableNodes(disableNodes)
                    .setProgressBar(progressBar_Img)
                    .setMassageLabel(log_Img);
            // 重新查询任务
            addInData(() -> {
                taskBean.setShowFileType(showFileType_Img.isSelected())
                        .setBeanList(tableView_Img.getItems())
                        .setCancelButton(cancel_Img)
                        .setTableView(tableView_Img)
                        .setTabId(tabId);
                // 校验匹配文件总大小是否能够正常导出
                if (checkFileSize()) {
                    // 组装excel任务
                    if (insertType_img.equals(insertImgType)) {
                        buildExcelTask = buildImgGroupExcel(taskBean, excelConfig);
                    } else {
                        buildExcelTask = buildNameGroupNumExcel(taskBean, excelConfig);
                    }
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
                        saveExcelTask.setOnFailed(event -> {
                            taskNotSuccess(taskBean, text_taskFailed);
                            resetTasks();
                            throw new RuntimeException(event.getSource().getException());
                        });
                        if (!saveExcelTask.isRunning()) {
                            Thread.ofVirtual()
                                    .name("saveExcelTask-vThread" + tabId)
                                    .start(saveExcelTask);
                        }
                    });
                    buildExcelTask.setOnFailed(event -> {
                        taskNotSuccess(taskBean, text_taskFailed);
                        resetTasks();
                        throw new RuntimeException(event.getSource().getException());
                    });
                    if (!buildExcelTask.isRunning()) {
                        Thread.ofVirtual()
                                .name("buildExcelTask-vThread" + tabId)
                                .start(buildExcelTask);
                    }
                }
            });
        }
    }

    /**
     * 设置导出文件按钮
     *
     * @param actionEvent 交互事件
     * @throws IOException io异常
     */
    @FXML
    private void exportPath(ActionEvent actionEvent) throws IOException {
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
     * @throws IOException io异常
     */
    @FXML
    private void getExcelPath(ActionEvent actionEvent) throws IOException {
        getConfig();
        List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
        String value = matchFileType_Img.getValue();
        if (text_matchImg.equals(value)) {
            extensionFilters.add(new FileChooser.ExtensionFilter("Excel(2007)", "*.xlsx"));
        } else if (text_matchFile.equals(value)) {
            extensionFilters.add(new FileChooser.ExtensionFilter("Excel", "*.xlsx", "*.xls"));
            extensionFilters.add(new FileChooser.ExtensionFilter("Excel(2007)", "*.xlsx"));
            extensionFilters.add(new FileChooser.ExtensionFilter("Excel(2003)", "*.xls"));
        }
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatFileChooser(window, excelInPath, extensionFilters, text_selectExcel);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            excelInPath = updatePathLabel(selectedFile.getPath(), excelInPath, key_excelInPath, excelPath_Img, configFile_Img);
            sheetName_Img.getItems().clear();
            addInData(null);
        }
    }

    /**
     * 重新查询按钮
     */
    @FXML
    private void reselect() {
        if (StringUtils.isEmpty(excelInPath)) {
            throw new RuntimeException(text_excelPathNull);
        }
        if (!new File(excelInPath).exists()) {
            throw new RuntimeException(text_excelNotExists);
        }
        updateLabel(log_Img, "");
        addInData(null);
    }

    /**
     * 是否展示文件拓展名选项监听
     */
    @FXML
    private void handleCheckBoxAction() {
        ObservableList<FileNumBean> fileBeans = tableView_Img.getItems();
        if (CollectionUtils.isNotEmpty(fileBeans) && StringUtils.isNotBlank(excelInPath)) {
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
        String value = insertImgType_Img.getValue();
        addValueToolTip(insertImgType_Img, tip_insertImgType, value);
        linkHBox_Img.setVisible(insertType_relativePath.equals(value) || insertType_absolutePath.equals(value));
    }

    /**
     * 超链接名称类型选项监听
     */
    @FXML
    private void linkNameType() {
        String linkNameType = linkNameType_Img.getValue();
        addValueToolTip(linkNameType_Img, tip_linkNameType, linkNameType);
        if (linkName_unified.equals(linkNameType)) {
            linkName_Img.setText("超链接名称:");
            linkRightHBox_Img.setVisible(false);
        } else if (linkName_splice.equals(linkNameType)) {
            linkName_Img.setText("超链接左侧名称:");
            linkRightHBox_Img.setVisible(true);
        }
    }

    /**
     * 工作表名称选项监听
     */
    @FXML
    private void sheetNameAction() {
        addValueToolTip(sheetName_Img, tip_sheetName, sheetName_Img.getValue());
    }

    /**
     * 隐藏文件查询设置选项监听
     */
    @FXML
    private void hideFileTypeAction() {
        addValueToolTip(hideFileType_Img, tip_hideFileType, hideFileType_Img.getValue());
    }

    /**
     * 文件插入类型选项监听
     */
    @FXML
    private void matchFileTypeAction() {
        String value = matchFileType_Img.getValue();
        addValueToolTip(matchFileType_Img, tip_matchFileType, value);
        removeChildren(fileTypeHBox_Img, imgHBox_Img, fileHBox_Img);
        if (text_matchImg.equals(value)) {
            fileTypeHBox_Img.getChildren().add(imgHBox_Img);
            ObservableList<String> items = insertImgType_Img.getItems();
            items.remove(insertType_img);
            items.addFirst(insertType_img);
            insertImgType_Img.setValue(items.getFirst());
            if (StringUtils.isNotBlank(excelInPath)) {
                File file = new File(excelInPath);
                if (!xlsx.equals(getFileType(file))) {
                    excelInPath = null;
                    setPathLabel(excelPath_Img, "");
                    excelType_Img.setText("选取excel模板后决定");
                }
            }
            addToolTip(tip_noFile + text_noImg, noFile_Img);
        } else if (text_matchFile.equals(value)) {
            fileTypeHBox_Img.getChildren().add(fileHBox_Img);
            ObservableList<String> items = insertImgType_Img.getItems();
            items.remove(insertType_img);
            insertImgType_Img.setValue(items.getFirst());
            addToolTip(tip_noFile + text_noFile, noFile_Img);
        }
    }

}
