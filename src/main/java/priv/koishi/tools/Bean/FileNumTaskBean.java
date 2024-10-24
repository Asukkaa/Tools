package priv.koishi.tools.Bean;

import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.List;

/**
 * @author KOISHI
 * Date:2024-10-24
 * Time:下午3:15
 */
@Data
@Accessors(chain = true)
public class FileNumTaskBean {

    /**
     * 要处理的文件夹文件
     */
    List<File> inFileList;

    /**
     * 文件名分隔符
     */
    String subCode;

    /**
     * 是否展示文件拓展名
     */
    boolean showFileType;

    /**
     * 用来更新数据的列表
     */
    TableView<FileNumBean> tableView;

    /**
     * 线程进度条
     */
    ProgressBar progressBar;

    /**
     * 页面标识符
     */
    String tabId;

}
