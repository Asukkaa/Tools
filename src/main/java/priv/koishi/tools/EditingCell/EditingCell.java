package priv.koishi.tools.EditingCell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.PopupWindow;
import javafx.util.Duration;

import java.util.Objects;

import static priv.koishi.tools.Utils.UiUtils.addValueToolTip;

/**
 * @author KOISHI
 * Date:2024-11-04
 * Time:下午6:11
 */
public class EditingCell<T> extends TableCell<T, String> {

    /**
     * 在进入编辑状态时,所显示的输入框
     */
    private TextField textField;

    private final ItemConsumer<T> itemConsumer;

    /**
     * 单元格鼠标悬停提示
     */
    private final String tip = "双击单元格可编辑修改后的文件名称";

    /**
     * 构造EditingCell对象,并且明确将该cell的值保存进相应的JavaBean的属性值的方法
     *
     * @param itemConsumer 用于引入lambda表达式的对象
     */
    public EditingCell(ItemConsumer<T> itemConsumer) {
        this.itemConsumer = itemConsumer;
        Tooltip tooltip = new Tooltip(tip);
        tooltip.setWrapText(true);
        tooltip.setShowDuration(new Duration(6000000));
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
        setTooltip(tooltip);
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            if (Objects.isNull(textField)) {
                createTextField();
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
            textField.requestFocus();
        }
        textField.setOnKeyTyped(event -> addValueToolTip(textField, tip));
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem());
        setGraphic(null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        updateItem(newValue, false);
        setTProperties(newValue);
    }

    /**
     * 将编辑后的对象属性进行保存.
     * 如果不将属性保存到cell所在表格的ObservableList集合中对象的相应属性中,
     * 则只是改变了表格显示的值,一旦表格刷新,则仍会表示旧值.
     */
    private void setTProperties(String newValue) {
        TableView<T> tableView = this.getTableView();
        T t = tableView.getItems().get(this.getIndex());
        itemConsumer.setTProperties(t, newValue);
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.focusedProperty().addListener((ob, old, now) -> {
            if (!now) {
                commitEdit(textField.getText());
            }
        });
        addValueToolTip(textField, "双击单元格可编辑修改后的文件名称");
    }

    private String getString() {
        return getItem() == null ? "" : getItem();
    }

}
