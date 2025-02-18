package priv.koishi.tools.Bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

/**
 * 自动操作步骤类
 *
 * @author KOISHI
 * Date:2025-02-17
 * Time:17:45
 */
@Data
@Accessors(chain = true)
public class ClickPositionBean {

    /**
     * 唯一标识符
     */
    String uuid = UUID.randomUUID().toString();

    /**
     * 起始横（X）坐标
     */
    String startX;

    /**
     * 起始纵（Y）坐标
     */
    String startY;

    /**
     * 结束横（X）坐标
     */
    String endX;

    /**
     * 结束纵（Y）坐标
     */
    String endY;

    /**
     * 点击时长（单位：秒）
     */
    String clickTime;

    /**
     * 点击次数
     */
    String clickNum;

    /**
     * 操作执行前等待时间（单位：秒）
     */
    String waitTime;

    /**
     * 操作类型
     */
    String type;

}
