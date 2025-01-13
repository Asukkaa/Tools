package priv.koishi.tools.Service;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableColumn;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.CommonUtils.getPropertyName;
import static priv.koishi.tools.Utils.CommonUtils.isGetterMethod;
import static priv.koishi.tools.Utils.ExcelUtils.*;
import static priv.koishi.tools.Utils.FileUtils.checkFileExists;
import static priv.koishi.tools.Utils.UiUtils.changeDisableControls;

/**
 * 获取文件夹下的文件信息功能导出任务类
 *
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午1:48
 */
public class FileNameToExcelService {

    /**
     * 构建输出文件名称的excel
     *
     * @param excelConfig excel设置
     * @param taskBean    任务线程所需参数
     * @return 不分组构建excel线程
     * @throws Exception io异常
     */
    public static Task<Workbook> buildFileNameExcel(ExcelConfig excelConfig, TaskBean<FileBean> taskBean) throws Exception {
        // 改变要防重复点击的组件状态
        changeDisableControls(taskBean, true);
        String excelType = excelConfig.getOutExcelType();
        Workbook workbook = null;
        if (xlsx.equals(excelType)) {
            workbook = new XSSFWorkbook();
            workbook = new SXSSFWorkbook((XSSFWorkbook) workbook);
        } else if (xls.equals(excelType)) {
            workbook = new HSSFWorkbook();
        }
        Sheet sheet = null;
        String excelInPath = excelConfig.getInPath();
        String sheetName = excelConfig.getSheetName();
        if (excelInPath != null && !excelInPath.isEmpty()) {
            checkFileExists(excelInPath, text_excelNotExists);
            // 输出路径与编辑路径不同先将要编辑的文件复制到输出路径
            checkCopyDestination(excelConfig);
            if (xlsx.equals(excelType)) {
                workbook = new XSSFWorkbook(new FileInputStream(excelInPath));
                workbook = new SXSSFWorkbook((XSSFWorkbook) workbook);
                if (StringUtils.isBlank(sheetName)) {
                    sheet = ((SXSSFWorkbook) workbook).getXSSFWorkbook().getSheetAt(0);
                } else {
                    sheet = ((SXSSFWorkbook) workbook).getXSSFWorkbook().getSheet(sheetName);
                    if (sheet == null) {
                        sheet = ((SXSSFWorkbook) workbook).getXSSFWorkbook().createSheet(sheetName);
                    }
                }
            }
            if (xls.equals(excelType)) {
                workbook = new HSSFWorkbook(new FileInputStream(excelInPath));
                if (StringUtils.isBlank(sheetName)) {
                    sheet = workbook.getSheetAt(0);
                } else {
                    sheet = workbook.getSheet(sheetName);
                    if (sheet == null) {
                        sheet = workbook.createSheet(sheetName);
                    }
                }
            }
        } else {
            if (xlsx.equals(excelType)) {
                sheet = ((SXSSFWorkbook) workbook).getXSSFWorkbook().createSheet(sheetName);
            } else if (xls.equals(excelType)) {
                sheet = workbook.createSheet(sheetName);
            }
        }
        // 构建excel
        return buildNoGroupExcel(taskBean, excelConfig, sheet, workbook);
    }

    /**
     * 不分组构建excel
     *
     * @param taskBean    线程所需参数
     * @param excelConfig excel设置
     * @param sheet       excel要导出的表
     * @param workbook    excel工作簿
     * @return 不分组构建excel线程
     */
    private static Task<Workbook> buildNoGroupExcel(TaskBean<FileBean> taskBean, ExcelConfig excelConfig, Sheet sheet, Workbook workbook) {
        return new Task<>() {
            @Override
            protected Workbook call() throws IllegalAccessException, InvocationTargetException {
                // 改变要防重复点击的组件状态
                changeDisableControls(taskBean, true);
                updateMessage(text_printData);
                List<FileBean> fileBeans = taskBean.getBeanList();
                int startRowNum = excelConfig.getStartRowNum();
                int startCellNum = excelConfig.getStartCellNum();
                int dataSize = fileBeans.size();
                updateMessage(text_identify + dataSize + text_file);
                if (!excelConfig.isExportFullList()) {
                    // 创建表头
                    if (excelConfig.isExportTitle()) {
                        Row row = getOrCreateRow(sheet, startRowNum);
                        Cell cell = row.createCell(startCellNum);
                        cell.setCellValue("文件名称");
                        startRowNum++;
                    }
                    List<String> names = new ArrayList<>();
                    // 获取文件名称
                    fileBeans.forEach(fileBean -> names.add(fileBean.getName()));
                    // 将数据写入单元格
                    for (int i = 0; i < dataSize; i++) {
                        String name = names.get(i);
                        Row row = getOrCreateRow(sheet, startRowNum);
                        Cell cell = row.createCell(startCellNum);
                        cell.setCellValue(name);
                        updateMessage(text_printing + (i + 1) + "/" + dataSize + text_file + name + text_coordinate + startRowNum + "," + startCellNum);
                        updateProgress(i + 1, dataSize);
                        startRowNum++;
                    }
                    autoSizeExcelCell(sheet, startCellNum);
                } else {
                    List<String> titles = new ArrayList<>();
                    List<String> ids = new ArrayList<>();
                    String tabId = "_Name";
                    // 获取属性顺序与名称
                    ObservableList<? extends TableColumn<?, ?>> columns = taskBean.getTableView().getColumns();
                    columns.forEach(c -> {
                        titles.add(c.getText());
                        String id = c.getId();
                        id = id.substring(0, id.lastIndexOf(tabId));
                        ids.add(id);
                    });
                    // 创建表头
                    if (excelConfig.isExportTitle()) {
                        startRowNum = buildExcelTitle(sheet, startRowNum, titles, startCellNum);
                    }
                    // 组装excel数据
                    int maxCellNum = startCellNum;
                    for (int i = 0; i < dataSize; i++) {
                        Row row = getOrCreateRow(sheet, startRowNum);
                        FileBean fileBean = fileBeans.get(i);
                        //获取所有属性值
                        Method[] methods = FileBean.class.getMethods();
                        Map<String, Object> properties = new HashMap<>();
                        for (Method method : methods) {
                            if (isGetterMethod(method)) {
                                String propertyName = getPropertyName(method.getName());
                                Object propertyValue = method.invoke(fileBean);
                                properties.put(propertyName, propertyValue);
                            }
                        }
                        // 将数据写入单元格
                        for (int j = 0; j < ids.size(); j++) {
                            int cellNum = startCellNum + j;
                            Cell cell = row.createCell(cellNum);
                            cell.setCellValue(properties.get(ids.get(j)).toString());
                            maxCellNum = Math.max(maxCellNum, cellNum);
                        }
                        updateMessage(text_printing + (i + 1) + "/" + dataSize + text_file + fileBean.getName() + text_coordinate + startRowNum + "," + startCellNum);
                        updateProgress(i + 1, dataSize);
                        startRowNum++;
                    }
                    autoSizeExcelCells(sheet, maxCellNum + 1, startCellNum);
                }
                updateMessage(text_printDown);
                return workbook;
            }
        };
    }

}
