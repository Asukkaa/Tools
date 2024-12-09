package priv.koishi.tools.Vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author KOISHI
 * Date:2024-12-09
 * Time:15:49
 */
@Data
@Accessors(chain = true)
public class FileNumVo {

    /**
     * 所匹配的数据数量
     */
    int dataNum;

    /**
     * 所匹配的图片数量
     */
    int imgNum;

    /**
     * 所匹配的图片大小
     */
    String imgSize;

}
