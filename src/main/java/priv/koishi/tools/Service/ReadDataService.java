package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.*;

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
    public static Task<List<FileNumBean>> readExcel(ExcelConfig excelConfig, TaskBean<FileNumBean> taskBean) {
        return new Task<>() {
            @Override
            protected List<FileNumBean> call() throws Exception {
                //Task的Message更新方法,这边修改之后,上面的监听方法会经过
                updateMessage("正在读取数据");
                List<FileNumBean> fileNumBeanList = new ArrayList<>();
                String excelInPath = excelConfig.getInPath();
                String sheetName = excelConfig.getSheet();
                int readRow = excelConfig.getReadRowNum();
                int readCell = excelConfig.getReadCellNum();
                int maxRow = excelConfig.getMaxRowNum();
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
                    FileConfig fileConfig = new FileConfig();
                    fileConfig.setMaxImgNum(taskBean.getMaxImgNum())
                            .setShowFileType(taskBean.isShowFileType())
                            .setSubCode(taskBean.getSubCode());
                    int imgNum = matchGroupData(fileNumBeanList, inFileList, fileConfig);
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
                Configuration configuration = taskBean.getConfiguration();
                CodeRenameConfig codeRenameConfig = null;
                StringRenameConfig stringRenameConfig = null;
                ExcelConfig excelConfig = null;
                int startName = -1;
                int tag = -1;
                if (configuration != null) {
                    if (configuration.getClass() == CodeRenameConfig.class) {
                        codeRenameConfig = (CodeRenameConfig) configuration;
                        startName = codeRenameConfig.getStartName();
                        tag = Integer.parseInt(codeRenameConfig.getTag());
                    } else if (configuration.getClass() == StringRenameConfig.class) {
                        stringRenameConfig = (StringRenameConfig) configuration;
                    } else if (configuration.getClass() == ExcelConfig.class) {
                        excelConfig = (ExcelConfig) configuration;
                    }
                }
                for (int i = 0; i < inFileSize; i++) {
                    FileBean fileBean = new FileBean();
                    fileBean.setId(i + 1);
                    File f = inFileList.get(i);
                    String fileName = getFileName(f);
                    if (taskBean.isShowFileType()) {
                        fileBean.setName(f.getName());
                    } else {
                        fileBean.setName(fileName);
                    }
                    buildRename(codeRenameConfig, fileName, fileBean, stringRenameConfig, excelConfig, startName, tag);
                    if (codeRenameConfig != null) {
                        if (tag < codeRenameConfig.getNameNum()) {
                            tag++;
                        } else {
                            startName++;
                            tag = Integer.parseInt(codeRenameConfig.getTag());
                        }
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
     * 构建文件重命名
     */
    private static void buildRename(CodeRenameConfig codeRenameConfig, String fileName, FileBean fileBean, StringRenameConfig stringRenameConfig,
                                    ExcelConfig excelConfig, int startName, int tag) {
        String fileRename = null;
        if (codeRenameConfig != null) {
            fileRename = fileName;
            String differenceCode = codeRenameConfig.getDifferenceCode();
            String space = "";
            if (codeRenameConfig.isAddSpace()) {
                space = " ";
            }
            switch (differenceCode) {
                case "阿拉伯数字：123": {
                    int startSize = codeRenameConfig.getStartSize();
                    String paddedNum = String.valueOf(startName);
                    // 使用String.format()函数进行补齐操作
                    if (startSize > 0) {
                        paddedNum = String.format("%0" + startSize + "d", startName);
                    }
                    String subCode = codeRenameConfig.getSubCode();
                    switch (subCode.substring(0, 4)) {
                        case "英文括号": {
                            fileRename = paddedNum + space + "(" + tag + ")";
                            break;
                        }
                        case "中文括号": {
                            fileRename = paddedNum + space + "（" + tag + "）";
                            break;
                        }
                        case "英文横杠": {
                            fileRename = paddedNum + space + "-" + tag;
                            break;
                        }
                        case "中文横杠": {
                            fileRename = paddedNum + space + "—" + tag;
                            break;
                        }
                    }
                    break;
                }
                case "中文数字：一二三": {
                    break;
                }
                case "小写英文字母：abc": {
                    break;
                }
                case "大小英文字母：ABC": {
                    break;
                }
            }
        } else if (stringRenameConfig != null) {

        } else if (excelConfig != null) {

        }
        fileBean.setRename(fileRename);
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
