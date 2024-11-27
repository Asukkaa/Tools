package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
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

import static priv.koishi.tools.Service.FileRenameService.buildRename;
import static priv.koishi.tools.Text.CommonTexts.*;
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
                //改变要防重复点击的组件状态
                changeDisableControls(taskBean, true);
                //Task的Message更新方法,这边修改之后,上面的监听方法会经过
                updateMessage(text_readData);
                List<FileNumBean> fileNumBeanList = new ArrayList<>();
                String excelInPath = excelConfig.getInPath();
                String sheetName = excelConfig.getSheet();
                int readRow = excelConfig.getReadRowNum();
                int readCell = excelConfig.getReadCellNum();
                int maxRow = excelConfig.getMaxRowNum();
                checkExcelParam(excelInPath);
                XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(excelInPath));
                //读取指定sheet
                XSSFSheet sheet;
                if (StringUtils.isEmpty(sheetName)) {
                    sheet = workbook.getSheetAt(0);
                } else {
                    sheet = workbook.getSheet(sheetName);
                    if (sheet == null) {
                        sheet = workbook.createSheet(sheetName);
                    }
                }
                //获取有文字的最后一行行号
                int lastRowNum = sheet.getLastRowNum();
                DataFormatter dataFormatter = new DataFormatter();
                for (int i = lastRowNum; i >= 0; i--) {
                    XSSFRow row = sheet.getRow(i);
                    //过滤中间的空单元格
                    if (row != null) {
                        if (StringUtils.isNotBlank(dataFormatter.formatCellValue(row.getCell(readCell)))) {
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
                //读取excel数据
                int id = 0;
                for (int i = readRow; i <= maxRow; ++i) {
                    XSSFRow row = sheet.getRow(i);
                    FileNumBean fileNumBean = new FileNumBean();
                    if (row != null) {
                        String stringCellValue = dataFormatter.formatCellValue(row.getCell(readCell));
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
                //如果是文件重命名的excel模板则无需匹配数据
                if (taskBean.isReturnRenameList()) {
                    if (CollectionUtils.isEmpty(fileNumBeanList)) {
                        throw new Exception(text_selectNull);
                    }
                    return fileNumBeanList;
                }
                //已经读取文件后再匹配数据
                List<File> inFileList = taskBean.getInFileList();
                if (CollectionUtils.isNotEmpty(inFileList)) {
                    FileConfig fileConfig = new FileConfig();
                    fileConfig.setMaxImgNum(taskBean.getMaxImgNum())
                            .setShowFileType(taskBean.isShowFileType())
                            .setSubCode(taskBean.getSubCode());
                    int imgNum = matchGroupData(fileNumBeanList, inFileList, fileConfig);
                    updateMessage(text_allHave + fileNumBeanList.size() + text_group + imgNum + text_picture);
                } else {
                    updateMessage(text_allHave + fileNumBeanList.size() + text_data);
                }
                //匹配数据
                return showReadExcelData(fileNumBeanList, taskBean);
            }
        };
    }

    /**
     * 渲染excel数据到列表中
     */
    public static List<FileNumBean> showReadExcelData(List<FileNumBean> fileBeans, TaskBean<FileNumBean> taskBean) throws Exception {
        if (CollectionUtils.isEmpty(fileBeans)) {
            throw new Exception(text_selectNull);
        }
        autoBuildTableViewData(taskBean.getTableView(), fileBeans, taskBean.getTabId());
        return fileBeans;
    }

    /**
     * 读取文件夹下的文件
     */
    public static Task<Void> readFile(TaskBean<FileBean> taskBean) {
        return new Task<>() {
            @Override
            protected Void call() throws IOException {
                //改变要防重复点击的组件状态
                changeDisableControls(taskBean, true);
                updateMessage(text_readData);
                //判断是否为重命名功能的查询文件
                Configuration configuration = taskBean.getConfiguration();
                CodeRenameConfig codeRenameConfig = null;
                StringRenameConfig stringRenameConfig = null;
                int startName = -1;
                int tag = -1;
                int nameNum = 1;
                if (configuration != null) {
                    if (configuration instanceof CodeRenameConfig) {
                        codeRenameConfig = (CodeRenameConfig) configuration;
                        startName = codeRenameConfig.getStartName();
                        tag = codeRenameConfig.getTag();
                    }
                    if (configuration instanceof StringRenameConfig) {
                        stringRenameConfig = (StringRenameConfig) configuration;
                    }
                }
                //组装文件数据
                List<File> inFileList = taskBean.getInFileList();
                List<FileBean> fileBeans = new ArrayList<>();
                int inFileSize = inFileList.size();
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
                    //组装文件重命名数据
                    buildRename(codeRenameConfig, fileBean, stringRenameConfig, startName, tag);
                    if (codeRenameConfig != null) {
                        if (nameNum < codeRenameConfig.getNameNum()) {
                            tag++;
                            nameNum++;
                        } else {
                            startName++;
                            tag = codeRenameConfig.getTag();
                            nameNum = 1;
                        }
                    }
                    //组装文件基础数据
                    fileBean.setPath(f.getPath());
                    fileBean.setFileType(getFileType(f));
                    fileBean.setSize(getFileUnitSize(f));
                    fileBean.setCreatDate(getFileCreatTime(f));
                    fileBean.setUpdateDate(getFileUpdateTime(f));
                    fileBeans.add(fileBean);
                    //Task的Progress(进度)更新方法,进度条的进度与该属性挂钩
                    updateProgress(i + 1, inFileSize);
                }
                updateMessage(text_allHave + inFileSize + text_file);
                //渲染数据
                showFileData(fileBeans, taskBean);
                return null;
            }
        };
    }

    /**
     * 渲染文件数据
     */
    public static void showFileData(List<FileBean> fileBeans, TaskBean<FileBean> taskBean) {
        autoBuildTableViewData(taskBean.getTableView(), fileBeans, taskBean.getTabId());
        fileSizeColum(taskBean.getTableColumn());
    }

}
