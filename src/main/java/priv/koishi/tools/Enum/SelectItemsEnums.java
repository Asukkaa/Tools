package priv.koishi.tools.Enum;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static priv.koishi.tools.Enum.SelectItemsEnum.*;

/**
 * 文件批量重命名功能分隔符选项集枚举
 *
 * @author KOISHI
 * Date:2024-10-21
 * Time:下午1:10
 */
public enum SelectItemsEnums {

    // 阿拉伯数字
    subCodeArabicNumItems(subCodeArabicNumAddSpaceItems, subCodeArabicNumNoSpaceItems),

    // 中文小写数字
    subCodeChineseNumItems(subCodeChineseAddSpaceNumItems, subCodeChineseNoSpaceNumItems),

    // 英文小写字母
    subCodeLowercaseItems(subCodeLowercaseAddSpaceNumItems, subCodeLowercaseNoSpaceNumItems),

    // 英文大写字母
    subCodeUppercaseNumItems(subCodeUppercaseAddSpaceNumItems, subCodeUppercaseNoSpaceNumItems);

    private final SelectItemsEnum[] selectItemsEnums;

    SelectItemsEnums(SelectItemsEnum... selectItemsEnums) {
        this.selectItemsEnums = selectItemsEnums;
    }

    public SelectItemsEnum getSelectItemsEnum(int i) {
        return selectItemsEnums[i];
    }

    public ObservableList<String> getItems(int i) {
        return FXCollections.observableArrayList(selectItemsEnums[i].getItems());
    }

}
