package priv.koishi.tools.Controller;

import javafx.animation.AnimationTimer;
import javafx.collections.ObservableList;
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
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import priv.koishi.tools.Bean.ClickPositionBean;
import priv.koishi.tools.EditingCell.EditingCell;
import priv.koishi.tools.Properties.CommonProperties;

import java.awt.*;

import static javafx.scene.input.MouseButton.PRIMARY;
import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.UiUtils.*;
import static priv.koishi.tools.Utils.UiUtils.integerRangeTextField;

/**
 * 自动点击工具页面控制器
 *
 * @author KOISHI
 * Date:2025-02-17
 * Time:17:21
 */
public class AutoClickController extends CommonProperties {

    /**
     * 页面标识符
     */
    private static final String tabId = "_Click";

    @FXML
    private AnchorPane anchorPane_Click;

    @FXML
    private VBox vbox_Click;

    @FXML
    private HBox fileNumberHBox_Click;

    @FXML
    private TextField mouseX_Click, mouseY_Click, wait_Click;

    @FXML
    private Label mousePosition_Click, dataNumber_Click, log_Click;

    @FXML
    private Button clearButton_Click, runClick_Click, clickTest_Click, addPosition_Click;

    @FXML
    private TableView<ClickPositionBean> tableView_Click;

    @FXML
    public TableColumn<ClickPositionBean, String> x_Click, y_Click, waitTime_Click, type_Click;

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
        table.setPrefHeight(stageHeight * 0.6);
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.9;
        table.setMaxWidth(tableWidth);
        Node settingVBox = scene.lookup("#vbox_Click");
        settingVBox.setLayoutX(stageWidth * 0.03);
        Node tabName = scene.lookup("#x_Click");
        tabName.setStyle("-fx-pref-width: " + tableWidth * 0.3 + "px;");
        Node tabState = scene.lookup("#y_Click");
        tabState.setStyle("-fx-pref-width: " + tableWidth * 0.3 + "px;");
        Node waitTime = scene.lookup("#waitTime_Click");
        waitTime.setStyle("-fx-pref-width: " + tableWidth * 0.3 + "px;");
        Node type = scene.lookup("#type_Click");
        type.setStyle("-fx-pref-width: " + tableWidth * 0.1 + "px;");
        Label dataNum = (Label) scene.lookup("#dataNumber_Click");
        HBox fileNumberHBox = (HBox) scene.lookup("#fileNumberHBox_Click");
        nodeRightAlignment(fileNumberHBox, tableWidth, dataNum);
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
        x_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.3));
        y_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.3));
        waitTime_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.3));
        type_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
    }

    /**
     * 获取点击步骤设置
     */
    private ClickPositionBean getClickSetting() {
        ClickPositionBean clickPositionBean = new ClickPositionBean();
        clickPositionBean.setWaitTime(String.valueOf(setDefaultIntValue(wait_Click, 0, 0, null)))
                .setX(String.valueOf(setDefaultIntValue(mouseX_Click, 0, 0, null)))
                .setY(String.valueOf(setDefaultIntValue(mouseY_Click, 0, 0, null)))
                .setType("左键单击");
        return clickPositionBean;
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        integerRangeTextField(wait_Click, 0, null, tip_wait);
        integerRangeTextField(mouseX_Click, 0, null, tip_mouseX);
        integerRangeTextField(mouseY_Click, 0, null, tip_mouseY);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_wait, wait_Click);
        addToolTip(tip_mouseX, mouseX_Click);
        addToolTip(tip_mouseY, mouseY_Click);
        addToolTip(tip_learButton, clearButton_Click);
        addToolTip(tip_runClick, runClick_Click);
        addToolTip(tip_addPosition, addPosition_Click);
        addToolTip(tip_clickTest, clickTest_Click);
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
    }

    /**
     * 运行自动点击按钮
     */
    @FXML
    public void runClick() throws Exception {
        ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
        if (CollectionUtils.isEmpty(tableViewItems)) {
            throw new Exception("列表中没有要执行的操作");
        }
        Robot robot = new Robot();
        // macos系统需要先点击一下将焦点切换到目标窗口
        if (systemName.contains(macos)) {
            ClickPositionBean clickPositionBean = tableViewItems.getFirst();
            double x = Double.parseDouble(clickPositionBean.getX());
            double y = Double.parseDouble(clickPositionBean.getY());
            robot.mouseMove(x, y);
            robot.mousePress(PRIMARY);
            robot.mouseRelease(PRIMARY);
        }
        tableViewItems.forEach(clickPositionBean -> {
            double x = Double.parseDouble(clickPositionBean.getX());
            double y = Double.parseDouble(clickPositionBean.getY());
            robot.mouseMove(x, y);
            try {
                Thread.sleep(Long.parseLong(clickPositionBean.getWaitTime()) * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            robot.mousePress(PRIMARY);
            robot.mouseRelease(PRIMARY);
        });
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
        // 创建一个Robot实例
        Robot robot = new Robot();
        ClickPositionBean clickPositionBean = getClickSetting();
        // 移动鼠标到指定位置并点击
        robot.mouseMove(Double.parseDouble(clickPositionBean.getX()), Double.parseDouble(clickPositionBean.getY()));
        // macos需要多点一次将切换目标程序窗口
        if (systemName.contains(macos)) {
            robot.mousePress(PRIMARY);
            robot.mouseRelease(PRIMARY);
        }
        robot.mousePress(PRIMARY);
        robot.mouseRelease(PRIMARY);
    }

    /**
     * 添加点击步骤
     */
    @FXML
    private void addPosition() {
        ClickPositionBean clickPositionBean = getClickSetting();
        ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
        tableViewItems.add(clickPositionBean);
        autoBuildTableViewData(tableView_Click, tableViewItems, tabId);
        dataNumber_Click.setText(text_allHave + tableViewItems.size() + text_data);
        // 表格设置为可编辑
        tableView_Click.setEditable(true);
        x_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setX));
        y_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setY));
        waitTime_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setWaitTime));
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
        updateLabel(log_Click, "");
    }

}
