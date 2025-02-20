package priv.koishi.tools.Bean;

import javafx.scene.control.*;
import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.tools.Configuration.Configuration;

import java.io.File;
import java.util.List;

/**
 * 多线程任务所需设置类
 *
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
    TableColumn<T, String> comparatorTableColumn;

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
     * 文件批量重命名功能的设置
     */
    Configuration configuration;

    /**
     * 读取重命名功能excel模板标志
     */
    boolean returnRenameList;

    /**
     * 取消线程按钮
     */
    Button cancelButton;

    /**
     * 要防重复点击的组件
     */
    List<Control> disableControls;

    /**
     * 默认排序类型
     */
    String sortType;

    /**
     * 是否倒序排序
     */
    boolean reverseSort;

    /**
     * 自动点击任务循环次数
     */
    int loopTime;

    /**
     * 执行自动流程前点击第一个起始坐标
     */
    boolean firstClick;

}
