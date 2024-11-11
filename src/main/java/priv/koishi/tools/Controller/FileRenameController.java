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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
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
import priv.koishi.tools.Properties.ToolsProperties;
import priv.koishi.tools.ThreadPool.ToolsThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static priv.koishi.tools.Enum.SelectItemsEnums.*;
import static priv.koishi.tools.Service.ReadDataService.readExcel;
import static priv.koishi.tools.Service.ReadDataService.readFile;
import static priv.koishi.tools.Service.RenameService.buildRename;
import static priv.koishi.tools.Service.RenameService.fileRename;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.TaskUtils.bindingProgressBarTask;
import static priv.koishi.tools.Utils.TaskUtils.taskUnbind;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * @author KOISHI
 * Date:2024-10-18
 * Time:下午4:36
 */
public class FileRenameController extends ToolsProperties {

    /**
     * 要处理的文件夹路径
     */
    static String inFilePath;

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
    static String tabId = "_Re";

    /**
     * 文件名起始编号
     */
    static int defaultStartNameNum = 1;

    /**
     * 默认起始读取行
     */
    static int defaultReadRow = 0;

    /**
     * 默认起始读取列
     */
    static int defaultReadCell = 0;

    /**
     * 配置文件路径
     */
    static String configFile = "config/fileRenameConfig.properties";

    /**
     * 线程池
     */
    private final ToolsThreadPoolExecutor toolsThreadPoolExecutor = new ToolsThreadPoolExecutor();

    /**
     * 线程池实例
     */
    ExecutorService executorService = toolsThreadPoolExecutor.createNewThreadPool();

    @FXML
    private ProgressBar progressBar_Re;

    @FXML
    private TableView<FileBean> tableView_Re;

    @FXML
    private TableColumn<FileBean, Integer> id_Re;

    @FXML
    private CheckBox openDirectory_Re, addSpace_Re;

    @FXML
    private HBox renameTypeHBox_Re, behaviorHBox_Re, targetStrHBox_Re;

    @FXML
    private VBox vbox_Re, codeRenameVBox_Re, strRenameVBox_Re, excelRenameVBox_Re;

    @FXML
    private Label excelPath_Re, fileNumber_Re, inPath_Re, log_Re, typeLabel_Re;

    @FXML
    private Button fileButton_Re, clearButton_Re, exportButton_Re, reselectButton_Re, updateRenameButton_Re;

    @FXML
    private TableColumn<FileBean, String> name_Re, rename_Re, path_Re, size_Re, fileType_Re, creatDate_Re, updateDate_Re;

    @FXML
    private ChoiceBox<String> hideFileType_Re, directoryNameType_Re, renameType_Re, subCode_Re, differenceCode_Re, targetStr_Re, leftBehavior_Re, rightBehavior_Re, renameBehavior_Re;

    @FXML
    private TextField sheetOutName_Re, filterFileType_Re, readRow_Re, readCell_Re, maxRow_Re, startName_Re, nameNum_Re, startSize_Re, left_Re, right_Re, renameStr_Re, leftValue_Re, rightValue_Re, renameValue_Re, tag_Re;

    /**
     * 组件自适应宽高
     */
    public static void fileRenameAdaption(Stage stage, Scene scene) {
        //设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Re");
        table.setPrefHeight(stageHeight * 0.5);
        //设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        Node vbox = scene.lookup("#vbox_Re");
        vbox.setLayoutX(stageWidth * 0.03);
        Node tableView = scene.lookup("#tableView_Re");
        tableView.setStyle("-fx-pref-width: " + tableWidth + "px;");
        Node id = scene.lookup("#id_Re");
        id.setStyle("-fx-pref-width: " + tableWidth * 0.04 + "px;");
        Node name = scene.lookup("#name_Re");
        name.setStyle("-fx-pref-width: " + tableWidth * 0.14 + "px;");
        Node rename = scene.lookup("#rename_Re");
        rename.setStyle("-fx-pref-width: " + tableWidth * 0.14 + "px;");
        Node fileType = scene.lookup("#fileType_Re");
        fileType.setStyle("-fx-pref-width: " + tableWidth * 0.06 + "px;");
        Node path = scene.lookup("#path_Re");
        path.setStyle("-fx-pref-width: " + tableWidth * 0.22 + "px;");
        Node size = scene.lookup("#size_Re");
        size.setStyle("-fx-pref-width: " + tableWidth * 0.08 + "px;");
        Node creatDate = scene.lookup("#creatDate_Re");
        creatDate.setStyle("-fx-pref-width: " + tableWidth * 0.16 + "px;");
        Node updateDate = scene.lookup("#updateDate_Re");
        updateDate.setStyle("-fx-pref-width: " + tableWidth * 0.16 + "px;");
        Label fileNum = (Label) scene.lookup("#fileNumber_Re");
        Button removeAll = (Button) scene.lookup("#clearButton_Re");
        Button exportAll = (Button) scene.lookup("#exportButton_Re");
        Button reselect = (Button) scene.lookup("#reselectButton_Re");
        Button updateButton = (Button) scene.lookup("#updateButton_Re");
        fileNum.setPrefWidth(tableWidth - removeAll.getWidth() - exportAll.getWidth() - reselect.getWidth() - updateButton.getWidth() - 50);
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
        taskBean.setProgressBar(progressBar_Re)
                .setMassageLabel(fileNumber_Re)
                .setTableView(tableView_Re)
                .setInFileList(inFileList)
                .setTableColumn(size_Re)
                .setShowFileType(false)
                .setTabId(tabId);
        //匹配重命名规则
        matchRenameConfig(taskBean);
        //获取Task任务
        Task<Void> readFileTask = readFile(taskBean);
        //绑定带进度条的线程
        bindingProgressBarTask(readFileTask, taskBean);
        readFileTask.setOnSucceeded(event -> {
            if ("按excel模板重命名".equals(renameType_Re.getValue()) && StringUtils.isNotBlank(excelPath_Re.getText())) {
                readExcelRename();
            } else {
                //表格设置为可编辑
                tableView_Re.setEditable(true);
                rename_Re.setCellFactory((tableColumn) -> new EditingCell<>(FileBean::setRename));
                taskUnbind(taskBean);
            }
        });
        executorService.execute(readFileTask);
    }

    /**
     * 读取excel重命名模板
     */
    private void readExcelRename() {
        int readRowValue = setDefaultIntValue(readRow_Re, defaultReadRow, 0, null);
        int readCellValue = setDefaultIntValue(readCell_Re, defaultReadCell, 0, null);
        int maxRowValue = setDefaultIntValue(maxRow_Re, -1, 1, null);
        ExcelConfig excelConfig = new ExcelConfig();
        excelConfig.setSheet(sheetOutName_Re.getText())
                .setInPath(excelPath_Re.getText())
                .setReadCellNum(readCellValue)
                .setReadRowNum(readRowValue)
                .setMaxRowNum(maxRowValue);
        TaskBean<FileNumBean> taskBean = new TaskBean<>();
        taskBean.setProgressBar(progressBar_Re)
                .setReturnRenameList(true)
                .setMassageLabel(log_Re)
                .setShowFileType(false)
                .setTabId(tabId);
        //获取Task任务
        Task<List<FileNumBean>> readExcelTask = readExcel(excelConfig, taskBean);
        readExcelTask.setOnSucceeded(event -> {
            List<String> excelRenameList = readExcelTask.getValue().stream().map(FileNumBean::getGroupName).toList();
            ObservableList<FileBean> fileBeanList = tableView_Re.getItems();
            showMatchExcelData(taskBean, fileBeanList, excelRenameList);
        });
        //绑定带进度条的线程
        bindingProgressBarTask(readExcelTask, taskBean);
        //使用新线程启动
        executorService.execute(readExcelTask);
    }

    /**
     * 展示读取excel重命名数据
     */
    private void showMatchExcelData(TaskBean<?> taskBean, ObservableList<FileBean> fileBeanList, List<String> excelRenameList) {
        if (CollectionUtils.isNotEmpty(fileBeanList) && CollectionUtils.isNotEmpty(excelRenameList)) {
            matchExcelRename(fileBeanList, excelRenameList);
        }
        //表格设置为可编辑
        tableView_Re.setEditable(true);
        rename_Re.setCellFactory((tableColumn) -> new EditingCell<>(FileBean::setRename));
        taskUnbind(taskBean);
        if (taskBean.getMassageLabel() == log_Re) {
            log_Re.setText("");
        }
    }

    /**
     * 匹配excel重命名数据
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
        autoBuildTableViewData(tableView_Re, fileBeanList, tabId);
    }

    /**
     * 匹配重命名规则
     */
    private void matchRenameConfig(TaskBean<FileBean> taskBean) {
        String renameType = renameType_Re.getValue();
        switch (renameType) {
            case "按编号规则重命名": {
                matchCodeRename(taskBean);
                break;
            }
            case "按指定字符重命名": {
                matchStringRename(taskBean);
                break;
            }
        }
    }

    /**
     * 按编号规则重命名
     */
    private void matchCodeRename(TaskBean<FileBean> taskBean) {
        int startNameValue = setDefaultIntValue(startName_Re, 1, 0, null);
        int startSizeValue = setDefaultIntValue(startSize_Re, 0, 0, null);
        int nameNumValue = setDefaultIntValue(nameNum_Re, 0, 0, null);
        int tagValue = setDefaultIntValue(tag_Re, 1, 0, null);
        CodeRenameConfig codeRenameConfig = new CodeRenameConfig();
        codeRenameConfig.setDifferenceCode(differenceCode_Re.getValue())
                .setAddSpace(addSpace_Re.isSelected())
                .setSubCode(subCode_Re.getValue())
                .setStartName(startNameValue)
                .setStartSize(startSizeValue)
                .setTag(tagValue)
                .setNameNum(nameNumValue);
        taskBean.setConfiguration(codeRenameConfig);
    }

    /**
     * 按指定字符重命名
     */
    private void matchStringRename(TaskBean<FileBean> taskBean) {
        String targetStr = targetStr_Re.getValue();
        StringRenameConfig stringRenameConfig = new StringRenameConfig();
        stringRenameConfig.setTargetStr(targetStr);
        if ("指定字符串".equals(targetStr) || "指定字符位置".equals(targetStr)) {
            String renameBehavior = renameBehavior_Re.getValue();
            stringRenameConfig.setRenameValue(renameValue_Re.getText())
                    .setRenameBehavior(renameBehavior);
            if ("替换所有字符为:".equals(renameBehavior)) {
                stringRenameConfig.setRenameStr(renameStr_Re.getText());
            } else if ("处理两侧字符".equals(renameBehavior)) {
                int left = setDefaultIntValue(left_Re, 0, 0, null);
                int right = setDefaultIntValue(right_Re, 0, 0, null);
                String leftBehavior = leftBehavior_Re.getValue();
                String rightBehavior = rightBehavior_Re.getValue();
                stringRenameConfig.setLeftBehavior(leftBehavior)
                        .setRightBehavior(rightBehavior)
                        .setRight(right)
                        .setLeft(left);
                if ("插入字符串为:".equals(leftBehavior) || "替换所有字符串为:".equals(leftBehavior)) {
                    stringRenameConfig.setLeftValue(leftValue_Re.getText());
                }
                if ("插入字符串为:".equals(rightBehavior) || "替换所有字符串为:".equals(rightBehavior)) {
                    stringRenameConfig.setRightValue(rightValue_Re.getText());
                }
            }
        }
        taskBean.setConfiguration(stringRenameConfig);
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
        outFileName = prop.getProperty("outFileName");
        excelInPath = prop.getProperty("excelInPath");
        defaultReadRow = Integer.parseInt(prop.getProperty("defaultReadRow"));
        defaultReadCell = Integer.parseInt(prop.getProperty("defaultReadCell"));
        defaultStartNameNum = Integer.parseInt(prop.getProperty("defaultStartNameNum"));
        input.close();
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() throws IOException {
        getConfig();
        vbox_Re.getChildren().remove(strRenameVBox_Re);
        vbox_Re.getChildren().remove(excelRenameVBox_Re);
        String optionTip = """
                插入：在匹配的字符位置插入所填写的字符串
                替换：将匹配的字符串替换为所填写的字符串
                删除：只删除指定位置的字符
                移除：移除指定位置左侧或右侧所有字符串""";
        addToolTip(rightBehavior_Re, optionTip);
        addToolTip(leftBehavior_Re, optionTip);
        addToolTip(startSize_Re, "只能填自然数，0为不限制编号位数，不填默认为0");
        addToolTip(renameStr_Re, "填写后会将匹配到的字符串替换为所填写的字符串");
        addToolTip(sheetOutName_Re, "须填与excel模板相同的表名才能正常读取模板");
        addToolTip(renameValue_Re, "填写后会根据其他配置项处理文件名中所匹配的字符");
        addToolTip(nameNum_Re, "只能填自然数，0为不使用分隔符进行分组重命名，不填默认为0");
        addToolTip(startName_Re, "只能填自然数，不填默认为 " + defaultStartNameNum);
        addToolTip(rightValue_Re, "将所填字符根据选项插入或替换目标字符左侧所匹配的字符");
        addToolTip(tag_Re, "只能填自然数，不填默认为1，会根据所填值设置相同文件名起始尾缀");
        addToolTip(leftValue_Re, "将所填字符根据选项插入或替换目标字符右侧所匹配的字符");
        addToolTip(maxRow_Re, "只能填正整数，不填默认不限制，会读取到有数据的最后一行，最小值为1");
        addToolTip(addSpace_Re, "win系统自动重命名规则为：文件名 + 空格 + 英文括号包裹的阿拉伯数字编号");
        addToolTip(filterFileType_Re, "填写后只会识别所填写的后缀名文件，多个文件后缀名用空格隔开，后缀名需带 '.'");
        addToolTip(left_Re, "只能填自然数，不填 0 默认匹配目标字符串左侧所有字符，填写后匹配目标字符串左侧所填写个数的单个字符");
        addToolTip(right_Re, "只能填自然数，不填为 0 默认匹配目标字符串右侧所有字符，填写后匹配目标字符串右侧所填写个数的单个字符");
        addToolTip(readRow_Re, "只能填自然数，不填默认为 " + defaultReadRow + " 从第 " + (defaultReadRow + 1) + " 行读取");
        addToolTip(readCell_Re, "只能填自然数，不填默认为 " + defaultReadCell + " ，从第 " + (defaultReadCell + 1) + " 列读取");
        //设置javafx单元格宽度
        id_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.04));
        name_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.14));
        rename_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.14));
        fileType_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.06));
        path_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.22));
        size_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.08));
        creatDate_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.16));
        updateDate_Re.prefWidthProperty().bind(tableView_Re.widthProperty().multiply(0.16));
    }

    /**
     * 选择文件夹按钮功能
     */
    @FXML
    private void inDirectoryButton(ActionEvent actionEvent) throws Exception {
        getConfig();
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Re);
        // 显示文件选择器
        File selectedFile = creatDirectoryChooser(actionEvent, inFilePath, "选择文件夹");
        FileConfig fileConfig = new FileConfig();
        fileConfig.setShowDirectoryName(directoryNameType_Re.getValue())
                .setShowHideFile(hideFileType_Re.getValue())
                .setFilterExtensionList(filterExtensionList)
                .setInFile(selectedFile);
        if (selectedFile != null) {
            String selectedFilePath = selectedFile.getAbsolutePath();
            updatePath(configFile, "inFilePath", selectedFilePath);
            inPath_Re.setText(selectedFilePath);
            addToolTip(inPath_Re, selectedFilePath);
            //读取数据
            List<File> inFileList = readAllFiles(fileConfig);
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
        addToolTip(inPath_Re, filePath);
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
        ObservableList<FileBean> nullData = FXCollections.observableArrayList(new ArrayList<>());
        tableView_Re.setItems(nullData);
        updateLabel(fileNumber_Re, "列表为空");
        updateLabel(log_Re, "");
        System.gc();
    }

    /**
     * 开始重命名按钮
     */
    @FXML
    private void renameAll() throws Exception {
        ObservableList<FileBean> fileBeans = tableView_Re.getItems();
        if (CollectionUtils.isEmpty(fileBeans)) {
            throw new Exception("要读取的文件列表为空，需要选择一个有文件的文件夹");
        }
        Map<String, List<FileBean>> fileBeanMap = fileBeans.stream().collect(Collectors.groupingBy(FileBean::getRename));
        List<String> errList = new ArrayList<>();
        fileBeanMap.forEach((rename, fileBeanList) -> {
            if (fileBeanList.size() > 1) {
                List<String> ids = new ArrayList<>();
                fileBeanList.forEach(fileBean -> {
                    int id = fileBean.getId();
                    ids.add(String.valueOf(id));
                });
                String errString = "序号为： " + String.join("、", ids) + " 的文件重命名重复";
                errList.add(errString);
            }
        });
        String errStrings = String.join("\n", errList);
        if (StringUtils.isNotBlank(errStrings)) {
            Alert alert = creatErrorDialog(errStrings);
            alert.setHeaderText("文件重命名重复");
            // 展示弹窗
            alert.showAndWait();
        } else {
            TaskBean<FileBean> taskBean = new TaskBean<>();
            taskBean.setProgressBar(progressBar_Re)
                    .setMassageLabel(log_Re)
                    .setBeanList(fileBeans)
                    .setTabId(tabId);
            //匹配重命名规则
            matchRenameConfig(taskBean);
            //获取Task任务
            Task<String> renameTask = fileRename(taskBean);
            //绑定带进度条的线程
            bindingProgressBarTask(renameTask, taskBean);
            renameTask.setOnSucceeded(event -> {
                taskUnbind(taskBean);
                if (openDirectory_Re.isSelected()) {
                    try {
                        openFile(renameTask.getValue());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                taskBean.getMassageLabel().setTextFill(Color.GREEN);
            });
            executorService.execute(renameTask);
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
            excelPath_Re.setText(excelInPath);
            addToolTip(excelPath_Re, excelInPath);
            readExcelRename();
        }
    }

    /**
     * 鼠标悬留提示输入的导出excel表名称
     */
    @FXML
    private void sheetHandleKeyTyped() {
        addValueToolTip(sheetOutName_Re, "须填与excel模板相同的表名才能正常读取模板");
    }

    /**
     * 鼠标悬留提示输入的文件名起始编号
     */
    @FXML
    private void startNameHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(startName_Re, 0, null, event);
        addValueToolTip(startName_Re, "只能填自然数，不填默认为 " + defaultStartNameNum);
    }

    /**
     * 鼠标悬留提示输入的文件名起始编号位数
     */
    @FXML
    private void startSizeHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(startSize_Re, 0, null, event);
        addValueToolTip(startSize_Re, "只能填自然数，0为不限制编号位数，不填默认为0");
    }

    /**
     * 鼠标悬留提示输入的相同编号文件数量
     */
    @FXML
    private void nameNumHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(nameNum_Re, 0, null, event);
        addValueToolTip(nameNum_Re, "只能填自然数，0为不使用分隔符进行分组重命名，不填默认为0");
    }

    /**
     * 鼠标悬留提示输入的需要识别的文件后缀名
     */
    @FXML
    private void filterHandleKeyTyped() {
        addValueToolTip(filterFileType_Re, "填写后只会识别所填写的后缀名文件，多个文件后缀名用空格隔开，后缀名需带 '.'");
    }

    /**
     * 重新查询按钮
     */
    @FXML
    private void reselect() throws Exception {
        String inFilePath = inPath_Re.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception("要查询的文件夹位置为空，需要先设置要查询的文件夹位置再继续");
        }
        updateLabel(log_Re, "");
        FileConfig fileConfig = new FileConfig();
        fileConfig.setFilterExtensionList(getFilterExtensionList(filterFileType_Re))
                .setShowDirectoryName(directoryNameType_Re.getValue())
                .setShowHideFile(hideFileType_Re.getValue())
                .setInFile(new File(inFilePath));
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
     * 限制读取起始行只能输入自然数
     */
    @FXML
    private void readRowHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(readRow_Re, 0, null, event);
        addValueToolTip(readRow_Re, "只能填自然数，不填默认为 " + defaultReadRow + " 从第 " + (defaultReadRow + 1) + " 行读取");
    }

    /**
     * 限制读取起始列只能输入自然数
     */
    @FXML
    private void readCellHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(readCell_Re, 0, null, event);
        addValueToolTip(readCell_Re, "只能填自然数，不填默认为 " + defaultReadCell + " 从第 " + (defaultReadCell + 1) + " 列读取");
    }

    /**
     * 限制读取最大行数只能输入正整数
     */
    @FXML
    private void maxRowHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(maxRow_Re, 1, null, event);
        addValueToolTip(maxRow_Re, "只能填正整数，不填默认不限制，会读取到有数据的最后一行，最小值为1");
    }

    /**
     * 根据区分编码类型更新重命名分隔符设置下拉框选项
     */
    @FXML
    private void differenceCodeAction() {
        String item = differenceCode_Re.getValue();
        switch (item) {
            case "阿拉伯数字：123": {
                updateSelectItems(addSpace_Re, subCode_Re, subCodeArabicNumItems);
                break;
            }
            case "中文数字：一二三": {
                updateSelectItems(addSpace_Re, subCode_Re, subCodeChineseNumItems);
                break;
            }
            case "小写英文字母：abc": {
                updateSelectItems(addSpace_Re, subCode_Re, subCodeLowercaseItems);
                break;
            }
            case "大小英文字母：ABC": {
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
        String item = renameType_Re.getValue();
        int index = vbox_Re.getChildren().indexOf(renameTypeHBox_Re) + 1;
        switch (item) {
            case "按编号规则重命名": {
                vbox_Re.getChildren().add(index, codeRenameVBox_Re);
                vbox_Re.getChildren().remove(strRenameVBox_Re);
                vbox_Re.getChildren().remove(excelRenameVBox_Re);
                break;
            }
            case "按指定字符重命名": {
                vbox_Re.getChildren().remove(codeRenameVBox_Re);
                vbox_Re.getChildren().add(index, strRenameVBox_Re);
                vbox_Re.getChildren().remove(excelRenameVBox_Re);
                targetStrAction();
                break;
            }
            case "按excel模板重命名": {
                vbox_Re.getChildren().remove(codeRenameVBox_Re);
                vbox_Re.getChildren().remove(strRenameVBox_Re);
                vbox_Re.getChildren().add(index, excelRenameVBox_Re);
                break;
            }
        }
    }

    /**
     * 监控匹配字符规则选项
     */
    @FXML
    private void targetStrAction() {
        String item = targetStr_Re.getValue();
        switch (item) {
            case "指定字符串": {
                typeLabel_Re.setText("要匹配的字符串:");
                renameValue_Re.setText("");
                addValueToolTip(renameValue_Re, "填写后会根据其他配置项处理文件名中所匹配的字符");
                renameBehavior_Re.getItems().remove("处理两侧字符");
                renameBehavior_Re.getItems().add("处理两侧字符");
                behaviorAction();
                break;
            }
            case "指定字符位置": {
                typeLabel_Re.setText("要匹配的字符位置:");
                renameValue_Re.setText("");
                addValueToolTip(renameValue_Re, "填写后会根据其他配置项处理文件名中所匹配的字符");
                renameBehavior_Re.getItems().remove("处理两侧字符");
                renameBehavior_Re.setValue(renameBehavior_Re.getItems().getFirst());
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
        String item = renameBehavior_Re.getValue();
        targetStrHBox_Re.setVisible(true);
        switch (item) {
            case "替换所有字符为:": {
                renameStr_Re.setVisible(true);
                behaviorHBox_Re.setVisible(false);
                break;
            }
            case "移除指定字符": {
                renameStr_Re.setVisible(false);
                behaviorHBox_Re.setVisible(false);
                break;
            }
            case "处理两侧字符": {
                renameStr_Re.setVisible(false);
                behaviorHBox_Re.setVisible(true);
                break;
            }
        }
    }

    /**
     * 监控指定位置之前处理方式下拉框
     */
    @FXML
    private void leftBehaviorAction() {
        String item = leftBehavior_Re.getValue();
        leftValue_Re.setVisible("插入字符串为:".equals(item) || "替换所有字符串为:".equals(item));
    }

    /**
     * 监控指定位置之后处理方式下拉框
     */
    @FXML
    public void rightBehaviorAction() {
        String item = rightBehavior_Re.getValue();
        rightValue_Re.setVisible("插入字符串为:".equals(item) || "替换所有字符串为:".equals(item));
    }

    /**
     * 要匹配的字符串鼠标悬停提示
     */
    @FXML
    private void renameValueHandleKeyTyped(KeyEvent event) {
        //这个输入框只有在输入指定字符位置时才限制输入范围
        if ("指定字符位置".equals(targetStr_Re.getValue())) {
            integerRangeTextField(renameValue_Re, 0, null, event);
        }
        addValueToolTip(renameValue_Re, "填写后会根据其他配置项处理文件名中所匹配的字符");
    }

    /**
     * 指定字符串所替换的字符串鼠标悬停提示
     */
    @FXML
    private void renameStrHandleKeyTyped() {
        addValueToolTip(renameStr_Re, "填写后会将匹配到的字符串替换为所填写的字符串");
    }

    /**
     * 限制向前匹配字符位置输入框内容
     */
    @FXML
    private void leftHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(left_Re, 0, null, event);
        addValueToolTip(left_Re, "只能填自然数，不填默认匹配目标字符串左侧所有字符，填写后匹配目标字符串左侧所填写个数的单个字符");
    }

    /**
     * 限制向后匹配字符位置输入框内容
     */
    @FXML
    private void rightHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(right_Re, 0, null, event);
        addValueToolTip(right_Re, "只能填自然数，不填默认匹配目标字符串右侧所有字符，填写后匹配目标字符串右侧所填写个数的单个字符");
    }

    /**
     * 给目标字符串左侧替换或插入输入框添加鼠标悬停提示
     */
    @FXML
    private void rightValueHandleKeyTyped() {
        addValueToolTip(rightValue_Re, "将所填字符根据选项插入或替换目标字符左侧所匹配的字符");
    }

    /**
     * 给目标字符串右侧替换或插入输入框添加鼠标悬停提示
     */
    @FXML
    private void leftValueHandleKeyTyped() {
        addValueToolTip(leftValue_Re, "将所填字符根据选项插入或替换目标字符右侧所匹配的字符");
    }

    /**
     * 给相同编号文件起始尾缀输入框添加鼠标悬停提示
     */
    @FXML
    private void tagHandleKeyTyped(KeyEvent event) {
        integerRangeTextField(tag_Re, 0, null, event);
        addValueToolTip(tag_Re, "只能填自然数，不填默认为1，会根据所填值设置相同文件名起始尾缀");
    }

    /**
     * 更新重命名按钮
     */
    @FXML
    private void updateRename() throws Exception {
        ObservableList<FileBean> fileBeans = tableView_Re.getItems();
        if (CollectionUtils.isEmpty(fileBeans)) {
            throw new Exception("要读取的文件列表为空，需要选择一个有文件的文件夹");
        }
        if ("按excel模板重命名".equals(renameType_Re.getValue()) && StringUtils.isNotBlank(excelPath_Re.getText())) {
            readExcelRename();
        } else if (!"按excel模板重命名".equals(renameType_Re.getValue())) {
            TaskBean<FileBean> taskBean = new TaskBean<>();
            //匹配重命名规则
            matchRenameConfig(taskBean);
            Configuration configuration = taskBean.getConfiguration();
            CodeRenameConfig codeRenameConfig = null;
            StringRenameConfig stringRenameConfig = null;
            int startName = -1;
            int tag = -1;
            int nameNum = 1;
            if (configuration != null) {
                if (configuration.getClass() == CodeRenameConfig.class) {
                    codeRenameConfig = (CodeRenameConfig) configuration;
                    startName = codeRenameConfig.getStartName();
                    tag = codeRenameConfig.getTag();
                } else if (configuration.getClass() == StringRenameConfig.class) {
                    stringRenameConfig = (StringRenameConfig) configuration;
                }
            }
            for (FileBean fileBean : fileBeans) {
                //组装文件重命名数据
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
            autoBuildTableViewData(tableView_Re, fileBeans, tabId);
            //表格设置为可编辑
            tableView_Re.setEditable(true);
            rename_Re.setCellFactory((tableColumn) -> new EditingCell<>(FileBean::setRename));
        }
        updateLabel(log_Re, "");
    }
}
