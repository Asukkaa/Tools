package priv.koishi.tools.CustomUI.FileTreeItem;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 文件树选择器监听器
 *
 * @author KOISHI
 * Date:2025-08-04
 * Time:20:11
 */
public interface SelectionListener {

    /**
     * 文件选择器确认选中监听器
     *
     * @param selectedFiles 选中的文件
     * @throws IOException 抛出IO异常
     */
    void onFilesSelected(List<File> selectedFiles) throws IOException;

}
