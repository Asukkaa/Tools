package priv.koishi.tools.Controller;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Setter;
import priv.koishi.tools.Bean.ClickPositionBean;

import java.util.Map;
import java.util.WeakHashMap;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 操作步骤详情页控制器
 *
 * @author koishi
 * Date 2022/3/11
 * Time 15:09
 */
public class DetailController extends RootController {

    /**
     * 带鼠标悬停提示的内容变化监听器
     */
    private final Map<Object, ChangeListener<?>> changeListeners = new WeakHashMap<>();

    /**
     * 详情页页面舞台
     */
    private Stage stage;

    /**
     * 页面数据对象
     */
    private ClickPositionBean selectedItem;

    @Setter
    private Runnable refreshCallback;

    @FXML
    public AnchorPane anchorPane_Det;

    @FXML
    public ChoiceBox<String> clickType_Det;

    @FXML
    public TextField clickName_Det, mouseStartX_Det, mouseStartY_Det, mouseEndX_Det, mouseEndY_Det, wait_Det,
            clickNumBer_Det, timeClick_Det, interval_Det;

    /**
     * 初始化数据
     *
     * @param item 列表选中的数据
     */
    public void initData(ClickPositionBean item) {
        selectedItem = item;
        clickName_Det.setText(item.getName());
        mouseStartX_Det.setText(item.getStartX());
        mouseStartY_Det.setText(item.getStartY());
        mouseEndX_Det.setText(item.getEndX());
        mouseEndY_Det.setText(item.getEndY());
        wait_Det.setText(item.getWaitTime());
        clickNumBer_Det.setText(item.getClickNum());
        timeClick_Det.setText(item.getClickTime());
        interval_Det.setText(item.getClickInterval());
        clickType_Det.setValue(item.getType());
        // 设置鼠标悬停提示
        setToolTip();
        // 设置文本输入框提示
        setPromptText();
    }

    /**
     * 给输入框添加内容变化监听
     */
    private void textFieldChangeListener() {
        // 操作名称文本输入框鼠标悬停提示
        ChangeListener<String> clickNameListener = textFieldValueListener(clickName_Det, tip_clickName);
        changeListeners.put(clickName_Det, clickNameListener);
        // 限制单次操作点击间隔文本输入框内容
        ChangeListener<String> waitListener = integerRangeTextField(wait_Det, 0, null, tip_wait);
        changeListeners.put(wait_Det, waitListener);
        // 限制操作时长文本输入内容
        ChangeListener<String> timeClickListener = integerRangeTextField(timeClick_Det, 0, null, tip_clickTime);
        changeListeners.put(timeClick_Det, timeClickListener);
        // 限制鼠标结束位置横(X)坐标文本输入框内容
        ChangeListener<String> mouseEndXListener = integerRangeTextField(mouseEndX_Det, 0, null, tip_mouseEndX);
        changeListeners.put(mouseEndX_Det, mouseEndXListener);
        // 限制鼠标结束位置纵(Y)坐标文本输入框内容
        ChangeListener<String> mouseEndYListener = integerRangeTextField(mouseEndY_Det, 0, null, tip_mouseEndY);
        changeListeners.put(mouseEndY_Det, mouseEndYListener);
        // 限制鼠标起始位置横(X)坐标文本输入框内容
        ChangeListener<String> mouseStartXListener = integerRangeTextField(mouseStartX_Det, 0, null, tip_mouseStartX);
        changeListeners.put(mouseStartX_Det, mouseStartXListener);
        // 限制鼠标起始位置纵(Y)坐标文本输入框内容
        ChangeListener<String> mouseStartYListener = integerRangeTextField(mouseStartY_Det, 0, null, tip_mouseStartY);
        changeListeners.put(mouseStartY_Det, mouseStartYListener);
        // 限制点击次数文本输入框内容
        ChangeListener<String> clickNumBerListener = integerRangeTextField(clickNumBer_Det, 0, null, tip_clickNumBer);
        changeListeners.put(clickNumBer_Det, clickNumBerListener);
    }

    /**
     * 设置文本输入框提示
     */
    private void setPromptText() {
        wait_Det.setPromptText(selectedItem.getWaitTime());
        clickName_Det.setPromptText(selectedItem.getName());
        mouseStartX_Det.setPromptText(selectedItem.getStartX());
        mouseStartY_Det.setPromptText(selectedItem.getStartY());
        timeClick_Det.setPromptText(selectedItem.getClickTime());
        clickNumBer_Det.setPromptText(selectedItem.getClickNum());
        interval_Det.setPromptText(selectedItem.getClickInterval());
    }

    /**
     * 移除所有监听器
     */
    @SuppressWarnings("unchecked")
    private void removeAllListeners() {
        // 处理带鼠标悬停提示的变更监听器集合，遍历所有entry，根据不同类型移除对应的选择/数值监听器
        changeListeners.forEach((key, listener) -> {
            if (key instanceof ChoiceBox<?> choiceBox) {
                choiceBox.getSelectionModel().selectedItemProperty().removeListener((InvalidationListener) listener);
            } else if (key instanceof Slider slider) {
                slider.valueProperty().removeListener((ChangeListener<? super Number>) listener);
            } else if (key instanceof TextInputControl textInput) {
                textInput.textProperty().removeListener((ChangeListener<? super String>) listener);
            } else if (key instanceof CheckBox checkBox) {
                checkBox.selectedProperty().removeListener((ChangeListener<? super Boolean>) listener);
            }
        });
        changeListeners.clear();
    }

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        addToolTip(tip_wait, wait_Det);
        addToolTip(tip_mouseEndX, mouseEndX_Det);
        addToolTip(tip_mouseEndY, mouseEndY_Det);
        addToolTip(tip_clickType, clickType_Det);
        addToolTip(tip_clickTime, timeClick_Det);
        addToolTip(tip_clickName, clickName_Det);
        addToolTip(tip_clickInterval, interval_Det);
        addToolTip(tip_clickNumBer, clickNumBer_Det);
        addToolTip(tip_mouseStartX, mouseStartX_Det);
        addToolTip(tip_mouseStartY, mouseStartY_Det);
    }

    /**
     * 页面初始化
     */
    @FXML
    private void initialize() {
        // 给输入框添加内容变化监听
        textFieldChangeListener();
        // 窗口关闭时移除所有监听器
        Platform.runLater(() -> {
            stage = (Stage) anchorPane_Det.getScene().getWindow();
            stage.setOnCloseRequest(e -> removeAllListeners());
        });
    }

    /**
     * 保存更改并关闭详情页按钮
     */
    @FXML
    private void saveDetails() {
        int mouseStartX = setDefaultIntValue(mouseStartX_Det, 0, 0, null);
        int mouseStartY = setDefaultIntValue(mouseStartY_Det, 0, 0, null);
        selectedItem.setName(clickName_Det.getText());
        selectedItem.setStartX(String.valueOf(mouseStartX));
        selectedItem.setStartY(String.valueOf(mouseStartY));
        selectedItem.setEndX(String.valueOf(setDefaultIntValue(mouseEndX_Det, mouseStartX, 0, null)));
        selectedItem.setEndY(String.valueOf(setDefaultIntValue(mouseEndY_Det, mouseStartY, 0, null)));
        selectedItem.setWaitTime(String.valueOf(setDefaultIntValue(wait_Det, 0, 0, null)));
        selectedItem.setClickTime(String.valueOf(setDefaultIntValue(timeClick_Det, 0, 0, null)));
        selectedItem.setClickNum(String.valueOf(setDefaultIntValue(clickNumBer_Det, 1, 1, null)));
        selectedItem.setClickInterval(String.valueOf(setDefaultIntValue(interval_Det, 0, 0, null)));
        selectedItem.setType(clickType_Det.getValue());
        stage.close();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

    /**
     * 删除当前步骤按钮
     */
    @FXML
    private void removeDetail() {
        selectedItem.setRemove(true);
        stage.close();
        // 触发列表刷新（通过回调）
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }

}
