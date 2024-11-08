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
    int left;

    /**
     * 右侧字符坐标
     */
    int right;

    /**
     * 左侧重命名方法
     */
    String leftBehavior;

    /**
     * 右侧重命名方法
     */
    String rightBehavior;

    /**
     * 左侧输入字符
     */
    String leftValue;

    /**
     * 右侧输入字符
     */
    String rightValue;

}
