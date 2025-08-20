package priv.koishi.tools.Configuration;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 拷贝文件配置
 *
 * @author KOISHI
 * Date:2025-08-19
 * Time:16:59
 */
@Data
@Accessors(chain = true)
public class CopyConfig {

    /**
     * 隐藏文件查询设置
     */
    String hideFileType;

    /**
     * 需要复制的文件拓展名
     */
    String filterFileType;

    /**
     * 反向过滤文件类型（true-反向过滤）
     */
    boolean reverseFileType;

    /**
     * 目录结构设置
     */
    String copyType;

    /**
     * 重名文件尾缀
     */
    String differenceCode;

    /**
     * 重名文件尾缀分隔符
     */
    String subCode;

    /**
     * 重名文件前缀
     */
    String prefix;

    /**
     * 重名文件起始尾缀
     */
    int tag;

    /**
     * 向分隔符左侧添加一个空格（true-添加空格）
     */
    boolean addSpace;

    /**
     * 目标文件夹位置
     */
    String outPath;

    /**
     * 复制数量
     */
    int copyNum;

    /**
     * 打开目标文件夹
     */
    boolean openDirectory;

}
