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
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.ClickPositionBean;
import priv.koishi.tools.Properties.CommonProperties;

import java.awt.*;

import static javafx.scene.input.MouseButton.PRIMARY;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * @author KOISHI
 * Date:2025-02-17
 * Time:17:21
 */
public class AutoClickController extends CommonProperties {

    private static final String tabId = "_Click";

    @FXML
    private AnchorPane anchorPane_Click;

    @FXML
    private VBox vbox_Click;

    @FXML
    private HBox fileNumberHBox_Click;

    @FXML
    private TextField mouseX_Click, mouseY_Click;

    @FXML
    private Label mousePosition_Click, fileNumber_Click, log_Click;

    @FXML
    private Button clearButton_Click, runClick_Click, clickTest_Click, addPosition_Click;

    @FXML
    private TableView<ClickPositionBean> tableView_Click;

    @FXML
    public TableColumn<ClickPositionBean, String> x_Click, y_Click;

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
        table.setPrefHeight(stageHeight * 0.3);
        // 设置组件宽度
        double stageWidth = stage.getWidth();
        double tableWidth = stageWidth * 0.5;
        table.setMaxWidth(tableWidth);
        Node settingVBox = scene.lookup("#vbox_Click");
        settingVBox.setLayoutX(stageWidth * 0.03);
        Node tabName = scene.lookup("#x_Click");
        tabName.setStyle("-fx-pref-width: " + tableWidth * 0.5 + "px;");
        Node tabState = scene.lookup("#y_Click");
        tabState.setStyle("-fx-pref-width: " + tableWidth * 0.5 + "px;");
        Label fileNum = (Label) scene.lookup("#fileNumber_Click");
        HBox fileNumberHBox = (HBox) scene.lookup("#fileNumberHBox_Click");
        nodeRightAlignment(fileNumberHBox, tableWidth, fileNum);
    }

    private void getMousePosition() {
        // 使用java.awt.MouseInfo获取鼠标的全局位置
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        double x = mousePoint.getX();
        double y = mousePoint.getY();
        mousePosition_Click.setText("X：" + x + " , Y：" + y);
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        x_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.5));
        y_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.5));
    }

    @FXML
    public void runClick() {
        ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
        Robot robot = new Robot();
        tableViewItems.forEach(clickPositionBean -> {
            double x = clickPositionBean.getX();
            double y = clickPositionBean.getY();
            robot.mouseMove(x, y);
            robot.mousePress(PRIMARY);
            robot.mouseRelease(PRIMARY);
        });
    }

    @FXML
    public void removeAll() {
        removeTableViewData(tableView_Click, fileNumber_Click, log_Click);
    }

    @FXML
    private void clickTest() {
        // 创建一个Robot实例
        Robot robot = new Robot();
        // 定义要点击的屏幕坐标
        double x = 0;
        double y = 0;
        String xText = mouseX_Click.getText();
        String yText = mouseY_Click.getText();
        if (StringUtils.isNotBlank(xText)) {
            x = Double.parseDouble(xText);
        }
        if (StringUtils.isNotBlank(yText)) {
            y = Double.parseDouble(yText);
        }
        // 移动鼠标到指定位置并点击
        robot.mouseMove(x, y);
        robot.mousePress(PRIMARY);
        robot.mouseRelease(PRIMARY);
    }

    @FXML
    private void initialize() {
        // 启动定时器，实时获取鼠标位置
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                getMousePosition();
            }
        };
        timer.start();
        bindPrefWidthProperty();
    }

    @FXML
    private void addPosition() {
        double x = 0;
        double y = 0;
        String xText = mouseX_Click.getText();
        String yText = mouseY_Click.getText();
        if (StringUtils.isNotBlank(xText)) {
            x = Double.parseDouble(xText);
        }
        if (StringUtils.isNotBlank(yText)) {
            y = Double.parseDouble(yText);
        }
        ClickPositionBean clickPositionBean = new ClickPositionBean();
        clickPositionBean.setX(x)
                .setY(y);
        ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
        tableViewItems.add(clickPositionBean);
        autoBuildTableViewData(tableView_Click, tableViewItems, tabId);
        updateLabel(log_Click, "");
    }

}
