package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.ExcelUtils.*;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.UiUtils.changeDisableControls;

/**
 * 向excel插入匹配图片线程任务
 *
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
     *
     * @param taskBean    线程设置参数
     * @param excelConfig excel导出设置
     * @return excel工作簿
     */
    public static Task<SXSSFWorkbook> buildImgGroupExcel(TaskBean<FileNumBean> taskBean, ExcelConfig excelConfig) {
        return new Task<>() {
            @Override
            protected SXSSFWorkbook call() throws Exception {
                //改变要防重复点击的组件状态
                changeDisableControls(taskBean, true);
                //校验excel输出路径是否与模板一致，若不一致则复制一份模板文件到输出路径
                checkCopyDestination(excelConfig);
                String excelInPath = excelConfig.getInPath();
                checkFileExists(excelInPath, text_excelNotExists);
                updateMessage(text_printData);
                fileInputStream = new FileInputStream(excelInPath);
                workbook = new XSSFWorkbook(fileInputStream);
                sxssfWorkbook = new SXSSFWorkbook(workbook, 50);
                taskBean.getCancelButton().setVisible(true);
                String sheetName = excelConfig.getSheetName();
                int startRowNum = excelConfig.getStartRowNum();
                int rowNum = startRowNum;
                int startCellNum = excelConfig.getStartCellNum();
                int maxCellNum = startCellNum;
                List<FileNumBean> fileNumBeans = taskBean.getBeanList();
                XSSFSheet sheet;
                if (StringUtils.isBlank(sheetName)) {
                    sheet = sxssfWorkbook.getXSSFWorkbook().getSheetAt(0);
                } else {
                    sheet = sxssfWorkbook.getXSSFWorkbook().getSheet(sheetName);
                }
                int fileNum = fileNumBeans.size();
                updateMessage(text_identify + fileNum + text_data);
                boolean exportFileNum = excelConfig.isExportFileNum();
                boolean exportFileSize = excelConfig.isExportFileSize();
                List<String> titles = new ArrayList<>();
                //创建表头
                if (excelConfig.isExportTitle()) {
                    titles = buildTitles(exportFileNum, exportFileSize, true);
                    rowNum = buildExcelTitle(sheet, startRowNum, titles, startCellNum);
                }
                for (int i = 0; i < fileNum; i++) {
                    if (isCancelled()) {
                        closeStream();
                        break;
                    }
                    FileNumBean fileNumBean = fileNumBeans.get(i);
                    List<String> imgList = fileNumBean.getFilePathList();
                    Row row = getOrCreateRow(sheet, rowNum);
                    Cell startCell = row.createCell(startCellNum);
                    //判断附加项导出设置
                    if (exportFileNum && !exportFileSize) {
                        //附加项只导出文件数量
                        startCell.setCellValue(fileNumBean.getGroupNumber());
                        maxCellNum = buildImgExcel(imgList, excelConfig, startCellNum + 1, rowNum, sheet, sxssfWorkbook, row, maxCellNum);
                    } else if (!exportFileNum && exportFileSize) {
                        //附加项只导出文件大小
                        startCell.setCellValue(fileNumBean.getFileUnitSize());
                        maxCellNum = buildImgExcel(imgList, excelConfig, startCellNum + 1, rowNum, sheet, sxssfWorkbook, row, maxCellNum);
                    } else if (exportFileNum) {
                        //附加项导出文件数量和大小
                        startCell.setCellValue(fileNumBean.getGroupNumber());
                        int sizeCellNum = startCellNum + 1;
                        Cell sizeCell = row.createCell(sizeCellNum);
                        sizeCell.setCellValue(fileNumBean.getFileUnitSize());
                        maxCellNum = buildImgExcel(imgList, excelConfig, sizeCellNum + 1, rowNum, sheet, sxssfWorkbook, row, maxCellNum);
                    } else {
                        //不导出附加项
                        maxCellNum = buildImgExcel(imgList, excelConfig, startCellNum, rowNum, sheet, sxssfWorkbook, row, maxCellNum);
                    }
                    updateMessage(text_printing + (i + 1) + "/" + fileNum + text_data);
                    updateProgress(i + 1, fileNum);
                    rowNum++;
                }
                //合并图片表头单元格
                if (excelConfig.isExportTitle()) {
                    sheet.addMergedRegion(new CellRangeAddress(startRowNum, startRowNum, startCellNum + titles.size() - 1, maxCellNum - 1));
                }
                updateMessage(text_printDown);
                return sxssfWorkbook;
            }
        };
    }

    /**
     * 插入图片
     *
     * @param imgList       图片路径
     * @param excelConfig   excel导出设置
     * @param cellNum       图片起始列号
     * @param rowNum        图片的起始行号
     * @param sheet         当前表
     * @param sxssfWorkbook 当前工作簿
     * @param row           当前行
     * @param maxCellNum    最大列号
     * @return 最大列号
     */
    private static int buildImgExcel(List<String> imgList, ExcelConfig excelConfig, int cellNum, int rowNum,
                                     Sheet sheet, SXSSFWorkbook sxssfWorkbook, Row row, int maxCellNum) throws IOException {
        if (CollectionUtils.isEmpty(imgList)) {
            Cell cell = getOrCreateCell(cellNum, row);
            if (excelConfig.isNoImg()) {
                cell.setCellValue("无图片");
            }
            maxCellNum = Math.max(maxCellNum, cellNum);
        } else {
            sxssfWorkbook.setCompressTempFiles(true);
            for (String i : imgList) {
                //将图片插入Excel单元格
                CreationHelper helper = sxssfWorkbook.getCreationHelper();
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                //设置图片的起始位置以及大小
                anchor.setCol1(cellNum);
                anchor.setRow1(rowNum);
                anchor.setCol2(cellNum + 1);
                anchor.setRow2(rowNum + 1);
                anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
                String extension = getFileType(new File(i));
                //读取图片文件
                inputStream = Files.newInputStream(Paths.get(i));
                try {
                    if (jpg.equals(extension) || jpeg.equals(extension)) {
                        drawing.createPicture(anchor, sxssfWorkbook.addPicture(inputStream.readAllBytes(), Workbook.PICTURE_TYPE_JPEG));
                    } else if (png.equals(extension)) {
                        drawing.createPicture(anchor, sxssfWorkbook.addPicture(inputStream.readAllBytes(), Workbook.PICTURE_TYPE_PNG));
                    }
                } finally {
                    inputStream.close();
                }
                sheet.setColumnWidth(cellNum, 256 * excelConfig.getImgWidth());
                sheet.getRow(rowNum).setHeightInPoints(excelConfig.getImgHeight());
                cellNum++;
                maxCellNum = Math.max(maxCellNum, cellNum);
            }
        }
        return maxCellNum;
    }

    /**
     * 线程取消时关闭所有流
     *
     * @throws IOException io异常
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
