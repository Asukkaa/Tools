package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import priv.koishi.tools.Bean.ExcelConfigBean;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;

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
    public static Task<XSSFWorkbook> buildImgGroupExcel(TaskBean<FileNumBean> taskBean, ExcelConfigBean excelConfigBean) {
        return new Task<>() {
            @Override
            protected XSSFWorkbook call() throws Exception {
                checkCopyDestination(excelConfigBean);
                File inputFile = new File(excelConfigBean.getInPath());
                if (!inputFile.exists()) {
                    throw new Exception("模板excel文件不存在");
                }
                updateMessage("正在导出数据");
                FileInputStream inputStream = new FileInputStream(inputFile);
                workbook = new XSSFWorkbook(inputStream);
                String sheetName = excelConfigBean.getSheet();
                startRowNum = excelConfigBean.getStartRowNum();
                startCellNum = excelConfigBean.getStartCellNum();
                List<FileNumBean> fileBeans = taskBean.getBeanList();
                logLabel = taskBean.getMassageLabel();
                int fileNum = fileBeans.size();
                updateMessage("已识别到 " + fileNum + " 组数据");
                if (StringUtils.isBlank(sheetName)) {
                    sheet = workbook.getSheetAt(0);
                } else {
                    sheet = workbook.getSheet(sheetName);
                }
                for (int i = 0; i < fileNum; i++) {
                    FileNumBean fileBean = fileBeans.get(i);
                    List<String> imgList = fileBean.getFilePathList();
                    buildImgExcel(imgList, excelConfigBean);
                    updateMessage("正在输出第" + (i + 1) + "/" + fileNum + "组数据");
                    updateProgress(i + 1, fileNum);
                    startRowNum++;
                }
                updateMessage("所有数据已输出完毕");
                return workbook;
            }
        };
    }

    /**
     * 插入图片
     */
    private static void buildImgExcel(List<String> imgList, ExcelConfigBean excelConfigBean) throws IOException {
        int imgWidth = excelConfigBean.getImgWidth();
        int imgHeight = excelConfigBean.getImgHeight();
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
            if (excelConfigBean.isNoImg()) {
                cell.setCellValue("无图片");
            }
        } else {
            for (String i : imgList) {
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
                // 读取图片文件
                InputStream inputStream = Files.newInputStream(Paths.get(i));
                if (".jpg".equals(extension) || ".jpeg".equals(extension)) {
                    drawing.createPicture(anchor, workbook.addPicture(inputStream, Workbook.PICTURE_TYPE_JPEG));
                } else if (".png".equals(extension)) {
                    drawing.createPicture(anchor, workbook.addPicture(inputStream, Workbook.PICTURE_TYPE_PNG));
                }
                inputStream.close();
                sheet.setColumnWidth(cellNum, imgWidth);
                sheet.getRow(startRowNum).setHeightInPoints(imgHeight);
                cellNum++;
            }
        }
    }

}
