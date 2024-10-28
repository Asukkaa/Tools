package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.ExcelConfigBean;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Utils.FileUtils.checkCopyDestination;
import static priv.koishi.tools.Utils.UiUtils.checkExcelParam;

/**
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午1:48
 */
public class FileNameToExcelService {

    /**
     * 构建输出文件名称的excel
     */
    public static Task<XSSFWorkbook> buildFileNameExcel(ExcelConfigBean excelConfigBean, TaskBean<FileBean> taskBean) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet;
        String excelInPath = excelConfigBean.getInPath();
        String sheetName = excelConfigBean.getSheet();
        if (excelInPath != null && !excelInPath.isEmpty()) {
            checkExcelParam(excelInPath);
            //输出路径与编辑路径不同先将要编辑的文件复制到输出路径
            checkCopyDestination(excelConfigBean);
            FileInputStream inputStream = new FileInputStream(excelInPath);
            workbook = new XSSFWorkbook(inputStream);
            if (sheetName == null || sheetName.isEmpty()) {
                sheet = workbook.getSheetAt(0);
            } else {
                sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    sheet = workbook.createSheet(sheetName);
                }
            }
            inputStream.close();
        } else {
            sheet = workbook.createSheet(sheetName);
        }
        //构建excel
        return buildNoGroupExcel(taskBean, excelConfigBean, sheet, workbook);
    }

    /**
     * 不分组构建excel
     */
    private static Task<XSSFWorkbook> buildNoGroupExcel(TaskBean<FileBean> taskBean, ExcelConfigBean excelConfigBean, XSSFSheet sheet, XSSFWorkbook workbook) {
        return new Task<>() {
            @Override
            protected XSSFWorkbook call() {
                updateMessage("正在导出数据");
                List<FileBean> fileBeans = taskBean.getBeanList();
                List<String> names = new ArrayList<>();
                fileBeans.forEach(fileBean -> names.add(fileBean.getName()));
                int startRowNum = excelConfigBean.getStartRowNum();
                int startCellNum = excelConfigBean.getStartCellNum();
                int nameSize = names.size();
                updateMessage("已识别到 " + nameSize + " 个文件");
                for (int i = 0; i < nameSize; i++) {
                    String name = names.get(i);
                    XSSFRow row = sheet.getRow(startRowNum);
                    if (row == null) {
                        row = sheet.createRow(startRowNum);
                    }
                    XSSFCell cell = row.createCell(startCellNum);
                    cell.setCellValue(name);
                    updateMessage("正在输出第 " + (i + 1) + "/" + nameSize + " 个文件：" + name + " 数据坐标：" + startRowNum + "," + startCellNum);
                    startRowNum++;
                }
                sheet.autoSizeColumn(startCellNum);
                //手动调整列宽，解决中文不能自适应问题,单元格单行最长支持255*256的宽度（每个单元格样式已经设置自动换行，超出即换行）,设置最低列宽度，列宽约六个中文字符
                int width = Math.max(15 * 256, Math.min(255 * 256, sheet.getColumnWidth(startCellNum) * 12 / 10));
                sheet.setColumnWidth(startCellNum, width);
                updateMessage("所有数据已输出完毕");
                return workbook;
            }
        };
    }

}
