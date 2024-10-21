package priv.koishi.tools.Service;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.ExcelConfigBean;
import priv.koishi.tools.Bean.FileNumBean;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Utils.CommonUtils.autoSizeExcel;
import static priv.koishi.tools.Utils.FileUtils.checkCopyDestination;
import static priv.koishi.tools.Utils.UiUtils.checkExcelParam;

/**
 * @author KOISHI
 * Date:2024-10-09
 * Time:下午4:17
 */
public class FileNumToExcelService {

    /**
     * 读取excel分组信息
     */
    public static List<FileNumBean> readExcel(ExcelConfigBean excelConfigBean) throws Exception {
        List<FileNumBean> fileNumBeanList = new ArrayList<>();
        String excelInPath = excelConfigBean.getInPath();
        String sheetName = excelConfigBean.getSheet();
        int readRow = excelConfigBean.getReadRowNum();
        int readCell = excelConfigBean.getReadCellNum();
        int maxRow = excelConfigBean.getMaxRowNum();
        checkExcelParam(excelInPath);
        FileInputStream inputStream = new FileInputStream(excelInPath);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet;
        if (StringUtils.isEmpty(sheetName)) {
            sheet = workbook.getSheetAt(0);
        } else {
            sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
            }
        }
        FileInputStream fileInputStream = new FileInputStream(excelConfigBean.getInPath());
        XSSFWorkbook excel = new XSSFWorkbook(fileInputStream);
        int lastRowNum = sheet.getLastRowNum();
        //获取有文字的最后一行行号
        for (int i = lastRowNum; i >= 0; i--) {
            XSSFRow row = sheet.getRow(i);
            //过滤中间的空单元格
            if (row != null) {
                XSSFCell cell = row.getCell(readCell);
                DataFormatter dataFormatter = new DataFormatter();
                String stringCellValue = dataFormatter.formatCellValue(cell);
                if (StringUtils.isNotBlank(stringCellValue)) {
                    lastRowNum = i;
                    break;
                }
            }
            if (i == 0 && lastRowNum != i){
                throw new Exception("未读取到excel模板分组信息");
            }
        }
        //获取要读取的最后一行
        if (maxRow == -1 || maxRow > lastRowNum) {
            maxRow = lastRowNum;
        } else {
            maxRow += readRow - 1;
        }
        int id = 0;
        for (int i = readRow; i <= maxRow; ++i) {
            XSSFRow row = sheet.getRow(i);
            FileNumBean fileNumBean = new FileNumBean();
            if (row != null) {
                XSSFCell cell = row.getCell(readCell);
                DataFormatter dataFormatter = new DataFormatter();
                String stringCellValue = dataFormatter.formatCellValue(cell);
                fileNumBean.setGroupName(stringCellValue);
            } else {
                fileNumBean.setGroupName("");
            }
            fileNumBean.setGroupId(++id);
            fileNumBeanList.add(fileNumBean);
        }
        excel.close();
        fileInputStream.close();
        return fileNumBeanList;
    }

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

}
