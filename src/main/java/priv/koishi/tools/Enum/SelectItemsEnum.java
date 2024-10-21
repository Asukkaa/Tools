package priv.koishi.tools.Enum;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author KOISHI
 * Date:2024-10-21
 * Time:下午12:12
 */
public enum SelectItemsEnum {

    //有空格的阿拉伯数字
    subCodeArabicNumAddSpaceItems("英文括号：01 (1).xx", "中文括号：01 （1）.xx", "英文横杠：01 -1.xx", "中文横杠：01 —1.xx"),
    //没空格的阿拉伯数字
    subCodeArabicNumNoSpaceItems("英文括号：01(1).xx", "中文括号：01（1）.xx", "英文横杠：01-1.xx", "中文横杠：01—1.xx"),
    //有空格的中文小写数字
    subCodeChineseAddSpaceNumItems("英文括号：01 (一).xx", "中文括号：01 （一）.xx", "英文横杠：01 -一.xx", "中文横杠：01 —一.xx"),
    //没空格的中文小写数字
    subCodeChineseNoSpaceNumItems("英文括号：01(一).xx", "中文括号：01（一）.xx", "英文横杠：01-一.xx", "中文横杠：01—一.xx"),
    //有空格的英文小写字母
    subCodeLowercaseAddSpaceNumItems("英文括号：01 (a).xx", "中文括号：01 （a）.xx", "英文横杠：01 -a.xx", "中文横杠：01 —a.xx"),
    //没空格的英文小写字母
    subCodeLowercaseNoSpaceNumItems("英文括号：01(a).xx", "中文括号：01（a）.xx", "英文横杠：01-a.xx", "中文横杠：01—a.xx"),
    //有空格的英文大写字母
    subCodeUppercaseAddSpaceNumItems("英文括号：01 (A).xx", "中文括号：01 （A）.xx", "英文横杠：01 -A.xx", "中文横杠：01 —A.xx"),
    //没空格的英文大写字母
    subCodeUppercaseNoSpaceNumItems("英文括号：01(A)xx", "中文括号：01（A）.xx", "英文横杠：01-A.xx", "中文横杠：01—A.xx");

    private final String[] items;

    SelectItemsEnum(String... items) {
        this.items = items;
    }

    public ObservableList<String> getItems() {
        return FXCollections.observableArrayList(items);
    }

    public String getValue(int i) {
        return items[i];
    }

}
