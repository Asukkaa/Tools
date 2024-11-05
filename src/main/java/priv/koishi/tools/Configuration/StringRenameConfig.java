package priv.koishi.tools.Configuration;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author KOISHI
 * Date:2024-11-05
 * Time:下午2:10
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class StringRenameConfig extends Configuration {

    /**
     * 匹配字符规则
     */
    String targetStr;

    /**
     * 要匹配的字符串
     */
    String renameValue;

    /**
     * 重命名方法
     */
    String renameBehavior;

    /**
     * 重命名字符串
     */
    String renameStr;

    /**
     * 左侧字符坐标
     */
    int before;

    /**
     * 右侧字符坐标
     */
    int after;

    /**
     * 左侧重命名方法
     */
    String beforeBehavior;

    /**
     * 右侧重命名方法
     */
    String afterBehavior;

    /**
     * 左侧输入字符
     */
    String beforeValue;

    /**
     * 右侧输入字符
     */
    String afterValue;

}
