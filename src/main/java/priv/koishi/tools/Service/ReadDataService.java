package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * @author KOISHI
 * Date:2024-10-25
 * Time:上午11:52
 */
public class ReadDataService {

    /**
     * 读取excel分组信息
     */
    public static Task<List<FileNumBean>> readExcel(ExcelConfigBean excelConfigBean, TaskBean<FileNumBean> taskBean) {
        return new Task<>() {
            @Override
            protected List<FileNumBean> call() throws Exception {
                //Task的Message更新方法,这边修改之后,上面的监听方法会经过
                updateMessage("正在读取数据");
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
                int lastRowNum = sheet.getLastRowNum();
                DataFormatter dataFormatter = new DataFormatter();
                //获取有文字的最后一行行号
                for (int i = lastRowNum; i >= 0; i--) {
                    XSSFRow row = sheet.getRow(i);
                    //过滤中间的空单元格
                    if (row != null) {
                        XSSFCell cell = row.getCell(readCell);
                        String stringCellValue = dataFormatter.formatCellValue(cell);
                        if (StringUtils.isNotBlank(stringCellValue)) {
                            lastRowNum = i;
                            break;
                        }
                    }
                    if (i == 0 && lastRowNum != i) {
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
                        String stringCellValue = dataFormatter.formatCellValue(cell);
                        fileNumBean.setGroupName(stringCellValue);
                    } else {
                        fileNumBean.setGroupName("");
                    }
                    fileNumBean.setGroupId(++id);
                    fileNumBeanList.add(fileNumBean);
                    //Task的Progress(进度)更新方法,进度条的进度与该属性挂钩
                    updateProgress(i, maxRow);
                }
                workbook.close();
                List<File> inFileList = taskBean.getInFileList();
                //已经读取文件后再匹配数据
                if (inFileList != null && !inFileList.isEmpty()) {
                    FileConfigBean fileConfigBean = new FileConfigBean();
                    fileConfigBean.setMaxImgNum(taskBean.getMaxImgNum())
                            .setShowFileType(taskBean.isShowFileType())
                            .setSubCode(taskBean.getSubCode());
                    int imgNum = matchGroupData(fileNumBeanList, inFileList, fileConfigBean);
                    updateMessage("共有 " + fileNumBeanList.size() + " 组数据，匹配到 " + imgNum + " 张图片");
                } else {
                    updateMessage("共有 " + fileNumBeanList.size() + " 组数据");
                }
                //匹配数据
                return showReadExcelData(fileNumBeanList, taskBean);
            }
        };
    }

    /**
     * 读取文件夹下的文件
     */
    public static Task<Void> readFile(TaskBean<FileBean> taskBean) {
        return new Task<>() {
            @Override
            protected Void call() throws IOException {
                updateMessage("正在读取数据");
                //组装数据
                List<File> inFileList = taskBean.getInFileList();
                List<FileBean> fileBeans = new ArrayList<>();
                int inFileSize = inFileList.size();
                for (int i = 0; i < inFileSize; i++) {
                    FileBean fileBean = new FileBean();
                    fileBean.setId(i + 1);
                    File f = inFileList.get(i);
                    if (taskBean.isShowFileType()) {
                        fileBean.setName(f.getName());
                    } else {
                        fileBean.setName(getFileName(f));
                    }
                    fileBean.setPath(f.getPath());
                    fileBean.setFileType(getFileType(f));
                    fileBean.setSize(getFileSize(f));
                    fileBean.setCreatDate(getFileCreatTime(f));
                    fileBean.setUpdateDate(getFileUpdateTime(f));
                    fileBeans.add(fileBean);
                    //Task的Progress(进度)更新方法,进度条的进度与该属性挂钩
                    updateProgress(i + 1, inFileSize);
                }
                updateMessage("共有" + inFileSize + " 个文件");
                //匹配数据
                showFileData(fileBeans, taskBean);
                return null;
            }
        };
    }

    /**
     * 匹配excel数据
     */
    public static List<FileNumBean> showReadExcelData(List<FileNumBean> fileBeans, TaskBean<FileNumBean> taskBean) throws Exception {
        if (CollectionUtils.isEmpty(fileBeans)) {
            throw new Exception("未查询到符合条件的数据，需修改查询条件后再继续");
        }
        autoBuildTableViewData(taskBean.getTableView(), fileBeans, taskBean.getTabId());
        return fileBeans;
    }

    /**
     * 匹配文件数据
     */
    public static void showFileData(List<FileBean> fileBeans, TaskBean<FileBean> taskBean) {
        autoBuildTableViewData(taskBean.getTableView(), fileBeans, taskBean.getTabId());
        fileSizeColum(taskBean.getTableColumn());
    }

}
