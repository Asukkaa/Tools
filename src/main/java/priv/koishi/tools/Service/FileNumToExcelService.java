package priv.koishi.tools.Service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.ExcelConfigBean;
import priv.koishi.tools.Bean.FileNumBean;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static priv.koishi.tools.Utils.CommonUtils.autoSizeExcel;
import static priv.koishi.tools.Utils.FileUtils.checkCopyDestination;

/**
 * @author KOISHI
 * Date:2024-10-09
 * Time:下午4:17
 */
public class FileNumToExcelService {

    /**
     * 构建分组统计excel
     */
    public static XSSFWorkbook buildNameGroupNumExcel(List<FileNumBean> fileBeans, ExcelConfigBean excelConfigBean) throws Exception {
        checkCopyDestination(excelConfigBean);
        File inputFile = new File(excelConfigBean.getInPath());
        if (!inputFile.exists()) {
            throw new Exception("模板excel文件不存在");
        }
        FileInputStream inputStream = new FileInputStream(inputFile);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        String sheetName = excelConfigBean.getSheet();
        String exportType = excelConfigBean.getExportType();
        int startRowNum = excelConfigBean.getStartRowNum();
        int startCellNum = excelConfigBean.getStartCellNum();
        int maxCellNum = startCellNum;
        Label logLabel = excelConfigBean.getLogLabel();
        logLabel.setText("已识别到 " + fileBeans.size() + " 组数据");
        XSSFSheet sheet;
        if (StringUtils.isBlank(sheetName)) {
            sheet = workbook.getSheetAt(0);
        } else {
            sheet = workbook.getSheet(sheetName);
        }
        for (FileNumBean fileBean : fileBeans) {
            int groupNumber = fileBean.getGroupNumber();
            List<String> fileNameList = fileBean.getFileNameList();
            XSSFRow row = sheet.getRow(startRowNum);
            if (row == null) {
                row = sheet.createRow(startRowNum);
            }
            XSSFCell numCell = row.createCell(startCellNum);
            switch (exportType) {
                case "文件数量": {
                    numCell.setCellValue(groupNumber);
                    break;
                }
                case "文件名称": {
                    maxCellNum = buildFileNameExcel(fileNameList, row, startCellNum, maxCellNum);
                    break;
                }
                case "文件数量和名称": {
                    numCell.setCellValue(groupNumber);
                    maxCellNum = buildFileNameExcel(fileNameList, row, startCellNum + 1, maxCellNum);
                    break;
                }
            }
            logLabel.setText("正在输出第" + (startRowNum + 1) + "/" + fileBeans.size() + "组数据");
            startRowNum++;
        }
        logLabel.setText("所有数据已输出完毕");
        logLabel.setTextFill(Color.GREEN);
        autoSizeExcel(sheet, maxCellNum, startCellNum);
        inputStream.close();
        return workbook;
    }

    /**
     * 处理文件名称
     */
    private static int buildFileNameExcel(List<String> fileNameList, XSSFRow row, int cellNum, int maxCellNum) {
        for (String s : fileNameList) {
            XSSFCell nameCell = row.createCell(cellNum);
            nameCell.setCellValue(s);
            cellNum++;
            if (cellNum > maxCellNum) {
                maxCellNum = cellNum;
            }
        }
        return maxCellNum;
    }

    public void simpleRead(ExcelConfigBean excelConfigBean) {
        String fileName = excelConfigBean.getInPath();
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        EasyExcel.read(fileName, FileNumBean.class, new FileNumBeanListener()).sheet(excelConfigBean.getSheet()).doRead();
    }

    public static class FileNumBeanListener extends AnalysisEventListener<FileNumBean> {
        @Override
        public void invoke(FileNumBean data, AnalysisContext context) {
            System.out.println("读取到数据：" + data);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 所有数据解析完成后做的事情
        }
    }

}
