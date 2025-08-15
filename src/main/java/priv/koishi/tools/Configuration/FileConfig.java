package priv.koishi.tools.Configuration;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.List;

/**
 * 文件读取设置参数类
 *
 * @author KOISHI
 * Date:2024-10-05
 * Time:下午1:39
 */
@Data
@Accessors(chain = true)
public class FileConfig {

    /**
     * 要读取的文件夹
     */
    File inFile;

    /**
     * 查询隐藏文件设置
     */
    String showHideFile;

    /**
     * 查询文件夹设置
     */
    String showDirectoryName;

    /**
     * 递归查询设置（true-开启递归）
     */
    boolean recursion;

    /**
     * 展示文件拓展名设置（true-展示拓展名）
     */
    boolean showFileType;

    /**
     * 文件类型过滤
     */
    List<String> filterExtensionList;

    /**
     * 最大图片匹配数
     */
    int maxImgNum;

    /**
     * 文件名称分割符
     */
    String subCode;

    /**
     * 文件名称筛选类型
     */
    String fileNameType;

    /**
     * 文件名称筛选字符串
     */
    String fileNameFilter;

    /**
     * 反向过滤文件名查询条件（true-反转查询）
     */
    boolean reverseFileName;

    /**
     * 反向过滤文件类型查询条件（true-反转查询）
     */
    boolean reverseFileType;

    /**
     * 文件名称筛选字符串大小写敏感（true-不区分大小写）
     */
    boolean filterNameCase;

}
