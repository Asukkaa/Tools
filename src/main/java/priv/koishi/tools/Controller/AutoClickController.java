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
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.ClickPositionBean;
import priv.koishi.tools.EditingCell.EditingCell;
import priv.koishi.tools.Properties.CommonProperties;

import java.awt.*;

import static javafx.scene.input.MouseButton.PRIMARY;
import static priv.koishi.tools.Finals.CommonFinals.macos;
import static priv.koishi.tools.Finals.CommonFinals.systemName;
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
    private Label mousePosition_Click, fileNumber_Click, log_Click;

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
        Label fileNum = (Label) scene.lookup("#fileNumber_Click");
        HBox fileNumberHBox = (HBox) scene.lookup("#fileNumberHBox_Click");
        nodeRightAlignment(fileNumberHBox, tableWidth, fileNum);
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
        double x = 0;
        String xText = mouseX_Click.getText();
        if (StringUtils.isBlank(xText)) {
           xText = String.valueOf(x);
        }
        x = Double.parseDouble(xText);
        double y = 0;
        String yText = mouseY_Click.getText();
        if (StringUtils.isBlank(yText)) {
            yText = String.valueOf(y);
        }
        y = Double.parseDouble(yText);
        long waitTime = 0;
        String waitTimeStr = wait_Click.getText();
        if (StringUtils.isBlank(waitTimeStr)) {
            waitTimeStr = String.valueOf(waitTime);
        }
        waitTime = Long.parseLong(waitTimeStr);
        ClickPositionBean clickPositionBean = new ClickPositionBean();
        clickPositionBean.setWait(waitTime)
                .setWaitTime(waitTimeStr)
                .setType("左键单击")
                .setXPosition(x)
                .setYPosition(y)
                .setX(xText)
                .setY(yText);
        return clickPositionBean;
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
    }

    /**
     * 运行自动点击按钮
     */
    @FXML
    public void runClick() {
        ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
        if (CollectionUtils.isNotEmpty(tableViewItems)) {
            Robot robot = new Robot();
            if (systemName.contains(macos)) {
                ClickPositionBean clickPositionBean = tableViewItems.getFirst();
                double x = clickPositionBean.getXPosition();
                double y = clickPositionBean.getYPosition();
                robot.mouseMove(x, y);
                robot.mousePress(PRIMARY);
                robot.mouseRelease(PRIMARY);
            }
            tableViewItems.forEach(clickPositionBean -> {
                double x = clickPositionBean.getXPosition();
                double y = clickPositionBean.getYPosition();
                robot.mouseMove(x, y);
                try {
                    Thread.sleep(clickPositionBean.getWait());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                robot.mousePress(PRIMARY);
                robot.mouseRelease(PRIMARY);
            });
        }
    }

    /**
     * 清空javafx列表按钮
     */
    @FXML
    public void removeAll() {
        removeTableViewData(tableView_Click, fileNumber_Click, log_Click);
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
        robot.mouseMove(clickPositionBean.getXPosition(), clickPositionBean.getYPosition());
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
        buildDeleteDataMenuItem(tableView_Click, fileNumber_Click, contextMenu);
        tableView_Click.setContextMenu(contextMenu);
        tableView_Click.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(tableView_Click, event.getScreenX(), event.getScreenY());
            }
        });
        updateLabel(log_Click, "");
    }

}
