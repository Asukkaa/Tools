package priv.koishi.tools.Bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * javafx列表展示文件分组数量信息类
 *
 * @author KOISHI
 * Date:2024-10-09
 * Time:下午4:05
 */
@Data
@Accessors(chain = true)
public class FileNumBean implements Indexable {

    /**
     * 序号
     */
    Integer index;

    /**
     * 分组序号
     */
    int groupId;

    /**
     * 分组名称
     */
    String groupName;

    /**
     * 文件数量
     */
    int groupNumber;

    /**
     * 文件名称列表字符串
     */
    String fileName;

    /**
     * 带单位的文件总大小
     */
    String fileUnitSize;

    /**
     * 文件总大小
     */
    long fileSize;

    /**
     * 文件路径列表
     */
    List<String> filePathList;

    /**
     * 文件名称列表
     */
    List<String> fileNameList;

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
