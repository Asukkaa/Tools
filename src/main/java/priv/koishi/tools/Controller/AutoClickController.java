package priv.koishi.tools.Controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import javafx.stage.Window;
import javafx.util.Duration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Bean.AutoClickTaskBean;
import priv.koishi.tools.Bean.ClickPositionBean;
import priv.koishi.tools.EditingCell.EditingCell;
import priv.koishi.tools.Listener.MousePositionListener;
import priv.koishi.tools.Listener.MousePositionUpdater;
import priv.koishi.tools.MainApplication;
import priv.koishi.tools.ThreadPool.ThreadPoolManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.MainApplication.mainScene;
import static priv.koishi.tools.MainApplication.mainStage;
import static priv.koishi.tools.Service.AutoClickService.autoClick;
import static priv.koishi.tools.Utils.CommonUtils.isInIntegerRange;
import static priv.koishi.tools.Utils.CommonUtils.removeNativeListener;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.TaskUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 自动点击工具页面控制器
 *
 * @author KOISHI
 * Date:2025-02-17
 * Time:17:21
 */
public class AutoClickController extends RootController implements MousePositionUpdater {

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
    public static final String defaultOutFileName = "PMC自动操作流程";

    /**
     * 默认录制准备时间
     */
    private static String preparationRecordTimeDefault;

    /**
     * 默认运行准备时间
     */
    private static String preparationRunTimeDefault;

    /**
     * 详情页高度
     */
    private int detailHeight;

    /**
     * 详情页宽度
     */
    private int detailWidth;

    /**
     * 浮窗X坐标
     */
    private int floatingX;

    /**
     * 浮窗Y坐标
     */
    private int floatingY;

    /**
     * 浮窗宽度
     */
    private int floatingWidth;

    /**
     * 浮窗高度
     */
    private int floatingHeight;

    /**
     * 要防重复点击的组件
     */
    private static final List<Node> disableNodes = new ArrayList<>();

    /**
     * 线程池实例
     */
    private static final ExecutorService executorService = ThreadPoolManager.getPool(AutoClickController.class);

    /**
     * 自动点击任务
     */
    private Task<Void> autoClickTask;

    /**
     * 页面标识符
     */
    private static final String tabId = "_Click";

    /**
     * 无辅助功能权限
     */
    private boolean isNativeHookException;

    /**
     * 正在录制标识
     */
    private boolean recordClicking;

    /**
     * 正在运行自动操作标识
     */
    private boolean runClicking;

    /**
     * 录制时间线
     */
    private Timeline recordTimeline;

    /**
     * 运行时间线
     */
    private Timeline runTimeline;

    /**
     * 录制开始时间
     */
    private long recordingStartTime;

    /**
     * 全局鼠标监听器
     */
    private NativeMouseListener nativeMouseListener;

    /**
     * 全局键盘监听器
     */
    private NativeKeyListener nativeKeyListener;

    /**
     * 浮窗Stage
     */
    private Stage floatingStage;

    /**
     * 顶部浮窗信息输出栏
     */
    private Label floatingLabel;

    /**
     * 顶部浮窗鼠标位置信息栏
     */
    private Label floatingMousePosition;

    @FXML
    public AnchorPane anchorPane_Click;

    @FXML
    public HBox fileNumberHBox_Click, tipHBox_Click, cancelTipHBox_Click, logHBox_Click;

    @FXML
    public ProgressBar progressBar_Click;

    @FXML
    public Label mousePosition_Click, dataNumber_Click, log_Click, tip_Click, cancelTip_Click, outPath_Click,
            err_Click;

    @FXML
    public CheckBox openDirectory_Click, showWindowRun_Click, hideWindowRun_Click, firstClick_Click,
            hideWindowRecord_Click, showWindowRecord_Click;

    @FXML
    public Button clearButton_Click, runClick_Click, clickTest_Click, addPosition_Click, loadAutoClick_Click,
            exportAutoClick_Click, addOutPath_Click, recordClick_Click;

    @FXML
    public TextField loopTime_Click, outFileName_Click, preparationRecordTime_Click, preparationRunTime_Click;

    @FXML
    public TableView<ClickPositionBean> tableView_Click;

    @FXML
    public TableColumn<ClickPositionBean, Integer> index_Click;

    @FXML
    public TableColumn<ClickPositionBean, String> name_Click, startX_Click, startY_Click, endX_Click, endY_Click,
            clickTime_Click, clickNum_Click, clickInterval_Click, waitTime_Click, type_Click;

    /**
     * 组件自适应宽高
     */
    public void adaption() {
        // 设置组件高度
        double stageHeight = mainStage.getHeight();
        tableView_Click.setPrefHeight(stageHeight * 0.45);
        // 设置组件宽度
        double stageWidth = mainStage.getWidth();
        double tableWidth = stageWidth * 0.95;
        tableView_Click.setMaxWidth(tableWidth);
        nodeRightAlignment(fileNumberHBox_Click, tableWidth, dataNumber_Click);
        nodeRightAlignment(tipHBox_Click, tableWidth, tip_Click);
        nodeRightAlignment(cancelTipHBox_Click, tableWidth, cancelTip_Click);
        if (err_Click != null) {
            nodeRightAlignment(logHBox_Click, tableWidth, err_Click);
        }
    }

    /**
     * 保存最后一次配置的值
     *
     * @throws IOException io异常
     */
    public void saveLastConfig() throws IOException {
        if (anchorPane_Click != null) {
            InputStream input = checkRunningInputStream(configFile_Click);
            Properties prop = new Properties();
            prop.load(input);
            String lastFirstClickValue = firstClick_Click.isSelected() ? activation : unActivation;
            prop.put(key_lastFirstClick, lastFirstClickValue);
            prop.put(key_lastOutFileName, outFileName_Click.getText());
            String lastOpenDirectoryValue = openDirectory_Click.isSelected() ? activation : unActivation;
            prop.put(key_lastOpenDirectory, lastOpenDirectoryValue);
            String lastHideWindowRunValue = hideWindowRun_Click.isSelected() ? activation : unActivation;
            prop.put(key_lastHideWindowRun, lastHideWindowRunValue);
            String lastShowWindowRunValue = showWindowRun_Click.isSelected() ? activation : unActivation;
            prop.put(key_lastShowWindowRun, lastShowWindowRunValue);
            String lastHideWindowRecordValue = hideWindowRecord_Click.isSelected() ? activation : unActivation;
            prop.put(key_lastHideWindowRecord, lastHideWindowRecordValue);
            String lastShowWindowRecordValue = showWindowRecord_Click.isSelected() ? activation : unActivation;
            prop.put(key_lastShowWindowRecord, lastShowWindowRecordValue);
            prop.put(key_lastPreparationRecordTime, preparationRecordTime_Click.getText());
            prop.put(key_lastPreparationRunTime, preparationRunTime_Click.getText());
            prop.put(key_outFilePath, outPath_Click.getText());
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
            setControlLastConfig(outPath_Click, prop, key_outFilePath);
            setControlLastConfig(loopTime_Click, prop, key_lastLoopTime);
            setControlLastConfig(firstClick_Click, prop, key_lastFirstClick);
            setControlLastConfig(outFileName_Click, prop, key_lastOutFileName);
            setControlLastConfig(openDirectory_Click, prop, key_lastOpenDirectory);
            setControlLastConfig(hideWindowRun_Click, prop, key_lastHideWindowRun);
            setControlLastConfig(showWindowRun_Click, prop, key_lastShowWindowRun);
            setControlLastConfig(hideWindowRecord_Click, prop, key_lastHideWindowRecord);
            setControlLastConfig(showWindowRecord_Click, prop, key_lastShowWindowRecord);
            setControlLastConfig(preparationRunTime_Click, prop, key_lastPreparationRunTime);
            setControlLastConfig(preparationRecordTime_Click, prop, key_lastPreparationRecordTime);
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
        floatingX = Integer.parseInt(prop.getProperty(key_floatingX));
        floatingY = Integer.parseInt(prop.getProperty(key_floatingY));
        detailWidth = Integer.parseInt(prop.getProperty(key_detailWidth));
        detailHeight = Integer.parseInt(prop.getProperty(key_detailHeight));
        floatingWidth = Integer.parseInt(prop.getProperty(key_floatingWidth));
        floatingHeight = Integer.parseInt(prop.getProperty(key_floatingHeight));
        preparationRunTimeDefault = prop.getProperty(key_defaultPreparationRunTime);
        preparationRecordTimeDefault = prop.getProperty(key_defaultPreparationRecordTime);
        input.close();
    }

    /**
     * 设置javafx单元格宽度
     */
    private void bindPrefWidthProperty() {
        index_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.05));
        name_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.15));
        startX_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        startY_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        endX_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        endY_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        clickTime_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.07));
        clickNum_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.07));
        clickInterval_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.07));
        waitTime_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.1));
        type_Click.prefWidthProperty().bind(tableView_Click.widthProperty().multiply(0.09));
    }

    /**
     * 设置单元格可编辑
     */
    private void makeCellCanEdit() {
        tableView_Click.setEditable(true);
        name_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setName));
        startX_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setStartX, true, 0, null));
        endX_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setEndX, true, 0, null));
        startY_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setStartY, true, 0, null));
        endY_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setEndY, true, 0, null));
        clickInterval_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setClickInterval, true, 0, null));
        waitTime_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setWaitTime, true, 0, null));
        clickTime_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setClickTime, true, 0, null));
        clickNum_Click.setCellFactory((tableColumn) -> new EditingCell<>(ClickPositionBean::setClickNum, true, 0, null));
    }

    /**
     * 显示详情页
     *
     * @param item 要显示详情的操作流程设置
     */
    private void showDetail(ClickPositionBean item) {
        URL fxmlLocation = getClass().getResource(resourcePath + "fxml/Detail-view.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DetailController controller = loader.getController();
        controller.initData(item);
        // 设置保存后的回调
        controller.setRefreshCallback(() -> {
            if (item.isRemove()) {
                tableView_Click.getItems().remove(item);
            }
            // 刷新列表
            tableView_Click.refresh();
            updateTableViewSizeText(tableView_Click, dataNumber_Click, text_process);
        });
        Stage detailStage = new Stage();
        Scene scene = new Scene(root, detailWidth, detailHeight);
        detailStage.setScene(scene);
        detailStage.setTitle(item.getName() + " 步骤详情");
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResource("icon/Tools.png")).toExternalForm()));
        scene.getStylesheets().add(Objects.requireNonNull(MainApplication.class.getResource("css/Styles.css")).toExternalForm());
        detailStage.show();
    }


    /**
     * 初始化浮窗
     */
    private void initFloatingWindow() {
        // 创建一个矩形作为浮窗的内容
        Rectangle rectangle = new Rectangle(floatingWidth, floatingHeight);
        // 设置透明度
        rectangle.setOpacity(0.5);
        StackPane root = new StackPane();
        root.setBackground(Background.fill(Color.TRANSPARENT));
        Color labelTextFill = Color.WHITE;
        floatingLabel = new Label(text_cancelTask);
        floatingLabel.setTextFill(labelTextFill);
        floatingMousePosition = new Label();
        floatingMousePosition.setTextFill(labelTextFill);
        VBox vBox = new VBox();
        vBox.getChildren().addAll(floatingMousePosition, floatingLabel);
        root.getChildren().addAll(rectangle, vBox);
        Scene scene = new Scene(root, Color.TRANSPARENT);
        vBox.setMouseTransparent(true);
        floatingStage = new Stage();
        // 设置透明样式
        floatingStage.initStyle(StageStyle.TRANSPARENT);
        // 设置始终置顶
        floatingStage.setAlwaysOnTop(true);
        floatingStage.setScene(scene);
    }

    /**
     * 显示浮窗
     */
    private void showFloatingWindow() throws IOException {
        getConfig();
        floatingStage.setX(floatingX);
        floatingStage.setY(floatingY);
        Platform.runLater(() -> {
            if (floatingStage != null && !floatingStage.isShowing()) {
                floatingStage.show();
            }
        });
    }

    /**
     * 隐藏浮窗
     */
    private void hideFloatingWindow() {
        Platform.runLater(() -> {
            if (floatingStage != null && floatingStage.isShowing()) {
                floatingStage.hide();
            }
        });
    }

    /**
     * 开始录制
     *
     * @param addType 添加类型
     */
    private void startRecord(int addType) {
        if (!runClicking && !recordClicking) {
            recordClicking = true;
            // 改变要防重复点击的组件状态
            changeDisableNodes(disableNodes, true);
            // 获取准备时间值
            int preparationTimeValue = setDefaultIntValue(preparationRecordTime_Click, Integer.parseInt(preparationRecordTimeDefault), 0, null);
            // 开始录制
            if (hideWindowRecord_Click.isSelected()) {
                mainStage.setIconified(true);
            }
            // 开启键盘监听
            startNativeKeyListener();
            // 设置浮窗文本显示准备时间
            floatingLabel.setText(text_cancelTask + preparationTimeValue + text_preparation);
            // 显示浮窗
            try {
                showFloatingWindow();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (preparationTimeValue == 0) {
                // 开启鼠标监听
                startNativeMouseListener(addType);
                // 录制开始时间
                recordingStartTime = System.currentTimeMillis();
                // 更新浮窗文本
                floatingLabel.setText(text_cancelTask + text_recordClicking);
            } else {
                recordTimeline = new Timeline();
                AtomicInteger preparationTime = new AtomicInteger(preparationTimeValue);
                // 创建 Timeline 来实现倒计时
                Timeline finalTimeline = recordTimeline;
                recordTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                    preparationTime.getAndDecrement();
                    if (preparationTime.get() > 0) {
                        floatingLabel.setText(text_cancelTask + preparationTime + text_preparation);
                    } else {
                        // 开启鼠标监听
                        startNativeMouseListener(addType);
                        // 录制开始时间
                        recordingStartTime = System.currentTimeMillis();
                        // 停止 Timeline
                        finalTimeline.stop();
                        // 更新浮窗文本
                        floatingLabel.setText(text_cancelTask + text_recordClicking);
                    }
                }));
                // 设置 Timeline 的循环次数
                recordTimeline.setCycleCount(preparationTimeValue);
                // 启动 Timeline
                recordTimeline.play();
            }
        }
    }

    /**
     * 获取操作设置并添加到列表中
     *
     * @param addType 添加类型
     */
    private void addClick(int addType) {
        if (autoClickTask == null && !recordClicking) {
            ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
            // 获取点击步骤设置
            ClickPositionBean clickPositionBean = getClickSetting(tableViewItems.size());
            List<ClickPositionBean> clickPositionBeans = new ArrayList<>();
            clickPositionBeans.add(clickPositionBean);
            // 向列表添加数据
            addData(clickPositionBeans, addType, tableView_Click, dataNumber_Click, text_process);
            // 初始化信息栏
            updateLabel(log_Click, "");
            // 显示详情
            showDetail(clickPositionBean);
        }
    }

    /**
     * 启动自动操作流程
     *
     * @param clickPositionBeans 自动操作流程
     */
    private void launchClickTask(List<ClickPositionBean> clickPositionBeans) throws IOException {
        if (!runClicking && !recordClicking) {
            runClicking = true;
            AutoClickTaskBean taskBean = new AutoClickTaskBean();
            taskBean.setLoopTime(setDefaultIntValue(loopTime_Click, 1, 0, null))
                    .setFirstClick(firstClick_Click.isSelected())
                    .setFloatingLabel(floatingLabel)
                    .setRunTimeline(runTimeline)
                    .setDisableNodes(disableNodes)
                    .setProgressBar(progressBar_Click)
                    .setBeanList(clickPositionBeans)
                    .setMassageLabel(log_Click);
            updateLabel(log_Click, "");
            if (hideWindowRun_Click.isSelected()) {
                mainStage.setIconified(true);
            }
            // 开启键盘监听
            startNativeKeyListener();
            // 创建一个Robot实例
            Robot robot = new Robot();
            autoClickTask = autoClick(taskBean, robot);
            // 绑定带进度条的线程
            bindingProgressBarTask(autoClickTask, taskBean);
            Label massageLabel = taskBean.getMassageLabel();
            autoClickTask.setOnSucceeded(event -> {
                taskUnbind(taskBean);
                massageLabel.setTextFill(Color.GREEN);
                massageLabel.setText(text_taskFinished);
                hideFloatingWindow();
                if (showWindowRun_Click.isSelected()) {
                    showStage(mainStage);
                }
                // 移除键盘监听器
                removeNativeListener(nativeKeyListener);
                autoClickTask = null;
                runTimeline = null;
                runClicking = false;
            });
            autoClickTask.setOnFailed(event -> {
                taskNotSuccess(taskBean, text_taskFailed);
                hideFloatingWindow();
                if (showWindowRun_Click.isSelected()) {
                    showStage(mainStage);
                }
                // 移除键盘监听器
                removeNativeListener(nativeKeyListener);
                // 移除开始前的倒计时
                if (runTimeline != null) {
                    runTimeline.stop();
                    runTimeline = null;
                }
                // 获取抛出的异常
                Throwable ex = autoClickTask.getException();
                autoClickTask = null;
                runClicking = false;
                throw new RuntimeException(ex);
            });
            autoClickTask.setOnCancelled(event -> {
                taskNotSuccess(taskBean, text_taskCancelled);
                hideFloatingWindow();
                if (showWindowRun_Click.isSelected()) {
                    showStage(mainStage);
                }
                // 移除键盘监听器
                removeNativeListener(nativeKeyListener);
                autoClickTask = null;
                runTimeline = null;
                runClicking = false;
            });
            if (runTimeline == null) {
                // 改变要防重复点击的组件状态
                changeDisableNodes(taskBean, true);
                // 获取准备时间值
                int preparationTimeValue = setDefaultIntValue(preparationRunTime_Click, Integer.parseInt(preparationRunTimeDefault), 0, null);
                // 设置浮窗文本显示准备时间
                floatingLabel.setText(text_cancelTask + preparationTimeValue + text_run);
                showFloatingWindow();
                // 延时执行任务
                runTimeline = executeRunTimeLine(preparationTimeValue);
            }
        }
    }

    /**
     * 延时执行任务
     *
     * @param preparationTimeValue 准备时间
     * @return runTimeline 运行时间线
     */
    private Timeline executeRunTimeLine(int preparationTimeValue) {
        if (preparationTimeValue == 0) {
            if (!autoClickTask.isRunning()) {
                // 使用新线程启动
                executorService.execute(autoClickTask);
            }
            return runTimeline;
        }
        runTimeline = new Timeline();
        AtomicInteger preparationTime = new AtomicInteger(preparationTimeValue);
        // 创建 Timeline 来实现倒计时
        Timeline finalTimeline = runTimeline;
        runTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            preparationTime.getAndDecrement();
            if (preparationTime.get() > 0) {
                floatingLabel.setText(text_cancelTask + preparationTime + text_run);
            } else {
                // 停止 Timeline
                finalTimeline.stop();
                if (!autoClickTask.isRunning()) {
                    // 使用新线程启动
                    executorService.execute(autoClickTask);
                }
            }
        }));
        // 设置 Timeline 的循环次数
        runTimeline.setCycleCount(preparationTimeValue);
        runTimeline.play();
        return runTimeline;
    }

    /**
     * 执行选中的步骤选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    private MenuItem buildClickTestMenuItem(TableView<ClickPositionBean> tableView, ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem("执行选中的步骤");
        menuItem.setOnAction(event -> {
            List<ClickPositionBean> selectedItem = tableView.getSelectionModel().getSelectedItems();
            if (CollectionUtils.isNotEmpty(selectedItem)) {
                try {
                    launchClickTask(selectedItem);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        contextMenu.getItems().add(menuItem);
        return menuItem;
    }

    /**
     * 插入数据选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    private void insertDataMenu(TableView<ClickPositionBean> tableView, ContextMenu contextMenu) {
        Menu menu = new Menu("插入数据");
        // 创建二级菜单项
        MenuItem insertUp = new MenuItem(menuItem_insertUp);
        MenuItem insertDown = new MenuItem(menuItem_insertDown);
        MenuItem recordUp = new MenuItem(menuItem_recordUp);
        MenuItem recordDown = new MenuItem(menuItem_recordDown);
        MenuItem insertTop = new MenuItem(menuItem_insertTop);
        MenuItem recordTop = new MenuItem(menuItem_recordTop);
        // 为每个菜单项添加事件处理
        insertUp.setOnAction(event -> insertDataMenuItem(tableView, menuItem_insertUp));
        insertDown.setOnAction(event -> insertDataMenuItem(tableView, menuItem_insertDown));
        recordUp.setOnAction(event -> insertDataMenuItem(tableView, menuItem_recordUp));
        recordDown.setOnAction(event -> insertDataMenuItem(tableView, menuItem_recordDown));
        insertTop.setOnAction(event -> insertDataMenuItem(tableView, menuItem_insertTop));
        recordTop.setOnAction(event -> insertDataMenuItem(tableView, menuItem_recordTop));
        // 将菜单添加到菜单列表
        menu.getItems().addAll(insertUp, insertDown, recordUp, recordDown, insertTop, recordTop);
        contextMenu.getItems().add(menu);
    }

    /**
     * 插入数据选项二级菜单选项
     *
     * @param tableView  要处理的数据列表
     * @param insertType 数据插入类型
     */
    private void insertDataMenuItem(TableView<ClickPositionBean> tableView, String insertType) {
        List<ClickPositionBean> selectedItem = tableView.getSelectionModel().getSelectedItems();
        if (CollectionUtils.isNotEmpty(selectedItem)) {
            switch (insertType) {
                case menuItem_insertUp: {
                    addClick(upAdd);
                    break;
                }
                case menuItem_insertDown: {
                    addClick(downAdd);
                    break;
                }
                case menuItem_recordUp: {
                    startRecord(upAdd);
                    break;
                }
                case menuItem_recordDown: {
                    startRecord(downAdd);
                    break;
                }
                case menuItem_insertTop: {
                    addClick(topAdd);
                    break;
                }
                case menuItem_recordTop: {
                    startRecord(topAdd);
                    break;
                }
            }
        }
    }

    /**
     * 查看所选项第一行详情选项
     *
     * @param tableView   要添加右键菜单的列表
     * @param contextMenu 右键菜单集合
     */
    private void buildDetailMenuItem(TableView<? extends ClickPositionBean> tableView, ContextMenu contextMenu) {
        MenuItem detailItem = new MenuItem("查看所选项第一行详情");
        detailItem.setOnAction(e -> {
            ClickPositionBean selected = tableView.getSelectionModel().getSelectedItems().getFirst();
            if (selected != null) {
                showDetail(selected);
            }
        });
        contextMenu.getItems().add(detailItem);
    }

    /**
     * 构建右键菜单
     */
    private void buildContextMenu() {
        // 添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        // 查看详情选项
        buildDetailMenuItem(tableView_Click, contextMenu);
        // 添加测试点击选项
        MenuItem menuItem = buildClickTestMenuItem(tableView_Click, contextMenu);
        // 没有运行必要权限则无法点击
        if (isNativeHookException) {
            menuItem.setDisable(true);
        }
        // 移动所选行选项
        buildMoveDataMenu(tableView_Click, contextMenu);
        // 修改操作类型
        buildEditClickTypeMenu(tableView_Click, contextMenu);
        // 插入数据选项
        insertDataMenu(tableView_Click, contextMenu);
        // 复制数据选项
        buildCopyDataMenu(tableView_Click, contextMenu, dataNumber_Click);
        // 取消选中选项
        buildClearSelectedData(tableView_Click, contextMenu);
        // 删除所选数据选项
        buildDeleteDataMenuItem(tableView_Click, dataNumber_Click, contextMenu, text_data);
        // 为列表添加右键菜单并设置可选择多行
        setContextMenu(contextMenu, tableView_Click);
    }

    /**
     * 获取点击步骤设置
     *
     * @return clickPositionBean 自动操作步骤类
     */
    private ClickPositionBean getClickSetting(int tableViewItemSize) {
        ClickPositionBean clickPositionBean = new ClickPositionBean();
        clickPositionBean.setName(text_step + (tableViewItemSize + 1) + text_isAdd)
                .setClickTime(defaultClickTimeOffset)
                .setType(mouseButton_primary)
                .setClickInterval("0")
                .setClickNum("1")
                .setWaitTime("0")
                .setStartX("0")
                .setStartY("0")
                .setEndX("0")
                .setEndY("0");
        if (tableViewItemSize == -1) {
            clickPositionBean.setName("测试步骤");
        }
        return clickPositionBean;
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 导出自动流程文件名称文本输入框鼠标悬停提示
        textFieldValueListener(outFileName_Click, tip_autoClickFileName + defaultOutFileName);
        // 限制循环次数文本输入框内容
        integerRangeTextField(loopTime_Click, 0, null, tip_loopTime);
        // 限制运行准备时间文本输入框内容
        integerRangeTextField(preparationRunTime_Click, 0, null, tip_preparationRunTime + preparationRunTimeDefault);
        // 限制录制准备时间文本输入框内容
        integerRangeTextField(preparationRecordTime_Click, 0, null, tip_preparationRecordTime + preparationRecordTimeDefault);
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_runClick, runClick_Click);
        addToolTip(tip_loopTime, loopTime_Click);
        addToolTip(tip_clickTest, clickTest_Click);
        addToolTip(tip_firstClick, firstClick_Click);
        addToolTip(tip_learButton, clearButton_Click);
        addToolTip(tip_addPosition, addPosition_Click);
        addToolTip(tip_recordClick, recordClick_Click);
        addToolTip(tip_hideWindowRun, hideWindowRun_Click);
        addToolTip(tip_showWindowRun, showWindowRun_Click);
        addToolTip(tip_outAutoClickPath, addOutPath_Click);
        addToolTip(tip_openDirectory, openDirectory_Click);
        addToolTip(tip_loadAutoClick, loadAutoClick_Click);
        addToolTip(tip_exportAutoClick, exportAutoClick_Click);
        addToolTip(tip_hideWindowRecord, hideWindowRecord_Click);
        addToolTip(tip_showWindowRecord, showWindowRecord_Click);
        addToolTip(tip_autoClickFileName + defaultOutFileName, outFileName_Click);
        addToolTip(tip_preparationRunTime + preparationRunTimeDefault, preparationRunTime_Click);
        addToolTip(tip_preparationRecordTime + preparationRecordTimeDefault, preparationRecordTime_Click);
    }

    /**
     * 将自动流程添加到列表中
     *
     * @param clickPositionBeans 自动流程集合
     */
    private void addAutoClickPositions(List<? extends ClickPositionBean> clickPositionBeans) throws IOException {
        for (ClickPositionBean clickPositionBean : clickPositionBeans) {
            clickPositionBean.setUuid(UUID.randomUUID().toString());
            if (!isInIntegerRange(clickPositionBean.getStartX(), 0, null) || !isInIntegerRange(clickPositionBean.getStartY(), 0, null)
                    || !isInIntegerRange(clickPositionBean.getEndX(), 0, null) || !isInIntegerRange(clickPositionBean.getEndY(), 0, null)
                    || !isInIntegerRange(clickPositionBean.getClickTime(), 0, null) || !isInIntegerRange(clickPositionBean.getClickNum(), 0, null)
                    || !isInIntegerRange(clickPositionBean.getClickInterval(), 0, null) || !isInIntegerRange(clickPositionBean.getWaitTime(), 0, null)
                    || !runClickTypeMap.containsKey(clickPositionBean.getType())) {
                throw new IOException(text_LackKeyData);
            }
        }
        // 向列表添加数据
        addData(clickPositionBeans, append, tableView_Click, dataNumber_Click, text_process);
        updateLabel(log_Click, text_loadSuccess + inFilePath);
        log_Click.setTextFill(Color.GREEN);
    }

    /**
     * 根据鼠标位置调整ui
     *
     * @param mousePoint 鼠标位置
     */
    @Override
    public void onMousePositionUpdate(Point mousePoint) {
        int x = (int) mousePoint.getX();
        int y = (int) mousePoint.getY();
        String text = "当前鼠标位置为： X: " + x + " Y: " + y;
        Platform.runLater(() -> {
            floatingMousePosition.setText(text);
            mousePosition_Click.setText(text);
            if (floatingStage != null && floatingStage.isShowing()) {
                floatingMove(floatingStage, mousePoint, defaultOffsetX, defaultOffsetY);
            }
        });
    }

    /**
     * 开启全局键盘监听
     */
    private void startNativeKeyListener() {
        removeNativeListener(nativeKeyListener);
        // 键盘监听器
        nativeKeyListener = new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                Platform.runLater(() -> {
                    // 仅在自动操作与录制情况下才监听键盘
                    if (recordClicking || runClicking) {
                        // 检测快捷键 esc
                        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
                            // 停止自动操作
                            if (autoClickTask != null && autoClickTask.isRunning()) {
                                autoClickTask.cancel();
                                autoClickTask = null;
                            }
                            // 停止录制计时
                            if (recordTimeline != null) {
                                recordTimeline.stop();
                                recordTimeline = null;
                                Platform.runLater(() -> {
                                    log_Click.setTextFill(Color.BLUE);
                                    log_Click.setText("录制已结束");
                                });
                            }
                            // 停止运行计时
                            if (runTimeline != null) {
                                runTimeline.stop();
                                runTimeline = null;
                                autoClickTask = null;
                                AutoClickTaskBean taskBean = new AutoClickTaskBean();
                                taskBean.setProgressBar(progressBar_Click)
                                        .setMassageLabel(log_Click);
                                taskNotSuccess(taskBean, text_taskCancelled);
                            }
                            // 改变要防重复点击的组件状态
                            changeDisableNodes(disableNodes, false);
                            // 移除鼠标监听器
                            removeNativeListener(nativeMouseListener);
                            hideFloatingWindow();
                            // 弹出程序主窗口
                            if (showWindowRecord_Click.isSelected()) {
                                showStage(mainStage);
                            }
                            // 移除键盘监听器
                            removeNativeListener(nativeKeyListener);
                            recordClicking = false;
                            runClicking = false;
                        }
                    }
                });
            }
        };
        GlobalScreen.addNativeKeyListener(nativeKeyListener);
    }

    /**
     * 开启全局鼠标监听
     */
    private void startNativeMouseListener(int addType) {
        removeNativeListener(nativeMouseListener);
        // 鼠标监听器
        nativeMouseListener = new NativeMouseListener() {
            // 记录点击时刻
            private long pressTime;
            // 记录松开时刻
            private long releasedTime;
            // 记录鼠标按钮
            private int pressButton;
            // 首次点击标记
            private boolean isFirstClick = true;
            // 记录点击信息
            ClickPositionBean clickBean;

            // 监听鼠标按下
            @Override
            public void nativeMousePressed(NativeMouseEvent e) {
                if (recordClicking) {
                    // 记录按下时刻的时间戳和坐标
                    pressTime = System.currentTimeMillis();
                    long waitTime;
                    if (isFirstClick) {
                        waitTime = pressTime - recordingStartTime;
                    } else {
                        waitTime = pressTime - releasedTime;
                    }
                    int dataSize = tableView_Click.getItems().size() + 1;
                    pressButton = e.getButton();
                    Point mousePoint = MousePositionListener.getMousePoint();
                    int startX = (int) mousePoint.getX();
                    int startY = (int) mousePoint.getY();
                    clickBean = new ClickPositionBean();
                    clickBean.setName(text_step + dataSize + text_isRecord)
                            .setUuid(UUID.randomUUID().toString())
                            .setType(recordClickTypeMap.get(pressButton))
                            .setWaitTime(String.valueOf(waitTime))
                            .setStartX(String.valueOf(startX))
                            .setStartY(String.valueOf(startY))
                            .setClickInterval("0")
                            .setClickNum("1");
                    Platform.runLater(() -> {
                        log_Click.setTextFill(Color.BLUE);
                        String log = text_recorded + recordClickTypeMap.get(pressButton) + " 点击 (" + clickBean.getStartX() + "," + clickBean.getStartY() + ")";
                        log_Click.setText(log);
                        floatingLabel.setText(text_cancelTask + text_recordClicking + "\n" + log);
                    });
                }
            }

            // 监听鼠标松开
            @Override
            public void nativeMouseReleased(NativeMouseEvent e) {
                if (recordClicking && pressButton == e.getButton()) {
                    isFirstClick = false;
                    releasedTime = System.currentTimeMillis();
                    // 计算点击持续时间（毫秒）
                    long duration = releasedTime - pressTime;
                    Point mousePoint = MousePositionListener.getMousePoint();
                    int endX = (int) mousePoint.getX();
                    int endY = (int) mousePoint.getY();
                    // 创建点击步骤对象
                    clickBean.setClickTime(String.valueOf(duration))
                            .setEndX(String.valueOf(endX))
                            .setEndY(String.valueOf(endY));
                    Platform.runLater(() -> {
                        // 添加至表格
                        List<ClickPositionBean> clickPositionBeans = new ArrayList<>();
                        clickPositionBeans.add(clickBean);
                        addData(clickPositionBeans, addType, tableView_Click, dataNumber_Click, text_process);
                        // 日志反馈
                        log_Click.setTextFill(Color.BLUE);
                        String log = text_recorded + recordClickTypeMap.get(pressButton) + " 松开 (" + clickBean.getEndX() + "," + clickBean.getEndY() + ")";
                        log_Click.setText(log);
                        floatingLabel.setText(text_cancelTask + text_recordClicking + "\n" + log);
                    });
                }
            }
        };
        GlobalScreen.addNativeMouseListener(nativeMouseListener);
    }

    /**
     * 设置要防重复点击的组件
     */
    private void setDisableNodes() {
        disableNodes.add(runClick_Click);
        disableNodes.add(clickTest_Click);
        disableNodes.add(recordClick_Click);
        disableNodes.add(addPosition_Click);
        disableNodes.add(clearButton_Click);
        disableNodes.add(loadAutoClick_Click);
        disableNodes.add(exportAutoClick_Click);
        Node fileRenameTab = mainScene.lookup("#fileRenameTab");
        disableNodes.add(fileRenameTab);
        Node fileNumToExcelTab = mainScene.lookup("#fileNumToExcelTab");
        disableNodes.add(fileNumToExcelTab);
        Node imgToExcelTab = mainScene.lookup("#imgToExcelTab");
        disableNodes.add(imgToExcelTab);
        Node fileNameToExcelTab = mainScene.lookup("#fileNameToExcelTab");
        disableNodes.add(fileNameToExcelTab);
        Node autoClickTab = mainScene.lookup("#autoClickTab");
        disableNodes.add(autoClickTab);
        Node settingTab = mainScene.lookup("#settingTab");
        disableNodes.add(settingTab);
        Node aboutTab = mainScene.lookup("#aboutTab");
        disableNodes.add(aboutTab);
    }

    /**
     * 禁用需要辅助控制权限的组件
     */
    private void setNativeHookExceptionLog() {
        isNativeHookException = true;
        runClick_Click.setDisable(true);
        recordClick_Click.setDisable(true);
        clickTest_Click.setDisable(true);
        String errorMessage = appName + " 缺少必要系统权限";
        if (isMac) {
            errorMessage = text_NativeHookException;
        }
        err_Click.setText(errorMessage);
        err_Click.setTooltip(creatTooltip(tip_NativeHookException));
    }

    /**
     * 页面初始化
     *
     * @throws IOException 配置文件读取失败
     */
    @FXML
    private void initialize() throws IOException {
        // 设置javafx单元格宽度
        bindPrefWidthProperty();
        // 读取配置文件
        getConfig();
        // 初始化浮窗
        initFloatingWindow();
        // 设置鼠标悬停提示
        setToolTip();
        // 给输入框添加内容变化监听
        textFieldChangeListener();
        // 设置初始配置值为上次配置值
        setLastConfig();
        try {
            // 注册全局输入监听器
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            setNativeHookExceptionLog();
        }
        if (StringUtils.isBlank(err_Click.getText())) {
            logHBox_Click.getChildren().remove(err_Click);
        }
        Platform.runLater(() -> {
            // 获取鼠标坐标监听器
            MousePositionListener.getInstance().addListener(this);
            // 设置要防重复点击的组件
            setDisableNodes();
            // 自动填充javafx表格
            autoBuildTableViewData(tableView_Click, ClickPositionBean.class, tabId, index_Click);
            // 表格设置为可编辑
            makeCellCanEdit();
            // 设置列表通过拖拽排序行
            tableViewDragRow(tableView_Click);
            // 构建右键菜单
            buildContextMenu();
        });
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
            throw new Exception(text_noAutoClickToRun);
        }
        // 启动自动操作流程
        launchClickTask(tableViewItems);
    }

    /**
     * 清空javafx列表按钮
     */
    @FXML
    public void removeAll() {
        if (autoClickTask == null && !recordClicking) {
            removeTableViewData(tableView_Click, dataNumber_Click, log_Click);
        }
    }

    /**
     * 点击测试按钮
     */
    @FXML
    private void clickTest() throws IOException {
        // 获取步骤设置
        List<ClickPositionBean> clickPositionBeans = new ArrayList<>();
        ClickPositionBean clickPositionBean = getClickSetting(-1);
        clickPositionBeans.add(clickPositionBean);
        // 启动自动操作流程
        launchClickTask(clickPositionBeans);
    }

    /**
     * 添加点击步骤
     */
    @FXML
    private void addPosition() {
        addClick(append);
    }

    /**
     * 导入操作流程按钮
     *
     * @throws IOException io异常
     */
    @FXML
    public void loadAutoClick(ActionEvent actionEvent) throws IOException {
        if (autoClickTask == null && !recordClicking) {
            getConfig();
            List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>(Collections.singleton(new FileChooser.ExtensionFilter("Perfect Mouse Control", "*.pmc")));
            Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
            File selectedFile = creatFileChooser(window, inFilePath, extensionFilters, text_selectAutoFile);
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
                    throw new IOException(text_loadAutoClick + inFilePath + text_formatError);
                }
                // 将自动流程添加到列表中
                addAutoClickPositions(clickPositionBeans);
            }
        }
    }

    /**
     * 导出操作流程按钮
     *
     * @throws Exception 列表中无要导出的自动操作流程
     */
    @FXML
    public void exportAutoClick() throws Exception {
        if (autoClickTask == null && !recordClicking) {
            ObservableList<ClickPositionBean> tableViewItems = tableView_Click.getItems();
            if (CollectionUtils.isEmpty(tableViewItems)) {
                throw new Exception(text_noAutoClickList);
            }
            if (StringUtils.isBlank(outFilePath)) {
                throw new Exception(text_outPathNull);
            }
            String fileName = setDefaultFileName(outFileName_Click, defaultOutFileName);
            ObjectMapper objectMapper = new ObjectMapper();
            String path = notOverwritePath(outFilePath + File.separator + fileName + PMC);
            objectMapper.writeValue(new File(path), tableViewItems);
            updateLabel(log_Click, text_saveSuccess + path);
            log_Click.setTextFill(Color.GREEN);
            if (openDirectory_Click.isSelected()) {
                openDirectory(path);
            }
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
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = creatDirectoryChooser(window, outFilePath, text_selectDirectory);
        if (selectedFile != null) {
            // 更新所选文件路径显示
            outFilePath = updatePathLabel(selectedFile.getPath(), outFilePath, key_outFilePath, outPath_Click, configFile_Click);
        }
    }

    /**
     * 拖拽释放行为
     *
     * @param dragEvent 拖拽事件
     * @throws IOException 导入自动化流程文件内容格式不正确、导入文件缺少关键数据
     */
    @FXML
    private void handleDrop(DragEvent dragEvent) throws IOException {
        if (autoClickTask == null && !recordClicking) {
            List<File> files = dragEvent.getDragboard().getFiles();
            List<ClickPositionBean> clickPositionBeans = new ArrayList<>();
            for (File file : files) {
                // 读取 JSON 文件并转换为 List<ClickPositionBean>
                ObjectMapper objectMapper = new ObjectMapper();
                File jsonFile = new File(file.getPath());
                try {
                    clickPositionBeans.addAll(objectMapper.readValue(jsonFile, objectMapper.getTypeFactory().constructCollectionType(List.class, ClickPositionBean.class)));
                } catch (IOException e) {
                    throw new IOException(text_loadAutoClick + inFilePath + text_formatError);
                }
            }
            // 将自动流程添加到列表中
            addAutoClickPositions(clickPositionBeans);
        }
    }

    /**
     * 拖拽中行为
     *
     * @param dragEvent 拖拽事件
     */
    @FXML
    private void acceptDrop(DragEvent dragEvent) {
        if (autoClickTask == null && !recordClicking) {
            List<File> files = dragEvent.getDragboard().getFiles();
            files.forEach(file -> {
                if (PMC.equals(getFileType(file))) {
                    // 接受拖放
                    dragEvent.acceptTransferModes(TransferMode.COPY);
                    dragEvent.consume();
                }
            });
        }
    }

    /**
     * 录制自动操作按钮
     */
    @FXML
    private void recordClick() {
        startRecord(append);
    }

}
