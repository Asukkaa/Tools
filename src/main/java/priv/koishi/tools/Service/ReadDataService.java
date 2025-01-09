package priv.koishi.tools.Service;

import javafx.concurrent.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.*;
import priv.koishi.tools.Vo.FileNumVo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static priv.koishi.tools.Service.FileRenameService.buildRename;
import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.matchGroupData;
import static priv.koishi.tools.Utils.FileUtils.*;
import static priv.koishi.tools.Utils.UiUtils.*;

/**
 * 读取数据线程任务类
 *
 * @author KOISHI
 * Date:2024-10-25
 * Time:上午11:52
 */
public class ReadDataService {

    /**
     * 读取excel分组信息
     *
     * @param excelConfig excel读取设置
     * @param taskBean    任务线程设置参数
     * @return 用于展示到javafx列表的数据
     */
    public static Task<List<FileNumBean>> readExcel(ExcelConfig excelConfig, TaskBean<FileNumBean> taskBean) {
        return new Task<>() {
            @Override
            protected List<FileNumBean> call() throws Exception {
                // 改变要防重复点击的组件状态
                changeDisableControls(taskBean, true);
                updateMessage(text_readData);
                List<FileNumBean> fileNumBeanList = new ArrayList<>();
                String excelInPath = excelConfig.getInPath();
                String sheetName = excelConfig.getSheetName();
                int readRow = excelConfig.getReadRowNum();
                int readCell = excelConfig.getReadCellNum();
                int maxRow = excelConfig.getMaxRowNum();
                checkFileExists(excelInPath, text_excelNotExists);
                Workbook workbook = getWorkbook(excelInPath);
                // 读取指定sheet
                Sheet sheet;
                if (StringUtils.isEmpty(sheetName)) {
                    sheet = workbook.getSheetAt(0);
                } else {
                    sheet = workbook.getSheet(sheetName);
                    if (sheet == null) {
                        throw new Exception("未读取到名称为 " + sheetName + " 的表");
                    }
                }
                // 获取有文字的最后一行行号
                int lastRowNum = sheet.getLastRowNum();
                DataFormatter dataFormatter = new DataFormatter();
                for (int i = lastRowNum; i >= 0; i--) {
                    Row row = sheet.getRow(i);
                    // 过滤中间的空单元格
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
                // 获取要读取的最后一行
                if (maxRow == -1 || maxRow > lastRowNum) {
                    maxRow = lastRowNum;
                } else {
                    maxRow += readRow - 1;
                }
                // 读取excel数据
                int id = 0;
                for (int i = readRow; i <= maxRow; ++i) {
                    Row row = sheet.getRow(i);
                    FileNumBean fileNumBean = new FileNumBean();
                    if (row != null) {
                        String stringCellValue = dataFormatter.formatCellValue(row.getCell(readCell));
                        fileNumBean.setGroupName(stringCellValue);
                    } else {
                        fileNumBean.setGroupName("");
                    }
                    fileNumBean.setGroupId(++id);
                    fileNumBeanList.add(fileNumBean);
                    updateProgress(i, maxRow);
                }
                workbook.close();
                // 如果是文件重命名的excel模板则无需匹配数据
                if (taskBean.isReturnRenameList()) {
                    if (CollectionUtils.isEmpty(fileNumBeanList)) {
                        throw new Exception(text_selectNull);
                    }
                    return fileNumBeanList;
                }
                // 已经读取文件后再匹配数据
                List<File> inFileList = taskBean.getInFileList();
                if (CollectionUtils.isNotEmpty(inFileList)) {
                    FileConfig fileConfig = new FileConfig();
                    fileConfig.setMaxImgNum(taskBean.getMaxImgNum())
                            .setShowFileType(taskBean.isShowFileType())
                            .setSubCode(taskBean.getSubCode());
                    FileNumVo fileNumVo = matchGroupData(fileNumBeanList, inFileList, fileConfig);
                    updateMessage(text_allHave + fileNumVo.getDataNum() + text_group + fileNumVo.getImgNum() + text_picture + text_totalFileSize + fileNumVo.getImgSize());
                } else {
                    updateMessage(text_allHave + fileNumBeanList.size() + text_data);
                }
                // 匹配数据
                return showReadExcelData(fileNumBeanList, taskBean);
            }
        };
    }

    /**
     * 根据不同格式excel创建不同工作簿
     *
     * @param excelInPath excel模板路径
     * @return 根据不同格式excel创建的作簿
     * @throws Exception 文件格式不支持
     */
    private static Workbook getWorkbook(String excelInPath) throws Exception {
        String excelType = getFileType(new File(excelInPath));
        Workbook workbook = null;
        if (xlsx.equals(excelType)) {
            workbook = new XSSFWorkbook(new FileInputStream(excelInPath));
        }
        if (xls.equals(excelType)) {
            workbook = new HSSFWorkbook(new FileInputStream(excelInPath));
        }
        if (workbook == null) {
            throw new Exception("当前读取模板文件格式为 " + excelType + " 目前只支持读取 .xlsx 与 .xls 格式的文件");
        }
        return workbook;
    }

    /**
     * 渲染excel数据到列表中
     *
     * @return 用于展示到javafx列表的数据
     * @throws Exception 未查询到符合条件的数据
     */
    public static List<FileNumBean> showReadExcelData(List<FileNumBean> fileBeans, TaskBean<FileNumBean> taskBean) throws Exception {
        if (CollectionUtils.isEmpty(fileBeans)) {
            throw new Exception(text_selectNull);
        }
        showFileSizeColumData(fileBeans, taskBean);
        return fileBeans;
    }

    /**
     * 读取文件夹下的文件
     *
     * @param taskBean 读取文件线程任务参数
     * @return 无参数线程任务
     */
    public static Task<Void> readFile(TaskBean<FileBean> taskBean) {
        return new Task<>() {
            @Override
            protected Void call() throws IOException {
                // 改变要防重复点击的组件状态
                changeDisableControls(taskBean, true);
                updateMessage(text_readData);
                // 判断是否为重命名功能的查询文件
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
                    } else if (configuration instanceof StringRenameConfig) {
                        stringRenameConfig = (StringRenameConfig) configuration;
                    }
                }
                // 组装文件数据
                List<File> inFileList = taskBean.getInFileList();
                // 数据排序
                comparingData(taskBean, inFileList);
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
                    String showStatus = f.isHidden() ? "隐藏" : "非隐藏";
                    fileBean.setShowStatus(showStatus);
                    // 组装文件重命名数据
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
                    // 组装文件基础数据
                    fileBean.setUpdateDate(getFileUpdateTime(f))
                            .setCreatDate(getFileCreatTime(f))
                            .setFileType(getFileType(f))
                            .setSize(getFileUnitSize(f))
                            .setPath(f.getPath());
                    fileBeans.add(fileBean);
                    updateProgress(i + 1, inFileSize);
                }
                updateMessage(text_allHave + inFileSize + text_file);
                // 渲染数据
                showFileSizeColumData(fileBeans, taskBean);
                return null;
            }
        };
    }

    /**
     * 数据排序
     *
     * @param taskBean 带有排序类型与是否倒序的线程设置参数
     * @param fileList 要排序的文件
     */
    private static void comparingData(TaskBean<?> taskBean, List<File> fileList) {
        String sortType = taskBean.getSortType();
        // 是否倒序排序
        boolean reverseSort = taskBean.isReverseSort();
        switch (sortType) {
            case "按文件名称排序": {
                comparingByName(fileList, reverseSort);
                break;
            }
            case "按文件创建时间排序": {
                comparingByCreatTime(fileList, reverseSort);
                break;
            }
            case "按文件修改时间排序": {
                comparingByUpdateTime(fileList, reverseSort);
                break;
            }
            case "按文件大小排序": {
                comparingBySize(fileList, reverseSort);
                break;
            }
            case "按文件类型排序": {
                comparingByType(fileList, reverseSort);
                break;
            }
        }
    }

    /**
     * 按文件类型排序
     *
     * @param fileList    要排序的文件
     * @param reverseSort 是否倒序标识，true倒序，false为正序
     */
    private static void comparingByType(List<File> fileList, boolean reverseSort) {
        fileList.sort((o1, o2) -> {
            // 比较文件后缀名
            String ext1 = getFileType(o1);
            String ext2 = getFileType(o2);
            if (reverseSort) {
                return ext2.compareTo(ext1);
            } else {
                return ext1.compareTo(ext2);
            }
        });
    }

    /**
     * 按文件大小排序
     *
     * @param fileList    要排序的文件
     * @param reverseSort 是否倒序标识，true倒序，false为正序
     */
    private static void comparingBySize(List<File> fileList, boolean reverseSort) {
        if (reverseSort) {
            fileList.sort((o1, o2) -> {
                long size1 = o1.length();
                long size2 = o2.length();
                return (int) (size2 - size1);
            });
        } else {
            fileList.sort(Comparator.comparing(File::length));
        }
    }

    /**
     * 按文件修改时间排序
     *
     * @param fileList    要排序的文件
     * @param reverseSort 是否倒序标识，true倒序，false为正序
     */
    private static void comparingByUpdateTime(List<File> fileList, boolean reverseSort) {
        if (reverseSort) {
            fileList.sort((f1, f2) -> {
                long diff = f2.lastModified() - f1.lastModified();
                if (diff > 0) {
                    return 1;
                } else if (diff == 0) {
                    return 0;
                } else {
                    return -1;
                }
            });
        } else {
            fileList.sort(Comparator.comparing(File::lastModified));
        }
    }

    /**
     * 按文件创建时间排序
     *
     * @param fileList    要排序的文件
     * @param reverseSort 是否倒序标识，true倒序，false为正序
     */
    private static void comparingByCreatTime(List<File> fileList, boolean reverseSort) {
        fileList.sort((o1, o2) -> {
            try {
                BasicFileAttributes attr1 = Files.readAttributes(o1.toPath(), BasicFileAttributes.class);
                BasicFileAttributes attr2 = Files.readAttributes(o2.toPath(), BasicFileAttributes.class);
                if (reverseSort) {
                    return Long.compare(attr2.creationTime().toMillis(), attr1.creationTime().toMillis());
                } else {
                    return Long.compare(attr1.creationTime().toMillis(), attr2.creationTime().toMillis());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 按文件名称排序
     *
     * @param fileList    要排序的文件
     * @param reverseSort 是否倒序标识，true倒序，false为正序
     */
    private static void comparingByName(List<File> fileList, boolean reverseSort) {
        if (reverseSort) {
            fileList.sort((o1, o2) -> o2.getName().compareTo(o1.getName()));
        } else {
            fileList.sort(Comparator.comparing(File::getName));
        }
    }

}
