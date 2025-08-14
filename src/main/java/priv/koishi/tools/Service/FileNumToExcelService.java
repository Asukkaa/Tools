package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.ExcelUtils.*;
import static priv.koishi.tools.Utils.FileUtils.checkFileExists;
import static priv.koishi.tools.Utils.UiUtils.changeDisableNodes;

/**
 * 文件分组匹配数据导出excel功能线程任务类
 *
 * @author KOISHI
 * Date:2024-10-09
 * Time:下午4:17
 */
public class FileNumToExcelService {

    /**
     * 构建分组统计excel
     *
     * @param taskBean    线程任务所需参数
     * @param excelConfig excel导出设置参数
     * @return excel工作簿
     */
    public static Task<Workbook> buildNameGroupNumExcel(TaskBean<FileNumBean> taskBean, ExcelConfig excelConfig) {
        return new Task<>() {
            @Override
            protected Workbook call() throws IOException {
                // 改变要防重复点击的组件状态
                changeDisableNodes(taskBean, true);
                // 校验excel输出路径是否与模板一致，若不一致则复制一份模板文件到输出路径
                checkCopyDestination(excelConfig);
                String excelInPath = excelConfig.getInPath();
                checkFileExists(excelInPath, text_excelNotExists);
                updateMessage(text_printData);
                String excelType = excelConfig.getOutExcelType();
                Workbook workbook = null;
                String sheetName = excelConfig.getSheetName();
                Sheet sheet = null;
                if (xlsx.equals(excelType)) {
                    workbook = new XSSFWorkbook(new FileInputStream(excelInPath));
                    workbook = new SXSSFWorkbook((XSSFWorkbook) workbook);
                    if (StringUtils.isBlank(sheetName)) {
                        sheet = ((SXSSFWorkbook) workbook).getXSSFWorkbook().getSheetAt(0);
                    } else {
                        sheet = ((SXSSFWorkbook) workbook).getXSSFWorkbook().getSheet(sheetName);
                    }
                }
                if (xls.equals(excelType)) {
                    workbook = new HSSFWorkbook(new FileInputStream(excelInPath));
                    if (StringUtils.isBlank(sheetName)) {
                        sheet = workbook.getSheetAt(0);
                    } else {
                        sheet = workbook.getSheet(sheetName);
                    }
                }
                if (workbook == null) {
                    throw new RuntimeException("当前读取模板文件格式为 " + excelType + " 目前只支持读取 .xlsx 与 .xls 格式的文件");
                }
                int startRowNum = excelConfig.getStartRowNum();
                int rowNum = startRowNum;
                int startCellNum = excelConfig.getStartCellNum();
                int maxCellNum = startCellNum;
                List<FileNumBean> fileBeans = taskBean.getBeanList();
                updateMessage(text_identify + fileBeans.size() + text_data);
                int fileBeansSize = fileBeans.size();
                updateMessage(text_identify + fileBeansSize + text_data);
                boolean exportFileNum = excelConfig.isExportFileNum();
                boolean exportFileSize = excelConfig.isExportFileSize();
                List<String> titles = new ArrayList<>();
                // 创建表头
                if (excelConfig.isExportTitle()) {
                    titles = buildTitles(exportFileNum, exportFileSize, false);
                    rowNum = buildExcelTitle(sheet, startRowNum, titles, startCellNum);
                }
                for (int i = 0; i < fileBeansSize; i++) {
                    FileNumBean fileBean = fileBeans.get(i);
                    List<String> fileNameList = fileBean.getFileNameList();
                    Row row = getOrCreateRow(sheet, rowNum);
                    Cell startCell = row.createCell(startCellNum);
                    // 判断附加项导出设置
                    if (exportFileNum && !exportFileSize) {
                        startCell.setCellValue(fileBean.getGroupNumber());
                        maxCellNum = buildFileName(fileNameList, row, startCellNum + 1, maxCellNum);
                    } else if (!exportFileNum && exportFileSize) {
                        // 附加项只导出文件大小
                        startCell.setCellValue(fileBean.getFileUnitSize());
                        maxCellNum = buildFileName(fileNameList, row, startCellNum + 1, maxCellNum);
                    } else if (exportFileNum) {
                        // 附加项导出文件数量和大小
                        startCell.setCellValue(fileBean.getGroupNumber());
                        int sizeCellNum = startCellNum + 1;
                        Cell sizeCell = row.createCell(sizeCellNum);
                        sizeCell.setCellValue(fileBean.getFileUnitSize());
                        maxCellNum = buildFileName(fileNameList, row, sizeCellNum + 1, maxCellNum);
                    } else {
                        // 不导出附加项
                        maxCellNum = buildFileName(fileNameList, row, startCellNum, maxCellNum);
                    }
                    updateMessage(text_printing + (i + 1) + "/" + fileBeansSize + text_data);
                    updateProgress(i + 1, fileBeansSize);
                    rowNum++;
                }
                // 合并文件名表头单元格
                if (excelConfig.isExportTitle()) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum, startCellNum + titles.size() - 1, maxCellNum - 1));
                }
                autoSizeExcelCells(sheet, maxCellNum, startCellNum);
                updateMessage(text_printDown);
                return workbook;
            }
        };
    }

    /**
     * 处理文件名称列
     *
     * @param fileNameList 要处理文件名
     * @param row          当前所在excel行
     * @param cellNum      当前行起始列号
     * @param maxCellNum   最大列号
     * @return 最大列号
     */
    private static int buildFileName(List<String> fileNameList, Row row, int cellNum, int maxCellNum) {
        if (CollectionUtils.isNotEmpty(fileNameList)) {
            for (String s : fileNameList) {
                Cell nameCell = row.createCell(cellNum);
                nameCell.setCellValue(s);
                cellNum++;
                maxCellNum = Math.max(maxCellNum, cellNum);
            }
        }
        return maxCellNum;
    }

}
