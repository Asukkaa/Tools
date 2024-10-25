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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.ExcelConfigBean;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.FileConfigBean;
import priv.koishi.tools.Bean.TaskBean;

import java.io.*;
import java.util.*;

import static priv.koishi.tools.Enum.SelectItemsEnums.*;
import static priv.koishi.tools.Service.FileNameToExcelService.buildFileNameExcel;
import static priv.koishi.tools.Service.ReadDataService.readFile;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.isInIntegerRange;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * @author KOISHI
 * Date:2024-10-18
 * Time:下午4:36
 */
public class FileRenameExcelController extends Properties {

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
     * 配置文件路径
     */
    static String configFile = "config/fileRenameConfig.properties";

    @FXML
    private ProgressBar progressBar_Re;

    @FXML
    private VBox vbox_Re;

    @FXML
    private HBox configRename_Re, excelRename_Re;

    @FXML
    private TableView<FileBean> tableView_Re;

    @FXML
    private TableColumn<FileBean, Integer> id_Re;

    @FXML
    private TableColumn<FileBean, String> name_Re, rename_Re, path_Re, size_Re, fileType_Re, creatDate_Re, updateDate_Re;

    @FXML
    private Label excelPath_Re, fileNumber_Re, inPath_Re, log_Re;

    @FXML
    private CheckBox openDirectory_Re, addSpace_Re;

    @FXML
    private ChoiceBox<String> hideFileType_Re, directoryNameType_Re, renameType_Re, subCode_Re, differenceCode_Re;

    @FXML
    private TextField sheetOutName_Re, filterFileType_Re, readRow_Re, readCell_Re, maxRow_Re, startName_Re, nameNum_Re, startSize_Re;

    @FXML
    private Button fileButton_Re, clearButton_Re, exportButton_Re, reSelectButton_Re, removeExcelButton_Re;

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
        Button reSelect = (Button) scene.lookup("#reSelectButton_Re");
        fileNum.setPrefWidth(tableWidth - removeAll.getWidth() - exportAll.getWidth() - reSelect.getWidth() - 40);
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
        taskBean.setShowFileType(false)
                .setProgressBar(progressBar_Re)
                .setMassageLabel(fileNumber_Re)
                .setTableColumn(size_Re)
                .setTableView(tableView_Re)
                .setInFileList(inFileList)
                .setTabId(tabId);
        //获取Task任务
        Task<Void> readFileTask = readFile(taskBean);
        //启动带进度条的线程
        startProgressBarTask(readFileTask, taskBean);
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
    }

    /**
     * 重写Properties的load方法，更换配置文件中的‘\’为‘/’
     */
    @Override
    public synchronized void load(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        while (true) {
            //缓冲流以行读取数据
            String line = bufferedReader.readLine();
            if (Objects.isNull(line)) {
                break;
            }
            //注意: properties属性类文件存在第一个隐藏字符,需要删除掉，否则第一个数据以key查找不存在
            if (line.startsWith("\uFEFF")) {
                line = line.substring(1);
            }
            //如果是#注释内容，则不做操作
            if (!line.startsWith("#") && !line.isEmpty()) {
                //将读取的数据格式为’=‘分割,以key,Value方式存储properties属性类文件数据
                String[] split = line.split("=");
                //由于‘\’在Java中表示转义字符，需要将读取的路径进行转换为‘/’符号,这里“\\\\”代表一个‘\’
                put(split[0], split[1].replaceAll("\\\\", "/"));
            }
        }
    }

    /**
     * 界面初始化
     */
    @FXML
    private void initialize() {
        addToolTip(filterFileType_Re, "填写后只会识别所填写的后缀名文件，多个文件后缀名用空格隔开，后缀名需带 '.'");
        addToolTip(sheetOutName_Re, "须填与excel模板相同的表名才能正常读取模板");
        addToolTip(removeExcelButton_Re, "删除excel模板路径");
        addToolTip(startSize_Re, "只能填数字，0为不限制编号位数，不填默认为0");
        addToolTip(startName_Re, "只能填数字，不填默认为0");
        addToolTip(nameNum_Re, "只能填数字，0为不使用分隔符进行分组重命名，不填默认为0");
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
        FileConfigBean fileConfigBean = new FileConfigBean();
        fileConfigBean.setInFile(selectedFile)
                .setShowHideFile(hideFileType_Re.getValue())
                .setShowDirectoryName(directoryNameType_Re.getValue())
                .setFilterExtensionList(filterExtensionList);
        if (selectedFile != null) {
            String selectedFilePath = selectedFile.getAbsolutePath();
            updatePath(configFile, "inFilePath", selectedFilePath);
            removeAll();
            inPath_Re.setText(selectedFilePath);
            addToolTip(inPath_Re, selectedFilePath);
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
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Re);
        File file = files.getFirst();
        FileConfigBean fileConfigBean = new FileConfigBean();
        fileConfigBean.setInFile(file)
                .setShowHideFile(hideFileType_Re.getValue())
                .setShowDirectoryName(directoryNameType_Re.getValue())
                .setFilterExtensionList(filterExtensionList);
        List<File> inFileList = readAllFiles(fileConfigBean);
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
        List<FileBean> nullFileBeans = new ArrayList<>();
        ObservableList<FileBean> nullData = FXCollections.observableArrayList(nullFileBeans);
        tableView_Re.setItems(nullData);
        // 解除绑定，设置文本，然后重新绑定
        fileNumber_Re.textProperty().unbind();
        fileNumber_Re.setText("列表为空");
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
        String sheetName = setDefaultStrValue(sheetOutName_Re, "Sheet1");
        List<String> names = new ArrayList<>();
        fileBeans.forEach(fileBean -> names.add(fileBean.getName()));
        log_Re.setTextFill(Color.BLACK);
        log_Re.setText("");
        ExcelConfigBean excelConfigBean = new ExcelConfigBean();
        excelConfigBean.setInPath(excelPath_Re.getText())
                .setLogLabel(log_Re)
                .setSheet(sheetName);
        XSSFWorkbook xssfWorkbook = buildFileNameExcel(names, excelConfigBean);
        String excelPath = saveExcel(xssfWorkbook, excelConfigBean);
        if (openDirectory_Re.isSelected()) {
            openFile(getFileMkdir(new File(excelPath)));
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
            removeExcelButton_Re.setVisible(true);
        }
    }

    /**
     * 鼠标悬留提示输入的导出excel表名称
     */
   @FXML
    private void sheetHandleKeyTyped() {
        aadValueToolTip(sheetOutName_Re, "须填与excel模板相同的表名才能正常读取模板");
    }

    /**
     * 鼠标悬留提示输入的文件名起始编号
     */
    @FXML
    private void startNameHandleKeyTyped() {
        if (!isInIntegerRange(startName_Re.getText(), 0, null)) {
            startName_Re.setText("");
        }
        aadValueToolTip(startName_Re, "只能填数字，不填默认为0");
    }

    /**
     * 鼠标悬留提示输入的文件名起始编号位数
     */
    @FXML
    private void startSizeHandleKeyTyped() {
        if (!isInIntegerRange(startSize_Re.getText(), 0, null)) {
            startSize_Re.setText("");
        }
        aadValueToolTip(startSize_Re, "只能填数字，0为不限制编号位数，不填默认为0");
    }

    /**
     * 鼠标悬留提示输入的相同编号文件数量
     */
    @FXML
    private void nameNumHandleKeyTyped() {
        if (!isInIntegerRange(nameNum_Re.getText(), 0, null)) {
            nameNum_Re.setText("");
        }
        aadValueToolTip(nameNum_Re, "只能填数字，0为不使用分隔符进行分组重命名，不填默认为0");
    }

    /**
     * 鼠标悬留提示输入的需要识别的文件后缀名
     */
    @FXML
    private void filterHandleKeyTyped() {
        aadValueToolTip(filterFileType_Re, "填写后只会识别所填写的后缀名文件，多个文件后缀名用空格隔开，后缀名需带 '.'");
    }

    /**
     * 重新查询按钮
     */
    @FXML
    private void reSelect() throws Exception {
        String inFilePath = inPath_Re.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception("要查询的文件夹位置为空，需要先设置要查询的文件夹位置再继续");
        }
        FileConfigBean fileConfigBean = new FileConfigBean();
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Re);
        fileConfigBean.setInFile(new File(inFilePath))
                .setShowHideFile(hideFileType_Re.getValue())
                .setShowDirectoryName(directoryNameType_Re.getValue())
                .setFilterExtensionList(filterExtensionList);
        List<File> inFileList = readAllFiles(fileConfigBean);
        addInData(inFileList);
    }

    /**
     * 是否向分隔符左侧添加一个空格选项监听
     */
    @FXML
    private void handleCheckBoxAction() {
        differenceCodeAction();
    }

    /**
     * 清空excel模板路径按钮
     */
    @FXML
    private void removeExcelPath() {
        excelPath_Re.setText("");
        excelPath_Re.setTooltip(null);
        removeExcelButton_Re.setVisible(false);
    }

    /**
     * 限制读取起始行只能输入自然数
     */
    @FXML
    private void readRowHandleKeyTyped() {
        if (!isInIntegerRange(readRow_Re.getText(), 0, null)) {
            readRow_Re.setText("");
        }
        aadValueToolTip(readRow_Re, "只能填数字，不填默认为0，从第一行读取");
    }

    /**
     * 限制读取起始列只能输入自然数
     */
    @FXML
    private void readCellHandleKeyTyped() {
        if (!isInIntegerRange(readCell_Re.getText(), 0, null)) {
            readCell_Re.setText("");
        }
        aadValueToolTip(readCell_Re, "只能填数字，不填默认为0，从第一列读取");
    }

    /**
     * 限制读取最大行数只能输入正整数
     */
    @FXML
    private void maxRowHandleKeyTyped() {
        if (!isInIntegerRange(maxRow_Re.getText(), 1, null)) {
            maxRow_Re.setText("");
        }
        aadValueToolTip(maxRow_Re, "只能填数字，不填默认不限制，会读取到有数据的最后一行，最小值为1");
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

}
