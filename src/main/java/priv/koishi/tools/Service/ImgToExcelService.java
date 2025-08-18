package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.ExcelUtils.*;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.UiUtils.changeDisableNodes;

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
     * 构建分组的图片excel
     *
     * @param taskBean    线程设置参数
     * @param excelConfig excel导出设置
     * @return excel工作簿
     */
    public static Task<Workbook> buildImgGroupExcel(TaskBean<FileNumBean> taskBean, ExcelConfig excelConfig) {
        return new Task<>() {
            @Override
            protected SXSSFWorkbook call() throws IOException {
                // 改变要防重复点击的组件状态
                changeDisableNodes(taskBean, true);
                // 校验excel输出路径是否与模板一致，若不一致则复制一份模板文件到输出路径
                checkCopyDestination(excelConfig);
                String excelInPath = excelConfig.getInPath();
                checkFileExists(excelInPath, text_excelNotExists);
                updateMessage(text_printData);
                try (FileInputStream fis = new FileInputStream(excelInPath)) {
                    XSSFWorkbook templateWorkbook = new XSSFWorkbook(fis);
                    sxssfWorkbook = new SXSSFWorkbook(templateWorkbook, 50);
                }
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
                sxssfWorkbook.setCompressTempFiles(true);
                List<String> titles = new ArrayList<>();
                // 创建表头
                if (excelConfig.isExportTitle()) {
                    titles = buildTitles(exportFileNum, exportFileSize, true);
                    rowNum = buildExcelTitle(sheet, startRowNum, titles, startCellNum);
                }
                boolean showFileType = taskBean.isShowFileType();
                for (int i = 0; i < fileNum; i++) {
                    if (isCancelled()) {
                        closeStream();
                        break;
                    }
                    FileNumBean fileNumBean = fileNumBeans.get(i);
                    List<String> imgList = fileNumBean.getFilePathList();
                    if (CollectionUtils.isNotEmpty(imgList)) {
                        imgList.sort(String.CASE_INSENSITIVE_ORDER);
                    }
                    Row row = getOrCreateRow(sheet, rowNum);
                    Cell startCell = row.createCell(startCellNum);
                    // 判断附加项导出设置
                    if (exportFileNum && !exportFileSize) {
                        // 附加项只导出文件数量
                        startCell.setCellValue(fileNumBean.getGroupNumber());
                        maxCellNum = buildImgExcel(imgList, excelConfig, startCellNum + 1, rowNum, sheet, row, maxCellNum, showFileType);
                    } else if (!exportFileNum && exportFileSize) {
                        // 附加项只导出文件大小
                        startCell.setCellValue(fileNumBean.getFileUnitSize());
                        maxCellNum = buildImgExcel(imgList, excelConfig, startCellNum + 1, rowNum, sheet, row, maxCellNum, showFileType);
                    } else if (exportFileNum) {
                        // 附加项导出文件数量和大小
                        startCell.setCellValue(fileNumBean.getGroupNumber());
                        int sizeCellNum = startCellNum + 1;
                        Cell sizeCell = row.createCell(sizeCellNum);
                        sizeCell.setCellValue(fileNumBean.getFileUnitSize());
                        maxCellNum = buildImgExcel(imgList, excelConfig, sizeCellNum + 1, rowNum, sheet, row, maxCellNum, showFileType);
                    } else {
                        // 不导出附加项
                        maxCellNum = buildImgExcel(imgList, excelConfig, startCellNum, rowNum, sheet, row, maxCellNum, showFileType);
                    }
                    updateMessage(text_printing + (i + 1) + "/" + fileNum + text_data);
                    updateProgress(i + 1, fileNum);
                    rowNum++;
                }
                // 合并图片表头单元格
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
     * @param imgList     图片路径
     * @param excelConfig excel导出设置
     * @param cellNum     图片起始列号
     * @param rowNum      图片的起始行号
     * @param sheet       当前表
     * @param row         当前行
     * @param maxCellNum  最大列号
     * @return 最大列号
     */
    private static int buildImgExcel(List<String> imgList, ExcelConfig excelConfig, int cellNum, int rowNum,
                                     Sheet sheet, Row row, int maxCellNum, boolean showFileType) throws IOException {
        if (CollectionUtils.isEmpty(imgList)) {
            Cell cell = getOrCreateCell(cellNum, row);
            if (excelConfig.isNoImg()) {
                cell.setCellValue(text_noImg);
            }
            maxCellNum = Math.max(maxCellNum, cellNum);
        } else {
            String insertImgType = excelConfig.getInsertType();
            if (insertType_img.equals(insertImgType)) {
                maxCellNum = insertImg(imgList, excelConfig, cellNum, rowNum, sheet, maxCellNum);
            } else if (insertType_relativePath.equals(insertImgType) || insertType_absolutePath.equals(insertImgType)) {
                maxCellNum = insertFileLink(imgList, excelConfig, cellNum, rowNum, sheet, maxCellNum, showFileType,
                        insertImgType, sxssfWorkbook);
            }
        }
        return maxCellNum;
    }

    /**
     * 插入图片
     *
     * @param imgList     图片路径
     * @param excelConfig excel导出设置
     * @param cellNum     图片起始列号
     * @param rowNum      图片的起始行号
     * @param sheet       当前表
     * @param maxCellNum  最大列号
     * @return 插入图片后的最大列号
     */
    private static int insertImg(List<String> imgList, ExcelConfig excelConfig, int cellNum, int rowNum, Sheet sheet,
                                 int maxCellNum) throws IOException {
        CreationHelper helper = sxssfWorkbook.getCreationHelper();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        for (String i : imgList) {
            // 将图片插入Excel单元格
            ClientAnchor anchor = helper.createClientAnchor();
            // 设置图片的起始位置以及大小
            anchor.setCol1(cellNum);
            anchor.setRow1(rowNum);
            anchor.setCol2(cellNum + 1);
            anchor.setRow2(rowNum + 1);
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
            File imgFile = new File(i);
            String extension = getFileType(imgFile);
            // 读取图片文件
            try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(imgFile.toPath()))) {
                // 分块读取
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(chunk)) != -1) {
                    buffer.write(chunk, 0, bytesRead);
                }
                buffer.flush();
                if (jpg.equals(extension) || jpeg.equals(extension)) {
                    drawing.createPicture(anchor, sxssfWorkbook.addPicture(buffer.toByteArray(), Workbook.PICTURE_TYPE_JPEG));
                } else if (png.equals(extension)) {
                    drawing.createPicture(anchor, sxssfWorkbook.addPicture(buffer.toByteArray(), Workbook.PICTURE_TYPE_PNG));
                }
                buffer.close();
            }
            sheet.setColumnWidth(cellNum, 256 * excelConfig.getImgWidth());
            sheet.getRow(rowNum).setHeightInPoints(excelConfig.getImgHeight());
            cellNum++;
            maxCellNum = Math.max(maxCellNum, cellNum);
        }
        return maxCellNum;
    }

    /**
     * 插入文件超链接
     *
     * @param pathList     文件路径
     * @param excelConfig  excel导出设置
     * @param cellNum      文件起始列号
     * @param rowNum       文件的起始行号
     * @param sheet        当前表
     * @param maxCellNum   最大列号
     * @param showFileType 是否显示文件类型
     * @param insertType   插入文件类型
     * @param workbook     当前工作簿
     * @return 插入文件后的最大列号
     */
    public static int insertFileLink(List<String> pathList, ExcelConfig excelConfig, int cellNum, int rowNum, Sheet sheet,
                                     int maxCellNum, boolean showFileType, String insertType, Workbook workbook) {
        CreationHelper helper = workbook.getCreationHelper();
        String linkNameType = excelConfig.getLinkNameType();
        String linkLeftName = excelConfig.getLinkLeftName();
        String linkRightName = excelConfig.getLinkRightName();
        String outPath = excelConfig.getOutPath();
        for (String path : pathList) {
            // 创建超链接单元格
            Cell cell = getOrCreateCell(cellNum, sheet.getRow(rowNum));
            // 创建文件超链接
            Hyperlink hyperlink = helper.createHyperlink(HyperlinkType.FILE);
            File imgFile = new File(path);
            // 路径转换为URI格式
            String uriPath;
            // 设置超链接路径
            if (insertType_absolutePath.equals(insertType)) {
                uriPath = String.valueOf(imgFile.toURI());
            } else {
                // 文件所在目录
                URI excelUri = new File(outPath).toURI();
                URI imgUri = imgFile.toURI();
                // 计算相对路径
                URI relativePath = excelUri.relativize(imgUri);
                uriPath = String.valueOf(relativePath);
            }
            hyperlink.setAddress(uriPath);
            // 应用超链接到单元格
            cell.setHyperlink(hyperlink);
            // 设置单元格样式
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setUnderline(Font.U_SINGLE);
            font.setColor(IndexedColors.BLUE.getIndex());
            style.setFont(font);
            cell.setCellStyle(style);
            // 设置列宽
            sheet.setColumnWidth(cellNum, 256 * (excelConfig.getImgWidth() * 2));
            String fileName;
            if (showFileType) {
                fileName = imgFile.getName();
            } else {
                fileName = getFileName(imgFile);
            }
            if (StringUtils.isNotBlank(linkLeftName)) {
                if (linkName_unified.equals(linkNameType)) {
                    fileName = linkLeftName;
                } else if (linkName_splice.equals(linkNameType)) {
                    fileName = linkLeftName + fileName + linkRightName;
                }
            }
            cell.setCellValue(fileName);
            cellNum++;
            maxCellNum = Math.max(maxCellNum, cellNum);
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
    }

}
