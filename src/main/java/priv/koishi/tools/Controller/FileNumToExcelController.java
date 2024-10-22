package priv.koishi.tools.Controller;

import javafx.collections.ObservableList;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.ExcelConfigBean;
import priv.koishi.tools.Bean.FileConfigBean;
import priv.koishi.tools.Bean.FileNumBean;

import java.io.*;
import java.util.*;

import static priv.koishi.tools.Service.FileNumToExcelService.buildNameGroupNumExcel;
import static priv.koishi.tools.Service.FileNumToExcelService.readExcel;
import static priv.koishi.tools.Utils.CommonUtils.isInIntegerRange;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * @author KOISHI
 * Date:2024-10-08
 * Time:下午3:29
 */
public class FileNumToExcelController extends Properties {

    /**
     * 要处理的文件夹路径
     */
    static String inFilePath;

    /**
     * 要处理的文件夹文件
     */
    static List<File> inFileList;

    /**
     * 列表中的数据
     */
    static List<FileNumBean> fileNumBeanList;

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
     * 配置文件路径
     */
    static String configFile = "fileNumToExcelConfig.properties";

    @FXML
    private VBox vbox_Num;

    @FXML
    private TableView<FileNumBean> tableView_Num;

    @FXML
    private TableColumn<FileNumBean, String> fileName_Num, groupId_Num;

    @FXML
    private TableColumn<FileNumBean, Integer> groupName_Num, groupNumber_Num;

    @FXML
    private CheckBox recursion_Num, openDirectory_Num, openFile_Num, showFileType_Num;

    @FXML
    private Button fileButton_Num, clearButton_Num, exportButton_Num, reSelectButton_Num;

    @FXML
    private ChoiceBox<String> excelType_Num, hideFileType_Num, directoryNameType_Num, exportType_Num;

    @FXML
    private Label outPath_Num, excelPath_Num, fileNumber_Num, inPath_Num, log_Num, exportTypeLabel_Num;

    @FXML
    private TextField excelName_Num, sheetOutName_Num, startRow_Num, startCell_Num, filterFileType_Num, subCode_Num, readRow_Num, readCell_Num, maxRow_Num;

    /**
     * 组件自适应宽高
     */
    public static void fileNumToExcelAdaption(Stage stage, Scene scene) {
        //设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Num");
        table.setPrefHeight(stageHeight * 0.5);
        //设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.94;
        Node fileNumVbox = scene.lookup("#vbox_Num");
        fileNumVbox.setLayoutX(stageWidth * 0.03);
        Node tableView = scene.lookup("#tableView_Num");
        tableView.setStyle("-fx-pref-width: " + tableWidth + "px;");
        Node groupId = scene.lookup("#groupId_Num");
        groupId.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node groupNameNum = scene.lookup("#groupName_Num");
        groupNameNum.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node groupNumberNum = scene.lookup("#groupNumber_Num");
        groupNumberNum.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node fileNameNum = scene.lookup("#fileName_Num");
        fileNameNum.setStyle("-fx-pref-width: " + tableWidth * 0.7 + "px;");
        Label fileNum = (Label) scene.lookup("#fileNumber_Num");
        Button removeAll = (Button) scene.lookup("#clearButton_Num");
        Button exportAll = (Button) scene.lookup("#exportButton_Num");
        Label exportTypeLabel = (Label) scene.lookup("#exportTypeLabel_Num");
        ChoiceBox<?> exportType = (ChoiceBox<?>) scene.lookup("#exportType_Num");
        Button reSelect = (Button) scene.lookup("#reSelectButton_Num");
        fileNum.setPrefWidth(tableWidth - removeAll.getWidth() - exportAll.getWidth() - exportTypeLabel.getWidth()
                - exportType.getWidth() - reSelect.getWidth() - 60);
    }

    /**
     * 读取文件数据
     */
    private void addInFile(File selectedFile, List<String> filterExtensionList) throws Exception {
        FileConfigBean fileConfigBean = new FileConfigBean();
        fileConfigBean.setInFile(selectedFile)
                .setShowHideFile(hideFileType_Num.getValue())
                .setShowDirectoryName(directoryNameType_Num.getValue())
                .setShowFileType(showFileType_Num.isSelected())
                .setRecursion(recursion_Num.isSelected())
                .setFilterExtensionList(filterExtensionList);
        inFileList = readAllFiles(fileConfigBean);
        //列表中有excel分组后再匹配数据
        ObservableList<FileNumBean> fileNumList = tableView_Num.getItems();
        if (fileNumList != null && !fileNumList.isEmpty()) {
            matchGroupData(fileNumList, inFileList, subCode_Num, showFileType_Num);
            showData(fileNumList);
        }
    }

    /**
     * 添加数据渲染列表
     */
    private void addInData() throws Exception {
        removeAll();
        //组装数据
        int readRowValue = setDefaultIntValue(readRow_Num, 0, 0, null);
        int readCellValue = setDefaultIntValue(readCell_Num, 0, 0, null);
        int maxRowValue = setDefaultIntValue(maxRow_Num, -1, 1, null);
        ExcelConfigBean excelConfigBean = new ExcelConfigBean();
        excelConfigBean.setSheet(sheetOutName_Num.getText())
                .setInPath(excelPath_Num.getText())
                .setReadCellNum(readCellValue)
                .setReadRowNum(readRowValue)
                .setMaxRowNum(maxRowValue);
        //读取excel分组信息
        List<FileNumBean> fileNumBeans = readExcel(excelConfigBean);
        //已经读取文件后再匹配数据
        if (inFileList != null && !inFileList.isEmpty()) {
            matchGroupData(fileNumBeans, inFileList, subCode_Num, showFileType_Num);
        }
        //匹配数据
        showData(fileNumBeans);
        fileNumber_Num.setText("共有 " + fileNumBeans.size() + " 组数据");
        //设置javafx单元格宽度
        groupId_Num.prefWidthProperty().bind(tableView_Num.widthProperty().multiply(0.1));
        groupName_Num.prefWidthProperty().bind(tableView_Num.widthProperty().multiply(0.1));
        groupNumber_Num.prefWidthProperty().bind(tableView_Num.widthProperty().multiply(0.1));
        fileName_Num.prefWidthProperty().bind(tableView_Num.widthProperty().multiply(0.7));
    }

    /**
     * 匹配数据
     */
    private void showData(List<FileNumBean> fileBeans) throws Exception {
        if (fileBeans.isEmpty()) {
            throw new Exception("未查询到符合条件的数据，需修改查询条件后再继续");
        }
        autoBuildTableViewData(tableView_Num, fileBeans, "_Num");
        fileNumBeanList = fileBeans;
    }

    /**
     * 读取配置文件
     */
    private static void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = new FileInputStream(configFile);
        // 加载properties文件
        prop.load(input);
        // 根据key读取value
        inFilePath = prop.getProperty("inFilePath");
        outFilePath = prop.getProperty("outFilePath");
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
        addToolTip(filterFileType_Num, "填写后只会识别所填写的后缀名文件，多个文件后缀名用空格隔开，后缀名需带 '.'");
        addNumImgToolTip(recursion_Num, subCode_Num, excelName_Num, sheetOutName_Num, startRow_Num, startCell_Num, readCell_Num, readRow_Num, maxRow_Num);
    }

    /**
     * 选择文件夹按钮功能
     */
    @FXML
    private void inDirectoryButton(ActionEvent actionEvent) throws Exception {
        getConfig();
        List<String> filterExtensionList = getFilterExtensionList(filterFileType_Num);
        // 显示文件选择器
        File selectedFile = creatDirectoryChooser(actionEvent, inFilePath, "选择文件夹");
        if (selectedFile != null) {
            String selectedFilePath = selectedFile.getPath();
            updatePath(configFile, "inFilePath", selectedFilePath);
            inPath_Num.setText(selectedFilePath);
            addToolTip(inPath_Num, selectedFilePath);
            //读取文件数据
            addInFile(selectedFile, filterExtensionList);
        }
    }

    /**
     * 拖拽释放行为
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) throws Exception {
        List<File> files = dragEvent.getDragboard().getFiles();
        File file = files.getFirst();
        excelPath_Num.setText(file.getPath());
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
        removeNumImgAll(tableView_Num, fileNumber_Num, log_Num, fileNumBeanList);
    }

    /**
     * 导出excel按钮
     */
    @FXML
    private void exportAll() throws Exception {
        reSelect();
        String outFilePath = outPath_Num.getText();
        String inFilePath = excelPath_Num.getText();
        String subCode = subCode_Num.getText();
        String inDirectory = inPath_Num.getText();
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
        int startRowValue = setDefaultIntValue(startRow_Num, 0, 0, null);
        int startCellValue = setDefaultIntValue(startCell_Num, 0, 0, null);
        String excelNameValue = setDefaultFileName(excelName_Num, "NameList");
        String sheetName = setDefaultStrValue(sheetOutName_Num, "Sheet1");
        ExcelConfigBean excelConfigBean = new ExcelConfigBean();
        excelConfigBean.setOutExcelExtension(excelType_Num.getValue())
                .setExportType(exportType_Num.getValue())
                .setInPath(excelPath_Num.getText())
                .setStartCellNum(startCellValue)
                .setStartRowNum(startRowValue)
                .setOutName(excelNameValue)
                .setOutPath(outFilePath)
                .setLogLabel(log_Num)
                .setSubCode(subCode)
                .setSheet(sheetName);
        fileNumBeanList.sort(Comparator.comparingInt(FileNumBean::getGroupId));
        XSSFWorkbook xssfWorkbook = buildNameGroupNumExcel(fileNumBeanList, excelConfigBean);
        String excelPath = saveExcel(xssfWorkbook, excelConfigBean);
        if (openDirectory_Num.isSelected()) {
            openFile(getFileMkdir(new File(excelPath)));
        }
        if (openFile_Num.isSelected()) {
            openFile(excelPath);
        }
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
            outPath_Num.setText(outFilePath);
            addToolTip(outPath_Num, outFilePath);
            String inFilePath = excelPath_Num.getText();
            if (StringUtils.isNotEmpty(inFilePath)) {
                reSelect();
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
            excelPath_Num.setText(excelInPath);
            addToolTip(excelPath_Num, excelInPath);
            addInData();
        }
    }

    /**
     * 限制导出预留行只能输入自然数
     */
    @FXML
    private void rowHandleKeyTyped() {
        if (!isInIntegerRange(startRow_Num.getText(), 0, null)) {
            startRow_Num.setText("");
        }
        aadValueToolTip(startRow_Num, "只能填数字，不填默认为0，不预留列");
    }

    /**
     * 限制导出预留列只能输入自然数
     */
    @FXML
    private void cellHandleKeyTyped() {
        if (!isInIntegerRange(startCell_Num.getText(), 0, null)) {
            startCell_Num.setText("");
        }
        aadValueToolTip(startCell_Num, "只能填数字，不填默认为0，不预留行");
    }

    /**
     * 鼠标悬留提示输入的导出excel文件名称
     */
    @FXML
    private void nameHandleKeyTyped() {
        aadValueToolTip(excelName_Num, "如果导出地址和名称与模板一样则会覆盖模板excel文件");
    }

    /**
     * 鼠标悬留提示输入的导出excel表名称
     */
    @FXML
    private void sheetHandleKeyTyped() {
        aadValueToolTip(sheetOutName_Num, "须填与excel模板相同的表名才能正常统计");
    }

    /**
     * 鼠标悬留提示输入的需要识别的文件后缀名
     */
    @FXML
    private void filterHandleKeyTyped() {
        aadValueToolTip(filterFileType_Num, "填写后只会识别所填写的后缀名文件，多个文件后缀名用空格隔开，后缀名需带 '.'");
    }

    /**
     * 鼠标悬留提示输入的文件名称分割符
     */
    @FXML
    private void subHandleKeyTyped() {
        aadValueToolTip(subCode_Num, "填写后会按所填写的字符串来分割文件名称，按照分割后的文件名称左侧字符串进行分组");
    }

    /**
     * 限制读取起始行只能输入自然数
     */
    @FXML
    private void readRowHandleKeyTyped() {
        if (!isInIntegerRange(readRow_Num.getText(), 0, null)) {
            readRow_Num.setText("");
        }
        aadValueToolTip(readRow_Num, "只能填数字，不填默认为0，从第一行读取");
    }

    /**
     * 限制读取起始列只能输入自然数
     */
    @FXML
    private void readCellHandleKeyTyped() {
        if (!isInIntegerRange(readCell_Num.getText(), 0, null)) {
            readCell_Num.setText("");
        }
        aadValueToolTip(readCell_Num, "只能填数字，不填默认为0，从第一列读取");
    }

    /**
     * 限制读取最大行数只能输入正整数
     */
    @FXML
    private void maxRowHandleKeyTyped() {
        if (!isInIntegerRange(maxRow_Num.getText(), 1, null)) {
            maxRow_Num.setText("");
        }
        aadValueToolTip(maxRow_Num, "只能填数字，不填默认不限制，会读取到有数据的最后一行，最小值为1");
    }

    /**
     * 重新查询按钮
     */
    @FXML
    private void reSelect() throws Exception {
        String inFilePath = excelPath_Num.getText();
        if (StringUtils.isEmpty(inFilePath)) {
            throw new Exception("excel模板文件位置为空，需要先设置excel模板文件位置再继续");
        }
        addInData();
    }

    /**
     * 是否展示文件拓展名选项监听
     */
    @FXML
    private void handleCheckBoxAction() throws Exception {
        ObservableList<FileNumBean> fileBeans = tableView_Num.getItems();
        if (fileBeans != null && !fileBeans.isEmpty()) {
            reSelect();
        }
    }

}
