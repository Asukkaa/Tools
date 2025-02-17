package priv.koishi.tools.EditingCell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.Objects;

import static priv.koishi.tools.Finals.CommonFinals.text_nowValue;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 可编辑的javafx列表单元格
 *
 * @author KOISHI
 * Date:2024-11-04
 * Time:下午6:11
 */
public class EditingCell<T> extends TableCell<T, String> {

    /**
     * 在进入编辑状态时,所显示的输入框
     */
    private TextField textField;

    /**
     * 用于引入lambda表达式的对象
     */
    private final ItemConsumer<T> itemConsumer;

    /**
     * 单元格鼠标悬停提示
     */
    private final String tip = "双击单元格可进行编辑";

    /**
     * 构造EditingCell对象,并且明确将该cell的值保存进相应的JavaBean的属性值的方法
     *
     * @param itemConsumer 用于引入lambda表达式的对象
     */
    public EditingCell(ItemConsumer<T> itemConsumer) {
        this.itemConsumer = itemConsumer;
        setTooltip(creatTooltip(tip));
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            if (Objects.isNull(textField)) {
                createTextField(getTableColumn().getText());
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
            textField.requestFocus();
        }
        textFieldValueListener(textField, tip);
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

    /**
     * 在单元格中创建输入框
     */
    private void createTextField(String tableColumnText) {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.focusedProperty().addListener((ob, old, now) -> {
            if (!now) {
                commitEdit(textField.getText());
            }
        });
        addValueToolTip(textField, "双击单元格可编辑 " + tableColumnText, text_nowValue);
    }

    /**
     * 获取单元格值的字符串
     */
    private String getString() {
        return getItem() == null ? "" : getItem();
    }

}
