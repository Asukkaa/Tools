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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.*;
import priv.koishi.tools.EditingCell.EditingCell;
import priv.koishi.tools.Properties.CommonProperties;
import priv.koishi.tools.ThreadPool.CommonThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static priv.koishi.tools.Enum.SelectItemsEnums.*;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Service.FileRenameService.*;
import static priv.koishi.tools.Service.ReadDataService.readExcel;
import static priv.koishi.tools.Service.ReadDataService.readFile;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 按指定规则批量重命名文件页面控制器
 *
 * @author KOISHI
 * Date:2024-10-18
 * Time:下午4:36
 */
public class FileRenameController extends CommonProperties {

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
     * 读取excel线程
     */
    private Task<List<FileNumBean>> readExcelTask;

    /**
     * 重命名线程
     */
    private Task<String> renameTask;

    /**
     * 程序主场景
     */
    private Scene mainScene;

    @FXML
    private AnchorPane anchorPane_Re;

    @FXML
    private ProgressBar progressBar_Re;

    @FXML
    private TableView<FileBean> tableView_Re;

    @FXML
    private TableColumn<FileBean, Integer> id_Re;

    @FXML
    private TableColumn<FileBean, String> name_Re, rename_Re, path_Re, size_Re, fileType_Re,
            creatDate_Re, updateDate_Re, showStatus_Re;

    @FXML
    private CheckBox openDirectory_Re, addSpace_Re;

    @FXML
    private VBox vbox_Re, codeRenameVBox_Re, strRenameVBox_Re, excelRenameVBox_Re;

    @FXML
    private Label excelPath_Re, fileNumber_Re, inPath_Re, log_Re, typeLabel_Re, tip_Re, warn_Re;

    @FXML
    private HBox renameTypeHBox_Re, behaviorHBox_Re, targetStrHBox_Re, warnHBox_Re, tipHBox_Re, fileNumberHBox_Re;

    @FXML
    private Button fileButton_Re, clearButton_Re, renameButton_Re, reselectButton_Re, updateRenameButton_Re,
            excelPathButton_Re, updateSameCode_Re;

    @FXML
    private ChoiceBox<String> hideFileType_Re, directoryNameType_Re, renameType_Re, subCode_Re, differenceCode_Re,
            targetStr_Re, leftBehavior_Re, rightBehavior_Re, renameBehavior_Re;

    @FXML
    private TextField sheetName_Re, filterFileType_Re, readRow_Re, readCell_Re, maxRow_Re, startName_Re, nameNum_Re,
            startSize_Re, left_Re, right_Re, renameStr_Re, leftValue_Re, rightValue_Re, renameValue_Re, tag_Re;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void fileRenameAdaption(Stage stage) {
        Scene scene = stage.getScene();
        // 设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Re");
        table.setPrefHeight(stageHeight * 0.45);
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        table.setMaxWidth(tableWidth);
        Node id = scene.lookup("#id_Re");
        id.setStyle("-fx-pref-width: " + tableWidth * 0.04 + "px;");
        Node name = scene.lookup("#name_Re");
        name.setStyle("-fx-pref-width: " + tableWidth * 0.12 + "px;");
        Node rename = scene.lookup("#rename_Re");
        rename.setStyle("-fx-pref-width: " + tableWidth * 0.12 + "px;");
        Node fileType = scene.lookup("#fileType_Re");
        fileType.setStyle("-fx-pref-width: " + tableWidth * 0.06 + "px;");
        Node path = scene.lookup("#path_Re");
        path.setStyle("-fx-pref-width: " + tableWidth * 0.2 + "px;");
        Node size = scene.lookup("#size_Re");
        size.setStyle("-fx-pref-width: " + tableWidth * 0.08 + "px;");
        Node showStatus = scene.lookup("#showStatus_Re");
        showStatus.setStyle("-fx-pref-width: " + tableWidth * 0.06 + "px;");
        Node creatDate = scene.lookup("#creatDate_Re");
        creatDate.setStyle("-fx-pref-width: " + tableWidth * 0.16 + "px;");
        Node updateDate = scene.lookup("#updateDate_Re");
        updateDate.setStyle("-fx-pref-width: " + tableWidth * 0.16 + "px;");
        Label fileNum = (Label) scene.lookup("#fileNumber_Re");
        HBox fileNumberHBox = (HBox) scene.lookup("#fileNumberHBox_Re");
        nodeRightAlignment(fileNumberHBox, tableWidth, fileNum);
        Label tip = (Label) scene.lookup("#tip_Re");
        HBox tipHBox = (HBox) scene.lookup("#tipHBox_Re");
        nodeRightAlignment(tipHBox, tableWidth, tip);
        Label warn = (Label) scene.lookup("#warn_Re");
        HBox warnHBox = (HBox) scene.lookup("#warnHBox_Re");
        nodeRightAlignment(warnHBox, tableWidth, warn);
    }

    /**
     * 保存最后一次配置的值
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    public static void fileRenameSaveLastConfig(Scene scene) throws IOException {
        AnchorPane anchorPane = (AnchorPane) scene.lookup("#anchorPane_Re");
        if (anchorPane != null) {
            InputStream input = checkRunningInputStream(configFile_Rename);
            Properties prop = new Properties();
            prop.load(input);
            ChoiceBox<?> directoryNameType = (ChoiceBox<?>) scene.lookup("#directoryNameType_Re");
            prop.put(key_lastDirectoryNameType, directoryNameType.getValue());
            ChoiceBox<?> hideFileType = (ChoiceBox<?>) scene.lookup("#hideFileType_Re");
            prop.put(key_lastHideFileType, hideFileType.getValue());
            CheckBox openDirectory = (CheckBox) scene.lookup("#openDirectory_Re");
            String openDirectoryValue = openDirectory.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, openDirectoryValue);
            TextField filterFileType = (TextField) scene.lookup("#filterFileType_Re");
            prop.put(key_lastFilterFileType, filterFileType.getText());
            Label inPath = (Label) scene.lookup("#inPath_Re");
            prop.put(key_lastInPath, inPath.getText());
            ChoiceBox<?> renameType = (ChoiceBox<?>) scene.lookup("#renameType_Re");
            String renameTypeValue = (String) renameType.getValue();
            prop.put(key_lastRenameType, renameTypeValue);
            // 根据文件重命名依据设置保存配置信息
            saveLastConfigByRenameType(prop, renameTypeValue, scene);
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
     * @param scene           程序主场景
     */
    private static void saveLastConfigByRenameType(Properties prop, String renameTypeValue, Scene scene) {
        switch (renameTypeValue) {
            case text_codeRename: {
                // 按编号规则重命名保存配置信息
                saveLastConfigByCodeRename(prop, scene);
                break;
            }
            case text_strRename: {
                // 按指定字符重命名保存配置信息
                saveLastConfigByStrRename(prop, scene);
                break;
            }
            case text_excelRename: {
                // 按excel模板重命名保存配置信息
                saveLastConfigByExcelRename(prop, scene);
                break;
            }
        }
    }

    /**
     * 按编号规则重命名保存配置信息
     *
     * @param prop  配置文件
     * @param scene 程序主场景
     */
    private static void saveLastConfigByCodeRename(Properties prop, Scene scene) {
        TextField startName = (TextField) scene.lookup("#startName_Re");
        prop.put(key_lastStartName, startName.getText());
        TextField startSize = (TextField) scene.lookup("#startSize_Re");
        prop.put(key_lastStartSize, startSize.getText());
        TextField nameNum = (TextField) scene.lookup("#nameNum_Re");
        prop.put(key_lastNameNum, nameNum.getText());
        TextField tag = (TextField) scene.lookup("#tag_Re");
        prop.put(key_lastTag, tag.getText());
        CheckBox addSpace = (CheckBox) scene.lookup("#addSpace_Re");
        String addSpaceValue = addSpace.isSelected() ? activation : unActivation;
        prop.put(key_lastAddSpace, addSpaceValue);
        ChoiceBox<?> differenceCode = (ChoiceBox<?>) scene.lookup("#differenceCode_Re");
        prop.put(key_lastDifferenceCode, differenceCode.getValue());
        ChoiceBox<?> subCode = (ChoiceBox<?>) scene.lookup("#subCode_Re");
        prop.put(key_lastSubCode, subCode.getValue());
    }

    /**
     * 按指定字符重命名保存配置信息
     *
     * @param prop  配置文件
     * @param scene 程序主场景
     */
    private static void saveLastConfigByStrRename(Properties prop, Scene scene) {
        ChoiceBox<?> targetStr = (ChoiceBox<?>) scene.lookup("#targetStr_Re");
        String targetStrValue = (String) targetStr.getValue();
        prop.put(key_lastTargetStr, targetStrValue);
        if (text_specifyString.equals(targetStrValue) || text_specifyIndex.equals(targetStrValue)) {
            TextField renameValue = (TextField) scene.lookup("#renameValue_Re");
            prop.put(key_lastRenameValue, renameValue.getText());
            ChoiceBox<?> renameBehavior = (ChoiceBox<?>) scene.lookup("#renameBehavior_Re");
            String renameBehaviorValue = (String) renameBehavior.getValue();
            prop.put(key_lastRenameBehavior, renameBehaviorValue);
            if (text_replace.equals(renameBehaviorValue)) {
                TextField renameStr = (TextField) scene.lookup("#renameStr_Re");
                prop.put(key_lastRenameStr, renameStr.getText());
            } else if (text_bothSides.equals(renameBehaviorValue)) {
                TextField left = (TextField) scene.lookup("#left_Re");
                prop.put(key_lastLeft, left.getText());
                ChoiceBox<?> leftBehavior = (ChoiceBox<?>) scene.lookup("#leftBehavior_Re");
                String leftBehaviorValue = (String) leftBehavior.getValue();
                prop.put(key_lastLeftBehavior, leftBehaviorValue);
                if (text_insert.equals(leftBehaviorValue) || text_replace.equals(leftBehaviorValue)) {
                    TextField leftValue = (TextField) scene.lookup("#leftValue_Re");
                    prop.put(key_lastLeftValue, leftValue.getText());
                }
                TextField right = (TextField) scene.lookup("#right_Re");
                prop.put(key_lastRight, right.getText());
                ChoiceBox<?> rightBehavior = (ChoiceBox<?>) scene.lookup("#rightBehavior_Re");
                String rightBehaviorValue = (String) rightBehavior.getValue();
                prop.put(key_lastRightBehavior, rightBehaviorValue);
                if (text_insert.equals(rightBehaviorValue) || text_replace.equals(rightBehaviorValue)) {
                    TextField rightValue = (TextField) scene.lookup("#rightValue_Re");
                    prop.put(key_lastRightValue, rightValue.getText());
                }
            }
        }
    }

    /**
     * 按excel模板重命名保存配置信息
     *
     * @param prop  配置文件
     * @param scene 程序主场景
     */
    private static void saveLastConfigByExcelRename(Properties prop, Scene scene) {
        TextField sheetName = (TextField) scene.lookup("#sheetName_Re");
        prop.put(key_lastSheetName, sheetName.getText());
        TextField readRow = (TextField) scene.lookup("#readRow_Re");
        prop.put(key_lastReadRow, readRow.getText());
        TextField readCell = (TextField) scene.lookup("#readCell_Re");
        prop.put(key_lastReadCell, readCell.getText());
        TextField maxRow = (TextField) scene.lookup("#maxRow_Re");
        prop.put(key_lastMaxRow, maxRow.getText());
        Label excelPath = (Label) scene.lookup("#excelPath_Re");
        prop.put(key_lastExcelPath, excelPath.getText());
    }

    /**
     * 添加数据渲染列表
     *
     * @param inFileList 要读取的文件
     * @throws Exception 未查询到符合条件的数据
     */
    private void addInData(List<File> inFileList) throws Exception {
        if (readFileTask == null) {
            removeAll();
            if (inFileList.isEmpty()) {
                throw new Exception(text_selectNull);
            }
            ChoiceBox<?> sort = (ChoiceBox<?>) mainScene.lookup("#sort_Set");
            CheckBox reverseSort = (CheckBox) mainScene.lookup("#reverseSort_Set");
            String sortValue = (String) sort.getValue();
            TaskBean<FileBean> taskBean = new TaskBean<>();
            taskBean.setReverseSort(reverseSort.isSelected())
                    .setDisableNodes(disableNodes)
                    .setComparatorTableColumn(size_Re)
                    .setProgressBar(progressBar_Re)
                    .setMassageLabel(fileNumber_Re)
                    .setTableView(tableView_Re)
                    .setInFileList(inFileList)
                    .setSortType(sortValue)
                    .setShowFileType(false)
                    .setTabId(tabId);
            // 匹配重命名规则
            matchRenameConfig(taskBean);
            // 获取Task任务
            readFileTask = readFile(taskBean);
            // 绑定带进度条的线程
            bindingProgressBarTask(readFileTask, taskBean);
            readFileTask.setOnSucceeded(event -> {
                if (text_excelRename.equals(renameType_Re.getValue()) && StringUtils.isNotBlank(excelPath_Re.getText())) {
                    readExcelRename();
                } else {
                    taskUnbind(taskBean);
                }
                readFileTask = null;
            });
            if (!readFileTask.isRunning()) {
                executorService.execute(readFileTask);
            }
        }
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
                    .setSheetName(sheetName_Re.getText())
                    .setInPath(excelPath_Re.getText());
            TaskBean<FileNumBean> taskBean = new TaskBean<>();
            taskBean.setDisableNodes(disableNodes)
                    .setProgressBar(progressBar_Re)
                    .setReturnRenameList(true)
                    .setMassageLabel(log_Re)
                    .setShowFileType(false)
                    .setTabId(tabId);
            // 获取Task任务
            readExcelTask = readExcel(excelConfig, taskBean);
            readExcelTask.setOnSucceeded(event -> {
                List<String> excelRenameList = readExcelTask.getValue().stream().map(FileNumBean::getGroupName).toList();
                ObservableList<FileBean> fileBeanList = tableView_Re.getItems();
                showMatchExcelData(taskBean, fileBeanList, excelRenameList);
                readExcelTask = null;
            });
            // 绑定带进度条的线程
            bindingProgressBarTask(readExcelTask, taskBean);
            // 使用新线程启动
            if (!readExcelTask.isRunning()) {
                executorService.execute(readExcelTask);
            }
        }
    }

    /**
     * 展示读取excel重命名数据
     *
     * @param taskBean        带有需要解绑的线程ui组件的线程参数类
     * @param fileBeanList    读取的文件信息
     * @param excelRenameList 读取的excel数据
     */
    private void showMatchExcelData(TaskBean<?> taskBean, ObservableList<FileBean> fileBeanList, List<String> excelRenameList) {
        if (CollectionUtils.isNotEmpty(fileBeanList) && CollectionUtils.isNotEmpty(excelRenameList)) {
            matchExcelRename(fileBeanList, excelRenameList);
        }
        // 表格设置为可编辑
        tableView_Re.setEditable(true);
        rename_Re.setCellFactory((tableColumn) -> new EditingCell<>(FileBean::setRename));
        taskUnbind(taskBean);
        if (taskBean.getMassageLabel() == log_Re) {
            log_Re.setText("");
        }
    }

    /**
     * 匹配excel重命名数据
     *
     * @param fileBeanList    要匹配的文件信息
     * @param excelRenameList 要匹配的excel数据
     */
    private void matchExcelRename(ObservableList<FileBean> fileBeanList, List<String> excelRenameList) {
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
                .setSubCode(subCode_Re.getValue());
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
            setControlLastConfig(renameType_Re, prop, key_lastRenameType);
            setControlLastConfig(hideFileType_Re, prop, key_lastHideFileType);
            setControlLastConfig(openDirectory_Re, prop, key_lastOpenDirectory);
            setControlLastConfig(inPath_Re, prop, key_lastInPath, anchorPane_Re);
            setControlLastConfig(filterFileType_Re, prop, key_lastFilterFileType);
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
        setControlLastConfig(excelPath_Re, prop, key_lastExcelPath, anchorPane_Re);
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        id_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.04));
        name_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.12));
        rename_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.12));
        fileType_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.06));
        path_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.2));
        size_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.08));
        showStatus_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.06));
        creatDate_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.16));
        updateDate_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.16));
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
        addToolTip(tip_targetStr, targetStr_Re);
        addToolTip(tip_rename, renameButton_Re);
        addToolTip(tip_leftValue, leftValue_Re);
        addToolTip(tip_startSize, startSize_Re);
        addToolTip(tip_renameStr, renameStr_Re);
        addToolTip(tip_sheetName, sheetName_Re);
        addToolTip(tip_fileButton, fileButton_Re);
        addToolTip(tip_rightValue, rightValue_Re);
        addToolTip(tip_renameType, renameType_Re);
        addToolTip(tip_subCodeSelect, subCode_Re);
        addToolTip(tip_learButton, clearButton_Re);
        addToolTip(tip_renameValue, renameValue_Re);
        addToolTip(tip_hideFileType, hideFileType_Re);
        addToolTip(tip_openDirectory, openDirectory_Re);
        addToolTip(tip_differenceCode, differenceCode_Re);
        addToolTip(tip_filterFileType, filterFileType_Re);
        addToolTip(tip_reselectButton, reselectButton_Re);
        addToolTip(tip_renameBehavior, renameBehavior_Re);
        addToolTip(tip_updateSameCode, updateSameCode_Re);
        addToolTip(tip_excelPathButton, excelPathButton_Re);
        addToolTip(tip_directoryNameType, directoryNameType_Re);
        addToolTip(tip_updateRenameButton, updateRenameButton_Re);
        addToolTip(tip_option, leftBehavior_Re, rightBehavior_Re);
        addToolTip(text_onlyNaturalNumber + defaultStartNameNum, startName_Re);
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
        // 限制相同编号文件起始尾缀输入框内容
        integerRangeTextField(tag_Re, 0, null, tip_tag);
        // 限制向左匹配字符位置输入框内容
        integerRangeTextField(left_Re, 0, null, tip_left);
        // 限制向右匹配字符位置输入框内容
        integerRangeTextField(right_Re, 0, null, tip_right);
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
        // 给目标字符串右侧替换或插入输入框添加鼠标悬停提示
        textFieldValueListener(leftValue_Re, tip_leftValue);
        // 指定字符串所替换的字符串鼠标悬停提示
        textFieldValueListener(renameStr_Re, tip_renameStr);
        // 鼠标悬留提示输入的导出excel表名称
        textFieldValueListener(sheetName_Re, tip_sheetName);
        // 给目标字符串左侧替换或插入输入框添加鼠标悬停提示
        textFieldValueListener(rightValue_Re, tip_rightValue);
        // 鼠标悬留提示输入的需要识别的文件后缀名
        textFieldValueListener(filterFileType_Re, tip_filterFileType);
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
        // 设置javafx单元格宽度
        bindPrefWidthProperty();
        // 给输入框添加内容变化监听
        textFieldChangeListener();
        // 设置初始配置值为上次配置值
        setLastConfig();
        Platform.runLater(() -> {
            mainScene = anchorPane_Re.getScene();
            // 设置要防重复点击的组件
            setDisableNodes();
            // 绑定表格数据
            autoBuildTableViewData(tableView_Re, FileBean.class, tabId);
            // 设置文件大小排序
            fileSizeColum(size_Re);
            // 表格设置为可编辑
            tableView_Re.setEditable(true);
            rename_Re.setCellFactory((tableColumn) -> new EditingCell<>(FileBean::setRename));
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Re);
            // 构建右键菜单
            tableViewContextMenu(tableView_Re, fileNumber_Re, anchorPane_Re);
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
        // 显示文件选择器
        File selectedFile = creatDirectoryChooser(actionEvent, inFilePath, text_selectDirectory);
        FileConfig fileConfig = new FileConfig();
        fileConfig.setShowDirectoryName(directoryNameType_Re.getValue())
                .setShowHideFile(hideFileType_Re.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setInFile(selectedFile);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            inFilePath = updatePathLabel(selectedFile.getAbsolutePath(), inFilePath, key_inFilePath, inPath_Re, configFile_Rename, anchorPane_Re);
            // 读取数据
            List<File> inFileList = readAllFiles(fileConfig);
            addInData(inFileList);
        }
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽释放事件
     * @throws Exception 未选择需要识别的图片格式
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) throws Exception {
        removeAll();
        List<File> files = dragEvent.getDragboard().getFiles();
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Re);
        File file = files.getFirst();
        FileConfig fileConfig = new FileConfig();
        fileConfig.setShowDirectoryName(directoryNameType_Re.getValue())
                .setShowHideFile(hideFileType_Re.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setInFile(file);
        List<File> inFileList = readAllFiles(fileConfig);
        String filePath = file.getPath();
        inPath_Re.setText(filePath);
        addToolTip(filePath, inPath_Re);
        addInData(inFileList);
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
                bindingProgressBarTask(renameTask, taskBean);
                renameTask.setOnSucceeded(event -> {
                    taskUnbind(taskBean);
                    if (openDirectory_Re.isSelected()) {
                        try {
                            openDirectory(renameTask.getValue());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    taskBean.getMassageLabel().setTextFill(Color.GREEN);
                    renameTask = null;
                });
                if (!renameTask.isRunning()) {
                    executorService.execute(renameTask);
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
        File selectedFile = creatFileChooser(actionEvent, excelInPath, extensionFilters, text_selectExcel);
        if (selectedFile != null) {
            excelInPath = updatePathLabel(selectedFile.getAbsolutePath(), excelInPath, key_excelInPath, excelPath_Re, configFile_Rename, anchorPane_Re);
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
        addInData(readAllFiles(fileConfig));
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
        switch (differenceCode_Re.getValue()) {
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
        switch (targetStr_Re.getValue()) {
            case text_specifyString: {
                typeLabel_Re.setText(text_matchString);
                renameValue_Re.setText("");
                addValueToolTip(renameValue_Re, tip_renameValue, text_nowValue);
                renameBehavior_Re.getItems().remove(text_bothSides);
                renameBehavior_Re.getItems().add(text_bothSides);
                // 根据重命名方法选项展示组件
                behaviorAction();
                break;
            }
            case text_specifyIndex: {
                typeLabel_Re.setText(text_matchIndex);
                renameValue_Re.setText("");
                addValueToolTip(renameValue_Re, tip_renameValue, text_nowValue);
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
        leftValue_Re.setVisible(text_insert.equals(item) || text_replace.equals(item));
    }

    /**
     * 监控指定位置右侧处理方式下拉框
     */
    @FXML
    public void rightBehaviorAction() {
        String item = rightBehavior_Re.getValue();
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
            // 表格设置为可编辑
            tableView_Re.setEditable(true);
            rename_Re.setCellFactory((tableColumn) -> new EditingCell<>(FileBean::setRename));
        }
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
                .setSubCode(subCode_Re.getValue());
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
        updateLabel(log_Re, "");
    }

}
