package priv.koishi.tools.Utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Configuration.ExcelConfig;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Utils.FileUtils.checkDirectory;

/**
 * excel相关工具类
 *
 * @author KOISHI
 * Date:2024-12-11
 * Time:14:33
 */
public class ExcelUtils {

    /**
     * 多列单元格自适应
     *
     * @param sheet        要处理的表
     * @param maxCellNum   最大列数
     * @param startCellNum 要处理的起始列
     */
    public static void autoSizeExcelCells(Sheet sheet, int maxCellNum, int startCellNum) {
        for (int i = startCellNum; i < maxCellNum; i++) {
            autoSizeExcelCell(sheet, i);
        }
    }

    /**
     * 单列单元格自适应
     *
     * @param sheet   要处理的表
     * @param cellNum 要处理的列
     */
    public static void autoSizeExcelCell(Sheet sheet, int cellNum) {
        sheet.autoSizeColumn(cellNum);
        // 手动调整列宽，解决中文不能自适应问题,单元格单行最长支持255*256的宽度（每个单元格样式已经设置自动换行，超出即换行）,设置最低列宽度，列宽约六个中文字符
        int width = Math.max(15 * 256, Math.min(255 * 256, sheet.getColumnWidth(cellNum) * 12 / 10));
        sheet.setColumnWidth(cellNum, width);
    }

    /**
     * 获取excel行
     *
     * @param sheet  要处理的表
     * @param rowNum 要获取的行号
     * @return 根据行号获取或创建的行
     */
    public static Row getOrCreateRow(Sheet sheet, int rowNum) {
        Row row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
        }
        return row;
    }

    /**
     * 获取excel单元格
     *
     * @param cellNum 要获取的列号
     * @param row     单元格所在的行
     * @return 根据列号获取或创建的单元格
     */
    public static Cell getOrCreateCell(int cellNum, Row row) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) {
            cell = row.createCell(cellNum);
        }
        return cell;
    }

    /**
     * 构建excel表头
     *
     * @param sheet   要处理的表
     * @param rowNum  表头所在行号
     * @param titles  表头数据
     * @param cellNum 表头起始列号
     * @return 表头所在行的下一行行号
     */
    public static int buildExcelTitle(Sheet sheet, int rowNum, List<String> titles, int cellNum) {
        Row row = getOrCreateRow(sheet, rowNum);
        for (int i = 0; i < titles.size(); i++) {
            Cell cell = row.createCell(cellNum + i);
            cell.setCellValue(titles.get(i));
        }
        rowNum++;
        return rowNum;
    }

    /**
     * 构建表头
     *
     * @param exportFileNum  导出文件数量标识，true导出，false不导出
     * @param exportFileSize 导出文件大小标识，true导出，false不导出
     * @param isImg          导出图片标识，true导出的是图片，false导出的是文件名
     * @return 表头数据list
     */
    public static List<String> buildTitles(boolean exportFileNum, boolean exportFileSize, boolean isImg) {
        List<String> titles = new ArrayList<>();
        // 判断导出图片还是文件名
        if (isImg) {
            titles.add("匹配的图片");
        } else {
            titles.add("文件名称");
        }
        // 判断附加项导出设置
        if (exportFileNum && !exportFileSize) {
            // 附加项只导出文件数量
            titles.addFirst("文件数量");
        } else if (!exportFileNum && exportFileSize) {
            // 附加项只导出文件大小
            titles.addFirst("文件总大小");
        } else if (exportFileNum) {
            // 附加项导出文件数量和大小
            titles.addFirst("文件总大小");
            titles.addFirst("文件数量");
        }
        return titles;
    }

    /**
     * 校验excel输出路径是否与模板一致，若不一致则复制一份模板文件到输出路径
     *
     * @param excelConfig excel设置
     * @throws IOException 文件设置可写失败
     */
    public static void checkCopyDestination(ExcelConfig excelConfig) throws IOException {
        String excelInPath = excelConfig.getInPath();
        String outPath = excelConfig.getOutPath();
        String excelName = excelConfig.getOutName();
        String outExcelExtension = excelConfig.getOutExcelType();
        Path sourcePath = Paths.get(excelInPath);
        checkDirectory(outPath);
        String path = outPath + "\\" + excelName + outExcelExtension;
        Path destinationPath = Paths.get(path);
        if (!excelInPath.equals(path)) {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            File destinationPathFile = new File(String.valueOf(destinationPath));
            if (!destinationPathFile.canWrite()) {
                if (!destinationPathFile.setWritable(true)) {
                    throw new RuntimeException("文件 " + destinationPath + " 设置为可写失败");
                }
            }
        }
    }

    /**
     * 保存excel
     *
     * @param workbook    excel工作簿
     * @param excelConfig excel设置
     * @return excel保存路径
     * @throws IOException io异常
     */
    public static String saveExcel(Workbook workbook, ExcelConfig excelConfig) throws IOException {
        String filePath = excelConfig.getOutPath() + File.separator + excelConfig.getOutName() + excelConfig.getOutExcelType();
        Path path = Paths.get(filePath);
        // 处理HSSFWorkbook
        if (workbook instanceof HSSFWorkbook) {
            // 直接保存HSSFWorkbook
            checkDirectory(new File(filePath).getParent());
            try (BufferedOutputStream bos = new BufferedOutputStream(
                    Files.newOutputStream(path), 65536)) {
                workbook.write(bos);
            }
            return filePath;
        }
        // 处理SXSSFWorkbook和XSSFWorkbook
        try (SXSSFWorkbook sxssfWorkbook = (workbook instanceof SXSSFWorkbook) ? (SXSSFWorkbook) workbook :
                new SXSSFWorkbook((XSSFWorkbook) workbook, 100, true, true)) {
            checkDirectory(new File(filePath).getParent());
            try (BufferedOutputStream bos = new BufferedOutputStream(
                    Files.newOutputStream(path), 65536)) {
                sxssfWorkbook.write(bos);
            }
        }
        return filePath;
    }

}
