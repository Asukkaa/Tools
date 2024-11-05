package priv.koishi.tools.Bean;

import javafx.scene.control.*;
import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.tools.Configuration.Configuration;

import java.io.File;
import java.util.List;

/**
 * @author KOISHI
 * Date:2024-10-24
 * Time:下午3:15
 */
@Data
@Accessors(chain = true)
public class TaskBean<T> {

    /**
     * 要处理的文件夹文件
     */
    List<File> inFileList;

    /**
     * 要处理的数据
     */
    List<T> beanList;

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
    TableView<T> tableView;

    /**
     * 需要排序的列
     */
    TableColumn<T, String> tableColumn;

    /**
     * 线程进度条
     */
    ProgressBar progressBar;

    /**
     * 线程信息栏
     */
    Label massageLabel;

    /**
     * 页面标识符
     */
    String tabId;

    /**
     * 最大图片匹配数
     */
    int maxImgNum;

    /**
     * 查询查询按钮
     */
    Button reselectButton;

    Configuration configuration;

}
