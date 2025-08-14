package priv.koishi.tools.Controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.*;
import priv.koishi.tools.CustomUI.EditingCell.EditingCell;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static priv.koishi.tools.Controller.MainController.settingController;
import static priv.koishi.tools.Enum.SelectItemsEnums.*;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.MainApplication.mainScene;
import static priv.koishi.tools.MainApplication.mainStage;
import static priv.koishi.tools.Service.FileRenameService.*;
import static priv.koishi.tools.Service.ReadDataService.*;
import static priv.koishi.tools.Utils.CommonUtils.swapCase;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;
import static priv.koishi.tools.Utils.UiUtils.addValueToolTip;

/**
 * 按指定规则批量重命名文件页面控制器
 *
 * @author KOISHI
 * Date:2024-10-18
 * Time:下午4:36
 */
public class FileRenameController extends RootController {

    /**
     * 要处理的文件夹路径
     */
    private static String inFilePath;

    /**
     * excel模板路径
     */
    private static String excelInPath;

    /**
     * 页面标识符
     */
    private static final String tabId = "_Re";

    /**
     * 文件名起始编号
     */
    private static int defaultStartNameNum;

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
     * 读取文件线程
     */
    private Task<Void> readFileTask;

    /**
     * 读取excel线程
     */
    private Task<List<FileNumBean>> readExcelTask;

    /**
     * 重命名线程
     */
    private Task<String> renameTask;

    @FXML
    public AnchorPane anchorPane_Re;

    @FXML
    public ProgressBar progressBar_Re;

    @FXML
    public TableView<FileBean> tableView_Re;

    @FXML
    public TableColumn<FileBean, Integer> id_Re, index_Re;

    @FXML
    public TableColumn<FileBean, ImageView> thumb_Re;

    @FXML
    public TableColumn<FileBean, String> name_Re, rename_Re, path_Re, size_Re, fileType_Re, newFileType_Re,
            creatDate_Re, updateDate_Re, showStatus_Re;

    @FXML
    public CheckBox openDirectory_Re, addSpace_Re;

    @FXML
    public VBox vbox_Re, codeRenameVBox_Re, strRenameVBox_Re, excelRenameVBox_Re;

    @FXML
    public Label excelPath_Re, fileNumber_Re, inPath_Re, log_Re, typeLabel_Re, tip_Re, warn_Re;

    @FXML
    public HBox renameTypeHBox_Re, behaviorHBox_Re, targetStrHBox_Re, warnHBox_Re, tipHBox_Re, fileNumberHBox_Re;

    @FXML
    public Button fileButton_Re, clearButton_Re, renameButton_Re, reselectButton_Re, updateRenameButton_Re,
            excelPathButton_Re, updateSameCode_Re, updateFileType_Re;

    @FXML
    public ChoiceBox<String> hideFileType_Re, directoryNameType_Re, renameType_Re, subCode_Re, differenceCode_Re,
            targetStr_Re, leftBehavior_Re, rightBehavior_Re, renameBehavior_Re, renameFileType_Re, addFileType_Re,
            sheetName_Re;

    @FXML
    public TextField filterFileType_Re, readRow_Re, readCell_Re, maxRow_Re, startName_Re, nameNum_Re,
            startSize_Re, left_Re, right_Re, renameStr_Re, leftValue_Re, rightValue_Re, renameValue_Re, tag_Re,
            renameFileTypeText_Re, prefix_Re;

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tableView_Re.setPrefHeight(stageHeight * 0.45);
        // 设置组件宽度
        double stageWidth = mainStage.getWidth();
        double tableWidth = stageWidth * 0.94;
        tableView_Re.setMaxWidth(tableWidth);
        tableView_Re.setPrefWidth(tableWidth);
        regionRightAlignment(fileNumberHBox_Re, tableWidth, fileNumber_Re);
        regionRightAlignment(tipHBox_Re, tableWidth, tip_Re);
        regionRightAlignment(warnHBox_Re, tableWidth, warn_Re);
        bindPrefWidthProperty();
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.03));
        id_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.03));
        thumb_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.1));
        name_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.1));
        rename_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.1));
        fileType_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.07));
        newFileType_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.11));
        path_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.1));
        size_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.06));
        showStatus_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.06));
        creatDate_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.12));
        updateDate_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.12));
    }

    /**
     * 保存最后一次配置的值
     *
     * @throws IOException io异常
     */
    public void saveLastConfig() throws IOException {
        if (anchorPane_Re != null) {
            InputStream input = checkRunningInputStream(configFile_Rename);
            Properties prop = new Properties();
            prop.load(input);
            prop.put(key_lastInPath, inPath_Re.getText());
            prop.put(key_lastHideFileType, hideFileType_Re.getValue());
            prop.put(key_renameFileType, renameFileType_Re.getValue());
            prop.put(key_lastFilterFileType, filterFileType_Re.getText());
            prop.put(key_renameFileTypeText, renameFileTypeText_Re.getText());
            prop.put(key_lastDirectoryNameType, directoryNameType_Re.getValue());
            String openDirectoryValue = openDirectory_Re.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, openDirectoryValue);
            String renameTypeValue = renameType_Re.getValue();
            prop.put(key_lastRenameType, renameTypeValue);
            prop.put(key_addFileType, addFileType_Re.getValue());
            // 根据文件重命名依据设置保存配置信息
            saveLastConfigByRenameType(prop, renameTypeValue);
            OutputStream output = checkRunningOutputStream(configFile_Rename);
            prop.store(output, null);
            input.close();
            output.close();
        }
    }

    /**
     * 根据文件重命名依据设置保存配置信息
     *
     * @param prop            配置文件
     * @param renameTypeValue 重命名类型
     */
    private void saveLastConfigByRenameType(Properties prop, String renameTypeValue) {
        switch (renameTypeValue) {
            case text_codeRename: {
                // 按编号规则重命名保存配置信息
                saveLastConfigByCodeRename(prop);
                break;
            }
            case text_strRename: {
                // 按指定字符重命名保存配置信息
                saveLastConfigByStrRename(prop);
                break;
            }
            case text_excelRename: {
                // 按excel模板重命名保存配置信息
                saveLastConfigByExcelRename(prop);
                break;
            }
        }
    }

    /**
     * 按编号规则重命名保存配置信息
     *
     * @param prop 配置文件
     */
    private void saveLastConfigByCodeRename(Properties prop) {
        prop.put(key_lastStartName, startName_Re.getText());
        prop.put(key_lastStartSize, startSize_Re.getText());
        prop.put(key_lastNameNum, nameNum_Re.getText());
        prop.put(key_lastTag, tag_Re.getText());
        String addSpaceValue = addSpace_Re.isSelected() ? activation : unActivation;
        prop.put(key_lastAddSpace, addSpaceValue);
        prop.put(key_lastDifferenceCode, differenceCode_Re.getValue());
        prop.put(key_lastSubCode, subCode_Re.getValue());
    }

    /**
     * 按指定字符重命名保存配置信息
     *
     * @param prop 配置文件
     */
    private void saveLastConfigByStrRename(Properties prop) {
        String targetStrValue = targetStr_Re.getValue();
        prop.put(key_lastTargetStr, targetStrValue);
        if (text_specifyString.equals(targetStrValue) || text_specifyIndex.equals(targetStrValue)) {
            prop.put(key_lastRenameValue, renameValue_Re.getText());
            String renameBehaviorValue = renameBehavior_Re.getValue();
            prop.put(key_lastRenameBehavior, renameBehaviorValue);
            if (text_replace.equals(renameBehaviorValue)) {
                prop.put(key_lastRenameStr, renameStr_Re.getText());
            } else if (text_bothSides.equals(renameBehaviorValue)) {
                prop.put(key_lastLeft, left_Re.getText());
                String leftBehaviorValue = leftBehavior_Re.getValue();
                prop.put(key_lastLeftBehavior, leftBehaviorValue);
                if (text_insert.equals(leftBehaviorValue) || text_replace.equals(leftBehaviorValue)) {
                    prop.put(key_lastLeftValue, leftValue_Re.getText());
                }
                prop.put(key_lastRight, right_Re.getText());
                String rightBehaviorValue = rightBehavior_Re.getValue();
                prop.put(key_lastRightBehavior, rightBehaviorValue);
                if (text_insert.equals(rightBehaviorValue) || text_replace.equals(rightBehaviorValue)) {
                    prop.put(key_lastRightValue, rightValue_Re.getText());
                }
            }
        }
    }

    /**
     * 按excel模板重命名保存配置信息
     *
     * @param prop 配置文件
     */
    private void saveLastConfigByExcelRename(Properties prop) {
        prop.put(key_lastReadRow, readRow_Re.getText());
        prop.put(key_lastReadCell, readCell_Re.getText());
        prop.put(key_lastMaxRow, maxRow_Re.getText());
        prop.put(key_lastExcelPath, excelPath_Re.getText());
    }

    /**
     * 读取配置文件
     *
     * @throws IOException io异常
     */
    private void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Rename);
        prop.load(input);
        inFilePath = prop.getProperty(key_inFilePath);
        excelInPath = prop.getProperty(key_excelInPath);
        defaultReadRow = Integer.parseInt(prop.getProperty(key_defaultReadRow));
        defaultReadCell = Integer.parseInt(prop.getProperty(key_defaultReadCell));
        defaultStartNameNum = Integer.parseInt(prop.getProperty(key_defaultStartNameNum));
        input.close();
    }

    /**
     * 设置初始配置值为上次配置值
     *
     * @throws IOException io异常
     */
    private void setLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Rename);
        prop.load(input);
        if (activation.equals(prop.getProperty(key_loadLastConfig))) {
            setControlLastConfig(inPath_Re, prop, key_lastInPath);
            setControlLastConfig(renameType_Re, prop, key_lastRenameType);
            setControlLastConfig(hideFileType_Re, prop, key_lastHideFileType);
            setControlLastConfig(renameFileType_Re, prop, key_renameFileType);
            setControlLastConfig(openDirectory_Re, prop, key_lastOpenDirectory);
            setControlLastConfig(filterFileType_Re, prop, key_lastFilterFileType);
            setControlLastConfig(renameFileTypeText_Re, prop, key_renameFileTypeText);
            setControlLastConfig(directoryNameType_Re, prop, key_lastDirectoryNameType);
            // 根据重命名类型设置上次配置值
            setLastConfigByRenameType(prop);
        }
        input.close();
    }

    /**
     * 根据重命名类型设置上次配置值
     *
     * @param prop 配置文件
     */
    private void setLastConfigByRenameType(Properties prop) {
        switch (prop.getProperty(key_lastRenameType)) {
            case text_codeRename: {
                // 按编号规则重命名设置上次配置值
                setLastConfigByCodeRename(prop);
                break;
            }
            case text_strRename: {
                // 按指定字符重命名设置上次配置值
                setLastConfigByStrRename(prop);
                break;
            }
            case text_excelRename: {
                // 根据excel重命名设置上次配置值
                setLastConfigByExcelRename(prop);
                break;
            }
        }
    }

    /**
     * 按编号规则重命名设置上次配置值
     *
     * @param prop 配置文件
     */
    private void setLastConfigByCodeRename(Properties prop) {
        setControlLastConfig(tag_Re, prop, key_lastTag);
        setControlLastConfig(subCode_Re, prop, key_lastSubCode);
        setControlLastConfig(nameNum_Re, prop, key_lastNameNum);
        setControlLastConfig(addSpace_Re, prop, key_lastAddSpace);
        setControlLastConfig(startName_Re, prop, key_lastStartName);
        setControlLastConfig(startSize_Re, prop, key_lastStartSize);
        setControlLastConfig(addFileType_Re, prop, key_addFileType);
        setControlLastConfig(differenceCode_Re, prop, key_lastDifferenceCode);
    }

    /**
     * 按指定字符重命名设置上次配置值
     *
     * @param prop 配置文件
     */
    private void setLastConfigByStrRename(Properties prop) {
        setControlLastConfig(targetStr_Re, prop, key_lastTargetStr);
        String lastTargetStr = prop.getProperty(key_lastTargetStr);
        if (text_specifyString.equals(lastTargetStr)) {
            // 指定字符串设置上次配置值
            setLastConfigBySpecifyString(prop);
        } else if (text_specifyIndex.equals(lastTargetStr)) {
            // 指定字符位置设置上次配置值
            setLastConfigBySpecifyIndex(prop);
        }
    }

    /**
     * 指定字符串设置上次配置值
     *
     * @param prop 配置文件
     */
    private void setLastConfigBySpecifyString(Properties prop) {
        setControlLastConfig(renameValue_Re, prop, key_lastRenameValue);
        setControlLastConfig(renameBehavior_Re, prop, key_lastRenameBehavior);
        String lastRenameBehavior = prop.getProperty(key_lastRenameBehavior);
        if (text_replace.equals(lastRenameBehavior)) {
            setControlLastConfig(renameStr_Re, prop, key_lastRenameStr);
        } else if (text_bothSides.equals(lastRenameBehavior)) {
            // 处理左侧字符设置上次配置值
            setLastConfigByOneSide(prop, left_Re, key_lastLeft, leftBehavior_Re, key_lastLeftBehavior, leftValue_Re, key_lastLeftValue);
            // 处理右侧字符设置上次配置值
            setLastConfigByOneSide(prop, right_Re, key_lastRight, rightBehavior_Re, key_lastRightBehavior, rightValue_Re, key_lastRightValue);
        }
    }

    /**
     * 处理单侧字符设置上次配置值
     *
     * @param prop            配置文件
     * @param side            填写匹配方向设置参数的文本输入框
     * @param sideKey         填写匹配方向设置参数的文本输入框上次填写值对应的key
     * @param sideBehavior    当前侧重命名行为下拉框
     * @param sideBehaviorKey 下拉框上次选项对应的key
     * @param sideValue       当前侧替换或插入字符文本输入框
     * @param valueKey        当前侧替换或插入字符文本输入框上次填写值对应的key
     */
    private void setLastConfigByOneSide(Properties prop, TextField side, String sideKey, ChoiceBox<String> sideBehavior, String sideBehaviorKey, TextField sideValue, String valueKey) {
        setControlLastConfig(side, prop, sideKey);
        setControlLastConfig(sideBehavior, prop, sideBehaviorKey);
        String lastLeftBehavior = prop.getProperty(sideBehaviorKey);
        if (text_insert.equals(lastLeftBehavior) || text_replace.equals(lastLeftBehavior)) {
            setControlLastConfig(sideValue, prop, valueKey);
        }
    }

    /**
     * 指定字符位置设置上次配置值
     *
     * @param prop 配置文件
     */
    private void setLastConfigBySpecifyIndex(Properties prop) {
        setControlLastConfig(renameValue_Re, prop, key_lastRenameValue);
        setControlLastConfig(renameBehavior_Re, prop, key_lastRenameBehavior);
        if (text_replace.equals(prop.getProperty(key_lastRenameBehavior))) {
            setControlLastConfig(renameStr_Re, prop, key_lastRenameStr);
        }
    }

    /**
     * 根据excel重命名设置上次配置值
     *
     * @param prop 配置文件
     */
    private void setLastConfigByExcelRename(Properties prop) {
        setControlLastConfig(maxRow_Re, prop, key_lastMaxRow);
        setControlLastConfig(readRow_Re, prop, key_lastReadRow);
        setControlLastConfig(readCell_Re, prop, key_lastReadCell);
        setControlLastConfig(sheetName_Re, prop, key_lastSheetName);
        setControlLastConfig(excelPath_Re, prop, key_lastExcelPath);
    }

    /**
     * 添加数据渲染列表
     *
     * @param inFileList 要读取的文件
     * @throws Exception 未查询到符合条件的数据
     */
    public void addInData(List<File> inFileList) throws Exception {
        if (readFileTask == null) {
            removeAll();
            if (inFileList.isEmpty()) {
                throw new Exception(text_selectNull);
            }
            TaskBean<FileBean> taskBean = creatTaskBean(inFileList);
            // 匹配重命名规则
            matchRenameConfig(taskBean);
            // 获取Task任务
            readFileTask = readFile(taskBean);
            // 绑定带进度条的线程
            bindingTaskNode(readFileTask, taskBean);
            readFileTask.setOnSucceeded(event -> {
                if (text_excelRename.equals(renameType_Re.getValue()) && StringUtils.isNotBlank(excelPath_Re.getText())) {
                    readExcelRename();
                }
                taskUnbind(taskBean);
                readFileTask = null;
            });
            readFileTask.setOnFailed(event -> {
                taskNotSuccess(taskBean, text_taskFailed);
                readFileTask = null;
                throw new RuntimeException(event.getSource().getException());
            });
            addFileType_Re.setDisable(true);
            if (!readFileTask.isRunning()) {
                Thread.ofVirtual()
                        .name("readFileTask-vThread" + tabId)
                        .start(readFileTask);
            }
        }
    }

    /**
     * 创建TaskBean
     *
     * @param inFileList 要读取的文件
     * @return TaskBean
     */
    private TaskBean<FileBean> creatTaskBean(List<File> inFileList) {
        ChoiceBox<String> sort = settingController.sort_Set;
        CheckBox reverseSort = settingController.reverseSort_Set;
        String sortValue = sort.getValue();
        TaskBean<FileBean> taskBean = new TaskBean<>();
        taskBean.setReverseSort(reverseSort.isSelected())
                .setComparatorTableColumn(size_Re)
                .setProgressBar(progressBar_Re)
                .setMassageLabel(fileNumber_Re)
                .setDisableNodes(disableNodes)
                .setTableView(tableView_Re)
                .setInFileList(inFileList)
                .setSortType(sortValue)
                .setShowFileType(false)
                .setTabId(tabId);
        return taskBean;
    }

    /**
     * 读取excel重命名模板
     */
    private void readExcelRename() {
        if (readExcelTask == null) {
            ExcelConfig excelConfig = new ExcelConfig();
            excelConfig.setReadCellNum(setDefaultIntValue(readCell_Re, defaultReadCell, 0, null))
                    .setReadRowNum(setDefaultIntValue(readRow_Re, defaultReadRow, 0, null))
                    .setMaxRowNum(setDefaultIntValue(maxRow_Re, -1, 1, null))
                    .setSheetName(sheetName_Re.getValue())
                    .setInPath(excelInPath);
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setDisableNodes(disableNodes)
                    .setProgressBar(progressBar_Re)
                    .setReturnRenameList(true)
                    .setMassageLabel(log_Re)
                    .setShowFileType(false)
                    .setSheet(sheetName_Re)
                    .setTabId(tabId);
            // 获取Task任务
            readExcelTask = readExcel(excelConfig, taskBean);
            // 绑定带进度条的线程
            bindingTaskNode(readExcelTask, taskBean);
            readExcelTask.setOnSucceeded(event -> {
                List<String> excelRenameList = readExcelTask.getValue().stream().map(FileNumBean::getGroupName).toList();
                ObservableList<FileBean> fileBeanList = tableView_Re.getItems();
                if (CollectionUtils.isNotEmpty(fileBeanList) && CollectionUtils.isNotEmpty(excelRenameList)) {
                    matchExcelRename(fileBeanList, excelRenameList);
                }
                taskUnbind(taskBean);
                updateLabel(log_Re, "");
                readExcelTask = null;
            });
            readExcelTask.setOnFailed(event -> {
                taskNotSuccess(taskBean, text_taskFailed);
                readExcelTask = null;
                throw new RuntimeException(event.getSource().getException());
            });
            // 使用新线程启动
            if (!readExcelTask.isRunning()) {
                Thread.ofVirtual()
                        .name("readExcelTask-vThread" + tabId)
                        .start(readExcelTask);
            }
        }
    }

    /**
     * 匹配excel重命名数据
     *
     * @param fileBeanList    要匹配的文件信息
     * @param excelRenameList 要匹配的excel数据
     */
    private void matchExcelRename(ObservableList<? extends FileBean> fileBeanList, List<String> excelRenameList) {
        int fileBeanListSize = fileBeanList.size();
        int excelRenameListSize = excelRenameList.size();
        for (int i = 0; i < fileBeanListSize; i++) {
            FileBean fileBean = fileBeanList.get(i);
            if (i < excelRenameListSize) {
                String fileRename = excelRenameList.get(i);
                if (StringUtils.isNotBlank(fileRename)) {
                    fileBean.setRename(fileRename);
                } else {
                    fileBean.setRename(fileBean.getName());
                }
            } else {
                fileBean.setRename(fileBean.getName());
            }
        }
    }

    /**
     * 匹配重命名规则
     *
     * @param taskBean 用于存储重命名设置的线程参数类
     */
    private void matchRenameConfig(TaskBean<FileBean> taskBean) {
        String renameType = renameType_Re.getValue();
        switch (renameType) {
            case text_codeRename: {
                matchCodeRename(taskBean);
                break;
            }
            case text_strRename: {
                matchStringRename(taskBean);
                break;
            }
        }
    }

    /**
     * 按编号规则重命名
     *
     * @param taskBean 用于存储重命名设置的线程参数类
     */
    private void matchCodeRename(TaskBean<FileBean> taskBean) {
        CodeRenameConfig codeRenameConfig = new CodeRenameConfig();
        codeRenameConfig.setStartName(setDefaultIntValue(startName_Re, 1, 0, null))
                .setStartSize(setDefaultIntValue(startSize_Re, 0, 0, null))
                .setNameNum(setDefaultIntValue(nameNum_Re, 0, 0, null))
                .setTag(setDefaultIntValue(tag_Re, 1, 0, null))
                .setDifferenceCode(differenceCode_Re.getValue())
                .setAddSpace(addSpace_Re.isSelected())
                .setSubCode(subCode_Re.getValue())
                .setPrefix(prefix_Re.getText());
        taskBean.setConfiguration(codeRenameConfig);
    }

    /**
     * 按指定字符重命名
     *
     * @param taskBean 用于存储重命名设置的线程参数类
     */
    private void matchStringRename(TaskBean<FileBean> taskBean) {
        String targetStr = targetStr_Re.getValue();
        StringRenameConfig stringRenameConfig = new StringRenameConfig();
        stringRenameConfig.setTargetStr(targetStr);
        if (text_specifyString.equals(targetStr) || text_specifyIndex.equals(targetStr)) {
            String renameBehavior = renameBehavior_Re.getValue();
            stringRenameConfig.setRenameValue(renameValue_Re.getText())
                    .setRenameBehavior(renameBehavior);
            if (text_replace.equals(renameBehavior)) {
                stringRenameConfig.setRenameStr(renameStr_Re.getText());
            } else if (text_bothSides.equals(renameBehavior)) {
                String leftBehavior = leftBehavior_Re.getValue();
                String rightBehavior = rightBehavior_Re.getValue();
                stringRenameConfig.setRight(setDefaultIntValue(right_Re, 0, 0, null))
                        .setLeft(setDefaultIntValue(left_Re, 0, 0, null))
                        .setRightBehavior(rightBehavior)
                        .setLeftBehavior(leftBehavior);
                if (text_insert.equals(leftBehavior) || text_replace.equals(leftBehavior)) {
                    stringRenameConfig.setLeftValue(leftValue_Re.getText());
                }
                if (text_insert.equals(rightBehavior) || text_replace.equals(rightBehavior)) {
                    stringRenameConfig.setRightValue(rightValue_Re.getText());
                }
            }
        }
        taskBean.setConfiguration(stringRenameConfig);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_tag, tag_Re);
        addToolTip(tip_left, left_Re);
        addToolTip(tip_right, right_Re);
        addToolTip(tip_maxRow, maxRow_Re);
        addToolTip(tip_nameNum, nameNum_Re);
        addToolTip(tip_Re.getText(), tip_Re);
        addToolTip(tip_addSpace, addSpace_Re);
        addToolTip(tip_rename, renameButton_Re);
        addToolTip(tip_leftValue, leftValue_Re);
        addToolTip(tip_startSize, startSize_Re);
        addToolTip(tip_renameStr, renameStr_Re);
        addToolTip(tip_fileButton, fileButton_Re);
        addToolTip(tip_rightValue, rightValue_Re);
        addToolTip(tip_learButton, clearButton_Re);
        addToolTip(tip_renameValue, renameValue_Re);
        addToolTip(tip_openDirectory, openDirectory_Re);
        addToolTip(tip_filterFileType, filterFileType_Re);
        addToolTip(tip_reselectButton, reselectButton_Re);
        addToolTip(tip_updateSameCode, updateSameCode_Re);
        addToolTip(tip_updateFileType, updateFileType_Re);
        addToolTip(tip_excelPathButton, excelPathButton_Re);
        addToolTip(tip_updateRenameButton, updateRenameButton_Re);
        addToolTip(tip_reNameFileTypeText, renameFileTypeText_Re);
        addValueToolTip(targetStr_Re, tip_targetStr, targetStr_Re.getValue());
        addValueToolTip(subCode_Re, tip_subCodeSelect, subCode_Re.getValue());
        addValueToolTip(sheetName_Re, tip_sheetName, sheetName_Re.getValue());
        addValueToolTip(leftBehavior_Re, tip_option, leftBehavior_Re.getValue());
        addValueToolTip(renameType_Re, tip_renameType, renameType_Re.getValue());
        addValueToolTip(rightBehavior_Re, tip_option, rightBehavior_Re.getValue());
        addToolTip(text_onlyNaturalNumber + defaultStartNameNum, startName_Re);
        addValueToolTip(addFileType_Re, tip_addFileType, addFileType_Re.getValue());
        addValueToolTip(hideFileType_Re, tip_hideFileType, hideFileType_Re.getValue());
        addValueToolTip(differenceCode_Re, tip_differenceCode, differenceCode_Re.getValue());
        addValueToolTip(renameFileType_Re, tip_reNameFileType, renameFileType_Re.getValue());
        addValueToolTip(renameBehavior_Re, tip_renameBehavior, renameBehavior_Re.getValue());
        addValueToolTip(directoryNameType_Re, tip_directoryNameType, directoryNameType_Re.getValue());
        addToolTip(text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row, readRow_Re);
        addToolTip(text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell, readCell_Re);
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(fileButton_Re);
        disableNodes.add(clearButton_Re);
        disableNodes.add(renameButton_Re);
        disableNodes.add(reselectButton_Re);
        disableNodes.add(updateSameCode_Re);
        disableNodes.add(excelPathButton_Re);
        disableNodes.add(updateRenameButton_Re);
        Node autoClickTab = mainScene.lookup("#autoClickTab");
        disableNodes.add(autoClickTab);
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 要匹配的字符串鼠标悬停提示
        if (text_specifyIndex.equals(targetStr_Re.getValue())) {
            integerRangeTextField(renameValue_Re, 0, null, tip_renameValue);
        }
        // 给目标字符串右侧替换或插入输入框添加鼠标悬停提示
        textFieldValueListener(leftValue_Re, tip_leftValue);
        // 指定字符串所替换的字符串鼠标悬停提示
        textFieldValueListener(renameStr_Re, tip_renameStr);
        // 给目标字符串左侧替换或插入输入框添加鼠标悬停提示
        textFieldValueListener(rightValue_Re, tip_rightValue);
        // 限制相同编号文件起始尾缀输入框内容
        integerRangeTextField(tag_Re, 0, null, tip_tag);
        // 限制向左匹配字符位置输入框内容
        integerRangeTextField(left_Re, 0, null, tip_left);
        // 限制向右匹配字符位置输入框内容
        integerRangeTextField(right_Re, 0, null, tip_right);
        // 鼠标悬留提示输入的需要识别的文件后缀名
        textFieldValueListener(filterFileType_Re, tip_filterFileType);
        // 给更新文件拓展名输入框添加鼠标悬停提示
        textFieldValueListener(renameFileTypeText_Re, tip_reNameFileTypeText);
        // 限制读取最大行数只能输入正整数
        integerRangeTextField(maxRow_Re, 1, null, tip_maxRow);
        // 鼠标悬留提示输入的相同编号文件数量
        integerRangeTextField(nameNum_Re, 0, null, tip_nameNum);
        // 鼠标悬留提示输入的文件名起始编号位数
        integerRangeTextField(startSize_Re, 0, null, tip_startSize);
        // 鼠标悬留提示输入的文件名起始编号
        integerRangeTextField(startName_Re, 0, null, text_onlyNaturalNumber + defaultStartNameNum);
        // 限制读取起始行只能输入自然数
        integerRangeTextField(readRow_Re, 0, null, text_onlyNaturalNumber + defaultReadRow + text_formThe + (defaultReadRow + 1) + text_row);
        // 限制读取起始列只能输入自然数
        integerRangeTextField(readCell_Re, 0, null, text_onlyNaturalNumber + defaultReadCell + text_formThe + (defaultReadCell + 1) + text_cell);
    }

    /**
     * 设置文本输入框提示
     */
    private void setPromptText() {
        readRow_Re.setPromptText(String.valueOf(defaultReadRow));
        readCell_Re.setPromptText(String.valueOf(defaultReadCell));
        startName_Re.setPromptText(String.valueOf(defaultStartNameNum));
    }

    /**
     * 构建右键菜单
     */
    private void setTableViewContextMenu() {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 移动所选行选项
        buildMoveDataMenu(tableView_Re, contextMenu);
        // 查看文件选项
        buildFilePathItem(tableView_Re, contextMenu);
        // 设置所选数据为同一编号
        updateSameCodeMenu(contextMenu);
        // 批量修改文件拓展名
        updateFileTypeMenu(contextMenu);
        // 取消选中选项
        buildClearSelectedData(tableView_Re, contextMenu);
        // 删除所选数据选项
        buildDeleteDataMenuItem(tableView_Re, fileNumber_Re, contextMenu, text_file);
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(contextMenu, tableView_Re);
    }

    /**
     * 设置所选数据为同一编号
     *
     * @param contextMenu 右键菜单
     */
    private void updateSameCodeMenu(ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem("设置所选数据为同一编号");
        menuItem.setOnAction(event -> {
            try {
                updateSameCode();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 批量修改文件拓展名
     *
     * @param contextMenu 右键菜单
     */
    private void updateFileTypeMenu(ContextMenu contextMenu) {
        Menu menu = new Menu("修改选中文件拓展名");
        // 创建二级菜单项
        MenuItem replace = new MenuItem(text_replace);
        MenuItem removeAll = new MenuItem(text_removeAll);
        MenuItem toUpperCase = new MenuItem(text_toUpperCase);
        MenuItem toLowerCase = new MenuItem(text_toLowerCase);
        MenuItem swapCase = new MenuItem(text_swapCase);
        ObservableList<FileBean> selectedItems = tableView_Re.getSelectionModel().getSelectedItems();
        replace.setOnAction(event -> updateFileTypes(selectedItems, text_replace));
        removeAll.setOnAction(event -> updateFileTypes(selectedItems, text_removeAll));
        toUpperCase.setOnAction(event -> updateFileTypes(selectedItems, text_toUpperCase));
        toLowerCase.setOnAction(event -> updateFileTypes(selectedItems, text_toLowerCase));
        swapCase.setOnAction(event -> updateFileTypes(selectedItems, text_swapCase));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(replace, removeAll, toUpperCase, toLowerCase, swapCase);
        contextMenu.getItems().add(menu);
    }

    /**
     * 批量修改文件拓展名
     *
     * @param fileBeans      文件列表
     * @param reNameFileType 修改方式
     */
    private void updateFileTypes(ObservableList<FileBean> fileBeans, String reNameFileType) {
        switch (reNameFileType) {
            case text_replace: {
                updateFileType(fileBeans);
                break;
            }
            case text_removeAll: {
                removeFileType(fileBeans);
                break;
            }
            case text_toUpperCase: {
                fileTypeToUpperCase(fileBeans);
                break;
            }
            case text_toLowerCase: {
                fileTypeToLowerCase(fileBeans);
                break;
            }
            case text_swapCase: {
                swapCaseFileType(fileBeans);
                break;
            }
        }
        tableView_Re.refresh();
        updateLabel(log_Re, "");
    }

    /**
     * 文件拓展名替换所有字符为
     *
     * @param fileBeans 文件列表
     */
    private void updateFileType(ObservableList<FileBean> fileBeans) {
        String fileType = renameFileTypeText_Re.getText();
        if (StringUtils.isNotBlank(fileType)) {
            if (!fileType.startsWith(".")) {
                fileType = "." + fileType;
            }
            for (FileBean fileBean : fileBeans) {
                fileBean.setNewFileType(fileType);
            }
        }
        tableView_Re.refresh();
        updateLabel(log_Re, "");
    }

    /**
     * 文件拓展名移除所有字符
     *
     * @param fileBeans 文件列表
     */
    private void removeFileType(ObservableList<FileBean> fileBeans) {
        for (FileBean fileBean : fileBeans) {
            fileBean.setNewFileType("");
        }
        tableView_Re.refresh();
        updateLabel(log_Re, "");
    }

    /**
     * 文件拓展名全部英文字符转为小写
     *
     * @param fileBeans 文件列表
     */
    private void fileTypeToLowerCase(ObservableList<FileBean> fileBeans) {
        for (FileBean fileBean : fileBeans) {
            fileBean.setNewFileType(fileBean.getFileType().toLowerCase());
        }
        tableView_Re.refresh();
        updateLabel(log_Re, "");
    }

    /**
     * 文件拓展名全部英文字符转为大写
     *
     * @param fileBeans 文件列表
     */
    private void fileTypeToUpperCase(ObservableList<FileBean> fileBeans) {
        for (FileBean fileBean : fileBeans) {
            fileBean.setNewFileType(fileBean.getFileType().toUpperCase());
        }
        tableView_Re.refresh();
        updateLabel(log_Re, "");
    }

    /**
     * 文件拓展名全部英文字符大小写互换
     *
     * @param fileBeans 文件列表
     */
    private void swapCaseFileType(ObservableList<FileBean> fileBeans) {
        for (FileBean fileBean : fileBeans) {
            fileBean.setNewFileType(swapCase(fileBean.getFileType()));
        }
        tableView_Re.refresh();
        updateLabel(log_Re, "");
    }

    /**
     * 启动读取文件任务
     *
     * @param fileConfig 读取文件任务配置
     */
    private void startReadFilesTask(FileConfig fileConfig) {
        TaskBean<FileBean> taskBean = creatTaskBean(null);
        Task<List<File>> readFileTask = readAllFilesTask(taskBean, fileConfig);
        bindingTaskNode(readFileTask, taskBean);
        readFileTask.setOnSucceeded(event -> {
            taskUnbind(taskBean);
            try {
                addInData(readFileTask.getValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        if (!readFileTask.isRunning()) {
            Thread.ofVirtual()
                    .name("readFileTask-vThread" + tabId)
                    .start(readFileTask);
        }
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
        // 设置要暂时移除的组件
        removeChildren(vbox_Re, strRenameVBox_Re, excelRenameVBox_Re);
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
            autoBuildTableViewData(tableView_Re, FileBean.class, tabId, index_Re);
            // 设置文件大小排序
            fileSizeColum(size_Re);
            // 表格设置为可编辑
            tableView_Re.setEditable(true);
            rename_Re.setCellFactory((tableColumn) -> new EditingCell<>(FileBean::setRename));
            newFileType_Re.setCellFactory((tableColumn) -> new EditingCell<>(FileBean::setNewFileType));
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Re);
            // 构建右键菜单
            setTableViewContextMenu();
            if (text_addFile.equals(addFileType_Re.getValue())) {
                setPathLabel(inPath_Re, "");
            }
            // 监听列表数据变化
            tableView_Re.getItems().addListener((ListChangeListener<FileBean>) change ->
                    addFileType_Re.setDisable(!tableView_Re.getItems().isEmpty()));
            addFileTypeAction();
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
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Re);
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        String addFileType = addFileType_Re.getValue();
        if (text_addFile.equals(addFileType)) {
            List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(filterExtensionList)) {
                List<String> filters = new ArrayList<>();
                for (String filter : filterExtensionList) {
                    if (filter.startsWith(".")) {
                        String f = "*" + filter;
                        filters.add(f);
                        extensionFilters.add(new FileChooser.ExtensionFilter(filter + " 文件", f));
                    }
                }
                extensionFilters.add(new FileChooser.ExtensionFilter("所有符合条件的文件格式", filters));
            }
            List<File> selectedFile = creatFilesChooser(window, inFilePath, extensionFilters, text_selectFile);
            if (CollectionUtils.isNotEmpty(selectedFile)) {
                inFilePath = selectedFile.getFirst().getParentFile().getPath();
                updateProperties(configFile_Rename, key_inFilePath, inFilePath);
                setPathLabel(inPath_Re, "");
                // fileChooser.showOpenMultipleDialog(window) 返回的是一个不可编辑的 List<File>，无法进行排序
                addInData(new ArrayList<>(selectedFile));
            }
        } else if (text_addDirectory.equals(addFileType)) {
            // 显示文件选择器
            File selectedFile = creatDirectoryChooser(window, inFilePath, text_selectDirectory);
            FileConfig fileConfig = new FileConfig();
            fileConfig.setShowDirectoryName(directoryNameType_Re.getValue())
                    .setShowHideFile(hideFileType_Re.getValue())
                    .setFilterExtensionList(filterExtensionList)
                    .setInFile(selectedFile);
            if (selectedFile != null) {
                // 更新所选文件路径显示
                inFilePath = updatePathLabel(selectedFile.getAbsolutePath(), inFilePath, key_inFilePath, inPath_Re, configFile_Rename);
                // 读取数据
                startReadFilesTask(fileConfig);
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
        TaskBean<FileBean> taskBean = creatTaskBean(null);
        Task<List<File>> readDropFilesTask = readDropFiles(taskBean, files);
        readDropFilesTask.setOnSucceeded(event -> {
            taskUnbind(taskBean);
            try {
                addInData(readDropFilesTask.getValue());
            } catch (Exception e) {
                showExceptionAlert(e);
            }
        });
        if (!readDropFilesTask.isRunning()) {
            Thread.ofVirtual()
                    .name("readDropFilesTask-vThread" + tabId)
                    .start(readDropFilesTask);
        }
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽中事件
     */
    @FXML
    private void acceptDrop(DragEvent dragEvent) {
        // 接受拖放
        dragEvent.acceptTransferModes(TransferMode.COPY);
        dragEvent.consume();
    }

    /**
     * 清空列表按钮功能
     */
    @FXML
    private void removeAll() {
        tableView_Re.getItems().stream().parallel().forEach(FileBean::clearResources);
        removeTableViewData(tableView_Re, fileNumber_Re, log_Re);
    }

    /**
     * 开始重命名按钮
     *
     * @throws Exception 要读取的文件列表为空
     */
    @FXML
    private void renameAll() throws Exception {
        if (renameTask == null) {
            updateLabel(log_Re, "");
            ObservableList<FileBean> fileBeans = tableView_Re.getItems();
            if (CollectionUtils.isEmpty(fileBeans)) {
                throw new Exception(text_fileListNull);
            }
            Map<String, List<FileBean>> fileBeanMap = fileBeans.stream().collect(Collectors.groupingBy(FileBean::getFullRename));
            List<String> repeatNameList = new ArrayList<>();
            fileBeanMap.forEach((rename, fileBeanList) -> {
                if (fileBeanList.size() > 1) {
                    List<String> ids = new ArrayList<>();
                    fileBeanList.forEach(fileBean -> ids.add(String.valueOf(fileBean.getId())));
                    String errString = "序号为: " + String.join("、", ids) + " 的文件重命名重复为 " + rename;
                    repeatNameList.add(errString);
                }
            });
            String repeatNames = String.join("\n", repeatNameList) + "\n";
            List<String> errNameList = new ArrayList<>();
            fileBeans.forEach(fileBean -> {
                String rename = fileBean.getFullRename();
                if (!isValidFileName(rename)) {
                    String errString = "序号为: " + fileBean.getId() + " 的文件 " + fileBean.getFullName() + " 非法重命名为 " + fileBean.getFullRename();
                    errNameList.add(errString);
                }
                if (!new File(fileBean.getPath()).exists()) {
                    String errString = "序号为: " + fileBean.getId() + " 的文件 " + fileBean.getFullName() + " 不存在";
                    errNameList.add(errString);
                }
            });
            String errNames = String.join("\n", errNameList) + "\n";
            String errString = "";
            if (StringUtils.isNotBlank(repeatNames)) {
                errString += "重复的名称：\n" + repeatNames;
            }
            if (StringUtils.isNotBlank(errNames)) {
                errString += "错误的名称：\n" + errNames;
            }
            if (StringUtils.isNotBlank(errString)) {
                Alert alert = creatErrorAlert(errString);
                alert.setHeaderText("文件重命名配置错误");
                // 展示弹窗
                alert.showAndWait();
            } else {
                TaskBean<FileBean> taskBean = new TaskBean<>();
                taskBean.setDisableNodes(disableNodes)
                        .setProgressBar(progressBar_Re)
                        .setMassageLabel(log_Re)
                        .setBeanList(fileBeans)
                        .setTabId(tabId);
                // 匹配重命名规则
                matchRenameConfig(taskBean);
                // 获取Task任务
                renameTask = fileRename(taskBean);
                // 绑定带进度条的线程
                bindingTaskNode(renameTask, taskBean);
                renameTask.setOnSucceeded(event -> {
                    String value = renameTask.getValue();
                    if (StringUtils.isBlank(value)) {
                        taskNotSuccess(taskBean, text_taskFailed);
                    } else {
                        taskUnbind(taskBean);
                        if (openDirectory_Re.isSelected()) {
                            openDirectory(value);
                        }
                        taskBean.getMassageLabel().setTextFill(Color.GREEN);
                    }
                    renameTask = null;
                });
                renameTask.setOnFailed(event -> {
                    taskNotSuccess(taskBean, text_taskFailed);
                    renameTask = null;
                    throw new RuntimeException(event.getSource().getException());
                });
                if (!renameTask.isRunning()) {
                    Thread.ofVirtual()
                            .name("renameTask-vThread" + tabId)
                            .start(renameTask);
                }
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
        List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(new FileChooser.ExtensionFilter("Excel", "*.xlsx")));
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatFileChooser(window, excelInPath, extensionFilters, text_selectExcel);
        if (selectedFile != null) {
            excelInPath = updatePathLabel(selectedFile.getAbsolutePath(), excelInPath, key_excelInPath, excelPath_Re, configFile_Rename);
            sheetName_Re.getItems().clear();
            readExcelRename();
        }
    }

    /**
     * 重新查询按钮
     *
     * @throws Exception 要查询的文件夹位置为空、要读取的文件夹不存在
     */
    @FXML
    private void reselect() throws Exception {
        String inFilePath = inPath_Re.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception(text_filePathNull);
        }
        File file = new File(inFilePath);
        if (!file.exists()) {
            throw new Exception(text_directoryNotExists);
        }
        updateLabel(log_Re, "");
        FileConfig fileConfig = new FileConfig();
        fileConfig.setFilterExtensionList(getFilterExtensionList(filterFileType_Re))
                .setShowDirectoryName(directoryNameType_Re.getValue())
                .setShowHideFile(hideFileType_Re.getValue())
                .setInFile(file);
        startReadFilesTask(fileConfig);
    }

    /**
     * 是否向分隔符左侧添加一个空格选项监听
     */
    @FXML
    private void handleCheckBoxAction() {
        differenceCodeAction();
    }

    /**
     * 根据区分编码类型更新重命名分隔符设置下拉框选项
     */
    @FXML
    private void differenceCodeAction() {
        String differenceCode = differenceCode_Re.getValue();
        addValueToolTip(differenceCode_Re, tip_differenceCode, differenceCode);
        switch (differenceCode) {
            case text_arabicNumerals: {
                updateSelectItems(addSpace_Re, subCode_Re, subCodeArabicNumItems);
                break;
            }
            case text_chineseNumerals: {
                updateSelectItems(addSpace_Re, subCode_Re, subCodeChineseNumItems);
                break;
            }
            case text_abc: {
                updateSelectItems(addSpace_Re, subCode_Re, subCodeLowercaseItems);
                break;
            }
            case text_ABC: {
                updateSelectItems(addSpace_Re, subCode_Re, subCodeUppercaseNumItems);
                break;
            }
        }
    }

    /**
     * 根据重命名方式展示设置组件
     */
    @FXML
    private void renameTypeAction() {
        addValueToolTip(renameType_Re, tip_renameType, renameType_Re.getValue());
        int index = vbox_Re.getChildren().indexOf(renameTypeHBox_Re) + 1;
        removeChildren(vbox_Re, strRenameVBox_Re, excelRenameVBox_Re, codeRenameVBox_Re);
        switch (renameType_Re.getValue()) {
            case text_codeRename: {
                vbox_Re.getChildren().add(index, codeRenameVBox_Re);
                updateSameCode_Re.setVisible(true);
                break;
            }
            case text_strRename: {
                vbox_Re.getChildren().add(index, strRenameVBox_Re);
                updateSameCode_Re.setVisible(false);
                // 根据匹配字符规则选项展示组件
                targetStrAction();
                break;
            }
            case text_excelRename: {
                vbox_Re.getChildren().add(index, excelRenameVBox_Re);
                updateSameCode_Re.setVisible(false);
                break;
            }
        }
    }

    /**
     * 监控匹配字符规则选项
     */
    @FXML
    private void targetStrAction() {
        addValueToolTip(targetStr_Re, tip_targetStr, targetStr_Re.getValue());
        switch (targetStr_Re.getValue()) {
            case text_specifyString: {
                typeLabel_Re.setText(text_matchString);
                renameValue_Re.setText("");
                addValueToolTip(renameValue_Re, tip_renameValue);
                renameBehavior_Re.getItems().remove(text_bothSides);
                renameBehavior_Re.getItems().add(text_bothSides);
                // 根据重命名方法选项展示组件
                behaviorAction();
                break;
            }
            case text_specifyIndex: {
                typeLabel_Re.setText(text_matchIndex);
                renameValue_Re.setText("");
                addValueToolTip(renameValue_Re, tip_renameValue);
                renameBehavior_Re.getItems().remove(text_bothSides);
                renameBehavior_Re.setValue(renameBehavior_Re.getItems().getFirst());
                // 根据重命名方法选项展示组件
                behaviorAction();
                break;
            }
            default: {
                targetStrHBox_Re.setVisible(false);
                behaviorHBox_Re.setVisible(false);
                break;
            }
        }
    }

    /**
     * 监控重命名方法选项
     */
    @FXML
    private void behaviorAction() {
        addValueToolTip(renameBehavior_Re, tip_renameBehavior, renameBehavior_Re.getValue());
        targetStrHBox_Re.setVisible(true);
        switch (renameBehavior_Re.getValue()) {
            case text_replace: {
                renameStr_Re.setVisible(true);
                behaviorHBox_Re.setVisible(false);
                break;
            }
            case text_remove: {
                renameStr_Re.setVisible(false);
                behaviorHBox_Re.setVisible(false);
                break;
            }
            case text_bothSides: {
                renameStr_Re.setVisible(false);
                behaviorHBox_Re.setVisible(true);
                break;
            }
        }
    }

    /**
     * 监控指定位置左侧处理方式下拉框
     */
    @FXML
    private void leftBehaviorAction() {
        String item = leftBehavior_Re.getValue();
        addValueToolTip(leftBehavior_Re, tip_option, item);
        leftValue_Re.setVisible(text_insert.equals(item) || text_replace.equals(item));
    }

    /**
     * 监控指定位置右侧处理方式下拉框
     */
    @FXML
    public void rightBehaviorAction() {
        String item = rightBehavior_Re.getValue();
        addValueToolTip(rightBehavior_Re, tip_option, item);
        rightValue_Re.setVisible(text_insert.equals(item) || text_replace.equals(item));
    }

    /**
     * 更新重命名按钮
     *
     * @throws Exception 要读取的文件列表为空
     */
    @FXML
    private void updateRename() throws Exception {
        ObservableList<FileBean> fileBeans = tableView_Re.getItems();
        if (CollectionUtils.isEmpty(fileBeans)) {
            throw new Exception(text_fileListNull);
        }
        if (text_excelRename.equals(renameType_Re.getValue()) && StringUtils.isNotBlank(excelPath_Re.getText())) {
            readExcelRename();
        } else if (!text_excelRename.equals(renameType_Re.getValue())) {
            TaskBean<FileBean> taskBean = new TaskBean<>();
            // 匹配重命名规则
            matchRenameConfig(taskBean);
            Configuration configuration = taskBean.getConfiguration();
            CodeRenameConfig codeRenameConfig = null;
            StringRenameConfig stringRenameConfig = null;
            int startName = -1;
            int tag = -1;
            int nameNum = 1;
            if (configuration != null) {
                if (configuration instanceof CodeRenameConfig) {
                    codeRenameConfig = (CodeRenameConfig) configuration;
                    startName = codeRenameConfig.getStartName();
                    tag = codeRenameConfig.getTag();
                } else if (configuration instanceof StringRenameConfig) {
                    stringRenameConfig = (StringRenameConfig) configuration;
                }
            }
            for (FileBean fileBean : fileBeans) {
                // 组装文件重命名数据
                buildRename(codeRenameConfig, fileBean, stringRenameConfig, startName, tag);
                if (codeRenameConfig != null) {
                    if (nameNum < codeRenameConfig.getNameNum()) {
                        tag++;
                        nameNum++;
                    } else {
                        startName++;
                        tag = codeRenameConfig.getTag();
                        nameNum = 1;
                    }
                }
            }
        }
        // 表格设置为可编辑
        tableView_Re.setEditable(true);
        rename_Re.setCellFactory((tableColumn) -> new EditingCell<>(FileBean::setRename));
        tableView_Re.refresh();
        updateLabel(log_Re, "");
    }

    /**
     * 设置所选数据为同一编号
     *
     * @throws Exception 未选中任何数据
     */
    @FXML
    private void updateSameCode() throws Exception {
        List<FileBean> selectedFileBeans = tableView_Re.getSelectionModel().getSelectedItems();
        if (CollectionUtils.isEmpty(selectedFileBeans)) {
            throw new Exception(text_nullSelect);
        }
        // 选中的数据按照最新设置进行重命名
        CodeRenameConfig codeRenameConfig = new CodeRenameConfig();
        codeRenameConfig.setStartSize(setDefaultIntValue(startSize_Re, 0, 0, null))
                .setTag(setDefaultIntValue(tag_Re, 1, 0, null))
                .setDifferenceCode(differenceCode_Re.getValue())
                .setAddSpace(addSpace_Re.isSelected())
                .setNameNum(selectedFileBeans.size())
                .setSubCode(subCode_Re.getValue())
                .setPrefix(prefix_Re.getText());
        ObservableList<FileBean> tableViewItems = tableView_Re.getItems();
        FileBean firstSelectFileBean = selectedFileBeans.getFirst();
        int startSelectIndex = tableViewItems.indexOf(firstSelectFileBean);
        FileBean lastSelectFileBean = selectedFileBeans.getLast();
        int endSelectIndex = tableViewItems.indexOf(lastSelectFileBean);
        String code = firstSelectFileBean.getCodeRename();
        int startCode = Integer.parseInt(code);
        int startTag = codeRenameConfig.getTag();
        if (StringUtils.isNotBlank(firstSelectFileBean.getTagRename())) {
            startTag = firstSelectFileBean.getTagRenameCode();
        }
        for (int i = startSelectIndex; i <= endSelectIndex; i++) {
            FileBean fileBean = tableViewItems.get(i);
            fileBean.setRename(getCodeRename(codeRenameConfig, fileBean, startCode, startTag));
            startTag++;
        }
        // 未选中的数据按照之前设置更新编号
        if (endSelectIndex < tableViewItems.size() - 1) {
            int unSelectStartIndex = endSelectIndex + 1;
            FileBean firstUnSelectFileBean = tableViewItems.get(unSelectStartIndex);
            CodeRenameConfig unSelectCodeRenameConfig = firstUnSelectFileBean.getCodeRenameConfig();
            int nameNum = 1;
            int startName = startCode + 1;
            int unSelectStartTag = unSelectCodeRenameConfig.getTag();
            int maxNameNum = unSelectCodeRenameConfig.getNameNum();
            for (int i = unSelectStartIndex; i < tableViewItems.size(); i++) {
                FileBean fileBean = tableViewItems.get(i);
                fileBean.setRename(getCodeRename(unSelectCodeRenameConfig, fileBean, startName, unSelectStartTag));
                if (nameNum < maxNameNum) {
                    unSelectStartTag++;
                    nameNum++;
                } else {
                    startName++;
                    unSelectStartTag = unSelectCodeRenameConfig.getTag();
                    nameNum = 1;
                }
            }
        }
        tableView_Re.refresh();
        updateLabel(log_Re, "");
    }

    /**
     * 批量修改文件拓展名
     *
     * @throws Exception 要读取的文件列表为空
     */
    @FXML
    private void updateFileTypes() throws Exception {
        ObservableList<FileBean> fileBeans = tableView_Re.getItems();
        if (CollectionUtils.isEmpty(fileBeans)) {
            throw new Exception(text_fileListNull);
        }
        // 批量修改文件拓展名
        updateFileTypes(fileBeans, renameFileType_Re.getValue());
    }

    /**
     * 更新文件拓展名设置
     */
    @FXML
    private void renameFileTypeAction() {
        addValueToolTip(renameFileType_Re, tip_reNameFileType, renameFileType_Re.getValue());
        renameFileTypeText_Re.setVisible(text_replace.equals(renameFileType_Re.getValue()));
    }

    /**
     * 添加数据设置下拉框监听
     */
    @FXML
    public void addFileTypeAction() {
        Platform.runLater(() -> {
            addValueToolTip(addFileType_Re, tip_addFileType, addFileType_Re.getValue());
            if (text_addDirectory.equals(addFileType_Re.getValue())) {
                reselectButton_Re.setVisible(true);
                fileButton_Re.setText(text_selectReadFolder);
                directoryNameType_Re.setDisable(false);
            } else {
                reselectButton_Re.setVisible(false);
                fileButton_Re.setText(text_selectReadFile);
                directoryNameType_Re.setValue(text_onlyFile);
                directoryNameType_Re.setDisable(true);
            }
        });
    }

    /**
     * 工作表名称选项监听
     *
     * @throws Exception 要读取的文件列表为空
     */
    @FXML
    private void sheetNameAction() throws Exception {
        addValueToolTip(sheetName_Re, tip_sheetName, sheetName_Re.getValue());
        ObservableList<FileBean> fileBeans = tableView_Re.getItems();
        if (CollectionUtils.isNotEmpty(fileBeans)) {
            updateRename();
        }
    }

    /**
     * 文件夹查询设置选项监听
     */
    @FXML
    private void directoryTypeAction() {
        addValueToolTip(directoryNameType_Re, tip_directoryNameType, directoryNameType_Re.getValue());
    }

    /**
     * 隐藏文件类型选项监听
     */
    @FXML
    private void hideFileTypeAction() {
        addValueToolTip(hideFileType_Re, tip_hideFileType, hideFileType_Re.getValue());
    }

    /**
     * 分隔符选项监听
     */
    @FXML
    private void subCodeAction() {
        addValueToolTip(subCode_Re, tip_subCode, subCode_Re.getValue());
    }

}
