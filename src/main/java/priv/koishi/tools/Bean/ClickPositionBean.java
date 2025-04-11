package priv.koishi.tools.Bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ClickPositionBean implements Indexable {

    /**
     * 唯一标识符
     */
    @JsonIgnore
    String uuid = UUID.randomUUID().toString();

    /**
     * 序号
     */
    @JsonIgnore
    Integer index;

    /**
     * 操作名称
     */
    String name;

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
     * 点击时长（单位：毫秒）
     */
    String clickTime;

    /**
     * 操作次数
     */
    String clickNum;

    /**
     * 操作间隔时间（单位：毫秒）
     */
    String clickInterval;

    /**
     * 操作执行前等待时间（单位：毫秒）
     */
    String waitTime;

    /**
     * 操作类型
     */
    String type;

    /**
     * 删除标志
     */
    @JsonIgnore
    boolean remove;

    /**
     * 为列表数据设置序号接口
     *
     * @param index 要设置的序号
     */
    @Override
    public void setIndex(int index) {
        this.index = index;
    }

}
