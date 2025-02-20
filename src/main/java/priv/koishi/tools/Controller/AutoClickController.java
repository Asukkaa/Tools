package priv.koishi.tools.Controller;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import priv.koishi.tools.Bean.ClickPositionBean;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.EditingCell.EditingCell;
import priv.koishi.tools.Properties.CommonProperties;
import priv.koishi.tools.ThreadPool.CommonThreadPoolExecutor;

import java.awt.*;
import java.util.concurrent.ExecutorService;

import static javafx.scene.input.MouseButton.PRIMARY;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Service.AutoClickService.autoClick;
import static priv.koishi.tools.Service.AutoClickService.click;
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
    private Label mousePosition_Click, dataNumber_Click, log_Click, tip_Click, cancelTip_Click;

    @FXML
    private Button clearButton_Click, runClick_Click, clickTest_Click, addPosition_Click;

    @FXML
    private TextField mouseStartX_Click, mouseStartY_Click, mouseEndX_Click, mouseEndY_Click, wait_Click, loopTime_Click, clickNumBer_Click, timeClick_Click, clickName_Click, interval_Click;

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
        table.setPrefHeight(stageHeight * 0.5);
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
        // 操作每次文本输入框鼠标悬停提示
        textFieldValueListener(clickName_Click, tip_clickName);
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
        addToolTip(tip_clickInterval, interval_Click);
        addToolTip(tip_learButton, clearButton_Click);
        addToolTip(tip_addPosition, addPosition_Click);
        addToolTip(tip_clickNumBer, clickNumBer_Click);
        addToolTip(tip_mouseStartX, mouseStartX_Click);
        addToolTip(tip_mouseStartY, mouseStartY_Click);
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
    private void initialize() {
        // 获取鼠标坐标监听器
        moussePositionListener();
        // 设置javafx单元格宽度
        bindPrefWidthProperty();
        // 设置鼠标悬停提示
        setToolTip();
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
        // macos需要多点一次将切换目标程序窗口
        if (systemName.contains(macos)) {
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

}
