package priv.koishi.tools.Service;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableColumn;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.ExcelConfig;
import priv.koishi.tools.Utils.CommonUtils;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.autoSizeExcelCell;
import static priv.koishi.tools.Utils.CommonUtils.getOrCreateRow;
import static priv.koishi.tools.Utils.FileUtils.checkCopyDestination;
import static priv.koishi.tools.Utils.UiUtils.changeDisableControls;
import static priv.koishi.tools.Utils.UiUtils.checkExcelParam;

/**
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午1:48
 */
public class FileNameToExcelService {

    /**
     * 构建输出文件名称的excel
     */
    public static Task<SXSSFWorkbook> buildFileNameExcel(ExcelConfig excelConfig, TaskBean<FileBean> taskBean) throws Exception {
        //改变要防重复点击的组件状态
        changeDisableControls(taskBean, true);
        XSSFWorkbook workbook = new XSSFWorkbook();
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(workbook);
        XSSFSheet sheet;
        String excelInPath = excelConfig.getInPath();
        String sheetName = excelConfig.getSheet();
        if (excelInPath != null && !excelInPath.isEmpty()) {
            checkExcelParam(excelInPath);
            //输出路径与编辑路径不同先将要编辑的文件复制到输出路径
            checkCopyDestination(excelConfig);
            workbook = new XSSFWorkbook(new FileInputStream(excelInPath));
            sxssfWorkbook = new SXSSFWorkbook(workbook);
            if (sheetName == null || sheetName.isEmpty()) {
                sheet = sxssfWorkbook.getXSSFWorkbook().getSheetAt(0);
            } else {
                sheet = sxssfWorkbook.getXSSFWorkbook().getSheet(sheetName);
                if (sheet == null) {
                    sheet = sxssfWorkbook.getXSSFWorkbook().createSheet(sheetName);
                }
            }
        } else {
            sheet = sxssfWorkbook.getXSSFWorkbook().createSheet(sheetName);
        }
        //构建excel
        return buildNoGroupExcel(taskBean, excelConfig, sheet, sxssfWorkbook);
    }

    /**
     * 不分组构建excel
     */
    private static Task<SXSSFWorkbook> buildNoGroupExcel(TaskBean<FileBean> taskBean, ExcelConfig excelConfig, XSSFSheet sheet, SXSSFWorkbook workbook) {
        return new Task<>() {
            @Override
            protected SXSSFWorkbook call() throws IllegalAccessException, InvocationTargetException {
                //改变要防重复点击的组件状态
                changeDisableControls(taskBean, true);
                updateMessage(text_printData);
                List<FileBean> fileBeans = taskBean.getBeanList();
                int startRowNum = excelConfig.getStartRowNum();
                int startCellNum = excelConfig.getStartCellNum();
                int dataSize = fileBeans.size();
                updateMessage(text_identify + dataSize + text_file);
                if (!excelConfig.isExportFullList()) {
                    //创建表头
                    if (excelConfig.isExportTitle()) {
                        XSSFRow row = getOrCreateRow(sheet, startRowNum);
                        XSSFCell cell = row.createCell(startCellNum);
                        cell.setCellValue("文件名称");
                        startRowNum++;
                    }
                    List<String> names = new ArrayList<>();
                    //获取文件名称
                    fileBeans.forEach(fileBean -> names.add(fileBean.getName()));
                    //将数据写入单元格
                    for (int i = 0; i < dataSize; i++) {
                        String name = names.get(i);
                        XSSFRow row = getOrCreateRow(sheet, startRowNum);
                        XSSFCell cell = row.createCell(startCellNum);
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
                    //获取属性顺序与名称
                    ObservableList<? extends TableColumn<?, ?>> columns = taskBean.getTableView().getColumns();
                    columns.forEach(c -> {
                        titles.add(c.getText());
                        String id = c.getId();
                        id = id.substring(0, id.lastIndexOf(tabId));
                        ids.add(id);
                    });
                    //创建表头
                    if (excelConfig.isExportTitle()) {
                        XSSFRow row = getOrCreateRow(sheet, startRowNum);
                        for (int i = 0; i < titles.size(); i++) {
                            XSSFCell cell = row.createCell(startCellNum + i);
                            cell.setCellValue(titles.get(i));
                        }
                        startRowNum++;
                    }
                    //组装excel数据
                    int maxCellNum = startCellNum;
                    for (int i = 0; i < dataSize; i++) {
                        XSSFRow row = getOrCreateRow(sheet, startRowNum);
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
                        //将数据写入单元格
                        for (int j = 0; j < ids.size(); j++) {
                            int cellNum = startCellNum + j;
                            XSSFCell cell = row.createCell(cellNum);
                            cell.setCellValue(properties.get(ids.get(j)).toString());
                            if (cellNum > maxCellNum) {
                                maxCellNum = cellNum;
                            }
                        }
                        updateMessage(text_printing + (i + 1) + "/" + dataSize + text_file + fileBean.getName() + text_coordinate + startRowNum + "," + startCellNum);
                        updateProgress(i + 1, dataSize);
                        startRowNum++;
                    }
                    CommonUtils.autoSizeExcelCells(sheet, maxCellNum + 1, startCellNum);
                }
                updateMessage(text_printDown);
                return workbook;
            }
        };
    }

    /**
     * 调用获取属性的方法
     */
    private static boolean isGetterMethod(Method method) {
        return method.getName().startsWith("get")
                && method.getParameterCount() == 0
                && !method.getReturnType().equals(void.class)
                && !method.isAnnotationPresent(Override.class);
    }

    /**
     * 获取javafxBean属性名称
     */
    private static String getPropertyName(String getterName) {
        return Character.toLowerCase(getterName.charAt(3)) + getterName.substring(4);
    }

}
