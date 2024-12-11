package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.ExcelUtils.*;
import static priv.koishi.tools.Utils.FileUtils.checkCopyDestination;
import static priv.koishi.tools.Utils.UiUtils.changeDisableControls;

/**
 * @author KOISHI
 * Date:2024-10-09
 * Time:下午4:17
 */
public class FileNumToExcelService {

    /**
     * 构建分组统计excel
     */
    public static Task<SXSSFWorkbook> buildNameGroupNumExcel(TaskBean<FileNumBean> taskBean, ExcelConfig excelConfig) {
        return new Task<>() {
            @Override
            protected SXSSFWorkbook call() throws Exception {
                //改变要防重复点击的组件状态
                changeDisableControls(taskBean, true);
                //校验excel输出路径是否与模板一致，若不一致则复制一份模板文件到输出路径
                checkCopyDestination(excelConfig);
                File inputFile = new File(excelConfig.getInPath());
                if (!inputFile.exists()) {
                    throw new Exception(text_excelNotExists);
                }
                updateMessage(text_printData);
                XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(inputFile));
                SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(workbook);
                String sheetName = excelConfig.getSheet();
                int startRowNum = excelConfig.getStartRowNum();
                int rowNum = startRowNum;
                int startCellNum = excelConfig.getStartCellNum();
                int maxCellNum = startCellNum;
                List<FileNumBean> fileBeans = taskBean.getBeanList();
                updateMessage(text_identify + fileBeans.size() + text_data);
                XSSFSheet sheet;
                if (StringUtils.isBlank(sheetName)) {
                    sheet = sxssfWorkbook.getXSSFWorkbook().getSheetAt(0);
                } else {
                    sheet = sxssfWorkbook.getXSSFWorkbook().getSheet(sheetName);
                }
                int fileBeansSize = fileBeans.size();
                updateMessage(text_identify + fileBeansSize + text_data);
                boolean exportFileNum = excelConfig.isExportFileNum();
                boolean exportFileSize = excelConfig.isExportFileSize();
                List<String> titles = new ArrayList<>();
                //创建表头
                if (excelConfig.isExportTitle()) {
                    titles = buildTitles(exportFileNum, exportFileSize, false);
                    rowNum = buildExcelTitle(sheet, startRowNum, titles, startCellNum);
                }
                for (int i = 0; i < fileBeansSize; i++) {
                    FileNumBean fileBean = fileBeans.get(i);
                    List<String> fileNameList = fileBean.getFileNameList();
                    XSSFRow row = getOrCreateRow(sheet, rowNum);
                    XSSFCell startCell = row.createCell(startCellNum);
                    //附加项只导出文件数量
                    if (exportFileNum && !exportFileSize) {
                        startCell.setCellValue(fileBean.getGroupNumber());
                        maxCellNum = buildFileName(fileNameList, row, startCellNum + 1, maxCellNum);
                    }
                    //附加项只导出文件大小
                    if (!exportFileNum && exportFileSize) {
                        startCell.setCellValue(fileBean.getFileUnitSize());
                        maxCellNum = buildFileName(fileNameList, row, startCellNum + 1, maxCellNum);
                    }
                    //附加项导出文件数量和大小
                    if (exportFileNum && exportFileSize) {
                        startCell.setCellValue(fileBean.getGroupNumber());
                        int sizeCellNum = startCellNum + 1;
                        XSSFCell sizeCell = row.createCell(sizeCellNum);
                        sizeCell.setCellValue(fileBean.getFileUnitSize());
                        maxCellNum = buildFileName(fileNameList, row, sizeCellNum + 1, maxCellNum);
                    }
                    //不导出附加项
                    if (!exportFileNum && !exportFileSize) {
                        maxCellNum = buildFileName(fileNameList, row, startCellNum, maxCellNum);
                    }
                    updateMessage(text_printing + (i + 1) + "/" + fileBeansSize + text_data);
                    updateProgress(i + 1, fileBeansSize);
                    rowNum++;
                }
                //合并文件名表头单元格
                if (excelConfig.isExportTitle()) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum, startCellNum + titles.size() - 1, maxCellNum - 1));
                }
                autoSizeExcelCells(sheet, maxCellNum, startCellNum);
                updateMessage(text_printDown);
                return sxssfWorkbook;
            }
        };
    }

    /**
     * 处理文件名称列
     */
    private static int buildFileName(List<String> fileNameList, XSSFRow row, int cellNum, int maxCellNum) {
        if (CollectionUtils.isNotEmpty(fileNameList)) {
            for (String s : fileNameList) {
                XSSFCell nameCell = row.createCell(cellNum);
                nameCell.setCellValue(s);
                cellNum++;
                maxCellNum = Math.max(maxCellNum, cellNum);
            }
        }
        return maxCellNum;
    }

}
