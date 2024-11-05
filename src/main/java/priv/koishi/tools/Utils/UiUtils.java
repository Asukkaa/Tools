package priv.koishi.tools.Utils;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.PopupWindow;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import priv.koishi.tools.Configuration.FileConfig;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Enum.SelectItemsEnums;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static priv.koishi.tools.Service.ReadDataService.showReadExcelData;
import static priv.koishi.tools.Utils.CommonUtils.*;
import static priv.koishi.tools.Utils.FileUtils.*;

/**
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午1:38
 */
public class UiUtils {

    /**
     * 鼠标停留提示框
     *
     * @param control 需要显示提示框的组件
     * @param tip     提示卡信息
     */
    public static void addToolTip(Control control, String tip) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(tip);
        tooltip.setShowDuration(new Duration(6000000));
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
        control.setTooltip(tooltip);
    }

    /**
     * 输入框鼠标停留提示输入值
     */
    public static void aadValueToolTip(TextField textField, String text) {
        String value = textField.getText();
        if (StringUtils.isNotEmpty(text)) {
            if (StringUtils.isNotBlank(value)) {
                addToolTip(textField, text + "\n" + value);
            } else {
                addToolTip(textField, text);
            }
        } else {
            if (StringUtils.isNotBlank(value)) {
                addToolTip(textField, value);
            } else {
                textField.setTooltip(null);
            }
        }
    }

    /**
     * 创建一个文件选择器
     */
    public static File creatFileChooser(ActionEvent event, String path, List<FileChooser.ExtensionFilter> extensionFilters, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        // 设置初始目录
        if (StringUtils.isBlank(path)) {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        } else {
            File file = new File(path);
            // 设置初始目录
            if (file.isDirectory()) {
                fileChooser.setInitialDirectory(file);
            } else if (file.isFile()) {
                file = new File(getFileMkdir(file));
                fileChooser.setInitialDirectory(file);
            }
        }
        //设置过滤条件
        if (CollectionUtils.isNotEmpty(extensionFilters)) {
            fileChooser.getExtensionFilters().addAll(extensionFilters);
        }
        return fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
    }

    /**
     * 创建一个文件夹选择器
     */
    public static File creatDirectoryChooser(ActionEvent event, String path, String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        // 设置初始目录
        if (StringUtils.isBlank(path) || !new File(path).isDirectory()) {
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        } else {
            directoryChooser.setInitialDirectory(new File(path));
        }
        return directoryChooser.showDialog(((Node) event.getSource()).getScene().getWindow());
    }

    /**
     * 处理要过滤的文件类型
     */
    public static List<String> getFilterExtensionList(TextField filterFileType) {
        String filterFileTypeValue = filterFileType.getText();
        List<String> filterExtensionList = new ArrayList<>();
        if (StringUtils.isNotBlank(filterFileTypeValue)) {
            filterExtensionList = Arrays.asList(filterFileTypeValue.toLowerCase().split(" "));
        }
        return filterExtensionList;
    }

    /**
     * 自定义单元格工厂，为单元格添加Tooltip
     */
    public static <S, T> void addTableColumnToolTip(TableColumn<S, T> column) {
        column.setCellFactory(new Callback<>() {
            @Override
            public TableCell<S, T> call(TableColumn<S, T> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null || StringUtils.isEmpty(item.toString())) {
                            setText(null);
                            setTooltip(null);
                        } else if (!item.toString().isEmpty()) {
                            setText(item.toString());
                            Tooltip tooltip = new Tooltip(item.toString());
                            tooltip.setWrapText(true);
                            tooltip.setShowDuration(new Duration(6000000));
                            tooltip.setShowDelay(Duration.ZERO);
                            tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
                            setTooltip(tooltip);
                        }
                    }
                };
            }
        });
    }

    /**
     * 文件大小排序
     */
    public static void fileSizeColum(TableColumn<?, String> sizeColumn) {
        // 自定义比较器
        Comparator<String> customComparator = Comparator.comparingDouble(FileUtils::fileSizeCompareValue);
        // 应用自定义比较器
        sizeColumn.setComparator(customComparator);
    }

    /**
     * 校验excel设置参数
     */
    public static void checkExcelParam(String excelInPath) throws Exception {
        File file = new File(excelInPath);
        if (!file.exists() || !file.isFile()) {
            throw new Exception("未读取到文件，excel模板路径设置有误");
        }
        String fileExtension = getFileType(file);
        if (!".xlsx".equals(fileExtension)) {
            throw new Exception("当前读取的文件格式为：" + fileExtension + " 只能读取.xlsx格式的excel");
        }
    }

    /**
     * 设置默认数值
     */
    public static int setDefaultIntValue(TextField textField, int defaultValue, Integer min, Integer max) {
        String valueStr = textField.getText();
        int value = defaultValue;
        if (isInIntegerRange(valueStr, min, max)) {
            value = Integer.parseInt(valueStr);
        }
        return value;
    }

    /**
     * 设置默认字符串值
     */
    public static String setDefaultStrValue(TextField textField, String defaultValue) {
        String valueStr = textField.getText();
        String value = defaultValue;
        if (StringUtils.isNotBlank(valueStr)) {
            value = valueStr;
        }
        return value;
    }

    /**
     * 设置默认文件名
     */
    public static String setDefaultFileName(TextField textField, String defaultValue) {
        //去掉开头的空字符
        String valueStr = textField.getText().replaceFirst("^\\s+", "");
        String value = defaultValue;
        if (isValidFileName(valueStr)) {
            value = valueStr;
        }
        return value;
    }

    /**
     * 为javafx单元格赋值并添加鼠标悬停提示
     */
    public static void buildCellValue(TableColumn<?, ?> tableColumn, String param) {
        tableColumn.setCellValueFactory(new PropertyValueFactory<>(param));
        addTableColumnToolTip(tableColumn);
    }

    /**
     * 处理异常的统一弹窗
     */
    public static void showExceptionDialog(Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("异常信息");
        if (ex.getCause().getCause() instanceof Exception) {
            alert.setHeaderText(ex.getCause().getCause().getMessage());
        } else {
            alert.setHeaderText(ex.getMessage());
        }
        // 创建展示异常信息的TextArea
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(errToString(ex));
        ex.printStackTrace();
        // 创建VBox并添加TextArea
        VBox details = new VBox();
        details.heightProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> textArea.setPrefHeight(details.getHeight())));
        details.getChildren().add(textArea);
        alert.getDialogPane().setExpandableContent(details);
        // 展示弹窗
        alert.showAndWait();
    }

    /**
     * 根据bean属性自动填充javafx表格
     */
    public static <T> void autoBuildTableViewData(TableView<T> tableView, List<T> dataList, String tabId) {
        Class<?> beanClass = dataList.getFirst().getClass();
        // 获取对象的所有字段
        List<Field> fields = List.of(beanClass.getDeclaredFields());
        ObservableList<? extends TableColumn<?, ?>> columns = tableView.getColumns();
        fields.forEach(f -> {
            String fieldName = f.getName();
            String finalFieldName;
            if (StringUtils.isNotEmpty(tabId)) {
                finalFieldName = fieldName + tabId;
            } else {
                finalFieldName = fieldName;
            }
            Optional<? extends TableColumn<?, ?>> matched = columns.stream().filter(c -> c.getId().equals(finalFieldName)).findFirst();
            matched.ifPresent(m -> buildCellValue(m, fieldName));
        });
        ObservableList<T> data = FXCollections.observableArrayList(dataList);
        tableView.setItems(data);
    }

    /**
     * 清空excel统计文件名或插入图片页面的列表
     */
    public static void removeNumImgAll(TableView<FileNumBean> tableView, Label fileNumber, Label log) {
        List<FileNumBean> nullFileBeans = new ArrayList<>();
        ObservableList<FileNumBean> nullData = FXCollections.observableArrayList(nullFileBeans);
        tableView.setItems(nullData);
        // 解除绑定，设置文本，然后重新绑定
        fileNumber.textProperty().unbind();
        fileNumber.setText("列表为空");
        log.textProperty().unbind();
        log.setText("");
        log.setTextFill(Color.BLACK);
    }

    /**
     * 匹配excel分组与文件夹下文件
     */
    public static int matchGroupData(List<FileNumBean> fileNumBeans, List<File> inFileList, FileConfig fileConfig) {
        List<String> paths = new ArrayList<>();
        inFileList.forEach(file -> paths.add(file.getPath()));
        List<FileNumBean> fileNumList = buildNameGroupData(paths, fileConfig);
        AtomicInteger imgNum = new AtomicInteger();
        fileNumBeans.forEach(bean1 -> {
            bean1.setGroupNumber(0);
            bean1.setFileName("");
            Optional<FileNumBean> matchedBeans = fileNumList.stream()
                    .filter(bean2 -> bean2.getGroupName().equals(bean1.getGroupName()))
                    .findFirst();
            matchedBeans.ifPresent(matched -> {
                bean1.setFileName(matched.getFileName());
                bean1.setGroupNumber(matched.getGroupNumber());
                bean1.setFileNameList(matched.getFileNameList());
                bean1.setFilePathList(matched.getFilePathList());
                imgNum.addAndGet(matched.getFilePathList().size());
            });
        });
        return imgNum.get();
    }

    /**
     * 分组组装javafx列表数据
     */
    private static List<FileNumBean> buildNameGroupData(List<String> paths, FileConfig fileConfig) {
        List<FileNumBean> fileNumBeans = new ArrayList<>();
        Map<String, List<String>> sortedByKey = getSortedByMap(paths, fileConfig.getSubCode(), fileConfig.getMaxImgNum());
        sortedByKey.forEach((k, v) -> {
            FileNumBean fileNumBean = new FileNumBean();
            fileNumBean.setGroupName(k);
            List<String> names = new ArrayList<>();
            v.forEach(p -> {
                try {
                    String fileName;
                    File file = new File(p);
                    if (fileConfig.isShowFileType()) {
                        fileName = file.getName();
                    } else {
                        fileName = getFileName(file);
                    }
                    names.add(fileName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            fileNumBean.setFileNameList(names);
            fileNumBean.setFileName(String.join("、", names));
            fileNumBean.setGroupNumber(v.size());
            fileNumBean.setFilePathList(v);
            fileNumBeans.add(fileNumBean);
        });
        return fileNumBeans;
    }

    /**
     * 为统计文件名和插入图片页面添加鼠标悬停提示
     */
    public static void addNumImgToolTip(CheckBox recursion, TextField subCode, TextField excelName, TextField sheetOutName, TextField maxRow) {
        addToolTip(recursion, "勾选后将会查询文件夹中的文件夹里的文件");
        addToolTip(sheetOutName, "须填与excel模板相同的表名才能正常统计");
        addToolTip(excelName, "如果导出地址和名称与模板一样则会覆盖模板excel文件");
        addToolTip(maxRow, "只能填数字，不填默认不限制，会读取到有数据的最后一行，最小值为1");
        addToolTip(subCode, "填写后会按所填写的字符串来分割文件名称，按照分割后的文件名称左侧字符串进行分组");
    }

    /**
     * 为统计文件名和插入图片页面列表设置字段宽度
     */
    public static Task<List<FileNumBean>> tableViewNumImgAdaption(Task<List<FileNumBean>> readExcelTask,
                                                                  TableColumn<FileNumBean, String> groupId,
                                                                  TableView<FileNumBean> tableView,
                                                                  DoubleProperty groupName, DoubleProperty groupNumber,
                                                                  TableColumn<FileNumBean, String> fileName) {
        groupId.prefWidthProperty().bind(tableView.widthProperty().multiply(0.1));
        groupName.bind(tableView.widthProperty().multiply(0.1));
        groupNumber.bind(tableView.widthProperty().multiply(0.1));
        fileName.prefWidthProperty().bind(tableView.widthProperty().multiply(0.7));
        return readExcelTask;
    }

    /**
     * 动态更新重命名分隔符设置下拉框
     */
    public static void updateSelectItems(CheckBox checkBox, ChoiceBox<String> choiceBox, SelectItemsEnums selectItemsEnums) {
        if (checkBox.isSelected()) {
            choiceBox.setItems(selectItemsEnums.getItems(0));
            choiceBox.setValue(selectItemsEnums.getSelectItemsEnum(0).getValue(0));
        } else {
            choiceBox.setItems(selectItemsEnums.getItems(1));
            choiceBox.setValue(selectItemsEnums.getSelectItemsEnum(1).getValue(0));
        }
    }

    /**
     * 分组匹配数
     */
    public static void machGroup(FileConfig fileConfig, ObservableList<FileNumBean> fileNumList, List<File> inFileList,
                                 TableView<FileNumBean> tableViewImg, String tabId, Label fileNumberImg) throws Exception {
        int imgNum = matchGroupData(fileNumList, inFileList, fileConfig);
        TaskBean<FileNumBean> taskBean = new TaskBean<>();
        taskBean.setTableView(tableViewImg)
                .setTabId(tabId);
        showReadExcelData(fileNumList, taskBean);
        fileNumberImg.setText("共有 " + fileNumList.size() + " 组数据，匹配到 " + imgNum + " 张图片");
    }

}
