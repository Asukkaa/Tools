package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import priv.koishi.tools.Configuration.ExcelConfig;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.FileUtils.checkCopyDestination;
import static priv.koishi.tools.Utils.FileUtils.getFileType;

/**
 * @author KOISHI
 * Date:2024-10-17
 * Time:下午4:53
 */
public class ImgToExcelService {

    /**
     * 构建分组的图片excel
     */
    public static Task<SXSSFWorkbook> buildImgGroupExcel(TaskBean<FileNumBean> taskBean, ExcelConfig excelConfig) {
        return new Task<>() {
            @Override
            protected SXSSFWorkbook call() throws Exception {
                checkCopyDestination(excelConfig);
                File inputFile = new File(excelConfig.getInPath());
                if (!inputFile.exists()) {
                    throw new Exception(text_excelNotExists);
                }
                updateMessage(text_printData);
                FileInputStream inputStream = new FileInputStream(inputFile);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(workbook, 50);
                String sheetName = excelConfig.getSheet();
                int startRowNum = excelConfig.getStartRowNum();
                int startCellNum = excelConfig.getStartCellNum();
                List<FileNumBean> fileBeans = taskBean.getBeanList();
                int fileNum = fileBeans.size();
                updateMessage(text_identify + fileNum + text_data);
                XSSFSheet sheet;
                if (StringUtils.isBlank(sheetName)) {
                    sheet = sxssfWorkbook.getXSSFWorkbook().getSheetAt(0);
                } else {
                    sheet = sxssfWorkbook.getXSSFWorkbook().getSheet(sheetName);
                }
                for (int i = 0; i < fileNum; i++) {
                    FileNumBean fileBean = fileBeans.get(i);
                    List<String> imgList = fileBean.getFilePathList();
                    buildImgExcel(imgList, excelConfig, startCellNum, startRowNum, sheet, sxssfWorkbook);
                    updateMessage(text_printing + (i + 1) + "/" + fileNum + text_data);
                    updateProgress(i + 1, fileNum);
                    startRowNum++;
                }
                updateMessage(text_printDown);
                return sxssfWorkbook;
            }
        };
    }

    /**
     * 插入图片
     */
    private static void buildImgExcel(List<String> imgList, ExcelConfig excelConfig, int startCellNum,
                                      int startRowNum, XSSFSheet sheet, SXSSFWorkbook sxssfWorkbook) throws IOException {
        int imgWidth = excelConfig.getImgWidth();
        int imgHeight = excelConfig.getImgHeight();
        int cellNum = startCellNum;
        if (CollectionUtils.isEmpty(imgList)) {
            XSSFRow row = sheet.getRow(startRowNum);
            if (row == null) {
                row = sheet.createRow(startRowNum);
            }
            XSSFCell cell = row.getCell(startCellNum);
            if (cell == null) {
                cell = row.createCell(startCellNum);
            }
            if (excelConfig.isNoImg()) {
                cell.setCellValue("无图片");
            }
        } else {
            for (String i : imgList) {
                // 将图片插入Excel单元格
                CreationHelper helper = sxssfWorkbook.getCreationHelper();
                Drawing<XSSFShape> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                // 设置图片的起始位置以及大小
                anchor.setCol1(cellNum);
                anchor.setRow1(startRowNum);
                anchor.setCol2(cellNum + 1);
                anchor.setRow2(startRowNum + 1);
                anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
                String extension = getFileType(new File(i));
                // 读取图片文件
                InputStream inputStream = Files.newInputStream(Paths.get(i));
                if (jpg.equals(extension) || jpeg.equals(extension)) {
                    drawing.createPicture(anchor, sxssfWorkbook.addPicture(inputStream.readAllBytes(), Workbook.PICTURE_TYPE_JPEG));
                } else if (png.equals(extension)) {
                    drawing.createPicture(anchor, sxssfWorkbook.addPicture(inputStream.readAllBytes(), Workbook.PICTURE_TYPE_PNG));
                }
                sxssfWorkbook.setCompressTempFiles(true);
                inputStream.close();
                sheet.setColumnWidth(cellNum, imgWidth);
                sheet.getRow(startRowNum).setHeightInPoints(imgHeight);
                cellNum++;
            }
        }
    }

}
