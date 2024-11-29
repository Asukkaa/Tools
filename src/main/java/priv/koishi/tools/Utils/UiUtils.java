package priv.koishi.tools.Utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.PopupWindow;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import priv.koishi.tools.Bean.FileBean;
import priv.koishi.tools.Bean.FileNumBean;
import priv.koishi.tools.Bean.TaskBean;
import priv.koishi.tools.Configuration.FileConfig;
import priv.koishi.tools.Enum.SelectItemsEnums;
import priv.koishi.tools.MessageBubble.MessageBubble;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static priv.koishi.tools.Service.ReadDataService.showReadExcelData;
import static priv.koishi.tools.Text.CommonTexts.*;
import static priv.koishi.tools.Utils.CommonUtils.*;
import static priv.koishi.tools.Utils.FileUtils.*;

/**
 * @author KOISHI
 * Date:2024-10-03
 * Time:下午1:38
 */
public class UiUtils {

    private static final DataFormat dataFormat = new DataFormat("application/x-java-serialized-object");

    private static final Logger logger = LogManager.getLogger(UiUtils.class);

    /**
     * 鼠标停留提示框
     *
     * @param control 需要显示提示框的组件
     * @param tip     提示卡信息
     */
    public static void addToolTip(Control control, String tip) {
        control.setTooltip(setTooltipConfig(tip));
    }

    /**
     * 设置鼠标停留提示框参数
     */
    public static Tooltip setTooltipConfig(String tip) {
        Tooltip tooltip = new Tooltip(tip);
        tooltip.setWrapText(true);
        tooltip.setShowDuration(showDuration);
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
        return tooltip;
    }

    /**
     * 输入框鼠标停留提示输入值
     */
    public static void addValueToolTip(TextField textField, String text) {
        String value = textField.getText();
        if (StringUtils.isNotEmpty(text)) {
            if (StringUtils.isNotEmpty(value)) {
                addToolTip(textField, text + "\n" + value);
            } else {
                addToolTip(textField, text);
            }
        } else {
            if (StringUtils.isNotEmpty(value)) {
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
            fileChooser.setInitialDirectory(new File(System.getProperty(userHome)));
        } else {
            File file = new File(path);
            // 设置初始目录
            if (file.isDirectory()) {
                fileChooser.setInitialDirectory(file);
            }
            if (file.isFile()) {
                file = new File(file.getParent());
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
            directoryChooser.setInitialDirectory(new File(System.getProperty(userHome)));
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
                            setTooltip(setTooltipConfig(item.toString()));
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
        //自定义比较器
        Comparator<String> customComparator = Comparator.comparingDouble(FileUtils::fileSizeCompareValue);
        //应用自定义比较器
        sizeColumn.setComparator(customComparator);
    }

    /**
     * 校验excel设置参数
     */
    public static void checkExcelParam(String excelInPath) throws Exception {
        File file = new File(excelInPath);
        if (!file.exists() || !file.isFile()) {
            throw new Exception(text_excelNotExists);
        }
        String fileExtension = getFileType(file);
        if (!xlsx.equals(fileExtension)) {
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
        logger.error(ex, ex);
        Alert alert = creatErrorDialog(errToString(ex));
        if (ex.getCause().getCause() instanceof Exception) {
            alert.setHeaderText(ex.getCause().getCause().getMessage());
        } else {
            alert.setHeaderText(ex.getMessage());
        }
        // 展示弹窗
        alert.showAndWait();
    }

    /**
     * 创建一个错误弹窗
     */
    public static Alert creatErrorDialog(String errString) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("异常信息");
        // 创建展示异常信息的TextArea
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(errString);
        // 创建VBox并添加TextArea
        VBox details = new VBox();
        details.heightProperty().addListener((v1, v2, v3) -> Platform.runLater(() -> textArea.setPrefHeight(details.getHeight())));
        details.getChildren().add(textArea);
        alert.getDialogPane().setExpandableContent(details);
        return alert;
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
        ObservableList<FileNumBean> nullData = FXCollections.observableArrayList(new ArrayList<>());
        tableView.setItems(nullData);
        updateLabel(fileNumber, text_dataListNull);
        updateLabel(log, "");
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
    public static void addNumImgToolTip(CheckBox recursion, TextField subCode, TextField excelName, TextField sheetName, TextField maxRow) {
        addToolTip(recursion, tip_recursion);
        addToolTip(sheetName, tip_sheetName);
        addToolTip(excelName, tip_excelName);
        addToolTip(maxRow, tip_maxRow);
        addToolTip(subCode, tip_subCode);
    }

    /**
     * 为统计文件名和插入图片页面列表设置字段宽度
     */
    public static void tableViewNumImgAdaption(TableColumn<FileNumBean, String> groupId, TableView<FileNumBean> tableView,
                                               DoubleProperty groupName, DoubleProperty groupNumber,
                                               TableColumn<FileNumBean, String> fileName) {
        groupId.prefWidthProperty().bind(tableView.widthProperty().multiply(0.1));
        groupName.bind(tableView.widthProperty().multiply(0.1));
        groupNumber.bind(tableView.widthProperty().multiply(0.1));
        fileName.prefWidthProperty().bind(tableView.widthProperty().multiply(0.7));
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
        fileNumberImg.setText(text_allHave + fileNumList.size() + text_group + imgNum + text_picture);
    }

    /**
     * 限制输入框只能输入指定范围内的整数
     */
    public static void integerRangeTextField(TextField textField, Integer min, Integer max, String tip) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            // 这里处理文本变化的逻辑
            if (!isInIntegerRange(newValue, min, max) && StringUtils.isNotBlank(newValue)) {
                textField.setText(oldValue);
            }
            addValueToolTip(textField, tip);
        });
    }

    /**
     * 监听输入框内容变化
     */
    public static void textFieldValueListener(TextField textField, String tip) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> addValueToolTip(textField, tip));
    }

    /**
     * 修改label信息
     */
    public static void updateLabel(Label label, String text) {
        label.textProperty().unbind();
        label.setText(text);
        label.setTextFill(Color.BLACK);
    }

    /**
     * 更新所选文件路径显示
     */
    public static String updatePathLabel(String selectedFilePath, String filePath, String pathKey, Label pathLabel, String configFile, AnchorPane anchorPane) throws IOException {
        //只有跟上次选的路径不一样才更新
        if (!filePath.equals(selectedFilePath)) {
            updateProperties(configFile, pathKey, selectedFilePath);
            filePath = selectedFilePath;
        }
        setPathLabel(pathLabel, selectedFilePath, false, anchorPane);
        return filePath;
    }

    /**
     * 设置列表通过拖拽排序行
     */
    public static <T> void tableViewDragRow(TableView<T> tableView) {
        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            //拖拽-检测
            row.setOnDragDetected(e -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(dataFormat, index);
                    db.setContent(cc);
                    e.consume();
                }
            });
            //释放-验证
            row.setOnDragOver(e -> {
                Dragboard db = e.getDragboard();
                if (db.hasContent(dataFormat)) {
                    if (row.getIndex() != (Integer) db.getContent(dataFormat)) {
                        e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        e.consume();
                    }
                }
            });
            //释放-执行
            row.setOnDragDropped(e -> {
                Dragboard db = e.getDragboard();
                if (db.hasContent(dataFormat)) {
                    int draggedIndex = (Integer) db.getContent(dataFormat);
                    int dropIndex;
                    if (row.isEmpty()) {
                        dropIndex = tableView.getItems().size();
                    } else {
                        dropIndex = row.getIndex();
                    }
                    tableView.getItems().add(dropIndex, tableView.getItems().remove(draggedIndex));
                    e.setDropCompleted(true);
                    tableView.getSelectionModel().select(dropIndex);
                    e.consume();
                }
            });
            return row;
        });
    }

    /**
     * 构建右键菜单
     */
    public static void tableViewContextMenu(TableView<FileBean> tableView, Label label) {
        //设置可以选中多行
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //添加右键菜单
        ContextMenu contextMenu = new ContextMenu();
        //所选行上移一行选项
        buildUpMoveDataMenuItem(tableView, contextMenu);
        //所选行下移一行选项
        buildDownMoveDataMenuItem(tableView, contextMenu);
        //打开所选文件选项
        buildOpenFileMenuItem(tableView, contextMenu);
        //打开所选文件所在文件夹选项
        buildOpenDirectorMenuItem(tableView, contextMenu);
        //删除所选数据选项
        buildDeleteDataMenuItem(tableView, label, contextMenu);
        tableView.setContextMenu(contextMenu);
        tableView.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * 所选行上移一行选项
     */
    private static void buildUpMoveDataMenuItem(TableView<FileBean> tableView, ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem("所选行上移一行");
        menuItem.setOnAction(event -> {
            var selectedCells = tableView.getSelectionModel().getSelectedCells();
            for (int i = 0; i < selectedCells.size(); i++) {
                int row = selectedCells.get(i).getRow();
                List<FileBean> fileList = tableView.getItems();
                if (row - i > 0) {
                    fileList.add(row, fileList.remove(row - 1));
                }
            }
        });
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 所选行下移一行选项
     */
    private static void buildDownMoveDataMenuItem(TableView<FileBean> tableView, ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem("所选行下移一行");
        menuItem.setOnAction(event -> {
            for (var position : tableView.getSelectionModel().getSelectedCells()) {
                int row = position.getRow();
                List<FileBean> fileList = tableView.getItems();
                if (row < fileList.size() - 1) {
                    fileList.add(row, fileList.remove(row + 1));
                }
            }
        });
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 打开所选文件所在文件夹选项
     */
    private static void buildOpenDirectorMenuItem(TableView<FileBean> tableView, ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem("打开所选文件所在文件夹");
        menuItem.setOnAction(event -> {
            List<FileBean> fileBeans = tableView.getSelectionModel().getSelectedItems();
            List<String> pathList = fileBeans.stream().map(FileBean::getPath).distinct().toList();
            pathList.forEach(path -> {
                try {
                    openFile(new File(path).getParent());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 打开所选文件选项
     */
    private static void buildOpenFileMenuItem(TableView<FileBean> tableView, ContextMenu contextMenu) {
        MenuItem menuItem = new MenuItem("打开所选文件");
        menuItem.setOnAction(event -> {
            List<FileBean> fileBeans = tableView.getSelectionModel().getSelectedItems();
            fileBeans.forEach(fileBean -> {
                try {
                    openFile(fileBean.getPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 删除所选数据选项
     */
    private static void buildDeleteDataMenuItem(TableView<FileBean> tableView, Label label, ContextMenu contextMenu) {
        MenuItem deleteDataMenuItem = new MenuItem("删除所选数据");
        deleteDataMenuItem.setOnAction(event -> {
            List<FileBean> fileBeans = tableView.getSelectionModel().getSelectedItems();
            ObservableList<FileBean> items = tableView.getItems();
            items.removeAll(fileBeans);
            label.setText(text_allHave + items.size() + text_file);
        });
        contextMenu.getItems().add(deleteDataMenuItem);
    }

    /**
     * 改变要防重复点击的组件状态
     */
    public static void changeDisableControls(TaskBean<?> taskBean, boolean disable) {
        List<Control> disableControls = taskBean.getDisableControls();
        if (CollectionUtils.isNotEmpty(disableControls)) {
            disableControls.forEach(dc -> dc.setDisable(disable));
        }
    }

    /**
     * 为配置组件设置上次配置值
     */
    @SuppressWarnings("unchecked")
    public static void setControlLastConfig(Control control, Properties prop, String Key, boolean canBlank, AnchorPane anchorPane) {
        String lastValue = prop.getProperty(Key);
        if (StringUtils.isNotBlank(lastValue)) {
            if (control instanceof ChoiceBox) {
                ChoiceBox<String> choiceBox = (ChoiceBox<String>) control;
                choiceBox.setValue(lastValue);
            }
            if (control instanceof CheckBox checkBox) {
                checkBox.setSelected(activation.equals(lastValue));
            }
            if (control instanceof Label label) {
                if (isValidPath(lastValue)) {
                    setPathLabel(label, lastValue, false, anchorPane);
                } else {
                    label.setText(lastValue);
                }
            }
            if (control instanceof TextField textField) {
                textField.setText(lastValue);
            }
        }
        if (StringUtils.isNotEmpty(lastValue) && canBlank) {
            if (control instanceof TextField textField) {
                textField.setText(lastValue);
            }
        }
    }

    /**
     * 显示可打开的文件类路径
     */
    public static void setPathLabel(Label pathLabel, String path, boolean openFile, AnchorPane anchorPane) {
        pathLabel.setText(path);
        pathLabel.getStyleClass().add("label-button-style");
        File file = new File(path);
        String openPath;
        //判断是否打开文件
        if (!openFile && file.isFile()) {
            openPath = file.getParent();
        } else {
            openPath = path;
        }
        pathLabel.setOnMouseClicked(event -> {
            //只接受左键点击
            if (event.getButton() == MouseButton.PRIMARY) {
                try {
                    if (!file.exists()) {
                        throw new IOException(text_fileNotExists);
                    }
                    openFile(openPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        addToolTip(pathLabel, path + "\n鼠标左键点击打开 " + openPath);
        //设置右键菜单
        setPathLabelContextMenu(pathLabel, anchorPane);
    }

    /**
     * 给路径Label设置右键菜单
     */
    public static void setPathLabelContextMenu(Label valueLabel, AnchorPane anchorPane) {
        String path = valueLabel.getText();
        File file = new File(path);
        ContextMenu contextMenu = new ContextMenu();
        MenuItem openDirectoryMenuItem = new MenuItem("打开文件夹");
        openDirectoryMenuItem.setOnAction(event -> {
            try {
                if (!file.exists()) {
                    throw new IOException(text_fileNotExists);
                }
                openFile(file.getParent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        contextMenu.getItems().add(openDirectoryMenuItem);
        if (file.isFile()) {
            MenuItem openFileMenuItem = new MenuItem("打开文件");
            openFileMenuItem.setOnAction(event -> {
                try {
                    if (!file.exists()) {
                        throw new IOException(text_fileNotExists);
                    }
                    openFile(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            contextMenu.getItems().add(openFileMenuItem);
        }
        MenuItem copyValueMenuItem = new MenuItem("复制路径");
        contextMenu.getItems().add(copyValueMenuItem);
        valueLabel.setContextMenu(contextMenu);
        copyValueMenuItem.setOnAction(event -> copyText(valueLabel, anchorPane));
        valueLabel.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(valueLabel, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * 添加复制label值右键菜单
     */
    public static void setCopyValueContextMenu(Label valueLabel, String text, AnchorPane anchorPane) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyValueMenuItem = new MenuItem(text);
        contextMenu.getItems().add(copyValueMenuItem);
        valueLabel.setContextMenu(contextMenu);
        valueLabel.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(valueLabel, event.getScreenX(), event.getScreenY());
            }
        });
        //设置右键菜单行为
        copyValueMenuItem.setOnAction(event -> copyText(valueLabel, anchorPane));
    }

    /**
     * 复制文本
     */
    public static void copyText(Label valueLabel, AnchorPane anchorPane) {
        //获取当前系统剪贴板
        Clipboard clipboard = Clipboard.getSystemClipboard();
        //创建剪贴板内容对象
        ClipboardContent content = new ClipboardContent();
        //将文本区域选中的文本放入剪贴板内容中
        content.putString(valueLabel.getText());
        //设置剪贴板内容
        clipboard.setContent(content);
        //复制成功消息气泡
        buildMessageBubble(anchorPane, text_copySuccess, 1);
    }

    /**
     * 创建消息弹窗
     */
    public static void buildMessageBubble(AnchorPane anchorPane, String text, double time) {
        MessageBubble bubble = new MessageBubble(text);
        anchorPane.getChildren().add(bubble);
        anchorPane.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEvent -> {
            // 获取鼠标位置
            double mouseX = mouseEvent.getX();
            double mouseY = mouseEvent.getY();
            bubble.setLayoutX(mouseX + 30);
            bubble.setLayoutY(mouseY);
        });
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(time), ae -> anchorPane.getChildren().remove(bubble));
        Timeline timeline = new Timeline(keyFrame);
        timeline.play();
    }

    /**
     * 设置要暂时移除的组件
     */
    public static void removeChildren(VBox vBox, VBox... vBoxes) {
        vBox.getChildren().removeAll(vBoxes);
    }

}
