package priv.koishi.tools.Utils;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KOISHI
 * Date:2024-12-11
 * Time:14:33
 */
public class ExcelUtils {

    /**
     * 多列单元格自适应
     */
    public static void autoSizeExcelCells(XSSFSheet sheet, int maxCellNum, int startCellNum) {
        for (int i = startCellNum; i < maxCellNum; i++) {
            autoSizeExcelCell(sheet, i);
        }
    }

    /**
     * 单列单元格自适应
     */
    public static void autoSizeExcelCell(XSSFSheet sheet, int cellNum) {
        sheet.autoSizeColumn(cellNum);
        //手动调整列宽，解决中文不能自适应问题,单元格单行最长支持255*256的宽度（每个单元格样式已经设置自动换行，超出即换行）,设置最低列宽度，列宽约六个中文字符
        int width = Math.max(15 * 256, Math.min(255 * 256, sheet.getColumnWidth(cellNum) * 12 / 10));
        sheet.setColumnWidth(cellNum, width);
    }

    /**
     * 获取excel行
     */
    public static XSSFRow getOrCreateRow(XSSFSheet sheet, int rowNum) {
        XSSFRow row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
        }
        return row;
    }

    /**
     * 获取excel单元格
     */
    public static XSSFCell getOrCreateCell(int startCellNum, XSSFRow row) {
        XSSFCell cell = row.getCell(startCellNum);
        if (cell == null) {
            cell = row.createCell(startCellNum);
        }
        return cell;
    }

    /**
     * 构建excel表头
     */
    public static int buildExcelTitle(XSSFSheet sheet, int rowNum, List<String> titles, int startCellNum) {
        XSSFRow row = getOrCreateRow(sheet, rowNum);
        for (int i = 0; i < titles.size(); i++) {
            XSSFCell cell = row.createCell(startCellNum + i);
            cell.setCellValue(titles.get(i));
        }
        rowNum++;
        return rowNum;
    }

    /**
     * 构建表头
     */
    public static List<String> buildTitles(boolean exportFileNum, boolean exportFileSize, boolean isImg) {
        List<String> titles = new ArrayList<>();
        //判断导出图片还是文件名
        if (isImg) {
            titles.add("匹配的图片");
        } else {
            titles.add("文件名称");
        }
        //附加项只导出文件数量
        if (exportFileNum && !exportFileSize) {
            titles.addFirst("文件数量");
        }
        //附加项只导出文件大小
        if (!exportFileNum && exportFileSize) {
            titles.addFirst("文件总大小");
        }
        //附加项导出文件数量和大小
        if (exportFileNum && exportFileSize) {
            titles.addFirst("文件总大小");
            titles.addFirst("文件数量");
        }
        return titles;
    }

}
