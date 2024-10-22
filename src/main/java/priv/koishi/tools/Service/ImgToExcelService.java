package priv.koishi.tools.Service;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.*;
import priv.koishi.tools.Bean.ExcelConfigBean;
import priv.koishi.tools.Bean.FileNumBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static priv.koishi.tools.Utils.FileUtils.checkCopyDestination;
import static priv.koishi.tools.Utils.FileUtils.getFileType;

/**
 * @author KOISHI
 * Date:2024-10-17
 * Time:下午4:53
 */
public class ImgToExcelService {

    /**
     * excel输出起始行
     */
    static int startRowNum;

    /**
     * excel输出起始列
     */
    static int startCellNum;

    /**
     * 要输出的表
     */
    static XSSFSheet sheet;

    /**
     * 要输出的excel文件
     */
    static XSSFWorkbook workbook;

    /**
     * 日志输出栏
     */
    static Label logLabel;

    /**
     * 构建分组的图片excel
     */
    public static XSSFWorkbook buildImgGroupExcel(List<FileNumBean> fileBeans, ExcelConfigBean excelConfigBean) throws Exception {
        checkCopyDestination(excelConfigBean);
        File inputFile = new File(excelConfigBean.getInPath());
        if (!inputFile.exists()) {
            throw new Exception("模板excel文件不存在");
        }
        FileInputStream inputStream = new FileInputStream(inputFile);
        workbook = new XSSFWorkbook(inputStream);
        String sheetName = excelConfigBean.getSheet();
        startRowNum = excelConfigBean.getStartRowNum();
        startCellNum = excelConfigBean.getStartCellNum();
        logLabel = excelConfigBean.getLogLabel();
        logLabel.setText("已识别到 " + fileBeans.size() + " 组数据");
        if (StringUtils.isBlank(sheetName)) {
            sheet = workbook.getSheetAt(0);
        } else {
            sheet = workbook.getSheet(sheetName);
        }
        for (FileNumBean fileBean : fileBeans) {
            List<String> imgList = fileBean.getFilePathList();
            buildImgExcel(imgList, excelConfigBean);
            logLabel.setText("正在输出第" + (startRowNum + 1) + "/" + fileBeans.size() + "组数据");
            startRowNum++;
        }
        logLabel.setText("所有数据已输出完毕");
        logLabel.setTextFill(Color.GREEN);
        return workbook;
    }

    /**
     * 插入图片
     */
    private static void buildImgExcel(List<String> imgList, ExcelConfigBean excelConfigBean) {
        int imgWidth = excelConfigBean.getImgWidth();
        int imgHeight = excelConfigBean.getImgHeight();
        int cellNum = startCellNum;
        if (imgList.isEmpty()) {
            XSSFRow row = sheet.getRow(startRowNum);
            if (row == null) {
                row = sheet.createRow(startRowNum);
            }
            XSSFCell cell = row.createCell(startCellNum);
            cell.setCellValue("无图片");
        } else {
            for (String i : imgList) {
                // 读取图片文件
                InputStream inputStream;
                byte[] bytes;
                try {
                    inputStream = Files.newInputStream(Paths.get(i));
                    bytes = IOUtils.toByteArray(inputStream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // 将图片插入Excel单元格
                CreationHelper helper = workbook.getCreationHelper();
                Drawing<XSSFShape> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                // 设置图片的起始位置以及大小
                anchor.setCol1(cellNum);
                anchor.setRow1(startRowNum);
                anchor.setCol2(cellNum + 1);
                anchor.setRow2(startRowNum + 1);
                anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
                String extension = getFileType(new File(i));
                if (".jpg".equals(extension) || ".jpeg".equals(extension)) {
                    drawing.createPicture(anchor, workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG));
                } else if (".png".equals(extension)) {
                    drawing.createPicture(anchor, workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG));
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logLabel.setTextFill(Color.RED);
                    throw new RuntimeException(e);
                }
                sheet.setColumnWidth(cellNum, imgWidth);
                sheet.getRow(startRowNum).setHeightInPoints(imgHeight);
                cellNum++;
            }
        }
    }

}
