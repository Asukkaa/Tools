package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import javafx.scene.control.Control;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;

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
     * excel XSSFWorkbook工作簿
     */
    private static XSSFWorkbook workbook;

    /**
     * excel SXSSFWorkbook工作簿
     */
    private static SXSSFWorkbook sxssfWorkbook;

    /**
     * excel输入流
     */
    private static FileInputStream fileInputStream;

    /**
     * 图片输入流
     */
    private static InputStream inputStream;

    /**
     * 构建分组的图片excel
     */
    public static Task<SXSSFWorkbook> buildImgGroupExcel(TaskBean<FileNumBean> taskBean, ExcelConfig excelConfig) {
        return new Task<>() {
            @Override
            protected SXSSFWorkbook call() throws Exception {
                List<Control> disableControls = taskBean.getDisableControls();
                if (CollectionUtils.isNotEmpty(disableControls)) {
                    disableControls.forEach(dc -> dc.setDisable(true));
                }
                checkCopyDestination(excelConfig);
                File inputFile = new File(excelConfig.getInPath());
                if (!inputFile.exists()) {
                    throw new Exception(text_excelNotExists);
                }
                updateMessage(text_printData);
                fileInputStream = new FileInputStream(inputFile);
                workbook = new XSSFWorkbook(fileInputStream);
                sxssfWorkbook = new SXSSFWorkbook(workbook, 50);
                taskBean.getCancelButton().setVisible(true);
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
                    if (isCancelled()) {
                        closeStream();
                        break;
                    }
                    List<String> imgList = fileBeans.get(i).getFilePathList();
                    buildImgExcel(imgList, excelConfig, startCellNum, startRowNum, sheet, sxssfWorkbook);
                    updateMessage(text_printing + (i + 1) + "/" + fileNum + text_data);
                    updateProgress(i + 1, fileNum);
                    startRowNum++;
                }
                return sxssfWorkbook;
            }
        };
    }

    /**
     * 插入图片
     */
    private static void buildImgExcel(List<String> imgList, ExcelConfig excelConfig, int startCellNum,
                                      int startRowNum, XSSFSheet sheet, SXSSFWorkbook sxssfWorkbook) throws IOException {
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
            sxssfWorkbook.setCompressTempFiles(true);
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
                inputStream = Files.newInputStream(Paths.get(i));
                try {
                    if (jpg.equals(extension) || jpeg.equals(extension)) {
                        drawing.createPicture(anchor, sxssfWorkbook.addPicture(inputStream.readAllBytes(), Workbook.PICTURE_TYPE_JPEG));
                    }
                    if (png.equals(extension)) {
                        drawing.createPicture(anchor, sxssfWorkbook.addPicture(inputStream.readAllBytes(), Workbook.PICTURE_TYPE_PNG));
                    }
                } finally {
                    inputStream.close();
                }
                sheet.setColumnWidth(cellNum, 256 * excelConfig.getImgWidth());
                sheet.getRow(startRowNum).setHeightInPoints(excelConfig.getImgHeight());
                cellNum++;
            }
        }
    }

    /**
     * 线程取消时关闭所有流
     */
    public static void closeStream() throws IOException {
        if (workbook != null) {
            workbook.close();
            workbook = null;
        }
        if (sxssfWorkbook != null) {
            sxssfWorkbook.close();
            sxssfWorkbook = null;
        }
        if (fileInputStream != null) {
            fileInputStream.close();
            fileInputStream = null;
        }
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
    }

}
