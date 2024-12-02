package priv.koishi.tools.Configuration;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午2:08
 */
@Data
@Accessors(chain = true)
public class ExcelConfig {

    /**
     * 模板文件路径
     */
    String inPath;

    /**
     * 导出文件夹路径
     */
    String outPath;

    /**
     * 导出文件名称
     */
    String outName;

    /**
     * 导出文件sheet名称
     */
    String sheet;

    /**
     * 导出文件拓展名
     */
    String outExcelExtension;

    /**
     * 导出文件起始行
     */
    int startRowNum;

    /**
     * 导出文件起始列
     */
    int startCellNum;

    /**
     * 读取模板起始行
     */
    int readRowNum;

    /**
     * 读取模板起始列
     */
    int readCellNum;

    /**
     * 读取模板最大行数
     */
    int maxRowNum;

    /**
     * 导出excel文件内容
     */
    String exportType;

    /**
     * 导出excel图片宽
     */
    int imgWidth;

    /**
     * 导出excel图片高
     */
    int imgHeight;

    /**
     * 是否标记无图片
     */
    boolean noImg;

    /**
     * 导出表头设置
     */
    boolean exportTitle;

    /**
     * 导出完整表设置
     */
    boolean exportFullList;

}
