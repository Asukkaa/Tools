package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
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
import java.util.List;

import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.autoSizeExcel;
import static priv.koishi.tools.Utils.FileUtils.checkCopyDestination;
import static priv.koishi.tools.Utils.UiUtils.setDisableControls;

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
                setDisableControls(taskBean, true);
                checkCopyDestination(excelConfig);
                File inputFile = new File(excelConfig.getInPath());
                if (!inputFile.exists()) {
                    throw new Exception(text_excelNotExists);
                }
                updateMessage(text_printData);
                FileInputStream inputStream = new FileInputStream(inputFile);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(workbook);
                String sheetName = excelConfig.getSheet();
                String exportType = excelConfig.getExportType();
                int startRowNum = excelConfig.getStartRowNum();
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
                for (int i = 0; i < fileBeansSize; i++) {
                    FileNumBean fileBean = fileBeans.get(i);
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
                    updateMessage(text_printing + (i + 1) + "/" + fileBeansSize + text_data);
                    //Task的Progress(进度)更新方法,进度条的进度与该属性挂钩
                    updateProgress(i + 1, fileBeansSize);
                    startRowNum++;
                }
                autoSizeExcel(sheet, maxCellNum, startCellNum);
                updateMessage(text_printDown);
                return sxssfWorkbook;
            }
        };
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

//    public void simpleRead(ExcelConfigBean excelConfigBean) {
//        String fileName = excelConfigBean.getInPath();
//        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
//        EasyExcel.read(fileName, FileNumBean.class, new FileNumBeanListener()).sheet(excelConfigBean.getSheet()).doRead();
//    }
//
//    public static class FileNumBeanListener extends AnalysisEventListener<FileNumBean> {
//        @Override
//        public void invoke(FileNumBean data, AnalysisContext context) {
//            System.out.println("读取到数据：" + data);
//        }
//
//        @Override
//        public void doAfterAllAnalysed(AnalysisContext context) {
//            // 所有数据解析完成后做的事情
//        }
//    }

}
