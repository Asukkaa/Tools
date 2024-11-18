package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Configuration.ExcelConfig;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Utils.FileUtils.checkCopyDestination;
import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.UiUtils.checkExcelParam;
import static priv.koishi.tools.Utils.UiUtils.setDisableControls;

/**
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午1:48
 */
public class FileNameToExcelService {

    /**
     * 构建输出文件名称的excel
     */
    public static Task<SXSSFWorkbook> buildFileNameExcel(ExcelConfig excelConfig, TaskBean<FileBean> taskBean) throws Exception {
        setDisableControls(taskBean, true);
        XSSFWorkbook workbook = new XSSFWorkbook();
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(workbook);
        XSSFSheet sheet;
        String excelInPath = excelConfig.getInPath();
        String sheetName = excelConfig.getSheet();
        if (excelInPath != null && !excelInPath.isEmpty()) {
            checkExcelParam(excelInPath);
            //输出路径与编辑路径不同先将要编辑的文件复制到输出路径
            checkCopyDestination(excelConfig);
            workbook = new XSSFWorkbook(new FileInputStream(excelInPath));
            sxssfWorkbook = new SXSSFWorkbook(workbook);
            if (sheetName == null || sheetName.isEmpty()) {
                sheet = sxssfWorkbook.getXSSFWorkbook().getSheetAt(0);
            } else {
                sheet = sxssfWorkbook.getXSSFWorkbook().getSheet(sheetName);
                if (sheet == null) {
                    sheet = sxssfWorkbook.getXSSFWorkbook().createSheet(sheetName);
                }
            }
        } else {
            sheet = sxssfWorkbook.getXSSFWorkbook().createSheet(sheetName);
        }
        //构建excel
        return buildNoGroupExcel(taskBean, excelConfig, sheet, sxssfWorkbook);
    }

    /**
     * 不分组构建excel
     */
    private static Task<SXSSFWorkbook> buildNoGroupExcel(TaskBean<FileBean> taskBean, ExcelConfig excelConfig, XSSFSheet sheet, SXSSFWorkbook workbook) {
        return new Task<>() {
            @Override
            protected SXSSFWorkbook call() {
                setDisableControls(taskBean, true);
                updateMessage(text_printData);
                List<String> names = new ArrayList<>();
                taskBean.getBeanList().forEach(fileBean -> names.add(fileBean.getName()));
                int startRowNum = excelConfig.getStartRowNum();
                int startCellNum = excelConfig.getStartCellNum();
                int nameSize = names.size();
                updateMessage(text_identify + nameSize + text_file);
                for (int i = 0; i < nameSize; i++) {
                    String name = names.get(i);
                    XSSFRow row = sheet.getRow(startRowNum);
                    if (row == null) {
                        row = sheet.createRow(startRowNum);
                    }
                    XSSFCell cell = row.createCell(startCellNum);
                    cell.setCellValue(name);
                    updateMessage(text_printing + (i + 1) + "/" + nameSize + text_file + name + text_coordinate + startRowNum + "," + startCellNum);
                    startRowNum++;
                }
                sheet.autoSizeColumn(startCellNum);
                //手动调整列宽，解决中文不能自适应问题,单元格单行最长支持255*256的宽度（每个单元格样式已经设置自动换行，超出即换行）,设置最低列宽度，列宽约六个中文字符
                int width = Math.max(15 * 256, Math.min(255 * 256, sheet.getColumnWidth(startCellNum) * 12 / 10));
                sheet.setColumnWidth(startCellNum, width);
                updateMessage(text_printDown);
                return workbook;
            }
        };
    }

}
