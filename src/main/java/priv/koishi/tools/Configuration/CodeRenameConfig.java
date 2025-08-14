package priv.koishi.tools.Configuration;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 按变编号重命名设置参数类
 *
 * @author KOISHI
 * Date:2024-11-05
 * Time:下午2:06
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CodeRenameConfig extends Configuration {

    /**
     * 文件名起始编号
     */
    int startName;

    /**
     * 文件始编号位数
     */
    int startSize;

    /**
     * 相同编号文件数量
     */
    int nameNum;

    /**
     * 区分编码类型
     */
    String differenceCode;

    /**
     * 分隔符
     */
    String subCode;

    /**
     * 向分隔符左侧添加一个空格
     */
    boolean addSpace;

    /**
     * 每个编码起始尾缀值
     */
    int tag;

    /**
     * 文件名前缀
     */
    String prefix;

}
