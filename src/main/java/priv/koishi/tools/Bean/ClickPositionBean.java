package priv.koishi.tools.Bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

/**
 * javafx列表展示自动鼠标点击步骤类
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
     * 鼠标点击横（X）坐标
     */
    String x;

    /**
     * 鼠标点击横（Y）坐标
     */
    String y;

    /**
     * 等待时间
     */
    String waitTime;

    /**
     * 鼠标点击类型
     */
    String type;

}
