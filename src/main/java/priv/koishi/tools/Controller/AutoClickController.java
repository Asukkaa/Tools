package priv.koishi.tools.Controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.ClickPositionBean;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.EditingCell.EditingCell;
import priv.koishi.tools.Properties.CommonProperties;
import priv.koishi.tools.ThreadPool.CommonThreadPoolExecutor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static javafx.scene.input.MouseButton.PRIMARY;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Service.AutoClickService.autoClick;
import static priv.koishi.tools.Service.AutoClickService.click;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningInputStream;
import static priv.koishi.tools.Utils.CommonUtils.checkRunningOutputStream;
import static priv.koishi.tools.Utils.FileUtils.openDirectory;
import static priv.koishi.tools.Utils.FileUtils.updateProperties;
import static priv.koishi.tools.Utils.TaskUtils.bindingProgressBarTask;
import static priv.koishi.tools.Utils.TaskUtils.taskUnbind;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 自动点击工具页面控制器
 *
 * @author KOISHI
 * Date:2025-02-17
 * Time:17:21
 */
public class AutoClickController extends CommonProperties {

    /**
     * 导出文件路径
     */
    private static String outFilePath;

    /**
     * 导入文件路径
     */
    private static String inFilePath;

    /**
     * 默认导出文件名称
     */
    private static String defaultOutFileName;

    /**
     * 线程池
     */
    private final CommonThreadPoolExecutor commonThreadPoolExecutor = new CommonThreadPoolExecutor();

    /**
     * 线程池实例
     */
    ExecutorService executorService = commonThreadPoolExecutor.createNewThreadPool();

    /**
     * 自动点击任务
     */
    private Task<Void> autoClickTask;

    /**
     * 页面标识符
     */
    private static final String tabId = "_Click";

    @FXML
    private AnchorPane anchorPane_Click;

    @FXML
    private VBox vbox_Click;

    @FXML
    private HBox fileNumberHBox_Click, tipHBox_Click, cancelTipHBox_Click;

    @FXML
    private ProgressBar progressBar_Click;

    @FXML
    private ChoiceBox<String> clickType_Click;

    @FXML
    private CheckBox firstClick_Click, openDirectory_Click;

    @FXML
    private Label mousePosition_Click, dataNumber_Click, log_Click, tip_Click, cancelTip_Click, outPath_Click;

    @FXML
    private Button clearButton_Click, runClick_Click, clickTest_Click, addPosition_Click, loadAutoClick_Click, exportAutoClick_Click, addOutPath_Click;

    @FXML
    private TextField mouseStartX_Click, mouseStartY_Click, mouseEndX_Click, mouseEndY_Click, wait_Click, loopTime_Click, clickNumBer_Click, timeClick_Click, clickName_Click, interval_Click, outFileName_Click;

    @FXML
    private TableView<ClickPositionBean> tableView_Click;

    @FXML
    public TableColumn<ClickPositionBean, String> name_Click, startX_Click, startY_Click, endX_Click, endY_Click, clickTime_Click, clickNum_Click, clickInterval_Click, waitTime_Click, type_Click;

    /**
     * 组件自适应宽高
     *
     * @param stage 程序主舞台
     */
    public static void autoClickAdaption(Stage stage) {
        Scene scene = stage.getScene();
        // 设置组件高度
        double stageHeight = stage.getHeight();
        TableView<?> table = (TableView<?>) scene.lookup("#tableView_Click");
        table.setPrefHeight(stageHeight * 0.45);
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.95;
        table.setMaxWidth(tableWidth);
        Node settingVBox = scene.lookup("#vbox_Click");
        settingVBox.setLayoutX(stageWidth * 0.03);
        Node name = scene.lookup("#name_Click");
        name.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node startX = scene.lookup("#startX_Click");
        startX.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node startY = scene.lookup("#startY_Click");
        startY.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node endX = scene.lookup("#endX_Click");
        endX.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node endY = scene.lookup("#endY_Click");
        endY.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node clickTime = scene.lookup("#clickTime_Click");
        clickTime.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node clickNum = scene.lookup("#clickNum_Click");
        clickNum.setStyle("-fx-pref-width: " + tableWidth * 0.07 + "px;");
        Node clickInterval = scene.lookup("#clickInterval_Click");
        clickInterval.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Node waitTime = scene.lookup("#waitTime_Click");
        waitTime.setStyle("-fx-pref-width: " + tableWidth * 0.13 + "px;");
        Node type = scene.lookup("#type_Click");
        type.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Label dataNum = (Label) scene.lookup("#dataNumber_Click");
        HBox fileNumberHBox = (HBox) scene.lookup("#fileNumberHBox_Click");
        nodeRightAlignment(fileNumberHBox, tableWidth, dataNum);
        Label tip = (Label) scene.lookup("#tip_Click");
        HBox tipHBox = (HBox) scene.lookup("#tipHBox_Click");
        nodeRightAlignment(tipHBox, tableWidth, tip);
        Label cancelTip = (Label) scene.lookup("#cancelTip_Click");
        HBox cancelTipHBox = (HBox) scene.lookup("#cancelTipHBox_Click");
        nodeRightAlignment(cancelTipHBox, tableWidth, cancelTip);
    }


    /**
     * 保存最后一次配置的值
     *
     * @param scene 程序主场景
     * @throws IOException io异常
     */
    public static void autoClickSaveLastConfig(Scene scene) throws IOException {
        AnchorPane anchorPane = (AnchorPane) scene.lookup("#anchorPane_Click");
        if (anchorPane != null) {
            InputStream input = checkRunningInputStream(configFile_Click);
            Properties prop = new Properties();
            prop.load(input);
            TextField mouseStartX = (TextField) scene.lookup("#mouseStartX_Click");
            prop.put(key_lastMouseStartX, mouseStartX.getText());
            TextField mouseStartY = (TextField) scene.lookup("#mouseStartY_Click");
            prop.put(key_lastMouseStartY, mouseStartY.getText());
            TextField mouseEndX = (TextField) scene.lookup("#mouseEndX_Click");
            prop.put(key_lastMouseEndX, mouseEndX.getText());
            TextField mouseEndY = (TextField) scene.lookup("#mouseEndY_Click");
            prop.put(key_lastMouseEndY, mouseEndY.getText());
            TextField wait = (TextField) scene.lookup("#wait_Click");
            prop.put(key_lastWait, wait.getText());
            TextField loopTime = (TextField) scene.lookup("#loopTime_Click");
            prop.put(key_lastLoopTime, loopTime.getText());
            TextField clickNumBer = (TextField) scene.lookup("#clickNumBer_Click");
            prop.put(key_lastClickNumBer, clickNumBer.getText());
            TextField timeClick = (TextField) scene.lookup("#timeClick_Click");
            prop.put(key_lastTimeClick, timeClick.getText());
            TextField interval = (TextField) scene.lookup("#interval_Click");
            prop.put(key_lastInterval, interval.getText());
            CheckBox firstClick = (CheckBox) scene.lookup("#firstClick_Click");
            String lastFirstClickValue = firstClick.isSelected() ? activation : unActivation;
            prop.put(key_lastFirstClick, lastFirstClickValue);
            TextField clickName = (TextField) scene.lookup("#clickName_Click");
            prop.put(key_lastClickName, clickName.getText());
            ChoiceBox<?> clickType = (ChoiceBox<?>) scene.lookup("#clickType_Click");
            prop.put(key_lastClickType, clickType.getValue());
            TextField outFileName = (TextField) scene.lookup("#outFileName_Click");
            prop.put(key_lastOutFileName, outFileName.getText());
            CheckBox openDirectory = (CheckBox) scene.lookup("#openDirectory_Click");
            String lastOpenDirectoryValue = openDirectory.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, lastOpenDirectoryValue);
            OutputStream output = checkRunningOutputStream(configFile_Click);
            prop.store(output, null);
            input.close();
            output.close();
        }
    }

    /**
     * 设置初始配置值为上次配置值
     *
     * @throws IOException io异常
     */
    private void setLastConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Click);
        prop.load(input);
        if (activation.equals(prop.getProperty(key_loadLastConfig))) {
            setControlLastConfig(wait_Click, prop, key_lastWait, false, null);
            setControlLastConfig(outPath_Click, prop, key_outFilePath, false, null);
            setControlLastConfig(interval_Click, prop, key_lastInterval, false, null);
            setControlLastConfig(loopTime_Click, prop, key_lastLoopTime, false, null);
            setControlLastConfig(clickName_Click, prop, key_lastClickName, true, null);
            setControlLastConfig(mouseEndX_Click, prop, key_lastMouseEndX, false, null);
            setControlLastConfig(mouseEndY_Click, prop, key_lastMouseEndY, false, null);
            setControlLastConfig(timeClick_Click, prop, key_lastTimeClick, false, null);
            setControlLastConfig(clickType_Click, prop, key_lastClickType, false, null);
            setControlLastConfig(firstClick_Click, prop, key_lastFirstClick, false, null);
            setControlLastConfig(mouseStartX_Click, prop, key_lastMouseStartX, false, null);
            setControlLastConfig(mouseStartY_Click, prop, key_lastMouseStartY, false, null);
            setControlLastConfig(clickNumBer_Click, prop, key_lastClickNumBer, false, null);
            setControlLastConfig(outFileName_Click, prop, key_lastOutFileName, false, null);
            setControlLastConfig(openDirectory_Click, prop, key_lastOpenDirectory, false, null);
        }
        input.close();
    }

    /**
     * 读取配置文件
     *
     * @throws IOException io异常
     */
    private void getConfig() throws IOException {
        Properties prop = new Properties();
        InputStream input = checkRunningInputStream(configFile_Click);
        prop.load(input);
        inFilePath = prop.getProperty(key_inFilePath);
        outFilePath = prop.getProperty(key_outFilePath);
        defaultOutFileName = prop.getProperty(key_defaultOutFileName);
        input.close();
    }

    /**
     * 获取鼠标坐标
     */
    private void getMousePosition() {
        // 使用java.awt.MouseInfo获取鼠标的全局位置
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        double x = mousePoint.getX();
        double y = mousePoint.getY();
        mousePosition_Click.setText("X：" + x + " , Y：" + y);
    }

    /**
     * 获取鼠标坐标监听器
     */
    private void moussePositionListener() {
        // 启动定时器，实时获取鼠标位置
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                getMousePosition();
            }
        };
        timer.start();
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        name_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        startX_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        startY_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        endX_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        endY_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        clickTime_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        clickNum_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.07));
        clickInterval_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        waitTime_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.13));
        type_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
    }

    /**
     * 设置单元格可编辑
     */
    private void makeCellCanEdit() {
        tableView_Click.setEditable(true);
        name_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setName));
        startX_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setStartX));
        endX_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setEndX));
        startY_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setStartY));
        endY_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setEndY));
        clickInterval_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setClickInterval));
        waitTime_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setWaitTime));
        clickTime_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setClickTime));
        clickNum_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setClickNum));
    }

    /**
     * 构建右键菜单
     */
    private void buildContextMenu() {
        // 设置可以选中多行
        tableView_Click.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 所选行上移一行选项
        buildUpMoveDataMenuItem(tableView_Click, contextMenu);
        // 所选行下移一行选项
        buildDownMoveDataMenuItem(tableView_Click, contextMenu);
        // 修改操作类型
        buildEditClickType(tableView_Click, contextMenu);
        // 删除所选数据选项
        buildDeleteDataMenuItem(tableView_Click, dataNumber_Click, contextMenu, text_data);
        tableView_Click.setContextMenu(contextMenu);
        tableView_Click.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(tableView_Click, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * 获取点击步骤设置
     *
     * @return clickPositionBean 自动操作步骤类
     */
    private ClickPositionBean getClickSetting(int tableViewItemSize) {
        ClickPositionBean clickPositionBean = new ClickPositionBean();
        int startX = setDefaultIntValue(mouseStartX_Click, 0, 0, null);
        int startY = setDefaultIntValue(mouseStartY_Click, 0, 0, null);
        clickPositionBean.setWaitTime(String.valueOf(setDefaultIntValue(wait_Click, 0, 0, null)))
                .setClickInterval(String.valueOf(setDefaultIntValue(interval_Click, 0, 0, null)))
                .setClickNum(String.valueOf(setDefaultIntValue(clickNumBer_Click, 1, 0, null)))
                .setClickTime(String.valueOf(setDefaultIntValue(timeClick_Click, 0, 0, null)))
                .setName(setDefaultStrValue(clickName_Click, "步骤" + (tableViewItemSize + 1)))
                .setEndX(String.valueOf(setDefaultIntValue(mouseEndX_Click, startX, 0, null)))
                .setEndY(String.valueOf(setDefaultIntValue(mouseEndY_Click, startY, 0, null)))
                .setType(clickType_Click.getValue())
                .setStartX(String.valueOf(startX))
                .setStartY(String.valueOf(startY));
        return clickPositionBean;
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 操作名称文本输入框鼠标悬停提示
        textFieldValueListener(clickName_Click, tip_clickName);
        // 导出自动流程文件名称文本输入框鼠标悬停提示
        textFieldValueListener(outFileName_Click, tip_autoClickFileName + defaultOutFileName);
        // 限制单次操作点击间隔文本输入框内容
        integerRangeTextField(wait_Click, 0, null, tip_wait);
        // 限制循环次数文本输入框内容
        integerRangeTextField(loopTime_Click, 0, null, tip_loopTime);
        // 限制操作时长文本输入内容
        integerRangeTextField(timeClick_Click, 0, null, tip_clickTime);
        // 限制鼠标结束位置横(X)坐标文本输入框内容
        integerRangeTextField(mouseEndX_Click, 0, null, tip_mouseEndX);
        // 限制鼠标结束位置纵(Y)坐标文本输入框内容
        integerRangeTextField(mouseEndY_Click, 0, null, tip_mouseEndY);
        // 限制鼠标起始位置横(X)坐标文本输入框内容
        integerRangeTextField(mouseStartX_Click, 0, null, tip_mouseStartX);
        // 限制鼠标起始位置纵(Y)坐标文本输入框内容
        integerRangeTextField(mouseStartY_Click, 0, null, tip_mouseStartY);
        // 限制点击次数文本输入框内容
        integerRangeTextField(clickNumBer_Click, 0, null, tip_clickNumBer);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_wait, wait_Click);
        addToolTip(tip_runClick, runClick_Click);
        addToolTip(tip_loopTime, loopTime_Click);
        addToolTip(tip_mouseEndX, mouseEndX_Click);
        addToolTip(tip_mouseEndY, mouseEndY_Click);
        addToolTip(tip_clickType, clickType_Click);
        addToolTip(tip_clickTime, timeClick_Click);
        addToolTip(tip_clickName, clickName_Click);
        addToolTip(tip_clickTest, clickTest_Click);
        addToolTip(tip_firstClick, firstClick_Click);
        addToolTip(tip_clickInterval, interval_Click);
        addToolTip(tip_learButton, clearButton_Click);
        addToolTip(tip_addPosition, addPosition_Click);
        addToolTip(tip_clickNumBer, clickNumBer_Click);
        addToolTip(tip_mouseStartX, mouseStartX_Click);
        addToolTip(tip_mouseStartY, mouseStartY_Click);
        addToolTip(tip_outAutoClickPath, addOutPath_Click);
        addToolTip(tip_openDirectory, openDirectory_Click);
        addToolTip(tip_loadAutoClick, loadAutoClick_Click);
        addToolTip(tip_exportAutoClick, exportAutoClick_Click);
        addToolTip(tip_autoClickFileName + defaultOutFileName, outFileName_Click);
    }

    /**
     * 注册全局按键监听器
     */
    private void getGlobalScreen() {
        // 注册全局键盘监听器
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                // 检测快捷键 esc
                if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
                    Platform.runLater(() -> {
                        if (autoClickTask != null && autoClickTask.isRunning()) {
                            autoClickTask.cancel();
                            TaskBean<FileNumBean> taskBean = new TaskBean<>();
                            taskBean.setProgressBar(progressBar_Click)
                                    .setMassageLabel(log_Click);
                            taskUnbind(taskBean);
                            log_Click.setText("任务已取消");
                            log_Click.setTextFill(Color.RED);
                        }
                    });
                }
            }
        });
    }

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() throws IOException {
        // 获取鼠标坐标监听器
        moussePositionListener();
        // 设置javafx单元格宽度
        bindPrefWidthProperty();
        // 读取配置文件
        getConfig();
        // 设置鼠标悬停提示
        setToolTip();
        // 设置初始配置值为上次配置值
        setLastConfig();
        // 给输入框添加内容变化监听
        textFieldChangeListener();
        // 注册全局按键监听器
        getGlobalScreen();
    }

    /**
     * 运行自动点击按钮
     *
     * @throws Exception 列表中没有要执行的操作
     */
    @FXML
    public void runClick() throws Exception {
        ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
        if (CollectionUtils.isEmpty(tableViewItems)) {
            throw new Exception("列表中没有要执行的操作");
        }
        TaskBean<ClickPositionBean> taskBean = new TaskBean<>();
        taskBean.setLoopTime(setDefaultIntValue(loopTime_Click, 1, 0, null))
                .setFirstClick(firstClick_Click.isSelected())
                .setProgressBar(progressBar_Click)
                .setBeanList(tableViewItems)
                .setMassageLabel(log_Click);
        updateLabel(log_Click, "");
        Robot robot = new Robot();
        autoClickTask = autoClick(taskBean, robot);
        // 绑定带进度条的线程
        bindingProgressBarTask(autoClickTask, taskBean);
        autoClickTask.setOnSucceeded(event -> {
            taskUnbind(taskBean);
            taskBean.getMassageLabel().setTextFill(Color.GREEN);
        });
        // 使用新线程启动
        executorService.execute(autoClickTask);
    }

    /**
     * 清空javafx列表按钮
     */
    @FXML
    public void removeAll() {
        removeTableViewData(tableView_Click, dataNumber_Click, log_Click);
    }

    /**
     * 点击测试按钮
     */
    @FXML
    private void clickTest() {
        // 获取点击步骤设置
        ClickPositionBean clickPositionBean = getClickSetting(-1);
        // 操作执行前等待时间
        try {
            Thread.sleep(Long.parseLong(clickPositionBean.getWaitTime()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 创建一个Robot实例
        Robot robot = new Robot();
        // 执行自动流程前点击第一个起始坐标
        if (firstClick_Click.isSelected()) {
            // 起止坐标
            double startX = Double.parseDouble(clickPositionBean.getStartX());
            double startY = Double.parseDouble(clickPositionBean.getStartY());
            // 移动鼠标到起始位置
            robot.mouseMove(startX, startY);
            robot.mousePress(PRIMARY);
            robot.mouseRelease(PRIMARY);
        }
        click(clickPositionBean, robot);
    }

    /**
     * 添加点击步骤
     */
    @FXML
    private void addPosition() {
        ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
        int tableViewItemSize = tableViewItems.size();
        // 获取点击步骤设置
        ClickPositionBean clickPositionBean = getClickSetting(tableViewItemSize);
        tableViewItems.add(clickPositionBean);
        // 自动填充javafx表格
        autoBuildTableViewData(tableView_Click, tableViewItems, tabId);
        // 同步表格数据量
        dataNumber_Click.setText(text_allHave + (tableViewItemSize + 1) + text_process);
        // 表格设置为可编辑
        makeCellCanEdit();
        // 设置列表通过拖拽排序行
        tableViewDragRow(tableView_Click);
        // 构建右键菜单
        buildContextMenu();
        // 初始化信息栏
        updateLabel(log_Click, "");
    }

    /**
     * 导入操作流程按钮
     *
     * @throws IOException io异常
     */
    @FXML
    public void loadAutoClick(ActionEvent actionEvent) throws IOException {
        getConfig();
        List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(new FileChooser.ExtensionFilter("Json", "*.json")));
        File selectedFile = creatFileChooser(actionEvent, inFilePath, extensionFilters, text_selectAutoFile);
        if (selectedFile != null) {
            inFilePath = selectedFile.getPath();
            updateProperties(configFile_Click, key_inFilePath, new File(inFilePath).getParent());
            // 读取 JSON 文件并转换为 List<ClickPositionBean>
            ObjectMapper objectMapper = new ObjectMapper();
            File jsonFile = new File(inFilePath);
            List<ClickPositionBean> clickPositionBeans;
            try {
                clickPositionBeans = objectMapper.readValue(jsonFile, objectMapper.getTypeFactory().constructCollectionType(List.class, ClickPositionBean.class));
            } catch (MismatchedInputException | JsonParseException e) {
                throw new IOException("导入自动化流程文件：" + inFilePath + " 内容格式不正确");
            }
            List<ClickPositionBean> tableViewItems = tableView_Click.getItems();
            tableViewItems.addAll(clickPositionBeans);
            // 自动填充javafx表格
            autoBuildTableViewData(tableView_Click, tableViewItems, tabId);
            // 同步表格数据量
            dataNumber_Click.setText(text_allHave + (tableViewItems.size() + 1) + text_process);
            // 表格设置为可编辑
            makeCellCanEdit();
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Click);
            // 构建右键菜单
            buildContextMenu();
            updateLabel(log_Click, text_loadSuccess + inFilePath);
            log_Click.setTextFill(Color.GREEN);
        }
    }

    /**
     * 导出操作流程按钮
     *
     * @throws Exception 列表中无要导出的自动操作流程
     */
    @FXML
    public void exportAutoClick() throws Exception {
        ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
        if (CollectionUtils.isEmpty(tableViewItems)) {
            throw new Exception("列表中没有要导出的自动操作流程");
        }
        if (StringUtils.isBlank(outFilePath)) {
            throw new Exception(text_outPathNull);
        }
        String fileName = setDefaultFileName(outFileName_Click, defaultOutFileName);
        ObjectMapper objectMapper = new ObjectMapper();
        String path = outFilePath + File.separator + fileName + json;
        objectMapper.writeValue(new File(path), tableViewItems);
        updateLabel(log_Click, text_saveSuccess + path);
        log_Click.setTextFill(Color.GREEN);
        if (openDirectory_Click.isSelected()) {
            openDirectory(path);
        }
    }

    /**
     * 设置操作流程导出文件夹地址按钮
     *
     * @throws IOException io异常
     */
    @FXML
    private void addOutPath(ActionEvent actionEvent) throws IOException {
        getConfig();
        File selectedFile = creatDirectoryChooser(actionEvent, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Click, configFile_Click, anchorPane_Click);
        }
    }

}
